/**
 * @(#)ConfigurationHelper.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.config;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * HMail配置助手类
 */
public final class ConfigurationHelper {

    /** 构造函数 */
    private ConfigurationHelper() {};

    /**
     * 自动创建配置文档实例, 并载入配置参数
     * 
     * @param filePath
     *            配置文件路径
     * @return 配置文档实例
     * @throws Exception
     *             载入参数失败
     */
    public static Configuration loadConfiguration(String filePath)
        throws Exception {
        Configuration config = null;

        // 从本地文件系统中读取配置
        config = new XMLFileConfigDocument(filePath);
        config.load();

        return config;
    }

    /**
     * 根据XML树生成相应的配置文档实例
     * 
     * @param rootElem
     *            XML树根节点
     * @return 配置文档实例
     */
    public static Configuration parseXmlElement(Element rootElem) {
        ConfigNode rootNode = XMLFileConfigDocument.convertElement(rootElem);
        if (rootNode == null) {
            return null;
        }

        return new XMLFileConfigDocument(rootNode);
    }

    /**
     * 以XML文档格式输出配置文档
     * <p>
     * 包含XML头声明
     * 
     * @param conf
     *            配置文档对象
     * @param out
     *            输出流
     * @param encoding
     *            字符集名称(值为null时自动使用GBK字符集)
     * @throws IOException
     */
    public static void writeAsXML(Configuration conf, OutputStream out,
        String encoding) throws IOException {
        writeAsXML(conf.getRootNode(), out, encoding);
    }

    /**
     * 以XML文档格式输出配置节点
     * <p>
     * 包含XML头声明
     * 
     * @param cn
     *            配置节点(作为XML文档中的根节点)
     * @param out
     *            输出流
     * @param encoding
     *            字符集名称(值为null时自动使用GBK字符集)
     * @throws IOException
     */
    public static void writeAsXML(ConfigNode cn, OutputStream out,
        String encoding) throws IOException {
        if (cn == null) {
            return;
        }

        if (encoding == null || encoding.equalsIgnoreCase("GBK")) {
            encoding = "GB2312";
        }

        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory
                .newInstance().newDocumentBuilder();

            Document document = documentBuilder.newDocument();
            document.setXmlStandalone(false);
            document.appendChild(XMLFileConfigDocument.convertConfigNode(
                document, cn));

            Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
            //
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "2");
            StreamResult streamResult = new StreamResult(out);
            transformer.transform(source, streamResult);

        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty(BaseConfigDocument.PROPERTY_HMAIL_HOME, ".");
        File file = new File("test/data/config.test.xml");

        long start = System.currentTimeMillis();

        start = System.currentTimeMillis();
        Configuration conf = loadConfiguration(file.getAbsolutePath());
        writeAsXML(conf, System.out, null);
        System.out.println(System.currentTimeMillis() - start);
    }

}
