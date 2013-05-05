package com.git.original.server.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.git.original.server.ISessionHandler;

/**
 * Netty框架的基本通道处理器
 */
public class NettyChannelHandler extends IdleableNettyChannelHandler {

    private static final Logger LOG = LoggerFactory
        .getLogger(NettyChannelHandler.class);

    /**
     * 所属的服务器对象
     */
    protected final NettyServer server;

    /**
     * 创建实例
     * 
     * @param server
     *            所属的服务器对象
     */
    public NettyChannelHandler(NettyServer server) {
        super(server.getAllServiceChannels());
        this.server = server;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss
     * .netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
        throws Exception {
        ctx.setAttachment(server.createHandler(
            NettyServer.channelIdGenerator.getAndIncrement(), e.getChannel()));

        ISessionHandler handler = (ISessionHandler) ctx.getAttachment();
        if (handler != null) {
            handler.connectionOpened();
        }
        super.channelConnected(ctx, e);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#channelDisconnected(org.
     * jboss.netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelStateEvent)
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx,
        ChannelStateEvent e) {
        try {
            ISessionHandler handler = (ISessionHandler) ctx.getAttachment();
            if (handler != null) {
                handler.connectionClosed();
            }
            super.channelDisconnected(ctx, e);
        } catch (Throwable t) {
            LOG.warn(t.getLocalizedMessage());
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss
     * .netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.MessageEvent)
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
        throws Exception {
        ISessionHandler handler = (ISessionHandler) ctx.getAttachment();
        if (handler != null) {
            handler.messageReceived(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.netease.hmail.server.netty.IdleableNettyChannelHandler#channelIdle
     * (org.jboss.netty.channel.ChannelHandlerContext,
     * org.jboss.netty.handler.timeout.IdleStateEvent)
     */
    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e)
        throws Exception {
        ISessionHandler handler = (ISessionHandler) ctx.getAttachment();
        if (handler != null) {
            handler.connectionIdle();
        }
    }

}
