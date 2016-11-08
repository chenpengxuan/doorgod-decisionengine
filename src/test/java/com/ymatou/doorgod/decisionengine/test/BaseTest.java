/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test;

//import com.ymatou.doorgod.decisionengine.Application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.ymatou.doorgod.decisionengine.Application;

/**
 * Created by tuwenjie on 2016/9/6.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class BaseTest {


    @Test
    public void testStartable() throws Exception {

        System.in.read();
    }
}
