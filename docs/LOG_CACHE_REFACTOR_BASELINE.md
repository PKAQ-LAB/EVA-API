# 日志与缓存重构基线

## 范围

- 业务日志继续使用 PostgreSQL 热表与归档表。
- 登录日志继续使用 PostgreSQL。
- 完整错误堆栈写入应用日志，由部署侧 Alloy 采集到 Loki。
- PostgreSQL 仅保存可聚合、可处置的错误事件摘要。
- Token、会话、权限、幂等及共享缓存统一使用 Redis。
- 删除 Caffeine 和不具备等价能力的日志 Supporter 切换配置。

## 修改前验证

执行日期：2026-09-22。

仓库未提供 Gradle Wrapper，使用本机 `gradle.bat` 9.5.1 执行：

```powershell
gradle.bat :eva-core:eva-core-log:test `
  :eva-core:eva-core-cache:test `
  :eva-core:eva-core-data-mybatis:test `
  :eva-web:eva-web-auth:test `
  :eva-web:eva-web-sys:eva-web-sys-service:test `
  :web-booter:test --no-daemon
```

结果：`BUILD SUCCESSFUL`，共 37 个任务，均为 `UP-TO-DATE`。

## 验收边界

1. standalone 与 schema-only 模式都必须显式依赖 Redis。
2. 安全状态不得降级到节点本地缓存。
3. 角色权限缓存键必须包含可信 schema 范围。
4. 防重复提交必须使用 Redis 原子写入与过期时间。
5. Loki、Alloy 和 Grafana 由部署侧提供，应用不得引入 Loki 客户端依赖。
6. 完整异常堆栈不得继续作为错误事件表的重复主体数据。
