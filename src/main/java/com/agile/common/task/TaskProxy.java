package com.agile.common.task;

import com.agile.common.util.clazz.TypeReference;
import com.agile.common.util.object.ObjectUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Constructor;
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

    public void invoke(Method method, Task task) throws InvocationTargetException, IllegalAccessException {

        method.setAccessible(true);
        Class<?>[] parameterTypes = method.getParameterTypes();

        Object bean = applicationContext.getBean(method.getDeclaringClass());
        if (bean == null) {
            bean = newInstance(method.getDeclaringClass());
        }
        if (parameterTypes.length == 1) {
            Object argument = ObjectUtil.to(task.getArgument(), new TypeReference<>(parameterTypes[0]));
            method.invoke(bean, argument);
        } else {
            method.invoke(bean);
        }
    }

    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ignored) {
        }
        try {
            Method method = Class.class.getDeclaredMethod("privateGetDeclaredConstructors", boolean.class);
            method.setAccessible(true);
            Constructor<T>[] constructors = (Constructor<T>[]) method.invoke(clazz, false);
            if (constructors.length > 0) {
                Constructor<T> privateConstructor = constructors[0];
                privateConstructor.setAccessible(true);
                return privateConstructor.newInstance();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
