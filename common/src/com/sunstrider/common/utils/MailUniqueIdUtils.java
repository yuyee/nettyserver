/**
 * @(#)MailUniqueIdUtils.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.utils;

import java.nio.ByteBuffer;

/**
 * 生成HMail系统对外服务的邮件唯一标识
 * <p>
 * 用于: POP3, IMAP, WEB等对外服务
 * <p>
 * 使用了MINA-2.0版本中的BASE64编解码相关代码, 但是为了避开http url中的特殊字符, 对base64算法做了以下修改:
 * <li>限定需要编码的字节数一定能被3整除, 废弃BASE64中的填充字符'='<br>
 * <li>废弃BASE64中的'+'字符, 改用'*'字符
 * <li>废弃BASE64中的'/'字符, 改用'-'字符
 */
public final class MailUniqueIdUtils {

    /**
     * 用于计算经过BASE64编码后的数据块
     * <p>
     * 每3个原始byte对应24个bit，对应编码后的4个byte
     */
    static final int TWENTYFOURBITGROUP = 24;

    /**
     * Used to test the sign of a byte.
     */
    static final int SIGN = -128;

    /**
     * byte --> base64 char
     */
    static final int BASELENGTH = 255;

    /**
     * base64 char --> byte
     */
    static final int LOOKUPLENGTH = 64;

    /** Hmail格式的Base64字符表单, 注意使用'-' 替代 '/' */
    private static byte[] hmailBase64Alphabet = new byte[BASELENGTH];

    /** Hmail格式的Base64字符查找表单, 注意使用'-' 替代 '/' */
    private static char[] lookUpHmailBase64Alphabet = new char[LOOKUPLENGTH];

    // Populating the lookup and character arrays
    static {
        for (int i = 0; i < BASELENGTH; i++) {
            hmailBase64Alphabet[i] = (byte) -1;
        }
        for (int i = 'Z'; i >= 'A'; i--) {
            hmailBase64Alphabet[i] = (byte) (i - 'A');
        }
        for (int i = 'z'; i >= 'a'; i--) {
            hmailBase64Alphabet[i] = (byte) (i - 'a' + 26);
        }

        hmailBase64Alphabet['*'] = 52;
        hmailBase64Alphabet['-'] = 53;

        for (int i = '9'; i >= '0'; i--) {
            hmailBase64Alphabet[i] = (byte) (i - '0' + 54);
        }

        for (int i = 0; i <= 25; i++) {
            lookUpHmailBase64Alphabet[i] = (char) ('A' + i);
        }

        for (int i = 26, j = 0; i <= 51; i++, j++) {
            lookUpHmailBase64Alphabet[i] = (char) ('a' + j);
        }

        lookUpHmailBase64Alphabet[52] = (char) '*';
        lookUpHmailBase64Alphabet[53] = (char) '-';

        for (int i = 54, j = 0; i <= 63; i++, j++) {
            lookUpHmailBase64Alphabet[i] = (char) ('0' + j);
        }

    }

    /** 构造函数 */
    private MailUniqueIdUtils() {};

    /**
     * 对原始字节数据进行特殊的BASE64编码
     * 
     * @param binaryData
     *            字节数据 长度必须能被3整除
     * @return BASE64编码后的字符数组
     */
    private static char[] encodeHmailBase64(byte[] binaryData) {

        if (binaryData.length % 3 != 0) { // 判断数组长度是否能被3整除
            throw new RuntimeException(
                "binaryData length must be divisible by 3");
        }

        int lengthDataBits = binaryData.length << 3;
        int numberTriplets = lengthDataBits / TWENTYFOURBITGROUP;
        int encodedDataLength = numberTriplets << 2;
        char[] encodedData = new char[encodedDataLength];
        byte k = 0, l = 0, b1 = 0, b2 = 0, b3 = 0;

        int encodedIndex = 0;
        int dataIndex = 0;
        int i = 0;

        // log.debug("number of triplets = " + numberTriplets);
        for (i = 0; i < numberTriplets; i++) {
            dataIndex = i * 3;
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            b3 = binaryData[dataIndex + 2];

            // log.debug("b1= " + b1 +", b2= " + b2 + ", b3= " + b3);

            l = (byte) (b2 & 0x0f);
            k = (byte) (b1 & 0x03);

            byte val1 = ((b1 & SIGN) == 0) ? (byte) (b1 >> 2)
                : (byte) ((b1) >> 2 ^ 0xc0);
            byte val2 = ((b2 & SIGN) == 0) ? (byte) (b2 >> 4)
                : (byte) ((b2) >> 4 ^ 0xf0);
            byte val3 = ((b3 & SIGN) == 0) ? (byte) (b3 >> 6)
                : (byte) ((b3) >> 6 ^ 0xfc);

            encodedData[encodedIndex] = lookUpHmailBase64Alphabet[val1];
            encodedData[encodedIndex + 1] = lookUpHmailBase64Alphabet[val2
                | (k << 4)];
            encodedData[encodedIndex + 2] = lookUpHmailBase64Alphabet[(l << 2)
                | val3];
            encodedData[encodedIndex + 3] = lookUpHmailBase64Alphabet[b3 & 0x3f];

            encodedIndex += 4;
        }

        return encodedData;
    }

    /**
     * 将特殊BASE64编码后的字符转换为原始二进制数据
     * 
     * @param base64Data
     *            经过BASE64编码后的字符数组
     * @return 原始二进制数据
     */
    private static byte[] decodeHmailBase64(char[] base64Data) {

        // handle the edge case, so we don't have to worry about it later
        if (base64Data.length == 0) {
            return new byte[0];
        }

        int numberQuadruple = base64Data.length >> 2;
        byte[] decodedData = new byte[base64Data.length - numberQuadruple];
        byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;

        int encodedIndex = 0;
        int dataIndex = 0;
        for (dataIndex = 0; dataIndex < base64Data.length; dataIndex += 4) {

            b1 = hmailBase64Alphabet[base64Data[dataIndex]];
            b2 = hmailBase64Alphabet[base64Data[dataIndex + 1]];
            b3 = hmailBase64Alphabet[base64Data[dataIndex + 2]];
            b4 = hmailBase64Alphabet[base64Data[dataIndex + 3]];

            decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
            decodedData[encodedIndex + 1] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
            decodedData[encodedIndex + 2] = (byte) (b3 << 6 | b4);

            encodedIndex += 3;
        }

        if (dataIndex != base64Data.length) { // 需要解码的字节数组长度必须能被4整除
            throw new RuntimeException(
                "encodedData length must be divisible by 4");
        }

        return decodedData;
    }

    /** MAGIC数值 */
    private static final byte MAGIC = (byte) 0xAA;

    /** 计算MD5的附加Key */
    private static final byte[] MD5_SEED = new byte[] { '3', 'g', 'o', '8',
        '&', '$', '8', '*', '3', '*', '3', 'h', '0', 'k', '(' };

    /**
     * 根据邮筒信息和邮件ID, 创建一个邮件的唯一标识
     * 
     * @param mailboxId
     *            邮筒ID
     * @param mailId
     *            邮件ID
     * @param mbDigest
     *            邮筒标识摘要(能基本保证不同Hmail内邮筒摘要不同)
     * @return 邮件唯一标识
     */
    public static String createUniqueId(long mailboxId, long mailId,
        byte[] mbDigest) {
        /**
         * 唯一标识创建算法:<br>
         * 1. 准备一个空白数组seed，长度为: 32 + mbDigest字节块长度(mbDigest为null时, 默认长度为0);
         * 一个18字节的空白数组buf<br>
         * 2. mailId --> 8字节, 填充到seed的0-7<br>
         * 3. mailboxId --> 8字节, 填充到seed的8-15<br>
         * 4. MD5_SEED --> 8字节, 填充到seed的16-31<br>
         * 5. 如果mbDigest不为null, 则填充到seed的32-末尾<br>
         * 6. 使用seed数组内容, 通过MD5获取一个摘要值md5Digest数组. 注意: MD5摘要值有128位<br>
         * 7. 将md5Digest数组中的末尾9个字节按顺序填充到buf的1,3,5,7,9,11,13,15,17<br>
         * 8. buf[16]填充MAGIC值<br>
         * 9. 将buf数组内的数据经过hmail的base64计算获取唯一标识字符串<br>
         */

        byte[] seed;
        if (mbDigest == null) {
            seed = new byte[32];
        } else {
            seed = new byte[32 + mbDigest.length];
            System.arraycopy(mbDigest, 0, seed, 32, mbDigest.length);
        }
        ByteBuffer bb = ByteBuffer.wrap(seed);
        bb.putLong(mailId);
        bb.putLong(mailboxId);
        bb.put(MD5_SEED);

        byte[] md5Digest = Md5Utils.md5(seed);

        byte[] buf = new byte[18];
        buf[0] = seed[0];
        buf[1] = md5Digest[7];
        buf[2] = seed[1];
        buf[3] = md5Digest[8];
        buf[4] = seed[2];
        buf[5] = md5Digest[9];
        buf[6] = seed[3];
        buf[7] = md5Digest[10];
        buf[8] = seed[4];
        buf[9] = md5Digest[11];
        buf[10] = seed[5];
        buf[11] = md5Digest[12];
        buf[12] = seed[6];
        buf[13] = md5Digest[13];
        buf[14] = seed[7];
        buf[15] = md5Digest[14];
        buf[16] = MAGIC;
        buf[17] = md5Digest[15];

        char[] cbuf = encodeHmailBase64(buf);
        return (new String(cbuf));
    }

    /**
     * 根据邮件唯一标识和邮筒信息, 解析出对应的邮件ID
     * 
     * @param mid
     *            邮件唯一标识
     * @param mailboxId
     *            邮筒ID
     * @param mbDigest
     *            邮筒标识摘要(能基本保证不同Hmail内邮筒摘要不同)
     * @return 邮件ID
     * @throws IllegalArgumentException
     *             邮件标识不合法
     */
    public static long parseUniqueId(String mid, long mailboxId, byte[] mbDigest)
        throws IllegalArgumentException {

        /**
         * 唯一标识解析算法:<br>
         * 1. 将唯一标识经过hmail的base64解码获得一个18字节的buf数组<br>
         * 2. 判断buf[16]是否与MAGIC数值一致<br>
         * 3. 创建一个空白数组seed, 长度为: 32 + mbDigest字节块长度(mbDigest为null时, 默认长度为0)<br>
         * 4. 将buf的0,2,4,6,8,10,12,14位置字节按顺序填充到seed的前8位<br>
         * 5. mailboxId --> 8字节, 填充到seed的8-15<br>
         * 6. MD5_SEED --> 8字节, 填充到seed的16-31<br>
         * 7. 如果mbDigest不为null, 则填充到seed的32-末尾<br>
         * 8. 使用seed数组内容, 通过MD5获取一个摘要值digest数组<br>
         * 9. 从digest[7]开始的9个字节按顺序分别与buf的第1,3,5,7,9,11,13,15,17字节比较是否一致<br>
         * 10. 如果上一步校验成功, 则把seed中0-7的字节转换为long数值, 即邮件的ID<br>
         */

        if (mid.length() != 24) {
            throw new IllegalArgumentException("mid");
        }

        byte[] buf = decodeHmailBase64(mid.toCharArray());
        if (buf[16] != MAGIC) {
            throw new IllegalArgumentException("mid");
        }

        byte[] seed;
        if (mbDigest == null) {
            seed = new byte[32];
        } else {
            seed = new byte[32 + mbDigest.length];
            System.arraycopy(mbDigest, 0, seed, 32, mbDigest.length);
        }
        ByteBuffer bb = ByteBuffer.wrap(seed);
        seed[0] = buf[0];
        seed[1] = buf[2];
        seed[2] = buf[4];
        seed[3] = buf[6];
        seed[4] = buf[8];
        seed[5] = buf[10];
        seed[6] = buf[12];
        seed[7] = buf[14];
        bb.putLong(8, mailboxId);
        bb.position(16);
        bb.put(MD5_SEED);

        byte[] digest = Md5Utils.md5(seed);
        if (digest[7] == buf[1] && digest[8] == buf[3] && digest[9] == buf[5]
            && digest[10] == buf[7] && digest[11] == buf[9]
            && digest[12] == buf[11] && digest[13] == buf[13]
            && digest[14] == buf[15] && digest[15] == buf[17]) {
            return ByteUtils.toLong(seed);
        } else {
            throw new IllegalArgumentException("mid");
        }

    }

    /**
     * 根据邮件唯一标识获取标识对应的邮件ID(仅供管理工具使用)
     * <p>
     * 注意: 与{@link #parseUniqueId(String, long, byte[])}方法相比,
     * 本方法不会去校验邮件ID是否与唯一标识中指定邮筒的关联关系
     * 
     * @param mid
     *            邮件唯一标识
     * @return 邮件ID
     * @throws IllegalArgumentException
     *             邮件标识不合法
     */
    public static long getMailIdFromUniqueId(String mid)
        throws IllegalArgumentException {

        /**
         * 解析算法:<br>
         * 1. 将唯一标识经过hmail的base64解码获得一个18字节的buf数组<br>
         * 2. 判断buf[16]是否与MAGIC数值一致<br>
         * 3. 创建一个空白数组seed, 长度为: 8<br>
         * 4. 将buf的0,2,4,6,8,10,12,14位置字节按顺序填充到seed的前8位<br>
         * 5. 把seed中0-7的字节转换为long数值, 即邮件的ID<br>
         */

        if (mid.length() != 24) {
            throw new IllegalArgumentException("mid");
        }

        byte[] buf = decodeHmailBase64(mid.toCharArray());
        if (buf[16] != MAGIC) {
            throw new IllegalArgumentException("mid");
        }

        byte[] seed = new byte[8];
        seed[0] = buf[0];
        seed[1] = buf[2];
        seed[2] = buf[4];
        seed[3] = buf[6];
        seed[4] = buf[8];
        seed[5] = buf[10];
        seed[6] = buf[12];
        seed[7] = buf[14];

        return ByteUtils.toLong(seed);
    }

    public static void main(String[] args) {
        long mailIdFromUniqueId = getMailIdFromUniqueId("ACsAlgArAJcAxRgUPwQOJKpB");
        System.out.println(mailIdFromUniqueId);
    }
}
