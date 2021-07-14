package com.example.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.exception.BizException;
import com.example.exception.ResultCode;

import java.util.Map;
import java.util.Optional;

/**
 * @Author jerrypro
 * @Date 2021/6/15
 * @Description JSON 参数校验用
 */
public class JsonRuleChecker {
    public static final String REGEX = "regex";
    public static final String LEN = "len";
    public static final String RELATE = "relate";
    public static final String RANGE = "range";
    public static final String DEFAULT = "default";
    public static final String VALUES = "values";
    public static final String RULE_NAME = "ruleName";
    public static final String CODE = "code";
    public static final String MSG = "msg";
    public static final String VALUE = "value";
    public static final String TYPE = "type";
    /**
     * 默认值里面项的属性Array
     */
    public static final String[] DEFAULT_RULE_ITEM_ARR = new String[]{RULE_NAME, CODE, MSG, VALUE, TYPE};
    private static final String CHECK_LIST = "checkList";
    private static final String DEFAULT_RULES = "defaultRules";
    private static final String REL_LIST = "relList";
    private static final String FIELD = "field";
    private static final String FIELD_NAME = "fieldName";
    private static final String RULES = "rules";
    private static final String REQUIRED = "required";
    public static final String[] DEFAULT_RULES_ARR = new String[]{REQUIRED, REGEX, LEN, RELATE, RANGE};

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
        return doCheck(JSON.parseObject(checkTarget), JSONObject.parseObject(checkRulesStr));
    }

    /**
     * 对checkList 进行参数校验
     *
     * @param checkTarget 需要参数校验的对象
     * @param checkRules  参数校验规则
     * @return 校验结果
     */
    private static String doCheck(JSONObject checkTarget, JSONObject checkRules) {
        // 获取需要校验的字段及对应的校验规则
        JSONArray checkList = getSoft(checkRules.getJSONArray(CHECK_LIST));
        // 获取关联字段的校验规则
        JSONArray relList = getSoft(checkRules.getJSONArray(REL_LIST));
        // 获取默认校验规则的map
        Map<String, JSONObject> defaultRuleMap = getDefaultRuleMap(checkRules);

        // 校验规则为空, 返回正常
        if (CollUtil.isEmpty(checkList)) {
            return CharSequenceUtil.EMPTY;
        }

        StringBuilder builder = new StringBuilder();
        // 遍历所有需要校验的字段
        for (int i = 0; i < checkList.size(); i++) {
            JSONObject checkItem = checkList.getJSONObject(i);
            // 将默认的规则设置进单个的校验规则中
            setCheckRuleForItem(checkItem, defaultRuleMap);
            // checkTarget 中包含所有的字段
            builder.append(doCheckList(checkTarget, checkItem));
        }
        return builder.toString();
    }

    /**
     * 将checkList中的rule按照defaultRuleMap设置检查的项目
     *
     * @param checkItem      单个参数校验的字段及其对应的校验规则
     * @param defaultRuleMap 默认的校验规则Map
     */
    private static void setCheckRuleForItem(JSONObject checkItem, Map<String, JSONObject> defaultRuleMap) {
        // 获取单个字段的校验规则
        final JSONArray jsonRules = getSoft(checkItem.getJSONArray(RULES));
        // 按照默认的ruleMap 设置对应的rule的值
        for (int i = 0; i < jsonRules.size(); i++) {
            final JSONObject rule = getSoft(jsonRules.getJSONObject(i));
            final JSONObject defaultRule = getSoft(defaultRuleMap.get(rule.getString(CODE)));
            for (String itemCode : DEFAULT_RULE_ITEM_ARR) {
                if (!StrUtil.isEmpty(defaultRule.getString(itemCode)) && StrUtil.isEmpty(rule.getString(itemCode))) {
                    rule.put(itemCode, defaultRule.get(itemCode));
                }
            }
        }
    }

    /**
     * 获取非null jsonArray
     *
     * @param jsonArray jsonArray
     * @return jsonArray
     */
    private static JSONArray getSoft(JSONArray jsonArray) {
        return Optional.ofNullable(jsonArray).orElse(new JSONArray());
    }

    /**
     * 获取非null对象
     * 如果是null则new一个对象, 防止之后使用的时候NPE
     *
     * @param jsonObject jsonObject
     * @return jsonObject
     */
    private static JSONObject getSoft(JSONObject jsonObject) {
        return Optional.ofNullable(jsonObject).orElse(new JSONObject());
    }

    /**
     * 从默认的规则中提取出JSONObject List
     * (将 require,  regex , len , relate, range 对应的List合并为一个)
     *
     * @param checkRules 默认的JSONArray校验规则
     * @return 校验规则
     */
    private static Map<String, JSONObject> getDefaultRuleMap(JSONObject checkRules) {
        // 获取默认的校验规则并按照k-v返回值
        final JSONObject defaultRules = getSoft(checkRules.getJSONObject(DEFAULT_RULES));
        Map<String, JSONObject> ruleMap = MapUtil.newHashMap();
        for (String ruleName : DEFAULT_RULES_ARR) {
            final JSONObject ruleItems = (JSONObject) defaultRules.get(ruleName);
            addRuleItemIntoRuleMap(ruleItems, ruleMap, ruleName);
        }
        return ruleMap;
    }

    /**
     * 将 ruleItems 中的项添加到 ruleList中
     *
     * @param ruleItems ruleItems
     * @param ruleMap   ruleMap
     * @param ruleName  规则名
     */
    private static void addRuleItemIntoRuleMap(JSONObject ruleItems, Map<String, JSONObject> ruleMap,
                                               String ruleName) {
        final JSONObject defaultValue = getSoft(ruleItems.getJSONObject(DEFAULT));
        final JSONArray values = getSoft(ruleItems.getJSONArray(VALUES));
        for (int i = 0; i < values.size(); i++) {
            final JSONObject value = getSoft(values.getJSONObject(i));
            setDefaultValue(value, defaultValue, ruleName);
            ruleMap.put(value.getString(CODE), value);
        }

    }

    /**
     * 如果JSONObject默认value为空 将defaultValue中的值设置进去
     *
     * @param value        value
     * @param defaultValue defaultValue
     * @param ruleName     规则名
     */
    private static void setDefaultValue(JSONObject value, JSONObject defaultValue, String ruleName) {
        for (String field : DEFAULT_RULE_ITEM_ARR) {
            final Object defaultFieldValue = defaultValue.get(field);
            final Object fieldValue = value.get(field);
            if (ObjectUtil.isNotEmpty(defaultFieldValue) && ObjectUtil.isEmpty(fieldValue)) {
                value.put(field, defaultFieldValue);
            }
            value.put(RULE_NAME, ruleName);
        }
    }

    /**
     * 按照checkRules中的规则对单个checkRule进行校验
     *
     * @param checkTarget 校验对象
     * @param checkItem   校验项
     * @return 执行参数校验
     */
    private static String doCheckList(JSONObject checkTarget, JSONObject checkItem) {
        // 需要需要校验规则列表
        JSONArray checkRules = checkItem.getJSONArray(RULES);
        // 获取字段值
        final String fieldValue = checkTarget.getString(checkItem.getString(FIELD));
        if (ObjectUtil.isEmpty(checkRules)) {
            return CharSequenceUtil.EMPTY;
        }
        // 按照校验项进行校验
        for (int i = 0; i < checkRules.size(); i++) {
            final JSONObject jsonItem = checkRules.getJSONObject(i);
            final String codeStr = jsonItem.getString(CODE);
            if (CharSequenceUtil.isEmpty(codeStr)) {
                throw new BizException(ResultCode.PARAM_CHECK_NOT_VALID.getCode(), StrUtil.format(
                        "参数校验配置错误:[{}]没有找到匹配项", CODE));
            }
            // 按照code 从map中获取对应的值
            String ruleName = jsonItem.getString(RULE_NAME);
            Console.log(ruleName);
        }
        return CharSequenceUtil.EMPTY;
    }

}
