package com.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description
 */
@AllArgsConstructor
@Getter
public enum LogLevel {

    /**
     * info
     */
    INFO(1, "INFO"),

    /**
     * warn
     */
    WARN(2, "WARN"),

    /**
     * error
     */
    ERROR(3, "ERROR");


    /**
     * 错误日志 code
     */
    private final Integer code;

    /**
     * 错误日志 name
     */
    private final String name;


}

