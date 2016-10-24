/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * @author luoshiqian 2016/10/24 17:10
 */
public class MongoTemplate extends org.springframework.data.mongodb.core.MongoTemplate {
    public MongoTemplate(Mongo mongo, String databaseName) {
        super(mongo, databaseName);
    }

    public MongoTemplate(Mongo mongo, String databaseName, UserCredentials userCredentials) {
        super(mongo, databaseName, userCredentials);
    }

    public MongoTemplate(MongoDbFactory mongoDbFactory) {
        super(mongoDbFactory);
    }

    public MongoTemplate(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter) {
        super(mongoDbFactory, mongoConverter);
    }

    public DBCollection createCollection(String collectionName, CollectionOptions collectionOptions) {
        return super.doCreateCollection(collectionName, convertToDbObject(collectionOptions));
    }

    protected DBObject convertToDbObject(CollectionOptions collectionOptions) {
        DBObject dbo = new BasicDBObject();

        if (collectionOptions.getCapped() != null) {
            dbo.put("capped", collectionOptions.getCapped().booleanValue());
        }
        if (collectionOptions.getSize() != null) {
            dbo.put("size", collectionOptions.getSize().longValue());
        }
        if (collectionOptions.getMaxDocuments() != null) {
            dbo.put("max", collectionOptions.getMaxDocuments().longValue());
        }

        return dbo;
    }
}
