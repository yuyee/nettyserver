/**
 * @(#)XTextUtils.java, 2013-2-24.
 * 
 * Copyright 2013 Netease, Inc. All rights reserved.
 * NETEASE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sunstrider.common.utils;

/**
 * RFC 1891: xtext 编解码工具类
 * <p>
 * xtext = *( xchar / hexchar )<br/>
 * <br/>
 * xchar = any ASCII CHAR between "!" (33) and "~" (126) inclusive, except for
 * "+" and "=".<br/>
 * <br/>
 * hexchar = ASCII "+" immediately followed by two upper case hexadecimal digits
 * <br/>
 * <p>
 * When encoding an octet sequence as xtext: <br/>
 * <li>Any ASCII CHAR between "!" and "~" inclusive, except for "+" and "=", <br/>
 * MAY be encoded as itself. (A CHAR in this range MAY instead be <br/>
 * encoded as a "hexchar", at the implementor's discretion.)
 * <li>ASCII CHARs that fall outside the range above must be encoded as
 * "hexchar".
 */
public final class XTextUtils {
    /**
     * 感叹号(ASCII值=33)
     */
    private static final char ASCII_CHAR_EXCLAMATION = '!';

    /**
     * 波浪号(ASCII值=126)
     */
    private static final char ASCII_CHAR_TILDE = '~';

    /**
     * 加号(ASCII值=43)
     */
    private static final char ASCII_CHAR_PLUS = '+';

    /**
     * 等号(ASCII值=61)
     */
    private static final char ASCII_CHAR_EQUAL = '=';

    /** 构造函数 */
    private XTextUtils() {};

    /**
     * 对XTEXT编码格式的字符串进行解码
     * 
     * @param xtext
     * @return
     */
    public static String decode(String xtext) {
        if (xtext == null) {
            return null;
        }

        int plusPos = xtext.indexOf(ASCII_CHAR_PLUS);
        if (plusPos < 0) {
            // TODO 目前假定为输入的xtext字符串格式标准, 因此没有'+'字符时则直接返回
            return xtext;
        }

        StringBuilder result = new StringBuilder(xtext.length());
        if (plusPos > 0) {
            result.append(xtext.substring(0, plusPos));
        }

        for (int i = plusPos; i < xtext.length(); i++) {
            char c = xtext.charAt(i);

            if (c == ASCII_CHAR_PLUS) {
                // decode "hexchar"
                if (plusPos + 2 > xtext.length()) {
                    throw new ArrayIndexOutOfBoundsException(
                        "illegal xtext string: pluspos=" + i
                            + " but string length=" + xtext.length());
                }

                int code = (Character.digit(xtext.charAt(i + 1), 16) << 4)
                    | Character.digit(xtext.charAt(i + 2), 16);
                i += 2;

                result.append((char) code);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * 16进制用到的字符
     */
    private static final char[] digits = { '0', '1', '2', '3', '4', '5', '6',
        '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * append xtexted unquoted string to append. specially, we treat char
     * special as a valid xchar here
     * 
     * @param toAppend
     * @param text
     */
    public static void appendXtext(StringBuilder toAppend, String unquoted,
        char special) {
        if (toAppend == null || unquoted == null) {
            return;
        }
        for (int i = 0; i < unquoted.length(); i++) {
            char c = unquoted.charAt(i);
            if (c == special
                || ((c >= ASCII_CHAR_EXCLAMATION && c <= ASCII_CHAR_TILDE)
                    && c != ASCII_CHAR_PLUS && c != ASCII_CHAR_EQUAL)) {
                toAppend.append(c);
            } else {
                toAppend.append('+').append(digits[(((int) c) & 0xf0) >> 4])
                    .append(digits[((int) c) & 0x0f]);
            }
        }
    }

    /**
     * 将字符串编码为XTEXT格式文本
     * 
     * @param text
     *            普通文本
     * @return XTEXT文本
     */
    public static String encode(String text) {
        if (text == null) {
            return null;
        }

        StringBuilder result = new StringBuilder(text.length());
        int pos = 0;
        for (; pos < text.length(); pos++) {
            char c = text.charAt(pos);

            if (c == ASCII_CHAR_PLUS || c == ASCII_CHAR_EQUAL) {
                break;
            } else if (c < ASCII_CHAR_EXCLAMATION || c > ASCII_CHAR_TILDE) {
                break;
            }
        }

        if (pos >= text.length()) {
            return text;
        }

        /*
         * 存在需要转义的字符
         */

        result.append(text.substring(0, pos));
        for (; pos < text.length(); pos++) {
            char c = text.charAt(pos);

            if (c >= ASCII_CHAR_EXCLAMATION && c <= ASCII_CHAR_TILDE) {
                if (c != ASCII_CHAR_PLUS && c != ASCII_CHAR_EQUAL) {
                    result.append(c);
                    continue;
                }
            }

            result.append('+').append(digits[(((int) c) & 0xf0) >> 4])
                .append(digits[((int) c) & 0x0f]);
        }

        return result.toString();
    }

}
