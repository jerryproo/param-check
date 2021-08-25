package com.example.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.enums.CheckModeEnum;
import com.example.enums.CheckRuleEnum;
import com.example.enums.CheckRuleNameEnum;
import com.example.vo.CheckItem;
import com.example.vo.CheckRule;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.example.enums.ConstantEnum.CHECK_MODE;
import static com.example.util.ParamCheckCommonUtil.getMessage;

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
     * @param <T>           校验对象类型
     * @return 校验结果, 正常时返回空
     */
    public static <T> String check(T t, String checkRulesStr) {
        String checkTarget = String.class.equals(t.getClass()) ? t.toString() : JSON.toJSONString(t);
        final JSONObject checkRuleJson = JSON.parseObject(checkRulesStr);
        final JSONObject checkTargetJson = JSON.parseObject(checkTarget);
        // 获取校验对象List
        return doCheck(checkTargetJson, checkRuleJson);
    }

    /**
     * 执行 参数校验
     *
     * @param checkTargetJson 校验对象
     * @param checkRuleJson   校验规则
     * @return 校验结果, 正常时返回空
     */
    private static String doCheck(JSONObject checkTargetJson, JSONObject checkRuleJson) {
        return ParamCheckRelateUtil.checkRelate(checkRuleJson, checkTargetJson) +
                doCheckCheckList(checkRuleJson, checkTargetJson);
    }

    private static String doCheckCheckList(JSONObject checkRuleJson, JSONObject checkTargetJson) {
        StringBuilder builder = new StringBuilder();
        final CheckModeEnum checkMode = getCheckMode(checkRuleJson);
        List<CheckRule> defaultCheckRuleList = ParamCheckCommonUtil.getDefaultCheckRuleList();
        final List<CheckItem> checkItemList =
                ParamCheckCommonUtil.getCheckRuleList(checkRuleJson, defaultCheckRuleList);

        // checkList 校验
        for (CheckItem item : checkItemList) {
            doCheckItem(item, checkTargetJson, checkItemList, checkMode, builder);
        }
        return builder.toString();
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
            ParamCheckCommonUtil.throwBizException("校验模式配置错误, 未找到匹配的校验模式");
        }
        return modeEnum;
    }


    /**
     * 按照配置的校验项对校验目标进行校验
     *
     * @param checkItem     单个字段的校验规则
     * @param checkTarget   参数校验的对象
     * @param checkItemList 配置的参数校验的字段的List
     * @param checkMode     校验规则 是否是一次错误即返回
     * @param builder       错误消息内容 builder
     */
    private static void doCheckItem(CheckItem checkItem, JSONObject checkTarget,
                                    List<CheckItem> checkItemList, CheckModeEnum checkMode,
                                    StringBuilder builder) {
        final String field = checkItem.getField();
        final List<CheckRule> checkRules = Optional.ofNullable(checkItem.getRules()).orElse(new ArrayList<>());
        final String fieldName = checkItem.getFieldName();
        final String fieldValue = checkTarget.getString(field);
        // 先进行required校验 确认是否非空, 非空则不要再进行之后的校验了
        if (checkRequire(checkRules, fieldValue, fieldName, builder)) {
            return;
        }
        final String result = doCheckItemRules(checkRules, fieldValue, fieldName, checkTarget, checkItemList);
        if (CharSequenceUtil.isNotEmpty(result)) {
            builder.append(result);
            if (checkMode == CheckModeEnum.ONE) {
                builder.append(CommonConstant.Separator.PERIOD);
            }
        }
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
    private static String doCheckItemRules(List<CheckRule> checkRules, String fieldValue, String fieldName,
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
                    builder.append(ParamCheckRelateUtil.doCheckRelate(checkRule, fieldValue, fieldName, checkTarget,
                            checkItemList));
                    break;
                default:
            }
        }
        return builder.toString();
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
            ParamCheckCommonUtil.throwBizException("范围校验参数配置错误,code:{}, value:{}", checkRule.getCode(),
                    checkRule.getValue());
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
     * @return 字段长度校验 为空表示正常
     */
    private static String doCheckLen(CheckRule checkRule, String fieldValue, String fieldName) {
        CheckRuleEnum ruleEnum = CheckRuleEnum.getByCode(checkRule.getCode());
        // 长度限定的边界值
        Integer bound = NumberUtil.parseToInt(checkRule.getValue());
        if (bound == null) {
            ParamCheckCommonUtil.throwBizException("{}校验值配置错误:{}, 转换为数字失败", checkRule.getCode(), checkRule.getValue());
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
     * 判断该字段是否必填
     *
     * @param checkRules 字段的所有校验规则
     * @param fieldValue 字段值
     * @param fieldName  字段名
     * @param builder    校验错误消息 builder
     * @return 是否必填
     */
    private static Boolean checkRequire(List<CheckRule> checkRules, String fieldValue, String fieldName,
                                        StringBuilder builder) {
        // 取得必填的校验项
        final List<CheckRule> requiredList = CheckRuleNameEnum.filterByRuleName(checkRules, CheckRuleNameEnum.REQUIRED);
        // 将必填校验项移除, 之后不再处理
        checkRules.removeAll(requiredList);
        if (requiredList.size() > 1) {
            ParamCheckCommonUtil.throwBizException("{}必填规则设置错误, 只可以设置一个必填规则", fieldName);
        } else if (requiredList.size() < 1) {
            return false;
        }
        final CheckRule checkRule = requiredList.get(0);
        boolean required = CheckRuleEnum.REQUIRE_TRUE.getCode().equals(checkRule.getCode());
        if (required && CharSequenceUtil.isEmpty(fieldValue)) {
            builder.append(getMessage(checkRule, fieldName));
            return true;
        }
        return false;
    }
}
