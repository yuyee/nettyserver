/**
 * @(#)IParamDataParser.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.json;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 数据解析接口,将底层数据的解析与上层的调用分离开来 在都是"消息长度+二进制数据"原则传递消息时,那么在 解析协议时,底层数据格式可进行更换.
 * </p>
 * <p>
 * 现在底层数据是采用json格式传输.
 * 
 * @author cmj
 */
public interface IParamDataParser {
    /**
     * 若属性名称对应的是一个解析器对象, 此方法获取对应的数据解析器.
     * 
     * @param name
     *            属性名称
     * @return 下一层次的数据解析器
     */
    public IParamDataParser getSubDataParserAsMap(String name);

    /**
     * 若属性名称对应的是一个数组,且数组的类型是解析器,次方法获取数组中解析器个数.
     * 
     * @param name
     *            属性名称
     * @return 包含的解析器的个数
     */
    public int getDataParserArrayCount(String name);

    /**
     * 根据下标位置得到下一层的解析器
     * 
     * @param name
     *            属性名称
     * @param idx
     *            下标位置
     * @return 解析器
     */
    public IParamDataParser getSubDataParserFromArray(String name, int idx);

    /**
     * 获取解析器，以List返回
     * 
     * @param name
     *            属性名称
     * @return 属性名称对应的数组，转换为list对象返回
     */
    public List<IParamDataParser> getSubDataParserAsList(String name);

    /**
     * 得到解析器解析的原始数据
     * 
     * @return 原始数据
     * @throws Exception
     */
    public String getOriginalData();

    /**
     * 通过属性名, 得到字符串类型的属性值
     * 
     * @param name
     *            属性名称
     * @return 字符串类型的属性值, 若属性值不存在, 返回null.
     */
    public String getString(String name);

    /**
     * 通过属性名, 得到整型类型的属性值
     * 
     * @param name
     *            属性名称
     * @return 整型类型的属性值, 若属性值不存在, 返回null.
     */
    public Integer getInt(String name);

    /**
     * 通过属性名, 得到长整型类型的属性值
     * 
     * @param name
     *            属性名称
     * @return 长整型类型的属性值, 若属性值不存在, 返回null.
     */
    public Long getLong(String name);

    /**
     * 通过属性名, 得到布尔类型的属性值
     * 
     * @param name
     *            属性名称
     * @return 布尔类型的属性值, 若属性值不存在, 返回null.
     */
    public Boolean getBoolean(String name);

    /**
     * 通过属性名, 得到日期类型的属性值
     * 
     * @param name
     *            属性名称
     * @return 日期类型的属性值, 若属性值不存在, 返回null.
     */
    public Date getDate(String name);

    /**
     * 通过属性名,获取一个{@link java.util.List List}的属性值, 其元素为{@link java.lang.String
     * String}类型.
     * 
     * @param name
     *            属性名称
     * @return 返回list对象
     */
    public List<String> getListStr(String name);

    /**
     * 通过属性名,获取一个{@link java.util.List List}的属性值, 其元素为{@link java.lang.Integer
     * Integer}类型.
     * 
     * @param name
     *            属性名称
     * @return 返回list对象
     */
    public List<Integer> getListInt(String name);

    /**
     * 通过属性名,获取一个{@link java.util.List List}的属性值, 其元素为{@link java.lang.Long
     * Long}类型.
     * 
     * @param name
     *            属性名称
     * @return 返回list对象
     */
    public List<Long> getListLong(String name);

    /**
     * 通过属性名,获取一个{@link java.util.List List},其元素类型为{@link java.util.Map Map}
     * 
     * @param name
     *            属性名称
     * @return 返回list对象
     */
    public List<Map<String, Object>> getListMap(String name);

    /**
     * 通过属性名, 获得一个{@link java.util.Map Map}类型的值
     * 
     * @param name
     *            属性名称
     * @return 返回map对象
     */
    public Map<String, Object> getMap(String name);

    public Object getValue(String name);
}
