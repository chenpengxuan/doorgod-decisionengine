/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.service.SchedulerService;

/**
 * 
 * @author qianmin 2016年9月12日 上午11:03:49
 * 
 */
@Component
public class RuleDiscoverer {

    @Autowired
    @Qualifier("scheduler")
    private Scheduler scheduler;

    @Autowired
    private SchedulerService schedulerService;

    private static final Logger logger = LoggerFactory.getLogger(RuleDiscoverer.class);

    public void execute() {

        List<LimitTimesRule> updatedRules = new ArrayList<>(); // TODO 从数据库中获取数据
        for (LimitTimesRule rule : updatedRules) {
            try {
                String ruleName = rule.getName();
                switch (rule.getUpdateType()) {
                    case "add":
                        RuleHolder.rules.put(ruleName, rule);
                        schedulerService.addJob(RuleExecutor.class, ruleName, rule.getStatisticStrategy());
                        break;
                    // case "update":
                    // schedulerService.modifyScheduler(ruleName, rule.getStatisticStrategy());
                    // break;
                    case "delete":
                        RuleHolder.rules.remove(ruleName);
                        schedulerService.removeScheduler(ruleName);
                        break;
                    case "pause":
                        RuleHolder.rules.remove(ruleName);
                        schedulerService.pauseScheduler(ruleName);
                        break;
                    case "resume":
                        RuleHolder.rules.put(ruleName, rule);
                        schedulerService.resumeScheduler(ruleName);
                        break;
                    default:
                        logger.error("Rule UpdateType not supported.");
                        break;
                }
            } catch (SchedulerException e) {
                logger.error("update rule schduler failed.", e);
            }
        }
    }
}
