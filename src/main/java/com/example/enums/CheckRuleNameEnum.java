package com.example.enums;

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
    private final String ruleName;

    /**
     * name
     */
    private final String desc;

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
}
