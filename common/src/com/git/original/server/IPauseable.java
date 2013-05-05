package com.git.original.server;

/**
 * 支持暂停 和 暂停恢复 接口类
 * 
 * @author Administrator
 */
public interface IPauseable {
	/**
	 * 暂停
	 * <p>
	 * 该方法的实现不应该出现长时间的堵塞
	 */
	public void pause();

	/**
	 * 暂停恢复
	 * <p>
	 * 该方法的实现不应该出现长时间的堵塞
	 */
	public void unpause();
}
