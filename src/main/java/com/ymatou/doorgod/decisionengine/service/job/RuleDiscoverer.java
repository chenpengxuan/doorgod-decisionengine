/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import static com.ymatou.doorgod.decisionengine.constants.Constants.SEPARATOR;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.ScopeEnum;
import com.ymatou.doorgod.decisionengine.model.po.RulePo;
import com.ymatou.doorgod.decisionengine.repository.RuleRepository;
import com.ymatou.doorgod.decisionengine.service.SchedulerService;
import org.springframework.util.CollectionUtils;

/**
 * 
 * @author qianmin 2016年9月12日 上午11:03:49
 * 
 */
@Component
public class RuleDiscoverer {

    private static final Logger logger = LoggerFactory.getLogger(RuleDiscoverer.class);

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private BizProps bizProps;

    @Transactional
    public void execute() {
        // 加载Redis定时同步数据到MongoDB任务(添加/修改)
        try {
            schedulerService.addJob(RulePersistence.class, "RedisToMongo", bizProps.getRulePersistenceCronExpression());
        } catch (SchedulerException e) {
            logger.error("add redis to mongo job failed.", e);
        }
        logger.info("load redis to mongo task success.");

        // 加载规则数据，更新规则统计的定时任务
        HashMap<String, LimitTimesRule> ruleData = fecthRuleData();
        for (LimitTimesRule rule : ruleData.values()) {
            try {
                String ruleName = rule.getName();
                switch (rule.getUpdateType()) {
                    case "add":
                        RuleHolder.rules.put(ruleName, rule);
                        if(CollectionUtils.isEmpty(rule.getGroupByKeys())){
                            schedulerService.addJob(RuleExecutor.class, ruleName, bizProps.getRuleExecutorCronExpression());
                        }else {
                            schedulerService.addJob(MongoSampleOffendersExecutor.class, "groupBy"+ruleName,
                                    bizProps.getMongoSampleOffendersCronExpression());
                        }
                        break;
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
        logger.info("load rule data: {}", JSON.toJSONString(ruleData));
    }

    public HashMap<String, LimitTimesRule> fecthRuleData() {
        HashMap<String, LimitTimesRule> rules = new HashMap<>();
        List<RulePo> rulePos = ruleRepository.findAll();
        for (RulePo rulePo : rulePos) {
            LimitTimesRule rule = new LimitTimesRule();
            rule.setName(rulePo.getName());
            rule.setOrder(rulePo.getOrder());
            rule.setScope(ScopeEnum.valueOf(rulePo.getScope()));
            rule.setStatisticSpan(rulePo.getStatisticSpan());
            rule.setTimesCap(rulePo.getTimesCap());
            rule.setRejectionSpan(rulePo.getRejectionSpan());
            rule.setUpdateType(rulePo.getUpdateType());
            if (!StringUtils.isBlank(rulePo.getKeys())) {
                rule.setDimensionKeys(new HashSet<>(Arrays.asList(rulePo.getKeys().split(SEPARATOR))));
            }
            if (!StringUtils.isBlank(rulePo.getGroupByKeys())) {
                rule.setGroupByKeys(new HashSet<>(Arrays.asList(rulePo.getGroupByKeys().split(SEPARATOR))));
            }
            if (!StringUtils.isBlank(rulePo.getUris())) {
                rule.setApplicableUris(new HashSet<>(Arrays.asList(rulePo.getUris().split(SEPARATOR))));
            }
            rules.put(rulePo.getName(), rule);
        }

        return rules;
    }
}
