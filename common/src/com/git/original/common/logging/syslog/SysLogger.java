/**
 * @(#)SysLogger.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.logging.syslog;

import com.git.original.common.utils.Pair;

/**
 * 专用于将日志记录到Syslog的Logger
 * 
 * @version 2011-4-7
 */
public interface SysLogger {

    /**
     * 打印info类型的日志
     * 
     * @param sessionId
     *            当前sessionId，不能为空
     * @param username
     *            当前username，可为空
     * @param pairs
     *            参数对，可为0个或多个 {@link Pair}
     */
    public void info(final String sessionId, final String username,
        final Pair<String, Object>... pairs);

    /**
     * 打印info 类型的日志
     * 
     * @param sessionId
     *            当前sessionId，不能为空，
     * @param username
     *            当前username, 可为空
     * @param log
     *            日志内容
     */
    public void info(final String sessionId, final String username,
        final String log);

    /**
     * 打印debug类型的日志
     * 
     * @param sessionId
     *            当前sessionId，不能为空
     * @param username
     *            当前username，可为空
     * @param pairs
     *            参数对，可为0个或多个 {@link Pair}
     */
    public void debug(final String sessionId, final String username,
        final Pair<String, Object>... pairs);

    /**
     * 打印warn类型的日志
     * 
     * @param sessionId
     *            当前sessionId，不能为空
     * @param username
     *            当前username，可为空
     * @param pairs
     *            参数对，可为0个或多个 {@link Pair}
     */
    public void warn(final String sessionId, final String username,
        final Pair<String, Object>... pairs);

    /**
     * 打印error类型的日志
     * 
     * @param sessionId
     *            当前sessionId，不能为空
     * @param username
     *            当前username，可为空
     * @param pairs
     *            参数对，可为0个或多个 {@link Pair}
     */
    public void error(final String sessionId, final String username,
        final Pair<String, Object>... pairs);

}
