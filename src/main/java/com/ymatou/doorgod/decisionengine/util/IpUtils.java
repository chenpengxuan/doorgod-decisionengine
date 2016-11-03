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

    public static List<String> ipRegexList = new ArrayList<>();
    public static String ipRegexCacheStr;
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
        if (StringUtils.isNotBlank(bizProps.getIgnoreRegexIps())) {
            ipRegexCacheStr = bizProps.getIgnoreRegexIps();
            reSetCache(bizProps.getIgnoreRegexIps());
        }
    }

    public void reload() {
        // 去掉了ip ignore
        if (StringUtils.isNotBlank(IpUtils.ipRegexCacheStr) && StringUtils.isBlank(bizProps.getIgnoreRegexIps())) {
            reSetCache(new ArrayList<>()); //清空
        } else if (StringUtils.isNotBlank(bizProps.getIgnoreRegexIps())
                && !bizProps.getIgnoreRegexIps().equals(ipRegexCacheStr)) {
            reSetCache(bizProps.getIgnoreRegexIps());//重新设置
        }
    }

    public void reSetCache(String ignoreRegexIps) {
        List<String> newIpRegexList = new ArrayList<>();
        for (String ipRegex : ignoreRegexIps.split(Constants.SEPARATOR)) {
            newIpRegexList.add(ipRegex);
        }
        reSetCache(newIpRegexList);
    }

    public void reSetCache(List<String> newIpRegexList) {
        IpUtils.ipRegexList = newIpRegexList;
        cacheManager.getCache("ipGuavaCache").clear();
    }
}
