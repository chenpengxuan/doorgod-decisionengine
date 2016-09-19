/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.TaskExecutor;

import com.ymatou.doorgod.decisionengine.holder.KafkaConsumerInstance;
import com.ymatou.doorgod.decisionengine.holder.ShutdownLatch;
import com.ymatou.doorgod.decisionengine.service.job.RuleDiscoverer;

import kafka.javaapi.consumer.ConsumerConnector;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * 
 * @author qianmin 2016年9月6日 下午5:36:15
 * 
 */
@EnableAspectJAutoProxy
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.ymatou")
public class Application {

    public static final Logger logger = LoggerFactory.getLogger(Application.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        // 启动Kafka Consumer
        KafkaConsumer<String, String> kafkaConsumer = (KafkaConsumer<String, String>) ctx.getBean("kafkaConsumer");
        ConsumerConnector consumerConnector = ctx.getBean(ConsumerConnector.class);
        TaskExecutor taskExecutor = (TaskExecutor) ctx.getBean("taskExecutor");
        new Thread(new KafkaConsumerInstance(kafkaConsumer,consumerConnector, taskExecutor)).start();

        // 加载Rule数据， 添加修改Rule的定时任务， RedisToMongo同步任务
        RuleDiscoverer ruleDiscoverer = ctx.getBean(RuleDiscoverer.class);
        ruleDiscoverer.execute();



        ShutdownLatch shutdownLatch = new ShutdownLatch("decisionengine");
        try {
            shutdownLatch.await();
        } catch (Exception e) {
            logger.warn("shut down ", e);
        }
    }
}
