/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test.service;

import com.ymatou.doorgod.decisionengine.service.DeviceIdService;
import com.ymatou.doorgod.decisionengine.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author luoshiqian 2016/11/4 10:48
 */
public class DeviceIdServiceTest extends BaseTest{

    @Autowired
    private DeviceIdService deviceIdService;

    @Test
    public void test(){
        assertTrue(deviceIdService.findByDeviceId("40137"));
        assertTrue(deviceIdService.findByDeviceId("438900"));
        assertFalse(deviceIdService.findByDeviceId("401371"));
        assertFalse(deviceIdService.findByDeviceId("192B7187-F2B6-4E15-BA27-4B7DBDC43259"));
    }
}
