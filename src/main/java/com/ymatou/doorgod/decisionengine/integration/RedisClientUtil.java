/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.integration;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.config.BizConfig;

import io.vertx.core.Vertx;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

/**
 * 
 * @author qianmin 2016年9月6日 下午6:44:24
 * 
 */
@Component
public class RedisClientUtil implements InitializingBean {

    @Autowired
    private BizConfig bizConfig;

    private RedisClient redisClient;

    public RedisClient instance() {
        return redisClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Vertx vertx = Vertx.vertx();
        redisClient = RedisClient.create(vertx,
                new RedisOptions().setHost(bizConfig.getRedisHost()));
    }
}
