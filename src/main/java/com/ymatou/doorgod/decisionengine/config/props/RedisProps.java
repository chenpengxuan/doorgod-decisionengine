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
 * redis属性配置
 *
 */
@Component
@DisconfFile(fileName = "redis.properties")
public class RedisProps {

    private String masterName;
    private String sentinelAddress;//ip:port,ip:port

    private int maxTotal;

    private boolean usePool;

    private int dbIndex;// database index

    private int timeout;

    @DisconfFileItem(name = "redis.masterName")
    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    @DisconfFileItem(name = "redis.maxTotal")
    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    @DisconfFileItem(name = "redis.usePool")
    public boolean isUsePool() {
        return usePool;
    }

    public void setUsePool(boolean usePool) {
        this.usePool = usePool;
    }

    @DisconfFileItem(name = "redis.sentinelAddress")
    public String getSentinelAddress() {
        return sentinelAddress;
    }

    public void setSentinelAddress(String sentinelAddress) {
        this.sentinelAddress = sentinelAddress;
    }

    @DisconfFileItem(name = "redis.dbIndex")
    public int getDbIndex() {

        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    @DisconfFileItem(name = "redis.timeout")
    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
