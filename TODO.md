# EVA-API 未完成任务

> 2026-09-13 基于 GitNexus 全量索引、现有代码审查报告和测试结果复核。

## P0
1. 统一错误码前缀、编码唯一性、国际化资源和参数占位符。
2. 清理 common 模块的废弃 API、乱码注释、重复工具类、原始泛型和未检查转换。
3. 修正过期的 `docs/CODE_REVIEW_REPORT.md`，为已解决项标记对应提交，并重新验证未解决项的文件和行号。
1. 统一 Module、Resource、租户套餐资源、租户资源和角色资源的生命周期及引用校验。
3. 1. 为 ModuleService、OrganizationService、PostService、RoleService、TenantService、TenantPackageService 和 UserService 补齐单元测试。
2. 为除已覆盖 `DictConvert` 外的 sys Convert 补齐字段级单元测试。
3. 统一字典明细、模块资源、角色用户、角色资源、岗位用户、租户资源和套餐资源的差异更新模式。
4. 统一枚举业务编码、MapStruct 转换、MyBatis TypeHandler 和 JSON 序列化，禁止使用枚举序号作为业务编码，并补齐未知业务码测试。

## P1 核心功能缺口
2. 将通知模块的演示 JSON 替换为真实的 Bo、Vo、Service 和数据源实现。
4. 部署 Alloy 采集应用文件日志到 Loki，并配置查询、保留和告警规则。
5. 完成基于 schema 的租户创建、迁移、上下文切换、异步传播、任务调度、归档和删除方案。
6. 完成生产可用的 TOTP 双因素认证，预计后端 8–12 人日、前端 4–6 人日、测试与联调 3–5 人日：
   - 新增独立的用户 MFA 表和 Flyway 迁移，必须加密保存 TOTP Secret，并仅保存恢复码摘要。
   - 将登录改造成密码认证与 TOTP 验证两个阶段，第一阶段必须仅签发 3–5 分钟有效且一次性消费的 challengeToken，禁止提前签发 Access Token 或 Refresh Token。
   - 新增 MFA 状态查询、绑定、确认启用、登录验证、关闭、恢复码登录和恢复码重新生成接口。
   - 使用 Redis 保存登录挑战、失败次数和已使用时间窗口，必须实现过期、限流、防重放和成功后立即删除。
   - MFA 关闭、重置或恢复码重新生成时必须重新验证身份，并清除该用户现有 Token。
   - 登录日志和认证错误码必须覆盖 MFA 成功、失败、锁定、恢复和重置场景，响应不得泄露账号是否启用 MFA。
   - Refresh Token 必须仅在 MFA 验证成功后签发，正常刷新时不得重复要求 TOTP。
   - 必须补齐 TOTP 时间窗口、challengeToken 过期与重放、失败限流、恢复码一次性消费、Token 签发边界和并发验证测试。

## P2 工程质量
5. 明确 APISIX 替换范围、路由、鉴权、网关请求头注入与剥离、限流、熔断、灰度和回滚方案。
