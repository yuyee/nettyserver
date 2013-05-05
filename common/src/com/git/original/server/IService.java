package com.git.original.server;

/**
 * 系统服务接口类
 * 
 * @author linaoxiang
 */
public interface IService {
	/**
	 * 服务准备
	 */
	public void prepare() throws Exception;

	/**
	 * 服务启动
	 */
	public void start() throws Exception;

	/**
	 * 服务关闭
	 * 
	 * @throws Exception
	 */
	public void shutdown() throws Exception;

	/**
	 * 获取当前服务运行状态
	 * 
	 * @return true=已经被启动;false=未被启动
	 */
	public boolean isRunning();

	/**
	 * 获取当前服务关闭状态
	 * 
	 * @return true=已经被关闭;false=未被关闭
	 */
	public boolean isClosed();

	/**
	 * 获取本服务对应的服务配置名称
	 * 
	 * @return
	 */
	public String getConfigName();
}
