/**
 * @(#)BaseRangeBuffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.buffer.impl;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sunstrider.common.buffer.Buffer;
import com.sunstrider.common.buffer.BufferException;
import com.sunstrider.common.buffer.RangeBuffer;
import com.sunstrider.common.buffer.ReadOnlyBuffer;
import com.sunstrider.common.buffer.impl.BaseReadOnlyBuffer.ReadOnlyBufferFacotry;

/**
 * 具有逻辑分区功能的buffer
 * <ul>
 * 注意：顺序读取数据
 * <li>1.该buffer保证并发性
 * <li>2.写的时候最好自己能保证数据写完整
 * <li>3.如果要读到完整的数据，那调用者自身必须要明确已经将数据完整的写到本Buffer，不然从当前整棵树的镜像去读,是读不到完整数据的
 * </ul>
 * 
 * @author linaoxiang
 */
public class BaseRangeBuffer extends AbstractBuffer implements RangeBuffer {

    public BaseRangeBuffer(BothBufferFactory factory) {
        super(factory);
    }

    /**
     * range buffer数据存储点
     * <p>
     * pos->buffer映射
     */
    private ConcurrentSkipListMap<Integer, Buffer> ranges = new ConcurrentSkipListMap<Integer, Buffer>();

    /**
     * 上一次读的pos
     */
    private AtomicInteger lastPos = new AtomicInteger(0);

    /**
     * 锁对象
     */
    protected Lock rangeLock = new ReentrantLock();

    @Override
    public ReadOnlyBuffer cloneBuffer() {
        return new ReadOnlyRangeBuffer(ReadOnlyBufferFacotry.getInstance(),
            ranges, this, this.writerIndex);
    }

    @Override
    public void range(int rangePos, byte[] src, int srcIndex, int length) {
        rangeLock.lock();

        try {
            Buffer range = rangeBuffer(rangePos);

            range.writeBytes(src, srcIndex, length);
            writerIndex += length;
        } finally {
            rangeLock.unlock();
        }
    }

    /**
     * 获取对应的分片Buffer
     * 
     * @param rangePos
     * @return
     */
    private Buffer rangeBuffer(int rangePos) {

        // 保证原子性
        rangeLock.lock();
        Buffer buffer = null;
        try {
            buffer = ranges.get(rangePos);
            if (buffer == null) {
                buffer = new LinkedBuffer((BothBufferFactory) this.factory,
                    ((BothBufferFactory) this.factory).getBuffer(1024));
                this.ranges.put(rangePos, buffer);
            }
        } finally {
            rangeLock.unlock();
        }

        return buffer;
    }

    @Override
    public void clear() {
        rangeLock.lock();
        try {
            super.clear();
            free();
        } finally {
            rangeLock.unlock();
        }
    }

    @Override
    public void writeBytes(byte[] src, int srcIndex, int length) {
        throw new UnsupportedOperationException(
            "Range buffer unsupport mehtod:writeBytes()");
    }

    @Override
    public int getRangeCount() {
        return this.ranges.size();
    }

    @Override
    public int capacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void rewind() {
        rangeLock.lock();
        try {
            super.rewind();

            Collection<Buffer> values = this.ranges.values();
            for (Buffer buffer: values) {
                if (buffer == null) {
                    continue;
                }
                buffer.rewind();
            }

            this.lastPos.set(0);
        } finally {
            rangeLock.unlock();
        }
    }

    @Override
    protected void free() {
        Set<Entry<Integer, Buffer>> entrySet = this.ranges.entrySet();

        rangeLock.lock();

        try {
            for (Entry<Integer, Buffer> entry: entrySet) {
                if (entry != null) {
                    try {
                        entry.getValue().clear();
                    } catch (Exception e) {
                        // ignore exception
                    }
                }
            }
        } finally {
            rangeLock.unlock();
        }

    }

    @Override
    protected void setBytes(int index, byte[] src, int srcIndex, int length) {
        // 已屏蔽# writeBytes(byte[] src, int srcIndex, int length),此方法将不再调用到
    }

    @Override
    protected int getBytes(int index, byte[] dst, int dstIndex, int length) {
        rangeLock.lock();
        try {
            return read(this.ranges, lastPos, lastPos.get(), dst, dstIndex,
                length);
        } finally {
            rangeLock.unlock();
        }
    }

    /**
     * 递归读取数据
     * <ul>
     * 注意：
     * <li>1.此方法的并发由上层调用控制
     * <li>2.如果要读到完整的数据，那调用者自身必须要明确已经将数据完整的写到本Buffer，不然从当前整棵树的镜像去读,是读不到完整数据的
     * </ul>
     * 
     * @param buffer
     * @param dst
     * @param dstIndex
     * @param len
     * @param pos
     *            range的key,通过pos,可获取对应的Buffer块
     */
    private int read(ConcurrentSkipListMap<Integer, Buffer> ranges,
        AtomicInteger lastPos, int pos, byte[] dst, int dstIndex, int len) {

        Buffer data = getCurrentBuffer(ranges, pos);

        int readed = 0;
        // 读取数据
        if (data.readable() && dstIndex < dst.length && len > 0) {
            readed = readFully(data, dst, dstIndex, len);
            dstIndex += readed;
            len -= readed;
        }

        // len>0说明当前的buffer已经读完，需要继续读下一个Buffer
        // this.ranges.lastKey() > pos说明当前的range块还未读到末尾节点
        if (len > 0 && ranges.lastKey() > pos) {
            // 获取下一个pos
            Integer higherKey = ranges.higherKey(pos);
            // 继续读
            return read(ranges, lastPos, higherKey, dst, dstIndex, len)
                + readed;
        } else {
            // 结束
            lastPos.set(pos);
            return readed;
        }
    }

    /**
     * 获取当前可读的最小pos buffer
     * 
     * @param pos
     * @return
     */
    private Buffer getCurrentBuffer(
        ConcurrentSkipListMap<Integer, Buffer> ranges, int pos) {
        Buffer data = null;
        if (pos == 0) {
            Entry<Integer, Buffer> firstEntry = ranges.firstEntry();
            if (firstEntry == null) {
                throw new BufferException("firstEntry  is null,lastPos="
                    + lastPos.get());
            }

            data = firstEntry.getValue();
        } else {
            data = ranges.get(pos);
        }

        return data;
    }

    /**
     * 尽量读取完整的数据
     * 
     * @param buffer
     *            源数据
     * @param dst
     *            目标数组
     * @param dstIndex
     *            起始位置
     * @param len
     *            读取长度
     * @return 返回真正读取的长度
     */
    private int readFully(Buffer buffer, byte[] dst, int dstIndex, int len) {
        int readableBytes = buffer.readableBytes();

        if (readableBytes > len) {
            readableBytes = len;
        }

        buffer.readBytes(dst, dstIndex, readableBytes);
        return readableBytes;
    }

    @Override
    protected long transferTo(long position, long count, OutputStream target) {
        rangeLock.lock();
        try {
            return singleTransferTo(this.ranges, this.lastPos, lastPos.get(),
                target, position, count);
        } finally {
            rangeLock.unlock();
        }
    }

    /**
     * sub_buffer transferTo,递归完成数据的传输
     * 
     * @param pos
     *            range标识
     * @param target
     * @param position
     * @param count
     * @return
     */
    private long singleTransferTo(
        ConcurrentSkipListMap<Integer, Buffer> ranges, AtomicInteger lastPos,
        int pos, OutputStream target, long position, long count) {
        Buffer data = getCurrentBuffer(ranges, pos);

        // 此次读取长度
        long readed = 0;

        // 读取数据
        if (data.readable() && count > 0) {
            readed = data.writeTo(count, target);
            position += readed;
            count -= readed;
        }

        // len>0说明当前的buffer已经读完，需要继续读下一个Buffer
        // this.ranges.lastKey() > pos说明当前的range块还未读到末尾节点
        if (count > 0 && ranges.lastKey() > pos) {
            // 获取下一个pos
            Integer higherKey = ranges.higherKey(pos);
            // 继续读
            return singleTransferTo(ranges, lastPos, higherKey, target,
                position, count) + readed;
        } else {
            // 结束
            lastPos.set(pos);
            return readed;
        }
    }

    static class ReadOnlyRangeBuffer extends BaseReadOnlyBuffer {

        /**
         * range buffer数据存储点
         * <p>
         * pos->buffer映射
         */
        private final ConcurrentSkipListMap<Integer, Buffer> ranges;

        /**
         * 上一次读的pos
         */
        private final AtomicInteger lastPos = new AtomicInteger(0);

        /**
         * 仅仅作为方法的提供者。。。。
         */
        final BaseRangeBuffer buffer;

        public ReadOnlyRangeBuffer(ReadOnlyBufferFacotry factory,
            ConcurrentSkipListMap<Integer, Buffer> ranges,
            BaseRangeBuffer buffer, int writeIndex) {
            super(factory, writeIndex);
            this.buffer = buffer;
            this.ranges = new ConcurrentSkipListMap<Integer, Buffer>();

            //clone
            Set<Entry<Integer, Buffer>> entrySet = ranges.entrySet();
            for (Entry<Integer, Buffer> entry: entrySet) {
                ReadOnlyBuffer cloneBuffer = entry.getValue().cloneBuffer();
                this.ranges.put(entry.getKey(), cloneBuffer);
            }
        }

        @Override
        protected void free() {

        }

        @Override
        protected int getBytes(int index, byte[] dst, int dstIndex, int length) {
            return buffer.read(this.ranges, lastPos, lastPos.get(), dst,
                dstIndex, length);
        }

        @Override
        protected long transferTo(long position, long count, OutputStream target) {
            return buffer.singleTransferTo(ranges, lastPos, lastPos.get(),
                target, position, count);
        }

        @Override
        public void rewind() {

            super.rewind();

            Collection<Buffer> values = this.ranges.values();
            for (Buffer buffer: values) {
                if (buffer == null) {
                    continue;
                }
                buffer.rewind();
            }

            this.lastPos.set(0);
        }

    }
}
