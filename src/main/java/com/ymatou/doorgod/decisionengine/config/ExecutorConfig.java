/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;

/**
 * 
 * @author qianmin 2016年9月9日 下午4:48:35
 * 
 */
@Configuration
public class ExecutorConfig {

    @Autowired
    private BizProps bizProps;

    @Bean(name = "putSampleThreadPool")
    public ExecutorService putSampleThreadPool() {

        return Executors.newFixedThreadPool(bizProps.getPutSampleThreadNums());
    }
}
