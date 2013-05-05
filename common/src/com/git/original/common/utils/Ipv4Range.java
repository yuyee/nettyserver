/**
 * @(#)Ipv4Range.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * IPv4范围类
 */
public class Ipv4Range {
    /** IP段 */
    protected static class IpSegment {
        /** 段起始IP地址(保证正数) */
        protected long start = 0;

        /** 段末尾IP地址(保证正数) */
        protected long end = 0;

        IpSegment(long ip) {
            start = ip;
            end = ip;
        }

        IpSegment(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * 输入参数为Ipv6格式的地址
     * 
     * @author <a href="mailto:qiusheng@corp.netease.com">QiuSheng</a>
     */
    protected static class IllegalIpv6ArgumentException extends
        IllegalArgumentException {
        /** 序列化ID */
        private static final long serialVersionUID = -5613687063259662667L;

        public IllegalIpv6ArgumentException(String s) {
            super(s);
        }
    }

    /** 平铺模式下: 在{@code ipSet} 集合中的ip地址个数上限 */
    private static final int TILE_COUNT_LIMIT = 1024;

    /**
     * IP单个地址集合
     */
    protected Set<Integer> ipSet = null;

    /**
     * IP段起始地址 --> IP段 映射表
     * <p>
     * 注意: 映射表中的IP都使用Long型,保证地址为正整数, 避免检索出错
     */
    protected NavigableMap<Long, IpSegment> ip2Seg = null;

    /**
     * IP地址组织方式
     * <p>
     * <li>true=使用单个地址集合
     * <li>false=使用IP段映射表
     */
    private boolean isTiled = true;

    /**
     * 判断字符串网络地址的IP是否存在本范围内
     * 
     * @param addr
     *            网络地址
     * @return true=存在范围内;false=存在范围外
     * @throws UnknownHostException
     */
    public boolean isLocated(String addr) throws UnknownHostException {
        if (addr == null) {
            return false;
        }

        return isLocated(InetAddress.getByName(addr));
    }

    /**
     * 判断网络地址的IP是否存在本范围内
     * 
     * @param addr
     *            网络地址
     * @return true=存在范围内;false=存在范围外
     * @throws UnknownHostException
     */
    public boolean isLocated(InetAddress addr) {
        if (addr == null) {
            return false;
        }

        return isLocated(ByteUtils.toInt(addr.getAddress()));
    }

    /**
     * 判断IP地址是否存在本范围内
     * 
     * @param ip
     *            网络IP
     * @return true=存在范围内;false=存在范围外
     * @throws UnknownHostException
     */
    public boolean isLocated(int ip) {
        if (isTiled) {
            if (ipSet == null) {
                return false;
            }
            return ipSet.contains(ip);
        } else if (!ip2Seg.isEmpty()) {
            long lip = 0xFFFFFFFFL & ip;
            Entry<Long, IpSegment> entry = ip2Seg.floorEntry(lip);
            if (entry != null && entry.getValue().end >= lip) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加一个/批字符串描述的网络地址IP到本范围内
     * <p>
     * <li>支持网络掩码格式: xxx.xxx.xxx.xxx/yy
     * <li>支持网络段格式: xxx.xxx.xxx.xxx-yyy.yyy.yyy.yyy
     * 
     * @param addrs
     *            网络地址
     * @throws UnknownHostException
     */
    public void addIp(String addrs) throws UnknownHostException {
        if (addrs == null || addrs.isEmpty()) {
            return;
        }

        long count;
        long startIp;
        long endIp;

        int minusPos = addrs.indexOf('-');
        if (minusPos > 0) {
            startIp = getIpv4AddressAsInteger(addrs.substring(0, minusPos)
                .trim()) & 0xFFFFFFFFL;
            endIp = getIpv4AddressAsInteger(addrs.substring(minusPos + 1,
                addrs.length()).trim()) & 0xFFFFFFFFL;

            count = endIp - startIp + 1;

        } else {
            int slashPos = addrs.lastIndexOf('/');
            if (slashPos == -1) {
                this.addIp(getIpv4AddressAsInteger(addrs));
                return;
            }

            int maskBits = Integer.parseInt(addrs.substring(slashPos + 1)
                .trim());
            if (maskBits < 0) {
                throw new IllegalArgumentException("network mask = " + maskBits);
            } else if (maskBits > 32) {
                throw new IllegalIpv6ArgumentException("network mask = "
                    + maskBits);
            } else if (maskBits == 0) {
                if (!IPUtils.isIPv4LiteralAddress(addrs.substring(0, slashPos))) {
                    throw new IllegalIpv6ArgumentException(
                        "only support ipv4 address");
                }

                count = (1L << 32);
                startIp = 0;
            } else {
                long mask = (maskBits == 32 ? 0x0FFFFFFFFL
                    : (0x0FFFFFFFFL ^ (0x0FFFFFFFFL >>> maskBits)));
                count = 1 << (32 - maskBits);

                startIp = getIpv4AddressAsInteger(addrs.substring(0, slashPos))
                    & mask;
            }

            endIp = startIp + count - 1;
        }

        if (count == (1L << 32)) {
            this.isTiled = false;
            this.ipSet = null;
            this.ip2Seg = new TreeMap<Long, IpSegment>();
            this.ip2Seg.put(0L, new IpSegment(startIp, endIp));
            return;
        }

        if (isTiled) {
            if (ipSet == null) {
                ipSet = new HashSet<Integer>();
            }

            if (this.ipSet.size() + count <= TILE_COUNT_LIMIT) {
                while (startIp <= endIp) {
                    this.ipSet.add((int) startIp++);
                }
                return;
            }

            this.ip2Seg = convertToMap(ipSet);
            this.isTiled = false;
            this.ipSet = null;
        }

        Entry<Long, IpSegment> entry = ip2Seg.floorEntry(startIp);
        IpSegment lower;
        if (entry == null) {
            lower = new IpSegment(startIp, endIp);
            ip2Seg.put(startIp, lower);
        } else if (entry.getValue().end < startIp) {
            lower = entry.getValue();
            if (lower.end == startIp - 1) {
                lower.end = endIp;
            } else {
                lower = new IpSegment(startIp, endIp);
                ip2Seg.put(startIp, lower);
            }
        } else {
            lower = entry.getValue();
            lower.end = endIp;
        }

        // 遍历, 合并范围相互覆盖的IP段
        while ((entry = ip2Seg.higherEntry(startIp)) != null) {
            IpSegment higher = entry.getValue();
            if (lower.end >= higher.start - 1) { // 合并两个IP段
                if (lower.end < higher.end) {
                    lower.end = higher.end;
                }

                ip2Seg.remove(higher.start);
                startIp = higher.end;
            } else {
                break;
            }
        }
    }

    /**
     * 添加一个网络地址的IP到本范围内
     * 
     * @param addr
     *            网络地址
     */
    public void addIp(InetAddress addr) {
        byte[] ba = addr.getAddress();
        if (ba.length > 4) {
            throw new IllegalIpv6ArgumentException("only support ipv4 address");
        }

        this.addIp(ByteUtils.toInt(ba));
    }

    /**
     * 获取整数类型的ip地址
     * 
     * @param str
     *            ip地址
     * @return ip整数
     */
    private int getIpv4AddressAsInteger(String str) {
        // see if it is IPv4 address
        if (!IPUtils.isIPv4LiteralAddress(str)) {
            throw new IllegalIpv6ArgumentException("only support ipv4 address");
        }

        byte[] ba = IPUtils.textToNumericFormatV4(str);

        if (ba == null) {
            throw new IllegalIpv6ArgumentException("only support IP address");
        }

        if (ba.length > 4) {
            throw new IllegalIpv6ArgumentException("only support ipv4 address");
        }

        return ByteUtils.toInt(ba);
    }

    /**
     * 添加一个IP地址到本范围内
     * 
     * @param ip
     *            ip地址
     */
    public void addIp(int ip) {
        if (isTiled) { // 添加到IP集合
            if (ipSet == null) {
                ipSet = new HashSet<Integer>();
            }
            ipSet.add(ip);

            if (ipSet.size() > TILE_COUNT_LIMIT) {
                this.ip2Seg = convertToMap(ipSet);
                this.isTiled = false;
                this.ipSet = null;
            }
        } else { // 添加到IP段映射表
            long lip = 0xFFFFFFFFL & ip;
            if (ip2Seg == null) {
                ip2Seg = new TreeMap<Long, IpSegment>();
            }

            Entry<Long, IpSegment> entry = ip2Seg.floorEntry(lip);
            IpSegment lower;
            if (entry == null) {
                lower = new IpSegment(lip);
                ip2Seg.put(lip, lower);
            } else if (entry.getValue().end < lip) {
                lower = entry.getValue();
                if (lower.end == lip - 1) {
                    lower.end = lip;
                } else {
                    lower = new IpSegment(lip);
                    ip2Seg.put(lip, lower);
                }
            } else {
                return; // 已经包含在现有IP段里了
            }

            entry = ip2Seg.higherEntry(lip);
            if (entry == null) {
                return;
            }

            IpSegment higher = entry.getValue();
            if (lower.end >= higher.start - 1) { // 合并两个IP段
                lower.end = higher.end;
                ip2Seg.remove(higher.start);
            }
        }
    }

    /**
     * 将IP单个地址集合转换为IP段映射表
     * 
     * @param ipSet
     *            IP地址集合
     * @return ip段落映射表
     */
    private static NavigableMap<Long, IpSegment> convertToMap(Set<Integer> ipSet) {
        if (ipSet == null || ipSet.isEmpty()) {
            return new TreeMap<Long, IpSegment>();
        }

        TreeMap<Long, IpSegment> result = new TreeMap<Long, IpSegment>();
        Long[] lips = new Long[ipSet.size()];
        int i = 0;
        for (Integer ip: ipSet) {
            lips[i++] = 0xFFFFFFFFL & ip;
        }
        Arrays.sort(lips);

        IpSegment lastSeg = null;
        for (Long lip: lips) {
            if (lastSeg == null || lastSeg.end < lip - 1) {
                lastSeg = new IpSegment(lip.longValue());
                result.put(lip, lastSeg);
            } else {
                lastSeg.end = lip;
            }
        }

        return result;
    }

    /**
     * 用于测试
     * 
     * @return the isTiled
     */
    protected boolean isTiled() {
        return isTiled;
    }

    /**
     * 用于测试
     * 
     * @return
     */
    protected int getSetSize() {
        return (this.ipSet == null ? 0 : ipSet.size());
    }

    /**
     * 用于测试
     * 
     * @return
     */
    protected int getSegmentCount() {
        return (this.ip2Seg == null ? 0 : ip2Seg.size());
    }

}
