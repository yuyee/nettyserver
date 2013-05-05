/**
 * @(#)LoggerHelper.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.logging;

import java.lang.reflect.Method;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * 日志助手类
 * 
 * @author linaoxiang
 */
public final class LoggerHelper {

	/** 构造函数 */
	private LoggerHelper() {
	};

	/**
	 * 获取当前JVM进程中slf4j的底层实现logger类型,目前仅只支持以下返回值
	 * <ul>
	 * <li><b>NULL</b> = slf4j未启用/生效
	 * <li><b>UNKNOWN</b> = 未知的slf4j实现
	 * <li><b>log4j</b> = 底层实现基于<a
	 * href="http://logging.apache.org/log4j/">log4j</a>
	 * <li><b>logback</b> = 底层实现基于<a href="http://logback.qos.ch/">logback</a>
	 * </ul>
	 * 
	 * @return logger类型
	 */
	public static final String getSlf4jLoggerType() {
		ILoggerFactory factory = LoggerFactory.getILoggerFactory();
		if (factory == null) {
			return "NULL";
		}

		if ("org.slf4j.impl.Log4jLoggerFactory".equals(factory.getClass()
				.getCanonicalName())) {
			return "log4j";
		} else if (factory.getClass().getCanonicalName()
				.startsWith("ch.qos.logback")) {
			// 当前slf4j基于logback
			return "logback";
		}

		return "UNKNOWN";
	}

	/**
	 * 重新载入当前JVM进程的Logger配置参数项
	 * 
	 * @param configUrl
	 *            指定的logger配置文件URL(null=使用当前配置文件URL)
	 * @return 读取的配置文件URL
	 */
	public static final String reloadLogConfig(String configUrl) {
		ILoggerFactory factory = LoggerFactory.getILoggerFactory();
		if (factory == null) {
			throw new IllegalArgumentException("slf4j logger factory is NULL!");
		}

		if ("org.slf4j.impl.Log4jLoggerFactory".equals(factory.getClass()
				.getCanonicalName())) {
			// 当前slf4j基于log4j
			return reloadLog4jConfig(configUrl);
		} else if (factory.getClass().getCanonicalName()
				.startsWith("ch.qos.logback")) {
			// 当前slf4j基于logback
			return reloadLogbackConfig(configUrl, factory);
		}

		throw new IllegalArgumentException("unknown logger factory class: "
				+ factory.getClass().getCanonicalName());
	}

	/**
	 * 重新载入当前JVM进程的Logback配置参数项
	 * 
	 * @param configUrl
	 *            指定的Logback配置文件URL(null=使用当前配置文件URL)
	 * @param factory
	 *            当前SLF4j使用的logger工厂实例
	 * @return 读取的配置文件URL
	 */
	public static final String reloadLogbackConfig(String configUrl,
			ILoggerFactory factory) {
		return InnerLogbackHelper.reloadLogbackConfig(configUrl, factory);
	}

	/**
	 * 重新载入当前JVM进程的Log4j配置参数项
	 * 
	 * @param configUrl
	 *            指定的log4j配置文件URL(null=使用当前配置文件URL)
	 * @return 读取的配置文件URL
	 */
	public static final String reloadLog4jConfig(String configUrl) {
		return InnerLog4jHelper.reloadLog4jConfig(configUrl);
	}

	/**
	 * 开启或关闭Jul2Slf4j
	 * 
	 * @param on
	 *            true=开启;false=关闭
	 * @throws Exception
	 */
	public static final void switchJulToSlf4j(boolean on) throws Exception {
		Class<?> clazz = LoggerHelper.class.getClassLoader().loadClass(
				"org.slf4j.bridge.SLF4JBridgeHandler");

		if (on) {
			Method method = clazz.getMethod("install");
			method.invoke(clazz);
		} else {
			Method method = clazz.getMethod("uninstall");
			method.invoke(clazz);
		}
	}

}
