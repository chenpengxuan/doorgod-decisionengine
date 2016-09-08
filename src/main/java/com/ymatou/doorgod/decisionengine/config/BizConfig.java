/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.config;

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
public class BizConfig {

    private String redisHost;

    private Long ipCountThreshold;

    @DisconfFileItem(name = "ipCount.Threshold")
    public Long getIpCountThreshold() {
        return ipCountThreshold;
    }

    public void setIpCountThreshold(Long ipCountThreshold) {
        this.ipCountThreshold = ipCountThreshold;
    }

    @DisconfFileItem(name = "redis.host")
    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }
}
