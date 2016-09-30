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
}
