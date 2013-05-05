/**
 * @(#)Configuration.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.config;

/**
 * 配置文档接口类
 * 
 * @author linaoxiang
 */
public interface Configuration {
	/**
	 * 获取当前配置版本号
	 * 
	 * @return 版本号
	 */
	long getVersion();

	/**
	 * 获取本配置在数据库中的版本号
	 * 
	 * @return
	 */
	String getDbVersion();

	/**
	 * 是否忽略数据库的配置版本
	 * 
	 * @return true=忽略
	 */
	boolean ignoreDb();

	/**
	 * 获取扫描配置是否发生变化,并自动重载的时间周期(毫秒)
	 * <ul>
	 * <li>值=null: 未定义,使用默认扫描时间周期
	 * <li>值<=0: 拒绝使用自动扫描
	 * </ul>
	 * 
	 * @return 自动重载的时间周期(毫秒)
	 */
	Long getScanMillis();

	/**
	 * 获取配置文档中的配置根节点
	 * 
	 * @return 配置根节点
	 */
	ConfigNode getRootNode();

	/**
	 * 载入配置参数
	 * <p>
	 * 可以被重复调用, 每次载入成功时应该自动更新配置版本号
	 * 
	 * @throws Exception
	 *             载入参数失败
	 */
	void load() throws Exception;

	/**
	 * 添加一个配置更新监控器
	 * 
	 * @param watcher
	 *            配置更新监控器实例
	 * @return true=添加成功; false=该监控器已添加
	 */
	boolean addWatcher(ConfigUpdateWatcher watcher);

	/**
	 * 移除一个配置更新监控器
	 * 
	 * @param watcher
	 *            配置更新监控器实例
	 */
	void removeWatcher(ConfigUpdateWatcher watcher);

	/**
	 * 清除所有配置更新监控器
	 */
	void clearWatcher();
}
