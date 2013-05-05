/**
 * @(#)FuzzyLogic.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.utils;

/**
 * 仅支持三值的模糊逻辑表示 保证以下条件成立 {@code
 *  FuzzyLogic a, b;
 *      ...
 *      (a == b) == (a.eqauls(b));
 * }
 * 
 * @version 2011-7-8
 */
public final class FuzzyLogic {
    /** 逻辑值字节常量: true */
    private static final int FUZZY_TRUE = 1;

    /** 逻辑值字节常量: false */
    private static final int FUZZY_FALSE = 0;

    /** 逻辑值字节常量: 结果不确定 */
    private static final int FUZZY_NOT_SURE = -1;

    /** 逻辑值字节常量: 错误值 */
    private static final int BAD = -100;

    /** 代表true的逻辑对象常量 */
    public static final FuzzyLogic TRUE = new FuzzyLogic(FUZZY_TRUE);

    /** 代表false的逻辑对象常量 */
    public static final FuzzyLogic FALSE = new FuzzyLogic(FUZZY_FALSE);

    /** 代表结果不确定的逻辑对象常量 */
    public static final FuzzyLogic NOT_SURE = new FuzzyLogic(FUZZY_NOT_SURE);

    /** 构造函数 */
    private FuzzyLogic(int value) {
        localValue = value;
    }

    /** 对应的整数数值 */
    private int localValue;

    public static FuzzyLogic valueOf(String value) {

        int intValue = toInt(value);

        switch (intValue) {

            case FUZZY_TRUE:
                return TRUE;
            case FUZZY_FALSE:
                return FALSE;
            case FUZZY_NOT_SURE:
                return NOT_SURE;
            default:
                return NOT_SURE;
        }
    }

    public static FuzzyLogic valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean booleanValue() {
        return localValue == FUZZY_TRUE;
    }

    public String toString() {
        return String.valueOf(localValue);
    }

    private static int toInt(String value) {
        int v = BAD;
        try {
            v = Integer.valueOf(value);
        } catch (Throwable th) {
            v = BAD;
        }
        return v;
    }

}
