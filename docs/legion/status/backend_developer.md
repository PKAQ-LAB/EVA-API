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
