/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration.runnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ymatou.doorgod.decisionengine.integration.DecisionEngine;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.model.StatisticItem;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * @author luoshiqian 2016/9/23 14:47
 */
public class StatisticSampleConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(StatisticSampleConsumer.class);

    private final KafkaConsumer<String, String> consumer;
    private final List<String> topics;
    private final DecisionEngine decisionEngine;

    public StatisticSampleConsumer(List<String> topics) {
        this.topics = topics;
        Properties props = SpringContextHolder.getBean("autoCommitConsumerProps");

        this.consumer = new KafkaConsumer<>(props);
        this.decisionEngine = SpringContextHolder.getBean(DecisionEngine.class);
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(topics);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("partition", record.partition());
                    data.put("offset", record.offset());
                    String value = record.value();
                    data.put("value", value);


                    logger.debug("consume record:", data);
                    decisionEngine.putStaticItem(JSON.parseObject(value, StatisticItem.class));
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        consumer.wakeup();
    }
}
