/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration.runnable;

import java.util.Collections;
import java.util.List;

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

        //FIXME:应该是根据配置创建一个consumer, autocommit务必设置为false
        this.consumer = SpringContextHolder.getBean("cacheReloaderConsumer");

        ruleDiscoverer = SpringContextHolder.getBean(RuleDiscoverer.class);
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(topics);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {

                    //FIXME: 消费过程中发生异常，导致consumer被close
                    ruleDiscoverer.reload();

                    consumer.commitSync(Collections.singletonMap(new TopicPartition(record.topic(), record.partition()),
                            //FIXME: record.offset()+1　??
                            new OffsetAndMetadata(record.offset())));

                    //FIXME:刷新缓存频率低，建议打info，便于分析/确认问题
                    logger.debug("consume record:", record);
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown
        } finally {
            consumer.close();
        }
    }

    //FIXME: never called
    public void shutdown() {
        consumer.wakeup();
    }
}
