/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.impl;

import java.util.Date;
import java.util.List;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ymatou.doorgod.decisionengine.service.SchedulerService;
import org.springframework.util.CollectionUtils;


/**
 * 
 * @author qianmin 2016年8月18日 下午3:04:02
 *
 */
@Service
public class SchedulerServiceImpl implements SchedulerService {

    @Autowired
    private Scheduler scheduler;

    @Override
    public void addJob(Class<? extends Job> job, String jobName, String cronExpression) throws Exception {
        try {
            List<? extends Trigger> triggerList = scheduler.getTriggersOfJob(new JobKey(jobName));
            if (triggerList == null || triggerList.isEmpty()) {
                JobDetail jobDetail = JobBuilder.newJob(job)
                        .withIdentity(jobName)
                        // .storeDurably(false) //Job是非持久性的，若没有活动的Trigger与之相关联，该Job会从Scheduler中删除掉
                        // .requestRecovery(true)
                        // //Scheduler非正常停止(进程停止或机器关闭等)时，Scheduler再次启动时，该Job会重新执行一次
                        .build();
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                                .withMisfireHandlingInstructionFireAndProceed())
                        .build();
                scheduler.scheduleJob(jobDetail, trigger);
            }
            else {
                modifyScheduler(jobName, cronExpression);
            }
        } catch (SchedulerException e) {
            List<? extends Trigger> triggerList = scheduler.getTriggersOfJob(new JobKey(jobName));

            //再次确认增加了，未增加则异常
            if(CollectionUtils.isEmpty(triggerList)){
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void modifyScheduler(String jobName, String cronExpression) throws SchedulerException {
        // 获取job的原trigger
        List<? extends Trigger> triggerList = scheduler.getTriggersOfJob(new JobKey(jobName));
        Trigger oldTrigger = triggerList.get(0); // job与trigger一一对应， job有且只有一个trigger

        String oldExpr = ((CronTrigger)oldTrigger).getCronExpression();
        if(!cronExpression.equals(oldExpr)){

            // 借助于原trigger相关联的triggerBuilder修改trigger
            TriggerBuilder tb = oldTrigger.getTriggerBuilder();

            Trigger newTrigger = tb.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

            scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);
        }
    }

    @Override
    public void pauseScheduler(String jobName) throws SchedulerException {
        scheduler.pauseJob(new JobKey(jobName));
    }

    @Override
    public void resumeScheduler(String jobName) throws SchedulerException {
        scheduler.resumeJob(new JobKey(jobName));
    }

    @Override
    public void removeScheduler(String jobName) throws Exception {
        JobKey jobKey = new JobKey(jobName);
        try {
            if(scheduler.checkExists(jobKey)){
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            //再次确认删除了，未删除则异常
            if(scheduler.checkExists(jobKey)){
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public Date getNextFireTime(String jobName) throws SchedulerException {
        List<? extends Trigger> triggerList = scheduler.getTriggersOfJob(new JobKey(jobName));
        Trigger oldTrigger = triggerList.get(0);
        return oldTrigger.getNextFireTime();
    }
}
