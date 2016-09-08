/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.integration;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.model.Visitor;

/**
 * 
 * @author qianmin 2016年9月6日 下午7:01:48
 * 
 */
@Component
public class DecisionEngine {

    @Autowired
    private RedisClientUtil redisClientUtil;
    @Autowired
    private IpCountHandler ipCountHandler;
    private ConcurrentHashMap<String, Integer> ipCountMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> deceveIdCount = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> idDecviceIdCount = new ConcurrentHashMap<>();

    public void process(ConsumerRecord<String, String> record) {

        mapCalculate(record);
    }

    private void mapCalculate(ConsumerRecord<String, String> record) {
        long offset = record.offset();
        String key = record.key();
        String value = record.value();

        Visitor visitor = parseVisitor(value);
        incrementAndGet(ipCountMap, visitor.getIp());
        incrementAndGet(deceveIdCount, visitor.getDeviceId());
        incrementAndGet(idDecviceIdCount, visitor.getIp() + visitor.getDeviceId());
    }


    public void redisCalculate() {
        ipCountMap.entrySet().parallelStream().forEach(entry -> {
            redisClientUtil.instance().hincrby("ipCount", entry.getKey(), entry.getValue(), ipCountHandler);
        });
    }

    public Integer incrementAndGet(ConcurrentHashMap<String, Integer> map, String key) {
        Integer value = null;
        if ((value = map.get(key)) != null) {
            value = map.put(key, value + 1);
        } else {
            value = map.put(key, value);
        }

        return value;
    }

    private Visitor parseVisitor(String visitorInfo) {
        Visitor visitor = new Visitor();
        // TODO
        return visitor;
    }

}
