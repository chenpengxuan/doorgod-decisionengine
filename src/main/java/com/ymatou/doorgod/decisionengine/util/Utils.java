/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
