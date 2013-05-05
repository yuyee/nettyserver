package com.git.original.server.netty;

import org.jboss.netty.channel.ChannelPipelineFactory;

import com.git.original.server.ServerConfig;

/**
 * 带系统配置信息的管道工厂基类
 * 
 * @author linaoxiang
 */
public abstract class ConfigableChannelPipelineFactory implements
		ChannelPipelineFactory {
	/** 系统配置对象 */
	protected final ServerConfig config;

	/**
	 * 构造函数
	 * 
	 * @param config
	 */
	public ConfigableChannelPipelineFactory(ServerConfig config) {
		this.config = config;
	}
}
