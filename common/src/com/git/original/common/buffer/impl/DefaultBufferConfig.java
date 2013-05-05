/**
 * @(#)DefaultBufferConfig.java, 2013-2-24. Copyright 2013 Netease, Inc. All
 *                               rights reserved. NETEASE
 *                               PROPRIETARY/CONFIDENTIAL. Use is subject to
 *                               license terms.
 */
package com.git.original.common.buffer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.git.original.common.buffer.BufferConfig;
import com.git.original.common.config.ConfigNode;
import com.git.original.common.config.Configuration;

public class DefaultBufferConfig implements BufferConfig {

    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultBufferConfig.class);

    /**
     * 服务器配置项：可以保留在内存中附件大小
     */
    public static final String CONF_BUFFER_MEMORY_TOTAL_SIZE = "buffer.memory-total-size";

    /**
     * 服务器配置项：一次分配的大小
     */
    public static final String CONF_BUFFER_BASE_BUF_SIZE = "buffer.base-buf-size";

    /**
     * 服务器配置项：临时文件前缀
     */
    public static final String CONF_BUFFER_FILE_PRE = "buffer.file-pre";

    /**
     * 服务器配置项：一次最大分配
     */
    public static final String CONF_BUFFER_PERALLOC_LIMIT = "buffer.peralloc-limit";

    /**
     * 服务器配置项：磁盘存储大小上限
     */
    public static final String CONF_BUFFER_DISK_TOTAL_SIZE = "buffer.disk-total-size";

    /**
     * 服务器配置项：磁盘存储父目录
     */
    public static final String CONF_BUFFER_DISK_HOME = "buffer.disk-home";

    /**
     * 可申请的总内存上限
     */
    private long memoryTotalSize;

    /**
     * 每次申请的BUF基本大小
     */
    private long baseBufSize;

    /**
     * 临时文件前缀
     */
    private String pre = "buffer";

    /**
     * 每次申请内存大小上限
     */
    private long perAllocLimit;

    /**
     * 磁盘存储总容量
     */
    private long diskTotalSize;

    /**
     * 磁盘父目录
     */
    private String diskHome = "/home/hmail/var/buffer";

    Configuration config;

    public DefaultBufferConfig(Configuration config) {
        this.config = config;
    }

    public void loadConfig() {

        ConfigNode rootNode = config.getRootNode();
        this.pre = rootNode.getString(CONF_BUFFER_FILE_PRE, pre);
        LOG.info(CONF_BUFFER_FILE_PRE + "={}", pre);

        this.diskHome = rootNode
                .getString(CONF_BUFFER_DISK_HOME, this.diskHome);
        LOG.info(CONF_BUFFER_DISK_HOME + "={}", diskHome);

        this.baseBufSize = rootNode.getLong(CONF_BUFFER_BASE_BUF_SIZE, 3) << 10;
        LOG.info(CONF_BUFFER_BASE_BUF_SIZE + "={}", baseBufSize);

        this.diskTotalSize = rootNode.getLong(CONF_BUFFER_DISK_TOTAL_SIZE, 5) << 30;
        LOG.info(CONF_BUFFER_DISK_TOTAL_SIZE + "={}", diskTotalSize);

        this.memoryTotalSize = rootNode.getLong(CONF_BUFFER_MEMORY_TOTAL_SIZE,
                1) << 30;
        LOG.info(CONF_BUFFER_MEMORY_TOTAL_SIZE + "={}", memoryTotalSize);

        this.perAllocLimit = rootNode.getLong(CONF_BUFFER_PERALLOC_LIMIT, 64) << 10;
        LOG.info(CONF_BUFFER_PERALLOC_LIMIT + "={}", perAllocLimit);
    }

    @Override
    public String getDiskHome() {
        return this.diskHome;
    }

    @Override
    public String getPrefix() {
        return this.pre;
    }

    @Override
    public long getMemoryTotalSize() {
        return this.memoryTotalSize;
    }

    @Override
    public long getDiskTotalSize() {
        return this.diskTotalSize;
    }

    @Override
    public long getPerAllocLimit() {
        return this.perAllocLimit;
    }

    @Override
    public long getBaseBufSize() {
        return this.baseBufSize;
    }
}
