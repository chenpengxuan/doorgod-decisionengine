/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ymatou.doorgod.decisionengine.config.props.KafkaProps;
import com.ymatou.doorgod.decisionengine.integration.runnable.CacheReloaderConsumer;
import com.ymatou.doorgod.decisionengine.integration.runnable.StatisticSampleConsumer;

/**
 * @author luoshiqian 2016/9/21 13:05
 */
@Component
public class KafkaClients implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(KafkaClients.class);

    @Resource(name = "offendersProducer")
    private Producer<String, String> offendersProducer;
    @Autowired
    private KafkaProps kafkaProps;

    /**
     * 发送更新offender消息
     * 
     * @param ruleName
     */
    public void sendUpdateOffendersEvent(String ruleName) {
        Map<String, String> map = Maps.newHashMap();
        map.put("ruleName", ruleName);
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(
                kafkaProps.getUpdateOffendersTopic(), JSON.toJSONString(map));
        offendersProducer.send(record);

        logger.info("send kafka offender:{}", ruleName);
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {


        int numConsumers = kafkaProps.getStatisticSampleThreadNums();

        ExecutorService executor = Executors.newFixedThreadPool(kafkaProps.getStatisticSampleThreadNums());

        List<String> topics = Arrays.asList(kafkaProps.getStatisticSampleTopic());

        final List<StatisticSampleConsumer> consumers = new ArrayList<>();
        for (int i = 0; i < numConsumers; i++) {
            StatisticSampleConsumer consumer = new StatisticSampleConsumer(topics);
            consumers.add(consumer);
            executor.submit(consumer);
        }

        // cache reload thread
        CacheReloaderConsumer cacheReloaderConsumer =
                new CacheReloaderConsumer(Arrays.asList(kafkaProps.getUpdateRuleTopic()));
        Thread cacheReloaderThread = new Thread(cacheReloaderConsumer, "cacheReloaderThread");
        cacheReloaderThread.setDaemon(true);
        cacheReloaderThread.start();


        // add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (StatisticSampleConsumer consumer : consumers) {
                    consumer.shutdown();
                }

                cacheReloaderConsumer.shutdown();

                shutDownExectors(Lists.newArrayList(executor));
            }
        });
    }


    private void shutDownExectors(ArrayList<ExecutorService> executors) {
        executors.forEach(executor -> {

            executor.shutdown();
            try {
                executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("InterruptedException", e);
            }

        });
    }
    
}
