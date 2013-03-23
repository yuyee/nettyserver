/**
 * @(#)SysLoggerHelper.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.logging;

import com.netease.hmail.common.logging.syslog.SysLogger;
import com.netease.hmail.common.logging.syslog.SysLoggerFactory;

/**
 * 系统日志帮助类
 * 
 * @version 2011-4-7
 */
public final class SysLoggerHelper {

    /**
     * Pop3 syslog日志
     */
    public static final SysLogger pop3SysLog = SysLoggerFactory
        .getLogger("com.netease.hmail.sys.pop3"); // 历史原因,忽略本命名不规范的情况

    /**
     * mta syslog日志
     */
    public static final SysLogger mtaSysLog = SysLoggerFactory
        .getLogger("com.netease.hmail.sys.mta"); // 历史原因,忽略本命名不规范的情况

    /**
     * transport route syslog日志
     */
    public static final SysLogger trSysLog = SysLoggerFactory
        .getLogger("com.netease.hmail.sys.tr"); // 历史原因,忽略本命名不规范的情况

    /**
     * imap4 syslog日志
     */
    public static final SysLogger imap4SysLog = SysLoggerFactory
        .getLogger("com.netease.hmail.sys.imap"); // 历史原因,忽略本命名不规范的情况

    /**
     * proxy syslog日志
     */
    public static final SysLogger proxy4SysLog = SysLoggerFactory
        .getLogger("com.netease.hmail.sys.proxy"); // 历史原因,忽略本命名不规范的情况

    /**
     * anti-spam syslog 日志
     */
    public static final SysLogger as4SysLog = SysLoggerFactory
        .getLogger("com.netease.hmail.sys.antispam"); // 历史原因,忽略本命名不规范的情况

    /**
     * web syslog 日志
     */
    public static final SysLogger webmail4SysLog = SysLoggerFactory
        .getLogger("com.netease.hmail.sys.webmail"); // 历史原因,忽略本命名不规范的情况

    /**
     * 邮件操作历史日志
     */
    public static final SysLogger history4SysLog = SysLoggerFactory
        .getLogger("com.netease.hmail.sys.history"); // 历史原因,忽略本命名不规范的情况

    /** 构造函数 */
    private SysLoggerHelper() {};
}
