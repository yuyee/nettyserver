package com.netease.hmail.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * ѹ�����߲�������
 * 
 * @author qiusheng
 */
public class CompressTester {
    @Test
    public void testZlibFullData() throws DataFormatException {
        Random rd = new Random();
        long totalSrcLength = 0;
        long totalCompressedLength = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            int length = rd.nextInt(1024);
            byte[] src = new byte[length * 1024];
            rd.nextBytes(src);
            totalSrcLength += src.length;

            ByteBuffer dst1 = CompressUtils.compressDefalte(
                ByteBuffer.wrap(src), Deflater.DEFAULT_COMPRESSION);
            totalCompressedLength += dst1.remaining();

            ByteBuffer dst2 = CompressUtils.uncompressDefalte(dst1);
            byte[] uncompressed = new byte[dst2.remaining()];
            dst2.get(uncompressed);

            Assert.assertArrayEquals(src, uncompressed);
        }

        System.out.println("during: " + (System.currentTimeMillis() - start));
        System.out.println("source length=" + totalSrcLength
            + "\tcompressed length=" + totalCompressedLength);
        System.out.println("compress rate:"
            + ((double) totalCompressedLength / totalSrcLength));
    }

    @Test
    public void testZlibNormalData() throws DataFormatException {
        Random rd = new Random();
        long totalSrcLength = 0;
        long totalCompressedLength = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            int length = rd.nextInt(1024);
            byte[] tmp = new byte[length * 1024];
            rd.nextBytes(tmp);
            byte[] src = new byte[tmp.length * 2];
            for (int j = 0; j < tmp.length; j++) {
                src[j * 2] = tmp[j];
            }
            totalSrcLength += src.length;

            ByteBuffer dst1 = CompressUtils.compressFastestDefalte(ByteBuffer
                .wrap(src));
            totalCompressedLength += dst1.remaining();

            ByteBuffer dst2 = CompressUtils.uncompressDefalte(dst1);
            byte[] uncompressed = new byte[dst2.remaining()];
            dst2.get(uncompressed);

            Assert.assertArrayEquals(src, uncompressed);
        }

        System.out.println("during: " + (System.currentTimeMillis() - start));
        System.out.println("source length=" + totalSrcLength
            + "\tcompressed length=" + totalCompressedLength);
        System.out.println("compress rate:"
            + ((double) totalCompressedLength / totalSrcLength));
    }

    @Test
    public void testSnappyFullData() throws DataFormatException {
        Random rd = new Random();
        long totalSrcLength = 0;
        long totalCompressedLength = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            int length = rd.nextInt(1024);
            byte[] src = new byte[length * 1024];
            rd.nextBytes(src);
            totalSrcLength += src.length;

            ByteBuffer dst1 = CompressUtils.compressSnappy(
                ByteBuffer.wrap(src), false);
            totalCompressedLength += dst1.remaining();

            ByteBuffer dst2 = CompressUtils.uncompressSnappy(dst1);
            byte[] uncompressed = new byte[dst2.remaining()];
            dst2.get(uncompressed);

            Assert.assertArrayEquals(src, uncompressed);
        }

        System.out.println("during: " + (System.currentTimeMillis() - start));
        System.out.println("source length=" + totalSrcLength
            + "\tcompressed length=" + totalCompressedLength);
        System.out.println("compress rate:"
            + ((double) totalCompressedLength / totalSrcLength));
    }

    @Test
    public void testSnappyNormalData() throws DataFormatException {
        Random rd = new Random();
        long totalSrcLength = 0;
        long totalCompressedLength = 0;

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            int length = rd.nextInt(1024);
            byte[] tmp = new byte[length * 1024];
            rd.nextBytes(tmp);
            byte[] src = new byte[tmp.length * 2];
            for (int j = 0; j < tmp.length; j++) {
                src[j * 2] = tmp[j];
            }
            totalSrcLength += src.length;

            ByteBuffer dst1 = CompressUtils.compressSnappy(
                ByteBuffer.wrap(src), false);
            totalCompressedLength += dst1.remaining();

            ByteBuffer dst2 = CompressUtils.uncompressSnappy(dst1);
            byte[] uncompressed = new byte[dst2.remaining()];
            dst2.get(uncompressed);

            Assert.assertArrayEquals(src, uncompressed);
        }

        System.out.println("during: " + (System.currentTimeMillis() - start));
        System.out.println("source length=" + totalSrcLength
            + "\tcompressed length=" + totalCompressedLength);
        System.out.println("compress rate:"
            + ((double) totalCompressedLength / totalSrcLength));
    }

    public static void main(String[] args) throws IOException {
        byte[] src = new byte[] { 54, -93, -40, -93, -93, -63, -65, -25, -35,
            102, 80, -108, -17, -110, 126, -87, 36, -102, -42, -127, 87, 47 };

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(bos);
        gos.write(src);
        gos.close();

        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(
            bos.toByteArray()));
        while (gis.read() != -1);

    }
}
