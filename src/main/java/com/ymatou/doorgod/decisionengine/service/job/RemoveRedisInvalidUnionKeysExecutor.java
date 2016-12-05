/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job;

import java.time.LocalDateTime;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Sets;
import com.ymatou.doorgod.decisionengine.util.DateUtils;


@Component
public class RemoveRedisInvalidUnionKeysExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RemoveRedisInvalidUnionKeysExecutor.class);

    private static final String KEY_SPLIT = ":";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void execute() {

        String keysPattern = "doorgod:*Union";
        Set<String> keys = stringRedisTemplate.keys(keysPattern);

        Set<String> needRemoveKeys = Sets.newHashSet();
        // union set name: doorgod:ruleName:set:time:Union
        long now = Long.valueOf(DateUtils.formatDefault(LocalDateTime.now().minusHours(1)));
        if (!CollectionUtils.isEmpty(keys)) {
            keys.forEach(s -> {
                String[] splits = s.split(KEY_SPLIT);
                if (splits.length == 5) {
                    String time = splits[3];

                    // 1小时之前的key都是需要删除的
                    if (Long.valueOf(time) < now) {
                        needRemoveKeys.add(s);
                    }
                }
            });
        }

        if (needRemoveKeys.size() > 0) {
            stringRedisTemplate.delete(needRemoveKeys);
        }

        logger.info("RemoveRedisInvalidUnionKeysExecutor need remove key size:{}", needRemoveKeys.size());
    }

}
