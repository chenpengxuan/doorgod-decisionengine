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
    //上报redis topN >0 才topN
    private int uploadRedisTopN;
    //每秒 每个rule map中最多元素个数
    private int maxSizePerSecAndRule;

    @DisconfFileItem(name = "biz.ruleDiscoverCronExpression")
    public String getRuleDiscoverCronExpression() {
        return ruleDiscoverCronExpression;
    }

    public void setRuleDiscoverCronExpression(String ruleDiscoverCronExpression) {
        this.ruleDiscoverCronExpression = ruleDiscoverCronExpression;
    }




    @DisconfFileItem(name = "biz.uploadRedisTopN")
    public int getUploadRedisTopN() {
        return uploadRedisTopN;
    }

    public void setUploadRedisTopN(int uploadRedisTopN) {
        this.uploadRedisTopN = uploadRedisTopN;
    }

    @DisconfFileItem(name = "biz.maxSizePerSecAndRule")
    public int getMaxSizePerSecAndRule() {
        return maxSizePerSecAndRule;
    }

    public void setMaxSizePerSecAndRule(int maxSizePerSecAndRule) {
        this.maxSizePerSecAndRule = maxSizePerSecAndRule;
    }
}
