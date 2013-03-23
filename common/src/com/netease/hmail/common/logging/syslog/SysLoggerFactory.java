/**
 * @(#)SysLoggerFactory.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.logging.syslog;

/**
 * SysLogger生成工厂
 * 
 * @version 2011-4-8
 */
public final class SysLoggerFactory {

    /** 构造函数 */
    private SysLoggerFactory() {};

    /**
     * 生成指定名字的SysLogger
     * 
     * @param clazz
     *            logger名
     * @return
     */
    public static SysLogger getLogger(String clazz) {
        return new Slf4jLoggerAdapter(clazz);
    }

}
