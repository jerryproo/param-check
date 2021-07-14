package com.example.vo;

import lombok.Data;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description
 */
@Data
public class CheckRule {
    /**
     * 校验规则名
     */
    private String ruleName;
    /**
     * 校验规则code
     */
    private String code;

    /**
     * 校验规则提示消息(可用{} 格式化)
     */
    private String msg;

    /**
     * 校验值
     */
    private String value;

    /**
     * 校验规则类型
     */
    private String type;
}
