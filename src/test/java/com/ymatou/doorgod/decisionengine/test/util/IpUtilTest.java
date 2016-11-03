/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test.util;

import com.ymatou.doorgod.decisionengine.test.BaseTest;
import com.ymatou.doorgod.decisionengine.util.IpUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author luoshiqian 2016/11/3 14:18
 */
public class IpUtilTest extends BaseTest {

    @Autowired
    private IpUtils ipUtils;

    //biz.ignoreRegexIps=127.0.0.1,192.168.*,172.*,10\\.10\\..*,10\\.11\\..*,10\\.12\\..*
    @Test
    public void test(){

        assertTrue(ipUtils.isIgnore("127.0.0.1"));

        assertTrue(ipUtils.isIgnore("192.168.0.1"));
        assertFalse(ipUtils.isIgnore("192.169.0.1"));

        assertTrue(ipUtils.isIgnore("172.1.1.1"));
        assertTrue(ipUtils.isIgnore("172.11.11.11"));
        assertFalse(ipUtils.isIgnore("173.11.11.11"));


        assertTrue(ipUtils.isIgnore("10.10.11.1"));
        assertFalse(ipUtils.isIgnore("101.10.11.1"));
        assertFalse(ipUtils.isIgnore("10.101.11.1"));

        assertTrue(ipUtils.isIgnore("10.11.11.1"));
        assertFalse(ipUtils.isIgnore("10.111.11.1"));
        assertFalse(ipUtils.isIgnore("101.111.11.1"));


        assertTrue(ipUtils.isIgnore("10.12.11.1"));
        assertFalse(ipUtils.isIgnore("10.121.11.1"));
        assertFalse(ipUtils.isIgnore("101.121.11.1"));

    }


}
