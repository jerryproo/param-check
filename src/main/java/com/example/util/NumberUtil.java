package com.example.util;

/**
 * @author 王正伟
 * 创建时间：2021/7/16
 */
public class NumberUtil {
    /**
     * 字符串转换为int , 无法转换时返回null
     *
     * @param value 待转换的值
     * @return 转换后的 intValue 无法转换时 返回 null
     */
    public static Integer parseToInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
