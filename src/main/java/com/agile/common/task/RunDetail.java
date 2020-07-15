package com.agile.common.task;

import java.util.Date;

/**
 * @author 佟盟
 * 日期 2019/5/13 14:44
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class RunDetail {
    private Long taskCode;
    private boolean ending;
    private Date startTime;
    private Date endTime;
    private StringBuilder log = new StringBuilder();

    void addLog(String log) {
        this.log.append(log).append("\n");
    }

    public void setTaskCode(Long taskCode) {
        this.taskCode = taskCode;
    }

    public void setEnding(boolean ending) {
        this.ending = ending;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setLog(StringBuilder log) {
        this.log = log;
    }

    public StringBuilder getLog() {
        return log;
    }

    public Long getTaskCode() {
        return taskCode;
    }

    public boolean isEnding() {
        return ending;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }
}
