package com.sunstrider.common.server.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty框架的基本通道处理器抽象基类
 */
public abstract class IdleableNettyChannelHandler extends SimpleChannelHandler {

    /**
     * 所属的通道组对象
     */
    private final ChannelGroup channelGroup;

    /** 普通日志记录 */
    private static final Logger LOG = LoggerFactory
        .getLogger(IdleableNettyChannelHandler.class);

    /**
     * 创建实例
     * 
     * @param server
     *            所属的服务器对象
     */
    public IdleableNettyChannelHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#channelOpen(org.jboss.netty
     * .channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
        throws Exception {
        Channel channel = e.getChannel();
        if (channel != null) {
            LOG.trace("channel open. channel_id={}", channel.getId());
        }

        if (this.channelGroup != null) {
            this.channelGroup.add(e.getChannel());
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#channelClosed(org.jboss.
     * netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
        throws Exception {
        Channel channel = e.getChannel();
        if (channel != null) {
            LOG.trace("channel closed. channel_id={}", channel.getId());
        }

        if (this.channelGroup != null) {
            this.channelGroup.remove(e.getChannel());
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#handleUpstream(org.jboss
     * .netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelEvent)
     */
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
        throws Exception {
        if (e instanceof IdleStateEvent) {
            channelIdle(ctx, (IdleStateEvent) e);
        } else {
            super.handleUpstream(ctx, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#exceptionCaught(org.jboss
     * .netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ExceptionEvent)
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
        throws Exception {
        if (e == null)
            return;
        Channel channel = e.getChannel();
        if (channel == null)
            return;

        try {
            if (channel.isConnected()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("channel_id=" + channel.getId() + " exception: ",
                        e.getCause());
                } else {
                    LOG.debug("channel_id=" + channel.getId() + " exception: "
                        + e.getCause());
                }
            }
        } finally {
            channel.close();
        }
    }

    /**
     * 通道触发IDLE事件
     * 
     * @param ctx
     * @param e
     * @throws Exception
     */
    public abstract void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e)
        throws Exception;

}
