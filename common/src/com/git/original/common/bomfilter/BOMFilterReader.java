package com.git.original.common.bomfilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 字节流读取器, 自动过滤Unicode字节流中的 BOM 标记字符
 * <p>
 * BOMs:
 * <li>FE FF = UTF-16, big-endian
 * <li>FF FE = UTF-16, little-endian
 * <li>EF BB BF = UTF-8
 * <li>00 00 FE FF = UTF-32, big-endian
 * <li>FF FE 00 00 = UTF-32, little-endian
 * <p>
 * 注意: 非线程安全
 * 
 * @author linaoxiang@corp.netease.com
 */
public class BOMFilterReader extends InputStreamReader {
    /**
     * 需要尝试过滤BOM字符的字符集标准名称集合
     * <p>
     * 根据IANA Charset Registry的名称构建
     */
    private static final Set<String> FILTER_CHARSET_NAME_SET = Collections
        .synchronizedSet(new HashSet<String>());
    static {
        FILTER_CHARSET_NAME_SET.add("UTF-8");
        FILTER_CHARSET_NAME_SET.add("UTF-16");
        FILTER_CHARSET_NAME_SET.add("UTF-16BE");
        FILTER_CHARSET_NAME_SET.add("UTF-16LE");
        FILTER_CHARSET_NAME_SET.add("UTF-32");
        FILTER_CHARSET_NAME_SET.add("UTF-32BE");
        FILTER_CHARSET_NAME_SET.add("UTF-32LE");
    }

    /**
     * 标记: 是否需要过滤BOM字符
     */
    private boolean needFilter = false;

    /**
     * 创建实例
     * 
     * @param in
     * @param charsetName
     * @throws UnsupportedEncodingException
     */
    public BOMFilterReader(InputStream in, String charsetName)
        throws UnsupportedEncodingException {
        super(in, charsetName);

        // 获取字符集的标准名称
        String enc = Charset.forName(this.getEncoding()).name();
        if (enc != null) {
            this.needFilter = FILTER_CHARSET_NAME_SET.contains(enc
                .toUpperCase());
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStreamReader#read()
     */
    @Override
    public int read() throws IOException {
        if (needFilter) {
            // 只需要过滤首个字符
            needFilter = false;

            int i = super.read();
            if (i != 0xFEFF) {
                return i;
            }
        }

        return super.read();
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStreamReader#read(char[], int, int)
     */
    @Override
    public int read(char[] cbuf, int offset, int length) throws IOException {
        if (length <= 0)
            return 0;

        if (needFilter) {
            // 只需要过滤首个字符
            needFilter = false;

            int i = super.read();
            if (i == -1) { // 没有字符可以读取
                return -1;
            } else if (i != 0xFEFF) { // 首字符不是BOM, 填充到cbuf数组
                cbuf[offset++] = (char) i;
                int readed = super.read(cbuf, offset, (--length));
                if (readed == -1) {
                    return 1;
                } else {
                    return readed + 1;
                }
            }
        }

        return super.read(cbuf, offset, length);
    }

}
