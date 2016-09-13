/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.baidu.disconf.client.DisconfMgrBeanSecond;
import com.ymatou.doorgod.decisionengine.config.props.RedisProps;

import redis.clients.jedis.JedisPoolConfig;

/**
 * @author luoshiqian 2016/9/9 13:01
 */
@Configuration
public class RedisConfig {

    @Autowired
    private RedisProps redisProps;
    @Autowired
    private DisconfMgrBeanSecond disconfMgrBeanSecond;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {

        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        redisSentinelConfiguration.setMaster(redisProps.getMasterName());
        for (String sentinelstr : redisProps.getSentinelAddress().split("[,]")) {
            String[] hp = sentinelstr.split(":");
            redisSentinelConfiguration.addSentinel(new RedisNode(hp[0], Integer.valueOf(hp[1])));
        }

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(redisProps.getMaxTotal());

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisSentinelConfiguration);
        jedisConnectionFactory.setPoolConfig(jedisPoolConfig);
        jedisConnectionFactory.setUsePool(redisProps.isUsePool());
        jedisConnectionFactory.setTimeout(redisProps.getTimeout());
        jedisConnectionFactory.setDatabase(redisProps.getDbIndex());

        return jedisConnectionFactory;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(jedisConnectionFactory);
        return stringRedisTemplate;
    }



}
