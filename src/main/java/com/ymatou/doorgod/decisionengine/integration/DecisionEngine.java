/*
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 */
package com.ymatou.doorgod.decisionengine.integration;

import com.google.common.collect.Sets;
import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.holder.RuleHolder;
import com.ymatou.doorgod.decisionengine.model.*;
import com.ymatou.doorgod.decisionengine.util.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.ymatou.doorgod.decisionengine.model.ScopeEnum.ALL;

/**
 *
 * @author qianmin 2016年9月6日 下午7:01:48
 *
 */
@Component
public class DecisionEngine {

    private static final Logger logger = LoggerFactory.getLogger(DecisionEngine.class);

    /**
     * key: rulename
     * value: hashMap:
     *                 key: reqTime (seconds)
     *                 value: treemap
     *                               key: sample
     *                               value: AtomicInteger 计数
     */
    public static Map<String,Map<String,Map<Sample,AtomicInteger>>> ruleTimeSampleMaps = new HashMap<>();

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private BizProps bizProps;


    /**
     * 将所有当前时间之前sample数据存入redis
     */
    public void putSampleToRedis(){

        /**
         * todo 扫描 当前时间 向前一分钟之前的所有数据  需要清空
         * 防止规则被删除，还有内存累计数据
         * todo 修成多线程处理不同规则
         */

        LocalDateTime dateTime  = LocalDateTime.now();
        String currentTime =  dateTime.format(Constants.FORMATTER_YMDHMS);
        RuleHolder.rules.values().forEach(rule -> {

            //1.组装规则需要 上报的数据
            Map<String, Map<Sample, AtomicInteger>> secondsTreeMap = ruleTimeSampleMaps.get(rule.getName());
            if(secondsTreeMap == null){
                return;
            }

            //获取比当前时间小的所有数据
            Set<String> needUploadTimes = secondsTreeMap.keySet().stream()
                    .filter(key -> Long.valueOf(key).compareTo(Long.valueOf(currentTime)) < 0)
                    .collect(Collectors.toSet());

            needUploadTimes.forEach(uploadTime -> {
                Map<Sample,AtomicInteger> sampleMap = secondsTreeMap.get(uploadTime);
                if(sampleMap == null){
                    return;
                }

                if(bizProps.getUploadRedisTopN() > 0){
                    List<Map.Entry<Sample, AtomicInteger>> sampleList = topNOfSamples(sampleMap, bizProps.getUploadRedisTopN());
                    uploadSample(rule,uploadTime,sampleList);
                    sampleList.clear();
                }else {
                    uploadSample(rule,uploadTime,sampleMap.entrySet());
                    sampleMap.clear();
                }
            });

        });
    }

    //找出top N
    private List<Map.Entry<Sample, AtomicInteger>> topNOfSamples(Map<Sample,AtomicInteger> sampleMap,int topNums){

        List<Map.Entry<Sample, AtomicInteger>> list = new ArrayList<>(sampleMap.entrySet());//map数据放入list中
        sampleMap.clear();//清空map数据

        //排序 大到小
        Collections.sort(list, (o1, o2) -> o2.getValue().intValue() - o1.getValue().intValue());

        List<Map.Entry<Sample, AtomicInteger>> newList = null;
        if (list.size() >= topNums) {
            newList = new ArrayList<>(topNums);
            newList.addAll(list.subList(0, topNums));
            list.clear();
            //list = null;
        }else {
            newList = list;
        }
        return newList;
    }

    //上报数据到redis
    private void uploadSample(LimitTimesRule rule,String uploadTime, Collection<Map.Entry<Sample, AtomicInteger>> samples) {
        //获取redis zset name
        String zSetName = RedisHelper.getNormalSetName(rule.getName(),uploadTime);

        samples.forEach(entry -> {
            double score = 1;
            if(redisTemplate.opsForZSet().getOperations().hasKey(zSetName)){
                score = redisTemplate.opsForZSet().incrementScore(zSetName, entry.getKey().toString(), entry.getValue().doubleValue());
            }else {
                redisTemplate.opsForZSet().add(zSetName,entry.getKey().toString(),entry.getValue().doubleValue());
                redisTemplate.opsForZSet().getOperations().expire(zSetName,getExpireByRule(rule), TimeUnit.SECONDS);//单位秒
            }

            logger.debug("ruleName:{},zsetName:{},zsetsample:{},score:{}", rule.getName(),
                    zSetName, entry.getKey().toString(), score);
        });
    }

    /**
     * 获取规则的过期时间
     *   小于60秒 系数为 1.5
     *   大于60秒 系数为 1.2
     * @param rule
     * @return
     */
    private long getExpireByRule(LimitTimesRule rule){
        if (rule.getTimesCap() < 60) {
            return ((Double)(rule.getTimesCap() * 1.5)).longValue();
        }
        return ((Double)(rule.getTimesCap() * 1.2)).longValue();
    }



    //累计
    public void putStaticItem(StatisticItem statisticItem){

        Sample sample = statisticItem.getSample();
        String reqTime = statisticItem.getReqTime();
        String uri = sample.getUri();
        Set<LimitTimesRule> set = getRulesByUri(uri);

        set.forEach(rule -> {

            Set<String> keys = rule.getDimensionKeys();

            Sample roleSample = sample.narrow(keys);
            //rule map 不存在则新建
            ruleTimeSampleMaps.putIfAbsent(rule.getName(),new TreeMap<>());
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
                logger.debug("ruleName:{},key:{},mapSize:{},sample:{},sampleCount:{}", rule.getName(),reqTime, sampleMap.size(),
                        roleSample, sampleCount);
            } else {
                sampleMap.putIfAbsent(roleSample, new AtomicInteger(0));
                int sampleCount = sampleMap.get(roleSample).incrementAndGet();// ++
                logger.debug("ruleName:{},key:{},mapSize:{},sample:{},sampleCount:{}", rule.getName(),reqTime, sampleMap.size(),
                        roleSample, sampleCount);
            }

        });

    }


    //获取规则
    public static Set<LimitTimesRule> getRulesByUri(String uri){
        return RuleHolder.rules.values().stream().filter(
                rule -> rule.getScope() == ALL || rule.getApplicableUris().stream().anyMatch(s -> uri.startsWith(s)))
                .collect(Collectors.toSet());
    }


}
