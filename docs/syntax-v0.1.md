# Veyra v0.1 语法草案

> 状态：设计中。本文用于指导 MVP，不是稳定语言规范。

## 源文件

- 源文件后缀：`.veyra`。
- 文件使用 UTF-8 编码。
- 换行统一视为语句边界。
- 缩进使用 4 个空格；MVP 禁止 Tab，混用缩进时报错。
- 空行不改变缩进层级。
- 单行注释以 `//` 开始。

## 关键字

```text
rule function let var if else return emit true false null
```

## 声明

```veyra
rule process(order: Order): Decision
    return Decision.approved()

function calculateDiscount(amount: double, vip: bool): double
    if vip && amount >= 1000
        return 0.8
    return 1.0
```

`rule` 是宿主调用的业务入口；`function` 表示可复用的纯函数。MVP 阶段每个脚本最多一个公开 `rule`，函数间调用是否开放由实现阶段确定。

## 变量

```veyra
let amount = order.amount
let threshold: double = 1000.0
var discount = 1.0
discount = 0.8
```

`let` 绑定不可重新赋值；`var` 允许重新赋值。省略类型时进行静态类型推导。

## 控制流

```veyra
if order.amount <= 0
    return Decision.rejected("INVALID")
else if order.amount < 500
    return Decision.approved(1.0)
else
    return Decision.approved(0.9)
```

条件表达式必须是 `bool`，不支持把数字、字符串或对象隐式当作布尔值。MVP 先支持 `if`、`else if`、`else` 和 `return`；循环、模式匹配和 lambda 暂不纳入。

## 表达式

```text
算术：+ - * / %
比较：== != > >= < <=
逻辑：&& || !
空值：?. ??
成员：object.property
调用：name(argument, named: argument)
```

支持数字、字符串、布尔值和 `null` 字面量。运算符不做隐式类型转换。

## 宿主访问和动作

```veyra
let total = order.amount
let available = inventory.available(order.productId)
emit inventory.reserve(order.productId, quantity: order.quantity)
```

普通调用表示宿主注册的查询能力；`emit` 表示产生宿主注册的命令。命令参数支持位置参数和命名参数，位置参数必须在命名参数之前。

## 空值语法糖

```veyra
let city = user.address?.city ?? "未知"
```

`?.` 和 `??` 的展开规则、可空类型传播规则将在类型系统实现前锁定。
