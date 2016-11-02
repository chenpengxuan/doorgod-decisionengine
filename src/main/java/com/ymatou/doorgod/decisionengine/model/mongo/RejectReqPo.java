/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model.mongo;

import com.ymatou.doorgod.decisionengine.model.Sample;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * @author luoshiqian 2016/9/14 16:24
 */

@Document(collection = "RejectReq")
public class RejectReqPo {

    @Id
    private String id;

    @Field("sample")
    private Sample sample;

    @Field("rejectTime")
    private String rejectTime;

    @Field("ruleName")
    private String ruleName;

    @Field("count")
    private Long count;

    @Field("addTime")
    private Date addTime;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public String getRejectTime() {
        return rejectTime;
    }

    public void setRejectTime(String rejectTime) {
        this.rejectTime = rejectTime;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }
}
