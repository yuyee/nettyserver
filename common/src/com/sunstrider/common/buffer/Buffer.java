/**
 * @(#)Buffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.buffer;

import java.io.OutputStream;

/**
 * buffer抽象接口
 * <ul>
 * 包含2个具体的实现类
 * <li>{@link com.sunstrider.common.buffer.impl.MemoryBuffer}
 * <li>{@link com.sunstrider.common.buffer.impl.DiskBuffer}
 * </ul>
 * 
 * @author linaoxiang
 */
public interface Buffer {

    /**
     * 往buffer里写数据
     * 
     * @param src
     *            源数据
     * @param srcIndex
     *            源数据起始点
     * @param length
     *            写入的数据长度
     */
    void writeBytes(byte[] src, int srcIndex, int length);

    /**
     * buffer写模式的pos
     * 
     * @return
     */
    int writerIndex();

    /**
     * buffer是否可写
     * 
     * @return
     */
    boolean writable();

    /**
     * buffer可写的数据长度
     */
    int writableBytes();

    /**
     * 将buffer的数据写入到{@link OutputStream}
     * 
     * @param count
     *            读取长度
     * @param out
     *            输出目的地地
     */
    public long writeTo(long count, OutputStream out);

    /**
     * 从buffer里读取数据
     * 
     * @param dst
     *            目标数组
     * @param dstIndex
     *            起始位置
     * @param length
     *            读取长度
     */
    int readBytes(byte[] dst, int dstIndex, int length);

    /**
     * 返回buffer可读的数据长度
     */
    int readableBytes();

    /**
     * buffer读模式的起始位置
     * 
     * @return
     */
    int readerIndex();

    /**
     * buffer是否可读
     * 
     * @return
     */
    boolean readable();

    /**
     * buffer容量
     * 
     * @return
     */
    int capacity();

    /**
     * 是否可用
     * 
     * @return
     */
    boolean isEnable();

    /**
     * 将read position 重置为 0,作为重复读的操作前提
     */
    void rewind();

    /**
     * 克隆一个可读的Buffer,readIndex=0
     * 
     * @return
     */
    public ReadOnlyBuffer cloneBuffer();

    /**
     * 清理buffer,释放该buffer占用的资源
     * <p>
     * 调用该操作之后，该buffer将不可用
     */
    void clear();

}
