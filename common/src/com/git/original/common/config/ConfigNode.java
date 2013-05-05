/**
 * @(#)ConfigNode.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.git.original.common.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * 配置参数节点
 * <p>
 * <li>配置参数名称不允许以句号(.)开头
 * <li>参数路径中使用句号(.)作为层级节点参数名之间的分隔符
 * <li>如果参数名中本身包含有句号(.), 则使用连续两个句号(..)进行表示<br>
 * 例如：存在节点 test.example 和 其子节点 item<br>
 * 使用参数路径指定item节点时, 应使用"test..example.item"
 */
public final class ConfigNode implements Comparable<ConfigNode>,
    Iterable<ConfigNode> {
    /**
     * 配置参数名
     */
    private String name;

    /**
     * 配置参数值
     */
    Object value;

    /**
     * 子配置参数映射表
     * <p>
     * 存在子配置参数节点时, 本参数值无效
     * <p>
     * 映射表中应只包含ConfigNode或者List<ConfigNode>对象
     */
    private final Map<String, Object> children = new LinkedHashMap<String, Object>();

    /**
     * 属性映射表
     * <p>
     * JDBC类型的配置节点暂不支持
     */
    private Map<String, String> attributes = null;

    /**
     * 创建配置参数节点
     * 
     * @param name
     *            参数名
     * @param value
     *            参数值
     */
    public ConfigNode(String name, Object value) {
        if (name == null || name.isEmpty() || name.charAt(0) == '.') {
            throw new IllegalArgumentException();
        }

        this.name = name;
        this.value = value;
    }

    /**
     * 获取配置参数名
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * 获取配置参数值
     * 
     * @return the value
     */
    public Object getValue() {
        if (this.hasChildren()) {
            return null;
        }
        return value;
    }

    /**
     * 是否包含有子配置节点
     * 
     * @return true=存在子配置节点; false=不存在子配置参数
     */
    public boolean hasChildren() {
        return (!children.isEmpty());
    }

    /**
     * 添加子配置节点
     * 
     * @param node
     *            子配置节点
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addChild(ConfigNode node) {
        if (node == null) {
            return;
        }

        Object target = children.get(node.name);
        if (target != null) {
            if (target instanceof List) {
                ((List) target).add(node);
            } else {
                ArrayList<ConfigNode> cnList = new ArrayList<ConfigNode>();
                cnList.add((ConfigNode) target);
                cnList.add(node);

                children.put(node.name, cnList);
            }
        } else {
            children.put(node.name, node);
        }
    }

    /**
     * 获取所有子节点集
     * <p>
     * 注意: 子节点可能是{@link ConfigNode}对象, 也可能是{@link List}对象(元素类型为ConfigNode)
     * 
     * @return
     */
    Collection<Object> getAllChildren() {
        return this.children.values();
    }

    /**
     * 获取所有子节点的迭代器
     * <p>
     * 注意: 该迭代器不允许被调用{@link Iterator#remove()}操作
     * 
     * @return
     */
    public Iterator<ConfigNode> iterator() {
        return new InnerIterator();
    }

    /**
     * 解析参数路径, 生成对应的参数名列表
     * <p>
     * <li>参数路径中使用句号(.)作为层级节点参数名之间的分隔符<br>
     * <li>参数名不可能以句号(.)开头
     * <li>如果参数名中本身包含有句号(.), 则使用连续两个句号(..)进行表示<br>
     * 例如：存在节点 test.example 和 其子节点 item<br>
     * 使用参数路径指定item节点时, 应使用"test..example.item"
     * 
     * @param key
     *            参数路径
     * @return 参数名列表
     */
    private String[] parseKeyPath(String key) {
        if (key == null) {
            return null;
        }

        char[] chars = key.toCharArray();
        StringBuilder sb = new StringBuilder(key.length());
        ArrayList<String> nameList = new ArrayList<String>();

        int start = 0;
        int end = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != '.') {
                end++;
                continue;
            }

            if (i + 1 >= chars.length) {
                break;
            }

            if (chars[i + 1] == '.') { // escape dot '.'
                sb.append(chars, start, end - start + 1);
                ++i;
            } else {
                sb.append(chars, start, end - start);
                nameList.add(sb.toString());
                sb.setLength(0);
            }
            start = (i + 1);
            end = start;
        }

        if (start < end) {
            sb.append(chars, start, end - start);
            nameList.add(sb.toString());
        } else if (sb.length() > 0) {
            nameList.add(sb.toString());
        }

        String[] result = new String[nameList.size()];
        return nameList.toArray(result);
    }

    /**
     * 获取指定参数路径的配置节点
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置节点
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 如果不存在指定路径的配置节点, 则返回null
     */

    @SuppressWarnings({ "rawtypes" })
    public ConfigNode getChild(String key) {
        if (key == null) {
            return null;
        }

        String[] names = this.parseKeyPath(key);
        Map<String, Object> currMap = this.children;
        Object target = null;

        for (int i = 0; i < names.length; i++) {
            if (currMap == null) {
                return null;
            }

            String name = names[i];
            target = currMap.get(name);
            if (target == null) {
                return null;
            }

            if (target instanceof List) {
                if (((List) target).isEmpty()) {
                    return null;
                }
                target = ((List) target).get(0);
            }

            if (target instanceof ConfigNode) {
                currMap = ((ConfigNode) target).children;
            } else {
                target = null;
                currMap = null;
            }
        }

        return (ConfigNode) target;
    }

    /**
     * 遍历配置节点树, 将符合参数路径的节点对象放入<code>cnList</code>列表
     * 
     * @param names
     *            参数路径解析后的参数名数组
     * @param index
     *            当前节点对应的数组下标
     * @param cnList
     *            符合参数路径的节点列表
     */
    @SuppressWarnings("unchecked")
    private void doGetChildList(String[] names, int index,
        List<ConfigNode> cnList) {
        Map<String, Object> currMap = this.children;

        for (int i = index; i < names.length; i++) { // 遍历参数名
            if (currMap == null) {
                return;
            }

            Object target = currMap.get(names[i]);
            if (target == null) {
                return;
            }

            if (i + 1 >= names.length) { // 参数路径遍历完毕, 将符合目标的节点存入cnList上
                if (target instanceof List) {
                    cnList.addAll((List<ConfigNode>) target);
                } else {
                    cnList.add((ConfigNode) target);
                }

                return;
            }

            if (target instanceof List) { // 路径中间节点中出现同名分支
                for (ConfigNode childNode: (List<ConfigNode>) target) {
                    childNode.doGetChildList(names, i + 1, cnList);
                }

                break;
            } else { // 路径中间节点
                currMap = ((ConfigNode) target).children;
            }
        }
    }

    /**
     * 获取指定参数路径的配置节点列表
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * <p>
     * 注意: 对本方法中返回的配置节点列表进行添加删除操作, 不会对现有配置节点树结构造成影响
     * 
     * @param key
     *            配置参数名路径
     * @return 如果不存在指定路径的配置节点, 则返回null
     */
    public List<ConfigNode> getChildList(String key) {

        if (key == null) {
            return null;
        }

        String[] names = this.parseKeyPath(key);
        ArrayList<ConfigNode> al = new ArrayList<ConfigNode>();
        this.doGetChildList(names, 0, al);

        return al;
    }

    /**
     * 强制以Boolean类型返回指定的配置参数值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws NullPointerException
     *             指定的配置参数不存在
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public boolean getBoolean(String key) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            throw new NullPointerException(key + " = null");
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not boolean");
        }

        if (cn.value instanceof Boolean) {
            return ((Boolean) cn.value).booleanValue();
        }
        return Boolean.parseBoolean(cn.value.toString());
    }

    /**
     * 强制以Boolean类型返回指定的配置参数值, 如果指定参数不存在则返回<code>defaultValue</code>值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            return defaultValue;
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not boolean");
        } else if (cn.value == null) {
            return defaultValue;
        }

        if (cn.value instanceof Boolean) {
            return ((Boolean) cn.value).booleanValue();
        }
        return Boolean.parseBoolean(cn.value.toString());
    }

    /**
     * 强制以Integer类型返回指定的配置参数值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws NullPointerException
     *             指定的配置参数不存在
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public int getInteger(String key) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            throw new NullPointerException(key + " = null");
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not integer");
        }

        if (cn.value instanceof Number) {
            return ((Number) cn.value).intValue();
        }
        return Integer.parseInt(cn.value.toString());
    }

    /**
     * 强制以Integer类型返回指定的配置参数值, 如果指定参数不存在则返回<code>defaultValue</code>值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public int getInteger(String key, int defaultValue) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            return defaultValue;
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not integer");
        } else if (cn.value == null) {
            return defaultValue;
        }

        if (cn.value instanceof Number) {
            return ((Number) cn.value).intValue();
        }
        return Integer.parseInt(cn.value.toString());
    }

    /**
     * 强制以Long类型返回指定的配置参数值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws NullPointerException
     *             指定的配置参数不存在
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public long getLong(String key) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            throw new NullPointerException(key + " = null");
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not long");
        }

        if (cn.value instanceof Number) {
            return ((Number) cn.value).longValue();
        }
        return Long.parseLong(cn.value.toString());
    }

    /**
     * 强制以Long类型返回指定的配置参数值, 如果指定参数不存在则返回<code>defaultValue</code>值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public long getLong(String key, long defaultValue) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            return defaultValue;
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not long");
        } else if (cn.value == null) {
            return defaultValue;
        }

        if (cn.value instanceof Number) {
            return ((Number) cn.value).longValue();
        }
        return Long.parseLong(cn.value.toString());
    }

    /**
     * 强制以Double类型返回指定的配置参数值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws NullPointerException
     *             指定的配置参数不存在
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public double getDouble(String key) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            throw new NullPointerException(key + " = null");
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not double");
        }

        if (cn.value instanceof Number) {
            return ((Number) cn.value).doubleValue();
        }
        return Double.parseDouble(cn.value.toString());
    }

    /**
     * 强制以Double类型返回指定的配置参数值, 如果指定参数不存在则返回<code>defaultValue</code>值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public double getDouble(String key, double defaultValue) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            return defaultValue;
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not double");
        } else if (cn.value == null) {
            return defaultValue;
        }

        if (cn.value instanceof Number) {
            return ((Number) cn.value).doubleValue();
        }
        return Double.parseDouble(cn.value.toString());
    }

    /**
     * 强制以String类型返回指定的配置参数值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws NullPointerException
     *             指定的配置参数不存在
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public String getString(String key) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            throw new NullPointerException(key + " = null");
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not String");
        }

        return cn.value.toString();
    }

    /**
     * 强制以String类型返回指定的配置参数值, 如果指定参数不存在则返回<code>defaultValue</code>值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public String getString(String key, String defaultValue) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            return defaultValue;
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not String");
        } else if (cn.value == null) {
            return defaultValue;
        }

        return cn.value.toString();
    }

    /**
     * 强制以String类型返回指定的配置参数值, 如果指定参数不存在或者参数值为空则返回<code>defaultValue</code>值
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public String getNonEmptyString(String key, String defaultValue) {
        ConfigNode cn = this.getChild(key);
        if (cn == null) {
            return defaultValue;
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not String");
        } else if (cn.value == null || cn.value.toString().trim().isEmpty()) {
            return defaultValue;
        }

        return cn.value.toString();
    }

    /**
     * 强制以String数组方式返回一组同名的配置参数值, 每个参数值对应一个数组元素
     * <p>
     * 如果指定参数的配置不存在, 则返回一个0长度的空字符串数组
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @return 参数值数组
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public String[] getStringArray(String key) {
        List<ConfigNode> cnList = this.getChildList(key);
        if (cnList == null || cnList.isEmpty()) {
            return (new String[0]);
        }

        String[] strs = new String[cnList.size()];
        for (int i = 0; i < strs.length; i++) {
            ConfigNode cn = cnList.get(i);
            if (cn == null) {
                continue;
            }

            if (cn.hasChildren()) {
                throw new ClassCastException(key + " is not all Strings");
            }

            if (cn.value != null) {
                strs[i] = cn.value.toString();
            }
        }

        return strs;
    }

    /**
     * 强制以long类型返回毫秒单位的时间类配置参数值, 如果指定参数不存在则返回<code>defaultValue</code>值
     * <p>
     * 自动将指定的时间参数值转换到毫秒单位,允许以下参数值中包含以下时间单位后缀
     * <ul>
     * <li>'d' 或 'D': 天
     * <li>'h' 或 'H': 小时
     * <li>'m' 或 'M': 分
     * <li>'s' 或 'S': 秒
     * <li>'ms' 或 无单位后缀 : 毫秒
     * </ul>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @param defaultValue
     *            指定参数不存在时的默认值(注意: 必须是毫秒单位的值)
     * @return 时间类型的参数值(单位: 毫秒)
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public long getMilliseconds(String key, long defaultValue) {
        ConfigNode cn = this.getChild(key);
        // 判断参数是否存在
        if (cn == null) {
            return defaultValue;
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not time value");
        } else if (cn.value == null) {
            return defaultValue;
        }

        if (cn.value instanceof Number) {
            // 没有单位后缀, 默认即毫秒单位
            return ((Number) cn.value).longValue();
        }

        String str = cn.value.toString();
        if (str.trim().isEmpty()) {
            return defaultValue;
        }

        return textToNumericMillis(str);
    }

    /**
     * 自动将指定的时间参数值(字符串类型)转换到毫秒单位时间值(long类型)
     * <p>
     * 允许以下参数值中包含以下时间单位后缀
     * <ul>
     * <li>'d' 或 'D': 天
     * <li>'h' 或 'H': 小时
     * <li>'m' 或 'M': 分
     * <li>'s' 或 'S': 秒
     * <li>'ms' 或 无单位后缀 : 毫秒
     * </ul>
     * 
     * @param timeStr
     *            时间参数值(字符串类型)
     * @return 毫秒单位时间值(long类型)
     * @throws ClassCastException
     *             非法的时间单位
     */
    public static long textToNumericMillis(String timeStr)
        throws ClassCastException {
        // 获取单位后缀. 注意: 后缀字符最多为2个
        int endIndex = timeStr.length();
        if (!Character.isDigit(timeStr.charAt(endIndex - 1))) {
            endIndex--;
        }
        if (endIndex > 0 && !Character.isDigit(timeStr.charAt(endIndex - 1))) {
            endIndex--;
        }

        // 获取时间数值
        long value = Long.parseLong(timeStr.substring(0, endIndex).trim());

        if (endIndex == timeStr.length()) {
            // 无指定单位, 默认毫秒
            return value;
        } else if (endIndex == timeStr.length() - 2) {
            if (timeStr.endsWith("ms")) {
                // 毫秒单位
                return value;
            } else {
                throw new ClassCastException("unknown time unit:"
                    + timeStr.substring(endIndex));
            }
        }

        // 根据单位后缀字符将时间值统一转换到毫秒单位
        switch (timeStr.charAt(endIndex)) {
            case 'd':
            case 'D':
                return TimeUnit.DAYS.toMillis(value);
            case 'h':
            case 'H':
                return TimeUnit.HOURS.toMillis(value);
            case 'm':
            case 'M':
                return TimeUnit.MINUTES.toMillis(value);
            case 's':
            case 'S':
                return TimeUnit.SECONDS.toMillis(value);
            default:
                // nothing todo
                break;
        }

        throw new ClassCastException("unknown time unit:"
            + timeStr.substring(endIndex));
    }

    /**
     * 强制以long类型返回字节单位的容量类型配置参数值, 如果指定参数不存在则返回<code>defaultValue</code>值
     * <p>
     * 自动将指定的容量参数值转换到字节单位,允许以下参数值中包含以下容量单位后缀.
     * <li>'t'或'T'或'TB': 1024 * 1024 * 1024 * 1024字节
     * <li>'g'或'G'或'GB': 1024 * 1024 * 1024字节
     * <li>'m'或'M'或'MB': 1024 * 1024字节
     * <li>'k'或'K'或'KB': 1024字节
     * <li>'B'或 无单位后缀 : 字节
     * <p>
     * 如果存在多个相关配置参数, 则选择第一个配置参数值
     * <p>
     * 参数路径可以是配置节点名称, 或者是采用'.'分隔的嵌套参数节点路径
     * 
     * @param key
     *            配置参数名路径
     * @param defaultValue
     *            指定参数不存在时的默认值(注意: 必须是字节单位的值)
     * @return 字节容量类型的参数值(单位: 字节)
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public long getByteSize(String key, long defaultValue) {
        ConfigNode cn = this.getChild(key);
        // 判断参数是否存在
        if (cn == null) {
            return defaultValue;
        } else if (cn.hasChildren()) {
            throw new ClassCastException(key + " is not size value");
        } else if (cn.value == null) {
            return defaultValue;
        }

        if (cn.value instanceof Number) {
            // 没有单位后缀, 默认即字节单位
            return ((Number) cn.value).longValue();
        }

        String str = cn.value.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }

        return textToNumericByteSize(str);
    }

    /**
     * 自动将指定的容量参数值转换到字节单位,允许以下参数值中包含以下容量单位后缀.
     * <ul>
     * <li>'t'或'T'或'TB': 1024 * 1024 * 1024 * 1024字节
     * <li>'g'或'G'或'GB': 1024 * 1024 * 1024字节
     * <li>'m'或'M'或'MB': 1024 * 1024字节
     * <li>'k'或'K'或'KB': 1024字节
     * <li>'B'或 无单位后缀 : 字节
     * </ul>
     * 
     * @param sizeStr
     *            容量参数值(字符串)
     * @return 字节容量类型的参数值(单位: 字节)
     * @throws ClassCastException
     *             转换参数值类型失败
     */
    public static long textToNumericByteSize(String sizeStr)
        throws ClassCastException {
        // 获取单位后缀. 注意: 后缀字符最多为2个
        int endIndex = sizeStr.length();
        if (!Character.isDigit(sizeStr.charAt(endIndex - 1))) {
            endIndex--;
        }
        if (endIndex > 0 && !Character.isDigit(sizeStr.charAt(endIndex - 1))) {
            endIndex--;
        }

        // 获取容量数值
        long value = Long.parseLong(sizeStr.substring(0, endIndex).trim());

        if (endIndex == sizeStr.length()) {
            // 无指定单位, 默认为字节
            return value;
        } else if (endIndex == sizeStr.length() - 2) {
            if (sizeStr.charAt(endIndex + 1) != 'b'
                && sizeStr.charAt(endIndex + 1) != 'B') {
                throw new ClassCastException("unknown size unit:"
                    + sizeStr.substring(endIndex));
            }
        }

        // 根据单位后缀字符将容量值统一转换到字节单位
        long radix = 1;
        switch (sizeStr.charAt(endIndex)) {
            case 't':
            case 'T':
                radix *= 1024;
            case 'g':
            case 'G':
                radix *= 1024;
            case 'm':
            case 'M':
                radix *= 1024;
            case 'k':
            case 'K':
                radix *= 1024;
            case 'b':
            case 'B':
                if (endIndex == sizeStr.length() - 2
                    && (sizeStr.charAt(endIndex) == 'b' || sizeStr
                        .charAt(endIndex) == 'B')) {
                    throw new ClassCastException("unknown size unit:"
                        + sizeStr.substring(endIndex));
                }

                return value * radix;
            default:
                // nothing todo
                break;
        }

        throw new ClassCastException("unknown size unit:"
            + sizeStr.substring(endIndex));
    }

    /**
     * 添加属性键值对
     * <p>
     * 旧的键值对将被替换
     * 
     * @param name
     *            属性名称
     * @param value
     *            属性值 (null=移除该属性值)
     */
    public void addAttribute(String name, String value) {
        if (name == null) {
            return;
        }

        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }

        if (value != null) {
            attributes.put(name, value);
        } else {
            attributes.remove(name);
        }
    }

    /**
     * 获取属性值
     * 
     * @param name
     *            属性名称
     * @return 属性值
     */
    public String getAttribute(String name) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(name);
    }

    /**
     * 获取属性值
     * 
     * @param name
     *            属性名称
     * @param defaultValue
     *            属性不存在时的默认返回值
     * @return 属性值
     */
    public String getAttribute(String name, String defaultValue) {
        String value = this.getAttribute(name);
        return (value == null ? defaultValue : value);
    }

    /**
     * 获取属性映射表实例
     * 
     * @return object=属性映射表; null=没有属性存在
     */
    public Map<String, String> attributes() {
        return this.attributes;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // 不标准的XML格式

        StringBuilder sb = new StringBuilder();
        sb.append('<').append(this.name);
        if (this.attributes != null && !this.attributes.isEmpty()) {

            for (Entry<String, String> entry: this.attributes.entrySet()) {
                if (entry == null) {
                    continue;
                }
                sb.append(' ').append(entry.getKey()).append("=\"")
                    .append(entry.getValue()).append('"');
            }
        } else {
            sb.append('>');
        }

        if (this.value != null) {
            sb.append(this.value);
        }

        sb.append("</").append(this.name).append('>');
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(ConfigNode other) {
        if (other == null) {
            return 1;
        }

        // 比较参数名称
        int diff = name.compareTo(other.name);
        if (diff != 0) {
            return diff;
        }

        // 比较参数值
        if (value == null) {
            if (other.value != null) {
                return -1;
            }
        } else if (other.value == null) {
            return 0;
        } else if (!value.equals(other.value)) {
            return 1;
        }

        // 比较子节点个数
        diff = children.size() - other.children.size();
        if (diff != 0) {
            return diff;
        }

        /*
         * 递归方式比较子节点
         */
        for (Entry<String, Object> entry: children.entrySet()) {
            Object child = entry.getValue();
            Object otherChild = other.children.get(entry.getKey());
            if (otherChild == null) {
                return 1;
            }

            if (child instanceof List) { // 比较子节点列表
                if (otherChild instanceof List) {
                    List<ConfigNode> childList = (List<ConfigNode>) child;
                    List<ConfigNode> otherChildList = (List<ConfigNode>) otherChild;

                    diff = childList.size() - otherChildList.size();
                    if (diff != 0) {
                        return diff;
                    }

                    for (int i = 0; i < childList.size(); i++) {
                        diff = childList.get(i)
                            .compareTo(otherChildList.get(i));
                        if (diff != 0) {
                            return diff;
                        }
                    }
                } else {
                    return 1;
                }
            } else { // 比较单个子节点
                if (otherChild instanceof ConfigNode) {
                    diff = ((ConfigNode) child)
                        .compareTo((ConfigNode) otherChild);
                    if (diff != 0) {
                        return diff;
                    }
                } else {
                    return -1;
                }
            }
        }

        /*
         * 比较属性值
         */
        if (attributes == null) {
            if (other.attributes != null) {
                return -1;
            }
        } else if (other.attributes == null) {
            return 1;
        } else {
            diff = attributes.size() - other.attributes.size();
            if (diff != 0) {
                return diff;
            }

            for (Entry<String, String> entry: attributes.entrySet()) {
                String otherAttrValue = other.attributes.get(entry.getKey());
                if (otherAttrValue == null) {
                    return 1;
                }

                diff = otherAttrValue.compareTo(entry.getValue());
                if (diff != 0) {
                    return diff;
                }
            }
        }

        return 0;
    }

    /**
     * 内部ConfigNode类型的迭代器类
     * <p>
     * 不允许调用{@link #remove()},会抛出异常
     * 
     * @author <a href="mailto:qiusheng@corp.netease.com">QiuSheng</a>
     */
    private class InnerIterator implements Iterator<ConfigNode> {
        /**
         * 当前子节点迭代器
         */
        private final Iterator<Object> currentIter;

        /**
         * 下次ConfigNode节点对象(null=没有其他子节点了)
         */
        private ConfigNode next = null;

        /**
         * 当前节点为List时的ConfigNode迭代器
         */
        private Iterator<ConfigNode> listElemIter = null;

        public InnerIterator() {
            currentIter = children.values().iterator();
            this.next();
        }

        @Override
        public boolean hasNext() {
            return (next != null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public ConfigNode next() {
            ConfigNode cn = next;

            next = iterInList();
            if (next != null) {
                return cn;
            }

            while (currentIter.hasNext()) {
                Object obj = currentIter.next();
                if (obj instanceof ConfigNode) {
                    next = (ConfigNode) obj;
                    return cn;
                } else if (obj instanceof List) {
                    listElemIter = ((List<ConfigNode>) obj).iterator();
                    next = iterInList();
                    if (next != null) {
                        return cn;
                    }
                }
            }

            return cn;
        }

        /**
         * 在List节点中遍历ConfigNode节点
         * 
         * @return
         */
        private ConfigNode iterInList() {
            next = null;
            while (listElemIter != null && listElemIter.hasNext()
                && next == null) {
                next = listElemIter.next();
            }

            if (next == null) {
                listElemIter = null;
            }

            return next;
        }

        @Override
        public void remove() {
            throw new IllegalAccessError("can't modify");
        }
    }

}
