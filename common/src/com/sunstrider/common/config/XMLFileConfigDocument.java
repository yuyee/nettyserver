/**
 * @(#)XMLFileConfigDocument.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 本地XML文件配置文档
 * <p>
 * 注意: XML格式的配置文件只支持Element元素, 不支持Attribute属性
 * 
 */
public class XMLFileConfigDocument extends BaseConfigDocument {

    /** 日志输出 */
    private static final Logger LOG = LoggerFactory
        .getLogger(XMLFileConfigDocument.class);

    /**
     * 配置文件路径
     */
    protected String configFilePath = null;

    /**
     * 配置文件简单摘要
     * <p>
     * 用于判断配置文件是否发生变化需要重载
     */
    private String configFileDigest = StringUtils.EMPTY;

    /**
     * 本配置文档在数据库中的版本号
     */
    private String dbVersion = null;

    /**
     * 是否忽略本配置文档在数据库中的版本(即, 不会被数据库内的配置版本覆盖)
     */
    private boolean ignoreDb = false;

    /**
     * 扫描配置是否发生变化,并自动重载的时间周期(毫秒)
     */
    private Long scanMillis = null;

    /**
     * 创建默认实例
     * 
     * @param confPath
     *            配置文件路径，支持相对路径, 默认当前路径为HMAIL_HOME
     */
    public XMLFileConfigDocument(String confPath) {
        super();
        this.configFilePath = confPath;
    }

    /**
     * 使用指定的根配置节点创建文档实例
     * 
     * @param rootNode
     *            配置根节点
     */
    XMLFileConfigDocument(ConfigNode rootNode) {
        super(rootNode);
    }

    @Override
    protected ConfigNode doLoad() throws Exception {
        return doLoad(this.configFilePath);
    }

    protected ConfigNode doLoad(String configFilePath) throws Exception {
        InputStream in = null;
        try {
            File confFile = new File(configFilePath);
            if (!confFile.isAbsolute()) {
                confFile = new File(this.getHmailHome(), configFilePath);
            }
            if (!confFile.exists()) {
                throw new FileNotFoundException("path:" + confFile);
            }

            String currentDigest = confFile.lastModified() + ";"
                + confFile.length();
            if (currentDigest.equals(this.configFileDigest)) {
                // 文件修改时间和大小都未变化, 认为配置文件未改动
                return null;
            }

            in = new FileInputStream(confFile);

            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            NodeBuilder nb = new NodeBuilder();
            parser.parse(in, nb);
            ConfigNode result = nb.getRootNode();

            if (result != null) {
                try {
                    this.dbVersion = result
                        .getAttribute(CONF_DOC_ATTRIBUTE_DB_VERSION);
                } catch (Exception ex) {
                    this.dbVersion = null;
                }

                try {
                    String str = result
                        .getAttribute(CONF_DOC_ATTRIBUTE_IGNORE_DB);
                    if (StringUtils.isEmpty(str)) {
                        this.ignoreDb = false;
                    } else {
                        this.ignoreDb = Boolean.parseBoolean(str);
                    }
                } catch (Exception ex) {
                    this.ignoreDb = false;
                }

                try {
                    String tmp = result
                        .getAttribute(CONF_DOC_ATTRIBUTE_SCAN_PERIOD);
                    if (StringUtils.isEmpty(tmp)) {
                        this.scanMillis = null;
                    } else {
                        this.scanMillis = ConfigNode.textToNumericMillis(tmp
                            .trim());
                    }
                } catch (Exception ex) {
                    this.scanMillis = null;
                }
            }

            LOG.debug("Load config from local dir success, file:{}",
                configFilePath);
            return result;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore exception
                }
            }
        }
    }

    @Override
    public String toString() {
        return "[FileConfig]" + configFilePath;
    }

    /**
     * 将XML对象转换为配置节点对象
     * 
     * @param elem
     *            XML节点
     * @return 配置节点对象
     */
    static ConfigNode convertElement(Element elem) {
        if (elem == null) {
            return null;
        }

        ConfigNode cn = new ConfigNode(elem.getTagName(), null);

        NamedNodeMap attrNodeMap = elem.getAttributes();
        if (attrNodeMap != null) {
            for (int i = 0; i < attrNodeMap.getLength(); i++) {
                Node node = attrNodeMap.item(i);
                cn.addAttribute(node.getNodeName(), node.getNodeValue());
            }
        }

        NodeList nodeList = elem.getChildNodes();
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                switch (node.getNodeType()) {
                    case Node.ATTRIBUTE_NODE:
                        cn.addAttribute(node.getNodeName(), node.getNodeValue());
                        break;
                    case Node.ELEMENT_NODE:
                        ConfigNode child = convertElement((Element) node);
                        cn.addChild(child);
                        break;
                    case Node.TEXT_NODE:
                        cn.value = node.getNodeValue();
                        break;
                    default:
                        continue;
                }
            }
        }

        return cn;
    }

    /**
     * 将配置节点对象转换为XML节点对象
     * 
     * @param cn
     *            配置节点对象
     * @return XML节点对象
     */
    @SuppressWarnings("unchecked")
    static Element convertConfigNode(Document doc, ConfigNode cn) {
        Element elem = doc.createElement(cn.getName());

        Map<String, String> attrMap = cn.attributes();
        if (attrMap != null) {
            for (Entry<String, String> entry: attrMap.entrySet()) {
                elem.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        if (cn.hasChildren()) {
            for (Object child: cn.getAllChildren()) {
                if (child instanceof List) {
                    List<ConfigNode> cnList = (List<ConfigNode>) child;
                    for (ConfigNode node: cnList) {
                        elem.appendChild(convertConfigNode(doc, node));
                    }
                } else {
                    elem.appendChild(convertConfigNode(doc, (ConfigNode) child));
                }
            }

        } else if (cn.value != null) {
            elem.setTextContent(cn.value.toString());
        }

        return elem;
    }

    @Override
    public String getDbVersion() {
        return this.dbVersion;
    }

    @Override
    public boolean ignoreDb() {
        return this.ignoreDb;
    }

    @Override
    public Long getScanMillis() {
        return this.scanMillis;
    }

    /** {@link ConfigNode}树构造器 */
    private static class NodeBuilder extends DefaultHandler {
        /** node树根节点 */
        private ConfigNode rootNode = null;

        /** 当前正在构建的node节点 */
        private ConfigNode currentNode = null;

        /** 正在构建的node节点栈(处理嵌套节点的情况) */
        private Deque<ConfigNode> cnDeque = new ArrayDeque<ConfigNode>();

        /** 当前节点的字符串值缓存区 */
        private StringBuilder tmpValue = new StringBuilder();

        /**
         * @return the rootNode
         */
        public ConfigNode getRootNode() {
            return rootNode;
        }

        /*
         * (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#startDocument()
         */
        @Override
        public void startDocument() throws SAXException {
            cnDeque.clear();
        }

        /*
         * (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#endDocument()
         */
        @Override
        public void endDocument() throws SAXException {
            if (!cnDeque.isEmpty()) {
                throw new SAXException("cnDeque is not empty");
            }
        }

        /*
         * (non-Javadoc)
         * @see
         * org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
         * java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
            this.currentNode = new ConfigNode(qName, null);

            if (attributes != null && attributes.getLength() > 0) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    currentNode.addAttribute(attributes.getQName(i),
                        attributes.getValue(i));
                }
            }

            this.cnDeque.push(currentNode);
            this.tmpValue.setLength(0);
        }

        /*
         * (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
         * java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException {
            if (this.currentNode != this.cnDeque.pop()) {
                // 是否和currentNode一致?
                throw new SAXException(
                    "current node is not the first one of the cnDeque.");
            }

            if (tmpValue.length() > 0) {
                this.currentNode.value = tmpValue.toString();
            }

            if (!this.cnDeque.isEmpty()) {
                this.cnDeque.peek().addChild(currentNode);
            } else {
                this.rootNode = this.currentNode;
            }
            this.currentNode = this.cnDeque.peek();
        }

        /*
         * (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length)
            throws SAXException {
            if (!this.cnDeque.isEmpty()) {
                tmpValue.append(ch, start, length);
            }
        }

    }

}
