/**
 * @(#)JSONArray.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.json;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A JSON array. JSONObject supports java.util.List interface. From
 * http://code.google.com/p/json-simple/
 * <p>
 * 使用了StringBuilder类代替了StringBuffer类
 * </p>
 */
@SuppressWarnings("rawtypes")
public class JSONArray extends ArrayList implements List, JSONAware,
    JSONStreamAware {
    /** 序列化ID */
    private static final long serialVersionUID = 3957988303675231981L;

    /**
     * Encode a list into JSON text and write it to out. If this list is also a
     * JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific
     * behaviours will be ignored at this top level.
     * 
     * @see org.json.simple.JSONValue#writeJSONString(Object, Writer)
     * @param list
     * @param out
     */
    public static void writeJSONString(List list, Writer out)
        throws IOException {
        if (list == null) {
            out.write("null");
            return;
        }

        boolean first = true;
        Iterator iter = list.iterator();

        out.write('[');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            } else {
                out.write(',');
            }

            Object value = iter.next();
            if (value == null) {
                out.write("null");
                continue;
            }

            JSONValue.writeJSONString(value, out);
        }
        out.write(']');
    }

    public void writeJSONString(Writer out) throws IOException {
        writeJSONString(this, out);
    }

    /**
     * Convert a list to JSON text. The result is a JSON array. If this list is
     * also a JSONAware, JSONAware specific behaviours will be omitted at this
     * top level.
     * 
     * @see org.json.simple.JSONValue#toJSONString(Object)
     * @param list
     * @param sb
     * @return JSON text, or "null" if list is null.
     */
    public static void appendTo(Iterable iterable, StringBuilder sb) {
        if (iterable == null) {
            sb.append("[]");
            return;
        }

        boolean first = true;
        Iterator iter = iterable.iterator();

        sb.append('[');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }

            Object value = iter.next();
            if (value == null) {
                sb.append("null");
                continue;
            }
            JSONValue.appendTo(value, sb);
        }
        sb.append(']');
    }

    @Override
    public void appendTo(StringBuilder sb) {
        appendTo(this, sb);
    }

    public static String toJSONString(Iterable it) {
        StringBuilder sb = new StringBuilder();
        appendTo(it, sb);

        return sb.toString();
    }

    public String toJSONString() {
        StringBuilder sb = new StringBuilder();
        appendTo(this, sb);

        return sb.toString();
    }

    public String toString() {
        return toJSONString();
    }
}
