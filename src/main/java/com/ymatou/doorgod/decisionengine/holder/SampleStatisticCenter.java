/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.holder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.ymatou.doorgod.decisionengine.integration.store.MongoSampleStore;
import com.ymatou.doorgod.decisionengine.integration.store.RedisSampleStore;
import com.ymatou.doorgod.decisionengine.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.model.StatisticItem;


/**
 * @author qianmin 2016年9月6日 下午7:01:48
 *
 */
@Component
public class SampleStatisticCenter {

    private static final Logger logger = LoggerFactory.getLogger(SampleStatisticCenter.class);

    /**
     * key: rulename
     * value: ConcurrentHashMap:
     *                 key: reqTime (seconds)
     *                 value: ConcurrentHashMap
     *                               key: sample
     *                               value: AtomicInteger 计数
     */
    public static Map<String,Map<String,Map<Sample,AtomicInteger>>> ruleTimeSampleMaps = new ConcurrentHashMap<>();
    /**
     * key: rulename
     * value: ConcurrentHashMap:
     *                 key: reqTime (seconds)
     *                 value: ConcurrentHashMap
     *                               key: sample (groupby key)
     *                               value: Set<Sample> (去掉groupby key 剩下的)
     */
    public static Map<String,Map<String,Map<Sample,Set<Sample>>>> groupByRuleTimeSampleMaps = new ConcurrentHashMap<>();

    @Autowired
    private BizProps bizProps;
    @Autowired
    private RedisSampleStore redisSampleStore;
    @Autowired
    private MongoSampleStore mongoSampleStore;

    private static long nextLogErrorTime = 0;

    public void putSampleToRedis(){
        redisSampleStore.putSample();
    }

    public void putSampleToMongo(){
        mongoSampleStore.putSample();
    }


    //累计
    public void putStatisticItem(StatisticItem statisticItem){

        Sample sample = statisticItem.getSample();
        String reqTime = statisticItem.getReqTime();

//        String nowStr = DateUtils.formatDefault(LocalDateTime.now().minusSeconds(5));
//        if(Long.valueOf(nowStr) > Long.valueOf(reqTime)){
//            if(nextLogErrorTime == 0 || nextLogErrorTime<=Long.valueOf(nowStr)){
//                logger.error("nowStr:{},reqTime:{} reqTime before now 5 seconds ,will not be statistic",nowStr,reqTime);
//                nextLogErrorTime = Long.valueOf(DateUtils.formatDefault(LocalDateTime.now().plusSeconds(60)));
//            }else {
//                logger.warn("nowStr:{},reqTime:{} reqTime before now 5 seconds ,will not be statistic",nowStr,reqTime);
//            }
//            return;
//        }

        String uri = statisticItem.getUri();
        Set<LimitTimesRule> set = getRulesByUri(uri);

        set.forEach(rule -> {

            try {
                if (CollectionUtils.isEmpty(rule.getGroupByKeys())) {
                    doStatisticNormalSet(rule, sample, reqTime);
                } else {
                    doStatisticGroupBySet(rule,sample,reqTime);
                }
            } catch (Exception e) {
                logger.error("putStatisticItem error,ruleName:{},reqTime:{}", rule.getName(),reqTime);
            }
        });

    }

    /**
     * 正常set的累计
     * @param rule
     * @param sample
     * @param reqTime
     */
    private void doStatisticNormalSet(LimitTimesRule rule,Sample sample,String reqTime){

        Set<String> keys = rule.getDimensionKeys();

        Sample roleSample = sample.narrow(keys);

        //rule map 不存在则新建
        ruleTimeSampleMaps.putIfAbsent(rule.getName(),new ConcurrentHashMap<>());
        Map<String,Map<Sample,AtomicInteger>> secondsTreeMap = ruleTimeSampleMaps.get(rule.getName());

        //秒级别map key:20160809122504 value: ConcurrentHashMap
        secondsTreeMap.putIfAbsent(reqTime,new ConcurrentHashMap<>());
        Map<Sample,AtomicInteger> sampleMap = secondsTreeMap.get(reqTime);

        //sample 计数   判断作限制
        if(sampleMap.size() >= bizProps.getMaxSizePerSecAndRule()){
            // 大于最大size 只能累计 不再增加
            AtomicInteger sampleCount = sampleMap.get(roleSample);
            if(null != sampleCount){
                sampleCount.incrementAndGet();
            }
            logger.debug("ruleName:{},key:{},mapSize:{},sampleCount:{}", rule.getName(), reqTime, sampleMap.size(),
                    sampleCount);
        } else {
            sampleMap.putIfAbsent(roleSample, new AtomicInteger(0));
            int sampleCount = sampleMap.get(roleSample).incrementAndGet();// ++
            logger.debug("ruleName:{},key:{},mapSize:{},sampleCount:{}", rule.getName(),reqTime, sampleMap.size(),
                    sampleCount);
        }

    }

    /**
     * 累计group by key
     * @param rule
     * @param sample
     * @param reqTime
     */
    private void doStatisticGroupBySet(LimitTimesRule rule,Sample sample,String reqTime){

        Set<String> keys = rule.getDimensionKeys();

        Set<String> groupByKeys = rule.getGroupByKeys();

        Sample originSample = sample.narrow(keys);
        Sample groupBySample = sample.narrow(groupByKeys);

        //rule map 不存在则新建
        groupByRuleTimeSampleMaps.putIfAbsent(rule.getName(),new ConcurrentHashMap<>());
        Map<String,Map<Sample,Set<Sample>>> secondsTreeMap = groupByRuleTimeSampleMaps.get(rule.getName());

        //秒级别map key:20160809122504 value: ConcurrentHashMap
        secondsTreeMap.putIfAbsent(reqTime,new ConcurrentHashMap<>());
        Map<Sample,Set<Sample>> sampleMap = secondsTreeMap.get(reqTime);

        //sample 计数   判断作限制
        if(sampleMap.size() >= bizProps.getMaxSizePerSecAndRule()){
            // 大于最大size 只能累计 不再增加
            Set<Sample> leftKeySet = sampleMap.get(groupBySample);
            if(null != leftKeySet){
                leftKeySet.add(originSample.unNarrow(groupByKeys));
            }
            //具体sample值无需输出
            logger.debug("ruleName:{},key:{},mapSize:{},originSample:{},groupbySample:{},groupBySetCount:{}", rule.getName(),
                    reqTime, sampleMap.size(),
                    originSample, groupBySample, leftKeySet.size());
        } else {
            Sample leftKeySample = originSample.unNarrow(groupByKeys);
            sampleMap.putIfAbsent(groupBySample, Sets.newConcurrentHashSet(Lists.newArrayList( leftKeySample )));
            sampleMap.get(groupBySample).add(leftKeySample);

            logger.info("ruleName:{},key:{},mapSize:{},originSample:{},groupbySample:{},new groupBySetCount:1",
                    rule.getName(), reqTime, sampleMap.size(), originSample, groupBySample);
        }
    }

    //获取规则
    public Set<LimitTimesRule> getRulesByUri(String uri){
        return RuleHolder.limitTimesRules.values().stream().filter(
                rule -> rule.applicable(uri))
                .collect(Collectors.toSet());
    }


}
