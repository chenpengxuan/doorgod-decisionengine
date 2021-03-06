/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.doorgod.decisionengine.holder;

import static com.ymatou.doorgod.decisionengine.constants.Constants.PerformanceServiceEnum.GROOVY_SCRIPT_TIME;
import static com.ymatou.doorgod.decisionengine.constants.Constants.PerformanceServiceEnum.ONE_STATISTIC_ITEM_ALL_RULE_TIME;
import static com.ymatou.doorgod.decisionengine.constants.Constants.PerformanceServiceEnum.SAMPLE_OVER_TIME;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.integration.store.MongoSampleStore;
import com.ymatou.doorgod.decisionengine.integration.store.RedisSampleStore;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.model.StatisticItem;
import com.ymatou.doorgod.decisionengine.script.ScriptContext;
import com.ymatou.doorgod.decisionengine.script.ScriptEngines;
import com.ymatou.doorgod.decisionengine.util.DateUtils;
import com.ymatou.performancemonitorclient.PerformanceStatisticContainer;


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
    public static final Map<String,Map<String,Map<Sample,AtomicInteger>>> ruleTimeSampleMaps = new ConcurrentHashMap<>();
    /**
     * key: rulename
     * value: ConcurrentHashMap:
     *                 key: reqTime (seconds)
     *                 value: ConcurrentHashMap
     *                               key: sample (groupby key)
     *                               value: ConcurrentHashMap
     *                                              key:Sample (去掉groupby key 剩下的)
     *                                              value:AtomicInteger
     */
    public static final Map<String,Map<String,Map<Sample,Map<Sample,AtomicInteger>>>> groupByRuleTimeSampleMaps = new ConcurrentHashMap<>();

    @Autowired
    private BizProps bizProps;
    @Autowired
    private RedisSampleStore redisSampleStore;
    @Autowired
    private MongoSampleStore mongoSampleStore;
    @Autowired
    private ScriptEngines scriptEngines;

    public void putSampleToRedis(){
        redisSampleStore.putSample();
    }

    public void putSampleToMongo(){
        mongoSampleStore.putSample();
    }


    //累计
    public void putStatisticItem(StatisticItem statisticItem){

        Sample sample = statisticItem.getNewSample();
        String reqTime = statisticItem.getReqTime();

        String nowStr = DateUtils.formatDefault(LocalDateTime.now().minusSeconds(30));
        if(Long.valueOf(nowStr) > Long.valueOf(reqTime)){
            logger.warn("nowStr:{},reqTime:{} reqTime before now 30 seconds ,will not be statistic",nowStr,reqTime);
            PerformanceStatisticContainer.add(0,SAMPLE_OVER_TIME.name());
            return;
        }

        Set<LimitTimesRule> set = getRules(statisticItem);

        ScriptContext scriptContext = new ScriptContext(statisticItem);

        PerformanceStatisticContainer.add(() -> {
            set.forEach(rule -> {
                boolean isMatching = PerformanceStatisticContainer.addWithReturn(
                        () -> scriptEngines.execIsMatching(rule.getName(), scriptContext), GROOVY_SCRIPT_TIME.name());

                logger.debug("execIsMatching result:{}",isMatching);
                if(!isMatching){
                    return;
                }

                try {
                    if (CollectionUtils.isEmpty(rule.getGroupByKeys())) {
                        doStatisticNormalSet(rule, sample, reqTime);
                    } else {
                        doStatisticGroupBySet(rule,sample,reqTime);
                    }
                } catch (Exception e) {
                    logger.error("putStatisticItem error,ruleName:{},reqTime:{}", rule.getName(),reqTime,e);
                }
            });
        },ONE_STATISTIC_ITEM_ALL_RULE_TIME.name());

    }

    /**
     * 正常set的累计
     * @param rule
     * @param sample
     * @param reqTime
     */
    private void doStatisticNormalSet(LimitTimesRule rule,Sample sample,String reqTime){

        Set<String> countingKeys = rule.getCountingKeys();
        if(CollectionUtils.isEmpty(countingKeys)){
            logger.error("rule name:{} countingKeys is null,please reset rule",rule.getName());
            return;
        }

        Sample roleSample = sample.narrow(countingKeys);

        //rule map 不存在则新建
        ruleTimeSampleMaps.putIfAbsent(rule.getName(),new ConcurrentHashMap<>());
        Map<String,Map<Sample,AtomicInteger>> secondsTreeMap = ruleTimeSampleMaps.get(rule.getName());

        //秒级别map key:20160809122500/20160809122510 value: ConcurrentHashMap
        /**
         * 将秒 改成10秒形式
         */
        String sampleTime = DateUtils.formatToTenSeconds(reqTime);
        secondsTreeMap.putIfAbsent(sampleTime,new ConcurrentHashMap<>());
        Map<Sample,AtomicInteger> sampleMap = secondsTreeMap.get(sampleTime);

        //sample 计数   判断作限制
        if(sampleMap.size() >= bizProps.getMaxSizePerSecAndRule()){
            // 大于最大size 只能累计 不再增加
            AtomicInteger sampleCount = sampleMap.get(roleSample);
            if(null != sampleCount){
                sampleCount.incrementAndGet();
            }
            logger.debug("ruleName:{},key:{},mapSize:{},sampleCount:{}", rule.getName(), sampleTime, sampleMap.size(),
                    sampleCount);
        } else {
            AtomicInteger count = (AtomicInteger) absolutelyPutReturnValue(sampleMap, roleSample, new AtomicInteger(0));
            if (count != null) {
                int sampleCount = count.incrementAndGet();// ++
                logger.debug("ruleName:{},key:{},mapSize:{},sampleCount:{}", rule.getName(), sampleTime,
                        sampleMap.size(),
                        sampleCount);
            }
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
        Sample leftKeySample = originSample.unNarrow(groupByKeys);

        //rule map 不存在则新建
        groupByRuleTimeSampleMaps.putIfAbsent(rule.getName(),new ConcurrentHashMap<>());
        Map<String,Map<Sample,Map<Sample,AtomicInteger>>> secondsTreeMap = groupByRuleTimeSampleMaps.get(rule.getName());

        //10秒级别map key:20160809122500/20160809122510 value: ConcurrentHashMap
        /**
         * 将秒 改成10秒形式
         */
        String sampleTime = DateUtils.formatToTenSeconds(reqTime);
        secondsTreeMap.putIfAbsent(sampleTime,new ConcurrentHashMap<>());
        Map<Sample,Map<Sample,AtomicInteger>> sampleMap = secondsTreeMap.get(sampleTime);

        //key: groupBy sample  value: ConcurrentHashMap
        if(sampleMap.size() >= bizProps.getMaxSizePerSecAndRule()){
            Map<Sample,AtomicInteger> groupBySampleMap = sampleMap.get(groupBySample);
            if(null != groupBySampleMap){
                if(groupBySampleMap.size() >= bizProps.getMaxSizePerSecAndRule()){
                    AtomicInteger leftKeyCount = groupBySampleMap.get(leftKeySample);
                    if(null != leftKeyCount){
                        leftKeyCount.incrementAndGet();
                    }
                }else {
                    groupBySampleMap.putIfAbsent(leftKeySample, new AtomicInteger(0));
                    groupBySampleMap.get(leftKeySample).incrementAndGet();// ++
                }
            }
            logger.debug("ruleName:{},key:{},mapSize:{},originSample:{},groupbySample:{},groupBySetCount:{}", rule.getName(),
                    sampleTime, sampleMap.size(),
                    originSample, groupBySample, groupBySampleMap != null ? groupBySampleMap.size() : 0);
        }else {
            
            Map<Sample, AtomicInteger> groupBySampleMap =
                    (Map<Sample, AtomicInteger>) absolutelyPutReturnValue(sampleMap, groupBySample,
                            new ConcurrentHashMap<>());
            if(null != groupBySampleMap){
                //key: leftkey sample value: AtomicInteger
                groupBySampleMap.putIfAbsent(leftKeySample,new AtomicInteger(0));

                groupBySampleMap.get(leftKeySample).incrementAndGet();
                logger.debug("ruleName:{},key:{},mapSize:{},originSample:{},groupbySample:{},new groupBySetCount:1",
                        rule.getName(), sampleTime, sampleMap.size(), originSample, groupBySample);
            }
        }
    }

    //获取规则
    public Set<LimitTimesRule> getRules(StatisticItem statisticItem) {
        return RuleHolder.limitTimesRules.values().stream()
                .filter(rule -> statisticItem.getMatchRules().contains(rule.getName()))
                .collect(Collectors.toSet());
    }


    /**
     * 保证 map put key,value
     * 防止并发情况下 内存数据 topN时 clear掉，229行 和 168行 不判断null 出现 nullpointer
     * @See com.ymatou.doorgod.decisionengine.integration.store.AbstractSampleStore  126 行 clear
     * @param map
     * @param key
     * @param value
     * @return
     */
    private Object absolutelyPutReturnValue(Map map, Object key, Object value) {
        map.putIfAbsent(key, value);
        Object obj = map.get(key);
        
        int i = 0;
        while (obj == null && i < 3) {
            map.putIfAbsent(key, value);
            obj = map.get(key);
            i++;
        }
        return obj;
    }

}
