/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ymatou.doorgod.decisionengine.util.MongoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.mongo.MongoGroupBySamplePo;
import com.ymatou.doorgod.decisionengine.model.Sample;

/**
 * @author luoshiqian 2016/9/14 16:01
 */
@Component("mongoSampleStore")
public class MongoSampleStore extends AbstractSampleStore {

    private static final Logger logger = LoggerFactory.getLogger(MongoSampleStore.class);

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

        String collectionName = MongoHelper.getGroupByCollectionName(rule);
        if(!mongoTemplate.collectionExists(collectionName)){
            mongoTemplate.createCollection(collectionName);
        }
        samples.forEach(entry -> {

            Sample sample = entry.getKey();
            Set<Sample> sampleSet = ((Set)entry.getValue());

            String groupByKeys = sample.toString();
            sampleSet.forEach(s -> {
                String leftKeys = s.toString();

                //uploadtime 找到start end time
                LocalDateTime localDateTime = LocalDateTime.parse(uploadTime, Constants.FORMATTER_YMDHMS);
                String startMinute = localDateTime.format(Constants.FORMATTER_YMDHM);
                String endMinute = localDateTime.plusMinutes(1).format(Constants.FORMATTER_YMDHM);

//                MongoGroupBySamplePo samplePo = new MongoGroupBySamplePo(startMinute,endMinute,groupByKeys,leftKeys);
                Query query = new Query(Criteria.where("startTime").is(startMinute)
                        .and("endTime").is(endMinute)
                        .and("groupByKeys").is(groupByKeys)
                        .and("leftKeys").is(leftKeys)
                );
                Update update = new Update();
                update.set("startTime",startMinute);
                update.set("endTime",endMinute);
                update.set("groupByKeys",groupByKeys);
                update.set("leftKeys",leftKeys);
                mongoTemplate.findAndModify(query,update,new FindAndModifyOptions()
                        .returnNew(true).upsert(true),MongoGroupBySamplePo.class,collectionName);

//                mongoTemplate.upsert(query,update,MongoGroupBySamplePo.class,collectionName);
//                if(!mongoTemplate.exists(query,collectionName)){
//                    mongoTemplate.insert(samplePo,collectionName);
//                }
            });
        });
    }


}
