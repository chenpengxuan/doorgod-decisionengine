/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author luoshiqian 2016/9/13 15:34
 */
public class ProducerTest {

    static Producer<String, String> producer = null;
    public static void init() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.16.100.105:9092");
        props.put("group.id", "doorgod");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

       producer = new KafkaProducer<String, String>(props);
    }

    public static void main(String[] args) {
        init();

        while (true){
            ProducerRecord<String, String> record = new ProducerRecord<String, String>("test111", RandomStringUtils.randomNumeric(10));
            producer.send(record);
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
