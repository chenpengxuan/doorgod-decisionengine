/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model.mongo;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author luoshiqian 2016/9/14 16:24
 */
public class MongoGroupBySampleStats {

    @Field("_id")
    private String groupByKeys;

    private int count;


    public String getGroupByKeys() {
        return groupByKeys;
    }

    public void setGroupByKeys(String groupByKeys) {
        this.groupByKeys = groupByKeys;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
