/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job.persistence;

import static com.ymatou.doorgod.decisionengine.constants.Constants.EMPTY_SET;
import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;
import static com.ymatou.doorgod.decisionengine.constants.Constants.MONGO_UNION;
import static com.ymatou.doorgod.decisionengine.constants.Constants.UNION_FOR_MONGO_PERSISTENCE_EXPIRE_TIME;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getNormalSetName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.mongo.MongoSamplePo;
import com.ymatou.doorgod.decisionengine.repository.MongoSampleRepository;
import com.ymatou.doorgod.decisionengine.util.RedisHelper;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * @author qianmin 2016年9月12日 上午11:05:19
 * 
 */
@Component
public class LimitTimesRuleSampleMongoPersistenceJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(LimitTimesRuleSampleMongoPersistenceJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime now = LocalDateTime.now();

        logger.info("exec job persist");
        for (LimitTimesRule rule : RuleHolder.rules.values()) {
            persistUnionSample(rule, now);
        }
        logger.info("presist redis data to mongodb. {}", now.format(FORMATTER_YMDHMS));
    }

    /**
     * 每一分钟持久化一次访问记录(各个rule的sample)
     * 
     * @param rule
     * @param now
     */
    public void persistUnionSample(LimitTimesRule rule, LocalDateTime now) {
        BizProps bizProps = SpringContextHolder.getBean(BizProps.class);
        StringRedisTemplate redisTemplate = SpringContextHolder.getBean(StringRedisTemplate.class);

        // 合并Redis以备持久化
        String currentBucket = RedisHelper.getUnionSetName(rule.getName(), now.format(FORMATTER_YMDHMS), MONGO_UNION);
        List<String> timeBuckets = getAllTimeBucket(rule, now);
        redisTemplate.opsForZSet().unionAndStore(RedisHelper.getEmptySetName(EMPTY_SET), timeBuckets,
                currentBucket);
        redisTemplate.opsForSet().getOperations().expire(currentBucket, UNION_FOR_MONGO_PERSISTENCE_EXPIRE_TIME,
                TimeUnit.SECONDS);

        // 持久化
        MongoSampleRepository sampleUnionRepository = SpringContextHolder.getBean(MongoSampleRepository.class);
        Set<TypedTuple<String>> sampleUnion =
                redisTemplate.opsForZSet().rangeWithScores(currentBucket, bizProps.getPresistToMongoTopN() * -1, -1);
        if (sampleUnion != null && sampleUnion.size() > 0) {
            List<MongoSamplePo> mongoSamples = new ArrayList<>();
            for (TypedTuple<String> sample : sampleUnion) {
                MongoSamplePo msp = new MongoSamplePo();
                msp.setRuleName(msp.getRuleName());
                msp.setSample(sample.getValue());
                msp.setCount(sample.getScore());
                msp.setTime(now.format(FORMATTER_YMDHMS));
                mongoSamples.add(msp);
            }
            sampleUnionRepository.save(mongoSamples);
            logger.info("persist union sample size: {}, rule: {}", mongoSamples.size(), rule.getName());
        }
    }


    private List<String> getAllTimeBucket(LimitTimesRule rule, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();
        for (int second = rule.getStatisticSpan(); second >= 1; second--) {
            timeBuckets.add(getNormalSetName(rule.getName(), now.minusSeconds(second).format(FORMATTER_YMDHMS)));
        }
        return timeBuckets;
    }
}
