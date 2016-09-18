/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author luoshiqian 2016/9/14 16:24
 */
@Document(collection = "sample")
public class MongoGroupBySamplePo {

    @Id
    private String id;

    @Field("startTime")
    private String startTime;
    @Field("endTime")
    private String endTime;
    @Field("groupByKeys")
    private String groupByKeys;
    @Field("leftKeys")
    private String leftKeys;


    public MongoGroupBySamplePo() {}

    public MongoGroupBySamplePo(String startTime, String endTime, String groupByKeys, String leftKeys) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.groupByKeys = groupByKeys;
        this.leftKeys = leftKeys;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getGroupByKeys() {
        return groupByKeys;
    }

    public void setGroupByKeys(String groupByKeys) {
        this.groupByKeys = groupByKeys;
    }

    public String getLeftKeys() {
        return leftKeys;
    }

    public void setLeftKeys(String leftKeys) {
        this.leftKeys = leftKeys;
    }
}
