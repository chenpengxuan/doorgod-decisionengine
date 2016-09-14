/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.holder;

import java.io.UnsupportedEncodingException;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.config.props.KafkaProps;
import com.ymatou.doorgod.decisionengine.integration.DecisionEngine;
import com.ymatou.doorgod.decisionengine.model.StatisticItem;
import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

public class KafkaConsumeSample implements Runnable {

    private KafkaStream stream;

    public KafkaConsumeSample(KafkaStream stream) {
        this.stream = stream;
    }

    @Override
    public void run() {

        DecisionEngine decisionEngine = SpringContextHolder.getBean(DecisionEngine.class);

        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while (it.hasNext()) {
            try {
                String str = new String(it.next().message(), "utf-8");
                System.out.println("consume:" + str);

                decisionEngine.putStaticItem(JSON.parseObject(str, StatisticItem.class));

            } catch (UnsupportedEncodingException e) {

            }
        }


    }
}
