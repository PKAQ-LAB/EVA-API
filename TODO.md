# EVA-API 未完成任务

> 2026-09-10 基于 GitNexus 全量索引、现有代码审查报告和测试结果复核。

## P0
1. 统一错误码前缀、编码唯一性、国际化资源和参数占位符。
2. 清理 common 模块的废弃 API、乱码注释、重复工具类、原始泛型和未检查转换。
3. 修正过期的 `docs/CODE_REVIEW_REPORT.md`，为已解决项标记对应提交，并重新验证未解决项的文件和行号。

## P1 核心功能缺口
1. 统一 Module、Resource、租户套餐资源、租户资源和角色资源的生命周期及引用校验。
2. 将通知模块的演示 JSON 替换为真实的 Bo、Vo、Service 和数据源实现。
3. 实现 MyBatis-Plus 细粒度数据权限拦截器，并覆盖本人、本部门、部门及下级、指定部门和全部数据范围；删除当前整文件注释的占位实现。
4. 落地系统日志进入 Loki、业务日志进入 MongoDB 的存储、查询、保留、重试和降级方案。
5. 完成基于 schema 的租户创建、迁移、上下文切换、异步传播、任务调度、归档和删除方案。

## P2 工程质量
1. 为 ModuleService、OrganizationService、PostService、RoleService、TenantService、TenantPackageService 和 UserService 补齐单元测试。
2. 为除已覆盖 `DictConvert` 外的 sys Convert 补齐字段级单元测试。
3. 统一字典明细、模块资源、角色用户、角色资源、岗位用户、租户资源和套餐资源的差异更新模式。
4. 统一枚举业务编码、MapStruct 转换、MyBatis TypeHandler 和 JSON 序列化，禁止使用枚举序号作为业务编码，并补齐未知业务码测试。
5. 明确 APISIX 替换范围、路由、鉴权、网关请求头注入与剥离、限流、熔断、灰度和回滚方案。
