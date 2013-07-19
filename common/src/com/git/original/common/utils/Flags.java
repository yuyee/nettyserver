package com.git.original.common.utils;
/**
 * <ul>标识基提供础类,提供3个基础方法，具体使用者可继承 Flags类，定义如下2个字段即可具备很强的可读性如:
 * static final int ID_READ = 0;
 * <br>public static final long READ = 1L << ID_READ;
 * <p>
 * 代码中即可通过mark(Flags.READ)来标识READ
 * unMark(Flags.READ)来去除标识READ
 * isMark(FLAGS.READ)来判断是否标识了READ
 * 
 * <p>
 * 通过一个flagValue即可让代码具备很强可读性且节省代码量，很适合需要记录多个标识位的业务场景
 * 
 * <li>提供{@link #mark(long)}标记标识
 * <li>提供{@link #unmark(long)}解除标识
 * <li>提供{@link #isMark(long)}查看标识
 * </ul>
 * 
 * @author aoxiang.lax
 * @version $Id: Flags.java, v 0.1 2013-7-19 下午3:43:37 aoxiang.lax Exp $
 */
public abstract class Flags{
    
    /**
     * 所有属性被关闭
     */
    public static final long NONE_FLAG = 0x0L;
    
    /**
     * 所有属性被打开
     */
    public static final long ALL_FLAG = 0xFFFFFFFFFFFFFFFFL;
    
    /**
     * 标识值
     */
    protected long flagsValue = 0;
    
    public Flags() {
        flagsValue = 0;
    }
    
    /**
     * 标记标识
     * 
     * @param toMarkFlags
     */
    public void mark(long toMarkFlags){
        flagsValue |= toMarkFlags;
    }
    
    /** 
     * 解除标识
     * @param toUnmarkFlags 待取消的标签
     */
    public void unmark(long toUnmarkFlags){
        flagsValue &= (~toUnmarkFlags);     
    }
    
    /**
     * 判断是否标识了该 flag
     * 
     * @param markFlags 标识位
     * @return 是否标识
     */
    public boolean isMark(long markFlags){
        return (flagsValue & markFlags) == markFlags;
    }
}
