package com.netease.hmail.common.buffer;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.netease.hmail.common.buffer.impl.DiskBufferFactory;
import com.netease.hmail.common.buffer.impl.DiskResourceManager;
import com.netease.hmail.common.buffer.impl.MemoryBufferFactory;
import com.netease.hmail.common.buffer.impl.MemoryResourceManager;

public class MemoryBufferTest {

    Buffer buffer;

    MemoryBufferFactory instance;

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
                return 0;
            }
        };
        Constructor<MemoryBufferFactory> constructors = MemoryBufferFactory.class
            .getDeclaredConstructor(ResourceManager.class);
        constructors.setAccessible(true);
        instance = constructors.newInstance(new MemoryResourceManager());
        instance.initialize(config);
        this.buffer = instance.getBuffer(100);
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

        this.buffer = instance.getBuffer(100);
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

        this.buffer = instance.getBuffer(100);
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
        this.buffer = instance.getBuffer(100);
        this.buffer.writeBytes(src, 0, src.length);

        assertTrue(this.buffer.readable());
    }

    @Test
    public void testWritable() {
        assertTrue(this.buffer.writable());
        this.buffer = instance.getBuffer(100);

        byte[] src = new byte[this.buffer.capacity()];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);

        assertTrue(!this.buffer.writable());
    }

    @Test
    public void testWriterIndex() {
        this.buffer = instance.getBuffer(100);
        byte[] src = new byte[100];
        for (int i = 0; i < src.length; i++) {
            src[i] = 'a';
        }

        this.buffer.writeBytes(src, 0, src.length);
        Assert.assertEquals(src.length, this.buffer.writerIndex());
    }

    @Test
    public void testWritableBytes() {
        this.buffer = instance.getBuffer(100);
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
        this.buffer = instance.getBuffer(100);
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
        this.buffer = instance.getBuffer(100);
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
        this.buffer = instance.getBuffer(100);
        this.buffer.clear();
        Assert.assertEquals(false, this.buffer.isEnable());
    }

}
