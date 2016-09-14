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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        StringRedisTemplate redisTemplate = SpringContextHolder.getBean(StringRedisTemplate.class);

        String jobName = context.getJobDetail().getKey().getName();
        LimitTimesRule rule = RuleHolder.rules.get(jobName);
        LocalDateTime now = LocalDateTime.now();
        List<String> timeBuckets = getAllTimeBucket(rule, now);
        String ruleName = rule.getName();

        String previousBucket = getUnionSetName(ruleName, now.minusSeconds(1).format(FORMATTER_YMDHMS), UNION);
        String currentBucket = getUnionSetName(ruleName, now.format(FORMATTER_YMDHMS), UNION);
        String secondWillDelete = getNormalSetName(rule.getName(), now.minusSeconds(1).format(FORMATTER_YMDHMS));
        String secondWillAdd = getNormalSetName(rule.getName(),
                now.minusSeconds(rule.getStatisticSpan() + 1).format(FORMATTER_YMDHMS));

        logger.debug("begin to execute rule: {}, time: {}", ruleName, now.format(FORMATTER_YMDHMS));
        logger.info(previousBucket + " " + redisTemplate.opsForZSet().size(previousBucket).toString() + " "
                + currentBucket);
        // redisTemplate.opsForZSet().add(timeBuckets.get(0), "init", 0);
        if (redisTemplate.opsForZSet().size(previousBucket) > 0) {
            redisTemplate.opsForZSet().scan(secondWillDelete, ScanOptions.scanOptions().build())
                    .forEachRemaining(c -> {
                        redisTemplate.opsForZSet().add(secondWillDelete, c.getValue(), c.getScore() * -1);
                    });
            long count = redisTemplate.opsForZSet().unionAndStore(previousBucket,
                    Arrays.asList(secondWillAdd, secondWillDelete), currentBucket);
            System.out.println(count);
        } else {
            // System.out.println(timeBuckets.get(0));
            // timeBuckets.stream().forEach(tb -> System.out.println(tb));
            timeBuckets.remove(0);
            long count = redisTemplate.opsForZSet().unionAndStore(timeBuckets.get(0), timeBuckets, currentBucket);
            System.out.println(count);
        }
        logger.info(previousBucket + " " + redisTemplate.opsForZSet().size(currentBucket).toString() + " "
                + currentBucket);
        Set<String> blacklist = redisTemplate.opsForZSet()
                .rangeByScore(currentBucket, rule.getTimesCap(), Integer.MAX_VALUE);
        if (!blacklist.isEmpty()) {
            redisTemplate.opsForZSet().removeRangeByScore(currentBucket, rule.getTimesCap(), Double.MAX_VALUE);

            String rejectTime = now.plusSeconds(rule.getRejectionSpan()).format(FORMATTER_YMDHMS);
            HashMap<String, String> blackListWithTime = new HashMap<>();
            blacklist.forEach(c -> {
                blackListWithTime.put(c, rejectTime);
            });
            redisTemplate.opsForHash().putAll(getBlackListMapName(ruleName), blackListWithTime);
            redisTemplate.convertAndSend(BLACK_LIST_CHANNEL, ruleName);
            logger.info("got blacklist: {}, time: {}", JSON.toJSONString(blacklist));
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
}
