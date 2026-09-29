# 路线图

## M0：设计基线

- [x] 确定 Veyra 名称和 JVM 嵌入式定位
- [x] 确定 `.veyra` 源文件后缀
- [x] 确定宿主契约检查为核心机制
- [x] 初步确定 `rule`、`function`、`let`、`var`、`emit` 语法
- [x] 建立设计文档和决策记录

## M1：语言前端 MVP

- [ ] Java 21 项目骨架和构建脚本
- [ ] Lexer：关键字、字面量、运算符、换行、`INDENT`、`DEDENT`
- [ ] Parser 和 AST
- [ ] 变量作用域与基础类型检查
- [ ] 词法、缩进和解析测试

## M2：宿主契约与解释执行

- [ ] `HostContext` 类型注册
- [ ] Java 属性访问检查
- [ ] 查询和命令契约
- [ ] 解释器和命令计划结果
- [ ] 订单定价/审批示例

## M3：可用性

- [ ] 脚本热加载
- [ ] 沙箱和执行预算
- [ ] 友好诊断与错误建议
- [ ] 与 Aviator、QLExpress 等方案的真实案例对比

## M4：性能与工具

- [ ] JVM 字节码后端
- [ ] Java API、Gradle/Maven 集成
- [ ] IntelliJ IDEA 插件
- [ ] 文档、示例和兼容性测试
