package com.sunstrider.common.buffer;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sunstrider.common.buffer.BufferConfig;
import com.sunstrider.common.buffer.impl.BaseBufferFactory;
import com.sunstrider.common.buffer.impl.BothBufferFactory;
import com.sunstrider.common.buffer.impl.DiskBufferFactory;
import com.sunstrider.common.buffer.impl.DiskResourceManager;
import com.sunstrider.common.buffer.impl.LinkedBuffer;
import com.sunstrider.common.buffer.impl.MemoryBufferFactory;
import com.sunstrider.common.buffer.impl.MemoryResourceManager;
import com.sunstrider.common.utils.HexUtils;

public class LinkedBufferTest {

    LinkedBuffer buffer;

    /**
     * �ڴ�ɷ����С
     */
    private int memoryBaseSize = 1024;

    /**
     * ���β��ԵĲ���buffer��С
     */
    private int opBaseSize = 2 * 1024;

    BothBufferFactory instance;

    @Before
    public void setUp() throws Exception {
        BufferConfig config = new BufferConfig() {

            @Override
            public long getMemoryTotalSize() {
                // TODO Auto-generated method stub
                return 10 << 20;
            }

            @Override
            public long getPerAllocLimit() {
                // TODO Auto-generated method stub
                return memoryBaseSize;
            }

            @Override
            public String getDiskHome() {
                // TODO Auto-generated method stub
                return "diskcache";
            }

            @Override
            public long getBaseBufSize() {
                // TODO Auto-generated method stub
                return memoryBaseSize;
            }

            @Override
            public String getPrefix() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public long getDiskTotalSize() {
                // TODO Auto-generated method stub
                return 0;
            }
        };

        Constructor<BothBufferFactory> constructors = BothBufferFactory.class
            .getDeclaredConstructor(null);
        constructors.setAccessible(true);
        instance = constructors.newInstance();
        instance.initialize(config);

        this.buffer = new LinkedBuffer(instance, instance.getBuffer(100));
    }

    @After
    public void tearDown() throws Exception {
        if (this.buffer.isEnable()) {
            this.buffer.clear();
        }
    }

    @Test
    public void testWriteBytes() {
        byte[] src = new byte[opBaseSize];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);

        byte[] dst = new byte[opBaseSize];
        this.buffer.readBytes(dst, 0, dst.length);

        String hexString = HexUtils.toHexString(src);

        String hexString2 = HexUtils.toHexString(dst);
        System.out.println("src=" + hexString);
        System.out.println("dst=" + hexString2);
        assertTrue(hexString.equals(hexString2));

    }

    @Test
    public void testReadBytes() {
        byte[] src = new byte[opBaseSize];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }
        this.buffer.writeBytes(src, 0, src.length);

        byte[] dst = new byte[opBaseSize];
        this.buffer.readBytes(dst, 0, dst.length);

        assertTrue(ByteBuffer.wrap(dst).equals(ByteBuffer.wrap(src)));
    }

    @Test
    public void testReadable() {
        byte[] src = new byte[opBaseSize];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }
        this.buffer.writeBytes(src, 0, src.length);

        assertTrue(this.buffer.readable());
    }

    @Test
    public void testWritable() {
        assertTrue(this.buffer.writable());
    }

    @Test
    public void testWriterIndex() {
        byte[] src = new byte[opBaseSize];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);
        Assert.assertEquals(src.length, this.buffer.writerIndex());
    }

    @Test
    public void testWritableBytes() {
        byte[] src = new byte[opBaseSize];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);

        int writableBytes = this.buffer.writableBytes();
        int capacity = this.buffer.capacity();
        Assert
            .assertEquals(writableBytes, capacity - this.buffer.writerIndex());
    }

    @Test
    public void testReadableBytes() {
        assertTrue(!this.buffer.readable());

        byte[] src = new byte[opBaseSize];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);

        assertTrue(this.buffer.readable());
    }

    @Test
    public void testReaderIndex() {
        assertTrue(!this.buffer.readable());

        byte[] src = new byte[opBaseSize];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);

        assertTrue(this.buffer.readable());

        this.buffer.readBytes(new byte[10], 0, 10);

        Assert.assertEquals(10, this.buffer.readerIndex());
    }

    @Test
    public void testClear() {
        this.buffer.clear();
        Assert.assertEquals(false, this.buffer.isEnable());
    }

}
