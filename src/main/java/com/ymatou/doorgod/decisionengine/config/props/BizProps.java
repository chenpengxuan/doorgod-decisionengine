/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.config.props;

import org.springframework.stereotype.Component;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;

/**
 * 
 * @author qianmin 2016年9月6日 下午6:54:41
 * 
 */
@Component
@DisconfFile(fileName = "biz.properties")
public class BizProps {

    private String ruleDiscoverCronExpression;

    @DisconfFileItem(name = "biz.ruleDiscoverCronExpression")
    public String getRuleDiscoverCronExpression() {
        return ruleDiscoverCronExpression;
    }

    public void setRuleDiscoverCronExpression(String ruleDiscoverCronExpression) {
        this.ruleDiscoverCronExpression = ruleDiscoverCronExpression;
    }
}
