package cloud.agileframework.task;

import java.util.Date;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2019/5/9 16:45
 * 描述 定时任务持久层操作
 * @version 1.0
 * @since 1.0
 */
public interface TaskService {
    /**
     * 取所有定时任务
     *
     * @return 定时任务列表
     */
    List<Task> getTask();

    /**
     * 根据定时任务标识查询所属任务目标
     *
     * @param code 定时任务标识
     * @return 任务目标列表
     */
    List<Target> getApisByTaskCode(long code);

    /**
     * 根据定时任务标识查询所属任务目标
     *
     * @param code 定时任务标识
     * @return 任务目标列表
     */
    List<Task> getTasksByApiCode(String code);

    /**
     * 保存任务和对应的任务列表
     *
     * @param task 任务
     */
    void save(Task task);

    /**
     * 删除定时任务
     *
     * @param taskCode 定时任务
     */
    void remove(long taskCode);

    /**
     * 运行
     *
     * @param taskCode 任务标识
     */
    void run(long taskCode);

    /**
     * 已完成运行
     *
     * @param taskCode 任务标识
     */
    void finish(long taskCode);

    /**
     * 记录运行日志
     *
     * @param runDetail 运行信息
     */
    void logging(RunDetail runDetail);

    /**
     * 获取分布式锁
     *
     * @param taskCode   锁名称
     * @param unlockTime 加锁时间（秒）
     * @return 如果获取到锁，则返回lockKey值，否则为null
     */
    boolean setNxLock(long taskCode, Date unlockTime);

    /**
     * 解锁同步任务
     *
     * @param taskCode 锁名字
     */
    void unLock(long taskCode);

    /**
     * 启用/禁用
     *
     * @param taskCode 任务标识
     * @param enable   true 启用
     */
    void enable(long taskCode, boolean enable);
}
