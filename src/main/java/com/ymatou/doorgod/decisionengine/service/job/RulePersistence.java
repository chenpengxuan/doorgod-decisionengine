/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;

import java.time.LocalDateTime;
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
import com.ymatou.doorgod.decisionengine.repository.SampleUnionRepository;
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
        StringRedisTemplate redisTemplate = SpringContextHolder.getBean(StringRedisTemplate.class);
        RuleExecutor ruleExecutor = SpringContextHolder.getBean(RuleExecutor.class);
        SampleUnionRepository sampleUnionRepository = SpringContextHolder.getBean(SampleUnionRepository.class);

        LocalDateTime now = LocalDateTime.now();
        logger.info("begin to save sample union to mongo. {}", now.format(FORMATTER_YMDHMS));
        for (LimitTimesRule rule : RuleHolder.rules.values()) {
            String currentBucket = new StringBuilder(rule.getName()).append(":").append("set").append(":")
                    .append(now.format(FORMATTER_YMDHMS)).append(":MongoDB").toString();
            List<String> timeBuckets = ruleExecutor.getAllTimeBucket(rule, now);
            long count =
                    redisTemplate.opsForZSet().unionAndStore(timeBuckets.get(0), timeBuckets.remove(0), currentBucket);
            System.out.println(count);
            Set<TypedTuple<String>> sampleUnion =
                    redisTemplate.opsForZSet().rangeByScoreWithScores(currentBucket, 1, Double.MAX_VALUE);

            sampleUnionRepository.save(sampleUnion);
        }
        logger.info("end to save {} sample union to mongo.", now.format(FORMATTER_YMDHMS));
    }


}
