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
