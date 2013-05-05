package com.sunstrider.common.bloom;

import java.util.zip.Deflater;

import org.junit.Ignore;
import org.junit.Test;

import com.git.original.common.bloom.BloomFilter;

import junit.framework.Assert;

public class BloomTester {

    @Ignore
    @Test
    public void testBloomFilter() {
        BloomFilter filter = new BloomFilter(16 * 1024);

        String seed = "testBloomFilter@test.com";
        for (int i = 0; i < 1000; i++) {
            filter.add(i + seed);
        }

        for (int i = 0; i < 1000; i++) {
            Assert.assertTrue(filter.membershipTest(i + seed));
        }

        seed = "a" + seed;
        for (int i = 0; i < 1000; i++) {
            Assert.assertFalse(filter.membershipTest(i + seed));
        }
    }

    //	@Ignore
    @Test
    public void testBloomFilterWithGzip() {
        BloomFilter filter = new BloomFilter(16 * 1024);
        Deflater def = new Deflater(Deflater.BEST_SPEED);
        String seed = "testBloomFilter@test.com";
        byte[] bytes, tmp;
        long start;
        int length = 0;

        for (int i = 0; i < 10; i++) {
            filter.add(i + seed);
        }
        bytes = filter.getBitAsObject().getAsBytes();
        tmp = new byte[bytes.length];
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            def.reset();
            def.setInput(bytes);
            def.finish();
            length = def.deflate(tmp);
        }
        System.out.println("10 element: "
            + (System.currentTimeMillis() - start) + " length=" + length);

        for (int i = 10; i < 50; i++) {
            filter.add(i + seed);
        }
        bytes = filter.getBitAsObject().getAsBytes();
        tmp = new byte[bytes.length];
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            def.reset();
            def.setInput(bytes);
            def.finish();
            length = def.deflate(tmp);
        }
        System.out.println("50 element: "
            + (System.currentTimeMillis() - start) + " length=" + length);

        for (int i = 50; i < 500; i++) {
            filter.add(i + seed);
        }
        bytes = filter.getBitAsObject().getAsBytes();
        tmp = new byte[bytes.length];
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            def.reset();
            def.setInput(bytes);
            def.finish();
            length = def.deflate(tmp);
        }
        System.out.println("500 element: "
            + (System.currentTimeMillis() - start) + " length=" + length);

        for (int i = 450; i < 1000; i++) {
            filter.add(i + seed);
        }
        bytes = filter.getBitAsObject().getAsBytes();
        tmp = new byte[bytes.length];
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            def.reset();
            def.setInput(bytes);
            def.finish();
            length = def.deflate(tmp);
        }
        System.out.println("1000 element: "
            + (System.currentTimeMillis() - start) + " length=" + length);

        for (int i = 1000; i < 1500; i++) {
            filter.add(i + seed);
        }
        bytes = filter.getBitAsObject().getAsBytes();
        tmp = new byte[bytes.length];
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            def.reset();
            def.setInput(bytes);
            def.finish();
            length = def.deflate(tmp);
        }
        System.out.println("1500 element: "
            + (System.currentTimeMillis() - start) + " length=" + length);
    }

}
