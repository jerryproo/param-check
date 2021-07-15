package com.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author jerrypro
 * @Date 2021/7/15
 * @Description
 */
@Getter
@AllArgsConstructor
public enum CheckRuleNameEnum {
    /**
     * 必填
     */
    REQUIRED("required", "必填"),

    /**
     * 长度
     */
    LEN("len", "长度"),

    /**
     * 正则
     */
    REGEX("regex", "正则"),

    /**
     * 关联
     */
    RELATE("relate", "关联"),

    /**
     * 范围
     */
    RANGE("range", "范围"),
    ;

    /**
     * code
     */
    private final String code;

    /**
     * name
     */
    private final String name;
}
