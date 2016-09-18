/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

import com.ymatou.doorgod.decisionengine.model.LimitTimesRule;

/**
 * @author luoshiqian 2016/9/12 14:20
 */
public class MongoHelper {

    public static String getGroupByCollectionName(LimitTimesRule rule){
        return "GroupSample_"+rule.getName();
    }
}
