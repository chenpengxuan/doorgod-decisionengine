/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration.runnable;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.holder.SampleStatisticCenter;
import com.ymatou.doorgod.decisionengine.model.StatisticItem;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * @author luoshiqian 2016/9/23 14:47
 */
public class StatisticSampleConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(StatisticSampleConsumer.class);

    private final KafkaConsumer<String, String> consumer;
    private final List<String> topics;
    private final SampleStatisticCenter sampleStatisticCenter;
    private boolean isFirstTime = true;

    public StatisticSampleConsumer(List<String> topics) {
        this.topics = topics;
        Properties props = SpringContextHolder.getBean("autoCommitConsumerProps");

        this.consumer = new KafkaConsumer<>(props);
        this.sampleStatisticCenter = SpringContextHolder.getBean(SampleStatisticCenter.class);
    }
    
    @Override
    public void run() {
        try {

            consumer.subscribe(topics);

            while (true) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(5000);
                    for (ConsumerRecord<String, String> record : records) {

                        logger.debug("StatisticSampleConsumer cnsume record:{}", record);
                        sampleStatisticCenter.putStatisticItem(JSON.parseObject(record.value(), StatisticItem.class));
                    }
                } catch (Exception e) {
                    logger.error("StatisticSampleConsumer consume record error", e);
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
