package com.example.util;

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
import java.util.Optional;
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
        final List<CheckItem> checkRuleList = getCheckItems(checkRule);
//        checkRuleList.forEach(item -> {
//            item.getRules().forEach(Console::log);
//        });
        return null;
    }

    /**
     * 获取校验项
     *
     * @param jsonObject 总的校验规则
     * @return 校验项目List
     */
    private static List<CheckItem> getCheckItems(JSONObject jsonObject) {
        // 获取设置的校验规则项
        final List<CheckItem> checkItems =
                JSON.parseArray(jsonObject.getString(CHECK_LIST), CheckItem.class);
        // 将校验规则按照默认规则设置进去
        final JSONObject defaultRule = jsonObject.getJSONObject(DEFAULT_RULE);
        Map<String, DefaultRule> defaultRuleMap = getDefaultRuleMap(defaultRule);
        // 将所有的添加到一个list中, 设置对应的ruleName
        final List<CheckRule> allDefaultRuleList = getDefaultRuleList(defaultRuleMap);
        setDefaultRuleIntoCheckItems(allDefaultRuleList, checkItems);
        return checkItems;
    }

    /**
     * 将默认值设置到校验规则中
     *
     * @param allDefaultRuleList 默认校验规则List
     * @param checkItems         本次的校验规则List
     */
    private static void setDefaultRuleIntoCheckItems(List<CheckRule> allDefaultRuleList, List<CheckItem> checkItems) {
        final Map<String, CheckRule> defaultRuleMap =
                allDefaultRuleList.stream().collect(Collectors.toMap(CheckRule::getCode, item -> item));
        checkItems.forEach(item -> setDefaultRuleForRules(defaultRuleMap, item.getRules()));
    }

    /**
     * 设置校验字段的规则
     *
     * @param defaultRuleMap 默认的校验规则List
     * @param rules          单项的校验规则List
     */
    private static void setDefaultRuleForRules(Map<String, CheckRule> defaultRuleMap, List<CheckRule> rules) {
        rules.forEach(rule -> {
            final CheckRule defaultRule =
                    Optional.ofNullable(defaultRuleMap.get(rule.getCode())).orElse(new CheckRule());
            setDefaultValueIfEmpty(defaultRule, rule);
        });
    }

    /**
     * 从JSON配置中提取出校验规则map
     *
     * @param defaultRule 校验规则JSONObject
     * @return 校验规则Map k-v  规则ruleName - 规则详情
     */
    private static Map<String, DefaultRule> getDefaultRuleMap(JSONObject defaultRule) {
        Map<String, DefaultRule> defaultRuleMap = MapUtil.newHashMap(5);
        defaultRuleMap.put(REQUIRED, JSON.parseObject(defaultRule.getString(REQUIRED), DefaultRule.class));
        defaultRuleMap.put(REGEX, JSON.parseObject(defaultRule.getString(REGEX), DefaultRule.class));
        defaultRuleMap.put(LEN, JSON.parseObject(defaultRule.getString(LEN), DefaultRule.class));
        defaultRuleMap.put(RELATE, JSON.parseObject(defaultRule.getString(RELATE), DefaultRule.class));
        defaultRuleMap.put(RANGE, JSON.parseObject(defaultRule.getString(RANGE), DefaultRule.class));
        return defaultRuleMap;
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
        // 校验规则 List
        final List<CheckRule> values = defaultRule.getValues();
        values.forEach(value -> setDefaultValueIfEmpty(defaultItem, value));
        return values;
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
