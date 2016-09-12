/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.service.job.RuleDiscoverer;

/**
 * 特定的定时任务配置（Rule更新扫描， Redis数据入MongoDB）
 * 
 * @author qianmin 2016年9月12日 下午3:09:29
 * 
 */
@Configuration
public class SchdulerConfig {

    @Autowired
    private BizProps bizProps;

    @Autowired
    private RuleDiscoverer ruleDiscoverer;

    @Bean
    @DependsOn("disconfMgrBean2")
    public MethodInvokingJobDetailFactoryBean ruleDiscoverJobDetail() {
        MethodInvokingJobDetailFactoryBean ruleDiscoverJobDatil = new MethodInvokingJobDetailFactoryBean();
        ruleDiscoverJobDatil.setTargetObject(ruleDiscoverer);
        ruleDiscoverJobDatil.setTargetMethod("execute");

        return ruleDiscoverJobDatil;
    }

    @Bean
    public CronTriggerFactoryBean ruleDiscoverTrigger() {
        CronTriggerFactoryBean ruleDiscoverTrigger = new CronTriggerFactoryBean();
        ruleDiscoverTrigger.setJobDetail(ruleDiscoverJobDetail().getObject());
        ruleDiscoverTrigger.setCronExpression(bizProps.getRuleDiscoverCronExpression());
        return ruleDiscoverTrigger;
    }

    @Bean(name = "ruleDiscoverScheduler")
    public SchedulerFactoryBean ruleDiscoverScheduler() {
        SchedulerFactoryBean ruleDiscoverScheduler = new SchedulerFactoryBean();
        ruleDiscoverScheduler.setTriggers(ruleDiscoverTrigger().getObject());
        return ruleDiscoverScheduler;
    }
}
