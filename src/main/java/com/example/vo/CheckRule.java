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
     * 规则名
     */
    private String ruleName;

    /**
     * 提示消息模板
     */
    private String msg;

    /**
     * 校验规则code
     */
    private String code;

    /**
     * 校验值
     */
    private String value;
}
