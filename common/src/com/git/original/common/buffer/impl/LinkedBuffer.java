/**
 * @(#)LinkedBuffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer.impl;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import com.git.original.common.buffer.Buffer;
import com.git.original.common.buffer.BufferLimitException;
import com.git.original.common.buffer.ReadOnlyBuffer;
import com.git.original.common.buffer.impl.BaseReadOnlyBuffer.ReadOnlyBufferFacotry;

/**
 * 链表式的Buffer数据链,可用来存储一些大数据块
 * 
 * @author linaoxiang
 */
public class LinkedBuffer extends AbstractBuffer {

    /**
     * Buffer链表
     */
    BufferNode<Buffer> header;

    /**
     * 当前写节点
     */
    AtomicReference<BufferNode<Buffer>> currentWrite = new AtomicReference<BufferNode<Buffer>>(
        null);

    /**
     * 当前读节点
     */
    AtomicReference<BufferNode<Buffer>> currentRead = new AtomicReference<BufferNode<Buffer>>(
        null);

    public LinkedBuffer(BothBufferFactory factory, Buffer current) {
        super(factory);
        this.header = new BufferNode<Buffer>(new MemoryBuffer(
            factory.memoryBufferFactory, 0));

        Buffer buffer = current;
        // default:128k
        if (buffer == null) {
            buffer = factory().getBuffer(128 * 1024);
        }

        // 设置当前读节点与写节点
        BufferNode<Buffer> append = this.header.append(buffer);
        this.currentRead.set(append);
        this.currentWrite.set(append);

    }

    @Override
    public void rewind() {
        super.rewind();

        // 重置读的头节点
        this.currentRead.set(this.header);

        // 并header开始，重置所有数据节点
        BufferNode<Buffer> current = header;
        while (current.hasNext()) {
            current = current.next;

            Buffer buffer = current.getData();

            // 节点重置
            try {
                buffer.rewind();
            } catch (Exception e) {
                // ignore exception
            }
        }

    }

    @Override
    protected void free() {
        BufferNode<Buffer> current = header;
        while (current.hasNext()) {
            current = current.next;

            Buffer buffer = current.getData();

            // 链表中具体的buffer类会调用自己的实例工厂将自己释放
            try {
                buffer.clear();
            } catch (Exception e) {
                // ignore exception
            }
        }
    }

    @Override
    public ReadOnlyBuffer cloneBuffer() {
        return new ReadOnlyLinkedBuffer(ReadOnlyBufferFacotry.getInstance(),
            new BufferNode<Buffer>(new MemoryBuffer(
                ((BothBufferFactory) factory).memoryBufferFactory, 0)), this,
            this.writerIndex);
    }

    @Override
    protected int getBytes(int index, byte[] dst, int dstIndex, int length) {

        BufferNode<Buffer> entry = currentRead.get();

        // 递归读取
        return read(entry, this.currentRead, dst, dstIndex, length);
    }

    /**
     * @param buffer
     * @param dst
     * @param dstIndex
     * @param len
     * @param pos
     */
    private int read(BufferNode<Buffer> buffer,
        AtomicReference<BufferNode<Buffer>> currentRead, byte[] dst,
        int dstIndex, int len) {

        Buffer data = buffer.getData();

        int readed = 0;
        // 读取数据
        if (data.readable() && dstIndex < dst.length && len > 0) {
            readed = readFully(data, dst, dstIndex, len);
            dstIndex += readed;
            len -= readed;
        }

        if (len > 0 && buffer.hasNext()) {
            // 继续读
            return read(buffer.next(), currentRead, dst, dstIndex, len)
                + readed;

        } else {
            // 结束
            currentRead.set(buffer);
            return readed;
        }
    }

    /**
     * 读取完整的数据
     * 
     * @param buffer
     * @param dst
     * @param dstIndex
     * @param len
     * @return
     */
    private int readFully(Buffer buffer, byte[] dst, int dstIndex, int len) {
        int readableBytes = buffer.readableBytes();
        readableBytes = Math.min(readableBytes, len);

        int readBytes = buffer.readBytes(dst, dstIndex, readableBytes);
        return readBytes;
    }

    @Override
    protected void setBytes(int index, byte[] src, int srcIndex, int length) {
        BufferNode<Buffer> entry = currentWrite.get();

        // 递归写入
        write(entry, src, srcIndex, length);
    }

    /**
     * @param buffer
     *            当前的写buffer
     * @param src
     *            数据源
     * @param srcIndex
     *            src的offset
     * @param len
     *            写的长度
     * @param pos
     *            已经写了多少
     * @throws BufferLimitException
     */
    private void write(BufferNode<Buffer> buffer, byte[] src, int srcIndex,
        int len) throws BufferLimitException {

        Buffer data = buffer.getData();

        // 写数据,buffer可写,且源数组的index不越界
        if (data.writable() && srcIndex < src.length && len > 0) {
            int writeFully = writeFully(data, src, srcIndex, len);
            srcIndex += writeFully; // offset+
            len -= writeFully; // len-
        }

        // 如果len>0,说明数据未写完，buffer满足不了len的长度，需要重新分配一个新的buffer
        if (len > 0) {
            // 尝试分配新的buffer继续写
            Buffer newBuffer = factory().getBuffer(len);
            BufferNode<Buffer> append = buffer.append(newBuffer);
            write(append, src, srcIndex, len);
        } else {
            // 结束
            this.currentWrite.set(buffer);
        }
    }

    /**
     * 写完整的数据
     * 
     * @param buffer
     * @param dst
     * @param dstIndex
     * @param len
     * @return
     */
    private int writeFully(Buffer buffer, byte[] src, int srcIndex, int len) {
        int writableBytes = buffer.writableBytes();
        if (writableBytes > len) {
            writableBytes = len;
        }

        buffer.writeBytes(src, srcIndex, writableBytes);
        return writableBytes;

    }

    /**
     * 链表节点
     * 
     * @author linaoxiang
     * @param <E>
     */
    class BufferNode<E> implements Iterator<BufferNode<E>> {
        private E element;

        private volatile BufferNode<E> next;

        public BufferNode(E element) {
            this.element = element;
            this.next = null;
        }

        public E getData() {
            return this.element;
        }

        public void setData(E e) {
            this.element = e;
        }

        public BufferNode<E> append(E e) {
            BufferNode<E> newEntry = new BufferNode<E>(e);
            this.next = newEntry;

            return newEntry;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public BufferNode<E> next() {
            return this.next;
        }

        @Override
        public void remove() {
            // 不支持删除节点
            throw new UnsupportedOperationException("BufferNode remove");
        }
    }

    @Override
    protected long transferTo(long position, long count, OutputStream target) {
        BufferNode<Buffer> entry = currentRead.get();

        // 递归读取
        return singleTransferTo(entry, currentRead, position, count, target);
    }

    /**
     * sub_buffer transferTo,递归完成数据的传输
     * 
     * @param buffer
     * @param dst
     * @param dstIndex
     * @param len
     * @param pos
     */
    private long singleTransferTo(BufferNode<Buffer> buffer,
        AtomicReference<BufferNode<Buffer>> currentRead, long position,
        long count, OutputStream target) {

        Buffer data = buffer.getData();
        long writeTo = 0;

        // 当前buffer数据可读，且整个读取过程未完成
        if (data.readable() && count > 0) {
            writeTo = data.writeTo(count, target);
            position += writeTo;
            count -= writeTo;
        }

        if (count > 0 && buffer.hasNext()) {
            // 继续读
            long singleTransferTo = singleTransferTo(buffer.next(),
                currentRead, position, count, target);

            return singleTransferTo + writeTo;
        } else {
            // 结束
            currentRead.set(buffer);
            return writeTo;
        }
    }

    static class ReadOnlyLinkedBuffer extends BaseReadOnlyBuffer {

        private final BufferNode<Buffer> header;

        /**
         * 当前读节点
         */
        AtomicReference<BufferNode<Buffer>> currentRead = new AtomicReference<BufferNode<Buffer>>(
            null);

        LinkedBuffer buffer;

        public ReadOnlyLinkedBuffer(ReadOnlyBufferFacotry factory,
            BufferNode<Buffer> newHeader, LinkedBuffer buffer, int writeIndex) {
            super(factory, writeIndex);

            this.header = newHeader;
            this.currentRead.set(header);
            this.buffer = buffer;

            //readonly link 
            BufferNode<Buffer> newLinkNode = this.header;

            // 原始链表的头
            BufferNode<Buffer> current = buffer.header;

            //clone
            while (current.hasNext()) {
                current = current.next;

                Buffer data = current.getData();
                ReadOnlyBuffer cloneBuffer = data.cloneBuffer();
                cloneBuffer.rewind();

                newLinkNode = newLinkNode.append(cloneBuffer);
            }
        }

        @Override
        protected void free() {

        }

        @Override
        protected int getBytes(int index, byte[] dst, int dstIndex, int length) {
            BufferNode<Buffer> entry = currentRead.get();

            // 递归读取
            return buffer.read(entry, this.currentRead, dst, dstIndex, length);
        }

        @Override
        protected long transferTo(long position, long count, OutputStream target) {
            BufferNode<Buffer> entry = currentRead.get();

            // 递归读取
            return buffer.singleTransferTo(entry, currentRead, position, count,
                target);
        }

    }

}
