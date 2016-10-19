/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model;

/**
 * Created by tuwenjie on 2016/9/9.
 */
public class StatisticItem {

    private String uri;

    private Sample sample;

    //请求时间:yyyyMMddHHmmss
    private String reqTime;

    //耗时:以毫秒为单位
    private long consumedTime;

    //http响应码
    private int statusCode;

    private boolean rejectedByFilter;

    private boolean rejectedByHystrix;

    private String hitRule;

    private long filterConsumedTime;

    private int origStatusCode;

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public String getReqTime() {
        return reqTime;
    }

    public void setReqTime(String reqTime) {
        this.reqTime = reqTime;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getConsumedTime() {
        return consumedTime;
    }

    public void setConsumedTime(long consumedTime) {
        this.consumedTime = consumedTime;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isRejectedByFilter() {
        return rejectedByFilter;
    }

    public void setRejectedByFilter(boolean rejectedByFilter) {
        this.rejectedByFilter = rejectedByFilter;
    }

    public boolean isRejectedByHystrix() {
        return rejectedByHystrix;
    }

    public void setRejectedByHystrix(boolean rejectedByHystrix) {
        this.rejectedByHystrix = rejectedByHystrix;
    }

    public String getHitRule() {
        return hitRule;
    }

    public void setHitRule(String hitRule) {
        this.hitRule = hitRule;
    }

    public long getFilterConsumedTime() {
        return filterConsumedTime;
    }

    public void setFilterConsumedTime(long filterConsumedTime) {
        this.filterConsumedTime = filterConsumedTime;
    }

    public int getOrigStatusCode() {
        return origStatusCode;
    }

    public void setOrigStatusCode(int origStatusCode) {
        this.origStatusCode = origStatusCode;
    }
}
