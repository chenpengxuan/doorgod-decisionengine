/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import com.ymatou.doorgod.decisionengine.config.props.MongoProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.springframework.data.redis.connection.RedisNode;

/**
 * @author luoshiqian 2016/9/9 13:51
 */
@Configuration
public class MongoConfig {

    @Autowired
    private MongoProps mongoProps;
    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private MongoClientOptions options;
    private MongoClient mongo;

    @PreDestroy
    public void close() {
        if (this.mongo != null) {
            this.mongo.close();
        }
    }

    @Bean
    public MongoClient mongo() throws UnknownHostException {

        List<ServerAddress> addresses = new ArrayList<ServerAddress>();

        for (String address : mongoProps.getMongoAddress().split("[,]")) {
            String[] ad = address.split(":");
            addresses.add(new ServerAddress(ad[0], Integer.valueOf(ad[1])));
        }

        this.mongo = new MongoClient(addresses);
        return this.mongo;
    }

    // public MongoClient mongoClient() throws UnknownHostException{
    //
    //
    // MongoClient client = new MongoClient(addresses);
    // return this.mongoProperties.createMongoClient(this.options,environment);
    // }
    @Bean
    public MongoDbFactory mongoDbFactory(MongoClient mongo) throws Exception {
        return new SimpleMongoDbFactory(mongo, mongoProps.getMongoDatabaseName());
    }


}
