/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job;

import static com.ymatou.doorgod.decisionengine.constants.Constants.*;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getBlackListMapName;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.time.LocalDateTime;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.MongoGroupBySamplePo;
import com.ymatou.doorgod.decisionengine.model.MongoGroupBySampleStats;
import com.ymatou.doorgod.decisionengine.util.MongoHelper;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * 
 * @author luoshiqian
 * 
 */
public class MongoSampleOffendersExecutor implements Job {

    private static final Logger logger = LoggerFactory.getLogger(MongoSampleOffendersExecutor.class);


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        MongoTemplate mongoTemplate =
                SpringContextHolder.getBean(MongoTemplate.class);

        StringRedisTemplate redisTemplate =
                SpringContextHolder.getBean(StringRedisTemplate.class);

        String jobName = context.getJobDetail().getKey().getName();
        LimitTimesRule rule = RuleHolder.rules.get(jobName.replace("groupBy",""));
        LocalDateTime now = LocalDateTime.now();
        String ruleName = rule.getName();

        String startTime = now.minusSeconds(rule.getStatisticSpan()).format(FORMATTER_YMDHM);
        String endTime = now.format(FORMATTER_YMDHM);
//        String startTime = "201609181750";
//        String endTime = "201609181803";

        Criteria criteria = Criteria.where("startTime").gte(startTime).and("endTime").lte(endTime);

        TypedAggregation<MongoGroupBySamplePo> aggregation = Aggregation.newAggregation(MongoGroupBySamplePo.class,
                match(criteria),
                group(Fields.from(Fields.field("groupByKeys","groupByKeys"))).count().as("count"),
                sort(Sort.Direction.DESC, "count"));

        String collectionName = MongoHelper.getGroupByCollectionName(rule);
        AggregationResults<MongoGroupBySampleStats> result =
                mongoTemplate.aggregate(aggregation, collectionName, MongoGroupBySampleStats.class);

        if (null != result) {
            boolean isBlackListChanged = false;
            List<MongoGroupBySampleStats> blackList = Lists.newArrayList();
            for (MongoGroupBySampleStats state : result.getMappedResults()) {
                // 超过 加入黑名单
                if (state.getCount() >= rule.getTimesCap()) {
                    blackList.add(state);
                    String rejectTime = now.plusSeconds(rule.getRejectionSpan()).format(FORMATTER_YMDHMS);
                    if (redisTemplate.opsForZSet().add(getBlackListMapName(ruleName), state.getGroupByKeys(),
                            Double.valueOf(rejectTime))) {
                        isBlackListChanged = true;
                    }
                }
            }

            if (isBlackListChanged) {
                redisTemplate.convertAndSend(BLACK_LIST_CHANNEL, ruleName);
                logger.debug("got blacklist: {}, time: {}", JSON.toJSONString(blackList));
            }
        }

        // todo 删除之前无用数据

    }
}
