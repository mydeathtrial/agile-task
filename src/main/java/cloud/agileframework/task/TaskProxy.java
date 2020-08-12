package cloud.agileframework.task;

import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author 佟盟
 * 日期 2019/6/25 15:37
 * 描述 定时任务调用代理
 * @version 1.0
 * @since 1.0
 */
public class TaskProxy implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private static final String TASK_CODE = "$TaskCode";
    private static final String TASK_INFO = "$Task";

    public void invoke(Method method, String sourceArgument, Task task) throws InvocationTargetException, IllegalAccessException {

        method.setAccessible(true);
        Class<?>[] parameterTypes = method.getParameterTypes();

        Object bean = applicationContext.getBean(method.getDeclaringClass());

        if (parameterTypes.length == 1) {
            Object argument;
            Class<?> parameterType = parameterTypes[0];
            if (TASK_CODE.equals(sourceArgument)) {
                argument = ObjectUtil.to(task.getCode(), new TypeReference<>(parameterType));
            } else if (TASK_INFO.equals(sourceArgument)) {
                argument = ObjectUtil.to(task, new TypeReference<>(parameterType));
            } else {
                argument = ObjectUtil.to(sourceArgument, new TypeReference<>(parameterType));
            }

            method.invoke(bean, argument);
        } else {
            method.invoke(bean);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
