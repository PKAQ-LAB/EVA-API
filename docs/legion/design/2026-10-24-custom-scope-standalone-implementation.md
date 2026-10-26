# CUSTOM 0003 standalone 归属校验修复交付

2026-10-24，backend_developer；cornerstone；总控独立会话正式源码写锁范围内实施。本文记录代码与隔离测试，不代替实际 HTTP、页面或正租户验收。

## 变更及边界

依据 `2026-10-24-custom-scope-standalone-ownership.md` 和 `2026-10-24-package-s-custom-readonly.md`，只改 RoleService 的非空组织集合归属核验及必要构造依赖：

- standalone/singleton 才允许兼容历史 NULL/0；同时要求 tenant.enable=false、非 schema 模式，可信 ThreadUser.userId>0、tenantId=0。
- TenantContext 可完全不存在；存在任何一个字段时必须同时是 tenant0 和配置 standaloneSchema，矛盾或部分上下文直接拒绝，不修改或清除它。
- 配置 schema 必须匹配 `[a-z_][a-z0-9_]{0,62}`；必须已有实际事务且 JdbcTemplate 的 DataSource 已绑定该事务。
- 同事务 JdbcTemplate 只读 PostgreSQL catalog，使用静态参数 `sys_organization`，要求 current_schema() 和该未限定关系实际解析到的 pg_namespace 都等于配置 schema；NULL、第三 schema、缺关系、异常均在组织查询及角色写入前失败。不 SET search_path，不查询跨 schema 组织数据。
- 原 OrganizationMapper.selectList 和实际数据插件、逻辑删除继续执行。仅 standalone 分支使用分组 `(tenant_id IS NULL OR tenant_id=0)`，仍 AND 全部正 ID；查询返回 ID 集合必须与提交集合完全相等。standalone 重复 ID 拒绝。
- platform/saas 沿用原 `tenant_id=ThreadUser.tenantId`，无 OR NULL；未知主 mode 在非空组织集合时拒绝。未改变正租户路由、接口、数据模型或插入约定。
- 未改全局 MetaObjectHandler、Org/Role Convert、字典/缓存、授权链或 permVer 实现；没有写 tenant0、回填七对象、迁移或创建 CUSTOM。

生产变更文件：`eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/role/service/RoleService.java`。原文件已有未提交的范围/冻结等变更，本次不把完整 HEAD diff 冒充本轮新增。新增 JdbcTemplate 是构造函数最后一个依赖，仓库唯一显式 new RoleService 的旧测试已适配，并把其旧的全 false mock mode 明确设为 saas；其原 14 项行为回归通过。

新增测试文件：同模块 `src/test/java/org/pkaq/sys/role/service/CustomScopeJdbcFixture.java`、`RoleCustomScopeStandaloneTest.java`；旧 `RoleServiceTest.java` 仅本轮适配构造依赖和正租户模式夹具。其余已有 dirty/OpenAPI/日志/日期等代码保持。

GitNexus 对 validateDataScope upstream 返回 target not found，影响结果 UNKNOWN，未宣称完整图覆盖，未重新索引。字面检索确认生产调用来自 editUniqueCode；先校验后唯一性检查/真实 MapStruct/写入/既有编辑 permVer。当前框架使用原 DataSource 的 Spring 事务和 MyBatis SpringManagedTransaction；未新增数据源、事务管理器或路由。

## 隔离测试性质与真实 SQL

新增夹具使用真实 RoleService、生成的 RoleConvertImpl、OrganizationMapper、MybatisPlusConfig 的实际插件、SqlSessionTemplate、Spring TransactionInterceptor 和 DataSourceTransactionManager；只有 JDBC、角色持久化及授权依赖是 mock，不连接实际数据库。每次 prepareStatement 断言实际事务存在、DataSource 绑定 ConnectionHolder、schema JDBC 与组织 Mapper 使用同一 Connection。

组织 JDBC 不直接返回预设 visibleIds：JSqlParser 解析插件完成后的实际 SQL WHERE，以实际绑定参数逐项过滤合成行，覆盖 AND/OR/NULL/IN/删除/创建者；不支持的谓词直接使测试失败。合成行包括 NULL、0、正tenant、deleted、他人创建及不在提交集合的独立99；最后一行用于捕获 OR 越过 ID 限制。所有 ID 都是测试合成值，非包 S 对象。

最终 XML 的 SYNTHETIC_BOUND_SQL 实际输出包括：

```sql
SELECT id FROM sys_organization WHERE deleted = 0
  AND (id IN (?, ?) AND (tenant_id IS NULL OR tenant_id = ?)) AND 1 = 1
-- JDBC_PARAMETERS={1=12, 2=13, 3=0}
```

dataPermission=false 时真实 SQL 同样保留 ID/逻辑删除/NULL0 分组；开启 creator scope 时由真实插件追加 CREATE_ID/MODIFY_ID=7，隐藏16整体拒绝。schema查询以参数化 to_regclass(?) 检查来源，不绕过该插件读取组织行。

48 项新增用例覆盖 NULL/0/混合成功、正tenant/删除/不存在/隐藏混入全拒、空/负/非数/溢出/重复 ID、身份缺失/非法、上下文冲突/部分存在、主模式/tenant flags、合法 singleton 与上下文、配置 schema 非法、current/实际关系 schema 不符或缺失、catalog异常、无实际事务/未绑定数据源、平台/saas严格原谓词、正租户列缺失异常不回退、写入异常rollback、CUSTOM编辑省略组织保留/冻结保留/合法后刷新版本、拒绝编辑零角色或版本写入。

其中校验失败的零写入由角色 insertOrUpdate 从未调用、用户/资源关联及 userMapper 完全未触达证明；rollback/commit 是 mock JDBC 上真实 Spring 事务调用的断言，不能将其声称为真实数据库提交/回退实验。正租户测试只验证原谓词不放宽及异常不兜底，不声称真实正租户 schema 路由或 V1 表兼容已验。

## 当前产物编译与哈希门禁

所有命令 cwd 为 `C:/home/workspace/lab/EVA-API`。未改依赖/版本或项目 build.gradle。使用既有 Gradle 与缓存，offline、no-daemon、-x jar，避开用户实例占用 JAR。

任务专用 init：`.gradle/custom-scope-current-20261024.gradle`。显式编译十个模块 classes/resources，依赖编译使用当前输出的独立副本，避免 Gradle 9 隐式产物依赖校验；测试 classpath 最前置全部十模块的新隔离快照。没有把旧 openapi snapshot 直接用于本修复。

```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' -I .gradle/custom-scope-current-20261024.gradle :eva-core:eva-core-contract:classes :eva-core:eva-core-common:classes :eva-core:eva-core-cache:classes :eva-core:eva-core-log:classes :eva-core:eva-core-data-mybatis:classes :eva-web:eva-web-core:classes :eva-web:eva-web-auth:classes :eva-core:eva-core-upload:classes :eva-web:eva-web-sys:eva-web-sys-domain:classes :eva-web:eva-web-sys:eva-web-sys-service:classes --offline --no-daemon --console=plain -x jar
& ./.gradle/custom-scope-snapshot-20261024.ps1
```

编译成功日志 `.gradle/custom-scope-compile3-20261024.log`。前两轮失败保留：第一轮 Gradle 产物依赖校验；第二轮发现 lambda scope 与局部变量同名，已改 ownership，不能以早期失败日志充当通过。随后新增编辑测试曾因夹具缺少 RoleUserEntity lambda元数据失败，补齐夹具后最终全通过，不改生产代码掩盖失败。

新快照 `.gradle/custom-scope-current-snapshot-20261024-a` 包含 **465 个类文件和27个资源，合计492文件**，全量 SHA256 manifest，不仅抽查 RoleService。脚本先校验源输出/复制字节相同；每个最终 Test.doFirst 再校验全部当前输出与副本及文件清单相同，记录 `CUSTOM_CURRENT_ALL_OUTPUT_HASHES_VERIFIED=492`。资源来自各模块已执行 processResources 的 build/resources/main。

- manifest SHA256：`A5853E84F152E9C952E60217C01BC2F44BC4B89CC4685A28149ED856467DDEFE`。
- 当前 RoleService.class SHA256：`80DFFA86998E324D80068B20F9588D71D5C35B8A849DA0530E86441E5C01E30C`；本机 LastWriteTime 2026-10-24 13:29:47。
- snapshot、init、日志、XML 位于忽略的 .gradle 下，不提交或发布。

## 最终测试命令及 XML 计数

```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' -I .gradle/custom-scope-current-20261024.gradle :eva-web:eva-web-sys:eva-web-sys-service:test --tests org.pkaq.sys.role.service.RoleCustomScopeStandaloneTest --tests org.pkaq.sys.role.service.RoleServiceTest --tests org.pkaq.sys.RoleFrozenMappingTest --tests org.pkaq.sys.AccountTenantFrozenMappingTest --tests org.pkaq.sys.TreeFrozenMappingTest --tests org.pkaq.sys.TreeFrozenEditTest --tests org.pkaq.sys.WriteProtectionServiceTest --offline --no-daemon --console=plain -x jar
& 'C:/home/DevApp/gradle/bin/gradle.bat' -I .gradle/custom-scope-current-20261024.gradle :eva-web:eva-web-auth:test --tests 'org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAudit*Test' :eva-core:eva-core-data-mybatis:test --tests org.pkaq.config.OpenApiAuditQueryPermissionTest --tests org.pkaq.config.MybatisPlusDataPermissionHandlerTest --tests org.pkaq.config.LogQueryPermissionInterceptorTest --offline --no-daemon --console=plain -x jar
```

最终日志分别 `.gradle/custom-scope-regression-final2-20261024.log`（BUILD SUCCESSFUL 31s）及 `.gradle/custom-scope-guard-regression-20261024.log`（BUILD SUCCESSFUL 28s）。按最终三个模块 TEST-*.xml 读取，不重复累加先前试跑：

| 测试类 | 数量 |
| --- | ---: |
| RoleCustomScopeStandaloneTest | 48 |
| RoleServiceTest | 14 |
| AccountTenantFrozenMappingTest | 4 |
| RoleFrozenMappingTest | 2 |
| TreeFrozenEditTest | 2 |
| TreeFrozenMappingTest | 2 |
| WriteProtectionServiceTest | 4 |
| OpenApiAuditCoreRoutingTest | 6 |
| OpenApiAuditMapperSqlTest | 1 |
| OpenApiAuditReadAccessTest | 22 |
| LogQueryPermissionInterceptorTest | 2 |
| MybatisPlusDataPermissionHandlerTest | 8 |
| OpenApiAuditQueryPermissionTest | 4 |
| 合计13类 | **119** |

全部 failures=errors=skipped=0。XML 原目录为各模块 `build/test-results/test/`，最终独立副本在 `.gradle/custom-scope-final-xml-20261024/{eva-web-sys-service,eva-web-auth,eva-core-data-mybatis}/`。旧 RoleService 测试包含 ALL、遗漏范围保留、只读及版本刷新；OpenAPI和日志读取门禁未退化。此处不是全仓全量测试或实际页面/权限验收。

## 待独立复验与运行边界

本轮生产 class 编译晚于当前已知 PID47664 10:08:31 启动；未重启、热替换或声称运行实例已加载新构造函数/guard。独立 QA 应先审本局部 diff、init、492文件manifest及48项安全用例；之后运行加载由总控按相关模块完整 classes/resources 一致性管理，不以单个 RoleService.class 混入旧依赖包。

包 S CUSTOM 尚未创建，本成员没有新增任何 S 对象或用户/资源授权；原七对象及 NULL归属、R字典/缓存/私密备份均未触碰。未进行实际 DB/HTTP/Redis 调用，未读取真实请求日志/个人数据，未改前端/vault/QA卡，未提交、推送或创建PR。

正租户 tenant V1基础表缺 TENANT_ID 与原 eq 谓词的潜在不兼容仍独立未解决；本次保持其现有失败闭合，不扩包T、不增加列或把 NULL 当共享归属。独立测试通过不等于 HTTP CUSTOM 保存已成功；需总控加载门禁解除后另派受控一次提交及详情回读。

本轮编译/测试及命令均已结束，交回源码写锁，等待独立安全复验；不自行恢复S或启动服务。
