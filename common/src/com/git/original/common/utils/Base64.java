/**
 * @(#)Base64.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.utils;

/**
 * Base64的编解码类
 * 
 * @author linaoxiang
 */
public final class Base64 {

	/** 构造函数 */
	private Base64() {
	};

	/** 编码常量表: 结果为字节类型数据 */
	private static final byte[] encodingTable = { (byte) 'A', (byte) 'B',
			(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G',
			(byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
			(byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q',
			(byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V',
			(byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a',
			(byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f',
			(byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k',
			(byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p',
			(byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u',
			(byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z',
			(byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
			(byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9',
			(byte) '+', (byte) '/' };

	/** 解码常量表 */
	private static final byte[] decodingTable;
	static {
		decodingTable = new byte[128];
		for (int i = 0; i < 128; i++) {
			decodingTable[i] = (byte) -1;
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			decodingTable[i] = (byte) (i - 'A');
		}
		for (int i = 'a'; i <= 'z'; i++) {
			decodingTable[i] = (byte) (i - 'a' + 26);
		}
		for (int i = '0'; i <= '9'; i++) {
			decodingTable[i] = (byte) (i - '0' + 52);
		}
		decodingTable['+'] = 62;
		decodingTable['/'] = 63;
	}

	/** 编码常量表: 结果为字符类型数据 */
	private static final char[] lookUpBase64Alphabet = new char[64];
	static {
		for (int i = 0; i <= 25; i++) {
			lookUpBase64Alphabet[i] = (char) ('A' + i);
		}

		for (int i = 26, j = 0; i <= 51; i++, j++) {
			lookUpBase64Alphabet[i] = (char) ('a' + j);
		}

		for (int i = 52, j = 0; i <= 61; i++, j++) {
			lookUpBase64Alphabet[i] = (char) ('0' + j);
		}
		lookUpBase64Alphabet[62] = (char) '+';
		lookUpBase64Alphabet[63] = (char) '/';
	}

	/**
	 * 将给定数据进行Base64编码
	 * 
	 * @param data
	 *            要进行Base64编码的数据
	 * @return Base64编码后的数据
	 */
	public static byte[] encode(byte[] data) {
		byte[] bytes;
		int modulus = data.length % 3;
		if (modulus == 0) {
			bytes = new byte[(4 * data.length) / 3];
		} else {
			bytes = new byte[4 * ((data.length / 3) + 1)];
		}
		int dataLength = (data.length - modulus);
		int a1;
		int a2;
		int a3;
		for (int i = 0, j = 0; i < dataLength; i += 3, j += 4) {
			a1 = data[i] & 0xff;
			a2 = data[i + 1] & 0xff;
			a3 = data[i + 2] & 0xff;
			bytes[j] = encodingTable[(a1 >>> 2) & 0x3f];
			bytes[j + 1] = encodingTable[((a1 << 4) | (a2 >>> 4)) & 0x3f];
			bytes[j + 2] = encodingTable[((a2 << 2) | (a3 >>> 6)) & 0x3f];
			bytes[j + 3] = encodingTable[a3 & 0x3f];
		}
		int b1;
		int b2;
		int b3;
		int d1;
		int d2;
		switch (modulus) {
		case 0: /* nothing left to do */
			break;
		case 1:
			d1 = data[data.length - 1] & 0xff;
			b1 = (d1 >>> 2) & 0x3f;
			b2 = (d1 << 4) & 0x3f;
			bytes[bytes.length - 4] = encodingTable[b1];
			bytes[bytes.length - 3] = encodingTable[b2];
			bytes[bytes.length - 2] = (byte) '=';
			bytes[bytes.length - 1] = (byte) '=';
			break;
		case 2:
			d1 = data[data.length - 2] & 0xff;
			d2 = data[data.length - 1] & 0xff;
			b1 = (d1 >>> 2) & 0x3f;
			b2 = ((d1 << 4) | (d2 >>> 4)) & 0x3f;
			b3 = (d2 << 2) & 0x3f;
			bytes[bytes.length - 4] = encodingTable[b1];
			bytes[bytes.length - 3] = encodingTable[b2];
			bytes[bytes.length - 2] = encodingTable[b3];
			bytes[bytes.length - 1] = (byte) '=';
			break;
		default:
			// nothing todo
			break;
		}
		return bytes;
	}

	/**
	 * 将给定数据进行Base64编码
	 * 
	 * @param data
	 *            要进行Base64编码的数据
	 * @param off
	 *            有效字节起始偏移量
	 * @param len
	 *            有效字节长度
	 * @return Base64编码后的数据
	 */
	public static byte[] encode(byte[] data, int off, int len) {
		byte[] bytes = new byte[((len + 2) / 3) << 2];

		int val;
		int inpos, outpos;
		int size = len;

		for (inpos = off, outpos = 0; size >= 3; size -= 3, outpos += 4) {
			val = data[inpos++] & 0xff;
			val <<= 8;
			val |= data[inpos++] & 0xff;
			val <<= 8;
			val |= data[inpos++] & 0xff;

			bytes[outpos + 3] = (byte) encodingTable[val & 0x3f];
			val >>= 6;
			bytes[outpos + 2] = (byte) encodingTable[val & 0x3f];
			val >>= 6;
			bytes[outpos + 1] = (byte) encodingTable[val & 0x3f];
			val >>= 6;
			bytes[outpos + 0] = (byte) encodingTable[val & 0x3f];
		}

		switch (size) {
		case 0: /* nothing left to do */
			break;
		case 1:
			val = data[inpos++] & 0xff;
			val <<= 4;

			bytes[outpos + 3] = (byte) '='; // pad character;
			bytes[outpos + 2] = (byte) '='; // pad character;
			bytes[outpos + 1] = (byte) encodingTable[val & 0x3f];
			val >>>= 6;
			bytes[outpos + 0] = (byte) encodingTable[val & 0x3f];

			break;
		case 2:
			val = data[inpos++] & 0xff;
			val <<= 8;
			val |= data[inpos++] & 0xff;
			val <<= 2;

			bytes[outpos + 3] = (byte) '='; // pad character;
			bytes[outpos + 2] = (byte) encodingTable[val & 0x3f];
			val >>>= 6;
			bytes[outpos + 1] = (byte) encodingTable[val & 0x3f];
			val >>>= 6;
			bytes[outpos + 0] = (byte) encodingTable[val & 0x3f];
			break;
		default:
			// nothing todo
			break;
		}
		return bytes;
	}

	/**
	 * 对给定的数据进行Base64解码
	 * <p>
	 * 注意: 能够自动清理输入参数中的非base64字符
	 * 
	 * @param data
	 *            需要进行Base64解码的数据，字节数组
	 * @return Base64解码后的数据
	 */
	public static byte[] decode(byte[] data) {
		byte[] bytes;
		byte b1;
		byte b2;
		byte b3;
		byte b4;
		data = discardNonBase64Bytes(data);

		if (data[data.length - 2] == '=') {
			bytes = new byte[(((data.length / 4) - 1) * 3) + 1];
		} else if (data[data.length - 1] == '=') {
			bytes = new byte[(((data.length / 4) - 1) * 3) + 2];
		} else {
			bytes = new byte[((data.length / 4) * 3)];
		}
		for (int i = 0, j = 0; i < (data.length - 4); i += 4, j += 3) {
			b1 = decodingTable[data[i]];
			b2 = decodingTable[data[i + 1]];
			b3 = decodingTable[data[i + 2]];
			b4 = decodingTable[data[i + 3]];
			bytes[j] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[j + 1] = (byte) ((b2 << 4) | (b3 >> 2));
			bytes[j + 2] = (byte) ((b3 << 6) | b4);
		}
		if (data[data.length - 2] == '=') {
			b1 = decodingTable[data[data.length - 4]];
			b2 = decodingTable[data[data.length - 3]];
			bytes[bytes.length - 1] = (byte) ((b1 << 2) | (b2 >> 4));
		} else if (data[data.length - 1] == '=') {
			b1 = decodingTable[data[data.length - 4]];
			b2 = decodingTable[data[data.length - 3]];
			b3 = decodingTable[data[data.length - 2]];
			bytes[bytes.length - 2] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[bytes.length - 1] = (byte) ((b2 << 4) | (b3 >> 2));
		} else {
			b1 = decodingTable[data[data.length - 4]];
			b2 = decodingTable[data[data.length - 3]];
			b3 = decodingTable[data[data.length - 2]];
			b4 = decodingTable[data[data.length - 1]];
			bytes[bytes.length - 3] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[bytes.length - 2] = (byte) ((b2 << 4) | (b3 >> 2));
			bytes[bytes.length - 1] = (byte) ((b3 << 6) | b4);
		}
		return bytes;
	}

	/**
	 * 对给定的数据进行Base64解码 *
	 * <p>
	 * 注意: 能够自动清理输入参数中的非base64字符
	 * 
	 * @param data
	 *            需要进行Base64解码的数据，字符串
	 * @return Base64解码后的数据
	 */
	public static byte[] decode(String data) {
		char[] ca = discardNonBase64Chars(data);
		return decode(ca, 0, ca.length);
	}

	/**
	 * 对给定的数据进行Base64解码
	 * <p>
	 * 注意: 不会自动清理输入参数中的非base64字符
	 * 
	 * @param data
	 *            需要进行Base64解码的数据，字符串
	 * @return Base64解码后的数据
	 */
	public static byte[] decode(char[] ca, int pos, int length) {
		byte[] bytes;
		byte b1;
		byte b2;
		byte b3;
		byte b4;

		int end = pos + length;

		if (ca[end - 2] == '=') {
			bytes = new byte[(((length / 4) - 1) * 3) + 1];
		} else if (ca[end - 1] == '=') {
			bytes = new byte[(((length / 4) - 1) * 3) + 2];
		} else {
			bytes = new byte[((length / 4) * 3)];
		}
		for (int i = pos, j = 0; i < (end - 4); i += 4, j += 3) {
			b1 = decodingTable[ca[i]];
			b2 = decodingTable[ca[i + 1]];
			b3 = decodingTable[ca[i + 2]];
			b4 = decodingTable[ca[i + 3]];
			bytes[j] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[j + 1] = (byte) ((b2 << 4) | (b3 >> 2));
			bytes[j + 2] = (byte) ((b3 << 6) | b4);
		}
		if (ca[end - 2] == '=') {
			b1 = decodingTable[ca[end - 4]];
			b2 = decodingTable[ca[end - 3]];
			bytes[bytes.length - 1] = (byte) ((b1 << 2) | (b2 >> 4));
		} else if (ca[end - 1] == '=') {
			b1 = decodingTable[ca[end - 4]];
			b2 = decodingTable[ca[end - 3]];
			b3 = decodingTable[ca[end - 2]];
			bytes[bytes.length - 2] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[bytes.length - 1] = (byte) ((b2 << 4) | (b3 >> 2));
		} else {
			b1 = decodingTable[ca[end - 4]];
			b2 = decodingTable[ca[end - 3]];
			b3 = decodingTable[ca[end - 2]];
			b4 = decodingTable[ca[end - 1]];
			bytes[bytes.length - 3] = (byte) ((b1 << 2) | (b2 >> 4));
			bytes[bytes.length - 2] = (byte) ((b2 << 4) | (b3 >> 2));
			bytes[bytes.length - 1] = (byte) ((b3 << 6) | b4);
		}
		return bytes;
	}

	/**
	 * 将给定数据进行Base64编码, 并直接返回字符串
	 * 
	 * @param data
	 *            要进行Base64编码的数据
	 * @return Base64编码后的数据
	 */
	public static String encodeToString(byte[] data) {
		char[] encodedData;
		int modulus = data.length % 3;
		if (modulus == 0) {
			encodedData = new char[(4 * data.length) / 3];
		} else {
			encodedData = new char[4 * ((data.length / 3) + 1)];
		}
		int dataLength = (data.length - modulus);
		int a1;
		int a2;
		int a3;
		for (int i = 0, j = 0; i < dataLength; i += 3, j += 4) {
			a1 = data[i] & 0xff;
			a2 = data[i + 1] & 0xff;
			a3 = data[i + 2] & 0xff;
			encodedData[j] = lookUpBase64Alphabet[(a1 >>> 2) & 0x3f];
			encodedData[j + 1] = lookUpBase64Alphabet[((a1 << 4) | (a2 >>> 4)) & 0x3f];
			encodedData[j + 2] = lookUpBase64Alphabet[((a2 << 2) | (a3 >>> 6)) & 0x3f];
			encodedData[j + 3] = lookUpBase64Alphabet[a3 & 0x3f];
		}
		int b1;
		int b2;
		int b3;
		int d1;
		int d2;
		switch (modulus) {
		case 0: /* nothing left to do */
			break;
		case 1:
			d1 = data[data.length - 1] & 0xff;
			b1 = (d1 >>> 2) & 0x3f;
			b2 = (d1 << 4) & 0x3f;
			encodedData[encodedData.length - 4] = lookUpBase64Alphabet[b1];
			encodedData[encodedData.length - 3] = lookUpBase64Alphabet[b2];
			encodedData[encodedData.length - 2] = '=';
			encodedData[encodedData.length - 1] = '=';
			break;
		case 2:
			d1 = data[data.length - 2] & 0xff;
			d2 = data[data.length - 1] & 0xff;
			b1 = (d1 >>> 2) & 0x3f;
			b2 = ((d1 << 4) | (d2 >>> 4)) & 0x3f;
			b3 = (d2 << 2) & 0x3f;
			encodedData[encodedData.length - 4] = lookUpBase64Alphabet[b1];
			encodedData[encodedData.length - 3] = lookUpBase64Alphabet[b2];
			encodedData[encodedData.length - 2] = lookUpBase64Alphabet[b3];
			encodedData[encodedData.length - 1] = '=';
			break;
		default:
			// nothing todo
			break;
		}

		return new String(encodedData);
	}

	private static byte[] discardNonBase64Bytes(byte[] data) {
		byte[] validBuf = null;
		int bytesCopied = 0;

		for (int i = 0; i < data.length; i++) {
			if (isValidBase64Byte(data[i])) {
				if (validBuf != null) {
					validBuf[bytesCopied++] = data[i];
				}
			} else if (validBuf == null) {
				validBuf = new byte[data.length];

				System.arraycopy(data, 0, validBuf, 0, i);
				bytesCopied = i;
			}
		}

		if (validBuf != null) {
			byte[] newData = new byte[bytesCopied];
			System.arraycopy(validBuf, 0, newData, 0, bytesCopied);
			return newData;
		}

		return data;
	}

	private static char[] discardNonBase64Chars(String data) {
		char[] validBuf = null;
		int bytesCopied = 0;

		char[] ca = data.toCharArray();
		for (int i = 0; i < ca.length; i++) {
			if (isValidBase64Byte((byte) ca[i])) {
				if (validBuf != null) {
					validBuf[bytesCopied++] = ca[i];
				}
			} else if (validBuf == null) {
				validBuf = new char[ca.length];

				System.arraycopy(ca, 0, validBuf, 0, i);
				bytesCopied = i;
			}
		}

		if (validBuf != null) {
			char[] newData = new char[bytesCopied];
			System.arraycopy(validBuf, 0, newData, 0, bytesCopied);
			return newData;
		}

		return ca;
	}

	private static boolean isValidBase64Byte(byte b) {
		if (b == '=') {
			return true;
		} else if ((b < 0) || (b >= 128)) {
			return false;
		} else if (decodingTable[b] == -1) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		String data = "中华人民共和国";
		byte[] result = Base64.encode(data.getBytes());
		System.out.println(data);
		System.out.println(new String(result));
		System.out.println(new String(Base64.decode(new String(result))));
	}
}
