/**
 * @(#)JSONUtils.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.json;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * json处理的工具类
 * 
 * @author cmj
 */
@SuppressWarnings("rawtypes")
public final class JSONUtils {
    /**
     * json使用的日期格式
     */
    public static SimpleDateFormat jsonDateFormatter = new SimpleDateFormat(
        "yyyy,MM,dd,HH,mm,ss,sss");

    /** 构造函数 */
    private JSONUtils() {};

    /**
     * 将Map对象转换成json格式的对象
     * 
     * @param map
     *            需要转换为json格式字符串的map
     * @return map转换为json格式后的字符串
     */
    public static void appendMapToJSON(Map map, StringBuilder sb) {
        JSONObject.appendTo(map, sb);
    }

    // /**
    // * 将List转换为json格式的数组
    // *
    // * @param list
    // * 需要转换为json格式的list对象
    // * @return 返回json格式的数组
    // */
    // @SuppressWarnings("unchecked")
    // public static String convertListToJSON(List list) {
    // return JSONArray.toJSONString(list);
    // }

    /**
     * 将List转换为json格式的数组
     * 
     * @param list
     *            需要转换为json格式的list对象
     * @return 返回json格式的数组
     */
    public static void appendListToJSON(List list, StringBuilder sb) {
        JSONArray.appendTo(list, sb);
    }

    /**
     * 将json格式的字节数组转换为map对象
     * <p>
     * 默认采用UTF-8对字节数组进行解码
     * 
     * @param buf
     *            json格式的字节数组
     * @return json数据对应的map对象
     * @throws Exception
     *             解析数据错误时,抛出异常
     */
    public static JSONObject convertJSONToMap(byte[] buf) throws Exception {
        try {
            return (JSONObject) JSONValue.parse(new String(buf, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * 将json字符串转换为Map对象
     * 
     * @param mapJsonStr
     *            需要转换为map对象的json字符串
     * @return 返回map对象
     * @throws Exception
     *             解析数据错误时,抛出异常
     */
    public static JSONObject convertJSONToMap(String mapJsonStr)
        throws Exception {
        return (JSONObject) JSONValue.parse(mapJsonStr);
    }

    /**
     * 将json数组转换为List对象
     * 
     * @param str
     *            json格式的数组字符串
     * @return 返回List对象
     * @throws Exception
     *             解析数据错误时,抛出异常
     */
    public static JSONArray convertJSONToList(String str) throws Exception {
        return (JSONArray) JSONValue.parse(str);
    }

    /**
     * 得到日期属性
     * <p>
     * 默认日期格式：yyyy-MM-dd HH:mm:ss
     * 
     * @param jsonOb
     *            json对象
     * @param prop
     *            属性名称
     * @return 返回日期的值
     */
    public static Date getDate(JSONObject jsonOb, String prop) {
        return (Date) jsonOb.get(prop);
    }

    // ---------------------------------------------------------------------获得JSON字符串
    /**
     * 添加一个JSON对象的字符串类型属性,并添加",".
     * <p>
     * 例如: property是"name", value是java, 则添加到StringBuilder中的字符串是:"name":"java",
     * </p>
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            字符串值
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendStringPropWithComma(StringBuilder sb,
        String prop, String value) {
        return appendStringProp(sb, prop, value).append(",");
    }

    /**
     * 添加一个JSON对象的字符串类型属性.
     * <p>
     * 例如: property是"name", value是java, 则添加到StringBuilder中的字符串是:"name":"java"
     * </p>
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            字符串值
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendStringProp(StringBuilder sb, String prop,
        String value) {
        if (value == null || value.trim().isEmpty()) {
            value = "";
        }
        sb.append("\"").append(prop).append("\":");
        sb.append("\"").append(JSONValue.escape(value)).append("\"");

        return sb;
    }

    /**
     * 添加一个JSON对象的整型类型的属性,并添加",".
     * <p>
     * 例如: property是"count", value是123, 则添加到StringBuilder中的字符串是:"count":123,
     * </p>
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            整型值
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendIntPropWithComma(StringBuilder sb,
        String prop, int value) {
        return appendIntProp(sb, prop, value).append(",");
    }

    /**
     * 添加一个JSON对象的整型类型的属性.
     * <p>
     * 例如: property是"count", value是123, 则添加到StringBuilder中的字符串是:"count":123
     * </p>
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            整型值
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendIntProp(StringBuilder sb, String prop,
        int value) {
        sb.append("\"").append(prop).append("\":");
        sb.append(value);

        return sb;
    }

    /**
     * 添加一个JSON对象的长整型类型的属性,并添加",".
     * <p>
     * 例如: property是"count", value是123, 则添加到StringBuilder中的字符串是:"count":123,
     * </p>
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            整型值
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendLongPropWithComma(StringBuilder sb,
        String prop, long value) {
        return appendLongProp(sb, prop, value).append(",");
    }

    /**
     * 添加一个JSON对象的长整型类型的属性
     * <p>
     * 例如: property是"count", value是123, 则添加到StringBuilder中的字符串是:"count":123
     * </p>
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            整型值
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendLongProp(StringBuilder sb, String prop,
        long value) {
        sb.append("\"").append(prop).append("\":");
        sb.append(value).append("L");

        return sb;
    }

    /**
     * 添加一个JSON对象的布尔类型的属性,并添加",".
     * <p>
     * 例如: property是"success", value是true,
     * 则添加到StringBuilder中的字符串是:"success":true,
     * </p>
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            布尔值
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendBoolPropWithComma(StringBuilder sb,
        String prop, boolean value) {
        return appendBoolProp(sb, prop, value).append(",");
    }

    /**
     * 添加一个JSON对象的布尔类型的属性
     * <p>
     * 例如: property是"success", value是true,
     * 则添加到StringBuilder中的字符串是:"success":true
     * </p>
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            布尔值
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendBoolProp(StringBuilder sb, String prop,
        boolean value) {
        sb.append("\"").append(prop).append("\":");
        sb.append(value);

        return sb;
    }

    /**
     * 添加一个JSON对象的日期类型的属性,并添加",".
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param date
     *            日期对象
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendDatePropWithComma(StringBuilder sb,
        String prop, Date date) {
        return appendDateProp(sb, prop, date).append(",");
    }

    /**
     * 添加一个JSON对象的日期类型的属性
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param date
     *            日期对象
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendDateProp(StringBuilder sb, String prop,
        Date date) {
        sb.append("\"").append(prop).append("\":");
        if (date == null) {
            sb.append("null");
        } else {
            sb.append(date.getTime()).append("D");
        }

        return sb;
    }

    /**
     * 添加字符串内容表示一个JSON对象的属性,并添加",".
     * <p>
     * 例如: property是"ob", value是"{\"num\":1235, \"price\":25}",
     * 则添加到StringBuilder中的字符串是:"ob":{\"num\":1235, \"price\":25},
     * </p>
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            添加的对象
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendObjectPropWithComma(StringBuilder sb,
        String prop, JSONAware value) {
        return appendObjectProp(sb, prop, value).append(",");
    }

    /**
     * 添加字符串内容表示一个JSON对象的属性
     * <p>
     * 例如: property是"ob", value是"{\"num\":1235, \"price\":25}",
     * 则添加到StringBuilder中的字符串是:"ob":{\"num\":1235, \"price\":25}
     * </p>
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            添加的对象
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendObjectProp(StringBuilder sb, String prop,
        JSONAware value) {
        sb.append("\"").append(prop).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            value.appendTo(sb);
        }

        return sb;
    }

    /**
     * 将map作为一个对象添加
     * 
     * @param sb
     * @param prop
     * @param value
     * @return
     */
    public static StringBuilder appendObjectPropWithComma(StringBuilder sb,
        String prop, Map value) {
        return appendObjectProp(sb, prop, value).append(",");
    }

    /**
     * 将map作为一个对象添加
     * 
     * @param sb
     * @param prop
     * @param value
     * @return
     */
    public static StringBuilder appendObjectProp(StringBuilder sb, String prop,
        Map value) {
        sb.append("\"").append(prop).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            JSONObject.appendTo(value, sb);
        }

        return sb;
    }

    /**
     * 将String作为一个对象添加
     * 
     * @param sb
     * @param prop
     * @param value
     * @return
     */
    public static StringBuilder appendObjectPropWithComma(StringBuilder sb,
        String prop, String value) {
        return appendObjectProp(sb, prop, value).append(",");
    }

    /**
     * 将String作为一个对象添加
     * 
     * @param sb
     * @param prop
     * @param value
     * @return
     */
    public static StringBuilder appendObjectProp(StringBuilder sb, String prop,
        String value) {
        sb.append("\"").append(prop).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append(value);
        }

        return sb;
    }

    /**
     * 添加一个数组,并添加",".
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            List对象
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendListWithComma(StringBuilder sb,
        String prop, List value) {
        return appendList(sb, prop, value).append(",");
    }

    /**
     * 添加一个数组.
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            List对象
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendList(StringBuilder sb, String prop,
        List value) {
        sb.append("\"").append(prop).append("\":");
        if (value == null || value.isEmpty()) {
            sb.append("[]");
        } else {
            JSONArray.appendTo(value, sb);
        }

        return sb;
    }

    /**
     * 添加一个map,并添加",".
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            Map对象
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendMapWithComma(StringBuilder sb,
        String prop, Map value) {
        return appendMap(sb, prop, value).append(",");
    }

    /**
     * 添加一个map.
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param prop
     *            属性名称
     * @param value
     *            Map对象
     * @return 返回添加一个属性后的StringBuilder对象
     */
    public static StringBuilder appendMap(StringBuilder sb, String prop,
        Map value) {
        sb.append("\"").append(prop).append("\":");
        if (value == null || value.isEmpty()) {
            sb.append("{}");
        } else {
            JSONObject.appendTo(value, sb);
        }

        return sb;
    }

    /**
     * 添加表示成功的属性
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @return 原始的StringBuilder对象
     */
    public static StringBuilder addSuccessPropWithComma(StringBuilder sb) {
        return JSONUtils.appendBoolPropWithComma(sb, "success", Boolean.TRUE);
    }

    /**
     * 添加表示失败的属性
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @return 原始的StringBuilder对象
     */
    public static StringBuilder addFailurePropWithComma(StringBuilder sb) {
        return JSONUtils.appendBoolPropWithComma(sb, "success", Boolean.FALSE);
    }

    // /**
    // * 添加一个List对象中的元素, 其元素表示的字符串是json对象
    // *
    // * @param sb
    // * 添加属性的StringBuilder对象
    // * @param prop
    // * 属性名称
    // * @param listStr
    // * List对象,其元素为字符串,且表示的是一个json对象
    // * @return 原始的StringBuilder对象
    // */
    // public static StringBuilder appendArrayOfObjectWithComma(StringBuilder
    // sb,
    // String prop, List<String> value) {
    // return appendArrayOfObject(sb, prop, value).append(",");
    // }
    //
    // /**
    // * 添加一个List对象中的元素, 其元素表示的字符串是json对象
    // *
    // * @param sb
    // * 添加属性的StringBuilder对象
    // * @param prop
    // * 属性名称
    // * @param listStr
    // * List对象,其元素为字符串,且表示的是一个json对象
    // * @return 原始的StringBuilder对象
    // */
    // public static StringBuilder appendArrayOfObject(StringBuilder sb,
    // String prop, List<String> value) {
    // sb.append("\"").append(prop).append("\":");
    // if (value == null || value.isEmpty()) {
    // sb.append("[]");
    // } else {
    // sb.append("[");
    // for (String s : value) {
    // sb.append(s);
    // sb.append(",");
    // }
    //
    // sb.deleteCharAt(sb.length() - 1);
    // sb.append("]");
    // }
    //
    // return sb;
    // }

    public static StringBuilder appendJSONAWareListJSONObjectWithComma(
        StringBuilder sb, String prop, Iterable iterable) {
        return appendJSONAWareListJSONObject(sb, prop, iterable).append(",");
    }

    public static StringBuilder appendJSONAWareListJSONObject(StringBuilder sb,
        String prop, Iterable iterable) {
        sb.append("\"").append(prop).append("\":");
        if (iterable == null) {
            sb.append("[]");
        } else {

            boolean first = true;
            Iterator iter = iterable.iterator();
            sb.append('[');
            while (iter.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }

                JSONAware value = (JSONAware) iter.next();
                if (value == null) {
                    sb.append("null");
                    continue;
                }

                value.appendTo(sb);
            }
            sb.append(']');
        }

        return sb;
    }

    /**
     * 添加一个map对象中key-value对象 取出Map中的key-value对, 添加到StringBuidler对象中
     * 
     * @param sb
     *            添加属性的StringBuilder对象
     * @param headerMap
     *            map对象
     * @return 原始的StringBuilder对象
     */
    public static StringBuilder appendElementsOfMapWithComma(StringBuilder sb,
        Map<String, Object> value) {
        if ((value == null) || (value.isEmpty())) {
            return sb;
        }

        for (Entry<String, Object> entry: value.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\"").append(":")
                .append(JSONValue.toJSONString(entry.getValue())).append(",");
        }

        return sb;
    }
}
