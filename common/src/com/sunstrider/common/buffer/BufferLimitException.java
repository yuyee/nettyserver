/**
 * @(#)BufferLimitException.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.buffer;

/**
 * buffer limit
 * 
 * @author linaoxiang
 */
public class BufferLimitException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public BufferLimitException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public BufferLimitException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public BufferLimitException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public BufferLimitException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
