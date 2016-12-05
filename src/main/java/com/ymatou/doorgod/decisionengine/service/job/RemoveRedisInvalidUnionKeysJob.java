/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

public class RemoveRedisInvalidUnionKeysJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(RemoveRedisInvalidUnionKeysJob.class);


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        RemoveRedisInvalidUnionKeysExecutor removeRedisInvalidUnionKeysExecutor = SpringContextHolder.getBean(RemoveRedisInvalidUnionKeysExecutor.class);
        try {
            removeRedisInvalidUnionKeysExecutor.execute();
        } catch (Exception e) {
            logger.error("error exec RemoveRedisInvalidUnionKeysJob rule:{}",
                    context.getJobDetail().getKey().getName(), e);
        }
    }
}
