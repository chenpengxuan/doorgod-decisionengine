/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job.offender;

import static com.ymatou.doorgod.decisionengine.constants.Constants.*;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.integration.KafkaClients;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.service.OffenderService;

/**
 * @author qianmin 2016年9月12日 上午11:04:36
 * @author luoshiqian
 * @author tuwenjie
 * 
 */
@Component
public class LimitTimesRuleSampleOffendersExecutor implements Job {

    private static final Logger logger = LoggerFactory.getLogger(LimitTimesRuleSampleOffendersExecutor.class);

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private KafkaClients kafkaClients;
    @Autowired
    private BizProps bizProps;
    @Autowired
    private OffenderService offenderService;

    public void execute(JobExecutionContext context) throws JobExecutionException {

        try {

            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

            // 被统计的规则
            String jobName = context.getJobDetail().getKey().getName();
            LimitTimesRule rule = RuleHolder.rules.get(jobName);
            if(null == rule){
                logger.info("exec offenders job :{} rule==null",jobName);
                return;
            }
            String ruleName = rule.getName();

            logger.info("exec offenders job :{}",ruleName);

            // 合并Redis涉及到的时间窗口
            LocalDateTime now = LocalDateTime.now();
            String nowFormated = now.format(FORMATTER_YMDHMS);

            List<String> timeBuckets = getAllTimeBucket(rule, now);

            String currentUnionName = getUnionSetName(ruleName, now.format(FORMATTER_YMDHMS), UNION);

            logger.info("begin to execute rule:{},time:{},currentUnionName:{}",
                    ruleName, now.format(FORMATTER_YMDHMS), currentUnionName);

            int previousSeconds = bizProps.getPreviousSecondsRedisSkip();
            String previousNSecondsUnionName = getPreviousNSecondsUnionName(rule, now, zSetOps,previousSeconds);//获取指定秒前的union 合集
            if (StringUtils.isNotBlank(previousNSecondsUnionName)) { // 存在N秒前的一个Union结果

                List<String> delSecTimeBuckets = getAllDeleteSecTimeBucket(rule, previousSeconds, now);
                List<String> addSecTimeBuckets = getAllAddSecTimeBucket(rule, previousSeconds, now);

                logger.info(
                        "union any.Now:{}, previousNSecondsUnionName:{}, delSecTimeBuckets:{}, addSecTimeBuckets: {}",
                        now.format(FORMATTER_YMDHMS), previousNSecondsUnionName, JSON.toJSONString(delSecTimeBuckets),
                        JSON.toJSONString(addSecTimeBuckets));

                redisTemplate.execute(new RedisCallback<Long>() {
                    @Override
                    public Long doInRedis(RedisConnection connection) throws DataAccessException {
                        // 合并需要被删除的， 被添加的， 已经合并的
                        int[] weights = new int[delSecTimeBuckets.size() + addSecTimeBuckets.size() + 1];
                        byte[][] setNameBytes = new byte[weights.length][];
                        int i = 0;
                        for ( String setName : delSecTimeBuckets ) {
                            setNameBytes[i] = setName.getBytes();
                            weights[i] = -1;
                            i++;
                        }
                        for ( String setName : addSecTimeBuckets ) {
                            setNameBytes[i] = setName.getBytes();
                            weights[i] = 1;
                            i++;
                        }
                        setNameBytes[i] = previousNSecondsUnionName.getBytes();
                        weights[i] = 1;

                        return connection.zUnionStore(currentUnionName.getBytes(), RedisZSetCommands.Aggregate.SUM,
                                weights, setNameBytes);
                    }
                });


            } else {// 不存在合并后的时间窗口
                // 合并所有的子时间窗口
                zSetOps.unionAndStore(getEmptySetName(EMPTY_SET), timeBuckets, currentUnionName);
            }

            zSetOps.getOperations().expire(currentUnionName, getExpireByRule(rule), TimeUnit.SECONDS);

            // 获取Offender
            Set<String> offenders = zSetOps.rangeByScore(currentUnionName, rule.getTimesCap(), Integer.MAX_VALUE);
            if (!offenders.isEmpty()) {
                zSetOps.removeRangeByScore(currentUnionName, rule.getTimesCap(), Double.MAX_VALUE);
                String releaseDate = now.plusSeconds(rule.getRejectionSpan()).format(FORMATTER_YMDHMS);

                boolean isOffendersChanged = false;
                for (String offender : offenders) {
                    if(offenderService.saveOffender(rule,offender,releaseDate,nowFormated)){
                        isOffendersChanged = true;
                    }
                }
                if (isOffendersChanged) {

                    kafkaClients.sendUpdateOffendersEvent(ruleName);
                    logger.info("got ruleName:{},offenders: {}", ruleName, JSON.toJSONString(offenders));
                }
            }
            logger.info("end to execute rule: {}, time: {}", ruleName, now.format(FORMATTER_YMDHMS));
        } catch (Exception e) {
            logger.error("error execute rule:{}",context.getJobDetail().getKey().getName(),e);
        }
    }

    /**
     * 往前N秒，获取已合并的时间窗口
     *
     * @param rule
     * @param now
     * @param zSetOps
     * @return
     */
    private String getPreviousNSecondsUnionName(LimitTimesRule rule, LocalDateTime now, ZSetOperations<String, String> zSetOps, int previousSeconds) {
        String previousNUnionName = getUnionSetName(rule.getName(), now.minusSeconds(previousSeconds).format(FORMATTER_YMDHMS), UNION);
        if (zSetOps.size(previousNUnionName) > 0) {
            return previousNUnionName;
        }
        return null;
    }

    /**
     * 往前找N秒，找到最近已合并的时间窗口
     * 
     * @param rule
     * @param now
     * @param zSetOps
     * @return
     */
    private String getNoEmptyUnionTimeBucket(LimitTimesRule rule, LocalDateTime now,
            ZSetOperations<String, String> zSetOps) {
        List<String> unionTimeBuckets = getPreviousUionTimeBucket(rule, now);
        String notEmptyUnionTimeBucket = null;
        for (String utb : unionTimeBuckets) {
            if (zSetOps.size(utb) > 0) {
                notEmptyUnionTimeBucket = utb; // 找到已经合并的时间窗口
                break;
            }
        }

        return notEmptyUnionTimeBucket;
    }

    /**
     * 找到所以需要合并的时间窗口
     * 
     * @param rule
     * @param now
     * @return
     */
    private List<String> getAllTimeBucket(LimitTimesRule rule, LocalDateTime now) {
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
