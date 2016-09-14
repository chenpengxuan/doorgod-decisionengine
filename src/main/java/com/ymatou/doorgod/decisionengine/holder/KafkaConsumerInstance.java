/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.holder;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.core.task.TaskExecutor;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.integration.DecisionEngine;
import com.ymatou.doorgod.decisionengine.model.StatisticItem;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * 
 * @author qianmin 2016年9月9日 下午4:23:52
 * 
 */
public class KafkaConsumerInstance implements Runnable {

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private KafkaConsumer<String, String> kafkaConsumer;
    private TaskExecutor taskExecutor;

    public KafkaConsumerInstance(KafkaConsumer<String, String> kafkaConsumer, TaskExecutor taskExecutor) {
        this.kafkaConsumer = kafkaConsumer;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void run() {
        try {
            DecisionEngine decisionEngine = SpringContextHolder.getBean(DecisionEngine.class);
            kafkaConsumer.subscribe(Arrays.asList("test111")); // TODO
            while (!closed.get()) {
                ConsumerRecords<String, String> records = kafkaConsumer.poll(10000);
                taskExecutor.execute(() -> {
                    for (ConsumerRecord<String, String> record : records) {

                        // System.out.printf("Thread = %s, offset = %d, partition = %s key = %s,
                        // value = %s\n",
                        // Thread.currentThread().getName(), record.offset(), record.partition(),
                        // record.key(),
                        // record.value());
                        String sampleStr = record.value();
                        decisionEngine.putStaticItem(JSON.parseObject(sampleStr, StatisticItem.class));
                        // System.out.println(sampleStr);
                    }

                });
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (!closed.get())
                throw e;
        } finally {
            kafkaConsumer.close();
        }
    }

    public void shutdown() {
        closed.set(true);
        kafkaConsumer.wakeup();
    }
}
