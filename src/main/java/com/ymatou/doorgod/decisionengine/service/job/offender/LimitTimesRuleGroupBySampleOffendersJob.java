/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job.offender;

import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHM;
import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.google.common.collect.Lists;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.integration.KafkaClients;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.mongo.MongoGroupBySamplePo;
import com.ymatou.doorgod.decisionengine.model.mongo.MongoGroupBySampleStats;
import com.ymatou.doorgod.decisionengine.service.OffenderService;
import com.ymatou.doorgod.decisionengine.util.MongoHelper;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * 
 * @author luoshiqian
 * 
 */
@DisallowConcurrentExecution
public class LimitTimesRuleGroupBySampleOffendersJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(LimitTimesRuleGroupBySampleOffendersJob.class);


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        MongoTemplate mongoTemplate = SpringContextHolder.getBean(MongoTemplate.class);
        KafkaClients kafkaClients = SpringContextHolder.getBean(KafkaClients.class);
        OffenderService offenderService = SpringContextHolder.getBean(OffenderService.class);

        String jobName = context.getJobDetail().getKey().getName();

        LimitTimesRule rule = RuleHolder.rules.get(jobName);

        if(null == rule){
            logger.info("exec LimitTimesRuleGroupBySampleOffendersJob:{} rule==null",jobName);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        String nowFormated = now.format(FORMATTER_YMDHMS);
        String ruleName = rule.getName();

        logger.debug("exec LimitTimesRuleGroupBySampleOffendersJob :{}",ruleName);
        String startTime = now.minusSeconds(rule.getStatisticSpan()).format(FORMATTER_YMDHM);
        String endTime = now.format(FORMATTER_YMDHM);
        try {
            Criteria criteria = Criteria.where("sampleTime").gte(startTime)
                                        .andOperator(Criteria.where("sampleTime").lte(endTime));

            logger.debug("test schedule ruleName:{} now:{} and sleep 2seconds",ruleName,System.currentTimeMillis());
            TimeUnit.SECONDS.sleep(2L);

            TypedAggregation<MongoGroupBySamplePo> aggregation = Aggregation.newAggregation(MongoGroupBySamplePo.class,
                    match(criteria),
                    group(fields("groupByKeys","leftKeys")),
                    group("_id.groupByKeys").count().as("count"),
                    match(Criteria.where("count").gte(rule.getTimesCap())),
                    sort(Sort.Direction.DESC, "count"));

            String collectionName = MongoHelper.getGroupByCollectionName(rule);
            AggregationResults<MongoGroupBySampleStats> result =
                    mongoTemplate.aggregate(aggregation, collectionName, MongoGroupBySampleStats.class);

            if (null != result) {
                boolean isOffendersChanged = false;
                logger.debug("after Aggregation result:{}",result.getMappedResults());

                List<MongoGroupBySampleStats> offenderStats = Lists.newArrayList();
                for (MongoGroupBySampleStats state : result.getMappedResults()) {
                    // 超过 加入黑名单
                    String releaseDate = now.plusSeconds(rule.getRejectionSpan()).format(FORMATTER_YMDHMS);
                    if(offenderService.saveOffender(rule,state.getGroupByKeys(),releaseDate,nowFormated)){
                        isOffendersChanged = true;
                        offenderStats.add(state);
                    }
                }
                if (isOffendersChanged) {
                    kafkaClients.sendUpdateOffendersEvent(ruleName);

                    logger.info("got ruleName:{},groupby offenders:{}", ruleName, offenderStats);
                }
            }
        } catch (Exception e) {
            logger.error("LimitTimesRuleGroupBySampleOffendersJob error,ruleName:{}",ruleName,e);
        }

    }
}
