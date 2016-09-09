/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test.repository;

import com.ymatou.doorgod.decisionengine.model.Demo;
import com.ymatou.doorgod.decisionengine.repository.DemoRepository;
import com.ymatou.doorgod.decisionengine.test.BaseTest;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Set;

/**
 * @author luoshiqian 2016/9/9 15:45
 */
@EnableAutoConfiguration
public class DemoTest extends BaseTest {

    @Autowired
    DemoRepository demoRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

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


}
