/**
 * @(#)Slf4jLoggerAdapter.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.logging.syslog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.hmail.common.utils.Pair;

/**
 * A warp of Slf4jLogger{@link org.slf4j.Logger}
 * <p>
 * Note that the logging levels mentioned in this class refer to those defined
 * in the <a
 * href="http://logging.apache.org/log4j/docs/api/org/apache/log4j/Level.html">
 * <code>org.apache.log4j.Level</code></a> class. TRACE level is not support
 * here
 * 
 * @version 2011-4-7
 */
public class Slf4jLoggerAdapter implements SysLogger {
    /** 底层logger实例 */
    private Logger logger;

    /** 输出格式 */
    private static String format = "[tid={},uid={},{}]";

    protected Slf4jLoggerAdapter(String name) {
        logger = LoggerFactory.getLogger(name);
    }

    /** 输出记录类型 */
    private static final class LogObject {
        /** 格式化后的输出字符串 */
        private String logStr = "";

        private LogObject(final Pair<String, Object>[] pairs) {
            if (pairs != null && pairs.length != 0) {
                StringBuilder sBuf = new StringBuilder();

                for (Pair<String, Object> p: pairs) {
                    if (p == null) {
                        continue;
                    }

                    if (sBuf.length() > 0) {
                        sBuf.append(',');
                    }

                    encode(sBuf, p.getFirst());
                    sBuf.append('=');
                    encode(sBuf, p.getSecond());
                }

                logStr = sBuf.toString();
            }
        }

        private void encode(StringBuilder dest, Object obj) {

            final String src = obj == null ? "" : obj.toString();

            for (int i = 0; i < src.length(); i++) {
                switch (src.charAt(i)) {
                    case '[':
                        dest.append("%5B");
                        break;
                    case ']':
                        dest.append("%5D");
                        break;
                    case ',':
                        dest.append("%2C");
                        break;
                    case '=':
                        dest.append("%3D");
                        break;
                    default:
                        dest.append(src.charAt(i));
                }
            }
        }

        public String toString() {
            return logStr;
        }
    }

    /**
     * @see SysLogger#info(String, String, Pair...);
     * @override
     */
    public void info(final String sessionId, final String username,
        final Pair<String, Object>... pairs) {
        if (logger.isInfoEnabled()) {
            if (sessionId == null) {
                throw new IllegalArgumentException("Session id can't be null!");
            }
            Object[] objs = { sessionId, (username == null) ? "" : username,
                new LogObject(pairs) };
            logger.info(format, objs);
        }
    }

    /**
     * @see SysLogger#debug(String, String, Pair...);
     * @override
     */
    public void debug(final String sessionId, final String username,
        final Pair<String, Object>... pairs) {
        if (logger.isDebugEnabled()) {
            if (sessionId == null) {
                throw new IllegalArgumentException("Session id can't be null!");
            }
            Object[] objs = { sessionId, (username == null) ? "" : username,
                new LogObject(pairs) };
            logger.debug(format, objs);
        }
    }

    /**
     * @see SysLogger#warn(String, String, Pair...);
     * @override
     */
    public void warn(final String sessionId, final String username,
        final Pair<String, Object>... pairs) {

        if (sessionId == null) {
            throw new IllegalArgumentException("Session id can't be null!");
        }
        Object[] objs = { sessionId, (username == null) ? "" : username,
            new LogObject(pairs) };
        logger.warn(format, objs);
    }

    /**
     * @see SysLogger#error(String, String, Pair...);
     * @override
     */
    public void error(final String sessionId, final String username,
        final Pair<String, Object>... pairs) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session id can't be null!");
        }
        Object[] objs = { sessionId, (username == null) ? "" : username,
            new LogObject(pairs) };
        logger.error(format, objs);
    }

    @Override
    public void info(String sessionId, String username, String log) {
        Object[] objs = { sessionId, (username == null) ? "" : username, log };
        logger.info(format, objs);

    }

}
