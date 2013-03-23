package com.netease.hmail.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

import sun.net.util.IPAddressUtil;

/**
 * @author qiu_sheng
 */
public class IpRangeTester {

    @Test
    public void testAddSingleIp() throws UnknownHostException {
        IpRange ir = new IpRange();
        Assert.assertTrue(ir.isTiled());

        ir.addIp(ByteUtils.toInt(InetAddress.getByName("127.0.0.1")
            .getAddress()));
        Assert.assertEquals(1, ir.getSetSize());
        ir.addIp("127.0.0.1");
        Assert.assertEquals(1, ir.getSetSize());
        ir.addIp("127.0.0.1/32");
        Assert.assertEquals(1, ir.getSetSize());
        Assert.assertTrue(ir.isTiled());

        ir.addIp(ByteUtils.toInt(InetAddress.getByName("1.23.23.5")
            .getAddress()));
        Assert.assertEquals(2, ir.getSetSize());
        ir.addIp("192.168.0.33");
        Assert.assertEquals(3, ir.getSetSize());
        ir.addIp("255.255.255.21");
        Assert.assertEquals(4, ir.getSetSize());
        ir.addIp("220.181.13.0/32");
        Assert.assertEquals(5, ir.getSetSize());
        Assert.assertTrue(ir.isTiled());
    }

    @Test
    public void testAddMultiIp() throws UnknownHostException {
        IpRange ir = new IpRange();
        Assert.assertTrue(ir.isTiled());

        ir.addIp("127.0.0.1/24");
        Assert.assertEquals(256, ir.getSetSize());
        Assert.assertTrue(ir.isTiled());
        ir.addIp("220.181.13.0/25");
        Assert.assertEquals(384, ir.getSetSize());
        Assert.assertTrue(ir.isTiled());

        ir.addIp("220.181.13.0/16");
        Assert.assertFalse(ir.isTiled());
        Assert.assertEquals(2, ir.getSegmentCount());
        ir.addIp("122.181.12.0/18");
        Assert.assertFalse(ir.isTiled());
        Assert.assertEquals(3, ir.getSegmentCount());

        ir = new IpRange();
        Assert.assertTrue(ir.isTiled());
        ir.addIp("220.181.13.0/22");
        Assert.assertEquals(1024, ir.getSetSize());
        Assert.assertTrue(ir.isTiled());
        ir.addIp("192.168.10.11");
        Assert.assertFalse(ir.isTiled());
        Assert.assertEquals(2, ir.getSegmentCount());

        ir = new IpRange();
        Assert.assertTrue(ir.isTiled());
        ir.addIp("220.181.13.1-220.181.13.5");
        Assert.assertEquals(5, ir.getSetSize());
        Assert.assertTrue(ir.isTiled());
        ir.addIp("192.168.10.11 - 192.168.10.110");
        Assert.assertTrue(ir.isLocated("192.168.10.43"));
    }

    @Test
    public void testLocateIp() throws UnknownHostException {
        IpRange ir = new IpRange();
        Assert.assertTrue(ir.isTiled());

        ir.addIp("192.168.0.3");
        Assert.assertTrue(ir.isLocated("192.168.0.3"));

        ir.addIp(ByteUtils.toInt(InetAddress.getByName("127.0.0.1")
            .getAddress()));
        Assert.assertEquals(2, ir.getSetSize());
        ir.addIp(ByteUtils.toInt(InetAddress.getByName("1.23.23.5")
            .getAddress()));
        Assert.assertEquals(3, ir.getSetSize());
        ir.addIp("192.168.0.33");
        Assert.assertEquals(4, ir.getSetSize());
        ir.addIp("220.181.13.130/26");
        Assert.assertEquals(68, ir.getSetSize());
        Assert.assertTrue(ir.isTiled());
        ir.addIp("122.181.0.0/16");
        Assert.assertFalse(ir.isTiled());
        Assert.assertEquals(6, ir.getSegmentCount());

        Assert.assertFalse(ir.isLocated("127.0.0.2"));
        Assert.assertTrue(ir.isLocated("127.0.0.1"));
        Assert.assertTrue(ir.isLocated("1.23.23.5"));
        Assert.assertFalse(ir.isLocated("192.168.0.32"));
        Assert.assertTrue(ir.isLocated("192.168.0.33"));
        Assert.assertFalse(ir.isLocated("192.168.0.34"));

        Assert.assertFalse(ir.isLocated("220.181.13.127"));
        long lip = ByteUtils.toInt(InetAddress.getByName("220.181.13.128")
            .getAddress()) & 0xFFFFFFFFL;
        for (int i = 0; i < 64; i++) {
            Assert.assertTrue(ir.isLocated((int) (lip + i)));
        }
        Assert.assertFalse(ir.isLocated("220.181.13.192"));

        lip = ByteUtils
            .toInt(InetAddress.getByName("122.181.0.0").getAddress()) & 0xFFFFFFFFL;
        for (int i = 0; i < 65536; i += 64) {
            Assert.assertTrue(ir.isLocated((int) (lip + i)));
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        // long l = (0x0FFFFFFFFL ^ (0xFFFFFFFFL >>> 16));
        // int ip =
        // ByteUtils.toInt(InetAddress.getByName("192.168.1.1").getAddress());
        //
        // System.out.println(ip & l);
        // System.out.println(1 << (32 - 16));
        //
        // IpRange iprange = new IpRange();
        // iprange.addIp("127.0.0.1");
        // iprange.addIp("192.168.130.55");
        // iprange.addIp("192.168.130.56");
        // iprange.addIp("192.168.1.1/16");
        // System.out.println(iprange.isLocated("192.168.135.51"));

        System.out.println(IPAddressUtil.isIPv4LiteralAddress("34.43"));

    }

}
