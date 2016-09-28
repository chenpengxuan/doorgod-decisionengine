/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job;

import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHM;
import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.time.LocalDateTime;

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
public class MongoGroupBySampleOffendersJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(MongoGroupBySampleOffendersJob.class);


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        MongoTemplate mongoTemplate = SpringContextHolder.getBean(MongoTemplate.class);
        KafkaClients kafkaClients = SpringContextHolder.getBean(KafkaClients.class);
        OffenderService offenderService = SpringContextHolder.getBean(OffenderService.class);

        String jobName = context.getJobDetail().getKey().getName();

        //FIXME: 不要加groupBy前缀。。。
        LimitTimesRule rule = RuleHolder.rules.get(jobName.replace("groupBy", ""));

        if(null == rule){
            logger.info("exec MongoGroupBySampleOffendersJob:{} rule==null",jobName);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        String nowFormated = now.format(FORMATTER_YMDHMS);
        String ruleName = rule.getName();

        logger.info("exec MongoGroupBySampleOffendersJob :{}",ruleName);

        String startTime = now.minusSeconds(rule.getStatisticSpan()).format(FORMATTER_YMDHM);
        String endTime = now.format(FORMATTER_YMDHM);

        //FIXME:写入时, startTime/endTime是不是已经合并了?
        Criteria criteria = Criteria.where("startTime").gte(startTime).and("endTime").lte(endTime);

        //FIXME: distinct count
        TypedAggregation<MongoGroupBySamplePo> aggregation = Aggregation.newAggregation(MongoGroupBySamplePo.class,
                match(criteria),
                group("groupByKeys").count().as("count"),
                sort(Sort.Direction.DESC, "count"));

        String collectionName = MongoHelper.getGroupByCollectionName(rule);
        AggregationResults<MongoGroupBySampleStats> result =
                mongoTemplate.aggregate(aggregation, collectionName, MongoGroupBySampleStats.class);

        if (null != result) {
            boolean isOffendersChanged = false;
            for (MongoGroupBySampleStats state : result.getMappedResults()) {
                // 超过 加入黑名单
                if (state.getCount() >= rule.getTimesCap()) {

                    String releaseDate = now.plusSeconds(rule.getRejectionSpan()).format(FORMATTER_YMDHMS);
                    if(offenderService.saveOffender(rule,state.getGroupByKeys(),releaseDate,nowFormated)){
                        isOffendersChanged = true;
                    }
                }
            }
            if (isOffendersChanged) {
                kafkaClients.sendUpdateOffendersEvent(ruleName);

                //FIXME:更详尽的日志
                logger.info("got groupby offenders");
            }
        }

    }
}
