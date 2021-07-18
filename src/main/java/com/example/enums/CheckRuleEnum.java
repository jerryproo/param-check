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
     * 无
     */
    NONE(CheckRuleNameEnum.NONE, "none"),

    /**
     * 必填
     */
    REQUIRE_TRUE(CheckRuleNameEnum.REQUIRED, "required-true"),


    /**
     * 最小长度
     */
    LEN_MIN(CheckRuleNameEnum.LEN, "len-min"),

    /**
     * 最大长度
     */
    LEN_MAX(CheckRuleNameEnum.LEN, "len-max"),

    /**
     * 正则规则
     */
    REGEX_NAME(CheckRuleNameEnum.REGEX, "regex-name"),

    /**
     * 可选范围
     */
    RANGE(CheckRuleNameEnum.RANGE, "range-1"),


    /**
     * 关联校验
     */
    RELATE_REQUIRE_ALL(CheckRuleNameEnum.RELATE, "relate-require-all"),
    /**
     * 关联必须有一个字段必填
     */
    RELATE_REQUIRE_ONE(CheckRuleNameEnum.RELATE, "relate-require-one"),

    /**
     * 字段不可以重复
     */
    RELATE_UNIQUE(CheckRuleNameEnum.RELATE, "relate-unique"),
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
     * 按照code 匹配取得 enum
     *
     * @param code code
     * @return code对应的enum
     */
    public static CheckRuleEnum getByCode(String code) {
        for (CheckRuleEnum ruleEnum : CheckRuleEnum.values()) {
            // code相同 或者初始值相同
            if (ruleEnum.getCode().equals(code)) {
                return ruleEnum;
            }
        }
        // 可能取到空 , relate 关联考虑放在前面做并从中移除掉
        return NONE;
    }

}
