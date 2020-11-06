package cloud.agileframework.task;

import java.lang.reflect.Method;

/**
 * @author 佟盟
 * 日期 2019/5/9 19:02
 * 描述 任务
 * @version 1.0
 * @since 1.0
 */
public interface Task {
    /**
     * 取任务唯一标识
     *
     * @return 唯一标识
     */
    Long getCode();

    /**
     * 取任务名字
     *
     * @return 任务名
     */
    String getName();

    /**
     * 取cron表达式
     *
     * @return 定时任务表达式
     */
    String getCron();

    /**
     * 是否集群同步
     *
     * @return 是否
     */
    Boolean getSync();

    /**
     * 是否可用
     *
     * @return 是否
     */
    Boolean getEnable();

    /**
     * 取任务目标标识
     *
     * @return 唯一标识
     */
    Method getMethod();

    /**
     * 任务入参
     *
     * @return 字符串入参
     */
    String getArgument();
}
