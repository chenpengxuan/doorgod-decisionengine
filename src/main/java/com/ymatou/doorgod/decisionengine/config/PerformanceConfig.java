/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.performancemonitorclient.PerformanceMonitorAdvice;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 性能监控配置
 * 
 * @author luoshiqian
 *
 */
@Aspect
@Configuration
public class PerformanceConfig {

    @Autowired
    private BizProps bizProps;

    @Bean(name = "performanceMonitorAdvice")
    @DependsOn("disconfMgrBean2")
    public PerformanceMonitorAdvice performanceMonitorAdvice(){
        PerformanceMonitorAdvice performanceMonitorAdvice = new PerformanceMonitorAdvice();
        performanceMonitorAdvice.setAppId("com.ymatou.doorgod.decisionengine");
        performanceMonitorAdvice.setServerUrl(bizProps.getPerformanceServerUrl());
        return performanceMonitorAdvice;
    }

    @Bean(name = "performancePointcut")
    public AspectJExpressionPointcut aspectJExpressionPointcut(){
        AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();

        aspectJExpressionPointcut.setExpression(
                "execution(* com.ymatou.doorgod.decisionengine.integration..*.*(..))"+
                "execution(* com.ymatou.doorgod.decisionengine.integration.service..*.*(..))"+
                "|| execution(* com.ymatou.doorgod.decisionengine.repository.*Repository.*(..))"
        );

        return aspectJExpressionPointcut;
    }


    /**
     * 对应xml
     *  <aop:config>
     *    <aop:advisor advice-ref="performanceMonitorAdvice"
     *        pointcut-ref="performancePointcut" />
     *   </aop:config>
     * @return
     */
    @Bean
    public Advisor performanceMonitorAdvisor(){
        return new DefaultPointcutAdvisor(aspectJExpressionPointcut(),performanceMonitorAdvice());
    }

}
