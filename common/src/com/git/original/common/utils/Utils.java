package com.git.original.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author linaoxiang
 * @Created 2011-3-21
 */
public final class Utils {

	/**
	 * HMail系统全局请求事务UID长度
	 */
	public static final int HMAIL_GLOBAL_TRANSACTION_UID_LENGTH = 32;

	private static final long BASE16MASK = 0xf000000000000000L;

	private static final int IP16MASK = 0xf0000000;

	private static final char[] BASE16MAP = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * 根据本地服务器上的session id及本地服务器的ip产生全局唯一的会话ID
	 * <p>
	 * 格式: <code>XXYYYYYYYYYYZZZZv...v</code>
	 * <ul>
	 * <li>XX = 服务编号(定长=2)
	 * <li>YYYYYYYYYY = 毫秒单位的时间戳(定长=10)
	 * <li>ZZZZ = 内网IPv4地址低16位的值(定长=4)
	 * <li>v...v = 当前服务的连接通道id(不定长)
	 * <ul>
	 * 
	 * @param localSessionId
	 *            服务器上当前sessionid
	 * @param ip
	 *            当前服务器ip
	 * @return 全局唯一的sessionid
	 */
	public static final String getGlobalSessionId(long localSessionId, int ip) {
		long millis = System.currentTimeMillis();

		StringBuilder idBuf = new StringBuilder(32);

		// 第二部分: 时间戳(定长=10)
		for (int i = 6; i < 16; i++) { // 时间戳取40位
			int index = (int) (((BASE16MASK >>> (i * 4)) & millis) >>> (64 - 4 * (i + 1)));
			char c = BASE16MAP[index];
			idBuf.append(c);
		}

		// 第三部分: 后半部分的IP地址(定长=4)
		for (int i = 4; i < 8; i++) {
			int index = (int) (((IP16MASK >>> i * 4) & ip) >>> (32 - 4 * (i + 1)));
			idBuf.append(BASE16MAP[index]);
		}

		// 第四部分: 当前服务的连接通道id(不定长)
		boolean writen = false;
		for (int i = 0; i < 16; i++) {
			int index = (int) (((BASE16MASK >>> (i * 4)) & localSessionId) >>> (64 - 4 * (i + 1)));
			char c = BASE16MAP[index];
			if (c != '0' || writen) {
				idBuf.append(c);
				writen = true;
			}
		}

		return idBuf.toString();
	}

	/** '2001-07-04' yyyy-MM-dd 日期格式正则表达式 */
	private static final Pattern datePattern = Pattern
			.compile("(\\d{1,4})-(\\d{1,2})-(\\d{1,2})");

	/** '12:08:56' HH:mm:ss 时间格式正则表达式 */
	private static final Pattern timePattern = Pattern
			.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})");

	/**
	 * 解析日期时间格式的参数
	 * <p>
	 * 支持以下格式字符串
	 * <li><i>yyyy-MM-dd</i><b>T</b><i>HH:mm:ss</i> = 完整的日期和时间, 示例:
	 * 2001-07-04T12:08:56
	 * <li><i>yyyy-MM-dd</i> = 指定日期(时间视为为00:00:00), 示例: 2001-07-04
	 * <li><i>HH:mm:ss</i> = 指定时间(指离当前时间最接近的时刻), 示例: 12:08:56
	 * 
	 * @param value
	 *            日期时间字符串
	 * @return
	 * @throws ParseException
	 */
	public static final synchronized Date parseDateConfigParam(String value)
			throws ParseException {
		if (value == null) {
			return new Date();
		}

		value = value.trim();
		if (value.isEmpty()) {
			return new Date();
		}

		// 时间字符串起始位置
		int timeStartPos = 0;

		Calendar cal = Calendar.getInstance();
		Matcher matcher = datePattern.matcher(value);
		if (matcher.find()) {
			if (matcher.start() != 0) {
				throw new ParseException("date value is illegal format", 0);
			}

			if (matcher.group(1) != null) {
				cal.set(Calendar.YEAR, Integer.parseInt(matcher.group(1)));
			}
			if (matcher.group(2) != null) { // 注意: Calendar.MONTH值从0开始
				cal.set(Calendar.MONTH, Integer.parseInt(matcher.group(2)) - 1);
			}
			if (matcher.group(3) != null) {
				cal.set(Calendar.DAY_OF_MONTH,
						Integer.parseInt(matcher.group(3)));
			}

			timeStartPos = matcher.end();
			if (timeStartPos < value.length()
					&& value.charAt(timeStartPos) == 'T') {
				timeStartPos++;
			}
		}

		if (timeStartPos >= value.length()) { // 没有设定时间参数, 强制默认时间为00:00:00
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		} else {
			matcher = timePattern.matcher(value);
			if (matcher.find(timeStartPos)) {
				if (matcher.start() != timeStartPos) {
					throw new ParseException("time value is illegal format",
							timeStartPos);
				}

				if (matcher.group(1) != null) {
					cal.set(Calendar.HOUR_OF_DAY,
							Integer.parseInt(matcher.group(1)));
				}
				if (matcher.group(2) != null) {
					cal.set(Calendar.MINUTE, Integer.parseInt(matcher.group(2)));
				}
				if (matcher.group(3) != null) {
					cal.set(Calendar.SECOND, Integer.parseInt(matcher.group(3)));
				}
			} else {
				throw new ParseException("time value is illegal format",
						timeStartPos);
			}
		}

		if (timeStartPos == 0
				&& cal.getTimeInMillis() < System.currentTimeMillis()) {
			// 如果只设置了时间参数, 并且指定的时间小于当前时间, 则自动视为下一天的同一时间
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}

		return cal.getTime();
	}

	/**
	 * RFC822时间格式
	 */
	private static SimpleDateFormat rfc822DateFormat = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

	/**
	 * 将long类型的毫米单位时间值格式为RFC822格式的时间串
	 * <p>
	 * 线程安全
	 * 
	 * @param time
	 *            毫米单位时间值
	 * @return RFC822格式的时间串
	 */
	public static String formatRfc822Date(long time) {
		Date dt = new Date(time);

		StringBuilder sb = new StringBuilder(48);
		synchronized (rfc822DateFormat) {
			sb.append(rfc822DateFormat.format(dt));
		}

		TimeZone tz = TimeZone.getDefault();
		sb.append(' ')
				.append('(')
				.append(tz.getDisplayName(tz.inDaylightTime(dt),
						TimeZone.SHORT, Locale.ENGLISH)).append(')');

		return sb.toString();
	}

	/**
	 * 读取指定你个长度的文件内容存放到数组内,并返回
	 * 
	 * @param src
	 *            源文件
	 * @param size
	 *            目标长度
	 * @throws IOException
	 */
	public static byte[] readFromFile(File src, Long size) throws IOException {
		FileInputStream fin = null;

		try {
			fin = new FileInputStream(src);
			byte[] buf = new byte[(int) (size == null || size.intValue() < 0 ? src
					.length() : size.intValue())];

			int total = buf.length;
			int offset = 0;
			int readed;
			int maxZeroCount = 100; // 最多允许出现连续100次0字节传输的情况发生
			while (total > 0) {
				readed = fin.read(buf, offset, total);

				if (readed < 0 || maxZeroCount <= 0) {
					throw new IOException("read file failed, path:" + src);
				} else if (readed == 0) {
					--maxZeroCount;
				} else {
					maxZeroCount = 100; // 重置
				}

				offset += readed;
				total -= readed;
			}

			return buf;
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 复制文件
	 * 
	 * @param src
	 *            源文件
	 * @param dest
	 *            目标文件
	 * @throws IOException
	 */
	public static void copyFile(File src, File dest) throws IOException {
		FileInputStream fin = null;
		FileOutputStream fos = null;

		try {
			fin = new FileInputStream(src);
			fos = new FileOutputStream(dest);

			long total = src.length();
			if (total == 0) {
				if (!dest.createNewFile()) {
					throw new IOException("create empty file failed, path:"
							+ dest);
				}
			}

			int maxZeroCount = 100; // 最多允许出现连续100次0字节传输的情况发生
			while (total > 0) {
				long transCount = fin.getChannel().transferTo(0, src.length(),
						fos.getChannel());
				if (transCount < 0 || maxZeroCount <= 0) {
					throw new IOException("copy file failed, path:" + dest);
				} else if (transCount == 0) {
					--maxZeroCount;
				} else {
					maxZeroCount = 100; // 重置
				}

				total -= transCount;
			}

		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 从一个邮件地址中获取域名部分的内容
	 * 
	 * @param emailAddress
	 * @return
	 */
	public static String getDomainPart(String emailAddress) {
		if (emailAddress == null)
			return null;

		int pos = emailAddress.lastIndexOf('@');
		if (pos < 0 || pos + 1 >= emailAddress.length()) {
			return null;
		}

		return emailAddress.substring(pos + 1).trim();
	}

	/**
	 * 对指定的特殊字符进行转义
	 * <p>
	 * 特殊字符包含以下:
	 * <ul>
	 * <li>'\'
	 * <li>'='
	 * <li>':'
	 * <li>';'
	 * <li>'\r'
	 * <li>'\n'
	 * </ul>
	 * 
	 * @param src
	 *            原始字符串
	 * @return 转义后的字符串
	 */
	public static final String escapeSpecialChars(String src) {
		if (src == null || src.isEmpty())
			return src;

		StringBuilder sb = null;

		char[] ca = src.toCharArray();
		for (int i = 0; i < ca.length; i++) {
			char c = ca[i];
			if (Character.isHighSurrogate(c) && i < ca.length - 1
					&& Character.isLowSurrogate(ca[i + 1])) {
				if (sb == null) {
					sb = new StringBuilder(src.length() + 1);
					if (i > 0) {
						sb.append(ca, 0, i);
					}
				}

				sb.append("\\u")
						.append(HexUtils.toHexString(ByteUtils
								.getAsBytes((short) c)))
						.append("\\u")
						.append(HexUtils.toHexString(ByteUtils
								.getAsBytes((short) ca[i + 1])));

				i++; // skip one char
				continue;
			}

			switch (c) {
			case '=':
			case ':':
			case ',':
			case '\\':
				if (sb == null) {
					sb = new StringBuilder(src.length() + 1);
					if (i > 0) {
						sb.append(ca, 0, i);
					}
				}

				sb.append('\\').append(c);
				break;
			case '\r':
				if (sb == null) {
					sb = new StringBuilder(src.length() + 1);
					if (i > 0) {
						sb.append(ca, 0, i);
					}
				}

				sb.append('\\').append('r');
				break;
			case '\n':
				if (sb == null) {
					sb = new StringBuilder(src.length() + 1);
					if (i > 0) {
						sb.append(ca, 0, i);
					}
				}

				sb.append('\\').append('n');
				break;
			default:
				if (sb != null) {
					sb.append(c);
				}
			}
		}

		if (sb == null)
			return src;

		return sb.toString();
	}

	/**
	 * 用于生成"Message-Id"的时间戳格式
	 */
	private static SimpleDateFormat MESSAGE_ID_DATE_FORMAT = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	/**
	 * 用于创建Postfix格式的"Message-Id"
	 * 
	 * @param queueId
	 *            Postfix队列ID
	 * @param hostname
	 *            Postfix当前服务器名称
	 * @return Message-Id信头值
	 */
	public static String createMessageId(String queueId, String hostname) {
		StringBuilder sb = new StringBuilder(80);
		sb.append('<');

		synchronized (Utils.MESSAGE_ID_DATE_FORMAT) {// 避免并发问题
			sb.append(Utils.MESSAGE_ID_DATE_FORMAT.format(new Date()));
		}

		if (queueId == null) {
			sb.append('.')
					.append(HexUtils.toHexString(ByteUtils.getAsBytes(System
							.nanoTime())));
		} else {
			sb.append('.').append(queueId);
		}

		if (hostname == null) {
			sb.append('@').append("unknown");
		} else {
			sb.append('@').append(hostname);
		}

		return sb.append('>').toString();
	}

}
