/**
 * @(#)MemoryBufferLimitException.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.buffer;

/**
 * 内存可分配的buffer到达上限
 * 
 * @author linaoxiang
 */
public class MemoryBufferLimitException extends BufferLimitException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MemoryBufferLimitException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public MemoryBufferLimitException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public MemoryBufferLimitException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public MemoryBufferLimitException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
