package com.example.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description
 */
@Data
public class RelItem {
    /**
     * 类型名
     */
    private String type;

    /**
     * 校验字段
     */
    private List<String> fields;
}
