/**
 * @(#)AbstractBuffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer.impl;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import com.git.original.common.buffer.Buffer;
import com.git.original.common.buffer.ReadOnlyBuffer;
import com.git.original.common.buffer.impl.BaseReadOnlyBuffer.ReadOnlyBufferFacotry;

/**
 * buffer抽象实现类
 * 
 * @author linaoxiang
 */
public abstract class AbstractBuffer implements Buffer {

    private static final AtomicLong seed = new AtomicLong(0);

    private long id = seed.incrementAndGet();

    AbstractBuffer(BaseBufferFactory factory) {
        this.factory = factory;
    }

    final BaseBufferFactory factory;

    /**
     * 已读postion
     */
    protected int readerIndex;

    /**
     * 已写postion
     */
    protected int writerIndex;

    /**
     * 该实例是否已经废弃
     */
    private boolean isClose = false;

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
        checkUseable();
        setBytes(writerIndex, src, srcIndex, length);
        writerIndex += length;
    }

    @Override
    public long writeTo(long count, OutputStream out) {
        checkUseable();
        int readableBytes = this.readableBytes();
        if (count > readableBytes) {
            count = readableBytes;
        }

        long bytes = transferTo(readerIndex, count, out);
        assert bytes >= 0;
        readerIndex += bytes;
        return bytes;
    }

    @Override
    public int readBytes(byte[] dst, int dstIndex, int length) {
        checkUseable();
        int bytes = getBytes(readerIndex, dst, dstIndex, length);
        assert bytes >= 0;
        readerIndex += bytes;
        return bytes;
    }

    @Override
    public boolean readable() {
        checkUseable();
        return readableBytes() > 0;
    }

    @Override
    public boolean writable() {
        checkUseable();
        return writableBytes() > 0;
    }

    @Override
    public int writerIndex() {
        checkUseable();
        return writerIndex;
    }

    @Override
    public int writableBytes() {
        checkUseable();
        return capacity() - writerIndex;
    }

    @Override
    public int readableBytes() {
        checkUseable();
        return writerIndex - readerIndex;
    }

    @Override
    public int readerIndex() {
        checkUseable();
        return readerIndex;
    }

    @Override
    public void clear() {
        if (isClose) {
            return;
        }

        readerIndex = 0;
        writerIndex = 0;

        // 由创建他的工厂负责清理它的资源
        factory().freeBuffer(this);

        isClose = true;
    }

    @Override
    public int capacity() {
        checkUseable();
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isEnable() {
        return !this.isClose;
    }

    @Override
    public void rewind() {
        setIndex(0, writerIndex);
    }

    @Override
    public ReadOnlyBuffer cloneBuffer() {
        return new DefautlReadOnlyBuffer(ReadOnlyBufferFacotry.getInstance(),
            this);
    }

    protected void setIndex(int readerIndex, int writerIndex) {
        if (readerIndex < 0 || readerIndex > writerIndex
            || writerIndex > capacity()) {
            throw new IndexOutOfBoundsException("Invalid writerIndex: "
                + writerIndex + " - Maximum is " + readerIndex + " or "
                + capacity());
        }
        this.readerIndex = readerIndex;
        this.writerIndex = writerIndex;
    }

    /**
     * 释放
     */
    protected abstract void free();

    /**
     * 获取创建该buffer的工厂
     * 
     * @return
     */
    protected BaseBufferFactory factory() {
        return this.factory;
    }

    /**
     * 获取数据
     * 
     * @param index
     * @param dst
     * @param dstIndex
     * @param length
     * @return 如果没有数据，则返回0
     */
    protected abstract int getBytes(int index, byte[] dst, int dstIndex,
        int length);

    /**
     * 写入数据
     * 
     * @param index
     * @param src
     * @param srcIndex
     * @param length
     */
    protected abstract void setBytes(int index, byte[] src, int srcIndex,
        int length);

    /**
     * 将字节从buffer传输到给定的可写入字节通道
     * 
     * @param position
     * @param count
     * @param target
     * @return 如果没有数据，则返回0
     */
    protected abstract long transferTo(long position, long count,
        OutputStream target);

    /**
     * 校验buffer是否有足够的数据可读
     * 
     * @param minimumReadableBytes
     */
    protected void checkReadableBytes(int minimumReadableBytes) {
        if (readableBytes() < minimumReadableBytes) {
            throw new IndexOutOfBoundsException(
                "Not enough readable bytes - Need " + minimumReadableBytes
                    + ", maximum is " + readableBytes());
        }
    }

    /**
     * 校验该buffer是否可用
     */
    protected void checkUseable() {
        if (this.isClose) {
            throw new IllegalAccessError("this buffer even close,buffer="
                + this);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AbstractBuffer [id=");
        builder.append(id);
        builder.append(", readerIndex=");
        builder.append(readerIndex);
        builder.append(", writerIndex=");
        builder.append(writerIndex);
        builder.append("]");
        return builder.toString();
    }

}
