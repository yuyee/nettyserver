package com.git.original.server.netty;

import java.util.Map.Entry;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.git.original.common.logging.LoggerHelper;
import com.git.original.server.AdminCmdExecutor;
import com.git.original.server.AdminCmdExecutor.CommandEntry;
import com.git.original.server.annotation.AdminCmdDescription;

/**
 * 管理连接网络连接通道处理器
 */
public class NettyAdminChannelHandler extends IdleableNettyChannelHandler {

    /**
     * 所属服务实例
     */
    private final NettyServer parentServer;

    /**
     * 管理指令执行器
     */
    private final AdminCmdExecutor cmdExecutor;

    /**
     * 执行器已经被初始化过了
     */
    private boolean executorInited = false;

    /**
     * 标记是否需要关闭处理器
     */
    private volatile boolean stopped = false;

    /**
     * 日志输出对象
     */
    private Logger logger;

    /**
     * 默认构造函数
     * 
     * @throws Exception
     */
    public NettyAdminChannelHandler(NettyServer server) {
        this(server, null);
    }

    /**
     * 指定日志输出名称的构造函数
     * 
     * @param logName
     *            日志名称(null=默认日志名称, 即当前实例类名)
     * @throws Exception
     * @throws
     */
    public NettyAdminChannelHandler(NettyServer server, String logName) {
        super(server.getAllAdminChannels());

        this.parentServer = server;

        if (logName == null) {
            logger = LoggerFactory.getLogger(server.getClass().getPackage()
                .getName()
                + ".AdminIoHandler");
        } else {
            logger = LoggerFactory.getLogger(logName);
        }

        this.cmdExecutor = new AdminCmdExecutor(ChannelHandlerContext.class,
            logger);
    }

    /**
     * 关闭本处理器
     */
    public void stop() {
        this.stopped = true;
    }

    /**
     * @param handler
     */
    public final void registerCmdHandler(Object handler) {
        this.cmdExecutor.analysisCmdAnnotation(handler);
    }

    /**
     * 获取当前管理处理器名称
     * 
     * @return
     */
    protected String getWelcomeBanner() {
        return "Welcome to HMail-Admin: " + logger.getName() + "\r\n"
            + "use `help` for command hint";
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
        String banner = getWelcomeBanner();
        if (banner != null) {
            e.getChannel().write(banner);
        }

        // 解析标注
        synchronized (this) {
            if (!executorInited) {
                this.cmdExecutor.analysisCmdAnnotation(this);
                executorInited = true;
            }
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
        logger.info("admin connection idle. session_id={}", e.getChannel()
            .getId());
        e.getChannel().close();
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
        Channel channel = e.getChannel();
        Object message = e.getMessage();

        if (this.stopped) {
            channel.close();
            return;
        }

        logger.debug("admin command = \"{}\" session_id={}", message,
            channel.getId());

        if (message == null) {
            logger.trace("command is null, session_id={}", channel.getId());
            return;
        }

        if (!(message instanceof String)) {
            logger
                .trace(
                    "unknown command type, except is String, bug actual={}, session_id={}",
                    message.getClass(), channel.getId());
            return;
        }

        // 规范指令
        String msg = normalizeCommand((String) message);
        String result = cmdExecutor.invokeCommand(msg, ctx);
        // 返回操作结果描述
        if (result != null) {
            channel.write(result);
        }
    }

    /**
     * 正常化指令字符串, 自动去除指令中的不可见字符
     * 
     * @param message
     * @return
     */
    private static String normalizeCommand(String message) {
        if (message == null)
            return null;

        StringBuilder sb = null;
        int i = 0;
        for (; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c < 32 || c > 126) {
                sb = new StringBuilder();
                sb.append(message.substring(0, i++));
                break;
            }
        }

        if (sb == null)
            return message;

        for (; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c < 32 || c > 126) {
                continue;
            }

            sb.append(c);
        }

        return sb.toString().trim();
    }

    /**
     * 输出当前管理模块能支持的指令
     * 
     * @param argStr
     *            参数
     * @return 响应内容
     */
    @AdminCmdDescription(cmd = "help", usage = "help [command]")
    public String help(String argStr) {
        if (argStr == null || argStr.isEmpty()) {

            StringBuilder sb = new StringBuilder("available commands: \r\n");
            for (Entry<String, CommandEntry> entry: cmdExecutor.getCmd2Entry()
                .entrySet()) {
                CommandEntry cmdEntry = entry.getValue();
                if (!entry.getKey().equals(cmdEntry.getCommand())) {
                    // 略过别名的直接映射,别名将在主指令中合并输出
                    continue;
                }

                sb.append('\t').append(cmdEntry.getCommand());

                if (cmdEntry.getAliasSet() != null) {
                    for (String alias: cmdEntry.getAliasSet()) {
                        sb.append(" / ").append(alias);
                    }
                }

                sb.append("\r\n");
            }
            return sb.toString();
        } else {
            CommandEntry cmdEntry = cmdExecutor.getCmd2Entry().get(
                argStr.toLowerCase().trim());
            if (cmdEntry == null) {
                return "unknown command: [" + argStr + "]";
            }

            StringBuilder result = new StringBuilder();
            result.append('[').append(argStr).append(']');
            if (cmdEntry.getSummary() != null) {
                result.append(cmdEntry.getSummary());
            } else {
                result.append("\r\n");
            }

            if (cmdEntry.getUsage() != null) {
                result.append("usage:\r\n").append(cmdEntry.getUsage());
            }

            if (cmdEntry.getArgDesc() != null) {
                result.append("option:\r\n").append(cmdEntry.getArgDesc());
            }

            return result.toString();
        }
    }

    /**
     * 管理连接关闭
     * 
     * @param argStr
     *            参数(忽略)
     * @param ctx
     *            连接通道处理器上下文实例
     * @return 响应内容
     */
    @AdminCmdDescription(cmd = "quit", alias = "exit", usage = "quit from administrator console")
    protected String quit(String argStr, ChannelHandlerContext ctx) {
        NettyServer.closeAfterWriteCompleted(ctx.getChannel(), "goodbye");
        return null;
    }

    /**
     * 重新载入指定配置内容
     * 
     * @param argStr
     *            参数
     * @return 响应内容
     */
    @AdminCmdDescription(cmd = "reload", param = "log", usage = "reload log: reload the current logger config file.")
    public String reloadLog(String args) {
        String configUrl = LoggerHelper.reloadLogConfig(args);

        StringBuilder sb = new StringBuilder();
        return sb.append("reload logger[")
            .append(LoggerHelper.getSlf4jLoggerType())
            .append("] config-url:\"").append(configUrl).append('"').toString();
    }

    /**
     * 执行"reload config"指令
     * <p>
     * 默认未实现
     * 
     * @param arg
     * @return
     */
    @AdminCmdDescription(cmd = "reload", param = "config", usage = "reload config: reload the current service config file")
    public String reloadConfig(String arg) {
        if (arg != null && !arg.isEmpty()) {
            return "not implement RELOAD [config] " + arg;
        }

        try {
            parentServer.getConfig().getConfiguration().load();
            return "reload config sucess";
        } catch (Throwable th) {
            logger.warn("reload config failed", th);
        }

        return "reload config failed";
    }

    /**
     * 执行JVM相关指令
     * 
     * @param argStr
     * @return
     */
    @AdminCmdDescription(cmd = "jvm", usage = "jvm <mem | freemem | maxmem | gc | threadcount>")
    protected String doJvm(String argStr) {
        if (argStr == null) {
            return "not implement JVM [null]";
        }

        String[] args = argStr.split("\\s+", 2);

        StringBuilder sb = new StringBuilder();
        sb.append('[').append(args[0]).append("] ");

        if ("mem".equalsIgnoreCase(args[0])) {
            return sb.append("total jvm memory = ")
                .append(Runtime.getRuntime().totalMemory()).toString();
        } else if ("freemem".equalsIgnoreCase(args[0])) {
            return sb.append("free jvm memory = ")
                .append(Runtime.getRuntime().freeMemory()).toString();
        } else if ("maxmem".equalsIgnoreCase(args[0])) {
            return sb.append("max jvm memory = ")
                .append(Runtime.getRuntime().maxMemory()).toString();
        } else if ("gc".equalsIgnoreCase(args[0])) {
            System.gc();
            return sb.append("call jvm gc()").toString();
        } else if ("threadcount".equalsIgnoreCase(args[0])) {
            return sb.append("jvm all threads estimate count = ")
                .append(getAllThreadEstimateCount()).toString();
        } else {
            return sb.append("not implement JVM [").append(args[0]).append(']')
                .toString();
        }
    }

    /**
     * 获取当前JVM中的活跃线程大约总数
     * 
     * @return
     */
    public static int getAllThreadEstimateCount() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup topGroup = group;

        // 遍历线程组树，获取根线程组
        while (group != null) {
            topGroup = group;
            group = group.getParent();
        }

        return topGroup.activeCount();
    }

}
