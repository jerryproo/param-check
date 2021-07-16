package com.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author jerrypro
 * @Date 2021/7/16
 * @Description
 */
@Getter
@AllArgsConstructor
public enum CheckModeEnum {
    /**
     * 所有校验一起返回
     */
    ALL("all"),

    /**
     * 有一个校验失败即返回
     */
    ONE("one");

    /**
     * code
     */
    private final String code;

    /**
     * 按照code取得检查模式
     *
     * @param code code
     * @return 检查模式
     */
    public static CheckModeEnum getByCode(String code) {
        for (CheckModeEnum modeEnum : CheckModeEnum.values()) {
            if (modeEnum.getCode().equals(code)) {
                return modeEnum;
            }
        }
        return null;
    }
}
