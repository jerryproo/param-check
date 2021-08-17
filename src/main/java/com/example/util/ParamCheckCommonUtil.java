package com.example.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.enums.LogLevel;
import com.example.enums.ResultCode;
import com.example.exception.BizException;
import com.example.vo.CheckRule;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author jerrypro
 * @Date 2021/8/16
 * @Description
 */
public class ParamCheckCommonUtil {
    /**
     * 异常抛出用方法
     *
     * @param template 消息模板
     * @param params   消息参数
     */
    public static void throwBizException(CharSequence template, Object... params) {
        throw new BizException(ResultCode.PARAM_CHECK_NOT_VALID.getCode(), StrUtil.format(template, params),
                LogLevel.ERROR);
    }

    /**
     * 获取校验对象指定字段(field)是否为空, 为空则添加到List并返回
     *
     * @param checkTarget 校验对象
     * @param field       字段
     * @param fieldName   与field对应的字段名
     * @return 校验的字段是否为空
     */
    public static List<String> getFieldEmptyList(JSONObject checkTarget, String field, String fieldName) {
        // 字段值为空的List
        List<String> list = new ArrayList<>();
        String fieldValue = checkTarget.getString(field);
        if (CharSequenceUtil.isEmpty(fieldValue)) {
            list.add(fieldName);
        }
        return list;
    }

    /**
     * 校验字段值是否有重复
     * 获取校验对象的当前字段(currField)是否与fieldValue相同, 相同则将fieldName添加到List返回
     *
     * @param checkTarget 校验对象
     * @param fieldValue  字段的值
     * @param fieldName   字段名
     * @param currField   当前字段
     * @return 重复字段名List
     */
    public static List<String> getFieldDuplicateList(JSONObject checkTarget, String fieldValue, String fieldName,
                                                     String currField) {
        List<String> list = new ArrayList<>();
        // 字段值为空无需校验
        if (CharSequenceUtil.isEmpty(fieldValue)) {
            return list;
        }
        String currFieldValue = checkTarget.getString(currField);
        if (StrUtil.equals(fieldValue, currFieldValue)) {
            list.add(fieldName);
        }
        return list;
    }

    /**
     * 拼接消息内容
     *
     * @param checkRule 校验规则
     * @param params    传入参数
     * @return 格式化好的消息内容
     */
    public static String getMessage(CheckRule checkRule, Object... params) {
        return StrUtil.format(checkRule.getMsg(), params) + CommonConstant.Separator.SEMICOLON;
    }

    /**
     * 设置校验字段的规则
     *
     * @param defaultRuleMap 默认的校验规则List
     * @param rules          单项的校验规则List
     */
    public static void setDefaultRuleForRules(Map<String, CheckRule> defaultRuleMap, List<CheckRule> rules) {
        if (CollUtil.isEmpty(rules)) {
            return;
        }
        rules.forEach(rule -> {
            final CheckRule defaultRule =
                    Optional.ofNullable(defaultRuleMap.get(rule.getCode())).orElse(new CheckRule());
            setDefaultValueIfEmpty(defaultRule, rule);
        });
    }

    /**
     * 如果为空, 将默认值设置到对应项
     *
     * @param defaultItem 默认项
     * @param checkRule   单项的默认校验规则
     */
    private static void setDefaultValueIfEmpty(CheckRule defaultItem, CheckRule checkRule) {
        // 默认项的字段List
        final List<Field> fields = Stream.of(defaultItem.getClass().getDeclaredFields()).collect(Collectors.toList());
        fields.forEach(field -> setDefaultItemForField(defaultItem, checkRule, field));
    }

    /**
     * 设置单项的默认校验规则
     *
     * @param defaultItem 默认项
     * @param checkRule   校验规则
     * @param field       字段信息
     */
    private static void setDefaultItemForField(CheckRule defaultItem, CheckRule checkRule, Field field) {
        final Object ruleValue = ReflectUtil.getFieldValue(checkRule, field);
        // 如果校验规则对应字段为空, 将默认值设置进去
        if (ObjectUtil.isEmpty(ruleValue)) {
            final Object defaultValue = ReflectUtil.getFieldValue(defaultItem, field);
            ReflectUtil.setFieldValue(checkRule, field, defaultValue);
        }
    }
}
