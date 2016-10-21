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

    // 定时同步 限次规则 Redis数据到Mongo
    private String limitTimesRuleSamplePersistCronExpr;
    // 定时统计 限次规则 生成黑名单
    private String limitTimesRuleSampleCronExpr;
    // 定时统计 限次规则 groupby 数据生成黑名单
    private String limitTimesRuleGroupBySampleCronExpr;
    // 上报redis topN >0 才topN
    private int uploadRedisTopN;
    // 上报mongo topN >0 才topN
    private int uploadMongoTopN;

    // 上传redis定时器频率 单位：毫秒
    private int uploadRedisTimerInterval;

    // 上传mongo定时器频率 单位：毫秒
    private int uploadMongoTimerInterval;

    // 每秒 每个rule map中最多元素个数
    private int maxSizePerSecAndRule;

    // 每次持久化前N个Sample
    private int presistToMongoTopN;

    //将sample放入redis 或 mongo 线程数
    private int putSampleThreadNums;

    //端口号
    private int port;

    private String performanceServerUrl;



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

    @DisconfFileItem(name = "biz.uploadMongoTopN")
    public int getUploadMongoTopN() {
        return uploadMongoTopN;
    }

    public void setUploadMongoTopN(int uploadMongoTopN) {
        this.uploadMongoTopN = uploadMongoTopN;
    }

    @DisconfFileItem(name = "biz.uploadRedisTimerInterval")
    public int getUploadRedisTimerInterval() {
        return uploadRedisTimerInterval;
    }

    public void setUploadRedisTimerInterval(int uploadRedisTimerInterval) {
        this.uploadRedisTimerInterval = uploadRedisTimerInterval;
    }

    @DisconfFileItem(name = "biz.uploadMongoTimerInterval")
    public int getUploadMongoTimerInterval() {
        return uploadMongoTimerInterval;
    }

    public void setUploadMongoTimerInterval(int uploadMongoTimerInterval) {
        this.uploadMongoTimerInterval = uploadMongoTimerInterval;
    }


    @DisconfFileItem(name = "biz.presistToMongoTopN")
    public int getPresistToMongoTopN() {
        return presistToMongoTopN;
    }

    public void setPresistToMongoTopN(int presistToMongoTopN) {
        this.presistToMongoTopN = presistToMongoTopN;
    }


    @DisconfFileItem(name = "biz.putSampleThreadNums")
    public int getPutSampleThreadNums() {
        return putSampleThreadNums;
    }

    public void setPutSampleThreadNums(int putSampleThreadNums) {
        this.putSampleThreadNums = putSampleThreadNums;
    }

    @DisconfFileItem(name = "biz.server.port")
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    @DisconfFileItem(name = "performance.server.url")
    public String getPerformanceServerUrl() {
        return performanceServerUrl;
    }

    public void setPerformanceServerUrl(String performanceServerUrl) {
        this.performanceServerUrl = performanceServerUrl;
    }

    @DisconfFileItem(name = "biz.limitTimesRuleSamplePersistCronExpr")
    public String getLimitTimesRuleSamplePersistCronExpr() {
        return limitTimesRuleSamplePersistCronExpr;
    }

    public void setLimitTimesRuleSamplePersistCronExpr(String limitTimesRuleSamplePersistCronExpr) {
        this.limitTimesRuleSamplePersistCronExpr = limitTimesRuleSamplePersistCronExpr;
    }

    @DisconfFileItem(name = "biz.limitTimesRuleSampleCronExpr")
    public String getLimitTimesRuleSampleCronExpr() {
        return limitTimesRuleSampleCronExpr;
    }

    public void setLimitTimesRuleSampleCronExpr(String limitTimesRuleSampleCronExpr) {
        this.limitTimesRuleSampleCronExpr = limitTimesRuleSampleCronExpr;
    }

    @DisconfFileItem(name = "biz.limitTimesRuleGroupBySampleCronExpr")
    public String getLimitTimesRuleGroupBySampleCronExpr() {
        return limitTimesRuleGroupBySampleCronExpr;
    }

    public void setLimitTimesRuleGroupBySampleCronExpr(String limitTimesRuleGroupBySampleCronExpr) {
        this.limitTimesRuleGroupBySampleCronExpr = limitTimesRuleGroupBySampleCronExpr;
    }
}
