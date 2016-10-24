/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.config;

import javax.sql.DataSource;

import com.baidu.disconf.client.DisconfMgrBeanSecond;
import com.baidu.disconf.client.config.DisClientConfig;
import com.ymatou.doorgod.decisionengine.constants.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * 数据库定时任务配置
 * 
 * @author qianmin 2016年8月18日 下午3:53:16
 *
 */
@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBean scheduler(DataSource dataSource, DisconfMgrBeanSecond disconfMgrBean2) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setDataSource(dataSource);
        // schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContextKey");
        schedulerFactoryBean.setConfigLocation(new ClassPathResource("quartz.properties"));
        schedulerFactoryBean.setAutoStartup(true);

        if(DisClientConfig.getInstance().ENV.equals(Constants.ENV_STG)){
            schedulerFactoryBean.setSchedulerName("STG-scheduler");
        }
        return schedulerFactoryBean;
    }


}
