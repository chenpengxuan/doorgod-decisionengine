/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration.store;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.ymatou.doorgod.decisionengine.util.MongoTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;

import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.holder.SampleStatisticCenter;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.model.mongo.MongoGroupBySamplePo;
import com.ymatou.doorgod.decisionengine.util.DateUtils;
import com.ymatou.doorgod.decisionengine.util.MongoHelper;

/**
 * @author luoshiqian 2016/9/14 16:01
 */
@Component("mongoSampleStore")
public class MongoSampleStore extends AbstractSampleStore {

    private static final Logger logger = LoggerFactory.getLogger(MongoSampleStore.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Collection<LimitTimesRule> findRule() {
        return RuleHolder.limitTimesRules.values().stream()
                .filter(rule -> !CollectionUtils.isEmpty(rule.getGroupByKeys())).collect(Collectors.toSet());
    }

    @Override
    protected Map<String, Map<String, Map<Sample, Map<Sample,AtomicInteger>>>> getMemoryMap() {
        return SampleStatisticCenter.groupByRuleTimeSampleMaps;
    }

    @Override
    protected int getTopN() {
        return bizProps.getUploadMongoTopN();
    }

    @Override
    public void uploadSampleToDb(LimitTimesRule rule, String uploadTime,
            Collection<Map.Entry<Sample, Object>> samples) {

        String collectionName = MongoHelper.getGroupByCollectionName(rule);
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName, Constants.COLLECTION_OPTIONS);
            mongoTemplate.indexOps(collectionName).ensureIndex(new Index("addTime", Sort.Direction.ASC));
            mongoTemplate.indexOps(collectionName).ensureIndex(new Index("sampleTime", Sort.Direction.ASC));
        }
        samples.forEach(entry -> {

            Sample sample = entry.getKey();
            Map<Sample,AtomicInteger> leftKeySampleMap = ((Map) entry.getValue());

            if(leftKeySampleMap.size() < bizProps.getUploadMongoGroupMinSize()){
                return;
            }
            String groupByKeys = sample.toString();

            leftKeySampleMap.entrySet().forEach(s -> {
                String leftKeys = s.getKey().toString();
                int count = s.getValue().intValue();

                // uploadtime 找到 那一分钟
                LocalDateTime localDateTime = DateUtils.parseDefault(uploadTime);
                String sampleTime = localDateTime.format(Constants.FORMATTER_YMDHM);

                Query query = new Query(
                        Criteria.where("sampleTime").is(sampleTime)
                                .and("groupByKeys").is(groupByKeys)
                                .and("leftKeys").is(leftKeys));
                Update update = new Update();
                update.set("addTime", new Date());
                update.inc("count",count);


                mongoTemplate.findAndModify(query, update, new FindAndModifyOptions()
                        .returnNew(true).upsert(true), MongoGroupBySamplePo.class, collectionName);

            });
        });
    }


}
