/**
 * @(#)ChecksumUtils.java, 2011-12-15. 
 * 
 * Copyright 2011 Netease, Inc. All rights reserved. 
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.utils;

import java.util.List;
import java.util.zip.CRC32;

/**
 * A checksum utils for index-search-server
 * 
 * 
 * @author linaoxiang
 */
public final class ChecksumUtils {
	/** 构造函数 */
	private ChecksumUtils() {
	};

	/**
	 * get the checksum of list of ids
	 * 
	 * @param mailIds
	 * @return
	 */
	public static int getIdsChecksum(List<String> mailIds) {
		CRC32 checker = new CRC32();
		int checksum = 0;
		for (String id : mailIds) {
			checksum ^= getChecksum(id, checker);
		}

		return checksum;
	}

	/**
	 * set the mailid on checksum
	 */
	public static int setChecksum(int checksum, String id) {
		return markChecksum(checksum, id);
	}

	/**
	 * unset the mailid on checksum
	 */
	public static int unsetChecksum(int checksum, String id) {
		return markChecksum(checksum, id);
	}

	private static int markChecksum(int checksum, String id) {
		checksum ^= getChecksum(id, new CRC32());
		return checksum;
	}

	public static int getChecksum(long mailid) {
		return getChecksum(String.valueOf(mailid), new CRC32());
	}

	/**
	 * get single id's checksum
	 * 
	 * @param id
	 * @param checker
	 * @return
	 */
	public static int getChecksum(String id, CRC32 checker) {

		if (checker == null) {
			checker = new CRC32();
		} else {
			checker.reset();
		}

		checker.update(id.getBytes(CharsetUtil.UTF_8));

		return (int) (checker.getValue() & 0xffffffffL);
	}
}
