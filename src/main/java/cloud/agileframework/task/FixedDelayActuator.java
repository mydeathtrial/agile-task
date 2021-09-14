package cloud.agileframework.task;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * @author 佟盟
 * 日期 2020/5/2 17:03
 * 描述 固定时间任务执行器
 * @version 1.0
 * @since 1.0
 */
public class FixedDelayActuator extends AbstractActuator<Instant> {
    public FixedDelayActuator(ScheduledFuture<?> scheduledFuture, Instant timeAbout, ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        super(scheduledFuture, timeAbout, threadPoolTaskScheduler);
    }

    @Override
    public void reStart(TaskJob job) {
        if (getScheduledFuture().isCancelled()) {
            setScheduledFuture(getThreadPoolTaskScheduler().scheduleWithFixedDelay(job, getTimeAbout().toEpochMilli()));
        }
    }

    @Override
    public Date nextExecutionTime() {
        return new Date(System.currentTimeMillis() + getTimeAbout().toEpochMilli());
    }
}
