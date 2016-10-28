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
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.util.SpringContextHolder;

/**
 * @author qianmin 2016年9月12日 上午11:04:36
 * @author luoshiqian
 * @author tuwenjie
 * 
 */
@DisallowConcurrentExecution
@Component
public class LimitTimesRuleSampleOffendersJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(LimitTimesRuleSampleOffendersJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        LimitTimesRuleSampleOffendersExecutor executor =
                SpringContextHolder.getBean(LimitTimesRuleSampleOffendersExecutor.class);
        try {
            executor.execute(context);
        } catch (Exception e) {
            logger.error("error exec LimitTimesRuleSampleOffendersJob rule:{}",
                    context.getJobDetail().getKey().getName(), e);
        }
    }

}
