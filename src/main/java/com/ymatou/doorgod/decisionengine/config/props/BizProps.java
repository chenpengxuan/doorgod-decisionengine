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
    // 定时发现Rule变化
    private String ruleDiscoverCronExpression;
    // 定时同步Redis数据到Mongo
    private String rulePersistenceCronExpression;
    // 定时统计Redis数据生成黑名单
    private String ruleExecutorCronExpression;
    // 上报redis topN >0 才topN
    private int uploadRedisTopN;
    // 每秒 每个rule map中最多元素个数
    private int maxSizePerSecAndRule;

    @DisconfFileItem(name = "biz.ruleDiscoverCronExpression")
    public String getRuleDiscoverCronExpression() {
        return ruleDiscoverCronExpression;
    }

    public void setRuleDiscoverCronExpression(String ruleDiscoverCronExpression) {
        this.ruleDiscoverCronExpression = ruleDiscoverCronExpression;
    }

    @DisconfFileItem(name = "biz.rulePersistenceCronExpression")
    public String getRulePersistenceCronExpression() {
        return rulePersistenceCronExpression;
    }

    public void setRulePersistenceCronExpression(String rulePersistenceCronExpression) {
        this.rulePersistenceCronExpression = rulePersistenceCronExpression;
    }

    @DisconfFileItem(name = "biz.ruleExecutorCronExpression")
    public String getRuleExecutorCronExpression() {
        return ruleExecutorCronExpression;
    }

    public void setRuleExecutorCronExpression(String ruleExecutorCronExpression) {
        this.ruleExecutorCronExpression = ruleExecutorCronExpression;
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
