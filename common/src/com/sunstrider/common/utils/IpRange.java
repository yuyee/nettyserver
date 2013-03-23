/**
 * @(#)IpRange.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP范围类
 * <p>
 * 支持Ipv4和Ipv6
 * <p>
 * 注意: 本类的所有添加和判断方法都是线程不安全的
 */
public class IpRange extends Ipv4Range {
    /**
     * Ipv6格式的地址范围(兼容Ipv4)
     */
    private Ipv6Range ipv6Range = null;

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.common.utils.Ipv4Range#isLocated(java.lang.String)
     */
    @Override
    public boolean isLocated(String addr) throws UnknownHostException {
        if (this.ipv6Range != null) {
            return this.ipv6Range.isLocated(addr);
        } else {
            return super.isLocated(addr);
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.netease.hmail.common.utils.Ipv4Range#isLocated(java.net.InetAddress)
     */
    @Override
    public boolean isLocated(InetAddress addr) {
        if (this.ipv6Range != null) {
            return this.ipv6Range.isLocated(addr);
        } else if (addr instanceof Inet4Address) {
            return super.isLocated(addr);
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.common.utils.Ipv4Range#isLocated(int)
     */
    @Override
    public boolean isLocated(int ip) {
        if (this.ipv6Range != null) {
            return ipv6Range.isLocated(ip);
        } else {
            return super.isLocated(ip);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.common.utils.Ipv4Range#addIp(java.lang.String)
     */
    @Override
    public void addIp(String addrs) throws UnknownHostException {
        if (this.ipv6Range != null) {
            this.ipv6Range.addIp(addrs);
        } else {
            try {
                super.addIp(addrs);
            } catch (IllegalIpv6ArgumentException ex) {
                this.createIpv6Range();
                this.ipv6Range.addIp(addrs);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.common.utils.Ipv4Range#addIp(java.net.InetAddress)
     */
    @Override
    public void addIp(InetAddress addr) {
        if (this.ipv6Range != null) {
            this.ipv6Range.addIp(addr);
        } else if (addr instanceof Inet4Address) {
            super.addIp(addr);
        } else {
            this.createIpv6Range();
            this.ipv6Range.addIp(addr);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.common.utils.Ipv4Range#addIp(int)
     */
    @Override
    public void addIp(int ip) {
        if (this.ipv6Range != null) {
            this.ipv6Range.addIp(ip);
        } else {
            super.addIp(ip);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.common.utils.Ipv4Range#isTiled()
     */
    @Override
    protected boolean isTiled() {
        if (this.ipv6Range != null) {
            return false;
        } else {
            return super.isTiled();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.netease.hmail.common.utils.Ipv4Range#getSetSize()
     */
    @Override
    protected int getSetSize() {
        if (this.ipv6Range != null) {
            return 0;
        } else {
            return super.getSetSize();
        }
    }

    /**
     * 将原本的Ipv4范围对象转换到Ipv6的范围对象
     */
    private void createIpv6Range() {
        if (this.ipv6Range != null) {
            return;
        }

        this.ipv6Range = new Ipv6Range();

        if (super.isTiled()) {
            if (super.ipSet != null) {
                for (Integer ip: super.ipSet) {
                    this.ipv6Range.addIp(ip.intValue());
                }

                super.ipSet = null;
            }
        } else {
            for (IpSegment seg: super.ip2Seg.values()) {
                StringBuilder sb = new StringBuilder(15);
                byte[] start = ByteUtils.getAsBytes((int) seg.start);
                sb.append(start[0] & 0xFF).append('.');
                sb.append(start[1] & 0xFF).append('.');
                sb.append(start[2] & 0xFF).append('.');
                sb.append(start[3] & 0xFF).append('-');

                byte[] end = ByteUtils.getAsBytes((int) seg.end);
                sb.append(end[0] & 0xFF).append('.');
                sb.append(end[1] & 0xFF).append('.');
                sb.append(end[2] & 0xFF).append('.');
                sb.append(end[3] & 0xFF);

                this.ipv6Range.addIp(sb.toString());
            }

            super.ip2Seg = null;
        }
    }
}
