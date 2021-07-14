package com.example.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description
 */
@Data
public class DefaultRule {
    /**
     * 规则名
     */
    private String ruleName;
    /**
     * 默认配置项
     */
    private DefaultItem defaultItem;

    /**
     * 默认参数校验的规则List
     */
    private List<CheckRule> values;
}
