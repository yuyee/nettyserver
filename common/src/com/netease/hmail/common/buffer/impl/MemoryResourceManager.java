/**
 * @(#)MemoryResourceManager.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.buffer.impl;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.hmail.common.buffer.BufferConfig;
import com.netease.hmail.common.buffer.ResourceManager;

/**
 * 内存资源管理
 * 
 * @author linaoxiang
 */
public class MemoryResourceManager implements ResourceManager {
    /** 日志描述 */
    private static final Logger LOG = LoggerFactory
        .getLogger(MemoryResourceManager.class);

    /**
     * 总的可分配的buffer最大内存限制
     */
    private long totalSize = 10 * 1024 * 1024 * 1024;

    /**
     * 一次分配buffer大小的上限
     * <p>
     * -1: 表示不设限制, 每次都按原大小的2倍扩展
     */
    private long perAllocLimit = 32 * 1024 * 1024;

    /**
     * 默认每次申请的内存块大小: 128KB
     */
    private long baseBufSize = 128 * 1024;

    /**
     * 当前剩余使用量
     */
    private AtomicLong remianSize = new AtomicLong(0);

    @Override
    public long register(long bufSize) {
        if (bufSize < 0) {
            throw new IllegalArgumentException("buf size <0");
        }

        long len = 0;
        if (bufSize < baseBufSize) {
            len = baseBufSize;
        } else if (bufSize > perAllocLimit) {
            len = perAllocLimit;
        } else {
            len = bufSize;
        }

        // 不是很严格地检验是否缓存内存是否超限
        if (remianSize.addAndGet(-len) > 0) {
            return len;
        }

        LOG.warn("memory resource limit.............................");

        return remianSize.getAndSet(0);
    }

    @Override
    public void unRegister(long bufSize) {
        if (bufSize < 0) {
            throw new IllegalArgumentException("buf size <0");
        }

        remianSize.addAndGet(bufSize);
    }

    @Override
    public void initialize(BufferConfig config) {
        this.totalSize = config.getMemoryTotalSize();
        this.baseBufSize = config.getBaseBufSize();
        this.perAllocLimit = config.getPerAllocLimit();
        this.remianSize.set(totalSize);

        LOG.info(this.getClass().getSimpleName() + " initialize success.{}",
            toString());
    }

    @Override
    public void reload(BufferConfig config) {

        long oldTotal = this.totalSize;

        this.totalSize = config.getMemoryTotalSize();
        this.baseBufSize = config.getBaseBufSize();
        this.perAllocLimit = config.getPerAllocLimit();

        // 计算差值
        long dif = this.totalSize - oldTotal;
        this.remianSize.addAndGet(dif);

        LOG.info(this.getClass().getSimpleName() + " reload success.{}",
            toString());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MemoryResourceManager [totalSize=");
        builder.append(totalSize);
        builder.append(", perAllocLimit=");
        builder.append(perAllocLimit);
        builder.append(", baseBufSize=");
        builder.append(baseBufSize);
        builder.append(", ");
        if (remianSize != null) {
            builder.append("remianSize=");
            builder.append(remianSize);
        }
        builder.append("]");
        return builder.toString();
    }


}
