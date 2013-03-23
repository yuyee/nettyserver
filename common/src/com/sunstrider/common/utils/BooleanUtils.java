/**
 * Copyright (c) www.netease.com 2010.<br>
 * Created on 2010-11-19
 * 
 * @author <a href="nisonghai@corp.netease.com">Ni Songhai</a>
 * @version $Revision: 1.0$
 */
package com.sunstrider.common.utils;

/**
 *
 */
public final class BooleanUtils {

    /** 构造函数 */
    private BooleanUtils() {};

    public static boolean valueOf(short value) {
        return (value == 1);
    }

    public static short toShort(boolean bool) {
        if (bool) {
            return (short) 1;
        } else {
            return (short) 0;
        }
    }

}
