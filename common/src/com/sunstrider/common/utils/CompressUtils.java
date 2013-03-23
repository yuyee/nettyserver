/**
 * @(#)CompressUtils.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.xerial.snappy.Snappy;

/**
 * 压缩工具类
 */
public final class CompressUtils {

    /** 用于在解压缩时填充数据,通知{@link Inflater}解压结束 */
    private static final byte[] INFALTE_PADDING_BYTES = new byte[] { 0x0 };

    /** 构造函数 */
    private CompressUtils() {};

    /**
     * 基于ZLIB压缩库，使用<a
     * href="http://en.wikipedia.org/wiki/DEFLATE">Deflate</a>算法对指定数据进行压缩
     * <p>
     * 压缩等级 默认为: {@link Deflater#BEST_SPEED}
     * 
     * @param buffer
     *            需要压缩的缓存区
     * @return 压缩后的数据
     * @see {@link Deflater}
     */
    public static ByteBuffer compressFastestDefalte(ByteBuffer buffer) {
        return compressDefalte(buffer, Deflater.BEST_SPEED, true);
    }

    /**
     * 基于ZLIB压缩库，使用<a
     * href="http://en.wikipedia.org/wiki/DEFLATE">Deflate</a>算法对指定数据进行压缩
     * 
     * @param buffer
     *            需要压缩的缓存区
     * @param level
     *            压缩等级(0-9)
     * @return 压缩后的数据
     * @see {@link Deflater}
     */
    public static ByteBuffer compressDefalte(ByteBuffer buffer, int level) {
        return compressDefalte(buffer, level, true);
    }

    /**
     * 基于ZLIB压缩库，使用<a
     * href="http://en.wikipedia.org/wiki/DEFLATE">Deflate</a>算法对指定数据进行压缩
     * 
     * @param buffer
     *            需要压缩的缓存区
     * @param level
     *            压缩等级(0-9)
     * @param nowarp
     *            if true then use GZIP compatible compression
     * @return 压缩后的数据
     * @see {@link Deflater}
     */
    public static ByteBuffer compressDefalte(ByteBuffer buffer, int level,
        boolean nowarp) {
        // 获取需要压缩的原始字节长度
        int srcLength = buffer.remaining();

        // Deflate no more than stride bytes at a time. This avoids
        // excess copying in deflateBytes (see Deflater.c)
        Deflater def = new Deflater(level, nowarp);
        if (buffer.hasArray()) {
            def.setInput(buffer.array(),
                buffer.position() + buffer.arrayOffset(), srcLength);
            buffer.position(buffer.limit());
        } else {
            byte[] srcBytes = new byte[srcLength];
            buffer.get(srcBytes);
            def.setInput(srcBytes);
        }
        def.finish();

        /*
         * 设置预设存储数据块 注意: 对于纯随机数值, 最终压缩后的数据大小可能会比原始数据多, 因此预分配时需要多分配5个字节以上, 减少数组扩容
         */
        int capacity = (srcLength > 4 * 1024) ? (srcLength >> 2)
            : (srcLength + 5);
        byte[] bytes = new byte[(capacity <= 0 ? 8 : capacity)]; // 保证不会出现0字节缓存
        int offset = 0;
        int limit = bytes.length;

        // 压缩
        do {
            if (offset >= limit) {
                // 扩容
                byte[] tmp = new byte[(bytes.length << 1)];
                System.arraycopy(bytes, 0, tmp, 0, bytes.length);

                offset = bytes.length;
                limit = tmp.length;
                bytes = tmp;
            }

            int len = def.deflate(bytes, offset, (limit - offset));
            if (len > 0) {
                offset += len;
            }
        } while (!def.finished());

        return ByteBuffer.wrap(bytes, 0, offset);
    }

    /**
     * 基于ZLIB压缩库，对<a
     * href="http://en.wikipedia.org/wiki/DEFLATE">Deflate</a>算法压缩后的数据进行解压缩操作
     * 
     * @param buffer
     *            压缩后的数据
     * @return 解压后的原始数据
     * @throws DataFormatException
     * @see {@link Inflater}
     */
    public static ByteBuffer uncompressDefalte(ByteBuffer buffer)
        throws DataFormatException {
        return uncompressDefalte(buffer, true);
    }

    /**
     * 基于ZLIB压缩库，对<a
     * href="http://en.wikipedia.org/wiki/DEFLATE">Deflate</a>算法压缩后的数据进行解压缩操作
     * 
     * @param buffer
     *            压缩后的数据
     * @return 解压后的原始数据
     * @throws DataFormatException
     * @see {@link Inflater}
     */
    public static ByteBuffer uncompressDefalte(ByteBuffer buffer, boolean nowrap)
        throws DataFormatException {

        // 获取需要解压缩的原始字节长度
        int srcLength = buffer.remaining();

        Inflater inf = new Inflater(nowrap);
        if (buffer.hasArray()) {
            inf.setInput(buffer.array(),
                buffer.position() + buffer.arrayOffset(), srcLength);
            buffer.position(buffer.limit());
        } else {
            byte[] srcBytes = new byte[srcLength];
            buffer.get(srcBytes);
            inf.setInput(srcBytes);
        }

        // 设置预设存储数据块
        int capacity = (srcLength < 1 * 1024) ? (srcLength << 2) : srcLength;
        byte[] bytes = new byte[(capacity <= 0 ? 8 : capacity)]; // 保证不会出现0字节缓存
        int offset = 0;
        int limit = bytes.length;

        // 解压缩
        do {
            if (offset >= limit) {
                // 扩容
                byte[] tmp = new byte[(bytes.length << 1)];
                System.arraycopy(bytes, 0, tmp, 0, bytes.length);

                offset = bytes.length;
                limit = tmp.length;
                bytes = tmp;
            }

            int inflated = inf.inflate(bytes, offset, (limit - offset));
            if (inflated > 0) {
                offset += inflated;
            } else if (inf.needsInput()) {
                inf.setInput(INFALTE_PADDING_BYTES);
            }
        } while (!inf.finished());

        inf.end();

        return ByteBuffer.wrap(bytes, 0, offset);
    }

    /**
     * 基于snappy-java压缩库，使用<a
     * href="http://code.google.com/p/snappy/">Snappy</a>算法对指定数据进行压缩
     * <p>
     * Note: snappy对类似 [0x00 0xY1Y2 0x00 0xY3Y4 ...]的数据压缩效果很差
     * 
     * @param buffer
     *            需要压缩的缓存区
     * @param autoShrink
     * @return 压缩后的数据
     */
    public static ByteBuffer compressSnappy(ByteBuffer buffer) {
        return compressSnappy(buffer, true);
    }

    /**
     * 基于snappy-java压缩库，使用<a
     * href="http://code.google.com/p/snappy/">Snappy</a>算法对指定数据进行压缩
     * <p>
     * Note: snappy对类似 [0x00 0xY1Y2 0x00 0xY3Y4 ...]的数据压缩效果很差
     * 
     * @param buffer
     *            需要压缩的缓存区
     * @param autoShrink
     * @return 压缩后的数据
     */
    public static ByteBuffer compressSnappy(ByteBuffer buffer,
        boolean autoShrink) {

        ByteBuffer compressed;

        try {
            if (buffer.isDirect()) {
                compressed = ByteBuffer.allocateDirect(Snappy
                    .maxCompressedLength(buffer.remaining()));
                Snappy.compress(buffer, compressed);
            } else {
                byte[] ba;
                int offset;
                int size = buffer.remaining();

                if (buffer.hasArray()) {
                    ba = buffer.array();
                    offset = buffer.arrayOffset() + buffer.position();
                    buffer.position(buffer.limit());
                } else {
                    ba = new byte[size];
                    buffer.get(ba);
                    offset = 0;
                }

                byte[] out = new byte[Snappy.maxCompressedLength(size)];
                int compressedByteSize = Snappy.compress(ba, offset, size, out,
                    0);

                if (autoShrink) {
                    compressed = ByteBuffer.allocate(compressedByteSize);
                    compressed.put(out, 0, compressedByteSize);
                    compressed.flip();
                } else {
                    compressed = ByteBuffer.wrap(out, 0, compressedByteSize);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("snappy compress failed:", ex);
        }

        return compressed;
    }

    /**
     * 基于snappy-java压缩库，使用<a
     * href="http://code.google.com/p/snappy/">Snappy</a>算法对指定数据进行解压缩
     * 
     * @param buffer
     *            需要解压缩的缓存区
     * @return 解压缩后的原始数据
     */
    public static ByteBuffer uncompressSnappy(ByteBuffer buffer) {

        try {
            byte[] ba;
            int offset;
            int size = buffer.remaining();

            if (buffer.hasArray()) {
                ba = buffer.array();
                offset = buffer.arrayOffset() + buffer.position();
                buffer.position(buffer.limit());
            } else {
                ba = new byte[size];
                buffer.get(ba);
                offset = 0;
            }

            byte[] out = new byte[Snappy.uncompressedLength(ba, offset, size)];
            int byteSize = Snappy.uncompress(ba, offset, size, out, 0);

            return ByteBuffer.wrap(out, 0, byteSize);

        } catch (IOException ex) {
            throw new RuntimeException("snappy compress failed:", ex);
        }
    }
}
