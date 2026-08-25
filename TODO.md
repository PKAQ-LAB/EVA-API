# EVA-API 未完成任务

> 2026-08-25 基于 GitNexus 全量索引（3,851 个节点、8,734 条关系、146 个功能簇、300 条执行流）、现有代码审查报告和测试结果复核。

## P0 当前版本交付

1. 修正 `web-booter` 中仍调用旧路由、旧请求结构和旧响应断言的 Controller 集成测试，确保 `gradle test` 全仓通过。
2. 补充 `RequestFilter` 的 Spring 容器级集成测试，覆盖 `eva.cloud.enable` 开关、错误和正确网关请求头、403 不触发二次错误派发以及 CORS 预检。

## P1 核心功能缺口

1. 实现 MyBatis-Plus 细粒度数据权限拦截器，并覆盖本人、本部门、部门及下级、指定部门和全部数据范围；删除当前整文件注释的占位实现。
2. 完成树节点移动或重命名后所有后代节点 `path`、`path_name` 和父节点名称的级联刷新，并补充跨层级移动回归测试。
3. 落地系统日志进入 Loki、业务日志进入 MongoDB 的存储、查询、保留、重试和降级方案。
4. 实现或删除 `BizlogJdbcSupporter.save` 的空持久化方法，并清理其中的乱码 TODO。
5. 实现、删除或明确约束 `MinIOFileUtil` 中的空回调方法。
6. 完成基于 schema 的租户创建、迁移、上下文切换、异步传播、任务调度、归档和删除方案。
7. 统一 Module、Resource、租户套餐资源、租户资源和角色资源的生命周期及引用校验。
8. 将通知模块的演示 JSON 替换为真实的 Bo、Vo、Service 和数据源实现。

## P2 工程质量

1. 为 ModuleService、OrganizationService、PostService、RoleService、TenantService、TenantPackageService 和 UserService 补齐单元测试。
2. 为所有 sys Convert 补齐字段级单元测试。
3. 统一字典明细、模块资源、角色用户、角色资源、岗位用户、租户资源和套餐资源的差异更新模式。
4. 统一枚举业务编码、MapStruct 转换、MyBatis TypeHandler 和 JSON 序列化，禁止使用枚举序号作为业务编码，并补齐未知业务码测试。
5. 统一错误码前缀、编码唯一性、国际化资源和参数占位符。
6. 清理 common 模块的废弃 API、乱码注释、重复工具类、原始泛型和未检查转换。
7. 修正过期的 `docs/CODE_REVIEW_REPORT.md`，为已解决项标记对应提交，并重新验证未解决项的文件和行号。
8. 明确 APISIX 替换范围、路由、鉴权、网关请求头注入与剥离、限流、熔断、灰度和回滚方案。
9. 为 GitNexus 安装 LadybugDB FTS 扩展并运行 `gitnexus analyze --repair-fts`，恢复 BM25 全文检索。
