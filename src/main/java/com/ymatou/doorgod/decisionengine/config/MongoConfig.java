/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.ymatou.doorgod.decisionengine.config.props.MongoProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import javax.annotation.PreDestroy;
import java.net.UnknownHostException;

/**
 * @author luoshiqian 2016/9/9 13:51
 */
@Configuration
public class MongoConfig {

    @Autowired
    private MongoProperties mongoProperties;
    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private MongoClientOptions options;
    private Mongo mongo;
    @PreDestroy
    public void close() {
        if (this.mongo != null) {
            this.mongo.close();
        }
    }
    @Bean
    public Mongo mongo() throws UnknownHostException {
        this.mongo = mongoClient();
        return this.mongo;
    }

    public MongoClient mongoClient() throws UnknownHostException{
        return this.mongoProperties.createMongoClient(this.options,environment);
    }
    @Bean
    public MongoDbFactory mongoDbFactory() throws Exception {
        return new SimpleMongoDbFactory(mongoClient(),"gsiao");
    }


}
