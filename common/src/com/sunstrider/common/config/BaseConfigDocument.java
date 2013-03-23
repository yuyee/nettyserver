/**
 * @(#)BaseConfigDocument.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.config;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现了{@link Configuration}接口的抽象配置文档基类
 */
public abstract class BaseConfigDocument implements Configuration {
    /**
     * HMail系统工作路径系统属性名称
     */
    public static final String PROPERTY_HMAIL_HOME = "HMAIL_HOME";

    /**
     * HMail系统默认工作路径
     */
    public static final String DEFAULT_HMAIL_HOME = "/home/hmail";

    /**
     * 配置文档(即根配置节点)属性: 数据库版本号
     */
    public static final String CONF_DOC_ATTRIBUTE_DB_VERSION = "db-version";

    /**
     * 配置文档(即根配置节点)属性: 是否忽略数据库版本
     */
    public static final String CONF_DOC_ATTRIBUTE_IGNORE_DB = "ignore-db";

    /**
     * 配置文档(即根配置节点)属性: 扫描配置是否发生变化,并自动重载的时间周期
     */
    public static final String CONF_DOC_ATTRIBUTE_SCAN_PERIOD = "scan-period";

    // -------------------------------------------------------

    /**
     * HMail系统默认工作路径
     */
    private String hmailHome = DEFAULT_HMAIL_HOME;

    /**
     * 配置根节点
     */
    private volatile ConfigNode rootNode = null;

    /**
     * 配置版本号
     */
    private volatile long version = System.currentTimeMillis();

    /**
     * 配置更新监控器集合
     */
    private final Set<ConfigUpdateWatcher> watcherSet = new HashSet<ConfigUpdateWatcher>();

    /** 日志记录 */
    private static final Logger LOG = LoggerFactory
        .getLogger(BaseConfigDocument.class);

    /**
     * 构建实例
     */
    public BaseConfigDocument() {
        this.hmailHome = System.getProperty(PROPERTY_HMAIL_HOME,
            DEFAULT_HMAIL_HOME);
    }

    /**
     * 使用指定的根配置节点创建文档实例
     * 
     * @param rootNode
     *            配置根节点
     */
    BaseConfigDocument(ConfigNode rootNode) {
        this();
        this.rootNode = rootNode;
    }

    /**
     * @return the hmailHome
     */
    public String getHmailHome() {
        return hmailHome;
    }

    /**
     * @param hmailHome
     *            the hmailHome to set
     */
    public void setHmailHome(String hmailHome) {
        this.hmailHome = hmailHome;
    }

    /**
     * @return the version
     */
    public final long getVersion() {
        return version;
    }

    public ConfigNode getRootNode() {
        return this.rootNode;
    }

    /**
     * 各类配置文档的载入实现
     * 
     * @return 新配置映射表 (null=配置未发生变化)
     * @throws Exception
     */
    protected abstract ConfigNode doLoad() throws Exception;

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.config.Configuration#load()
     */
    @Override
    public final synchronized void load() throws Exception {
        ConfigNode newRootNode = doLoad();

        /*
         * 更新配置
         */
        if (newRootNode == null
            || (rootNode != null && rootNode.compareTo(newRootNode) == 0)) {
            // 配置值未发生改变
            return;
        }

        // 更新节点及版本号
        this.rootNode = newRootNode;
        this.version++;

        synchronized (this.watcherSet) {
            for (ConfigUpdateWatcher watcher: this.watcherSet) {
                // 如果UpdateWatcher.notify实现不合理, 此处会造成线程阻塞
                try {
                    watcher.notify(this.rootNode, this.version);
                } catch (Throwable th) {
                    LOG.debug("notify watch failed: ", th);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.netease.hmail.config.Configuration#addWatcher(com.netease.hmail.config
     * .UpdateWatcher)
     */
    @Override
    public boolean addWatcher(ConfigUpdateWatcher watcher) {
        synchronized (this.watcherSet) {
            return this.watcherSet.add(watcher);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.config.Configuration#clearWatcher()
     */
    @Override
    public void clearWatcher() {
        synchronized (this.watcherSet) {
            this.watcherSet.clear();
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.netease.hmail.config.Configuration#removeWatcher(com.netease.hmail
     * .config.UpdateWatcher)
     */
    @Override
    public void removeWatcher(ConfigUpdateWatcher watcher) {
        synchronized (this.watcherSet) {
            this.watcherSet.remove(watcher);
        }
    }

    /**
     * 获取系统当前定义的HMAIL的Home目录
     * 
     * @return
     */
    public static final String getSystemHmailHome() {
        return System.getProperty(PROPERTY_HMAIL_HOME, DEFAULT_HMAIL_HOME);
    }

    /**
     * 空白配置文档类
     * 
     * @author qiu_sheng
     */
    public static class EmptyConfigDocument extends BaseConfigDocument {

        /**
         * 构造函数
         */
        public EmptyConfigDocument() {
            super(new ConfigNode("root", "empty"));
        }

        @Override
        protected ConfigNode doLoad() throws Exception {
            return this.getRootNode();
        }

        @Override
        public boolean ignoreDb() {
            return true;
        }

        @Override
        public String getDbVersion() {
            return null;
        }

        @Override
        public Long getScanMillis() {
            return 0L;
        }
    }

}
