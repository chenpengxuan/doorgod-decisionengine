/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;
import static com.ymatou.doorgod.decisionengine.constants.Constants.MONGO_UNION;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getBlackListMapName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.po.OffenderPo;
import com.ymatou.doorgod.decisionengine.repository.OffenderRepository;
import com.ymatou.doorgod.decisionengine.repository.SampleUnionRepository;
import com.ymatou.doorgod.decisionengine.util.RedisHelper;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * 
 * @author qianmin 2016年9月12日 上午11:05:19
 * 
 */
@Component
public class RulePersistence implements Job {

    private static final Logger logger = LoggerFactory.getLogger(RulePersistence.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime now = LocalDateTime.now();
        logger.info("begin to presist to mongo. {}", now.format(FORMATTER_YMDHMS));
        for (LimitTimesRule rule : RuleHolder.rules.values()) {
            persistUnionSample(rule, now);
            persistOffender(rule);
        }
        logger.info("end to presist to mongo. {}", now.format(FORMATTER_YMDHMS));
    }

    public void persistUnionSample(LimitTimesRule rule, LocalDateTime now) {
        StringRedisTemplate redisTemplate = SpringContextHolder.getBean(StringRedisTemplate.class);
        RuleExecutor ruleExecutor = SpringContextHolder.getBean(RuleExecutor.class);
        String currentBucket = RedisHelper.getUnionSetName(rule.getName(), now.format(FORMATTER_YMDHMS), MONGO_UNION);
        List<String> timeBuckets = ruleExecutor.getAllTimeBucket(rule, now);
        String firstTimeBucket = timeBuckets.get(0);
        timeBuckets.remove(0);
        long count = redisTemplate.opsForZSet().unionAndStore(firstTimeBucket, timeBuckets, currentBucket);

        SampleUnionRepository sampleUnionRepository = SpringContextHolder.getBean(SampleUnionRepository.class);
        Set<TypedTuple<String>> sampleUnion =
                redisTemplate.opsForZSet().rangeByScoreWithScores(currentBucket, 1, Double.MAX_VALUE);

        sampleUnionRepository.save(sampleUnion);

        logger.info("persist union sample size: {}, rule: {}", count, rule.getName());
    }

    public void persistOffender(LimitTimesRule rule) {
        // 持久化Offender
        StringRedisTemplate redisTemplate = SpringContextHolder.getBean(StringRedisTemplate.class);
        OffenderRepository offenderRepository = SpringContextHolder.getBean(OffenderRepository.class);
        Set<TypedTuple<String>> blackList =
                redisTemplate.opsForZSet().rangeWithScores(getBlackListMapName(rule.getName()), 0, -1);
        if (blackList != null && blackList.isEmpty()) {
            List<OffenderPo> offenderPos = new ArrayList<>();
            for (TypedTuple<String> typeTuple : blackList) {
                OffenderPo offenderPo = new OffenderPo();
                offenderPo.setSample(typeTuple.getValue());
                offenderPo.setTime(String.valueOf(typeTuple.getScore()));
                offenderPos.add(offenderPo);
            }
            offenderRepository.save(offenderPos);
        }

        // 删除到期的Offender
        redisTemplate.opsForZSet().removeRangeByScore(getBlackListMapName(rule.getName()), 0,
                Double.valueOf(LocalDateTime.now().format(FORMATTER_YMDHMS)));
        logger.info("persist offender size: {}, rule: {}", blackList.size(), rule.getName());
    }

}
