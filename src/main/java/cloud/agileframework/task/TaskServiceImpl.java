package cloud.agileframework.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author 佟盟
 * 日期 2019/5/9 16:04
 * 描述 定时任务持久层操作
 * @version 1.0
 * @since 1.0
 */
public class TaskServiceImpl implements TaskService {
    private final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    private static final List<Task> TASKS_CACHE = new ArrayList<>();

    @Override
    public List<? extends Task> getTask() {
        return TASKS_CACHE;
    }

    @Override
    public void saveOrUpdate(Task task) {
        TASKS_CACHE.removeIf(node -> node.getCode().equals(task.getCode()));
        TASKS_CACHE.add(task);
    }


    @Override
    public void remove(long taskCode) {
        TASKS_CACHE.removeIf(node -> node.getCode().equals(taskCode));
    }

    @Override
    public void running(long taskCode) {
    }

    @Override
    public void suspend(long taskCode) {
    }

    @Override
    public void logging(RunDetail runDetail) {
        logger.debug(runDetail.getLog().toString());
    }

    @Override
    public boolean setNxLock(long taskCode, Date unlockTime) {
        return true;
    }

    @Override
    public void unLock(long taskCode) {
    }

    @Override
    public void enable(long taskCode, boolean enable) {
        Optional<Task> optional = TASKS_CACHE.stream().filter(task -> task.getCode().equals(taskCode)).findFirst();
        if (optional.isPresent()) {
            Task task = optional.get();
            TASKS_CACHE.remove(task);

            TASKS_CACHE.add(new Task() {
                @Override
                public Long getCode() {
                    return task.getCode();
                }

                @Override
                public String getName() {
                    return task.getName();
                }

                @Override
                public String getCron() {
                    return task.getCron();
                }

                @Override
                public Boolean getSync() {
                    return task.getSync();
                }

                @Override
                public Boolean getEnable() {
                    return !task.getEnable();
                }

                @Override
                public Method getMethod() {
                    return task.getMethod();
                }

                @Override
                public String getArgument() {
                    return task.getArgument();
                }
            });
        }
    }

}
