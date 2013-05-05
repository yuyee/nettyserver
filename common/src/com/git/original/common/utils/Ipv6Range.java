/**
 * @(#)Ipv6Range.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 支持IPv6的范围类
 */
public class Ipv6Range {
    /**
     * IPv6的字节长度
     */
    private static final int IPV6_ADDRESS_BYTE_COUNT = 16;

    /**
     * 全0的Ipv6特殊地址
     */
    private static final Ipv6 IPV6_ZERO = new Ipv6(0, 0);

    /**
     * IP段起始地址 --> IP段 映射表
     * <p>
     * 注意: 映射表中的IP都使用Long型,保证地址为正整数, 避免检索出错
     */
    private final NavigableMap<Ipv6, Ipv6Segment> ip2Seg = new TreeMap<Ipv6, Ipv6Segment>();

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

        return isLocated(addr.getAddress());
    }

    /**
     * 判断IP地址是否存在本范围内
     * 
     * @param ipv4
     *            网络IPv4值
     * @return true=存在范围内;false=存在范围外
     * @throws UnknownHostException
     */
    public boolean isLocated(int ipv4) {
        return isLocated(ByteUtils.getAsBytes(ipv4));
    }

    /**
     * 判断IP地址是否存在本范围内
     * 
     * @param byte[] 字节格式的网络IP地址
     * @return true=存在范围内;false=存在范围外
     * @throws UnknownHostException
     */
    public boolean isLocated(byte[] ipBytes) {
        if (ipBytes != null && !ip2Seg.isEmpty()) {
            Ipv6 ip = getIpv6Address(ipBytes);
            Entry<Ipv6, Ipv6Segment> entry = ip2Seg.floorEntry(ip);
            if (entry != null && entry.getValue().end.compareTo(ip) >= 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * 添加一个/批字符串描述的网络地址IP到本范围内
     * <p>
     * IPv4格式:
     * <ul>
     * <li>支持单个IP: xxx.xxx.xxx.xxx
     * <li>支持网络掩码格式: xxx.xxx.xxx.xxx/yy
     * <li>支持网络段格式: xxx.xxx.xxx.xxx-yyy.yyy.yyy.yyyy
     * </ul>
     * IPv6格式:
     * <ul>
     * <li>支持单个IP: xxxx:xxxx::xxxx
     * <li>支持网络掩码格式: xxxx:xxxx::xxxx/yy
     * <li>支持网络段格式: xxxx:xxxx::xxxx-yyyy:yyyy::yyyy
     * </ul>
     * 
     * @param addrs
     *            网络地址
     */
    public void addIp(String addrs) {
        if (addrs == null || addrs.isEmpty()) {
            return;
        }

        Ipv6 startIp;
        Ipv6 endIp;

        int minusPos = addrs.indexOf('-');
        if (minusPos > 0) { // "xxxx:xxxx::xxxx-yyyy:yyyy::yyyy" 格式IP段
            startIp = getIpv6Address(getIpv6AddrBytes(addrs.substring(0,
                minusPos).trim()));
            endIp = getIpv6Address(getIpv6AddrBytes(addrs.substring(
                minusPos + 1, addrs.length()).trim()));
        } else {
            int slashPos = addrs.lastIndexOf('/');
            if (slashPos == -1) { // "xxxx:xxxx::xxxx" 单个IP格式
                this.addIp(getIpv6AddrBytes(addrs));
                return;
            }

            /*
             * "xxxx:xxxx::xxxx/yy" 格式IP段
             */

            byte[] rawBytes = getIpv6AddrBytes(addrs.substring(0, slashPos));
            if (rawBytes == null) {
                throw new IllegalArgumentException("network address = "
                    + addrs.substring(0, slashPos));
            }
            boolean isIpv6Addr = (rawBytes.length == IPV6_ADDRESS_BYTE_COUNT);

            int maskBits = Integer.parseInt(addrs.substring(slashPos + 1)
                .trim());
            if (!isIpv6Addr) {
                maskBits += 96; // IPv4的地址格式, 强制添加 mask位数
            }

            if (maskBits == 128) {
                this.addIp(rawBytes);
                return;
            }

            if (maskBits < 0 || maskBits > 128) {
                throw new IllegalArgumentException("network mask = " + maskBits);
            } else if (maskBits == 0) {
                startIp = IPV6_ZERO;
                if (isIpv6Addr) {
                    endIp = new Ipv6(-1, -1);
                } else {
                    endIp = new Ipv6(0, 0xFFFFFFFF);
                }
            } else {
                int byteCount = maskBits >> 3; // 获取mask的字节数
                maskBits &= 0x7; // 获取残留的mask的bit数
                int mask = 0x0FF ^ (0x0FF >>> maskBits);

                byte[] startBytes, endBytes;
                if (rawBytes.length < IPV6_ADDRESS_BYTE_COUNT) {
                    startBytes = new byte[IPV6_ADDRESS_BYTE_COUNT];
                    System.arraycopy(rawBytes, 0, startBytes, startBytes.length
                        - rawBytes.length, rawBytes.length);
                } else {
                    startBytes = rawBytes;
                }

                endBytes = new byte[IPV6_ADDRESS_BYTE_COUNT];
                if (byteCount > 0) {
                    System.arraycopy(startBytes, 0, endBytes, 0, byteCount);
                }

                if (maskBits > 0) {
                    byte v = startBytes[byteCount];

                    startBytes[byteCount] = (byte) (v & mask);
                    endBytes[byteCount] = (byte) (v | (~mask));

                    byteCount++;
                }

                if (byteCount < IPV6_ADDRESS_BYTE_COUNT) {
                    Arrays.fill(startBytes, byteCount, IPV6_ADDRESS_BYTE_COUNT,
                        (byte) 0x0);
                    Arrays.fill(endBytes, byteCount, IPV6_ADDRESS_BYTE_COUNT,
                        (byte) 0xFF);
                }

                startIp = getIpv6Address(startBytes);
                endIp = getIpv6Address(endBytes);
            }
        }

        Entry<Ipv6, Ipv6Segment> entry = ip2Seg.floorEntry(startIp);
        Ipv6Segment lower;
        if (entry == null) {
            lower = new Ipv6Segment(startIp, endIp);
            ip2Seg.put(startIp, lower);
        } else if (entry.getValue().end.compareTo(startIp) < 0) {
            lower = entry.getValue();
            if (lower.end.differ(startIp) == -1) {
                lower.end = endIp;
            } else {
                lower = new Ipv6Segment(startIp, endIp);
                ip2Seg.put(startIp, lower);
            }
        } else {
            lower = entry.getValue();
            lower.end = endIp;
        }

        // 遍历, 合并范围相互覆盖的IP段
        while ((entry = ip2Seg.higherEntry(startIp)) != null) {
            Ipv6Segment higher = entry.getValue();
            if (lower.end.differ(higher.start) >= -1) { // 合并两个IP段
                if (lower.end.compareTo(higher.end) < 0) {
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
            throw new IllegalArgumentException("only support ipv4 address");
        }

        this.addIp(ByteUtils.toInt(ba));
    }

    /**
     * 将数字格式的ip字符串地址转换到字节数组格式
     * 
     * @param numericFormatHost
     *            数字格式的ip字符串地址
     * @return 字节数组格式ip地址
     */
    private byte[] getIpv6AddrBytes(String numericFormatHost) {
        // see if it is IPv4 address
        byte[] addr = IPUtils.textToNumericFormatV4(numericFormatHost);
        if (addr == null) {
            // see if it is IPv6 address
            addr = IPUtils.textToNumericFormatV6(numericFormatHost);
        }

        return addr;
    }

    /**
     * 将字节数组格式的IP地址转换到Ipv6对象
     * <p>
     * 支持IPv4的字节地址
     * 
     * @param ipBytes
     *            字节数组格式的IP地址
     * @return Ipv6对象
     */
    private Ipv6 getIpv6Address(byte[] ipBytes) {
        if (ipBytes == null) {
            return null;
        }

        long lower = 0;
        long upper = 0;
        if (ipBytes.length > 8) {
            upper = ByteUtils.bytesToLong(ipBytes, 0, ipBytes.length - 8, true);
            lower = ByteUtils.bytesToLong(ipBytes, ipBytes.length - 8, 8, true);
        } else {
            lower = ByteUtils.bytesToLong(ipBytes, 0, ipBytes.length, true);
        }

        return (new Ipv6(upper, lower));
    }

    /**
     * 添加一个IPv4地址到本范围内
     * 
     * @param ipv4
     *            ip地址
     */
    public void addIp(int ipv4) {
        this.addIp(ByteUtils.getAsBytes(ipv4));
    }

    /**
     * 添加一个IP地址到本范围内
     * 
     * @param ip
     *            字节数组格式的ip地址
     */
    public void addIp(byte[] ipBytes) {
        Ipv6 ip = getIpv6Address(ipBytes);
        if (ip == null) {
            return;
        }

        Ipv6Segment lower;
        Entry<Ipv6, Ipv6Segment> entry = ip2Seg.floorEntry(ip);
        if (entry == null) {
            lower = new Ipv6Segment(ip);
            ip2Seg.put(ip, lower);
        } else if (entry.getValue().end.compareTo(ip) < 0) {
            lower = entry.getValue();
            if (lower.end.differ(ip) == -1) {
                lower.end = ip;
            } else {
                lower = new Ipv6Segment(ip);
                ip2Seg.put(ip, lower);
            }
        } else {
            return; // 已经包含在现有IP段里了
        }

        entry = ip2Seg.higherEntry(ip);
        if (entry == null) {
            return;
        }

        Ipv6Segment higher = entry.getValue();
        if (lower.end.differ(higher.start) >= -1) { // 合并两个IP段
            lower.end = higher.end;
            ip2Seg.remove(higher.start);
        }
    }

    /**
     * 用于测试
     * 
     * @return
     */
    protected int getSegmentCount() {
        return (this.ip2Seg == null ? 0 : ip2Seg.size());
    }

    // ----------------------------------------------

    /** IP段 */
    private static class Ipv6Segment {
        /** 段起始IP地址(保证正数) */
        Ipv6 start = IPV6_ZERO;

        /** 段末尾IP地址(保证正数) */
        Ipv6 end = IPV6_ZERO;

        Ipv6Segment(Ipv6 ip) {
            start = ip;
            end = ip;
        }

        Ipv6Segment(Ipv6 start, Ipv6 end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * 描述一个IPv6的地址
     * 
     * @author <a href="mailto:qiusheng@corp.netease.com">QiuSheng</a>
     */
    private static class Ipv6 implements Comparable<Ipv6> {
        /**
         * 高64位地址值
         */
        final long upper;

        /**
         * 低64为地址值
         */
        final long lower;

        /**
         * 构造函数
         * 
         * @param upper
         * @param lower
         */
        public Ipv6(long upper, long lower) {
            this.upper = upper;
            this.lower = lower;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return (int) ((upper ^ (upper >>> 32)) ^ (lower ^ (lower >>> 32)));
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (obj != null && (obj instanceof Ipv6)) {
                Ipv6 other = (Ipv6) obj;
                if (this.upper == other.upper && this.lower == other.lower) {
                    return true;
                }
            }

            return false;
        }

        /** 工具常量: 16位二进制对应的字节长度 */
        private static final int INT16SZ = 2;

        /** 工具常量: 字符串格式时的Ipv6字符长度 */
        private static final int INADDRSZ = 16;

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(39);

            byte[] src = new byte[16];
            src[0] = (byte) ((upper >> 56) & 0xFF);
            src[1] = (byte) ((upper >> 48) & 0xFF);
            src[2] = (byte) ((upper >> 40) & 0xFF);
            src[3] = (byte) ((upper >> 32) & 0xFF);
            src[4] = (byte) ((upper >> 24) & 0xFF);
            src[5] = (byte) ((upper >> 16) & 0xFF);
            src[6] = (byte) ((upper >> 8) & 0xFF);
            src[7] = (byte) ((upper >> 0) & 0xFF);
            src[8] = (byte) ((lower >> 56) & 0xFF);
            src[9] = (byte) ((lower >> 48) & 0xFF);
            src[10] = (byte) ((lower >> 40) & 0xFF);
            src[11] = (byte) ((lower >> 32) & 0xFF);
            src[12] = (byte) ((lower >> 24) & 0xFF);
            src[13] = (byte) ((lower >> 16) & 0xFF);
            src[14] = (byte) ((lower >> 8) & 0xFF);
            src[15] = (byte) ((lower >> 0) & 0xFF);

            for (int i = 0; i < (INADDRSZ / INT16SZ); i++) {
                sb.append(Integer.toHexString(((src[i << 1] << 8) & 0xff00)
                    | (src[(i << 1) + 1] & 0xff)));
                if (i < (INADDRSZ / INT16SZ) - 1) {
                    sb.append(":");
                }
            }
            return sb.toString();
        }

        @Override
        public int compareTo(Ipv6 other) {
            long delta = differ(other);
            if (delta > 0) {
                return 1;
            } else if (delta < 0) {
                return -1;
            }

            return 0;
        }

        /**
         * 比较两个Ipv6地址
         * <p>
         * 当两者的差值超过Long的取值范围时,将恒定为 {@link Long#MAX_VALUE} 或
         * {@link Long#MIN_VALUE}
         * 
         * @param other
         *            待比较的地址
         * @return 差值
         */
        public long differ(Ipv6 other) {
            if (this == other) {
                return 0;
            }

            if (other == null) {
                return Long.MAX_VALUE;
            }

            if (this.upper == other.upper) {
                return compareLong(this.lower, other.lower);
            }

            long delta = compareLong(this.upper, other.upper);
            if (delta > 0) {
                return Long.MAX_VALUE;
            } else if (delta < 0) {
                return Long.MIN_VALUE;
            } else {
                return 0;
            }
        }

        /**
         * 比较两个长整型数组
         * <p>
         * 当两者的差值超过Long的取值范围时,将恒定为 {@link Long#MAX_VALUE} 或
         * {@link Long#MIN_VALUE}
         * 
         * @param value
         * @param other
         * @return 差值
         */
        private static long compareLong(long value, long other) {
            long delta = (value >>> 63) - (other >>> 63);
            if (delta == 1) {
                return Long.MAX_VALUE;
            } else if (delta == -1) {
                return Long.MIN_VALUE;
            }

            delta = (value & Long.MAX_VALUE) - (other & Long.MAX_VALUE);
            return delta;
        }

    }
}
