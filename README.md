# agile-task ： 任务调度组件

[![spring](https://img.shields.io/badge/Spring-LATEST-green)](https://img.shields.io/badge/Spring-LATEST-green)
[![maven](https://img.shields.io/badge/build-maven-green)](https://img.shields.io/badge/build-maven-green)

## 它有什么作用

* **持久化定时任务**
  任务数据支持直接落入数据库，防止重启数据丢失，持久化防止支持自定义扩展
  通过实现接口cloud.agileframework.task.TaskService，可以自定义持久化方式，
  如落入MySQL、Redis、Oracle等。默认持久化方式为内存形式，内存形式不支持重启。

* **启动加载**
  程序伴随spring应用启动时自动加载定时任务数据，无需人工干预

* **动态修改**
  在程序运行过程中，支持通过restful api或自定义程序调用，动态添加、修改、删除定时任务 且不需要重启服务

* **周期时间任务**
  支持spring cron表达式形式的周期性定时任务

* **固定时间点任务**
  支持以时间戳为表达式的固定时间点任务，如仅需要执行一次的定时任务。

* **多表达式**
  支持在一个定时任务中，输入多个固定或周期任务表达式，以英文分号分隔，则任务将同时识别
  多表达式，针对同一个任务，避免了按不同周期或时间点创建多个任务定义的繁琐操作，也便于维护。

* **任务冲突**
  任务冲突分应用级别冲突与表达式级别冲突： 应用级别冲突指在分布式或集群中，相同任务，在同一时刻时，触发多个应用程序同时执行任务调度，
  此种情况下，agile-task提供加解锁接口，锁方式用户可根据实际场景自行定义，可直接使用redis锁实现，该锁于agile-cache组件中
  提供，开箱即用。 表达式级别冲突指当同一个任务，定义`多表达式`
  时，表达式之间会产生时间点重叠，agile-task会自动计算重叠时间点，且保证重叠时间点 仅运行一次的效果。

* **方法集与排序**
  任务一般只捆绑一个程序中的方法，但agile-task也支持一个任务直接捆绑多个方法，且为捆绑的多个方法进行排序。
  当任务执行时会按照定义的方法的排序顺序，依次执行捆绑的方法。

* **定义复杂任务参数**
  支持为任务定义复杂参数，参数的内容将以json串的形式持久化，运行时翻译为任务所需类型参数，其中内置 两个常用参数标识符，作为特殊参数识别：
  <br> `$Task`:任务信息，当参数声明为该标识符时，任务执行过程直接以任务定义信息为参数调用定时任务方法。
  <br> `$TaskCode`:任务标识，当参数声明为该标识符时，任务执行过程直接以任务唯一标识为参数调用定时任务方法。

-------

## 快速入门

开始你的第一个项目是非常容易的。

#### 步骤 1: 下载包

您可以从[最新稳定版本]下载包(https://github.com/mydeathtrial/agile-task/releases). 该包已上传至maven中央仓库，可在pom中直接声明引用

以版本agile-task-2.1.0.M5.jar为例。

#### 步骤 2: 添加maven依赖

```xml
<!--声明中央仓库-->
<repositories>
    <repository>
        <id>cent</id>
        <url>https://repo1.maven.org/maven2/</url>
    </repository>
</repositories>
        <!--声明依赖-->
<dependency>
<groupId>cloud.agileframework</groupId>
<artifactId>agile-task</artifactId>
<version>2.1.0.M5</version>
</dependency>
```

#### 步骤 3: 开箱即用

##### 组件开关

```
//组件加载开关，默认为`true`开启
agile.task.enable=true

//组件restful api加载开关，默认为`false`关闭
agile.task.controller.enable=true
```

##### restful方式动态添加任务

```
//以新建任务唯一标识为24，每秒执行一次的定时任务为例，直接调用接口，输入以下body参数
地址：POST http://localhost:8080/task
参数：
{
    "code": "1",
    "name": "task",
    "cron": "0/1 * * * * ?",
    "sync": true,
    "enable": true,
    "targets": [
        {
            "code": "public void cloud.agileframework.task.controller.TaskController.hello(cloud.agileframework.task.Task)",
            "argument": "$Task"
        }
    ]
}
```

任务运行日志

```
2020-08-12 10:31:20.466  INFO 16320 --- [  pool-1-定时任务-3] cloud.agileframework.task.TaskJob        : 任务:[24][任务完成]
2020-08-12 10:31:20.467  INFO 16320 --- [  pool-1-定时任务-3] cloud.agileframework.task.TaskJob        : 任务:[24][下次执行时间2020-08-12 10:31:11]
2020-08-12 10:31:21.006  INFO 16320 --- [  pool-1-定时任务-3] cloud.agileframework.task.TaskJob        : 任务:[24][开始执行]
2020-08-12 10:31:20.466  INFO 16320 --- [  pool-1-定时任务-3] cloud.agileframework.task.TaskJob        : 任务:[24][任务完成]
2020-08-12 10:31:20.467  INFO 16320 --- [  pool-1-定时任务-3] cloud.agileframework.task.TaskJob        : 任务:[24][下次执行时间2020-08-12 10:31:12]
2020-08-12 10:31:21.006  INFO 16320 --- [  pool-1-定时任务-3] cloud.agileframework.task.TaskJob        : 任务:[24][开始执行]
...
```

##### restful方式动态启停

```
//以刚刚创建的任务24为例
停用地址：POST http://localhost:8080/task/24/false
启用地址：POST http://localhost:8080/task/24/true
```

##### restful方式查询

```
//以刚刚创建的任务24为例
地址：GET http://localhost:8080/task/24
//查询所有任务
地址：GET http://localhost:8080/task
```

##### restful方式更新

```
//以刚刚创建的任务24为例，修改任务执行周期为多表达式，参数改为任意字符串
地址：POST http://localhost:8080/task
参数：
{
    "code": "1",
    "name": "task",
    "cron": "0/1 * * * * ?",
    "sync": true,
    "enable": true,
    "targets": [
        {
            "code": "public void cloud.agileframework.task.controller.TaskController.hello(java.lang.String)",
            "argument": "我是入参，我也可以是json串结构哦"
        }
    ]
}
```

#### 进阶 1: 自定义持久化方式

##### 自定义任务数据结构

> 任务结构:`cloud.agileframework.task.Task`
> 默认结构:`cloud.agileframework.task.controller.CustomTask`
>
>任务持久化接口：`cloud.agileframework.task.TaskService`
> 默认持久化方法：`cloud.agileframework.task.TaskServiceImpl`

##### 数据结构说明

**任务定义(`cloud.agileframework.task.Task`)**

```
public interface Task {
    /**
     * 取任务唯一标识
     *
     * @return 唯一标识
     */
    Long getCode();

    /**
     * 取任务名字
     *
     * @return 任务名
     */
    String getName();

    /**
     * 取cron表达式
     *
     * @return 定时任务表达式
     */
    String getCron();

    /**
     * 是否集群同步
     *
     * @return 是否
     */
    Boolean getSync();

    /**
     * 是否可用
     *
     * @return 是否
     */
    Boolean getEnable();

    /**
     * 取任务目标标识
     *
     * @return 唯一标识
     */
    Method getMethod();

    /**
     * 任务入参
     *
     * @return 字符串入参
     */
    String getArgument();
}
```

