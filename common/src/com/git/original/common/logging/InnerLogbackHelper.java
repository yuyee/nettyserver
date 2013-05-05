/**
 * @(#)InnerLogbackHelper.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.logging;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.ILoggerFactory;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;

/**
 * 避免LoggerHelper直接依赖logback包
 */
final class InnerLogbackHelper {

    /** 构造函数 */
    private InnerLogbackHelper() {};

    /**
     * 重新载入当前JVM进程的Logback配置参数项
     * 
     * @param configUrl
     *            指定的Logback配置文件URL(null=使用当前配置文件URL)
     * @return 读取的配置文件URL
     */
    public static final String reloadLogbackConfig(String configUrl,
        ILoggerFactory factory) {
        ContextBase context = (ContextBase) factory;

        URL url = null;
        if (configUrl == null) {
            url = ConfigurationWatchListUtil.getMainWatchURL(context);
        } else {
            try {
                url = new URL(configUrl);
            } catch (MalformedURLException ex) {
                // attempt to get the resource from the class path
                url = ConfigurationWatchListUtil.getMainWatchURL(context);
            }
        }

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        try {
            if (url.getProtocol() == null
                || url.getProtocol().equalsIgnoreCase("file")) {
                // 指定的是本地文件路径
                configurator.doConfigure(url.getPath());
            } else {
                // 指定的URL
                configurator.doConfigure(url);
            }
        } catch (JoranException e) {
            throw new RuntimeException("reload logback config failed: ", e);
        }

        return url.toString();
    }
}
