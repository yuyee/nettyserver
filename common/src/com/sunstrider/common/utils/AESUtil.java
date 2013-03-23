/**
 * @(#)AESUtil.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.utils;

import java.nio.ByteBuffer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-128 ECB模式加密解密 帮助类
 * <p>
 * 此帮助类有限制：1.密钥必须是16位的 2.待加密内容的长度必须是16的倍数，如果不是16的倍数，就会出javax.crypto.
 * IllegalBlockSizeException异常
 */
public final class AESUtil {

    /** 构造函数 */
    private AESUtil() {};

    /**
     * 加密
     * 
     * @param content
     *            內容
     * @param secretKey
     *            密钥
     * @return
     * @throws AESException
     */
    public static byte[] encrypt(byte[] content, byte[] secretKey)
        throws Exception {
        if (content == null || secretKey == null) {
            throw new NullPointerException(
                "content or secretKey should not be null");
        }

        if (content.length % 16 != 0) {
            throw new Exception("content.length is not a multiple of 16");
        }

        try {
            SecretKeySpec key = new SecretKeySpec(secretKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * 解密
     * 
     * @param content
     *            待解密内容
     * @param secretKey
     *            解密密钥
     * @return
     * @throws AESException
     */
    public static byte[] decrypt(byte[] content, byte[] secretKey)
        throws Exception {
        if (content == null || secretKey == null) {
            throw new NullPointerException(
                "content or secretKey should not be null");
        }

        try {
            SecretKeySpec key = new SecretKeySpec(secretKey, "AES");
            Cipher out = Cipher.getInstance("AES/ECB/NoPadding");
            out.init(Cipher.DECRYPT_MODE, key);
            byte[] result = out.doFinal(content);
            return result; //
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * 对称加密
     * 
     * @param content
     *            待加密内容
     * @param secretKey
     *            密文，必须16位
     * @return
     * @throws AESException
     */
    public static byte[] symmetricEncrypt(byte[] content, byte[] secretKey)
        throws Exception {
        if (content == null) {
            throw new NullPointerException("content is null");
        }

        if (secretKey == null) {
            throw new NullPointerException("secretKey is null");
        }

        if (secretKey.length != 16) {
            throw new NullPointerException(
                "secretKey.length is not be 16,please check");
        }

        int x = (-1 - content.length) & 0xf;
        byte[] gap = new byte[x];
        for (int i = 0; i < gap.length; i++) {
            gap[i] = 0;
        }
        ByteBuffer bb = ByteBuffer.allocate(1 + x + content.length);
        bb.put((byte) x).put(gap).put(content);
        bb.flip();
        byte[] result = AESUtil.encrypt(bb.array(), secretKey);
        return result;
    }

    /**
     * 对称解密
     * 
     * @param content
     * @param secretKey
     *            密文，必须16位
     * @return
     * @throws AESException
     */
    public static byte[] symmetricDecrypt(byte[] content, byte[] secretKey)
        throws Exception {
        byte[] src = AESUtil.decrypt(content, secretKey);
        ByteBuffer decSrc = ByteBuffer.wrap(src);
        int x = decSrc.get();
        decSrc.position(decSrc.position() + x);
        byte[] result = new byte[decSrc.remaining()];
        decSrc.get(result);
        return result;
    }

}
