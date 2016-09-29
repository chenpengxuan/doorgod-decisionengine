/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration.store;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.ymatou.doorgod.decisionengine.holder.SampleStatisticCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.util.RedisHelper;

/**
 * @author luoshiqian 2016/9/14 16:01
 */
@Component("redisSampleStore")
public class RedisSampleStore extends AbstractSampleStore {

    private static final Logger logger = LoggerFactory.getLogger(RedisSampleStore.class);
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Collection<LimitTimesRule> findRule() {
        return RuleHolder.rules.values().stream()
                .filter(rule -> CollectionUtils.isEmpty(rule.getGroupByKeys())).collect(Collectors.toSet());
    }

    @Override
    protected Map<String, Map<String, Map<Sample, AtomicInteger>>> getMemoryMap() {
        return SampleStatisticCenter.ruleTimeSampleMaps;
    }

    @Override
    protected int getTopN() {
        return bizProps.getUploadRedisTopN();
    }

    @Override
    public void uploadSampleToDb(LimitTimesRule rule, String uploadTime,
            Collection<Map.Entry<Sample, Object>> samples) {
        // 获取redis zset name
        String zSetName = RedisHelper.getNormalSetName(rule.getName(), uploadTime);

        samples.forEach(entry -> {
            try {
                double score = 1;
                if (redisTemplate.opsForZSet().getOperations().hasKey(zSetName)) {
                    score = redisTemplate.opsForZSet().incrementScore(zSetName, entry.getKey().toString(),
                            ((AtomicInteger) entry.getValue()).doubleValue());
                } else {
                    redisTemplate.opsForZSet().add(zSetName, entry.getKey().toString(),
                            ((AtomicInteger) entry.getValue()).doubleValue());
                    redisTemplate.opsForZSet().getOperations().expire(zSetName, getExpireByRule(rule), TimeUnit.SECONDS);// 单位秒
                }

                logger.debug("ruleName:{},zsetName:{},zsetsample:{},score:{}", rule.getName(),
                        zSetName, entry.getKey().toString(), score);
            } catch (Exception e) {
                logger.error("uploadSample to redis error",e);
            }
        });

    }

    /**
     * 获取规则的过期时间 小于60秒 系数为 1.5 大于60秒 系数为 1.2
     * 
     * @param rule
     * @return
     */
    private long getExpireByRule(LimitTimesRule rule) {
        if (rule.getTimesCap() < 60) {
            return ((Double) (rule.getStatisticSpan() * 1.5)).longValue();
        }
        return ((Double) (rule.getStatisticSpan() * 1.2)).longValue();
    }

}
