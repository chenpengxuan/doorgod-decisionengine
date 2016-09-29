/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test;

import com.ymatou.doorgod.decisionengine.integration.SampleStatisticCenter;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.model.StatisticItem;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author luoshiqian 2016/9/12 15:46
 */
@EnableAutoConfiguration
public class SampleStatisticCenterTest extends BaseTest{

    ExecutorService writeExecutor = Executors.newFixedThreadPool(5);
    ExecutorService readExecutor = Executors.newFixedThreadPool(5);


    @Autowired
    SampleStatisticCenter sampleStatisticCenter;

    public void testPutStaticItemAndRedis(){

        writeExecutor.execute(() -> {

            String aa = "aa"+ RandomStringUtils.random(3);
                while (true){
                    StatisticItem a = new StatisticItem();
                    LocalDateTime dateTime  = LocalDateTime.now();
                    String str =  dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
                    a.setReqTime(str);
                    Sample sample2 = new Sample();
                    sample2.addDimensionValue(aa,aa);
                    a.setSample(sample2);
                    sampleStatisticCenter.putStatisticItem(a);
                    try {
                        TimeUnit.MILLISECONDS.sleep(200+new Random().nextInt(300));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        });


    }


}
