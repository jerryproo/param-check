package com.example.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.enums.CheckModeEnum;
import com.example.enums.CheckRuleEnum;
import com.example.enums.CheckRuleNameEnum;
import com.example.enums.LogLevel;
import com.example.enums.ResultCode;
import com.example.exception.BizException;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.enums.ConstantEnum.CHECK_LIST;
import static com.example.enums.ConstantEnum.CHECK_MODE;
import static com.example.enums.ConstantEnum.DEFAULT_RULE;
import static com.example.enums.ConstantEnum.RELATE_LIST;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description 参数校验工具
 */
@Slf4j
public class ParamCheckUtil {
    private static final String COMMA = ";";

    /**
     * 参数校验
     *
     * @param t             校验对象
     * @param checkRulesStr 参数校验规则
     * @param <T>           校验对象类型
     * @return 校验结果, 正常时返回空
     */
    public static <T> String check(T t, String checkRulesStr) {
        String checkTarget = String.class.equals(t.getClass()) ? t.toString() : JSON.toJSONString(t);
        return doCheck(JSON.parseObject(checkTarget), JSON.parseObject(checkRulesStr));
    }

    /**
     * 执行 参数校验
     *
     * @param checkTarget   校验对象
     * @param checkRuleJson 校验规则
     * @return 校验结果
     */
    private static String doCheck(JSONObject checkTarget, JSONObject checkRuleJson) {
        // 获取校验对象List
        final List<CheckItem> checkItemList = getCheckRuleList(checkRuleJson);
        final CheckModeEnum checkMode = getCheckMode(checkRuleJson);
        // 按照ruleName 分别调用各种处理方式
        StringBuilder builder = new StringBuilder();
        // 关联字段校验
        final List<CheckRule> relateList = getRelateList(checkRuleJson);
        for (CheckRule checkRule : relateList) {
            builder.append(doCheckRelate(checkRule, null, null, checkTarget, checkItemList));
        }
        // checkList 校验
        for (CheckItem item : checkItemList) {
            final String checkRes = doCheckByRuleName(item, checkTarget, checkItemList);
            // 根据校验模式, 如果是全返回 则添加检查的结果
            // 如果是单个返回 则直接返回检查结果
            if (CharSequenceUtil.isNotEmpty(checkRes)) {
                switch (checkMode) {
                    case ALL:
                        builder.append(checkRes);
                        break;
                    case ONE:
                        return checkRes;
                    default:
                }
            }
        }

        return builder.toString();
    }

    /**
     * 获取关联校验的List
     *
     * @param checkRuleJson 参数校验配置的json对象
     * @return 关联校验的规则List
     */
    private static List<CheckRule> getRelateList(JSONObject checkRuleJson) {
        final List<CheckRule> defaultRuleList = getDefaultRuleList(checkRuleJson);
        final Map<String, CheckRule> defaultRuleMap =
                defaultRuleList.stream().collect(Collectors.toMap(CheckRule::getCode, o -> o));
        final List<CheckRule> relateList = JSON.parseArray(checkRuleJson.getString(RELATE_LIST), CheckRule.class);
        setDefaultRuleForRules(defaultRuleMap, relateList);
        return relateList;
    }

    /**
     * 获取检测模式 (统一返回/单个返回)
     *
     * @param checkRule 校验规则jsonObject
     * @return checkMode
     */
    private static CheckModeEnum getCheckMode(JSONObject checkRule) {
        final CheckModeEnum modeEnum = CheckModeEnum.getByCode(checkRule.getString(CHECK_MODE));
        if (ObjectUtil.isEmpty(modeEnum)) {
            throwBizException("校验模式配置错误, 未找到匹配的校验模式");
        }
        return modeEnum;
    }


    /**
     * 按照配置的校验项对校验目标进行校验
     *
     * @param checkItem     单个字段的校验规则
     * @param checkTarget   参数校验的对象
     * @param checkItemList 配置的参数校验的字段的List
     * @return 校验结果
     */
    private static String doCheckByRuleName(CheckItem checkItem, JSONObject checkTarget,
                                            List<CheckItem> checkItemList) {
        final String field = checkItem.getField();
        final String fieldName = checkItem.getFieldName();
        final List<CheckRule> checkRules = Optional.ofNullable(checkItem.getRules()).orElse(new ArrayList<>());
        // 获取字段值, 先用String表示
        final String fieldValue = checkTarget.getString(field);
        // 先进行required校验 确认是否非空, 非空则不要再进行之后的校验了
        final String requireMsg = checkRequire(checkRules, fieldValue, fieldName);
        if (CharSequenceUtil.isNotEmpty(requireMsg)) {
            return requireMsg;
        }
        return checkExtraRule(checkRules, fieldValue, fieldName, checkTarget, checkItemList);
    }

    /**
     * 除必填项外的其他校验规则
     *
     * @param checkRules    校验规则List
     * @param fieldValue    字段值
     * @param fieldName     字段名
     * @param checkTarget   校验对象
     * @param checkItemList 校验字段List
     */
    private static String checkExtraRule(List<CheckRule> checkRules, String fieldValue, String fieldName,
                                         JSONObject checkTarget, List<CheckItem> checkItemList) {
        StringBuilder builder = new StringBuilder();
        // 遍历校验规则List, 获取校验信息
        for (CheckRule checkRule : checkRules) {
            CheckRuleNameEnum nameEnum = CheckRuleNameEnum.getByCode(checkRule.getCode());
            // 按照code分别进行各项校验
            // todo 使用适配器模式处理
            switch (nameEnum) {
                case LEN:
                    builder.append(doCheckLen(checkRule, fieldValue, fieldName));
                    break;
                case REGEX:
                    builder.append(doCheckRegex(checkRule, fieldValue, fieldName));
                    break;
                case RANGE:
                    builder.append(doCheckRange(checkRule, fieldValue, fieldName));
                    break;
                case RELATE:
                    builder.append(doCheckRelate(checkRule, fieldValue, fieldName, checkTarget, checkItemList));
                    break;
                default:
            }
        }
        return builder.toString();
    }

    /**
     * 关联校验
     *
     * @param checkRule     校验规则
     * @param fieldValue    字段值
     * @param fieldName     字段名
     * @param checkTarget   校验的对象(包含所有字段)
     * @param checkItemList 配置的所有的校验项
     * @return 关联校验结果, 校验通过则返回空值
     */
    private static String doCheckRelate(CheckRule checkRule, String fieldValue, String fieldName,
                                        JSONObject checkTarget, List<CheckItem> checkItemList) {
        String value = checkRule.getValue();
        String code = checkRule.getCode();
        CheckRuleEnum ruleEnum = CheckRuleEnum.getByCode(code);
        if (!JSONUtil.isJsonArray(value)) {
            value = "[" + value + "]";
        }
        JSONArray fields = JSONArray.parseArray(value);
        Map<String, CheckItem> itemMap = checkItemList.stream().collect(Collectors.toMap(CheckItem::getField, o -> o));
        List<String> list = new ArrayList<>();
        List<String> allFieldNameList = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            String currField = fields.getString(i);
            CheckItem checkItem = itemMap.get(currField);
            if (ObjectUtil.isEmpty(checkItem) || CharSequenceUtil.isEmpty(checkItem.getFieldName())) {
                throwBizException("关联校验失败,为获取到关联的字段信息,code:{},字段信息:{}", code, currField);
            }
            String currFieldName = checkItem.getFieldName();
            allFieldNameList.add(currFieldName);
            switch (ruleEnum) {
                case RELATE_REQUIRE_ALL:
                case RELATE_REQUIRE_ONE:
                    list.addAll(getFieldEmptyList(checkTarget, currField, currFieldName));
                    break;
                case RELATE_UNIQUE:
                    list.addAll(getFieldDuplicateList(checkTarget, fieldValue, currFieldName, currField));
                    fieldValue = checkTarget.getString(currField);
                    break;
                default:
                    throwBizException("字段关联校验配置错误, 未找到合适的规则, code:{}", code);
            }
        }
        if (CollUtil.isNotEmpty(list)) {
            if (CharSequenceUtil.isNotEmpty(fieldName)) {
                allFieldNameList.add(fieldName);
            }
            switch (ruleEnum) {
                case RELATE_REQUIRE_ALL:
                    return getMessage(checkRule, String.join(",", allFieldNameList));
                case RELATE_REQUIRE_ONE:
                    // 至少有一个不为空时, 判断list长度, 如果和字段数一样多说明全为空, 则报错
                    if (list.size() == fields.size()) {
                        return getMessage(checkRule, String.join(",", list));
                    }
                    break;
                case RELATE_UNIQUE:
                    if (list.size() > 0) {
                        return getMessage(checkRule, String.join(",", allFieldNameList));
                    }
                    break;
                default:
            }
        }
        return CharSequenceUtil.EMPTY;
    }

    /**
     * 校验字段值是否有重复
     *
     * @param checkTarget 校验对象
     * @param fieldValue  字段值
     * @param fieldName   字段名
     * @param currField   单签字段
     * @return 重复字段名List
     */
    private static List<String> getFieldDuplicateList(JSONObject checkTarget, String fieldValue, String fieldName,
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
     * 获取字段为空的名称List
     *
     * @param checkTarget 校验对象
     * @param field       字段
     * @param fieldName   字段名
     * @return 校验的字段是否为空
     */
    private static List<String> getFieldEmptyList(JSONObject checkTarget, String field, String fieldName) {
        // 字段值为空的List
        List<String> list = new ArrayList<>();
        String fieldValue = checkTarget.getString(field);
        if (CharSequenceUtil.isEmpty(fieldValue)) {
            list.add(fieldName);
        }
        return list;
    }

    /**
     * 参数范围校验
     *
     * @param checkRule  校验规则
     * @param fieldValue 校验字段
     * @param fieldName  字段名
     * @return 范围校验结果
     */
    private static String doCheckRange(CheckRule checkRule, String fieldValue, String fieldName) {
        String rangeStr = checkRule.getValue();
        // 范围为空 报错
        if (CharSequenceUtil.isEmpty(rangeStr)) {
            throwBizException("范围校验参数配置错误,code:{}, value:{}", checkRule.getCode(), checkRule.getValue());
        }
        // 为数组时校验是否满足数组设定
        if (JSONUtil.isJsonArray(rangeStr)) {
            return doCheckRangeArray(checkRule, fieldValue, fieldName);
        } else if (!StrUtil.equals(rangeStr, fieldValue)) {
            // 不为数组, 校验是否为当前值
            return getMessage(checkRule, fieldName, rangeStr);
        }
        return CharSequenceUtil.EMPTY;
    }

    /**
     * 范围校验
     *
     * @param checkRule  范围数组
     * @param fieldValue 字段值
     * @param fieldName  字段名
     * @return 参数范围校验
     */
    private static String doCheckRangeArray(CheckRule checkRule, String fieldValue, String fieldName) {
        JSONArray values = JSONArray.parseArray(checkRule.getValue());
        for (int i = 0; i < values.size(); i++) {
            if (StrUtil.equals(values.getString(i), fieldValue)) {
                return CharSequenceUtil.EMPTY;
            }
        }
        return getMessage(checkRule, fieldName, checkRule.getValue());
    }

    /**
     * 正则表达时间校验
     *
     * @param checkRule  校验规则
     * @param fieldValue 字段值
     * @param fieldName  字段名
     * @return 正则校验结果, 为空表示正常
     */
    private static String doCheckRegex(CheckRule checkRule, String fieldValue, String fieldName) {
        boolean matches = Pattern.matches(checkRule.getValue(), fieldValue);
        if (!matches) {
            return getMessage(checkRule, fieldName);
        }
        return CharSequenceUtil.EMPTY;
    }

    /**
     * 参数长度校验
     *
     * @param checkRule  校验规则
     * @param fieldValue 字段值
     * @param fieldName  字段名称
     * @return 字段长度校验 为空鼻癌哦是正常
     */
    private static String doCheckLen(CheckRule checkRule, String fieldValue, String fieldName) {
        CheckRuleEnum ruleEnum = CheckRuleEnum.getByCode(checkRule.getCode());
        // 长度限定的边界值
        Integer bound = NumberUtil.parseToInt(checkRule.getValue());
        if (bound == null) {
            throwBizException("{}校验值配置错误:{}, 转换为数字失败", checkRule.getCode(), checkRule.getValue());
        }
        int fieldLen = StrUtil.length(fieldValue);
        switch (ruleEnum) {
            case LEN_MIN:
                if (fieldLen < bound) {
                    return getMessage(checkRule, fieldName, bound);
                }
                break;
            case LEN_MAX:
                if (fieldLen > bound) {
                    return getMessage(checkRule, fieldName, bound);
                }
                break;
            default:
        }
        return CharSequenceUtil.EMPTY;
    }

    /**
     * 拼接消息内容
     *
     * @param checkRule 校验规则
     * @param params    传入参数
     * @return 格式化好的消息内容
     */
    private static String getMessage(CheckRule checkRule, Object... params) {
        return StrUtil.format(checkRule.getMsg(), params) + COMMA;
    }

    /**
     * 判断该字段是否必填
     *
     * @param checkRules 字段的所有校验规则
     * @param fieldValue 字段值
     * @param fieldName  字段名
     */
    private static String checkRequire(List<CheckRule> checkRules, String fieldValue, String fieldName) {
        final List<CheckRule> requiredList = CheckRuleNameEnum
                .filterByRuleName(checkRules, CheckRuleNameEnum.REQUIRED);
        // 将必填校验项移除, 之后不再处理
        checkRules.removeAll(requiredList);
        if (requiredList.size() > 1) {
            throwBizException("{}必填规则设置错误, 只可以设置一个必填规则", fieldName);
        } else if (requiredList.size() < 1) {
            return CharSequenceUtil.EMPTY;
        }
        final CheckRule checkRule = requiredList.get(0);
        boolean required = CheckRuleEnum.REQUIRE_TRUE.getCode().equals(checkRule.getCode());
        if (required && CharSequenceUtil.isEmpty(fieldValue)) {
            return getMessage(checkRule, fieldName);
        }
        return CharSequenceUtil.EMPTY;
    }

    /**
     * 获取校验项
     *
     * @param checkRuleJson 总的校验规则
     * @return 校验项目List
     */
    private static List<CheckItem> getCheckRuleList(JSONObject checkRuleJson) {
        // 获取设置的校验规则项
        final List<CheckItem> checkItems =
                JSON.parseArray(checkRuleJson.getString(CHECK_LIST), CheckItem.class);
        final List<CheckRule> allDefaultRuleList = getDefaultRuleList(checkRuleJson);
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
     * 从JSON配置中提取出校验规则map
     *
     * @param defaultRuleObj 校验规则JSONObject
     * @return 校验规则Map k-v  规则ruleName - 规则详情
     */
    private static Map<String, DefaultRule> getDefaultRuleMap(JSONObject defaultRuleObj) {
        Map<String, DefaultRule> defaultRuleMap = MapUtil.newHashMap(5);
        for (CheckRuleNameEnum nameEnum : CheckRuleNameEnum.values()) {
            String ruleName = nameEnum.getRuleName();
            String rule = defaultRuleObj.getString(ruleName);
            DefaultRule defaultRule = JSON.parseObject(rule, DefaultRule.class);
            if (ObjectUtil.isNotEmpty(defaultRule)) {
                defaultRuleMap.put(ruleName, defaultRule);
            }
        }
        return defaultRuleMap;
    }

    /**
     * 获取默认校验List
     *
     * @param checkRuleJson 读取的JSON配置对象
     * @return 默认校验规则List
     */
    private static List<CheckRule> getDefaultRuleList(JSONObject checkRuleJson) {
        final JSONObject defaultRule = checkRuleJson.getJSONObject(DEFAULT_RULE);
        Map<String, DefaultRule> defaultRuleMap = getDefaultRuleMap(defaultRule);
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

    /**
     * 异常抛出用方法
     *
     * @param template 消息模板
     * @param params   消息参数
     */
    private static void throwBizException(CharSequence template, Object... params) {
        throw new BizException(ResultCode.PARAM_CHECK_NOT_VALID.getCode(), StrUtil.format(template, params),
                LogLevel.ERROR);
    }
}
