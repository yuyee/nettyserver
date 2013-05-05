/**
 * @(#)BufferContainer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.git.original.common.buffer.Buffer;
import com.git.original.common.buffer.BufferConfig;
import com.git.original.common.buffer.RangeBuffer;

/**
 * Buffer容器
 * <p>
 * 实例化之前，请注意参考下机器的资源分配
 * 
 * @author linaoxiang
 */
public final class BufferContainer {
    /** 日志描述 */
    private static final Logger LOG = LoggerFactory
        .getLogger(BufferContainer.class);

    private static final class DefaultHolder {
        private static final BufferContainer instance = new BufferContainer();
    }

    private BufferContainer() {

    }

    public static final BufferContainer getInstance() {
        return DefaultHolder.instance;
    }

    private Map<String, BothBufferFactory> factorys = new HashMap<String, BothBufferFactory>();

    /**
     * 工具类初始化,默认为Both模式
     * 
     * @param config
     */
    public void initialize(BufferConfig config) {
        String prefix = config.getPrefix();
        BothBufferFactory bufferFactory = null;
        synchronized (factorys) {
            bufferFactory = factorys.get(prefix);
            if (bufferFactory != null) {
                bufferFactory.initialize(config);
            } else {
                bufferFactory = new BothBufferFactory();
                bufferFactory.initialize(config);
                this.factorys.put(prefix, bufferFactory);
            }
        }

        LOG.info("BufferContainer initialize a bufferFactory,{}",
            bufferFactory.toString());

    }

    /**
     * 分配buffer
     * 
     * @param capacity
     * @return
     */
    public Buffer linkedBuffer(String key, int capacity) {
        BothBufferFactory bufferFactory = this.factorys.get(key);

        return new LinkedBuffer(bufferFactory,
            bufferFactory.getBuffer(capacity));
    }

    /**
     * disk buffer
     * 
     * @param key
     * @return
     */
    public Buffer diskBuffer(String key) {
        BothBufferFactory bufferFactory = this.factorys.get(key);
        return bufferFactory.diskBufferFactory.getBuffer(1024);
    }

    /**
     * 分配buffer
     * 
     * @param capacity
     * @return
     */
    public RangeBuffer rangeBuffer(String key, int capacity) {
        BothBufferFactory bufferFactory = this.factorys.get(key);

        return new BaseRangeBuffer(bufferFactory);
    }

}
