package com.netease.hmail.common.utils;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

/**
 * ��com.netease.hmail.common.utils.ByteUtils���в���
 * 
 * @author qiu_sheng
 */
public class ByteUtilsTester {

    @Test
    public void testByteUtils() {
        byte[] buf;

        long l = 0x12456789abcdef12L;
        buf = ByteUtils.longToBytes(l);
        Assert.assertEquals(l, ByteUtils.bytesToLong(buf, true));
        buf = ByteUtils.longToBytes(l, false);
        Assert.assertEquals(l, ByteUtils.bytesToLong(buf, false));
        buf = ByteUtils.getAsBytes(l);
        Assert.assertEquals(l, ByteUtils.toLong(buf));

        int i = 0x12456789;
        buf = ByteUtils.intToBytes(i);
        Assert.assertEquals(i, ByteUtils.bytesToInt(buf, true));
        buf = ByteUtils.intToBytes(i, false);
        Assert.assertEquals(i, ByteUtils.bytesToInt(buf, false));
        buf = ByteUtils.intToBytes(i, false, 6);
        Assert.assertTrue(Arrays.equals(new byte[] { (byte) 0x89, 0x67, 0x45,
            0x12, 0, 0 }, buf));
        buf = ByteUtils.intToBytes(i, true, 2);
        Assert.assertEquals(0x6789, ByteUtils.bytesToInt(buf, true));
        buf = ByteUtils.getAsBytes(i);
        Assert.assertEquals(i, ByteUtils.toInt(buf));

        i = 23;
        buf = ByteUtils.intToBytes(i, true, 2);
        Assert.assertEquals(i, ByteUtils.bytesToInt(buf, true));

        short s = -324;
        buf = ByteUtils.shortToBytes(s, true);
        Assert.assertEquals(s, ByteUtils.bytesToShort(buf, true));
        buf = ByteUtils.shortToBytes(s, false);
        Assert.assertEquals(s, ByteUtils.bytesToShort(buf, false));
        buf = ByteUtils.getAsBytes(s);
        Assert.assertEquals(s, ByteUtils.toShort(buf));

        boolean b = true;
        buf = ByteUtils.getAsBytes(b);
        Assert.assertEquals(b, ByteUtils.toBoolean(buf));
        b = false;
        buf = ByteUtils.getAsBytes(b);
        Assert.assertEquals(b, ByteUtils.toBoolean(buf));

        long l1 = 0x12456789abcdef12L;
        long l2 = 0x12abcdef12456789L;
        byte[] buf1 = ByteUtils.longToBytes(l1);
        byte[] buf2 = ByteUtils.longToBytes(l2);
        ByteUtils.or(buf1, buf2, 0);
        Assert.assertEquals((l1 | l2), ByteUtils.toLong(buf1));
    }

}
