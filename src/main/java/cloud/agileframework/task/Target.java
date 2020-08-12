package cloud.agileframework.task;


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
     * 任务入参
     *
     * @return 字符串入参
     */
    String getArgument();

    /**
     * 排序
     *
     * @return 优先级
     */
    int getOrder();
}
