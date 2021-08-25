package com.example.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.enums.CheckRuleNameEnum;
import com.example.enums.ConstantEnum;
import com.example.enums.LogLevel;
import com.example.enums.ResultCode;
import com.example.exception.BizException;
import com.example.vo.CheckItem;
import com.example.vo.CheckRule;
import com.example.vo.DefaultRule;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.enums.ConstantEnum.CHECK_LIST;

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

    /**
     * 获取默认校验List
     *
     * @return 默认校验规则List
     */
    public static List<CheckRule> getDefaultCheckRuleList() {
        List<DefaultRule> defaultRuleList = getDefaultRuleList();
        List<CheckRule> list = new ArrayList<>();
        defaultRuleList.forEach(v -> list.addAll(getCheckRuleList(v)));
        return list;
    }

    /**
     * 从JSON配置中提取出默认的校验规则map
     *
     * @return 校验规则List 规则详情
     */
    private static List<DefaultRule> getDefaultRuleList() {
        String defaultRuleFileName = "default_rule.json";
        // 暂时从文件读取, 之后可以从数据库等地方获取
        final String defaultCheckRuleStr = FileUtil.readFile(defaultRuleFileName);
        final JSONObject defaultRuleObj = JSON.parseObject(defaultCheckRuleStr).getJSONObject(ConstantEnum.RULES);
        List<DefaultRule> defaultRuleList = new ArrayList<>();
        for (CheckRuleNameEnum nameEnum : CheckRuleNameEnum.values()) {
            String ruleName = nameEnum.getRuleName();
            String rule = defaultRuleObj.getString(ruleName);
            DefaultRule defaultRule = JSON.parseObject(rule, DefaultRule.class);
            if (ObjectUtil.isNotEmpty(defaultRule)) {
                defaultRule.setRuleName(ruleName);
                defaultRuleList.add(defaultRule);
            }
        }
        return defaultRuleList;
    }

    /**
     * 将所有默认的校验项添加到一个中，
     * 并将默认值设置进去
     *
     * @param defaultRule 默认规则项
     * @return 设置好了默认项的默认校验规则的List
     */
    public static Collection<? extends CheckRule> getCheckRuleList(DefaultRule defaultRule) {
        // 默认规则的默认项
        final CheckRule defaultItem = defaultRule.getDefaultItem();
        // 校验规则 List
        final List<CheckRule> values = defaultRule.getValues();
        values.forEach(value -> setDefaultValueIfEmpty(defaultItem, value));
        return values;
    }

    /**
     * 获取校验项
     *
     * @param checkRuleJson   总的校验规则
     * @param defaultRuleList 默认校验规则
     * @return 校验项目List
     */
    public static List<CheckItem> getCheckRuleList(JSONObject checkRuleJson, List<CheckRule> defaultRuleList) {
        // 获取设置的校验规则项
        final List<CheckItem> checkItems =
                JSON.parseArray(checkRuleJson.getString(CHECK_LIST), CheckItem.class);
        setDefaultRuleIntoCheckItems(defaultRuleList, checkItems);
        return checkItems;
    }

    /**
     * 将默认值设置到校验规则中
     *
     * @param allDefaultRuleList 默认校验规则List
     * @param checkItems         本次的校验规则List
     */
    public static void setDefaultRuleIntoCheckItems(List<CheckRule> allDefaultRuleList, List<CheckItem> checkItems) {
        final Map<String, CheckRule> defaultRuleMap =
                allDefaultRuleList.stream().collect(Collectors.toMap(CheckRule::getCode, item -> item));
        checkItems.forEach(item -> setDefaultRuleForRules(defaultRuleMap, item.getRules()));
    }
}
