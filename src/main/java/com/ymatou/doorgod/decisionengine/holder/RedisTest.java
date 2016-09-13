/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.holder;

import com.google.common.collect.Sets;
import com.ymatou.doorgod.decisionengine.integration.DecisionEngine;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.model.StatisticItem;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author luoshiqian 2016/9/13 12:40
 */
public class RedisTest implements Runnable {
    ExecutorService writeExecutor = Executors.newFixedThreadPool(5);
    ScheduledExecutorService readExecutor = Executors.newSingleThreadScheduledExecutor();
    @Override
    public void run() {
        DecisionEngine decisionEngine = SpringContextHolder.getBean(DecisionEngine.class);
        String keys[] = new String[]{"ip","deviceId"};
        String ips[] = new String[]{
                "192.168.0.1",
                "192.168.0.2",
                "192.168.0.3",
                "192.168.0.4",
                "192.168.0.5",
                "192.168.0.6",
                "192.168.0.7",
                "192.168.0.8",
                "192.168.0.9",
                "192.168.0.10",
                "192.168.0.11",
                "192.168.0.12",
                "192.168.0.13",
                "192.168.0.14",
                "192.168.0.15",
                "192.168.0.16",
                "192.168.0.17",
                "192.168.0.18",
                "192.168.0.19",
                "192.168.0.20",
        };
        String deviceIds[] = new String[]{
                "aaaaa-bbbbb-cccccc-ddddd-1",
                "aaaaa-bbbbb-cccccc-ddddd-2",
                "aaaaa-bbbbb-cccccc-ddddd-3",
                "aaaaa-bbbbb-cccccc-ddddd-4",
                "aaaaa-bbbbb-cccccc-ddddd-5",
                "aaaaa-bbbbb-cccccc-ddddd-5",
                "aaaaa-bbbbb-cccccc-ddddd-7",
                "aaaaa-bbbbb-cccccc-ddddd-8",
                "aaaaa-bbbbb-cccccc-ddddd-9",
                "aaaaa-bbbbb-cccccc-ddddd-10",
                "aaaaa-bbbbb-cccccc-ddddd-11",
                "aaaaa-bbbbb-cccccc-ddddd-12",
                "aaaaa-bbbbb-cccccc-ddddd-13",
                "aaaaa-bbbbb-cccccc-ddddd-14",
                "aaaaa-bbbbb-cccccc-ddddd-15",
                "aaaaa-bbbbb-cccccc-ddddd-16",
                "aaaaa-bbbbb-cccccc-ddddd-17",
                "aaaaa-bbbbb-cccccc-ddddd-18",
                "aaaaa-bbbbb-cccccc-ddddd-19",
                "aaaaa-bbbbb-cccccc-ddddd-20",
        };
        LimitTimesRule rule = new LimitTimesRule();
        rule.setName("testrule");
        Set<String> keySet = Sets.newHashSet();
        keySet.addAll(Arrays.asList(keys));
        rule.setDimensionKeys(keySet);
        rule.setRejectionSpan(300);
        rule.setTimesCap(120);
        rule.setApplicableUris(Sets.newHashSet("/api/xxx.do"));

        RuleHolder.rules.put("testrule",rule);

        // 初始化延迟5秒开始执行 固定每秒执行一次
        readExecutor.scheduleAtFixedRate(() -> {
            try {
                decisionEngine.putSampleToRedis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5L * 1000, 1000, TimeUnit.MILLISECONDS);


//        for(int i=0;i<5;i++){
//
//            writeExecutor.execute(() -> {
//                while (true){
//                    StatisticItem a = new StatisticItem();
//                    LocalDateTime dateTime  = LocalDateTime.now();
//                    String str =  dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
//                    a.setReqTime(str);
//                    Sample sample2 = new Sample();
//                    sample2.addDimensionValue("uri","/api/xxx.do");
//                    sample2.addDimensionValue("ip",ips[new Random().nextInt(5)]);
//                    sample2.addDimensionValue("deviceId",deviceIds[new Random().nextInt(5)]);
//                    a.setSample(sample2);
//                    decisionEngine.putStaticItem(a);
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(new Random().nextInt(10));
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//
//        }



    }
}
