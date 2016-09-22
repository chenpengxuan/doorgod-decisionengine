/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.core.task.TaskExecutor;

import com.ymatou.doorgod.decisionengine.config.props.KafkaProps;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

/**
 * 
 * @author qianmin 2016年9月9日 下午4:23:52
 * 
 */
public class KafkaConsumerInstance implements Runnable {

    private ConsumerConnector consumerConnector;
    private TaskExecutor taskExecutor;

    public KafkaConsumerInstance(ConsumerConnector consumerConnector,
            TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        this.consumerConnector = consumerConnector;
    }

    @Override
    public void run() {

        KafkaProps kafkaProps = SpringContextHolder.getBean(KafkaProps.class);

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(kafkaProps.getStatisticSampleTopic(), new Integer(3));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap =
                consumerConnector.createMessageStreams(topicCountMap);
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
}
