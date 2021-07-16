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
     * 无规则, 默认项
     */
    NONE(null, "", "无规则"),
    /**
     * 必填
     */
    REQUIRE_TRUE(CheckRuleNameEnum.REQUIRED, "required-true", "必填"),


    /**
     * 最小长度
     */
    LEN_MIN(CheckRuleNameEnum.LEN, "len-min", "最小长度"),

    /**
     * 最大长度
     */
    LEN_MAX(CheckRuleNameEnum.LEN, "len-max", "最大长度"),

    /**
     * 正则规则
     */
    REGEX(CheckRuleNameEnum.REGEX, "reg-name", "正则规则"),

    /**
     * 可选范围
     */
    RANGE(CheckRuleNameEnum.RANGE, "range-1", "范围"),
    ;
    /**
     * ruleName
     */
    private final CheckRuleNameEnum ruleNameEnum;

    /**
     * code
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 按照code 匹配取得 enum
     *
     * @param code code
     * @return code对应的enum
     */
    public static CheckRuleEnum getByCode(String code) {
        for (CheckRuleEnum ruleEnum : CheckRuleEnum.values()) {
            if (ruleEnum.getCode().equals(code)) {
                return ruleEnum;
            }
        }
        return CheckRuleEnum.NONE;
    }

}
