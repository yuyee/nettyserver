/**
 * @(#)DefautlReadOnlyBuffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.buffer.impl;

import java.io.OutputStream;

/**
 * 基于AbstractBuffer的read-only Buffer
 * <p>
 * 此Buffer不具备关闭内部资源的功能,因为作为一个只读性的镜像点，不应具备直接更改底层资源的能力
 * 
 * @author linaoxiang
 */
public class DefautlReadOnlyBuffer extends BaseReadOnlyBuffer {

    /**
     * real resource
     */
    AbstractBuffer buffer;

    public DefautlReadOnlyBuffer(ReadOnlyBufferFacotry factory, int writeIndex) {
        super(factory, writeIndex);
    }

    public DefautlReadOnlyBuffer(ReadOnlyBufferFacotry factory,
        AbstractBuffer buffer) {
        super(factory, buffer.writerIndex());
        this.buffer = buffer;
    }

    @Override
    protected void free() {}

    @Override
    protected int getBytes(int index, byte[] dst, int dstIndex, int length) {
        // TODO Auto-generated method stub
        return buffer.getBytes(index, dst, dstIndex, length);
    }

    @Override
    protected long transferTo(long position, long count, OutputStream target) {
        // TODO Auto-generated method stub
        return buffer.transferTo(position, count, target);
    }
}
