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

    public static final String SEPARATOR = ";";

    public static final String UNION = "Union";

    public static final String MONGO_UNION = "Mongo";

    public static final String BLACK_LIST_CHANNEL = "BlackList";

    public static final String EMPTY_SET = "DoorGodEmptySet";

}
