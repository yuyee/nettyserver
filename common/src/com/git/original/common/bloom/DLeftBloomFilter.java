/**
 * @(#)DLeftBloomFilter.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.bloom;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.git.original.common.utils.Md5Utils;

/**
 * D-Left Bloom Filter实现，提供
 * 
 * @author linaoxiang
 */
public class DLeftBloomFilter {
	/** 日志描述 */
	public static final Logger LOG = LoggerFactory
			.getLogger(DLeftBloomFilter.class);

	/**
	 * 元素个数
	 */
	private int maxSize;

	/**
	 * 每个子表的bucket size
	 */
	private int bucketSize;

	/**
	 * 对应的子表
	 */
	private byte[][] tableBuf;

	/**
	 * 每个桶对应8个cell,每个cell 2bit表示count,16位，1个桶即对应一个short
	 */
	private short[][] countBuf;

	/**
	 * 桶byte length
	 */
	private int bucketLength = 0;

	/**
	 * key使用小写
	 */
	private boolean keyIsLowerCase = true;

	/**
	 * hash数
	 */
	public static final int NBHASH = 4;

	/**
	 * 最大count 3
	 */
	private static final int MAXCOUNT = 3;

	/**
	 * 默认值byte,用于删除
	 */
	private static final byte[] DELBYTES = new byte[3];

	/**
	 * 置换时随机置数3
	 */
	private static final int a = 3;

	/**
	 * fingerPrints占用的bits
	 */
	private static final int fingerBitsSize = 24;

	// private static int[] c = new int[NBHASH];

	public DLeftBloomFilter(int maxsize) {
		this.maxSize = maxsize;
		this.bucketSize = ((this.maxSize - 1) / fingerBitsSize) + 1;
		this.tableBuf = new byte[NBHASH][];
		this.countBuf = new short[NBHASH][];
		this.bucketLength = (3 << 3) + 1;
		for (int i = 0; i < NBHASH; i++) {
			// 8cell + 1byte(负载)
			tableBuf[i] = new byte[(this.bucketSize * this.bucketLength)];
			countBuf[i] = new short[this.bucketSize];
		}
	}

	/**
	 * DLeftBloomFilter初始化
	 * 
	 * @param maxsize
	 *            集合最大数
	 * @param keyIsLowerCase
	 *            key是否为小写字母 true是，false不是，默认是true
	 */
	public DLeftBloomFilter(int maxsize, boolean keyIsLowerCase) {
		this.maxSize = maxsize;
		this.bucketSize = ((this.maxSize - 1) / fingerBitsSize) + 1;
		this.tableBuf = new byte[NBHASH][];
		this.countBuf = new short[NBHASH][];
		this.bucketLength = (3 << 3) + 1;
		for (int i = 0; i < NBHASH; i++) {
			// 8cell + 1byte(负载)
			tableBuf[i] = new byte[(this.bucketSize * this.bucketLength)];
			countBuf[i] = new short[this.bucketSize];
		}
		//
		this.keyIsLowerCase = keyIsLowerCase;
	}

	/**
	 * key返回所有hash定位的桶
	 * 
	 * @param key
	 * @return
	 */
	private HashInfo hash(String key) {
		byte[] md5value = Md5Utils.md5(key);

		/**
		 * key在每个子表中的bucket index
		 */
		int[] h = new int[NBHASH];
		/**
		 * 手印及手印值，手印值在置换后在各个桶中还是一样的
		 */
		byte[] fingerPrint = new byte[3];
		int fingerValue = 0;
		System.arraycopy(md5value, 13, fingerPrint, 0, 3);

		for (int i = 0; i < NBHASH; i++) {
			byte[] dest = new byte[3];
			// md5 划分,定位bucket
			System.arraycopy(md5value, i * 3, dest, 0, 3);
			// 15bit index
			int index = hash24BitTo15Bit(dest);
			// 39bit index + finger
			long newhash = hashReplaceTo39Bit(index, fingerPrint);
			// 取index 前15bit
			index = (int) (newhash >> fingerBitsSize);
			// fingerPrint 后24bit(四个位置置换的目的是保证每个元素的位置不重复，其手印在置换后，对应的四个新hash依然一样)
			fingerValue = (int) (newhash & (0xFFFFFF));
			// bucket index
			h[i] = hash(index);
		}
		// 各个子表对应的hash,以及手印
		HashInfo info = new HashInfo();
		info.setHashs(h);
		info.setFingerValue(fingerValue);

		return info;
	}

	/**
	 * 跟据res,进行桶索引查询
	 * 
	 * @param res
	 * @return
	 */
	private int hash(int res) {
		return res % (bucketSize);
		// return res % (1 << 15); (集合超过32768*4*6,才可以用此取桶号)
	}

	/**
	 * 3byte转换 int值
	 * 
	 * @param key
	 * @return
	 */
	private int hash(byte[] key) {
		int res = ((((key[2] & 0xFF) << 16) | ((key[1] & 0xFF) << 8) | (key[0] & 0xFF)));
		return res;
	}

	/**
	 * 24bit高位地址到15bit
	 * 
	 * @param key
	 * @return
	 */
	private int hash24BitTo15Bit(byte[] key) {
		int res = ((((key[2] & 0xFF) << 16) | ((key[1] & 0xFF) << 8) | (key[0] & 0xFF)));
		return (res % (0x7FFF));
	}

	/**
	 * 15位index与24位手印H(x) * a mod 2（39次方） 置换成新的39bit数值。
	 * 
	 * @param bucketIndex
	 *            //15位bit
	 * @param fingerPrint
	 *            //手印
	 * @return
	 */
	private long hashReplaceTo39Bit(int bucketIndex, byte[] fingerPrint) {
		long n = (a * (((long) bucketIndex << fingerBitsSize) + hash(fingerPrint)));
		long m = (1L << 39);
		return n % m;
	}

	/**
	 * 判断手印在此子表中是否存在了，如果存在，返回bucket中的位置，不存在，返回-1
	 * 
	 * @param tableIndex
	 *            子表标记
	 * @param bucketIndex
	 *            bucket标记
	 * @param fingerPrint
	 * @param info
	 * @return
	 */
	public int checkFingerPrint(int tableIndex, int bucketIndex,
			int fingerPrint, HashInfo info) {
		// 定位桶
		int bucketByteSize = this.bucketLength * bucketIndex;
		// 取此桶当前cell数
		int cellCount = tableBuf[tableIndex][bucketByteSize + fingerBitsSize];
		for (int i = 0; i <= cellCount; i++) {
			int begin = bucketByteSize + i * 3;
			// 取手印
			byte[] finger = new byte[3];
			System.arraycopy(tableBuf[tableIndex], begin, finger, 0, 3);
			int hashfinger = hash(finger);
			// 比较手印,已经存在，返回index
			if (hashfinger == fingerPrint) {
				info.setFingerPrintExists(tableIndex, bucketIndex, i);
				return i;
			}
		} // end for

		return -1;
	}

	/**
	 * 判断key是否存在
	 * 
	 * @param key
	 * @return
	 */
	private HashInfo membershipExist(String key) {
		HashInfo info = hash(key);
		// 计算手印
		int fingerValue = info.getFingerValue();
		int[] hashs = info.getHashs();
		// 遍历每个子表，对应bucket,判断手印是否已经存在。
		for (int i = 0; i < hashs.length; i++) {
			int bindex = hashs[i];
			// i个子表
			int result = checkFingerPrint(i, bindex, fingerValue, info);
			if (result >= 0) {
				return info;
			}
		} // end for
		return info;
	}

	/**
	 * 判断key是否存在
	 * 
	 * @param key
	 * @return
	 */
	public boolean membershipTest(String key) {
		if (key == null) {
			return false;
		}
		if (keyIsLowerCase) {
			key = key.toLowerCase();
		}
		return membershipExist(key).isExist();
	}

	/**
	 * 计算当前Bucket的cell个数，包括碰撞的个数。(即每个cell的count总和)
	 * 
	 * @param cellCount
	 *            //每个元素的count值
	 * @param cellNum
	 *            //元素个数
	 * @return
	 */
	public int countBucketLoad(int cellCount, byte cellNum) {
		int result = 0;
		for (int i = 0; i < cellNum; i++) {
			result += ((cellCount >> (2 * i)) & 0x3) + 1;
		}
		return result;
	}

	/**
	 * 增加
	 * 
	 * @param key
	 */
	public void add(String key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (keyIsLowerCase) {
			key = key.toLowerCase();
		}
		//
		HashInfo info = membershipExist(key);
		// 已经存在,count+1
		if (info.isExist()) {
			int tableIndex = info.getTableIndex();
			int bucketIndex = info.getBucketIndex();
			int cellIndex = info.getCellIndex();

			// 暂不考虑并发带来的问题
			short count = countBuf[tableIndex][bucketIndex];
			// 右移2*cell位，&03,定位cellIndex对应的Count
			short cellCount = (short) ((count >> (2 * cellIndex)) & 0x3);
			// 小于最大值,count + 1;
			if (count < MAXCOUNT) {
				cellCount++;
				count = (short) (count | (cellCount << 2));
				countBuf[tableIndex][bucketIndex] = count;
			}
		} else {
			// 不存在，比较各bucket中的cell，取负载最轻的
			int[] hashs = info.getHashs();
			int minBucketIndex = 0;
			int minCellIndex = 0;
			int tableIndex = 0;
			// 桶负载
			int bucketLoad = 32;
			for (int i = 0; i < hashs.length; i++) {
				int bindex = hashs[i];
				// 定位桶
				int bucketByteSize = this.bucketLength * bindex;
				// 取此桶当前负载数
				int tempBucketCount = countBucketLoad(countBuf[i][bindex],
						tableBuf[i][bucketByteSize + fingerBitsSize]);
				// 负载比较
				if (tempBucketCount < bucketLoad) {
					bucketLoad = tempBucketCount;
					minCellIndex = tableBuf[i][bucketByteSize + fingerBitsSize];
					minBucketIndex = bindex;
					tableIndex = i;
				}
			} // end for

			// 确定手印值
			ByteBuffer buf = ByteBuffer.allocate(4);
			buf.putInt(info.getFingerValue());
			buf.flip();
			byte[] fingerPrint = buf.array();

			byte cellCount = tableBuf[tableIndex][this.bucketLength
					* minBucketIndex + fingerBitsSize];
			if (cellCount == 8) {
				LOG.error("add the key:" + key
						+ "  wrong and the cell num exceed  8");
			} else {
				// 定位后，把手印放进去
				int destIndex = this.bucketLength * minBucketIndex
						+ minCellIndex * 3;
				tableBuf[tableIndex][destIndex] = fingerPrint[3];
				tableBuf[tableIndex][destIndex + 1] = fingerPrint[2];
				tableBuf[tableIndex][destIndex + 2] = fingerPrint[1];
				// cellCount + 1
				tableBuf[tableIndex][this.bucketLength * minBucketIndex
						+ fingerBitsSize] = (byte) (cellCount + 1);
			}
			// c[tableIndex]++;
		}
	}

	/**
	 * 参数
	 * 
	 * @param key
	 */
	public void delete(String key) {
		if (key == null) {
			throw new NullPointerException("Key may not be null");
		}
		if (keyIsLowerCase) {
			key = key.toLowerCase();
		}
		HashInfo info = membershipExist(key);
		if (!info.isExist()) {
			throw new IllegalArgumentException("Key is not a member");
		}

		// 删除处理
		int tableIndex = info.getTableIndex();
		int bucketIndex = info.getBucketIndex();
		int cellIndex = info.getCellIndex();

		// 暂不考虑并发带来的问题
		short count = countBuf[tableIndex][bucketIndex];
		// 右移2*cell位，&03,定位cellIndex对应的Count
		short cellCount = (short) ((count >> (2 * cellIndex)) & 0x3);
		// 减1;
		if (count >= 1) {
			cellCount--;
			count = (short) (count | (cellCount << 2));
			countBuf[tableIndex][bucketIndex] = count;
		} else {
			// 删除 定位
			int destIndex = bucketIndex * this.bucketLength + cellIndex * 3;
			System.arraycopy(DELBYTES, 0, tableBuf[tableIndex], destIndex, 3);
		}
	}// end del

	public static void main(String[] args) {

		DLeftBloomFilter df = new DLeftBloomFilter(500000);

		df.add("junit.com-27269");

		int tcount = 0;
		int fcount = 0;
		for (int i = 0; i < 10000; i++) {
			if (df.membershipTest(String.valueOf(i))) {
				tcount++;
			} else {
				fcount++;
			}
		}
		System.out.println("before add tcount:" + tcount + " fcount:" + fcount);

		df.add("a");
		df.add("a");

		int begin = 100000;
		int end = 300000;
		for (int i = begin; i < end; i++) {
			df.add(String.valueOf(i));
		}

		for (int i = 0; i < 4; i++) {
			// System.out.println("table "+i+":"+df.c[i]);
		}

		// /**
		// add 后
		tcount = 0;
		fcount = 0;
		for (int i = begin; i < end; i++) {
			if (df.membershipTest(String.valueOf(i))) {
				tcount++;
			} else {
				fcount++;
			}
		}
		System.out.println("after add tcount:" + tcount + " fcount:" + fcount);

		for (int i = begin; i < end; i++) {
			df.delete(String.valueOf(i));
		}
		tcount = 0;
		fcount = 0;
		for (int i = begin; i < (end + 100000); i++) {
			if (df.membershipTest(String.valueOf(i))) {
				tcount++;
			} else {
				fcount++;
			}
		}
		System.out.println("after delete tcount:" + tcount + " fcount:"
				+ fcount);

		// **/
		String domain = "heroyang.com";
		long t1 = System.nanoTime();
		df.add(domain);
		long t2 = System.nanoTime();

		boolean result = df.membershipTest(domain);
		long t3 = System.nanoTime();

		df.delete(domain);
		long t4 = System.nanoTime();

		System.out.println("membershipTest1:" + result);

		System.out.println("membershipTest2:" + df.membershipTest(domain));

		System.out.println("add domain time:" + (t2 - t1));
		System.out.println("membershipTest domain time:" + (t3 - t2));
		System.out.println("delete domain time:" + (t4 - t3));

	}

}
