/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.constants.Constants;
import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;
import com.ymatou.doorgod.decisionengine.model.Sample;

/**
 * @author luoshiqian 2016/9/14 15:41
 */
public abstract class AbstractSampleStore {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSampleStore.class);

    /**
     * 下次清空
     *   key: object 需要清空的对像
     *   value: 下次清空时间
     */
    private static Map<Object,String> nextTimeClearMap = Maps.newConcurrentMap();


    @Autowired
    protected BizProps bizProps;

    public void putSample(){

        LocalDateTime dateTime  = LocalDateTime.now();
        String currentTime =  dateTime.format(Constants.FORMATTER_YMDHMS);

        findRule().forEach(rule -> {
            try {
                putSample(rule,currentTime);
            } catch (Exception e) {
                logger.error("putSample rule:{},currentTime:{} error",rule,currentTime,e);
            }
        });
    }



    /**
     * 返回需要遍历的规则
     * @return
     */
    protected abstract Collection<LimitTimesRule> findRule();

    /**
     * 返回内存数据
     * @return
     */
    protected abstract Map getMemoryMap();

    /**
     * 返回topN : topN > 0 才进行top
     * @return
     */
    protected abstract int getTopN();

    /**
     * 由子类实现 不同的存储
     */
    protected abstract void uploadSampleToDb(LimitTimesRule rule,String uploadTime, Collection<Map.Entry<Sample, Object>> samples);


    private final void putSample(LimitTimesRule rule,String currentTime){

        //1.组装规则需要 上报的数据
        Map<String,Map<String,Map<Sample,Object>>> memoryMap = getMemoryMap();

        //删除无用的内存 预防 规则变更等情况引起的无用数据
        clearUselessMemory(memoryMap,currentTime);

        Map<String, Map<Sample, Object>> secondsTreeMap = memoryMap.get(rule.getName());
        if(secondsTreeMap == null){
            return;
        }

        //获取比当前时间小的所有数据
        Set<String> needUploadTimes = secondsTreeMap.keySet().stream()
                .filter(key -> Long.valueOf(key).compareTo(Long.valueOf(currentTime)) < 0)
                .collect(Collectors.toSet());

        int topN = getTopN();
        needUploadTimes.forEach(uploadTime -> {
            Map<Sample,Object> sampleMap = secondsTreeMap.get(uploadTime);
            if(sampleMap == null){
                return;
            }
            if(topN > 0){
                List<Map.Entry<Sample, Object>> sampleList = topNOfSamples(sampleMap, topN);
                uploadSampleToDb(rule,uploadTime,sampleList);
                sampleList.clear();
            }else {
                uploadSampleToDb(rule,uploadTime,sampleMap.entrySet());
                sampleMap.clear();
            }
        });
    }


    //找出top N
    protected List topNOfSamples(Map<Sample,Object> sampleMap, int topNums){

        List<Map.Entry<Sample, Object>> list = new ArrayList<>(sampleMap.entrySet());//map数据放入list中
        sampleMap.clear();//清空map数据

        //排序 大到小
        Collections.sort(list, (o1, o2) ->
            {
                Object a1 = o1.getValue();
                Object a2 = o2.getValue();

                if (a1 instanceof AtomicInteger) {
                    return ((AtomicInteger) a2).intValue() - ((AtomicInteger) a1).intValue();
                } else {
                    return ((Set) a2).size() - ((Set) a1).size();
                }
            }
        );

        List<Map.Entry<Sample, Object>> newList = null;
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

    /**
     * 删除2小时之前 无用的内存 预防 规则变更等情况引起的无用数据
     */
    private void clearUselessMemory(Map<String,Map<String,Map<Sample,Object>>> memoryMap,String currentTime){

        String twoHoursLater = LocalDateTime.parse(currentTime, Constants.FORMATTER_YMDHMS)
                                            .plusHours(2).format(Constants.FORMATTER_YMDHMS);
        nextTimeClearMap.putIfAbsent(memoryMap,twoHoursLater );

        //当前时间超过下次清空时间 则执行清空
        if(Long.valueOf(currentTime) >= Long.valueOf(nextTimeClearMap.get(memoryMap))){
            logger.info("begin to clear useless data,size:{}",memoryMap.size());
            //清空
            memoryMap.entrySet()
                    .removeIf(stringMapEntry -> Long.valueOf(stringMapEntry.getKey()) <= Long.valueOf(twoHoursLater));
            nextTimeClearMap.put(memoryMap,twoHoursLater);

            logger.info("end to clear useless data,size:{}",memoryMap.size());

        }
    }
}
