/**
 * @(#)DiskResourceManager.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.buffer.impl;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunstrider.common.buffer.BufferConfig;
import com.sunstrider.common.buffer.ResourceManager;

/**
 * 磁盘资源管理器
 * 
 * @author linaoxiang
 */
public class DiskResourceManager implements ResourceManager {
    private static final Logger LOG = LoggerFactory
        .getLogger(DiskResourceManager.class);

    /**
     * 总的可分配的buffer最大磁盘限制
     */
    private long totalSize = 100 * 1024 * 1024 * 1024;

    /**
     * 总的可分配的buffer最大内存限制:100g
     */
    private AtomicLong remainSize = new AtomicLong(0);

    @Override
    public long register(long bufSize) {
        if (bufSize < 0) {
            throw new IllegalArgumentException("buf size <0");
        }

        if (remainSize.addAndGet(-bufSize) > 0) {
            return bufSize;
        }

        LOG.warn("disk resource limit.............................");
        return remainSize.getAndSet(0);
    }

    @Override
    public void unRegister(long bufSize) {
        if (bufSize < 0) {
            throw new IllegalArgumentException("buf size <0");
        }

        remainSize.addAndGet(bufSize);

    }

    @Override
    public void initialize(BufferConfig config) {
        this.totalSize = config.getDiskTotalSize();
        this.remainSize.set(totalSize);

        LOG.info(this.getClass().getSimpleName() + " initialize success.{}",
            toString());
    }

    @Override
    public void reload(BufferConfig config) {
        long oldTotal = this.totalSize;

        this.totalSize = config.getDiskTotalSize();
        // 计算差值
        long dif = this.totalSize - oldTotal;
        this.remainSize.addAndGet(dif);

        LOG.info(this.getClass().getSimpleName() + " reload success.{}",
            toString());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DiskResourceManager [totalSize=");
        builder.append(totalSize);
        builder.append(", remainSize=");
        builder.append(remainSize.get());
        builder.append("]");
        return builder.toString();
    }

}
