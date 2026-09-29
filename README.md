# Veyra

> 让动态业务规则拥有静态契约。

Veyra 是一门嵌入 Java 应用的静态业务脚本语言。它使用简洁的缩进语法编写可热更新的业务规则，并在执行前校验脚本与 Java 宿主数据、查询能力和业务命令之间的契约。

Veyra 的目标不是替代 Java，也不是成为通用编程语言。Java 负责应用主体、领域模型、事务和基础设施；Veyra 负责可变的业务判断、计算和受控动作计划。

## 核心模型

```text
Java 宿主
  -> 注册允许访问的数据和能力
  -> 加载 Veyra 脚本
  -> 编译期完成语法、类型和宿主契约检查
  -> 执行规则并产生类型化业务命令
  -> Java 宿主决定事务、重试和最终执行
```

示例（语法草案，尚未进入可运行版本）：

```veyra
rule processOrder(order: Order, user: User): Decision
    if order.quantity <= 0
        emit orders.reject(order.id, reason: "数量必须大于 0")
        return Decision.rejected("INVALID_QUANTITY")

    let available = inventory.available(order.productId)

    if available < order.quantity
        emit orders.reject(order.id, reason: "库存不足")
        return Decision.rejected("OUT_OF_STOCK")

    let discount = calculateDiscount(order.amount, user.vip)
    emit inventory.reserve(order.productId, quantity: order.quantity)
    emit orders.approve(order.id, discount: discount)
    return Decision.approved(discount)
```

## 设计原则

- **Java 是宿主，Veyra 是脚本。** 领域实体由 Java 定义，Veyra 不负责替代领域模型。
- **先检查，后执行。** 未定义变量、类型错误、非法属性和未授权能力必须在加载时报告。
- **显式副作用。** 属性读取和纯查询使用普通表达式；业务动作使用 `emit`，避免隐藏副作用。
- **默认受限。** 脚本只能访问宿主明确注册的类型、属性、查询和命令。
- **小而专注。** 首版只服务于 JVM 应用中的业务规则，不追求通用语言特性。

## 当前状态

项目处于语言设计和 MVP 前置阶段。当前文档定义了 v0.1 的方向和语法草案，尚未承诺稳定语法或运行时兼容性。

实现顺序：

1. 词法分析、缩进 Token 和语法解析。
2. AST、符号表和基础类型检查。
3. Java 宿主契约检查。
4. 解释器和命令计划结果。
5. 热加载、沙箱和 JVM 字节码后端。
6. IntelliJ IDEA 插件和其他开发工具支持。

## 文档

- [语言设计](docs/language-design.md)
- [v0.1 语法](docs/syntax-v0.1.md)
- [类型系统](docs/type-system.md)
- [宿主契约](docs/host-contract.md)
- [动作模型](docs/action-model.md)
- [诊断规范](docs/diagnostics.md)
- [路线图](docs/roadmap.md)
- [设计决策记录](decisions/0001-project-direction.md)

## 许可证

Veyra 使用 Apache License 2.0，详见 [LICENSE](LICENSE)。
