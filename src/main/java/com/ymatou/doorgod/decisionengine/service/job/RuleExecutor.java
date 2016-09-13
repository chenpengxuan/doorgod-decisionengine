/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import static com.ymatou.doorgod.decisionengine.constants.Constants.BLACK_LIST_CHANNEL;
import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;
import static com.ymatou.doorgod.decisionengine.constants.Constants.UNION;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.ymatou.doorgod.decisionengine.util.RedisHelper;
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

        String previousBucket = RedisHelper.getUnionSetName(ruleName,
                now.minusSeconds(1).format(FORMATTER_YMDHMS), UNION);
        String currentBucket = RedisHelper.getUnionSetName(ruleName,
                now.format(FORMATTER_YMDHMS), UNION);
        String secondWillDelete = RedisHelper.getNormalSetName(rule.getName(),
                now.minusSeconds(1).format(FORMATTER_YMDHMS));
        String secondWillAdd = RedisHelper.getNormalSetName(rule.getName(),
                now.minusSeconds(rule.getStatisticSpan() + 1).format(FORMATTER_YMDHMS));

        logger.debug("begin to execute rule: {}, time: {}", ruleName, now.format(FORMATTER_YMDHMS));
        if (redisTemplate.opsForZSet().size(previousBucket) > 0) {
            redisTemplate.opsForZSet().scan(secondWillDelete, ScanOptions.scanOptions().build())
                    .forEachRemaining(c -> {
                        redisTemplate.opsForZSet().add(secondWillDelete, c.getValue(), c.getScore() * -1);
                    });
            redisTemplate.opsForZSet().unionAndStore(previousBucket,
                    Arrays.asList(secondWillAdd, secondWillDelete), currentBucket);
        } else {
            redisTemplate.opsForZSet().unionAndStore(timeBuckets.get(0), timeBuckets.remove(0), currentBucket);
        }

        Set<String> blacklist = redisTemplate.opsForZSet()
                .rangeByScore(currentBucket, rule.getTimesCap(), Integer.MAX_VALUE);
        if (!blacklist.isEmpty()) {
            redisTemplate.opsForZSet().removeRangeByScore(currentBucket, rule.getTimesCap(), Double.MAX_VALUE);
            redisTemplate.convertAndSend(BLACK_LIST_CHANNEL, blacklist);
            logger.info("got blacklist: {}, time: {}", JSON.toJSONString(blacklist));
        }
        logger.debug("end to execute rule: {}, time: {}", ruleName, now.format(FORMATTER_YMDHMS));
    }

    public List<String> getAllTimeBucket(LimitTimesRule rule, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();

        // RuleName:Set:yyyyMMddHHmmss
        for (int second = 1; second <= rule.getStatisticSpan(); second++) {
            timeBuckets.add(RedisHelper.getNormalSetName(rule.getName(),
                    now.minusSeconds(second).format(FORMATTER_YMDHMS)));
        }

        return timeBuckets;
    }


    public static void main(String[] args) {
        LimitTimesRule rule = new LimitTimesRule();
        rule.setStatisticSpan(60);
        rule.setName("rule");
        RuleExecutor ruleExecutor = new RuleExecutor();


        List<String> times = ruleExecutor.getAllTimeBucket(rule, LocalDateTime.now());
        times.stream().forEach(c -> System.out.println(c));
    }
}
