package com.example.util;

import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.vo.CheckItem;
import com.example.vo.CheckRule;
import com.example.vo.DefaultRule;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.enums.ConstantEnum.CHECK_LIST;
import static com.example.enums.ConstantEnum.DEFAULT_RULE;
import static com.example.enums.ConstantEnum.LEN;
import static com.example.enums.ConstantEnum.RANGE;
import static com.example.enums.ConstantEnum.REGEX;
import static com.example.enums.ConstantEnum.RELATE;
import static com.example.enums.ConstantEnum.REQUIRED;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description 参数校验工具
 */
@Slf4j
public class ParamCheckUtil {
    /**
     * 参数校验
     *
     * @param t             校验对象
     * @param checkRulesStr 参数校验规则
     * @param <T>           校验对象
     * @return 校验结果, 正常时返回空
     */
    public static <T> String check(T t, String checkRulesStr) {
        String checkTarget = String.class.equals(t.getClass()) ? t.toString() : JSON.toJSONString(t);
        return doCheck(JSON.parseObject(checkTarget), JSON.parseObject(checkRulesStr));
    }

    /**
     * 执行 参数校验
     *
     * @param checkTarget 校验对象
     * @param checkRule   校验规则
     * @return 校验结果
     */
    private static String doCheck(JSONObject checkTarget, JSONObject checkRule) {
        // 获取校验对象List
        final List<CheckItem> checkRuleList = getCheckItem(checkRule);
        Console.log(checkRuleList);
        return null;
    }

    /**
     * 获取校验项
     *
     * @param jsonObject 总的校验规则
     * @return 校验项目List
     */
    private static List<CheckItem> getCheckItem(JSONObject jsonObject) {
        // 获取设置的校验规则项
        final List<CheckItem> checkItems =
                JSON.parseArray(jsonObject.getString(CHECK_LIST), CheckItem.class);
        // 将校验规则按照默认规则设置进去
        final JSONObject defaultRule = jsonObject.getJSONObject(DEFAULT_RULE);
        Map<String, DefaultRule> defaultRuleMap = MapUtil.newHashMap(5);
        defaultRuleMap.put(REQUIRED, JSON.parseObject(defaultRule.getString(REQUIRED), DefaultRule.class));
        defaultRuleMap.put(REGEX, JSON.parseObject(defaultRule.getString(REGEX), DefaultRule.class));
        defaultRuleMap.put(LEN, JSON.parseObject(defaultRule.getString(LEN), DefaultRule.class));
        defaultRuleMap.put(RELATE, JSON.parseObject(defaultRule.getString(RELATE), DefaultRule.class));
        defaultRuleMap.put(RANGE, JSON.parseObject(defaultRule.getString(RANGE), DefaultRule.class));

        final List<CheckRule> allDefaultRuleList = getDefaultRuleList(defaultRuleMap);
        // 将所有的添加到一个list中, 设置对应的ruleName
        allDefaultRuleList.forEach(Console::log);

        // 将默认值设置到对应的校验规则中
        checkItems.forEach(item -> {

        });

        return null;
    }

    /**
     * 获取默认校验List
     *
     * @param defaultRuleMap 校验项目Map
     * @return 默认校验规则List
     */
    private static List<CheckRule> getDefaultRuleList(Map<String, DefaultRule> defaultRuleMap) {
        List<CheckRule> defaultRuleList = new ArrayList<>();
        defaultRuleMap.forEach((k, v) -> {
            v.getDefaultItem().setRuleName(k);
            defaultRuleList.addAll(getRuleList(v));
        });
        return defaultRuleList;
    }

    /**
     * 将所有默认的校验项添加到一个中，
     * 并将默认值设置进去
     *
     * @param defaultRule 默认规则项
     * @return 设置好了默认项的默认校验规则的List
     */
    private static Collection<? extends CheckRule> getRuleList(DefaultRule defaultRule) {
        // 默认规则的默认项
        final CheckRule defaultItem = defaultRule.getDefaultItem();
        // 默认项的字段List
        final List<Field> fields = Stream.of(defaultItem.getClass().getDeclaredFields()).collect(Collectors.toList());
        // 校验规则 List
        final List<CheckRule> values = defaultRule.getValues();
        values.forEach(value -> setDefaultItemByFields(defaultItem, fields, value));
        return values;
    }

    /**
     * 按照默认项的字段设置默认校验规则的默认值
     *
     * @param defaultItem 默认项
     * @param fields      字段List
     * @param checkRule   单项的默认校验规则
     */
    private static void setDefaultItemByFields(CheckRule defaultItem, List<Field> fields, CheckRule checkRule) {
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
        if (ObjectUtil.isEmpty(ruleValue)) {
            final Object defaultValue = ReflectUtil.getFieldValue(defaultItem, field);
            ReflectUtil.setFieldValue(checkRule, field, defaultValue);
        }
    }
}
