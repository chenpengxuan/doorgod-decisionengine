/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model.po;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author luoshiqian 2016/9/12 16:50
 */
@Entity
@Table(name = "rule")
public class RulePo extends Audit {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String desc;

    @Column(name = "statistic_span")
    private Integer statisticSpan;

    @Column(name = "times_cap")
    private Long timesCap;

    @Column(name = "rejection_span")
    private Integer rejectionSpan;

    @Column(name = "`keys`")
    private String keys;

    @Column(name = "groupby_keys")
    private String groupByKeys;

    @Column(name = "counting_keys")
    private String countingKeys;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(name = "`order`")
    private Integer order;

    @Column(name = "uris")
    private String uris;

    @Column(name = "match_script")
    private String matchScript;

    @Column(name = "groupby_count")
    private Integer groupByCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getStatisticSpan() {
        return statisticSpan;
    }

    public void setStatisticSpan(Integer statisticSpan) {
        this.statisticSpan = statisticSpan;
    }

    public Long getTimesCap() {
        return timesCap;
    }

    public void setTimesCap(Long timesCap) {
        this.timesCap = timesCap;
    }

    public Integer getRejectionSpan() {
        return rejectionSpan;
    }

    public void setRejectionSpan(Integer rejectionSpan) {
        this.rejectionSpan = rejectionSpan;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getGroupByKeys() {
        return groupByKeys;
    }

    public void setGroupByKeys(String groupByKeys) {
        this.groupByKeys = groupByKeys;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getUris() {
        return uris;
    }

    public void setUris(String uris) {
        this.uris = uris;
    }

    public String getCountingKeys() {
        return countingKeys;
    }

    public void setCountingKeys(String countingKeys) {
        this.countingKeys = countingKeys;
    }

    public String getMatchScript() {
        return matchScript;
    }

    public void setMatchScript(String matchScript) {
        this.matchScript = matchScript;
    }

    public Integer getGroupByCount() {
        if (groupByCount == null){
            return 0;
        }
        return groupByCount;
    }

    public void setGroupByCount(Integer groupByCount) {
        this.groupByCount = groupByCount;
    }
}
