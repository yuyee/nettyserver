/**
 * @(#)RefreshHelper.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.config;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置文档自动刷新助手
 */
public final class RefreshHelper {

    /**
     * 默认自动扫描配置文件是否发生变化的时间间隔: 30分钟
     */
    public static final long DEFAULT_SCAN_CONFIG_INTERVAL_MILLIS = 30L * 60 * 1000;

    /**
     * 自动刷新定时器
     */
    private static final Timer refresherTimer = new Timer(
        "Config-Refresh-Timer", true);

    /**
     * 已注册的自动刷新配置任务
     */
    private static final ConcurrentHashMap<Configuration, TimerTask> registeredTasks = new ConcurrentHashMap<Configuration, TimerTask>();

    /** 日志记录 */
    private static final Logger LOG = LoggerFactory
        .getLogger(RefreshHelper.class);

    /** 构造函数 */
    private RefreshHelper() {};

    /**
     * 注册一份需要自动刷新的配置文档
     * <p>
     * 刷新时间自动尝试从conf对象中获取, 若没有指定, 则使用
     * {@link #DEFAULT_SCAN_CONFIG_INTERVAL_MILLIS}
     * 
     * @param conf
     *            配置文档对象
     * @return true=注册成功; false=该配置文档已经被注册
     * @throws IllegalArgumentException
     *             注册失败
     * @throws IllegalStateException
     *             注册失败
     */
    public static synchronized boolean registerConfig(Configuration conf) {
        if (registeredTasks.containsKey(conf)) {
            return false;
        }

        Long scanMillis = conf.getScanMillis();
        if (scanMillis == null) {
            scanMillis = DEFAULT_SCAN_CONFIG_INTERVAL_MILLIS;
        } else if (scanMillis.longValue() <= 0) {
            return false;
        }

        RefreshTask task = new RefreshTask(conf);
        refresherTimer.scheduleAtFixedRate(task, scanMillis, scanMillis);

        registeredTasks.put(conf, task);
        return true;
    }

    /**
     * 注册一份需要自动刷新的配置文档
     * 
     * @param conf
     *            配置文档对象
     * @param period
     *            自动刷新间隔时间(ms)
     * @return true=注册成功; false=该配置文档已经被注册
     * @throws IllegalArgumentException
     *             注册失败
     * @throws IllegalStateException
     *             注册失败
     */
    public static synchronized boolean registerConfig(Configuration conf,
        long period) {
        if (registeredTasks.containsKey(conf)) {
            return false;
        }

        RefreshTask task = new RefreshTask(conf);
        refresherTimer.scheduleAtFixedRate(task, period, period);

        registeredTasks.put(conf, task);
        return true;
    }

    /**
     * 替换注册的自动扫描任务
     * 
     * @param conf
     *            配置文档对象
     * @param period
     *            新的自动刷新间隔时间(ms)
     * @return true=替换成功;false=替换失败(例如该配置文档原本没有被注册为自动扫描)
     */
    static synchronized boolean replaceRegister(Configuration conf, long period) {
        RefreshTask task = (RefreshTask) registeredTasks.remove(conf);
        if (task == null) {
            return false;
        }

        task.cancel();

        if (period <= 0) {
            // 不需要再注册了
            return true;
        }

        task = new RefreshTask(conf);
        refresherTimer.scheduleAtFixedRate(task, period, period);

        registeredTasks.put(conf, task);
        return true;
    }

    /**
     * 取消指定配置文档的自动刷新任务
     * 
     * @param conf
     *            配置文档对象
     * @return true=取消成功; false=配置文档尚未注册
     */
    public static synchronized boolean unregisterConfig(Configuration conf) {
        TimerTask task = registeredTasks.remove(conf);
        if (task == null) {
            return false;
        }

        task.cancel();
        return true;
    }

    /**
     * 检查指定配置文档是否注册了自动刷新任务
     * 
     * @param conf
     *            配置文档对象
     * @return true=已注册了自动刷新任务; false=没有注册自动刷新任务
     */
    public static boolean isRegistered(Configuration conf) {
        return (registeredTasks.containsKey(conf));
    }

    /**
     * 配置文档刷新任务类
     * 
     * @author qiu_sheng
     */
    private static class RefreshTask extends TimerTask {
        /**
         * 相关联的配置文档对象
         */
        final Configuration conf;

        public RefreshTask(Configuration conf) {
            this.conf = conf;
        }

        @Override
        public void run() {
            try {
                Long oldScanMillis = this.conf.getScanMillis();
                if (oldScanMillis == null) {
                    oldScanMillis = DEFAULT_SCAN_CONFIG_INTERVAL_MILLIS;
                }

                // 执行配置文档的载入操作
                this.conf.load();

                Long newScanMillis = this.conf.getScanMillis();
                if (newScanMillis == null) {
                    newScanMillis = DEFAULT_SCAN_CONFIG_INTERVAL_MILLIS;
                }

                if (!oldScanMillis.equals(newScanMillis)) {
                    // 扫描周期发生了变化, 重新构建定时任务
                    replaceRegister(conf, newScanMillis.longValue());
                }

            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("refresh configuration failed: ", e);
                } else {
                    LOG.info("refresh configuration failed.");
                }
            }
        }

    }

}
