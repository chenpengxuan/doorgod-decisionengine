/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.service.impl;

import javax.annotation.Resource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ymatou.doorgod.decisionengine.service.DeviceIdService;

/**
 * @author luoshiqian 2016/10/14 18:19
 */
@Service
public class DeviceIdServiceImpl implements DeviceIdService {

//    @Resource(name = "deviceIdMongoTemplate")
//    private MongoTemplate deviceIdMongoTemplate;

    @Override
    @Cacheable(cacheNames = "guavaCache")
    public boolean findByDeviceId(String deviceId) {

//        // 查询MongoDB中是否已经存在， 若不存在则保存
//        Query query = new Query(Criteria.where("deviceid").is(deviceId));
//
//        return deviceIdMongoTemplate.exists(query, "deviceinfodb");
        return true;
    }
}
