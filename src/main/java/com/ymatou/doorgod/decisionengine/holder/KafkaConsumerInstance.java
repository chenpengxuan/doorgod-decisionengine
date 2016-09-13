/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.holder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ymatou.doorgod.decisionengine.config.props.KafkaProps;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
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
    private ConsumerConnector consumerConnector;
    private TaskExecutor taskExecutor;

    public KafkaConsumerInstance(KafkaConsumer<String, String> kafkaConsumer,ConsumerConnector consumerConnector, TaskExecutor taskExecutor) {
        this.kafkaConsumer = kafkaConsumer;
        this.taskExecutor = taskExecutor;
        this.consumerConnector = consumerConnector;
    }

    @Override
    public void run() {
//        try {
//            DecisionEngine decisionEngine = SpringContextHolder.getBean(DecisionEngine.class);
//            kafkaConsumer.subscribe(Arrays.asList("test111")); // TODO
//            while (!closed.get()) {
//                ConsumerRecords<String, String> records = kafkaConsumer.poll(10000);
//
//                taskExecutor.execute(() -> {
//                    for (ConsumerRecord<String, String> record : records) {
//                        String sampleStr = record.value();
//                        decisionEngine.putStaticItem(JSON.parseObject(sampleStr, StatisticItem.class));
//                    }
//                });
//            }
//        } catch (WakeupException e) {
//            // Ignore exception if closing
//            if (!closed.get())
//                throw e;
//        } finally {
//            kafkaConsumer.close();
//        }

        KafkaProps kafkaProps = SpringContextHolder.getBean(KafkaProps.class);

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(kafkaProps.getStatisticSampleTopic(), new Integer(3));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(kafkaProps.getStatisticSampleTopic());


        // now launch all the threads
        //
        Executor executor = Executors.newFixedThreadPool(3);

        // now create an object to consume the messages
        //
        for (final KafkaStream stream : streams) {
            executor.execute(new KafkaConsumeSample(stream));
        }
    }

    public void shutdown() {
        closed.set(true);
        kafkaConsumer.wakeup();
    }
}
