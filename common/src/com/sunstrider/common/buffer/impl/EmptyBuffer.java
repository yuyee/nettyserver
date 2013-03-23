package com.sunstrider.common.buffer.impl;

import java.io.OutputStream;

import com.sunstrider.common.buffer.Buffer;
import com.sunstrider.common.buffer.BufferConfig;
import com.sunstrider.common.buffer.BufferLimitException;

public class EmptyBuffer extends AbstractBuffer {
    private static BaseBufferFactory emptyBufferFactory = new BaseBufferFactory() {

        @Override
        public void initialize(BufferConfig config) {
            // TODO Auto-generated method stub

        }

        @Override
        public Buffer getBuffer(int capacity) throws BufferLimitException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Buffer getBuffer(byte[] array, int offset, int length)
            throws BufferLimitException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        void freeBuffer(AbstractBuffer buffer) {
            // TODO Auto-generated method stub

        }

    }

    ;

    public EmptyBuffer() {
        super(emptyBufferFactory);
        this.readerIndex = 0;
        this.writerIndex = 0;
    }

    @Override
    protected void free() {
        // TODO Auto-generated method stub

    }

    @Override
    protected int getBytes(int index, byte[] dst, int dstIndex, int length) {
        return 0;
    }

    @Override
    protected void setBytes(int index, byte[] src, int srcIndex, int length) {}

    @Override
    protected long transferTo(long position, long count, OutputStream target) {
        return 0;
    }

}
