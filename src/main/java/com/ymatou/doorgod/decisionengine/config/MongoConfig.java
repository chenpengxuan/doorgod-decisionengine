/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import java.net.UnknownHostException;

import javax.annotation.PreDestroy;

import com.ymatou.doorgod.decisionengine.util.MongoTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.ymatou.doorgod.decisionengine.config.props.MongoProps;
import com.ymatou.doorgod.decisionengine.util.MyWriteConcernResolver;

/**
 * @author luoshiqian 2016/9/9 13:51
 */
@Configuration
public class MongoConfig extends AbstractMongoConfiguration {

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


    @Override
    protected String getDatabaseName() {
        return mongoProps.getMongoDatabaseName();
    }

    @Override
    @Bean
    public MongoClient mongo() throws UnknownHostException {

        MongoClientURI uri = new MongoClientURI(mongoProps.getMongoAddress());
        this.mongo = new MongoClient(uri);
        return this.mongo;
    }

    @Bean
    public MongoDbFactory mongoDbFactory(MongoClient mongo) throws Exception {
        return new SimpleMongoDbFactory(mongo, mongoProps.getMongoDatabaseName());
    }


    @Override
    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongo(),mongoProps.getMongoDatabaseName());
        mongoTemplate.setWriteConcernResolver(new MyWriteConcernResolver());
        return mongoTemplate;
    }

}
