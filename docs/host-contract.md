# Java 宿主契约

## 注册原则

宿主必须显式注册脚本可见的类型和能力。Veyra 不扫描整个 JVM，也不允许脚本通过反射绕过白名单。

概念 API：

```java
HostContext.builder()
    .allowType("Order", Order.class)
    .allowType("User", User.class)
    .allowQuery("inventory", inventoryContract)
    .allowCommand("orders", ordersContract)
    .build();
```

API 名称仍可能调整，语义边界先固定。

## 属性访问

```veyra
order.amount
user.vip
```

编译器将属性映射到宿主明确允许的 getter、Java record accessor 或字段。属性拼写错误必须在编译/加载阶段报告，并尽可能提供相近名称建议。

## 查询能力

查询可以返回值，但必须由宿主标记为只读能力。查询不得通过隐藏行为修改业务状态；这项约束需要宿主注册 API 和运行时共同保证。

## 命令能力

命令由 `emit` 产生。每个命令必须有稳定名称、参数契约和版本策略。Veyra 默认产生命令计划，Java 宿主决定是否执行、如何纳入事务以及失败后的处理。

## 安全边界

默认拒绝：文件、网络、进程、线程、反射、系统属性、任意类加载和任意 Java 方法调用。任何新增能力都必须进入白名单，并补充测试和决策记录。
