package com.rpc.utils;

/**
 * string工具类
 */
public class StringUtil {

    /**
     * 判断字符串是否有空格
     * @param s
     * @return
     */
    public static boolean isBlank(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
