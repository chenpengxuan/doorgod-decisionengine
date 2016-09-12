/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author luoshiqian 2016/9/12 11:32
 */
public class Rule {

    private String name;
    private Set<RuleUri> ruleUriSet;

    private Set<String> dimensionKeys = new HashSet<String>( );

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<RuleUri> getRuleUriSet() {
        return ruleUriSet;
    }

    public void setRuleUriSet(Set<RuleUri> ruleUriSet) {
        this.ruleUriSet = ruleUriSet;
    }

    public Set<String> getDimensionKeys() {
        return dimensionKeys;
    }

    public void setDimensionKeys(Set<String> dimensionKeys) {
        this.dimensionKeys = dimensionKeys;
    }
}
