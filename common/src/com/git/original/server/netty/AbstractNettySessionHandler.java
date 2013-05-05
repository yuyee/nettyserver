package com.git.original.server.netty;

import org.jboss.netty.channel.Channel;

import com.git.original.common.utils.Utils;
import com.git.original.server.ISessionHandler;

/**
 * Netty框架下实现{@link ISessionHandler}接口的抽象基类
 */
public abstract class AbstractNettySessionHandler implements ISessionHandler {
    /**
     * 连接唯一ID
     */
    protected final long channelUid;

    /**
     * 连接对象
     */
    protected final Channel channel;

    /**
     * 会话全局唯一标记
     */
    protected String uniqueSessionId;

    /**
     * 构造函数
     * 
     * @param server
     * @param channelUid
     * @param channel
     * @throws Exception
     */
    public AbstractNettySessionHandler(NettyServer server, long channelUid,
        Channel channel) {
        this.channelUid = channelUid;
        this.channel = channel;
        this.uniqueSessionId = Utils.getGlobalSessionId(this.getServiceType(),
            channelUid, server.getLocalIp());
    }

    /**
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * @return the uniqueSessionId
     */
    public String getUniqueSessionId() {
        return uniqueSessionId;
    }

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.server.ISessionHandler#connectionOpened()
     */
    @Override
    public void connectionOpened() throws Exception {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.server.ISessionHandler#connectionClosed()
     */
    @Override
    public void connectionClosed() throws Exception {
        if (channel != null) {
            channel.close();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.server.ISessionHandler#connectionIdle()
     */
    @Override
    public void connectionIdle() throws Exception {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * @see
     * com.netease.hmail.server.ISessionHandler#messageReceived(java.lang.Object
     * )
     */
    @Override
    public void messageReceived(Object message) throws Exception {
        // TODO Auto-generated method stub
    }

}
