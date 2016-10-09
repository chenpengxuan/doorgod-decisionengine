/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

import com.ymatou.doorgod.decisionengine.service.job.offender.LimitTimesRuleGroupBySampleOffendersJob;
import com.ymatou.doorgod.decisionengine.service.job.offender.LimitTimesRuleSampleOffendersJob;
import com.ymatou.doorgod.decisionengine.service.job.persistence.LimitTimesRuleSampleMongoPersistenceJob;
import com.ymatou.doorgod.decisionengine.util.Utils;
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
            schedulerService.addJob(LimitTimesRuleSampleMongoPersistenceJob.class, "RedisToMongo",
                    bizProps.getRulePersistenceCronExpression());
        } catch (SchedulerException e) {
            logger.error("add redis to mongo job failed.", e);
        }
        logger.info("load redis to mongo task success.");

        // 加载规则数据，更新规则统计的定时任务
        reload();
    }

    @Transactional
    public void reload() {
        // 加载规则数据，更新规则统计的定时任务
        HashMap<String, LimitTimesRule> ruleData = fetchLimitTimesRules();
        HashMap<String, LimitTimesRule> rules = new HashMap<>();
        for (LimitTimesRule rule : ruleData.values()) {
            try {
                String ruleName = rule.getName();
                rules.put(ruleName, rule);

                if (CollectionUtils.isEmpty(rule.getGroupByKeys())) {
                    schedulerService.addJob(LimitTimesRuleSampleOffendersJob.class, ruleName,
                            //FIXME:更好的命名
                            bizProps.getRuleExecutorCronExpression());
                } else {
                    schedulerService.addJob(LimitTimesRuleGroupBySampleOffendersJob.class,ruleName,
                            //FIXME: 更好的命名
                            bizProps.getMongoSampleOffendersCronExpression());
                }
                //FIXME: 异常处理，吃掉?
            } catch (SchedulerException e) {
                logger.error("update rule schduler failed.", e);
            }
        }
        // 已删除的规则 定时任务
        RuleHolder.limitTimesRules.keySet().stream()
                .filter(ruleName -> ruleData.values().stream().noneMatch(rule -> rule.getName().equals(ruleName)))
                .forEach(ruleName -> {
                    try {
                        RulePo rulePo = new RulePo();
                        rulePo.setName(ruleName);
                        RulePo rule = ruleRepository.findOne(Example.of(rulePo));
                        //FIXME:为什么要判断rule是否存在呢?
                        if (rule != null) {
                            String jobName = ruleName;
                            schedulerService.removeScheduler(jobName);
                        }
                    } catch (Exception e) {
                        logger.error("update rule schduler failed.", e);
                    }
                });

        // 替换
        RuleHolder.limitTimesRules = rules;
        logger.info("load rule data: {}", JSON.toJSONString(ruleData));

        //FIXME:如果rule的统计时长变更了，是不是需要清除原redis的union？
    }

    public HashMap<String, LimitTimesRule> fetchLimitTimesRules() {
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
                rule.setDimensionKeys(Utils.splitByComma(rulePo.getKeys()));
            }
            if (StringUtils.isNotBlank(rulePo.getGroupByKeys())) {
                rule.setGroupByKeys(Utils.splitByComma(rulePo.getGroupByKeys()));
            }
            if (StringUtils.isNotBlank(rulePo.getUris())) {
                rule.setApplicableUris(Utils.splitByComma(rulePo.getUris()));
            }
            rules.put(rulePo.getName(), rule);
        }

        return rules;
    }

}
