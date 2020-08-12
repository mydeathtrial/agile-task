package cloud.agileframework.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

/**
 * @author 佟盟
 * 日期 2020/4/29 17:42
 * 描述 定时任务抽象类
 * @version 1.0
 * @since 1.0
 */
public class TaskJob implements Runnable {

    static final String START_TASK = "任务:[{}][开始执行]";
    static final String NO_API_TASK = "任务:[%s][非法任务，未绑定任何api信息，任务结束]";
    static final String NO_SUCH_METHOD_TASK = "任务:[%s][未找到对应的任务执行方法%s]";
    static final String ILLEGAL_API_TASK = "任务:[%s][非法任务，入参大于1个，任务结束]";
    static final String EXCEPTION_API_TASK = "任务:[%s][任务异常]";
    static final String RUN_TASK_API = "任务:[{}][执行]";
    static final String EXCEPTION_RUN_TASK_API = "任务:[%s][任务异常]";
    static final String END_TASK = "任务:[{}][任务完成]";
    static final String NEXT_TASK = "任务:[{}][下次执行时间{}]";
    static final String LAST_TASK = "任务:[{}][本次为最后一次执行]";

    private final Logger logger = LoggerFactory.getLogger(TaskJob.class);
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 持久层工具
     */
    private final TaskService taskService;
    private final Task task;

    /**
     * 任务执行代理
     */
    private final TaskProxy taskProxy;

    public TaskJob(TaskService taskService, TaskProxy taskProxy, Task task) {
        this.taskService = taskService;
        this.taskProxy = taskProxy;
        this.task = task;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                //重启该定时任务下所有执行器
                TaskManager.reStart(task.getCode());

                //获取下次执行时间（秒）
                Date nextTime = TaskManager.nextExecutionTimeByTaskCode(getTask().getCode());

                //判断是否需要同步，同步情况下获取同步锁后方可执行，非同步情况下直接运行
                if (getTask().getSync() != null && getTask().getSync()) {
                    //如果抢到同步锁，设置锁定时间并直接运行
                    if (taskService.setNxLock(getTask().getCode(), nextTime)) {
                        invoke();
                    }
                } else {
                    invoke();
                }

                if (nextTime == null) {
                    logger.info(LAST_TASK, getTask().getCode());
                } else {
                    logger.info(NEXT_TASK, getTask().getCode(), simpleDateFormat.format(nextTime));
                }
            } catch (Exception e) {
                logger.error(String.format(EXCEPTION_API_TASK, getTask().getCode()), e);
            }
        }
    }

    /**
     * 逐个执行定时任务目标方法
     */
    public void invoke() {
        RunDetail runDetail = new RunDetail();
        runDetail.setTaskCode(task.getCode());
        runDetail.setStartTime(new Date());
        runDetail.setEnding(true);
        start(runDetail);
        running(runDetail);
        end(runDetail);
    }

    void exception(Throwable e, RunDetail runDetail) {
        runDetail.addLog(exceptionToString(e));
        runDetail.setEnding(false);
        if (logger.isErrorEnabled()) {
            logger.error(String.format(EXCEPTION_API_TASK, runDetail.getTaskCode()), e);
        }
    }

    /**
     * 异常转字符串
     *
     * @param e 异常
     * @return 字符串
     */
    public static String exceptionToString(Throwable e) {
        StringWriter writer = new StringWriter();
        try (PrintWriter pw = new PrintWriter(writer);) {
            e.printStackTrace(pw);
        }
        return writer.toString();
    }

    void start(RunDetail runDetail) {
        if (logger.isInfoEnabled()) {
            logger.info(START_TASK, runDetail.getTaskCode());
        }
        if (taskService != null) {
            //通知持久层，任务开始运行
            taskService.run(runDetail.getTaskCode());
        }

    }

    void end(RunDetail runDetail) {
        if (logger.isInfoEnabled()) {
            logger.info(END_TASK, runDetail.getTaskCode());
        }
        if (taskService != null) {
            runDetail.setEndTime(new Date());
            taskService.logging(runDetail);
            //通知持久层，任务开始运行
            taskService.finish(task.getCode());
        }
    }

    private void running(RunDetail runDetail) {
        if (ObjectUtils.isEmpty(task.targets())) {
            String log = String.format(NO_API_TASK, getTask().getCode());
            runDetail.addLog(log);
            logger.error(String.format(log, runDetail.getTaskCode()));
            return;
        }

        task.targets().stream().sorted(Comparator.comparingInt(Target::getOrder)).forEach(target -> {
            String log;
            Method method;
            try {
                method = TaskManager.getApi(target.getCode());
            } catch (NoSuchMethodException e) {
                log = String.format(NO_SUCH_METHOD_TASK, task.getCode(), target.getCode());
                runDetail.addLog(log);
                logger.error(log);
                return;
            }
            String code = method.toGenericString();
            if (method.getParameterCount() > 1) {
                log = String.format(ILLEGAL_API_TASK, code);
                runDetail.addLog(log);
                logger.error(String.format(log, code));
                return;
            }

            Optional.ofNullable(taskProxy).ifPresent(proxy -> {
                try {
                    logger.debug(RUN_TASK_API, code);
                    proxy.invoke(method, target.getArgument(), getTask());
                } catch (InvocationTargetException | IllegalAccessException e) {
                    logger.error(String.format(EXCEPTION_RUN_TASK_API, code), e);
                    exception(e, runDetail);
                }
            });
        });
    }

    public Task getTask() {
        return task;
    }
}
