/**
 * @(#)ReadOnlyBuffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.buffer;

/**
 * 只读标识接口
 * <ul>
 * 屏蔽RangeBuffer的几个读操作接口
 * <li>{@link RangeBuffer#writeBytes(byte[], int, int)}
 * <li>{@link RangeBuffer#range(int, byte[], int, int)}
 * <li>{@link RangeBuffer#writableBytes()}
 * <li>{@link RangeBuffer#writerIndex()}
 * </ul>
 * 
 * @author linaoxiang
 */
public interface ReadOnlyBuffer extends Buffer {
}
