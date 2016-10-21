/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.service.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ymatou.doorgod.decisionengine.script.ScriptBean;
import com.ymatou.doorgod.decisionengine.service.DeviceIdService;

/**
 * @author luoshiqian 2016/10/14 18:19
 */
@Service
public class DeviceIdServiceImpl implements DeviceIdService, ScriptBean {

    private static final Logger logger = LoggerFactory.getLogger(DeviceIdServiceImpl.class);

    @Resource(name = "deviceIdMongoTemplate")
    private MongoTemplate deviceIdMongoTemplate;

    @Override
    @Cacheable(cacheNames = "deviceIdGuavaCache")
    public boolean findByDeviceId(String deviceId) {

        Query query = new Query(Criteria.where("deviceid").is(deviceId));

        return deviceIdMongoTemplate.exists(query, "deviceId");
    }

}
