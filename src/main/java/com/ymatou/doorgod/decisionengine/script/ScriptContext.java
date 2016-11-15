/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.script;

import com.ymatou.doorgod.decisionengine.model.StatisticItem;

import java.util.Map;
import java.util.TreeMap;

/**
 * script 上下文
 * @author luoshiqian 2016/10/20 15:33
 */
public class ScriptContext {

    private String uri;

    // 请求时间:yyyyMMddHHmmss
    private String reqTime;

    // 耗时:以毫秒为单位
    private long consumedTime;

    // http响应码
    private int statusCode;

    private boolean rejectedByFilter;

    private boolean rejectedByHystrix;

    private String hitRule;

    private long filterConsumedTime;

    private int origStatusCode;

    private TreeMap<String, String> dimensionValues = new TreeMap<String, String>();

    public ScriptContext(StatisticItem statisticItem) {
        this.uri = statisticItem.getUri();
        this.reqTime = statisticItem.getReqTime();
        this.consumedTime = statisticItem.getConsumedTime();
        this.statusCode = statisticItem.getStatusCode();
        this.rejectedByFilter = statisticItem.isRejectedByFilter();
        this.rejectedByHystrix = statisticItem.isRejectedByHystrix();
        this.hitRule = statisticItem.getHitRule();
        this.filterConsumedTime = statisticItem.getFilterConsumedTime();
        this.origStatusCode = statisticItem.getOrigStatusCode();
        if(null != statisticItem.getNewSample()){
            dimensionValues.putAll(statisticItem.getNewSample().getDimensionValues());
        }
    }

    public String get(String key) {
        return dimensionValues.get(key);
    }

    public void put(String key, String value) {
        dimensionValues.put(key, value);
    }

    public void putAll(Map<String, String> map) {
        dimensionValues.putAll(map);
    }


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getReqTime() {
        return reqTime;
    }

    public void setReqTime(String reqTime) {
        this.reqTime = reqTime;
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

    public TreeMap<String, String> getDimensionValues() {
        return dimensionValues;
    }

    public void setDimensionValues(TreeMap<String, String> dimensionValues) {
        this.dimensionValues = dimensionValues;
    }
}
