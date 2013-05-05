package com.sunstrider.common.utils;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.git.original.common.utils.HexUtils;

import junit.framework.Assert;

/**
 * ��com.netease.hmail.common.utils.HexUtils���в���
 * 
 * @author qiu_sheng
 */
public class HexUtilsTester {

    @Test
    public void testHexUtils() {
        long l = 0x12456789abcdef12L;
        Assert.assertEquals("12456789abcdef12".toUpperCase(),
            HexUtils.toHexString(l, 0));
        Assert.assertEquals("abcdef12".toUpperCase(),
            HexUtils.toHexString(l, 8));
        Assert.assertEquals("12", HexUtils.toHexString(l));

        int i = 0x12456789;
        Assert.assertEquals("12456789", HexUtils.toHexString(i, 0));
        Assert.assertEquals("0012456789", HexUtils.toHexString(i, 10));
        Assert.assertEquals("89", HexUtils.toHexString(i));

        short s = 0xfed;
        Assert.assertEquals("fed".toUpperCase(), HexUtils.toHexString(s, 0));
        Assert.assertEquals("000fed".toUpperCase(), HexUtils.toHexString(s, 6));
        Assert.assertEquals("ed".toUpperCase(), HexUtils.toHexString(s));

        byte b = 0x7e;
        Assert.assertEquals("7E", HexUtils.toHexString(b, 0));
        Assert.assertEquals("000000000000007E", HexUtils.toHexString(b, 16));
        Assert.assertEquals("7E", HexUtils.toHexString(b));

        Random rd = new Random();
        for (int j = 0; j < 10; j++) {
            byte[] ba = new byte[rd.nextInt(1024 + j)];
            rd.nextBytes(ba);

            String str = HexUtils.toHexString(ba);
            if (rd.nextBoolean()) {
                str = str.toLowerCase();
            }
            byte[] array = HexUtils.parseHexString(str);
            Assert.assertTrue(Arrays.equals(ba, array));
        }

    }

}
