package cloud.agileframework.task.config;

import cloud.agileframework.task.TaskManager;
import cloud.agileframework.task.TaskProxy;
import cloud.agileframework.task.TaskService;
import cloud.agileframework.task.TaskServiceImpl;
import cloud.agileframework.task.controller.TaskController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 描述：
 * <p>创建时间：2018/11/29<br>
 *
 * @author 佟盟
 * @version 1.0
 * @since 1.0
 */
@EnableScheduling
@Configuration
@ConditionalOnProperty(name = "enable", prefix = "agile.task", matchIfMissing = true)
public class TaskAutoConfiguration {
    @Bean
    public TaskManager customTaskServer(ApplicationContext applicationContext, TaskService taskTargetService, TaskProxy taskProxy) {
        return new TaskManager(applicationContext, taskTargetService, taskProxy);
    }

    @Bean
    public TaskProxy taskProxy() {
        return new TaskProxy();
    }

    @Bean
    @ConditionalOnMissingBean(TaskService.class)
    public TaskService taskService() {
        return new TaskServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "enable", prefix = "agile.task.controller")
    public TaskController taskController() {
        return new TaskController();
    }
}
