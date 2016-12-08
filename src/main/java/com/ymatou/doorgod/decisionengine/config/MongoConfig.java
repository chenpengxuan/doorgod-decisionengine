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
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoTypeMapper;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.ymatou.doorgod.decisionengine.config.props.DbProps;
import com.ymatou.doorgod.decisionengine.util.MongoTemplate;
import com.ymatou.doorgod.decisionengine.util.MyWriteConcernResolver;

/**
 * @author luoshiqian 2016/9/9 13:51
 */
@Configuration
public class MongoConfig extends AbstractMongoConfiguration {

    @Autowired
    private DbProps dbProps;
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
        return dbProps.getMongoDatabaseName();
    }

    @Override
    @Bean
    public MongoClient mongo() throws UnknownHostException {

        MongoClientURI uri = new MongoClientURI(dbProps.getMongoAddress());
        this.mongo = new MongoClient(uri);
        return this.mongo;
    }

    @Bean
    public MongoDbFactory mongoDbFactory(MongoClient mongo) throws Exception {
        return new SimpleMongoDbFactory(mongo, dbProps.getMongoDatabaseName());
    }


    @Override
    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory(mongo),mappingMongoConverter());
        mongoTemplate.setWriteConcernResolver(new MyWriteConcernResolver());
        return mongoTemplate;
    }

    @Bean
    @Override
    public MappingMongoConverter mappingMongoConverter() throws Exception {
        MappingMongoConverter mmc = super.mappingMongoConverter();
        mmc.setTypeMapper(customTypeMapper());
        return mmc;
    }

    @Bean
    public MongoTypeMapper customTypeMapper() {
        //remove _class
        return new DefaultMongoTypeMapper(null);
    }

}
