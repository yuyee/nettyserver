/**
 * @(#)DiskBuffer.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.buffer.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

import com.git.original.common.buffer.BufferException;
import com.git.original.common.buffer.BufferLimitException;
import com.git.original.common.buffer.ResourceManager;

/**
 * 基于磁盘存储的buffer类
 * 
 * @author linaoxiang
 */
public class DiskBuffer extends AbstractBuffer {

    /**
     * 磁盘缓存文件对象
     */
    private final File diskBufFile;

    /**
     * 当前文件句柄
     */
    private final RandomAccessFile currDiskBuf;

    /**
     * 文件句柄
     */
    private FileChannel channle;

    /**
     * 磁盘管理器
     */
    private final ResourceManager manager;

    public DiskBuffer(BaseBufferFactory factory, File diskBufFile,
        ResourceManager manager) {
        super(factory);
        this.diskBufFile = diskBufFile;
        this.manager = manager;
        try {
            currDiskBuf = new RandomAccessFile(diskBufFile, "rw");
            this.channle = currDiskBuf.getChannel();
        } catch (FileNotFoundException e) {
            throw new BufferException("instance DiskBuffer fail,path="
                + diskBufFile.getAbsolutePath(), e);
        }
    }

    @Override
    public int capacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getBytes(int index, byte[] dst, int dstIndex, int length) {

        try {
            ByteBuffer wrap = ByteBuffer.wrap(dst, dstIndex, length);
            int read = this.channle.read(wrap, index);
            if (read == 0 || read == -1) {
                return 0;
            }

            return read;
        } catch (IOException e) {
            throw new BufferException("getBytes fail,dst.length" + dst.length
                + ",index=" + dstIndex + ",length=" + length, e);
        }
    }

    @Override
    protected void setBytes(int index, byte[] src, int srcIndex, int length) {

        long register = this.manager.register(length);
        if (register == 0 || register < 0) {
            throw new BufferLimitException("disk resource limit!");
        }

        try {
            this.currDiskBuf.write(src, srcIndex, length);
        } catch (IOException e) {
            throw new BufferException("setBytes fail,srt.length" + src.length
                + ",index=" + srcIndex + ",length=" + length, e);
        }

    }

    @Override
    protected long transferTo(long position, long count, OutputStream target) {

        try {

            long transferTo = this.channle.transferTo(position, count,
                Channels.newChannel(target));
            if (transferTo == 0 || transferTo < 0) {
                transferTo = 0;
            }

            return transferTo;
        } catch (IOException e) {
            throw new BufferException("transferTo fail,writeIndex" + position
                + ",count=" + count, e);
        }
    }

    @Override
    protected void free() {
        try {
            this.currDiskBuf.close();
            this.channle.close();
        } catch (Exception e) {
            // ignore exception
        } finally {
            this.diskBufFile.delete();
        }
    }

    public File getDiskBufFile() {
        return diskBufFile;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DiskBuffer [diskBufFile=");
        builder.append(diskBufFile);
        builder.append(", toString()=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
}
