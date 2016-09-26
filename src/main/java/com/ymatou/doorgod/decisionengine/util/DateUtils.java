/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author luoshiqian 2016/9/19 18:26
 */
public class DateUtils {

    public static String nowFullStr() {
        return LocalDateTime.now().format(FORMATTER_YMDHMS);
    }

    public static LocalDateTime parseDefault(String dateStr){
        return LocalDateTime.parse(dateStr, FORMATTER_YMDHMS);
    }

    public static String parseAndFormat(String date, DateTimeFormatter fromFormatter, DateTimeFormatter toFormatter) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, fromFormatter);
        return localDateTime.format(toFormatter);
    }


}
