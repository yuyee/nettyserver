/**
 * @(#)ResourceBufferFactory.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.buffer.impl;

import com.netease.hmail.common.buffer.BufferConfig;
import com.netease.hmail.common.buffer.BufferLimitException;
import com.netease.hmail.common.buffer.ResourceManager;

/**
 * 资源型
 * 
 * @author linaoxiang
 */
abstract class ResourceBufferFactory extends BaseBufferFactory {

    protected final ResourceManager manager;

    public ResourceBufferFactory(ResourceManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public void initialize(BufferConfig config) {
        this.manager.initialize(config);
    }

    /**
     * 申请资源
     * 
     * @param capacity
     * @return
     */
    protected long apply(int capacity) {
        if (capacity <= 0) {
            return 0;
        }

        long register = this.manager.register(capacity);

        if (register == 0 || register < 0) {
            throw new BufferLimitException("resource limit");
        }

        return register;
    }

    /**
     * 回收资源
     * 
     * @param capacity
     */
    protected void retrieve(int capacity) {
        this.manager.unRegister(capacity);
    }

}
