package com.netease.hmail.common.config;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ���ڱ���XML�ļ��������ĵ���JUNIT������
 * <p>
 * �����ļ�: test/data/config.test.xml
 * 
 * @author qiu_sheng
 */
public class XMLConfigTester {

    static XMLFileConfigDocument config = new XMLFileConfigDocument(
        "test/data/config.test.xml");

    @BeforeClass
    public static void prepare() throws Exception {
        System.setProperty(BaseConfigDocument.PROPERTY_HMAIL_HOME, ".");
        config.setHmailHome(".");
        config.load();
    }

    @Test
    public void testBoolean() {
        ConfigNode cn = config.getRootNode();

        Assert.assertEquals(false, cn.getBoolean("boolean-false"));
        Assert.assertEquals(true, cn.getBoolean("boolean-true"));
        Assert.assertEquals(true, cn.getBoolean("boolean-none", true));
    }

    @Test
    public void testNumber() {
        ConfigNode cn = config.getRootNode();

        Assert.assertEquals(12345, cn.getInteger("int"));
        Assert.assertEquals(1234567890123456789L, cn.getLong("long"));
        Assert.assertEquals(12345.6789, cn.getDouble("double"));

        Assert.assertEquals(54321, cn.getInteger("int-none", 54321));
        Assert.assertEquals(987654321987654321L,
            cn.getLong("long-none", 987654321987654321L));
        Assert
            .assertEquals(9876.54321, cn.getDouble("double-none", 9876.54321));

        Assert.assertEquals(12345, cn.getInteger("int", 54321));
        Assert.assertEquals(1234567890123456789L,
            cn.getLong("long", 987654321987654321L));
        Assert.assertEquals(12345.6789, cn.getDouble("double", 9876.54321));
    }

    @Test
    public void testMilliseconds() {
        ConfigNode cn = config.getRootNode();

        Assert.assertEquals(1L * 24 * 3600 * 1000,
            cn.getMilliseconds("day-millis", 1L));
        Assert.assertEquals(12L * 3600 * 1000,
            cn.getMilliseconds("hour-millis", 1L));
        Assert.assertEquals(23L * 60 * 1000,
            cn.getMilliseconds("min-millis", 1L));
        Assert.assertEquals(34L * 1000, cn.getMilliseconds("sec-millis", 1L));
        Assert.assertEquals(1L * 24 * 3600 * 1000,
            cn.getMilliseconds("up-day-millis", 1L));
        Assert.assertEquals(12L * 3600 * 1000,
            cn.getMilliseconds("up-hour-millis", 1L));
        Assert.assertEquals(23L * 60 * 1000,
            cn.getMilliseconds("up-min-millis", 1L));
        Assert
            .assertEquals(34L * 1000, cn.getMilliseconds("up-sec-millis", 1L));
        Assert.assertEquals(23423142342343243L,
            cn.getMilliseconds("ms-millis", 1L));
        Assert.assertEquals(23423142342343243L,
            cn.getMilliseconds("default-millis", 1L));

        Assert.assertEquals(12345678L,
            cn.getMilliseconds("none-day-millis", 12345678L));
        Assert.assertEquals(123456789L,
            cn.getMilliseconds("none-hour-millis", 123456789L));
        Assert.assertEquals(1234567890L,
            cn.getMilliseconds("none-min-millis", 1234567890L));
        Assert.assertEquals(12345678901L,
            cn.getMilliseconds("none-sec-millis", 12345678901L));
        Assert.assertEquals(123456789012L,
            cn.getMilliseconds("none-ms-millis", 123456789012L));
        Assert.assertEquals(1234567890123L,
            cn.getMilliseconds("none-default-millis", 1234567890123L));
    }

    @Test
    public void testString() {
        ConfigNode cn = config.getRootNode();

        Assert.assertEquals("163.com", cn.getString("string"));
        Assert.assertEquals("none", cn.getString("string-none", "none"));

        Assert.assertEquals("true", cn.getString("boolean-true"));
        Assert.assertEquals("12345", cn.getString("int"));

        String[] strs = cn.getStringArray("Array.item");
        Assert.assertEquals(4, strs.length);
        Assert.assertEquals("80", strs[0]);
        Assert.assertEquals("443", strs[1]);
        Assert.assertEquals("110", strs[2]);
        Assert.assertEquals("995", strs[3]);
    }

    @Test
    public void testNested() {
        ConfigNode cn = config.getRootNode();

        Assert.assertEquals("80", cn.getString("Array.item"));
        Assert.assertEquals(true,
            cn.getBoolean("nes..ted.nested...boolean-true"));
        Assert.assertEquals(1234567890123456789L,
            cn.getLong("nes..ted.nested...long"));
        Assert.assertEquals("163.com",
            cn.getString("nes..ted.nested...string", "none"));

        String[] strs = cn.getStringArray("nes..ted.nested...Array.item");
        Assert.assertEquals(4, strs.length);
        Assert.assertEquals("80", strs[0]);
        Assert.assertEquals("443", strs[1]);
        Assert.assertEquals("110", strs[2]);
        Assert.assertEquals("995", strs[3]);

        ConfigNode child = cn.getChild("nes..ted.nested..");
        Assert.assertEquals(1234567890123456789L, child.getLong("long"));
    }

    @Test
    public void testWrite() throws IOException {
        ConfigurationHelper.writeAsXML(config, System.out, "GBK");
    }

    @Test
    public void testCompare() throws Exception {
        ConfigNode cn = config.getRootNode();
        ConfigNode other = config.doLoad();

        Assert.assertTrue(cn.compareTo(null) > 0);
        Assert.assertTrue(cn.compareTo(other) == 0);

        Object oldValue = other.getChild("Array.item").value;
        other.getChild("Array.item").value = "100";
        Assert.assertTrue(cn.compareTo(other) != 0);
        other.getChild("Array.item").value = oldValue;
        Assert.assertTrue(cn.compareTo(other) == 0);

        cn.getChild("nes..ted.nested..").getAllChildren()
            .remove(cn.getChild("nes..ted.nested...long"));
        Assert.assertTrue(cn.compareTo(other) != 0);
        other.getChild("nes..ted.nested..").getAllChildren()
            .remove(other.getChild("nes..ted.nested...long"));
        Assert.assertTrue(cn.compareTo(other) == 0);

        cn.getChild("string").addAttribute("compareAttr", "value");
        Assert.assertTrue(cn.compareTo(other) != 0);
        // key��Сд��һ��
        other.getChild("string").addAttribute("compareattr", "value");
        Assert.assertTrue(cn.compareTo(other) != 0);
        other.getChild("string").addAttribute("compareAttr", "value");
        other.getChild("string").addAttribute("compareattr", null);
        Assert.assertTrue(cn.compareTo(other) == 0);
    }

}
