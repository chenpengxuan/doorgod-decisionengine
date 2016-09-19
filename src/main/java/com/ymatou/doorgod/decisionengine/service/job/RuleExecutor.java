/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import static com.ymatou.doorgod.decisionengine.constants.Constants.EMPTY_SET;
import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;
import static com.ymatou.doorgod.decisionengine.constants.Constants.OffENDER_CHANNEL;
import static com.ymatou.doorgod.decisionengine.constants.Constants.PREVIOUS_COUNT;
import static com.ymatou.doorgod.decisionengine.constants.Constants.UNION;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getEmptySetName;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getNormalSetName;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getOffendersMapName;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getUnionSetName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
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

        // 被统计的规则
        String jobName = context.getJobDetail().getKey().getName();
        LimitTimesRule rule = RuleHolder.rules.get(jobName);
        String ruleName = rule.getName();

        // 合并Redis涉及到的时间窗口
        LocalDateTime now = LocalDateTime.now();
        List<String> timeBuckets = getAllTimeBucket(rule, now);
        String previousBucket = getUnionSetName(ruleName, now.minusSeconds(1).format(FORMATTER_YMDHMS), UNION);
        String currentBucket = getUnionSetName(ruleName, now.format(FORMATTER_YMDHMS), UNION);
        String secAdd = getNormalSetName(rule.getName(), now.minusSeconds(1).format(FORMATTER_YMDHMS));
        String secDelete = getNormalSetName(rule.getName(),
                now.minusSeconds(rule.getStatisticSpan() + 1).format(FORMATTER_YMDHMS));
        logger.debug("begin to execute rule:{},time:{},previousBucket:{},currentBucket:{},secAdd:{},secDelete:{}",
                ruleName, now.format(FORMATTER_YMDHMS), previousBucket, currentBucket, secAdd, secDelete);
        try {
            if (Long.valueOf(now.getSecond()) % 2 == 0) {
                Thread.sleep(3500); // TODO REMOVE
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 合并时间窗口
        if (redisTemplate.opsForZSet().size(previousBucket) > 0) { // 前一秒合并的时间窗口不为空
            redisTemplate.opsForZSet().scan(secDelete, ScanOptions.scanOptions().build())
                    .forEachRemaining(c -> {
                        redisTemplate.opsForZSet().incrementScore(previousBucket, c.getValue(), c.getScore() * -1);
                    });
            redisTemplate.opsForZSet().unionAndStore(previousBucket, secAdd, currentBucket);
            redisTemplate.opsForSet().getOperations().expire(currentBucket, getExpireByRule(rule), TimeUnit.SECONDS);
        } else {
            String notEmptyUnionTimeBucket = getNoEmptyUnionTimeBucket(rule, now, redisTemplate);
            if (!StringUtils.isBlank(notEmptyUnionTimeBucket)) { // 存在合并后的时间窗口
                LocalDateTime time = LocalDateTime.parse(notEmptyUnionTimeBucket.split(":")[3], FORMATTER_YMDHMS);
                int seconds = (int) Duration.between(time, now).getSeconds();
                String addTimeBucket = getUnionSetName(ruleName, time.format(FORMATTER_YMDHMS), "UnionAdd");
                String deleteTimeBucket = getUnionSetName(ruleName, time.format(FORMATTER_YMDHMS), "UnionDelete");
                List<String> deleteSecTimeBuckets = getAllDeleteSecTimeBucket(rule, seconds, now);
                List<String> addSecTimeBuckets = getAllAddSecTimeBucket(rule, seconds, now);

                logger.debug(
                        "union any. Now:{}, notEmptyUnionTimeBucket:{}, deleteSecTimeBuckets:{}, addSecTimeBuckets: {}",
                        now.format(FORMATTER_YMDHMS), notEmptyUnionTimeBucket, JSON.toJSONString(deleteSecTimeBuckets),
                        JSON.toJSONString(addSecTimeBuckets));

                redisTemplate.opsForZSet().unionAndStore(getEmptySetName(EMPTY_SET), deleteSecTimeBuckets,
                        deleteTimeBucket); // 合并需要被删除的时间窗口
                redisTemplate.opsForZSet().unionAndStore(getEmptySetName(EMPTY_SET), addSecTimeBuckets,
                        addTimeBucket); // 合并需要被添加的时间窗口
                redisTemplate.opsForZSet().scan(deleteTimeBucket, ScanOptions.scanOptions().build())
                        .forEachRemaining(c -> {
                            redisTemplate.opsForZSet().incrementScore(deleteTimeBucket, c.getValue(),
                                    c.getScore() * -1); // 被删除的score * -1
                        });
                redisTemplate.opsForZSet().unionAndStore(deleteTimeBucket,
                        Arrays.asList(new String[] {notEmptyUnionTimeBucket, addTimeBucket}),
                        currentBucket); // 合并需要被删除的， 被添加的， 已经合并的

                redisTemplate.opsForSet().getOperations().expire(addTimeBucket, 1, TimeUnit.SECONDS);
                redisTemplate.opsForSet().getOperations().expire(deleteTimeBucket, 1, TimeUnit.SECONDS);
                redisTemplate.opsForSet().getOperations().expire(currentBucket, getExpireByRule(rule),
                        TimeUnit.SECONDS);
            } else {// 不存在合并后的时间窗口
                // 合并所有的子时间窗口
                redisTemplate.opsForZSet().unionAndStore(getEmptySetName(EMPTY_SET), timeBuckets, currentBucket);
                redisTemplate.opsForSet().getOperations().expire(currentBucket, getExpireByRule(rule),
                        TimeUnit.SECONDS);
            }
        }

        Set<String> offenders =
                redisTemplate.opsForZSet().rangeByScore(currentBucket, rule.getTimesCap(), Integer.MAX_VALUE);
        if (!offenders.isEmpty()) {
            redisTemplate.opsForZSet().removeRangeByScore(currentBucket, rule.getTimesCap(), Double.MAX_VALUE);
            String rejectTime = now.plusSeconds(rule.getRejectionSpan()).format(FORMATTER_YMDHMS);
            boolean isOffendersChanged = false;
            for (String offender : offenders) {
                if (redisTemplate.opsForZSet().add(getOffendersMapName(ruleName), offender,
                        Double.valueOf(rejectTime))) {
                    isOffendersChanged = true;
                }
            }
            if (isOffendersChanged) {
                redisTemplate.convertAndSend(OffENDER_CHANNEL, ruleName);
                logger.info("got offenders: {}, time: {}", JSON.toJSONString(offenders));
            }
        }
        logger.debug("end to execute rule: {}, time: {}", ruleName, now.format(FORMATTER_YMDHMS));
    }

    private String getNoEmptyUnionTimeBucket(LimitTimesRule rule, LocalDateTime now,
            StringRedisTemplate redisTemplate) {
        List<String> unionTimeBuckets = getPreviousUionTimeBucket(rule, now);
        String notEmptyUnionTimeBucket = null;
        for (String utb : unionTimeBuckets) {
            if (redisTemplate.opsForZSet().size(utb) > 0) {
                notEmptyUnionTimeBucket = utb; // 找到已经合并的时间窗口
                break;
            }
        }

        return notEmptyUnionTimeBucket;
    }

    public List<String> getAllTimeBucket(LimitTimesRule rule, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();
        for (int second = rule.getStatisticSpan(); second >= 1; second--) {
            timeBuckets.add(getNormalSetName(rule.getName(), now.minusSeconds(second).format(FORMATTER_YMDHMS)));
        }
        return timeBuckets;
    }

    private List<String> getPreviousUionTimeBucket(LimitTimesRule rule, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();
        for (int second = 1; second <= PREVIOUS_COUNT; second++) {
            timeBuckets.add(getUnionSetName(rule.getName(), now.minusSeconds(second).format(FORMATTER_YMDHMS), UNION));
        }
        return timeBuckets;
    }

    private List<String> getAllDeleteSecTimeBucket(LimitTimesRule rule, int seconds, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();
        for (int second = rule.getStatisticSpan() + seconds; second > rule.getStatisticSpan(); second--) {
            timeBuckets.add(getNormalSetName(rule.getName(), now.minusSeconds(second).format(FORMATTER_YMDHMS)));
        }
        return timeBuckets;
    }

    private List<String> getAllAddSecTimeBucket(LimitTimesRule rule, int seconds, LocalDateTime now) {
        List<String> timeBuckets = new ArrayList<>();
        for (int second = seconds; second >= 1; second--) {
            timeBuckets.add(getNormalSetName(rule.getName(), now.minusSeconds(second).format(FORMATTER_YMDHMS)));
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
