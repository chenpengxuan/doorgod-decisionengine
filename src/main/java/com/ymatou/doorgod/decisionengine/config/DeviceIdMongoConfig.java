/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import java.net.UnknownHostException;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.ymatou.doorgod.decisionengine.config.props.MongoProps;

/**
 * @author luoshiqian 2016/9/9 13:51
 */
@Configuration
public class DeviceIdMongoConfig extends AbstractMongoConfiguration {

    @Autowired
    private MongoProps mongoProps;

    private MongoClient deviceIdMongo;

    @PreDestroy
    public void close() {
        if (this.deviceIdMongo != null) {
            this.deviceIdMongo.close();
        }
    }


    @Override
    protected String getDatabaseName() {
        return mongoProps.getDeviceIdMongoDatabaseName();
    }

    @Override
    @Bean(name = "deviceIdMongo")
    public MongoClient mongo() throws UnknownHostException {

        MongoClientURI uri = new MongoClientURI(mongoProps.getDeviceIdMongoAddress());
        this.deviceIdMongo = new MongoClient(uri);
        return this.deviceIdMongo;
    }

    @Bean(name = "deviceIdMongoFactory")
    public MongoDbFactory mongoDbFactory(MongoClient mongo) throws Exception {
        return new SimpleMongoDbFactory(mongo, mongoProps.getDeviceIdMongoDatabaseName());
    }


    @Override
    @Bean(name = "deviceIdMongoTemplate")
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongo(), mongoProps.getDeviceIdMongoDatabaseName());
        return mongoTemplate;
    }

}
