/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration.runnable;

import java.util.Collections;
import java.util.List;

import com.baidu.disconf.client.config.DisClientConfig;
import com.ymatou.doorgod.decisionengine.constants.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.doorgod.decisionengine.service.job.RuleDiscoverer;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * @author luoshiqian 2016/9/23 14:47
 */
public class CacheReloaderConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CacheReloaderConsumer.class);

    private final KafkaConsumer<String, String> consumer;
    private final List<String> topics;
    private final RuleDiscoverer ruleDiscoverer;

    public CacheReloaderConsumer(List<String> topics) {
        this.topics = topics;

        this.consumer = SpringContextHolder.getBean("cacheReloaderConsumer");

        ruleDiscoverer = SpringContextHolder.getBean(RuleDiscoverer.class);
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(topics);

            while (true) {
                String env = DisClientConfig.getInstance().ENV;
                if(env.equals(Constants.ENV_STG)){
                    return;
                }
                ConsumerRecords<String, String> records = consumer.poll(5000);
                try {
                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("begin consume record:{}", record);

                        ruleDiscoverer.reload();


                        consumer.commitSync(Collections.singletonMap(new TopicPartition(record.topic(), record.partition()),
                                new OffsetAndMetadata(record.offset() + 1)));

                        logger.info("end consume record:{}", record);
                    }
                } catch (Exception e) {
                    logger.error("cacheReloaderConsumer consume record error:{}", e);
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
