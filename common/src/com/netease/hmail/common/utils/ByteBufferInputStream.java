/**
 * @(#)ByteBufferInputStream.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.utils;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An {@link InputStream} which reads data from a {@link ByteBuffer}.
 * <p>
 * A read operation against this stream will occur at the {@code position} of
 * its underlying buffer and the {@code position} will increase during the read
 * operation.
 * <p>
 * This stream implements {@link DataInput} for your convenience. The endianness
 * of the stream is not always big endian but depends on the endianness of the
 * underlying buffer.
 */
public class ByteBufferInputStream extends InputStream implements DataInput {
    /**
     * 底层ByteBuffer
     */
    private final ByteBuffer buffer;

    /**
     * 起始偏移位置
     */
    private final int startIndex;

    /**
     * 有效字节结束偏移位置
     */
    private final int endIndex;

    /**
     * Creates a new stream which reads data from the specified {@code buffer}
     * starting at the current {@code position} and ending at the current
     * {@code limit}.
     */
    public ByteBufferInputStream(ByteBuffer buffer) {
        this(buffer, buffer.remaining());
    }

    /**
     * Creates a new stream which reads data from the specified {@code buffer}
     * starting at the current {@code position} and ending at
     * {@code position + length}.
     * 
     * @throws IndexOutOfBoundsException
     *             if {@code readerIndex + length} is greater than
     *             {@code writerIndex}
     */
    public ByteBufferInputStream(ByteBuffer buffer, int length) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        if (length > buffer.remaining()) {
            throw new IndexOutOfBoundsException();
        }

        this.buffer = buffer;
        startIndex = buffer.position();
        endIndex = startIndex + length;
        buffer.mark();
    }

    /**
     * Returns the number of read bytes by this stream so far.
     */
    public int readBytes() {
        return buffer.position() - startIndex;
    }

    @Override
    public int available() throws IOException {
        return endIndex - buffer.position();
    }

    @Override
    public void mark(int readlimit) {
        buffer.mark();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        if (!buffer.hasRemaining()) {
            return -1;
        }
        return buffer.get();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int available = available();
        if (available == 0) {
            return -1;
        }

        len = Math.min(available, len);
        buffer.get(b, off, len);
        return len;
    }

    @Override
    public void reset() throws IOException {
        buffer.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        if (n > Integer.MAX_VALUE) {
            return skipBytes(Integer.MAX_VALUE);
        } else {
            return skipBytes((int) n);
        }
    }

    @Override
    public boolean readBoolean() throws IOException {
        checkAvailable(1);
        return read() != 0;
    }

    @Override
    public byte readByte() throws IOException {
        if (!buffer.hasRemaining()) {
            throw new EOFException();
        }
        return buffer.get();
    }

    @Override
    public char readChar() throws IOException {
        return (char) readShort();
    }

    @Override
    public double readDouble() throws IOException {
        return buffer.getDouble();
    }

    @Override
    public float readFloat() throws IOException {
        return buffer.getFloat();
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        checkAvailable(len);
        buffer.get(b, off, len);
    }

    @Override
    public int readInt() throws IOException {
        checkAvailable(4);
        return buffer.getInt();
    }

    /** 按行读取时的字符串构造器实例 */
    private final StringBuilder lineBuf = new StringBuilder();

    @Override
    public String readLine() throws IOException {
        lineBuf.setLength(0);
        for (;;) {
            int b = read();
            if (b < 0 || b == '\n') {
                break;
            }

            lineBuf.append((char) b);
        }

        while (lineBuf.charAt(lineBuf.length() - 1) == '\r') {
            lineBuf.setLength(lineBuf.length() - 1);
        }

        return lineBuf.toString();
    }

    @Override
    public long readLong() throws IOException {
        checkAvailable(8);
        return buffer.getLong();
    }

    @Override
    public short readShort() throws IOException {
        checkAvailable(2);
        return buffer.getShort();
    }

    @Override
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return readByte() & 0xff;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xffff;
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int nBytes = Math.min(available(), n);
        buffer.position(buffer.position() + nBytes);
        return nBytes;
    }

    /**
     * 检查需要读取的数据量是否越界
     * 
     * @param fieldSize
     *            需要读取的数据量长度
     * @throws IOException
     */
    private void checkAvailable(int fieldSize) throws IOException {
        if (fieldSize < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (fieldSize > available()) {
            throw new EOFException();
        }
    }
}
