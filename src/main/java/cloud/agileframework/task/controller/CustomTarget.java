package cloud.agileframework.task.controller;

import cloud.agileframework.task.Target;

/**
 * @author 佟盟
 * 日期 2020/8/00010 20:19
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class CustomTarget implements Target {
    private String code;
    private String argument;
    private int order;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getArgument() {
        return argument;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "CustomTarget{" +
                "code='" + code + '\'' +
                ", argument='" + argument + '\'' +
                ", order=" + order +
                '}';
    }
}
