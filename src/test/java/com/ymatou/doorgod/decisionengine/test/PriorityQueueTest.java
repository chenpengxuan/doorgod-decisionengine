/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;
import com.google.common.collect.MinMaxPriorityQueue;
import com.ymatou.doorgod.decisionengine.model.Sample;
import com.ymatou.doorgod.decisionengine.util.TopNList;
import org.junit.Test;

/**
 * @author luoshiqian 2016/10/31 12:01
 */
public class PriorityQueueTest {


    static class Nums{
        int n;

        public Nums(int n) {
            this.n = n;
        }

    }

    Comparator comparator = new Comparator<Nums>() {

        @Override
        public int compare(Nums o1,Nums o2) {
            if(o1 == null){
                return 1;
            }
            if(o2 == null){
                return 0;
            }
            return o2.n - o1.n;
        }
    };



    @Test
    public void test(){


        TopNList<Nums> topNList = new TopNList(10,comparator);

        for(int i=0;i <100;i++){
            topNList.put(new Nums(new Random().nextInt(100)));
        }

        List<Nums> tops = topNList.getTop();

        tops.forEach(nums -> {
            System.out.println(nums.n);
        });

    }


    @Test
    public void test1(){

        PriorityQueue<Nums> priorityQueue = new PriorityQueue(10,comparator);

        for(int i=0;i <100;i++){
            priorityQueue.add(new Nums(new Random().nextInt(100)));
        }

        while (priorityQueue.size()>0){
            System.out.println(priorityQueue.poll().n);
        }
    }


    @Test
    public void test2(){

        MinMaxPriorityQueue<Nums> queue = MinMaxPriorityQueue.orderedBy(comparator).maximumSize(10).create();

        for(int i=0;i <100;i++){
            queue.add(new Nums(new Random().nextInt(100)));
        }

        while (queue.size()>0){
            System.out.println(queue.poll().n);
        }
    }



    @Test
    public void testTime(){
        Map<Sample,AtomicInteger> map = Maps.newHashMap();
        for(int i =0;i<10000;i++){
            Sample sample = new Sample();
            sample.addDimensionValue("test",i+"");
            map.put(sample,new AtomicInteger(i));
        }
        int topNums = 3;

        List<Map.Entry<Sample, AtomicInteger>> list = new ArrayList<>(map.entrySet());
        List<Map.Entry<Sample, AtomicInteger>> list2 = new ArrayList<>(map.entrySet());
        List<Map.Entry<Sample, AtomicInteger>> list3 = new ArrayList<>(map.entrySet());

        Comparator<Map.Entry<Sample, AtomicInteger>> comparator1 = new Comparator<Map.Entry<Sample, AtomicInteger>>() {
            @Override
            public int compare(Map.Entry<Sample, AtomicInteger> o1, Map.Entry<Sample, AtomicInteger> o2) {
                Object a1 = o1.getValue();
                Object a2 = o2.getValue();
                return ((AtomicInteger) a2).intValue() - ((AtomicInteger) a1).intValue();
            }
        };

        Comparator<Map.Entry<Sample, AtomicInteger>> comparator2 = new Comparator<Map.Entry<Sample, AtomicInteger>>() {
            @Override
            public int compare(Map.Entry<Sample, AtomicInteger> o1, Map.Entry<Sample, AtomicInteger> o2) {
                Object a1 = o1.getValue();
                Object a2 = o2.getValue();
                return ((AtomicInteger) a1).intValue() - ((AtomicInteger) a2).intValue();
            }
        };
        long start = System.currentTimeMillis();
        Collections.sort(list, comparator1);

        List<Map.Entry<Sample, AtomicInteger>> newList = new ArrayList<>(topNums);
        newList.addAll(list.subList(0, topNums));
//        newList.forEach(sampleAtomicIntegerEntry -> {
//            System.out.println(sampleAtomicIntegerEntry.getValue().get());
//        });

        System.out.println((System.currentTimeMillis()-start) +"ms");


        long start2 = System.currentTimeMillis();
        TopNList<Map.Entry<Sample,AtomicInteger>> topNList = new TopNList(topNums,comparator2);
        list2.forEach(sampleAtomicIntegerEntry -> {
            topNList.put(sampleAtomicIntegerEntry);
        });
        List<Map.Entry<Sample,AtomicInteger>> tops = topNList.getTop();
//        tops.forEach(sampleAtomicIntegerEntry -> {
//            System.out.println(sampleAtomicIntegerEntry.getValue().get());
//        });
        System.out.println((System.currentTimeMillis()-start2) +"ms");



        long start3 = System.currentTimeMillis();
        MinMaxPriorityQueue<Map.Entry<Sample,AtomicInteger>> queue = MinMaxPriorityQueue.orderedBy(comparator1).maximumSize(topNums).create();

        list3.forEach(sampleAtomicIntegerEntry -> {
            queue.add(sampleAtomicIntegerEntry);
        });
        List<Map.Entry<Sample, AtomicInteger>> newList2 = new ArrayList<>(topNums);
        while (queue.size()>0){
            newList2.add(queue.poll());
        }
//        newList2.forEach(sampleAtomicIntegerEntry -> {
//            System.out.println(sampleAtomicIntegerEntry.getValue().get());
//        });
        System.out.println((System.currentTimeMillis()-start3) +"ms");
    }
}
