package com.example.handler;

import com.example.vo.CheckObject;

/**
 * @Author jerrypro
 * @Date 2021/7/19
 * @Description
 */
public interface ParamCheckHandlerAdapter {
    /**
     * 校验 当前 adapter 是否支持该 handler
     *
     * @param handler 要校验的 handler 对象
     * @return 当前对象是否可以使用给定的 handler
     */
    boolean supports(Object handler);

    /**
     * 进行参数校验
     *
     * @param checkObject 参数校验对象信息
     * @param handler     使用的handler, 必须先调用supports并返回tru8/ e
     * @return 参数校验结果, 无异常时返回空
     */
    String handle(CheckObject checkObject, Object handler);
}
