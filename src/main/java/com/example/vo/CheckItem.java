package com.example.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description
 */
@Data
public class CheckItem {
    /**
     * 字段
     */
    private String field;

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段校验规则
     */
    private List<CheckRule> rules;
}
