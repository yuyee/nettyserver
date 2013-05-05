/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $ Created on
 * 2006-4-10
 */
package com.git.original.common.json;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A JSON object. Key value pairs are unordered. JSONObject supports
 * java.util.Map interface. From http://code.google.com/p/json-simple/
 * <p>
 * 使用了StringBuilder类代替了StringBuffer类
 */
@SuppressWarnings("rawtypes")
public class JSONObject extends HashMap implements Map, JSONAware,
    JSONStreamAware {

    /** 序列化ID */
    private static final long serialVersionUID = -503443796854799292L;

    /**
     * Encode a map into JSON text and write it to out. If this map is also a
     * JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific
     * behaviours will be ignored at this top level.
     * 
     * @see org.json.simple.JSONValue#writeJSONString(Object, Writer)
     * @param map
     * @param out
     */
    public static void writeJSONString(Map map, Writer out) throws IOException {
        if (map == null) {
            out.write("null");
            return;
        }

        boolean first = true;
        Iterator iter = map.entrySet().iterator();

        out.write('{');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            } else {
                out.write(',');
            }
            Map.Entry entry = (Map.Entry) iter.next();
            out.write('\"');
            out.write(escape(String.valueOf(entry.getKey())));
            out.write('\"');
            out.write(':');
            JSONValue.writeJSONString(entry.getValue(), out);
        }
        out.write('}');
    }

    public void writeJSONString(Writer out) throws IOException {
        writeJSONString(this, out);
    }

    /**
     * @param map
     * @param sb
     * @return JSON text, or "null" if map is null.
     */
    public static void appendTo(Map map, StringBuilder sb) {
        if (map == null) {
            sb.append("{}");

            return;
        }

        boolean first = true;
        Iterator iter = map.entrySet().iterator();

        sb.append('{');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }

            Map.Entry entry = (Map.Entry) iter.next();
            toJSONString(String.valueOf(entry.getKey()), entry.getValue(), sb);
        }
        sb.append('}');
    }

    /**
     * @param key
     *            键
     * @param value
     *            值
     * @param sb
     * @return JSON text, or "null" if map is null.
     */
    public static void appendTo(Object key, Object value, StringBuilder sb) {
        JSONValue.appendTo(key, sb);
        sb.append(':');
        JSONValue.appendTo(value, sb);
    }

    @Override
    public void appendTo(StringBuilder sb) {
        appendTo(this, sb);
    }

    /**
     * Convert a map to JSON text. The result is a JSON object. If this map is
     * also a JSONAware, JSONAware specific behaviours will be omitted at this
     * top level.
     * 
     * @see org.json.simple.JSONValue#toJSONString(Object)
     * @param map
     * @return JSON text, or "null" if map is null.
     */
    public static String toJSONString(Map map) {
        StringBuilder sb = new StringBuilder();
        appendTo(map, sb);

        return sb.toString();
    }

    public String toJSONString() {
        StringBuilder sb = new StringBuilder();
        appendTo(this, sb);

        return sb.toString();
    }

    private static void toJSONString(String key, Object value, StringBuilder sb) {
        sb.append('"');
        if (key == null) {
            sb.append("null");
        } else {
            JSONValue.escape(key, sb);
        }

        sb.append('"').append(':');

        JSONValue.appendTo(value, sb);
    }

    public String toString() {
        return toJSONString();
    }

    public static String toString(String key, Object value) {
        StringBuilder sb = new StringBuilder();
        toJSONString(key, value, sb);
        return sb.toString();
    }

    /**
     * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
     * (U+0000 through U+001F). It's the same as JSONValue.escape() only for
     * compatibility here.
     * 
     * @see org.json.simple.JSONValue#escape(String)
     * @param s
     * @return
     */
    public static String escape(String s) {
        return JSONValue.escape(s);
    }
}
