/**
 * @(#)BothBufferFactory.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.buffer.impl;

import com.netease.hmail.common.buffer.Buffer;
import com.netease.hmail.common.buffer.BufferConfig;
import com.netease.hmail.common.buffer.BufferException;
import com.netease.hmail.common.buffer.BufferLimitException;
import com.netease.hmail.common.buffer.MemoryBufferLimitException;

/**
 * 支持在内存,磁盘 缓存。
 * <p>
 * {@link #initialize(BufferConfig)}初始化内存以及磁盘Buffer工厂
 * 
 * @author linaoxiang
 */
public class BothBufferFactory extends BaseBufferFactory {

    /**
     * 内存型 {@link MemoryBufferFactory}
     */
    MemoryBufferFactory memoryBufferFactory = new MemoryBufferFactory(
        new MemoryResourceManager());

    /**
     * 磁盘型 {@link DiskBufferFactory}
     */
    DiskBufferFactory diskBufferFactory = new DiskBufferFactory(
        new DiskResourceManager());

    /**
     * 优先内存缓存
     */
    BothBufferFactory() {}

    @Override
    public Buffer getBuffer(int capacity) throws BufferLimitException {

        Buffer current = null;
        try {
            current = memoryBufferFactory.getBuffer(capacity);
        } catch (MemoryBufferLimitException e) {
            current = this.diskBufferFactory.getBuffer(capacity);
        } catch (BufferLimitException e) {
            throw e;
        }

        return current;
    }

    @Override
    public Buffer getBuffer(byte[] array, int offset, int length)
        throws BufferLimitException {
        Buffer buffer = getBuffer(0);

        try {
            // 将原数据写到新的buffer中
            if (array != null) {
                buffer.writeBytes(array, offset, length);
            }
        } catch (Throwable t) {
            buffer = null;
            throw new BufferException("allocate buffer error", t);
        }

        return buffer;
    }

    @Override
    public void freeBuffer(AbstractBuffer buffer) {
        buffer.free();
    }

    @Override
    public void initialize(BufferConfig config) {
        this.memoryBufferFactory.initialize(config);
        this.diskBufferFactory.initialize(config);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BothBufferFactory [memoryBufferFactory=");
        builder.append(memoryBufferFactory);
        builder.append(", diskBufferFactory=");
        builder.append(diskBufferFactory);
        builder.append("]");
        return builder.toString();
    }

}
