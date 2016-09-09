/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by tuwenjie on 2016/9/6.
 */
@RunWith(SpringRunner.class)
@EnableAspectJAutoProxy
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.ymatou")
@Configuration
@ContextConfiguration(classes = BaseTest.class)
public class BaseTest {


    @Test
    public void testStartable() throws Exception {

//        System.in.read();
    }
}
