/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;
import com.ymatou.doorgod.decisionengine.cache.GuavaCacheFactoryBean;

/**
 * @author luoshiqian 2016/10/14 13:02
 */
@Configuration
public class CacheConfig {


    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Lists.newArrayList(
                deviceIdGuavaCache()));

        return cacheManager;
    }


    @Bean(name = "deviceIdGuavaCache")
    public Cache deviceIdGuavaCache() {
        GuavaCacheFactoryBean factoryBean = new GuavaCacheFactoryBean();
        factoryBean.setName("deviceIdGuavaCache");
        factoryBean.setMaximumSize(100000L);
        factoryBean.afterPropertiesSet();

        return factoryBean.getObject();
    }

}
