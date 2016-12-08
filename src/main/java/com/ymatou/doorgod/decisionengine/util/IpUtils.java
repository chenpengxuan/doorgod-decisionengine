/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.constants.Constants;

/**
 * @author luoshiqian 2016/11/2 18:16
 */
@Component
public class IpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpUtils.class);

    private List<String> ipRegexList = new ArrayList<>();
    private String ipRegexCacheStr = "";
    @Autowired
    private BizProps bizProps;
    @Autowired
    private CacheManager cacheManager;

    @Cacheable("ipGuavaCache")
    public boolean isIgnore(String ip) {

        for (String ipRegex : ipRegexList) {
            if (Pattern.matches(ipRegex, ip)) {
                return true;
            }
        }
        return false;
    }

    @PostConstruct
    public void init(){
        resetCache(bizProps.getIgnoreRegexIps());
    }

    public void reload() {
        if (!bizProps.getIgnoreRegexIps().equals(ipRegexCacheStr)) {
            resetCache(bizProps.getIgnoreRegexIps());// 重新设置
        }
    }

    public void resetCache(String ignoreRegexIps) {
        List<String> newIpRegexList = new ArrayList<>();

        if(StringUtils.isNotBlank(ignoreRegexIps)){
            for (String ipRegex : ignoreRegexIps.split(Constants.SEPARATOR)) {
                String temp = ipRegex.trim();
                if(StringUtils.isNotBlank(temp)){
                    newIpRegexList.add(temp);
                }
            }
        }
        resetCache(newIpRegexList);
        ipRegexCacheStr = ignoreRegexIps;
    }

    public void resetCache(List<String> newIpRegexList) {
        ipRegexList = newIpRegexList;
        cacheManager.getCache("ipGuavaCache").clear();

        LOGGER.info("resetcache ipRegexCacheStr:{},newIgnoreRegexIps:{}",ipRegexCacheStr,bizProps.getIgnoreRegexIps());
    }
}
