/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ymatou.doorgod.decisionengine.model.mongo.MongoSamplePo;
import com.ymatou.doorgod.decisionengine.service.SampleService;

/**
 * @author luoshiqian 2016/10/18 19:54
 */
@Service
public class SampleServiceImpl implements SampleService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<MongoSamplePo> findTopNSampleByRuleNameAndDate(String ruleName, String startTime, String endTime) {

        Query query = new Query(
                Criteria.where("ruleName").is(ruleName)
                        .where("time").gte(startTime)
                        .andOperator(Criteria.where("time").lte(endTime)));

        List<MongoSamplePo> samplePoList = mongoTemplate.find(query, MongoSamplePo.class);

        return samplePoList;
    }

}
