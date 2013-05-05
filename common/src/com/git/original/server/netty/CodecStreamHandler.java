package com.git.original.server.netty;

import java.net.SocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * 协议字节流编解码处理器抽象类
 * <p>
 * 注意: 必须一个Channel对应一个编解码处理器实例
 */
public abstract class CodecStreamHandler extends SimpleChannelUpstreamHandler
    implements ChannelDownstreamHandler {

    /**
     * 帧数据缓存
     */
    protected ChannelBuffer cumulation;

    /**
     * 缓存块自动缩减时的容量下限
     */
    private int minShrinkCapacity = 32 * 1024;

    /**
     * 构造函数
     */
    public CodecStreamHandler() {}

    public int getMinShrinkCapacity() {
        return minShrinkCapacity;
    }

    public void setMinShrinkCapacity(int minCapacity) {
        this.minShrinkCapacity = minCapacity;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
        throws Exception {

        Object m = e.getMessage();
        if (!(m instanceof ChannelBuffer)) {
            ctx.sendUpstream(e);
            return;
        }

        ChannelBuffer input = (ChannelBuffer) m;
        if (!input.readable()) {
            return;
        }

        ChannelBuffer cumulation = cumulation(ctx);
        if (cumulation.readable()) {
            cumulation.discardReadBytes();
            cumulation.writeBytes(input);
            callDecode(ctx, e.getChannel(), cumulation, e.getRemoteAddress());
        } else {
            callDecode(ctx, e.getChannel(), input, e.getRemoteAddress());
            if (input.readable()) {
                cumulation.writeBytes(input);
            }
        }
    }

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent evt)
        throws Exception {
        if (!(evt instanceof MessageEvent)) {
            ctx.sendDownstream(evt);
            return;
        }

        MessageEvent e = (MessageEvent) evt;
        Object originalMessage = e.getMessage();

        if (originalMessage instanceof ChannelBuffer) {
            // 对于ChannelBuffer不尝试编码
            ctx.sendDownstream(evt);
        } else {
            ChannelBuffer encodedMessage = encode(ctx, e.getChannel(),
                originalMessage);
            if (encodedMessage != null) {
                Channels.write(ctx, e.getFuture(), encodedMessage,
                    e.getRemoteAddress());
            }
        }
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx,
        ChannelStateEvent e) throws Exception {
        cleanup(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
        throws Exception {
        cleanup(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
        throws Exception {
        ctx.sendUpstream(e);
    }

    /**
     * Decodes the received data so far into a frame when the channel is
     * disconnected.
     * 
     * @param ctx
     *            the context of this handler
     * @param channel
     *            the current channel
     * @param buffer
     *            the cumulative buffer of received packets so far. Note that
     *            the buffer might be empty, which means you should not make an
     *            assumption that the buffer contains at least one byte in your
     *            decoder implementation.
     * @return the decoded frame if a full frame was received and decoded.
     *         {@code null} if there's not enough data in the buffer to decode a
     *         frame.
     */
    protected Object decodeLast(ChannelHandlerContext ctx, Channel channel,
        ChannelBuffer buffer) throws Exception {
        return decode(ctx, channel, buffer);
    }

    private void callDecode(ChannelHandlerContext context, Channel channel,
        ChannelBuffer cumulation, SocketAddress remoteAddress) throws Exception {

        if (cumulation.readable()) {
            do {
                int oldReaderIndex = cumulation.readerIndex();
                Object frame = decode(context, channel, cumulation);
                if (frame == null) {
                    if (oldReaderIndex == cumulation.readerIndex()) {
                        // Seems like more data is required.
                        // Let us wait for the next notification.
                        break;
                    } else {
                        // Previous data has been discarded.
                        // Probably it is reading on.
                        continue;
                    }
                } else if (oldReaderIndex == cumulation.readerIndex()) {
                    throw new IllegalStateException(
                        "decode() method must read at least one byte "
                            + "if it returned a frame (caused by: "
                            + getClass() + ")");
                }

                Channels.fireMessageReceived(context, frame, remoteAddress);
            } while (cumulation.readable());

            // 尝试缩减缓存
            shrinkCumulation(context);
        }
    }

    private void cleanup(ChannelHandlerContext ctx, ChannelStateEvent e)
        throws Exception {
        try {
            ChannelBuffer cumulation = this.cumulation;
            if (cumulation == null) {
                return;
            } else {
                this.cumulation = null;
            }

            if (cumulation.readable()) {
                // Make sure all frames are read before notifying a closed
                // channel.
                callDecode(ctx, ctx.getChannel(), cumulation, null);
            }

            // Call decodeLast() finally. Please note that decodeLast() is
            // called even if there's nothing more to read from the buffer to
            // notify a user that the connection was closed explicitly.
            Object partialFrame = decodeLast(ctx, ctx.getChannel(), cumulation);
            if (partialFrame != null) {
                Channels.fireMessageReceived(ctx, partialFrame, null);
            }
        } finally {
            ctx.sendUpstream(e);
        }
    }

    /**
     * @param ctx
     * @return
     */
    protected ChannelBuffer cumulation(ChannelHandlerContext ctx) {
        ChannelBuffer c = cumulation;
        if (c == null) {
            c = ChannelBuffers.dynamicBuffer(ctx.getChannel().getConfig()
                .getBufferFactory());
            cumulation = c;
        }
        return c;
    }

    /**
     * 自动缩小超长的帧数据缓存
     * 
     * @param ctx
     * @return
     */
    protected void shrinkCumulation(ChannelHandlerContext ctx) {
        ChannelBuffer c = cumulation;
        if (this.cumulation != null) {
            if (cumulation.readableBytes() < this.minShrinkCapacity
                && cumulation.capacity() > this.minShrinkCapacity) {
                c = ChannelBuffers.dynamicBuffer(this.minShrinkCapacity, ctx
                    .getChannel().getConfig().getBufferFactory());
                c.writeBytes(cumulation);

                this.cumulation = c;
            }
        }
    }

    /**
     * 对指定字节格式消息内容进行解码, 转换为业务消息对象
     * 
     * @param ctx
     *            网络通道处理器上下文对象
     * @param channel
     *            底层网络通道
     * @param buffer
     *            字节消息
     * @return 解码后的业务消息对象(null=字节消息不足,需要等待更多数据)
     * @throws Exception
     */
    protected abstract Object decode(ChannelHandlerContext ctx,
        Channel channel, ChannelBuffer buffer) throws Exception;

    /**
     * 对业务消息对象进行编码, 转换为字节格式消息内容
     * 
     * @param ctx
     *            网络通道处理器上下文对象
     * @param channel
     *            底层网络通道
     * @param msg
     *            业务消息对象
     * @return 编码后的字节信息 (注意: 不能返回<b>null</b>, 可以返回
     *         {@link ChannelBuffers#EMPTY_BUFFER} 作为替代);
     * @throws Exception
     */
    protected abstract ChannelBuffer encode(ChannelHandlerContext ctx,
        Channel channel, Object msg) throws Exception;
}
