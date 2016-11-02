/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model;

import com.alibaba.fastjson.JSON;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * 收到kafka数据样本 Created by tuwenjie on 2016/9/7.
 */
public class Sample {

    public static final String NULL_VALUE_PLACEHOLDER = "NULL_FLAG";


    // 样本值
    private TreeMap<String, String> dimensionValues = new TreeMap<String, String>();


    public void addDimensionValue(String key, String value) {
        if (key != null && key.trim().length() > 0) {
            if (value != null) {
                dimensionValues.put(key, value);
            } else {
                dimensionValues.put(key, NULL_VALUE_PLACEHOLDER);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Sample sample = (Sample) o;

        return dimensionValues != null ? dimensionValues.equals(sample.dimensionValues)
                : sample.dimensionValues == null;

    }

    @Override
    public int hashCode() {
        return dimensionValues != null ? dimensionValues.hashCode() : 0;
    }

    public Sample narrow(Set<String> subKeys) {
        Sample sample = new Sample();
        subKeys.forEach(s -> {
            sample.addDimensionValue(s, dimensionValues.get(s));
        });

        return sample;
    }

    /**
     * 反向
     * @param subKeys
     * @return
     */
    public Sample unNarrow(Set<String> subKeys) {
        Sample sample = new Sample();

        dimensionValues.forEach((key, val) -> {
            if(!subKeys.contains(key)){
                sample.addDimensionValue(key, val);
            }
        });
        return sample;
    }

    public TreeMap<String, String> getDimensionValues() {
        return dimensionValues;
    }

    public void setDimensionValues(TreeMap<String, String> dimensionValues) {
        this.dimensionValues = dimensionValues;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static Sample fromJsonStr(String str){
        return JSON.parseObject(str,Sample.class);
    }
}
