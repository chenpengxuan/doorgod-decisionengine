/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service;

import java.util.Date;

import org.quartz.Job;
import org.quartz.SchedulerException;

/**
 * 
 * @author qianmin 2016年8月18日 下午3:03:48
 *
 */
public interface SchedulerService {

    void initScheduler();

    void addJob(Class<? extends Job> job, String jobName, String cronExpression) throws SchedulerException;

    void modifyScheduler(String jobName, String cronExpression) throws SchedulerException;

    void pauseScheduler(String jobName) throws SchedulerException;

    void resumeScheduler(String jobName) throws SchedulerException;

    void removeScheduler(String jobName) throws SchedulerException;

    Date getNextFireTime(String jobName) throws SchedulerException;
}
