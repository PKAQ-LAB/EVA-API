# backend_developer 状态卡

- 任务：MODULE-DETAIL-CONVERT-20261013
- 日期：2026-10-13（Asia/Shanghai）
- 状态：完成实现与定向验证，等待总控安排独立 QA；写入锁已释放，无运行中的本任务命令。
- 唯一总控：01a0bc51-e17e-7990-ad76-13c63f385f33
- 写入范围：EVA-API 内 ModuleService.getModule、相关回归测试及本卡。
- Git：cornerstone，HEAD a17321e9；origin/cornerstone 本地跟踪引用 ahead/behind 0/0；未 fetch，不能声称远端实时状态。
- 原有未跟踪 GO_REFACTOR_PLAN.md 保留。未提交、推送、创建 PR、启动服务或访问数据库；未写前端仓库。

## 根因与修复

ModuleService.getModule 原来把 this.get(id) 当作实体查询。实际调用 StdService.get(Long) 会先执行父类 Convert.toVo。ModuleService 构造注入的 ModuleConvert 是独立 MapStruct 接口，并非父类 Convert；仓库唯一继承 Convert 的 RoleConvert 不提供 ModuleEntity 到详情的对应映射。使用实际生成 RoleConvert 初始化父类转换字段的回归测试，原代码复现 BizException，堆栈完全对应 Convert.convert:68 → Convert.toVo:48 → StdService.get:90 → ModuleService.getModule:193。故问题发生在显式 entityToDetailVo 之前，不是详情资源读取错误。

生产代码只将该详情方法改为 mapper.selectById(id)，保留零 ID 的 PARAM_ERROR 与不存在记录的 CAN_NOT_FIND_RECORD，随后仍使用原 ModuleConvert.entityToDetailVo 和 resourceEntityToVo。原查询 Mapper、资源 main_id 条件及原 Controller 均未改动；未改映射、API、DB、权限或租户配置。path 仍是节点 ID 路径；routeUrl 是路由 URL，componentUrl 是组件 URL，不重新定义字段。

## 文件与验证证据

- eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/module/service/ModuleService.java:192：9 行新增、1 行删除，修改仅位于 getModule；UTF-8、LF。
- eva-web/eva-web-sys/eva-web-sys-service/src/test/java/org/pkaq/sys/module/service/ModuleServiceTest.java：4 项回归，使用真实 MapStruct 生成实现，Mock Mapper，无数据库连接。
- 基线 compileTestJava 成功；原代码回归 4 项中 2 项失败（有资源、空资源），2 项通过（零 ID、不存在记录）；异常堆栈与用户提供的四层行号完全一致。
- 修复后命令：gradle.bat :eva-web:eva-web-sys:eva-web-sys-service:test --tests org.pkaq.sys.module.service.ModuleServiceTest --offline --no-daemon --console=plain
- 修复后 BUILD SUCCESSFUL，4 tests / 0 failures / 0 errors / 0 skipped；包含生产 compileJava。
- 断言覆盖模块 ID、pid、path、code、name、icon、routeUrl、componentUrl、isleaf、frozen、tenantId、revision、sort，资源字段与 main_id 查询条件，空资源列表及两个原错误码；未触发事件或角色资源写入。
- 证据：.gradle/module-detail-baseline.log、.gradle/module-detail-fixed.log，以及 eva-web/eva-web-sys/eva-web-sys-service/build/test-results/test/TEST-org.pkaq.sys.module.service.ModuleServiceTest.xml（后者为修复后结果）。
- git diff --check 通过。Git 的 LF→CRLF 提示来自既有 autocrlf 设置，当前两个 Java 文件均为 LF。

## 图检查与限制

- 修改前 impact 通过 UID 消歧：唯一直接调用者 ModuleCtrl.get；risk LOW；涉及该入口的 6 条执行流程。
- 修改后 detect_changes：risk_level high，2 个 touched symbols（ModuleService 与 getModule），6 条 affected flows，1 个 tracked changed file。此 HIGH 结果必须由独立 QA 复核，不作为自动发布许可；未跟踪测试不在该图检查结果内。
- 索引落后 HEAD 14 个提交。已尝试 node .gitnexus/run.cjs analyze --index-only，长时间无后续进展后停止；索引未刷新。已通过 indexed commit 0017a316 与 HEAD 比较确认 module 目录、RoleConvert、Convert、StdService 相关源码没有差异，并结合当前源码核对，仍不声称已完成新索引的完整图验证。
- 仓库无本地 PROJECT_PROFILE.md、CAPABILITY_MAP.md、.agent-project.yaml 或独立当前任务文件；已读 AIBrain 中匹配项目资料、仓库 AGENTS.md、根 CLAUDE.md 与 .claude/CLAUDE.md；本次以总控明确派发的唯一任务为执行范围，未扩大接入配置。
- 环境无 gradlew.bat；使用已安装 gradle.bat（Gradle 9.5.1 / JDK 25）成功执行。现有 wrapper 属性指向 9.6.1，本任务未改依赖或构建配置。
- 基线编译已有弃用 API、泛型及 Gradle 弃用警告；基线错误转换递归曾伴随 Java instrumentation 报文，修复后定向执行成功，仅保留 JVM/Gradle 警告。
- 未运行全量测试、服务或实际 HTTP 请求，尚无用户浏览器点击及真实权限/租户环境验收结论。

## 交接给总控的独立 QA 项

独立复核最小差异和回归测试；验证现有模块编辑点击能返回原 ModuleDetailVo 与 resources；验证 path、routeUrl、componentUrl 各自语义；复核零 ID、不存在记录的原错误码；检查请求权限拦截及租户上下文下的隔离行为。不得为验收改契约或新增数据库数据；总控决定后续验收安排，本成员未自行唤起 QA 或决定发布。

## MODULE-RESOURCE-SEED-DEV-CLEANUP-20261013

- 状态：实际开发库写入、精确缓存失效及验证完成；等待总控安排独立最终 QA。写入锁已释放，没有运行中的本任务命令。
- 用户授权：实际接口资源种子、数据库直接删除开发工具相关菜单；总控补充明确授权无引用 BUTTON/OPERATE 字典项精确清理、仅本次已确认缓存键失效。用户后续授权分步提交 push，但总控要求本次先交付 DB 证据，等待独立最终 QA 通过后正式唤起；本成员未提交或推送。
- Git：cornerstone / a17321e9，本地跟踪 origin/cornerstone 仍 0/0；未 fetch，不代表实时远端状态。之前 getModule 修复和原未跟踪 GO_REFACTOR_PLAN.md 均保留。

### 真实连接与归属

连接取自 application-dev.yaml，密码只在进程中读取，没有写入脚本、备份、Git 或报告；PostgreSQL 脱敏目标指纹 d22bc534e71c，database eva / schema eva，实际只读盘点事务 transaction_read_only=on。开发目标与 application-prod 目标不同（prod 配置仍是 mysql）。仓库默认 dev，eva.tenant.enable=false、mode=standalone；实际系统模块、资源、字典与授权表仅发现 eva schema，未发现 eva_core 或 tenant_* 业务表。sys_tenant 仅既有 system 记录且 schema_name 为 null，不把该记录解释成 schema 租户模式，未做租户 schema 迁移。

逻辑引用列与 FK 已盘点；实际关联资源表为 sys_roleres_ref、sys_tenant_resource、sys_tenant_package_resource、sys_role_resource。sys_role_module 是派生 VIEW，不直接删除；其他发现的 main_id 是字典明细或模块资源本身。本次系统表未发现菜单/资源 FK，唯一已有 FK 在账号 profile 关系，不涉及本次操作。

### 精确目标、影响与结果

开发工具菜单按 id + code + route_url + pid/path 与 V1 历史双重核对，不使用名称 LIKE 删除：1000000000000000300（dev，/dev）、1000000000000000301（dev.generator，/dev/generator）、1000000000000000302（dev.workflow，/dev/workflow）；仅这 3 个节点，没有额外后代。精确资源 ID 为 1000000000000001300/1301/1302/1303（4 条）。资源类型父字典 ID 为 1000000000000004050；1000000000000004057 BUTTON、1000000000000004058 OPERATE 均是实际 V1 种子记录，所有发现 schema 的资源引用为 0，允许无损删除这 2 条扩展项。

实际删除：sys_module 3、sys_module_resources 4、sys_roleres_ref 8、sys_tenant_resource 4、sys_tenant_package_resource 0、sys_role_resource 8、sys_dict_item 2，合计 29 行。实际新增：sys_module_resources 54 行。没有插入任何角色/租户/套餐授权，没有修改其他模块、资源或授权行；事务内对其他资源及相关表剩余数据逐行比较一致。

最终 SQL 只读盘点：sys_module 10 行、sys_module_resources 77 行（原 27 - 4 + 54）、2 个既有角色各 23 条 sys_roleres_ref（原各 27 - 4），扁平 sys_role_resource 46 行。dev 模块/资源及对应授权清零；RESOURCE_TYPE 当前仅 *,GET,POST,PUT,DELETE,PATCH，非 HTTP 资源 0。重复运行只读计划：54 条均已存在，to_insert=0、全部 cleanup_counts=0；没有重复 code / main_id+method+url。

### 端点与鉴权证据

54 条资源从当前真实 @RestController 类级及方法级 Mapping 生成，附源码路径与 SHA256。覆盖 UserCtrl、OrganizationCtrl、PostCtrl、RoleCtrl、ModuleCtrl、DictCtrl、BizLogCtrl、ErrorCtrl。岗位资源沿用既有 sys.organization 模块归属；没有新建模块。当前 RoleCtrl 继承 StdCtrl 的公开映射均已被本类覆写，无漏加继承端点。对账号/组织/岗位/角色/模块/字典逐个断言实际 list/edit/del 存在，另覆盖 get、switch/frozen、sort、checkUnique、grant/grantPost、grantResource/grantUser/listUser/fetchResource 等真实接口。字典保留实际存在的 GET /del/{id}（deprecated）和 POST /del/{id}，不根据 CRUD 名称猜 HTTP 方法。新增资源只有 GET/POST，无 BUTTON/OPERATE、无 * 方法、无 /** pattern、无 /api 前缀。

application.yaml 的 context-path 是 /api；JwtAuthFilter.resolveRequestPath 优先 servletPath，否则只剥离真实 context-path 路径段。ResourceAuthorizationService 把 HTTP method 与规范化路径交给 RoleResourceCacheService；实际 matcher 是 AntPathMatcher，具体 HTTP 方法严格匹配，{id}/{code}/{type}/{value} 动态模板可直接匹配单层参数，无需扩大为子树。独立实际 Spring AntPathMatcher 探针通过，覆盖动态 ID、双参数、拒绝额外子层、拒绝其他操作及 del/delete 区分。

权限候选由 SysRoleResourceMapper.selectEffectiveResourcesByRoleId 筛选有效模块/角色与 HTTP 类型，非零租户角色必须有 sys_tenant_resource 上限。当前既有管理员及另一角色的授权只减少 dev 的 4 条，其他 23 条保留，未给新种子自动授权。现存 *:/sys 等菜单权限与 V7 的 *:/sys/.../** 仍能覆盖子路径；本次不移除这些旧业务授权，不声称新增精确资源使运行权限收窄，也不能借现有通配证明逐操作拒绝效果。

### 备份、事务与恢复

成功执行前备份目录：C:/home/workspace/lab/EVA-API/.gradle/module-resource-backup-20261013T062507Z。rows.json 只含上述 29 条相关行/引用；restore.sql 含精确新种子删除与原行恢复；sha256.json 保存内容校验；committed.json 记录实际提交及计数。目录属于既有 Git 忽略 .gradle，秘密和数据备份不入 Git。恢复 SQL 遇到原行冲突会停止，检测新种子取得后续角色/租户/套餐授权也会拒绝，不用覆盖式 upsert；未来发生数据修改时必须先复核，不自动执行恢复。必须通过本操作脚本的 connect(False) 相同开发目标指纹校验连接执行恢复，不能将 restore.sql 对其他同名数据库盲目执行。

执行使用 SERIALIZABLE 事务与相关 7 表 SHARE ROW EXCLUSIVE 锁，先重新验证目标/后代/类型引用/精确计数，再备份并校验读回和 SHA256，依次删除角色/租户/套餐引用、精确扁平权限、资源、模块和无引用字典项，最后插入精确种子。提交前在 SAVEPOINT 中实际执行 restore.sql（移除其事务外壳），逐行验证原资源/关系恢复，再 ROLLBACK TO SAVEPOINT 返回本次已应用状态，确认清理目标仍空后提交；恢复演练通过。

第一次执行误用了资源表不存在的 revision/frozen 列，报 UndefinedColumn，事务完整回滚；独立只读核对菜单/资源/关系/字典均保持原数量。随后按现库列修正插入语句，无 DDL 或迁移，第二次成功。失败尝试的备份目录 .gradle/module-resource-backup-20261013T062405Z 没有 committed.json，不能把它当已执行结果；当前以 062507Z 为恢复依据。

### 精确缓存失效

Redis 开发目标指纹 c2a1d585223c / db 0，与 prod 配置不同。所有操作只用源码导出的精确候选键 TYPE/PTTL/GET/SMEMBERS/DUMP，不使用 KEYS、扫描全库、flushdb。字典键内容与原 RESOURCE_TYPE 8 项相符；两角色缓存的 27 个 method:path 与原 DB 扁平权限逐项一致，确认本次实体归属后备份 DUMP 与 SHA256。

实际原子失效 3 个键：dictdata::RESOURCE_TYPE；eva:security:tenant:0:role-resource:766008469304664561；eva:security:tenant:0:role-resource:1000000000000000004。三键原 TTL 均为 -1；Lua 先比对三键 DUMP 与备份完全相同，再一次性 DEL，任何并发变化均拒绝删除；即时 EXISTS 均为 0。其他候选 scoped 字典/tenant ID 键不存在，不作操作。缓存备份 redis-before.json、redis-invalidated.json 在上述成功 DB 备份目录内。

角色缓存后续访问按现有 flat 表懒加载；字典 queryDict(RESOURCE_TYPE) 缓存未命中按 DB 重建。fetchDicts 在其他字典缓存非空时不会自动补单个被失效字典，因此真实前端 reload/query 行为仍需独立 QA，不声称浏览器已经显示 6 项；禁止用前端硬过滤掩盖。未启动或重启服务、未调用浏览器/HTTP；新 Redis 内容的懒加载不是本次实际验证项。

### 可追踪文件与验证

- docs/sql/module_resource_seed_dev_cleanup_20261013.py：默认只读计划；显式 --apply 才写 DB，固定开发目标指纹，凭据读取既有配置，精确目标/计数/碰撞校验，备份及恢复演练，失败回滚。禁止 Python -O 绕过校验。
- docs/sql/module_resource_seed_routes_20261013.json：54 条真实端点/HTTP method/说明/模块归属/源码 SHA256，含 list/edit/del 与动态模板。
- docs/sql/module_resource_cache_cleanup_20261013.py：仅 3 个确认归属的开发缓存键、DUMP 条件校验、精确备份/原子失效，无秘密。
- .gradle/module-resource-readonly-report.json 为最终只读 DB 盘点；.gradle/module-resource-plan.json 为重复执行只读计划；.gradle/module-resource-auth-validation.log 为定向鉴权测试日志。
- gradle.bat :eva-web:eva-web-auth:test --tests org.pkaq.core.auth.adapter.mybatis.rbac.service.RoleResourceCacheServiceTest --tests org.pkaq.core.auth.authentication.filter.JwtAuthFilterTest --offline --no-daemon --console=plain：BUILD SUCCESSFUL；3+2=5 项，零失败/错误/跳过，覆盖具体方法不扩大、模块前缀边界、同角色跨租户缓存隔离及 JWT 上下文路径。
- 实际 Spring AntPathMatcher 探针通过；数据库事务恢复演练通过；SQL 后置核对及只读幂等计划通过；git diff --check 通过。未全量测试或端到端 HTTP/浏览器验证。

### 独立 QA 与发布交接

请总控安排 quality_reviewer 只读检查开发目标/schema、29 行精确删除、54 条资源与实际源码 mapping 一致、没有新增授权、无 dev 悬挂关系、RESOURCE_TYPE 的 6 项和无非 HTTP 引用、备份完整性与恢复脚本/演练证据、三个精确缓存键失效证据及前端 lazy reload 行为。既有 wildcard 保留与逐操作拒绝验证不足必须清晰报告。发布候选仅安全脚本/资源清单/必要测试/状态文档及已 QA 的 getModule 修复；本地备份、连接信息、GO_REFACTOR_PLAN.md 不可暂存。独立最终 QA 后由总控正式唤起才分步提交 push，先核验实际 upstream 并 fetch；分叉冲突暂停，不 rebase 或强推。

## 2026-10-18 最终 QA 与发布门禁

今日总控正式传达最终 QA 通过，本成员已只读核对前端 docs/legion/status/quality_reviewer.md 的本任务记录。QA 当前开发库实测：54 条资源逐条对应 manifest、29 行清理目标均为 0、其他既有资源/扁平权限与 10 月 13 日报告投影相同；备份哈希匹配，3 个精确 Redis 键仍不存在，不能说缓存已经重建。真实 DictSelector 在 data 为 undefined 时单项 query 回退，已由实际 effect 模拟核对，不是完整浏览器或实际 HTTP 通过。既有宽授权与 fetchDicts 部分缺项限制保留。

QA 强制重跑鉴权 5 项通过，零失败/错误/跳过；模块 4 项历史独立通过且候选未变，复用该验证。纠正前文测试覆盖描述：JwtAuthFilterTest 两项覆盖 permVer 与可信服务端角色/ThreadUser，并不直接测试 /api 剥离；路径剥离仅源码核对，动态参数边界另有实际 AntPathMatcher 探针通过。没有本次 P0/P1/P2，未宣称真实逐操作 HTTP 拒绝效果。

用户已授权本批全部分步提交普通 push，总控正式唤起。发布前 fetch origin cornerstone 成功后发现远端由 a17321e9 前进至 59dfca28031f282d049ca0a2438d6019624d4232，本地 HEAD 仍 a17321e9ac5a4f676fa1890d88d161a6986b7b3b，ahead/behind 为 0/4。新增 4 个提交只改 gradle/libs.versions.toml（Flyway 13.10.3、版本检查插件 0.66.0），未触及本批 6 个发布候选；这只表示内容范围未交叉，不代表已复验新依赖。

按本次明确“远端新增即暂停、不得自行同步”的发布边界，本轮在暂存/提交前停止，未 merge/pull/rebase/reset/stash、未 commit/push，也未重跑 DB/Redis 操作。暂存区为空，GO_REFACTOR_PLAN.md 及 .gradle 备份证据保留；已释放写入锁，等待总控决定是否授权 fast-forward 同步并安排依赖变化后的必要复验。不得将本状态记为已发布或 Git 同步完成。

### 已获授权安全快进，离线验证受阻

用户随后明确回复“允许”，总控授权只接入已知 59dfca28 基线。执行前 git ls-remote 再次确认远端 cornerstone 仍为 59dfca28031f282d049ca0a2438d6019624d4232，暂存区为空、stash 为空；git merge --ff-only origin/cornerstone 正常成功。快进后全部 6 候选文件及受保护 GO_REFACTOR_PLAN.md 的 SHA256 与快进前完全一致，成功备份 rows.json/restore.sql 的 SHA256 校验通过，HEAD 与 origin/cornerstone 为 0/0。没有切分支、rebase、stash、reset 或覆盖文件。

新版本 Flyway core/PostgreSQL 插件 13.10.3 与版本检查插件 0.66.0 的本地缓存未发现。已按指令尝试：gradle.bat :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.module.service.ModuleServiceTest :eva-web:eva-web-auth:test --rerun --tests org.pkaq.core.auth.adapter.mybatis.rbac.service.RoleResourceCacheServiceTest --tests org.pkaq.core.auth.authentication.filter.JwtAuthFilterTest :web-booter:compileJava --offline --no-daemon --console=plain。

结果为 exit 1 / BUILD FAILED in 50s，失败于 build.gradle 第 1 行插件解析，offline 无法解析 com.github.ben-manes.versions:com.github.ben-manes.versions.gradle.plugin:0.66.0；没有进入本次 4+3+2 项测试或启动模块编译。此为新基线离线依赖解析阻塞，不是已确认源码回归，不能沿用旧基线测试结果声称新基线验证通过，也不能据此判断该插件版本在联网仓库是否存在。日志位于 .gradle/module-release-post-ff-validation.log。

按“验证阻塞则暂停”的本次发布边界停止，未联网安装依赖、未降级/更改版本或绕过插件/Hook，未 stage/commit/push；未执行任何 DB/Redis 脚本或写入、未启动服务/浏览器。当前本地已安全快进但尚未发布本批改动，等待总控决定联网解析新基线依赖后再继续比例合理验证和三组提交推送。全部命令结束，写入锁已释放。

### 2026-10-18 联网验证通过与授权提交

总控随后明确允许正常联网解析上游已声明依赖。保持获准 59dfca28 基线、插件 ID/版本、Flyway 版本和仓库配置不变，使用同一验证命令去掉 --offline；没有新增依赖声明、降级、跳过插件或清理依赖缓存。实际 exit 0 / BUILD SUCCESSFUL in 2m19s，27 tasks（10 executed / 17 up-to-date），:web-booter:compileJava 成功，两个目标 test 任务实际执行。XML 确认 ModuleServiceTest 4 项、RoleResourceCacheServiceTest 3 项、JwtAuthFilterTest 2 项，共 9 项，failures/errors/skipped 全 0；日志 .gradle/module-release-online-validation.log。新基线只验证这些目标和必要生产编译，不冒称全仓或真实 HTTP 联调通过。既有 JVM sharing、弃用 API/泛型及 Gradle10 兼容警告仍存在。

测试后再次 ls-remote 确认远端仍为 59dfca28031f282d049ca0a2438d6019624d4232。代码、测试与 3 个资源维护文件哈希仍与 QA 候选一致；唯一主动收口内容是本状态卡。提交前图检查对原模块修改仍为 HIGH / 6 流程，与已审范围一致；新维护文件在旧索引没有符号覆盖，0 changed symbols 不能视为安全证明，继续以独立源码/AST/manifest/实际只读 DB 审查补证。

已按精确路径正常提交、未禁用 Hook：

- 第一组 86cc7b2c284b487c5ca2a4561877913e87a9cba2，fix(module): use typed conversion for module details，仅 ModuleService.java 和 ModuleServiceTest.java。
- 第二组 d9f908845ae0eecc2c2ede4221dd8d3166e82a38，chore(resources): add scoped development maintenance scripts，仅两份安全维护脚本及 54 条端点 manifest。
- 第三组为本状态卡，docs(legion): record backend release verification；该提交自身 SHA 以最终 git log/总控回报为准，避免在文件中嵌入自身循环哈希。

本记录在第三组提交前写入，推送仍须正常 git push origin cornerstone 和远端复核；不提前声称成功。最终 HEAD/ls-remote/origin 一致、0/0、每组文件清单和保护项校验将留存在 Git 忽略的 .gradle/module-release-final.json 并主动回报总控。GO_REFACTOR_PLAN.md 哈希及原空 stash 保持；rows/restore/cache 备份仍 Git 忽略，不可入提交。此发布仅提交已交付代码/安全脚本/文档，10 月 13 日数据库数据变更是此前已执行操作，本轮未重新执行 --apply、恢复或 Redis 写入。既有宽授权、fetchDicts 部分缺项/前端回退及恢复 SQL 必须先经脚本开发目标指纹校验的限制继续保留。

## LOCAL-MAVEN-PUBLISH-20261018

状态：版本最小改动与 Gradle 元数据自检完成；实际 Maven 发布等待本机仓库路径选择及受口令保护的签名密钥。未执行实际 publish、远程 Maven、commit/push 或 DB/Redis 操作，全部任务命令已结束并交锁。

当前分支 cornerstone / 6a0fee51，与本地跟踪 origin/cornerstone 0/0（本任务未 fetch）；开始时 tracked worktree 干净，仅原 GO_REFACTOR_PLAN.md 未跟踪。本任务修改 build.gradle、docs/PROJECT_KNOWLEDGE.md 和本状态卡，保护 GO 与既有备份；不涉及前端或其他项目。

已读更新后的项目 AGENTS 用户指令、仓库 AGENTS/CLAUDE、匹配项目 AIBrain 与权威 legion-session-orchestration.md、完整 Ponytail 技能。根版本原为 4.0.3.12，catalog spring_boot 为 4.1.1；build.gradle 现从 libs.versions.spring.boot 读取，要求三段数字，再仅把第一段改为 1，得到 1.1.1。后续 Boot 4.2.3 对应 EVA 1.2.3，Boot 5.0.0 对应 EVA 1.0.0；不把 Boot major 带入 EVA，不支持静默解析 SNAPSHOT 等非三段数字。没有修改 Boot 或任何依赖版本。内部依赖均用 project(...)，未发现其他现行构建中的旧版本常量；唯一项目知识库 GroupId/Version 表格同步为 1.1.1。

GitNexus 对 File:build.gradle 的精确 impact 为 UNKNOWN / 无可解析调用，不视为低风险；已从 settings、全部 publish.gradle 引用、版本常量搜索和 Gradle 实际 publication 补证。根 publishToMavenLocal --dry-run 成功，12 个库均有 mavenJava publication，包含 jar/sources/javadoc 和 signMavenJavaPublication；聚合 eva-core/eva-web/eva-web-sys 与 web-booter/license-server 没有 publication，不计作已发布产物。按现配置发布范围为 org.pkaq 下 eva-core-cache、eva-core-common、eva-core-contract、eva-core-data-mybatis、eva-core-license、eva-core-log、eva-core-upload、eva-core-websocket、eva-web-auth、eva-web-core、eva-web-sys-domain、eva-web-sys-service；启动模块带环境配置的 JAR 未列入库发布。

元数据自检实际命令：gradle.bat inspectLocalMavenPlan -I .gradle/local-maven-inspect.init.gradle --no-configure-on-demand --offline --no-daemon --console=plain；exit 0 / BUILD SUCCESSFUL in 10s。断言全部 project/publication 版本为 1.1.1、12 个真实 publication、不含两个启动模块，版本公式示例与非三段拒绝自检通过。最初把检查任务注册在 projectsEvaluated 导致 configuration-on-demand 下任务发现失败，已仅修正忽略目录内的检查脚本注册时机，未改项目配置机制。证据 .gradle/local-maven-plan.json / local-maven-inspect.log / local-maven-publish-dryrun.log。上述是 dry-run 与元数据检查，不是实际发布、JAR/POM 文件验收或本任务定向测试通过。

实际 Gradle MavenLocal 有效路径为 file:/D:/DevApp/cache/maven，与 C:/Users/A/.m2/settings.xml 的 localRepository 一致；当前 D:/ 根盘不存在，因此该目标不可写。本机默认 C:/Users/A/.m2/repository 也尚不存在；只读检查 12 个候选 1.1.1 坐标均无该路径下同版本目录，但用户尚未选择它，未创建或写入。未改 settings.xml，也未用 -Dmaven.repo.local 擅自改发布目标。publish.gradle 固定 D:/key/maven_publish_secret.gpg，同样不可用；未改签名开关或在无密钥时冒称签名通过。

### 用户新增重建签名 key 授权后的检查

用户授权“key 好像确实丢了，重新搞一个”，总控明确要求强口令保护且口令仅用户本机可信 pinentry 输入。实际可用工具是 C:/home/DevApp/Git/usr/bin/gpg.exe，版本 GnuPG 2.4.9；同目录有 gpgconf.exe、gpg-agent.exe、pinentry-w32.exe、pinentry.exe，keyboxd.exe 位于 Git/usr/lib/gnupg。系统 PATH 未发现独立 GPG；直接 PowerShell 调用初次因 keyboxd 子进程 DLL 路径失败。仅在当前 shell 进程临时前置 Git/usr/bin（结束后恢复 PATH），keyboxd --version、GPG 公/私钥元数据查询均 exit 0，结果无 sec/pub/uid/fpr 记录，不能恢复既有匹配密钥。未读取或导出私钥/密码。GPG 首次查询自动创建用户目录 C:/Users/A/.gnupg 及 trustdb/common.conf，不是生成密钥，没有将它们写到项目或 Git。

项目公开开发者配置为 ID PKAQ、name WuF、email pkaq@msn.com，可作为新 UID 候选，尚未擅自决定新身份。没有运行生成命令、没有创建无口令密钥、没有上传 keyserver、撤销旧 key 或修改 Maven 账号。当前工具无法代表用户安全输入强口令；应由用户在本机 PowerShell 前置 Git/usr/bin 到该会话 PATH 后执行 & 'C:/home/DevApp/Git/usr/bin/gpg.exe' --pinentry-mode ask --full-generate-key，在真实交互中确认公开 UID，并在 pinentry 中输入强口令；不得通过聊天、命令参数或仓库文件传递口令。

待总控协调：用户确认新 UID 或在本机生成并只回报公开 fingerprint；用户选择真实可写本地 Maven 目录（或修复 D 盘设置）。之后最小核对 Gradle 外部 GPG/keyring 读取方式，保留远端签名；发布前再检查具体坐标冲突和可恢复备份，再构建与实际 publishToMavenLocal，逐个验证 jar/pom/module 及内部版本。当前没有 Maven 仓库写入、没有发布成功产物，不会以聚合任务或 dry-run exit 0 替代实际发布。

## RESOURCE-CTRL-RECONCILE-ERROR-20261018

状态：只读资源重审及三类明确后台代码修复完成，等待总控组织独立 QA；资源数据及契约选择保持暂停。全部命令结束，写入锁释放。没有服务重启/浏览器/DBRedis写、测试数据修复、依赖升级、stash/reset/commit/push 或下级代理。当前用户服务未被重启，不能宣称本次代码已经在其进程生效。

### 当前后台异常与运行问题定位

已读完整 runtime-crud 状态中的 7 项 P2 与测试对象台账。当前成员没有附加应用终端；application-dev 配置日志 D:/applogs/webapp.log，而当前 D 盘不存在，工作区 logs/booter logs 未发现可用日志文件。随后仅通过已确认开发 PG 连接的强制 read_only=on 事务查询错误事件受限元数据及相关测试 ID 字段，不打印 params 原文、token、密码或个人信息；没有以历史异常冒充新栈。

今日最新错误事件 ID 1559388435168960512，request/last time 2026-10-18 20:25:29，exception_type 为 MissingServletRequestParameterException，class RequestParamMethodArgumentResolver / method handleMissingValueInternal；仅从记录提取可公开匹配信息，确认缺少 roleId，关联路径 /sys/role/listUser。现 RoleCtrl.listUser 确实要求 @RequestParam Long roleId。旧 Convert.convert BizException 最近仍为 10-13，不能称此次缺参错误是旧模块转换复发。当前记录没有可用的本次业务完整 stack，按缺参证据交总控协调前端，不放宽 roleId 必填或绕过权限。

QA 测试记录只读回查：O01 frozen=1/sort=12；M01 frozen=1/sort=3；M02E frozen=0/sort=1，route_url=/sys/role 与 icon=step-forward 均已存。D01 的 remark2 已在 DB，因此备注丢失是回传链问题：DictViewVo 无 remark；按总控指令此为契约补充，暂停不加 VO。服务本来按同级 nextSort 维护创建排序，不能把表单99被服务生成排序替换直接判为数据库丢失。本次不直接修正任何已存测试对象。

### 资源重审与暂停的数据计划

完整只读现状与逐行 ID/mainId模块归属/code/type/path/描述及角色/租户/套餐引用数在 docs/legion/design/resource-ctrl-reconcile-20261018.md，安全本地原始对照在 .gradle/runtime-resource-reconcile-20261018.json。独立重扫当前 @RestController 类级/方法级映射及已覆写继承映射，不用旧 manifest 自证：现有具体 HTTP method/path 没有找不到实际端点的条目；仍须区分需求语义与技术可达性。

现状78条=旧菜单/广域23+HTTP种子54+QA自建1。标准GET list/get/{id}与POST edit/del（字典del/{id}）可归28条；另外26条为真实校验、排序、冻结、授权及字典辅助/兼容GET删除等接口，关联角色/租户/套餐目前均0。旧23条广域的角色引用46、租户引用23、套餐0、对应扁平权限46；直接删除会撤销现有管理员等角色已授权行为。岗位7条现归 sys.organization，无独立岗位菜单，不擅自新建。推荐具体CRUD按Ctrl/操作对照展示，特殊接口保留其真实定义并单独明确范围；是否排除26条、如何逐Ctrl退役23旧广域及受限迁移既有授权，由用户/总控确认。

本轮批准执行的数据计划为 add=0/update=0/delete=0，未重放旧 --apply、未删除旧宽授权、未给任何角色/租户自动授予新资源。QA自建资源及5条测试记录保护。形成精确变更/依赖/备份/事务方案且总控确认之前，不做资源 DB 更正。

### 已完成的同契约最小修复

- LogQueryBo.begin/end 添加与现 DateRangeBo 一致的 yyyy-MM-dd 日期绑定注解；字段与业务查询接口不变。真实 Spring DataBinder/DefaultFormattingConversionService 回归先复现日期绑定错误，修复后接受 QA 当前日期格式，非法日期仍报绑定错误。
- Module.xml 的 selectModuleMapList 由递归 BaseResultMap 改为现有 FlatResultMap。原结果已通过 collection/listChildren 装载后代，MapStruct 将内嵌子节点投影为 StdTreeVo，随后 ModuleService 又对全扁平行 TreeHelper 追加完整详情子节点，导致重复后代与第一份子节点 route/icon 缺失。前端 normalizeTree 的去重会保留缺字段的前项。现仅取消该扁平查询的递归装载，保留原列、过滤、排序、权限/租户与单次 Service 组树；真实 MyBatis XMLMapperBuilder 验证结果映射无 nested query、实体类型不变且 SQL 包含 route/icon。测试最初缺 module alias 的 fixture 失败已纠正，随后修前确实失败于递归映射断言，未把 fixture 错误冒充产品缺陷。
- ModuleConvert 的 ModuleAoeBo、OrganizationConvert 的 OrganizationAoeBo 冻结字段复用既有 BaseEnum.of(FrozenEnumm.class, code)，不用生成的 values()[code]。枚举原顺序是 FROZEN(1)、UN_FROZEN(0)、READ_ONLY(9999)，ordinal 令0/1颠倒并令9999越界；真实 MapStruct 实现先复现颠倒，再验证0/1/9999与null全部正确。不修改共享 FrozenEnumm 顺序、BaseEnum 或全局映射规则。
- ModuleService.editModule 与 OrganizationService.edit 在校验现记录可编辑后保留 origin.frozen，普通编辑不暗中切换状态；原独立 switchFrozen 路径仍执行显式目标码。真实服务+Mapper mock 回归验证编辑保持原冻结状态，模块显式 switch 仍触发 cascadeFrozen。未更改冻结接口参数或返回契约。

图检查：ModuleConvert CRITICAL（28影响）、OrganizationConvert HIGH（13）、OrganizationService CRITICAL（9），已在改动前报告并按具体方法缩小范围；LogQueryBo 在旧索引缺失、selectModuleMapList 无解析调用均 UNKNOWN，不能当低风险，已用当前源码调用搜索、真实 XML/MapStruct/绑定与服务回归补证。索引落后21提交限制保留，未声称完整图覆盖。其他 UserConvert/RoleConvert/TenantConvert/TenantPackageConvert 的生成代码也仍有 ordinal 冻结转换，作为同类存量风险报告，未为本次扩大到这些业务；模块查询 BO 的未使用转换方法也未扩改。

### 验证及交接

修前：日期2项中有效日期失败、非法日期通过；MapStruct两项分别复现冻结码错误；修正fixture后的 MyBatis 列表1项失败于存在 nested query。修后命令：gradle.bat :eva-core:eva-core-log:test --tests org.pkaq.core.log.bo.LogQueryBoTest :eva-web:eva-web-sys:eva-web-sys-service:test --tests org.pkaq.sys.module.mapper.ModuleListMappingTest --tests org.pkaq.sys.TreeFrozenMappingTest --tests org.pkaq.sys.TreeFrozenEditTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest --offline --no-daemon --console=plain --continue。BUILD SUCCESSFUL in23s，24tasks/7executed17up-to-date；XML计数2+1+2+2+4+4=15项，零失败/错误/跳过，含必要生产编译。日志 .gradle/runtime-fixes-baseline.log、runtime-mapping-baseline.log、runtime-fixes-verified.log；没有 @SpringBootTest 或真实数据库写入测试。

遗留跨端信息：错误日志后端契约为 Response<PageVo>，其 data 内 records/total/current/size；前端 error/service.ts 仍声明 data 为数组且 http.list 不解包，符合 forEach 运行错误线索，应由前端适配现返回，后端未改。在线后端 OnlineUserVo 仅 userId/tenantId/sessionId/device/时间等，没有 account/token；过滤参数是 userId/targetTenantId，前端按 account 查询不会生效。CacheTokenUtil 新写 loginTime 为毫秒数，但本轮未读取真实 token 缓存，1794/1798年的具体现存时间值与解析链仍需前端安全核实，不猜单位或改后台时间类型。未给前端直接发消息，全部交总控。

本任务源码6文件：LogQueryBo.java、Module.xml、ModuleConvert.java、OrganizationConvert.java、ModuleService.java、OrganizationService.java；新增4个定向测试与资源重审设计文档，另更新本状态卡。原 build.gradle/docs 项目知识库版本准备、WebBooter.java 的他人修改、technical_designer 设计与状态、GO 及备份均保护。git diff --check通过，未提交/推送。请独立 QA 复核以上最小差异和15项测试，再在用户允许重启/重新编译其应用后复验对应页面；7项运行P2不能据此一并宣称关闭，组织旧记录状态/排序展示、模块表单排序、备注VO补充、在线字段、角色缺参及资源语义仍需各自确认或协同。

## VSCODE-JAVA-STARTUP-20261018

状态：只读诊断完成，已找到 EVA 工作区真实注解处理器导入失败；没有配置/源码修复授权，不自动实施。全部命令结束，交锁。仅追加本诊断记录，保护已有 Maven 版本准备、已 QA 后台修复、他人 CacheCodes/WebBooter/.vscode/design/QA 卡及原 GO。没有启动或停止应用服务、改 User/workspace settings、升级扩展、清理 LS 缓存、改构造器/JDK/wrapper、禁用 Lombok、Git/DB 操作。

### 已证实的工具与工作区

当前已装 redhat.java 1.57.0、Java Debug 0.60.0、Java Test 0.47.0、Gradle for Java 3.18.1、Pleiades JDK pack 2026.624.0；Java 扩展自带 changelog 在 1.46.0 已记录支持 Java25，不能说当前插件不支持Java25。匹配 workspaceStorage 是 13dd2f51f76c0833abee7d4629c80c9a，其 workspace.json 明确 folder 为 EVA-API；没有读取其他项目日志。

只取 User settings 的 Java 白名单字段：Gradle Java home 指向 Zulu25；Gradle home 指向 Pleiades gradle/latest。实际该目录存在，--version 为 Gradle9.8.1、Launcher/Daemon Zulu25.0.3。项目 wrapper 声明9.6.1，User runtimes 有17/21/25/27且27为default；但运行中的本工作区主 LS JVM 是 redhat 扩展附带25.0.4.1，cache项目 .classpath 为 JavaSE-25、JDT compliance/source/target 均25，因此默认27不是已证实的本项目错误原因。未读取或输出 vmargs/环境秘密。

当前 .vscode/launch.json 有 Current File、NetUtils、LicenseBooter、WebBooter 四入口；WebBooter 的 mainClass=org.pkaq.WebBooter/projectName=web-booter 正确存在。没有代替用户运行任何入口。当前 CacheCodes 文件已是用户/总控修改后的 @RequiredArgsConstructor/@Getter、prefix初始化为common，不是本成员修改。

### 真实首因与诊断证据

工作区主 JDT 日志位置：C:/Users/A/AppData/Roaming/Code/User/workspaceStorage/13dd2f51f76c0833abee7d4629c80c9a/redhat.java/jdt_ws/.metadata/.log，当前 session 2026-10-18 16:50:21。16:51:27、20:04:38、20:04:39 三次真实错误都明确使用 Gradle distribution gradle-9.6.1-bin.zip，而非9.8.1目录，说明 wrapper 实际生效，不能凭 User home 版本差异认定主因。

对应调用链 GradleBuildSupport.syncAnnotationProcessingConfiguration → redhat.java/1.57.0/.../gradle/apt/init.gradle 第88行 collectApConfiguration 的 processors.addAll(apPath.getFiles()) → DefaultConfiguration.IllegalResolutionException：Resolution of the configuration ':eva-core:annotationProcessor' was attempted without an exclusive lock. This is unsafe and not allowed. 这是 Java LS 同步注解处理配置的实际失败，不是业务启动/数据库异常，也不是仅凭经验猜测 Lombok 未安装。

当前 LS 进程确实带 javaagent，文件名 lombok-1.18.39-4050.jar；java.jdt.ls.lombokSupport.enabled 默认true，User/workspace均未禁用。项目 Gradle catalog Lombok1.18.48，implementation+annotationProcessor声明存在。注解处理模型同步失败与 IDE 诊断不一致直接相关；尚不能仅据日志把本次 Run/Debug 最终失败唯一归于这一条，需要用户当前 Debug Console 的第一条错误及实际使用入口来闭合启动链。

同工作区 .markers.snap 真实保留 CacheCodes.java 的 The blank final field prefix may not have been initialized；主 LS 在21:59:04仍报告CacheCodes有1项问题，BizCode0项。另有 Java Test ASM9.10.1 bundle安装失败/依赖闭包不能刷新，以及 Spring Boot 扩展 classpath listener 10秒超时；作为并存工具异常记录，不把它们冒称 CacheCodes/启动唯一根因。未复制完整 JVM 参数、请求、token、密码或所有设置。

### 无服务启动的编译复核与后续选项

实际命令 gradle.bat :eva-core:eva-core-cache:compileJava --rerun --offline --no-daemon --console=plain，使用现终端 Gradle9.5.1/Zulu25.0.3，exit0、BUILD SUCCESSFUL in59s、3tasks全部executed；日志 .gradle/vscode-java-cache-compile-20261018.log。javap 独立确认当前 CacheCodes.class 存在 getMsg/getCode/getPrefix 与双String私有构造器，说明实际 Gradle/Javac+Lombok能编译当前源码，不应继续靠手写构造器掩盖 IDE 导入错误。原弃用/泛型/Gradle10警告保留，不称零warning。

候选最小动作应先修复/验证 Java 扩展的 Gradle APT 模型导入，再确认实际 WebBooter launch；可以经用户授权做仅本工作区导入链对照（如与已成功 CLI 一致的 Gradle9.5.1，以及明确wrapper是否继续生效）或选择修复该同步锁问题的兼容扩展版本。尚未用9.5.1 Tooling API重放同一APT模型，因此不能说改Gradle Home或降版本已经证明可修；也不能说清缓存或更换JDK已必要。任何设置变更/扩展升级/LS重导入均须总控依据当前诊断取得具体实施授权。本轮不改任何此类配置；若需补证，只需用户当前Debug Console第一条错误与点击的入口，不需要凭据或全量启动日志。

### 同版本模型重放补证（用户要求确认原因）

已在忽略目录 .gradle/VscodeAptModelProbe.java 建最小诊断，通过已安装发行包的 Tooling API，使用 model(Map.class)、EVA 项目路径、Zulu25 与 Java 扩展原始 apt/init.gradle（未修改）及 --offline，只请求模型而不执行应用任务。Gradle9.6.1 的安装路径为 C:/home/DevApp/cache/gradle/wrapper/dists/gradle-9.6.1-bin/4ticwg1pgcbps2hj28r8so764/gradle-9.6.1。重放和复核均得到与真实 LS 相同的 init.gradle:88、:eva-core:annotationProcessor、IllegalResolutionException/exclusive lock；复核进程 exit1，日志 .gradle/vscode-apt-model-961.log。这将原 LS 日志中的 APT 同步失败确认为可重现的模型导入故障，不是历史孤例。

同一当前源码用上述 Gradle9.6.1/bin/gradle.bat :eva-core:eva-core-cache:compileJava --rerun --offline --no-daemon --console=plain，exit0、BUILD SUCCESSFUL in21s、3tasks全部executed；日志 .gradle/vscode-cache-compile-961.log。保留既有弃用与 unchecked 警告。对照使用已安装 CLI9.5.1 的 Tooling API 和同一原始 init.gradle，仍 exit1 并在第88行出现完全相同独占锁异常，日志 .gradle/vscode-apt-model-951.log。因此9.5.1的普通编译成功不能推出其IDE模型正常，单改Gradle Home为9.5.1不是已验证的修复。

已证：Java扩展 APT 模型在此项目跨9.6.1/9.5.1均违反Gradle配置解析锁约束，普通9.6.1 Javac/Lombok编译则正常。合理关联但未闭环：导入失败可能影响编辑器注解处理识别及现存CacheCodes marker；不能宣称已经证明该marker的唯一成因。未证：当前用户点击的JavaDebug入口与实际启动终止首错；匹配workspace下未找到可用debug日志，不运行应用补造证据。请总控仅向用户收集当前点击入口及Debug Console第一条错误，再闭合启动链。未确认兼容修复扩展版本，也未改配置、升级/清缓存、手写构造器或变更JDK。

续诊前后 build.gradle、wrapper properties、CacheCodes.java、.vscode/settings.json、.vscode/launch.json 五项 SHA256 均与本轮基线一致；全部已有工作区修改保留。仅新增忽略目录诊断与追加本卡，git diff --check通过（既有LF/CRLF提示保留）；无新提交/推送、DB写或服务启动，诊断命令已结束并释放写入锁。

### Debug Java 入口核验（用户已确认入口）

只读本机 Java Debug0.60.0 的 package.json 与 dist/extension.js：编辑器右上角 Debug Java 对应 java.debug.debugJavaFile，调用 k(uri,false)，先等待 Java LS standard mode；无显式URI时取 activeTextEditor 的 Java 文件，经 resolveMainMethod 查询主类。非main文件可能回落最近使用launch或主类选择，因此“Debug Java”入口本身不能证明当时活动文件必为WebBooter。若解析出org.pkaq.WebBooter/web-booter，startDebugging按mainClass+projectName匹配launch.json，次选同mainClass但无projectName配置，否则创建临时launch，再交VSCode debug.startDebugging；不是简单执行当前侧栏选中的任意launch。

配置解析器在主类验证后、解析classpath前调用 buildWorkspace({mainClass,projectName,isFullBuild:false})；正确设置名是 java.debug.settings.forceBuildBeforeLaunch，扩展默认true，User/workspace当前均未覆盖。buildWorkspace执行 vscode.java.buildWorkspace。只反汇编已装server/com.microsoft.java.debug.plugin-0.53.2.jar（不执行扩展代码或调试）：JavaDebugDelegateCommandHandler转交Compile.compile，调用JDT BuildWorkspaceHandler.buildProjects，再收集目标项目及其classpath依赖项目错误marker，非空返回WITH_ERROR。这解释了为什么普通Gradle编译通过不能替代JavaDebug的JDT构建状态，也说明cache错误marker有机会影响WebBooter的依赖构建，但没有本次请求返回值，不能断言这次已触发。

关键实测配置：User settings未设置onBuildFailureProceed；工作区.vscode/settings.json现有 java.debug.settings.onBuildFailureProceed=true。该文件SHA256仍为1d2f52ad0eb3f5c37058d04dbb66a82764e867b4cb9e9a01a5f8434167311005，与上轮完全一致，是原有配置，本成员未改。扩展默认虽为false，但不能将默认冒充本项目实际值；true时普通构建失败路径跳过“Build failed, do you want to continue?”询问并继续，取消/Java扩展不可用仍可提前返回。因此现证据不支持“CacheCodes marker被默认构建门禁拦住了本次启动”的结论；若继续启动后运行到JDT错误字节码，也可能出现Unresolved compilation problem，但当前无真实首错，不能把可能性写成事实。

复查匹配workspace主LS与对应window的Java/Gradle/Spring日志，无本次Debug构建状态、所选main或启动终止首错可用。用户已提供入口，不重复追问入口；剩余仅需本次Debug Console/运行终端第一条实际错误或可见错误弹窗文字。未执行Debug/bootRun、未开端口、未关启动前编译或修改继续策略。五项源码配置hash仍保持基线；仅追加本卡与ignored反汇编诊断文本，命令结束交锁。APT模型9.6.1和9.5.1均失败、9.6.1普通编译成功结论保持，不推导降级修复。

### 原生电脑UI核验及JDT错误字节码

用户通过总控明确授权电脑调试后，完整读取 computer-use SKILL.md、guidance/api/confirmations，使用node_repl直接import @oai/sky；runtime实际可用，没有绕过cua native限制或自建helper。list_apps唯一返回VSCode窗口id526132、title WebBooter.java - EVA-API - Visual Studio Code，再用其真实对象选择和截图。没有操作Codex/扩展窗口、其他项目、认证或安全设置。仅点击Debug Console和Problems两个标签；未在终端点击/输入/滚动或执行应用，未按Debug重新启动。

原窗口活动文件确为WebBooter.java。首次截图自然显示原有集成终端Debug:We...页签，末尾Spring ClassFileAnnotationMetadata/ClassPathScanningCandidateComponentProvider栈与已返回EVA-API提示符；没有活动调试工具栏。该画面只能证明有过一次应用异常，尾部不足以确证首因或精确launch参数。技能guidance的Non-negotiable Windows Automation Safety禁止自动化terminal applications，因此未交互终端检索首栈。切换Debug Console为空，显示需要启动debug session才能求值；Problems共515，状态栏17errors/497warnings/1info，可见CacheCodes.prefix、SequenceGenerator.parser、CommonCodes.prefix、ResponseCodes.prefix、MybatisPlusDataPermissionHandler.ignoredStatementCache等blank final field Java错误。截图由工具原生展示，无保存文件路径，不虚报截图文件。

新增强证据（只读类文件，不初始化类）：eva-core/eva-core-cache/bin/main/org/pkaq/core/cache/CacheCodes.class确含Unresolved compilation problem和blank final field prefix文本；javap -c -p明确私有枚举构造器指令为new java/lang/Error、ldc对应错误、invokespecial Error.<init>、athrow。诊断文本.gradle/vscode-cache-jdt-bytecode.txt。相同当前源码的Gradle输出build/classes/java/main版本无该错误文本且普通编译通过。这将“当前marker → 实际JDT生成错误字节码”确证，不能再把问题仅当编辑器红线；onBuildFailureProceed=true下加载此class会抛Error。仍缺本次实际launch classpath及首个Caused by，不能断言已证明本次运行加载此文件。仅在TEMP中匹配cp_*.argfile/java-*.argfile/vscode-java*.argfile并过滤EVA-API，未找到匹配残留；未读取其他项目内容到输出。

建议先修复并验证Java扩展APT导入/增量编译链，使bin输出不再含错误字节码，再验证Debug Java；不建议继续手改CacheCodes、关编译验证或降9.5.1。没有实施修复、停止IDEA服务或新开后端。五项源码/配置hash仍与基线相同；命令和UI操作均结束，交回总控UI锁。

### 环境修复方案的实际验证与配置范围

用户要求“别改代码，修复它”后，未改任何业务源码、构建脚本、依赖版本或扩展源码。官方JDTLS问题 https://github.com/eclipse-jdtls/eclipse.jdt.ls/issues/3807 描述同样Gradle9并行多项目在apt/init.gradle:88的锁错误；当前项目gradle.properties已有org.gradle.parallel=true。诊断probe仅在忽略目录增加可选JVM参数，以原Gradle9.6.1、原init和同项目参数加-Dorg.gradle.parallel=false后exit0，MODEL_SUCCESS projectCount=18，日志.gradle/vscode-apt-model-961-serial.log。对照未加该参数的原锁失败保持，已验证关闭导入并行能解决本项目APT模型失败，不需要降Gradle或禁用AP。

实际redhat.java1.57.0元数据java.import.gradle.jvmArguments与java.import.gradle.arguments均scope=machine，不能可靠工作区覆盖；User settings两项当前均UNSET。可实施最小方案为备份后User级增加java.import.gradle.jvmArguments=-Dorg.gradle.parallel=false，仅影响VSCode Java导入并行度，CLI/IDEA和项目gradle.properties保持；workspace原onBuildFailureProceed=true恢复false。修改User级影响其他Gradle工作区，已向总控精确报告；总控已向用户询问该额外范围，当前等待答复，不把等待当批准。

本轮尚未修改任何User/workspace设置，未触发clean/reimport，未声称JDT bin或diagnostics已修复；无服务启动/停止/DB写或Git发布。获得范围确认后再备份、最小设置、使用官方Java clean/reimport命令，并以新LS导入日志/错误消失及bin javap无Error完成验收。当前命令结束，UI锁交回总控。

### 已授权实施后的暂停检查点

用户先明确允许User级导入参数和Java缓存重建，随后明确“等会再继续吧，我先弄个别的”。现立即暂停所有主动UI输入、下载、核验和轮询，不回滚已授权配置、不杀VSCode后台任务或用户服务；唯一VSCode UI锁交回总控。恢复必须等待用户/总控明确继续。

已完成配置：原User settings备份C:/Users/A/.codex/backups/eva-vscode-20261018/user-settings.before.json，原workspace备份同目录workspace-settings.before.json（含用户完整配置，禁止输出内容或提交）。User原java.import.gradle.jvmArguments未设置，现仅增加字符串-Dorg.gradle.parallel=false；workspace仅java.debug.settings.onBuildFailureProceed由true改false。JSONC解析差异断言各仅1属性变化。精确回滚为删除新增User属性、workspace false恢复true，须先核对后续用户修改；不要整份备份覆盖新设置。导入参数影响其他VSCode Gradle工作区导入并行度，不影响CLI/IDEA。未改任何JDK、Gradle脚本/依赖版本、业务源码、扩展源码或缓存路径。

通过实际官方命令Java: Clean Java Language Server Workspace并确认Reload and delete，完成一次clean/reload。新LS session2026-10-18 23:12，首次BuildServer导入下载Javadoc较久，23:39:07记录Updating classpaths for18projects/36buildtargets，23:39:19 Workspace initialized in1631199ms，23:39:23 buildjobsfinished，JavaReady；新session独占锁错误0，Springclasspathlistener OK。普通Gradle9.6.1 compileJava修后exit0 BUILD SUCCESSFUL1m27s（3tasks/1executed2up-to-date），日志.gradle/vscode-cache-compile-after-config.log。不能把此称完全修复：重导入后仍17项Java错误，CacheCodes/多个final字段错误继续存在。

第二根因已隔离确证：.gradle/vscode-lombok-probe/InitializedFinalProbe.java只有@RequiredArgsConstructor/@Getter、final message和已初始化final prefix。使用已装扩展1.57.0自带ECJ3.46.200.v20260930-0211与同一Lombok1.18.39-4050/JDK25/-25，exit-1复现The blank final field prefix may not have been initialized，日志result.log。故不是业务源码或Gradle缓存路径才产生此错误，是当前ECJ/Lombok组合兼容性。已通过官方Java: Force Java Compilation选择Full，暂停前最后画面70%、Found26errors/1620warnings（工作区显示会动态变化），尚未最终验收；此IDE后台任务不强杀。旧bin/main/CacheCodes.class仍含throw Error且旧mtime；当前BSP缓存.classpath实际src/main/java的output为build/classes/java/main，default output=bin，不能把旧bin未更新时间单独当当前产物失败证据；Gradlebuild/classes版本当时仍正常。继续时先确认全量编译结果与实际classpath输出，不盲删bin。

官方候选已形成可审查证据但未安装：Marketplace官方公开API显示redhat.java预发布1.58.2026101608，generic VSIX来源redhat.gallerycdn.vsassets.io，54,051,468bytes、SHA256 bf895fdc72584de37a167083ead16a0a2cb6385ce5d8b0cc891e8c6bd89a1301，落忽略.gradle/vscode-lombok-probe/redhat-java-1.58.2026101608-official.vsix。仅解出诊断ECJ，不装入VSCode；包内ECJ3.46.200.v20261008-1115，与已装相同Lombok。隔离同样例用候选ECJ+原已装Lombok/JDK25/-25，exit0，仅Unsafe弃用warning，candidate-result.log；进程已自然结束，没有遗留自有命令。官方Eclipse修复https://github.com/eclipse-jdt/eclipse.jdt.core/pull/5490（Oct8）与https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5489描述Lombok构造器兼容问题；官方stable latest仍1.57.0。候选小样例已通过但真实JDT运行未验证，预发布安装仍需用户另行确认，暂停期间不实施。若以后获准，影响所有VSCode Java项目，回滚可用官方扩展Install Another Version选择当前stable1.57.0；此仅方案未操作。下一次导入文档缓存现已下载，不能保证重建耗时。

用户新增共用缓存要求已只读核对：Machine/当前进程GRADLE_USER_HOME均C:/home/DevApp/cache/gradle；Userenv未覆盖，VSCode java.import.gradle.user.home未设置，IDEA项目gradle.xml无serviceDirectoryPath覆盖。原生inventory显示IDEA当前无运行窗口，未启动。实际IDEA idea.log在2026-10-18 15:42:18上下文含EVA-API及该共享home/modules-2的binary/sources路径；VSCode正在下载的micrometer/guava/spring-websocket的-javadoc.jar实际落相同共享home，与binary并存。共享依赖缓存已成立，Javadoc classifier首次补下载不等于另建缓存；各Gradle版本私有metadata和IDEA/JDT索引保持独立。Maven另指D:/DevApp/cache/maven，不与Gradle混同，未改。没有移动/删除/合并缓存。

暂停前build.gradle、wrapper properties、CacheCodes.java、launch.json哈希均与原基线一致，原WebBooter/已QA修改/GO保护；除授权settings与本状态/忽略诊断外无本次写入。无应用启动、服务停止、DBRedis写、commit/push或子代理。待恢复：先读当前工作区变化及IDE编译自然完成结果，再将官方候选和影响交总控供用户选择；未经明确继续和预发布安装授权不得切换扩展。

### 恢复后的有效产物、下载清理与版本选择（2026-10-19）

用户通过总控恢复，要求共用IDEA缓存、避免无意义下载、清理多余诊断下载、必要时重装扩展且不改代码。已fresh list_apps唯一选择EVA VSCode，实际窗口最小化，按API先activate/get_window刷新后读取；没有复用旧坐标。当前JavaReady，无FullCompile进度，仍17errors/497warnings/1info；全量编译任务已自然结束，blankfinal诊断未消失，不能报告修复完成。当前BSP真实缓存.classpath的src/main/java输出为build/classes/java/main，该CacheCodes.class存在且不含Unresolved compilation文本；旧bin/main类仍含Error但mtime未变，是旧输出，不能将它继续当本次有效产物。build.gradle、wrapper properties、CacheCodes.java、launch.json四项hash均同原基线。

没有改变共享GradleHome C:/home/DevApp/cache/gradle，没有移动、删除或合并共享缓存。核对已装redhat.java1.57.0与Gradle3.18.1实际配置schema，没有暴露Gradle Javadoc下载开关；java.maven.downloadSources/java.eclipse.downloadSources均针对其他导入器，java.hover.javadoc.enabled只影响显示，BuildServer仅on/off不等于文档下载开关。未写猜测的java.import.gradle.downloadJavadoc字段，也未为禁文档下载扩大到构建脚本或关闭AP/Lombok/编译验证；本轮未新增设置或反复clean/reload。

删除前已精确列出本次诊断文件名称/字节数/用途，并解析.gradle/vscode-lombok-probe为绝对目录；仅对DirectoryName等于该目录的两份冗余解包使用Remove-Item -LiteralPath：lombok-1.18.39-4050.jar 2,028,749bytes（与已安装文件重复）、org.eclipse.jdt.core.compiler.batch_3.46.200.v20261008-1115.jar 3,391,674bytes（隔离试验完成）。合计5,420,423bytes，均可从保留VSIX恢复。保留candidate-result.log/result.log、InitializedFinalProbe.java、诊断输出目录、设置备份及唯一必要候选redhat-java-1.58.2026101608-official.vsix 54,051,468bytes，后续若获准切换无需重新下载该包；未删工作区文件、用户依赖、扩展安装目录或共享Javadoc缓存。没有遗留自有诊断进程使用这些解包文件。

官方GitHub releases/latest API再次确认lateststable=v1.57.0，published_at=2026-10-13T12:57:58Z，与已装同版；重装同版不能根治已由隔离样例复现的ECJ兼容缺陷。唯一已验证候选仍官方预发布1.58.2026101608，原agent+候选ECJ隔离fixture exit0，但真实JDT尚未切换验收。用户“重装”未明确同意预发布，已将具体版本、影响全VSCode Java项目、可用官方Install Another Version回滚1.57.0及重建成本交总控征询；未安装/切换/降级。所有主动命令已结束，UI锁交回总控；无应用启动/停止、DBRedis、GPG、Git发布、源码/构建/依赖声明修改或下级代理。

## LOCAL-GRADLE-NO-WRAPPER-20261019

状态：用户授权的本地Gradle切换及精确Wrapper删除完成，命令结束、UI锁交还总控，供独立QA。当前cornerstone与本地upstream为0/0，未fetch/切分支/提交/推送；保护所有已有源码和设计/Maven/QA/GO修改。本次仅改workspace配置、删除唯一ignored Wrapper文本并追加本卡，没有业务源码、build.gradle、依赖声明或扩展版本变更。

已核实PATH Gradle命令C:/home/DevApp/gradle/bin/gradle.bat，实际Gradle9.5.1/Zulu25.0.3。Pleiades latest目录9.8.1不是此次选择。共享GRADLE_USER_HOME继续C:/home/DevApp/cache/gradle，User原-Dorg.gradle.parallel=false不变。当前运行的用户调试进程从原生VSCode画面可见，未启动、停止或操作其集成终端；本次没有重启Java或反复clean/reload。

执行前清单仅gradle/wrapper/gradle-wrapper.properties 238bytes（已读指向9.6.1），无gradlew/gradlew.bat/gradle-wrapper.jar；该文件被.gitignore忽略且不在Git索引，因此不能宣称从Git恢复。先备份C:/Users/A/.codex/backups/eva-local-gradle-20261019/gradle-wrapper.properties.before和workspace-settings.before.json，再用apply_patch删除唯一属性文件。其原SHA256为5f6039ba1fdd81614ecf3cbf75527431ebe027e9e92aa5504f50e15294b1d2f3。空wrapper目录无文件、无需递归删除；gradle/libs.versions.toml、用户安装包、共享Gradle缓存和先前设置备份全部保留。现存.gitignore Wrapper忽略规则保留，避免未来工具再生成后意外提交。恢复须在无后续用户修改时将备份属性复制回同原路径，并仅撤销本轮新增两设置；不可用reset/stash或覆盖他人变化。

实际已装schema支持java.import.gradle.wrapper.enabled为window boolean与java.import.gradle.home为machine-overridable string。Gradle3.18.1/BSP实际代码getConfigJavaImportGradleHome/Version/WrapperEnabled读取同一java配置并组装syncGradleBuild，配置变更触发refresh，故不需要改用户级home。workspace仅追加wrapper.enabled=false与home=C:/home/DevApp/gradle，保留onBuildFailureProceed=false；未设显式Gradle版本、未改Java/JDK或IDEA正常设置。自动刷新后的匹配EVA窗口Gradle for Java日志（Code/logs/20261018T224657/window1/exthost/output_logging_20261018T231113/2-Gradle for Java.log）明确先9.6.1后新Gradle Version:9.5.1，证明实际工具选择已切换，不仅是配置表面值。

影响检查：GitNexus对Wrapper属性目标返回UNKNOWN/未索引，不能当低风险；补全非生成目录literal扫描后无有效gradlew调用入口。README/脚本/Docker/CI无需改命令；.github仅dependabot Gradle生态配置。唯一剩余wrapper文本引用是docs/SECURITY_CAPABILITIES_VERIFICATION.md对旧命令不存在的历史事实与本状态历史记录，保持原证据，不将历史记录改写为当前命令。外部环境/CI以后需预先安装兼容本地Gradle，仓库不再携带Wrapper自动下载与版本锁定能力。

验证：C:/home/DevApp/gradle/bin/gradle.bat projects :eva-core:eva-core-cache:compileJava --offline --no-daemon --console=plain，exit0 BUILD SUCCESSFUL17s，4tasks全executed，日志.gradle/local-gradle-951-verify-20261019.log。真实AP模型以useInstallation(C:/home/DevApp/gradle)、Zulu25、-Dorg.gradle.parallel=false及当前已装Java扩展的原始apt/init.gradle重放，exit0 MODEL_SUCCESS projectCount=18，日志.gradle/local-gradle-951-apt-20261019.log。初次重放发现之前globalStorage解包临时路径已不存在，未当业务失败；改从当前已装org.eclipse.jdt.ls.core_1.62.0.202610131035.jar的gradle/apt/init.gradle原样提取到ignored诊断目录，SHA256 fc827f1ab7fd81c676b77c7d83c29871bd0d1b39281817021ccfe0513a1ead81，不改扩展源码、无需网络下载。最终断言workspace值正确、Wrapper无文件、catalog仍在；build.gradle/CacheCodes.java/launch.json hash保持原基线，diff --check通过，既有换行提示保留。

边界：本轮验证本地Gradle配置与AP导入/编译，不声称当前稳定Java扩展的ECJ/Lombok blankfinal诊断已修复，不启动新服务验证业务，不触碰DBRedis/GPG/发布或子代理。需要独立QA确认本轮最小配置与删除清单；若后续继续解决JDT兼容性仍走已记录候选授权流程。

## LOG-OPERATION-LINK-20261019

状态：只读诊断进行中，源码/DBRedis/HTTP/UI/服务均未写入或操作。QA保有唯一浏览器锁，后续修复须由总控分配写锁；用户最终修复后提交push授权已知，但此阶段不得提前发布。GitNexus query/context对业务日志未命中，索引落后21提交，改用当前源码/字节码证据，不能宣称图覆盖完整。

优先发现：UserCtrl.repwd注解args=param:0，RePwdBo为@Data且含originPassword/newPassword；现有编译产物javap确认toString模板含这两个字段。BizLogAdvice只对PARAMS/RESPONSE调用LogSanitizer，DESCRIPTION直接MessageFormat.format(description,formatArgs)，故重设密码操作会将BO字符串中的明文凭据写入描述路径，既有JSON脱敏不能保护该字段。仅静态和产物核验，未用真实密码调用或查看真实日志内容；应先修复再由QA使用非真实测试凭据验证，不能为本任务手工补日志。同期发现repwd/grant/grantPost/grantResource/grantUser皆EDIT默认0.id，但对应BO无id，会被processOperatorType回落CREATE，日志操作类型失真。已提交总控协调，未自行改代码。

### LOG-DESCRIPTION-SECRET交付（2026-10-22恢复收口）

状态：已按总控后端唯一源码写锁完成最小日志安全修复及26项定向回归，命令全部结束，释放后端源码锁，等待独立安全QA及运行态操作回归。用户已重启的服务未被本成员启动、停止或改密；无浏览器/UI、真实凭据读取、数据库/Redis写入、手工造日志、依赖安装、环境/GPG/Maven操作、提交或推送。全任务验收后发布授权保留，由总控统一安排，不提前发布。

生产差异5文件：BizLogAdvice.java、LogSanitizer.java、BusinessLogEventHandler.java、UserCtrl.java、RoleCtrl.java。测试差异3文件：新增core-log/pointcut/BizLogAdviceTest.java、新增sys/BizLogOperationTypeTest.java，补充原LogSanitizerTest.java；本状态卡追加结果。原build.gradle/Maven版本准备、CacheCodes/WebBooter/.vscode、已QA15项业务修复/设计和GO均保护，未扩大到字典/权限实现。图impact对Advice/Sanitizer/Handler未命中、UserCtrl/RoleCtrl返回UNKNOWN且索引落后21提交，未当作低风险；用全部当前注解调用/字段/测试链补证，图覆盖限制保留。

共享根因修复：描述格式化前对整对象仅投影id/userId/roleId/tenantId/mainId及批量param的数字标识，不再直接调用未知业务对象toString；显式凭据字段路径统一隐藏，描述最终脱敏并限4000字符，保留操作主体、目标数字ID及成功/失败信息。参数/响应仍保留安全业务信息。修正同一BO多个字段作为格式参数时仅末项被赋值的问题，空bizId不再在finally抛NPE掩盖业务异常；反射摘要失败不再输出可能含参数的异常消息。回滚事件显式success=false，避免先成功再整体事务回滚时仍被标记成功。改密、用户授角色/授岗位、角色授资源/授用户五个接口明确UPDATE，角色授权描述仅使用roleId；不改请求/返回、日志表、权限或业务处理。

恢复后新增嵌套凭据回归先复现3项失败，不能沿用中断前21pass当完整安全结论。旧正则对凭据数组和编码JSON字符串处理不完整；现LogSanitizer复用已安装Jackson，按JSON字段结构将凭据值整体隐藏，递归处理对象/数组及编码JSON，保留可解析JSON和安全字段；过深内容在32层隐藏，避免无界递归。普通文本继续原JSON片段/URL参数/Authorization规则；解析异常不回显原始内容。不宣称能从无标签任意文本推断哪些字符是秘密。首次补修后唯一失败为测试将项目Long字符串ID误当Integer，已按现序列化契约校正，未因此改产品ID类型。全部验证仅合成canary，未读取或展示真实凭据。

最终命令：C:/home/DevApp/gradle/bin/gradle.bat :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest -x jar --offline --no-daemon --console=plain。exit0 BUILD SUCCESSFUL33s，14tasks/3executed11up-to-date，两模块test均强制executed；XML9+7+1+1+4+4=26项，0failure/error/skipped。真实Advice→事件→实际BusinessLogEventHandler→mock仓库最终全部字段校验原/新密码与token消失，覆盖成功、业务异常不被替换、回滚、租户/操作者、空ID、创建/更新、返回对象、嵌套凭据数组及编码JSON，保留目标ID且params/response可解析。源码实际编译已完成，最终轮未改生产源码；日志.gradle/log-description-secret-final-20261022.log，失败基线.gradle/log-secret-nested-baseline-20261022.log。10-19完整打包曾被用户进程锁住upload/cache/log三个JAR而失败，故本次仅跳过jar重打包、复用依赖既有包；当前修改的core-log用自身最新classes测试，sys控制器在其自身classes验证，未跳过修改模块的测试。不能称全量build/可发布打包通过，JAR重建须总控与用户进程协调。diff --check通过，既有换行/弃用/JVM警告不冒称零警告。

### 系统操作与业务日志覆盖矩阵（源码预期，待QA实际操作）

| 模块/操作 | 当前应产生日志类型 | 覆盖和引用联动边界 |
| --- | --- | --- |
| 用户新增/编辑、删除、列表/详情、冻结 | C/U、D、R、U | 唯一性校验无BizLog；改密现明确U，用户角色/岗位关系变更现明确U并记录目标userId |
| 角色新增/编辑、删除、列表/详情、冻结 | C/U、D、R、U | fetchResource/listUser为R，grantResource/grantUser现明确U；checkUnique无BizLog |
| 组织新增/编辑、删除、列表/详情、排序/冻结 | C/U、D、R、U | checkUnique无BizLog，父子层级及关联岗位/用户仍需QA专用数据回归 |
| 岗位新增/编辑、删除、列表/详情、排序/冻结 | C/U、D、R、U | checkUnique无BizLog；所属组织和用户岗位引用需一起验证 |
| 模块新增/编辑、删除、树/详情、排序/冻结 | C/U、D、R、U | 资源定义随ModuleAoeBo.resources由同一次模块edit差异同步，只产模块操作日志，无独立资源CRUD日志行；删除/更新资源触发角色缓存刷新事件不等于另产业务日志 |
| 字典新增/编辑、GET/POST删除、冻结 | C/U、D、U | list/get/query/byCode/validate/checkUnique无BizLog，不能期待查询R；根归属/双根PID契约待设计，不在本次迁移 |
| 通知新增/编辑、删除、列表/详情、冻结 | C/U、D、R、U | 公共notices查询无BizLog |
| 租户与租户套餐增改删查/冻结 | C/U、D、R、U | checkUnique无BizLog；平台查看租户选项/组织/角色为R；不可将standalone当前账号通过当跨租户隔离通过 |
| 在线/登录/接口/异常/业务日志查看 | 在线强退U，其余查看无BizLog | 登录/接口/异常有各自日志链，不能用业务日志代替；读取业务日志本身不递归产生日志 |
| AI提示词生成 | R | 当前按查询类型记录，不代表代码已生成或执行 |

C/U为EDIT依据首参id识别，不含此次已改为明确U的五操作。现所有sys控制器日志注解均未配置bizId，不能要求按B_ID必然查到这些操作；B_ID仅在显式注解配置时赋值。mCode来自ThreadUser.moduleCode，但当前认证buildUser未设置该字段，模块筛选可能空；尚未查真实日志证明现值，QA先按操作者/时间/目标描述和方法对应，不能手写补齐数据。校验绑定失败或资源拒绝发生在Controller前，不进入其切面，因此不保证业务日志行；到达注解方法的业务异常才由finally发事件，失败应false/失败前缀。没有@BizLog的方法不因日志开关打开而自动覆盖。

链路与查询：配置文件eva.bizlog.enabled=true（不是当前进程有效配置的替代证明）→BizLogAdvice→BizLogEvent→AFTER_COMMIT fallback/AFTER_ROLLBACK + log_task异步池→PostgresBusinessLogRepository→LOG_BIZ。池2/4线程、队列256、满时CallerRuns；存储失败只记应用错误、不破坏业务，无持久队列/自动重试，因此不能宣称绝不丢日志或立即可见。列表返回Response.data内PageVo.records/total/current/size，详情返回JSON字符串；默认近7日，含归档默认true。日期参数yyyy-MM-dd的end转00:00:00且SQL<=end，同日筛选有排除当天白天操作的边界，需QA核实际请求，不把空列表直接判生成失败。归档按默认6月、每日02:20、批1000/最多100批及PG advisory锁迁至LOG_BIZ_ARCHIVE；此轮未触发归档或执行会写真实PG的BusinessLogIntegrationTest。租户查询standalone固定0，租户模式当前租户，平台管理员可选目标租户/跨租户，不能据源码代替受限账号验证。

权限边界：真实JWT请求经ResourceAuthorizationService→RoleResourceCacheService按tenant/角色、HTTP method和Ant路径判定，旧星号目录资源兼容子路径放行；/sys/organization不应误匹配organizationExtra，受限账号需验证方法与精确路径，并排除旧宽授权覆盖，不能把现管理员通过判收敛。旧DynamicSecurityMetadataSource是并存实现，不将它的strict=false全局集合逻辑冒充当前JWT实际入口。数据权限由角色dataScope进入ThreadUser，MybatisPlusDataPermissionHandler拼SQL：0000全量、0001部门、0002部门及下级、0003指定部门、0007本人创建或修改；0004/0005/0006岗位类型虽枚举存在但当前switch未实现，结果回落拒绝，不得宣传全覆盖。无ThreadUser/排除表/Ignore路径另有边界；LOG_BIZ/ARCHIVE DDL没有CREATE_ID/MODIFY_ID，当前数据权限排除表和BusinessLogMapper亦无Ignore，受限范围访问日志有生成不存在列条件的源码风险，未用真实受限账号复现，不擅自扩大权限或修改审计访问策略。

DATA_SCOPE依赖缺口：前端角色DictSelector确用DATA_SCOPE且required，默认/请求归一化0000；后端RoleAoeBo/VO描述同编码，RoleService缺值默认0000，枚举0000=ALL。设计成员报告该字典主项/8明细已逻辑删除，可能令权限编辑空选；已有角色隔离仍按保存dataScope枚举执行，不因字典删除自动失效。空值保存默认ALL有扩大风险，应由总控结合具体契约/真实受限账号确认，不擅自复活字典、改角色或真实权限。

运行版本边界：10-19只读发现用户WebBooter PID40004启动21:50:32，LogQueryBo日期注解产物21:49:36、ModuleConvert产物21:50:24早于其启动；仅为产物/启动时间证据，不能证明每个已加载类或当日新修复生效。10-22用户报告已重启，不擅自再次重启或读取凭据；本次修改的日志安全补修必须由总控协调当前应用加载/重建并由QA真实操作验证。建议独立QA先复审合成canary与当前差异，待确认运行版本后再使用专用测试数据验证各操作；不以真实密码或旧日志秘密作取证。

### P1-LOG-SECRET-TEXT-FALLBACK（2026-10-22补修交锁）

状态：补修及定向回归完成，所有命令已结束，写锁交还总控组织独立复验；本成员不自证独立通过。已读取quality_reviewer-log-secret-20261022.md的四个真实binary合成探针，承认此前26项只覆盖合法JSON，不能证明文本回退安全；未查询真实日志、密码或数据库。

本次增量只改LogSanitizer.java、BizLogAdvice.java及其LogSanitizerTest/BizLogAdviceTest和本卡。移除JSON_SECRET_PATTERN猜值边界回退；完整JSON启用FAIL_ON_TRAILING_TOKENS后递归按字段脱敏。非完整JSON中存在对象起始符、引号、反斜杠，或带凭据标签的数组结构时保守整体隐藏，不尝试提取部分数组、不新增窄regex、不记录解析异常原文。合法外层JSON里的TextNode递归也遵循同一失败闭合路径。既有URL查询/header规则、长度与32层投影限制保留；超出Jackson解析深度也整体隐藏。不推断无标签秘密，不做Base64解码。可用性代价明确：带前缀的安全JSON片段、引号/反斜杠等无法可靠解析的结构化文本也可能隐藏。

描述参数若为完整结构化字符串则在插入模板前隐藏该参数；整对象描述仍只投影预先验证的数字ID/数字集合，输出改为Class[id=数字]形式，不重新嵌入JSON。原因是最终模板内嵌JSON已不是完整JSON，会被保守规则整体隐藏；这样保留独立目标ID/普通模板，不调用未知对象toString。现有多字段、空bizId、事务回滚false及原业务异常原样抛出保持。

新增11项测试：Sanitizer新增6项（前缀token数组、Unicode转义键、不完整数组、合法外层不可信TextNode、尾随内容/1100层解析失败、普通数字目标描述及不假设Base64）；Advice新增4项，每项四类之一分别走成功/原异常/finally/实际rollback事件监听器至最终mock BusinessLogRepository并逐实体全部字段断言无合成canary，保留目标31与失败标识；另1项四类逐一走真实ErrorIncidentEventHandler到mock ErrorIncidentRepository，验证params/summary及实体全部字段无canary并保tenant。未把只测sanitize返回值充当最终writer证据。

首轮回归有1项失败：合法JSON备注在最终描述中触发整体隐藏，目标ID被一同隐藏；已以“结构化字符串仅隐藏参数+数字对象投影用普通文本”修正，未削弱canary断言。首轮日志.gradle/log-secret-text-fallback-20261022.log。最终命令：

```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest -x jar --offline --no-daemon --console=plain
```

最终exit0、BUILD SUCCESSFUL 1m49s，14tasks/4executed/10up-to-date；实际执行core-log生产compileJava、compileTestJava及两个模块test。XML时间2026-10-22T13:30:21Z至13:30:42Z：Advice14+Sanitizer13+注册1+操作类型1+Module4+WriteProtection4=37，failures/errors/skipped均0。最终日志.gradle/log-secret-text-fallback-final-20261022.log。git diff --check exit0；既有CRLF/JVM/Gradle弃用warning仍在。-x jar仍仅绕开运行服务锁住的包装任务；没有完整打包成功或当前进程已加载新类的证据，不kill/restart服务。

权限P0只读计划已报总控：RoleService新增缺scope拒绝，更新null保真实原scope/orgIds，显式blank/invalid及未实现岗位新授拒，0003全量合法本tenant组织校验；两日志Repository读入口有效身份+ALL才沿原tenant条件访问，其余用现4200拒绝，拦截器仅相关日志查询失败闭合、不全局Ignore。前端三个ALL默认需同批配套。该组RoleService/Repository/Handler生产文件本轮均无diff，未混入本P1。具体日志本人/部门语义、错误聚合归属、真实授权和DATA_SCOPE恢复仍不自行决定。所有用户既有dirty、Maven/CacheCodes/WebBooter/GO/设计与前端锁保持；无依赖、DB/Redis/HTTP、UI、vault写入、提交推送或子代理。

### P1-LOG-SECRET-LINEBREAK-KEY（2026-10-22补修交锁）

状态：本阶段补修及定向验证完成，全部命令结束，写锁交总控派独立安全QA；不自证独立通过。已读最新QA节，原TEXT-FALLBACK四探针已闭合，而合法JSON的token尾LF可绕过敏感字段判定。本阶段只增量修改LogSanitizer.java及直接LogSanitizerTest/BizLogAdviceTest、本卡，不动Advice生产逻辑、RoleService/两日志Repository/数据权限guard或前端。

根因与修复：共享SECRET_FIELD_PATTERN原先以外层.*配合matches匹配整个字段，点号不跨Java行终止符。现移除外层.*，isSecretField统一用find查找已有敏感标签；数组文本回退调用同一isSecretField，保持同一判定源。标签集合和大小写规则不新增，敏感标签在字段中出现即识别，字段前后有换行不再绕过。没有删除标签内部字符或推断任意Unicode混淆/编码。strict完整JSON、保守文本回退、深度/长度、目标投影、原异常抛出和rollbackfalse均保留。

新增4项回归，逐一循环LF、CR、CRLF、NEL(U+0085)、LS(U+2028)、PS(U+2029)、VT、FF：共享识别覆盖全部现有敏感标签及前后边界，并保留普通字段/null阴性；JSON覆盖嵌套对象、数组凭据及编码JSON，保留普通目标32；实际Advice→事件→BusinessLogEventHandler→最终mockRepository全实体无合成canary，含直接params/response及嵌套编码文本的success/failure/finally/rollback，保留独立目标31；实际ErrorIncidentEventHandler→最终mockErrorIncidentRepository的params/summary及全实体无canary，保tenant44。不是只测独立正则。

先只添加测试、未修生产时，4项全部失败（包含真实两个writer断言）；日志.gradle/log-secret-linebreak-red-20261022.log，exit1/BUILD FAILED51s，5tasks/2executed。随后共享匹配三处最小修改，强制完整原37+新增4全部通过：Advice16+Sanitizer15+注册1+操作类型1+Module4+WriteProtection4=41；XML2026-10-22T13:43:52Z至13:44:02Z，failure/error/skipped全0。最终命令：

```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest -x jar --offline --no-daemon --console=plain
```

最终exit0/BUILD SUCCESSFUL1m15s，14tasks/3executed11up-to-date，生产core-log compileJava及两个test任务真实执行；测试compile在red阶段已实际执行，green复用同一未改测试产物。日志.gradle/log-secret-linebreak-final-20261022.log。git diff --check exit0/index空，既有CRLF/JVM/Gradle弃用warning保留。-x jar仍避开被用户服务锁住的包装，不宣称完整打包/当前进程已加载修复。未用真实凭据或读库日志；无DB/Redis/HTTP、UI、服务重启、依赖、vault写入、提交推送、子代理。其余用户dirty与权限P0待办保持。

### P0-ROLE-SCOPE-LOG-QUERY-GUARD（2026-10-23交锁）

状态：按总控正式锁完成原设计第9节安全收紧与定向验证，所有命令结束，后端写锁交还总控派独立复验；不自证独立QA或运行通过。分支cornerstone，保留本轮前所有dirty。仅5个生产文件（RoleService、LogQueryAccess新文件、PostgresBusinessLogRepository、PostgresErrorIncidentRepository、MybatisPlusDataPermissionHandler）与4个直接测试文件（RoleServiceTest、ErrorIncidentRepositoryTest、BusinessLogRepositoryTest新文件、LogQueryPermissionInterceptorTest新文件）及本卡；原日志脱敏生产/测试未修改。

角色：去DEFAULT_DATA_SCOPE=ALL。新增null/blank/非法/0004/5/6拒PARAM_ERROR，写入及权限版本更新前校验；更新在原@Transactional+TenantSchema方法读取ensureRoleEditable返回的真实原记录，未传/null scope保原scope与dataOrgIds（忽略同期伪造的新orgIds）；原null/invalid/未实现值原样保留，不因改名提升ALL。显式blank/invalid/未实现范围新授仍拒。明确已有/新提交合法0000不改。相同0003但orgIds未传时保原orgIds并校验，切换0003必须非空；ID逗号分段完整解析、正long、溢出/非法字符/空分段/部分不可见全部拒绝，不静默剔除非法项。OrganizationMapper按完整ID集合+显式可信tenant_id过滤，再比对全集合；standalone固定0，其余ThreadUser tenant，并仍处于原TenantSchema路由和原数据权限可见性内。没有客户端tenant/schema字段、新授权上限或新枚举语义，原permVer失效路径保留。

日志：两个Repository的list/get开头复用LogQueryAccess.requireAll，必须当前ThreadUser userId>0、tenantId>=0且至少一个明确0000范围；缺身份/范围、unknown或仅受限范围用现CommonCodes.PERMISSION_DENY(4200)，count/page/get均未调用Mapper。有效ALL与受限多角色并集保持ALL，null范围项不崩溃；接口资源许可仍由原JWT入口检查，不靠篡改角色/Ignore模拟许可。业务日志standalone0、普通可信本tenant、原platform管理员target规则及错误日志原tenant过滤均不变。MybatisPlusDataPermissionHandler只对相关日志表和BusinessLogMapper.select*/countLogs、ErrorIncidentMapper.select*（含分页count）读语句前置同守卫，早于通用exclude/disabled，不生成CREATE_ID/MODIFY_ID。无全局Ignore或excludeTables变更；archiveBefore/tryArchiveLock、匿名save/upsert与其他表后台读取不受本守卫影响。没有新增本人/部门审计日志语义、错误聚合个人归属、迁移或DB/API字段。

测试：RoleServiceTest11覆盖新增缺值/空/非法/未实现零写、编辑遗漏保原ALL/部门/自定义/null/invalid/岗位原值，显式空非法更新拒绝，自定义非法/部分不可见拒绝，相同自定义保组织和刷新受影响permVer，显式ALL可保存。另用真实Spring事务拦截器+实际TenantSchemaTransactionalAspect/Router+mock JDBC与resolver，证明组织校验在实际事务、可信tenant_22上下文且SQL带tenant_id=22，组织不可见则rollback、无角色写/权限版本写，finally恢复上下文；这是JVM模拟数据库连接证据，不是真实PG跨租户验收。

LogQueryPermissionInterceptorTest通过实际DataPermissionInterceptor.beforeQuery和真实BusinessLogMapper.xml生成的detail/count/hot+archive UNION SQL，验证匿名/受限拒绝与ALL无无效审计列、保tenant条件；错误日志selectOne/List/Page/生成count/Count在通用数据权限关闭时仍拒缺身份/unknown。BusinessLogRepositoryTest4验证匿名/缺范围/invalid身份/八类受限或unknown全Mapper零调用，detail/count/page的本tenant/两种includeArchived、standalone0和原platform目标规则及多角色ALL，匿名写入不变；ErrorRepositoryTest6保get/list可信tenant、standalone0、匿名/受限零调用和匿名upsert。既有数据权限8、目标租户执行2与日志安全41一起回归。

最终命令（cwd固定EVA-API）：
```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' :eva-core:eva-core-data-mybatis:test --rerun --tests org.pkaq.config.MybatisPlusDataPermissionHandlerTest --tests org.pkaq.config.LogQueryPermissionInterceptorTest --tests org.pkaq.core.mybatis.log.PostgresBusinessLogRepositoryTest --tests org.pkaq.core.mybatis.exception.PostgresErrorIncidentRepositoryTest --tests org.pkaq.core.mybatis.tenant.TargetTenantExecutorSecurityTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.role.service.RoleServiceTest --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest -x jar --offline --no-daemon --console=plain
```

最终exit0 BUILD SUCCESSFUL44s、17tasks5executed12up-to-date；最终实际sys生产compileJava/compileTestJava及三个模块test执行，core-data生产compile在本阶段首轮实际执行后未变。XML北京时间2026-10-23 07:22:05至07:22:19：日志32+coredata22+sys20=74，failure/error/skipped全0（含原41）。日志.gradle/p0-role-log-guard-final-20261023.log。首轮p0-role-log-guard-20261023.log失败1项为平台管理员fixture误填roles而非项目实际rolesMap；第二轮新增事务fixture用接口代理导致ClassCastException，均修fixture不改生产授权；临时第二轮日志被最终同名输出替代，失败原因仅据本会话工具记录，不伪造保留文件。最后补显式组织tenant条件及实际事务SQL参数断言后完整74重新执行通过。git diff --check exit0、index空；既有CRLF/JVM/Gradle warning及RoleServiceTest泛型unchecked提示保留。

限制/交接：-x jar仍因用户运行服务锁住既有包装，单测使用模块自身编译类，未做完整JAR重建/当前运行类加载验证。实际UI/HTTP、无旧宽授权的受限身份、真实PG读写与跨租户隔离仍需总控在确认运行版本后安排；未读真实日志或秘密。前端三个ALL默认需由前端成员同批收口，不能仅此后端通过称全流程完成。未碰字典/真实权限/角色数据/Redis/依赖/vault/前端/服务/Git提交推送，无子代理；设计中待确认的字典迁移和日志长期归属政策未实现。

### P1-ROLE-FROZEN-ORDINAL及确认同根转换（2026-10-23交锁）

状态：总控现场P1补修及追加同根User/Tenant/Package转换任务完成，所有命令结束，写锁交回总控派独立QA。未操作现场数据或服务。此次增量5生产：role/convert/RoleConvert.java、role/service/RoleService.java、user/convert/UserConvert.java、tenant/convert/TenantConvert.java、tenant/pkg/convert/TenantPackageConvert.java；3测试：新增sys/RoleFrozenMappingTest.java、sys/AccountTenantFrozenMappingTest.java，追加role/service/RoleServiceTest.java；及本卡。其他既有dirty/P0/log安全源码保留。

证据/归因：已读前端QA runtime-20261019卡现场最新P1节。StdBo.frozen是Integer业务码，FrozenEnumm次序FROZEN(1)、UN_FROZEN(0)、READ_ONLY(9999)，原RoleConvertImpl.fromBo(RoleAoeBo)188行为values()[getFrozen()]，0/1被反转，9999数组越界。RoleConvert本次前无diff，HEAD同一未配置映射；声明blame0e6b0cf33（2025-11）及更早路径，最后文件提交71c2c262（2026-08-24），Service调用同为既有链路；本P0未新增ordinal逻辑。生成证据与现场一致，未声称读取过实际请求体或DB。

修复：四个Convert按已有Module/Organization正确范式显式Mapping，复用BaseEnum.of(FrozenEnumm.class, code)，不改enum顺序/生成文件/BO/字段与API。RoleAoe、UserAoe、UserQuery、TenantAoe、TenantPackageAoe五个整数入口按0/1/9999映射，null码仍null、非法码按现BaseEnum异常拒绝；表达式显式处理来源BO为null，保MapConvertConfig.RETURN_DEFAULT原返回默认实体的语义，不因表达式移出MapStruct null块引入NPE。当前生成RoleConvertImpl205、UserConvertImpl121/151、TenantConvertImpl64、TenantPackageConvertImpl43已是BaseEnum.of带null来源保护；sys生成ConvertImpl全域搜索FrozenEnumm.values无结果。没有手改生成物。

RoleService在既有同事务origin校验后，普通编辑转换实体追加保origin.frozen的4行，防未传/陈旧或伪造冻结输入影响普通name/remark编辑；系统只读仍先ensureRoleEditable拒，专用switchFrozen原实现未修改。P0 scope/org/permVer与资源/用户引用链不绕过。真实生成转换的Service测试覆盖原正常/锁定各输入null/0/1/9999，name/remark正常保存且trusted frozen、原scope/org保留，permVer仍刷新；只读原记录及非法码2拒绝零角色/权限写。另加专用switchFrozen真实Service回归，正常与锁定可切换，只读跳过，参数仍为明确FrozenEnumm。

同根追加先核源码/BO/生产caller/历史：UserService.saveUser调用UserAoe转换；UserQuery.frozen明确Integer状态且有同根生成入口（当前查无生产caller，仍保持同一声明契约，仅字段映射）；UserTenantAdminBo无frozen，不碰该入口。TenantAoe继承StdBo、TenantEntity继承StdEntity，TenantService.edit调用；TenantPackageAoe/Entity同Frozen契约，PackageService.edit先Convert再既有resolveFrozen，所以即使后面覆盖也会先被9999旧ordinal阻断。User声明存量7d70dd639/8f1adb10d（2025），Tenant声明7fe87eb3f（2025），Package文件既有71c2c262（2026-08-24）。三服务未盲加origin覆盖：Package本就显式resolveFrozen允许合法状态更新，Tenant/User亦保原业务校验与状态接口，不新增业务政策。仅Role有本现场普通编辑保持状态要求和复现，扩大范围经总控正式授权后顺序实施，无同仓并发测试。

验证分阶段：Role先82项通过（.gradle/role-frozen-ordinal-20261023.log，1m16s/17tasks5executed）；同根及空BO边界、专用开关补齐后最终完整97项强制执行。RoleFrozenMappingTest2、AccountTenantFrozenMappingTest4各实际Mappers.getMapper调用生成实现，覆盖0/1/9999/null字段/nullBO/非法-1,2,3,9998,10000,MAX；RoleServiceTest由11增为14，原11保持；其他UserAccountProfile8、TenantServiceCompatibility2、TreeFrozenMapping2/Edit2、Module4、WriteProtection4、操作类型1及coredata22、corelog32。总97，failure/error/skipped全0，XML北京时间10-23 17:11:14至17:11:22。

最终命令：
```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.RoleFrozenMappingTest --tests org.pkaq.sys.AccountTenantFrozenMappingTest --tests org.pkaq.sys.role.service.RoleServiceTest --tests org.pkaq.sys.TreeFrozenMappingTest --tests org.pkaq.sys.TreeFrozenEditTest --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest --tests org.pkaq.sys.user.service.UserAccountProfileServiceTest --tests org.pkaq.sys.tenant.service.TenantServiceCompatibilityTest :eva-core:eva-core-data-mybatis:test --rerun --tests org.pkaq.config.MybatisPlusDataPermissionHandlerTest --tests org.pkaq.config.LogQueryPermissionInterceptorTest --tests org.pkaq.core.mybatis.log.PostgresBusinessLogRepositoryTest --tests org.pkaq.core.mybatis.exception.PostgresErrorIncidentRepositoryTest --tests org.pkaq.core.mybatis.tenant.TargetTenantExecutorSecurityTest :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest -x jar --offline --no-daemon --console=plain
```

最终exit0 BUILD SUCCESSFUL23s，17tasks4executed13up-to-date，sys compileTest及三个module test真实执行；四Convert及RoleService生产compileJava/MapStruct在此前完整30秒通过轮实际执行，最终仅追加测试生产未变，所以复用。最终.gradle/frozen-code-mapping-final-20261023.log。git diff --check0/index空，既有CRLF/Gradle/JVM及泛型unchecked/旧测试deprecated提示保留。不宣称lint/fullbuild/JAR或当前用户进程已加载；-x jar延续避运行包装锁，无启停服务或新依赖。

运行/恢复待办：R01/2111725377803354113当前现场临时remark EVA_QA_20261023_R01_可恢复回归、缺陷冻结1；原remark EVA_QA_20261018_角色已编辑、原冻结0。此次未恢复，修复后的普通备注编辑会保现有1，不会自动把历史缺陷状态改回0；需总控确认新运行类加载后，由QA按明确精确恢复方案分别恢复原备注/原状态并回读，不能声称已回滚。其scope界面仍0000；隐藏组织关联未读不补造。其他用户/租户/套餐运行写验、只读/授权边界及实际新JAR加载仍待独立QA，不以mockService/生成测试代替真实页面通过。无真实secret/DBHTTPRedis写、前端/QA卡/vault/依赖/服务/Git提交推送或子代理。

### 全sys/系统日志逐项验收矩阵：只读盘点（2026-10-23）

本次遵总控在QA独立97项复验窗口只读准备；未修改生产/测试源码、未跑测试/构建或并发编译，未访问HTTP/DB/Redis/凭据/日志全文，无服务/依赖/vault/前端/Git写入。仅追加本状态卡。下列是当前源码能力清单，不是当前运行进程的路由清单，更不是全CRUD/权限已验收。只读确认sys目录14个Controller、78个Get/Post等方法注解，认证模块日志3个Controller另6个方法，共84个源码映射；平台inspect受eva.mode=platform条件约束。RoleCtrl覆盖StdCtrl同签名方法，未重复计入父类CRUD；其他Ctrl基类无默认CRUD。

表中G=GET、P=POST；每个路径都必须与HTTP方法一起验收；edit是现有新增/修改共用入口，不臆造REST PUT/DELETE。共用后缀必须逐项执行，不能只测一条edit代替所有CRUD。

| 领域/根路径 | 现有准确能力与路由后缀 | 联动/验收边界 |
| --- | --- | --- |
| 账号 /sys/account | G /list、/get/{id}；P /checkUnique、/edit、/del、/switch、/repwd、/grant、/grantPost | 账号与可选档案同ID；部门、岗位、角色授权和候选回填；分页/搜索/空值；当前账号/系统只读保护、乐观锁失败不单改档案与permVer；改密是本人操作，不能以批量重置任意账号代替；0/1状态及null/非法边界 |
| 角色 /sys/role | G /get/{id}、/list、/fetchResource、/listUser；P /checkUnique、/switch、/grantResource、/grantUser、/del、/edit | 资源树授权与用户授权独立，listUser实参roleId/deptId；租户资源上限、数据不可见用户不被误删；scope/org缺省保留与新增必选、0003全量合法本tenant、0004/5/6不新授；ordinary编辑保状态而专用switch可切，系统只读拒绝 |
| 组织 /sys/organization | G /list、/get/{id}；P /checkUnique、/edit、/del、/sort、/switch | 父子/移动路径/叶子标志、同级排序、级联目标冻结；有子组织/关联账号删除拒绝，选择器与账号部门关系；旧O01恢复需核真实原值 |
| 岗位 /sys/post | G /list、/get/{id}；P /checkUnique、/edit、/del、/sort、/switch | 父子/路径/同级排序/级联状态，岗位用户引用清理及候选；与账号grantPost交叉回填；有子节点及只读保护，不能凭组织模块通过代替岗位验收 |
| 模块 /sys/module | G /get/{id}、/list；P /del、/edit、/checkUnique、/sort、/frozen | 父子树/移动/叶子/排序，注意状态路由为frozen而非switch，目标状态参数；普通编辑保原冻结；资源子表随同edit创建/修改/移除，mainId/外ID保护；删除角色引用及资源缓存事件，菜单刷新与按钮状态需另验 |
| 字典 /sys/dictionary | G /query/{code}、/list、/validate/{type}/{value}、/get/{id}、/get/type/{code}、/del/{id}；P /checkUnique、/del/{id}、/edit、/switch | 旧GET删除也是写能力，必须连同POST删除做权限/只读检查，不能因方法GET当安全查询。主项+明细在edit下联动、删除/回滚/缓存提交后刷新、code选择器/序列化旁路；DATA_SCOPE缺失不能自动授ALL。两根归属与开发数据恢复方案仍待具体确认，不能把设计当现已实施 |
| 通知 /sys/notice；公共 /notices | G /sys/notice/list、/get/{id}、/notices；P /sys/notice/edit、/del、/switch | 发布/有效期/状态影响公共列表；公共notices查询与后台管理权分离，无单独public写接口；后台增删改、分页和只读需实际验收 |
| 租户 /sys/tenant | G /list、/get/{id}；P /checkUnique、/edit、/switch、/del | 平台管理服务模式边界，standalone/租户端不能因UI隐藏就算拒绝；code不可改/不可复用、套餐/资源/角色/人数/有效期、初始化管理员及组织/schema、冻结失效/下线。真实创建会产生多表/schema及身份，不属于任意无影响测试；本次仅修Frozen code映射，不擅改合法状态更新契约 |
| 套餐 /sys/tenant/package | G /list、/get/{id}；P /checkUnique、/edit、/del、/switch | 资源全集/人数/天数与租户授权引用，活动使用中删除保护；Service已有resolveFrozen行为保持，不能盲加origin覆盖禁掉合法状态变更 |
| 平台只读租户inspect /sys/platform/tenants | G /options、/{tenantId}/organizations、/{tenantId}/roles | 仅platform条件装配；可信平台能力+有效目标租户、跨schema后上下文恢复；没有增改删/授权接口，普通tenant管理员不能冒充platform inspector |
| 当前身份派生 /auth | G /fetchMenus、/fetchDicts | 只读当前用户菜单/资源和字典；菜单返回不等于URL鉴权，fetchDicts缓存来源/缺项/切身份需验证；不是开放全局CRUD |
| 生成辅助 /dev/generator | P /prompt | 输入模型/输出提示词，只是生成辅助；没有“执行任意代码/写源码CRUD”路由，不将提示词成功当实际代码生成/部署成功 |
| 业务日志 /monitor/log/biz | G /get/{id}、/list | 刻意只读，系统通过Advice/事件/事务监听器保存，热表/归档UNION及count同条件。P0明确可信身份+有效ALL才读取，仍须接口资源许可及原tenant边界；无前台增改删日志端点，不手写日志造通过 |
| 错误日志 /monitor/log/error | G /get/{id}、/list | 刻意只读；异常事件按指纹聚合，P0同ALL守卫，不能将LOGIN_USER文本当不可变个人所有者；不得故意生成含秘密错误或修改旧日志“验CRUD” |
| 登录日志 /monitor/log/login | G /get/{id}、/list | 认证模块；登录/会话关闭事件产生，独立的查询/tenant规则，不能把biz/error P0守卫直接宣传为本接口已ALL保护；账号/IP/success/session/日期筛选及详情/分页真实验证 |
| 接口调用日志 /monitor/log/openapi | G /get/{id}、/list | 认证模块；真实API调用审计写入，查询过滤appKey/appName/path/success/日期。无前台增改删；其tenant/数据范围政策须独立核实，见下面源码缺口，不能用biz日志范围验收代替 |
| 在线用户 /monitor/log/online | G /list；P /offline | 认证模块；offline用@RequestParam userId和可选targetTenantId，并非JSON Body。在线列表/会话/Token撤销/登录日志关闭联动；强退是明确管理动作，不是删除历史日志；禁止踢任意真实用户取证 |

边界依赖但不并入84条sys/日志映射：POST /auth/getAlpha认证、POST /auth/register注册；头像/上传服务亦是账号页面外依赖，不能为验sysCRUD擅自改注册/认证/上传契约。接口可用性需按实际启用模式和资源许可确认。

权限/菜单/按钮链必须分层验收：JwtAuthFilter解析JWT/核账户租户状态及permVer→AuthPermissionContextService从可信tenant角色/档案构造ThreadUser→ResourceAuthorizationService.hasPermission→RoleResourceCacheService按tenant+role缓存METHOD:path→Controller/Service；数据查询由MybatisPlusDataPermissionHandler在分页前加数据范围。前端按钮/菜单是否显示还要QA逐DOM及后端请求负向验证；本只读盘点不审当前冻结的前端源码，不把返回resources等同按钮已受控。

SysRoleResourceMapper的真实投影是RESOURCE_URL→RESOURCE_PATH、UPPER(RESOURCE_TYPE)→HTTP_METHOD，仅GET/POST/PUT/DELETE/PATCH/*进入有效资源查询；有效模块/角色未删未冻结，并受租户资源上限约束。ModuleResourcesBo注释写“按钮、操作”不代表后端用这两个文字做HTTP鉴权。验收台账必须同时记资源ID/code（按钮标识）、method、完整path、所属mainId、角色和tenant。资源定义随module.edit联动，无独立资源CRUD Controller；角色grantResource只是授权关系，不应当作资源定义编辑测试。

匹配负向的硬前提：多角色/多资源是任一允许；*方法目录资源会兼容其子路径，旧宽*可覆盖新精确条目。负向账号必须列清实际全部角色及有效路径，确保无祖先宽授权，否则“删除某个按钮资源后仍可调用”不能直接判匹配器错，也不能凭管理员调用成功宣称逐操作已收敛。验证至少包括GET/POST同path方法错配、/get/{id}变量匹配、相似前缀organizationExtra不应被organization目录误收、无角色/空资源、跨tenant相同roleId缓存隔离、模块/角色冻结与删除、resource变更/permVer后旧Token缓存失效。真实角色/套餐/租户授予撤销只能在总控授权的专用数据方案下操作，不能暗改权限凑负向身份。

数据范围：现0000 ALL、0001部门、0002部门及下级、0003指定部门、0007本人创建/修改；0004/5/6未实现不新授，旧值保持拒绝。每类必须用可信受限身份对list/search/page.total/detail/跨ID/同级与子部门/全部不可见及混合批量写核验，不能只看列表空。角色未传scope/org保原、新增缺scope拒绝、0003含一个外tenant或非法ID全拒、失败不触角色/permVer写；多角色明确ALL的并集保持。平台inspect能力与普通ROLE_ADMIN不是同一作用域；standalone tenant0通过不等于saas/platform隔离通过。

额外重要只读待核：OpenApiCallLogService.get直接selectById，list直接selectPage，无显式tenant过滤或@TenantSchema；OpenApiCallLogEntity表API_CALL_LOG继承StdEntity，Mapper无Ignore。当前MybatisPlusConfig装配DataPermission→Pagination→OptimisticLocker，没有TenantLineInnerInterceptor（本次源码检索无该类用法）；因此不能假设有旧的行租户拦截兜底。是否该日志本来平台全局审计、何种schema/tenant所有权，必须按产品既有政策及真实受限访问单独验证；本次不证明存在越权/泄露，也不自行补新访问策略。Login/Online已有各自tenant解析，不套用OpenAPI结论。

配置/进程边界：eva.yaml源文件resourcePermission/dataPermission均true，tenant enable=false/mode standalone；这不是当前进程有效配置证据。resource权限关闭或permit路径可绕资源缓存，仍须认证与数据守卫；dataPermission关闭时生产根本不注册DataPermissionInterceptor，AuthPermissionContextService给empty scopes，biz/error Repository自身P0guard仍拒。此前直接手动创建handler的disabled测试证明的是该对象逻辑，不是生产所有任意Mapper调用都被注册插件保护。独立97还未完成、当前类/JAR/运行路由/真实配置加载仍待总控确认；不能用compile时间、JavaPID或前端热更新自证后端新类生效。

已有证据分级（本轮无重跑）：开发交付97项的确切18类/用例数为Advice16、Sanitizer15、Registration1、LogQueryInterceptor2、DataPermission8、ErrorRepository6、BusinessRepository4、TargetTenant2、AccountTenantFrozenMapping4、OperationType1、ModuleService4、RoleService14、RoleFrozenMapping2、TenantServiceCompatibility2、TreeFrozenEdit2、TreeFrozenMapping2、UserAccountProfile8、WriteProtection4，failure/error/skipped均0；总控QA正在独立复验，不能冒称已最终通过。这些覆盖合成秘密最终writer、映射code/非法/null、Role实际Convert/保状态/只读/专用switch、scope/org事务/租户条件、日志拒绝零Mapper/热归档/count等，不覆盖所有84个HTTP请求及真实权限。

已存在但本轮只核源码/文件名、未执行或确认当前结果的补充测试：DictServiceTest（明细更新/空行删除、commit后刷新/rollback不刷、只读拒）、DictConvertTest、DictCacheHelperTenantTest；NoticeServiceTest（公共有效通知、默认发布/状态）/NoticeCtrlTest；ResourceAuthorizationServiceTest（开关/permit/role）、RoleResourceCacheServiceTest（宽目录/精确method/tenant缓存）、SysRoleResourceMapperTest、AuthPermissionContextServiceTest、MybatisPermissionSnapshotQueryTest；OnlineUserServiceTest、LoginLogServiceTest；TenantModeBoundaryReviewTest/TenantCodeIdentityTest/TenantSchemaProvisionerIdentityTest/PlatformTenantInspectServiceTest及其Contract/ConditionalContext测试。测试文件存在≠通过；本次未找到可据以宣称Post完整CRUD、OpenAPI日志完整查询权限、所有按钮/菜单/真实业务DB回滚已覆盖的证据，不补造测试结论。

总控逐项验收行建议每次记录：对象ID及原值（必要字段而非秘密）→入口URL+method/身份tenant/角色资源前提→页面动作/实际响应（在工具能力允许时）→数据库业务效果或页面独立回读→关联候选/菜单/按钮/缓存/日志影响→负向结果→恢复原值与复读证据。每个C/R/U/D/专用动作分别填结果和未测原因。页面暂无数据/按钮隐藏/HTTP200/构建通过均不能单独判整体通过；日志只读项将C/U/D写为“不提供，审计写入由真实已授权操作触发”，不能任意补写/删日志。

当前恢复门禁：R01缺陷frozen1/临时remark仍待已加载修复后精确恢复原frozen0及EVA_QA_20261018_角色已编辑；新普通编辑会保1，不能只还备注声称完整回滚。D01/O01/M01/M02等旧对象须先回读真实原值，未删未恢复项逐条保留；新增/删除/授权/改密/在线强退/租户初始化的真实数据操作必须使用专用可恢复方案和既有授权，不因“全部验收”扩至未明确的真实用户与秘密。无DB/API迁移或字典归属决策被本清单自动批准。

### P2-BIZLOG-END-DATE-EXCLUDES-TODAY（2026-10-23交锁）

状态：补修与定向回归完成，所有命令结束，交还总控后端唯一源码锁供独立QA。当前cornerstone，保留全部原dirty。增量4生产文件：PostgresBusinessLogRepository.java、BusinessLogMapper.java、mapper/BusinessLogMapper.xml、PostgresErrorIncidentRepository.java；3测试：新增BusinessLogDateRangeTest.java、更新PostgresBusinessLogRepositoryTest.java及PostgresErrorIncidentRepositoryTest.java；仅另追加本卡。外部LogQueryBo/DateRangeBo参数名、yyyy-MM-dd格式、Response/PageVo及详情接口未变，前端未改。

根因证据：读QA runtime最新P2节及sys-log matrix：专用模块在选择10-23→10-24时可见20:31:54/20:31:41更新，只把end改回10-23后同日0行；QA仅安全页面字段，未捕获HTTP码、不拿空表断言无日志。源码LogQueryBo begin/end及错误查询DateRangeBo均Date+DateTimeFormat yyyy-MM-dd，绑定当地00:00；业务Repository原format(end)后，真实Mapper countLogs/selectLogPage热/归档四支均OPERATE_DATETIME<=end，明确只到结束日00:00；错误Repository wrapper同request_time<=end。此链与现场对照一致。DateUtils.format/parse/beginOfDay/addDay均使用既有JVM本地时区/Calendar机制，不另设UTC或硬编码时区。

修复：用户明确end日期先DateUtils.beginOfDay，再DateUtils.addDay(...,1)，按本地日历到次日零点，SQL用严格<；不是plus24h或23:59:59。begin仍>=且默认近7日逻辑不变。业务Mapper仅内部增加endExclusive布尔参数，Repository count/page传同一begin/end/flag，四支XML选择<或<=；明确end为排他，未传end仍以原当前时刻与<=处理，避免将默认范围扩到次日或改变默认时刻精度契约。内部Mapper方法签名随之改变，不是新增外部HTTP参数/API契约；运行时需一致重建/加载Repository、Mapper与XML，不能用旧包装/混合热替换当验证。

错误日志确认同一Date yyyy-MM-dd契约，故同步仅该必要入口：明确end同次日零点<，缺end仍now<=；begin/租户过滤与ALL guard保持。LoginLogQueryBo/OpenApiCallLogQueryBo的begin/end是LocalDateTime，服务以明确timestamp<=查询，无同一日期粒度注解，不能机械推进一天扩大范围；本轮只列证据、未修改这两个时间戳契约或其日志授权策略。业务/错误get按ID查询不套日期条件，原热归档详情与tenant语义保持；未动保存/归档、资源鉴权或P0身份/ALL/tenant保护。

验证：BusinessLogDateRangeTest3从真实Spring DataBinder绑定yyyy-MM-dd至实际Repository→mock Mapper捕获count/page相同参数，再交给实际BusinessLogMapper.xml生成BoundSql；同日、跨月1/31、跨年12/31、闰日2/29，验证begin0点包容、当日最后微秒包含、次日0点及前日末微秒排除（JVM比较与实际SQL>=/<操作符配对，不声称实际PG执行）。每种count/page×includeArchived开/关检查严格上界、热/归档end参数出现次数一致及保begin>=，默认flagfalse仍生成<=。仅begin/仅end/两者空（Repository null BO）及now默认边界；America/New_York春秋DST分别23/25小时，证明Calendar日推进非固定24h，finally恢复测试进程默认时区。错误实际wrapper测试跨年终止<2027-01-01 00:00、begin>=2026-12-31及tenant34；旧默认/租户/拒绝零Mapper回归保持。既有LogQueryBoTest2检验日期格式与非法字符串拒绝。

最终命令：
```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' :eva-core:eva-core-data-mybatis:test --rerun --tests org.pkaq.core.mybatis.log.BusinessLogDateRangeTest --tests org.pkaq.config.MybatisPlusDataPermissionHandlerTest --tests org.pkaq.config.LogQueryPermissionInterceptorTest --tests org.pkaq.core.mybatis.log.PostgresBusinessLogRepositoryTest --tests org.pkaq.core.mybatis.exception.PostgresErrorIncidentRepositoryTest --tests org.pkaq.core.mybatis.tenant.TargetTenantExecutorSecurityTest :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.bo.LogQueryBoTest --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.RoleFrozenMappingTest --tests org.pkaq.sys.AccountTenantFrozenMappingTest --tests org.pkaq.sys.role.service.RoleServiceTest --tests org.pkaq.sys.TreeFrozenMappingTest --tests org.pkaq.sys.TreeFrozenEditTest --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest --tests org.pkaq.sys.user.service.UserAccountProfileServiceTest --tests org.pkaq.sys.tenant.service.TenantServiceCompatibilityTest -x jar --offline --no-daemon --console=plain
```

exit0 BUILD SUCCESSFUL29s，17tasks6executed11up-to-date；实际core-data processResources/compileJava/compileTestJava和三个module test执行。XML北京时间10-23 20:49:24至20:49:34，总103：原97+BusinessDate3+ErrorDate1+既有LogQueryBo2，全部failure/error/skipped0；corelog34、coredata26、sys43。日志.gradle/bizlog-end-date-20261023.log。git diff --check0/index空，旧CRLF/JVM/Gradle警告保留。修改Mapper签名的直接Java caller仅Repository和同步更新的直接测试（已rg核对），旧热/归档/分页权限拦截测试通过。

限制：-x jar仍规避用户运行JAR锁，非完整打包成功/当前进程已加载新Repository/Mapper/XML。没有真实PG/HTTP取数证据，不读日志正文、不手写日志或改现场记录。需总控确认一致运行版本后，QA再选同一专用模块、同日范围验证列表与total/分页、跨日排除；权限/tenant受限身份、归档实数据等仍按总体矩阵逐项待验，不用合成或SQL生成冒充实库。无前端/QA卡/vault/依赖/服务启停/真实DBHTTPRedis写/Git提交推送或子代理。

### P2-ERRORLOG-OMITTED-END-EXPANSION（2026-10-23恢复后交锁）

状态：按用户恢复后单次派发继续，先核Git/Controller/自身记录/QA日期卡，确认上轮尚未修改ErrorCtrl且无新增Controller日期测试，不覆盖既有103项补修。现完成最小修复与扩展107项验证，全部命令结束，写锁交回总控派独立复验；不自证日期整项通过。本轮仅1个生产文件ErrorCtrl.java（删缺end预填3行，补说明注释）、新增ErrorCtrlDateRangeTest.java、本卡；.gradle下仅本地忽略的测试init脚本/日志，不改项目Gradle、依赖、配置或数据库。

已确认遗漏：原ErrorCtrl.list把HTTP未传end先setEnd(new Date)，实际Repository遂走“明确结束日”分支到次日零点；此前只测direct Repository未覆盖真实调用者，103绿不能证明HTTP缺省契约正确。现Controller保留end=null，仍原样保begin缺省近7日、pageNo缺省1/pageCount缺省10与Response，Repository已有缺end now<=统一处理；明确yyyy-MM-dd结束日仍次日零点<，不撤回整日修复。rg核对该错误Repository的生产HTTP调用入口仅ErrorCtrl，未再发现同类setEnd(new Date)预填。没有扩改Login/OpenAPI LocalDateTime timestamp契约或原ALL/tenant/资源策略。

真实列/比较边界：源码V1__INITDB.sql的LOG_ERROR.REQUEST_TIME、LOG_BIZ.OPERATE_DATETIME及V4__BUSINESS_LOG_ARCHIVE.sql归档OPERATE_DATETIME均VARCHAR(32)，不是PG timestamp列。ExceptionAdvice.requestTime=DateUtils.now，原DateUtils固定yyyy-MM-dd HH:mm:ss字符串；本次保持既有固定宽度字符串查询比较，不做DDL、类型转换或PG时区运算。此前DST测试说明的是JVM Date/Calendar边界与生成参数，微秒样例是JVM谓词/SQL操作符配对，不是实库timestamp精度/PG时区实证。

新增4项ErrorCtrlDateRangeTest：实际Spring日期绑定→真实ErrorCtrl→真实PostgresErrorIncidentRepository→仅mock ErrorIncidentMapper，捕获实际wrapper/PageResult；两日期缺省与onlybegin均end保持null、SQL request_time<=当前秒字符串（上下界夹在调用前后now，不到明日），原默认begin近7日及1/10分页/自定义3/7保留；明确12-31日期走<2027-01-01零点且begin>=，tenant34过滤保留；匿名及0007真实链在Mapper前拒绝零调用。先未修改Controller跑4项，只有缺end/onlybegin两项失败，explicit end/权限两项通过，日志.gradle/errorlog-omitted-end-red-20261023.log，exit1 BUILD FAILED34s；随后最小删预填，107全部通过。

避免旧JAR掩盖：因本轮仍-x jar，sys测试若默认依赖旧core-data JAR不能作为当前真实链证据。使用临时.gradle/errorlog-controller-current-classes.gradle仅给sys的test.classpath前置当前core-data sourceSets.main.output并dependsOn其classes；不改build.gradle或发布依赖、不影响用户服务。初始red/首轮green曾校验实际Repository CodeSource为当前classes且通过，最终测试去掉绑定具体物理目录的断言以允许正常新JAR全构建，但相同init前置当前classes仍生效，功能/日期/权限断言全部保留。独立QA复用下面-I命令与现存init文件，或一致完整重建依赖后用正常测试classpath；禁止引用旧JAR而声称该真实Repository链已测。

最终命令（原103项全保留+新4项）：
```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' -I '.gradle/errorlog-controller-current-classes.gradle' :eva-core:eva-core-data-mybatis:test --rerun --tests org.pkaq.core.mybatis.log.BusinessLogDateRangeTest --tests org.pkaq.config.MybatisPlusDataPermissionHandlerTest --tests org.pkaq.config.LogQueryPermissionInterceptorTest --tests org.pkaq.core.mybatis.log.PostgresBusinessLogRepositoryTest --tests org.pkaq.core.mybatis.exception.PostgresErrorIncidentRepositoryTest --tests org.pkaq.core.mybatis.tenant.TargetTenantExecutorSecurityTest :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.bo.LogQueryBoTest --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.log.error.ctrl.ErrorCtrlDateRangeTest --tests org.pkaq.sys.RoleFrozenMappingTest --tests org.pkaq.sys.AccountTenantFrozenMappingTest --tests org.pkaq.sys.role.service.RoleServiceTest --tests org.pkaq.sys.TreeFrozenMappingTest --tests org.pkaq.sys.TreeFrozenEditTest --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest --tests org.pkaq.sys.user.service.UserAccountProfileServiceTest --tests org.pkaq.sys.tenant.service.TenantServiceCompatibilityTest -x jar --offline --no-daemon --console=plain
```

exit0 BUILD SUCCESSFUL31s，17tasks4executed13up-to-date；最终sys测试compile与三个module test真实执行，ErrorCtrl生产compile在首轮完整107绿（38s）实际执行，最终仅测试移除物理路径绑定、生产未变。XML北京时间10-23 21:22:11至21:22:23，共21类107项failure/error/skipped全0（corelog34/coredata26/sys47），日志.gradle/errorlog-omitted-end-final-20261023.log。git diff --check0/index空，旧CRLF/JVM/Gradle warning保留。

运行门禁：用户说已重启发生在本次源码修改/编译之前，不能据此宣称已加载ErrorCtrl新逻辑。旧日期补修增加的BusinessLogMapper boolean签名与Repository/XML仍必须一致重建加载；测试init只作用JVM测试，不允许旧JAR/新XML/单类混热替换当运行验证。待总控独立QA通过后，再确认用户按原方式加载一致产物，恢复只读日期现场验证；无浏览器、DB/HTTP读写、日志全文、秘密、服务启停、前端/QA卡/vault/依赖/Git提交推送或子代理。

### SYS-SUPPLEMENTAL-SAFE-TESTS（2026-10-24）

状态：补充验证完成，所有命令结束，结果交总控继续总体矩阵；无生产/测试源码修改或新增产品bug。本轮先读新AGENTS/_line与_ref权威规则、Git、sys-log矩阵和选中测试及其配置，不迁移表、不调整存量命名。旧107目标不重复跑。本轮只追加本卡和.gradle忽略的本地init/输出，保护所有既有dirty，未提交推送。

安全筛选：选定sys11类、auth12类，共23类。Mapper/JdbcTemplate/DataSource/Connection/PreparedStatement/ResultSet、RedisTemplate/SetOperations/RedisUtil/IAuthSessionStore、审计/路由外部依赖均为Mockito替身；DictCache使用ConcurrentMapCacheManager。TenantSchemaProvisionerIdentityTest虽调用真实provisioner，但JDBC和DataSource完全mock、已执行迁移版本查询也mock，不连PG或执行真实迁移。PlatformTenantInspectConditionalContextTest为WebApplicationContextRunner+局部TestConfiguration，仅Import两个目标bean及mock依赖/真实convert，未启动用户应用/嵌入Web服务/自动装配生产数据源。JWT类使用代码内合成固定测试材料，仅内存签发验证，不读取真实凭据。TenantRoutingBoundary用替身事务/Router；Login/Online的日志写入、下线和Token撤销均止于mock，不踢真实用户。测试选择精确类名，无集成fixture通配匹配。

当前产物校验：-x jar情况下不使用旧core-data依赖包装作为测试真源。临时.gradle/supplemental-current-classes-20261024.gradle在projectsEvaluated后，只给sys/auth测试classpath前置当前contract/common/cache/log/data-mybatis/web-core的sourceSets.main.output，dependsOn各classes；doFirst检查当前outputs存在并输出SUPPLEMENTAL_CURRENT_OUTPUTS。日志两目标均记录前置类/资源路径；相关生产compile全部UP-TO-DATE为当前源码输出，auth compileTestJava真实执行。未改build.gradle、依赖版本、服务进程classpath或JAR，也未升级/联网取包；init仅作用测试JVM。

| 实际执行测试类（完整包名前缀见下方命令及XML） | XML用例数 | 安全边界/主要覆盖 |
| --- | ---: | --- |
| org.pkaq.sys.dict.cache.DictCacheHelperTenantTest | 1 | 内存字典缓存/schema隔离，RedisUtil mock |
| org.pkaq.sys.dict.convert.DictConvertTest | 5 | 真实生成BO/VO/明细字段与状态转换，纯内存 |
| org.pkaq.sys.dict.service.DictServiceTest | 6 | mock Mapper差异保存/空明细删除、事务同步提交/回滚缓存、只读拒写 |
| org.pkaq.sys.notice.ctrl.NoticeCtrlTest | 1 | mock Service公共通知兼容结构 |
| org.pkaq.sys.notice.service.NoticeServiceTest | 2 | mock Mapper有效通知/新增默认时间状态 |
| org.pkaq.sys.tenant.PlatformTenantInspectConditionalContextTest | 2 | 局部mock上下文，standalone不注册/platform三GET映射 |
| org.pkaq.sys.tenant.PlatformTenantInspectContractTest | 3 | 平台能力/接口只读契约，非网络请求 |
| org.pkaq.sys.tenant.service.PlatformTenantInspectServiceTest | 4 | 平台/租户角色身份矩阵，mock路由与Mapper |
| org.pkaq.sys.tenant.service.TenantCodeIdentityTest | 16 | 合法code/历史占用/不可修改/查重，mock存储 |
| org.pkaq.sys.tenant.service.TenantModeBoundaryReviewTest | 3 | 三mode拒绝边界，任何存储/初始化/事件前拒 |
| org.pkaq.sys.tenant.service.TenantSchemaProvisionerIdentityTest | 7 | 可信schema归属、长度、历史占用与幂等，JDBC全替身 |
| org.pkaq.core.auth.adapter.mybatis.audit.service.LoginLogServiceTest | 9 | mock查询tenant/平台target、会话关闭/合成请求审计 |
| org.pkaq.core.auth.adapter.mybatis.rbac.mapper.SysRoleResourceMapperTest | 1 | 仅反射@Select字符串，HTTP类型筛选/租户资源上限，不执行SQL |
| org.pkaq.core.auth.adapter.mybatis.rbac.service.RoleResourceCacheServiceTest | 3 | mockRedis宽*目录/精确method/相同roleId tenant隔离 |
| org.pkaq.core.auth.adapter.mybatis.role.service.MybatisPermissionSnapshotQueryTest | 1 | mockMapper→真实权限快照映射 |
| org.pkaq.core.auth.architecture.TenantRoutingBoundaryReviewTest | 4 | 替身事务中嵌套路由/异常上下文恢复，无PG search_path执行 |
| org.pkaq.core.auth.audit.service.OnlineUserServiceTest | 4 | mock会话列表、tenant/target及下线边界，不真实强退 |
| org.pkaq.core.auth.authentication.ctrl.TokenCtrlCapabilitiesTest | 2 | 合成request/token替身，refresh/permVer撤销无外部会话 |
| org.pkaq.core.auth.authentication.JwtTenantClaimsTest | 3 | 合成测试材料本地JWT租户/会话声明兼容 |
| org.pkaq.core.auth.authentication.vo.LoginUserInfoVoCapabilitiesTest | 1 | mock身份依赖，本地平台能力派生 |
| org.pkaq.core.auth.authorization.service.AuthPermissionContextServiceTest | 3 | mock角色档案，scope开关/组织解析/资料缺失 |
| org.pkaq.core.auth.authorization.service.ResourceAuthorizationServiceTest | 2 | mock资源缓存，开关/permit/可信角色 |
| org.pkaq.core.auth.util.CacheTokenUtilTest | 3 | mock会话存储，tenant/user/session及摘要匹配，不连接Redis |

准确命令：
```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' -I '.gradle/supplemental-current-classes-20261024.gradle' :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.dict.service.DictServiceTest --tests org.pkaq.sys.dict.convert.DictConvertTest --tests org.pkaq.sys.dict.cache.DictCacheHelperTenantTest --tests org.pkaq.sys.notice.service.NoticeServiceTest --tests org.pkaq.sys.notice.ctrl.NoticeCtrlTest --tests org.pkaq.sys.tenant.service.TenantSchemaProvisionerIdentityTest --tests org.pkaq.sys.tenant.service.TenantModeBoundaryReviewTest --tests org.pkaq.sys.tenant.service.TenantCodeIdentityTest --tests org.pkaq.sys.tenant.service.PlatformTenantInspectServiceTest --tests org.pkaq.sys.tenant.PlatformTenantInspectContractTest --tests org.pkaq.sys.tenant.PlatformTenantInspectConditionalContextTest :eva-web:eva-web-auth:test --rerun --tests org.pkaq.core.auth.authorization.service.ResourceAuthorizationServiceTest --tests org.pkaq.core.auth.authorization.service.AuthPermissionContextServiceTest --tests org.pkaq.core.auth.adapter.mybatis.rbac.service.RoleResourceCacheServiceTest --tests org.pkaq.core.auth.adapter.mybatis.rbac.mapper.SysRoleResourceMapperTest --tests org.pkaq.core.auth.adapter.mybatis.role.service.MybatisPermissionSnapshotQueryTest --tests org.pkaq.core.auth.audit.service.OnlineUserServiceTest --tests org.pkaq.core.auth.adapter.mybatis.audit.service.LoginLogServiceTest --tests org.pkaq.core.auth.architecture.TenantRoutingBoundaryReviewTest --tests org.pkaq.core.auth.authentication.JwtTenantClaimsTest --tests org.pkaq.core.auth.authentication.ctrl.TokenCtrlCapabilitiesTest --tests org.pkaq.core.auth.authentication.vo.LoginUserInfoVoCapabilitiesTest --tests org.pkaq.core.auth.util.CacheTokenUtilTest -x jar --offline --no-daemon --console=plain
```

结果exit0 BUILD SUCCESSFUL26s，16tasks4executed12up-to-date。实际执行sys/auth两个test、auth processResources和compileTestJava；sys测试compile复用未变产物。XML UTC2026-10-24T00:25:56.817Z至00:26:05.677Z（北京时间08:25:56至08:26:05），sys50+auth36=86，23类全部failure/error/skipped0。每类文件在对应模块build/test-results/test/TEST-上述完整类名.xml；日志.gradle/sys-supplemental-20261024.log包含真实任务执行和两个CURRENT_OUTPUTS列表。任务会按本次selection重写sys/auth结果目录，因此不能把当前XML宣称仍同时存有此前107整套；旧107按既有独立QA/日志交接取证，不把当前86说成同次全量193通过。

排除/未跑：web-booter BusinessLogIntegrationTest为@SpringBootTest+真实@Autowired JdbcTemplate/Repository/ArchiveJob，明确insertLog/归档会触实际存储，即使@Transactional也不获本次mock范围授权；NoticeIntegrationTest、ModuleTreeIntegrationTest、OrganizationTreeIntegrationTest、LogoutIntegrationTest、RequestFilterEnabledIntegrationTest、RequestFilterDisabledIntegrationTest及BaseTest都是全应用@SpringBootTest/相关真实默认配置，未证明完全隔离PG/Redis/应用启动或没有副作用，故不运行。EvaConfigDataIntegrationTest与本次业务补充范围不同，亦未扩大执行；不将所有Integration命名类无证据地统称会写库。不运行迁移、全仓test或未筛选fixture。

结论边界：未发现新的产品失败，本轮不申请源码锁、不改变原契约/授权。86项补上“此前仅见测试文件”的开发验证证据；SQL反射、mock事务/Redis/JDBC、局部Web上下文都不等于真实受限登录URL拒绝、按钮/菜单DOM、跨tenant PG/缓存实例隔离、异步审计归档或端到端CRUD已通过。Post完整CRUD/OpenAPI日志可见性政策、真实双角色/受限身份/跨tenant及总体84路由矩阵按总控/QA继续；原运行加载门禁不因这些测试解除。不提交push、不重启用户服务，无UI/DBHTTP/真实日志/凭据/Redis操作、源码/依赖/配置/vault/表迁移写入或子代理。

## 2026-10-24 09:08 OpenAPI 集中审计 A 实施与隔离回归交接

总控在本轮独占源码锁内明确派发已获用户批准的集中审计 A 政策，批准依据见设计文档 `docs/legion/design/2026-10-23-openapi-log-boundary.md` 第 8 节。当前分支 cornerstone；原有大量未提交修改及未跟踪文件保持，index 为空。本节只交付读取资格与必要路由，不声明运行服务已经加载。全部构建测试命令已退出，新增源码工作完成，现将唯一后端源码锁交回总控以安排独立 QA；未经新派发不继续写源码。

生产文件（本轮 6 个，不覆盖已有 biz/error 保护）：

- `eva-web/eva-web-auth/src/main/java/org/pkaq/core/auth/spi/IOpenApiAuditAdministratorQuery.java`：窄管理员资格 SPI，不向安全核心引入存储类型。
- `eva-web/eva-web-auth/src/main/java/org/pkaq/core/auth/adapter/mybatis/audit/service/OpenApiAuditAdministratorQuery.java`：服务端 userId 参数化查询当前 SYS_ACCOUNT、SYS_ROLEUSER_REF、SYS_ROLE，账号与角色均要求 tenant0、DELETED=0、FROZEN IN(0,9999)，角色 CODE=ROLE_ADMIN；关联表按实际 USER_ID/ROLE_ID 联结，没有臆造 membership.DELETED/FROZEN。未复用缺少 R.DELETED 条件的旧角色快照 SQL，未改其全局语义或缓存。
- `eva-web/eva-web-auth/src/main/java/org/pkaq/core/auth/adapter/mybatis/audit/service/OpenApiAuditReadAccess.java`：可信人类身份、模式和有效管理员常驻检查；standalone 使用已配置唯一数据源 schema 并拒绝冲突 TenantContext；platform 仅可信 tenant0，在真实只读事务通过现 CoreSchemaExecutor 显式访问 core，finally 恢复外层线程上下文；不用 TenantAuthRouter.execute(0)。资格窄查失败或异常不执行日志动作。resource 开启仍执行原 GET 资源匹配，关闭仅跳过匹配；不按 ALL/空 rolesMap/空 scopes/TENANT_INSPECT/API 用户放行，不合成 ALL，不改全局角色装载。
- `eva-web/eva-web-auth/src/main/java/org/pkaq/core/auth/adapter/mybatis/audit/service/OpenApiCallLogService.java`：get/list 均先经同一守卫；list 内分页及自动 count 保持原查询与同源读取；URI、BO/VO、Response、筛选及时段语义不变。save 原实现未修改，不增加人类读取门禁。
- `eva-core/eva-core-data-mybatis/src/main/java/org/pkaq/core/mybatis/log/OpenApiAuditQueryScope.java`：同步线程读取窗口绑定同一可信 ThreadUser 实例及 tenant0，仅允许完整指定 OpenApiCallLogMapper 命名空间下的详情/列表/计数及分页自动 count 语句；finally 清理或恢复嵌套窗口。唯一生产调用者是已完成管理员和资源核验的 OpenApiAuditReadAccess，非通用授权入口。
- `eva-core/eva-core-data-mybatis/src/main/java/org/pkaq/config/MybatisPlusDataPermissionHandler.java`：仅 API_CALL_LOG 的上述受控读取窗口跳过通用 CREATE_ID/scope 条件；其他 Mapper 读取该表拒绝，其他表仍走原数据权限。未新增全局 Ignore/excludeTables，未改变数据插件开关装配。data 关闭未装插件时仍由 Service 常驻守卫保护。

新增 4 个测试类共 33 项，完全隔离真实 DB/Redis/HTTP：

| 测试类 | 数量 | 验证边界 |
| --- | ---: | --- |
| org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAuditReadAccessTest | 22 | data/resource 四组合的正反资格；真实 MybatisPlusConfig 局部 Bean 装配证明 data 关时不装拦截器但仍拒绝；空角色/scopes 有效管理员、正租户同名管理员/ALL/TENANT_ADMIN/TENANT_INSPECT/API_USER、缺身份、资源拒绝、资格查询异常、standalone 上下文冲突、匿名失败审计及插入异常隔离；Mapper 与资格/资源接口为 mock |
| org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAuditCoreRoutingTest | 6 | 真实 Spring 只读事务代理 + DataSourceTransactionManager + 真实 CoreSchemaExecutor/资格查询适配器，连接/JdbcTemplate 全 mock；core 资格及日志动作同事务/同源，资格不存在/查询异常/日志异常/路由失败/恢复失败后的线程窗口及上下文清理，校验 SQL 未删/冻结状态与账号角色关联谓词 |
| org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAuditMapperSqlTest | 1 | 真实 BaseMapper 注入 selectById/selectList/selectCount SQL、真实数据权限及 PaginationInnerInterceptor 的 selectList_mpCount；替身 Executor 返回合成 count，无数据库；同集中表、无 tenant 行归属过滤/CREATE_ID 限制 |
| org.pkaq.config.OpenApiAuditQueryPermissionTest | 4 | 真实 DataPermissionInterceptor 下详情/列表/count 仅在窗口内可读；其他 Mapper/自定义语句不可借用，其他表继续 deny-all；同字段但不同用户对象、匿名、正租户拒绝，嵌套及异常退出恢复 |

已删/冻结账号角色验证是实际资格 SQL 谓词断言加 mock 查询 false 的失败闭合组合，不是对真实 PostgreSQL 删除/冻结数据的执行证明。资源规则使用真实 ResourceAuthorizationService、替身资源查询接口；未登录真实受限身份或修改授权。

最终一次联合回归：auth 68（16 类）+ core-data 30（7 类）+ sys 18（2 类）= **116 项、25 类，failures/errors/skipped 全 0**，其中新增 33。保留旧权限上下文、角色资源、登录/在线日志、JWT/Token/permVer、存储边界、tenant 路由、biz/error 数据守卫与日期、RoleService 及 ErrorCtrl 日期回归。XML 时间 UTC 2026-10-24T01:06:32.977Z 至 01:06:42.758Z（北京时间 09:06:32 至 09:06:42）。结果分别在各模块 `build/test-results/test/TEST-完整类名.xml`。本次 selection 重写三个结果目录，不把此前 107/86 与本次 116 宣称为同次全量执行。

准确命令（exit0，BUILD SUCCESSFUL 25s，19 tasks：3 executed、16 up-to-date；三个 test 都真实执行）：

```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' -I .gradle/openapi-current-classes-20261024.gradle :eva-web:eva-web-auth:test --rerun --tests '*OpenApiAudit*Test' --tests '*ResourceAuthorizationServiceTest' --tests '*AuthPermissionContextServiceTest' --tests '*RoleResourceCacheServiceTest' --tests '*SysRoleResourceMapperTest' --tests '*MybatisPermissionSnapshotQueryTest' --tests '*OnlineUserServiceTest' --tests '*LoginLogServiceTest' --tests '*TenantRoutingBoundaryReviewTest' --tests '*AuthStorageBoundaryTest' --tests '*JwtTenantClaimsTest' --tests '*TokenCtrlCapabilitiesTest' --tests '*LoginUserInfoVoCapabilitiesTest' --tests '*CacheTokenUtilTest' :eva-core:eva-core-data-mybatis:test --rerun --tests '*OpenApiAuditQueryPermissionTest' --tests '*MybatisPlusDataPermissionHandlerTest' --tests '*LogQueryPermissionInterceptorTest' --tests '*PostgresBusinessLogRepositoryTest' --tests '*PostgresErrorIncidentRepositoryTest' --tests '*BusinessLogDateRangeTest' --tests '*TargetTenantExecutorSecurityTest' :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests '*RoleServiceTest' --tests '*ErrorCtrlDateRangeTest' -x jar --offline --no-daemon --console=plain
```

最终日志 `.gradle/openapi-read-final-20261024.log`。最初直接前置跨模块输出遭 Gradle 9 implicit-dependency 校验拒绝，不能算测试通过；随后用当前已编译 classes 与当前源码 resources 的独立快照 `.gradle/openapi-current-snapshot-20261024-a/` 隔离测试加载，未更新被服务占用的 JAR。7 模块 classes 文件数为 contract28/common81/cache8/log16/data42/web-core20/auth93；5 个关键类（OpenApiAuditQueryScope、DataPermissionHandler、ReadAccess、AdministratorQuery、CallLogService）源产物与快照 SHA-256 一致，清单 `sha256.txt`；临时 init 的每个 test doFirst 同时核验原始输出及快照哈希，日志三次记录 `OPENAPI_CURRENT_SNAPSHOT_SHA256_VERIFIED`，classpath 最前为快照，排除旧依赖 JAR 覆盖新守卫。compileJava/compileTestJava 在前序本轮真实执行、最后一轮均按相同源码输出 up-to-date；未修改 build.gradle、依赖版本或配置。该临时快照与 init 仅供本轮复验；生产源码以后变化会因关键类哈希不一致中断，必须重新编译/重建快照，不能长期复用旧快照。

测试过程曾有一项路由失败用例红灯：Mockito when-restub 会先调用旧 answer，把模拟 search_path 预置成 core；已改为 doAnswer/doThrow 避免重设替身时执行动作，生产路由实现未因此改变。初次红灯日志 `.gradle/openapi-read-fixture-red-20261024.log`，最终 6 项路由用例全绿。未把替身配置错误报告为真实数据库路由故障。

GitNexus 对 OpenApiCallLogService 的 upstream impact 返回 target not found/UNKNOWN，未声称低风险或重建索引；以字面调用链补充确认唯一业务读取调用 Ctrl、审计写入 SPI 边界。tracked 本轮差异 git diff --check 通过（已有 autocrlf 警告不等于内容错误）；8 个新增 Java 文件 LF、无 Tab/尾空格、行长 <=120。没有覆盖既有 dirty 文件、提交/push、创建 PR、升级依赖、改库/schema/旧日志归属/权限授权、操作真实 DB/Redis/HTTP、重启用户服务或修改前端/vault。

待独立 QA：复核集中资格与受控数据旁路；按已批准测试包与现运行加载门禁验证真实 standalone 管理员及普通受限主体、资源开关与日志 get/list/total 的 HTTP 行为；platform/core 的真实环境路由和正 tenant 否定行为仍待适用环境验收，未实施测试包 T 或新建 tenant/schema。匿名与失败调用真实链路审计、现存集中记录的物理位置/旧 NULL 行可见性未读库验证；未回填、搬迁或新增日志 owner，不能用 mock/SQL 生成测试代替运行态或数据库验收。

## 2026-10-24 11:18 包 S S0 限定只读预检交接

按总控新派发读取包 S 已批准 fixture、最新包 S 账本/sys 矩阵、项目规则及 Git；不申请或使用源码写锁，仅写自身 status 与新附件 `docs/legion/design/2026-10-24-package-s-preflight.md`。cornerstone，原 dirty/untracked 保留，index 空；本轮无测试构建/HTTP/UI/源码/配置/依赖/数据库与 Redis 写/服务启停/授权/凭据持久化/vault/Git 发布。

当前实际 9016 监听 PID47664/java.exe，2026-10-24 10:08:31.509+08:00 启动，入口 WebBooter、工作目录本项目，JVM classpath 指向本地 web-booter/build/classes/java/main 及 build/resources/main，非 bootJar。只读 VM.system_properties 内部允许列表提取及实际 @argfile 核对未发现 profiles/mode/tenant/data/resource 覆盖；没有输出整条命令行、环境或其他系统属性。已知 .vscode/launch.json 未发现允许字段的显式 env/args 覆盖且无 envFile，但该文件不当作进程环境。

实际 classpath 内 6 份 application/EVA YAML 与当前源文件 SHA-256 相同，副本修改时间 10-19 21:50:27，早于此进程。application.yaml 文件仅 default profile=dev、声明导入 EVA 配置、port9016；EVA 文件候选元组 standalone / tenant=false,standalone / data=true / resource=true。**仅文件证据，不是 Spring effective**：未读取目标进程环境/Spring Bean，也不能排除其他属性源，四项 effective 保持未核，未用源码默认补足。

该进程现有 5432 TCP 连接与 application-dev.yaml 的 PostgreSQL 端点匹配，但目标非 loopback；dev 文件名、库名形态与端点匹配不足单独确认非生产开发实例。执行总控“仅确定当前开发 PostgreSQL、身份不明就停”：本轮停于认证前，没有 DB 连接/SQL/真实 catalog/current_database/current_schema，未取用凭据认证或猜测/切库，未输出地址/URL/口令。不能将候选 counts、资源 ID、有效状态或授权前值记 0/空/无冲突；这些全未核，未启动只读事务不能冒称 DB READ ONLY 验证通过。

附件已列 O_READ/O_WRITE/L_READ 8 个精确预期 code+method+path，真实资源行 ID/mainId/有效性均未填种子值；已按实体/Mapper核实源映射 sys_module_resources/sys_module/角色/组织/账号及档案字段，明确资源无 FROZEN，角色 code 会补 ROLE_ 大写，账号登录值是 SYS_ACCOUNT.ACCOUNT、档案 code 在 SYS_ACCOUNT_PROFILE。仅为后续经授权 catalog 核验计划，不是数据库存在性证明；宽*只识别不授予。包 S 实际创建/授权仍0，新角色记“本包初始未存在、无实际ID”，同名数据库冲突未排除，不把计划空授权伪装成既有行前值。

最小待确认为同一张当前实例身份卡：由服务操作者确认 9016/PID47664 的已生效 mode、tenant.enable/mode、data/resource，以及其当前连接 PostgreSQL 确为本次获准开发测试库非生产；不需口令/Token/整配置/环境。若不能确认该库则提供合法已获准目标配置来源而非聊天凭据。无需重复包S总授权，不开actuator/切配置/重启服务。已主动向总控报告此门禁；本轮只读动作结束，附件交总控/QA复用，S0未通过，创建/授权不得提前。

## 2026-10-24 11:29 包 S 用户确认后只读数据库预检完成

总控转达用户对当前实例身份卡明确回答“正确”：当前9016为standalone、tenant关闭、data/resource开启，当前连接非loopback PG为本次获准开发测试库。此确认解除前节身份不明门禁，不扩大为任何DB写授权。续核前后PID仍47664/原10:08:31启动的WebBooter，配置指向与源一致、与进程现有5432连接匹配；effective配置结论明确源于用户确认而不是文件推断。

本轮实际完成3个psycopg2只读会话，UTC03:27:01.445、03:27:41.272、03:28:10.690（北京时间11:27–11:28）：database=eva/schema=eva；连接默认只读on、事务只读on，资源/候选结束前复核仍on，各次显式rollback后close。凭据只从既有本地加载配置内部取用，未放入命令行、输出、文档或新文件。首选psycopg3脚本exit1无完整结果，不算SQL成功证据；未安装/升级依赖，改用既有psycopg2完成可核只读结果。没有写入、DDL/迁移、显式锁、临时表或尝试其他库。

必要catalog仅eva下sys_module_resources/sys_module/sys_role/sys_organization/sys_account/sys_account_profile 6表必要列，类型均与现实体/Mapper的所需投影匹配。8条指定资源按精确code或URL全状态限定查询，实际恰8行、每tuple唯一，DELETED=0；关联sys.organization/log.biz/log.error及sys/log父模块均DELETED=0/FROZEN=0，父链完整。实际回读精确集合（bigint须按字符串，不经JS Number）：

- O_READ：1000000000000011010（GET /sys/organization/list）、1000000000000011011（GET /sys/organization/get/{id}）。
- O_WRITE：1000000000000011009（POST /sys/organization/checkUnique）、1000000000000011012（POST /sys/organization/edit）。
- L_READ：1000000000000011050（biz get）、1000000000000011051（biz list）、1000000000000011052（error get）、1000000000000011053（error list）。

三模块mainId分别1000000000000000102（sys.organization）、1000000000000000202（log.biz）、1000000000000000203（log.error）。随后仅对源种子清单的这8条做对照，ID/code/type/url/main_id与实际回读全部相同，差异0；不是按种子猜运行ID，也不推广为全库或全授权一致。

相关模块内另核7条宽资源，全部排除、不授予：MENU_SYS_ORGANIZATION、API_SYS_POST、API_SYS_ORGANIZATION、MENU_LOG_BIZ、API_LOG_BIZ、MENU_LOG_ERROR、API_LOG_ERROR；完整实际ID/path/type在附件。API_SYS_POST实际挂组织模块且路径/sys/post/**，不能按组织模块整选复制。未修其归属或迁移命名，不扩本包权限。

13个专用候选全状态精确参数化count均0：2个SYS_ACCOUNT.ACCOUNT账号、5个角色（候选name/原code及ROLE_QA_R_*规范化code）、4部门和2作者样本（code/name）；没有按deleted/frozen过滤隐藏冲突，没有返回账号行/密码/hash/电话/真实姓名/token或任意用户权限/业务日志。五角色前值可记“初始未存在，无roleId/资源关联”，不把计划空集合冒称既有授权回读。实际创建/授权仍全0，无待回收对象，保护旧R01/O01/D01/M01/M02和管理员。

证据写回 `docs/legion/design/2026-10-24-package-s-preflight.md` 第8–11节，顶部已更新当前结论，第1–7节只作首轮历史不再阻塞。附件含脱敏JSON、READ ONLY/rollback/close、catalog、8精确资源完整表、父模块、7宽项、候选counts及QA集合。属于时间点读快照、不持续加锁；后续合法创建/授权前仍须回读和唯一性处理。只读资源/候选缺口已补齐，未发现缺行/重复/禁用/候选冲突；不等于新身份/菜单/必填父节点/HTTP拒绝/缓存撤销/S1–S8已通过。所有连接和命令结束，交总控派QA下一阶段，本成员不创建/授予/切状态。

本轮仅写自己的status及该附件，无源码/配置/依赖/UI/HTTP/Redis/服务/测试构建/vault/提交push；当前cornerstone、index空。前节“未执行DB/仍待用户确认”已由此续核状态替代，不能继续作为当前阻塞。

## 2026-10-24 包 S 组织编码及 DATA_SCOPE 限定只读诊断

按总控派发，只读核当前 Org checkUnique/edit BO/Controller/Service/Mapper、异常/i18n、字典实体/查询与定向缓存链；不获生产源码写锁，不构建测试、不发HTTP或读秘密日志。使用gitnexus-debugging query/context仅定位，索引behind21提交，不重建，以当前源码复核。当前cornerstone/index空；9016前后仍PID47664/10:08:31启动WebBooter，已获用户确认的同一开发PG指向不变。

新附件 `docs/legion/design/2026-10-24-data-scope-readonly-diagnosis.md`：第1节为实际DB脱敏JSON，第2节Org静态链，第3节精确恢复候选，第4节定向缓存边界。Org BO及StdBo无字母数字Pattern，name/code只@NotBlank；checkUnique无@Valid，仅同pid下code或name重复、编辑排自身；edit有@Valid、Convert原样code。当前默认错误ORG_CODE_EXIST4310、MISS_CODE_OR_NAME及CAN_NOT_FIND_RECORD/PARAM_ERROR都不是“编码格式错误，仅允许字母数字”，所读i18n亦无该提示覆盖。不能凭当前源码判定后端既有policy禁止下划线；现场Form根因交frontend另查，不静态冒称HTTP已发/未发或运行类已加载，不擅改候选code绕过。

根pid null/0，源码新建PATH=/新ID，不要求前端parentPath；非根需Mapper可见父节点及非null path，否则找不到记录；改父拒自身/后代并维护子孙path/叶子标记。未读组织业务行或实际约束catalog，未证明现场父节点可用性，不把此树约束混成格式错误。

当前DATA_SCOPE只读SQL时间UTC03:40:31.475Z/北京时间11:40:31，eva/eva，default及transaction readonly均on，末次复核on，已rollback/close，写0。仅查eva.sys_dict/sys_dict_item必要catalog、code=DATA_SCOPE或已知主ID及其关联8条，未查其他字典/旧叶/用户权限或个人/日志。主1000000000000004001：type/code DATA_SCOPE、name数据权限、deleted1/frozen0/sort1/tenant0、真实pid0/path /1000000000000004001/isleaf=true。明细1000000000000004002–1000000000000004009对应0000–0007，全部deleted1/frozen0/tenant0、main_id指该主项，sort1–8。明细catalog无pid/path/isleaf，未猜列或按旧树改写。

供technical_designer与总控请用户确认的技术最小候选（本成员不决定或执行）：主ID4001完整值及0000/1/2/3/7对应实际ID1000000000000004002、1000000000000004003、1000000000000004004、1000000000000004005、1000000000000004009，共6行仅deleted1→0；0004/5/6对应1000000000000004006/4007/4008完整ID见附件保持deleted1。code/value/name/frozen/sort/tenant/pid/path/isleaf/main_id均不变，不自动回挂旧root4000或改系统只读/归属。角色服务新授拒缺/非法scope及4/5/6，现有缺scope保留不合成ALL；数据插件实现0/1/2/3/7而非4/5/6，恢复标签不能冒称实现功能。原始前值及逐行后值表在附件。

缓存源码已闭合说明：queryDict对非null空Map也命中，查不到删除主项后会cachePut空map，故单改DB不保证生效。cacheName=dictdata；scopedKey无TenantContext.schema时DATA_SCOPE、有时<schema>::DATA_SCOPE；默认RedisCacheConfiguration物理前缀预期dictdata::，此为源码默认不是实际Redis读取。standalone路由器不新增schema绑定，正常无外层ctx时预期dictdata::DATA_SCOPE，不凭数据库schema=eva就盲选eva前缀；须确认合法执行上下文再定唯一key，不能扫删。

已有定向Java入口DictCacheHelper.remove("DATA_SCOPE")；正常合法字典写入的reloadTypesAfterCommit保留schema并按受影响type单项remove+put。DictCtrl没有公开单code evict/reload HTTP，直接SQL不会触发应用afterCommit；不能用无意义edit/switch造副作用或自行新开接口。init/reload()/reload(Map)/removeAll均会广清，remove(code,key)仅改读出map不回写，也不适用。精确恢复若获批，还须确认DB提交后通过现合法受控入口仅驱逐对应单key并回源验证；本轮无Redis读写/调用。没有可用合法入口就列执行前置，不能全局cache init/flush蒙混。

实际快照已按总控转达用户授权的本项目成员协作范围同时主动发总控与technical_designer会话01a14ee2-9540-7e82-b922-1c003668970c；补充完整源码/缓存证据后再交接。本轮只写自身status+该附件，所有连接/命令结束，无恢复/授权/服务/源码/依赖/测试构建/HTTP/UI/Git发布/vault写。包S实际创建/授权仍0，保护旧QA及管理员；恢复选择与缓存执行方式待设计/总控组织用户具体确认。

## 2026-10-24 包 R 已获批准，Redis 来源前置失败，DB 写前停止

总控转达用户结构化批准 R 第8节完整六行恢复及缓存安全前置，并指定本成员唯一R数据执行者；完整读设计第8/9节和实际readonly诊断后复核仍9016/PID47664/10:08:31启动WebBooter、cornerstone/index空。源码无写锁，不构建测试、不改服务/配置/前后端/依赖或任何既有角色授权。批准不是执行，R执行结果附件新增 `docs/legion/design/2026-10-24-package-r-execution.md`。

先创建受保护本地 `.gradle/package-r-20261024/`，绝对路径验证在当前工作区.gradle内、git check-ignore通过、目录原不存在未覆盖；ACL取消继承，仅当前执行用户与SYSTEM两条FullControl。JVM只读class_hierarchy证明RedisCacheManager类已加载，但不把这当实际Bean证明。既有加载配置与源相符，Redis文件声明DB0/6379；凭据仅配置内存取用，不输出/文件持久化/argv传递，没有安装依赖。

本地现有Python运行时的受控维护客户端只做同配置Redis连接元数据核验（AUTH/SELECT配置DB，CLIENT LIST TYPE NORMAL内部按本PID连接端点筛选，丢弃其他记录、不输出地址/用户名）。未执行KEYS/SCAN或其它业务key读取。初次严格对应失败已即时报总控；随后只排除IPv4/IPv6/mapped表示差异，没有降低来源要求或试其它DB/namespace。UTC04:05:45.789804Z/北京时间12:05:45最终计数：进程同配置Redis端点连接2；源端口单独相符2；地址规范化后源地址+端口严格相符0。故该PID的实际Redis DB归属仍不能证明，可能代理/NAT但原因未证，不用端口碰巧相同、DB文件0或PGschema=eva补足。

严格执行批准前置“无法安全核实单键则DB写之前停”：唯一源码候选dictdata::DATA_SCOPE的TYPE/GET/DUMP/PTTL均未执行，TTL/值未知；没有EVAL/DEL/FLUSH/init/removeAll或任何缓存修改。R本轮未打开PG会话、未采新SQL前像、未UPDATE/COMMIT，executedIDs=[]、SQL affected0、DB写0、cache mutation0。既有六行实际是否仍相同不能拿旧快照冒充此次复核；没有SQL/缓存前像备份，不宣称比较失效能力或恢复后五项已经生效。所有Redis连接正常关闭、命令结束。

私有目录目前只保存 `preflight-stop-metadata.json`（允许计数/候选键/时间/停止原因，无地址或认证）；SHA256=A85FC699286A23DC00F7C5AAF3EA43D803D6BCAEAF1C463548269626753700A2。该文件是停止证据不是可回退的数据/缓存备份，未进入Git/vault。没有写入就无需数据库回退、缓存恢复或权限回收；S仍暂停、创建/授权0。本轮只写自身status与R执行附件及批准的ignored停止记录，未提交push。

继续R的最小具体缺口是当前应用真实Redis DB/namespace的可追溯归属证明（能解释该PID端点与服务端记录地址不一致的合法实例管理证据，或能核实实际Bean/context的合法受控单键维护入口），不是重问R总授权或索取凭据。证明后还须先核唯一键、比较失效与私有备份，再新鲜核九行关键前像及触发器/规则，才能1+5条件更新/完整字段比对/三排除不变，提交后比较单键失效；并发前值改变不盲删，SQL回退不覆盖并发。当前正式停在写前并交总控，不自行假定代理、试删其它前缀或先恢复后找入口。

## 2026-10-24 包 R 续行完成：SQL六行提交、唯一缓存键比较失效，交QA回源

总控转达用户明确确认唯一实例缓存卡：应用实际使用既有Redis DB0，没有自定义prefix/代理路由/运行时覆盖，解除前节来源门禁；保留地址不匹配的历史技术观察，不重复要求同一确认。仍同9016/PID47664/10:08:31实例、同已确认开发eva/eva及配置，cornerstone/index空。当前结果取代前节“写前停止”为最新R状态，完整记录 `docs/legion/design/2026-10-24-package-r-execution.md` 第5–8节；前1–4节仅历史。

写前只在DB0候选dictdata::DATA_SCOPE读取TYPE/GET/DUMP/PTTL，实为string/4字节空Map/PTTL=-1。值、DUMP、元数据在私有ACL且ignored目录独占保存，无凭据/地址；不读其它key/db/前缀。精确一个KEY的Lua probe比较原TYPE/GET/DUMP/TTL一致返回1，不执行DEL；ACL DRYRUN确认此键DEL权限，故原子比较能力在写库前已验证。再次probe在SQL前仍1。

UTC04:18:43.221Z新READ ONLY完整前像主1+明细8已备份，所有批准关键值与之前快照一致。两表无启用自定义trigger、rules或RLS。写入连接先只读核eva/eva，再仅此维护连接进入SERIALIZABLE READ WRITE；事务内再核元数据、九行完整to_jsonb前像和主项唯一。main SQL仅ID+deleted1+完整旧JSON条件SET deleted0；items SQL仅五个获批完整旧JSON数组条件SET deleted0。RETURNING精确main1+items5=6，executedIDs为1000000000000004001、1000000000000004002、1000000000000004003、1000000000000004004、1000000000000004005、1000000000000004009。

提交前完整九行比对只允许六个deleted变0，包含版本/审计时间/创建修改人等所有其余列不变，三排除1000000000000004006、1000000000000004007、1000000000000004008完整行仍deleted1/frozen0。预期后像独占落盘成功才commit；UTC04:21:46.414961Z/北京时间12:21:46成功完成。提交后只读重新读九行与预期完全相同，按主/明细deleted/frozen条件的SQL逻辑映射恰0000/0001/0002/0003/0007，复核事务readonly on，rollback/close。不把这当应用queryDict/前端已验。

UTC04:22:22.229622Z/北京时间12:22:22，同DB0唯一dictdata::DATA_SCOPE Lua正式分支比较原GET/DUMP/无TTL变化成功，DEL返回1，只删这个键；后复读TYPE none/PTTL=-2，连接close。未手工回填map、RESTORE、改TTL或全清。无并发比较冲突/阶段失败，无回退执行；不能将缓存备份盲灌回旧空Map。

私有目录 `.gradle/package-r-20261024/` 的8个前像/历史/结果文件均Git ignored且仅当前用户+SYSTEM两规则，目录ACLprotected。backup-manifest.json自身SHA256=38798B01328AFF83F6935162A517B8FAF5B0ED9A224349E6800068A6A4E38BC1；完整文件表和恢复界限在R附件。关键SQL前像F6CF2BEF3CC78D800C8D275439D6BC13FA1780B1A57AD64D85B5199781AA3961；预期九行后像CE87396F90F76ED9023EDCFD6708DC7389048FA50ED6789C58BC98BD5552D003；SQL结果0902298771174032EA7AD5BEC399E429CBA405FEB03E37DD4E6F63488D6D6173；cache结果E57A02DB9E73F4599E46E8E7CB1F18A176006E8D91B47EB9793EF564E464F716。其余cache原值/DUMP/元数据hash见附件/私有清单，不展开审计字段或认证。

若后续要求回退，必须核同实例+备份hash+六行完整本包后像，WHERE完整后像条件只把自身六行deleted0→1，仍expect1+5，不能全行覆盖、upsert或覆盖并发；缓存须核当时同源唯一键后按新鲜前值比较，不盲RESTORE旧空Map。DB/cache/UI不原子，包S若已继续还须总控重核实际对象与授权，R不负责自动撤销其后续行为。

R数据与单键动作已完成，所有DB/Redis连接、命令结束，正式交回数据窗口供独立QA。待QA用现有合法管理员正常GET单项让queryDict回源，并关闭重开角色新增抽屉验证恰五项、无默认0000、不含岗位3项；本轮未发HTTP/读写UI，不能假称该验收已通过。未创建S对象或授予权限，未触既有roles/关联/其它字典/根/旧叶/schema/源码/配置/前后端/依赖/服务/构建测试/vault/Git提交push，仅R获批6行和唯一key及本人文档/私有备份写入。

## 2026-10-24 R页面仍空：后端限定只读诊断完成，当前无再改TYPE/数据依据

已读QA新data-scope-r卡：正常新增表单仍暂无数据、无默认ALL/必填正常，未取HTTPstatus/response，不能当空map或权限根因。按新派发只读诊断，不继承R写权；未再写SQL/Redis、未失效/回填或读其它namespace，不HTTP/UI/任意用户授权/真实日志/测试构建/启停/源码/依赖/配置/提交push。9016仍PID47664/10:08:31实例，cornerstone/index空。

新附件 `docs/legion/design/2026-10-24-package-r-read-diagnosis.md`。UTC04:30:10.904Z精确DB0 dictdata::DATA_SCOPE已是string、PTTL=-1、五code_map（0000/0001/0002/0003/0007），不再是R前空值；value SHA898fa95f715d0afb8bb507df5dac027a0fab6ce198a59e26da6ef43a7e7653ad，dump SHA db81244a2229b33582b2fc09d405fdf0e68aeaf473800b3b41c787d892761601。UTC04:33:59再为序列化层次只读核同key：第一JSON解码str、第二dict、仍五code及同valuehash；这符合helper先Map→JSON String、Redis再序列化String的路径，不是两层缓存。本成员没有回填，不能确定是哪次正常调用写入，更不能据此称QA那次HTTP200。

UTC04:32:06.181Z当前eva/eva READ ONLY主+8明细：R主及五项仍deleted0/frozen0、TYPE=CODE=DATA_SCOPE/tenant0/PID0/PATH自身，3岗位仍deleted1，归属和顺序原样。当前loader基础条件CODE=DATA_SCOPE/deleted0/frozen<>1命中主1+五明细；仅在该主ID范围内反事实TYPE='code'计数0、TYPE='DATA_SCOPE'计数1。当前selectDictByType实际用getCode，不使用这个TYPE='code'条件，不能将0计数当作修改TYPE的理由。事务/default只读均on，末尾核on，rollback/close；没有读取任意个人/其他字典/用户授权。

当前JVM classpath199项仅内部取目标首解析来源：DictService/Helper/Ctrl/Entity/Item均sys-service/build/classes/java/main，与当前输出字节相同；Dict/Item XML首来源该模块build/resources，与source逐字节相同、只有resultMap没有自定义loader。RedisConfiguration首来源core-cache-1.1.1.jar但内容与当前输出相同，不因旧文件名认定旧逻辑；Response/CommonResponseAdvice也与当前输出相同。DictService/Helper/Ctrl class文件时间10-19 21:50早于当前10-24启动；只读javap中selectDictByType bootstrap12明确DictEntity.getCode、bootstrap1为StdEntity.getFrozen，queryDict先cache非null返回、miss再select/cachePut。没有当前相关文件不一致证据，但这不等于读取了进程内存字节或证明那次HTTP。

链路/契约：DictCtrl GET /sys/dictionary/query/{code}→queryDict→Helper.get(scopedKey)→同一Spring Redis Cache条目；miss→code/frozen/逻辑未删主项→main_id未删明细+Java过滤冻结→D_CODE/D_VALUE Map。无queryDict @Cacheable/持久本地Map，逻辑scopedKey+Redis cacheName前缀是命名组合，不是第二缓存层；未探其它key或清缓存。dataPermission表排除不含两字典表、mapper无Ignore，源码可因真实scope追加条件，JwtAuthFilter也先资源匹配再runWithUser；本轮只查source、不读真实管理员授权或伪造scope，等价SQL不冒充已包含那次未知权限谓词。不能根据这些断言HTTP许可/拒绝。

Ctrl.success→Response.success(data,BizCode)成功data为Map<String,String>，CommonResponseAdvice保留data只动code/message等，不转换数组/lines或再套data/result。已按总控指定会话与项目协作授权，把安全实证、Bootstrap绑定及成功响应Map契约同步frontend 01a132b1-0e33-7a73-9344-4ccaee63479c。当前没有后端TYPE/数据/loader必改点证据；最小剩余是合法页面实际请求是否到预期API、HTTP/业务success和data形状以及前端消费路径，交frontend/QA定位。若真涉及R外数据或契约，先列精确差异确认，不擅扩R或授ALL/Ignore。

只写本附件和自身status，原R备份/manifest未变，所有PG/Redis/命令结束。本成员创建S/授权0；完整R五选项运行验收仍未通过，等待真实请求/页面链证据，不因数据库/单key五值而关闭该门禁。

## 2026-10-24 S0七对象与CUSTOM保存只读补证完成

按总控限定只读派发，已读最新QA S账本、fixture/preflight/当前源，仍原9016/PID47664/10:08:31实例、cornerstone/index空。新附件 `docs/legion/design/2026-10-24-package-s-custom-readonly.md` 第1节为UTC05:08:14.921Z实际READ ONLY JSON，第2节两表必要tenant列元数据，第3–7节绑定/字节码/NULL归属惯例及安全纠正边界。只读阶段无源码/数据/Redis/HTTP/服务/测试构建/授权变更，两个PG事务均eva/eva、readonly/default on、rollback/close，不读个人资料或旧日志。

四org实际ID2113856261050376193(P)、2113856614139469826(D1)、2113856917966462977(D2)、2113857168370606081(D1C)，三role2113857683657629697(SELF0007)、2113858118523068418(DEPT0001)、2113858458634985474(TREE0002)全deleted0/frozen0/tenant_id NULL。org父链/PATH/SORT与QA P→D1/D2、D1→D1C相符；role data_org_ids实际NULL，不伪装数组。按3roleID查询SYS_ROLERES_REF/SYS_ROLEUSER_REF完整技术集合均空。CUSTOM原/规范化code/name全状态0，现无持久角色行，不重复提交或造actualID。审计只含create/modify技术ID和实际原时间，不映射个人身份。

D2按ID+deleted0命中1，加tenant_id=0命中0。当前RoleService standalone分支明确要求tenant0，visibleIds须全等；若实际payload为0003且D2，NULL/0冲突足以拒绝，无须猜数据插件是否另缩可见范围。但实际payload/HTTP未知，不能登记那次已返回4200。源分支PARAM_ERROR=0x0400-00002，绑定失败可更早发生，未读取真实请求日志补证。

绑定源码：RoleAoeBo.dataOrgIds为String；前端Form和service数组join(',')、String保留，后端逐段正Long解析及0003非空校验。已按规范先读前端规则/AGENTS/CLAUDE，仅读角色绑定段不跨仓写。编译RoleService字节与当前classpath首来源一致，SHA d8ca70bb04643b00bd6d77e46e0182184c4da0e34d5c326ad78ee0342a161202；javap证先validate再unique/convert/insert，standalone lconst0/else可信ThreadUser tenant、OrgMapper及Set.equals实际存在。permVer仅既有编辑后更新受影响用户，不能拿本次新建校验失败当已改授权。

插入约定：BO/Convert/新增服务不赋tenant；StdEntity编译@TableField(fill=INSERT)，MetaObjectHandler文档/字节码明确不自动填tenant，当前MP3.5.17 withInsertFill直接保留列/参数的字节码已核。两表tenant列实际nullable YES/default0，无启用自定义trigger。存在显式NULL绕过DBdefault的生成路径，但无当次INSERT抓包，不伪造SQL现场。NULL符合现有普通插入/schema隔离可能值，不能自动判脏并修七行。

最小纠正交设计审查：使standalone唯一可信schema归属核验与NULL/0约定一致，同时保全部正ID/逻辑未删/data可见/完整Set及positive tenant隔离；不可全局OR NULL、去tenant/data guard或授ALL。不触R字典/缓存，不补写0/迁移/清理/改冻排序。完整证据主动同步总控及technical_designer；随后总控另授CUSTOM standalone最小实现唯一源码锁，属于下一实施阶段，只读七对象保持不动。

## 2026-10-24 CUSTOM standalone 局部修复与隔离回归交锁

已按总控正式源码锁及 designer `2026-10-24-custom-scope-standalone-ownership.md` 完成 RoleService 局部修复：仅 standalone/singleton+tenant关闭+可信user tenant0+无矛盾上下文+原实际事务绑定连接，先核配置schema/current_schema/实际sys_organization namespace，再经原Mapper/逻辑删除/数据插件参数化分组NULL或0及全部正ID集合全等。standalone重复ID拒绝，unknown主mode失败闭合；positive保原eq、不ORNULL，普通插入不改NULL约定。只新增JdbcTemplate末位构造依赖、旧RoleService测试夹具适配及两份真实Mapper/SQL/事务mockJDBC测试，不改全局handler/DB/API/路由或七行。

十模块当前classes/resources已offline编译成功，-x jar不碰用户JAR；新隔离快照465类+27资源共492文件，每个最终测试前全量核原输出/副本SHA与清单一致。manifest SHA A5853E84F152E9C952E60217C01BC2F44BC4B89CC4685A28149ED856467DDEFE；RoleService.class SHA 80DFFA86998E324D80068B20F9588D71D5C35B8A849DA0530E86441E5C01E30C、13:29:47晚于运行PID47664 10:08:31，未声称当前实例已加载。最终13类119项（新增48+SYS原28+AUTH29+DATA14），失败/错误/跳过0；包含原ALL/编辑遗漏/只读/版本刷新、NULL0/正值混入/删除隐藏/身份schema事务异常、实际SQL分组和原OpenAPI/日志guard。schema和rollback证据是隔离mockJDBC事务，不冒充真实PG或HTTP联调。

精确命令、局部变更、真实SQL输出、最终XML副本和日志均列在 `docs/legion/design/2026-10-24-custom-scope-standalone-implementation.md`。GitNexus target not found=UNKNOWN后做字面追踪，未重建索引。所有命令结束，现交回源码写锁供独立安全QA；运行加载归总控，不热换单class、不启停服务、不恢复S或创建CUSTOM。未DB/HTTP/Redis调用或写入、未触R备份/七对象归属/授权/前端/vault/QA卡，未commit/push。正tenant V1无TENANT_ID的原潜在契约风险独立未解、包T未验。


## 2026-10-24 13:55 新两角色空关联与LIMIT短名候选只读补证完成

按总控限定派发，仅对CUSTOM2113870080799694849、ALL2113870419179364353及精确账号QA_LIM_20261023/QA_PEER_20261023补证。当前9016监听PID39300/java、13:43:51启动；app-dev当前build资源与源一致，内部确认新PID现有PG端点匹配获准开发目标，不输出URL/凭据/命令行/环境。driver实际eva/eva，transaction/default只读on/on，末尾复核on/on、rollback/close、writes0。实际observedAt UTC05:55:35.971678（北京时间13:55:35）。

CUSTOM code ROLE_QA_R_CUSTOM_20261023/name QA_R_CUSTOM_20261023、scope0003、data_org_ids恰D2字符串2113856917966462977；ALL code ROLE_QA_R_ALL_20261023/name QA_R_ALL_20261023、scope0000/orgIds NULL。两行tenant NULL/frozen0/deleted0。仅这两个roleId的SYS_ROLERES_REF和SYS_ROLEUSER_REF未过滤完整集合均空；旧七对象未重新查询或改动，前次空关联证据保留。QA实际S0创建4org+5role=9来自其正常UI记录，本成员创建和grant均0。

总控同用途替代短名QA_LIM_20261023长度15，全状态SYS_ACCOUNT.ACCOUNT精确参数count0；原PEER候选QA_PEER_20261023长度16、count0。未查询任意身份/密码/个人资料，未造“已创建/已登录/持续无冲突”。S0附件2026-10-24-package-s-preflight.md第12节已附实际JSON、边界和SHA FCE2BE7CA8D39CCC5A3745C42D49940BC707F112B3893B2E5DE035104E7D4230。只写该附件/本人status，无源码/配置/编译测试/HTTP/Redis/服务/前端/vault/QA卡/提交推送；所有连接命令结束，结果主动报总控及指定QA。


## 2026-10-24 15:32 PEER部门与编辑候选限定只读诊断交付

按总控仅只读指定PEER2113894345662021633核当前新9016 PID49000/java 14:48:04启动；旧39300前置安全停止未连库。新PID config源/资源一致、PG端点匹配获准目标，driver实际eva/eva、transaction/default只读on/on，末尾on/on、rollback/close、writes0。

关键实证：账号tenant0/frozen0/deleted0，档案ACCOUNT_ID同值，DEPT_ID实际D1=2113856614139469826，不是NULL；仅该USER_ID两完整role/post关系未过滤均空，无意外admin或非空。D1存在有效，五S role原scope0007/1/2/3/0、CUSTOM仅D2及frozen0/deleted0保持。不取密码/哈希/电话邮箱姓名IP/Token或任意他人权限。

源码/当前字节码确认deptId→Convert→saveProfile.ACCOUNT_ID/DEPT_ID，ALWAYS策略下编辑真提交null可清空，不能代修；详情/列表LEFT JOIN档案映射deptId，VO无deptName。候选角色GET /sys/role/list，无scope参数，PageVo.records；前端回报源实际/api/sys/role/list?pageNo1&pageSize200、5分钟全局key缓存，非账号id分支，不据source造现场缓存根因。组织树受原查询/data插件约束，id/key/title无value别名，Long响应配置转String。当前PID199项classpath中19class+4XML首来源全同当前输出，XML同source，javap部门引用全在；不冒HTTP/内存实证。

自身附件2026-10-24-package-s-peer-department-readonly.md及S0第13节落证，安全DB/运行JSON和逐项SHA在.gradle。已主动给总控/frontend真实D1、空关联、类型/树契约，所有连接命令结束；只写本人status/S附件，无源码/测试构建/配置/HTTP/Redis/服务/授权/第二账号创建/提交push，不自行回填或放权。


## 2026-10-24 account view已授角色最小读契约：只读核查交设计

收到总控新派发及用户新明确授权：双击用户view只显示被查看者已拥有角色，新增编辑保可选候选；必要可补最小后端读接口/契约，不授权DB/角色/写grant。本轮仅source读取及本人status，无后端写锁，无DB/HTTP/Redis/日志秘密/构建测试/服务/源码/依赖/提交push；不重查PEER或任意用户权限，旧PEER roles空仅历史快照。

现有契约不足：UserDetailVo仅List<Long> roleIds，没有角色id/name/code/frozen对象数组。GET /sys/account/get/{id}→UserService.getUser @Transactional(readOnly=true)+@TenantSchema，先UserMapper.selectById命中可见未删账号，再Convert、RoleUserMapper.selectRoleIds、postIds。UserEntity.roles字段虽存在，但详情selectById的BaseResultMap不装roles；VO也无此字段。UserRoleRef.xml只按USER_ID返回ROLE_ID，RoleUserMapper类@Ignore，没有有效role join或角色元数据。UserMapper.getUserWithModuleAndRoleById带@Ignore且还加载模块；RoleMapper.selectByUserId亦@Ignore，旧Role.xml只frozen!=1没有显式deleted条件；二者不能为了view直接复用。AuthRole XML是登录权限快照专用，也不是查看某目标用户的最小元数据接口。这里是source边界，未执行上述查询或扩修历史实现。

普通候选GET /sys/role/list是RoleQueryBo.name/code+分页，返回PageVo.records，不按目标USER_ID已授关系查询，前端当前pageSize200可能漏掉该用户真实已授但不在前200候选的角色。仅以roleIds过滤候选不能保证已授名称完整；roleIds空可真实呈空，非空未解析不能假称无角色或回退全候选。

推荐交designer的最小路线（尚未实现或定稿）：扩原详情响应assignedRoles=[{id,name,code,frozen}]，不新增GET路径/资源seed，不改原roleIds/postId和写BO；view独占消费该已授数组，新增编辑候选不收缩。先按原account/get资源鉴权+数据规则确认目标账号可见，再只读其真实ref角色ID，在非Ignore的RoleMapper标准参数化查询id/name/code/frozen、in该完整去重集合；保MP逻辑删除及角色数据插件，无分页200上限，不查全候选。UserRoleRefSerivce已有userMapper/roleUserMapper/roleMapper，可优先复用局部只读能力，避免为了此需求新框架/给UserService重复注入。原关系Mapper历史Ignore不应延伸为新角色元数据查询的Ignore。

状态/完整性须designer明确：建议“已授关系”与“当前有效权限”分开，未删的冻结1或只读9999角色可随frozen显示状态，不凭认证快照过滤抹掉仍存在的授予；deleted/orphan/数据不可见不得冒充已无任何角色。可最小assignedRolesComplete布尔对照原去重roleIds与可见返回ID，false展示信息不完整而不补造名称/放宽范围，或明定不完整即拒绝；具体由designer收敛，不自行加一堆计数字段。空ref→[]且完整；目标不存在/无数据可见性、未认证/资源拒绝、SQL/路由异常走原失败，不用catch转[]或返回success全候选。原roleIds不能因隐藏/冻结而被重写，否则会改变编辑授予语义；assignedRoles只读，不参与saveRoles或permVer。

权限源：JwtAuthFilter用可信operator角色先ResourceAuthorizationService匹配请求method/path，再runWithUser执行；当前account/get权限不会自动授权独立role/list或任意新GET路径。最小扩同detail的用户已授角色摘要应经用户新读需求与designer明确纳入原detail能力，同时角色metadata仍受当前operator原数据范围；不为可见性加ALL/admin/Ignore/permit或宽资源grant。源V7有API_SYS_ACCOUNT通配seed只是历史定义，不代表当前真实用户持有，未查运行权限。若另起新GET，应先明确其资源映射，不能自动改DB seed补通配。

tenant/schema：原getUser事务TenantSchema→TenantSchemaRouter/TrustedTenantSchemaResolver只接受可信ThreadUser租户映射，不接客户端tenant/schema。当前router用trustedSchema,core search_path有缺表回退潜在点；新account/ref/role已授读需designer审同一可信schema来源约束，不因row tenantNULL/0跨tenant共享，不套standalone ORNULL或向tenant V1增列。正tenant基础表缺tenant列的已知独立风险不能用新读需求偷偷改契约。数据开关关闭仅沿原开关，不绕可信身份、目标关系和tenant边界。

影响范围初判：sys-domain UserDetailVo与最小角色摘要VO，sys-service UserService.getUser及现用户角色关系服务的局部只读方法/必要Mapper路径，相关用户详情测试与前端view消费；不涉及数据库表、迁移、授权写、账号创建、scope字典、全局数据插件或资源缓存。必要后续隔离验收：目标可见/不可见、空/部分/全部已授、角色>200且全表候选不含、重复/冻结/删/孤立/数据隐藏、跨tenant/缺表fallback、资源及数据开关、异常不转空、原roleIds编辑保留与零写grant/permVer。当前未运行，不能称通过。

已将上述source证据与建议同步technical_designer 01a14ee2-9540-7e82-b922-1c003668970c，说明用户新已允许必要读API，不重复请求笼统批准；等待其只读合同收敛及总控正式实现锁。完成后同步总控与frontend，不自行实现或发请求。


## 2026-10-24 account assignedRoles实现与177项隔离回归交锁

按root正式源码锁和designer定稿，仅原UserDetailVo增assignedRoles四字段摘要，原roleIds一次精确ref快照保持，空refs不查全表、207角色不截断，无complete/API route/DB/seed/grant/BO变化。UserRoleRefSerivce局部MANDATORY读先可信身份/mode/context+同事务源、原非Ignore账号可见，再原ref及非Ignore RoleMapper.selectMaps投影四列IN真实正ID；root/designer已确认rawMap避免实体code改写/NULL丢失。冻结1/9999可见未删照实，隐藏/删除/孤立只缺摘要，原roleIds不改；SQL/身份/源错误不吞空。

数据查询六表account/profile/ref/role/org/postref current_schema及实际namespace核全；正tenant局部CoreSchemaExecutor复核真实SYS_TENANT来源core及权威映射、finally恢复，不全局修resolver、不ORNULL/eq租户/DDL。root另确认当前getUser岗位读取用已有postUserMapper执行与原服务完全同等精确SELECT，避免核源后的二次TenantSchema路由；原Mapper Ignore未新增或扩张，仅在6表来源+可见target之后读精确内部关联，role元数据保持插件。实际外层TenantAspect与shadow控制表fixture证明post只在A、无二次router/set_config/tenant shadow查表；源错误拒绝与历史单role缺摘要区分。

最终显式十模块classes/resources编译成功，snapshot-d467类+27资源=494，测试前各模块全hash+清单及结束后全量hash复核一致，27源资源同字节；manifest D87ED7CCFD71317A2A1909451D0425F4721A4B7636BF6CC51A798A02CD349EED。新42项真实Service/MapStruct/XML/非IgnoreMP Map SQL/AST/Data插件/Spring只读事务mockJDBC安全用例+原回归，最终18类177项（SYS126/AUTH37/DATA14）全部fail/error/skip0，不累加历史119。初轮fixture问题及c快照并行清单门禁失败如实保留；最终Test依赖全部main产物完成后才校验，没放松门禁。

精确命令/实际BoundSql/每类XML数/源边界/逐项class SHA/加载前置见docs/legion/design/2026-10-24-account-assigned-role-implementation.md；最新日志.gradle/assigned-role-regression-d-20261024.log，XML副本.gradle/assigned-role-final-xml-20261024，汇总assigned-role-final-results-20261024.json。GitNexus getUser LOW但落后21提交，仅UserCtrl直接调用，另补字面源追踪，不称完整图安全。源码cornerstone/index空，scoped diff check通过。

新UserService/UserRoleRef class17:07:26/25晚于此前已核14:48实例，无运行加载/HTTP证明；root先独立QA再管完整模块加载，前端不得假当前已返回新字段。所有命令结束交回源码锁，本轮不PG/HTTP/Redis/秘密日志/服务/实际S/R数据写、不前端/vault/QA卡/依赖/提交push或账号/grant。运行UI与S/T未由此完成。


## 2026-10-24 QA assignedRoles独立门禁失败：只读诊断与冻结调度提案

按root本轮仅允许诊断/验证辅助范围，已读实际QA日志.gradle/qa-assigned-role-regression-20261024-a.log、现init、QA自己的assigned-role卡、gradle.properties及当前规则/Git。cornerstone，原业务dirty保留。本轮没有运行Gradle（含help/dry-run/taskgraph）、177/其它测试、快照重制、实际PGHTTPRedis/服务/依赖或业务源码；QA继续独占验证槽。只新增下列两个忽略目录辅助草案及本人status，未动旧init/快照/QA卡。

事实：QA29s exit1，data:test doFirst在init56 Current output inventory mismatch；日志中web-core compileJava在该门禁失败后完成，SYS/AUTH Test未执行。QA结束494/27资源与snapshot-d完全相同是QA已给证据，本轮未重复做此全量重验证。结束一致只能支持瞬时变化可能，不能证明具体竞态/缺哪项；现有日志没有完整执行图/门禁现场added/missing，原因尚未完全定性。**更正先前交付中“已消除竞态/仅dependsOn即可保证all-main先于Test”的过早结论**：代码有依赖声明，但QA实际执行顺序不满足承诺，不能把dev177代替独立通过。

最小提案采用三个独立、同步等待退出的Gradle进程，避免再依赖同一执行图内的main/Test排序：第一阶段显式十模块classes（含main资源），第二阶段三模块compileTestJava各--rerun+processTestResources+testClasses，前两阶段禁止Test进入图。均offline/no-daemon/no-parallel/no-configure-on-demand/max-workers2，保原-x jar、原当前产物classpath init。第三阶段仅精确三个Test各--rerun与当前18类筛选（OpenApi通配展开三个确切类），排除main/test编译、资源、classes/testClasses和jar；新whenReady守卫要求图**只有这三个Test**，并核所有SourceSets输出/资源目录，不允许任何编译/复制/打包生产节点。图不符在任务执行前失败并落真实图，不假定-x或依赖声明已经有效。

snapshot-d D87ED7CCFD71317A2A1909451D0425F4721A4B7636BF6CC51A798A02CD349EED始终封存：每阶段前后核原manifest哈希、494完整清单/所有原输出与副本SHA、27源资源一致；source/build/init/helper输入锁与准备后的测试class/resource锁保持，无删除门禁、吞差异、自动接受/覆盖/重建snapshot。任何added/missing/changed停报并保该阶段log/gate错误。每个Test在原hash门禁前另存即时清单差异与原路径示例，便于下轮区分图顺序、路径规范化或外部修改，仍不提前定因。Test.afterSuite实际根套件写当轮新目录执行计数126/37/14及fail/skip，不借旧XML凑177。

拟写入/现冻结文件仅：
1. .gradle/assigned-role-staged-verify-20261024.py：三阶段调度和独立hash/输入闭包；默认仅打印PLAN，只有显式--execute才运行，拒绝外部任意参数，工作目录锁本仓库。日志/图/清单/实际suite结果写全新.gradle/assigned-role-staged-runs/UTC标识-UUID目录，不覆盖旧证据。
2. .gradle/assigned-role-phase-guard-20261024.gradle：准备阶段禁Test；执行阶段要求精确三Test、无其它节点及SourceSet产物生产者；保原init的classpath和hash钩子，无业务/资源权限/契约变更。

本轮仅对Python文本做ast.parse语法解析；未导入/执行helper（连默认PLAN也未运行），Groovy运行/Gradle图语义尚未经验证，草案不可称调度已修复或独立177已通过。旧.gradle/assigned-role-current-20261024.gradle保持未改，SHA DF6C0C52D56EFD41D819C67663458830F4C4F23A6E0350450902DBF0F449DACF。

当前没有理由重建snapshot-d：用户给的QA结束状态仍完全相同。未来执行提案会允许前两阶段重新生成十模块build/classes/resources和三模块test产物，原字节必须仍等于d；若需要新业务产物或快照，先停报具体差异、来源、影响给root，不擅封新版本。待root审两个草案、QA释放日志槽后另派调度，拟入口仅python .gradle/assigned-role-staged-verify-20261024.py --execute（此命令本轮未执行）。本成员现冻结辅助，无业务源码写权、不抢QA槽、不重复跑177。

冻结helper SHA256 3BDB1170757F2B79B33C16CCC18C24F5C1E12993B3DF7E4F2CE16CB18446DA89；guard SHA256 68417F27830D9209F8B5DAF530F8E45C38B380D9F53139C905640326DD165125。


## 2026-10-24 QA P2父构建输入补录：仅Python辅助已重冻

按root本轮窄授权，仅改.gradle/assigned-role-staged-verify-20261024.py的code_inputs显式固定名单，新增eva-core/build.gradle、eva-web/build.gradle、eva-web/eva-web-sys/build.gradle。三文件均实际存在，参与父项目配置；未改变阶段顺序/过滤/计数、hash门禁、输入失败闭合、生产排除图、默认PLAN或--execute行为。没有动Groovy guard、原init、snapshot-d、业务源码/配置/依赖/服务/QA卡；QA当前sys整轮槽不抢占。没有执行helper（含PLAN）或Gradle/测试/重验证，只ast.parse与静态单行diff。

精确差异只新增一行fixed +=三路径；从新文件删除该唯一行可重建原冻结SHA 3BDB1170757F2B79B33C16CCC18C24F5C1E12993B3DF7E4F2CE16CB18446DA89，已用静态字节比较确认，无其它辅助行为变动。

其他同类配置遗漏已按settings显式项目列表核文件名/存在性及literal apply-from引用，未自动递归扫描用户Gradle目录、环境、env/密钥或秘密配置：--no-configure-on-demand会评估settings内其它已包括项目，所以web-booter/build.gradle、eva-core/eva-core-license/build.gradle、eva-core/eva-core-websocket/build.gradle、eva-server/eva-license-server/build.gradle这4个现存脚本同样不在当前code_inputs固定名单。license/websocket只见引用既已锁的根publish.gradle，无新literal应用脚本；web-booter/license-server没有literal apply-from。eva-server/build.gradle当前不存在，但该隐式父项目是settings路径的一部分，未来创建也属需考虑的固定候选。按本轮只纳入3父文件的授权，没有自动扩大补录这4+1路径，已向root列明，仍不宣称配置输入闭包完整或辅助可放行执行。

已按显式标准路径核settings.gradle.kts、build.gradle.kts、gradle/verification-metadata.xml、gradle/gradle-daemon-jvm.properties、根gradle.lockfile及buildSrc，当前均不存在；未扫其它任意路径或输出配置值。当前根build/settings/gradle.properties/libs catalog/publish/wrapper、10叶build.gradle、3父build.gradle及3辅助文件继续按原逻辑锁输入。只有root审后再定是否把上述其余实际配置脚本按显式名单补齐，不自动改动或接受新snapshot。

新冻结Python SHA256 78CA27D01CBF7796E9C22A79A0E6E2C6CF6D54B7C28F35782D99BE09A46ED37C；Groovy guard保持68417F27830D9209F8B5DAF530F8E45C38B380D9F53139C905640326DD165125；原init保持DF6C0C52D56EFD41D819C67663458830F4C4F23A6E0350450902DBF0F449DACF。结果主动报root，当前冻结不执行。


## 2026-10-24 明确配置闭包与不存在候选门禁补齐，Python辅助再次冻结

按root新增窄授权，本轮仍只改Python辅助code_inputs；现存web-booter/build.gradle、eva-core/eva-core-license/build.gradle、eva-core/eva-core-websocket/build.gradle、eva-server/eva-license-server/build.gradle已加显式文件哈希，原三父构建与10叶配置继续保留。不再将这四项留作未补配置闭包。

明确不存在候选清单为eva-server/build.gradle、settings.gradle.kts、build.gradle.kts、gradle/verification-metadata.xml、gradle/gradle-daemon-jvm.properties、gradle.lockfile。这六项在每次code_inputs入口先核存在性（含符号链接），任何一项出现立即停止，**初次启动辅助时也不接受它为新baseline**，不读取新文件内容；固定字典额外记录#presence=absent。所有其它固定配置路径也记录present/absent标记并对已存在常规文件保原SHA，不接受目录替代文件，阶段间出现/删除/变化仍失败闭合。

buildSrc单独仅做exists/is_symlink元数据判定，任意形式出现即在模块src递归/Gradle前拒绝，并记buildSrc#presence=absent；不递归、不导入、不读其未知内容。未读取用户Gradle目录、环境、密钥或秘密路径。只涉及root批准的明确仓库配置文件名与存在性，未扩大自动扫描范围。

静态验收：仅Python ast.parse及文本/AST diff；逆向移除本次code_inputs变更可精确恢复前次78CA27D01CBF7796E9C22A79A0E6E2C6CF6D54B7C28F35782D99BE09A46ED37C；所有函数中唯code_inputs的AST改变，commands/main阶段顺序/参数、测试筛选、原哈希门禁、图guard均未变。无helper执行（包括PLAN）、Gradle/任务图/测试/重验证或快照操作，QA仍sys整轮唯一槽。本成员未改业务源/数据/依赖/服务/QA卡/提交push。

新冻结Python SHA256 0A85F5E26C226D6B231E3D3C4DFD1AE82401190BE6C24737A3A87B165207B7C1。Groovy guard保持68417F27830D9209F8B5DAF530F8E45C38B380D9F53139C905640326DD165125；原init保持DF6C0C52D56EFD41D819C67663458830F4C4F23A6E0350450902DBF0F449DACF。此为静态修订已完成，不称辅助实际执行或独立177通过；主动报root后冻结。


## 2026-10-27 C方案授权范围与隔离验证准备

用户经总控明确选择C；当前cornerstone/HEAD 1f708e20，生产写入仅限eva-web/eva-web-sys/eva-web-sys-service/src/main/resources/mapper/User.xml的BaseResultMap。保所有SQL、方法、全局配置、实体/转换/接口/DB及前端不变。正式设计第8节、仓库规则和权威军团/表命名规则已重新读取；graph impact selectById为UNKNOWN，原索引落后25提交，实际源补核6调用与写保护，不能当低风险。现有QA/designer脏文件及.vscode/GO计划保留。

精确测试文件计划（均属eva-web/eva-web-sys/eva-web-sys-service）：新增src/test/java/org/pkaq/sys/role/service/UserMapperBootstrapFixture.java、UserMapperBootstrapTest.java、UserMapperConsumerTest.java；扩展已有同目录AssignedRoleDetailJdbcFixture.java；新增src/test/resources/fixtures/user-before-c.xml，封存改前User.xml作为隔离反例，放fixtures目录防止生产mapper扫描拾取。既有AssignedRoleDetailTest及其它生产Java不改；保已有手工正序语义用例，但新增普通生产MybatisPlusAutoConfiguration/MybatisPlusConfig组件bootstrap，不能用旧177替代。

隔离方案：只用Mockito DataSource/Connection/ResultSet与合成行，连接元数据仅返回PostgreSQL，任何非允许SELECT抛错计数；无URL/DriverManager/Hikari/Flyway/Redis/网络/实际账号数据。真实Boot4 MyBatis自动配置和现插件创建factory；正常classpath扫描、测试资源User先common/反序分别验证，旧XML反例在内存资源数组中User先common，绝不将旧XML复制回生产或改全局排序。枚举/日期/审计/自动账号字段使用真实Mapper/type handler映射；service写保护用真实映射后的合成实体与写入mock，验证拒绝前无关联/档案/permVer写。repwd只用合成密码，保持原校验逻辑，不加新只读限制。

生产准备需静态XML16项唯一映射和除BaseResultMap外字节/结构不变证明。当前仅获文件准备与静态差异窗口，尚无Gradle/Test/全工程编译槽；旧snapshot-d及全部旧helper封存不覆盖。后续新验证/产物证据使用独立C-20261027命名，先记录源/构建配置/测试/所有classpath产物来源与变更，不能借旧snapshot哈希或旧177称新代码通过。QA独立复验、用户正常重启、live statement及合法UI验收仍待后续调度。无自动commit/push。

## 2026-10-27 C源文件准备完成，等待独立静态审与验证槽

已落唯一生产User.xml BaseResultMap及计划的3个新Java/旧fixture扩展/fixtures原XML反例，新增实施附件docs/legion/design/2026-10-27-user-result-map-c-implementation.md。Python标准库仅静态XML/字节/格式检查：16唯一属性与common13+profile3等价，除整个map块外与HEAD原字节完全一致；旧反例与HEAD原User.xml全字节一致；旧AssignedRoleDetailTest与HEAD一致。XML SHA256 5ED33960854D9203F6AE3D9D1F0F5D8D4F2F822409C49D6169B097C93A89B9E9，旧反例BFC0F6B427D65F8E1F62D3D5364FECDD2F181DBC05C3AC40729BD0144A5F24EF。scoped git diff --check通过，未暂存；LF/CRLF Git转换提示不等于测试执行结果。

最小context仅MPauto/MybatisPlusConfig+合成EvaConfig+唯一mockDS，移除系统环境/属性源，无ConfigData/WebBooter/组件扫描。与WebBooter一致的org.pkaq自动配置根包元数据仅供实际MP注册器扫@Mapper，解决无XML的PostUserMapper而不提前手工addMapper。普通resolver原glob原顺序，无customizer；强制顺序与旧反例单独factory。加组件数量、未知连接/metadata/SQL拒绝及Isolated；共用fixture保旧入口，只有实际JOIN才供profile列。新测试覆盖计划与确切类/命令形态/新C产物门禁方案均见附件。

没有运行任何Gradle、Java编译、Spring context或新旧测试；因此旧反例是否实际复现、C三顺序是否实际通过、全字段handler等价和旧42项回归均待验证，不报绿。旧snapshot-d/全部旧helper保持封存，未新造快照。designer/QA正独立仅读实际文件；本成员交回源码准备锁等待重大静态意见和总控唯一验证槽。无IDE/服务/真实数据访问、依赖、vault、前端、其他成员卡或提交push操作。

## 2026-10-27 QA预审隔离mustfix已静态修订，再冻结待复核

按root转QA具体意见，仅改本轮新tests/fixture与本人文档：新bootstrap的PreparedStatement/Statement/ResultSetMetaData/ResultSet未知调用现统一AssertionError，原手工入口仍用原默认应答；参数setter改精确名称/两参数标量白名单，禁止任意set及流/三参数setObject重载。新增合成负向用例核未知DS/Connection/metadata/statement/setter/resultset及写+batch均拒绝，正向读取零写和负向预期拒绝计数分开。新容器增加单例初始化前BeanDefinition固定类/工厂方法白名单，不仅靠类型数量。

新增实际C映射后的普通账号有/无profile正向save/role/post服务mock写入，补缺tenant列保持NULL、managedList/page/UserWithModuleAndRole注册引用检查、VO无password/合成哈希。原UserAccountProfileServiceTest8项列本批补证，旧实体mock不冒充C端到端；list/page/嵌套仅statement检查未实际分页/嵌套执行的缺口已明确。附件同步具体覆盖及限制。此轮依然只有apply_patch文件修订/静态格式与差异检查，没有Gradle/编译/容器/测试，未称mustfix已执行验证；交回锁待QA读最新文件，再由总控授唯一验证槽。

## 2026-10-27 C受控验证因共享产物瞬态阻塞，临时IDE配置已恢复并交槽

经总控逐轮授权和QA静态审的新C辅助最终Python C06A8B3AA91EFCC4D8412F2D9972EA250182690B893FCFB594E51E74D4273D27、init A053111FAE1D1E07777B0A587B7EFFFE8DF4EAAC4004B2B510BEC5250B1A8C54。旧snapshot-d/旧helpers全部封存。实际各run及失败分类已补实施附件：配置时提前classpath解析、交叉中断、upload缺common、contract快照NoSuchFileException，均保原日志，未测试通过。

用户准A临时关闭workspace自动构建后，最新单次run为.gradle/C-20261027-runs/20261027T143903Z-df529796，main1m17s、9actionable7执行2uptodate，在auth编译前copy门禁停止。诊断实际核到common此前多个依赖source/copy81完整；auth现场sourceReadable=true但before/after/copy均0、didWork=false，故源目录当时为空，不是已证Project.copy漏复制，未判写者。SYS编译/资源和两新Test尚未执行，不能给C报绿；无C快照封存、无弱化门禁/跳upload/关tracking/清cache/改依赖或Files.copy替换。

settings操作前发现用户新增suspendAllThreads:true，最新原SHA059FF8E0D20427CF6AED4E40C0A525942031D5E96E9D9E5B4A427BB8C290DDAE已保字节。仅临时新增java.autobuild.enabled:false；本轮失败后已apply_patch仅删除本次键，恢复SHA精确059FF8，用户新改保留。没有IDE输入/服务Stop/Restart/语言serverkill；后台9016仍PID18544监听。文件false与20s稳定曾证，IDE全idle未证，不把设置当根因或暂停全任务已完成。

结束当前main494对旧d added/missing/changed全空，User输出仍旧BFC0，源仍C5ED；源码/tests/辅助冻结，不再盲目新run。交回唯一验证槽，无实际DB/Redis/HTTP写读验证、发布或提交push；后续需另定共享输出排除范围或受控构建方案，QA/live加载/页面验收未完成。

## 2026-10-27 独立输出辅助按方案静态准备完成，未占执行槽

完整读设计隔离方案，Git仍cornerstone/HEAD1f708e20，现有QA/designer/.vscode/GO计划保护。仅apply_patch新增ignored C-isolated-20261027.py/init及本人附件/status，旧C06A/A053/旧d/全部历史run冻结不动，未改生产/XML/tests/IDE/服务/依赖。新Py SHA8596837C64CE8C1EFC5D3EFC375171D0F256BD07766F807EC08B3492645FBB71；init B905456A6D564BFF8C9F37F520EEC7B08155DC684D9F5747E5661E3F040C32A7。

所有Project早期RUN/products/full-path/build绑定，独立project-cache；所有SourceSet/compile/generated/header/tmp/Test工作/报告及archive/variant/graph输出模型核RUN。源码sourceElements只作为精确已声明只读输入分类，不冒输出、不作compiled CP。编译复制和main/test清单全部实际隔离路径，无旧live产物函数；固定十模块/顺序与两类、offline单worker三阶段、Test-only图、旧d仅User C5ED差异、源/产物/外部jar前后hash门禁保留。classpath配置期保持惰性，任务中实际分类拒旧run/标准build/internal jar。root problems报告预期与实际日志位置另核；插件运行时路径仍待执行证明。

只跑轻量Python AST与默认PLAN：确认无Gradle调用、无run目录创建、三阶段flags/精确筛选，旧辅助/旧manifest哈希未变。Groovy/路径模型/编译/测试尚未运行，不报绿；具体门禁、限制及候选--execute命令已写实施附件。交新文件SHA供designer/QA独立静态审，等待总控另授一次隔离验证槽；本轮不占前端loading QA槽，不轮询等待、不提交push。

## 2026-10-27 隔离首轮配置失败已停报交槽

终审组合为Python3C944ECBA933068FF58E0513C894D40711D43BB5D741E2252621306E0A44561E+init A0BD3342E2A553CBC96B585103C43138BB6A8457754918402D96DFF71EABC63D，含仅自身test编译main.resources白名单及外部jar跨阶段累计锁。合成文件负测与窄diff已报，不改真实缓存。

单次获准run=.gradle/C-isolated-runs/20261027T151302Z-69bf3297，main配置22s exit1，Groovy221行首多行+触发String.positive()错误；未taskgraph/model、未compile/resources/Test准备或测试，测试计数无，不以0项当通过。实际root Problems报告在RUN/products/_root/build/reports/problems；仅此实际路径已证隔离，其它模型未证。未自动修/重跑，源/tests/helper冻结，所有失败证据保留并释放唯一槽。

结束settings仍恢复SHA059FF8，9016仍18544监听，无本轮Gradleclient。live494无增缺，相对旧d唯一User.xml资源现C5ED、mtime22:43:15；本隔离run无任务且不回拷live，归因未知、不作factory已加载证据，隔离baseline不读live。其它493等旧d。完整事实及限制已入实施附件，无IDE/服务/依赖/源码改动或提交push。

## 2026-10-27 隔离c7099319编译通过，25项止于fixture CGLIB白名单

仅获准init二元+窄修经designer审后，以Python3C944ECB/init E1BA9AC3新run=.gradle/C-isolated-runs/20261027T151732Z-c7099319。main/test-classes均exit0，实际图30/15任务；main494(467class27resource)对旧d仅User C5ED差异，新snapshot manifest44D18D9417FB88E868530A9134881CABFF8481618D9B92A1850A0079984876BF。模型19Project/10固定module，SYS测试产物36项，外部累计jar143→169→178，执行前后源/产物/快照/累计依赖门禁均核。

Test图仅SYS:test，45s exit1；实际两类13+12=25项，全25fail/0error/0skip，suite与XML一致。全部同一消息：MybatisPlusConfig被Spring合法增强为$$SpringCGLIB$$0，BootstrapFixture139按原类名白名单拒绝；尚未到Mapper/consumer断言，不报C业务通过/生产回归。分类fixture门禁形态兼容不足，未自动放宽/修复/重跑，源码/tests/helper冻结，结果与候选精确userClass判别方向已报总控待另审。

settings仍恢复059FF8，9016/PID18544仍运行，无本轮client残留；不操作IDE/服务/源/缓存/依赖，不回拷live/提交push。实际model/CP/累计hash/XML/gate保run，实施附件同步。已交回唯一验证槽，无原42及其它消费者回归/独立QA/live/UI通过结论。

## 2026-10-27 d3f8469b实际26项：2通过，24止于getType漏项，已交槽

精确CGLIB窄修仅Fixture ADDFD47C/Test9DE3EFE6后经独立审，新run=.gradle/C-isolated-runs/20261027T153549Z-d3f8469b。main/test-classes0，图30/15/1不变，Test阶段1m31s exit1；26项2pass/24fail/0error/0skip。实际通过配置增强正负边界与JDBC未知/写拒绝负例；其余全部底层strict ResultSet.getType拒绝(23直接、1断言异常类型包裹)而未进入业务判断。MyBatis3.5.19 skipRows391起无条件rs.getType已源码核，分类fixture只读cursor元数据漏项，非C生产回归结论。

候选新bootstrap getType返回TYPE_FORWARD_ONLY与合成next-only游标一致、旧手工路径保原默认，但当前未改/重跑，待另授。原输入/隔离产物/快照/累计jar门禁已核至结果失败前，settings仍059FF8/9016PID18544，无client残留。结果入附件，源/tests/helper冻结，唯一槽已交还，不操作IDE/服务/缓存/依赖/DB/回拷live/commitpush；C业务验收及旧回归/独立QA/live未完成。

## 2026-10-27 7f8f0654三阶段及两新类26项通过，交槽待原回归/独立QA

经独立审准确补getType/单结果metadata false与manual原默认，锁定共用fixture3EBECA1D/Test22B14EFB，BootstrapFixture ADDF/Consumer B9FD/生产/helper3C944+E1BA冻结。单次run=.gradle/C-isolated-runs/20261027T155115Z-7f8f0654，main2m/test准备1m25s/Test1m31s均exit0，图30/15/1，Test仅SYS:test。实际Bootstrap14+Consumer12=26，26pass/0fail/0error/0skip，fresh suite/XML一致，非COMPLETED字符串或旧177。

模型19Project/10module；新main494(467class27resource)对旧d仅User C5ED差异，snapshot manifest44D18D9417FB88E868530A9134881CABFF8481618D9B92A1850A0079984876BF，test产物38，external累计143→169→178。源/构建/辅助/隔离main-generated-header-test-snapshot/跨阶段jar前后门禁全部通过，原失败run保留。细粒度NORMAL逐资源顺序未持久化，现只有资源唯一性断言与实际CP，限制入附件；不夸大完整分页/嵌套执行。

本轮仅这两类26通过，原AssignedRoleDetail42、原档案/其他安全回归及独立QA/live加载/UI仍待。settings恢复059FF8、9016PID18544保持，无client残留，无IDE/DB/服务/缓存/依赖/回拷live/commitpush。源码/tests/helper冻结，实际证据及限制已报总控，唯一槽交还；下一回归需先精确清单另授，不追加运行或轮询。

## 固定20类fresh隔离回归辅助静态准备完成，待QA独占执行

按总控新授权仅新增ignored C-full-regression-20261028.py/init和本人文档；Py SHA833A4293D895E9CE58608CA4A4D4E5F8CC95F2E721DA7B479DC506D33A17D359，init4E0F05675184875E56984B575E42D1D7DB3DA3BEC233D9A90935565DF36093A2。旧C3C944/E1BA、7f8f0654/run/seal及生产/tests/依赖/IDE服务全部未改。精确SYS11/AUTH6/DATA3全类名/源路径规则/先前计数和直接/共享影响已入实施附件，不拿历史177或C26预填新总数。

新三阶段仍10main→三模块testClasses→准确3Test0producer、逐任务exact filters，单worker/offline/全Project RUN产品/project-cache及全部源/产物/跨阶段jar门禁保。Py五函数作多模块输入/清单/命令/实际XML汇总适配，其它门禁函数AST同原C；每个Test前核三个prepared集合，CP仅sealed main+对应module test输出+外部缓存。AUTH/DATA无testresources只省精确已声明且未生成的NO-SOURCE test根，类产物与每类XML必须正，未知目录/缺失/旧run/live/internaljar仍拒。

仅运行轻量AST/defaultPLAN/20源声明及flags比对，确认PLAN无目录/Gradle副作用；用临时合成XML核校验器正例与零项/skip/漏类拒绝，合成目录清理，不算真实测试。正式候选C:/home/DevApp/python3/python.exe .gradle/C-full-regression-20261028.py --execute已交designer和总控供QA审，尚未Gradle、不占重验证槽，不轮询/提交推送。最终实际计数、独立QA与live验收仍待。

## 2026-10-28 QA固定20类当轮203/203通过，待真实运行与UI验收

前述待原回归及独立QA执行项已由QA本轮证据更新：执行者为quality_reviewer，本人backend_developer非本轮执行者，未另起Gradle或复跑。引用QA状态docs/legion/status/quality_reviewer-assigned-role-detail-20261024.md的2026-10-28固定20类独立执行记录；RUN=.gradle/C-full-regression-runs/20261027T162106Z-4bef1b8a。本次只读核对该RUN的identity.json、audit/test/gate.json与audit/test/xml-summary.json：HEAD为1f708e202af0ba817b55c0f6be9a9085eea054ea，test阶段exit0，SYS11类152项、AUTH6类37项、DATA3类14项，共20类203/203通过，fail/error/skip均0，problems为空，各executedSuite均SUCCESS且计数一致；不是以历史177加26推算结果。

QA独立记录三阶段均exit0，实际任务图30/25/3，末图仅三个批准Test且无producer；其独核source/config/test/helper及presence、snapshot494、三模块prepared清单、实际CP及累计178外部jar锁均通过。上述完整门禁结论引用QA当轮独核，本人本次仅核状态与指定汇总文件，未重新执行这些门禁。此前7f8f0654两类26项与失败RUN继续保留为历史证据，不替代本轮固定20类结论，也不扩大为全仓或live/UI验收通过。

源码、tests、helper和配置继续冻结；本次仅更新本人status，不新run、不暂存/提交/推送、不操作IDE或服务。下一阶段待用户正常重启后台并解除Edge扩展UI阻塞后，由总控安排核PEER/LIMIT真实详情D1、运行加载证据及S权限链；当前这些验收仍未完成。不为验收改密或重填部门，不擅设断点或读取秘密。

## 2026-10-28 保存检查点并暂停，等待用户明确继续

暂停前已完成并向总控报告的只读运行核查：当时9016为PID18544，启动时间2026-10-27 21:43:45+08；源码User.xml及标准输出eva-web/eva-web-sys/eva-web-sys-service/build/resources/main/mapper/User.xml均为C修复SHA256 5ED33960854D9203F6AE3D9D1F0F5D8D4F2F822409C49D6169B097C93A89B9E9。标准输出mtime为2026-10-27 22:43:15.831+08，晚于当时进程启动，仅作线索，不证明内存加载旧或新。通过系统进程信息定位启动argfile，内部仅提取classpath/入口允许字段，199项中索引170为该标准资源目录；逐目录/JAR查mapper/User.xml仅此一处提供。未attach/jcmd/断点/自定义HTTP/DB/Redis/服务操作，未扫描未知运行日志；当前factory实际statement及详情链加载证据仍缺。一次路径提取正则误带Java/JDWP启动串，已立即改为精确argfile匹配，未含凭据或环境信息；不在状态卡复制启动串。

上述PID及时间仅为用户此次重启前的历史观测。总控随后转达用户已正常重启后台，但本人未再查新PID或作任何运行操作；重启后PEER/LIMIT部门D1新证尚未取得，Edge官方扩展UI拒绝阻挡复验，不能以用户重启或203/203隔离通过宣称live恢复。独立固定20类203/203证据保留；S权限链未完成。总控转达Cube已移除reduced-motion静态覆盖且CSS检查通过，但真实动画待验，此项不是本人实施或验证结论。

遵照用户“保存进度暂停 等待我继续”，本次仅补本人状态卡保存以上未落卡结果。没有本人在途命令、待恢复的临时设置或持有的验证锁；此前本人临时autobuild键已移除，不重复操作用户设置。立即停止测试、浏览器、IDE、源/helper/配置、数据及服务操作，不启动任务、重试或轮询，不暂存/提交/推送；其余文件保持冻结。等待用户明确“继续”后由总控重新派发，当前任务未全部完成。

## 2026-10-30 发布后恢复：C3用户跳过，余项与日志精确定位

用户最新“这一项跳过测试 继续”仅对应PEER部门D1→D1C的C3，记USER_SKIPPED/NOT_VERIFIED；不再要求该动作或hidden探针，不将跳过扩大到全部权限。00:02:21独立PG窗口证PEER仍D1/SELF/permVer6，LIMIT仍D1/DEPT/permVer3，posts空、两role两R资源和两样本技术字段保持；该时点不是本轮07:09再次核账号/角色。本轮Git核cornerstone/ade2f918，发布后的唯一允许写入是本状态卡，不commit/push。先前30类228/228及11合成PG独立QA通过是代码/隔离证，不能替代live矩阵。此前暂停记录是历史，后续以用户恢复/跳过指令为准。AIBrain项目页分叉/旧177 pending属历史，未拿它否定当前已实核发布。

运行证据分类：C1资源GET拒绝、C2 visible六请求正向按总控/QA最新实跑记录保持通过；早期SELF本人创建/本人读写仅保各自当轮证据，不把按钮隐藏当直接edit-denial。C3跳过；TREE/CUSTOM/ALL/多角色互补并集/ALL覆盖、三日志受限与跨租户、直接编辑拒绝仍未验证。sys账号/角色/组织/模块已有部分真实查询或往返证，新增删除引用/只读及直接接口负向仍按2026-10-23运行矩阵留缺口；字典根/明细契约未闭；岗位/通知/租户/套餐/平台查看缺合法UI入口或可信身份，不造接口/对象、不以97/203/228单测填绿。

本轮仅精确日志技术投影，开发source/build配置相同、dev目标与prod不同，实际eva/eva、default/transaction readonly on/on、REPEATABLE READ；北京时间2026-10-30 07:09:14.874199至07:09:14.908357，rollback/close完成、writes0。业务表log_biz仅tenant0且user_id属于两个固定S账号或b_id精确两S样本、2026-10-23至10-31范围、最新25条；登录表sys_login_log同tenant/两user_id/时窗、最新8条。仅ID、时间、操作类型、user_id/b_id及登录成功布尔，不读正文/operator/姓名/联系方式/payload/堆栈/密码hash。无广扫、无归档扩查，所返业务b_id均NULL，不能从本轮读记录擅认精确样本关联。

可交QA定位：LIMIT业务读记录2115792898971795458、2115792896765591554在2026-10-29 21:08:30（R）；PEER创建记录2115579583825846273在2026-10-29 07:00:52（C，b_id NULL，仅确定主体，不能擅称样本创建关联）。登录2115792863936774145在21:08:22.893074为LIMIT成功PASSWORD；PEER登录2115764777216245762在19:16:46.524918。事件ID存在不等于已通过日志UI详情验收。没有批准的安全错误事件，错误日志不查正文或制造故障。

代码LogQueryBo有begin/end/operator/operateType/mCode/bId，但业务日志页面只提交日期。当前合法UI可设10-29当天，再只识别已定位ID/时间，不浏览或截图其它正文；缺精确主体/ID控件时停止，不靠373条翻页、私请求、DOM私有数据或临时ALL扩大查看。登录日志当前静态route缺口，错误日志仅日期；三日志均受LogQueryAccess.requireAll要求明确0000，DEPT拥有资源也不能读。ALL日志资源须独立逐次授权且仅S事件，不能从包S历史批准推定当前动作已批准。

最短后续：先QA完成不依赖C3的sys只读入口/状态及安全日志定位；再由总控逐次确认当前角色资源/身份并安排资源有而数据scope受限的日志拒绝、组织直接edit拒绝。保持两作者D1时，TREE只可正向见L/P，不能证明D1C覆盖或D2排除；CUSTOM仅D2的原计划前提尚须行动时复核，若仍成立应两样本都隐藏，可做负向读，但不冒称原S5只P。DEPT与CUSTOM此时无互补正样本，原互补并集证据留未验。可提另审最小ALL比较候选：CUSTOM单独（均隐藏）→CUSTOM+ALL（均可见），避免重提C3，仍须用户针对资源/角色动作明确批准，不自动实施或替代原DEPT+ALL场景。真正TREE跨部门/互补并集必须有另行批准的可区分基线，否则保持未验。最后才收尾撤权，不先撤导致阻断验证。所有TREE/CUSTOM/ALL当前完整refs/scope以行动时快照为准，本轮未重查，不伪称仍空或已授权。

本轮无新helper、Gradle/228重跑、下载、HTTP/Redis/服务/IDE/浏览器、源码/配置/权限/部门/样本写或下级代理；QA持浏览器锁。只更新本卡并主动回总控，后续动作待派。

## 2026-10-30 用户继续：历史登录日志入口与逐动作剩余清单

仅只读源/现有成员卡，不重复完整历史梳理、228测试或日志PG查询；本轮唯一写本卡。当前实际菜单依据QA最近安全观察为sys账号/组织/角色/模块/字典和log在线/业务/错误；下面逐动作区分实证，不将源码提供接口当运行已验。C3继续USER_SKIPPED/NOT_VERIFIED，不再要求D1→D1C或hidden。

历史登录日志结论：后端独立API已实现，前端页面未实现。auth模块LoginLogCtrl.java:22、35、49定义GET /monitor/log/login/list及/get/{id}（部署统一前缀下/api/...），不属于/monitor/log/online。LoginLogService.java:68、81读独立SYS_LOGIN_LOG，经applyTenantFilter:266处理standalone tenant0/平台目标/当前租户；LoginLogQueryBo支持account、success、sessionId、ip、riskFlag、begin/end，其中account为like非技术userId精确过滤。现源码V7__API_RESOURCE_PERMISSION.sql:20-22声明资源1000000000000001407/API_LOG_LOGIN，URL /monitor/log/login/**、方法*，挂日志父模块；这是源码seed，不是当前实际授予。ResourceAuthorizationService.java:33-38在资源开关开启且非permit时核角色资源匹配；合法调用还须认证及实际租户/通用数据权限条件，不能只凭管理员菜单推接口开放。

前端config/routes.ts仅/log/online、/log/biz、/log/error，src/apis.ts:49-58仅ONLINE/BIZLOG/ERROR常量；src/utils/menuNavigation.ts:15-24、43-52认可的日志路径/回退映射也仅这三项。src/pages/log无历史login页面/service；online/service.ts:24-25只消费ONLINE_LIST=/api/monitor/log/online/list。全src/config没有/monitor/log/login或历史login入口引用，因此“在线用户不显示既有历史登录ID”是对象不同，不能当日志丢失，也不能继续要求QA寻找不存在的页面。若要该功能需另立前端实现/正式菜单与授权范围决策，本轮不新增。精确历史记录ID沿此前已证，不重复查库。

更正上段历史表述：LogQueryAccess.requireAll及MybatisPlusDataPermissionHandler.java:60-65固定日志守卫涵盖log_biz/log_biz_archive/log_error及相应Mapper，不含SYS_LOGIN_LOG；LoginLogService本身也未调用requireAll。不能宣称历史登录日志已明确要求0000，或把其受限负向等同业务/错误日志。登录的实际通用数据权限/租户与资源联合行为仍未运行验证，需按现代码另审，不在本轮补实现。GitNexus查询LoginLogService未入旧索引，采用精确源码核对，不把图缺失当没有调用。

| 模块/明确未完成动作 | 已有真实证据 | 缺少的验收证据/允许复用对象 |
| --- | --- | --- |
| 账号：当前部署下资料详情与完整角色回显 | S账号已创建，PEER/LIMIT技术部门与roles曾PG独核；最近QA仅精确搜索PEER未开PII详情 | 受控只读完整角色/部门实际回显；复用PEER/LIMIT，不读联系方式或密码 |
| 账号：普通资料编辑往返 | 角色关联C2动作实证，不是一般资料编辑 | 独立可恢复字段/原值及完整表单保持证据；不以跳过C3重提部门编辑 |
| 账号：锁定/解锁 | 代码冻结保护有隔离测试，近期只看按钮 | S账号状态动作及恢复、受限直拒，须行动前批准；不得锁当前执行管理员 |
| 账号：岗位授权与回读 | 两S账号posts空的历史独核 | 专用岗位合法入口/对象尚缺；不向空集合臆造岗位 |
| 账号：删除及引用拒绝 | 已有两账号创建与角色关联记录 | 恢复性、引用影响及清理批准，最后阶段才可用S账号；不删原管理员 |
| 账号：重置密码/跨账号拒绝 | 代码测试涵盖规则 | 本地用户接管及专门授权未有，不能自动修改两S密码或拿源码测试填绿 |
| 角色：TREE查询范围 | 五S角色创建、SELF/DEPT已有实证 | TREE行动时完整refs及新批准角色切换；两作者仍D1仅能正向L/P，跨部门与子树覆盖未验 |
| 角色：CUSTOM查询范围 | CUSTOM历史仅D2；未做当前动作核 | 行动前复核orgIds/资源/完整roles；当前若仍D2应两样本隐藏，不沿旧“仅P”结论 |
| 角色：多受限scope互补并集 | DEPT同D1双样本读实证 | 当前无互补正样本，不能拿DEPT+CUSTOM同结果证明OR；无新可区分基线即未验 |
| 角色：ALL覆盖受限scope | 仅代码守卫/单测证，当前ALL未运行 | 可另审CUSTOM单独隐藏→CUSTOM+ALL可见的最小比较；角色/资源扩大逐次确认，不自动执行 |
| 角色：用户授权回读/移除 | C2 LIMIT仅DEPT已核 | 逆向移除、关系恢复及版本刷新真实证，复用五S角色/两账号；不重复R01已闭合冻结往返 |
| 角色：删除引用保护 | 原R01查询/编辑往返已过 | 被引用S角色拒删及最终无引用回收须单独授权；R01不是可删除样本 |
| 组织：受限直接edit拒绝 | SELF本人创建/本人编辑阶段及C1资源GET门禁 | 资源缺失/有资源但不可见对象的直接edit负向分别未验；仅L/P，不能用隐藏按钮或GET结果代替 |
| 组织：冻结/解冻保护 | O01锁定无入口；S L只读框700×608骨架稳定、0编辑控件及字段底框已实证 | 正常授权冻结/恢复及服务端拒绝证，S样本不得改作者/MODIFY_ID来制造证明，须独立批准 |
| 组织：排序与父子调整 | S四部门父/path曾独核 | sort/移动及path联动/恢复未验；不能动S作者部门或pid绕过C3，保留未验 |
| 组织：删除及引用拒绝 | S样本创建真实，未删除 | 账号引用/子节点保护及最终回收方案，原O01不删，四S部门有引用须最后收尾 |
| 模块：新增与新增资源关联 | M01/M02查询和字段往返恢复已过 | 新建可能增加可访问菜单/资源，缺专门最小对象与确认；不复用创建假功能 |
| 模块：排序/冻结 | 已有M01/M02当前值保持证 | 专门sort/frozen动作及恢复未验；不以普通edit往返代替 |
| 模块：删除/角色资源引用拒绝 | 父子及资源GET回显已过 | 引用拒绝、撤引用和删除恢复方案未有；M01/M02非自动清理对象 |
| 字典：两根/明细归属契约 | D01/QA01查询历史证，旧两根和平表冲突已记录 | 用户归属契约未闭，不能先做新增或写DATA_SCOPE测试数据 |
| 字典：明细新增编辑删除/排序往返 | D01现有明细及排序原值历史记录 | 分别新增、修改、删除、排序与恢复真证均未闭；只能沿D01/QA01原对象边界且先契约确认 |
| 字典：备注保存与开关/删除保护 | 旧VO备注空/未反复写，查看曾通过 | 真值契约、switch/引用拒删独立证；不凭缺字段填值，不删除系统范围字典 |
| 业务日志：安全详情与日期边界 | 已定位S技术ID，原模块写日志摘要，UI表头/按钮已证 | 10-29当天最多两页三白名单ID定位及一详情尚未完成；命中才验字段标签/0控件/加载底框，不读正文，未命中即停 |
| 业务日志：受限资源+数据联合拒绝 | LogQueryAccess隔离测试通过 | 同筛选list/get/count真实联合负向及ALL正向尚缺；资源授予逐次确认，仅S事件 |
| 错误日志：详情/聚合/范围 | 正常入口与表头只读已证 | 无批准安全错误ID；聚合/指纹/详情/受限及租户未验，不制造异常或开历史正文 |
| 历史登录：页面与详情/筛选验收 | 后端独立API和S成功日志存在的历史技术证 | 前端page/route/service/API常量均缺，属实现缺口；现无合法UI可验，不online代替 |
| 在线用户：专用会话显示及只读详情 | 菜单/表头已观察，未读取token；前端静态组件测试已有 | 专用S在线会话当前行与安全字段/时序证，不能看任意会话token |
| 在线用户：强退与受限拒绝 | 尚未执行强退 | 需要独立测试会话及用户本地重登录接管、明确offline资源授权；不能强退当前管理员 |
| 认证菜单：多角色/租户切换刷新 | C1/C2菜单能力字段及资源门禁有实跑，管理员菜单有证 | 切换后的最小元数据边界、旧会话失效/跨租户联合真证未验；不把新脚本token当浏览器会话失效证 |

岗位、通知、租户、套餐、平台租户查看及OpenAPI历史日志在后端有各自接口，但当前实际菜单/route没有对应普通页面，不列为QA“未找到”；缺入口实现/可信租户或安全调用样例，全部另行计划，不从当前8页扩接口或新建业务对象。隐藏TanStack示例不是这些模块替代品。已完成动作不重复：R01冻结值保持往返、M01备注/M02名称恢复、L组织本次只读布局与骨架稳定、C1资源GET拒绝、C2同D1 visible读均按各自真实证保留；228隔离测试与已推送ade2f918不扩大成全运行通过。

本轮无数据库/日志新查询、HTTP/Redis/浏览器、Gradle/服务/源码/契约/权限/部门/新helper/Git写或子代理；只更新本卡。具体执行先QA安全登录/草稿前置，再有界业务日志定位及不需写的既有S只读；任何编辑/删除/强退/角色资源扩大、缺页面实施都另获精确动作批准。主动将本结论交总控，等待下一任务。

### 2026-10-30 历史登录读取政策：实现前源码核验与设计待确认

总控已授权最小后端实现，但要求与 technical_designer 确认资格、物理来源及接管边界后才改源码。本阶段只读当前 cornerstone，HEAD 为 ade2f9185983a94b8792ab9e003a9b3d9ab343c6；现有成员状态卡、设计文档各归原作者，未修改其他成员文件。.vscode 两文件及 GO_REFACTOR_PLAN.md 哈希与受保护基线一致。GitNexus 查询索引落后 45 提交，LoginLogService context 未收录，不能以图中无引用当作安全证明，结论以下述当前源码为准。

当前 LoginLogService.get/list 只有 applyTenantFilter：standalone 无管理员资格即 tenant0，platform 使用 ThreadUserHelper.isAdmin 角色快照可不加 tenant 条件；普通用户缺独立常驻拒绝。SYS_LOGIN_LOG 没有 CREATE_ID/MODIFY_ID，而通用数据权限会拼这两列。MybatisPlusConfig 仅在 dataPermission.enable 时注册 DataPermissionInterceptor，不能只在 handler 增加校验就声称 data=false 下直接 Mapper 调用也被保护。

当前正式私有管理员链为 TenantService 新建时调用 TenantPrivateAccountService，在可信目标 schema 创建 SYS_ACCOUNT；synchronizeTenantAdministrator 调用 TenantLocalAdminService，在同私有 schema 创建 CODE=TENANT_ADMIN、FROZEN=9999 的角色及 SYS_ROLEUSER_REF。私有账号/角色无 TENANT_ID，角色无 SYSTEM_ROLE。core 的 V2__TENANT_ROLE_AUTHORIZATION.sql 曾迁移生成 SYSTEM_ROLE=TENANT_ADMIN 与旧关系，但 TenantAuthorizationMapper.ensureTenantAdminRole/bindTenantAdminRole 当前只有接口/XML 定义，无生产 Java 调用；不能据此认定它仍是私有账号的有效资格来源，也不能 core/private 两源 OR 兜底。

LoginLogService.saveSuccess 的 resolveRiskFlags 也调用原 BaseMapper.selectCount/selectOne，必须保留内部风险读取职责。与设计成员已确认，scope 外拒绝仅适用于新增专用历史 detail/page/count 方法，不能把所有 BaseMapper 查询套人类管理员门禁。候选为专用 SelectProvider 在 SQL 生成前校验局部历史 scope，在 data 开关关闭时仍生效；handler 只对固定历史方法和分页 count 的有效局部 scope 接管缺失 owner 列，不改变全局开关，不复用 OpenAPI scope。此机制仅候选，尚未实现或测试。

来源前置仍未成立：当前所有登录日志写入、关闭、风险读取及历史读取均为未限定 schema 的 SQL，服务自身无事务或显式 core 路由。认证的 TenantAuthRoutingService 会先恢复路由再返回，success handler 随后写日志；其他 close 调用又可能位于已有业务事务。静态默认 standaloneSchema=eva 与 dev JDBC currentSchema=eva 一致，但配置 coreSchema=eva_core 不能证明 schema 模式写日志实际始终落 core；本轮没有读取运行数据库或连接状态，不能将静态配置视为运行证明。未擅改写入、迁移历史或新增契约。

已向 technical_designer 提交完整方案：standalone/core 有效 ROLE_ADMIN 复用 IOpenApiAuditAdministratorQuery 并验证实际表来源；正租户先从有效 core 租户映射验证可信 schema，再核同源私有有效账号、系统管理员角色和 membership，不看 rolesMap、不新增 adminId-only。list/get 统一资格及显式 tenant 条件，target 仅缩小范围，所有上下文 finally 恢复；资源开启叠加现真实 GET 路径权限。私有系统角色识别条件、旧 core 机制是否需兼容以及 schema 日志来源须设计/总控收口，未先实施。

本阶段只更新本卡并向本项目设计/总控交接；无源码/测试修改、数据库/HTTP/Redis/浏览器操作、228 重跑、Gradle、新 helper、服务重启、授权变更、提交推送或子代理。该状态是实现前设计依赖待确认，不是政策实现完成或运行验收通过。

### 2026-10-30 推荐拟改方案：集中 core 与独立审计事务，待用户批准

按总控要求停止继续溯源或 Provider 依赖验证，与设计成员收敛以下可审推荐；仅设计，不能按已批准的查看政策直接实施存储变更。schema 模式的未来登录审计统一 coreSchema，standalone 保持 configured standaloneSchema 的实际默认来源，来源不一致即拒绝而不猜测或切换其他库。

| 现有调用链 | 拟改边界与执行器 | 保持的职责 |
| --- | --- | --- |
| saveSuccess | 一个登录专用 REQUIRES_NEW 写事务，schema 内 CoreSchemaExecutor；risk 的两次 SELECT 与 INSERT 同源、同事务 | 风险读取不套人类管理员门禁 |
| saveFailure | 同一登录专用写边界内 INSERT | 失败审计仍可在无 ThreadUser 时工作 |
| saveLogout | 同一次写边界完成 closeSession UPDATE 与 LOGOUT INSERT，提取内部更新实现避免嵌套新事务 | 退出记录和会话关闭使用相同可信来源 |
| 独立 closeSession、rotateSession、closeUserSessions | 各在同一登录专用写边界更新，schema 固定 core | 不依赖调用方租户事务的 search_path |
| 历史 list/get/分页 count | 独立 REQUIRES_NEW 只读事务，先核资格，再在同源可信日志表用固定 tenant SQL 读 | target 只缩小数据集，不决定 schema；API 和返回字段不变 |

LoginLogService 保持 ILoginAudit 现有全部签名及 best-effort 外观。写事务由独立 Spring 协作者的代理开启，现服务外层 catch 必须覆盖代理返回时的提交异常，不能靠 self-invocation 上的事务注解。复用项目现有事务设施和 CoreSchemaExecutor 的事务级 search_path 与 finally 恢复，不新增通用执行框架；standalone 不重定向 schema。新增事务故障不能穿透导致认证主流程失败。

历史读取统一资格：standalone 有效管理员固定 tenant0；实际 platform/schema 且可信 tenant0 在 core 核有效管理员后允许已批准的全租户范围；正租户从受信 core SYS_TENANT 解析有效私有 schema，使用 ITenantAuthRouter/TenantAuthRoutingService 核同源有效账号、系统 TENANT_ADMIN 角色与 membership，恢复路由后到 core 按该可信 tenant 等值读取。不能使用只允许平台 tenant0 的 TargetTenantExecutor，也不能 core/private 角色两源 OR 兜底。资源开关开启时继续叠加实际 GET 路径授权；开关关闭不取消服务资格和固定 tenant SQL。

拟改文件为 LoginLogService、LoginLogMapper、登录专用 ReadAccess/QueryScope、MybatisPlusDataPermissionHandler，以及仅为登录审计提供代理事务边界的协作者和必要定向测试。专用历史 Mapper 与最小 scope 用于固定可信 tenant SQL、分页同范围及精确避免 owner 缺列；若坚持 data=false 下直接 Mapper 无 scope 也拒绝，才在该专用方法增加局部 Provider 常驻校验，不改变全局拦截器注册。Provider 每次 BoundSql 与分页派生 count 的实际路径仍是批准实施后的定向验证项，本阶段不声称已证明。

旧日志完整保留原物理位置，不自动扫描多 schema、迁移、回填、并集或改 tenant 归属。新视图只读取 core（standalone 为其默认配置源）中已有明确 tenant 的记录及之后新写记录，其他遗留来源不声称已可见，后续读取或迁移另案决定。需用户批准的实质变化为 schema 新写归属 core、审计提交独立于业务回滚及额外事务/连接开销；core 不可用时该条审计可能 best-effort 失败，不能据此阻断登录，也不能称所有历史已集中。

推荐与设计成员已交总控，当前等待总控向用户确认该存储/事务行为变化；无生产改动或新测试进程，不将前端定向验证占槽期间的只读设计伪装为实施完成。

### 2026-10-30 集中方案已获用户批准，后端实现候选交独审

总控转达用户明确批准集中 core 来源、独立审计事务及旧日志原位保留后，已按方案实施；前述“待批准”状态仅为历史过程，不再作为当前门禁。当前 cornerstone HEAD 仍为 ade2f9185983a94b8792ab9e003a9b3d9ab343c6，没有提交或推送。

生产改动共 7 个文件：LoginLogService、LoginLogStorage、LoginLogReadAccess、LoginLogMapper、LoginLogSqlProvider、LoginLogQueryScope，以及 MybatisPlusDataPermissionHandler。LoginLogStorage 的独立 Spring 代理以 REQUIRES_NEW 建立写/只读事务；schema 复用 CoreSchemaExecutor 固定 core，standalone 验证实际默认来源且不改路由，缺表或同名表回退拒绝。search_path 和 TenantContext 在 finally 恢复。Service 在代理外捕获包含提交阶段的审计异常；success 风险查询与 INSERT 同一动作事务，logout 的 close UPDATE 与退出 INSERT 同一事务，独立 close/rotate/closeUserSessions 同源。

历史 list/get 统一实际有效管理员门禁，standalone 固定 tenant0；platform/schema 可信 tenant0 用现有效管理员查询；正租户通过可信 core 租户映射与 ITenantAuthRouter，在私有实际同源账号、系统 TENANT_ADMIN 角色及 membership 核资格，再恢复 core 读且固定 tenant 等值条件。负数/异租户 target 拒绝，平台合法 target 只缩小范围。resource 开关开启叠原实际 GET 路径；data/resource 关闭不取消资格。Provider 在实际 BoundSql 构建时核历史或系统专用窗口，SQL 的 tenant 只来自窗口，不接受任意 wrapper。data handler 仅精确历史/count、风险及会话 update 语句在其匹配窗口内不拼缺失 owner 列，不改全局插件注册或其它日志规则。

4 个定向测试类为 LoginLogServiceTest、LoginLogReadAccessTest、LoginLogStorageTest、LoginLogMapperSqlTest。最终 30/30 成功、失败/跳过 0；覆盖原客户端解析与可信退出身份、所有审计写入口、success/logout 事务粒度、真实事务代理提交失败外 catch、独立事务挂起/恢复及业务外层回滚、源回退拒绝、上下文清理、管理员/伪管理员/失效关系及开关、同租户 list/get/target、实际 Provider 重复 BoundSql 无窗口拒绝、MP 派生 count 与 records 同 tenant SQL、精确 owner 接管及窗口隔离。首次 26 项中发现平台 null target 的 Long 三元表达式拆箱问题，已修复；第二轮增加 count 断言后纠正“派生 count 注册到 configuration”的测试假设，实际为 MP 插件缓存；调试日志全部保留，最终结果为 test-4.log。

本次使用 JDK25 对 7 个新增/修改生产源和 4 个定向测试重新 javac 编译，并使用现有 JUnit6 执行；依赖为此前 ade2f918 已核 snapshot-C 产品与当前逐个复核的 150 个外部 jar，新编译 classes 置于 classpath 首位。证据目录为 .gradle/login-log-20261030，main.args/test.args/run.args、main-2-compile.log/test-4-compile.log/test-4.log 及全部源/产物/依赖哈希在 manifest.json；manifest SHA256 为 D582E2130B00A9799DB4361A61496A43A4D04E9E602E283209AF64BADE090ABB。测试编译存在缺少可选 API Guardian 注解类型的警告，生产编译存在 Lombok/JDK Unsafe 警告，编译退出均为 0；没有将局部 javac 称为完整 Gradle 构建。合成 JDBC/事务管理器配合真实 Spring 代理、core 执行器和 MyBatis/MP，不是真实 PostgreSQL 或运行态证据。

实现候选及精确 SHA 已交 technical_designer 独审、quality_reviewer 独立复验，唯一重测试槽已交 QA，本角色冻结源码/测试等待发现。受保护 .vscode/GO 三文件哈希保持原值；未改他人状态卡，未运行 228、下载依赖、新 PG/真实数据库/HTTP/Redis/浏览器、迁移旧日志、重启服务或提交推送，C3 仍 USER_SKIPPED。应用变更需要重新构建并重启 web-booter 后端，未自行操作；源表存在、部署实际权限及新页面运行态仍待后续有界验证，旧其它 schema 历史不迁移或并集，新页面只承诺集中后新记录。

### 2026-10-30 独立复验通过后的应用编译与菜单运行准备

总控转达 QA 已独立 fresh 编译 7main/4tests、30/30 通过，结果为 .gradle/login-log-qa-20261030T043709Z-8ea9528d/qa-result.json，SHA256=BD1B5A402B97DB6EC664DB5C56F90B2DBC34E537CD201766F50A1C6E5B1565F6。technical_designer 亦已在 login-log-source-review.md 对冻结源码独审通过；生产源/测试保持原 manifest 的 11 文件 SHA，未为运行准备再改源。

总控另授权一次必要本地应用编译和精确 dev 菜单元数据只读。发现运行中的 JDT、Gradle daemon 和 WebBooter 后，未用默认 build 输出、未停任何进程或改 IDE 设置。使用用户本地 Gradle、offline、禁工具链自动下载，将全部项目 buildDirectory 和 project-cache 放进 .gradle/login-log-integration-20261030 的独立目录；18 行 init 在任务执行前核任务白名单和输出边界。dry-run 通过后仅真实执行一次 :web-booter:compileJava，BUILD SUCCESSFUL in 59s，11 个依赖闭包 compileJava 任务全部执行，产出 480 个 class。没有 wrapper、下载、clean、bootJar、test/228、bootRun 或服务操作。

集成编译证据 result.json SHA256=89019919739C9DA1CAB08CFC0140EAB32171CECE084C6EA2D5526BA85009CCB8，compile.log SHA256=7DD240B20BE9FADBD282EAE1109E769E44D65425DECD33AEB474E935901E7E18。冻结的 11 文件当前 SHA 仍一致，受保护三文件哈希未变。这次可以称真实 Gradle 应用主代码集成编译通过；仍不能称 Bean 启动装配、真实 PG/HTTP/UI、部署或完整构建测试通过。隔离产物没有覆盖运行目录，后续应用仍需正常构建部署和重启 web-booter，未自行执行。

菜单元数据两次严格只读 dev PG 窗口为 12:43:19.466698→.497802 和 12:47:13.858975→.871185 +08；dev/prod 辨别、source/build 配置一致、eva/eva、双 RO on、REPEATABLE READ、rollback/close，写 0。仅父/子模块、资源及 role→resource 技术字段，无账号、个人字段、日志或私有 schema 扫描。确认日志父 ID1000000000000000200、三子201/202/203，缺候选204和 /log/login；API_LOG_LOGIN ID1000000000000001407 挂父200。父资源1200与API1407均仅有效 tenant0 ROLE_ADMIN(role1000000000000000004)及旧core正租户管理员role766008469304664561有直接grant；此非账号或私有资格证明。

具体提案/SQL/回退写入 docs/legion/design/2026-10-30-login-log-menu-plan-backend.md，未执行：新增module1000000000000000204，code log.login、标题登录日志、/log/login、./log/login、parent200和完整PATH、sort4；仅把既有1407 MAIN_ID200→204，resourceID/url/method及全部grants不变，不造MENU资源。事务锁/预像/ID-code-route冲突检查与影响1行，漂移停止；回退还原1407挂接并软删除仅本次新增叶，拒绝有其它引用或改造的节点，不物理删数据。执行前需重核grant快照与当前元数据，再由总控呈用户确认，不能把此提案当数据库授权。

设计成员已源审确认复用API资源作叶投影符合当前菜单Mapper，前端静态已有/log/login显式route。另发现并复读确认：AuthorizedMenuService schema分支明确拒tenantId<=0；因此该dev菜单提案不能解决platform tenant0菜单入口，已主动报总控独立收口，不擅改冻结源或补grant。正租户schema仍需真实私有role资源与SYS_TENANT_RESOURCE交集，本轮未查/未扩。最终运行userinfo、前端页面与缓存刷新仍未验；C3维持skip，无commit/push或子代理。

### 2026-10-30 用户批准的开发库菜单两行变更已提交，交 QA 只读复核

总控转达用户明确批准后，已验证已审菜单提案 SHA256=18A055BD787A747D2D1BCF09B5914C52E40E7BB9C6EEE02C67947A22882E919B，冻结 11 个生产/测试文件及 .vscode/GO 三保护文件仍与原 manifest 一致。最新 Git 仅既有源码候选/成员文档及设计文件，本轮不改生产源、测试、他人状态卡或 Git。

仅执行一次已批准的 dev 前向 SQL：新增 SYS_MODULE.ID=1000000000000000204，code=log.login、name=登录日志、PID=1000000000000000200、完整 PATH=/1000000000000000200/1000000000000000204、ROUTE_URL=/log/login、COMPONENT_URL=./log/login、ISLEAF=true、SORT=4、REVISION=0、TENANT_ID=0、DELETED=0、FROZEN=0；SYS_MODULE_RESOURCES.ID=1000000000000001407 的 MAIN_ID 从父200移至新叶204。两行各恰1，已经 COMMIT，连接已关闭，没有失败重试。

同事务先核 dev 与 prod 目标不同、source/build dev datasource 一致、eva/eva 且非 recovery，五张 metadata/grant 表实际来源均 eva；锁定两个菜单/资源表，fresh 完整父预像与旧资源投影精确匹配已审快照，ID/code/route/path 在全表含删除记录均无冲突，确认无未审用户 trigger。实际执行原批准 SQL 前已保存必要技术前像及恢复依据，无账号、个人信息、凭据或日志内容。新菜单数据库时间等于事务时间，未虚构 create/modify 操作者。

提交前验证 module 新叶字段精确、资源仅 MAIN_ID 改变；1407 完整行排除 MAIN_ID 后的指纹前后一致，其余全部 module/resource 行集合的 count/hash 一致，全部 SYS_ROLERES_REF 与 SYS_TENANT_RESOURCE 集合的 count/hash 亦一致。未写 grant、套餐、权限缓存、其它 schema 或旧日志。事务内数据库时间从 2026-10-30 13:07:02.848315 到提交前核验 13:07:39.786625 +08，之后 commit 返回成功并 close；此处未将提交前时间伪称精确提交时刻。

证据目录 .gradle/login-log-menu-apply-20261030T050654Z-dbcc5036，含原批准 forward-approved.sql、preimage.json、verified-before-commit.json、result.json。result SHA256=D0EF103C7B34A55080F636D5CDCA746576DF0334BB5337EE36C6B53731551082，状态 COMMITTED_TWO_APPROVED_ROWS、committed=true、connection_closed=true。回退依据保留原 MAIN_ID/资源预像与新叶 ID；若以后授权回退，按原已审防漂移恢复和软删方案另行执行，本轮未回退或物理删除。

已主动向总控报告，并明确把 DB 窗口交 quality_reviewer 仅作独立只读复核；本角色不再查询或写入该范围。QA 复核结果、userinfo/缓存刷新、前端页面运行、后端新产物装载以及独立的平台 tenant0 菜单缺口仍各自待验，不以两行数据提交替代。未服务启停、HTTP、Redis、迁移、其它 CRUD、授权修改、Git commit/push或子代理；C3 仍 skip。

### 2026-10-30 LIMIT 历史登录资源拒绝本地探针准备完成

总控另派仅准备、不真实执行的四请求 getpass 探针；原目标脚本未存在，现已完成 .gradle/login-log-denial-probe-20261030.py，SHA256=769FE2F8D7C705BD9DB760863C3D38FC5C68F0C8FDB94B9A1D2D5E77BDD93C05。单文件含必要 fake，不改旧工具或其 globals，不建立新验证框架。真实脚本从未执行 --run，只有默认 PLAN 与 --self-test；56 个 fake 场景通过，realHttpRequests=0。

已核 LoginLogCtrl/QueryBo 实际 list/get 路径，AuthCodes.RESOURCE_FORBIDDEN 完整业务码为 0x000-00024，JwtAuthFilter 在 chain 前资源校验失败写该响应；服务内 CommonCodes.PERMISSION_DENY 为 4200，两者不混淆。脚本固定 localhost:9016/api、QA_LIM_20261023、actor2114719096971497474，最多依序 login POST→fetchMenus GET→历史 list?pageNo=1&pageSize=1&account=QA_LIM_20261023→自身批准历史 get/2115792863936774145。不能传任意 base/account/ID/路径；预算最多4请求，90秒总预算，沿冻结 AF5 传输的本地直连/回环复核/限时与大小/无代理重定向重试行为。

默认 PLAN 无网络、不取密码；真实运行仅用户本地 TTY getpass、禁止无回显退化，密码摘要和 token 只在内存。复用已冻结 stage1 的严格 JSON、envelope 和 token 纯函数，启动非 PLAN 模式前核其 SHA。login 和 menu 的 resourcePermissionEnabled 均必须 is True，身份 ID exact int/str 核对。API 两项只有 HTTP200、success is False、完整0x000-00024、空失败data 才通过；任何数据或成功信号立即 UNEXPECTED_ALLOWED 并停止后续读取；4200、过期、未知码或其它失败均不冒通过。输出只白名单阶段/HTTP/code/emptybool与请求计数，不输出 body、账号资料、日志、密码、token 或异常原文。administratorGuard 始终标 NOT_PROVEN，只证明当前未授资源接口门禁，不能证明越过资源后的管理员资格。

fake 覆盖拒绝正向、list/get 意外成功或数据泄露提前停止、业务码混淆、身份/登录失败、两个能力严格布尔前置、请求数量/顺序/目标漂移、总预算超时，以及合成 HTTP 的 connect timeout、timer expiry、redirect、非回环、非法 JSON、内容类型、大小限制、秘密输出与连接/token清理。证据 .gradle/login-log-denial-probe-20261030-evidence.json，SHA256=C86D1FE3EF76B148B77D02AF26429DEB74DD3C45B120DBC86B338394F093AA35，记录默认/fake退出均0、stderr空、冻结旧三工具及生产/测试11文件和保护三文件SHA未变。

冻结脚本/证据/命令已同时交 technical_designer 独审、quality_reviewer 独立 fake 复验和总控；本地测试槽明确交 QA，本角色停止测试与脚本写入，待实际发现再协调。只有独审收齐后由总控给用户真实运行命令，不自动运行；未真实 HTTP/DB/浏览器/服务/权限操作、未改生产源或提交推送，未重跑228，C3仍skip。UI真实进展由总控/QA自己的证据负责，本角色不把菜单隐藏或route403当直接API拒绝通过。

### 2026-10-30 剩余 CUSTOM 数据范围行动前提一次只读核验

总控转达真实登录日志四请求资源拒绝已过，本轮不重跑该探针。另授权一次精确 S 对象技术元数据核验；仅开发库一次连接，无自动重试。数据库窗口 17:09:44.336068→17:09:44.409063 +08，dev/prod 区分、source/build datasource 一致、eva/eva、七目标表真实来源 eva、transaction/default RO on/on、REPEATABLE READ，最后 rollback/close，写0。无个人字段、账号名称/密码、日志、Redis或缓存/token读取；本轮唯一写入为本状态卡。

两账号完整当前角色关系未漂移：LIMIT2114719096971497474 仅 DEPT2113858118523068418，PEER2113894345662021633 仅 SELF2113857683657629697。两账号 tenant0、deleted0/frozen0/revision0，profile_account_id 同各自主键且 dept_id 均为 D1=2113856614139469826；LIMIT perm_ver3，PEER perm_ver6。按两个账号及五角色关系作未过滤全集查询，仅这两条关系，无其它成员或角色。

五角色均 deleted0/frozen0、tenant_id NULL、revision NULL：SELF2113857683657629697 scope0007；DEPT2113858118523068418 scope0001；TREE2113858458634985474 scope0002；CUSTOM2113870080799694849 scope0003，data_org_ids 精确字符串2113856917966462977（D2）；ALL2113870419179364353 scope0000。其余四角色 data_org_ids 为 NULL。完整 resource refs 恰4条：SELF与DEPT各有1000000000000011010、1000000000000011011；TREE/CUSTOM/ALL资源集合均[]，其 membership 也均[]。两个资源实际有效 tenant0/deleted0，分别 GET /sys/organization/list 与 GET /sys/organization/get/{id}，不把页面或父通配作为替代授权。

四部门均 deleted0/frozen0、tenant_id NULL：P2113856261050376193 pid0，PATH=/2113856261050376193；D1与D2=2113856917966462977 均父P，PATH各为/P/自身；D1C2113857168370606081 父D1、PATH=/P/D1/D1C，完整ID链与既有快照一致。未移动任何部门。

L样本2115435549295775746 与P样本2115579583561605122 均 pid0、deleted0/frozen0、tenant_id NULL，PATH各为自身根路径。L create_id与modify_id均LIMIT，其两个有效账号profile部门均D1；P create_id为PEER、modify_id NULL，其有效作者账号profile部门为D1。未修改作者、修改者或归属。

源码当前 DataPermissionEnumm 指定部门=0003、本部门=0001；AuthPermissionContextService 将逗号字符串解析正ID集合。MybatisPlusDataPermissionHandler 对0003生成 EXISTS：有效 SYS_ACCOUNT 与 SYS_ACCOUNT_PROFILE 按 ACCOUNT_ID join、ACCOUNT_ID IN(样本.CREATE_ID,样本.MODIFY_ID)、DEPT_ID IN(指定部门)。不是按样本自身PID或树位置判定。仅对本次两个样本执行同形纯SELECT，CUSTOM_D2作者匹配两条均 false，DEPT_D1作者匹配两条均 true，因此基于当前可信字段，LIMIT单独切CUSTOM后L/P均应hidden。未将该SQL推导冒充真实API的数据门禁结果。

仅核源配置布尔：eva.yaml及当前build对应配置选定项一致，resource-permission.enable=true、data-permission.enable=true、tenant.enable=false；eva-dev.yaml无这些项覆写。未检查运行环境、缓存或runtime capability，不据此声称运行开关已证。

最短下一阶段提案（尚未执行）：用户分别批准CUSTOM2113870080799694849仅授既有两个精确GET资源，以及LIMIT完整roles由[DEPT]改[CUSTOM]；动作后独立回读CUSTOM恰两R、LIMIT仅CUSTOM，之后固定L/P的list/get预期均hidden。PEER/SELF、两账号部门、所有样本和其作者保持，C3仍skip；不新增样本、不动PEER至D1C、不授编辑或ALL/TREE。ALL2113870419179364353若后续用于对比，其两个GET grant必须另列独立确认，不能附带本次批准。现 org-scope-read-probe 的 --expect-peer hidden 仍强制L visible，不能直接用来验CUSTOM双hidden；后续探针或有界UI读须单独收口，而本轮不写新脚本、不发HTTP。

完整前提及最短方案已主动交总控，数据库连接已关闭，DB窗口交回；无grant/角色切换/部门/样本/服务/生产源/Git写，不推进未获批准动作。

### 2026-10-30 CUSTOM 双 hidden 最小探针准备，未真实执行

用户经总控已批准 CUSTOM 两精确GET资源与 LIMIT 单DEPT→单CUSTOM，实际保存由 QA 正常管理员 UI 另行执行；本角色不代做数据库或缓存写，也不在保存前重复核库。仅准备 .gradle/org-custom-both-hidden-probe-20261030.py，冻结 SHA256=EED7FDBDCBD55E3269680A333FC2BDE30DD1B65A6188214C6D317A9DBE53A8FC。

该脚本是最小独立判定包装，直接复用 SHA固定EF9E9184 的 org-scope-read-probe 原 LocalHttp、Budget、target 与其 D005A1E8 stage1 认证/JSON/token纯函数；没有复制新transport、改旧脚本/global或新框架。原请求序列已逐字核为固定 localhost9016/api、LIMIT身份、login/menu/listL/listP/getL/getP 共6。新包装不调用旧 read_check，避免其对L永远positive的旧假设。

源码已核 OrganizationService.list 的空结果为严格[]，get缺记录抛 CommonCodes.CAN_NOT_FIND_RECORD 完整0x000-02001。两list只认 HTTP200、success is True、0000、data类型list且严格[]；两get只认 HTTP200、success is False、完整0x000-02001、空data。任意非空数据 UNEXPECTED_VISIBLE 首错停，成功空detail、list null/{}、资源00024、过期、其它码或HTTP错均FAIL，不把一种错误当另一种通过。login/menu capability均严格True、actor exact int/str ID，资料/对象不展开。输出继承安全code白名单与技术阶段形状，dataScopeRootCause固定 NOT_PROVEN_BY_READS，读取结果本身不证明当前实际角色配置。

默认 PLAN 无网络/无密码，getpass仅用户local TTY，密码摘要和token仅内存，未执行真实--run。PLAN与59fake成功，realHttpRequests0；fake覆盖每一单独样本可见即停、list空形状、成功空get不得过、失败空get合法形状、码混淆/资源拒绝/过期/HTTP错/timeout、严格能力/身份/登录坏、请求上限与秘密输出/token清理。证据 .gradle/org-custom-both-hidden-probe-20261030-evidence.json，SHA256=20125C691E928AFCB0926EE2FC4F55BE03E8CBDC116D2E47041BA0121D268D0C。

冻结脚本/证据已交 technical_designer 独立源审并主动总控；本角色停止脚本写入与fake，源审后交 QA 独立fake复验，不先给真实通过结论。无真实HTTP/DB、服务、生产源码、旧工具或Git写，不重228、不恢复C3；CUSTOM实际保存后fresh PG核验按总控另派。

### 2026-10-30 CUSTOM/LIMIT 两笔 UI 保存后的单次独立只读后验

QA交两笔各一次正常UI真实保存及重开证据后，总控正式派发本次单次fresh后验；此前准备消息与派发交叉，不代表未执行。实际数据库窗口17:28:02.576799→17:28:02.667468 +08，dev/prod辨别、source/build datasource一致、eva/eva、八目标表实际来源eva、transaction/default RO on/on、REPEATABLE READ，一次connect，无重试，rollback/close已完成，写0。无PII/密码/日志/cache/token/真实HTTP或任何授权写。

批准改动已独立确认：CUSTOM2113870080799694849完整资源集合恰1000000000000011010与1000000000000011011，实际仍有效tenant0/deleted0的两个精确GET组织list/get资源；LIMIT2114719096971497474完整角色集合仅CUSTOM，permVer实际由3变为4，未预猜数值。PEER2113894345662021633仍仅SELF2113857683657629697，permVer仍6。

按两账号及五角色查询的完整membership仅PEER→SELF、LIMIT→CUSTOM两行；DEPT2113858118523068418现membership[]为批准移出，TREE2113858458634985474及ALL2113870419179364353也均[]。完整资源refs恰6行：SELF/DEPT/CUSTOM各两个既有R，TREE/ALL资源均[]。五角色scope0007/0001/0002/0003/0000及tenant NULL/deleted0/frozen0/revision NULL未漂移，CUSTOM data_org_ids仍仅D2=2113856917966462977，其余NULL。

两账号tenant0/deleted0/frozen0/revision0、profile ACCOUNT_ID同主键，DEPT_ID均D1=2113856614139469826，完整POST refs均[]。四部门P/D1/D2/D1C的parent/path、tenant及删除冻结状态，与17:09前像完全一致。L2115435549295775746仍pid0、create_id=modify_id=LIMIT；P2115579583561605122仍pid0、create_id=PEER/modify_id=NULL，两样本deleted0/frozen0、path根及tenantNULL未漂移；两个作者及L修改者仍关联未删账号的D1档案。当前同形SQL D2作者谓词两条均false，双hidden源码/SQL预期保持，但尚无本次真实六请求结果，不能记数据scope运行通过。

结果及DB窗口释放已主动交总控/QA。随后收到总控转达用户“三分钟后继续”，本次RO已先完成并安全关闭；仅保存本卡后停新动作，不再查询、fake、HTTP、授权、服务或源/Git操作，等总控恢复。C3保持skip。

### 2026-10-30 ALL 两笔 UI 保存后的单次独立只读后验

总控预授“QA实际保存交窗后自动一次RO”，早期SAVE0阻塞期间未连库；QA于21:39:19正式交两笔各SAVE1和重开读证后执行一次，不等待重复确认也不重复执行。实际数据库窗口21:40:36.114589→21:40:37.066815 +08，dev/prod区分、source/build datasource一致、eva/eva与八目标表来源、transaction/default RO on/on、REPEATABLE READ均核通过，1connect、0写、rollback/close完成，DB窗交回。

实际完整角色关系为三条：PEER2113894345662021633→SELF2113857683657629697；LIMIT2114719096971497474→CUSTOM2113870080799694849与ALL2113870419179364353。LIMIT permVer实际4→5，PEER仍6；两账号tenant0/deleted0/frozen0/revision0、profile同ID、dept均D1=2113856614139469826，完整POST refs均[]。无额外角色或岗位。

ALL完整资源集合恰1000000000000011010、1000000000000011011，scope0000/orgIds NULL；CUSTOM仍scope0003、data_org_ids仅D2=2113856917966462977且资源恰同两R。两个R仍有效tenant0/deleted0、GET组织list/get。五角色完整资源refs共8条，只有SELF/DEPT/CUSTOM/ALL各R2，TREE资源[]；SELF仅PEER，CUSTOM/ALL仅LIMIT，DEPT/TREE membership[]。其余scope0007/0001/0002、五角色tenantNULL/deleted0/frozen0/revisionNULL不变。

四部门P/D1/D2/D1C的parent/path/作者/状态及两样本技术前像均不变：L2115435549295775746 create_id=modify_id=LIMIT；P2115579583561605122 create_id=PEER/modify_id=NULL；两样本pid0/deleted0/frozen0、tenantNULL/root path不变，关联作者/修改者账号未删、profile仍D1。限定集合比较diffs[]，未读PII/密码/日志/cache/token，也未写权限或业务数据。

此前已纯本地实核冻结 EF9E9184 org-scope、D005A1E8 stage1 与AF5FEFBA旧transport工具完整SHA未变，无新脚本、fake或228。org-scope的 --expect-peer visible 对L/P的list和get都走固定样本positive判定，因此适用于本次CUSTOM+ALL；源码ALL优先返回1=1且两个固定样本有效，预期两样本均可见。用户本地Cmder CMD命令为："C:\home\DevApp\python3\python.exe" "C:\home\workspace\lab\EVA-API\.gradle\org-scope-read-probe-20261029.py" --run --expect-peer visible。本角色未运行真实探针；实际六请求由总控组织用户local getpass。结果范围仅这两样本的ALL覆盖与CUSTOM+ALL组合，不代表互补受限角色OR、全表ALL遍历或C3恢复。

实际结果、命令和DB窗口释放已主动交总控/QA；无真实HTTP、缓存、其它授权、服务、生产源或Git操作。C3维持skip。

### 2026-10-31 用户要求保存暂停：字典 A 当前快照

收到总控转达用户“保存 暂停”后停止新实现、测试、命令和审查派发；仅保存本卡与向总控报告。B 的 businessWritable/readOnly 两能力字段未批准，本轮未添加；B/C分类、虚根、分源、缓存及旧数据分类/迁移均未实施，现有角色/样本保留，C3仍skip。

A阶段当前仅改3文件，按最后已完成的本地哈希形成暂停候选快照，不回滚不扩大：DictViewVo.java SHA256=26CBDF5055858AF0D247B8022AE2DC4FCCA3FD84F799A5132745EB9DAE3D6BD1；DictService.java SHA256=AFA80BB99BA4E1F08D38B8ADBFBF0C48126D115D437639863717D3B198C2313D；DictServiceTest.java SHA256=42CBDF3B24A7A1674F77D279202542E3F76976D9E4C1E133E89A0AE38CC29D05。此为暂停冻结候选，尚未生成正式交接manifest、完成设计冻结diff签审或QA独立复验，不能称A正式交付。

当前生产修改为DictViewVo增加真实String remark；DictService在任何主写前对一次读取的旧明细全集预检：ID必须属本主且唯一，null/空行拒，9999行字段变化或遗漏拒；orders/frozen省略时保旧值，key/value/remark严格原值比较；未变9999行保留且跳update，普通/新增主与明细状态只收null/0/1；删除主含9999子行拒；switch全批预检主9999/不存在/无效ID后才写，原diff及事务/afterCommit缓存职责保留。没有修改MapStruct转换器注解或数据库，备注依赖同名自动映射。

验证使用现有最小输出隔离方式，临时init在.gradle/dict-a-20261031，把build/project-cache放独立目录，offline用户本地Gradle只筛DictServiceTest与DictConvertTest；主依赖闭包编译、生成MapStruct及两个测试类编译实际完成。首轮25项中1项为重复ID早拒导致Mockito未使用桩；补混批/只读显式及省略/普通子共存用例后的第二轮33项中2项同为正向测试里被覆盖的无用桩。删除该无用桩后，最后命令session53332已在暂停消息前结束，exit0、BUILD SUCCESSFUL in30s，日志.gradle/dict-a-20261031/run-final.log。尚未另开命令统计最终XML明细，不把此前失败日志覆盖或称完整项目测试通过；早期run-first.log/run-second.log保留。

最后已执行检查确认登录日志已验11文件与.vscode/GO三保护文件SHA均原值；A改动范围差异检查通过，未碰他人状态卡。无活动测试/编译命令或DB/浏览器连接，唯一后端测试槽空闲；未派QA独测或冻结后设计复审。恢复后的精确下一步为：先重核暂停3文件SHA与最终XML结果，整理A最小manifest/diff，交designer按3文件独审与QA独立定向复验；有实际发现再最小修复。未A独审/QA通过前不推进B/C，也不擅添加未批准能力字段。

本轮无真实dev数据库写、分类/迁移、Redis或运行缓存操作、HTTP、服务启停、clean共享产物、依赖下载、Git提交推送或子代理。应用A最终仍需正常构建并由用户安排重启后端，当前未重启；暂停期间不做新动作，等待用户继续。

### 2026-10-31 继续后仅核证字典 A 并正式冻结交接

总控转用户继续，仅解除A收尾暂停。按总控暂停记录与本卡核Git、三文件暂停SHA、登录11源及保护3文件，均无漂移；未覆盖、重写生产源码或重新运行Gradle/测试。当前A冻结改动仍仅DictViewVo(26CBDF5055858AF0D247B8022AE2DC4FCCA3FD84F799A5132745EB9DAE3D6BD1)、DictService(AFA80BB99BA4E1F08D38B8ADBFBF0C48126D115D437639863717D3B198C2313D)、DictServiceTest(42CBDF3B24A7A1674F77D279202542E3F76976D9E4C1E133E89A0AE38CC29D05)。

最终原有XML实际汇总DictServiceTest28项、DictConvertTest5项，共33/33，failures/errors/skipped均0，实际case名与XML哈希已记录。已有run-final.log为BUILD SUCCESSFUL in30s，SHA256=0F905F1FD5BBC8D3413576D7AD74A1801F7338DF8739A253A94FE27A1029A114；最终graph中compileTestJava和test为执行、主compile等为UP-TO-DATE，主闭包在此前轮次已编，不能冒作恢复后fresh构建。前两次Mockito无用桩失败日志run-first/run-second原样保留。

只读核生成DictConvertImpl.java包含setRemark(entity.getRemark())同名映射，SHA256=50B5CC523239CB98E9C0282C10E142BE2A1F143DD639FFADC67EADA89517B027；7个相关class产物、未修改的DictConvert/DictConvertTest源和两个XML全列入交接。没有当时全仓预执行输入hash快照，已明确写入限制而不补造历史证明；当前三源码可与final后暂停SHA精确绑定，QA独立fresh定向编译用于补验证，不因此重跑全套。

正式manifest为.gradle/dict-a-20261031/manifest.json，SHA256=D30E31C3239FEF62B7E0DB0A672EAB34814906A6FBF86501E008B2CCE31C1342；冻结diff同目录frozen.diff，SHA256=235A7577BCD577E42B80CDA7E2BA12D821ACFED305845AB98E1FAA20FC1B29EB。已交technical_designer绑定最终签审、quality_reviewer独立定向复验和总控，测试槽交QA；本角色停源/测试写，无活动编译或测试命令，不将尚待的独立签证称已通过。

B businessWritable/readOnly仍未批准，B/C分类、迁移、虚根、分源和缓存不实施；旧数据、角色/样本保留、C3skip。无DB、HTTP、服务启停、Git提交推送；应用A仍需正常构建部署并由用户安排后端重启，真实运行未验。

### 2026-10-31 A 终审阻断后的保护快照窄修新冻结

designer终审发现旧D30E/33候选的listItems受owner数据权限裁剪，SELF主体可操作主而看不见他人9999子，遗漏/删主保护可能漏检。已正式停旧版QA放行；旧manifest及33项XML/所有日志原样保留，不能用旧绿色抵销缺口。总控另正式授权本次A内窄修，未扩大到B/C。

新增package-private DictItemSnapshot，入口每次自行通过原权限Mapper按主ID等值FOR UPDATE取得可操作主，而非信任传入实体或客户端ID作为授权；先核有效ThreadUser、真实事务及绑定连接、运行模式与实际SYS_DICT/SYS_DICT_ITEM关系schema。schema模式从core实际SYS_TENANT源与可信resolver解析、匹配当前正租户上下文，CoreSchemaExecutor恢复原search_path；错/缺映射或来源拒绝，不读保护数据。随后只参数化读取当前主main_id等值、deleted0、frozen9999的固定保护字段并锁行，不取隐藏普通行。edit在主锁后一次原visible明细快照，合并仅保护集合预检/diff；隐藏普通ID仍拒，保护行未变不update。del同入口先查保护行拒删。UI list/get继续原scope，无全局Ignore、ALL或新通用窗口框架。

新冻结共5文件：DictViewVo SHA26CBDF5055858AF0D247B8022AE2DC4FCCA3FD84F799A5132745EB9DAE3D6BD1；DictService SHAAD27E1518779749115053A0A3CC5C733A0BD15FC8D85262D677840DCA5136E91；DictItemSnapshot SHA96BE73DD2F6D35DD07F4BB955F40F64F4C57ED04EA3904248493F68F15BE359B；DictServiceTest SHA7DF22D30117680D2C9A8F169B61705B08797EE99E4AE7CD5301031699D185E1D；DictItemSnapshotTest SHAA0F954F42F89B5AD0C0EB5F7658007EA885A2FAF706876A6213A0EED273EF40C。

新隔离目录.gradle/dict-a-integrity-20261031，沿最小init/offline用户本地Gradle，只3字典类。最终XML实际Service28+Convert5+Snapshot8=41，failure/error/skip0，run-schema.log BUILD SUCCESSFUL in47s。首轮37中1项夹具把缓存按code查询误判主锁查询，修夹具后通过；其后按designer要求补schema分支回归，包含真实CoreSchemaExecutor+合成Jdbc的正租户成功、错映射与缺映射路由恢复。真实DataPermissionInterceptor仅证SELF SQL改写，实际Mapper/JDBC行结果仍合成，未称真实PG执行或并发锁压力验证；同时覆盖隐藏9999遗漏/删主零写、未变保护合法保存且无子update、隐藏普通ID拒、无父授权/无事务/无身份/错源拒读。相关限制写入manifest。

新manifest SHA1191CA15373D72FE37C03283772F4CB8A6E5E73D2702B2A91D13C37A8CADCC84，含inputs精确5文件副本、完整新旧文件diff、每个case/XML/产物/日志哈希。已交designer最终绑定审查、QA待验材料及总控，明示终签通过前不跑新版、不跑旧D30E；本人停止源/测试写并交测试槽。登录日志11已验源及保护3文件再次核原值，旧D30E哈希保持不变。

无真实开发库写/PG/HTTP、Redis、DDL、分类迁移、服务启停、下载、全量228、login或前端回归、Git提交推送；B两个能力字段仍未批准，B/C不实施，C3skip。当前仅新版候选与本地定向通过，独立终签/QA及真实运行仍待。

### 2026-10-31 B 批准后实施与来源窄修待最终复验

总控已转达用户批准B两个响应布尔businessWritable/readOnly，A1191已获designer及QA独立41项通过；旧卡的“B未批准”仅为历史状态。当前仅standalone B，未进入C分源、v2缓存或Serializer重构。SYS_DICT新增nullable CATEGORY及两值check迁移只成文未执行，无旧行猜分类/UPDATE。list保持平数组并在成功外层输出严格boolean能力，row/get输出category/readOnly/remark；get可选category核真实类，edit只接受省略或BUSINESS，真实SYSTEM/NULL各写口拒绝。有效维护关系查询同事务真实账号及tenant0有效ROLE_ADMIN关系，不信rolesMap/ALL或资源、数据权限开关；开启资源门禁时再核有效角色/模块资源及原匹配语义。sys未新增auth依赖，原code KV缓存/query/select/fetch/validate及afterCommit旧新code刷新保持。

首次全新隔离.gradle/dict-b-20261031 offline单worker编译依赖闭包和四字典类58/58通过，run-1.log为BUILD SUCCESSFUL in1m27s，首XML另存first-run-xml。designer随后发现可信表名单漏SYS_DICT_ITEM，管理读可能跨search_path取同名明细；首轮自然结束后才最小补表来源核验及缺/异源子表拒读零Mapper调用测试，另补省类业务更新、混合switch零写、保留code及操作资源拒绝回归。11个B源输入已冻结为inputs-pre-final.json和逐文件inputs副本，frozen.diff SHA38016FE5B355821696CF40EE3A338E3F070817706E3D392FCD1EFBA549D24A18；补丁后最终复验尚待，不拿首58证明补丁。版本门禁识别受控V14列型/约束名称/validated，不声称检查任意外部同名约束表达式。

唯一测试槽已先交frontend做批准的B聚焦回归，后端冻结源并等明确交回再增量复验；随后提交精确manifest给designer终签及QA独立验证，不并行重任务。Login11、保护3和旧A1191证据均精确核原值。无真实DB/DDL/DML/HTTP/服务启停/缓存清理/下载/提交推送，C3skip及角色样本保留。

补丁后冻结复验已完成：frontend第二轮自然结束明确交槽后，后端在同一隔离目录offline增量执行，sys-service主编译、测试编译及test均实际执行，其余依赖UP-TO-DATE；run-final.log为BUILD SUCCESSFUL in33s。最终XML为DictServiceTest28、DictConvertTest5、DictItemSnapshotTest8、DictBusinessAccessTest25，共66/66，failure/error/skip均0。11个源与3个support执行前后hash一致；MapStruct产物含category及remark映射。正式manifest SHA256=3C2C96FA4500C5A1D8890803E0C572AA18D7F3722D14B2B7609EE1A3209198DE，含11输入、3support、14保护hash、27产物、各case/XML、diff和日志。首58 XML留存first-run-xml，只作补丁前历史。

进程已自然结束，唯一重测试槽明确交还frontend；本角色停B源码和重测试写，正式候选交designer终签，QA独验须待前端归还槽及设计终签。当前66为本地合成JDBC/Mockito定向证据，不冒作真实PG/运行时/数据库分类部署已验。另总控已派模块编辑新堆栈只读triage，先冻结B再独立核源码与产物，不混入B或自动重启服务。

designer已绑定3C2C96FA正式清单通过B源码终签，SYS_DICT_ITEM来源缺口关闭；未新增阻断，QA另行独验。本角色未重跑。精确执行参数补记command.json，仅从实际runner转录，不改正式manifest/源或冒作再次执行。

模块只读triage：现getModule第193行为声明、199为mapper.selectById、203为ModuleConvert.entityToDetailVo，不调用StdService.get。B隔离fresh与共享build两份ModuleService.class字节码一致，SHAEE56A2982145C592B082F995C77A4E24213C59E8B242CF1567C55ACECB7FCB66；javap行号表也无193执行点。历史86cc7b2c已有该修复，原ModuleServiceTest确用Mappers.getMapper真实ModuleConvert及RoleConvert并断言字段；历史baseline失败/fixed成功日志仍在，旧共享XML已不在，本次未运行模块测试。用户四层栈不对应这两份当前class，但尚未核实际JVM加载来源，不能确认运行实例已升级；建议补该次请求时间/实际后端进程及已加载类CodeSource/资源路径与构建ID，不先改源或重启。

总控另正式授权一次限定dev只读元数据调查。2026-10-31 15:31:03.483415→.591310 +08仅1connect，开发/生产区分、源/构建datasource一致、批准target匹配、eva/eva及两表实源，transaction/default RO on/on、REPEATABLE READ，0写、rollback/close完成。结果.gradle/dict-b-metadata-ro-20261031T073102Z-634822d9/result.json SHAF7FFB2A55613441197D5E1F671B903E12B6B8758201B1144537A28C6AE614AF0，读后不再查询。CATEGORY列不存在；47主项中9有效，8个精确ID/code匹配当前V1内置主项为SYSTEM候选，QA2111791956297613314为UNKNOWN，不凭type/code名称或frozen0猜类。DATA_SCOPE现deleted0、明细总8/有效5，非照旧deleted1快照；38非有效主项保持原deleted非零，其中历史SYSTEM_DICT不重建。有效code无重码，历史ALL/DELETE各重复仅非有效；SYSTEM/BUSINESS无真实code占用，虚根不用落库。

离线候选/47行技术前像/UNKNOWN选择与启用回退门禁写docs/legion/design/2026-10-31-dictionary-b-classification-preflight-backend.md；未取PID/PATH、keyValue、remark、联系人或业务正文，未跨schema/生产读取，未执行V14、分类DML、缓存或HTTP。完整候选待用户审核，UNKNOWN未决必须阻止分类版启用，不静默漏行或第三根。B源及清单保持冻结，C与样本角色不动。

### 2026-10-31 同步新基线后转单初始化整合（进行中）

总控同步主分支快进至064ed4f9cfb588527970926a324b9f59cb219a2b，目录原未提交材料保留；Flyway13.12.0/Minio9.0.4。本角色未做Git同步、提交推送或覆盖。旧B3C2C/66和13.11产物只属旧HEAD，不能称新基线已验。用户确认九行分类后已准备过未执行的V14离线工具提案，随后用户要求发布前仅一生产Flyway初始化，原V14执行方案现退役封历史，不再运行、收口或求执行批准；真实开发库数据/history未动。

按新任务主db/migration原14份收为唯一V1__INITDB.sql，租户目录V1/V2收为唯一私有模板（非生产Flyway链）。主直接建最终account/profile、保账号审计/凭据/关系，最终日志/归档/notice/租户全局code/schema约束及strict provision函数，角色授权final字段表索引、API13+平台GET3、8内置SYSTEM/CATEGORY。原历史backfill/rename/跨schema升级循环删除；已批登录叶204/资源1407父204直接入新空库seed，不额外扩大grant。旧主V2–V14及tenantV2共14增量删除，原16份SQL精确前像存.gradle/single-init-20261031/before。重复docs全量SQL/旧V12维护工具移除，账号说明改唯一入口。dev/aliyun/test-dev仅baseline-on-migrate默认false/注释，enabled、DSN和秘密不变；非空无history不能自动baseline套新init。

provisioner仅跑一个最终模板并写自有账本1；有账本只接受最终账户表和单版本1，不自动处理旧升级账本。designer指出无账本仅marker/旧role会被IF NOT EXISTS接纳，已按root授权最小补当前可信schema pg_class关系/pg_type相关类型非空拒绝，未跨schema数据扫描。合成用例同init-tenant实例加入仅marker及仅旧role两个schema拒绝、不建account/version、marker保留。未新造schema_only模式；designer已撤回其无来源core空私有表要求，保持真实主/私有模板边界。既有_line/_ref不一致未擅重命名映射。

矩阵在docs/legion/design/2026-10-31-single-init-consolidation-backend.md，列原各版本结构/种子去向及删除的历史过程。旧7升级类由SingleInitializationPostgresTest最终状态/单1/validate二次0/旧history及非空库拒绝/真实provisioner隔离替代；现有路由、身份、授权事务fixture仅必要适配。拟固定5类、6自有合成PG实例，薄.gradle/single-init-20261031.py/.gradle复用旧helper只读pg_preflight/observe_processes与PostgresInstanceEvidence协议，所有producer/PG/tmp为新RUN，当前13.12 CP检查，缺依赖停、无下载。designer已认可范围，薄工具待只读收口；目前仅Python语法解析/源检查，未启动Gradle/PG/服务或新连接。

DATA_SCOPE新空库五个已实现值还是原八seed仍待用户全局决定，不能将devR仅恢复五值当全局政策；当前暂保原八候选/测试断言，不称最终seed通过。不实现岗位权限或删枚举。Login11与.vscode/GO保护3再次精确核原值；原B唯一V14路径被新初始化明确吸收，其余B源保持。实际dev/prod迁移/history、C、Redis/HTTP、重启、clean/baseline/repair、commit/push均未执行。

用户随后明确批准新初始化只预置已实现0000/0001/0002/0003/0007五码。已仅删除主V1三条岗位ITEM种子，原ID/枚举/权限实现及现库不动；新测试改完整五码集合、矩阵记录独立政策授权，原八值前像保留。最终候选主V1 SHA BDDFAAF79F63E637011011FEBA993DA6DCF2F4FED6D771740A3704CE00770DC4，租户模板 C1366A460C047FC0E13FAC7F1E6E7B8A280B3AF129A782AEA1E5A2D02F2823BF，新PG测试949678E67DC7EC3697A3068B34CBEED1D2170CA2124C2705A1E66D96D3D268EC。

总控已把薄工具独立安全审交QA，解除等待designer同项终签；designer保生产迁移内容审。工具py157F10B880496A8B8EA25507ED05F9FC079047D110EBA680D1A8B22363C16E64/gradle736452AD812D0602CA3D5B5C353FD5338FB11EC4137261A730E826A24F7C4E13保持冻结。当前QA已开始独立只读审，收到明确通过即按既授唯一槽执行5类/6PG，无需再root ACK；此刻仍未运行Gradle/PG，不拿范围认可替代工具审查。

### 2026-10-31 单初始化实际验证与交叉归属候选待审

随后按QA工具安全通过与总控唯一槽实际执行四轮，所有旧失败日志/XML保留。121646Z-ce9c86a9因测试List导入遗漏编译失败；修复后122031Z-6de54302五类18项中17通过，SingleInit3/SchemaMode5/Identity7/Contract2均过，AuthPG套餐fixture缺CODE/NAME失败。只补测试fixture必填列后123744Z-d55a7473单授权1/1与Gradle通过，但OS监听归属0、整轮门禁失败。再按一次获批的十秒握手窄诊断125255Z-7af920cb，旧observer对早期RUN postgres父链不完整中止，无法ack，Auth1因握手超时失败；不称功能或整轮通过。

7af920cb sample81窗口12:54:44.188938→47.367820Z，候选PID1620/start44.730519Z/parent47724、RUN postgres正确hash但data=null/ports[]，父47724既不在同payload也不在known.history。正式JDBC postmasterStart48.782637Z晚于该采样，不能冒配为正式网络服务或推定bootstrap角色。四轮终态无残留、生产输入无漂移，无强杀/真实库/服务重启。候选旧快照及四轮证据索引.gradle/single-init-20261031/candidate-evidence.json SHAB0CEBA5F363B0906C85FB9A869513A003EFA295AF66945AF12C57950F9E37486，状态NOT_RELEASE_READY；该快照不覆盖后续工具改动。

总控已明确批准QA建议的等价交叉标准，现仅test-only Evidence和派生观察器候选：持有只读JDBC backend连接最多十秒，独立request保留backendPID；同次OS payload必须交叉本RUN pidfile/serverPID/start/nativeSHA/data/loopback端口以及backendPID/start/真实parent=serverPID，再结合nonce/version/JDBC时序；全部满足才ack。原helper7392不改，精确旧父链错误保留legacyParentFailures和UNKNOWN继续收样，其他错误、终态未知残留仍失败，不谎称恢复JVM祖先链。cross-proof模块807EFC90、diagnostic py1CB171F7、Evidence943794D7已交QA增量源审，尚未运行新Gradle/PG；负向临时文件自检011CC394通过、AST通过。获QA明确安全通过才按root既授一次Auth1，成功后才最终5/6，失败具体交接不盲循环。生产迁移/服务/配置保持不动。

之后源审绑定更严格字段/class/path检查及pidfile保原文版本cross CBECAA86/diagDBA1FCC2通过，实际唯一窄run130832Z-b1599817失败：Auth1fail1，sample71 crossProof发生FileNotFound，未ack；terminal[]/drift[]。request13:09:56.266384Z，server7928/backend15264父7928/port56160的payload完成13:10:11.488245Z，而XML明确13:10:06.315 shutdown、06.425 close完成，未获得完整pidfile交叉证明，不能称通过。原request和rawOSdata的Path.parts逐段相同、无非ASCII且lexical等值；双反斜杠仅异常repr不足以证明路径格式错误，未做unescape或放松resolve。具体阶段当时未保存，不能从错误字符串断定为哪一次resolve。

总控另明确批准性能窄修：原helper不动，派生reader精确三锚点批量一次监听表读取，内部筛RUN postgres PID并按PID映射原ports；保原validator、UNKNOWN及全交叉条件、十秒上限、终态门禁。cross加每次resolve的受限stage；可信data固定pidfile首次读即xb/fsync保原字节，重读严格同字节不覆盖。新候选cross CF2D6C6ED1D0C6834C92AD0038CEB7D3BA575F67CE47612A5E601BE633810C52/diag75516A05EFF2485B2A55DF96A32EB485E9B1EDB8DA6098DB932A8E4BEFFCC18E已交QA增量源审，尚未新run；两个纯mock/临时文件自检通过，涵盖单次调用、正确PID映射、错误PID和外RUN不借端口、非loopback保留由门禁拒、获取异常无proof、未知模板拒、WindowsPath双slash等值及snapshot变更拒。上一轮源保tools-b1599817，不覆盖旧失败。新候选须QA明确通过后才一次Auth1，再成功才最终5/6，不自动循环。

QA绑定通过后获批唯一窄run131604Z-1d58b2a4实际失败：sample82明确pidfile/JDBC mismatch，Auth1fail1/gradle1/ack0/terminal[]/drift[]；pidfile原字节已保存，PID45228/data同RUN/port61370/listen127/ready，第3行1793452674，JDBC13:17:55.050226Z，差1.050226秒触发本人无来源的<1秒条件。OSserver创建13:17:54.854901Z；真实83样本均没有request backend27756，不伪造pair。旧源保tools-1d58b2a4，旧失败不覆盖。

现按官方REL_14_22源码和总控明确标准删除错误阈值，采用整数UTC微秒O<(P+1)秒、P秒<=J、O<=J<=B<=同payload完成S，十秒握手及其余门禁不变。cross DCBD4717A3401193DBA9A3371FEC5BDC4832A9F021F79B7512E3B6A5E806E887、最终py1328994D4DB2328F771662BD36D8833165EF17BE6161B40AE451814B5833D8F6/gradleC55BB55F1F8D08FEC1489FDD5E6A07563681CFC55D638D463DC1951659B6D075已交QA源审。最终固定5类Identity7/Single3/Mode5/Auth1/Contract2=18及6标签/6data/6proof，保offline/current13.12/native14.22/RUN隔离与输入seal；helper7392/Java9437/生产源不动。离线真实131604重放PENDING_PAIR无ack，明示synthetic完整pair正例及13逐条件负例全过，绝不作运行证据；原两纯fake自检及AST通过。总控正式授权QA通过后直接一次最终5/6，不再单PG循环，真实新RUN缺任一门禁仍失败。

### 2026-10-31 单初始化最终五类六实例本地通过并交QA后验

QA绑定最终源锁并独立执行三纯离线自检通过后，仅运行一次最终RUN `.gradle/single-init-runs/20261031T132455Z-638bc62c`，自然exit0。Gradle BUILD SUCCESSFUL in3m28s，32 actionable tasks均executed；实际XML Identity7/SingleInit3/SchemaMode5/AuthPG1/Contract2=18，failure/error/skip0。六label/六不同data/六真实crossproof/request/ack/pidfile原文及同批OSserver+backend均齐，sampleCompleted保存，observerError=null/diagnostic{}/terminal[]/drift[]，本地严格门禁passed=true。12条旧parent失败与全部UNKNOWN候选仍在，不宣称旧祖先链恢复；旧六轮失败/离线synthetic没有拼入此成功。

源锁crossDCBD4717/finalpy1328994D/finalgradleC55BB55F/Java9437不变，原helper7392及157F/7364工具未改。完整本RUN证据已正式交QA独立后验，尚不称独立终签通过；本角色停止源和重测试写，整理最终manifest与14保护文件复核，不追加PG/测试。真实dev/prod、Flyway历史/repair/clean/baseline、HTTP、Redis、应用重启、Git提交推送均未操作；旧库后续方案另经用户明确批准。

QA随后已独立终验并绑定正式manifest通过。最终清单 `.gradle/single-init-20261031/final-638bc62c/manifest.json` SHA F604BC566082A41DE9349DFB657C1F6038BE2C273DF43B860004552D814AC0A9，21源/14保护/36artifact/10关键产物/204去重外部依赖/16原迁移前像精确核匹配，18case与6crossproof一致。清单创建时的PENDING状态按不可变证据原样保留，独立终态另附本RUN `audit/test/qa-final-manifest-binding.json` SHA4B85B5AC125E854D72C6643270E60EB0EF973A7F6269CB1A23B98CF4A4D96BD8（QA_FINAL_MANIFEST_BOUND_PASS）；原始独验60AB5A09及范围补证29E8267D同时保留。

QA范围明确仅本单初始化子任务：新隔离库单V1/单私有模板/五码/八SYSTEM无QA种子，以及旧history或非空私有schema拒绝；本地18/18和六实例完整交叉独验通过。没有宣布EVA总任务或现库处理完成。后端槽已空闲，后续不再源写或重跑，仅将此冻结候选/QA终态交总控组织其余验收。

### 2026-10-31 现开发库保数据：一次fresh只读取证与离线适配交设计审

总控转达用户选择现库保留数据、仅精确必要结构和已确认分类、不自动改Flyway历史；当前只授权一次fresh dev RO及具体备份/执行/回退方案，不授权dump或写库。按obsidian-memory仅读EVA项目/权威命名及军团规则，权威资料按历史指引使用，实际事实以此次源/库证据为准；未改AIBrain或Codex记忆。

RO工具.gradle/dev-category-adaptation-readonly-20261031.py最终604E550FCAC5BDF44D871D07ED07A87AFF6DD6635EEF1D734EBD322DD9818C23获QA安全源审后，仅执行一次。结果.gradle/dev-category-adaptation-ro-20261031T134806Z-fe20cfc2/result.json SHA73AF8FEAB6517FF2562C882929949AE35AF485D3D1CD5125C098E5EEBBB3864C，21:48:07.526505→12.550380+08，单connect/27SELECT/RO双on/repeatable/rollbackclose/写0；源构建datasource一致、devprod不同、eva/eva与批准指纹匹配，F604和14保护原值。QA未重连，独核报告9A5767F0811D6A68A17898724F361B123CD3BDD5792E4EB32E9DF0F6BBDEEFDA通过。

fresh库PG18.3/version180003，CATEGORY/check缺失；默认名flyway_schema_history全schema catalog空，无该表历史行，不是旧V13已验；运行实例自定义history/覆盖仍须核，不创建或改历史。47条技术前像全同旧、9有效含已批QA BUSINESS/8SYSTEM、38非有效原样、有效code无重码。必要8表/account_profile均eva普通表，B所需账号/角色/授权列已有；无相关用户trigger/eventtrigger/RLS/rules/partition，owner-member布尔均true。仅读列名等catalog、主项id/code/type/frozen/deleted/category、item/授权技术ID摘要与计数，无账号口令/个人值/日志或明细业务值。Flyway源+构建enabledfalse/baselinefalse，当前维护进程3环境覆盖键不存在，不冒称实际运行JVM绑定已核。

已新增独立维护提案docs/legion/design/2026-10-31-dev-category-preserve-data-adaptation-backend.md（D8A59A6A），forward SQL9B855B68/rollback SQL4FB67D22，均不在Flyway目录、不复活退役V14。事务片段精确47前像与9映射、category列/check+9分类同SERIALIZABLE事务、技术摘要前后同、receipt fsync后才由受控入口commit，unknown不重试；默认回退只九category→原NULL且保新列/check，不drop。PG18完整custom备份/xb独占/fsync/hash/离线解码与隔离恢复边界、应用先后/实际Flyway禁用核定已写；当前未找到PG18 dump/restore/psql、实际JVM/停写窗未核、可写入口尚未实现并独审，均明确阻断实际执行。A6633195静态契约检查通过，47个bigint逐字精确、9映射唯一、只category DML、无commit/history指令、保护14不变；没有SQL parser/PG运行验证，初次简单关键字检查因历史code为CREATE/DELETE误报已修检查，不是SQL执行失败。

三文件已冻结交technical_designer独审及总控，等待设计意见。不再连库、不实际dump/DDL/DML/恢复/服务/Redis/HTTP/Git；原单初始化manifest与生产源保持冻结，C3skip/角色样本保留。

总控随后将同项独审转交QA，解除等待旧designer。按用户最新要求聚焦核心CRUD/查询、认证/资源数据权限、隔离、必要DB适配和关键联动，取消主题/配色/像素布局动效等非功能补验，不删现有主题测试源，不重复已绿项；C未落地不能称整体完成。QA对EED995提案完整审阅后通过，随后将本人发现的完整约束漂移问题合并为唯一mustfix，总控批准与必要专用执行载体同一次SOURCE收口，实际操作仍不批。

V2现已一次完整交QA增量源审：proposal-manifest-v2.json SHAE486A48CE897CC2D3F4C8DC30EFC1A4344F1861390DA43E816C3286513A7258E；docs/legion/design/sql/2026-10-31-dev-category-execution-seal.json SHA10ABB8BA0F89012F51D3AFB748816D930F94A66438C2DCCE33EDBDFD3C8B388E。载体dev_category_adaptation.py C302D096/MD D6A17F21/forward D4A6AA2B/rollback96D171BD，13必要源/事实/config hash及14保护，旧EED995与v1三原文hash保留、F604不变。forward锁内比fresh6完整约束JSONB，真实7项后像入receipt；rollback仅从已校验committed/prepared/backup链取完整catalog，锁内比再九NULL，保结构，不猜表达式、不手填SHA证明来源。

入口默认plan无连接/网络/写目录，只四专用phase。backup需明确phase批准+窗口/runtime/全相关停写交接+外部sealSHA+已核官方PG18客户端凭证；新私有ACL目录、空间、xb archive/fsync、退出/警告、TOC全解码后才成功receipt。apply同窗目标/source/backup archive字节SHA精确、单SERIALIZABLE持锁冻结SQL、九后像/7约束与prepared/commit-intent独占fsync后commit；UNKNOWN不主动rollback或重试，confirmed后凭证失败保confirmed。rollback需另批ID和已提交receipt链，只九NULL保结构。签名/官方archivechecksum与文件hash/version18复核在获实际下载/备份批准后才运行，source不下载、不启服务、不改PATH。MD将官方便携工具获取与backup/停写条件下适配合为一次实际确认，不单开工具审批循环。

本次只AST/defaultplan及必要purefake/静态契约：未批准拒外部动作、完整约束漂移拒、提交前失败rollback、UNKNOWN不rollback不假committed、成功持久receipt、driver秘密异常不输出、47精确bigint/9映射/6约束JSON均通过；无SQL parser/真实PG/实际客户端或SQL执行。无实际下载、备份、DBwrite/历史/服务/恢复/HTTP/Redis/Git动作；不重66/临时PG/视觉，不再新增验证框架。源锁后等QA对本完整组一次结论，具体必须问题才最小修；实际运行绑定/停写与工具来源由总控在下一次用户合并实际操作确认中核实，不能虚报。

QA随后已一次完整源审V2/340行专用入口/凭证链通过，唯一完整约束mustfix已关闭，无新增mustfix。独立报告 `.gradle/dev-category-adaptation-20261031/qa-v2-independent-source-review.json` SHA A23BC42329ACEDDFB5309CCB3388F8BC5E6775B73B4F511C061B4CE93EC198E5；manifest E486A48C/seal10ABB8BA/runnerC302D096、13源/14保护/3份V1/2RO材料/HEAD064 cornerstone匹配，QA独立静态及纯fake exit0、diff --check通过。本地冻结材料不重写pending字样，由独立报告绑定终态SOURCE PASS；不是数据库适配或整体EVA通过。

后端准备工作交总控一次实际操作确认：官方便携PG18获取与来源/签名或checksum/文件hash、backup私有目录/空间及真实可读archive凭证、真实JVM/profile/datasource/Flyway与所有相关writer/migrator从backup至RO后验持续停写，全部具足后单事务category列/check+9map，再一次必要已批RO后验。flags仅操作方交接，不替代实际事实；UNKNOWN不得重试/假回滚、rollback仍另批。本人不再源改/测试或增设门槛；当前没有下载、dump、写库、服务、历史、HTTP/Redis/Git动作。现库CATEGORY仍缺失、C未落地，未称all done。

### 2026-11-01 原remote dev：窄前像备用SOURCE待范围批准与QA

用户要求不下载工具直接连接，目标经总控明确仍原已审remote dev eva、非prod，并确认所有相关写入已停止。总控另就“仅本次结构及九分类前像、非pg_dump全库备份”请求实际批准，尚未转达用户同意；当前仅授权备用SOURCE，未连接或写库。旧C302/10ABB/E486保持不变，不伪造BACKUP_VALIDATED、不取消dev/prod目标门禁。

新专用dev_category_scoped_preimage.py A95ADE2702C3FBE265C20D1FC83DA695BCF13F8C52727278B271752DAB1E156C及说明8F95F17D已交QA增量审，scope seal EC88EE57E04F9AC0F2F14B0E2E1074B058484D75816B1C02916A103016BB55DA。显式SCOPED_PREIMAGE_ONLY_NOT_FULL_BACKUP；默认plan无连接；实际scope批准、停写/runtime交接、新seal具足才允许phase。复用已封旧函数/源保护和完整forward/rollback SQL，仅三处固定scope token/GUC/error替换并验精确次数，不调用旧backup/mutate或伪备份链。持锁重核47技术/六约束/列缺失，九原NULL与结构前像先xb/fsync/hash，再同原事务新增字段/check+九映射；七约束/九后像持久prepared后commit，UNKNOWN不主动rollback或重试。另批rollback只九NULL保结构，不声称全库或完整原schema恢复。AST、默认plan、缺批准拒绝、两SQL派生形状纯本地检查通过；无真实SQL/PG/下载/dump/服务/Git，不重绿套件或视觉。必须用户明确本scope批准且QA通过，才可由总控派一次实际执行。

用户随后明确批准窄scope实际执行，相关停写确认持续有效。QA A95/EC88源审通过后，第一次调用在private-output-acl、connect之前失败，NOT_ATTEMPTED/无DB连接或事务；旧目录20261031T223147Z-ba489c14保留。根因纯本地定位为powershell所选runtime Security模块类型文件AuthorizationManager拒绝；系统WindowsPowerShell绝对路径+仅子进程内置Modules可加载。获总控授权只改新scope目录函数，旧C302/10ABB/E486和A95副本/EC88均保留，未改业务SQL/执行策略/全局环境。新目录自检172990ca实际读回protected/exact本人SYSTEMAdmin三Allow/FullControl双继承/零deny及额外SID通过，QA另只读独核通过；runner3C06657F/新seal-v2 AC07CAF0增量获QA明确通过，按原授权仅再一次新独占RUN。

新RUN `.gradle/dev-category-scoped-runs/20261031T223709Z-957662f0` 单SERIALIZABLE事务成功，commitOutcome CONFIRMED、closedtrue、rolledBackfalse、exit0；committed.json SHA7CCB5426E83034C13769FA0D54C4860E6533D6245C3D27F5D891333173E0D80C。执行前再次9016监听/WebBooter JVM皆false；source/build Flyway关闭、所有seal/14保护匹配，外部writer是用户确认非自动探测。进程生命周期包络北京时间11月1日06:37:08.974254→06:37:10.448698，覆盖连接开闭，未采服务器精确会话起止，不冒报。47/六约束/缺列和技术摘要锁内核通过，九原NULL/结构前像先独占fsync及readback；新增category/check+8SYSTEM一QA BUSINESS、38历史NULL/原技术摘要/完整七约束通过后持久prepared/intent再COMMIT确认，所有凭证在该RUN。

写连接已关闭、独立RO窗口已交QA和总控，后端不再DB连接或写入，独立后验尚待。scope仍仅本次结构和九分类前像、不是全库备份；无下载/pgdump/Flyway/history/角色样本/其他数据/服务/HTTP/Git操作，不重跑已绿项，未称整体EVA完成。

QA独立唯一RO后验已通过并交锁：`.gradle/dev-category-adaptation-ro-20261031T223736Z-6e21be32/result.json` SHA FFED0AC622482DC54BE1B20CCF49381A004DCD09C9F01A859FA2E82FA4C8DEBC，北京时间06:37:37.249679→37.540115，1connect/27SELECT/始末eva-eva/180003/双ROon/repeatable/rollbackclose。实际varchar16 nullable/defaultNULL、完整七约束逐字段等prepared/原六不变/checkvalid；47技术前像全同、九精确8SYSTEM+QA BUSINESS、38历史NULL，itemCounts/原preservationSummaries及授权技术摘要、无相关trigger/默认history目录空/14保护均原值。7CCB committed/prepared/preimage/intent/result链逐SHA/数据归属独核通过。独立审计65E2F518EAF764ADBC5FCC2E1E0C0E8E136CE063079E34FE76D8C9BBBD853CBB同RO目录。本次DB分类缺口收口，后端不再连接/迁移/清缓存/重跑，交总控后续核心功能安排。

验收边界为已批技术字段/计数/摘要，不假称全部非category业务字段逐字节比对或全库备份；fullBackupMadefalse。没有常规build、服务重启、HTTP或UI启动验收结果，C仍未实现、EVA未all done；若总控另安排后续build/启动，须保持正确dev绑定和真实Flywaydisabled，不运行现库单V1或history修复，并遵守服务授权。

### 2026-11-01 字典B真实UI自有样本清理前：一次限定RO

总控明确授权一次必要dev RO，仅主2116663333998792706/code EVA_QA_B_20261101_01及所属两明细/同code计数、受限FK与树引用，不写、不查真实业务日志。一次连接已RO双on/repeatable/rollbackclose，结果.gradle/dict-b-ui-owned-ro-20261031T225125Z-6b703db6/result.json SHAC0C3ED6FE522304EB7260514A83B4E3C5DD192BFD4394F84C075097F22BD5035。主BUSINESS/删冻0，已知最终name/remark仅布尔匹配均true；同code total1/active1。两item技术ID2116663334133010434/QA_B01与2116663334263033858/QA_B02，main_id相同/删冻0，tenant_id实为NULL，不猜0，未读取d_value或PII。

主/子eva普通表，入向FK catalog为空、该主pid子节点0，main_id明细完整数量恰2；枚举技术关系无外部引用，不声称排除所有潜在字符串业务引用。代码delDict先明细再主逻辑删除已读；仅由QA正常UI清理本轮对象，后端不DML。源码DICT_CODE_EXISTS定义0x0000-10001，但不是当次live重复请求的实测业务码；本次只证明检查时无第二条及主最终值未覆盖，不补发保存、不广查日志。准确ID/边界已交QA与总控，DB窗口已交回，无后续连接或代码/测试/HMR/服务/Git动作。

### 2026-11-01 B明细删除保存真实失败：PG兼容窄修待QA

QA仅一次QA_B01删除草稿保存未持久，已放弃自有草稿并重载两明细、停写未主删。按总控先前失败条件预授窄RO核三ID仍删冻0/同码physical1 active1/8SYSTEM+D01保持；结果fc4f82ce SHA8F547CDA，1connect双ROon rollbackclose，不当清理成功。此查询与root新定位消息交叉，随后不重复状态RO。根因从edit→diffSaveItems→dictItemMapper.deleteById实读，未发现后续逻辑函数改写；GitNexus旧行号只作入口未重索引。

按root正式进一步授权，真实当前MP/DictItemMapper生成SQL为deleted=UNIX_TIMESTAMP()，纯本地probe无连接；一次极窄dev函数RO（不重查三ID）在06:57:59.303244→.335961，PG180003/eva/search_patheva/default和txnROon/repeatable/rollbackclose，当前/eva/pg_catalog unix_timestamp()皆NULL且可信候选空，function-ro.json SHA9219AD78。兼容缺陷可复现，但本次UI请求原异常码未采，不假称捕获。配置日志路径文件不存在未继续广查。profile scheme实际为dev PostgreSQL/prod和aliyun MySQL，已纠正最初在结果返回前误写三PG的消息，未误改两个MySQL配置，也不再扩大输出配置原文。

唯一生产补丁application-dev.yaml增加CAST(FLOOR(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)) AS bigint)逻辑删除覆盖；main/prod/aliyun原hash保持。新增DictLogicalDeletePostgresTest单方法用真实Spring YAML/Binder绑定四配置、真实Mapper及一合成PG：旧路径实际42883，新dev删除值>0/物理3行仍保留/其他2行0/重复删返回0，三个MySQL仍UNIX_TIMESTAMP。RUN .gradle/dict-logical-delete-pg-20261031T230318Z-3c525eba exit0/pass/inputsUnchanged；同一@Test通过main和Java source-file模式用已核缓存classpath/native执行，不称普通Gradlebuild或JUnit XML，原launcher参数长度206在JVM/PG前失败，标准Java argfile解决。可选API Guardian Status.STABLE编译警告已记录；配置日志OFF/输出脱敏，无凭据传播。没有重66/18/视觉或新进程审计框架。

冻结manifest .gradle/dict-delete-20261101/manifest.json SHA EA454C14792186350D0E7262E1924A005B6686F2FA9262B014839216E9E8F6AF，dev D6920CD1/test A05B26C1，源/产物/根因及三个MySQL保护hash已交QA一次独审+必要定向复验，唯一重槽交QA。service/9999/SYSTEM/权限/API/DB结构和实际样本未改，后端未实际DML或启动服务。加载新dev属性需用户正常build/restart，QA通过后仅补这次删除/级联核心复验，不重复已绿项；当前主及两item仍保留、C未落地，不称整体完成。

QA窄修已完整源审+一次独立同一test合成PG复验通过，无mustfix。独立RUN.gradle/qa-dict-logical-delete-20261031T230648Z-e5b19913，result SHAECCFAFB4F3CC5B3A2BA76B2747866BDFE2F71FBC3CCD74EA68993015483E7FC3/log D5F152C439D9CCD8C1877007806ECA846C1704CA94D5A1CDD149D9E002695BF5，输入无漂移/旧42883与新软删保留/范围/幂等断言通过。QA核新增dev覆盖块移除后字节恰恢复A4E18124，三个MySQL原hash不变；源码EA454C14/两源/根因产物精确绑定。QA曾在JVM前因错误假设build资源旧值而断言停，实际build dev已D692，纠正后只一次真正PG运行，不称产品失败或新增功能测试。

未完成边界：非完整Gradlebuild/JUnit XML/实际服务HTTP或UI删除复验；实际build资源新值不证明当前JVM配置已重载。后端源及测试现冻结、重槽空闲，不自行服务启停或真实DML。已交总控一次通知用户正常dev重启，确认后QA只续原QA_B01删除保存回读和本轮主级联，不重新建/编辑样本、不动D01/8sys，不重复已绿项；原UI异常码未捕获如实保留。

### 2026-11-01 用户重启后菜单/分类能力前置异常：非侵入运行版本取证

QA重启后尚未删除/保存，页面仅黄warning/空根且菜单缩减，身份header不能证明认证初始化。总控授权非侵入runtime只读，不查DB/HTTP/token或完整cmd/env、不attach/改IDE/重启。实际9016 PID32400，启动UTC2026-10-31T23:12:55.5274990Z（北京07:12:55），Java25/org.pkaq.WebBooter/1个argsfile；白名单解析classpath选定模块均本checkout的build/classes与resources，并非已发现旧本地Maven模块jar。证据.gradle/runtime-capability-20261101/runtime-technical.json。DictCtrl2B4C3AED/DictBusinessAccess68B5F31B/DictListResponse79FA1340磁盘SHA精确等已验B66产物；AuthBootstrapResponse14768D06及SuccessHandlerB24153B8等相关类均存在，所列七类mtime早于processstart。磁盘及classpath不能证明已加载类，已明确loadedClassProvenanceVerified=false。

build主配置默认dev、build dev存在新epoch覆盖/D692；CLI未检出显式activeprofile或configlocation，不证明环境/外部配置未知覆盖不存在。只读前端精确条件已与QA对齐：boolfalse或空array+bool仍满足契约；当前黄warning也可因fetchMenus/resourcePermissionEnabled未知导致GET读权拒绝、query未发出而出现。无当前真实响应元信息，不猜旧后端、失效token或配置回归，不改源/HMR迎合。已交总控最少安全取证字段：fetchMenus是否发出/HTTPstatus/success-code/resourcePermissionEnabled存在类型值/menus-resources计数，以及dictlist是否发出/HTTPstatus/success-code/businessWritable存在类型值/dataarray-length/分类与readOnly类型完整性bool；不要完整响应/PII/token。源/真实样本不动，未连接DB/调用HTTP/启动服务/重测或Git操作，待总控恢复正常认证能力前置后仅续原删除。

### 2026-11-01 B核心删除持久化与自有样本清理闭环

用户本地curl新认证三接口契约由总控核正常后，QA仅一次正式浏览器reload恢复正常能力，随后真实UI原样本QA_B01行删除确认/主保存一次/服务端回读仅QA_B02，再本轮主删除确认一次，搜索仅两虚根无匹配，未重新新增编辑或重测唯一性/视觉。此为QA真实Edge UI结果，未借curl当UI通过；操作07:37:08之后，随后handoff无dirty/在途请求。

按总控原预授最后一次限定RO已完成：.gradle/dict-b-ui-final-ro-20261031T233911Z-c888813f/result.json SHA667A0D4D6F6FCC85C54E52A389EA4514793BC10D79F39E29533C9EADE53D9479，北京时间07:39:12.844607→.901845，1connect/eva-eva/双ROon/repeatable/rollbackclose/写0。主2116663333998792706 BUSINESS/frozen0/deleted1793489885；item2116663334133010434 QA_B01 deleted1793489851、item2116663334263033858 QA_B02 deleted1793489885，main_id皆正确且物理仍存在。该code physical1/active0，原active SYSTEM8=8/D01 BUSINESS=1不变，status FINAL_OWNED_SAMPLE_LOGICAL_CLEANUP_VERIFIED。恰与先单明细保存后主级联顺序一致，与06:54失败三行active证据分开保留。

B本轮核心CRUD/逻辑删除与自有样本清理已由真实UI加限定持久化后验闭环，DB窗口交回，无后端清理DML/缓存/HTTP/服务/Git操作，不再查询此码或重跑PG/已绿项/全元数据。原live重复尝试的特定错误码未捕获仍不冒称通过；C未落地、其它EVA整体范围不由本项宣告完成。源码继续冻结，等待总控其它核心范围安排。

### 2026-11-01 C原批准范围：当前源码只读影响报告

总控正式进入原批准C，但当前仅status/design可写，业务源/HMR/单V1/模板/真实DB/服务/HTTP/cache/Git冻结，不重开B或主题。只读核原minimal-contract及当前服务/mapper/cache/serializer/router/resolver/模板/provisioner/租户管理员和有效资源源，GitNexus只作调用入口（旧索引不重建），未跑测试或连接数据库。

影响报告docs/legion/design/2026-11-01-dictionary-c-source-impact-backend.md SHA F096649343DC10BCA3101DA83010BA9B656DA598228A54C1DC92602C74215447已交总控/QA。当前私有单V1已有dict/item/CATEGORY/check/唯一索引，无core种子且账本1，旧文V1/V2后再增版本描述不沿用；C建议不改初始化/账本，不自动升级或扫描真实tenant，入口另核字典结构/来源。必解私有表无tenant_id但StdEntity默认Mapper映射含该列，建议现两Mapper做schema专用明确列读写，保持审计/乐观锁/个人维护主权限、不改全局StdEntity或补DB列；若转为补列必须先报新DB契约。实际私有维护角色TENANT_ADMIN，standalone仍ROLE_ADMIN，私有SQL不能照抄tenant_id=0；真实资格/资源上限不依赖rolesMap/ALL，平台0/C3另案不扩大。

最小拟动8现有生产文件为DictService/BusinessAccess/ItemSnapshot/DictMapper/ItemMapper/CacheHelper/DictInit/JacksonCodeSerializer；HTTP/IDictService签名/Long ID/get可选category/code map/B能力契约保持，AuthCtrl现委托可不改，不新增通用supporter。显式coreSYSTEM/私有BUSINESS、缺表拒回落/同ID消歧/同code冲突拒；cache改显式owner v2，旧键不读不全清、前缀限定scan/evict、完整code清单避免部分缓存误全量，afterCommit捕owner，新旧code仅本owner刷新；Serializer统一可信主体service只读proxy，无主体原码；启动无owner跳过。init(Map)/reload(Map)等不得任意Map污染共享缓存。共享字典专用全量语义与个人管理权限分开受限，不全局Ignore/ALL。后续只C隔离/事务/资格/serializer/bootstrap与私有真实Mapper的必要合成验证，未将这些计划当完成。

原C总体政策已批不重问；已向root说明优先不新增API/DB契约的现有内部适配路线。当前只报告实际影响和源写边界，未生成业务patch；等总控/QA收口后按明确范围实施，不自行破坏F604或变更存量结构。

### 2026-11-01 C源码已实施冻结，转独立QA

总控随后正式授予8现有生产文件及必要就近tests的C source锁和唯一重测试槽；已完成无新增API/DB契约的内部适配。来源严格为core SYSTEM/当前私有BUSINESS或standalone唯一schema，物理结构/真实主体/分类不就绪拒绝，私有Mapper显式列不含tenant_id、schema限定避免同ID一级缓存混源，保原数据scope/9999整批保护/审计逻辑删除与revision。实际ROLE_ADMIN/TENANT_ADMIN资格及启用时core资源上限不依赖rolesMap/ALL；平台0/C3未扩权。v2 owner缓存、完整清单与冲突先于命中、afterCommit仅captured owner/新旧code失效，外层读写事务完全绕cache，Serializer/启动/Map初始化旁路收口。QA两mustfix（nonnull客户端revision前置比对、dirtycache回滚）已源内修复。

最终8prod+5tests清单.gradle/dict-c-20261031T235530Z/source-freeze-v2.json SHA C21751F68472842485CB2950441B6996416E7F5D05D2B50BA18018415C258FF3；Java25缓存CP独立输出最终javac成功，不等于Gradle全构建；4旧内部夹具仅签名/审计/来源适配并编译，未重B66/旧18/主题。1个新增JUnit方法经main调用，test-6/result.json SHA 3DD1E8F915E928226B5094A1DBD91F822F8FBE41F8D7212066A6C2BE3AF3E972、log SHA F5E6EA1E2D578E3DEF24D0B5C4AB7C13C86952F38F2B269F01C62EB5C57D0232，08:07:47→08:08:22+08 单合成PG14.22六组C断言PASS，含真实私有无tenant_id CRUD、同主明细ID两tenant隔离、缓存部分预热/冲突/legacy、普通SELF管理与完整标签、维护/资源/SYSTEM/9999、stalebo、冷暖dirtyread回滚、callback外层context保持、缺结构拒回落、Serializer/fetch及standalone/platform0。随后仅格式整理再编译；精确最终冻结源码runtime仍待QA，不冒称其已验。前4失败及首过test-5均保留，详见docs/legion/design/2026-11-01-dictionary-c-source-freeze-backend.md。

14保护文件+V1/私模板/provisioner+devdeletefix配置/test哈希全true，代码UTF8LF/max120/限定diffcheck通过。所有自有PG自然关闭，最后IDs限定核无存活；无实际devDB/HTTP/cache清理/服务/HMR/build/Git发布。源写及后端测试停止，重槽已交QA并同时通知root；仅等待具体mustfix或最终独验结果，不等待旧designer、不重问C总体批准，不把本项当C3或EVA整体完成。

### 2026-11-01 08:15 C精确冻结独验闭环

QA独立源审及一次exact冻结运行通过，无剩余mustfix；原revision/dirtycache两项已由源码及stale/warm/cold真实事务断言关闭。绑定sourcefreeze C21751F68472842485CB2950441B6996416E7F5D05D2B50BA18018415C258FF3，QA RUN .gradle/qa-dict-c-20261101T001442Z-d642216d，独立22源javac exit0，单@Test main+独立临时PG14.22，08:14:42.325606→08:15:19.711186+08 exit0/六阶段PASS，13源码+19保护始末不漂移。result SHA CBA4D39016E60C95A317BE0FCBB190778C59F02DBF1CAC286DE1C82B87E41243；compilelog AEF4D3B25D4980682CE1D8CEFCF7F8AAFE28CAC2B41D14F50C42BB47502697E1；runlog B20386226A5C1D20D98E9B0FF52F5257003122AA83DDC13ADAC44C91A716DB1D。后端仅只读核result和SHA一致，不再跑测试或连接DB；设计交接报告已补最终结果。

边界保持cachedCP/一个@Test非Gradle全套或JUnitXML，旧4fixtures仅compiled、内存cache/mockRedis、bootstrap委托源码+service合成，不冒真实HTTP/dev/Redis通过。用户正常构建/重启加载exact候选未执行；真实租户结构核验联调需另获明确授权，缺结构拒不自动升级/迁移/改账本/清Redis。源码及重槽归还root，B不重开，C3/平台0认证/账户角色最终清理另案，整体不宣告all done。

### 2026-11-01 C正常dev重启后的缓存定位，只读源码收口

总控已转达用户完成dev正常构建重启且Flyway关闭；此为用户报告，不由后端重复操作服务。后端只读核application.yaml/application-dev.yaml/config/eva/eva.yaml/eva-dev.yaml四对source/build文件SHA相等；源码配置eva.mode=standalone、tenant.enable=false、standalone-schema=eva，CommonConstant.CACHE_DICTDATA=dictdata。Helper逻辑键分别为v2::eva::SYSTEM::FROZEN_STATUS及v2::eva::BUSINESS::EVA_QA_20261018_D01；当前RedisConfiguration使用默认cache前缀和String键序列化、无自定义prefix，推导物理键为dictdata::v2::eva::SYSTEM::FROZEN_STATUS及dictdata::v2::eva::BUSINESS::EVA_QA_20261018_D01。dictdata配置ttl=0s，预期不设过期；以上是confirmed-source推导，未排除外部覆盖、未声称live键存在/类型/TTL或实际已加载Bean已核。

缓存触发明确：GET /sys/dictionary/query/{code}及validate通过queryDict读取指定字典；GET /auth/fetchDicts经同一service按数据库完整清单逐码命中/填缺键。前端正常初始化先验证用户再调用/api/auth/fetchDicts，未成功验证或未发生初始化不能推断已触发。上述均须可信有效主体、实际字典来源/结构/分类成立；冻结字典不进入标签结果，同code冲突先拒。实际只读事务才读写共享缓存；若加入实际外层读写事务，则直接同源读DB且完全绕过cache。Serializer有可信主体且非空注解/值时走service代理query，无主体保原码。服务启动DictInit无主体时跳过，因此重启本身不会自动预热。

管理list/get仅走原scope的Mapper，不走标签缓存；当前字典页重试/列表刷新只refetch管理list，不能当缓存刷新或冷读验证。前端refreshDictState在成功业务变更后另调用initialState.fetchDict，但本次未执行任何写入，也不为暖缓存新增写操作。现DictCtrl无专用cache refresh/reload/clear端点；service内部init/reload不是UI/API入口，Map入参不作为共享值，按DB权威值读取。正常query/fetch是标签读与缺键填充，不会强制刷新已命中的值；写后仅afterCommit按捕获BUSINESS owner/new-old code失效，不能改用全clear或旧键旁路。

可用工具检查：PATH未发现redis-cli/redis-cli.exe，当前Python缺redis模块，按总控约束未安装、未造Java/socket/helper，未读取连接秘密；Redis连接0、EXISTS/TYPE/TTL命令0，真实Redis验证未覆盖。缺键即便出现也可能仅因当前管理读取未触标签缓存，不能判失败。本项不新增验收阻塞或宣告all done。

QA报告Edge因另一扩展UI打开被官方getAXState阻挡，未读取header/draft、未reload、未执行SYS/D01查询或写入，不能冒称本次正常runtime UI通过。由总控等用户完成或关闭该扩展界面后明确继续正常读取现有SYS/D01。后端本次仅源码/四配置哈希读取与本状态追加，无DB/HTTP/源代码/服务/Git操作，至此停止；真实跨tenant未建、权限scope等剩余项仍按原清单另行推进，不隐含放弃或新增门槛。

### 2026-11-01 C最小standalone真实UI只读验收通过

QA报告用户解除Edge扩展阻挡后，使用fresh官方Edge1当前可信localhost8000/#/sys/dictionary，核管理员/空选择无草稿，仅正常reload一次。实际显示系统菜单、2固定根、8SYSTEM及原D01 BUSINESS共9有效主，已软删EVA_QA_B_20261101_01未出现，无契约warning/错误。FROZEN_STATUS加载后主字段及0正常/1已锁定/9999只读、sort1/2/3符合，明细仅查看，保存/新增明细disabled，系统行无删除入口。原D01加载后code/name/remark及原QA01正常，管理员编辑/保存/新增明细/业务删除入口可见，但未修改或执行写入。再次读取FROZEN_STATUS内容及只读状态一致，停系统只读页，无dirty并已markHandoff，浏览器锁归还总控。

此为QA实际正常UI范围通过，后端未重新操作浏览器或运行查询。管理list/get不触标签cache，重复读取只证明UI一致，不证明Redis冷/热命中；页面无专用定向强刷入口，未调用内部reload。正常reload可能触bootstrap，但未逐字段抓取其HTTP响应，不冒fetchDicts完整HTTP契约已核。真实跨tenant、live Redis及原剩余权限scope仍未覆盖，不扩为C整体或真实多租户通过。本轮QA无字典新增编辑删除、fixture、权限修改、DB、服务、源码、Git或主题动作，后端仅本状态追加并同步总控，不重测已通过项。

### 2026-11-01 核心权限负向与S收尾操作包，仅设计待一次批准

总控将已完成C最小运行范围关闭后，派只读源审Organization BO/edit、resource matcher、permVer、用户/角色/模块删除及最新S/历史CRUD矩阵；已完成docs/legion/design/2026-11-01-core-negative-and-s-closeout-execution-package.md。本轮无fresh DB/HTTP/浏览器/权限/禁用/删除/服务/源改/编译/新测试/Git动作，未把本包当执行授权。

主线精确为现LIMIT CUSTOM+ALL无POST时有效P edit→00024；SELF仅R加既有E=1012、LIMIT完整roles替换SELF（PEER原SELF保持）后fresh T1同P edit→02001；同T1一次L GET存活控制，暂停待LIMIT roles→[]，旧T1再GET预期00023，记录旧权限不能沿用而非token必销毁。之后才PEER roles→[]、四有资源S角色资源清空/TREE空不保存、五角色和两账号禁用保留及两次fresh登录拒绝。未恢复C3/部门移动、新样本或重scope正向。

一次批准包还独立列B账号PEER备注往返（无专用PATCH，profile三字段ALWAYS同值写、头像非空有文件副作用，条件不足不硬save）；C原S保留的三个逻辑删例外仅DEPT/L/LIMIT，主实体/profile物理保留；D旧M01/M02独立解锁/父带子拒删/子→父逻辑删例外，资源无任何角色引用前置，不造新授权。R01/O01/四部门/P作者样本/PEER主行不删。role/user delete源码级联解除关系，不存在普通成员必拒守卫；module只有明确子节点guard，资源引用先删refs再逻辑删定义，不能把异常catch文案当有引用必拒。实体物理保留与正常refs物理移除口径遵总控最新纠正。

有效P body仅绑定一次获准fresh前像的公开name/code/pid/revision/frozen/sort与固定QA备注；意外放行须用户明确批准恢复P remark+revision+modify审计的逐列postimage条件单行回退，不靠管理员再edit冒精确恢复；临时授权逆差分和permVer不回退亦列明。旧T1密码/token仅本地getpass内存、固定回环/路径/主体、暂停时不心跳、不换新会话，旧只读脚本不被改白名单。包已列统一freshRO的两账号/五角色/两样本/四部门/4资源及可选两模块明确字段与完整refs，当前未知字面值不臆填。等待root整理成一次用户选择批准，不新连DB或先行改权限。

### 2026-11-01 09:59 一次统一fresh RO完成，执行包收敛

总控另行正式授权一次dev限定RO后已执行，RUN .gradle/core-closeout-ro-20261101T015928Z-fd0f3334/result.json SHA D18132104F37909744CD1C2ED4811E7834EB56FFDADB97BF2BD5A26EC67751EF。09:59:30.184344→09:59:30.435294+08，eva/eva、singleconn/repeatable、transaction/default RO起止on/on，22限定SELECT/writes0/rollbackclose=true。四配置源build及批准dev指纹核同源且区分prod；C八生产源码仍冻结。受限目录已核仅当前SID/SYSTEM/Admin3主体ACL，原profile name/code/dept本机私存但姓名未输出；未读密码hash/contacts/头像值/日志正文。proposed-private-payloads.json SHA F63DB2A672BABCF732EE39FF508CE4D3D194BED7290056804AC41D03F64A25A2仅候选body，不授权执行。

fresh确认LIMIT revision0/permVer5/frozen0/deleted0、仅CUSTOM+ALL；PEER revision0/permVer6/frozen0/deleted0、仅SELF，两头像空/profile name非空/dept D1/posts[]。五S角色scope及CUSTOM单D2原串不变，完整members3/resourceRefs8，无其它成员。R01先历史矩阵第34行精确定位2111725377803354113，再实核ROLE_EVA_QA_20261018_R01，资源/members/派生均[]，保留不写。L/P两组织根、revisionNULL/frozen0/sort1，P CREATE_ID=PEER/MODIFY_ID=NULL，L两作者字段LIMIT，均无子/无account引用；固定有效P body公开name/code已绑定入包，不再臆造。

M01=2111793871655895042与M02=2111794099880558593实际都frozen1/deleted0，树只这两节点、M01根/M02唯一子。唯一资源2111793873144872961 MAIN_ID=M01 GET /sys/module/list有效，M02资源空。全部精确角色引用[]，不含R01或真实admin，无需任何新grant或解除module refs。所涉十表相关FK仅profile→account，用户trigger[]；不能把模块catch文案当资源引用必拒。若用户选旧模块例外，必须明确M01目标解锁会级联M02一起1→0，再父带子拒删一次、子→父及自有定义逻辑删。D1实际2账号引用且有D1C，四部门不动不删、不测试错位的账号引用守卫。

最终操作包已按本次前像重写，A负向/旧T1/撤权禁用、B有条件PEER备注及profile同值重写、C DEPT/L/LIMIT三个逻辑删保留例外、D旧模块明确例外一次给root，不分散新窗口。本地只读javap核既有依赖默认NOT_NULL，源码/配置无覆盖；profile空值/头像/字段条件落地，实际固定payload通道仍须批准且不得用普通UI附带未知字段硬save。旧T1只按源码版本/资源结果记录原权限不可沿用，不称JWT必毁。

最高覆盖修正：上一节草案的自动业务/审计/revision恢复及前像SQL建议已被root明确撤回，绝非已批准动作。有效P POST若意外允许、未知响应或持久值异常，立即停所有后续操作，保留样本/日志/当前授权并报root；不自动恢复任何业务/作者/审计/revision，不自行回宽角色。后续最小业务回退必须用户另批。该规则已写入最终包，当前未发任何HTTP、grant/冻结/逻辑删/回退，未改源/服务/缓存/编译测试/Git，RO连接关闭后停止，等root统一用户批准实际动作。

### 2026-11-01 A已批准后的resource本地probe候选交QA

root转达用户call_f2915e00756746dd9e11c480ddf66d71明确批准完整A/B/C/D后，仅按本阶段派发制作.gradle/org-edit-resource-denial-20261101.py，SHA BC09B49FD572D80C03BD34A2D7987C37908A856EF80B3BFA04A49CD0E745D8F8。固定pure依赖旧stage1 D005A1E898CD5B900FB75E3149F78D20F1E99BCC2E7529424C79F5E2F95335AE，未修改旧工具白名单/全局；新transport沿冻结回环实现复制，仅ORDER3=LIMIT login/fetchMenus/唯一有效P POST。body与封存公开字段匹配，只有HTTP200/原生false/完整00024/空data算资源拒绝。默认PLAN无密码网络；--run仅用户本机TTY getpass内存，固定localhost9016/actor/路由、总90秒/连接3秒/请求wall5秒/2MiB，无proxy/redirect/retry/Cookie fallback/refresh。意外成功/未知/超时停止，不自动恢复、无DB或授权动作。

开发侧仅默认PLAN和7个新fake case通过，realHttpRequests0/passwordInputfalse，未真实--run。已直接交QA独立source/fake审并抄root，待QA通过后仅root给用户真实命令。SELF/T1暂停和disabled阶段尚未实现，不冒全A完成；不因B/D浏览器阻挡延迟该候选。

QA另报B正常UI包含tel/email/roles/avatar且无revision，按包已停零保存；D官方Edge另扩展UI阻挡，零解锁删除，root等用户解除。本后端未碰浏览器/当前SELF授权/PEER/模块基线。B附加仅源核时发现新成立条件：私有已封存PEER原remark确为NULL，StdEntity.remark无ALWAYS、既有DbConfig默认NOT_NULL，saveUser用updateById。故即使本地fixed payload安全省略其它字段，先写非空remark后再以fresh revision传null会被忽略，不能恢复原NULL；空字符串也不是等价恢复。当前没有公开remark-clear端点。未为B改API/PATCH/字段策略或执行SQL/HTTP，已报root该方案在现约束下不能安全闭环，不实施会留下非空备注的两次保存。

### 2026-11-01 A resource probe独立源审/fake通过，待用户本机阶段执行

QA独核候选BC09B49FD572D80C03BD34A2D7987C37908A856EF80B3BFA04A49CD0E745D8F8及pure依赖D005A1E898CD5B900FB75E3149F78D20F1E99BCC2E7529424C79F5E2F95335AE未变，完整安全源审无mustfix；独立默认PLAN exit0/NOT_RUN/max3/network0/passwordfalse，独立self-test exit0/FAKE_PASS7/network0/passwordfalse。固定有效P body/唯一POST/严格00024及受限传输/秘密输出/失败停止均通过本阶段审查。QA未真实--run/索密码/HTTP/DB，不能当live拒绝已证。

此SHA resource-only候选可由root按既授阶段交用户本机执行，不需再QA_ACK。SELF/T1暂停/旧会话/disabled阶段尚未交审，不继承该通过；B资料与D浏览器阻挡均仍零写，不借此改变A授权前像。后端仅状态追加并回root，不再重跑fake或接管凭据/真实请求。

### 2026-11-01 用户resource真实通过，后续SELF/T1与disabled仅离线候选

root转达用户本人真实resource-denial-post-v1结果success=true/RESOURCE_POST_DENIAL_MATCHED/requests3，POST_EDIT_P HTTP200/原生false/完整0x000-00024/automaticRecoveryfalse，本阶段通过不重跑；不是QA或后端代用户HTTP，不扩大成SELF或数据编辑拒绝。QA随后官方库存只见Edge登录页，未保存SELF增加E/LIMIT仅SELF两笔；需用户正常本地admin登录后继续原已批授权，不重问授权、不借CLI会话代Edge有效登录。B仍NULL恢复限制/D仍零写，不改变A基线。

按root无依赖派发仅新增.gradle/org-self-session-disabled-20261101.py，最终候选SHA 9F25088C6E7AE19572F6F4797FD2E318697B379F33804C780234A1711EADB46B；只读依赖既审resource BC09B49FD572D80C03BD34A2D7987C37908A856EF80B3BFA04A49CD0E745D8F8，再链pure D005A1E898CD5B900FB75E3149F78D20F1E99BCC2E7529424C79F5E2F95335AE，旧文件/白名单不改。默认PLAN无网络/密码；--run-self-session固定5序loginLIM/menu能力true/同P有效POST02001/ownL GET成功/保同T1等待已批准撤LIMITroles后再GETL。Windows控制台msvcrt限180秒Enter等待，总240秒，未创建阻塞input线程/心跳；最后只能原token，00023严格版本通过，00024仅部分原权限不可用并overallfalse/versionGatefalse，普通00005不代证，不声称JWT销毁。

--run-disabled独立阶段仅两固定账号各fresh login一次，前个不是HTTP200/false/通用00000/空data即停不索第二密码，max2/总90秒；00001非禁用证。外部冻结回读及LIMIT软删前置由root负责，输出preconditionsVerifiedByClient=false不假造数据库事实。共享传输保固定localhost9016/peer回环/3连接5wall/2MiB/no proxyredirectretrycookie refresh；getpass/MD5/token仅内存，安全结果不输出body/message/PII/claims，finally清理。意外成功/未知/超时停，不自动业务或审计回写。

开发侧仅新默认PLAN及12个fake场景通过：同token、POST误结果/意外成功、own失败、旧24部分与05非证、等待超时无末GET、两禁用首错不继续、输出secret哨兵不泄漏、HTTPConnection构造硬阻断。错误处理typeguard小修后只复验该新fake，realHttpRequests0/passwordInputfalse，不重resource7或其它套件。已直接交QA独立source/fake审并抄root，实际run只能root在resource已证+两笔授权实际回读后或两账号冻结回读/删LIMIT之前调度用户本地执行。当前无真实HTTP/DB/权限/业务/浏览器/服务/应用源码/Git动作。

### 2026-11-01 SELF/T1及disabled候选独立门禁通过

QA独立源审和离线运行明确通过，无mustfix；绑定9F25088C6E7AE19572F6F4797FD2E318697B379F33804C780234A1711EADB46B，依赖BC09→D005在加载时双SHA验证。独立PLAN exit0 NOT_RUN/network0/passwordfalse，selfmax5/wait180/disabledmax2/rootDispatchtrue；独立self-test exit0 NEW_PHASES_FAKE_PASS12/network0/passwordfalse，真实HTTP构造硬阻断。严格02001+ownGET控制、原token限时暂停、00023完整版本拒绝与00024部分结果区分、disabled通用失败需外部冻结事实、不合结果即停与秘密安全均经源核通过，不含真实HTTP/登录或DB。

总控可在各阶段前置实际落地后给用户这个固定SHA的本机命令，无需再QA_ACK；当前官方Edge仍登录页、SELF加E/LIMIT仅SELF两笔授权零写，故不提前启动T1或等待。disabled仍须两账号frozen1外部回读且LIMIT未删。B NULL恢复阻挡保持；后端不重离线门禁、不接管凭据、不修改权限/业务/服务/源码，仅记录并交总控调度。

### 2026-11-01 10:22后 A两笔授权真实UI落地

QA正式交实际UI保存及重开回读：SELF技术ID2113857683657629697原R两GET，仅增加E1000000000000011012，保存一次后重开checked精确1000000000000011010/1000000000000011011/1000000000000011012，无checkUnique1009/其它资源；LIMIT技术ID2114719096971497474原CUSTOM+ALL，专用授权窗完整移出后仅SELF移入，保存一次重开仍只有SELF，CUSTOM/ALL回可选。角色穿梭只显示name，SELF ID映射来自已核角色DOM，不冒隐藏ID已读。未改scope/dept/posts/PEER关系或资料；PEER因原SELF新增E的影响在用户已批范围。保存起点北京时间10:22:35之后，未虚构具体完成时刻；浏览器#/sys/account/dialog0/无draft/markHandoff已交总控。

该新状态支持root安排用户fresh T1的9F250候选--run-self-session，尚无本阶段用户实际摘要；不因UI保存冒physical refs/permVer数据库独核。QA此刻不撤LIMIT、不冻/删；BNULL方案不执行、D排后0写。后端仅本卡追加与新状态报告，不接管凭据/HTTP、不DB、不重fake/实现或改其它状态，待root调度。

### 2026-11-01 SELF前像下误用旧resource阶段，证据分离

root转达用户在两笔授权落地后误跑旧resource-denial-post-v1：successfalse/UNEXPECTED_POST_RESULT/requests3，固定POST_EDIT_P实际HTTP200/原生false/完整0x000-02001/automaticRecoveryfalse。旧阶段期待00024，因当前SELF资源已含E而断言不匹配；不是意外放行或新缺陷，不回滚、不重复授权、不重跑旧resource。该摘要仅支持此刻固定P数据拒绝观察，缺ownL控制及同T1撤权，不标整SELF阶段通过。root另给正确9F250 --run-self-session命令；后端仅记录，无重测/源码/DB/HTTP或对总控重复ACK，待后续实际结果。

### 2026-11-01 原生密码弹窗与非秘密撤权信号适配候选

用户新明确允许总控自行本机测试并本机输入密码，root选择不收聊天密码，派最小独立native launcher。仅新增.gradle/org-native-prompt-launcher-20261101.py，SHA 82F1293480A66A8CDD3813FFDA83A164CF1F7CB480853B413690B02687D0AD58，固定hash导入原9F250阶段→BC09→D005，不改任何旧文件/TTY模式/业务断言/路由或应用源码。

按微软CredUIPromptForCredentialsW文档使用ctypes原生弹窗，flags=0x140082（ALWAYS_SHOW_UI/DO_NOT_PERSIST/GENERIC/KEEP_USERNAME），固定测试账号且禁止改用户名，新随机target避免匹配既有凭据，无CredRead/Enumerate/Confirm调用，不保存选项；密码在该进程转换UTF8 MD5，原生缓冲及可变编码副本finally清零，不入文件/env/argv/聊天/输出。取消/API失败停止并给安全checkpoint或Win32数字，不降级聊天密码。原生弹窗每次最多60秒且受原阶段总预算约束，超时只结束本测试进程，无其它服务/窗口动作。未实测当前桌面实际弹窗可用性，不冒已可用或已输入凭据。

--run-self-native使用stdin pipe，必须tty=false；在任何弹窗/HTTP前检查pipe类型，非pipe明确SIGNAL_PIPE_REQUIRED。原phase已证SELF POST02001+ownL成功后，回调flush WAITING_FOR_REVOCATION安全标记，同T1只在内存，PeekNamedPipe限时轮询非秘密精确continue换行（最多32字节/180秒且不超原240秒），无心跳/input线程，不读新token。root实际撤LIMIT roles并正常回读后才向同session stdin发送continue换行；错误信号/通道断开/超时停，不自动恢复。--run-disabled-native逐账号弹窗，仍原max2/90秒及冻结回读/LIMIT未删外部前置；业务结果均直接复用9F250函数，不改00023/00024部分结果/00000断言。

开发仅默认PLAN和7个新adapter fake通过，nativeDialogs0/passwordInputfalse/realHttpRequests0；fake核原生flags/固定账号/MD5、取消、缓冲清零、分片continue、错误/超长/尾随内容拒绝、超时及秘密哨兵不输出。未重原12/7或真实run，候选交QA独立source/fake审与root。启动为Python -I -B新文件--run-self-native（或disabled-native），root须exec_command tty=false/yield1000，读取session安全checkpoint后用write_stdin发送非秘密continue换行，不把密码写stdin。

并发保护：root随后报告用户原TTY9F250已进入等待；QA紧急撤权遇timeout后重绑至登录页，无任何保存/撤空。该实际会话由root处理，不为迁移方式中止或新开并发session；本launcher始终仅离线准备，原9F250/依赖hash未变。不给零撤权造00023前置，不自行发continue/撤权/冻结/删除/DB或HTTP；待root当前阶段结果及新调度。

### 2026-11-01 native独审通过，QA撤权面板准备就绪但未保存

QA独立核launcher SHA82F1293480A66A8CDD3813FFDA83A164CF1F7CB480853B413690B02687D0AD58及9F250→BC09→D005链，无mustfix。独立PLAN exit0 NOT_RUN及self-test exit0 NATIVE_ADAPTER_FAKE_PASS7，均真实HTTP0/nativeDialogs0/passwordInputfalse。真弹窗及实际exec stdin管道仍未测试，原未完成TTY不冒实际通过；后端不重跑。

QA最新fresh官方Edge1核管理员，LIMIT技术行2114719096971497474，角色面板加载完成已选恰SELF1项/可选8，面板无改动、未保存撤权，markHandoff保留。该状态供root在新native实际WAITING_FOR_REVOCATION出现后派唯一SELF→[]保存并重开回读，再向同session发非秘密continue；后端没有启动真实run或发信号，实际启动/当前旧会话冲突处理均交root，不自行把QA就绪当撤权已落地。disabled仍需frozen1外证且LIMIT未删，未执行。

### 2026-11-01 工具无stdin PIPE的失败与独立桥候选

root真实启动原82 native候选时，exec ttyfalse返回exit1 SIGNAL_PIPE_REQUIRED，按原设计在任何prompt/HTTP前停止。此为执行器stdin能力不符，不是业务门禁失败或已输入密码。root派最小父Popen桥；原82/9F250/BC09/D005全部保持，未让用户重新运行命令或迁移其旧T1。

新增.gradle/org-native-pipe-bridge-20261101.py，最终候选SHA CA7BB5A9EB8634DCB3F3CFECF2137D9456042612414828D9E833E38F4800D934。父仅Popen冻结82一子进程，stdin/stdout显式PIPE、stderr丢弃，无shell/代理/凭据参数；原生弹窗仍在该child处理，桥不获取密码/token。随机run目录先设并核currentSID/SYSTEM/Admin3主体受保护ACL；child实际WAIT才创建ready metadata、输出实际bridgeRun及signalArguments。root必须先等QA真实撤LIMIT全部角色并重开回读，再独立调用同桥--signal实际run，fsync临时文件+hardlink排他原子发布continue.signal，内容仅固定continue换行。父一次转发给原child，不换会话；早/旧/过期/重复/非法信号拒绝，无密码文件/env/argv。

父总预算270秒，finally仅收本Popen持有的child、关闭管道并落done；源核补reader_done等待队列完成，防exit竞态。stdout仅解析冻结child的已知JSON字段，未知原始行不输出。开发仅默认PLAN及新10fake通过，realChildren0/nativeDialogs0/realHttpRequests0；只测新桥原子一次信号/早旧重复非法拒/协议/ownchild清理/实际main超预算/断管关闭，不重旧7/12或真实run。

QA已独审通过中间49F737C39AE2F8D97033903A1818A4E6E84CDCCDA97CE8CABD3B98C51D8011C0及9fake，与最后断管cleanup修交叉。最终CA仅删除unused import、stream.close捕OSError、done双finally及新增broken-close第10fake；已要求QA增量绑定最终CA，不把49通过套CA。root已获明确候选/命令并暂不启动待增量结论，QA面板继续SELF未撤权。待最终pass后root执行--run-self-bridge并空poll原session，见WAIT派QA保存/回读，再另exec --signal实际path，最后poll原父final；无需往工具stdin送continue，亦不让用户重跑。后端未真实启动bridge/child/popup/HTTP、未改权限/DB/服务/应用源或Git。

### 2026-11-01 native SELF与同T1撤权真实完成，等待最终RO调度

QA已独立增量通过最终CA7BB5A9EB8634DCB3F3CFECF2137D9456042612414828D9E833E38F4800D934及新10fake，无mustfix。root实际session74713在收49中间通过消息后启动，与最终cleanup窄修消息交叉；两候选都已独审，后端未再改桥/依赖或并发启动。按root要求只读核当前CA文件LastWriteUtc04:39:54.4511152Z，bridge父PID14356创建04:39:54.4694240Z，写文件早约18.309ms，但无启动源码hash封存，不冒loadedSHA确定性。实际run b51f0ae8793d4c00aeac8e98a5837147的ready/continue/done仅核名称长度时间，未读凭据或会话输出；最初误报目录空已即时更正，不保留为事实。

root正式交真实结果：session74713 exit0，self-session-v1 success=true/SELF_AND_OLD_PERMISSION_REJECTION_MATCHED/requests5；P POST HTTP200/false/02001，ownL GET200/true/0000，QA收到实际WAIT后仅LIMIT仅SELF→[]保存一次并重开实际0已选/9可选，12:41:52已回root；root发送非秘密文件signal后同T1 GET200/false/00023，sameTokenUsed/versionGateMatchedtrue、automaticRecoveryfalse，bridgeComplete=true/signalSenttrue。明确为本次真实SELF数据拒绝与旧会话撤权后原权限不可沿用通过，不称JWT必销毁，不拼未完成旧TTY或离线fake，不重测。

root已另派QA余下PEER撤权/四角色资源回收/冻结4角色（DEPT留C）/两账号禁用，LIMIT暂不删；disabled真实弹窗由root在冻结回读后调原82，无需PIPE桥。后端不抢先发请求、查询DB、写权限或重验证，仅保存本卡，等待root明确最终一次combined RO授权；桥与四依赖全部锁冻结，按指令不再修或重复ACK。

### 2026-11-01 SELF资源清空被正常UI阻挡，余项暂停

QA交新阻挡：已完成PEER角色撤空保存及重开回读；SELF模块资源逐项取消到最后E时出现“至少选择一个资源”，关闭警告后AX最后E仍Value1，随后仅关闭授权面板未确认保存。现role页dialog0/无draft/markHandoff，无自动回写。该面板本轮未服务保存，SELF最后实际持久化证据仍原三资源（未再重开/DB查询冒新后验）。其它role资源/账号冻结/删除仍本轮0动作；QA暂停后续，待root安排已批准清空资源的可用通道或顺序。

后端仅记录新阻挡并通知root，不自行绕UI私HTTP、不新增API/改源码、不提前最终RO或冻结、禁用/删除，不把PEER角色清空当SELF资源回收完成。

root随后派只读确认空授权后端契约，已直接给frontend/root精确位置：RoleCtrl:67接POST /sys/role/grantResource，RoleResourceRefBo字段resourceId（单数List、无非空校验）；RoleService:318/332及sanitizeIds:723接受空集合，ensureResourcesValid:573/ensureTenantResourcesAuthorized:583/ensureSchemaResourcesAuthorized:679空均return，:342-348按本role既有差分删refs，:359-361仅差分时发布正常资源重建事件/推进当前成员permVer，listener AFTER_COMMIT重建本role缓存。故现后端已支持{roleId:固定ID,resourceId:[]}正常清空，不需新API或手工cacheclear。这里只读源码事实，不代表此请求已运行或SELF已经空；未测试/API/DB/源写，当前禁用账号和后续前端写锁仍交root安排。

QA另交root独立派发的账号冻结实际UI：LIMIT/PEER两个精确技术ID原未锁定，各点锁定一次后显示已锁定/解锁入口，正常查询回读两条仍存在且已锁定；12:45:45前已通知root可运行disabled-native，LIMIT未删。未改profile/资源/删除或DB；account页无dialog/draft/在途写并已暂停交source编辑窗口。该事实仅关闭disabled的UI前置，不当fresh登录拒绝已通过；后端不代run或新查DB，等root实际结果/最终RO派发。

### 2026-11-01 disabled真实闭环，最终combined RO仅准备

root正式交真实native disabled session33288 exit0：success=true/BOTH_EXPECTED_LOGIN_REJECTIONS_MATCHED/requests2，LIM和PEER各fresh login HTTP200/原生false/完整00000。结合QA先前frozen1且LIMIT尚未删的外部实际UI证据，该阶段通过；不是仅凭通用LOGIN_FAILED独立证明冻结。未重测、未从后台代root读取任何凭据或session。

root已放QA按C逻辑删LIMIT并继续其余资源/角色/组织/模块清理，仍在执行，不提前标后值。后端仅本机读取09:59封存result的固定ID清单，准备最终一次限定combined RO：两账号物理行（LIMIT逻辑删且冻结、PEER保留冻结）、两个profile仍在及原CODE/DEPT、五S角色（DEPT逻辑删、其余四冻结）、两组织样本（L逻辑删、P保护原业务作者字段不变）、M01/M02及唯一自有资源2111793873144872961逻辑删而物理保留；按两USER_ID和五ROLE_ID双向完整membership/posts/resource refs及对应派生授权精确清空；保护R01/四S部门及P样本与原技术前像核差分。模块原引用空，不扩大到真实admin或全权限表。非批准对象不查询；不读姓名/联系方式/密码/日志，不重复FK/trigger/全元数据或dict数据，不迁移或cacheflush。

最终检查保持指定dev/source-build同源、单连接repeatable、起止双ROon、显式列及ID参数、rollback/close，无数据库写。当前只有范围准备，尚未连接DB或创建假后验；必须等QA所有实际清理结束且root明确combined RO执行窗口才实施。桥及依赖保持锁定，后端不新增实现、不催重复ACK，待root。

### 2026-11-01 角色普通编辑冻结与冻结账号删除：仅源定位

root/QA交SELF普通编辑frozen从0改1、保存关闭后列表两次仍未锁定；其它角色资源空保存及回读已完成为QA报告，未冒充DB后验。只读确认RoleService:194-199先Convert后强制existing entity.frozen=origin.frozen，普通编辑刻意保留原冻结状态；RoleConvert确有frozen映射，并非转换遗漏。该次普通编辑仍insertOrUpdate且:200-201推进当前角色成员permVer，不能称零写；其它业务/审计字段是否有差分待最终限定RO比前像，未提前查询。正常RoleCtrl:50-55 POST /sys/role/switch调用RoleService:213-233逐行toggle，READ_ONLY9999跳过；当前1会变0，非幂等“设为停用”。QA发现的选行后批量停用角色为既有正常入口，需root派精确四目标、先确认0、执行一次并回读，不重复普通保存或盲点切换。

冻结账号删除链：UserCtrl:91-98 POST /sys/account/del→UserService:148-159，ensureUsersEditable:423-429→mapper/User.xml:85-89只拦FROZEN=9999或profile.CODE=9999，普通冻结1不拦；正常鉴权/授权仍须满足。现有后端可在保持frozen1时逻辑删除账号、清理其角色/岗位关联并发离线事件，profile物理保留，无需先解锁。前端冻结1行仅查看/解锁是现有安全交互，后端未要求移除通用限制。无正常UI入口时提交root的最小选择为保留冻结并标删除未验，或用户另批仅固定LIMIT、正常管理员鉴权、既有/del受控通道；不得私fetch/复用cookie或新增契约。临时解锁会产生可登录窗口且删后frozen0偏离原C目标，须另批，未实施。以上已发root/frontend，未真实API/DB/业务源改/切换/删除，最终RO仍未获执行窗口。

### 2026-11-01 用户批准LIMIT专用原生删除通道：待QA与root调度

root转用户明确批准call31b4fb64：既有删除接口仅逻辑删除LIMIT，管理员密码本机弹窗。新增唯一独立工具.gradle/limit-native-delete-20261101.py，SHA256 092290C3B1D9E3D51594BB87D6785268BF62F40C0B66D9BF737B1026A021926B。默认PLAN及13个删除功能必要fake均exit0，真实HTTP0/原生弹窗0；未重跑旧green。固定依赖原生工具82F129及其原hash链，未改任何冻结依赖/应用源码/接口/DB。原生CredUI允许本机输入应用用户名与密码，flags0x40082、随机target、不保存凭据、缓冲finally清零，不打印账号或秘密。管理员身份必须来自成功登录服务器生成user_info.authorities内ROLE_ADMIN、返回account匹配本机输入、actor ID非LIMIT，依据UrlAuthenticationSuccessHandler和ThreadUserHelper现有实现，不猜管理员账号名或信UI称呼。

固定loopback localhost9016/api仅5请求：login、fetchMenus、get固定LIMIT、POST /sys/account/del body仅param=[2114719096971497474]、同token再次get固定LIMIT。前像匹配id/account、整数frozen1、09:59技术档案code/dept、roles/posts/assignedRoles空；只输出技术匹配布尔，不输出详情原文/姓名/联系方式/账号凭据/token。总90秒/弹窗至多60秒/connect3/request5，拒绝redirect，无proxy/cookie/回退，无重试或自动恢复。任何未知/漂移/失败立即停；deleteAttempted/deleteAcknowledged明确区别。删除后详情0x001-01002同时涵盖权限不足或不存在，因此成功仅命名DELETE_ACK_AND_DETAIL_UNAVAILABLE_MATCHED，不冒充物理行逻辑删除和profile保留证明，交最终限定RO。13fake含正常、非admin、自身目标身份、错ID、未冻结、引用非空、档案漂移、删除超时、删除拒绝、删后仍可读，以及原生缓冲清零/取消/错误body拒绝；零秘密输出断言通过。

已发送QA窄source/fake独审和root命令/hash；--run-native未执行，真实运行须QA通过后root明确调度。命令为C:/home/DevApp/python3/python.exe .gradle/limit-native-delete-20261101.py --run-native，cwd EVA-API/login:false，无需stdin pipe。最终RO仍无执行窗口，不提前连接。

QA随后独立审查同一092290候选通过，无mustfix；实核完整hash，独立默认PLAN exit0/PLAN_ONLY/0HTTP0native，--self-test exit0/LIMIT_DELETE_FAKE_PASS/13场景/0HTTP0native；未真实run、未输入密码/真实HTTP/DB、未改旧依赖。已将此通过及原命令交root，候选锁定不再修改；真实运行仍须root调度，后端未代执行，finalRO未开放。

root交真实session41077结果：启动后仅到NATIVE_ADMIN_CREDENTIAL_PROMPT，随后NATIVE_PROMPT_TIMEOUT、exit1、automaticRecovery=false；未删除，不自动重试。该结果只记录为凭据弹窗超时阻挡，不能计为删除失败响应或清理完成；后端不改timeout/框架、不重跑fake、不代run。root另交QA实际UI已删除DEPT/L并回读、四角色冻结通过、D仍进行中；均只作为root/QA报告，不冒充数据库后验。最终combined RO仍等待root明确执行窗口，未查DB。

### 2026-11-01 D模块冻结入口阻挡：仅源定位

root交M01/M02均冻结1，模块edit开关无专用UI解锁绑定，当前未写。只读确认ModuleService:125-128明确edit不处理frozen，:157-164读取原模块后保留origin.frozen；ensureModuleEditable仅拦9999，不能靠edit开关解锁。现有ModuleCtrl:83-90 POST /sys/module/frozen接ModuleFrozenBo（继承SingleArray，param集合+frozen目标0/1），Service:307-338设明确目标状态而非role式toggle，已是目标跳过，missing/9999跳过，子节点父frozen1时单独解锁跳过；因此可能正常成功应答而状态未改。mapper/Module.xml:59-68更新自身及path前缀所有后代，排除9999，但没有deleted过滤，逻辑删除后代物理行也在级联匹配范围，不能称仅传入ID或仅active后代。M01从1设0会同时解锁M02，M02先设0则被父冻结阻挡，不重复盲切换。

已交root/frontend最小方案为绑定已有switchModuleStatus到独立启用/冻结动作并保留级联确认与9999保护，普通edit职责不变，不新增API；是否实施及级联范围由root裁定，后端未改源/接口实调。仅为清理，deleteModule:76-121守卫仅拦9999和有子节点，并不拦普通冻结1，可按M02后M01删除无需解锁，但必须有已授权正常入口，不绕前端安全交互。父删getDeleteBlockers正确拦子节点，不能为了测0x000-02002移除此保护；CommonCodes:48及Service:84-94只能证明源码有该分支，真实后端子节点拒绝尚未触达。09:59子孙范围历史证据不替代最新物理范围，未额外查DB。LIMIT仍等用户native输入ready且root派发，不代run；最终RO未开。

### 2026-11-01 13:54 获批模块专属一次RO：完整物理级联范围恰M01/M02

root因cascadeFrozen无deleted过滤新实质风险，明确授权本次模块专属前置RO（不是finalcombinedRO），仅锚M01自身/全部物理后代技术字段及资源引用计数。新增本地固定工具.gradle/module-cascade-scope-ro-20261101.py，复用已审datasource AST解析及bridge.private_run三主体ACL目录helper，未执行旧桥/凭据/业务HTTP。一次--execute exit0/APPROVED_SCOPE_MATCHED，时间13:54:08.677620→13:54:08.703629+08；dev批准指纹与source-build同源、dev/prod区分通过，1连接/3 SELECT（起止session+范围聚合）、eva/eva、repeatable read、起止transaction_read_only/default_transaction_read_only均on、writes0、rollback/closed均true。

范围SQL以固定M01锚点递归pid物理后代并合并实际path前缀集合，不过滤deleted，技术投影及关联聚合结果恰M01 2111793871655895042(pid0,path/本ID,deleted0,frozen1)与M02 2111794099880558593(pidM01,path/M01/M02,deleted0,frozen1)，无额外物理行或逻辑删后代。M01资源总1/active1/角色资源ref0，M02资源0/active0/ref0。结果受限文件.gradle/native-self-bridge-089e47b584fc491999fca5b147d72b83/module-cascade-scope-result.json，SHA256 08625F0F8AA74CF10FC337AE3FBE1C2D10E384C10B39F941DDE758A5ACDFC901；目录名源自复用ACL helper，不代表运行桥。实际SQL SHA256 0BAEFA09A5D3A19B4131BA63A7C5FD36F5F46F4E708589312EA16D5FA249DD75。已将精确结果发root/QA；此时点物理范围支持批准M01→M02级联，未执行解锁/删除，不重查、不查其它域，最终combined RO仍未开放。

QA随后交D实际UI完成（北京时间13:58:58）：fresh admin先确认M01/M02均1，M01专用级联解锁确认一次、回读均0；仅M01删除被正常父子UI保护拦一次且父子仍在，不是server02002；仅M02逻辑删确认一次回读子0父1，再仅M01逻辑删确认一次回读均0。结束dialogs0/selectedRows0/无draft已交root，LIMIT未碰。QA模块修复独审manifest43AF63及五文件hash一致、独立8定向pass/13skip及Biome五文件pass，无mustfix；首次pnpm.cmd regex管道解析未实际测试，经直接node本地vitest后通过，未重旧全套。后端只记QA报告，不冒DB物理行/资源删除后验；server02002分支仍未真实触达，资源物理保留与逻辑删值留最终combined RO，未自行开窗。

### 2026-11-01 LIMIT可见终端适配候选：待QA与root启动

root交用户确认native admin popup根本未显示，两次均仅NATIVE_PROMPT_TIMEOUT且无del，禁止再盲native重试。root选择已授权可见本机PowerShell窗口，用户只输入local账号/getpass密码，无需复制命令。新增独立wrapper.gradle/limit-delete-console-20261101.py SHA256 8970396A63CC88CEC1DA57D23C7BB1A0B7B9CF329AC69B9FB475CC4F51682B39及固定launcher.gradle/limit-delete-console-launch-20261101.ps1 SHA D135145DE5802EA7F43074D07B98098A27107977D774368F409AF50D91E516E6。原删除092290实核未改，wrapper hash锁复用原run/LocalHttp及原admin身份/固定五请求/LIMIT唯一ID/所有业务断言，不重复native或旧十三fake。

wrapper默认PLAN，--prepare仅复用已审bridge.private_run创建随机三主体ACL目录，返回runDirectory/summaryPath，排它写console-plan含purpose/nonce/hash/15分钟期限；--run-console需匹配限定路径/计划/wrapperhash及排它console-started，拒绝复用运行。真实Windows stdin/stdout TTY才input账号/getpass密码，GetPassWarning直接拒绝回显fallback，凭据不经工具stdin/argv/env、不写transcript，仅内存MD5，清空可变编码字节及引用。输入期至多60秒，超时仅在首HTTP前发布安全summary并exit2；HTTP阶段仍原总90秒deadline和固定transport，无重试/恢复。summary.json排它安全白名单含退出码/请求数/尝试与ACK/最终RO待验标记，不复制用户名、密码、token、响应原文或异常文本；缺失/未知须停，不能自动重跑。

仅新增七必要adapter fake（本机输入摘要/非TTY/EOF/禁止回显降级/过期预算/输入超时/安全摘要过滤）及默认PLAN均exit0，0真实HTTP/0native/0realConsoleInput；launcher PowerShell静态语法解析0错误。已交QA新增wrapper/launcher窄源码与fake独审及root精确--prepare/Start-Process -WindowStyle Normal -File启动命令与summary路径获取方式；真实可见窗口和--prepare均未由后端执行，等待QA及root调度。无应用/API/DB/服务写，最终combined RO仍未开放。

QA新console adapter窄审通过、无mustfix，实核897039 wrapper及D135145 launcher完整hash，原delete092290未改；独立默认PLAN exit0/0HTTP/inputfalse，仅新增七fake exit0/CONSOLE_ADAPTER_FAKE_PASS/0HTTP/0native/realConsoleInputfalse。未重原十三/prepare/开窗/真实run。已向root交通过和原精确启动方式，候选锁定；真实窗口/TTY可见性与删除结果须root实跑验证，离线通过不能记为删除完成。summary缺失或未知仍停不自动重试，finalcombinedRO继续未开放。

### 2026-11-01 可见启动失败根因与唯一BOM候选

root实际prepare生成native-self-bridge-cfa10f49cb8141a5b4552b309b93e648，仅console-plan.json311字节；两次系统WindowsPowerShell Start-Process固定旧PS1均提前退出，后一次明确ExitCode0，无console-started/summary，因此未进入凭据或删除流程，root停止重复开窗。获批仅无input/HTTP离线诊断：同系统WinPS5.1.26100.9711 DefaultEncoding=gb2312，旧UTF8无BOM PS1 ParseFile errors0但Python CommandAst0，Comment token含固定Python调用true；GB2312解码将下一调用行吞入中文注释，解释了仅SetLocation后exit空LASTEXITCODE表现0。相同WinPS通过EncodedCommand直接python-u默认PLAN及-I-B默认PLAN均PLAN_ONLY/exit0，排除解释器-u与隔离参数差异；没启动真实窗口或输入/HTTP/DB。此前仅PowerShell7解析无错误不足以证明WindowsPowerShell5的调用存在，现用实际目标宿主定位修正。

root明确授权只加UTF8 BOM，保原文本/参数/空行/timeout/逻辑。新增独立.gradle/limit-delete-console-launch-utf8bom-20261101.ps1，SHA256 240E9026BB9E07779D45A57BCD19EA884D2A464CEB09AC237B4DEC1091C79939；新bytes前缀EFBBBF且去三字节后与旧D135完整bytes完全相等。原launcher D135/wrapper897039/delete092290哈希均不变。实际WinPS5.1 ParseFile新候选errors0/pythonCommandCount1/commentContainsPython0，命令元素恰固定python.exe、-u、固定wrapper、--run-console、$RunDirectory。已交QA仅字节一致性及实际WinPS AST/原hash窄审，不跑旧fake或真实-File；root审核后仅新prepare并替换-File候选路径，再由root决定一次Normal窗口，旧plan不复用。额外preflight草稿未运行且已移除，无新框架或其它修复，最终RO仍未开。

QA随后对同一240E9026 BOM候选窄独验通过、无mustfix：EFBBBF及去BOM全部bytes逐项一致、真实System32 WinPS5.1.26100.9711/gb2312解析0error且恰1完整固定Python调用、注释不含调用，旧D135/wrapper897039/delete092290完整hash未变。未执行-File/prepare/run-console/窗口/input/HTTP/DB，未重七/十三fake。已将通过和仅新prepare一次+新BOMlauncher路径交root，候选锁定，旧plan不复用；解析通过不等于真实窗口可见或删除完成。后端不代run，finalcombinedRO仍未开。

### 2026-11-01 14:19 最终获批combined限定RO完成：清理物理后验匹配

root交新BOM真实visible console成功：native-self-bridge-fb9005810053452d8d438ba1815d02ac/summary.json，success true、DELETE_ACK_AND_DETAIL_UNAVAILABLE_MATCHED、requests5、deleteAttempted/ACK/adminConfirmed true、automaticRecovery false、exit0；root正式开放最终一次combined RO窗口。新增固定脚本.gradle/final-combined-closeout-ro-20261101.py SHA256 ACA634C79EEB458469AEABD344F23EA7C885C394FE61B2D40DECE053D762DF5C，先固定12个域查询/显式投影/ID参数及起止session，共14 SELECT；封存D181前像与private哈希均实核，姓名仅本机内存匹配，不输出或保存原值，无密码/联系方式/日志。O01不在09:59前像及role name不在原角色投影，事先声明不比较，未臆造。

真实唯一--execute exit0，FINAL_BOUNDED_RO_MATCHED_WITH_DECLARED_PREIMAGE_GAPS。北京时间14:19:53.547369→14:19:53.680020，1连接、14 SELECT，eva/eva、repeatable read，起止transaction_read_only/default_transaction_read_only均on；dev批准指纹/source-build同源/dev与prod区分均通过，writes0、rollback/closed true。受限结果.gradle/native-self-bridge-3564c306497c4ca28a7b3638b5b982dc/final-combined-ro-result.json SHA256 D618212D88FAFE3EFDE6A72386CD28816250948D8D502B4E92C38FA5AB23D68B，同目录final-combined-ro-query-manifest.json SHA256 4992650438CB0DB2A793A657282818FE6671F18B3A1D72C319B92D33265523CD；目录名仅复用已审三主体ACL helper，不代表启动桥。成功console安全摘要SHA256 8BD6DAD819E8EDD79B10C2D6B27F1E21DF6D4A3FD5B8F3EC3789E2C188F33E82。

物理后值：LIMIT deleted1793513685/frozen1/permVer8，PEER deleted0/frozen1/permVer9，两remark均原NULL；两profile物理存在，原code/name/dept逐字段布尔全true。SELF/TREE/CUSTOM/ALL deleted0/frozen1，DEPT物理deleted1793512023/frozen0；五role+R01及两账号的双向role成员0、两user posts0、限定role与模块自有资源refs0、六role派生权限0。角色业务code/data_scope/data_org_ids/tenant/sort与前像一致，未要求普通编辑的审计/revision/permVer回退。R01全部原封存投影字段一致。L物理deleted1793512092；P作者样本保留，全部原业务/作者/审计字段匹配。四S部门全部原投影字段和active三节点树匹配，D1物理profile2、active账号1，符合保留PEER及LIMIT profile；其它限定部门/L/P样本引用0。O01本次物理保留deleted0/frozen1/root，但无前像，仅标NOT_COMPARED_NO_SEALED_PREIMAGE，不能称业务字段已核未变。

M01/M02物理保留、deleted1793512722/1793512707且均frozen0，唯一自有resource2111793873144872961物理保留deleted1793512722，模块及其资源active均0、相关refs0；原业务/创建字段匹配，批准的M01叶子变化不回退。B备注普通保存仍未执行，NULL保留不是该测试通过；父删由UI保护拦截，server02002仍未真实触达。未恢复业务/审计/权限、未写DB/缓存/服务/API、未创建fixture或重复旧测试，无第二连接。结果、manifest、脚本/hash已交root及QA，仅要求离线独审实际文件及固定scope，不再查询数据库或重跑--execute；该授权窗口已用完。

QA最终独立复核通过（限定范围及声明前像缺口），实核D618212结果与499265清单完整hash，完整读取固定脚本/清单/实际结果，确认12限定域SELECT加起止session共14、单conn/起止双RO/repeatable/eva-eva/批准dev及source-build匹配/0写/rollbackclose证据一致，无超范围SQL或PII原文输出；未重新查询数据库或执行脚本。C/D物理后验、A授权撤空/冻结与档案保留、保护对象原封存投影一致均获独审；B原NULL备注往返未做、server02002未触达、O01及角色名缺前像仍保留未覆盖，用户C3 skip/平台0及真实tenant排除不扩。QA快报资源拒绝“02024”不是正式业务码，原实际证据完整码仍0x000-00024，已在交root时明确，不改原证据。无需新增测试/连接/gate，不称全系统全分支绿；后端授权最终窗口已用完，交root整体收尾。

用户最终决策call940f07373da14562a1a519cfafe09284：B PEER备注往返明确跳过，状态USER_SKIPPED/NOT_COVERED，按当前核心范围收尾，不再待决策。未执行备注恢复、测试、数据库或服务操作，未修改业务源码或Git状态；最终RO及QA证据保持不变。root宣布本次核心范围all done，后端卡已关闭本项待决策。

### 2026-11-01 新任务0006：代码与初始化获批，现库另审

用户call76107391acf6406ea2f48de62fd97cd5批准代码与初始化调整，现库字段与字典适配另审。本角色仅EVA-API源码/测试/两份单初始化；前端、真实dev库、API、缓存、服务及Git发布均未操作。按设计最终节实现独立dataPostIds数组↔CSV、角色岗位全集本源验证、请求内可信schema有效账号/角色/岗位交集，以及方法级controller纯读窗口+实际只读事务+实际MappedStatement SELECT三条件。原角色OR/ALL、专有日志资格、Ignore和全局数据开关保留；16入口接线、Module两读补事务，StdService不全局开放，GET删除/写预读不进入窗口。role/savePosts原版本链复用，post冻删实际级联成员版本同事务推进；不添加JWT岗位claims或长期缓存。

主V1/tenant唯一模板各新增DATA_POST_IDS TEXT；主SYSTEM DATA_SCOPE增加0006“指定岗位”，ID1000000000000004008/main1000000000000004001/sort7，原五项及0007sort8不变，private模板不新增SYSTEM种子。现库未适配前不可加载新字段运行，未重放V1或实际DDL/DML。现Post.xml原级联WHERE DELETED=0未排9999子孙，本次按实际更新集合捕获失效用户，不自行改旧岗位级联政策。

源冻结.gradle/role-0006-20261101/source-freeze.json，SHA256 CB76828AE249553AE2523F250847B2EB6E2BDF3F4673F1ACE10D301B734B57BF：40个Java/XML/SQL输入、50产物及args/log完整hash。原login/用户13保护（handler为批准改动排除）与字典13源全匹配，限定diff --check通过。最终cached-classpath javac exit0（有Lombok/APIguardian既有警告，不是全Gradle）；初轮六类28/28/0skip test-1；补真实MP同selectById读写/页count、跨schema同ID/缺表不回落、有效0/9999/冻删/坏CSV、岗位版本失败事务回滚及主/私有初始化后的三类19/19 test-3；最后仅规范历史CSV整体回显，最终RoleServiceTest16/16 test-final-unit。测试均本地隔离PG14.22，已正常关闭；回滚用例为真实Spring JDBC事务和实际服务，post/user mutations为JDBC-backed mock，级联成员SQL另用真实Mapper执行，不能冒充全部生产Mapper/JWT/browser链实测。初失败日志原样保留（测试静态mock限定、mock varargs类型错误），没有真实dev操作。

源已锁定交root、designer只读审及QA独立定向复核；后端无重进程，明确释放唯一重槽交QA，不再自启动重测或随手改冻结输入。当前待独立QA，尚不宣称新功能联合运行完成；现库字段/字典及服务加载另批。

设计独审发现standalone岗位关系pu归属缺口后，CB768候选已撤回，root授权仅补pu.tenant_id NULL/0及真实PG负例，private分支不引用不存在列。新唯一产品冻结source-freeze-v2.json SHA237DB48F945863B9B377B275AEEC550C4245D821C97FF05F1A7855789D621462，40输入/50产物。独立单类PG验证pu99拒绝/0NULL允许、private无该列positive及原MP/版本/双初始化断言通过；第一次单类因fixture借用其它测试的PostEntity lambda缓存失败，已在自身fixture显式初始化，不放宽生产逻辑。QA另建独立classes/generated、去后端旧roleclasses并前置自己的类，六类28/28/0skip及一个合成PG自然通过；结果.gradle/qa-role-0006-64251dfe59/result.json SHA4925AA027D6EC01FD207DD49E3A3D7FEA5407154088ACF4FD02D9E5D2EF3DFCE，运行前后40输入均匹配。产品源码阶段关闭，不重复旧测试或改产品；真实JWT/API/browser/现库部署尚未验证。

root另授现库适配前置RO一次，16:13:51.496072→.572444+08实做1连接/7SELECT/双ROon/repeatable/eva-eva/0写/rollbackclose。结果.gradle/native-self-bridge-971bfcdf557b4760aaf0dfefc1861a21/role0006-preflight-result.json SHAE58E09A72B9BDA287CA6A3C3618AD20D5E3876FB20825DA17A1F8F50C3886EED，清单CEDADC261D4C281B60C4F28110ABD198DFC1548696163F2AAEDA16A9772E1C8B。仅核角色列/约束和SYSTEM DATA_SCOPE，不读角色业务/用户/PII：列缺失，主4001 SYSTEM/frozen0/deleted0/tenant0，原五码及sort匹配；0006唯一物理明细4008/main4001/指定岗位/sort7/frozen0/tenant0已存在但deleted1，不能INSERT或另猜ID。源/构建配置standalone及数据源一致、连接真实schema eva；未核进程CLI/env覆盖，不冒运行态配置已证。窗口用完，无写授权。

现库维护候选置于ignored .gradle/role0006-adaptation-20261101，candidate-manifest.json SHA A9CD50A4E41C428FEC1A60F49079D493FD02A3865DEE6D9AF6409F5DFA29F796，qaReviewed=false且不存在可执行maintenance-seal。forward-operations.sql仅加nullable TEXT无default及固定4008 deleted1→0；Python默认PLAN0连接，未来独立批准并QA绑定后才可锁/精确前像重核/受限前像落盘/单事务执行和后验。Java维护源只复用RedisConfiguration.cacheManager及DictCacheHelper.remove(Owner(eva,SYSTEM),DATA_SCOPE)，不用BUSINESS reload/扫描/全清/端点/重启；源码与build Redis绑定一致且dev/prod endpoint+db不同，指纹1c2ed984b89f。既有CacheManager fake实证仅DEL dictdata::v2::eva::SYSTEM::DATA_SCOPE一键；局部维护javac、Java PLAN/fake及Python PLAN均exit0，真实Redis连接0、DB写0。未来先只读PING，不通则不建PG连接；DB commit后收据绑定再单键evict，commit确认未知标COMMIT_OUTCOME_UNKNOWN、提交后缓存失败保DB_COMMITTED_CACHE_PENDING，不自动重试/恢复业务。rollback-review.sql默认ROLLBACK，仅供另批恢复deleted1，新列不自动DROP。候选已交root/designer/QA窄审，唯一重槽再次交QA；当前40产品输入仍匹配，不把产品QA或前置RO当维护执行批准，也不把私有前像/本机维护器纳入仓库发布。

维护包独立QA完成，无mustfix：candidate A9CD50十一文件实核、独立classes重编译字节与候选class一致、Python/Java缺省PLAN均0连接、真实项目CacheManager单键fake通过且0真实写，未PG/Redis/PING、未重产品测试。QA新增maintenance-seal.json SHA453F10428A7AB47846A714876A72603FA606C93D9D7249E04E2D886E12E19438，qaReviewed=true、existingDbOrRedisExecutionApproved=false，14绑定文件复核全部匹配；未伪造批准窗口。designer只读审亦无新阻挡，E58仅限定技术前像非完整行/全库备份，Java程序只作为Python已审主入口内部子进程，不作独立获批入口。已交root一次具体加列/恢复一条/提交后SYSTEM单键失效的执行审批依据；真实操作仍未获用户单独批准，后端不代run、不改产品或seal。无活动重进程，槽已释放。

### 2026-11-01 字典内存同步A获批：后端条件快照候选待独审

QA最终离线独审D87FAE23结果与0568BAAF查询清单完整SHA、脚本固定5SELECT及内部绑定通过，与UI修改回读/正常逻辑删一致，未重执行或查DB。后端本项收口，无新gate/认证/测试/DBRedis/服务/发布；保留global React map远程更新未现场观察、旧probe Vary误解析不签live头及窄屏未实测边界，交root统一最终收尾。

20:46 root另授权唯一临时字典物理只读后验已执行完，固定master2116862974824505346/codeEVA_QA_DICT_SYNC_20261101_01/BUSINESS及其全部明细、同code activecount，起止session共5SELECT、单conn、eva/eva双RO on/repeatable、批准dev及source-build一致，0写/rollbackclose，无密码提示/HTTP/Redis/服务。结果.gradle/native-self-bridge-d746ced9b00847cab95aa63614a86dfc/dict-fixture-postcheck-result.json SHAD87FAE23B6911BE24E66B5EC8610A97AE1B4ABA3E3C35F60EC98E32937D9A90F；清单SHA0568BAAF52E63633D43A40D287802283896939F78D079B4B570727B1D033EB3C。20:46:17.039451→.686069+08，INDEPENDENT_FIXTURE_READONLY_POSTCHECK_MATCHED：主项物理保留deleted1793537002，唯一明细实际ID2116862983326359554/d_codeQA_SYNC_01/同步后/sort1物理保留且同deleted值，activecode与activeitems均0。已交root/QA仅离线独审，窗口用完，不追加连接；该证据是UI改标签和逻辑删的物理后验，不冒global React map live观察。probe取消及用户不再输入凭据约束继续生效。

CA68后续designer独审信任边界及9输入/role40一致，无新增阻断；QA独立实际Gradle只HTTP类+PG指定新方法加--rerun/offline/no-daemon/no-parallel/maxworkers1，自然exit0/38s，XML各1/0fail/error/skip，真实计数50且新增10字典不增，9输入运行后0漂移，无mustfix。结果.gradle/qa-dict-sync-batch-20261101/result.json SHA524C2D72123C36189A1FBBE9AF75872E684C2EB4E2F370ABB5F5A031E31E4C61；日志SHAF0802F0AF3FBC6B1069B1CC26AA33959E8E47DE71D1E0274992ABED670103747。已报root后端收口保持冻结，重槽交frontend；未重启/真实probe或生产性能验证，无真实dev/Redis/fixture操作。

root后续授权限定snapshot查询放大质量修复，designer确认信任边界后只改DictService.snapshot委托、DictBusinessAccess.snapshotProjection及原PG新方法。一次requireSource，事务内按唯一schema结构/分类去重，每owner一主清单和一次JOIN明细；全量未删主项含frozen先核code冲突，主NULL沿原getInt语义、item仍frozen<>1，空字典/排序/身份Owner/hash/RR保持。旧sources/completeDicts/completeItems/query/管理/缓存原样不变。真实Gradle仅HTTP类和PG新方法两个target自然exit0/50s，XML各1test/0fail/error/skip，实际schema模式JDBC语句50（含测试SHOW与路由），增加10个有明细字典后仍50，不冒生产耗时SLA。候选source-freeze-v2.json SHACA68BBCC4C6FA19496E4F4A13ABBEB8CD25183E33BE5A46A4E3D2390CA3EC06E封存9输入/14相关产物及batch日志/XML；相对06BE恰上述3输入变，其余6不变，0006v3 40输入实比全部不变。已交root/designer/QA独审，重槽归还，无真实dev/Redis/服务操作；旧06BE只保历史不作为新候选。另root后续可见console探针两次真实摘要证明login已成功、首次fetch未完整返回，非无文件/0请求；探针A35仅加安全阶段/耗时分类及GET30秒工具预算获QA窄审，尚未成功新基线或改删fixture。等待新源码独审及用户重构建重启后由root调度，不自行重复凭据请求。

后续designer独审06BE冻结9输入及0006v3 40输入全部匹配，无新增阻断；QA独立源审及真实Gradle指定HTTP类+PG新增单方法加--rerun/offline/no-daemon/no-parallel/maxworkers1自然exit0/41s，29tasks1exec28up-to-date，两XML各1test/0fail/error/skip，9输入运行后无漂移。结果.gradle/qa-dict-sync-backend-20261101/result.json SHA358ABE013137AA8124F926CF347715C8A08F97B8E372798137DC5E69B855425D，日志qa-targeted.log SHAB93895C6AC63CED646DA37BC83B386A6149EF703D282001FC35FC496F24FAC2C。MockMvc真实HTTP链仍使用服务与认证数据替身，隔离PG为合成fixture，不冒running server或浏览器；旧PG/0006未重。已报root后端收口保持冻结，待前端冻结独审和运行态安排，槽归还，无真实dev/Redis/服务操作。

root转用户在内存单快照方案说明后明确“可以”，批准现fetchDicts条件同步及前端60秒/10秒/本标签提交立即校验方案，前后端写入隔离。后端仅9输入：DictService新增经代理REQUIRES_NEW实际只读REPEATABLE_READ快照，inventory核完整SYSTEM/BUSINESS来源后完整DB投影并构造不可变有序map和weak ETag；格式版本、可信tenant/user、实际Owner schema/category与全部有序键值按UTF8长度前缀参与SHA256，不输出身份来源明文，不读写Redis。现query/管理/serializer缓存及写afterCommit失效语义保留，无schema/版本表/计数器/permVer改动。

AuthCtrl现GET /auth/fetchDicts返回正常200 ResponseEntity让Spring在Advice之前条件GET命中空体304，200原Response.data字典map不变，private,no-store；Vary Authorization/Cookie合并已有Origin，CORS仅exposeETag。新增SysCodes.DICT_SNAPSHOT_INVALID=0x0000-10002，已知来源/分类/同码/结构问题typed异常只在本端点变HTTP409原失败格式，固定“字典快照来源或结构不可用”、无ETag；普通DataAccess异常仍既有500，鉴权保持既有错误。designer指出缺表queryForObject零行误分类已以scalar subquery返回NULL后typed拒绝最小修，不宽捕数据库异常。

真实本机Gradle/JDK25/IDEA共用缓存/offline/no-daemon/no-parallel/maxworkers1主和测试编译成功。首轮两个定向中单隔离PG新方法conditionalSnapshotReadsOneReadonlyDatabaseVersionWithoutSharedCache通过：RO on/RR实查、并发SYSTEM提交在同快照不可见/下次可见、旧缓存不污染、顺序/冻结、用户/租户及缺表列/同码冲突。HTTP初失败为fixture缺父类mapper替身及OpenAPI默认配置临时对象导致mock filter阻链，修fixture而未放宽生产；所有失败日志保留。最终仅HTTP重验gradle-http-4.log自然exit0/24s，1test/0skip/0failure，真实WebSecurity/JwtAuthFilter/Controller/CommonResponseAdvice/ExceptionAdvice链验证原200、weak304空体、no-store/Vary含Origin、ETag expose、匿名/冻结拒绝、typed409与普通DB500；认证数据和服务替身不冒称liveJWT/API。

候选.gradle/dict-sync-20261101/source-freeze.json SHA06BEA8A7C5CE27F2DEABC77C90FEB02634AF04AF2C936273455B43947C393EDE封存9源码/测试、13实际Gradle类产物、全部日志及http-final-result.xml；0006v3 BE51969全部40源码当前逐项未变，现库398/C9E1结果保持。已交root/designer/QA独立定向复核，仅HTTP类及PG新单方法，不重旧28/旧PG方法；重槽已还frontend，无活动进程。未操作真实dev/Redis/服务/发布，尚待QA，不宣称运行联调。

### 2026-11-01 用户实际Gradle编译阻断修复：仅RecordRow测试夹具

QA后续窄独验通过，无mustfix：v3 BE51969D及40输入实核，逆变换精确恢复v2测试文件hash，证明仅getter替换、全部字段/lambda/断言不变。独立实际Gradle目标compileTestJava加--rerun强制执行，离线/单worker/no-daemon/no-parallel自然exit0/20s，13tasks1executed12up-to-date，日志.gradle/qa-role-0006-64251dfe59/gradle-fixture-compile.log；未重旧28或PG、未触真实DBRedis/服务/全局processor，仅既有unchecked与弃用警告。旧28仍仅绑定v2，v3新增证据为差异等价与真实Gradle编译。本项已交root收口，重槽归还，无服务启动验证。

用户本机启动构建报PostScopePostgresTest四处RecordRow::getId/getLabel缺失，root授权最高优先级窄修。当前真实build.gradle仅main annotationProcessor声明Lombok，无testAnnotationProcessor；原fixture依赖@Data生成访问方法，前轮手工cachedjavac显式processorpath掩盖了真实Gradle配置差异，本轮明确纠正验证。先用本机Gradle9.5.1与IDEA共用缓存实际复现compileTestJava exit1/27s，恰294/302两处各两个invalid method reference，无额外不同编译错。仅移除该fixture@Data与import，显式增加两个中文JavaDoc getter；字段、Mapper调用、业务断言、全局Gradle和生产源码均不变。

实际执行C:/home/DevApp/gradle/bin/gradle.bat :web-booter:compileTestJava --offline --no-daemon --no-parallel --max-workers=1 --console=plain，JAVA_HOME=zulu25，GRADLE_USER_HOME=C:/home/DevApp/cache/gradle。after exit0/18s，最终注释版本final exit0/17s、13tasks1executed12up-to-date；日志.gradle/role-0006-20261101/gradle-compileTestJava-before.log、after.log、final.log原样保留，仍有unchecked与Gradle弃用警告，不称全构建或整套测试通过。新源码修订source-freeze-v3.json SHABE51969D5484938BF2999BC69072D48EA3F1F2315FB4B41FB84DBDE0084B7CBA，40输入仅fixture变（SHA5ED87B2E3CAB54B80185F88C5D843BED029B5B47D06FF2E9E807B4564B38080D），其余39与237DB封存逐项相等，实际Gradle四fixture类产物记录，不覆盖旧冻结或冒充旧28测试验证此版本。唯一重槽释放交QA窄复核；不再改源/重旧测试/查真实DBRedis/启动服务，现库适配及未批字典合同保持。

### 2026-11-01 16:41 获批现库适配后独立只读后验

QA后续离线独复核通过：实核C9E1AC99结果及335FDB97查询清单，完整审阅脚本、六条SELECT与E58/398绑定，全部后像和只读生命周期证据一致，无第二次数据库或Redis查询。本角色范围收口，已回报root；重构建重启与真实JWT/API/browser仍未覆盖，不自行启动或重复已通过测试。

用户纠正选择后明确“选错了 批准”，root使用唯一批准窗口ba740637d272489eb90e2757db399d6b执行已封存维护一次，结果.gradle/native-self-bridge-d03d415fbd994aee9d8da6411c9e561e/role0006-adaptation-result.json SHA398BE19D9DF4FEEE3809197B0C1FA494F548127F5C44398DEAD512BD813AE0C5：DB_AND_SYSTEM_CACHE_DONE、提交确认、加列成功、恢复4008一条、单连接关闭，SYSTEM DATA_SCOPE单键失效成功，无自动恢复。后端没有重复该执行。

root另授一次独立最小只读后验，固定脚本.gradle/role-0006-db-postcheck-20261101.py于16:41:06.658489→.721203+08执行1连接/6 SELECT，起止eva/eva、transaction_read_only/default_transaction_read_only均on、repeatable read；批准dev指纹及source/build配置一致，0写、rollback/closed true，无Redis连接或服务操作。结果.gradle/native-self-bridge-ef77cac2d7a444858eda6d47d3b1bdf3/role0006-postcheck-result.json SHAC9E1AC99DF79E67B5F79801C4F5A06E797AE8FEBE75CA10F23E56C1A2DD73711，清单role0006-postcheck-queries.json SHA335FDB97B28D8377D2D691CCFA1EA3B07BD457F786B0F9E2AC4238517759B556。INDEPENDENT_POST_ADAPTATION_RO_MATCHED：新列TEXT/nullable/无default且全NULL，master及原五明细全部封存投影不变，六明细恰批准后像，4008仅deleted1→0、其他投影不变，activeCodes0000/0001/0002/0003/0006/0007。结果绑定E58前像与398执行收据；窗口已用完，已交root/QA仅离线独审，不再查询数据库或重跑。runtimeProcessConfigurationVerified仍false；用户后续自行重新构建/重启，未宣称部署或JWT/API/browser联调完成。
