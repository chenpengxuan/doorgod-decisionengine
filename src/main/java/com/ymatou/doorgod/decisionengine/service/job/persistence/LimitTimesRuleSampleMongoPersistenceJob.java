/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job.persistence;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * @author qianmin 2016年9月12日 上午11:05:19
 * 
 */
public class LimitTimesRuleSampleMongoPersistenceJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(LimitTimesRuleSampleMongoPersistenceJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LimitTimesRuleSampleMongoPersistenceExecutor executor =
                SpringContextHolder.getBean(LimitTimesRuleSampleMongoPersistenceExecutor.class);
        try {
            executor.execute(context);
        } catch (Exception e) {
            logger.error("error exec LimitTimesRuleSampleMongoPersistenceExecutor rule:{}",
                    context.getJobDetail().getKey().getName(), e);
        }
    }
}
