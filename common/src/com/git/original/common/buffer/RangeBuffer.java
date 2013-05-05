/**
 * @(#)RangeBuffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer;

/**
 * 具有逻辑分区型的Buffer,可支持同时往不同的区域写数据
 * 
 * @author linaoxiang
 */
public interface RangeBuffer extends Buffer {

    /**
     * 可以根据rangePos将数据写到对应的逻辑分区里
     * 
     * @param rangePos
     *            分区标识
     * @param src
     *            源数据
     * @param srcIndex
     *            数据offset
     * @param length
     *            写入数据长度
     */
    public void range(int rangePos, byte[] src, int srcIndex, int length);

    /**
     * 获取buffer当前的range数
     * 
     * @return
     */
    public int getRangeCount();
}
