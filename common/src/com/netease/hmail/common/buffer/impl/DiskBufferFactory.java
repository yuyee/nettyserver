/**
 * @(#)DiskBufferFactory.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.buffer.impl;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.hmail.common.buffer.Buffer;
import com.netease.hmail.common.buffer.BufferConfig;
import com.netease.hmail.common.buffer.BufferException;
import com.netease.hmail.common.buffer.DiskBufferLimitException;
import com.netease.hmail.common.buffer.ResourceManager;

/**
 * 磁盘buffer工厂,file的命名以pid+"-"+randomId+".tmp"
 * 
 * @author linaoxiang
 */
public class DiskBufferFactory extends ResourceBufferFactory {
    DiskBufferFactory(ResourceManager manager) {
        super(manager);
    }

    /** 日志描述 */
    private static final Logger LOG = LoggerFactory
        .getLogger(DiskBufferFactory.class);

    /**
     * 默认的种子分配器
     */
    private static AtomicLong seed = new AtomicLong(0);

    /**
     * 当前创建的磁盘文件计数器
     */
    private static AtomicLong openFile = new AtomicLong(0);

    /**
     * 文件名前缀
     */
    private String prefix;

    /**
     * 磁盘主目录
     */
    private String mainDiskHome = "/home/var/diskallocat";

    @Override
    public Buffer getBuffer(int capacity) throws DiskBufferLimitException {
        File file = allocate();
        DiskBuffer diskBuffer = new DiskBuffer(this, file, super.manager);
        LOG.trace("alloct buffer,buffer={}", diskBuffer.toString());
        return diskBuffer;
    }

    @Override
    public Buffer getBuffer(byte[] array, int offset, int length)
        throws DiskBufferLimitException {
        Buffer buffer = getBuffer(0);

        try {
            // 将原数据写到新的buffer中
            if (array != null) {
                buffer.writeBytes(array, offset, length);
            }
        } catch (Throwable t) {
            buffer = null;
            throw new BufferException("allocate buffer error", t);
        }

        LOG.trace("alloct buffer,buffer={}", buffer.toString());
        return buffer;
    }

    @Override
    public void freeBuffer(AbstractBuffer buffer) {

        buffer.free();

        super.retrieve(buffer.writerIndex);
        openFile.decrementAndGet();

        LOG.trace("free buffer,buffer={},openFileNum={}",
            new Object[] { buffer.toString(), openFile.get() });
    }

    @Override
    public void initialize(BufferConfig config) {

        String diskHome = config.getDiskHome();

        // 如果不存在，则创建磁盘目录
        File file = new File(diskHome);
        if (!file.exists()) {
            file.mkdirs();
        }

        this.mainDiskHome = file.getAbsolutePath();

        // 获取当前java进程的pid
        String pid = getPid();

        // 查看配置的前缀
        String configPre = config.getPrefix();
        if (configPre != null && !configPre.trim().equals("")) {
            this.prefix = pid + "-" + configPre;
        } else {
            this.prefix = pid;
        }

        this.manager.initialize(config);
        LOG.info("DiskBufferFactory initialize,{}", toString());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DiskBufferFactory [prefix=");
        builder.append(prefix);
        builder.append(", mainDiskHome=");
        builder.append(mainDiskHome);
        builder.append("]");
        return builder.toString();
    }

    /**
     * 分配一个磁盘文件资源
     * 
     * @param id
     * @return
     */
    private File allocate() {
        String name = this.prefix + "-" + seed.incrementAndGet() + ".tmp";

        openFile.incrementAndGet();
        return new File(mainDiskHome, name);
    }

    /**
     * 获取当前进程号
     * 
     * @return
     */
    private String getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
        try {
            return name.substring(0, name.indexOf('@'));
        } catch (Exception e) {
            return name;
        }
    }

}
