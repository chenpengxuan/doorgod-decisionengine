/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration.runnable;

import java.util.List;
import java.util.Properties;

import com.ymatou.doorgod.decisionengine.model.RejectReqEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.model.mongo.RejectReqPo;
import com.ymatou.doorgod.decisionengine.service.RejectReqService;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * @author luoshiqian 2016/9/23 14:47
 */
public class RejectReqConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RejectReqConsumer.class);

    private final KafkaConsumer<String, String> consumer;
    private final List<String> topics;

    public RejectReqConsumer(List<String> topics) {
        this.topics = topics;
        Properties props = SpringContextHolder.getBean("autoCommitConsumerProps");

        this.consumer = new KafkaConsumer<>(props);

    }

    @Override
    public void run() {
        RejectReqService rejectReqService = SpringContextHolder.getBean(RejectReqService.class);
        try {
            consumer.subscribe(topics);

            
            while (true) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                    for (ConsumerRecord<String, String> record : records) {

                        logger.debug("RejectReqConsumer consume record:{}", record);
                        rejectReqService.saveRejectReq(JSON.parseObject(record.value(), RejectReqEvent.class));

                    }
                } catch (Exception e) {
                    logger.error("RejectReqConsumer consume record error", e);
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
