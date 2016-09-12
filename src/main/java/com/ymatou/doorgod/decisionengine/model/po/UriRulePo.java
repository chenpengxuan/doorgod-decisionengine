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
 * @author luoshiqian 2016/9/12 16:58
 */
@Entity
@Table(name = "uri_rule")
public class UriRulePo extends Audit {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uri")
    private String uri;

    @Column(name = "rule_id")
    private Long ruleId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }
}
