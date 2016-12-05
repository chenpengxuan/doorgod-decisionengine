/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.service.job;

import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.baidu.disconf.client.config.DisClientConfig;
import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.StatusEnum;
import com.ymatou.doorgod.decisionengine.model.po.RulePo;
import com.ymatou.doorgod.decisionengine.repository.RuleRepository;
import com.ymatou.doorgod.decisionengine.script.ScriptEngines;
import com.ymatou.doorgod.decisionengine.service.SchedulerService;
import com.ymatou.doorgod.decisionengine.service.job.offender.LimitTimesRuleGroupBySampleOffendersJob;
import com.ymatou.doorgod.decisionengine.service.job.offender.LimitTimesRuleSampleOffendersJob;
import com.ymatou.doorgod.decisionengine.service.job.persistence.LimitTimesRuleSampleMongoPersistenceJob;
import com.ymatou.doorgod.decisionengine.util.Utils;

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

    @Autowired
    private ScriptEngines scriptEngines;

    @Transactional
    public void execute() {
        String env = DisClientConfig.getInstance().ENV;
        if (env.equals(Constants.ENV_STG)) {
            return;
        }
        // 加载Redis定时同步数据到MongoDB任务(添加/修改)
        try {
            schedulerService.addJob(LimitTimesRuleSampleMongoPersistenceJob.class, "RedisToMongo",
                    bizProps.getLimitTimesRuleSamplePersistCronExpr());
            logger.info("load redis to mongo task success.");
        } catch (Exception e) {
            logger.error("add redis to mongo job failed.", e);
        }

        // 定时清除由于超时 导致union zset没有设置过期
        addRemoveRedisInvalidUnionKeysJob();

        // 加载规则数据，更新规则统计的定时任务
        reload();
    }

    /**
     * 定时清除由于超时 导致union zset没有设置过期  每天早上5点执行 "0 0 5 * * ?"
     */
    public void addRemoveRedisInvalidUnionKeysJob(){
        try {
            schedulerService.addJob(RemoveRedisInvalidUnionKeysJob.class, "removeRedisInvalidUnionKeys", "0 0 5 * * ?");
            logger.info("load redis to mongo task success.");
        } catch (Exception e) {
            logger.error("add remove redis union keys job failed.", e);
        }
    }

    @Transactional
    public void reload() {
        // 加载规则数据，更新规则统计的定时任务
        HashMap<String, LimitTimesRule> ruleData = fetchLimitTimesRules();
        HashMap<String, LimitTimesRule> rules = new HashMap<>();

        for (LimitTimesRule rule : ruleData.values()) {

            String ruleName = rule.getName();

            rules.put(ruleName, rule);

            scriptEngines.fillScript(ruleName, rule.getMatchScript());

            if (CollectionUtils.isEmpty(rule.getGroupByKeys())) {
                schedulerService.addJob(LimitTimesRuleSampleOffendersJob.class, ruleName,
                        bizProps.getLimitTimesRuleSampleCronExpr());
            } else {
                schedulerService.addJob(LimitTimesRuleGroupBySampleOffendersJob.class, ruleName,
                        bizProps.getLimitTimesRuleGroupBySampleCronExpr());
            }
        }

        // 已删除的规则 定时任务
        RuleHolder.limitTimesRules.keySet().stream()
                .filter(ruleName -> ruleData.values().stream().noneMatch(rule -> rule.getName().equals(ruleName)))
                .forEach(ruleName -> schedulerService.removeScheduler(ruleName));

        // 替换
        RuleHolder.limitTimesRules = rules;
        logger.info("load rule data: {}", JSON.toJSONString(ruleData));
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
            rule.setGroupByCount(rulePo.getGroupByCount());

            if (StringUtils.isNotBlank(rulePo.getKeys())) {
                rule.setDimensionKeys(Utils.splitByComma(rulePo.getKeys()));
            }
            if (StringUtils.isNotBlank(rulePo.getGroupByKeys())) {
                rule.setGroupByKeys(Utils.splitByComma(rulePo.getGroupByKeys()));
            }
            if (StringUtils.isNotBlank(rulePo.getCountingKeys())) {
                rule.setCountingKeys(Utils.splitByComma(rulePo.getCountingKeys()));
            }
            if (StringUtils.isNotBlank(rulePo.getUris())) {
                rule.setApplicableUris(Utils.splitByComma(rulePo.getUris()));
            }

            rule.setMatchScript(rulePo.getMatchScript());

            rules.put(rulePo.getName(), rule);

        }

        return rules;
    }

}
