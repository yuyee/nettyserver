package com.git.original.server.netty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.git.original.common.config.BaseConfigDocument;
import com.git.original.common.config.ConfigNode;
import com.git.original.common.config.ConfigUpdateWatcher;
import com.git.original.common.config.Configuration;
import com.git.original.common.config.RefreshHelper;
import com.git.original.common.utils.BackgroundHelper;
import com.git.original.common.utils.ByteUtils;
import com.git.original.common.utils.IPUtils;
import com.git.original.server.IService;
import com.git.original.server.ISessionHandler;
import com.git.original.server.ServerConfig;
import com.git.original.server.netty.TextLineMessageCodecHandler.OverLengthPolicy;

/**
 * 基于Netty框架的服务器端抽象类
 * 
 * @author linaoxiang
 */
public abstract class NettyServer implements IService, ConfigUpdateWatcher {
	/**
	 * Netty框架服务器端支持的IO类型枚举
	 * 
	 * @author linaoxiang
	 */
	public enum NettyIoType {
		/** JAVA默认的阻塞式网络IO */
		BLOCK_IO,
		/** JAVA非阻塞式网络IO */
		NON_BLOCK_IO,
	}

	/**
	 * Netty处理器名称: 编解码过滤层
	 */
	public static final String NETTY_HANDLER_NAME_CODEC_FILTER = "codecFilter";

	/**
	 * Netty处理器名称: 线程池过滤层
	 */
	public static final String NETTY_HANDLER_NAME_THREAD_POOL_FILTER = "threadpoolFilter";

	/**
	 * Netty处理器名称: SSL端口检测过滤层
	 */
	public static final String NETTY_HANDLER_NAME_DETECT_SSL_PORT_FILTER = "detectSslPortFilter";

	/**
	 * Netty处理器名称: SSL过滤层
	 */
	public static final String NETTY_HANDLER_NAME_SSL_FILTER = "sslFilter";

	/**
	 * Netty处理器名称: 空闲状态触发过滤层
	 */
	public static final String NETTY_HANDLER_NAME_IDLE_TRIGGER_FILTER = "IdleStateTriggerFilter";

	/**
	 * Netty处理器名称: 业务逻辑处理器
	 */
	public static final String NETTY_HANDLER_NAME_BUSINESS_HANDLER = "businessHandler";

	/**
	 * SSL的服务器侦听Channel对象集合
	 */
	private final Set<Channel> sslServerChannelSet;

	/**
	 * 服务器默认网络IO类型
	 */
	private NettyIoType defaultIoType;

	/**
	 * 网络IO类型 --> ServerBootstrap的映射关系
	 */
	private final Map<NettyIoType, ServerBootstrap> ioType2Bootstrap = new HashMap<NettyServer.NettyIoType, ServerBootstrap>();

	/**
	 * true=只允许开启管理端口(即该服务器端不提供对外服务);false=允许开启服务端口
	 */
	private final boolean onlyAdminAcceptor;

	/**
	 * 管理连接接收器
	 */
	protected ServerBootstrap adminBootstrap;

	/**
	 * 网络对外服务通道组
	 */
	private ChannelGroup allServiceChannels;

	/**
	 * 网络管理通道组
	 */
	private ChannelGroup allAdminChannels;

	/**
	 * 事件调用处理的线程池
	 */
	protected ThreadPoolExecutor serverExeutor;

	/**
	 * SSL环境对象
	 */
	private SSLContext sslContext;

	/**
	 * 本服务器内网IP地址
	 */
	private int localIp = 0x7F000001;

	/**
	 * 本服务使用的服务配置文档结点
	 */
	private Configuration serverConfigDoc = null;

	/**
	 * 服务执行状态
	 * <p>
	 * <li>0 = 初始化
	 * <li>1 = 完成启动准备
	 * <li>2 = 正在执行
	 * <li>10 = 服务被关闭
	 */
	private AtomicInteger status = new AtomicInteger(SERVER_STATUS_NEW);

	/** 服务初始化 */
	private static final int SERVER_STATUS_NEW = 0;

	/** 服务完成启动准备 */
	private static final int SERVER_STATUS_PREPARED = 1;

	/** 服务正在执行 */
	private static final int SERVER_STATUS_RUNNING = 2;

	/** 服务被关闭 */
	private static final int SERVER_STATUS_CLOSED = 10;

	/**
	 * JVM全局的Netty框架IO线程池
	 */
	private static ExecutorService globalThreadPool;

	/**
	 * JVM全局的Netty框架NIO工厂
	 */
	private static ChannelFactory globalNioChannelFactory;

	/**
	 * 用于触发Idle事件的定时器
	 */
	protected static final HmailHashedWheelTimer idleTimer = new HmailHashedWheelTimer();

	/** An id generator guaranteed to generate unique IDs for the session */
	static AtomicLong channelIdGenerator = new AtomicLong(0);

	/** 日志记录 */
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 指定Netty使用slf4j作为默认的logger
	 */
	static {
		org.jboss.netty.logging.InternalLoggerFactory
				.setDefaultFactory(new org.jboss.netty.logging.Slf4JLoggerFactory());
	}

	/**
	 * 默认构造函数
	 */
	public NettyServer() {
		this(false);
	}

	/**
	 * 可以指定是否生成客户端连接BootStrap的构造函数
	 * 
	 * @param onlyAdminAcceptor
	 *            true=不生成客户端连接BootStrap; false=生成客户端连接BootStrap(默认方式)
	 */
	protected NettyServer(boolean onlyAdminAcceptor) {
		this(onlyAdminAcceptor, NettyIoType.NON_BLOCK_IO);
	}

	/**
	 * 可以指定是否生成客户端连接BootStrap的构造函数
	 * <p>
	 * 用于兼容老接口
	 * 
	 * @param onlyAdminAcceptor
	 *            true=不生成客户端连接BootStrap; false=生成客户端连接BootStrap(默认)
	 * @param useOldIo
	 *            true=使用Blocking方式的对外服务网络通道; false=使用NonBlocking方式的对外服务网络通道(默认)
	 */
	protected NettyServer(boolean onlyAdminAcceptor, boolean useOldIo) {
		this.onlyAdminAcceptor = onlyAdminAcceptor;

		if (useOldIo) {
			this.defaultIoType = NettyIoType.BLOCK_IO;
		} else {
			this.defaultIoType = NettyIoType.NON_BLOCK_IO;
		}

		this.sslServerChannelSet = new HashSet<Channel>();
	}

	/**
	 * 可以指定是否生成客户端连接Acceptor的构造函数
	 * 
	 * @param onlyAdminAcceptor
	 *            true=不生成客户端连接BootStrap; false=生成客户端连接BootStrap(默认)
	 * @param ioType
	 *            默认网络IO类型
	 */
	protected NettyServer(boolean onlyAdminAcceptor, NettyIoType ioType) {
		this.onlyAdminAcceptor = onlyAdminAcceptor;

		if (ioType == null) {
			ioType = NettyIoType.NON_BLOCK_IO;
		}
		this.defaultIoType = ioType;

		this.sslServerChannelSet = new HashSet<Channel>();
	}

	/**
	 * 获取JVM全局的Netty框架IO线程池(如果尚不存在,则自动创建)
	 * 
	 * @return
	 */
	private static synchronized ExecutorService getGlobalThreadPool() {
		if (NettyServer.globalThreadPool == null) {
			ThreadFactory factory = new BackgroundHelper.DefaultNamedThreadFactory(
					"netty-global-io-daemon-threads", false);
			NettyServer.globalThreadPool = Executors
					.newCachedThreadPool(factory);
		}

		return NettyServer.globalThreadPool;
	}

	/**
	 * 获取JVM全局的Netty框架NIO工厂(如果尚不存在,则自动创建)
	 * 
	 * @return
	 */
	protected final static synchronized ChannelFactory getGlobalNioChannelFactory() {
		if (NettyServer.globalNioChannelFactory == null) {
			// Default to number of cores * 2
			int threadsToUse = Runtime.getRuntime().availableProcessors() * 2;

			NettyServer.globalNioChannelFactory = new NioServerSocketChannelFactory(
					NettyServer.getGlobalThreadPool(),
					NettyServer.getGlobalThreadPool(), threadsToUse);
		}

		return NettyServer.globalNioChannelFactory;
	}

	@Override
	public boolean isRunning() {
		return (this.status.get() == SERVER_STATUS_RUNNING);
	}

	@Override
	public boolean isClosed() {
		return (this.status.get() == SERVER_STATUS_CLOSED);
	}

	/**
	 * 获取当前服务端所在的服务器本地IP地址值
	 * 
	 * @return
	 */
	public int getLocalIp() {
		return localIp;
	}

	/**
	 * @return the allServiceChannels
	 */
	public ChannelGroup getAllServiceChannels() {
		return allServiceChannels;
	}

	/**
	 * @return the allAdminChannels
	 */
	public ChannelGroup getAllAdminChannels() {
		return allAdminChannels;
	}

	/**
	 * 初始化服务侦听实例
	 * 
	 * @param ioType
	 *            服务IO类型
	 * @throws Exception
	 */
	private ServerBootstrap prepareServiceBootStrap(NettyIoType ioType)
			throws Exception {
		if (ioType == null) {
			throw new NullPointerException("ioType is null");
		}

		if (this.allServiceChannels == null) {
			this.allServiceChannels = new DefaultChannelGroup(this.getClass()
					.getSimpleName() + ".service-group");
		}

		ServerBootstrap bootstrap = this.ioType2Bootstrap.get(ioType);
		if (bootstrap != null)
			return bootstrap; // 已经初始化了

		switch (ioType) {
		case BLOCK_IO:
			bootstrap = new ServerBootstrap(new OioServerSocketChannelFactory(
					NettyServer.getGlobalThreadPool(),
					NettyServer.getGlobalThreadPool()));

			// Options for a parent channel
			bootstrap.setOption("reuseAddress", true);
			bootstrap.setOption("child.trafficClass", 0x10);
			break;

		default:
			bootstrap = new ServerBootstrap(getGlobalNioChannelFactory());

			// Options for a parent channel
			bootstrap.setOption("reuseAddress", true);
			bootstrap.setOption("child.trafficClass", 0x10);
			break;
		}

		// Options for its children
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.receiveBufferSize", 32 * 1024);
		bootstrap.setOption("child.sendBufferSize", 32 * 1024);

		bootstrap.setPipelineFactory(createPipelineFactory(this.getConfig()));

		// bootstrap 不为null
		this.ioType2Bootstrap.put(ioType, bootstrap);

		return bootstrap;
	}

	@Override
	public void prepare() throws Exception {
		if (this.status.get() >= SERVER_STATUS_PREPARED) {
			throw new IllegalStateException("forbid prepare server");
		}

		ServerConfig conf = this.getConfig();

		// 设置服务器端当前默认时区
		TimeZone.setDefault(TimeZone.getTimeZone(conf.getServerTimeZone()));
		logger.info("configuration <{}>={}", ServerConfig.CONF_SERVER_TIMEZONE,
				conf.getServerTimeZone());

		// 解析配置文件中用户指定的IO类型
		String ioTypeStr = conf.getServerNetworkIoType();
		if (ioTypeStr != null) {
			if (ioTypeStr.equalsIgnoreCase(ServerConfig.IO_TYPE_OIO)) {
				this.defaultIoType = NettyIoType.BLOCK_IO;
			} else if (ioTypeStr.equalsIgnoreCase(ServerConfig.IO_TYPE_NIO)) {
				this.defaultIoType = NettyIoType.NON_BLOCK_IO;
			}
		}

		// 生成默认的服务器执行线程池
		this.serverExeutor = createExecutor();

		/*
		 * 创建SSL上下文
		 */
		this.sslContext = this.createSslContext(conf.getSslCertNode());

		if (!this.onlyAdminAcceptor) {
			this.ioType2Bootstrap.clear();
			this.allServiceChannels = null;
		}

		// 获取本机内网IP
		this.localIp = IPUtils.getLocalIp();

		this.status.set(SERVER_STATUS_PREPARED);
	}

	/**
	 * 创建服务管道工厂实例
	 * 
	 * @param conf
	 *            配置文档实例
	 * @return 工厂实例
	 * @throws Exception
	 */
	protected ChannelPipelineFactory createPipelineFactory(ServerConfig conf)
			throws Exception {

		// ssl端口探测Filter
		final DetectSslChannelHandler detectFilter = new DetectSslChannelHandler(
				NettyServer.this);
		// 业务处理线程池Filter
		final ChannelHandler executorFilter = this
				.createExecutorFilter(serverExeutor);
		// 业务处理器
		final IdleableNettyChannelHandler businessHandler = createIoHandler();

		return new ConfigableChannelPipelineFactory(conf) {
			public ChannelPipeline getPipeline() {
				ChannelPipeline pipeline = Channels.pipeline();

				pipeline.addLast(
						NETTY_HANDLER_NAME_IDLE_TRIGGER_FILTER,
						new IdleStateHandler(idleTimer, 0, 0, this.config
								.getServerIdleTimeout()));

				pipeline.addLast(NETTY_HANDLER_NAME_DETECT_SSL_PORT_FILTER,
						detectFilter);

				// 处理字节流的Filter(在Netty的IO主线程中执行, 注意执行效率)
				CodecStreamHandler codecFilter = createCodecFilter();
				if (codecFilter != null) {
					pipeline.addLast(NETTY_HANDLER_NAME_CODEC_FILTER,
							codecFilter);
				}

				if (executorFilter != null) {
					pipeline.addLast(NETTY_HANDLER_NAME_THREAD_POOL_FILTER,
							executorFilter);
				}

				pipeline.addLast(NETTY_HANDLER_NAME_BUSINESS_HANDLER,
						businessHandler);

				return pipeline;
			}
		};
	}

	@Override
	public final synchronized void start() throws Exception {
		if (this.status.get() >= SERVER_STATUS_RUNNING) {
			// 服务器已经被启动
			return;
		}

		if (this.status.get() == SERVER_STATUS_NEW) {
			throw new IllegalStateException("call server prepare first");
		}

		logger.info("Server begins to run.");

		doStart();

		logger.info("Server is running.");

		// 设置启动标记
		this.status.set(SERVER_STATUS_RUNNING);
	}

	@Override
	public final synchronized void shutdown() {
		if (this.isClosed()) {
			// 已经被关闭
			return;
		}

		logger.info("Server is Closing.");
		doShutdown();
		logger.info("Server is Closed.");

		// 标记已经被关闭
		this.status.set(SERVER_STATUS_CLOSED);
	}

	/**
	 * 解析需要侦听的地址
	 * <p>
	 * 格式: ip:port 或 file_path(仅对unix socket类型IO有效)<br>
	 * 特殊格式 *:port 表示侦听在所有IP上
	 * 
	 * @param addrStr
	 *            地址字符串
	 * @return 侦听地址
	 * @throws IOException
	 */
	private InetSocketAddress parseBindAddressString(NettyIoType ioType,
			String addrStr) throws IOException {
		if (addrStr == null || addrStr.trim().isEmpty())
			return null;
		if (ioType == null) {
			ioType = this.defaultIoType;
		}

		InetAddress netAddr = null;
		int port;

		int pos = addrStr.lastIndexOf(':');
		if (pos < 0) {
			port = Integer.parseInt(addrStr.trim());
		} else {
			String hostName = addrStr.substring(0, pos).trim();
			if (hostName.isEmpty() || hostName.equals("*")) {
				// 侦听在全部地址
				netAddr = null;
			} else if (hostName.equalsIgnoreCase("internal")) {
				// 只侦听在本地IP
				netAddr = InetAddress.getByAddress(ByteUtils.getAsBytes(this
						.getLocalIp()));
			} else {
				// 侦听在指定IP
				netAddr = InetAddress.getByName(hostName);
			}

			port = Integer.parseInt(addrStr.substring(pos + 1).trim());
		}

		return new InetSocketAddress(netAddr, port);
	}

	/**
	 * 服务器具体的启动操作
	 * 
	 * @throws Exception
	 */
	protected void doStart() throws Exception {
		final ServerConfig conf = this.getConfig();

		if (this.ioType2Bootstrap != null) {

			if (!this.ioType2Bootstrap.isEmpty()) {
				throw new IOException("oops has already bind address?");
			}

			/*
			 * 打开侦听普通端口
			 */
			List<ConfigNode> addrNodes = conf.getServerBindAddressNodes();
			if (addrNodes == null || addrNodes.size() == 0) {
				logger.warn("not specify any bind address");
			} else {
				for (ConfigNode cn : addrNodes) {
					Channel channel = doBindAddress(cn);
					if (channel != null) {
						logger.info("Listened on: {}[actual={}]",
								cn.toString(), channel.getLocalAddress());
					}
				}
			}

			/*
			 * 打开侦听SSL端口
			 */
			sslServerChannelSet.clear();
			List<ConfigNode> sslAddrNodes = conf.getServerBindSslAddressNodes();
			if (sslAddrNodes != null) {
				for (ConfigNode cn : sslAddrNodes) {
					Channel channel = doBindAddress(cn);
					if (channel != null) {
						sslServerChannelSet.add(channel);
						logger.info("SSL Listened on: {}[actual={}]",
								cn.toString(), channel.getLocalAddress());
					}
				}
			}
		}

		/*
		 * 打开管理端口
		 */
		int adminPort = conf.getAdminPort();
		if (adminPort != -1) {
			adminBootstrap = new ServerBootstrap(getGlobalNioChannelFactory());

			// Options for a parent channel
			adminBootstrap.setOption("reuseAddress", true);
			// Options for its children
			adminBootstrap.setOption("child.tcpNoDelay", true);

			this.allAdminChannels = new DefaultChannelGroup(this.getClass()
					.getSimpleName() + ".admin-group");

			final ChannelHandler adminHandler = createAdminIoHandler();

			adminBootstrap
					.setPipelineFactory(new ConfigableChannelPipelineFactory(
							conf) {
						public ChannelPipeline getPipeline() {
							ChannelPipeline pipeline = Channels.pipeline();

							pipeline.addLast(
									NETTY_HANDLER_NAME_IDLE_TRIGGER_FILTER,
									new IdleStateHandler(idleTimer, 0, 0, 300));

							final ChannelHandler codecHandler = new TextLineMessageCodecHandler(
									conf.getAdminCommandLengthLimit(),
									CharsetUtil.UTF_8, "\r\n",
									OverLengthPolicy.FORCE_SPLIT, false);
							pipeline.addLast(NETTY_HANDLER_NAME_CODEC_FILTER,
									codecHandler);

							pipeline.addLast(
									NETTY_HANDLER_NAME_BUSINESS_HANDLER,
									adminHandler);

							return pipeline;
						}
					});

			Channel channel = adminBootstrap.bind(new InetSocketAddress(
					"127.0.0.1", adminPort));
			this.allAdminChannels.add(channel);
			logger.info("Admin Listened on: 127.0.0.1:" + adminPort);
		}

		// 打开配置变更监控
		this.serverConfigDoc = conf.getConfiguration();
		RefreshHelper.registerConfig(this.serverConfigDoc);
	}

	/**
	 * 绑定侦听地址
	 * 
	 * @param addrNode
	 *            侦听地址配置节点
	 * @return 成功侦听的网络通道对象
	 * @throws Exception
	 */
	protected Channel doBindAddress(ConfigNode addrNode) throws Exception {
		if (addrNode == null)
			return null;

		String ioTypeStr = addrNode.getAttribute(
				ServerConfig.BIND_ADDRESS_ATTRIBUTE_IO_TYPE, null);
		NettyIoType ioType;
		if (ServerConfig.IO_TYPE_OIO.equalsIgnoreCase(ioTypeStr)) {
			ioType = NettyIoType.BLOCK_IO;
		} else if (ServerConfig.IO_TYPE_NIO.equalsIgnoreCase(ioTypeStr)) {
			ioType = NettyIoType.NON_BLOCK_IO;
		} else {
			ioType = this.defaultIoType;
		}

		String addrStr = (String) addrNode.getValue();
		InetSocketAddress addr = parseBindAddressString(ioType, addrStr);
		if (addr == null)
			return null;

		ServerBootstrap bootstrap = prepareServiceBootStrap(ioType);
		if (bootstrap == null) {
			throw new IOException("create bootstrap failed. net_io_type="
					+ ioType);
		}

		Channel channel = bootstrap.bind(addr);
		this.allServiceChannels.add(channel);
		return channel;
	}

	/**
	 * 服务器具体的关闭操作
	 */
	protected void doShutdown() {
		if (this.serverConfigDoc != null) {
			this.serverConfigDoc.clearWatcher();
			RefreshHelper.unregisterConfig(this.serverConfigDoc);
		}

		// 首先关闭Channel服务器监听实例, 阻止新的连接或者消息产生
		if (this.allServiceChannels != null) {
			try {
				ChannelGroupFuture future = this.allServiceChannels.close();
				future.awaitUninterruptibly(10 * 1000);
			} catch (Throwable th) {
			}

			// 关闭所有IO类型的bootstrap
			for (ServerBootstrap bootstrap : this.ioType2Bootstrap.values()) {
				try {
					bootstrap.releaseExternalResources();
				} catch (Throwable th) {
				}
			}

			this.ioType2Bootstrap.clear();
		}

		// 关闭管理端口监听实例
		if (this.allAdminChannels != null) {
			try {
				ChannelGroupFuture future = this.allAdminChannels.close();
				future.awaitUninterruptibly(5 * 1000);
			} catch (Throwable th) {
			}

			try {
				this.adminBootstrap.releaseExternalResources();
			} catch (Throwable th) {
			}
		}

		/*
		 * 等待线程, 线程池正常关闭
		 */
		try {
			// 首先发送线程池关闭信号
			if (this.serverExeutor != null) {
				this.serverExeutor.shutdownNow();
				this.serverExeutor.awaitTermination(30, TimeUnit.SECONDS);
			}
		} catch (Exception ex) {
			// 忽略异常
		}
	}

	/**
	 * 检查网络通道的本地端口, 如果是指定的SSL端口, 则自动启用SSL加密
	 * 
	 * @param channel
	 *            网络通道
	 */
	public final void checkAndTriggerSsl(Channel channel) {
		if (channel == null || channel.getParent() == null) {
			return;
		}

		SocketAddress localAddr = channel.getLocalAddress();
		if (localAddr == null) {
			return;
		}

		if (!(localAddr instanceof InetSocketAddress)) {
			return;
		}

		if (this.sslServerChannelSet.contains(channel.getParent())) {
			// 需要打开SSL过滤层
			SSLEngine engine = sslContext.createSSLEngine();
			engine.setUseClientMode(false);

			SslHandler sslHandler = new SslHandler(engine, false);
			channel.getPipeline().addAfter(
					NETTY_HANDLER_NAME_DETECT_SSL_PORT_FILTER,
					NETTY_HANDLER_NAME_SSL_FILTER, sslHandler);
			logger.debug("SSL start. channel_id={}", channel.getId());
		}
	}

	/**
	 * 为指定网络会话开启TLS
	 * 
	 * @param channel
	 *            网络通道
	 * @param disableEncryptOnce
	 *            true=下一次数据发送请求忽略SSL加密;false=随后的所有发送请求都将进行SSL加密
	 */
	public final void startTLS(Channel channel, boolean disableEncryptOnce) {
		if (this.ioType2Bootstrap == null || this.ioType2Bootstrap.isEmpty()) {
			logger.warn("please create client acceptor before call startTLS");
			return;
		}

		// 打开SSL过滤层
		ChannelHandler handler = channel.getPipeline().get(
				NETTY_HANDLER_NAME_SSL_FILTER);
		if (handler == null) {
			// 需要打开SSL过滤层
			SSLEngine engine = sslContext.createSSLEngine();
			engine.setUseClientMode(false);

			channel.getPipeline().addAfter(
					NETTY_HANDLER_NAME_DETECT_SSL_PORT_FILTER,
					NETTY_HANDLER_NAME_SSL_FILTER,
					new SslHandler(engine, disableEncryptOnce));
		} else {
			// 已经开启了SSL, ignore
		}
	}

	/**
	 * 当前通道是否已经开启了SSL
	 * 
	 * @param channel
	 * @return
	 */
	public final boolean isSslTriggered(Channel channel) {
		if (channel == null || channel.getPipeline() == null) {
			return false;
		}

		ChannelHandler handler = channel.getPipeline().get(
				NETTY_HANDLER_NAME_SSL_FILTER);
		return (handler != null);
	}

	/**
	 * 创建默认线程池
	 * <p>
	 * OrderedMemoryAwareThreadPoolExecutor
	 * 
	 * @return
	 * @throws Exception
	 */
	protected ThreadPoolExecutor createExecutor() throws Exception {
		int threadCount = this.getConfig().getServerHandleThreadCount();
		int corePoolSize = 1;
		if (threadCount > 1) {
			corePoolSize = threadCount / 2;
		}

		ThreadPoolExecutor executor = new OrderedMemoryAwareThreadPoolExecutor(
				corePoolSize, 0, 0);
		executor.setMaximumPoolSize(threadCount);

		return executor;
	}

	protected void updateExecutor(ServerConfig config) {

		int threadCount = config.getServerHandleThreadCount();
		int corePoolSize = 1;
		if (threadCount > 1) {
			corePoolSize = threadCount / 2;
		}

		serverExeutor.setCorePoolSize(corePoolSize);
		serverExeutor.setMaximumPoolSize(threadCount);

	}

	/**
	 * 用于生成SSL上下文
	 * 
	 * @param sslCertNode
	 *            SSL证书配置节点
	 * @return 服务器端SSL上下文
	 * @throws Exception
	 */
	protected SSLContext createSslContext(ConfigNode sslCertNode)
			throws Exception {
		if (sslCertNode == null || !sslCertNode.hasChildren()) {
			// 使用默认的SSL证书配置
			sslCertNode = ServerConfig.defaultSslCertConfigNode;
		}

		// Create keystore
		KeyStore ks = KeyStore.getInstance(sslCertNode
				.getString(ServerConfig.SSL_CERT_NODE_KEYSTORE_TYPE));
		String ksPwd = sslCertNode
				.getString(ServerConfig.SSL_CERT_NODE_KEYSTORE_PASSWORD);

		URI ksUri = new URI(
				sslCertNode.getString(ServerConfig.SSL_CERT_NODE_KEYSTORE_PATH));
		if (ksUri.getScheme() == null) { // 认为是文件
			File ksFile = new File(ksUri.getPath());
			if (!ksFile.isAbsolute()) {
				ksFile = new File(System.getProperty(
						BaseConfigDocument.PROPERTY_GIT_HOME,
						BaseConfigDocument.DEFAULT_GIT_HOME), ksUri.getPath());
			}
			ksUri = ksFile.toURI();
		}

		InputStream ksStream = ksUri.toURL().openStream();
		try {
			ks.load(ksStream, ksPwd.toCharArray());
		} finally {
			ksStream.close();
		}

		// 指定SSL使用X.509凭证制度
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		KeyManager[] keyManagers = null;
		if (kmf != null) {
			kmf.init(ks, (ksPwd == null ? null : ksPwd.toCharArray()));
			keyManagers = kmf.getKeyManagers();
		}

		TrustManagerFactory tmf = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		TrustManager[] trustManagers = null;
		if (tmf != null) {
			tmf.init((KeyStore) null);
			trustManagers = tmf.getTrustManagers();
		}

		SSLContext context = SSLContext.getInstance("TLS");
		context.init(keyManagers, trustManagers, null);

		return context;
	}

	/**
	 * 创建一个业务通道的网络IO处理器实例
	 * 
	 * @return
	 */
	protected IdleableNettyChannelHandler createIoHandler() {
		return new NettyChannelHandler(this);
	}

	/**
	 * 创建一个管理通道的网络IO处理器实例
	 * 
	 * @return
	 */
	protected ChannelHandler createAdminIoHandler() {
		return new NettyAdminChannelHandler(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.netease.hmail.common.config.ConfigUpdateWatcher#notify(long)
	 */
	@Override
	public void notify(ConfigNode node, long version) {
		notifyConfigChanged(node, version);
	}

	/**
	 * 配置文档内容变更通知
	 * 
	 * @param node
	 *            新配置根节点
	 * @param version
	 *            新配置文档版本
	 */
	protected void notifyConfigChanged(ConfigNode node, long version) {
		ServerConfig config = null;
		try {
			config = this.getConfig();
		} catch (Exception ex) {
			logger.warn("get server config failed:", ex);
			config = null;
		}

		if (config != null) {
			config.loadFromConfigNode();
			updateExecutor(config);
		}

	}

	/**
	 * 获取服务器端配置实例
	 * 
	 * @return 服务器端配置对象
	 * @throws Exception
	 */
	public abstract ServerConfig getConfig() throws Exception;

	/**
	 * 创建业务逻辑处理的线程池过滤层
	 * 
	 * @param executor
	 *            线程池对象
	 * @return 过滤层实例
	 */
	protected abstract ExecutionHandler createExecutorFilter(Executor executor);

	/**
	 * 为指定的Netty网络通道创建一个处理器, 用于处理该会话请求
	 * 
	 * @param channelUid
	 *            长整型的通道ID
	 * @param channel
	 *            Netty网络通道(connected)
	 * @return 该通道对应的请求处理器
	 */
	protected abstract ISessionHandler createHandler(long channelUid,
			Channel channel);

	/**
	 * 创建供当前服务器使用的网络消息编解码器工厂
	 * 
	 * @return 工厂实例 (null=不需要使用编解码层)
	 */
	protected abstract CodecStreamHandler createCodecFilter();

	/**
	 * 在发送完指定消息后关闭网络通道
	 * 
	 * @param channel
	 *            网络通道
	 * @param message
	 *            需要发送的最后一份消息(null=直接关闭通道)
	 */
	public static final void closeAfterWriteCompleted(final Channel channel,
			Object message) {
		if (message == null) {
			channel.close();
			return;
		}

		ChannelFuture future = channel.write(message);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 释放一些全局的netty相关资源
	 * <p>
	 * 注意: 除非能确定需要推出JVM, 否则不建议调用本方法
	 */
	public static final synchronized void releaseGlobalResource() {
		if (idleTimer != null) {
			try {
				idleTimer.explicitStop();
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}

		if (globalNioChannelFactory != null) {
			try {
				globalNioChannelFactory.releaseExternalResources();
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}

		if (globalThreadPool != null) {
			try {
				globalThreadPool.shutdownNow();
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	// ----------------------------------------------------------------

	/**
	 * @author linaoxiang
	 */
	private static class HmailHashedWheelTimer extends HashedWheelTimer {
		/**
		 * 默认构造函数
		 */
		public HmailHashedWheelTimer() {
			super(new BackgroundHelper.DefaultDaemonThreadFactory(
					"netty-timer-global-daemon-threads"), 200,
					TimeUnit.MILLISECONDS);
		}

		/**
		 * 调用本方法无效
		 */
		@Override
		public Set<Timeout> stop() {
			// 不允许被默认stop()方法停止
			return null;
		}

		public synchronized Set<Timeout> explicitStop() {
			return super.stop();
		}
	}

}
