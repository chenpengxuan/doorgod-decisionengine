/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration.runnable;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.ymatou.doorgod.decisionengine.model.RejectReqEvent;
import com.ymatou.doorgod.decisionengine.service.RejectReqService;
import com.ymatou.doorgod.decisionengine.util.IpUtils;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.holder.SampleStatisticCenter;
import com.ymatou.doorgod.decisionengine.model.StatisticItem;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

import static com.ymatou.doorgod.decisionengine.constants.Constants.KEY_NAME_IP;
import static com.ymatou.doorgod.decisionengine.constants.Constants.PerformanceServiceEnum.ONE_KAFKA_PULL_STATISTIC_TIME;
import static com.ymatou.doorgod.decisionengine.constants.Constants.PerformanceServiceEnum.ONE_STATISTIC_ITEM_TIME;
import static com.ymatou.doorgod.decisionengine.constants.Constants.PerformanceServiceEnum.SAMPLE_OVER_TIME;

/**
 * @author luoshiqian 2016/9/23 14:47
 */
public class StatisticSampleConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(StatisticSampleConsumer.class);

    private final KafkaConsumer<String, String> consumer;
    private final List<String> topics;
    private final SampleStatisticCenter sampleStatisticCenter;
    private final RejectReqService rejectReqService;
    private final IpUtils ipUtils;

    public StatisticSampleConsumer(List<String> topics) {
        this.topics = topics;
        Properties props = SpringContextHolder.getBean("autoCommitConsumerProps");

        this.consumer = new KafkaConsumer<>(props);
        this.sampleStatisticCenter = SpringContextHolder.getBean(SampleStatisticCenter.class);
        this.rejectReqService = SpringContextHolder.getBean(RejectReqService.class);
        this.ipUtils = SpringContextHolder.getBean(IpUtils.class);
    }

    @Override
    public void run() {
        try {

            consumer.subscribe(topics);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(5000);
                try {
                    PerformanceStatisticContainer.add(() -> {
                        for (ConsumerRecord<String, String> record : records) {

                            logger.debug("StatisticSampleConsumer consume record:{}", record);
                            StatisticItem statisticItem = JSON.parseObject(record.value(), StatisticItem.class);
                            if (statisticItem.getSample() == null) {
                                continue;
                            }

                            if (statisticItem.isRejectedByFilter() || statisticItem.isRejectedByHystrix()) {
                                RejectReqEvent rejectReqEvent = new RejectReqEvent();
                                rejectReqEvent.setRuleName(statisticItem.getHitRule());
                                rejectReqEvent.setSample(statisticItem.getSample());
                                rejectReqEvent.setTime(statisticItem.getReqTime());
                                rejectReqEvent.setUri(statisticItem.getUri());

                                rejectReqService.saveRejectReq(rejectReqEvent);
                            } else {
                                String ip = statisticItem.getSample().getDimensionValues().get(KEY_NAME_IP);
                                if (StringUtils.isNotBlank(ip) && ipUtils.isIgnore(ip)) {
                                    continue;
                                }
                                PerformanceStatisticContainer.add(
                                        () -> sampleStatisticCenter.putStatisticItem(statisticItem),
                                        ONE_STATISTIC_ITEM_TIME.name());

                            }
                        }
                    }, ONE_KAFKA_PULL_STATISTIC_TIME.name());
                } catch (Exception e) {
                    logger.error("StatisticSampleConsumer consume record error", e);
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        consumer.wakeup();
    }
}
