/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model;

/**
 * 请求被拒绝时间
 * Created by tuwenjie on 2016/9/8.
 */
public class RejectReqEvent {

    //"yyyyMMddHHmmss"
    private String time;

    private String ruleName;

    private Sample sample;

    private String uri;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
