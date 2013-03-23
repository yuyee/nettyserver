/**
 * @(#)PasswordChiperUtil.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.utils;

/**
 * agent password 加密管理器,以-128 ECB模式加密,最终以16进制字符串形式存储于持久层
 * 
 * @author lax
 */
public final class PasswordChiperUtil {

    /**
     * 密钥，“hmail”的md5值
     */
    private static final byte[] secretKey;

    static {
        secretKey = Md5Utils.md5("hmail");
    }

    /** 构造函数 */
    private PasswordChiperUtil() {};

    /**
     * 加密
     * 
     * @param password
     * @return
     */
    public static String encrypt(String password) throws RuntimeException {

        try {
            byte[] content = password.getBytes("UTF-8");
            byte[] enc = AESUtil.symmetricEncrypt(content, secretKey);
            return HexUtils.toHexString(enc, 0, enc.length);
        } catch (Exception e) {
            throw new RuntimeException("password  encrypt fail", e);
        }
    }

    /**
     * 解密
     * 
     * @param content
     * @return
     */
    public static String decrypt(String content) throws RuntimeException {
        byte[] password = null;
        try {
            password = AESUtil.symmetricDecrypt(
                HexUtils.parseHexString(content), secretKey);
            return new String(password, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("password  decrypt fail");
        }
    }

    public static void main(String[] args) {
        System.out.println(decrypt("D520DCAF85C0C0F2EBB882C8FD171D0A"));
    }
}
