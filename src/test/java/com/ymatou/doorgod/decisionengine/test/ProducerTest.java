/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.integration.DecisionEngine;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.model.StatisticItem;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author luoshiqian 2016/9/13 15:34
 */
public class ProducerTest {

    static ExecutorService writeExecutor = Executors.newFixedThreadPool(5);
    static Producer<String, String> producer = null;
    public static void init() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.16.100.105:9092");
        props.put("group.id", "doorgod");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

       producer = new KafkaProducer<String, String>(props);
    }

    public static void main(String[] args) {
        init();

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
        rule.setStatisticSpan(120);
        rule.setApplicableUris(Sets.newHashSet("/api/xxx.do"));

        RuleHolder.rules.put("testrule",rule);


        for(int i=0;i<5;i++){

            writeExecutor.execute(() -> {
                while (true){
                    StatisticItem a = new StatisticItem();
                    LocalDateTime dateTime  = LocalDateTime.now();
                    String str =  dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
                    a.setReqTime(str);
                    Sample sample2 = new Sample();
                    sample2.addDimensionValue("uri","/api/xxx.do");
                    sample2.addDimensionValue("ip",ips[new Random().nextInt(5)]);
                    sample2.addDimensionValue("deviceId",deviceIds[new Random().nextInt(5)]);
                    a.setSample(sample2);

                    ProducerRecord<String, String> record = new ProducerRecord<String, String>("test111", JSON.toJSONString(a));

                    try {
                        TimeUnit.MILLISECONDS.sleep(new Random().nextInt(10));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

}
