/**
 * @(#)BaseBufferFactory.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer.impl;

import com.git.original.common.buffer.BufferFactory;

/**
 * 基础factory,{@link #freeBuffer(AbstractBuffer)}不对外公开
 * 
 * @author linaoxiang
 */
public abstract class BaseBufferFactory implements BufferFactory {

    /**
     * buffer的销毁由创建自己的工厂负责
     * 
     * @param buffer
     */
    abstract void freeBuffer(AbstractBuffer buffer);

}
