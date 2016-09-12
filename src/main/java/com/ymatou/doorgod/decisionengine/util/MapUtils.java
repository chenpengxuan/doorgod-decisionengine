///*
// *
// *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
// *  All rights reserved.
// *
// */
//
//package com.ymatou.doorgod.decisionengine.util;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.TreeMap;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.stream.Collectors;
//
//import com.google.common.collect.Sets;
//import com.ymatou.doorgod.decisionengine.model.Rule;
//import com.ymatou.doorgod.decisionengine.model.RuleUri;
//import com.ymatou.doorgod.decisionengine.model.Sample;
//import com.ymatou.doorgod.decisionengine.model.StatisticItem;
//
///**
// * @author luoshiqian 2016/9/9 16:44
// */
//public class MapUtils {
//
//
//    /**
//     * key: rulename
//     * value: hashMap:
//     *                 key: reqTime (seconds)
//     *                 value: treemap
//     *                               key: sample
//     *                               value: AtomicLong 计数
//     */
//    public static Map<String,Map<String,Map<Sample,AtomicLong>>> ruleTimeSampleMaps = new HashMap<>();
//    public static Set<Rule> ruleSet = Sets.newHashSet();
//
//
//    public static Map<String,Map<Sample,AtomicLong>> sampleMaps = new TreeMap<>();
//    public static void main(String[] args) {
//        Map<Sample,AtomicLong> map = new ConcurrentHashMap<>();
//
//        Sample sample = new Sample();
//        Sample sample1 = new Sample();
//        map.put(sample,new AtomicLong(1));
//
//        System.out.println(map.get(sample1).incrementAndGet());
//        System.out.println(map.get(sample1));
//
//
////
//        sampleMaps.put("20160809122504",map);
//        sampleMaps.put("20160809122505",map);
//        sampleMaps.put("20160809122503",map);
//        sampleMaps.put("20160809122511",map);
//        sampleMaps.put("20160809122523",map);
//
//        System.out.println(sampleMaps);
//        ruleTimeSampleMaps.put("rolename",sampleMaps);
//        System.out.println(Long.valueOf("20160809122504").compareTo(Long.valueOf("20160809122505"))<0);
//        Set<String> keySet = sampleMaps.keySet().stream().filter(
//                str -> Long.valueOf(str).compareTo(Long.valueOf("20160809122501"))< 0
//        ).collect(Collectors.toSet());
//
//        System.out.println(keySet.size());
//        System.out.println(keySet);
//
//
////        StatisticItem statisticItem = new StatisticItem();
////        statisticItem.setReqTime("20160809122504");
////        statisticItem.setSample(new Sample());
//
////        ExecutorService executorService = Executors.newFixedThreadPool(5);
////
////        for(int i=0;i<5;i++){
////            executorService.execute(() -> {
////                String aa = "aa"+ RandomStringUtils.random(3);
////                while (true){
////                    StatisticItem a = new StatisticItem();
////                    LocalDateTime dateTime  = LocalDateTime.now();
////                    String str =  dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
////                    a.setReqTime(str);
////                    Sample sample2 = new Sample();
////                    sample2.addDimensionValue(aa,aa);
////                    a.setSample(sample2);
////                    putStaticItem(a);
////                    try {
////                        TimeUnit.MILLISECONDS.sleep(200+new Random().nextInt(300));
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
////                }
////
////            });
////        }
//
//
//    }
//
//
//    //累计
//    public static void putStaticItem(StatisticItem statisticItem){
//
//        Sample sample = statisticItem.getSample();
//        String reqTime = statisticItem.getReqTime();
//        String uri = sample.getUri();
//        Set<Rule> set = getRulesByUri(sample.getUri());
//
//        set.forEach(rule -> {
//
//            //找到对应规则uri设置
//            RuleUri ruleUri = rule.getRuleUriSet().stream().filter(r -> r.getUri().equalsIgnoreCase(uri)).findFirst().orElse(null);
//            if(ruleUri == null){
//                return;
//            }
//            Set<String> keys = ruleUri.getDimensionKeys();
//
//            Sample roleSample = sample.narrow(keys);
//            //rule map 不存在则新建
//            ruleTimeSampleMaps.putIfAbsent(rule.getName(),new TreeMap<>());
//            Map<String,Map<Sample,AtomicLong>> secondsTreeMap = ruleTimeSampleMaps.get(rule.getName());
//
//            //秒级别map key:20160809122504 value: ConcurrentHashMap
//            secondsTreeMap.putIfAbsent(reqTime,new ConcurrentHashMap<>());
//            Map<Sample,AtomicLong> sampleMap = secondsTreeMap.get(reqTime);
//
//            //sample 计数
//            sampleMap.putIfAbsent(roleSample,new AtomicLong(0));
//            sampleMap.get(statisticItem.getSample()).incrementAndGet();//++
//        });
//
//    }
//
//
//    //获取规则
//    public static Set<Rule> getRulesByUri(String uri){
//        return ruleSet.stream().filter(
//                rule -> rule.getRuleUriSet().stream().anyMatch(ruleUri -> ruleUri.getUri().equalsIgnoreCase(uri)))
//                .collect(Collectors.toSet());
//    }
//
//
//
//}
