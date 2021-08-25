package com.example.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.enums.CheckRuleEnum;
import com.example.vo.CheckItem;
import com.example.vo.CheckRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.enums.ConstantEnum.RELATE_LIST;
import static com.example.util.ParamCheckCommonUtil.getFieldEmptyList;
import static com.example.util.ParamCheckCommonUtil.getMessage;
import static com.example.util.ParamCheckCommonUtil.setDefaultRuleForRules;
import static com.example.util.ParamCheckCommonUtil.throwBizException;

/**
 * @Author jerrypro
 * @Date 2021/8/16
 * @Description
 */
public class ParamCheckRelateUtil {

    public static String checkRelate(JSONObject checkRuleJson, JSONObject checkTargetJson) {
        StringBuilder builder = new StringBuilder();
        List<CheckRule> defaultCheckRuleList = ParamCheckCommonUtil.getDefaultCheckRuleList();
        final List<CheckItem> checkItemList =
                ParamCheckCommonUtil.getCheckRuleList(checkRuleJson, defaultCheckRuleList);

        final List<CheckRule> relateList = getRelateList(checkRuleJson, defaultCheckRuleList);
        for (CheckRule checkRule : relateList) {
            builder.append(doCheckRelate(checkRule, null, null, checkTargetJson, checkItemList));
        }
        return builder.toString();
    }

    /**
     * 获取关联校验的List
     *
     * @param checkRuleJson   参数校验配置的Json对象
     * @param defaultRuleList 默认校验规则
     * @return 关联校验的规则List
     */
    private static List<CheckRule> getRelateList(JSONObject checkRuleJson, List<CheckRule> defaultRuleList) {
        final Map<String, CheckRule> defaultRuleMap =
                defaultRuleList.stream().collect(Collectors.toMap(CheckRule::getCode, o -> o));
        final List<CheckRule> relateList = JSON.parseArray(checkRuleJson.getString(RELATE_LIST), CheckRule.class);
        setDefaultRuleForRules(defaultRuleMap, relateList);
        return relateList;
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
    public static String doCheckRelate(CheckRule checkRule, String fieldValue, String fieldName,
                                       JSONObject checkTarget, List<CheckItem> checkItemList) {
        String value = checkRule.getValue();
        String code = checkRule.getCode();
        CheckRuleEnum ruleEnum = CheckRuleEnum.getByCode(code);
        if (!JSONUtil.isJsonArray(value)) {
            value = StrUtil.BRACKET_START + value + StrUtil.BRACKET_END;
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
                    return getMessage(checkRule, String.join(StrUtil.COMMA, allFieldNameList));
                case RELATE_REQUIRE_ONE:
                    // 至少有一个不为空时, 判断list长度, 如果和字段数一样多说明全为空, 则报错
                    if (list.size() == fields.size()) {
                        return getMessage(checkRule, String.join(StrUtil.COMMA, list));
                    }
                    break;
                case RELATE_UNIQUE:
                    if (list.size() > 0) {
                        return getMessage(checkRule, String.join(StrUtil.COMMA, allFieldNameList));
                    }
                    break;
                default:
            }
        }
        return CharSequenceUtil.EMPTY;
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
}
