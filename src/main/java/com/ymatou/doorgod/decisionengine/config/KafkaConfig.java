/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.config;

import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ymatou.doorgod.decisionengine.config.props.KafkaProps;
import com.ymatou.doorgod.decisionengine.util.Utils;

/**
 * 
 * @author qianmin 2016年9月9日 下午3:56:05
 * 
 */
@Configuration
public class KafkaConfig {

    @Autowired
    private KafkaProps kafkaProps;

    @Bean(name = "cacheReloaderConsumer")
    public KafkaConsumer<String, String> cacheReloaderConsumer() {
        String localIp = Utils.localIp();
        if (localIp == null || localIp.equals("127.0.0.1")) {
            // 本地ip将用作groupId, 本地Ip拿不到，拒绝应用启动
            throw new RuntimeException("Failed to fetch local ip");
        }

        Properties props = consumerCommonProps();
        props.put("group.id", localIp + "_DE");
        props.put("client.id", localIp);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        return consumer;
    }

    /**
     * 配置详见 http://kafka.apache.org/documentation.html#newconsumerconfigs
     * 
     * @return
     */
    @Bean(name = "autoCommitConsumerProps")
    public Properties statisticSampleConsumerProps() {
        Properties props = consumerCommonProps();

        props.put("auto.offset.reset", "latest");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");

        return props;
    }

    @Bean(name = "consumerCommonProps")
    public Properties consumerCommonProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaProps.getBootstrapServers());
        props.put("group.id", kafkaProps.getGroupId());
        props.put("max.poll.records", "50000");
        props.put("enable.auto.commit", "false"); //默认设置为false
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }


    @Bean(name = "offendersProducer")
    public Producer<String, String> offendersProducer() {
        Properties props = new Properties();

        props.put("bootstrap.servers", kafkaProps.getBootstrapServers());

        props.put("acks", "all");
        props.put("retries",1);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(props);

        return producer;
    }
}
