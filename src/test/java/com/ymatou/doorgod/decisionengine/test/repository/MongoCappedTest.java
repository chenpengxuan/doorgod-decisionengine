/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test.repository;

import com.google.common.collect.Maps;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.util.MongoTemplate;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.ymatou.doorgod.decisionengine.model.Demo;
import com.ymatou.doorgod.decisionengine.repository.DemoRepository;
import com.ymatou.doorgod.decisionengine.test.BaseTest;
import com.ymatou.doorgod.decisionengine.util.CollectionOptions;

import java.util.Map;

/**
 * @author luoshiqian 2016/9/9 15:45
 */
@EnableAutoConfiguration
public class MongoCappedTest extends BaseTest {

    @Autowired
    DemoRepository demoRepository;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MongoTemplate mongoTemplate;

    public static final CollectionOptions COLLECTION_OPTIONS = new CollectionOptions(1024 * 10L,null, true);

    @Test
    public void testAddCaped(){


        String collectName = "demo";//+RandomStringUtils.random(5);
        if(!mongoTemplate.collectionExists(collectName)){
            mongoTemplate.createCollection(collectName,COLLECTION_OPTIONS);
        }

        //只能加进去 217个
        for(int i=0;i<218;i++){
            Demo demo = new Demo();
            demo.setName("test"+ RandomStringUtils.randomAlphabetic(10));
            mongoTemplate.save(demo,collectName);
        }

        //再加入20个其他结构
        for(int i=0;i<20;i++){

            Map<String,Object> map = Maps.newHashMap();
            map.put("name1","newSampe"+ RandomStringUtils.randomAlphabetic(10));
            Sample sample = new Sample();
            sample.getDimensionValues().put("ip","182.16.8."+i);
            map.put("sample",sample);
            mongoTemplate.save(map,collectName);
        }

        //再加10个之前结构
        for(int i=0;i<10;i++){
            Demo demo = new Demo();
            demo.setName("test"+ RandomStringUtils.randomAlphabetic(10));
            mongoTemplate.save(demo,collectName);
        }
    }



}
