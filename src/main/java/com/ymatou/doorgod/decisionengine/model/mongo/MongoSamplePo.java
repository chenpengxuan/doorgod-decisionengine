/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.model.mongo;

import com.ymatou.doorgod.decisionengine.model.Sample;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 
 * @author qianmin 2016年9月18日 上午11:02:52
 * 
 */
@Document(collection = "sample")
public class MongoSamplePo {

    @Id
    private String id;

    @Field("ruleName")
    private String ruleName;

    @Field("sample")
    private Sample sample;

    @Field("count")
    private Double count;

    @Field("time")
    private String time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
