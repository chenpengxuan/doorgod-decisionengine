/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model.mongo;

import java.util.Date;

/**
 * @author luoshiqian 2016/9/14 16:24
 */

public class MongoGroupBySamplePo {

    private String id;

    private String sampleTime;

    private String groupByKeys;

    private String leftKeys;

    private Long count;

    private Date addTime;


    public String getSampleTime() {
        return sampleTime;
    }

    public void setSampleTime(String sampleTime) {
        this.sampleTime = sampleTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
