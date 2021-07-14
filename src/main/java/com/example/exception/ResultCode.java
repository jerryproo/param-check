package com.example.exception;

import lombok.Getter;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description
 */
@Getter
public enum ResultCode {
    /**
     * 参数校验配置错误
     */
    PARAM_CHECK_NOT_VALID(4055, "参数校验配置错误"),
    /**
     * 模板格式不正确
     */
    TEMPLATE_FORMAT_DATA_ERROR(4282, "模板格式不正确");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.msg = message;
    }

    public static ResultCode getResultCode(int code) {
        for (ResultCode item : ResultCode.values()) {
            if (code == item.getCode()) {
                return item;
            }
        }
        return null;
    }

    public static String getMsg(int code) {
        for (ResultCode item : values()) {
            if (code == item.getCode()) {
                return item.msg;
            }
        }
        return null;
    }

    public static Integer getCode(String msg) {
        for (ResultCode item : values()) {
            if (item.name().toLowerCase().equals(msg)) {
                return item.code;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
