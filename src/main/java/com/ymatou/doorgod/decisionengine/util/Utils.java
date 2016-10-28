/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

import static com.ymatou.doorgod.decisionengine.constants.Constants.FORMATTER_YMDHMS;
import static com.ymatou.doorgod.decisionengine.util.RedisHelper.getNormalSetName;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.ymatou.doorgod.decisionengine.constants.Constants;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static volatile String localIp;

    private Utils() {}


    public static String getCurrentTime( ) {
        LocalDateTime dateTime = LocalDateTime.now();
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static Date parseDate( String date ) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Set<String> splitByComma(String text ) {
        Set<String> result = new HashSet<String>( );
        if (StringUtils.hasText(text)) {
            text = text.trim();
            String[] splits = text.split(Constants.SEPARATOR);
            for (String split : splits ) {
                if (StringUtils.hasText(split.trim())) {
                    result.add(split.trim());
                }
            }
        }
        return result;
    }

    public static String localIp( ) {
        if (localIp != null) {
            return localIp;
        }
        synchronized (Utils.class) {
            if (localIp == null) {
                try {
                    Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                            .getNetworkInterfaces();

                    while (netInterfaces.hasMoreElements() && localIp == null) {
                        NetworkInterface ni = netInterfaces.nextElement();
                        if (!ni.isLoopback() && ni.isUp() && !ni.isVirtual()) {
                            Enumeration<InetAddress> address = ni.getInetAddresses();

                            while (address.hasMoreElements() && localIp == null) {
                                InetAddress addr = address.nextElement();

                                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()
                                        && !(addr.getHostAddress().indexOf(":") > -1)) {
                                    localIp = addr.getHostAddress();

                                }
                            }
                        }
                    }

                } catch (Throwable t) {
                    localIp = "127.0.0.1";
                    LOGGER.error("Failed to extract local ip. use 127.0.0.1 instead", t);
                }
            }

            if (localIp == null ) {
                localIp = "127.0.0.1";
                LOGGER.error("Failed to extract local ip. use 127.0.0.1 instead");
            }

            return localIp;
        }
    }


    /**
     * 找到所以需要合并的时间窗口
     *
     * @param ruleName
     * @param now
     * @param timeSpan
     * @return
     */
    public static List<String> getAllTimeBucket(String ruleName, LocalDateTime now,int timeSpan) {

        LocalDateTime formatedTenSecDate = DateUtils.parseToTenSecondsDate(now);

        List<String> timeBuckets = new ArrayList<>();
        for (int nums = timeSpan / 10; nums > 0; nums--) {
            timeBuckets.add(getNormalSetName(ruleName, formatedTenSecDate.minusSeconds(nums * 10).format(FORMATTER_YMDHMS)));
        }
        return timeBuckets;
    }

}
