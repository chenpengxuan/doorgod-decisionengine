/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.constants;

import java.time.format.DateTimeFormatter;

/**
 * @author luoshiqian 2016/9/12 15:27
 */
public class Constants {
    public static final DateTimeFormatter FORMATTER_YMDHMS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter FORMATTER_YMDHM = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public static final String SEPARATOR = ";";

    public static final String UNION = "Union";

    public static final String MONGO_UNION = "Mongo";

    public static final String OffENDER_CHANNEL = "Offender";

    public static final String EMPTY_SET = "DoorGodEmptySet";

    public static final int UNION_FOR_MONGO_PERSISTENCE_EXPIRE_TIME = 60; // 为mongo持久化而union的并集过期时间,
                                                                          // 60s

    public static final int PREVIOUS_COUNT = 10; // 当union后得时间窗口不存在时， 往前找多少格

}
