/**
 * @(#)BaseReadOnlyBuffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.git.original.common.buffer.Buffer;
import com.git.original.common.buffer.BufferConfig;
import com.git.original.common.buffer.BufferException;
import com.git.original.common.buffer.BufferFactory;
import com.git.original.common.buffer.BufferLimitException;
import com.git.original.common.buffer.ReadOnlyBuffer;

/**
 * 只读buffer
 * <ul>
 * <li>可重复读
 * </ul>
 * 
 * @author linaoxiang
 */
public abstract class BaseReadOnlyBuffer extends AbstractBuffer implements
    ReadOnlyBuffer {

    public BaseReadOnlyBuffer(ReadOnlyBufferFacotry factory, int writeIndex) {
        super(factory);
        this.writerIndex = writeIndex;
    }

    @Override
    protected BaseBufferFactory factory() {
        checkUseable();
        return ReadOnlyBufferFacotry.getInstance();
    }

    @Override
    public void rewind() {
        super.rewind();
    }

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public boolean writable() {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public int writerIndex() {
        return writerIndex;
    }

    @Override
    public int writableBytes() {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    protected void setBytes(int index, byte[] src, int srcIndex, int length) {
        throw new UnsupportedOperationException("read only");
    }

    /**
     * 注意： 屏蔽{@link BufferFactory#getBuffer(int)}和
     * {@link BufferFactory#getBuffer(byte[], int, int)}
     * ,因为一旦你调用了这2个方法，即意味着你可以开辟存储区域，那么你这个Buffer也就不是一个只读的Buffer
     * 
     * @author linaoxiang
     */
    protected static class ReadOnlyBufferFacotry extends BaseBufferFactory {

        /** 日志描述 */
        private static final Logger LOG = LoggerFactory
            .getLogger(ReadOnlyBufferFacotry.class);

        private static final ReadOnlyBufferFacotry INSTANCE_BE = new ReadOnlyBufferFacotry();

        public static ReadOnlyBufferFacotry getInstance() {
            return INSTANCE_BE;
        }

        @Override
        public void initialize(BufferConfig config) {}

        @Override
        public Buffer getBuffer(int capacity) throws BufferLimitException {
            throw new BufferException("read only");
        }

        @Override
        public Buffer getBuffer(byte[] array, int offset, int length)
            throws BufferLimitException {
            throw new BufferException("read only");
        }

        @Override
        public void freeBuffer(AbstractBuffer buffer) {
            buffer.free();
            LOG.trace("free buffer,buffer={}", buffer.toString());
        }

    }

}
