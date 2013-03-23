/**
 * @(#)RemoteLoggerUtils.java, 2011-11-18. 
 * 
 * Copyright 2011 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.logging;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.netease.hmail.common.json.JSONParser;
import com.netease.hmail.common.json.JSONValue;
import com.netease.hmail.common.json.ParseException;
import com.netease.hmail.common.utils.IPUtils;

/**
 * 远程日志助手类
 */
public final class RemoteLoggerHelper {
    /**
     * 远程日志编号生成器
     */
    private static final AtomicLong REMOTE_LOG_SERIAL_NUMBER_POOL = new AtomicLong(
        System.currentTimeMillis());

    // ------------------------------------------------

    /**
     * 远程日志类型
     */
    public static final String REMOTE_LOG_PARAM_TYPE = "type";

    /**
     * 远程日志类型: 邮件删除日志
     */
    public static final String REMOTE_LOG_PARAM_TYPE_REMOVE_MAIL = "mailrm";

    /**
     * 远程日志类型: 邮件接收日志
     */
    public static final String REMOTE_LOG_PARAM_TYPE_RECEIVE_MAIL = "mailin";

    /**
     * 远程日志类型: 邮件投递日志
     */
    public static final String REMOTE_LOG_PARAM_TYPE_DELIVER = "deliver";

    /**
     * 远程日志类型: 邮件投递统计日志
     */
    public static final String REMOTE_LOG_PARAM_TYPE_DELIVER_STAT = "statdl";

    /**
     * 远程日志内容
     */
    public static final String REMOTE_LOG_PARAM_CONTENT = "ct";

    /** 远程日志参数: 邮筒ID */
    public static final String REMOTE_LOG_PARAM_MAILBOX_ID = "mbid";

    /** 远程日志参数: 文件夹ID */
    public static final String REMOTE_LOG_PARAM_FOLDER_ID = "fid";

    /** 远程日志参数: 邮件ID */
    public static final String REMOTE_LOG_PARAM_MAIL_ID = "mid";

    /** 远程日志参数: 接收邮件'From'字段 */
    public static final String REMOTE_LOG_PARAM_MAIL_FROM = "from";

    /** 远程日志参数: 接收邮件'To'字段 */
    public static final String REMOTE_LOG_PARAM_MAIL_TO = "to";

    /** 远程日志参数: 接收邮件'Subject'字段 */
    public static final String REMOTE_LOG_PARAM_MAIL_SUBJECT = "subject";

    /** 远程日志参数: 发起操作的客户端/服务器IP */
    public static final String REMOTE_LOG_PARAM_REMOTE_IP = "rip";

    /** 远程日志参数: 执行操作的应用服务器ip值 */
    public static final String REMOTE_LOG_PARAM_APP_IP = "aip";

    /** 远程日志参数: 日志编号 */
    public static final String REMOTE_LOG_PARAM_LOG_SERIAL_NUMBER = "lsn";

    /** 远程日志参数: 事务ID */
    public static final String REMOTE_LOG_PARAM_TRANSACTION_ID = "tid";

    // ------------------------------------------------

    /** 邮件删除日志参数: 删除数值 */
    public static final String RL_REMOVE_PARAM_VALUE = "value";

    /** 邮件删除日志参数: 邮件ID列表 */
    public static final String RL_REMOVE_PARAM_MAIL_IDS = "mids";

    /** 邮件删除日志参数: 删除时间戳 */
    public static final String RL_REMOVE_PARAM_DELETE_TIME = "dt";

    // ------------------------------------------------

    /** 邮件接收日志参数: 接收结果数值 */
    public static final String RL_RECEIVE_PARAM_RESULT = "result";

    /** 邮件接收日志参数: 接收邮件大小 */
    public static final String RL_RECEIVE_PARAM_SIZE = "size";

    /** 邮件接收日志参数: 接收邮件时间戳 */
    public static final String RL_RECEIVE_PARAM_RECEIVED_TIME = "rt";

    /** 邮件接收日志参数: 接收邮件的域 */
    public static final String RL_RECEIVE_PARAM_RECEIVED_DOMAIN = "domain";

    /** 邮件接收日志参数: 实际进信帐户 */
    public static final String RL_RECEIVE_PARAM_RECIPIENT = "rcpt";

    /** 邮件接收日志参数: 实际进信主域名ID */
    public static final String RL_RECEIVE_PARAM_MAIN_DOMAIN_ID = "mdid";

    // ------------------------------------------------
    /** 邮件投递日志参数: mail-from参数值 */
    public static final String RL_DELIVER_PARAM_SENDER = "sender";

    /** 邮件投递日志参数: 发信人域名 */
    public static final String RL_DELIVER_PARAM_DOMAIN = "domain";

    /** 邮件投递日志参数: 投递状态 */
    public static final String RL_DELIVER_PARAM_STATUS = "st";

    /** 邮件投递日志参数: 邮件类型 */
    public static final String RL_DELIVER_PARAM_MAIL_TYPE = "mt";

    /** 邮件投递日志参数: 投递时间戳 */
    public static final String RL_DELIVER_PARAM_DELIVER_TIME = "dt";

    /** 邮件投递日志参数: 发送副本邮件ID */
    public static final String RL_DELIVER_PARAM_SAVESENT_MAIL_ID = "smid";

    /** 邮件投递日志参数: 进信邮筒ID */
    public static final String RL_DELIVER_PARAM_RECEIVED_MAILBOX_ID = "rmbid";

    /** 邮件投递日志参数: 进信邮件ID */
    public static final String RL_DELIVER_PARAM_RECEIVED_MAIL_ID = "rmid";

    /** 邮件投递统计日志参数: 发送邮件的rcpt个数 */
    public static final String RL_DELIVER_PARAM_RCPT_COUNT = "rc";

    /** 邮件投递统计日志参数: 发信人主帐户ID */
    public static final String RL_DELIVER_PARAM_MAIN_ACCOUNT_ID = "maid";

    /** 邮件投递统计日志参数: 发信人主域名ID */
    public static final String RL_DELIVER_PARAM_MAIN_DOMAIN_ID = "mdid";

    // ------------------------------------------------

    /** 构造函数 */
    private RemoteLoggerHelper() {};

    /**
     * 构建一条邮件删除的远程日志记录
     * 
     * @param rmValue
     *            删除数值
     * @param mailBoxId
     *            邮筒ID
     * @param mid
     *            被删除的邮件ID
     * @param delTime
     *            删除时间戳
     * @param userIp
     *            触发删除操作的用户IP
     * @return 删除日志记录
     */
    public static String createRemoveMailLog(int rmValue, long mailBoxId,
        long mid, Long delTime, String userIp) {
        StringBuilder sb = new StringBuilder(128);
        sb.append('{'); // start record json object

        sb.append('"').append(REMOTE_LOG_PARAM_TYPE).append("\":\"")
            .append(REMOTE_LOG_PARAM_TYPE_REMOVE_MAIL).append("\",\"")
            .append(REMOTE_LOG_PARAM_CONTENT).append("\":{"); // start
                                                              // content
                                                              // json object

        sb.append('"').append(RL_REMOVE_PARAM_VALUE).append("\":");
        JSONValue.appendTo(rmValue, sb);
        appendObject(REMOTE_LOG_PARAM_MAILBOX_ID, mailBoxId, sb);

        appendObject(REMOTE_LOG_PARAM_FOLDER_ID, null, sb);

        sb.append(",\"").append(RL_REMOVE_PARAM_MAIL_IDS).append("\":[");
        JSONValue.appendTo(mid, sb);
        sb.append(']');

        appendObject(RL_REMOVE_PARAM_DELETE_TIME, delTime, sb);
        appendObject(REMOTE_LOG_PARAM_REMOTE_IP, userIp, sb);

        appendObject(REMOTE_LOG_PARAM_APP_IP,
            IPUtils.getLocalIp(IPUtils.LOOPBACK_ADDRESS_VALUE), sb);
        appendObject(REMOTE_LOG_PARAM_LOG_SERIAL_NUMBER,
            Long.toHexString(REMOTE_LOG_SERIAL_NUMBER_POOL.getAndIncrement()),
            sb);

        sb.append('}'); // end content json object

        // end record json object
        return sb.append('}').toString();
    }

    /**
     * 构建一条邮件删除的远程日志记录
     * 
     * @param rmValue
     *            删除数值
     * @param mailBoxId
     *            邮筒ID
     * @param fid
     *            被清理的文件夹ID
     * @param mids
     *            被删除的邮件ID列表
     * @param delTime
     *            删除时间戳
     * @param userIp
     *            触发删除操作的用户IP
     * @return 删除日志记录
     */
    public static String createRemoveMailLog(int rmValue, long mailBoxId,
        Long fid, List<Long> mids, Long delTime, String userIp) {
        StringBuilder sb = new StringBuilder(128);
        sb.append('{'); // start record json object

        sb.append('"').append(REMOTE_LOG_PARAM_TYPE).append("\":\"")
            .append(REMOTE_LOG_PARAM_TYPE_REMOVE_MAIL).append("\",\"")
            .append(REMOTE_LOG_PARAM_CONTENT).append("\":{"); // start
                                                              // content
                                                              // json object

        sb.append('"').append(RL_REMOVE_PARAM_VALUE).append("\":");
        JSONValue.appendTo(rmValue, sb);
        appendObject(REMOTE_LOG_PARAM_MAILBOX_ID, mailBoxId, sb);

        appendObject(REMOTE_LOG_PARAM_FOLDER_ID, fid, sb);

        if (mids == null || mids.isEmpty()) {
            sb.append(",\"").append(RL_REMOVE_PARAM_MAIL_IDS).append("\":[]");
        } else {
            sb.append(",\"").append(RL_REMOVE_PARAM_MAIL_IDS).append("\":");
            JSONValue.appendTo(mids, sb);
        }

        appendObject(RL_REMOVE_PARAM_DELETE_TIME, delTime, sb);
        appendObject(REMOTE_LOG_PARAM_REMOTE_IP, userIp, sb);

        appendObject(REMOTE_LOG_PARAM_APP_IP,
            IPUtils.getLocalIp(IPUtils.LOOPBACK_ADDRESS_VALUE), sb);
        appendObject(REMOTE_LOG_PARAM_LOG_SERIAL_NUMBER,
            Long.toHexString(REMOTE_LOG_SERIAL_NUMBER_POOL.getAndIncrement()),
            sb);

        sb.append('}'); // end content json object

        // end record json object
        return sb.append('}').toString();
    }

    /**
     * 构建一条邮件接收的远程日志记录
     * 
     * @param result
     *            接收结果数值
     * @param mailBoxId
     *            邮筒ID
     * @param fid
     *            接收邮件的文件夹ID
     * @param mid
     *            接收后产生的邮件ID
     * @param from
     *            邮件'From'信头
     * @param to
     *            邮件'To'信头
     * @param subject
     *            邮件'Subject'信头
     * @param size
     *            邮件大小
     * @param receivedTime
     *            邮件接收时间
     * @param realRcpt
     *            实际进信帐户地址
     * @param domain
     *            实际进信域名的域名
     * @param mainDomainId
     *            实际进信域名的主ID
     * @param transId
     *            接收事务ID
     * @param deliverIp
     *            投递该邮件的远程服务器IP
     * @return 邮件接收日志记录
     */
    public static String createReceiveMailLog(int result, long mailBoxId,
        Long fid, Long mid, String from, String to, String subject, long size,
        Long receivedTime, String realRcpt, String domain, Long mainDomainId,
        String transId, String deliverIp) {
        StringBuilder sb = new StringBuilder(128);
        sb.append('{'); // start record json object

        sb.append('"').append(REMOTE_LOG_PARAM_TYPE).append("\":\"")
            .append(REMOTE_LOG_PARAM_TYPE_RECEIVE_MAIL).append("\",\"")
            .append(REMOTE_LOG_PARAM_CONTENT).append("\":{"); // start
                                                              // content
                                                              // json object

        sb.append('"').append(RL_RECEIVE_PARAM_RESULT).append("\":");
        JSONValue.appendTo(result, sb);
        appendObject(REMOTE_LOG_PARAM_MAILBOX_ID, mailBoxId, sb);
        appendObject(REMOTE_LOG_PARAM_FOLDER_ID, fid, sb);
        appendObject(REMOTE_LOG_PARAM_MAIL_ID, mid, sb);

        appendObject(REMOTE_LOG_PARAM_MAIL_FROM, from, sb);
        appendObject(REMOTE_LOG_PARAM_MAIL_TO, to, sb);
        appendObject(REMOTE_LOG_PARAM_MAIL_SUBJECT, subject, sb);

        appendObject(RL_RECEIVE_PARAM_SIZE, size, sb);
        appendObject(RL_RECEIVE_PARAM_RECEIVED_TIME, receivedTime, sb);

        appendObject(RL_RECEIVE_PARAM_RECIPIENT, realRcpt, sb);
        appendObject(RL_RECEIVE_PARAM_RECEIVED_DOMAIN, domain, sb);
        appendObject(RL_RECEIVE_PARAM_MAIN_DOMAIN_ID, mainDomainId, sb);

        appendObject(REMOTE_LOG_PARAM_TRANSACTION_ID, transId, sb);
        appendObject(REMOTE_LOG_PARAM_REMOTE_IP, deliverIp, sb);

        appendObject(REMOTE_LOG_PARAM_APP_IP,
            IPUtils.getLocalIp(IPUtils.LOOPBACK_ADDRESS_VALUE), sb);
        appendObject(REMOTE_LOG_PARAM_LOG_SERIAL_NUMBER,
            Long.toHexString(REMOTE_LOG_SERIAL_NUMBER_POOL.getAndIncrement()),
            sb);

        sb.append('}'); // end content json object

        // end record json object
        return sb.append('}').toString();
    }

    /**
     * 构建一条邮件投递日志记录
     * 
     * @param sender
     * @param subject
     * @param rcpt
     * @param status
     * @param msgType
     * @param deliverTime
     * @param senderMboxId
     * @param saveSentMailId
     * @param receivedMboxId
     * @param receivedMailId
     * @param deliverTransId
     * @return
     */
    public static String createRcptDeliverStatusLog(String sender,
        String subject, String rcpt, short status, short msgType,
        long deliverTime, Long senderMboxId, Long saveSentMailId,
        Long receivedMboxId, Long receivedMailId, String deliverTransId) {
        StringBuilder sb = new StringBuilder(256);
        sb.append('{'); // start record json object

        sb.append('"').append(REMOTE_LOG_PARAM_TYPE).append("\":\"")
            .append(REMOTE_LOG_PARAM_TYPE_DELIVER).append("\",\"")
            .append(REMOTE_LOG_PARAM_CONTENT).append("\":{"); // start
                                                              // content
                                                              // json object

        sb.append('"').append(RL_DELIVER_PARAM_SENDER).append("\":");
        JSONValue.appendTo(sender, sb);
        appendObject(REMOTE_LOG_PARAM_MAIL_SUBJECT, subject, sb);
        appendObject(REMOTE_LOG_PARAM_MAIL_TO, rcpt, sb);
        appendObject(RL_DELIVER_PARAM_STATUS, status, sb);
        appendObject(RL_DELIVER_PARAM_MAIL_TYPE, msgType, sb);
        appendObject(RL_DELIVER_PARAM_DELIVER_TIME, deliverTime, sb);

        if (senderMboxId != null) {
            appendObject(REMOTE_LOG_PARAM_MAILBOX_ID, senderMboxId, sb);
        }
        if (saveSentMailId != null) {
            appendObject(RL_DELIVER_PARAM_SAVESENT_MAIL_ID, saveSentMailId, sb);
        }
        if (receivedMboxId != null) {
            appendObject(RL_DELIVER_PARAM_RECEIVED_MAILBOX_ID, receivedMboxId,
                sb);
        }
        if (receivedMailId != null) {
            appendObject(RL_DELIVER_PARAM_RECEIVED_MAIL_ID, receivedMailId, sb);
        }

        appendObject(REMOTE_LOG_PARAM_TRANSACTION_ID, deliverTransId, sb);

        appendObject(REMOTE_LOG_PARAM_APP_IP,
            IPUtils.getLocalIp(IPUtils.LOOPBACK_ADDRESS_VALUE), sb);
        appendObject(REMOTE_LOG_PARAM_LOG_SERIAL_NUMBER,
            Long.toHexString(REMOTE_LOG_SERIAL_NUMBER_POOL.getAndIncrement()),
            sb);

        sb.append('}'); // end content json object

        // end record json object
        return sb.append('}').toString();
    }

    /**
     * 构建一条邮件投递统计日志记录
     * 
     * @param sender
     * @param domain
     * @param rcptCount
     * @param deliverTime
     * @param mainAccountId
     * @param mainDomainId
     * @param transId
     * @param appIp
     * @return
     */
    public static String createDeliverStatLog(String sender, String domain,
        int rcptCount, long deliverTime, Long mainAccountId, Long mainDomainId,
        String transId, int appIp) {
        StringBuilder sb = new StringBuilder(128);
        sb.append('{'); // start record json object

        sb.append('"').append(REMOTE_LOG_PARAM_TYPE).append("\":\"")
            .append(REMOTE_LOG_PARAM_TYPE_DELIVER_STAT).append("\",\"")
            .append(REMOTE_LOG_PARAM_CONTENT).append("\":{"); // start
                                                              // content
                                                              // json object

        sb.append('"').append(RL_DELIVER_PARAM_SENDER).append("\":");
        JSONValue.appendTo(sender, sb);
        appendObject(RL_DELIVER_PARAM_DOMAIN, domain, sb);

        appendObject(RL_DELIVER_PARAM_RCPT_COUNT, rcptCount, sb);
        appendObject(RL_DELIVER_PARAM_DELIVER_TIME, deliverTime, sb);

        if (mainAccountId != null) {
            appendObject(RL_DELIVER_PARAM_MAIN_ACCOUNT_ID, mainAccountId, sb);
        }
        if (mainDomainId != null) {
            appendObject(RL_DELIVER_PARAM_MAIN_DOMAIN_ID, mainDomainId, sb);
        }

        appendObject(REMOTE_LOG_PARAM_TRANSACTION_ID, transId, sb);
        appendObject(REMOTE_LOG_PARAM_APP_IP, appIp, sb);

        sb.append('}'); // end content json object

        // end record json object
        return sb.append('}').toString();
    }

    /**
     * 解析远程日志记录字符串
     * 
     * @param logRecord
     *            日志记录
     * @return 日志参数表
     * @throws IllegalArgumentException
     * @throws ParseException
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseRemoteLogRecord(String logRecord)
        throws ParseException {
        if (logRecord == null) {
            return null;
        }

        if (logRecord.isEmpty()) {
            throw new IllegalArgumentException("not found param["
                + REMOTE_LOG_PARAM_TYPE + "]");
        }

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(logRecord);
        if (!(obj instanceof Map)) {
            throw new IllegalArgumentException("illegal remote log record:"
                + logRecord);
        }

        Map<String, Object> map = (Map<String, Object>) obj;
        if (!map.containsKey(REMOTE_LOG_PARAM_TYPE)) {
            throw new IllegalArgumentException(
                "missing remote log record type:" + logRecord);
        }

        return map;
    }

    /**
     * 以",key=value"的格式追加指定参数对到字符串缓存
     * <p>
     * 如果value为null, 则追加",key="字符串
     * 
     * @param paramKey
     *            待追加的参数名
     * @param paramValue
     *            待追加的参数值
     * @param sb
     *            被追加的字符串缓存
     */
    private static void appendObject(String paramKey, Object paramValue,
        StringBuilder sb) {
        sb.append(",\"").append(paramKey).append("\":");
        JSONValue.appendTo(paramValue, sb);
    }

}
