/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.integration.DecisionEngine;

/**
 * @author luoshiqian 2016/9/18 15:24
 */
@Component
public class TimerConfig {

    private static final Logger logger = LoggerFactory.getLogger(TimerConfig.class);
    private ScheduledExecutorService redisExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService mongoExecutor = Executors.newSingleThreadScheduledExecutor();
    @Autowired
    private BizProps bizProps;
    @Autowired
    private DecisionEngine decisionEngine;

    @PostConstruct
    public void initRedisTimer() {
        // 初始化延迟5秒开始执行 固定每n毫秒执行一次
        redisExecutor.scheduleAtFixedRate(() -> {
            try {
                decisionEngine.putSampleToRedis();
            } catch (Exception e) {
                // 所有异常都catch到 防止异常导致定时任务停止
                logger.error("上报redis出错", e);
            }
        }, 5L * 1000, bizProps.getUploadRedisTimerInterval(), TimeUnit.MILLISECONDS);

    }

    @PostConstruct
    public void initMongoTimer() {
        mongoExecutor.scheduleAtFixedRate(() -> {
            try {
                decisionEngine.putSampleToMongo();
            } catch (Exception e) {
                logger.error("上报mongo出错", e);
            }
        }, 5L * 1000, bizProps.getUploadMongoTimerInterval(), TimeUnit.MILLISECONDS);
    }


    @PreDestroy
    public void destory() {
        redisExecutor.shutdown();
        mongoExecutor.shutdown();
    }

}
