/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.config;

import com.baidu.disconf.client.DisconfMgrBean;
import com.baidu.disconf.client.DisconfMgrBeanSecond;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author luoshiqian 2016/8/30 15:49
 */
@Configuration
public class DisconfMgr {


    @Bean(name = "disconfMgrBean", destroyMethod = "destroy")
    public DisconfMgrBean disconfMgrBean() {

        DisconfMgrBean disconfMgrBean = new DisconfMgrBean();
        disconfMgrBean.setScanPackage("com.ymatou.doorgod.decisionengine");

        return disconfMgrBean;
    }

    @Bean(name = "disconfMgrBean2", destroyMethod = "destroy")
    public DisconfMgrBeanSecond disconfMgrBean2(DisconfMgrBean disconfMgrBean) {
        DisconfMgrBeanSecond disconfMgrBeanSecond = new DisconfMgrBeanSecond();
        try {
            disconfMgrBeanSecond.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return disconfMgrBeanSecond;
    }

}
