/**
 * @(#)InnerLog4jHelper.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.logging;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.OptionConverter;

/**
 * 避免LoggerHelper直接依赖log4j包
 */
final class InnerLog4jHelper {

    /** 构造函数 */
    private InnerLog4jHelper() {};

    /**
     * 重新载入当前JVM进程的Log4j配置参数项
     * 
     * @param configUrl
     *            指定的log4j配置文件URL(null=使用当前配置文件URL)
     * @return 读取的配置文件URL
     */
    public static final String reloadLog4jConfig(String configUrl) {
        if (configUrl == null) {
            configUrl = System.getProperty("log4j.configuration");
        }

        URL url = null;
        if (configUrl == null) {
            url = Loader.getResource("log4j.xml");
            if (url == null) {
                url = Loader.getResource("log4j.properties");
            }
        } else {
            try {
                url = new URL(configUrl);
            } catch (MalformedURLException ex) {
                // attempt to get the resource from the class path
                url = Loader.getResource(configUrl);
            }
        }

        // 重新载入配置
        OptionConverter.selectAndConfigure(url,
            OptionConverter.getSystemProperty("log4j.configuratorClass", null),
            LogManager.getLoggerRepository());

        return url.toString();
    }
}
