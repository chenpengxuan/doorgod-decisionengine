/*
 *
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

import static com.ymatou.doorgod.decisionengine.constants.Constants.DATE_FORMAT_YMDHMS;
import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author luoshiqian 2016/9/19 18:26
 */
public class DateUtils {

    public static String nowFullStr() {
        return LocalDateTime.now().format(FORMATTER_YMDHMS);
    }

    public static String formatDefault(LocalDateTime date) {
        return date.format(FORMATTER_YMDHMS);
    }

    public static LocalDateTime parseDefault(String dateStr) {
        return LocalDateTime.parse(dateStr, FORMATTER_YMDHMS);
    }

    public static String parseAndFormat(String date, DateTimeFormatter fromFormatter, DateTimeFormatter toFormatter) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, fromFormatter);
        return localDateTime.format(toFormatter);
    }

    public static String formatToTenSeconds(String reqTime) {
        return reqTime.substring(0, reqTime.length() - 1) + "0";
    }

    public static String formatToTenSeconds(LocalDateTime date) {

        return formatToTenSeconds(formatDefault(date));
    }


    public static LocalDateTime parseToTenSecondsDate(LocalDateTime date) {
        return parseDefault(formatToTenSeconds(formatDefault(date)));
    }

    public static String formatFromTimeMillis(String timeMillis) {
        String formated = DATE_FORMAT_YMDHMS.format(new Date(Long.valueOf(timeMillis)));
        return formated;
    }
}
