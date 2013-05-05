/**
 * Copyright (c) www.netease.com 2010.<br>
 * Created on 2010-11-18
 * 
 * @version $Revision: 1.0$
 */
package com.git.original.common.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供一些反射方面功能的封装.
 */
public final class BeanUtils {

    /** 日志记录 */
    protected static final Logger LOG = LoggerFactory
        .getLogger(BeanUtils.class);

    /** 内部保存class属性与Field映射关系，减少重复调用时，反射检索Class字段 */
    private static ConcurrentMap<Class<?>, Map<String, Field>> propertyToFieldMappings = new ConcurrentHashMap<Class<?>, Map<String, Field>>();

    /** 构造函数 */
    private BeanUtils() {};

    /**
     * 循环向上转型,获取对象的DeclaredField.
     * 
     * @throws NoSuchFieldException
     *             如果没有该Field时抛出.
     */
    public static Field getDeclaredField(Object object, String propertyName)
        throws NoSuchFieldException {
        if (object == null) {
            throw new NullPointerException(
                "Get declared field object is empty.");
        }

        Map<String, Field> mappings = propertyToFieldMappings.get(object
            .getClass());
        if (mappings == null) {
            Field[] fields = object.getClass().getDeclaredFields();
            mappings = new HashMap<String, Field>(fields.length);
            for (Field field: fields) {
                mappings.put(field.getName(), field);
            }
            propertyToFieldMappings.putIfAbsent(object.getClass(), mappings);
        }

        return mappings.get(propertyName);
    }

    /**
     * 循环向上转型,获取对象的DeclaredField.
     * 
     * @throws NoSuchFieldException
     *             如果没有该Field时抛出.
     */
    public static Field getDeclaredField(Class<?> clazz, String propertyName)
        throws NoSuchFieldException {
        if (clazz == null) {
            throw new NullPointerException("Get declared field class is empty.");
        }
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass
            .getSuperclass()) {
            try {
                return superClass.getDeclaredField(propertyName);
            } catch (NoSuchFieldException e) {
                // Field不在当前类定义,继续向上转型
            }
        }
        throw new NoSuchFieldException("No such field: " + clazz.getName()
            + '.' + propertyName);
    }

    /**
     * 暴力获取对象变量值,忽略private,protected修饰符的限制.
     * 
     * @throws NoSuchFieldException
     *             如果没有该Field时抛出.
     */
    public static Object forceGetProperty(Object object, String propertyName)
        throws NoSuchFieldException {
        if (object == null) {
            throw new NullPointerException(
                "Force get property object is empty.");
        }

        Field field = getDeclaredField(object, propertyName);

        boolean accessible = field.isAccessible();
        field.setAccessible(true);

        Object result = null;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            LOG.info("error wont' happen");
        }
        field.setAccessible(accessible);
        return result;
    }

    /**
     * 暴力设置对象变量值,忽略private,protected修饰符的限制.
     * 
     * @throws NoSuchFieldException
     *             如果没有该Field时抛出.
     */
    public static void forceSetProperty(Object object, String propertyName,
        Object newValue) throws NoSuchFieldException {
        if (object == null) {
            throw new NullPointerException(
                "Force set property object is empty.");
        }

        Field field = getDeclaredField(object, propertyName);
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        try {
            field.set(object, newValue);
        } catch (IllegalAccessException e) {
            LOG.info("Error won't happen");
        }
        field.setAccessible(accessible);
    }

    /**
     * 暴力调用对象函数,忽略private,protected修饰符的限制.
     * 
     * @throws NoSuchMethodException
     *             如果没有该Method时抛出.
     */
    public static Object invokePrivateMethod(Object object, String methodName,
        Object... params) throws NoSuchMethodException {
        if (object == null) {
            throw new NullPointerException(
                "Invoke private method object is empty.");
        }

        Class<?>[] types = null;
        if (params != null) {
            types = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                types[i] = params[i].getClass();
            }
        }
        Class<?> clazz = object.getClass();
        Method method = null;
        for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass
            .getSuperclass()) {
            try {
                method = superClass.getDeclaredMethod(methodName, types);
                break;
            } catch (NoSuchMethodException e) {
                // 方法不在当前类定义,继续向上转型
            }
        }

        if (method == null) {
            throw new NoSuchMethodException("No Such Method:"
                + clazz.getSimpleName() + methodName);
        }

        boolean accessible = method.isAccessible();
        method.setAccessible(true);
        Object result = null;
        try {
            result = method.invoke(object, params);
        } catch (Exception e) {
            LOG.error("invokePrivateMethod exception", e);
        }
        method.setAccessible(accessible);
        return result;
    }

    /**
     * 按Filed的类型取得Field列表.
     */
    public static List<Field> getFieldsByType(Object object, Class<?> type) {
        List<Field> list = new ArrayList<Field>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field: fields) {
            if (field.getType().isAssignableFrom(type)) {
                list.add(field);
            }
        }
        return list;
    }

    /**
     * 按FiledName获得Field的类型.
     */
    public static Class<?> getPropertyType(Class<?> type, String name)
        throws NoSuchFieldException {
        return getDeclaredField(type, name).getType();
    }

    /**
     * 获得field的getter函数名称.
     */
    public static String getGetterName(Class<?> type, String fieldName) {
        if (type.getName().equals("boolean")) {
            return "is" + StringUtils.capitalize(fieldName);
        } else {
            return "get" + StringUtils.capitalize(fieldName);
        }
    }

    /**
     * 获得field的setter函数名称.
     */
    public static String getSetterName(Class<?> type, String fieldName) {
        return "set" + StringUtils.capitalize(fieldName);
    }

    /**
     * 获得field的getter函数,如果找不到该方法,返回null.
     */
    public static Method getGetterMethod(Class<?> type, String fieldName) {
        try {
            return type.getMethod(getGetterName(type, fieldName));
        } catch (NoSuchMethodException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public static List<String> getPropertyNames(Class<?> type) {
        List<String> list = new ArrayList<String>();
        Method[] methods = type.getMethods();
        for (Method method: methods) {
            if (method.getName().startsWith("set")) {
                String propertyName = method.getName().substring(3,
                    method.getName().length());
                propertyName = propertyName.substring(0, 1).toLowerCase()
                    + propertyName.substring(1, propertyName.length());
                list.add(propertyName);
            }
        }
        return list;
    }
}
