/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.service.job.offender;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * 
 * @author luoshiqian
 * 
 */
@DisallowConcurrentExecution
public class LimitTimesRuleGroupBySampleOffendersJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(LimitTimesRuleGroupBySampleOffendersJob.class);


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        LimitTimesRuleGroupBySampleOffendersExecutor executor =
                SpringContextHolder.getBean(LimitTimesRuleGroupBySampleOffendersExecutor.class);
        try {
            executor.execute(context);
        } catch (Exception e) {
            logger.error("exec LimitTimesRuleGroupBySampleOffendersJob error", e);
        }
    }
}
