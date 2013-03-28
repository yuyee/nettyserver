/**
 * @(#)ServiceType.java, 2012-4-25. Copyright 2012 Netease, Inc. All rights
 *                       reserved. NETEASE PROPRIETARY/CONFIDENTIAL. Use is
 *                       subject to license terms.
 */
package com.sunstrider.common.server;

/**
 * @author <a href="mailto:qiusheng@corp.netease.com">QiuSheng</a>
 */
public enum ServiceType {
    /** 不确定 */
    UNDEFINED((byte) 0),
    /** POP3服务 */
    POP3((byte) 1),
    /** IMAP服务 */
    IMAP((byte) 2),
    /** session服务(即webmail) */
    SESSION((byte) 3),

    /** 邮件投递请求 */
    DELIVER((byte) 10),
    /** sasl-dovecot-auth服务 */
    SASL_AUTH((byte) 11),
    /** MILTER服务 */
    MILTER((byte) 12),
    /** inspect服务 */
    INSPECT((byte) 13),
    /** bounce服务 */
    BOUNCE((byte) 14),
    /** 本地直接进信服务 */
    LOCAL_SEND((byte) 15),

    /** record服务 */
    RECORD((byte) 100),
    /** maintain服务 */
    MAINTAIN((byte) 101),
    /** maintain服务 */
    MANAGER((byte) 102),

    /** 代理服务 */
    PROXY((byte) 200),
    /** 代收服务 */
    AGENT((byte) 201);
    ;

    private final byte code;

    private ServiceType(byte code) {
        this.code = code;
    }

    /**
     * @return the code
     */
    public byte getCode() {
        return code;
    }

}
