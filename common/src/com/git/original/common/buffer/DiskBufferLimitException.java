/**
 * @(#)DiskBufferLimitException.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer;

/**
 * 磁盘limit exception
 * 
 * @author linaoxiang
 */
@SuppressWarnings("serial")
public class DiskBufferLimitException extends BufferLimitException {

    public DiskBufferLimitException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public DiskBufferLimitException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public DiskBufferLimitException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public DiskBufferLimitException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
