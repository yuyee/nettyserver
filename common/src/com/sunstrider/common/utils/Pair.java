/*
 * ***** BEGIN LICENSE BLOCK ***** Zimbra Collaboration Suite Server Copyright
 * (C) 2006, 2007, 2009, 2010 Zimbra, Inc. The contents of this file are subject
 * to the Zimbra Public License Version 1.3 ("License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at http://www.zimbra.com/license. Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. ***** END LICENSE BLOCK *****
 */
package com.sunstrider.common.utils;

import com.sunstrider.common.logging.syslog.SysLogger;

/**
 * 直接使用Zimbra中的Pair类
 * 
 * @param <F>
 *            pair中第一个对象的类型
 * @param <S>
 *            pair中第二个对象的类型
 */
public class Pair<F, S> {
    /** 第一个元素 */
    private F mFirst;

    /** 第二个元素 */
    private S mSecond;

    /** 构造函数 */
    public Pair(F first, S second) {
        mFirst = first;
        mSecond = second;
    }

    public F car() {
        return getFirst();
    }

    public S cdr() {
        return getSecond();
    }

    public F getFirst() {
        return mFirst;
    }

    public S getSecond() {
        return mSecond;
    }

    public void setFirst(F first) {
        mFirst = first;
    }

    public void setSecond(S second) {
        mSecond = second;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair that = (Pair) obj;
            if (mFirst != that.mFirst
                && (mFirst == null || !mFirst.equals(that.mFirst))) {
                return false;
            }
            if (mSecond != that.mSecond
                && (mSecond == null || !mSecond.equals(that.mSecond))) {
                return false;
            }
            return true;
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        int code1 = mFirst == null ? 0 : mFirst.hashCode();
        int code2 = mSecond == null ? 0 : mSecond.hashCode();
        return code1 ^ code2;
    }

    @Override
    public String toString() {
        return "(" + mFirst + "," + mSecond + ")";
    }

    /**
     * 供{@link SysLogger}使用
     * <p>
     * 当value==null时, 自动设置value=""
     * 
     * @param key
     * @param value
     * @return
     */
    public static Pair<String, Object> pair(String key, Object value) {
        Pair<String, Object> p = new Pair<String, Object>(key,
            (value == null ? "" : value));
        return p;
    }

    public static void main(String[] args) {
        System.out.println(new Pair<String, String>("foo", "bar")
            .equals(new Pair<String, String>("foo", "bar")));
        System.out.println(new Pair<String, String>("foo", null)
            .equals(new Pair<String, String>("fo" + 'o', null)));
        System.out.println(new Pair<String, String>(null, "bar")
            .equals(new Pair<String, String>(null, "foo")));
        System.out.println(new Pair<String, String>("foo", "bar")
            .equals(new Pair<String, Integer>("foo", 8)));
        System.out.println(new Pair<String, String>(null, "bar")
            .equals(new Pair<Integer, String>(0, "bar")));
    }
}
