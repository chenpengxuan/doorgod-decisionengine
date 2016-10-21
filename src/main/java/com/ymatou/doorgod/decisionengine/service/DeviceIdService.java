/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.service;

/**
 * @author luoshiqian 2016/10/14 18:08
 */
public interface DeviceIdService {

    /**
     * 查询deviceId 是否激活
     * 
     * @param deviceId
     * @return
     */
    boolean findByDeviceId(String deviceId);



}
