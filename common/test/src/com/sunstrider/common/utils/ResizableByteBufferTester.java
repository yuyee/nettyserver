package com.sunstrider.common.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import org.junit.Test;

import com.git.original.common.utils.HexUtils;
import com.git.original.common.utils.ResizableByteBuffer;

public class ResizableByteBufferTester {

    @Test
    public void testString() throws CharacterCodingException {
        ResizableByteBuffer bb = ResizableByteBuffer.allocate(10, false);

        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        bb.putString("test1", encoder);

        bb.flip();
        byte[] b = bb.getAsBytes();
        System.out.println(HexUtils.toHexString(b, 0, b.length, ","));

    }

    @Test
    public void testStream() throws Exception {
        ResizableByteBuffer bb = ResizableByteBuffer.allocate(1000, false);

        OutputStream out = bb.asOutputStream();
        out.write("哈哈哈".getBytes());
        out.write("\r\n".getBytes());
        out.write((byte) '1');

        bb.flip();

        InputStream in = bb.asInputStream();
        byte[] ba = new byte[bb.remaining()];
        in.read(ba, 0, ba.length);

        System.out.write(ba);
    }

}
