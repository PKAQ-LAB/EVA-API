# 历史登录日志集中实现独立源码审查

结论：冻结范围内未发现可复现的源码阻断，通过独立静态审查，可交 QA 独立复验。本结论不代替 QA 签审、完整构建或真实数据库验证，也不授权部署或迁移。

## 冻结对象

2026-10-30 实核 `.gradle/login-log-20261030/manifest.json` SHA256 为 `D582E2130B00A9799DB4361A61496A43A4D04E9E602E283209AF64BADE090ABB`，其中 sources 所列 7 个生产文件、4 个测试文件逐项重新计算 SHA 全部一致。基准 HEAD 为 `ade2f9185983a94b8792ab9e003a9b3d9ab343c6`。本结论仅绑定该清单，后续改源须重新核定。

## 源码结论

LoginLogReadAccess 在任何权限开关前检查可信主体、运行模式及 target；standalone 只允许 tenant0，schema 平台全局只允许 tenant0，并在受信 core 物理关系上使用既有有效账号—ROLE_ADMIN—membership 查询。正租户经 core SYS_TENANT 和 TrustedTenantSchemaResolver/租户认证路由核真实私有 schema，在实际私有账号、受保护 TENANT_ADMIN 角色及 membership 上判定，账号 deleted/frozen、角色 deleted/frozen 均显式约束。rolesMap 的 ROLE_ADMIN 不提供全局资格，没有 core legacy 角色 OR 兜底。资源开关开启仍额外检查实际 GET 路径。

列表 null/本租户 target 和详情使用相同可信租户过滤；平台 null 为全局，指定非负 target 只缩小，负数及正租户外部 target 拒绝。Provider 每次 BoundSql 构建重新核固定窗口，数值租户条件来自窗口而不是 query.targetTenantId；分页 count 从相同已固定 SQL 派生，未增加可直接调用的 count Mapper。data=false 不影响 Provider 门禁。通用 data handler 仅豁免确切 history/detail/page/派生 count 或系统风险及 update 方法，并要求对应窗口，没有全局 Ignore、全 Mapper select 放行或 ALL 扩权。

LoginLogStorage 是独立 Spring bean，调用者经其代理启动 REQUIRES_NEW；读独立 readonly，写不共用调用者事务。schema 使用 CoreSchemaExecutor 将审计读写切到 core；standalone 只验证配置默认物理源，不主动改源。current_schema 与 to_regclass 实际 relation namespace 同时校验，缺表、临时表或 search_path 回退拒绝，不建表、不迁移或搜索旧 schema。资格路由退出恢复 core 后才进入历史窗口；CoreSchemaExecutor、TenantAuthRoutingService 和存储窗口均恢复原 search_path/TenantContext，scope finally 恢复先前窗口并校验主体对象及 id/tenant 未变。

成功登录的 risk 两查询与 insert 使用一个存储动作；注销 close 与 insert 一个动作；独立 close/rotate/closeAll 各一个。风险 Provider 只使用 system scope，不要求人类管理员；system scope 不能替代 history scope。会话 update 保留可信 tenant/user/session 与 PASSWORD/未注销限制。已检索的生产调用点均经集中存储或历史门禁，无遗留 BaseMapper 历史读取调用。审计事务异常包括代理提交异常由 LoginLogService 外层捕获；已批准的独立提交、额外连接和业务回滚不撤审计行为保留。

## 测试证据与边界

已逐份只读审查 4 个测试类。它们覆盖权限开关组合、普通及伪 ROLE_ADMIN、平台/私有资格、外租户/负 target、资源拒绝、无映射拒绝、Provider 重复 BoundSql、真实 MP 自动 count 同范围、精确 data handler 窗口、嵌套异常与身份变更、成功/注销动作粒度、UA 解析及当前身份注销。StorageTest 使用真实 Spring 事务代理、真实 CoreSchemaExecutor 与合成 JDBC/事务管理器，覆盖新事务、readonly、来源回退、上下文恢复、实际代理提交失败及调用者回滚后审计独立提交。

只读核对 `test-4.log` 尾部为 30 found/30 successful/0 failed/0 skipped；这是后端已有执行证据，本角色未执行测试。readAccess 的角色/路由依赖使用 mock，未实际执行 PostgreSQL 角色 SQL；分页 Executor、JDBC 与事务状态仍为合成，不能从这些测试推出真实 PG、连接池容量、部署实例或 HTTP 已通过。后端局部 javac 依赖旧产品快照，不能当作完整 Gradle 构建。旧日志不迁移、多 schema 历史不保证完整的交付限制仍成立。

本角色仅写此设计及本人状态卡，未改生产/测试/辅助源，未运行测试、Gradle、DB、PG、HTTP、UI 或服务操作；C3 保持 USER_SKIPPED。
