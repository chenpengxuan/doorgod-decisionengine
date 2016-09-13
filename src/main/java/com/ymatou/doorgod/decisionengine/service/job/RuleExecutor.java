/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;

/**
 * 
 * @author qianmin 2016年9月12日 上午11:04:36
 * 
 */
@Component
public class RuleExecutor implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().getName();

        LimitTimesRule rule = RuleHolder.rules.get(jobName);
        int span = rule.getStatisticSpan();

        rule.getName();
    }

    public List<String> getAllTime(LimitTimesRule rule) {
        List<String> times = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formatNow = now.format(formatter);
        for (int second = 1; second <= rule.getStatisticSpan(); second++) {
            times.add(now.minusSeconds(second).format(formatter));
        }
        return times;
    }


    public static void main(String[] args) {
        LimitTimesRule rule = new LimitTimesRule();
        rule.setStatisticSpan(60);
        RuleExecutor ruleExecutor = new RuleExecutor();


        List<String> times = ruleExecutor.getAllTime(rule);
        times.stream().forEach(c -> System.out.println(c));



    }
}
