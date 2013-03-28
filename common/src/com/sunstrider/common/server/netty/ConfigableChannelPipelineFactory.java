package com.sunstrider.common.server.netty;

import org.jboss.netty.channel.ChannelPipelineFactory;

import com.sunstrider.common.server.ServerConfig;

/**
 * 带系统配置信息的管道工厂基类
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
