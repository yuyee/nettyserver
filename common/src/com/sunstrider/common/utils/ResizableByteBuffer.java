/**
 * @(#)ResizableByteBuffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.util.Date;

/**
 * 模仿MINA的IoBuffer, 实现一个可自增长的字节块
 * <p>
 * 可以方便地实现序列化和反序列化
 */
public class ResizableByteBuffer {
    /**
     * We don't have any access to Buffer.markValue(), so we need to track it
     * down, which will cause small extra overhead.
     */
    private int mark = -1;

    /**
     * 底层数据缓存块
     */
    private ByteBuffer buf;

    /**
     * 构造函数, 使用指定的缓存块作为底层数据缓存
     * 
     * @param bb
     *            数据缓存块
     */
    protected ResizableByteBuffer(ByteBuffer bb) {
        this.buf = bb;
    }

    /**
     * 获取底层数据缓存块对象
     * 
     * @return 缓存块对象
     */
    public ByteBuffer buf() {
        return this.buf;
    }

    /**
     * 当前缓存块容量
     * 
     * @return 缓存块容量
     */
    public final int capacity() {
        return buf.capacity();
    }

    /**
     * 设置缓存容量到指定值
     * 
     * @param newCapacity
     *            指定的缓存容量
     * @return 缓存容量为指定值的{@link ResizableByteBuffer}对象
     */
    public final ResizableByteBuffer capacity(int newCapacity) {
        // Allocate a new buffer and transfer all settings to it.
        if (newCapacity > capacity()) {
            // Expand:
            // // Save the state.
            int pos = position();
            int limit = limit();

            // // Reallocate.
            ByteBuffer oldBuf = this.buf;
            ByteBuffer newBuf = allocateNioBuffer(newCapacity, isDirect());
            oldBuf.clear();
            newBuf.put(oldBuf);
            this.buf = newBuf;

            // // Restore the state.
            this.buf.limit(limit);
            if (mark >= 0) {
                this.buf.position(mark);
                this.buf.mark();
            }
            this.buf.position(pos);
        }

        return this;
    }

    /**
     * {@linkplain ByteBuffer#isDirect()}
     */
    public final boolean isDirect() {
        return buf.isDirect();
    }

    /**
     * {@linkplain ByteBuffer#array()}
     */
    public final byte[] array() {
        return buf.array();
    }

    /**
     * {@linkplain ByteBuffer#arrayOffset()}
     */
    public final int arrayOffset() {
        return buf.arrayOffset();
    }

    /**
     * {@linkplain ByteBuffer#position()}
     */
    public final int position() {
        return buf.position();
    }

    /**
     * {@linkplain ByteBuffer#position(int)}
     */
    public final ResizableByteBuffer position(int newPosition) {
        autoExpand(newPosition, 0);
        buf.position(newPosition);
        if (mark > newPosition) {
            mark = -1;
        }
        return this;
    }

    /**
     * {@linkplain ByteBuffer#limit()}
     */
    public final int limit() {
        return buf.limit();
    }

    /**
     * {@linkplain ByteBuffer#limit(int)}
     */
    public final ResizableByteBuffer limit(int newLimit) {
        autoExpand(newLimit, 0);
        buf.limit(newLimit);
        if (mark > newLimit) {
            mark = -1;
        }
        return this;
    }

    /**
     * {@linkplain ByteBuffer#mark()}
     */
    public final ResizableByteBuffer mark() {
        buf.mark();
        mark = position();
        return this;
    }

    /**
     * {@linkplain ByteBuffer#markValue()}
     */
    public final int markValue() {
        return mark;
    }

    /**
     * {@linkplain ByteBuffer#skip(int)}
     */
    public final ResizableByteBuffer skip(int size) {
        autoExpand(size);
        return position(position() + size);
    }

    /**
     * {@linkplain ByteBuffer#reset()}
     */
    public final ResizableByteBuffer reset() {
        buf.reset();
        return this;
    }

    /**
     * {@linkplain ByteBuffer#clear()}
     */
    public final ResizableByteBuffer clear() {
        buf.clear();
        mark = -1;
        return this;
    }

    /**
     * {@linkplain ByteBuffer#flip()}
     */
    public final ResizableByteBuffer flip() {
        buf.flip();
        mark = -1;
        return this;
    }

    /**
     * {@linkplain ByteBuffer#rewind()}
     */
    public final ResizableByteBuffer rewind() {
        buf.rewind();
        mark = -1;
        return this;
    }

    /**
     * {@linkplain ByteBuffer#remaining()}
     */
    public final int remaining() {
        return limit() - position();
    }

    /**
     * {@linkplain ByteBuffer#hasArray()}
     */
    public final boolean hasArray() {
        return buf.hasArray();
    }

    /**
     * {@linkplain ByteBuffer#hasRemaining()}
     */
    public final boolean hasRemaining() {
        return limit() > position();
    }

    /**
     * {@linkplain ByteBuffer#get()}
     */
    public final byte get() {
        return buf.get();
    }

    /**
     * {@linkplain ByteBuffer#get(int)}
     */
    public final byte get(int index) {
        return buf.get(index);
    }

    /**
     * {@linkplain ByteBuffer#get(byte[])}
     */
    public ResizableByteBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    /**
     * {@linkplain ByteBuffer#get(byte[], int, int)}
     */
    public final ResizableByteBuffer get(byte[] dst, int offset, int length) {
        buf.get(dst, offset, length);
        return this;
    }

    /**
     * {@linkplain ByteBuffer#getShort()}
     */
    public final short getShort() {
        return buf.getShort();
    }

    /**
     * {@linkplain ByteBuffer#getInt()}
     */
    public final int getInt() {
        return buf.getInt();
    }

    /**
     * 读取Integer对象
     * <p>
     * 如果读到的数值为-1, 则返回null
     * 
     * @return
     * @see #putLongObject(Long)
     */
    public final Integer getIntObject() {
        int i = buf.getInt();
        return (i == -1 ? null : i);
    }

    /**
     * {@linkplain ByteBuffer#getLong()}
     */
    public final long getLong() {
        return buf.getLong();
    }

    /**
     * 读取Long对象
     * <p>
     * 如果读到的数值为-1, 则返回null
     * 
     * @return
     * @see #putLongObject(Long)
     */
    public final Long getLongObject() {
        long l = buf.getLong();
        return (l == -1 ? null : l);
    }

    /**
     * {@linkplain ByteBuffer#getFloat()}
     */
    public final float getFloat() {
        return buf.getFloat();
    }

    /**
     * {@linkplain ByteBuffer#getDouble()}
     */
    public final double getDouble() {
        return buf.getDouble();
    }

    /**
     * 读取当前位置开始的1个字节, 并以boolean类型值返回. 当前位置向后移动1个字节
     * 
     * @return boolean值
     */
    public final boolean getBoolean() {
        return (buf.get() == 1 ? true : false);
    }

    /**
     * 读取当前位置开始的8个字节, 并以Date类型值返回. 当前位置向后移动8个字节
     * <p>
     * 注意: 如果读取到的数值为-1, 则返回null
     * 
     * @return Date对象
     */
    public Date getDate() {
        long t = buf.getLong();
        if (t == -1) {
            return null;
        }

        return (new Date(t));
    }

    /**
     * 从缓存块中读取一个字符串
     * 
     * @param cs
     *            所用字符集
     * @return 字符串对象
     */
    public String getString(Charset cs) throws CharacterCodingException {
        return getString(cs.newDecoder());
    }

    /**
     * 从缓存块中读取一个字符串
     * 
     * @param decoder
     *            所用字符集解码器
     * @return 字符串对象
     */
    public String getString(CharsetDecoder decoder)
        throws CharacterCodingException {
        int length = buf.getInt();

        if (length < 0) {
            return null;
        }
        if (length == 0) {
            return "";
        }

        int oldLimit = buf.limit();
        int end = buf.position() + length;

        if (oldLimit < end) {
            throw new BufferUnderflowException();
        }

        limit(end);
        decoder.reset();

        int en = (int) (length * (double) decoder.maxCharsPerByte());
        char[] ca = new char[en];
        CharBuffer cb = CharBuffer.wrap(ca);
        CoderResult cr = decoder.decode(buf, cb, true);
        if (!cr.isUnderflow()) {
            cr.throwException();
        }
        cr = decoder.flush(cb);
        if (!cr.isUnderflow()) {
            cr.throwException();
        }

        limit(oldLimit);
        position(end);

        return cb.flip().toString();
    }

    /**
     * {@linkplain ByteBuffer#put(byte)}
     */
    public final ResizableByteBuffer put(byte b) {
        autoExpand(1);
        buf.put(b);
        return this;
    }

    /**
     * {@linkplain ByteBuffer#put(byte[])}
     */
    public ResizableByteBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }

    /**
     * {@linkplain ByteBuffer#put(byte[], int, int)}
     */
    public final ResizableByteBuffer put(byte[] src, int offset, int length) {
        autoExpand(length);
        buf.put(src, offset, length);
        return this;
    }

    /**
     * {@linkplain ByteBuffer#put(ByteBuffer)}
     */
    public final ResizableByteBuffer put(ByteBuffer src) {
        autoExpand(src.remaining());
        buf.put(src);
        return this;
    }

    /**
     * {@linkplain ByteBuffer#putShort(short)}
     */
    public final ResizableByteBuffer putShort(short value) {
        autoExpand(2);
        buf.putShort(value);
        return this;
    }

    /**
     * {@linkplain ByteBuffer#putShort(int, short)}
     */
    public final ResizableByteBuffer putShort(int index, short value) {
        autoExpand(index, 2);
        buf.putShort(index, value);
        return this;
    }

    /**
     * {@linkplain ByteBuffer#putInt(int)}
     */
    public final ResizableByteBuffer putInt(int value) {
        autoExpand(4);
        buf.putInt(value);
        return this;
    }

    /**
     * {@linkplain ByteBuffer#putInt(int, int)}
     */
    public final ResizableByteBuffer putInt(int index, int value) {
        autoExpand(index, 4);
        buf.putInt(index, value);
        return this;
    }

    /**
     * 如果value == null, 则填入-1值
     * 
     * @param value
     * @return
     * @see #getIntObject()
     */
    public final ResizableByteBuffer putIntObject(Integer value) {
        return putInt(value == null ? -1 : value.intValue());
    }

    /**
     * {@linkplain ByteBuffer#putLong(long)}
     */
    public final ResizableByteBuffer putLong(long value) {
        autoExpand(8);
        buf.putLong(value);
        return this;
    }

    /**
     * {@linkplain ByteBuffer#putLong(int, long)}
     */
    public final ResizableByteBuffer putLong(int index, long value) {
        autoExpand(index, 8);
        buf.putLong(index, value);
        return this;
    }

    /**
     * 如果value == null, 则填入-1值
     * 
     * @param value
     * @return
     * @see #getLongObject()
     */
    public final ResizableByteBuffer putLongObject(Long value) {
        return putLong(value == null ? -1 : value.longValue());
    }

    /**
     * {@linkplain ByteBuffer#putFloat(float)}
     */
    public final ResizableByteBuffer putFloat(float value) {
        autoExpand(4);
        buf.putFloat(value);
        return this;
    }

    /**
     * {@linkplain ByteBuffer#putDouble(double)}
     */
    public final ResizableByteBuffer putDouble(double value) {
        autoExpand(8);
        buf.putDouble(value);
        return this;
    }

    /**
     * 将boolean值转换成1个字节, 并保存到本缓存块内
     * 
     * @param b
     *            boolean值
     * @return 本缓存块对象
     */
    public final ResizableByteBuffer putBoolean(boolean b) {
        autoExpand(1);
        buf.put((byte) (b ? 1 : 0));
        return this;
    }

    /**
     * 支持保存为null值的日期参数
     * <p>
     * 注意: {@link Date#getTime()}值为-1时，被视为等同于{@code dt}参数为null
     * 
     * @param dt
     *            日期对象
     * @return 本缓存块对象
     */
    public ResizableByteBuffer putDate(Date dt) {
        autoExpand(8);
        buf.putLong((dt == null ? -1 : dt.getTime()));
        return this;
    }

    /**
     * 支持保存为null值的字符串参数
     * 
     * @param val
     *            字符串
     * @param encoder
     *            字符集编码器
     * @return 本缓存块对象
     * @throws CharacterCodingException
     */
    public ResizableByteBuffer putString(CharSequence val,
        CharsetEncoder encoder) throws CharacterCodingException {
        if (val == null) {
            return putInt(-1);
        }

        int oldPos = this.position();
        putInt(0); // 初始化长度
        if (val.length() == 0) {
            return this;
        }

        CharBuffer in = CharBuffer.wrap(val);
        encoder.reset();

        int expandedState = 0;
        int startPos = this.position();
        for (;;) {
            CoderResult cr;
            if (in.hasRemaining()) {
                cr = encoder.encode(in, buf, true);
            } else {
                cr = encoder.flush(buf);
            }

            if (cr.isUnderflow()) {
                break;
            }
            if (cr.isOverflow()) {
                switch (expandedState) {
                    case 0:
                        autoExpand((int) Math.ceil(in.remaining()
                            * encoder.averageBytesPerChar()));
                        expandedState++;
                        break;
                    case 1:
                        autoExpand((int) Math.ceil(in.remaining()
                            * encoder.maxBytesPerChar()));
                        expandedState++;
                        break;
                    default:
                        throw new RuntimeException("Expanded by "
                            + (int) Math.ceil(in.remaining()
                                * encoder.maxBytesPerChar())
                            + " but that wasn't enough for '" + val + "'");
                }
                continue;
            } else {
                expandedState = 0;
            }
            cr.throwException();
        }

        // 更新长度
        this.buf.putInt(oldPos, (this.position() - startPos));

        return this;
    }

    /**
     * 将指定输入流中的所有数据转储到当前缓冲块中
     * 
     * @param in
     *            输入流对象
     * @return 本缓存块对象
     * @throws IOException
     */
    public ResizableByteBuffer dump(InputStream in) throws IOException {
        final int segSize = 16 * 1024;

        if (this.hasArray()) { // 底层是数组结构
            this.expand(segSize, true);

            int count = -1;
            while ((count = in.read(buf.array(),
                buf.arrayOffset() + buf.position(), buf.remaining())) != -1) {
                buf.position(buf.position() + count);
                this.expand(segSize, true);
            }

        } else {
            byte[] ba = new byte[segSize];
            int count = -1;
            while ((count = in.read(ba)) != -1) {
                this.put(ba, 0, count);
            }
        }

        return this;
    }

    /**
     * 获取当前缓存块中的有效数据副本, 并以字节数组的格式返回
     * 
     * @return 数据副本
     */
    public byte[] getAsBytes() {
        if (!this.hasRemaining()) {
            return new byte[0];
        }

        byte[] b = new byte[this.buf.remaining()];
        this.buf.get(b);
        return b;
    }

    /**
     * 将缓存区内的字节数据全部转换为字符串
     * 
     * @param cs
     *            字符集名称
     * @return 字符串
     * @throws CharacterCodingException
     */
    public String asString(Charset cs) throws CharacterCodingException {
        return asString(cs, false);
    }

    /**
     * 将缓存区内的字节数据全部转换为字符串
     * 
     * @param cs
     *            字符集名称
     * @param stopOnMalformedInput
     *            true=一旦解码过程中产生的{@link MalformedInputException}异常则停止解码,
     *            但不会抛出异常, 只返回已成功解码的字符串;<br>
     *            false=正常抛出 {@link MalformedInputException}异常
     * @return 字符串
     * @throws CharacterCodingException
     */
    public String asString(Charset cs, boolean stopOnMalformedInput)
        throws CharacterCodingException {
        int length = buf.remaining();
        if (length == 0) {
            return "";
        }

        CharsetDecoder cd = cs.newDecoder();
        int en = (int) (length * cd.maxCharsPerByte());
        char[] ca = new char[en];
        CharBuffer cb = CharBuffer.wrap(ca);

        cd.reset();
        cd.onMalformedInput(CodingErrorAction.REPORT);

        CoderResult cr = cd.decode(this.buf, cb, true);
        if (!cr.isUnderflow()) {
            if (!cr.isMalformed() || !stopOnMalformedInput) {
                cr.throwException();
            }
        }

        cr = cd.flush(cb);
        if (!cr.isUnderflow()) {
            if (!cr.isMalformed() || !stopOnMalformedInput) {
                cr.throwException();
            }
        }

        buf.position(buf.limit());
        return cb.flip().toString();
    }

    /**
     * 将缓存区内的字节数据全部转换为字符串
     * <p>
     * 如果解码中遇到非法字符，则直接调用{@link CodingErrorAction#REPLACE}动作
     * 
     * @param cs
     *            字符集名称
     * @return 字符串
     */
    public String asStringReplaceMalformed(Charset cs) {
        int length = buf.remaining();
        if (length == 0) {
            return "";
        }

        CharsetDecoder cd = cs.newDecoder();
        int en = (int) (length * cd.maxCharsPerByte());
        char[] ca = new char[en];
        CharBuffer cb = CharBuffer.wrap(ca);

        cd.reset();
        cd.onMalformedInput(CodingErrorAction.REPLACE);

        cd.decode(this.buf, cb, true);
        cd.flush(cb);

        buf.position(buf.limit());
        return cb.flip().toString();
    }

    /**
     * 以{@link OutputStream}接口访问本缓存块
     * 
     * @return 本缓存块的输出流接口对象
     */
    public OutputStream asOutputStream() {
        return new OutputStream() {
            @Override
            public void write(byte[] b, int off, int len) {
                ResizableByteBuffer.this.put(b, off, len);
            }

            @Override
            public void write(int b) {
                ResizableByteBuffer.this.put((byte) b);
            }
        };
    }

    /**
     * 以{@link InputStream}接口访问本缓存块
     * 
     * @return 本缓存块的输入流接口对象
     */
    public InputStream asInputStream() {
        return new InputStream() {
            @Override
            public int available() {
                return ResizableByteBuffer.this.remaining();
            }

            @Override
            public synchronized void mark(int readlimit) {
                ResizableByteBuffer.this.mark();
            }

            @Override
            public boolean markSupported() {
                return true;
            }

            @Override
            public int read() {
                if (ResizableByteBuffer.this.hasRemaining()) {
                    return ResizableByteBuffer.this.get() & 0xff;
                }

                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                int remaining = ResizableByteBuffer.this.remaining();
                if (remaining > 0) {
                    int readBytes = Math.min(remaining, len);
                    ResizableByteBuffer.this.get(b, off, readBytes);
                    return readBytes;
                }

                return -1;
            }

            @Override
            public synchronized void reset() {
                ResizableByteBuffer.this.reset();
            }

            @Override
            public long skip(long n) {
                int bytes;
                if (n > Integer.MAX_VALUE) {
                    bytes = ResizableByteBuffer.this.remaining();
                } else {
                    bytes = Math.min(ResizableByteBuffer.this.remaining(),
                        (int) n);
                }
                ResizableByteBuffer.this.skip(bytes);
                return bytes;
            }
        };
    }

    /**
     * This method forwards the call to {@link #expand(int)} only when
     * <tt>autoExpand</tt> property is <tt>true</tt>.
     */
    protected ResizableByteBuffer autoExpand(int expectedRemaining) {
        return expand(expectedRemaining, true);
    }

    /**
     * This method forwards the call to {@link #expand(int)} only when
     * <tt>autoExpand</tt> property is <tt>true</tt>.
     */
    protected ResizableByteBuffer autoExpand(int pos, int expectedRemaining) {
        return expand(pos, expectedRemaining, true);
    }

    private ResizableByteBuffer expand(int expectedRemaining, boolean autoExpand) {
        return expand(position(), expectedRemaining, autoExpand);
    }

    private ResizableByteBuffer expand(int pos, int expectedRemaining,
        boolean autoExpand) {
        int end = pos + expectedRemaining;
        int newCapacity;
        if (autoExpand) {
            newCapacity = ResizableByteBuffer.normalizeCapacity(end);
        } else {
            newCapacity = end;
        }
        if (newCapacity > capacity()) {
            // The buffer needs expansion.
            capacity(newCapacity);
        }

        if (end > limit()) {
            // We call limit() directly to prevent StackOverflowError
            buf.limit(end);
        }
        return this;
    }

    private static ByteBuffer allocateNioBuffer(int capacity, boolean direct) {
        ByteBuffer nioBuffer;
        if (direct) {
            nioBuffer = ByteBuffer.allocateDirect(capacity);
        } else {
            nioBuffer = ByteBuffer.allocate(capacity);
        }
        return nioBuffer;
    }

    /**
     * Normalizes the specified capacity of the buffer to power of 2, which is
     * often helpful for optimal memory usage and performance. If it is greater
     * than or equal to {@link Integer#MAX_VALUE}, it returns
     * {@link Integer#MAX_VALUE}. If it is zero, it returns zero.
     */
    protected static int normalizeCapacity(int requestedCapacity) {
        if (requestedCapacity < 0) {
            return Integer.MAX_VALUE;
        }

        int newCapacity = Integer.highestOneBit(requestedCapacity);
        newCapacity <<= (newCapacity < requestedCapacity ? 1 : 0);
        return newCapacity < 0 ? Integer.MAX_VALUE : newCapacity;
    }

    /**
     * 创建指定容量的数据缓存对象
     * 
     * @param capacity
     *            起始容量
     * @param direct
     *            true=底层使用direct类型缓存; false=底层使用普通缓存
     * @return 缓存对象
     */
    public static ResizableByteBuffer allocate(int capacity, boolean direct) {
        return wrap(allocateNioBuffer(capacity, direct));
    }

    /**
     * 使用指定字节数组作为起始数据缓存区，创建缓存对象
     * 
     * @param array
     *            字节数组
     * @return 缓存对象
     */
    public static ResizableByteBuffer wrap(byte[] array) {
        return new ResizableByteBuffer(ByteBuffer.wrap(array));
    }

    /**
     * 使用指定字节数组作为起始数据缓存区，创建缓存对象
     * 
     * @param array
     *            字节数组
     * @param offset
     *            数组可用于缓存的起始偏移量
     * @param length
     *            数组可用于缓存的字节长度
     * @return 缓存对象
     */
    public static ResizableByteBuffer wrap(byte[] array, int offset, int length) {
        return new ResizableByteBuffer(ByteBuffer.wrap(array, offset, length));
    }

    /**
     * 使用指定{@link ByteBuffer}作为底层缓存块，创建缓存对象
     * 
     * @param nioBuffer
     *            底层缓存
     * @return 缓存对象
     */
    public static ResizableByteBuffer wrap(ByteBuffer nioBuffer) {
        return new ResizableByteBuffer(nioBuffer);
    }

}
