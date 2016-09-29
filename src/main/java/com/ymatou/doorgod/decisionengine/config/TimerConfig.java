/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import java.time.LocalDateTime;
import java.util.Map;
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
import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.integration.SampleStatisticCenter;

/**
 * @author luoshiqian 2016/9/18 15:24
 */
@Component
public class TimerConfig {

    private static final Logger logger = LoggerFactory.getLogger(TimerConfig.class);
    private ScheduledExecutorService redisExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService mongoExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService clearUselessMemoryExecutor = Executors.newSingleThreadScheduledExecutor();
    @Autowired
    private BizProps bizProps;
    @Autowired
    private SampleStatisticCenter sampleStatisticCenter;

    @PostConstruct
    public void putSampleToReadisTimer() {
        // 初始化延迟5秒开始执行 固定每n毫秒执行一次
        redisExecutor.scheduleAtFixedRate(() -> {
            try {
                sampleStatisticCenter.putSampleToRedis();
            } catch (Exception e) {
                // 所有异常都catch到 防止异常导致定时任务停止
                logger.error("上报redis出错", e);
            }
        }, 5L * 1000, bizProps.getUploadRedisTimerInterval(), TimeUnit.MILLISECONDS);

    }

    @PostConstruct
    public void putGroupBySampleToMongoTimer() {
        mongoExecutor.scheduleAtFixedRate(() -> {
            try {
                sampleStatisticCenter.putSampleToMongo();
            } catch (Exception e) {
                logger.error("上报mongo出错", e);
            }
        }, 5L * 1000, bizProps.getUploadMongoTimerInterval(), TimeUnit.MILLISECONDS);
    }

    // 每十秒 清空 10秒之前的所有数据
    @PostConstruct
    public void clearUselessMemoryTimer() {

        clearUselessMemoryExecutor.scheduleAtFixedRate(() -> {
            try {
                Map ruleTimeSampleMaps = SampleStatisticCenter.ruleTimeSampleMaps;
                clearUselessMemory(ruleTimeSampleMaps);

                Map groupByRuleTimeSampleMaps = SampleStatisticCenter.groupByRuleTimeSampleMaps;
                clearUselessMemory(groupByRuleTimeSampleMaps);

            } catch (Exception e) {
                logger.error("上报mongo出错", e);
            }
        }, 5L * 1000, 10000, TimeUnit.MILLISECONDS);
    }


    @PreDestroy
    public void destory() {
        redisExecutor.shutdown();
        mongoExecutor.shutdown();
    }


    /**
     * 删除10秒之前 无用的内存 预防 内存数据一直增加 Map<String,Map<String,Map<Sample,Object>>> Map
     * 
     * @param memoryMap
     */
    private void clearUselessMemory(Map<String, Map<String, Map>> memoryMap) {

        String needRemoveBeforeDate = LocalDateTime.now().minusSeconds(10).format(Constants.FORMATTER_YMDHMS);

        logger.info("begin to clear useless data,size:{}", memoryMap.size());
        // 清空
        memoryMap.entrySet().stream().forEach(ruleMapEntry -> ruleMapEntry.getValue().entrySet()
                .removeIf(entry -> Long.valueOf(entry.getKey()) <= Long.valueOf(needRemoveBeforeDate)));

        logger.info("end to clear useless data,size:{}", memoryMap.size());
    }
}
