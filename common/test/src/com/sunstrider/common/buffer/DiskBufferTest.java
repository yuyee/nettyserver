package com.sunstrider.common.buffer;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sunstrider.common.buffer.Buffer;
import com.sunstrider.common.buffer.BufferConfig;
import com.sunstrider.common.buffer.ResourceManager;
import com.sunstrider.common.buffer.impl.DiskBufferFactory;
import com.sunstrider.common.buffer.impl.DiskResourceManager;

public class DiskBufferTest {
    Buffer buffer;

    @Before
    public void setUp() throws Exception {
        BufferConfig config = new BufferConfig() {

            @Override
            public long getMemoryTotalSize() {
                // TODO Auto-generated method stub
                return 10 * 1024 * 1024;
            }

            @Override
            public long getPerAllocLimit() {
                // TODO Auto-generated method stub
                return 1024 * 1024;
            }

            @Override
            public String getDiskHome() {
                // TODO Auto-generated method stub
                return "diskcache";
            }

            @Override
            public long getBaseBufSize() {
                // TODO Auto-generated method stub
                return 1024;
            }

            @Override
            public String getPrefix() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public long getDiskTotalSize() {
                // TODO Auto-generated method stub
                return 10 << 10;
            }

        };
        Constructor<DiskBufferFactory> constructors = DiskBufferFactory.class
            .getDeclaredConstructor(ResourceManager.class);
        constructors.setAccessible(true);
        DiskBufferFactory instance = constructors
            .newInstance(new DiskResourceManager());
        instance.initialize(config);
        this.buffer = instance.getBuffer(0);
    }

    @After
    public void tearDown() throws Exception {
        if (this.buffer.isEnable()) {
            this.buffer.clear();
        }
    }

    @Test
    public void testWriteBytes() {
        byte[] src = new byte[100];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);

        byte[] dst = new byte[100];
        this.buffer.readBytes(dst, 0, dst.length);

        assertTrue(ByteBuffer.wrap(dst).equals(ByteBuffer.wrap(src)));

    }

    @Test
    public void testReadBytes() {
        byte[] src = new byte[100];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);

        byte[] dst = new byte[100];
        this.buffer.readBytes(dst, 0, dst.length);

        assertTrue(ByteBuffer.wrap(dst).equals(ByteBuffer.wrap(src)));
    }

    @Test
    public void testReadable() {
        byte[] src = new byte[100];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);

        assertTrue(this.buffer.readable());
    }

    @Test
    public void testWritable() {
        assertTrue(this.buffer.writable());

        // �ļ�buffer,�������4G������ⲻ����
    }

    @Test
    public void testWriterIndex() {
        byte[] src = new byte[100];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);
        Assert.assertEquals(src.length, this.buffer.writerIndex());
    }

    @Test
    public void testWritableBytes() {
        byte[] src = new byte[100];
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

        byte[] src = new byte[100];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);

        assertTrue(this.buffer.readable());
    }

    @Test
    public void testReaderIndex() {
        assertTrue(!this.buffer.readable());

        byte[] src = new byte[100];
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
