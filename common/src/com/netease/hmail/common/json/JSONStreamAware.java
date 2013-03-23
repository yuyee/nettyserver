/**
 * @(#)JSONStreamAware.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.json;

import java.io.IOException;
import java.io.Writer;

/**
 * Beans that support customized output of JSON text to a writer shall implement
 * this interface. From http://code.google.com/p/json-simple/
 */
public interface JSONStreamAware {
    /**
     * write JSON string to out.
     */
    void writeJSONString(Writer out) throws IOException;
}
