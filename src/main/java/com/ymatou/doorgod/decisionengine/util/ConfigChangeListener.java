/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baidu.disconf.client.common.annotations.DisconfUpdateService;
import com.baidu.disconf.client.common.update.IDisconfUpdate;

/**
 * @author luoshiqian 2016/11/2 18:33
 */
@DisconfUpdateService(confFileKeys = {"biz.properties"})
@Component
public class ConfigChangeListener implements IDisconfUpdate {

    @Autowired
    private IpUtils ipUtils;

    @Override
    public void reload() throws Exception {
        ipUtils.reload();
    }
}
