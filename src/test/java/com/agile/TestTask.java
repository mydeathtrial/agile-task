package com.agile;

import cloud.agileframework.task.Target;
import cloud.agileframework.task.Task;
import cloud.agileframework.task.TaskManager;
import cloud.agileframework.task.TaskServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2020/7/14 18:03
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class TestTask {

    @Autowired
    private TaskManager taskManager;

    @Test
    public void run() throws Exception {
        taskManager.updateTask(new Task() {
            @Override
            public Long getCode() {
                return 1L;
            }

            @Override
            public String getName() {
                return "测试任务";
            }

            @Override
            public String getCron() {
                return "0/1 * * * * ? ";
            }

            @Override
            public Boolean getSync() {
                return true;
            }

            @Override
            public Boolean getEnable() {
                return true;
            }

            @Override
            public List<Target> targets() {
                return new ArrayList<Target>() {{
                    add(new Target() {
                        @Override
                        public String getCode() {
                            try {
                                return TaskServiceImpl.class.getMethod("getTask").toGenericString();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public String getArgument() {
                            return null;
                        }

                        @Override
                        public int getOrder() {
                            return 0;
                        }
                    });
                }};
            }
        });
    }
}
