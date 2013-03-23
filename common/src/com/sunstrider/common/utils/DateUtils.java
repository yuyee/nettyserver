/**
 * Copyright (c) www.netease.com 2010.<br>
 * Created on 2010-11-25
 * 
 * @author <a href="nisonghai@corp.netease.com">Ni Songhai</a>
 * @version $Revision: 1.0$
 */
package com.sunstrider.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date处理
 */
public final class DateUtils {
    /**
     * 线程本地变量
     */
    private static ThreadLocal<DateFormat> dfLocal = new ThreadLocal<DateFormat>();

    /** 构造函数 */
    private DateUtils() {};

    /**
     * Long类型转Date处理
     * 
     * @param timeMillis
     * @return
     */
    public static Date getDate(Long timeMillis) {
        if (timeMillis == null || timeMillis == 0) {
            return null;
        } else {
            return new Date(timeMillis);
        }
    }

    /**
     * Date转Long类型处理
     * 
     * @param date
     * @return
     */
    public static Long getTimeMillis(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }

    /**
     * 解析"yyyy-MM-dd"格式的字符串到日期对象
     * <p>
     * 线程安全
     * 
     * @throws ParseException
     */
    public static Date parseSimpleDateString(String source)
        throws ParseException {
        DateFormat df = dfLocal.get();
        if (df == null) {
            df = new SimpleDateFormat("yyyy-MM-dd");
            dfLocal.set(df);
        }

        return df.parse(source);
    }
}
