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
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.StatusEnum;
import com.ymatou.doorgod.decisionengine.model.po.RulePo;
import com.ymatou.doorgod.decisionengine.repository.RuleRepository;
import com.ymatou.doorgod.decisionengine.service.SchedulerService;

/**
 * 加载Rule数据， 添加修改Rule的定时任务， RedisToMongo同步任务
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
            schedulerService.addJob(MongoSamplePersistenceJob.class, "RedisToMongo",
                    bizProps.getRulePersistenceCronExpression());
        } catch (SchedulerException e) {
            logger.error("add redis to mongo job failed.", e);
        }
        logger.info("load redis to mongo task success.");

        // 加载规则数据，更新规则统计的定时任务
        reload();
    }

    public void reload() {
        // 加载规则数据，更新规则统计的定时任务
        HashMap<String, LimitTimesRule> ruleData = fecthRuleData();
        HashMap<String, LimitTimesRule> rules = new HashMap<>();
        for (LimitTimesRule rule : ruleData.values()) {
            try {
                String ruleName = rule.getName();
                rules.put(ruleName, rule);

                if (CollectionUtils.isEmpty(rule.getGroupByKeys())) {
                    schedulerService.addJob(MongoSampleOffendersJob.class, ruleName,
                            bizProps.getRuleExecutorCronExpression());
                } else {
                    schedulerService.addJob(MongoGroupBySampleOffendersJob.class, "groupBy" + ruleName,
                            bizProps.getMongoSampleOffendersCronExpression());
                }
            } catch (SchedulerException e) {
                logger.error("update rule schduler failed.", e);
            }
        }
        // 已删除的规则 定时任务
        RuleHolder.rules.keySet().stream()
                .filter(ruleName -> ruleData.values().stream().noneMatch(rule -> rule.getName().equals(ruleName)))
                .forEach(ruleName -> {
                    try {
                        RulePo rulePo = new RulePo();
                        rulePo.setName(ruleName);
                        RulePo rule = ruleRepository.findOne(Example.of(rulePo));
                        if (rule != null) {
                            String jobName = ruleName;
                            if (StringUtils.isNotBlank(rule.getGroupByKeys())) {
                                jobName = "groupBy" + ruleName;
                            }
                            schedulerService.removeScheduler(jobName);
                        }
                    } catch (Exception e) {
                        logger.error("update rule schduler failed.", e);
                    }
                });

        // 替换
        RuleHolder.rules = rules;
        logger.info("load rule data: {}", JSON.toJSONString(ruleData));

    }

    public HashMap<String, LimitTimesRule> fecthRuleData() {
        HashMap<String, LimitTimesRule> rules = new HashMap<>();
        List<RulePo> rulePos = ruleRepository.findByStatusAndRuleType(StatusEnum.ENABLE.name(),
                Constants.RULE_TYPE_NAME_LIMIT_TIMES_RULE);
        for (RulePo rulePo : rulePos) {
            LimitTimesRule rule = new LimitTimesRule();
            rule.setName(rulePo.getName());
            rule.setOrder(rulePo.getOrder());
            rule.setStatisticSpan(rulePo.getStatisticSpan());
            rule.setTimesCap(rulePo.getTimesCap());
            rule.setRejectionSpan(rulePo.getRejectionSpan());
            if (StringUtils.isNotBlank(rulePo.getKeys())) {
                rule.setDimensionKeys(new HashSet<>(Arrays.asList(rulePo.getKeys().split(SEPARATOR))));
            }
            if (StringUtils.isNotBlank(rulePo.getGroupByKeys())) {
                rule.setGroupByKeys(new HashSet<>(Arrays.asList(rulePo.getGroupByKeys().split(SEPARATOR))));
            }
            if (StringUtils.isNotBlank(rulePo.getUris())) {
                rule.setApplicableUris(new HashSet<>(Arrays.asList(rulePo.getUris().split(SEPARATOR))));
            }
            rules.put(rulePo.getName(), rule);
        }

        return rules;
    }

}
