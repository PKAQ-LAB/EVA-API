# 安全能力组合独立验收

验收日期：2026-10-07。执行角色：quality_reviewer。仓库分支：cornerstone。

## 验证结果

执行 `gradle.bat test build --no-daemon`，结果为 `BUILD SUCCESSFUL in 4m 7s`，59 个任务，45 个执行、14 个已是最新。

项目没有 `gradlew.bat`，初次 wrapper 命令未找到，随后使用环境中已安装的 Gradle；此项属于命令环境问题，不是测试失败。

本轮 68 份 `build/test-results/test/TEST-*.xml` 合计 183 个测试，失败 0、错误 0、跳过 0。报告文件修改时间全部位于本轮 20:39:26 至 20:43:04，未混入更早的 XML。

| 模块 | 测试数 |
| --- | ---: |
| eva-core-cache | 2 |
| eva-core-common | 9 |
| eva-core-data-mybatis | 22 |
| eva-core-log | 6 |
| eva-core-upload | 1 |
| eva-web-auth | 41 |
| eva-web-core | 9 |
| eva-web-sys-service | 40 |
| web-booter | 53 |

编译仍有既有 unchecked/deprecation 与 Gradle 插件弃用警告，本轮没有构建错误。

## 请求级证据

`WebSecurityCapabilitiesTest` 的 2 个测试通过，使用实际 `WebSecurityConfig`、Spring Security 过滤链和 `JwtAuthFilter`，通过 MockMvc 发起请求。

| 配置及身份 | 请求 | 实际断言结果 |
| --- | --- | --- |
| 认证关闭，显式公开路径 | GET /public/ping | HTTP 200，内容 ok |
| 认证关闭，未声明公开 | GET /sys/user/list | HTTP 200，业务 success=false |
| 认证关闭，即使匿名配置包含 /auth/** | GET /auth/getAlpha | HTTP 200，业务 success=false |
| 认证开启，没有 Token | GET /sys/user/list | HTTP 200，业务 success=false |
| 认证开启，有效身份测试替身 | GET /sys/user/list | HTTP 200，内容 ok |

拒绝响应使用项目既有 HTTP 200 加业务失败结构，不能把 HTTP 200 单独解释为获得业务访问权限。测试同时确认请求结束后没有遗留 `ThreadUser` 作用域。

这些请求测试中的 JWT 校验、账号查询、租户解析与会话依赖使用 mock；它们验证真实过滤链的分支和响应，不能作为真实密钥签名、Redis 会话或完整用户登录端到端联调的证明。

## 数据权限证据

独立新增 `DataPermissionCompositionReviewTest`，5 个测试全部通过。调用实际 `DataPermissionInterceptor.beforeQuery` 改写 MyBatis `BoundSql`，再调用实际 `PaginationInnerInterceptor.autoCountSql` 生成总数查询。

原始查询：

```sql
SELECT biz.ID FROM BIZ_ORDER biz WHERE biz.DELETED = 0
```

资源权限关闭、数据权限开启且当前用户为 11、范围为本人时，改写结果包含原有 `biz.DELETED = 0` 和以下追加条件：

```sql
(biz.CREATE_ID = 11 OR biz.MODIFY_ID = 11)
```

验收确认：资源权限关闭不阻止数据条件注入；数据权限关闭时原 SQL 不变且插件链不包含数据权限拦截器；已认证用户没有范围或范围编码未知时注入 `1 = 0`；数据权限插件位于分页插件之前，总数 SQL 与数据 SQL 均保留同一范围。

该独立测试验证 SQL 生成，不执行该 SQL 对真实业务表的查询，不能替代真实数据集下分页总数与记录集合的联调。

## 全量回归的数据库范围与已观察副作用

没有启动监听端口的用户应用服务，也没有执行手工数据库清理、结构调整或部署操作。

但全量 `web-booter` 既有 SpringBootTest 使用 `src/test/resources/application-dev.yaml` 配置的外部开发 PostgreSQL。测试 XML 记录了 Hikari 成功建立连接，不能声称本轮没有连接开发数据库。该外部库测试配置 `spring.flyway.enabled=false`，不会由应用自动执行 Flyway 迁移。

既有 `*PostgresTest` 中部分测试使用 `EmbeddedPostgres` 创建隔离数据库，并在那里创建 schema、表及执行迁移；这些迁移不能归为对外部开发库执行迁移。

OrganizationTree、ModuleTree、Notice、BusinessLog 等既有事务集成测试声明了 `@Transactional`。继承 `BaseTest` 的 Controller 测试没有统一测试事务：本轮 `ModuleCtrlTest` 执行 `POST /sys/module/frozen`（id=1、frozen=1），XML 中可见查询 id=1，并明确出现 `INSERT INTO log_biz`，记录匿名冻结操作的业务日志。该测试的日志中未观察到相应模块 UPDATE，因此不能证明 id=1 的模块状态发生变化；同时不能声称外部开发库完全无写入。其他 Controller 查询也可能通过业务日志切面写日志，本轮没有实施日志删除或任何补偿性数据库修改。

## 保留边界

- 注册按用户要求暂缓，未增加注册端点和虚假的注册开关。
- 关闭安全能力是运行配置组合，不代表数据库、Redis 和系统业务 Bean 已从构建依赖中移除。
- 本人范围包含创建者及修改者；后台任务无用户上下文时不注入用户数据权限。以上既有行为保持不变。
- 纯认证仍校验账号状态、权限版本与会话；平台模式仍加载可信角色来计算跨租户查看能力。
- Jev 为 disabled，本轮未调用云端决策。
- 本轮未提交、推送、创建 PR；原有 GO_REFACTOR_PLAN.md 未触碰。
