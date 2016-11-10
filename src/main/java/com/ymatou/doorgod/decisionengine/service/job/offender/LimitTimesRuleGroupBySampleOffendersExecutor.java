/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job.offender;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.mongodb.DBObject;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.integration.KafkaClients;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.model.mongo.MongoGroupBySamplePo;
import com.ymatou.doorgod.decisionengine.model.mongo.MongoGroupBySampleStats;
import com.ymatou.doorgod.decisionengine.service.OffenderService;
import com.ymatou.doorgod.decisionengine.util.MongoHelper;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHM;
import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;
import static com.ymatou.doorgod.decisionengine.constants.Constants.PerformanceServiceEnum.MONGO_GROUP_AGGREGATION;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * 
 * @author luoshiqian
 * 
 */
@Component
public class LimitTimesRuleGroupBySampleOffendersExecutor{

    private static final Logger logger = LoggerFactory.getLogger(LimitTimesRuleGroupBySampleOffendersExecutor.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private KafkaClients kafkaClients;
    @Autowired
    private OffenderService offenderService;

    public void execute(JobExecutionContext context){

        String jobName = context.getJobDetail().getKey().getName();

        LimitTimesRule rule = RuleHolder.limitTimesRules.get(jobName);

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

            long start = System.currentTimeMillis();

            TypedAggregation<MongoGroupBySamplePo> aggregation = Aggregation.newAggregation(MongoGroupBySamplePo.class,
                    match(criteria),
                    group(fields("groupByKeys","leftKeys")).sum("count").as("totalCount"),
                    match(Criteria.where("totalCount").gte(rule.getGroupByCount())),
                    group("_id.groupByKeys").count().as("count"),
                    match(Criteria.where("count").gte(rule.getTimesCap())),
                    sort(Sort.Direction.DESC, "count"));

            String collectionName = MongoHelper.getGroupByCollectionName(rule);
            AggregationResults<DBObject> result =
                    mongoTemplate.aggregate(aggregation, collectionName, DBObject.class);

            long consumed = (System.currentTimeMillis() - start);
            logger.info("aggregation consumed:{}ms,rulename:{}", consumed, ruleName);
            PerformanceStatisticContainer.add(consumed,MONGO_GROUP_AGGREGATION.name());

            if (null != result) {
                boolean isOffendersChanged = false;
                logger.debug("after Aggregation result:{}",result.getMappedResults());

                List<Sample> offenderList = Lists.newArrayList();
                for (DBObject dbObject: result.getMappedResults()) {
                    // 超过 加入黑名单
                    Map<String,String> dimensionValues = (Map<String,String>)dbObject.get("dimensionValues");
                    Sample offender = new Sample();
                    offender.getDimensionValues().putAll(dimensionValues);
                    if(offenderService.saveOffender(rule, offender,now,nowFormated)){
                        isOffendersChanged = true;
                        offenderList.add(offender);
                    }
                }
                if (isOffendersChanged) {
                    kafkaClients.sendUpdateOffendersEvent(ruleName);

                    logger.info("got ruleName:{},groupby offenders:{}", ruleName, offenderList);
                }
            }
        } catch (Exception e) {
            logger.error("LimitTimesRuleGroupBySampleOffendersJob error,ruleName:{}",ruleName,e);
        }

    }
}
