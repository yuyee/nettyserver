/**
 * @(#)MessageIdUtils.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.netease.hmail.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 生成Message-ID的工具类
 */
public final class MessageIdUtils {
    /**
     * Message-ID的模式
     */
    private static Pattern messageIdPattern = Pattern
        .compile("<?([^<]*?)\\.(\\d+)\\.\\d+\\.Hmail\\.(.*?@[^>]*)>?");

    /** 构造函数 */
    private MessageIdUtils() {};

    /**
     * 产生Message-ID
     * <p>
     * 格式为: <code>&lt;external-id.msg-type.current-millis.Hmail.uid&gt;</code>
     * 
     * @param uid
     *            用户的email
     * @param msgType
     *            发信类型
     * @param mailboxId
     *            邮筒ID
     * @param mailId
     *            邮件ID
     * @param mbDigest
     *            邮筒标识摘要(能基本保证不同Hmail内邮筒摘要不同)
     * @return 惟一的Message-ID
     */
    public static String createMessageId(String uid, int msgType,
        long mailboxId, long mailId, byte[] mbDigest) {
        String mailExternalId = MailUniqueIdUtils.createUniqueId(mailboxId,
            mailId, mbDigest);
        StringBuilder s = new StringBuilder();

        // Unique string is <mailExternalId>.<msgType>.<currentTime>.Hmail.<uid>
        s.append('<').append(mailExternalId).append('.').append(msgType)
            .append('.').append(System.currentTimeMillis()).append('.')
            .append("Hmail.").append(uid).append('>');

        return s.toString();
    }

    /**
     * 解析Message-ID, 获取邮件的External-Id和用户帐号, 以<code>MessageIdParseInfo</code>
     * 对象返回.
     * 
     * @param messageId
     *            邮件message-id
     * @return 解析结果, {@link MessageIdParseInfo}
     * @throws IllegalArgumentException
     */
    public static MessageIdParseInfo parseMessageId(String messageId)
        throws IllegalArgumentException {
        Matcher matcher = messageIdPattern.matcher(messageId);
        if (matcher.find()) {
            if (matcher.group(1).isEmpty() || matcher.group(2).isEmpty()
                || matcher.group(3).isEmpty()) {
                throw new IllegalArgumentException(
                    "Message-ID format is error.");
            }

            MessageIdParseInfo msgParseInfo = new MessageIdParseInfo(
                matcher.group(1), matcher.group(3), matcher.group(2));

            return msgParseInfo;
        } else {
            throw new IllegalArgumentException("Message-ID format is error.");
        }
    }

    /**
     * 邮件MessageId的解析结果类
     * <p>
     * 主要包含了邮件的external id和用户帐号信息
     * 
     * @author cmj
     */
    public static class MessageIdParseInfo {
        /**
         * 邮件的external id
         */
        private String mailExternalId = null;

        /**
         * 用户的帐号
         */
        private String uid = null;

        /**
         * 邮件类型
         */
        private String msgType = null;

        /**
         * 构造函数
         * 
         * @param mailExternalId
         *            邮件的外部Id
         * @param uid
         *            用户帐号
         */
        public MessageIdParseInfo(String mailExternalId, String uid,
            String msgType) {
            super();
            this.mailExternalId = mailExternalId;
            this.uid = uid;
            this.msgType = msgType;
        }

        /**
         * @return the mail external id
         */
        public String getMailExternalId() {
            return mailExternalId;
        }

        /**
         * @return the uid
         */
        public String getUid() {
            return uid;
        }

        /**
         * @return the msgType
         */
        public String getMsgType() {
            return msgType;
        }

    }

    public static void main(String[] args) {
        String s = "ALAApgDwAAsAC46IVqnBnapt.1.1313666918947.Hmail.test2@hztest.mail.netease.com";
        MessageIdParseInfo info = parseMessageId(s);
        System.out.println(info.mailExternalId);
    }
}
