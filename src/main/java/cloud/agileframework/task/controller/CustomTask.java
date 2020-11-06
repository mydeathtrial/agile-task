package cloud.agileframework.task.controller;

import cloud.agileframework.task.Task;
import cloud.agileframework.task.TaskManager;

import java.lang.reflect.Method;

/**
 * @author 佟盟
 * 日期 2020/8/00010 20:10
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class CustomTask implements Task {
    private Long code;
    private String name;
    private String cron;
    private boolean sync;
    private boolean enable;
    private String methodName;
    private String argument;

    @Override
    public Long getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCron() {
        return cron;
    }

    @Override
    public Boolean getSync() {
        return sync;
    }

    @Override
    public Boolean getEnable() {
        return enable;
    }

    @Override
    public Method getMethod() {
        try {
            return TaskManager.getApi(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getArgument() {
        return argument;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    @Override
    public String toString() {
        return "CustomTask{" +
                "code=" + code +
                ", name='" + name + '\'' +
                ", cron='" + cron + '\'' +
                ", sync=" + sync +
                ", enable=" + enable +
                ", method=" + methodName +
                ", argument=" + argument +
                '}';
    }
}
