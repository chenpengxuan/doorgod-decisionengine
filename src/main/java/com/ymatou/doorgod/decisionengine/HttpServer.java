package com.ymatou.doorgod.decisionengine;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import com.ymatou.doorgod.decisionengine.service.job.RemoveRedisInvalidUnionKeysExecutor;

/**
 * @author luoshiqian 2016/9/28 14:20
 */
@Component
public class HttpServer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    @Autowired
    private BizProps bizProps;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private RemoveRedisInvalidUnionKeysExecutor removeRedisInvalidUnionKeysExecutor;

    public void start(int port) {
        try {
            ServerSocket ss = new ServerSocket(port);

            while (true) {
                try {
                    Socket socket = ss.accept();
                    BufferedReader bd = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    /**
                     * 接受HTTP请求
                     */
                    String requestHeader;
                    String requestUrl = "";
                    while ((requestHeader = bd.readLine()) != null && !requestHeader.isEmpty()) {
                        if (requestHeader.contains("warmup")) {
                            requestUrl = "warmup";
                        }else if (requestHeader.contains("clearDeviceIdCache")) {
                            requestUrl = "clearDeviceIdCache";
                        }else if (requestHeader.contains("removeRedisUnionKeys")) {
                            requestUrl = "removeRedisUnionKeys";
                        }
                    }

                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

                    String str = " \n" +
                            "HTTP/1.1 200 OK \n" +
                            "Content-type:text/html \n" +
                            "\n";
                    if (requestUrl.equals("warmup")) {
                        str += "ok";
                    }else if(requestUrl.equals("clearDeviceIdCache")){
                        Cache cache = cacheManager.getCache("ipGuavaCache");
                        com.google.common.cache.Cache guavaCache = (com.google.common.cache.Cache)cache.getNativeCache();
                        logger.info("clearDeviceIdCache,current size:{} start",guavaCache.size());
                        cache.clear();
                        logger.info("start clearDeviceIdCache,current size:{} end",guavaCache.size());
                    }else if(requestUrl.equals("removeRedisUnionKeys")){
                        removeRedisInvalidUnionKeysExecutor.execute();
                    } else {
                        str += "version: 2016-09-28 first version <br>";
                        str += "version: 2016-11-01 优化性能监控，升级异监控<br>";
                        str += "version: 2016-11-01-2 升级性能监控<br>";
                        str += "version: 2016-11-03-1 mongo sample 从json string 转为 document 删除mongo中 _class<br>";
                        str += "version: 2016-11-09-1 有效deviceId 增加状态验证<br>";
                        str += "version: 2016-11-10-1 增加offender 被挡时间 使用乘方<br>";
                        str += "version: 2016-11-15-1 不处理拒绝的请求,add rest: /clearDeviceIdCache <br>";
                        str += "version: 2016-12-05-1 增加定时任务每天早上5点 清空1小时之前所有union redis zset,add rest: /removeRedisUnionKeys <br>";
                    }
                    out.write(str);
                    out.flush();
                    out.close();
                    socket.close();
                } catch (Exception e) {
                    logger.warn("httpserver execption", e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("启动httpServer失败", e);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Thread thread = new Thread(() -> {
            this.start(bizProps.getPort());
        }, "httpServer");
        thread.setDaemon(true);
        thread.start();

    }
}
