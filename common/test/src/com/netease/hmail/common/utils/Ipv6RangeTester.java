package com.netease.hmail.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author qiu_sheng
 */
public class Ipv6RangeTester {

    @Test
    public void testAddIp() throws UnknownHostException {
        Ipv6Range ir = new Ipv6Range();

        ir.addIp(ByteUtils.toInt(InetAddress.getByName("127.0.0.1")
            .getAddress()));
        ir.addIp("127.0.0.1");
        ir.addIp("127.0.0.1/32");

        ir.addIp(ByteUtils.toInt(InetAddress.getByName("1.23.23.5")
            .getAddress()));
        ir.addIp("192.168.0.33");
        ir.addIp("255.255.255.21");
        ir.addIp("220.181.13.0/32");

        ir = new Ipv6Range();
        ir.addIp("127.0.0.1/24");
        ir.addIp("220.181.13.0/25");
        ir.addIp("220.181.13.0/16");
        Assert.assertEquals(2, ir.getSegmentCount());
        ir.addIp("122.181.12.0/18");
        Assert.assertEquals(3, ir.getSegmentCount());

        ir = new Ipv6Range();
        ir.addIp("::7F00:0001/120");
        ir.addIp("DCB5:0D00::0/25");
        ir.addIp("DCB5:0D00::0/16");
        Assert.assertEquals(2, ir.getSegmentCount());
        ir.addIp("7AB5:0C00::0/18");
        Assert.assertEquals(3, ir.getSegmentCount());

        ir = new Ipv6Range();
        ir.addIp("220.181.13.1-220.181.13.5");
        ir.addIp("192.168.10.11 - 192.168.10.110");
        Assert.assertTrue(ir.isLocated("192.168.10.43"));

        ir = new Ipv6Range();
        ir.addIp("::DCB5:0D01:0000:0001-::DCB5:0D05:0000:0001");
        ir.addIp("::C0A8:0A0B:0000:0001 - ::C0A8:0A6E:0000:0001");
        Assert.assertTrue(ir.isLocated("::C0A8:0A2B:0000:0001"));
    }

    @Test
    public void testLocateIp() throws UnknownHostException {
        Ipv6Range ir = new Ipv6Range();

        ir.addIp("192.168.0.3");
        Assert.assertTrue(ir.isLocated("192.168.0.3"));

        ir.addIp(ByteUtils.toInt(InetAddress.getByName("127.0.0.1")
            .getAddress()));
        ir.addIp(ByteUtils.toInt(InetAddress.getByName("1.23.23.5")
            .getAddress()));
        ir.addIp("192.168.0.33");
        ir.addIp("220.181.13.130/26");
        ir.addIp("122.181.0.0/16");
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

        System.out.println(IPUtils.isIPv6LiteralAddress("23.23.23.23"));

        IpRange ir = new IpRange();
        ir.addIp("::/0");

        System.out.println(ir.isLocated("fe80::5054:ff:fe12:3402"));

    }

}
