package com.git.original.server.netty;

import java.util.EnumSet;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

/**
 * 扩展{@link ExecutionHandler}功能, 可以指定只对具体事件才会启用线程池处理
 */
public class UpstreamEventExecutionHandler extends ExecutionHandler {
    /**
     * 可使用线程池处理的事件类型枚举类
     */
    public enum UpstreamEventType {
        /** 消息接收 */
        MESSAGE_RECEIVED,
        /** 异常捕获 */
        EXCEPTION_CAUGHT,
        /** 通道空闲 */
        CHANNEL_IDLE,

        /** 通道已连接 */
        CHANNEL_CONNECTED,
        /** 通道已关闭 */
        CHANNEL_DISCONNECTED,

        /** 通道创建 */
        CHANNEL_OPENED,
        /** 通道销毁 */
        CHANNEL_CLOSED,
    }

    /**
     * 默认会使用线程池处理的事件类型
     */
    public static final UpstreamEventType[] DEFAULT_EVENT_TYPES = new UpstreamEventType[] {
        UpstreamEventType.MESSAGE_RECEIVED, UpstreamEventType.EXCEPTION_CAUGHT,
        UpstreamEventType.CHANNEL_IDLE, UpstreamEventType.CHANNEL_CONNECTED,
        UpstreamEventType.CHANNEL_DISCONNECTED };

    /**
     * 当前处理器允许启用线程池处理的事件类型集合
     */
    private final EnumSet<UpstreamEventType> eventTypeSet;

    /**
     * 构造函数
     * 
     * @param executor
     *            内部使用的线程池对象
     * @param eventTypes
     *            允许开启线程池处理的事件类型列表(null=所有事件都启用线程池;empty=使用默认列表
     *            {@link #DEFAULT_EVENT_TYPES})
     */
    public UpstreamEventExecutionHandler(Executor executor,
        UpstreamEventType... eventTypes) {
        super(executor);

        if (eventTypes == null) {
            this.eventTypeSet = null;
        } else if (eventTypes.length == 0) {
            this.eventTypeSet = EnumSet.of(DEFAULT_EVENT_TYPES[0],
                DEFAULT_EVENT_TYPES);
        } else {
            this.eventTypeSet = EnumSet.of(eventTypes[0], eventTypes);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jboss.netty.handler.execution.ExecutionHandler#handleUpstream(org
     * .jboss.netty.channel.ChannelHandlerContext,
     * org.jboss.netty.channel.ChannelEvent)
     */
    @Override
    public void handleUpstream(ChannelHandlerContext context, ChannelEvent e)
        throws Exception {

        if (eventTypeSet == null) { // 所有事件都进入线程池处理
            super.handleUpstream(context, e);
            return;
        }

        boolean matched = false;

        if (e instanceof MessageEvent) {
            matched = eventTypeSet.contains(UpstreamEventType.MESSAGE_RECEIVED);
        } else if (e instanceof IdleStateEvent) {
            matched = eventTypeSet.contains(UpstreamEventType.CHANNEL_IDLE);
        } else if (e instanceof ChannelStateEvent) {
            ChannelStateEvent evt = (ChannelStateEvent) e;
            switch (evt.getState()) {
                case OPEN:
                    if (Boolean.TRUE.equals(evt.getValue())) {
                        matched = eventTypeSet
                            .contains(UpstreamEventType.CHANNEL_OPENED);
                    } else {
                        matched = eventTypeSet
                            .contains(UpstreamEventType.CHANNEL_CLOSED);
                    }
                    break;
                case CONNECTED:
                    if (evt.getValue() != null) {
                        matched = eventTypeSet
                            .contains(UpstreamEventType.CHANNEL_CONNECTED);
                    } else {
                        matched = eventTypeSet
                            .contains(UpstreamEventType.CHANNEL_DISCONNECTED);
                    }
                    break;
            }
        } else if (e instanceof ExceptionEvent) {
            matched = eventTypeSet.contains(UpstreamEventType.EXCEPTION_CAUGHT);
        }

        if (matched) {
            // 启用线程池
            super.handleUpstream(context, e);
        } else {
            // 不使用线程池
            context.sendUpstream(e);
        }

    }

}
