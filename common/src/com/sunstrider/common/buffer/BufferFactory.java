/**
 * @(#)BufferFactory.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.buffer;

/**
 * buffer工厂
 * <ul>
 * 包含3个基本的工厂实例
 * <li>{@link com.sunstrider.common.buffer.impl.DiskBufferFactory}
 * <li>{@link com.sunstrider.common.buffer.impl.MemoryBufferFactory}
 * <li>{@link com.sunstrider.common.buffer.impl.BothBufferFactory}
 * </ul>
 * 
 * @author linaoxiang
 */
public interface BufferFactory {

    /**
     * 初始化
     * 
     * @param config
     */
    public void initialize(BufferConfig config);

    /**
     * 根据容量返回Buffer实例
     * 
     * @param capacity
     * @return
     */
    Buffer getBuffer(int capacity) throws BufferLimitException;

    /**
     * 根据参数offset,length返回buffer实例
     * 
     * @param array
     * @param offset
     * @param length
     * @return
     */
    Buffer getBuffer(byte[] array, int offset, int length)
        throws BufferLimitException;

}
