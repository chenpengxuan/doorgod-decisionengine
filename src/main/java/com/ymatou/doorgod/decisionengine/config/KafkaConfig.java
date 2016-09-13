/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.config;

import java.util.Properties;

import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ymatou.doorgod.decisionengine.config.props.KafkaProps;

/**
 * 
 * @author qianmin 2016年9月9日 下午3:56:05
 * 
 */
@Configuration
public class KafkaConfig {

    @Autowired
    private KafkaProps kafkaProps;

    @Bean
    public KafkaConsumer<String, String> kafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaProps.getBootstrapServers());
        props.put("group.id", kafkaProps.getGroupId());
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);


        return consumer;
    }

    @Bean
    public ConsumerConnector consumerConnector(){
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaProps.getBootstrapServers());
        props.put("group.id", kafkaProps.getGroupId());
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        ConsumerConnector consumerConnector =
                kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
        return consumerConnector;
    }
}
