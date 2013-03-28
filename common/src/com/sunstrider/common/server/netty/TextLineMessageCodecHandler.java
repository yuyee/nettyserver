package com.sunstrider.common.server.netty;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文本行消息编解码器
 * <p>
 * 注意: 必须一个{@link Channel}对应一个实例，否则会出现异常
 */
public class TextLineMessageCodecHandler extends CodecStreamHandler {

    /**
     * 当接收到的行数据超长时的处理策略枚举类
     * 
     * @author qiu_sheng
     */
    public enum OverLengthPolicy {
        /** 发现超长行, 丢弃数据并抛出异常 */
        THROW_EXCEPTION,
        /** 发现超长行, 自动丢弃 */
        DISCARD_QUIETLY,
        /** 发现超长行, 强制截断为一行 */
        FORCE_SPLIT
    }

    /** 行结束符CRLF: \r\n */
    public static final String LINE_DELIMITER_CRLF = "\r\n";

    /** 行结束符CR: \r */
    public static final String LINE_DELIMITER_CR = "\r";

    /** 行结束符LF: \n */
    public static final String LINE_DELIMITER_LF = "\n";

    /** 字符集 */
    private final Charset charset;

    /** 行结束符按指定字符集编码后的字节数组 */
    private final byte[] delimiter;

    /** 最大行长度 */
    private final int maxLineLength;

    /**
     * 当接收到的行数据超长时的处理策略
     */
    private final OverLengthPolicy policy;

    /**
     * 返回字符行时是否包含结尾符号
     * <p>
     * 主要用于后端处理时能够判断是否因为单行数据过长而出现的强制分行情况
     */
    private final boolean includeDelimiter;

    /**
     * 是否正在丢弃超长的帧数据
     */
    private boolean discardingTooLongFrame = false;

    /**
     * 超长帧的长度
     */
    private int tooLongFrameLength = 0;

    /**
     * 最后一次探测到数据长度
     */
    private int lastDetectedLength = 0;

    /**
     * 将行结束的单个LF统一转换为CRLF
     */
    private boolean convertLF2CRLF = false;

    /**
     * 当前字符集下的CR字符对应字节
     */
    private byte[] crBytes = null;

    /**
     * 当前字符集下的CRLF字符对应字节
     */
    private byte[] crlfBytes = null;

    /**
     * 最后一次为长度超限后强制切分产生的帧
     */
    protected boolean forceSplitted = false;

    /** 日志记录 */
    private static final Logger LOG = LoggerFactory
        .getLogger(TextLineMessageCodecHandler.class);

    /**
     * 构造函数
     * <p>
     * 默认使用UTF-8字符集, 行结束符为{@link #LINE_DELIMITER_CRLF}
     * 
     * @param maxFrameLength
     *            单行字节数上限
     */
    public TextLineMessageCodecHandler(int maxFrameLength) {
        this(maxFrameLength, CharsetUtil.UTF_8, LINE_DELIMITER_CRLF, null,
            false);
    }

    /**
     * 构造函数
     * <p>
     * 默认使用行结束符为{@link #LINE_DELIMITER_CRLF}
     * 
     * @param maxFrameLength
     *            单行字节数上限
     * @param cs
     *            字符集
     */
    public TextLineMessageCodecHandler(int maxFrameLength, Charset cs) {
        this(maxFrameLength, cs, LINE_DELIMITER_CRLF, null, false);
    }

    /**
     * 构造函数
     * 
     * @param maxLineLength
     *            单行字节数上限
     * @param cs
     *            字符集
     * @param lineDelimiter
     *            文本行结束字符串
     * @param policy
     *            处理超长行数据时的策略(null=默认使用{@link OverLengthPolicy#THROW_EXCEPTION})
     * @param includeDelimiter
     *            返回字符行时是否包含结尾符号
     */
    public TextLineMessageCodecHandler(int maxLineLength, Charset cs,
        String lineDelimiter, OverLengthPolicy policy, boolean includeDelimiter) {
        if (cs == null) {
            throw new NullPointerException("charset is null");
        }
        this.charset = cs;

        if (lineDelimiter == null || lineDelimiter.isEmpty()) {
            throw new NullPointerException("lineDelimiter is null or empty");
        }
        this.delimiter = lineDelimiter.getBytes(cs);

        if (policy == null) {
            this.policy = OverLengthPolicy.THROW_EXCEPTION;
        } else {
            this.policy = policy;
        }

        this.includeDelimiter = includeDelimiter;
        if (!this.includeDelimiter) {
            this.maxLineLength = maxLineLength + this.delimiter.length;
        } else {
            this.maxLineLength = maxLineLength;
        }
    }

    /**
     * @param maxLineLength
     * @param cs
     * @param policy
     * @param convertLF2CRLF
     */
    public TextLineMessageCodecHandler(int maxLineLength, Charset cs,
        boolean convertLF2CRLF, OverLengthPolicy policy) {
        if (cs == null) {
            throw new NullPointerException("charset is null");
        }
        this.charset = cs;

        this.convertLF2CRLF = convertLF2CRLF;
        if (this.convertLF2CRLF) {
            this.delimiter = LINE_DELIMITER_LF.getBytes(cs);
            this.crBytes = LINE_DELIMITER_CR.getBytes(cs);
            this.crlfBytes = LINE_DELIMITER_CRLF.getBytes(cs);
        } else {
            this.delimiter = LINE_DELIMITER_CRLF.getBytes(cs);
            this.crlfBytes = this.delimiter;
        }

        if (policy == null) {
            this.policy = OverLengthPolicy.THROW_EXCEPTION;
        } else {
            this.policy = policy;
        }

        this.includeDelimiter = false;
        this.maxLineLength = maxLineLength
            + LINE_DELIMITER_CRLF.getBytes(cs).length;
    }

    public Charset getCharset() {
        return charset;
    }

    public byte[] getDelimiter() {
        return (convertLF2CRLF ? this.crlfBytes : delimiter);
    }

    /**
     * @return the convertLF2CRLF
     */
    public boolean isConvertLF2CRLF() {
        return convertLF2CRLF;
    }

    /**
     * @return the policy
     */
    public OverLengthPolicy getPolicy() {
        return policy;
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, Channel channel,
        ChannelBuffer buffer) throws Exception {

        ChannelBuffer frame = this.decodeFrameBuffer(channel, buffer);
        if (frame == null) {
            return null;
        }

        return frame.toString(charset);
    }

    /**
     * 解码出一帧的字节数据
     * 
     * @param channel
     * @param buffer
     * @param codecContext
     * @return
     * @throws Exception
     */
    protected ChannelBuffer decodeFrameBuffer(Channel channel,
        ChannelBuffer buffer) throws Exception {
        // 重置状态
        this.forceSplitted = false;

        if (!buffer.readable())
            return null;

        // 保存当前读取index
        buffer.markReaderIndex();
        // 跳过上次已经检测过的字节, 避免重复检测
        buffer.readerIndex(buffer.readerIndex() + this.lastDetectedLength);

        // 本次检测结果的帧长度
        int currFrameLength = indexOf(buffer, this.delimiter);
        // 保存当前检测后的读取index
        int detectedIndex = buffer.readerIndex();

        // 恢复读取index
        buffer.resetReaderIndex();

        if (currFrameLength >= 0) { // 发现帧结束符
            // 追加上一次检测未通过时的字节长度
            int frameLength = this.lastDetectedLength + currFrameLength;
            int delimLength = delimiter.length;

            if (this.isConvertLF2CRLF() && crBytes != null
                && detectedIndex >= crBytes.length) {
                boolean hitCr;
                if (crBytes != null && crBytes.length == 1) {
                    hitCr = (buffer.getByte(detectedIndex - 1) == crBytes[0]);
                } else if (crBytes != null && crBytes.length > 1) {
                    hitCr = true;
                    for (int i = 0, j = detectedIndex - crBytes.length; i < crBytes.length; i++, j++) {
                        if (crBytes[i] != buffer.getByte(j)) {
                            hitCr = false;
                            break;
                        }
                    }
                } else {
                    hitCr = false;
                }

                if (hitCr) {
                    frameLength -= crBytes.length;
                    delimLength += crBytes.length;
                }
            }

            ChannelBuffer frame;

            this.lastDetectedLength = 0; // 重置未通过检测的字节长度

            if (this.discardingTooLongFrame) { // 当policy为强制切分时,discardingTooLongFrame永远不可能为true
                // We've just finished discarding a very large frame.
                // Go back to the initial state.
                this.discardingTooLongFrame = false;
                buffer.skipBytes(frameLength + delimLength);

                int tooLongFrameLength = this.tooLongFrameLength;
                this.tooLongFrameLength = 0;
                fail(tooLongFrameLength);
                return null;
            }

            if (frameLength > this.maxLineLength) {
                if (policy == OverLengthPolicy.FORCE_SPLIT) {
                    this.forceSplitted = true;
                    frame = buffer.readBytes(maxLineLength);
                    return frame;
                } else {
                    // Discard read frame.
                    buffer.skipBytes(frameLength + delimLength);
                    fail(frameLength);
                    return null;
                }
            }

            if (this.includeDelimiter) {
                frame = buffer.readBytes(frameLength + delimLength);
            } else {
                frame = buffer.readBytes(frameLength);
                buffer.skipBytes(delimLength);
            }

            return frame;
        } else {
            // 计算未通过检测的字节长度
            this.lastDetectedLength = detectedIndex - buffer.readerIndex();

            if (!this.discardingTooLongFrame) {
                if (buffer.readableBytes() > this.maxLineLength) {

                    if (policy == OverLengthPolicy.FORCE_SPLIT) {
                        this.forceSplitted = true;
                        ChannelBuffer frame = buffer.readBytes(maxLineLength);
                        this.lastDetectedLength = 0;
                        return frame;
                    } else {
                        // Discard the content of the buffer until a delimiter
                        // is found.
                        this.tooLongFrameLength = buffer.readableBytes();
                        buffer.skipBytes(buffer.readableBytes());
                        this.lastDetectedLength = 0; // 数据被废弃, 重置未通过检测的字节长度
                        this.discardingTooLongFrame = true;
                    }
                }
            } else {
                // Still discarding the buffer since a delimiter is not found.
                this.tooLongFrameLength += buffer.readableBytes();
                buffer.skipBytes(buffer.readableBytes());
                this.lastDetectedLength = 0; // 数据被废弃, 重置未通过检测的字节长度
            }

            return null;
        }
    }

    /**
     * 解码出现超长错误
     * 
     * @param frameLength
     * @throws Exception
     */
    private void fail(long frameLength) throws Exception {
        Exception ex;
        if (frameLength > 0) {
            ex = new TooLongFrameException("frame length exceeds "
                + this.maxLineLength + ": " + frameLength + " - discarded");
        } else {
            ex = new TooLongFrameException("frame length exceeds "
                + this.maxLineLength + " - discarding");
        }

        if (policy == OverLengthPolicy.THROW_EXCEPTION) {
            throw ex;
        } else {
            if (LOG.isTraceEnabled()) {
                LOG.trace(ex.toString(), ex);
            }
            LOG.info(ex.toString());
        }
    }

    /**
     * 如果{@code needle}存在于{@code haystack}中, 则返回一个非负数的长度, 标记本次检测过程中发现的帧长度
     * <p>
     * 注意: 本方法将修改{@code haystack.readerIndex()}值:
     * <li>如果needle串不存在于haystack中, 则新的readerIndex = writerIndex
     * <li>如果needle串存在于haystack中, 则新的readerIndex =
     * haystack中第一个符合needle串的起始偏移量(即needle[0]对应在haystack中的数据偏移量)
     * <li>如果haystack长度不足, 但结尾部分的数据吻合needle串的起始字节数据顺序, 则新的readerIndex =
     * haystack尾部吻合needle起始部分数据串的起始偏移量
     */
    private static int indexOf(ChannelBuffer haystack, byte[] needle) {
        for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i++) {
            int haystackIndex = i;
            int needleIndex;

            for (needleIndex = 0; needleIndex < needle.length; needleIndex++) {
                if (haystack.getByte(haystackIndex) != needle[needleIndex]) {
                    break;
                } else {
                    haystackIndex++;
                    if (haystackIndex == haystack.writerIndex()
                        && needleIndex != needle.length - 1) {

                        haystack.readerIndex(i);
                        return -1;
                    }
                }
            }

            if (needleIndex == needle.length) {
                // Found the needle from the haystack!
                int length = i - haystack.readerIndex();
                haystack.readerIndex(i);
                return length;
            }
        }

        haystack.readerIndex(haystack.writerIndex());
        return -1;
    }

    @Override
    public ChannelBuffer encode(ChannelHandlerContext ctx, Channel channel,
        Object msg) throws Exception {
        if (msg == null) {
            throw new NullPointerException("message");
        }

        if (!(msg instanceof CharSequence)) {
            throw new ClassCastException("msg is not instance of charsequence");
        }

        return ChannelBuffers.wrappedBuffer(encodeString(CharBuffer
            .wrap((CharSequence) msg), (this.convertLF2CRLF ? this.crlfBytes
            : this.delimiter), this.charset));
    }

    /**
     * 将指定字符串编码成字节数据块, 并在末尾附加上指定的字节数据
     * 
     * @param src
     *            字符串
     * @param additional
     *            末尾字节数据
     * @param charset
     *            字符集
     * @return 编码后的字节数据块
     */
    public static ByteBuffer encodeString(CharBuffer src, byte[] additional,
        Charset charset) {
        final CharsetEncoder encoder = CharsetUtil.getEncoder(charset);
        final ByteBuffer dst = ByteBuffer.allocate((int) ((double) src
            .remaining() * encoder.maxBytesPerChar()) + additional.length);
        try {
            CoderResult cr = encoder.encode(src, dst, true);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            cr = encoder.flush(dst);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
        } catch (CharacterCodingException x) {
            throw new IllegalStateException(x);
        }

        dst.put(additional);
        dst.flip();
        return dst;
    }

}
