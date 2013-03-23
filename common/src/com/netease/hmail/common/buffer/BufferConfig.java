/**
 * @(#)BufferConfig.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.buffer;

/**
 * buffer配置
 * 
 * @author linaoxiang
 */
public interface BufferConfig {

    /**
     * buffer cache的name，根据名字可找到对应的Buffer工厂
     * 
     * @return
     */
    public String getPrefix();

    /**
     * 获取磁盘缓存主目录
     * 
     * @return
     */
    public String getDiskHome();

    /**
     * 获取内存缓存总大小
     * 
     * @return
     */
    public long getMemoryTotalSize();

    /**
     * 获取磁盘缓存的总大小
     * 
     * @return
     */
    public long getDiskTotalSize();

    /**
     * 获取一次内存分配最大上限
     * 
     * @return
     */
    public long getPerAllocLimit();

    /**
     * 获取一次常规内顿分配大小
     * 
     * @return
     */
    public long getBaseBufSize();

}
