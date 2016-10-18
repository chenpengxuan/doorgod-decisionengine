/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

import org.springframework.data.mongodb.core.MongoAction;
import org.springframework.data.mongodb.core.WriteConcernResolver;

import com.mongodb.WriteConcern;

/**
 * @author luoshiqian 2016/10/18 16:43
 */
public class MyWriteConcernResolver implements WriteConcernResolver {

    public WriteConcern resolve(MongoAction action) {
        if (action.getCollectionName().contains("GroupSample_")
                || action.getCollectionName().equals("RejectReq")
                || action.getCollectionName().equals("sample")
                ) {
            return WriteConcern.UNACKNOWLEDGED;
        } else {
            return action.getDefaultWriteConcern();
        }
    }
}

