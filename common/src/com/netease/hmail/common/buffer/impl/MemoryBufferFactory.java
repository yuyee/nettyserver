/**
 * @(#)MemoryBufferFactory.java, 2013-2-24. Copyright 2013 Netease, Inc. All
 *                               rights reserved. NETEASE
 *                               PROPRIETARY/CONFIDENTIAL. Use is subject to
 *                               license terms.
 */
package com.netease.hmail.common.buffer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.hmail.common.buffer.Buffer;
import com.netease.hmail.common.buffer.BufferConfig;
import com.netease.hmail.common.buffer.MemoryBufferLimitException;
import com.netease.hmail.common.buffer.ResourceManager;

/**
 * 内存buffer创建工厂
 * 
 * @author linaoxiang
 */
public class MemoryBufferFactory extends ResourceBufferFactory {

    MemoryBufferFactory(ResourceManager manager) {
        super(manager);
    }

    /** 日志描述 */
    private static final Logger LOG = LoggerFactory
        .getLogger(MemoryBufferFactory.class);

    @Override
    public Buffer getBuffer(int capacity) throws MemoryBufferLimitException {
        byte[] buffer = null;

        try {
            long len = apply(capacity);
            buffer = new byte[(int) len];
        } catch (Throwable t) {
            buffer = null;
            throw new MemoryBufferLimitException("allocate buffer error", t);
        }

        MemoryBuffer memoryBuffer = new MemoryBuffer(this, buffer);
        LOG.trace("alloct buffer,buffer={}", memoryBuffer);
        return memoryBuffer;
    }

    @Override
    public Buffer getBuffer(byte[] array, int offset, int length)
        throws MemoryBufferLimitException {
        byte[] buffer = null;
        try {
            long len = apply(length);
            // 不是很严格地检验是否缓存内存是否超限
            buffer = new byte[(int) len];
            if (array != null) {
                System.arraycopy(array, 0, buffer, 0, array.length);
            }

        } catch (Throwable t) {
            buffer = null;
            throw new MemoryBufferLimitException("allocate buffer error", t);
        }

        MemoryBuffer memoryBuffer = new MemoryBuffer(this, buffer);
        // 设置writeIndex
        if (array != null) {
            memoryBuffer.setIndex(0, array.length);
        }

        LOG.trace("alloct buffer,buffer={}", memoryBuffer);
        return memoryBuffer;
    }

    @Override
    public void freeBuffer(AbstractBuffer buffer) {
        super.retrieve(buffer.capacity());

        buffer.free();
        LOG.trace("free buffer,buffer={}", buffer.toString());
    }

    @Override
    public void initialize(BufferConfig config) {

        super.initialize(config);

        LOG.info("MemoryBufferFactory initialize,{}", toString());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MemoryBufferFactory [manager=");
        builder.append(manager);
        builder.append("]");
        return builder.toString();
    }

}
