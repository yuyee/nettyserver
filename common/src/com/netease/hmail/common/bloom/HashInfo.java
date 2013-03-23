/**
 * @(#)HashInfo.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.bloom;

/**
 * HashInfo信息比较
 */
class HashInfo {

    /**
     * 手印value
     */
    int fingerValue;

    /**
     * d位置hash值
     */
    int[] hashs;

    /**
     * 手印是否存在
     */
    boolean isExist;

    /**
     * 如果手印存在，标记第几张子表
     */
    int tableIndex;

    /**
     * 如果手印存在，标记第几个bucket数
     */
    int bucketIndex;

    /**
     * 手印存在，标记手印存在的cell index
     */
    int cellIndex;

    public int getFingerValue() {
        return fingerValue;
    }

    public void setFingerValue(int fingerValue) {
        this.fingerValue = fingerValue;
    }

    public int[] getHashs() {
        return hashs;
    }

    public void setHashs(int[] hashs) {
        this.hashs = hashs;
    }

    public boolean isExist() {
        return isExist;
    }

    public void setExist(boolean isExist) {
        this.isExist = isExist;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    public int getBucketIndex() {
        return bucketIndex;
    }

    public void setBucketIndex(int bucketIndex) {
        this.bucketIndex = bucketIndex;
    }

    public int getCellIndex() {
        return cellIndex;
    }

    public void setCellIndex(int cellIndex) {
        this.cellIndex = cellIndex;
    }

    /**
     * 设置此手印存在的信息，包括的子表信息，桶信息，cell信息.
     * 
     * @param tableindex
     * @param bucketindex
     * @param cellindex
     */
    public void setFingerPrintExists(int tableindex, int bucketindex,
        int cellindex) {
        this.tableIndex = tableindex;
        this.bucketIndex = bucketindex;
        this.cellIndex = cellindex;
        this.isExist = true;
    }
}
