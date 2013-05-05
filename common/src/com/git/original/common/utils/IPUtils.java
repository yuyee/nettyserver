/**
 * @(#)IPUtils.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.utils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;

/**
 * IP相关常用方法
 * 
 * @author linaoxiang
 * @version 2011-4-8
 */
public final class IPUtils {
	/**
	 * "127.0.0.1"本机回送地址值
	 */
	public static final int LOOPBACK_ADDRESS_VALUE = 0x7F000001;

	/** RFC 2821 */
	public static final String RFC_2821_IPV6_PREFIX = "IPv6:";

	/** Ipv4地址对应的字节长度 */
	private static final int INADDR4SZ = 4;

	/** Ipv6地址对应的字节长度 */
	private static final int INADDR16SZ = 16;

	/** 常量: 16位二进制对应的字节长度 */
	private static final int INT16SZ = 2;

	/** 本机的ipv4内网地址值 */
	private static volatile int localIp = 0;

	/** 构造函数 */
	private IPUtils() {
	};

	/**
	 * 获取本机外网IP地址
	 * <p>
	 * 挑选优先级:<br/>
	 * 1. 220.xxx.xxx.xxx<br>
	 * 2. 123.xxx.xxx.xxx<br>
	 * other<br>
	 * 
	 * @return
	 * @throws SocketException
	 *             If an I/O error occurs.
	 * @throws RuntimeException
	 *             can not get net address
	 */
	public static InetAddress getWANIpv4Address() throws SocketException,
			RuntimeException {
		return getWANIpv4Address(null);
	}

	/**
	 * 获取本机外网IP地址
	 * <p>
	 * 挑选优先级:<br/>
	 * 1. 220.xxx.xxx.xxx<br>
	 * 2. 123.xxx.xxx.xxx<br>
	 * other<br>
	 * 
	 * @return
	 * @throws SocketException
	 *             If an I/O error occurs.
	 * @throws NullPointerException
	 *             can not found the interface
	 * @throws RuntimeException
	 *             can not get net address
	 */
	public static InetAddress getWANIpv4Address(String interfaceName)
			throws SocketException, NullPointerException, RuntimeException {

		InetAddress ipStartWith123 = null;
		InetAddress ipv4Addr = null;

		if (StringUtils.isNotEmpty(interfaceName)) {
			// 指定了接口
			NetworkInterface netInterface = NetworkInterface
					.getByName(interfaceName.trim());
			if (netInterface == null) {
				throw new NullPointerException(
						"can not found the network interface by name: "
								+ interfaceName);
			}

			Enumeration<InetAddress> addrEnum = netInterface.getInetAddresses();
			while (addrEnum.hasMoreElements()) {
				InetAddress addr = addrEnum.nextElement();
				String hostAddr = addr.getHostAddress();

				if (hostAddr.startsWith("220.")) {
					return addr;
				} else if (ipStartWith123 == null
						&& hostAddr.startsWith("123.")) {
					ipStartWith123 = addr;
				} else if (addr instanceof Inet4Address) {
					ipv4Addr = addr;
				}
			}
		} else {
			/*
			 * 获取本机的所有网卡地址
			 */
			Enumeration<NetworkInterface> interfaceEnum = NetworkInterface
					.getNetworkInterfaces();
			while (interfaceEnum.hasMoreElements()) {
				NetworkInterface netInterface = interfaceEnum.nextElement();
				if (netInterface.isLoopback() || !netInterface.isUp()) {
					continue;
				}

				Enumeration<InetAddress> addrEnum = netInterface
						.getInetAddresses();
				while (addrEnum.hasMoreElements()) {
					InetAddress addr = addrEnum.nextElement();
					String hostAddr = addr.getHostAddress();

					if (hostAddr.startsWith("220.")) {
						return addr;
					} else if (ipStartWith123 == null
							&& hostAddr.startsWith("123.")) {
						ipStartWith123 = addr;
					} else if (addr instanceof Inet4Address) {
						ipv4Addr = addr;
					}
				}
			}
		}

		if (ipStartWith123 != null) {
			return ipStartWith123;
		} else if (ipv4Addr != null) {
			return ipv4Addr;
		}

		throw new RuntimeException("can not get WAN Address");
	}

	/**
	 * 获取本机内网IP地址
	 * <p>
	 * 挑选优先级:<br/>
	 * 1. 192.xxx.xxx.xxx<br>
	 * 2. 172.xxx.xxx.xxx<br>
	 * 3. 10.xxx.xxx.xxx<br>
	 * other<br>
	 * 
	 * @return 整型的ipv4地址值
	 * @throws SocketException
	 */
	public static int getLocalIp() throws SocketException {
		if (localIp != 0) {
			return localIp;
		}

		return doGetLocalIp();
	}

	/**
	 * 获取本机内网IP地址
	 * <p>
	 * 挑选优先级:<br/>
	 * 1. 192.xxx.xxx.xxx<br>
	 * 2. 172.xxx.xxx.xxx<br>
	 * 3. 10.xxx.xxx.xxx<br>
	 * other<br>
	 * 
	 * @return 整型的ipv4地址值
	 */
	public static int getLocalIp(int defaultAddr) {
		if (localIp != 0) {
			return localIp;
		}

		try {
			return doGetLocalIp();
		} catch (SocketException ex) {
			localIp = defaultAddr;
			return defaultAddr;
		}
	}

	/**
	 * 获取本机内网IP地址
	 * <p>
	 * 挑选优先级:<br/>
	 * 1. 192.xxx.xxx.xxx<br>
	 * 2. 172.xxx.xxx.xxx<br>
	 * 3. 10.xxx.xxx.xxx<br>
	 * other<br>
	 * 
	 * @return 整型的ipv4地址值
	 * @throws SocketException
	 */
	private static synchronized int doGetLocalIp() throws SocketException {
		if (localIp != 0) {
			return localIp;
		}

		Integer ipStartWith10 = null;
		Integer ipStartWith172 = null;
		Integer other = null;

		/*
		 * 获取本机的所有IP地址
		 */
		Enumeration<NetworkInterface> interfaceEnum = NetworkInterface
				.getNetworkInterfaces();
		while (interfaceEnum.hasMoreElements()) {
			NetworkInterface netInterface = interfaceEnum.nextElement();
			if (!netInterface.isUp()) {
				continue;
			}

			Enumeration<InetAddress> addrEnum = netInterface.getInetAddresses();
			while (addrEnum.hasMoreElements()) {
				InetAddress addr = addrEnum.nextElement();
				String hostAddr = addr.getHostAddress();

				if (hostAddr.startsWith("192.")) {
					localIp = ByteUtils.toInt(addr.getAddress());
					return localIp;
				} else if (ipStartWith172 == null
						&& hostAddr.startsWith("172.")) {
					ipStartWith172 = ByteUtils.toInt(addr.getAddress());
				} else if (ipStartWith10 == null && hostAddr.startsWith("10.")) {
					ipStartWith10 = ByteUtils.toInt(addr.getAddress());
				} else if (other == null && (addr instanceof Inet4Address)) {
					other = ByteUtils.toInt(addr.getAddress());
				}
			}
		}

		if (ipStartWith172 != null) {
			localIp = ipStartWith172;
			return localIp;
		} else if (ipStartWith10 != null) {
			localIp = ipStartWith10;
			return localIp;
		} else if (other != null) {
			localIp = other;
			return localIp;
		}

		throw new RuntimeException("can not get Local Server IPv4 Address");
	}

	/**
	 * 获取本机内网IP地址字符串类型
	 * <p>
	 * 挑选优先级: 1. 192.xxx.xxx.xxx<br>
	 * 2. 172.xxx.xxx.xxx<br>
	 * 3. 10.xxx.xxx.xxx<br>
	 * other<br>
	 * 
	 * @throws SocketException
	 */
	public static String getLocalIpAddress() throws SocketException {
		int ip = getLocalIp();
		StringBuilder sb = new StringBuilder(15);
		sb.append(ip >>> 24).append('.').append((ip >> 16) & 0xFF).append('.')
				.append((ip >> 8) & 0xFF).append('.').append(ip & 0xFF);
		return sb.toString();
	}

	/**
	 * Converts IPv4 address in its textual presentation form into its numeric
	 * binary form.
	 * <p>
	 * 摘自: openjdk-7
	 * 
	 * @param src
	 *            a String representing an IPv4 address in standard format
	 * @return a byte array representing the IPv4 numeric address
	 */
	public static byte[] textToNumericFormatV4(String src) {
		if (src.length() == 0) {
			return null;
		}
		byte[] res = new byte[INADDR4SZ];
		String[] s = src.split("\\.", -1);
		long val;
		try {
			switch (s.length) {
			case 1:
				/*
				 * When only one part is given, the value is stored directly in
				 * the network address without any byte rearrangement.
				 */
				val = Long.parseLong(s[0]);
				if (val < 0 || val > 0xffffffffL) {
					return null;
				}
				res[0] = (byte) ((val >> 24) & 0xff);
				res[1] = (byte) (((val & 0xffffff) >> 16) & 0xff);
				res[2] = (byte) (((val & 0xffff) >> 8) & 0xff);
				res[3] = (byte) (val & 0xff);
				break;
			case 2:
				/*
				 * When a two part address is supplied, the last part is
				 * interpreted as a 24-bit quantity and placed in the right most
				 * three bytes of the network address. This makes the two part
				 * address format convenient for specifying Class A network
				 * addresses as net.host.
				 */
				val = Integer.parseInt(s[0]);
				if (val < 0 || val > 0xff) {
					return null;
				}
				res[0] = (byte) (val & 0xff);
				val = Integer.parseInt(s[1]);
				if (val < 0 || val > 0xffffff) {
					return null;
				}
				res[1] = (byte) ((val >> 16) & 0xff);
				res[2] = (byte) (((val & 0xffff) >> 8) & 0xff);
				res[3] = (byte) (val & 0xff);
				break;
			case 3:
				/*
				 * When a three part address is specified, the last part is
				 * interpreted as a 16-bit quantity and placed in the right most
				 * two bytes of the network address. This makes the three part
				 * address format convenient for specifying Class B net- work
				 * addresses as 128.net.host.
				 */
				for (int i = 0; i < 2; i++) {
					val = Integer.parseInt(s[i]);
					if (val < 0 || val > 0xff) {
						return null;
					}
					res[i] = (byte) (val & 0xff);
				}
				val = Integer.parseInt(s[2]);
				if (val < 0 || val > 0xffff) {
					return null;
				}
				res[2] = (byte) ((val >> 8) & 0xff);
				res[3] = (byte) (val & 0xff);
				break;
			case 4:
				/*
				 * When four parts are specified, each is interpreted as a byte
				 * of data and assigned, from left to right, to the four bytes
				 * of an IPv4 address.
				 */
				for (int i = 0; i < 4; i++) {
					val = Integer.parseInt(s[i]);
					if (val < 0 || val > 0xff) {
						return null;
					}
					res[i] = (byte) (val & 0xff);
				}
				break;
			default:
				return null;
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return res;
	}

	/**
	 * Convert IPv6 presentation level address to network order binary form.
	 * <p>
	 * credit:
	 * <p>
	 * Converted from C code from Solaris 8 (inet_pton) Any component of the
	 * string following a per-cent % is ignored.
	 * <p>
	 * 摘自: openjdk-7
	 * 
	 * @param src
	 *            a String representing an IPv6 address in textual format
	 * @return a byte array representing the IPv6 numeric address
	 */
	public static byte[] textToNumericFormatV6(String src) {
		// Shortest valid string is "::", hence at least 2 chars
		if (src.length() < 2) {
			return null;
		}
		int colonp;
		char ch;
		boolean sawXDigit;
		int val;
		char[] srcb = src.toCharArray();
		byte[] dst = new byte[INADDR16SZ];
		int srcbLength = srcb.length;
		int pc = src.indexOf("%");
		if (pc == srcbLength - 1) {
			return null;
		}
		if (pc != -1) {
			srcbLength = pc;
		}
		colonp = -1;
		int i = 0, j = 0;
		/* Leading :: requires some special handling. */
		if (srcb[i] == ':') {
			if (srcb[++i] != ':') {
				return null;
			}
		}
		int curtok = i;
		sawXDigit = false;
		val = 0;
		while (i < srcbLength) {
			ch = srcb[i++];
			int chval = Character.digit(ch, 16);
			if (chval != -1) {
				val <<= 4;
				val |= chval;
				if (val > 0xffff) {
					return null;
				}
				sawXDigit = true;
				continue;
			}
			if (ch == ':') {
				curtok = i;
				if (!sawXDigit) {
					if (colonp != -1) {
						return null;
					}
					colonp = j;
					continue;
				} else if (i == srcbLength) {
					return null;
				}
				if (j + INT16SZ > INADDR16SZ) {
					return null;
				}
				dst[j++] = (byte) ((val >> 8) & 0xff);
				dst[j++] = (byte) (val & 0xff);
				sawXDigit = false;
				val = 0;
				continue;
			}
			if (ch == '.' && ((j + INADDR4SZ) <= INADDR16SZ)) {
				String ia4 = src.substring(curtok, srcbLength);
				/* check this IPv4 address has 3 dots, ie. A.B.C.D */
				int dotCount = 0, index = 0;
				while ((index = ia4.indexOf('.', index)) != -1) {
					dotCount++;
					index++;
				}
				if (dotCount != 3) {
					return null;
				}
				byte[] v4addr = textToNumericFormatV4(ia4);
				if (v4addr == null) {
					return null;
				}
				for (int k = 0; k < INADDR4SZ; k++) {
					dst[j++] = v4addr[k];
				}
				sawXDigit = false;
				break; /* '\0' was seen by inet_pton4(). */
			}
			return null;
		}
		if (sawXDigit) {
			if (j + INT16SZ > INADDR16SZ) {
				return null;
			}
			dst[j++] = (byte) ((val >> 8) & 0xff);
			dst[j++] = (byte) (val & 0xff);
		}
		if (colonp != -1) {
			int n = j - colonp;
			if (j == INADDR16SZ) {
				return null;
			}
			for (i = 1; i <= n; i++) {
				dst[INADDR16SZ - i] = dst[colonp + n - i];
				dst[colonp + n - i] = 0;
			}
			j = INADDR16SZ;
		}
		if (j != INADDR16SZ) {
			return null;
		}
		byte[] newdst = convertFromIPv4MappedAddress(dst);
		if (newdst != null) {
			return newdst;
		} else {
			return dst;
		}
	}

	private static byte[] convertFromIPv4MappedAddress(byte[] addr) {
		if (isIPv4MappedAddress(addr)) {
			byte[] newAddr = new byte[INADDR4SZ];
			System.arraycopy(addr, 12, newAddr, 0, INADDR4SZ);
			return newAddr;
		}
		return null;
	}

	/**
	 * Utility routine to check if the InetAddress is an IPv4 mapped IPv6
	 * address.
	 * <p>
	 * 摘自: openjdk-7
	 * 
	 * @return a <code>boolean</code> indicating if the InetAddress is an IPv4
	 *         mapped IPv6 address; or false if address is IPv4 address.
	 */
	private static boolean isIPv4MappedAddress(byte[] addr) {
		if (addr.length < INADDR16SZ) {
			return false;
		}
		if ((addr[0] == 0x00) && (addr[1] == 0x00) && (addr[2] == 0x00)
				&& (addr[3] == 0x00) && (addr[4] == 0x00) && (addr[5] == 0x00)
				&& (addr[6] == 0x00) && (addr[7] == 0x00) && (addr[8] == 0x00)
				&& (addr[9] == 0x00) && (addr[10] == (byte) 0xff)
				&& (addr[11] == (byte) 0xff)) {
			return true;
		}
		return false;
	}

	/**
	 * 摘自: openjdk-7
	 * 
	 * @param src
	 *            a String representing an IPv4 address in textual format
	 * @return a boolean indicating whether src is an IPv4 literal address
	 */
	public static boolean isIPv4LiteralAddress(String src) {
		return textToNumericFormatV4(src) != null;
	}

	/**
	 * 摘自: openjdk-7
	 * 
	 * @param src
	 *            a String representing an IPv6 address in textual format
	 * @return a boolean indicating whether src is an IPv6 literal address
	 */
	public static boolean isIPv6LiteralAddress(String src) {
		return textToNumericFormatV6(src) != null;
	}

	public static void main(String[] args) throws UnknownHostException {
		System.out.println(isIPv6LiteralAddress("2001:200:0:0:0:0:0:0"));
		InetAddress ia = InetAddress.getByName("2001:200:0:0:0:0:0:0");
		System.out.println(ia instanceof Inet6Address);

	}
}
