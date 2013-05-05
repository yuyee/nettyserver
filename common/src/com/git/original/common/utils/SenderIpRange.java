/**
 * @(#)SenderIpRange.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.utils;

import java.net.UnknownHostException;

/**
 * ip地址范围
 * 
 * @author linaoxiang
 * @version 2011-8-10
 */
public final class SenderIpRange {
	/** 原始的ip列表字符串 */
	final String sourceIps;

	/** ip范围集合对象 */
	final IpRange range;

	/**
	 * 格式： ips: iprange,ipragne,...
	 * 
	 * @param ips
	 * @throws NullPointerException
	 *             when ips == null
	 */
	public SenderIpRange(String ips) {
		range = new IpRange();
		String[] parts = ips.split(",");
		for (String ip : parts) {
			try {
				range.addIp(ip);
			} catch (UnknownHostException e) {
				continue;
			}
		}
		sourceIps = ips;
	}

	public boolean isLocated(String ip) {
		try {
			return range.isLocated(ip);
		} catch (UnknownHostException e) {
			return false;
		}
	}

	public String toString() {
		return sourceIps;
	}

}
