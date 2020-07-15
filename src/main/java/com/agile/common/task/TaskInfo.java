package com.agile.common.task;

import java.util.Date;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2020/4/30 15:09
 * 描述 定时任务信息
 * @version 1.0
 * @since 1.0
 */
public class TaskInfo {

    /**
     * 任务标识
     */
    private final long code;
    /**
     * 任务相关执行器
     */
    private List<TaskActuatorInterface> actuators;
    /**
     * 执行的任务
     */
    private final TaskJob job;

    public TaskInfo(List<TaskActuatorInterface> actuators, TaskJob job) {
        this.code = job.getTask().getCode();
        this.actuators = actuators;
        this.job = job;
    }

    /**
     * 任务停止
     */
    public void stop() {
        actuators.forEach(TaskActuatorInterface::cancel);
    }

    /**
     * 启动
     */
    public synchronized void start() {
        actuators.forEach(n -> n.reStart(job));
        nextExecutionTime();
    }

    public long getCode() {
        return code;
    }

    public List<TaskActuatorInterface> getActuators() {
        return actuators;
    }

    public void setActuators(List<TaskActuatorInterface> actuators) {
        this.actuators = actuators;
    }

    public TaskJob getJob() {
        return job;
    }

    /**
     * 获取下一次执行时间
     *
     * @return 时间
     */
    public Date nextExecutionTime() {
        Date nextExecutionTime = null;

        for (TaskActuatorInterface actuator : getActuators()) {

            Date nextTime = actuator.nextExecutionTime();
            if (nextTime == null || nextTime.getTime() <= System.currentTimeMillis()) {
                continue;
            }

            // 如果指定时间与周期时间点碰撞，则将对应的指定时间执行器取消
            if (nextExecutionTime == null || nextTime.getTime() < nextExecutionTime.getTime()) {
                nextExecutionTime = actuator.nextExecutionTime();
            } else if (nextTime.getTime() == nextExecutionTime.getTime()) {
                actuator.cancel();
            }
        }

        return nextExecutionTime;
    }
}
