/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import static com.ymatou.doorgod.decisionengine.constants.Constants.BLACK_LIST_CHANNEL;
import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;
import static com.ymatou.doorgod.decisionengine.constants.Constants.UNION;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getBlackListMapName;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getNormalSetName;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getUnionSetName;

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
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * 
 * @author qianmin 2016年9月12日 上午11:04:36
 * 
 */
@Component
public class RuleExecutor implements Job {

    private static final Logger logger = LoggerFactory.getLogger(RuleExecutor.class);

    private static int i = 0;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        StringRedisTemplate redisTemplate =
                SpringContextHolder.getBean(StringRedisTemplate.class);

        String jobName = context.getJobDetail().getKey().getName();
        LimitTimesRule rule = RuleHolder.rules.get(jobName);
        LocalDateTime now = LocalDateTime.now();
        // LocalDateTime now = LocalDateTime.of(2016, 9, 14, 15, 58, 8).plusSeconds(i++);
        List<String> timeBuckets = getAllTimeBucket(rule, now);
        String ruleName = rule.getName();

        String previousBucket = getUnionSetName(ruleName,
                now.minusSeconds(1).format(FORMATTER_YMDHMS), UNION);
        String currentBucket = getUnionSetName(ruleName, now.format(FORMATTER_YMDHMS), UNION);
        String secondWillAdd = getNormalSetName(rule.getName(),
                now.minusSeconds(1).format(FORMATTER_YMDHMS));
        String secondWillDelete = getNormalSetName(rule.getName(),
                now.minusSeconds(rule.getStatisticSpan() + 1).format(FORMATTER_YMDHMS));

        logger.info("begin to execute rule: {}, time: {}", ruleName, now.format(FORMATTER_YMDHMS));
        if (redisTemplate.opsForZSet().size(previousBucket) > 0) {
            redisTemplate.opsForZSet().scan(secondWillDelete, ScanOptions.scanOptions().build())
                    .forEachRemaining(c -> {
                        redisTemplate.opsForZSet()
                                .incrementScore(previousBucket, c.getValue(), c.getScore() * -1);
                    });
            redisTemplate.opsForZSet().unionAndStore(previousBucket, secondWillAdd, currentBucket);
            redisTemplate.opsForSet().getOperations().expire(currentBucket, getExpireByRule(rule),
                    TimeUnit.SECONDS);
            System.out.println("union start and end");
        } else {
            // redisTemplate.opsForZSet().unionAndStore(EMPTY_SET, timeBuckets, currentBucket);
            redisTemplate.opsForZSet().unionAndStore(timeBuckets.get(0), timeBuckets,
                    currentBucket);
            redisTemplate.opsForSet().getOperations().expire(currentBucket, getExpireByRule(rule),
                    TimeUnit.SECONDS);
            System.out.println("union all");
        }

        Set<String> blacklist = redisTemplate.opsForZSet()
                .rangeByScore(currentBucket, rule.getTimesCap(), Integer.MAX_VALUE);
        if (!blacklist.isEmpty()) {
            redisTemplate.opsForZSet().removeRangeByScore(currentBucket, rule.getTimesCap(), Double.MAX_VALUE);
            String rejectTime = now.plusSeconds(rule.getRejectionSpan()).format(FORMATTER_YMDHMS);
            boolean isBlackListChanged = false;
            for (String black : blacklist) {
                // if (redisTemplate.opsForHash().putIfAbsent(getBlackListMapName(ruleName), black,
                // rejectTime)) {
                // isBlackListChanged = true;
                // }
                if (redisTemplate.opsForZSet().add(getBlackListMapName(ruleName), black, Double.valueOf(rejectTime))) {
                    isBlackListChanged = true;
                }
            }
            if (isBlackListChanged) {
                redisTemplate.convertAndSend(BLACK_LIST_CHANNEL, ruleName);
                logger.info("got blacklist: {}, time: {}", JSON.toJSONString(blacklist));
            }
        }
        logger.debug("end to execute rule: {}, time: {}", ruleName, now.format(FORMATTER_YMDHMS));
    }

    public List<String> getAllTimeBucket(LimitTimesRule rule, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();
        for (int second = rule.getStatisticSpan(); second >= 1; second--) {
            timeBuckets.add(getNormalSetName(rule.getName(), now.minusSeconds(second).format(FORMATTER_YMDHMS))); // RuleName:Set:yyyyMMddHHmmss
        }

        return timeBuckets;
    }

    private long getExpireByRule(LimitTimesRule rule) {
        if (rule.getTimesCap() < 60) {
            return ((Double) (rule.getStatisticSpan() * 1.5)).longValue();
        }
        return ((Double) (rule.getStatisticSpan() * 1.2)).longValue();
    }
}
