/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 
 * @author qianmin 2016年9月14日 下午6:54:45
 * 
 */
@Document(collection = "offender")
public class OffenderPo {

    @Id
    private String id;

    @Field("sample")
    private String sample;

    @Field("addTime")
    private String addTime;

    @Field("rejectTime")
    private String rejectTime;

    public OffenderPo() {}

    public OffenderPo(String sample, String rejectTime) {
        this.sample = sample;
        this.rejectTime = rejectTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public String getRejectTime() {
        return rejectTime;
    }

    public void setRejectTime(String rejectTime) {
        this.rejectTime = rejectTime;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }
}
