/**
 * @(#)JSONAware.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.json;

/**
 * Beans that support customized output of JSON text shall implement this
 * interface. From http://code.google.com/p/json-simple/
 */
public interface JSONAware {
    /**
     * @return JSON text
     */
    String toJSONString();

    /**
     * add the object to <code>StringBuilder</code>
     * 
     * @param sb
     */
    public void appendTo(StringBuilder sb);
}
