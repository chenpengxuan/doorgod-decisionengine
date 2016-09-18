/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration;

import com.mongodb.MongoClient;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.MongoGroupBySamplePo;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.repository.MongoGroupBySampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author luoshiqian 2016/9/14 16:01
 */
@Component("mongoSampleStore")
public class MongoSampleStore extends AbstractSampleStore {

    private static final Logger logger = LoggerFactory.getLogger(MongoSampleStore.class);
    @Autowired
    private MongoGroupBySampleRepository mongoGroupBySampleRepository;
    @Autowired
    private MongoClient mongo;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected Collection<LimitTimesRule> findRule() {
        return RuleHolder.rules.values().stream()
                .filter(rule -> !CollectionUtils.isEmpty(rule.getGroupByKeys())).collect(Collectors.toSet());
    }

    @Override
    protected Map<String, Map<String, Map<Sample, Set<Sample>>>> getMemoryMap() {
        return DecisionEngine.groupByRuleTimeSampleMaps;
    }

    @Override
    protected int getTopN() {
        return bizProps.getUploadMongoTopN();
    }

    @Override
    protected void uploadSampleToDb(LimitTimesRule rule, String uploadTime,
            Collection<Map.Entry<Sample, Object>> samples) {

        samples.forEach(entry -> {

            Sample sample = entry.getKey();
            Set<Sample> sampleSet = ((Set)entry.getValue());

            String groupByKeys = sample.toString();
            sampleSet.forEach(s -> {
                String leftKeys = s.toString();
                //todo uploadtime 找到start end time
                MongoGroupBySamplePo samplePo = new MongoGroupBySamplePo(uploadTime,uploadTime,groupByKeys,leftKeys);
                Query query = new Query(Criteria.where("startTime").is(uploadTime)
                        .and("startTime").is(uploadTime)
                        .and("groupByKeys").is(groupByKeys)
                        .and("leftKeys").is(leftKeys)
                );

                if(!mongoTemplate.exists(query,MongoGroupBySamplePo.class)){
                    mongoTemplate.insert(samplePo,getCollectionName(rule));
                }
            });
//            logger.debug("ruleName:{},count:{},sample:{}", rule.getName(), samplePo.getCount(), samplePo.getSample());
        });
    }

    private String getCollectionName(LimitTimesRule rule){
        return "GroupSample_"+rule.getName();
    }

    /**
     * 获取规则的过期时间 小于60秒 系数为 1.5 大于60秒 系数为 1.2
     * 
     * @param rule
     * @return
     */
    private long getExpireByRule(LimitTimesRule rule) {
        if (rule.getTimesCap() < 60) {
            return ((Double) (rule.getStatisticSpan() * 1.5)).longValue();
        }
        return ((Double) (rule.getStatisticSpan() * 1.2)).longValue();
    }

}
