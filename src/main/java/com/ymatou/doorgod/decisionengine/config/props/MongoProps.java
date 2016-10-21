/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * redis属性配置
 *
 */
@ConfigurationProperties(prefix = "mongo")
public class MongoProps {

    private String mongoAddress;
    private String mongoDatabaseName;

    private String deviceIdMongoAddress;
    private String deviceIdMongoDatabaseName;


    public String getMongoAddress() {
        return mongoAddress;
    }

    public void setMongoAddress(String mongoAddress) {
        this.mongoAddress = mongoAddress;
    }

    public String getMongoDatabaseName() {
        return mongoDatabaseName;
    }

    public void setMongoDatabaseName(String mongoDatabaseName) {
        this.mongoDatabaseName = mongoDatabaseName;
    }

    public String getDeviceIdMongoAddress() {
        return deviceIdMongoAddress;
    }

    public void setDeviceIdMongoAddress(String deviceIdMongoAddress) {
        this.deviceIdMongoAddress = deviceIdMongoAddress;
    }

    public String getDeviceIdMongoDatabaseName() {
        return deviceIdMongoDatabaseName;
    }

    public void setDeviceIdMongoDatabaseName(String deviceIdMongoDatabaseName) {
        this.deviceIdMongoDatabaseName = deviceIdMongoDatabaseName;
    }
}
