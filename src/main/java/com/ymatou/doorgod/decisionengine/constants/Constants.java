/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.constants;


import com.ymatou.doorgod.decisionengine.util.CollectionOptions;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * @author luoshiqian 2016/9/12 15:27
 */
public class Constants {
    public static final DateTimeFormatter FORMATTER_YMDHMS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter FORMATTER_YMDHM = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    public static final SimpleDateFormat DATE_FORMAT_YMDHMS = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final String RULE_TYPE_NAME_LIMIT_TIMES_RULE = "LimitTimesRule";
    public static final String RULE_TYPE_NAME_BLACKLIST_RULE = "BlacklistRule";

    public static final String SEPARATOR = "[,]";

    public static final String UNION = "Union";

    public static final String MONGO_UNION = "Mongo";

    public static final String EMPTY_SET = "DoorGodEmptySet";

    public static final int UNION_FOR_MONGO_PERSISTENCE_EXPIRE_TIME = 60; // 为mongo持久化而union的并集过期时间,
                                                                          // 60s

    public static final int PREVIOUS_COUNT = 10; // 当union后得时间窗口不存在时， 往前找多少格

    public static final int PRESIST_TO_MONGO_COUNT = -1000;

    public static final String IP_DEVICEID_INVALID_RULE = "IP_DEVICEID_INVALID_RULE";


    /**
     * mongo max size 2G
     */
    public static final long MAX_SIZE = 2L * 1024 * 1024 * 1024;

    public static final CollectionOptions COLLECTION_OPTIONS = new CollectionOptions(MAX_SIZE,null, true);

    public static final String ENV_STG = "STG";

}
