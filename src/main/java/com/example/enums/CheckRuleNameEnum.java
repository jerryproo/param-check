package com.example.enums;

import cn.hutool.core.util.StrUtil;
import com.example.vo.CheckRule;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author jerrypro
 * @Date 2021/7/15
 * @Description
 */
@Getter
@AllArgsConstructor
public enum CheckRuleNameEnum {
    /**
     * 无
     */
    NONE("none"),
    /**
     * 必填
     */
    REQUIRED("required"),

    /**
     * 长度
     */
    LEN("len"),

    /**
     * 正则
     */
    REGEX("regex"),

    /**
     * 关联
     */
    RELATE("relate"),

    /**
     * 范围
     */
    RANGE("range"),
    ;

    /**
     * code
     */
    private final String ruleName;


    /**
     * 按照规则名过滤
     *
     * @param checkRules 所有的校验规则
     * @param nameEnum   规则名Enum
     * @return 对应规则名的List
     */
    public static List<CheckRule> filterByRuleName(List<CheckRule> checkRules, CheckRuleNameEnum nameEnum) {
        return checkRules.stream().filter(item -> nameEnum.getRuleName().equals(item.getRuleName())).collect(Collectors.toList());
    }

    /**
     * 按照code取得类型
     *
     * @param code code
     * @return 校验规则 nameEnum
     */
    public static CheckRuleNameEnum getByCode(String code) {
        for (CheckRuleNameEnum nameEnum : CheckRuleNameEnum.values()) {
            if (StrUtil.startWith(code, nameEnum.getRuleName())) {
                return nameEnum;
            }
        }
        return NONE;
    }

}
