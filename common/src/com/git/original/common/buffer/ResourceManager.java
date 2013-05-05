/**
 * @(#)ResourceManager.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer;

/**
 * 资源管理器，以其管理机器资源的使用
 * 
 * @author linaoxiang
 */
public interface ResourceManager {

    /**
     * 重载配置
     * 
     * @param config
     */
    public void reload(BufferConfig config);

    /**
     * 初始化
     * 
     * @param config
     */
    public void initialize(BufferConfig config);

    /**
     * 申请大小为bufSize的buffer 空间
     * 
     * @param buffer
     */
    public long register(long bufSize);

    /**
     * 撤销一个大小为bufSize的buffer空间
     * 
     * @param buffer
     */
    public void unRegister(long bufSize);

}
