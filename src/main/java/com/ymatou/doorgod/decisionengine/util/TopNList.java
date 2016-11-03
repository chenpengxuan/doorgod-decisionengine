/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author luoshiqian 2016/10/31 12:55
 */
public class TopNList<T> {

    private int topNums;
    private PriorityQueue<T> priorityQueue;
    private Comparator<T> comparator;

    public TopNList(int topNums,Comparator<T> comparator) {
        this.comparator = comparator;
        this.topNums = topNums;
        priorityQueue = new PriorityQueue<>(topNums,comparator);
    }

    public void put(T t){
        if(priorityQueue.size() < topNums){
            priorityQueue.add(t);
        }else if(comparator.compare(priorityQueue.peek(),t) < 0) {
            priorityQueue.poll();
            priorityQueue.add(t);
        }
    }

    public List<T> getTop(){
        List<T> topNList = new ArrayList<>(topNums);
        while (priorityQueue.size()>0){
            topNList.add(priorityQueue.poll());
        }
        return topNList;
    }

}
