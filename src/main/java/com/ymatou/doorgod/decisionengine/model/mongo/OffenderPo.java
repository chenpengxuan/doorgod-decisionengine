/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.model.mongo;

import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.model.Sample;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 
 * @author qianmin 2016年9月14日 下午6:54:45
 * 
 */
@Document(collection = Constants.COLLECTION_NAME_LIMIT_TIMES_RULE_OFFENDER)
public class OffenderPo {

    @Id
    private String id;

    @Field("ruleName")
    private String ruleName;

    @Field("sample")
    private Sample sample;

    @Field("addTime")
    private String addTime;

    @Field("releaseDate")
    private Long releaseDate;

    public OffenderPo() {}

    public OffenderPo(Sample sample, Long releaseDate) {
        this.sample = sample;
        this.releaseDate = releaseDate;
    }

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

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Long getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Long releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }
}
