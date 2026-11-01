# 发布前单初始化：实现矩阵与验证边界

2026-10-31，基线 HEAD 064ed4f9cfb588527970926a324b9f59cb219a2b，Flyway 13.12.0、Minio 9.0.4。用户要求发布前生产 Flyway 只保留一个初始化脚本；总控明确租户模板是独立私有 schema 的 ScriptUtils 建库，不是另一条 Flyway 链。

## 资源和职责

生产 `db/migration` 原 V1–V14 合并为唯一 `V1__INITDB.sql`；租户 `db/tenant-migration` 原 V1/V2 合并为唯一 `V1__TENANT_SCHEMA_BASE.sql`，`TenantSchemaProvisioner` 只执行此最终模板并维护版本 1 的自有账本。两套均为 PostgreSQL，没有其他生产数据库方言脚本。没有新造 schema_only 模式或参数：源码没有该开关，主初始化保留既有 standalone 管理员、默认组织/岗位/授权种子；私有模板没有 core 菜单/日志/种子。

原 16 份 SQL 的精确前像和 hash 保存在 `.gradle/single-init-20261031/before` / `before-migrations.json`，不覆盖旧验证证据。已移除 13 个主增量（包括本地未执行的 V14）和租户 V2。独立重复的 `docs/sql/system_management_full_postgresql.sql` 及依赖旧 V12 的 `scripts/AccountProfileMigration.java` 已移除；账号文档改为引用唯一初始化入口，不能继续提供已删除升级脚本的执行指引。

## 逐版本去向

| 原版本 | 最终去向/锚点 | 不保留的历史操作 |
| --- | --- | --- |
| 主 V1 | 原树表/字典主明细/角色关系/审计列、兼容视图、默认组织岗位和管理员；账号直接最终分表 | 不再创建 SYS_USER；独立重复 docs SQL 移除 |
| 主 V2 | SYS_ROLE.TEMPLATE_ROLE_ID/SYSTEM_ROLE；SYS_TENANT_ROLE ACTIVE=false/GRANTED_AT/REVOKED_AT/STATUS_REASON；SYS_TENANT_PACKAGE_GRANT 与 active/template/system 唯一索引 | 旧授权先撤再回填、已有租户管理员派生及能力传播循环；新空库没有这些历史租户行，不默认新增授权 |
| 主 V3 | SYS_TENANT.SCHEMA_NAME VARCHAR(63)、安全名称 CHECK 与全局非空 schema 唯一索引；函数取原 V13 最终版本 | 被 V13 替代的 CREATE SCHEMA IF NOT EXISTS 宽松函数 |
| 主 V4 | LOG_BIZ USER_ID/SUCCESS、三日志 TENANT_ID NOT NULL DEFAULT0；LOG_BIZ_ARCHIVE 完整字段和两表八个复合检索索引 | NULL tenant 旧数据 UPDATE 回填 |
| 主 V5 | core SYS_NOTICE 及类型/时间索引；私有模板同名私有表保持 | 无 |
| 主 V6 | 私有模板直接有 SYS_NOTICE 和索引 | 扫描登记历史租户并跨 schema 补建通知表的循环 |
| 主 V7 | API1401–1413 原 endpoint/method/id；原 ROLE_ADMIN 资源授予统一在原 V1 管理员种子处完成 | 迁移时按已有模块资源传播其他角色/租户能力；空库没有该历史集合 |
| 主 V8 | LOG_ERROR 最终 fingerprint/type/summary/trace/time/count/status、tenant fingerprint 唯一和时间索引；没有 EX_DESC | 旧 EX_DESC 摘要回填/历史 fingerprint 构造/删旧列过程 |
| 主 V9 | SYS_LOGIN_LOG 最终 session/device/os/browser/risk/activity/logout 列及两索引（原 V1 已有） | 重复 ADD COLUMN |
| 主 V10 | API1414–1416 三个精确平台 GET 资源；仅种子 ROLE_ADMIN 授予，无租户传播 | 重复平台 grant 语句合入统一种子集合 |
| 主 V11 | 不创建 BLACK_LIST | 无需 DROP 不存在的遗留表 |
| 主 V12 | 直接 SYS_ACCOUNT（保凭据/审计/状态/联系信息/权限版本）+可选 SYS_ACCOUNT_PROFILE（ACCOUNT_ID PK/FK、CODE/NAME/DEPT_ID 及索引）；管理员资料单独绑定原 ID | SYS_USER 重命名、跨租户扫描、复制资料/数目回填与旧索引重命名 |
| 主 V13 | tenant CODE NOT NULL、0号eva/正租户短code canonical CHECK；CODE全局唯一（含已删），SCHEMA_NAME全局非空唯一；严格 EVA_PROVISION_TENANT_SCHEMA（同名拒绝） | 数字旧 schema 重命名、元数据猜前缀/更新、ACCESS EXCLUSIVE 历史扫描 |
| 主 V14 | SYS_DICT.CATEGORY VARCHAR(16) nullable + CK_SYS_DICT_CATEGORY（NULL/SYSTEM/BUSINESS）；八内置主项直接 SYSTEM | 独立增量文件、开发 QA 具体 ID 分类及旧库 UPDATE；无持久虚根 |
| 私有 V1/V2 | 私有单模板直接 account/profile、组织/岗位/字典/关系/角色/通知；CATEGORY 结构一致；全结构完成后记最终版本1 | SYS_USER 及 V2 拆分、顺序升级；既有非1账本/旧账号结构拒绝自动重新初始化 |

已批准菜单例外同时落实：新库直接种登录叶 ID1000000000000000204，route=/log/login、pid200、path200/204、component=./log/login、sort4；既有 API1407 归属204，不新增 MENU grant 或向其他角色传播。依据已批准 `2026-10-30-login-log-menu-plan-backend.md`，不是读取或修改真实开发库。

八内置字典主项 SYSTEM，明细仍在 SYS_DICT_ITEM，无 QA 样本、SYSTEM_DICT 根或把历史明细作为主项恢复。用户已明确批准新库 DATA_SCOPE 仅预置五个已实现范围，按当前枚举对应 0000/0001/0002/0003/0007；不预置 0004/0005/0006 三个岗位范围。该项是本次独立确认的默认种子政策变更，不是从开发库R包删除状态推断；不删除枚举、不实现岗位权限或修改现库。原八值源码仍在隔离前像，实库新测试按完整五码集合断言。

现存表名 SYS_DICT_ITEM/SYS_MODULE_RESOURCES/SYS_TENANT_RESOURCE 等虽有历史命名不一致，本次不擅重命名物理表或 Mapper，避免破坏当前 A/B/权限；没有新增业务表分类或新名字。已读取 AIBrain 顶级 `_line`/`_ref` 规则，存量调整需另列影响批准。

## 调用与配置保护

provisioner 保留可信 core 元数据锁、数据库 code、现映射权威、占用检查、严格建 schema 及事务 search_path 恢复。账本为1才允许读取最终 account/profile；含旧升级版本、SYS_USER、无账本既有账号或异常账本时拒绝，不自动补列、恢复或重跑模板。新 schema 只跑一个最终模板，不再读取租户 V2。

dev/aliyun/test-dev 原 baseline-on-migrate=true 已改 false，enabled=false 和数据源/其他配置不动，避免非空无history库误开启后自动baseline0并套用新V1。实际环境覆盖仍须独立核定；本任务没有执行或伪造 baseline/history。主/私有初始化均不是旧库升级路径。

旧开发库 V1 checksum、后续 V2–13 history 将与新单 V1 不匹配；无history但非空库也不得自动 baseline。用户必须另行选择并批准旧库保留/另建或受控升级方案。禁止 clean、repair、删除 history、drop 数据库、静默重建或启动新产物自动处理。旧 V14 执行提案已标退役，其源 seal 会因旧路径变化拒绝，不再准备或运行。

## 测试收口与当前状态

原 7 个增量/历史迁移测试（业务归档、错误聚合、租户通知/API升级、V13重命名、账号拆分、平台inspect、11→13真实链）不再保留已删除路径或旧版本数量。以 `SingleInitializationPostgresTest` 三个真实合成PG用例替代：官方13.12仅1/validate/二次0；最终账户/日志/授权/字典/约束/索引及已批菜单种子；真实旧history校验失败和非空无history拒初始化；实际provisioner最终模板/私有A/B隔离/版本1幂等/旧账本拒绝。现有租户路由测试改读取新主初始化，不再拼 V3/V13。授权SQL回归保留撤销/联合/租户裁剪/权限版本事务测试，仅将fixture从旧迁移生成改成新初始化最终结构。

`PostgresInstanceEvidence` 固定标签改为 init-empty/init-history/init-tenant，其余既有路由/权限标签保持。仍必须以受控临时目录、loopback自有合成PG和现有证据协议运行，不能连接dev/生产或把源码contains断言称实库测试通过。

当前候选尚未完成最终验收。已经实际执行四轮隔离验证，原日志/XML/进程记录均保留在 `.gradle/single-init-runs`：121646Z-ce9c86a9 首轮缺少测试 List 导入导致编译失败；122031Z-6de54302 五类18项中17通过，授权fixture漏填套餐CODE/NAME导致一项失败；修正fixture后的123744Z-d55a7473单授权1/1通过，但没有取得完整OS监听归属，整轮仍失败；125255Z-7af920cb窄诊断在早期RUN postgres祖先链不完整处停止观察，十秒握手超时，一项失败。第二轮三个新初始化用例确已通过，但不能替代最终整轮验收或OS归属门禁。

7af920cb保留的早期PID1620/start12:54:44.730519Z/parent47724候选没有data和监听，且父实例没有被封存；采样结束12:54:47.367820Z早于JDBC正式postmasterStart12:54:48.782637Z，不能把它指认为网络postmaster或追认bootstrap角色。所有已执行轮次终态无残留、生产输入无漂移；没有强杀、连接真实开发/生产库或启停应用。旧B66/旧Flyway13.11证据只属旧HEAD，不能称新064ed4f9及本次合并通过。

总控随后批准等价交叉归属标准：保留早期UNKNOWN和旧祖先链错误；正式服务必须同时满足本RUN pidfile与OS server PID/start/native hash/data/loopback监听、持续只读技术连接的JDBC backend PID与同次OS backend PID/start/parent=server PID、JDBC nonce/version/data/start/port及终态无残留。不能用pidfile或JDBC单独兜底，也不声称恢复Test JVM祖先链。当前仅test-only Evidence/派生观察器最小候选及负向临时文件自检完成，待QA增量源审后才允许一次单授权诊断；成功再审定最终五类/六PG，不循环盲试。生产SQL/provisioner/配置不因该验证工具问题再改。

后续两次窄诊断也保留为失败。130832Z-b1599817未取得pidfile完整归属，观察结果返回前夹具已关闭；131604Z-1d58b2a4批量采样已及时保存pidfile原文，但实现曾错误要求pidfile与JDBC启动时间差小于一秒，因1.050226秒差拒绝。旧轮均无ack、无终态残留、无生产输入漂移，不作为通过证书。

该一秒条件没有源码依据，现按总控审定时间顺序替换：以整数UTC微秒计算，O为OS server创建时间，P为pidfile整数秒，J为JDBC postmasterStart，B为同次OS backend创建时间，S为该payload完成时间；要求O<(P+1)秒、P秒<=J且O<=J<=B<=S。PG14.22在早期初始化MyStartTime并写入pidfile，较后另行设置PgStartTime，因此J-P没有固定一秒上限。源码依据：[早期时间与稍后PgStartTime](https://github.com/postgres/postgres/blob/REL_14_22/src/backend/postmaster/postmaster.c)、[pidfile写MyStartTime](https://github.com/postgres/postgres/blob/REL_14_22/src/backend/utils/init/miscinit.c)、[SQL函数返回PgStartTime](https://github.com/postgres/postgres/blob/REL_14_22/src/backend/utils/adt/timestamp.c)。其余PID/hash/data/port/nonce/backend父边及十秒握手不变。

131604真实旧样本完全没有request对应backend，离线重放只返回PENDING_PAIR且不发ack；明示synthetic完整pair正例与逐条件负例仅用于验证门禁实现，不能补成运行证明。最终新工具single-init-final固定五类18项和六标签/六数据目录/六完整交叉归属，待QA绑定增量源审后由总控授权只运行一次最终五类/六PG；不再追加单PG重复fresh编译。最终真实RUN缺任一条件即失败，不能拼接旧轮或合成记录。

最终工具经QA独立源审及纯离线自检通过后，唯一最终RUN `20261031T132455Z-638bc62c` 已自然完成：Gradle exit0，BUILD SUCCESSFUL in3m28s、32项任务全部实际执行；Identity7、SingleInit3、SchemaMode5、AuthorizationPostgres1、Contract2，共18项，failure/error/skip全0。六实例init-empty/init-history/init-tenant/init-authorization/mode-provision/mode-route均有本轮真实JDBC、request、ack、pidfile原文及同批OS server/backend交叉证明，六数据目录互异，输入无漂移、observerError=null、终态无残留。12条旧祖先链失败与全部UNKNOWN原始候选继续保留，未伪称恢复该祖先链。本地新门禁passed=true；证据已交QA独立后验，独立验收与生产部署不是本地通过自动完成。实际开发/生产库、历史、应用与服务均未操作。
