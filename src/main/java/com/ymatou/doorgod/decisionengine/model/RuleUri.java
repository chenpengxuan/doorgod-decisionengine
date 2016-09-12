/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 规则uri
 *   aliasForKeys:
 *               key: dimensionKeys 样本维护的key
 *               value: alias 别名
 * @author luoshiqian 2016/9/12 12:32
 */
public class RuleUri {

    private String uri;

    private Map<String,String> aliasForKeys = new HashMap<>();

    private Set<String> dimensionKeys;//包括rule 的keys ，以及修改key 使用别名替换， set的时候放进去

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


    public Set<String> getDimensionKeys() {
        return dimensionKeys;
    }

    public void setDimensionKeys(Set<String> dimensionKeys) {
        this.dimensionKeys = dimensionKeys;
    }

    public Map<String, String> getAliasForKeys() {
        return aliasForKeys;
    }

    public void setAliasForKeys(Map<String, String> aliasForKeys) {
        this.aliasForKeys = aliasForKeys;
    }
}
