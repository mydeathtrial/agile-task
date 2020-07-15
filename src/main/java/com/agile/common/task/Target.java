package com.agile.common.task;


import java.lang.reflect.Method;

/**
 * @author 佟盟
 * 日期 2019/5/9 16:00
 * 描述 任务目标
 * @version 1.0
 * @since 1.0
 */
public interface Target {
    /**
     * 取任务目标标识
     *
     * @return 唯一标识
     */
    String getCode();

    /**
     * 根据方法创建执行目标
     *
     * @param method 方法
     * @return 执行目标
     */
    static Target create(Method method) {
        return method::toGenericString;
    }
}
