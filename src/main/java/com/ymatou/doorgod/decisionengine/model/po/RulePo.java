/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.model.po;


/**
 * @author luoshiqian 2016/9/12 16:50
 */
public class RulePo {

    private Long id;
    private String name;
    private String desc;


    // 统计时间段(秒计)，譬如60s
    private int statisticSpan;

    // 统计策略(cron表达式)
    private String statisticStrategy;

    // 统计时间段内，访问次数达到该上限（inclusive），即触发限制
    private long timesCap;

    // 达到上限后，拒绝访问的时长，以秒计，譬如300s
    private int rejectionSpan;

    private String updateType;

    private boolean isUpdate;

    /**
     * @see com.ymatou.doorgod.decisionengine.model.ScopeEnum
     */
    private String scope;

    private String keys;
    private String groupByKeys;

    private String ruleType;// 限次 黑名单

}
