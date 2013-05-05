/**
 * @(#)Md5Utils.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5值相关方法
 */
public final class Md5Utils {
    /**
     * 防止重复初始化
     */
    private static ThreadLocal<MessageDigest> md5ThreadLocal = new ThreadLocal<MessageDigest>() {
        @Override
        protected final MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(" no md5 algorythm found");
            }
        }
    };

    /** 构造函数 */
    private Md5Utils() {};

    /**
     * 获取本线程内的MD5计算器
     * <p>
     * 注意: 该实例不应该被缓存,并且使用过程中不能调用{@link Md5Utils}的接口, 否则会导致计算错误
     * 
     * @return
     */
    public static MessageDigest getLocalMd5() {
        MessageDigest md5 = md5ThreadLocal.get();
        md5.reset();
        return md5;
    }

    /**
     * 计算字符串的MD5值
     * 
     * @param key
     *            字符串
     * @return 字节数组格式的MD5值
     */
    public static byte[] md5(String key) {
        MessageDigest md5 = md5ThreadLocal.get();
        md5.reset();
        md5.update(key.getBytes());
        return md5.digest();
    }

    /**
     * 计算字符串的MD5值, 并返回高64位的Long类型数值
     * 
     * @param key
     *            字符串
     * @return 高64位的Long类型数值
     */
    public static long md5HighOrder(String key) {
        MessageDigest md5 = md5ThreadLocal.get();
        md5.reset();
        md5.update(key.getBytes());
        return ByteUtils.toLong(md5.digest());
    }

    /**
     * 计算指定字节数组的MD5值
     * 
     * @param key
     *            字节数组
     * @return 128位MD5值
     */
    public static byte[] md5(byte[] key) {
        MessageDigest md5 = md5ThreadLocal.get();
        md5.reset();
        md5.update(key);
        return md5.digest();
    }

    /**
     * 计算指定字节数组的MD5值
     * 
     * @param key
     *            字节数组
     * @param offset
     *            有效字节起始偏移量
     * @param length
     *            有效字节长度
     * @return 128位MD5值
     */
    public static byte[] md5(byte[] key, int offset, int length) {
        MessageDigest md5 = md5ThreadLocal.get();
        md5.reset();
        md5.update(key, offset, length);
        return md5.digest();
    }

}
