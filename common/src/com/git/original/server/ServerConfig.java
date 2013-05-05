package com.git.original.server;

import java.nio.charset.Charset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.git.original.common.config.ConfigNode;
import com.git.original.common.config.Configuration;

/**
 * 服务器端配置抽象类
 */
public abstract class ServerConfig {
    /**
     * 服务器配置根节点名称: server
     */
    public static final String CONF_SERVER_NODE = "server";

    /**
     * 配置路径: 连接空闲超时
     */
    public static final String CONF_SERVER_IDLE_TIMEOUT = "server.idle-timeout";

    /**
     * 配置路径: 连接写超时
     */
    public static final String CONF_SERVER_WRITE_TIMEOUT = "server.write-timeout";

    /**
     * 配置路径: 网络IO类型
     */
    public static final String CONF_SERVER_NETWORK_IO_TYPE = "server.network-io-type";

    /** 服务网络IO类型: 阻塞式网络IO */
    public static final String IO_TYPE_OIO = "oio";

    /** 服务网络IO类型: 非阻塞式网络IO */
    public static final String IO_TYPE_NIO = "nio";

    /**
     * 配置路径: 普通连接绑定地址
     */
    public static final String CONF_SERVER_BIND_ADDRESS = "server.address-list.address";

    /**
     * 配置路径: SSL连接绑定地址
     */
    public static final String CONF_SERVER_BIND_SSL_ADDRESS = "server.ssl-address-list.address";

    /**
     * 侦听地址属性值: 网络IO类型
     * 
     * @see #IO_TYPE_OIO
     * @see #IO_TYPE_NIO
     */
    public static final String BIND_ADDRESS_ATTRIBUTE_IO_TYPE = "type";

    /**
     * 服务器管理端口配置参数节点路径
     */
    public static final String CONF_SERVER_ADMIN_PORT = "server.admin-port";

    /**
     * 服务器接收的指令行最大长度(字节)配置参数节点路径
     */
    public static final String CONF_SERVER_LINE_LENGTH_LIMIT = "server.line-length-limit";

    /**
     * 服务器管理指令最大长度(字节)配置参数节点路径
     */
    public static final String CONF_SERVER_ADMIN_COMMAND_LENGTH_LIMIT = "server.admin-command-length-limit";

    /**
     * 配置路径: 默认字符集
     */
    public static final String CONF_SERVER_CHARSET = "server.charset";

    /**
     * 配置路径: 默认时区
     */
    public static final String CONF_SERVER_TIMEZONE = "server.timezone";

    /**
     * 配置路径: 业务处理线程池容量上限
     */
    public static final String CONF_SERVER_HANDLE_THREAD_COUNT = "server.handle-thread-count";

    /**
     * 配置路径: SSL证书配置
     */
    public static final String CONF_SERVER_SSL_CERT = "server.ssl-cert";

    /**
     * 配置节点: SSL证书KeyStore格式
     * <p>
     * 支持PKCS12 和 JKS 格式
     */
    public static final String SSL_CERT_NODE_KEYSTORE_TYPE = "keystore-type";

    /**
     * 配置节点: SSL证书KeyStore文件路径
     */
    public static final String SSL_CERT_NODE_KEYSTORE_PATH = "keystore-path";

    /**
     * 配置节点: SSL证书KeyStore保护密码
     */
    public static final String SSL_CERT_NODE_KEYSTORE_PASSWORD = "keystore-password";

    /** 配置项: 认证插件模块节点 */
    public static final String CONF_SERVER_AUTH_PLUGIN = "server.auth-plugin";

    /**
     * 认证插件模块配置子节点: 类名(包括完整的包路径)
     */
    public static final String AUTH_PLUGIN_CHILD_CLASSNAME = "class-name";

    // ----------------------------------------

    /**
     * 默认的SSL证书配置节点
     */
    public static final ConfigNode defaultSslCertConfigNode;
    static {
        defaultSslCertConfigNode = new ConfigNode(CONF_SERVER_SSL_CERT, null);
        defaultSslCertConfigNode.addChild(new ConfigNode(
            SSL_CERT_NODE_KEYSTORE_TYPE, "PKCS12"));
        defaultSslCertConfigNode.addChild(new ConfigNode(
            SSL_CERT_NODE_KEYSTORE_PATH, ServerConfig.class.getResource(
                "/server.p12").toString()));
        defaultSslCertConfigNode.addChild(new ConfigNode(
            SSL_CERT_NODE_KEYSTORE_PASSWORD, "12345"));
    }

    /**
     * 默认指令行消息最长字节数: 2048
     */
    public static final int DEFAULT_LINE_LENGTH_LIMIT = 2048;

    /**
     * 默认管理指令消息最长字节数: 1024
     */
    public static final int DEFAULT_ADMIN_COMMAND_LENGTH_LIMIT = 1024;

    // ----------------------------------------
    /**
     * 配置文档类
     */
    protected final Configuration config;

    /**
     * 日志记录对象
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 构建实例
     * 
     * @throws Exception
     */
    public ServerConfig(Configuration config) {
        this.config = config;
    }

    /**
     * 获取配置根节点
     * 
     * @return
     */
    protected ConfigNode getRootConfigNode() {
        return config.getRootNode();
    }

    /**
     * 获取SSL证书配置节点
     * 
     * @return
     */
    public ConfigNode getSslCertNode() {
        return config.getRootNode().getChild(ServerConfig.CONF_SERVER_SSL_CERT);
    }

    /**
     * 获取服务器使用的字符集
     * <p>
     * 默认值: 当前默认字符集
     * 
     * @return the charset
     */
    public String getServerCharset() {
        return config.getRootNode().getString(CONF_SERVER_CHARSET,
            Charset.defaultCharset().name());
    }

    /**
     * 获取服务器当前默认时区
     * <p>
     * 默认值: 东8区
     * 
     * @return the charset
     */
    public String getServerTimeZone() {
        return config.getRootNode().getString(CONF_SERVER_TIMEZONE, "GMT+8:00");
    }

    /**
     * 获取服务器业务处理器线程池大小
     * <p>
     * 默认值: 32
     * 
     * @return the thread count
     */
    public int getServerHandleThreadCount() {
        return config.getRootNode().getInteger(CONF_SERVER_HANDLE_THREAD_COUNT,
            32);
    }

    /**
     * 获取服务器空闲超时(单位:s)
     * <p>
     * 默认值: 30s
     * 
     * @return
     */
    public int getServerIdleTimeout() {
        return config.getRootNode().getInteger(CONF_SERVER_IDLE_TIMEOUT, 30);
    }

    /**
     * 获取服务器写超时(单位:s)
     * <p>
     * 默认值: 120s
     * 
     * @return
     */
    public int getServerWriteTimeout() {
        return config.getRootNode().getInteger(CONF_SERVER_WRITE_TIMEOUT, 120);
    }

    /**
     * 返回服务网络IO类型
     * 
     * @see {@link #IO_TYPE_OIO}, {@link #IO_TYPE_NIO}, {@link #IO_TYPE_UIO}
     * @return null=使用服务默认IO类型
     */
    public String getServerNetworkIoType() {
        return config.getRootNode().getNonEmptyString(
            CONF_SERVER_NETWORK_IO_TYPE, null);
    }

    /**
     * 获取服务器普通侦听地址列表
     * 
     * @return
     */
    public String[] getServerBindAddress() {
        return config.getRootNode().getStringArray(CONF_SERVER_BIND_ADDRESS);
    }

    /**
     * 获取服务器普通侦听地址节点列表
     * 
     * @return
     */
    public List<ConfigNode> getServerBindAddressNodes() {
        return config.getRootNode().getChildList(CONF_SERVER_BIND_ADDRESS);
    }

    /**
     * 获取服务器SSL侦听地址列表
     * 
     * @return
     */
    public String[] getServerBindSslAddress() {
        return config.getRootNode()
            .getStringArray(CONF_SERVER_BIND_SSL_ADDRESS);
    }

    /**
     * 获取服务器SSL侦听地址节点列表
     * 
     * @return
     */
    public List<ConfigNode> getServerBindSslAddressNodes() {
        return config.getRootNode().getChildList(CONF_SERVER_BIND_SSL_ADDRESS);
    }

    /**
     * 获取指令消息最长字节数
     * 
     * @return
     */
    public int getCommandLineLengthLimit() {
        return config.getRootNode().getInteger(CONF_SERVER_LINE_LENGTH_LIMIT,
            DEFAULT_LINE_LENGTH_LIMIT);
    }

    /**
     * 获取管理端口
     * 
     * @return 管理端口(-1 = 不需要开启管理端口)
     */
    public int getAdminPort() {
        return config.getRootNode().getInteger(
            ServerConfig.CONF_SERVER_ADMIN_PORT, -1);
    }

    /**
     * 获取管理指令消息最长字节数
     * 
     * @return
     */
    public int getAdminCommandLengthLimit() {
        return config.getRootNode().getInteger(
            CONF_SERVER_ADMIN_COMMAND_LENGTH_LIMIT,
            DEFAULT_ADMIN_COMMAND_LENGTH_LIMIT);
    }

    /**
     * 获取指定的认证插件配置项节点
     * 
     * @return
     */
    public ConfigNode getAuthPluginNode() {
        return config.getRootNode().getChild(CONF_SERVER_AUTH_PLUGIN);
    }

    /**
     * 获取服务器xml配置
     * 
     * @return
     */
    public Configuration getConfiguration() {
        return config;
    }

    /**
     * 从配置文档中载入配置数值
     */
    public abstract void loadFromConfigNode();

}
