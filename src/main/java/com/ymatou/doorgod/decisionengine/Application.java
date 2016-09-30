/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine;

import com.ymatou.doorgod.decisionengine.config.ConnectionConfig;
import com.ymatou.doorgod.decisionengine.config.props.MongoProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.ymatou.doorgod.decisionengine.holder.ShutdownLatch;
import com.ymatou.doorgod.decisionengine.service.job.RuleDiscoverer;

/**
 * 
 * @author qianmin 2016年9月6日 下午5:36:15
 * 
 */
@EnableAspectJAutoProxy
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.ymatou.doorgod.decisionengine")
@EnableConfigurationProperties({ConnectionConfig.class, MongoProps.class})
public class Application {

    public static final Logger logger = LoggerFactory.getLogger(Application.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

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
