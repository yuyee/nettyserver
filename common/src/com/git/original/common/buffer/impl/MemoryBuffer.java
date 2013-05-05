/**
 * @(#)MemoryBuffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer.impl;

import java.io.IOException;
import java.io.OutputStream;

import com.git.original.common.buffer.BufferException;

/**
 * 内存buffer
 * 
 * @author linaoxiang
 */
public class MemoryBuffer extends AbstractBuffer {

    /**
     * 数据
     */
    protected byte[] array;

    /**
     * 创建一个待读写的buffer
     * 
     * @param length
     */
    public MemoryBuffer(MemoryBufferFactory factory, int length) {
        this(factory, new byte[length], 0, 0);
    }

    /**
     * 创建一个待读写的buffer
     * 
     * @param length
     */
    public MemoryBuffer(MemoryBufferFactory factory, byte[] array) {
        this(factory, array, 0, 0);
    }

    /**
     * 创建一个空的buffer
     * 
     * @param length
     */
    MemoryBuffer(MemoryBufferFactory factory, byte[] array, int readerIndex,
        int writerIndex) {
        super(factory);

        if (array == null) {
            throw new NullPointerException("array");
        }
        this.array = array;
        setIndex(readerIndex, writerIndex);
    }

    @Override
    public int capacity() {
        checkUseable();
        return array.length;
    }

    @Override
    protected int getBytes(int index, byte[] dst, int dstIndex, int length) {
        checkBounds(dstIndex, length, dst.length);

        length = Math.min(length, readableBytes());

        if (length < 0 || length == 0) {
            return 0;
        }

        System.arraycopy(array, index, dst, dstIndex, length);

        return length;
    }

    void checkBounds(int off, int len, int size) { // package-private
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    protected void setBytes(int index, byte[] src, int srcIndex, int length) {
        System.arraycopy(src, srcIndex, array, index, length);
    }

    @Override
    protected long transferTo(long position, long count, OutputStream target) {

        count = Math.min(count, readableBytes());
        if (count < 0 || count == 0) {
            return 0;
        }

        try {
            target.write(array, (int) position, (int) count);
        } catch (IOException e) {
            throw new BufferException("transferTo fail,position=" + position
                + ",count=" + count);
        }

        return count;

    }

    @Override
    protected void free() {
        this.array = null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MemoryBuffer [array=");
        builder.append(array == null ? 0 : array.length);
        builder.append("]");
        return builder.toString();
    }

}
