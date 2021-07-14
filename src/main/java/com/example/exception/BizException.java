package com.example.exception;

import lombok.Getter;

/**
 * @Author jerrypro
 * @Date 2021/7/14
 * @Description
 */
@Getter
public class BizException extends RuntimeException {

    private Integer code;
    private Exception exception;

    private LogLevel logLevel = LogLevel.INFO;

    public BizException() {
        super();
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, LogLevel logLevel) {
        super(message);
        this.logLevel = logLevel;
    }


    public BizException(Throwable cause) {
        super(cause);
    }

    public BizException(ResultCode rc) {
        this(rc.getMsg());
        this.code = rc.getCode();
    }

    public BizException(Integer code, String msg) {
        this(msg);
        this.code = code;
    }

    public BizException(Integer code, String msg, Exception exception) {
        this(msg);
        this.code = code;
        this.exception = exception;
    }

    public BizException(ResultCode rc, LogLevel logLevel) {
        this(rc.getMsg());
        this.code = rc.getCode();
        this.logLevel = logLevel;
    }

    public BizException(Integer code, String msg, LogLevel logLevel) {
        this(msg);
        this.code = code;
        this.logLevel = logLevel;
    }

    public BizException(Integer code, String msg, Exception exception, LogLevel logLevel) {
        this(msg);
        this.code = code;
        this.exception = exception;
        this.logLevel = logLevel;
    }

}
