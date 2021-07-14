package com.example.util;

import cn.hutool.core.lang.Console;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.vo.CheckItem;
import com.example.vo.DefaultRule;

import java.util.List;
import java.util.Map;

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

        // 将所有的添加到一个list中, 设置对应的ruleName
        defaultRuleMap.values().forEach(Console::log);

        // 将默认值设置到对应的校验规则中
        checkItems.forEach(item -> {

        });

        return null;
    }
}
