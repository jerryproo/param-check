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
public enum CheckRuleEnum {
    /**
     * 必填
     */
    REQUIRE_TRUE("required", "required-true", "必填"),

    /**
     * 非必填
     */
    REQUIRED_FALSE("required", "required-false", "非必填(可空)"),

    /**
     * 最小长度
     */
    LEN_MIN("len", "len-min", "最小长度"),

    /**
     * 最大长度
     */
    LEN_MAX("len", "len-max", "最大长度"),

    /**
     * 正则规则
     */
    REGEX("regex", "reg-name", "正则规则"),

    /**
     * 可选范围
     */
    RANGE("range", "range-1", "范围"),
    ;
    /**
     * ruleName
     */
    private final String ruleName;

    /**
     * code
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;

}
