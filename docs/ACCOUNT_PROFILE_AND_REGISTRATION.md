# 账号、管理资料与注册

## 当前代码分析与变更范围

原 `SYS_USER` 同时保存密码、登录标识和部门资料。直接复用管理端新增用户来实现注册，会把普通互联网账号耦合到组织、岗位和角色管理。此次将底层账号创建规则下沉到公共契约和 MyBatis 适配器，认证不反向依赖系统管理模块。

| 对象 | 职责 | 本次处理 |
| --- | --- | --- |
| `SYS_ACCOUNT` | 账号、密码哈希、昵称、头像、联系信息、冻结状态、权限版本和审计 | 原 `SYS_USER` 重命名，原 ID 不变 |
| `SYS_ACCOUNT_PROFILE` | 可选工号、姓名、部门 | `ACCOUNT_ID` 主键且外键引用账号；无密码，无重复冻结或删除状态 |
| 用户管理 BO/VO | 管理端组合视图 | 保留现有 HTTP 接口；账号 LEFT JOIN 可选资料 |
| 认证 SPI | 查询账号及可选权限资料 | 纯认证不读取角色或资料表；权限上下文需要时才查询 |
| 租户初始化 | 租户 schema、只读根组织、管理员和资料 | 管理员绑定根组织；standalone 不执行租户管理 |
| 数据权限 | 本人、部门及部门树范围 | 创建人/修改人仍是账号 ID；部门条件关联资料及未删除账号 |

角色、岗位、登录日志和业务记录中的用户 ID 继续指向同一账号 ID，不重新生成 ID。现有管理端新增和自助注册使用同一个 `IAccountCreation`，复用保留账号校验、唯一性检查和 BCrypt 哈希；部门、岗位、角色仅由管理用例添加。

## 数据库升级

- 已有 standalone/平台库执行 `db/migration/V12__ACCOUNT_PROFILE_SPLIT.sql`，同时升级平台登记的既有租户 schema，不扫描无关 schema。
- 新租户顺序执行租户 V1、V2；版本 2 完成后不再执行 V1，以免重新生成 `SYS_USER`。
- 原始 V1 等已发布迁移保持不变，不修改旧迁移的校验和。
- 旧表与新表重叠、资料复制数量不一致时迁移失败，必须排查，不覆盖已有表。
- 数据迁移与新应用必须一起部署；旧应用仍查询 `SYS_USER`，不能在新结构上继续运行。
- `docs/sql/system_management_full_postgresql.sql` 仅用于新建 standalone 数据库。不得对已有库重跑管理员初始化或覆盖密码。

开发库可使用 `scripts/AccountProfileMigration.java inspect` 检查。`migrate` 在同一事务中锁定旧表，复制完整旧账号表至禁止 PUBLIC 访问的时间戳备份 schema，再执行 V12 并校验后提交。备份含敏感密码哈希，禁止导出到仓库、日志或公共目录；它不是全库备份。失败不提交，原结构不变。

开发配置当前关闭 Flyway，工具不伪造 Flyway 历史。以后启用 Flyway 时必须核对实际结构与历史，并制定对应 baseline，不得在已手动升级的数据库上重新运行初始化。生产环境使用正式备份、维护窗口和迁移流程，不使用此开发工具。

## 注册首期边界

注册默认关闭，通过 `eva.auth.registration.enabled` 或 `EVA_AUTH_REGISTRATION_ENABLED` 控制。仅 standalone、未开启租户且认证和 JWT 机制有效时可用。关闭时即使 anonymous 配置包含 `/auth/**` 也不能开放注册。

JWT 与 OpenAPI 过滤器的路径回退解析已修复：`servletPath` 为空时只去掉真实 context-path 路径段，`/api/auth/register` 对应 `/auth/register`，不会将 `/apix` 当作 `/api` 剥离。仅精确 POST 注册路径在有效开启时允许跳过残留认证凭据。

首期仅接收账号、密码和昵称，不接收管理员状态、角色、部门、岗位、租户、权限版本或客户端提供的 ID。不创建资料和权限，不返回 Token、不自动登录。开启数据权限时，没有数据范围的账号继续默认拒绝查询。

账号为 3–64 位 ASCII 用户名，首位字母或数字，其余仅允许字母、数字、`_`、`.`、`-`；沿用管理端保留账号规则。密码至少 8 个 Unicode 字符，最多 72 个 UTF-8 字节，禁止空白密码和控制字符。昵称可省略，最大 64 字符。未知字段直接安全拒绝，不将字段值回显。

接口为 `POST /api/auth/register`，成功后使用现有登录接口登录。租户成员创建、邀请、邮箱/短信验证、密码找回、MFA/2FAS 不在本次注册范围。

本次未新增注册限流或验证码；面向公网开启前必须补充防滥用措施，并使用 HTTPS。默认关闭不是公网防滥用方案。

注册密码不得写入 JSON 响应、对象日志、错误日志或业务日志；数据库只保存哈希。重复账号既要业务预检查，也要依赖数据库唯一索引处理并发争用。

### 现有密码协议兼容

现有前端登录和管理端新增用户先提交 `MD5(原始密码)`，后端保存并验证 `BCrypt(MD5)`。注册接口接收原始密码，在服务端校验后转为同一摘要，再调用公共创建服务进行一次 BCrypt。不能直接保存 `BCrypt(原始密码)`，否则现有登录页面会认证失败。

MD5 是保留现有协议的兼容步骤，不是新增安全保障；原始密码与该摘要都必须通过 HTTPS 传输。不得自动探测字符串是否像 MD5，也不得在旧登录接口同时尝试多种密码形式。若以后删除客户端 MD5，必须单独设计带版本的凭据迁移，不能静默改变已有账号。

### 开发库执行记录

2026-10-08 已对开发配置指向的 PostgreSQL `eva` 数据库、`eva` schema 执行 V12。切换前未发现本机 9016 服务监听。原账号 1 条，迁移后账号 1 条、资料 1 条；逐值检查账号全部保留字段、ID、密码哈希及三项管理资料一致。

受限备份 schema 为 `eva_account_backup_20261008_145858`，其中表 `eva` 保存完整旧用户表。此备份只用于受控恢复参考，不得直接覆盖上线后产生的新数据，也不得自动删除；后续确认恢复策略后再清理。

## 验证记录

测试使用隔离 PostgreSQL 和显式选择的测试类，不允许全量 `web-booter:test` 连接开发库写测试记录；构建通过不代替注册请求或数据库行为验收。

```powershell
$env:GRADLE_USER_HOME = 'C:\home\DevApp\cache\gradle'
gradle.bat :eva-core:eva-core-common:test :eva-core:eva-core-contract:test :eva-core:eva-core-cache:test :eva-core:eva-core-data-mybatis:test :eva-core:eva-core-log:test :eva-web:eva-web-auth:test :eva-web:eva-web-sys:eva-web-sys-service:test --no-daemon
gradle.bat :web-booter:test --tests org.pkaq.sys.user.AccountProfileSplitMigrationPostgresTest --tests org.pkaq.sys.user.UserAccountProfileMapperPostgresTest --tests org.pkaq.sys.tenant.service.TenantPrivateAccountSchemaPostgresTest --tests org.pkaq.core.mybatis.tenant.TenantSchemaModePostgresTest --tests org.pkaq.sys.user.RegistrationAccountPostgresTest --no-daemon
gradle.bat build -x test --no-daemon
```

2026-10-08 模块专项 186 项通过，失败、错误、跳过均为 0；contract 模块无测试源码。认证最终 89 项（16:02:56 报告）复跑通过，包括 AuthUserEntity、JwtUserDetail 密码哈希 JSON/toString 脱敏和 getter 不变检查；其余 common 9、cache 2、data-mybatis 28、log 7、sys-service 51 项为本阶段已经通过的结果，最终命令中由 Gradle 复用。最后凭据注解补丁的实际命令为 `gradle.bat :eva-web:eva-web-auth:test --no-daemon`。

初轮失败中，重复控制器注入和未指定 standalone 属于测试配置问题；随后发现两过滤器的 context-path 回退缺陷，已修复生产解析并增加相似前缀、禁用注册和真实 `/api` 请求门控测试。

PostgreSQL 专项 5 类共 17 项最终通过（15:58:19 报告），失败、错误、跳过均为 0。本次模块及数据库专项合计 203 项。覆盖 V12/V2 迁移保留 ID/哈希/外键、幂等执行、失败整体回滚、登记租户升级、无关 schema 不变、真实用户 Mapper 联表及资料清空、租户根组织和管理员创建。

注册运行证据：MockMvc 向真实注册控制器发送 POST，真实 Service/MyBatis 写入隔离 PostgreSQL；未知角色/租户/部门字段返回 400 且账号数不增加；数据库哈希匹配现有 MD5 登录协议，真实 LoginAuthenticationProvider 认证成功；业务权限为空、无角色、没有资料表也能认证；数据范围为空时 SQL 为 `1 = 0`。并发屏障模拟两个预检查均通过的竞争，真实数据库唯一索引仅允许一条账号写入。

首次注册集成断言误将 Spring Security 自动附加的 `FACTOR_PASSWORD` 等同业务角色，已按框架语义改为验证只有密码认证因子、Principal 业务权限为空且没有角色。这不是新增 MFA/2FAS。

证据边界：注册 MockMvc 使用 standaloneSetup，完整 SecurityFilterChain 由独立认证测试覆盖；真实登录认证提供器为 Java 调用，不是 HTTP 登录端到端。并发测试没有经过 Spring 事务代理，管理资料多表写入失败的完整代理事务回滚、全部业务端到端发布验收仍须另补。未启动应用服务或浏览器，也未向开发库写入测试注册账号。

实现验收阶段的最终 `gradle.bat build -x test --no-daemon` 成功（21 秒，40 个任务，4 个执行）；源码差异检查通过。当时未提交、推送或创建 PR，前端未修改；原未跟踪 `GO_REFACTOR_PLAN.md` 保留。

随后按用户“分步提交 push”授权发布至 `origin/cornerstone`：账号拆分与迁移为 `37254881`，注册及安全适配为 `3db0a92c`，说明与验收文档另行提交。发布前模块测试命令成功（Gradle 复用结果），203 项 XML 报告均无失败、错误或跳过，构建再次成功（15 秒）。本次只移除两个新迁移文件末尾的多余空行，未改变 SQL 行为；未重新执行 PostgreSQL 专项或开发库迁移。

GitNexus 索引落后当前提交，本次变更图仍标记核心账号及权限链路为高影响；不能把旧索引结果或未纳入索引的新文件视为完整验收。本次以逐文件调用关系核对、源码扫描及专项运行结果补充。
