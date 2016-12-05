/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test.service;

import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.service.OffenderService;
import com.ymatou.doorgod.decisionengine.service.job.RuleDiscoverer;
import com.ymatou.doorgod.decisionengine.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDateTime;
import java.util.Set;

import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;

/**
 * @author luoshiqian 2016/11/10 18:26
 */
public class OffenderServiceTest extends BaseTest{

    @Autowired
    private OffenderService offenderService;
    @Autowired
    private RuleDiscoverer ruleDiscoverer;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test(){
        ruleDiscoverer.execute();

        Sample sample = new Sample();
        sample.getDimensionValues().put("ip","192.168.0.6");

        //保存第一个
        offenderService.saveOffender(RuleHolder.limitTimesRules.get("IP_Prevent"),sample, LocalDateTime.now(),LocalDateTime.now().format(FORMATTER_YMDHMS));

        //保存第二个
        offenderService.saveOffender(RuleHolder.limitTimesRules.get("IP_Prevent"),sample, LocalDateTime.now().plusMinutes(10),LocalDateTime.now().plusMinutes(10).format(FORMATTER_YMDHMS));


        //保存第三个
        offenderService.saveOffender(RuleHolder.limitTimesRules.get("IP_Prevent"),sample, LocalDateTime.now().plusMinutes(30),LocalDateTime.now().plusMinutes(30).format(FORMATTER_YMDHMS));
    }


    @Test
    public void testRedisSetKeys(){

        Set<String> keys = stringRedisTemplate.keys("*union*");

        keys.forEach(s -> {
            Set<ZSetOperations.TypedTuple<String>> a =  stringRedisTemplate.opsForZSet().rangeWithScores(s,0,-1);
            a.forEach(stringTypedTuple -> System.out.println(stringTypedTuple.getValue()));
        });

        stringRedisTemplate.delete(keys);
        System.out.println(keys);

        stringRedisTemplate.opsForZSet().add("20161205111910_union_1","test",65);
        stringRedisTemplate.opsForZSet().add("20161205111920_union_1","test",65);
        stringRedisTemplate.opsForZSet().add("20161205111930_union_1","test",65);
    }

}
