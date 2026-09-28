# 类型系统草案

## 目标

类型系统服务于业务规则的提前校验，不追求成为完整的通用 JVM 类型系统。脚本类型必须足以表达计算、条件、宿主属性和宿主能力调用。

## 内置类型

首版候选类型：

```text
bool
int
long
double
string
```

`null` 不是所有类型的默认值。可空类型用 `?` 表示，例如 `User?`、`string?`。

## 推导和赋值

```veyra
let amount = order.amount       // 从宿主契约推导
let rate: double = 0.8          // 显式类型
var discount = 1.0
```

赋值要求类型兼容；MVP 不进行隐式字符串转数字、数字转布尔等转换。数值提升规则需要在实现前单独确定并测试。

## 宿主类型

脚本可以引用由 `HostContext` 注册的 Java 类型简称。未注册的类型即使存在于 classpath，也不能被脚本使用。

## 类型检查边界

- `if` 条件必须为 `bool`。
- 属性链的每一级都必须存在并且可访问。
- 调用参数数量、顺序、命名和类型必须匹配宿主契约。
- `function` 不得产生 `emit` 副作用。
- `rule` 的返回值必须符合入口契约。

## 待决事项

- Java `int`、`long`、`double` 的精确映射和数值提升。
- record accessor、getter、公共字段的优先级。
- 可空类型与 Java 平台类型的映射。
- 集合、日期和枚举的首版支持范围。
