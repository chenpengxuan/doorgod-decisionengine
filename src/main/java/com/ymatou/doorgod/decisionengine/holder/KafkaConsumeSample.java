/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.holder;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author qianmin 2016年9月9日 下午4:23:52
 * 
 */
public class KafkaConsumeSample implements Runnable {

    private KafkaStream stream;

    public KafkaConsumeSample(KafkaStream stream) {
        this.stream = stream;
    }

    @Override
    public void run() {

        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while (it.hasNext()){
            try {
                String str = new String(it.next().message(),"utf-8");
                System.out.println("consume:"+str);


            } catch (UnsupportedEncodingException e) {

            }
        }


    }
}
