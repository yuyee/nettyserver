/**
 * @(#)DefaultParamDataParserImpl.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 默认的解析器实现, 采用json格式传递数据.
 * 
 * @author cmj
 */
public class DefaultParamDataParserImpl implements IParamDataParser {
    /**
     * json对象
     */
    private JSONObject jsonOb;

    /**
     * 创建解析器对象, 传入原始数据
     * 
     * @param buf
     *            需要解析的字节数组
     * @throws Exception
     */
    public DefaultParamDataParserImpl(byte[] buf) throws Exception {
        jsonOb = JSONUtils.convertJSONToMap(buf);
    }

    /**
     * 创建解析对象
     * 
     * @throws Exception
     */
    public DefaultParamDataParserImpl() throws Exception {

    }

    /**
     * 创建对象
     * 
     * @param msg
     *            需要解析的字符串
     */
    public DefaultParamDataParserImpl(String msg) throws Exception {
        jsonOb = JSONUtils.convertJSONToMap(msg);
    }

    /**
     * 获取指定属性的值, 作为List类型返回, 其中list元素类型String
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getListStr(String name) {
        JSONArray array = null;
        try {
            array = (JSONArray) jsonOb.get(name);
        } catch (Exception e) {
            // ignore exception
        }

        return array;
    }

    /**
     * 获取指定属性的值, 作为List类型返回, 其中list元素类型Integer
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Integer> getListInt(String name) {
        JSONArray array = null;

        try {
            array = (JSONArray) jsonOb.get(name);
        } catch (Exception e) {
            // ignore exception
        }

        return array;
    }

    /**
     * 获取指定属性的值, 作为List类型返回, 其中list元素类型Long
     */
    @SuppressWarnings("unchecked")
    public List<Long> getListLong(String name) {
        JSONArray array = null;

        try {
            array = (JSONArray) jsonOb.get(name);
        } catch (Exception e) {
            // ignore exception
        }

        return array;
    }

    /**
     * 获取指定属性的值, 作为List类型返回, 其中list元素类型是Map
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> getListMap(String name) {
        JSONArray array = null;

        try {
            array = (JSONArray) jsonOb.get(name);
        } catch (Exception e) {
            // ignore exception
        }

        return array;
    }

    /**
     * 获取指定属性的值, 作为一个map返回
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getMap(String name) {
        JSONObject map = null;

        try {
            map = (JSONObject) jsonOb.get(name);
        } catch (Exception e) {
            // ignore exception
        }

        return map;
    }

    /**
     * 获取指定属性的值, 作为String类型返回
     */
    @Override
    public String getString(String name) {
        return (String) jsonOb.get(name);
    }

    /**
     * 获取指定属性的值, 作为boolean类型返回
     */
    @Override
    public Boolean getBoolean(String name) {
        return (Boolean) jsonOb.get(name);
    }

    /**
     * 获取指定属性的值, 作为int类型返回
     */
    @Override
    public Integer getInt(String name) {
        return (Integer) jsonOb.get(name);
    }

    /**
     * 获取指定属性的值, 作为long类型返回
     */
    @Override
    public Long getLong(String name) {
        return (Long) jsonOb.get(name);
    }

    /**
     * 获取日期
     */
    @Override
    public Date getDate(String name) {
        return JSONUtils.getDate(jsonOb, name);
    }

    /**
     * 递归获取解析器
     */
    @Override
    public IParamDataParser getSubDataParserAsMap(String name) {
        DefaultParamDataParserImpl dataParser = null;
        try {
            JSONObject ob = (JSONObject) jsonOb.get(name);
            if (ob == null) {
                return null;
            }
            dataParser = new DefaultParamDataParserImpl();
            dataParser.jsonOb = ob;
        } catch (Exception e) {
            // ignore exception
        }
        return dataParser;
    }

    /**
     * 得到对应的字符串表示数据
     */
    @Override
    public String getOriginalData() {
        String retStr = null;
        try {
            retStr = jsonOb.toString();
        } catch (Exception e) {
            // ignore exception
        }

        return retStr;
    }

    /**
     * 得到数组的大小
     */
    @Override
    public int getDataParserArrayCount(String name) {
        int count = 0;
        try {
            JSONArray array = (JSONArray) jsonOb.get(name);
            if (array != null) {
                count = array.size();
            }
        } catch (Exception e) {
            // ignore exception
        }
        return count;
    }

    /**
     * 根据索引获取数组中的解析器
     */
    @Override
    public IParamDataParser getSubDataParserFromArray(String name, int idx) {
        DefaultParamDataParserImpl dataParser = null;
        try {
            JSONArray array = (JSONArray) jsonOb.get(name);
            if (array == null) {
                return null;
            }
            JSONObject ob = (JSONObject) array.get(idx);
            dataParser = new DefaultParamDataParserImpl();
            dataParser.jsonOb = ob;
        } catch (Exception e) {
            // ignore exception
        }

        return dataParser;
    }

    /**
     * 将数组包含的解析器添加到List对象返回
     */
    @Override
    public List<IParamDataParser> getSubDataParserAsList(String name) {
        List<IParamDataParser> parsers = new ArrayList<IParamDataParser>();

        try {
            JSONArray array = (JSONArray) jsonOb.get(name);
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject ob = (JSONObject) array.get(i);
                    DefaultParamDataParserImpl dataParser = new DefaultParamDataParserImpl();
                    dataParser.jsonOb = ob;
                    parsers.add(dataParser);
                }
            }
        } catch (Exception e) {
            // ignore exception
        }

        return parsers;
    }

    @Override
    public Object getValue(String name) {
        return jsonOb.get(name);
    }
}
