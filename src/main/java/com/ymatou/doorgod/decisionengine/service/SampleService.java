/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.service;

import com.ymatou.doorgod.decisionengine.model.mongo.MongoSamplePo;

import java.util.List;

/**
 * @author luoshiqian 2016/10/14 18:08
 */
public interface SampleService {


    List<MongoSamplePo> findTopNSampleByRuleNameAndDate(String ruleName,String startTime,String endTime);


}
