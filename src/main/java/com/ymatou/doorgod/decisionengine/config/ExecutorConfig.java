/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.ymatou.doorgod.decisionengine.util.LogRejectedPolicy;

/**
 * 
 * @author qianmin 2016年9月9日 下午4:48:35
 * 
 */
@Configuration
public class ExecutorConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(50);
        taskExecutor.setQueueCapacity(1000);
        taskExecutor.setKeepAliveSeconds(300);
        taskExecutor.setRejectedExecutionHandler(new LogRejectedPolicy());
        return taskExecutor;
    }

//    public TaskExecutor taskExecutorForRedis
}
