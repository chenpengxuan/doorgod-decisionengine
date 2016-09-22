/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.ymatou.doorgod.decisionengine.config.props.KafkaProps;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author luoshiqian 2016/9/21 13:05
 */
@Component
public class KafkaClients {
    private static final Logger logger = LoggerFactory.getLogger(KafkaClients.class);

    @Resource(name = "offendersProducer")
    private Producer<String,String> offendersProducer;
    @Autowired
    private KafkaProps kafkaProps;

    /**
     * 发送更新offender消息
     * @param ruleName
     */
    public void sendUpdateOffendersEvent(String ruleName) {
        Map<String, String> map = Maps.newHashMap();
        map.put("ruleName", ruleName);
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(
                kafkaProps.getUpdateOffendersTopic(), JSON.toJSONString(map));
        offendersProducer.send(record);

        logger.info("send kafka offender:{}",ruleName);
    }



}
