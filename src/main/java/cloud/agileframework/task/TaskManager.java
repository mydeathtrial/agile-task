package cloud.agileframework.task;

import cloud.agileframework.task.exception.NotFoundTaskException;
import cloud.agileframework.task.factory.TaskThreadFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.ProxyUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * @author 佟盟 on 2018/2/2
 * @author 佟盟
 */
public class TaskManager implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private static final String NO_SUCH_TARGETS_ERROR = "任务:[{}]任务未绑定任何执行方法数据，无法加载该任务";
    private static final String NO_SUCH_CRON_ERROR = "任务:[{}]定时任务未配置时间表达式，无法加载该任务";
    private static final String INIT_TASK = "任务:[{}][完成初始化][下次执行时间{}]";
    private static final String INIT_TASKS = "检测出定时任务%s条";

    private static final Map<Long, TaskInfo> TASK_INFO_MAP = new HashMap<>();
    private static final Map<String, Method> API_BASE_MAP = new HashMap<>();

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    private final ApplicationContext applicationContext;
    private final TaskService taskService;
    private final TaskProxy taskProxy;

    public TaskManager(ApplicationContext applicationContext, TaskService taskManager, TaskProxy taskProxy) {
        this.applicationContext = applicationContext;
        this.taskService = taskManager;
        this.taskProxy = taskProxy;
        this.threadPoolTaskScheduler.setThreadFactory(new TaskThreadFactory("定时任务"));
        this.threadPoolTaskScheduler.initialize();
        this.threadPoolTaskScheduler.setPoolSize(10);
    }

    /**
     * spring容器初始化时初始化全部定时任务
     */
    @Override
    public void run(ApplicationArguments args) {
        threadPoolTaskScheduler.execute(() -> {
            initMethodCache();
            //获取持久层定时任务数据集
            List<Task> list = taskService.getTask();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format(INIT_TASKS, list.size()));
            }
            for (Task task : list) {
                //获取定时任务详情列表
                List<Target> targets = taskService.getApisByTaskCode(task.getCode());
                if (ObjectUtils.isEmpty(targets)) {
                    logger.error(NO_SUCH_TARGETS_ERROR, task.getCode());
                    continue;
                }
                try {
                    updateTask(task);
                } catch (NotFoundTaskException | NoSuchMethodException e) {
                    logger.error("初始化定时任务异常", e);
                }
            }
        });
    }

    /**
     * 初始化全部任务目标方法
     */
    private void initMethodCache() {
        String[] beans = applicationContext.getBeanDefinitionNames();

        for (String beanName : beans) {

            Object bean = applicationContext.getBean(beanName);
            Class<?> clazz = ProxyUtils.getUserClass(bean);
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getParameterCount() > 1) {
                    continue;
                }
                API_BASE_MAP.put(method.toGenericString(), method);
            }
        }
    }

    /**
     * 获取方法缓存
     *
     * @param generic 根据方法的toGenericString检索
     * @return 方法信息
     */
    static Method getApi(String generic) throws NoSuchMethodException {
        Method method = API_BASE_MAP.get(generic);
        if (method == null) {
            throw new NoSuchMethodException(generic);
        }
        return method;
    }

    /**
     * 添加/更新定时任务
     *
     * @param task 定时任务信息
     */
    public void updateTask(Task task) throws NotFoundTaskException, NoSuchMethodException {
        if (API_BASE_MAP.isEmpty()) {
            initMethodCache();
        }
        if (ObjectUtils.isEmpty(task.targets())) {
            logger.error(NO_SUCH_TARGETS_ERROR, task.getCode());
            return;
        }

        // 当任务已经存在时，删掉旧的过时任务，再重新定义任务
        if (TASK_INFO_MAP.get(task.getCode()) != null) {
            removeTask(task.getCode());
        }

        //取定时任务表达式
        String cronString = task.getCron();
        if (StringUtils.isEmpty(cronString)) {
            logger.error(NO_SUCH_CRON_ERROR, task.getCode());
            return;
        }
        String[] crones = cronString.split(";");

        // 任务信息集合
        List<TaskActuatorInterface> actuators = new ArrayList<>();

        //新建任务
        TaskJob job = new TaskJob(taskService, taskProxy, task);

        for (String cron : crones) {
            cron = cron.trim();
            ScheduledFuture<?> scheduledFuture = null;
            // 如果表达式位时间戳，则执行固定时间执行任务,否则执行周期任务
            if (NumberUtils.isCreatable(cron)) {
                long executeTime = NumberUtils.toLong(cron);
                Instant instant;

                if (executeTime <= System.currentTimeMillis()) {
                    continue;
                }
                instant = Instant.ofEpochMilli(executeTime);

                if (task.getEnable() != null && task.getEnable()) {
                    scheduledFuture = threadPoolTaskScheduler.schedule(job, instant);
                }

                actuators.add(new InstantActuator(scheduledFuture, instant, threadPoolTaskScheduler));
            } else {
                //新建定时任务触发器
                CronTrigger trigger = new CronTrigger(cron);

                if (task.getEnable() != null && task.getEnable()) {
                    scheduledFuture = threadPoolTaskScheduler.schedule(job, trigger);
                }

                actuators.add(new TriggerActuator(scheduledFuture, trigger, threadPoolTaskScheduler));
            }
        }

        TaskInfo taskInfo = new TaskInfo(actuators, job);
        //定时任务装入缓冲区
        TASK_INFO_MAP.put(task.getCode(), taskInfo);

        // 同步更新持久层数据
        taskService.save(task);

        if (taskInfo.nextExecutionTime() != null && logger.isDebugEnabled()) {
            logger.debug(INIT_TASK, task.getCode(),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(taskInfo.nextExecutionTime())
            );
        }
    }

    /**
     * 获取任务下一次执行时间
     *
     * @param taskCode 定时任务标识
     * @return 标识
     */
    public static Date nextExecutionTimeByTaskCode(Long taskCode) {
        TaskInfo taskInfo = TASK_INFO_MAP.get(taskCode);
        if (taskInfo != null) {
            return taskInfo.nextExecutionTime();
        }
        return null;
    }

    /**
     * 启动定时任务
     *
     * @param taskCode 定时任务标识
     */
    public static void reStart(Long taskCode) {
        TaskInfo taskInfo = TASK_INFO_MAP.get(taskCode);
        if (taskInfo != null) {
            taskInfo.start();
        }
    }

    /**
     * 删除定时任务
     *
     * @param taskCode 任务标识
     * @throws NotFoundTaskException 没找到
     */
    public void removeTask(Long taskCode) throws NotFoundTaskException {
        if (TASK_INFO_MAP.containsKey(taskCode)) {
            stopTask(taskCode);
            TASK_INFO_MAP.remove(taskCode);
        }
        taskService.remove(taskCode);
    }

    /**
     * 停止定时任务
     *
     * @param taskCode 任务标识
     * @throws NotFoundTaskException 没找到
     */
    public void stopTask(Long taskCode) throws NotFoundTaskException {
        TaskInfo taskInfo = TASK_INFO_MAP.get(taskCode);
        if (ObjectUtils.isEmpty(taskInfo)) {
            throw new NotFoundTaskException(String.format("未找到定时任务[%s]", taskCode));
        }
        //任务取消
        taskInfo.stop();
        taskService.unLock(taskInfo.getCode());
        taskService.enable(taskCode, false);
    }

    public void startTask(long taskCode) throws NotFoundTaskException {
        TaskInfo taskInfo = TASK_INFO_MAP.get(taskCode);
        if (ObjectUtils.isEmpty(taskInfo)) {
            throw new NotFoundTaskException(String.format("未找到主键为%s的定时任务", taskCode));
        }
        taskInfo.start();
        taskService.enable(taskCode, true);
    }

    public void removeTaskByMethod(Method method) throws NotFoundTaskException {
        List<Task> tasks = taskService.getTasksByApiCode(method.toGenericString());
        if (tasks == null) {
            return;
        }
        for (Task task : tasks) {
            removeTask(task.getCode());
        }
    }
}
