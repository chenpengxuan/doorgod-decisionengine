/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
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

import com.ymatou.doorgod.decisionengine.util.DateUtils;
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
            LimitTimesRule rule = RuleHolder.limitTimesRules.get(jobName);
            if (null == rule) {
                logger.info("exec offenders job :{} rule==null", jobName);
                return;
            }
            String ruleName = rule.getName();

            logger.info("exec offenders job :{}", ruleName);

            // 合并Redis涉及到的时间窗口
            String nowFormated = DateUtils.formatToTenSeconds(LocalDateTime.now());
            LocalDateTime now = DateUtils.parseDefault(nowFormated);

            List<String> timeBuckets = getAllTimeBucket(rule, now);

            String currentUnionName = getUnionSetName(ruleName, nowFormated, UNION);

            logger.info("begin to execute rule:{},time:{},currentUnionName:{}",
                    ruleName, now.format(FORMATTER_YMDHMS), currentUnionName);

            zSetOps.unionAndStore(getEmptySetName(EMPTY_SET), timeBuckets, currentUnionName);

            // 获取Offender
            Set<String> offenders = zSetOps.rangeByScore(currentUnionName, rule.getTimesCap(), Integer.MAX_VALUE);

            //删除union
            zSetOps.removeRange(currentUnionName,0,-1);

            if (!offenders.isEmpty()) {
                String releaseDate = now.plusSeconds(rule.getRejectionSpan()).format(FORMATTER_YMDHMS);

                boolean isOffendersChanged = false;
                for (String offender : offenders) {
                    if (offenderService.saveOffender(rule, offender, releaseDate, nowFormated)) {
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
            logger.error("error execute rule:{}", context.getJobDetail().getKey().getName(), e);
        }
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
        for (int nums = rule.getStatisticSpan() / 10; nums > 0; nums--) {
            timeBuckets.add(getNormalSetName(rule.getName(), now.minusSeconds(nums * 10).format(FORMATTER_YMDHMS)));
        }
        return timeBuckets;
    }

    private long getExpireByRule(LimitTimesRule rule) {
        if (rule.getTimesCap() < 60) {
            return ((Double) (rule.getStatisticSpan() * 2.0)).longValue();
        }
        return ((Double) (rule.getStatisticSpan() * 1.5)).longValue();
    }
}
