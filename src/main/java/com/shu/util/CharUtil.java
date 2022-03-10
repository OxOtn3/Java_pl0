package com.shu.util;

/**
 * @author oxotn3
 * @create 2022-03-10
 * @description 字符工具类
 */
public class CharUtil {
    public static boolean isSpace(char c) {
        return Character.isWhitespace(c);
    }

    public static boolean isLetter(char c) {
        return Character.isLetter(c);
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static char toLowerCase(char c) {
        return Character.toLowerCase(c);
    }
}
