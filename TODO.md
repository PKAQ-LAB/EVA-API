# EVA-API 未完成任务

## P0 当前版本交付

1. 提交并复核当前工作区中的 MyBatis-Plus 适配、字典 MapStruct、字典差异保存测试和网关请求校验改动。
2. 启动 `web-booter`，验证 `eva.cloud.enable` 关闭、开启、错误请求头、正确请求头和 CORS 预检场景。
3. 验证字典启动预加载、事务提交后缓存刷新、字典编辑和明细差异保存。

## P1 核心功能缺口

1. 实现 MyBatis-Plus 细粒度数据权限拦截器，并覆盖本人、本部门、部门及下级、指定部门和全部数据范围。
2. 完成树节点移动或重命名后所有子节点 `path` 和 `path_name` 的级联刷新。
3. 落地系统日志进入 Loki、业务日志进入 MongoDB 的存储、查询、保留、重试和降级方案。
4. 实现或删除 `BizlogJdbcSupporter` 的空持久化方法。
5. 实现、删除或明确说明 `MinIOFileUtil` 中的空回调方法。
6. 完成基于 schema 的租户创建、迁移、上下文切换、异步传播、任务调度、归档和删除方案。
7. 统一 Module、Resource、租户套餐资源、租户资源和角色资源的生命周期及引用校验。
8. 将通知模块的演示 JSON 替换为真实的 Bo、Vo、Service 和数据源实现。

## P2 工程质量

1. 为 ModuleService、OrganizationService、PostService、RoleService、TenantService、TenantPackageService 和 UserService 补齐单元测试。
2. 为所有 sys Convert 补齐字段级单元测试。
3. 统一字典明细、模块资源、角色用户、角色资源、岗位用户、租户资源和套餐资源的差异更新模式。
4. 统一枚举业务编码、MapStruct 转换、MyBatis TypeHandler 和 JSON 序列化，禁止使用枚举序号作为业务编码。
5. 统一错误码前缀、编码唯一性、国际化资源和参数占位符。
5. 清理 common 模块的废弃 API、乱码注释、重复工具类、原始泛型和未检查转换。
6. 明确 APISIX 替换范围、路由、鉴权、网关请求头注入与剥离、限流、熔断、灰度和回滚方案。
