package cloud.agileframework.task.controller;

import cloud.agileframework.task.Task;
import cloud.agileframework.task.TaskManager;
import cloud.agileframework.task.TaskService;
import cloud.agileframework.task.exception.NotFoundTaskException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author 佟盟
 * 日期 2020/8/00010 19:29
 * 描述 定时任务对外暴露api
 * @version 1.0
 * @since 1.0
 */
@Controller
public class TaskController {
    private final Logger logger = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    private TaskManager taskManager;

    @Autowired
    private TaskService taskService;

    public void hello(String id) {
        logger.info("Hello World!");
        System.out.println(id);
    }

    @PostMapping(value = "/task")
    public void add(@RequestBody CustomTask task, HttpServletResponse response) throws NoSuchMethodException, NotFoundTaskException, IOException {
        taskManager.updateTask(task);
        response(true, null, response);
    }

    @DeleteMapping(value = "/task/{taskCode}")
    public void add(@PathVariable Long taskCode, HttpServletResponse response) throws NotFoundTaskException, IOException {
        taskManager.removeTask(taskCode);
        response(true, null, response);
    }

    @PutMapping(value = "/task")
    public void update(@RequestBody CustomTask task, HttpServletResponse response) throws NoSuchMethodException, NotFoundTaskException, IOException {
        taskManager.updateTask(task);
        response(true, null, response);
    }

    @GetMapping(value = "/task/{taskCode}")
    public void query(@PathVariable Long taskCode, HttpServletResponse response) throws IOException {
        Optional<Task> taskOptional = taskService.getTask().stream().filter(task -> task.getCode().equals(taskCode)).findFirst();
        if (taskOptional.isPresent()) {
            response(true, taskOptional.get(), response);
        } else {
            response(false, null, response);
        }
    }

    @GetMapping(value = "/task")
    public void query(HttpServletResponse response) throws IOException {
        response(true, taskService.getTask(), response);
    }

    @PutMapping(value = "/task/{taskCode}/{status}")
    public void enable(@PathVariable Long taskCode, @PathVariable boolean status, HttpServletResponse response) throws IOException, NotFoundTaskException {
        if (status) {
            taskManager.startTask(taskCode);
        } else {
            taskManager.stopTask(taskCode);
        }

        response(true, null, response);
    }

    private void response(boolean isSuccess, Object context, HttpServletResponse response) throws IOException {
        JSONObject json = new JSONObject();
        if (isSuccess) {
            response.setStatus(HttpStatus.OK.value());
            json.put("status", "success");
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            json.put("status", "fail");
        }
        json.put("context", context);
        response.getWriter().print(json.toJSONString());
    }
}
