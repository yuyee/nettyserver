/**
 * @(#)ConfigUpdateWatcher.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.config;

/**
 * 配置更新监控接口
 */
public interface ConfigUpdateWatcher {
    /**
     * 配置更新通知
     * <p>
     * 注意: 该方法的实现应该尽量精简, 不能出现长时间锁等待或阻塞的情况
     * 
     * @param node
     *            更新后的配置根节点
     * @param version
     *            更新后的配置版本号
     */
    void notify(ConfigNode node, long version);
}
