/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.integration.store;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;
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

    @Resource(name = "putSampleThreadPool")
    private ExecutorService putSampleThreadPool;

    public void putSample(){


        LocalDateTime dateTime  = LocalDateTime.now();
        String currentTime =  dateTime.format(Constants.FORMATTER_YMDHMS);

        findRule().forEach(rule ->
            putSampleThreadPool.execute(() -> {
                try {
                    putSample(rule,currentTime);
                } catch (Exception e) {
                    logger.error("putSample rule:{},currentTime:{} error",rule,currentTime,e);
                }
            })
        );
    }



    /**
     * 返回需要遍历的规则
     * @return
     */
    public abstract Collection<LimitTimesRule> findRule();

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
    public abstract void uploadSampleToDb(LimitTimesRule rule,String uploadTime, Collection<Map.Entry<Sample, Object>> samples);


    private void putSample(LimitTimesRule rule,String currentTime){

        //1.组装规则需要 上报的数据
        Map<String,Map<String,Map<Sample,Object>>> memoryMap = getMemoryMap();

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

            List<Map.Entry<Sample, Object>> sampleList = topNOfSamples(sampleMap, topN);
            uploadSampleToDb(rule,uploadTime,sampleList);
            sampleList = null;

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
            list = null;
        }else {
            newList = list;
        }
        return newList;
    }

}
