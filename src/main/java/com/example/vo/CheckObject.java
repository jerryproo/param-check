package com.example.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Map;

/**
 * @Author jerrypro
 * @Date 2021/7/19
 * @Description 参数校验的时候的各项对象信息
 */
@Data
public class CheckObject {
    /**
     * 校验规则
     */
    private CheckRule checkRule;

    /**
     * 校验的字段值
     */
    private String fieldValue;

    /**
     * 校验的字段名
     */
    private String fieldName;

    /**
     * 校验的对象(JSONObject格式)
     */
    private JSONObject checkTarget;

    /**
     * 配置的字段信息, k-v field-fieldName 可以依据字段取得其字段名
     */
    private Map<String, String> filedNameMap;
}
