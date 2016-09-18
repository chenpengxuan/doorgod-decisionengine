/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test.repository;

import com.ymatou.doorgod.decisionengine.model.Demo;
import com.ymatou.doorgod.decisionengine.model.MongoGroupBySamplePo;
import com.ymatou.doorgod.decisionengine.model.MongoGroupBySampleStats;
import com.ymatou.doorgod.decisionengine.repository.DemoRepository;
import com.ymatou.doorgod.decisionengine.test.BaseTest;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Set;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

/**
 * @author luoshiqian 2016/9/9 15:45
 */
@EnableAutoConfiguration
public class DemoTest extends BaseTest {

    @Autowired
    DemoRepository demoRepository;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    public void testAdd(){

        Demo demo = new Demo();
        demo.setName("test"+ RandomStringUtils.random(10));
        demoRepository.save(demo);

        List<Demo> demoList = demoRepository.findAll();
        System.out.println(demoList);
    }

    @Test
    public void testList(){
        List<Demo> demoList = demoRepository.findAll();
        System.out.println(demoList);
    }

    @Test
    public void testRedisZSet(){
        String setName = "rule";
        redisTemplate.opsForZSet().add(setName,"one",1.0);
        redisTemplate.opsForZSet().add(setName,"two",2.0);
        redisTemplate.opsForZSet().add(setName,"three",3.0);
        redisTemplate.opsForZSet().add(setName,"four",4.0);
        redisTemplate.opsForZSet().add(setName,"five",5.0);

        Set<String> set = redisTemplate.opsForZSet().range(setName,0,-1);

        System.out.println(set);
    }

    public void testMongo(){
        long i = mongoTemplate.count(new Query(Criteria.where("startTime").gte("201609181501").and("endTime").lte("201609181503")),"GroupSample_testrule3");
        System.out.println(i);
        GroupBy groupBy = new GroupBy("startTime","endTime","groupByKeys");
        groupBy.reduceFunction("function(doc,prev){return prev.count+=1}");
        groupBy.initialDocument("{count:0}");
        Criteria criteria = Criteria.where("startTime").gte("201609181455").and("endTime").lte("201609181503");
        Object o = mongoTemplate.group(criteria,"GroupSample_testrule3",groupBy,Object.class);

        System.out.println(o);


        TypedAggregation<MongoGroupBySamplePo> aggregation = Aggregation.newAggregation(MongoGroupBySamplePo.class,
                match(criteria),
                group("startTime","endTime","groupByKeys").count().as("count"),
                sort(Sort.Direction.DESC,"startTime","endTime","count")
        );
        AggregationResults<MongoGroupBySampleStats> result = mongoTemplate.aggregate(aggregation,"GroupSample_testrule3", MongoGroupBySampleStats.class);
        System.out.println(result);
    }

}
