# 动作模型

## 为什么需要动作模型

只有属性读取、计算和 `return` 的脚本只能表达决策，不能参与真实业务流程。Veyra 需要表达“批准、拒绝、预留库存”等动作，同时保持副作用可见、可审计、可测试。

## `emit`

```veyra
emit inventory.reserve(order.productId, quantity: order.quantity)
emit orders.approve(order.id, discount: discount)
```

`emit` 产生一条类型化命令，不表示脚本可以任意调用 Java。命令名称和参数必须来自宿主注册的命令契约。

## 命令计划

规则执行结果概念上包含两部分：

```text
Decision：Approved / Rejected / ...
Commands：按脚本顺序排列的命令列表
```

宿主可以在事务或其他可靠执行边界内消费命令。Veyra 不负责替代宿主的事务、幂等、重试和分布式一致性机制。

## 后续问题

- 命令是否允许条件分支后重复产生；
- 命令计划的序列化格式；
- 命令失败时是中止、返回错误还是交由宿主补偿；
- 是否需要声明命令的幂等性和资源成本。
