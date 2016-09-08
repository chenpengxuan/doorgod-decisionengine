/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.integration;

import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.config.BizConfig;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * 
 * @author qianmin 2016年9月6日 下午7:36:00
 * 
 */
@Component
public class IpCountHandler implements Handler<AsyncResult<Long>> {

    private BizConfig BizConfig;

    @Override
    public void handle(AsyncResult<Long> event) {
        if (event.succeeded()) {
            if (event.result() > BizConfig.getIpCountThreshold()) {

            }
        }
        // event.result()

    }

}
