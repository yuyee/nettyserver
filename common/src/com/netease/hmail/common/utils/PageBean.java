/**
 * Copyright (c) www.netease.com 2010.<br>
 * Created on 2010-12-7
 * 
 * @author <a href="nisonghai@corp.netease.com">Ni Songhai</a>
 * @version $Revision: 1.0$
 */
package com.netease.hmail.common.utils;

/**
 * 用于分页处理
 */
public class PageBean {

    /**
     * 开始值
     */
    private int firstResult = 0;

    /**
     * 每页最大值
     */
    private int maxResults = 10;

    public PageBean() {}

    public PageBean(int firstResult, int maxResults) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }

    public boolean isValid() {
        return (firstResult > 0 || maxResults > 0);
    }

    /**
     * 返回当前第一条记录开始
     * 
     * @return
     */
    public int getFirstResult() {
        return firstResult;
    }

    /**
     * 返回每页最大记录数
     * 
     * @return
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * 设置第一条记录起始值
     * 
     * @param i
     */
    public void setFirstResult(int i) {
        firstResult = i;
    }

    /**
     * 设置每页最大记录数
     * 
     * @param i
     */
    public void setMaxResults(int i) {
        maxResults = i;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{offset=" + this.firstResult + ",limit=" + this.maxResults
            + "}";
    }

}
