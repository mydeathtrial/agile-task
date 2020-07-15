# agile-task

基于Spring的定时任务组件，支持固定时间和周期时间，支持数据持久化，支持同一个配置多执行周期，可自行扩展持久化方式。
默认为基于内存的任务调度。

## 任务管理器

>任务管理器提供对任务的实时控制，如启停、新增、修改执行周期、删除等控制
>任务管理器：com.agile.common.task.TaskManager

## 任务持久化扩展

>持久化扩展接口：com.agile.common.task.TaskService
>默认基于内存持久化实现：com.agile.common.task.TaskServiceImpl

## 自定义任务数据结构

>任务结构接口：com.agile.common.task.Task
>默认结构：基于com.agile.common.task.Task接口的匿名内部类
>
>任务执行方法结构接口：com.agile.common.task.Target
>默认执行方法结构：基于com.agile.common.task.Target接口的匿名内部类