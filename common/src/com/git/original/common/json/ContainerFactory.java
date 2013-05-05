/**
 * @(#)ContainerFactory.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.json;

import java.util.List;
import java.util.Map;

/**
 * Container factory for creating containers for JSON object and JSON array.
 * From http://code.google.com/p/json-simple/
 * 
 * @see org.json.simple.parser.JSONParser#parse(java.io.Reader,
 *      ContainerFactory)
 */
@SuppressWarnings("rawtypes")
public interface ContainerFactory {
    /**
     * @return A Map instance to store JSON object, or null if you want to use
     *         org.json.simple.JSONObject.
     */
    Map createObjectContainer();

    /**
     * @return A List instance to store JSON array, or null if you want to use
     *         org.json.simple.JSONArray.
     */
    List creatArrayContainer();
}
