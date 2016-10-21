package com.ymatou.doorgod.decisionengine.model;

import java.util.HashSet;
import java.util.Set;

/**
 * 限次规则 Created by tuwenjie on 2016/9/7.
 */
public class LimitTimesRule extends AbstractRule {

    // 统计时间段(秒计)，譬如60s
    private int statisticSpan;

    // 统计时间段内，访问次数达到该上限（inclusive），即触发限制
    private long timesCap;

    // 达到上限后，拒绝访问的时长，以秒计，譬如300s
    private int rejectionSpan;

    private String updateType;

    // 需要计数的 keys
    private Set<String> countingKeys;

    private String matchScript;

    /**
     * 用来从http请求提取样本的样本维度KEY
     */
    private Set<String> dimensionKeys = new HashSet<String>();

    /**
     * <code>dimensionKeys</code>子集，用来定义次数基于那些keys统计
     * <code>null</code>或空，代表次数基于完整的<code>dimensionKeys</code>统计
     */
    private Set<String> groupByKeys = new HashSet<String>();

    public int getStatisticSpan() {
        return statisticSpan;
    }

    public void setStatisticSpan(int statisticSpan) {
        this.statisticSpan = statisticSpan;
    }

    public long getTimesCap() {
        return timesCap;
    }

    public void setTimesCap(long timesCap) {
        this.timesCap = timesCap;
    }

    public int getRejectionSpan() {
        return rejectionSpan;
    }

    public void setRejectionSpan(int rejectionSpan) {
        this.rejectionSpan = rejectionSpan;
    }

    public Set<String> getDimensionKeys() {
        return dimensionKeys;
    }

    public void setDimensionKeys(Set<String> dimensionKeys) {
        this.dimensionKeys = dimensionKeys;
    }

    public Set<String> getGroupByKeys() {
        return groupByKeys;
    }

    public void setGroupByKeys(Set<String> groupByKeys) {
        this.groupByKeys = groupByKeys;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public Set<String> getCountingKeys() {
        return countingKeys;
    }

    public void setCountingKeys(Set<String> countingKeys) {
        this.countingKeys = countingKeys;
    }

    public String getMatchScript() {
        return matchScript;
    }

    public void setMatchScript(String matchScript) {
        this.matchScript = matchScript;
    }
}
