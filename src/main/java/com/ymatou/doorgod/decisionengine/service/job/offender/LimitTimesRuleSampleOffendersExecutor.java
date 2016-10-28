/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job.offender;

import static com.ymatou.doorgod.decisionengine.constants.Constants.*;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getEmptySetName;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getUnionSetName;
import static com.ymatou.doorgod.decisionengine.util.Utils.getExpireByRule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.integration.KafkaClients;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.service.OffenderService;
import com.ymatou.doorgod.decisionengine.util.DateUtils;
import com.ymatou.doorgod.decisionengine.util.Utils;

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

            List<String> timeBuckets = Utils.getAllTimeBucket(rule.getName(), now,rule.getStatisticSpan());

            String currentUnionName = getUnionSetName(ruleName, nowFormated, UNION);

            long start = System.currentTimeMillis();

            zSetOps.unionAndStore(getEmptySetName(EMPTY_SET), timeBuckets, currentUnionName);

            logger.info("zset union consumed:{}ms,rulename:{},currentUnionName:{}",
                    (System.currentTimeMillis() - start), ruleName, currentUnionName);

            zSetOps.getOperations().expire(currentUnionName, getExpireByRule(rule), TimeUnit.SECONDS);

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

}
