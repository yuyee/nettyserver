/**
 * @(#)RangeBufferFactory.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer.impl;

import com.git.original.common.buffer.BufferConfig;
import com.git.original.common.buffer.BufferException;
import com.git.original.common.buffer.BufferLimitException;
import com.git.original.common.buffer.RangeBuffer;

/**
 * 区域型buffer工厂类
 * 
 * @author linaoxiang
 */
public class RangeBufferFactory extends BaseBufferFactory {

    BothBufferFactory facotry = new BothBufferFactory();

    /**
     * 优先内存缓存
     */
    RangeBufferFactory() {}

    @Override
    public void initialize(BufferConfig config) {
        facotry.initialize(config);
    }

    @Override
    public RangeBuffer getBuffer(int capacity) throws BufferLimitException {
        return new BaseRangeBuffer(facotry);
    }

    @Override
    public RangeBuffer getBuffer(byte[] array, int offset, int length)
        throws BufferLimitException {

        RangeBuffer buffer = getBuffer(0);

        try {
            // 将原数据写到新的buffer中
            if (array != null) {
                buffer.range(0, array, offset, length);
            }
        } catch (Throwable t) {
            buffer = null;
            throw new BufferException("allocate buffer error", t);
        }

        return buffer;
    }

    @Override
    void freeBuffer(AbstractBuffer buffer) {
        buffer.free();
    }
}
