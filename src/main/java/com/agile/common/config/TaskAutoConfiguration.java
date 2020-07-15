package com.agile.common.config;

import com.agile.common.task.TaskManager;
import com.agile.common.task.TaskProxy;
import com.agile.common.task.TaskService;
import com.agile.common.task.TaskServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 描述：
 * <p>创建时间：2018/11/29<br>
 *
 * @author 佟盟
 * @version 1.0
 * @since 1.0
 */
@Configuration
@ConditionalOnProperty(name = "enable", prefix = "agile.task")
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
    @ConditionalOnMissingBean(
            type = {"com.agile.common.task.TaskService"}
    )
    @Lazy
    public TaskService taskService() {
        return new TaskServiceImpl();
    }
}
