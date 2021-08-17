package com.example.util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @Author jerrypro
 * @Date 2021/8/16
 * @Description
 */
public class FileUtil {
    /**
     * 以UTF8格式读取指定文件内容
     *
     * @param fileName 文件名
     * @return 文件内容
     */
    public static String readFile(String fileName) {
        return readFile(fileName, StandardCharsets.UTF_8);
    }

    /**
     * 按照指定字符集读取指定文件内容
     *
     * @param fileName 文件名
     * @param charSet  字符集
     * @return 文件内容
     */
    public static String readFile(String fileName, Charset charSet) {
        final File file =
                new File(Objects.requireNonNull(FileUtil.class.getClassLoader().getResource(fileName)).getFile());
        return new String(cn.hutool.core.io.FileUtil.readBytes(file), charSet);
    }
}
