package com.git.original.server.netty;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于存储附属信息的环境类
 */
public class AttachmentContext {
    /**
     * 内部线程安全的HashMap对象
     */
    private final ConcurrentHashMap<Object, Object> attachments = new ConcurrentHashMap<Object, Object>(
        4);

    /**
     * Default constructor
     */
    public AttachmentContext() {}

    /**
     * 获取指定附件
     * 
     * @param key
     *            附件名称
     * @return 附件对象(null = 附件不存在)
     */
    public Object getAttachment(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        return attachments.get(key);
    }

    /**
     * 获取指定附件
     * 
     * @param key
     *            附件名称
     * @param defaultValue
     *            默认附件
     * @return 附件对象(当附件不存在时,自动返回默认附件)
     */
    public Object getAttachment(Object key, Object defaultValue) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        Object answer = attachments.get(key);
        if (answer == null) {
            return defaultValue;
        }

        return answer;
    }

    /**
     * 设置附件
     * 
     * @param key
     *            附件名称
     * @param value
     *            附件
     * @return 当前已经存在的附件对象(null=当前不存在该名称附件)
     */
    public Object setAttachment(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        if (value == null) {
            return attachments.remove(key);
        }

        return attachments.put(key, value);
    }

    /**
     * 当指定名称的附件不存在时,设置附件
     * 
     * @param key
     *            附件名称
     * @param value
     *            附件
     * @return 当前已经存在的附件对象(null=当前不存在该名称附件,设置成功)
     */
    public Object setAttachmentIfAbsent(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        if (value == null) {
            return null;
        }

        return attachments.putIfAbsent(key, value);
    }

    /**
     * 移除指定名称的附件
     * 
     * @param key
     *            附件名称
     * @return 当前已经存在的附件对象
     */
    public Object removeAttachment(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        return attachments.remove(key);
    }

    /**
     * 移除指定名称和内容的附件
     * 
     * @param key
     *            附件名称
     * @param value
     *            需要移除的附件值
     * @return true=指定的附件存在,移除成功;false=附件不存在或者同名的附件不是指定的附件值, 移除失败
     */
    public boolean removeAttachment(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        if (value == null) {
            return false;
        }

        return attachments.remove(key, value);
    }

    /**
     * 替换指定名称的附件值
     * 
     * @param key
     *            附件名称
     * @param oldValue
     *            老附件值
     * @param newValue
     *            新附件值
     * @return true=指定的附件存在,移除成功;false=附件不存在或者同名的附件不是指定的附件值, 移除失败
     */
    public boolean replaceAttachment(Object key, Object oldValue,
        Object newValue) {
        return attachments.replace(key, oldValue, newValue);
    }

    /**
     * 判断指定名称的附件是否存在
     * 
     * @param key
     *            附件名称
     * @return true=附件存在;false=附件不存在
     */
    public boolean containsAttachment(Object key) {
        return attachments.containsKey(key);
    }

    /**
     * 获取当前附件名称集合
     * <p>
     * 注意: 返回的集合对象不允许修改
     * 
     * @return
     */
    public Set<Object> getAttachmentKeys() {
        return Collections.unmodifiableSet(attachments.keySet());
    }

    /**
     * 清除当前所有已存储的附件
     * 
     * @throws Exception
     */
    public void clearAttachments() throws Exception {
        attachments.clear();
    }
}
