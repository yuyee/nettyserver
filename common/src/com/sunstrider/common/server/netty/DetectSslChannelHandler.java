package com.sunstrider.common.server.netty;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;

/**
 */
public class DetectSslChannelHandler implements ChannelUpstreamHandler {
    /**
     * 所属服务器实例
     */
    private final NettyServer server;

    /**
     * 构造函数
     * 
     * @param server
     *            所属服务器
     */
    public DetectSslChannelHandler(NettyServer server) {
        if (server == null) {
            throw new NullPointerException("server is null");
        }

        this.server = server;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jboss.netty.channel.SimpleChannelUpstreamHandler#handleUpstream(org
     * .jboss.netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelEvent)
     */
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
        throws Exception {

        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent evt = (ChannelStateEvent) e;
            if (evt.getState() == ChannelState.CONNECTED
                && evt.getValue() != null) {
                server.checkAndTriggerSsl(evt.getChannel());
            }
        }

        ctx.sendUpstream(e);
    }

}
