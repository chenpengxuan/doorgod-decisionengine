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

    @Column(name = "desc")
    private String desc;

    @Column(name = "statistic_span")
    private int statisticSpan;

    @Column(name = "times_cap")
    private long timesCap;

    @Column(name = "rejection_span")
    private int rejectionSpan;

    @Column(name = "update_type")
    private String updateType;

    /**
     * @see com.ymatou.doorgod.decisionengine.model.ScopeEnum
     */
    @Column(name = "scope")
    private String scope;

    @Column(name = "key")
    private String keys;

    @Column(name = "group_key")
    private String groupByKeys;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(name = "order")
    private int order;

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

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
