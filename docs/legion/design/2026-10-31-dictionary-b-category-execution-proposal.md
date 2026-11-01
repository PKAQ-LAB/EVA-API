# 字典 B：受控 dev 分类实施与回退提案

已退役：用户随后要求发布前只保留一个生产 Flyway 初始化脚本。本提案的 V13→V14 前置、13.11.1 工具封存和命令不再适用，禁止执行；仅保留为未执行历史。当前任务是新库单 V1 整合，既有数据库处理须另行批准。

2026-10-31。用户已确认映射：1000000000000004001/4010/4020/4030/4040/4050/4060/4070 对应八个内置字典为 SYSTEM；2111791956297613314 / EVA_QA_20261018_D01 为 BUSINESS。其余 38 条非有效历史主项保持原状、不恢复。本次授权仅确认映射和离线准备，尚未授权任何实际数据库连接、DDL、DML、服务启停或缓存操作。

本提案不修改已冻结 B 源码、V14、Serializer 或 code KV 缓存；不进入 C 分源。旧只读候选清单中的 UNKNOWN 状态已被本次明确决定取代，不再询问归属。

## 冻结材料与实际可执行入口

- B 源码清单仍为 `.gradle/dict-b-20261031/manifest.json`，SHA256 `3C2C96FA4500C5A1D8890803E0C572AA18D7F3722D14B2B7609EE1A3209198DE`。
- 当前 V14 只有 VARCHAR(16) nullable CATEGORY 列和 SYSTEM/BUSINESS/NULL 合法值 CHECK；SHA256 `B2D83145F1471BAB9943BC99C3F502DF07BB6F270FEB3C68E2A03CD09C6C05C9`。不加入开发 QA 的具体 ID，不回放 V1。
- `sql/2026-10-31-dictionary-b-category-preflight-proposal.sql`：实施窗口内的结构前只读检查和全字段指纹 seal。
- `sql/2026-10-31-dictionary-b-category-forward-proposal.sql`：正式 V14 成功后，独立分类事务。
- `sql/2026-10-31-dictionary-b-category-rollback-proposal.sql`：匹配已提交后像才回退同九行分类，保留列及 Flyway 历史。
- `sql/dict_b_category_proposal.py`：单任务维护入口，默认只验本地 seal；实际 apply/rollback 必须显式执行开关与批准停写窗口编号。独占新 RUN、凭据仅在进程内、受限日志、CSV 持久化和提交检查均在此入口落实，不交给注释约定。
- `sql/DictBMigrationProposal.java`：只调用正式 Flyway 13.11.1 API 的维护入口，不启动 Spring。使用现有本地缓存依赖，不下载、不装 CLI、不运行旧验证套件。
- `sql/2026-10-31-dictionary-b-execution-seal.json`：逐文件源码/SQL、B 清单、14 个迁移文件和现有 118 个依赖 JAR 的 SHA256 与精确 classpath；SHA256 `B616B200491E3D406EB7B7253BBD4831EB8EA5F2E29D3F4D1177279882945256`。依赖来自既有封存 booter classpath，加本地已存在的 Flyway core/PostgreSQL 13.11.1 JAR，Java argfile 不含凭据。

当前 PATH 未发现 Flyway CLI，因此不提供虚构的已安装 CLI 命令。正式 validate/info/target14/migrate 由上述已封存官方库入口完成；本次仅静态读取本地 JAR API 签名，未编译或运行 Java、未启动维护连接。Python 仅作离线语法检查；不声称执行 SQL、Flyway 或数据库验证通过。

## 已知前像与尚未读取的事实

一次既有 RO 快照窗口为 2026-10-31 15:31:03.483415 至 .591310 +08，结果 SHA256 `F7FFB2A55613441197D5E1F671B903E12B6B8758201B1144537A28C6AE614AF0`。当时 dev 与 prod 区分、源/构建 datasource 一致、eva/eva 两表实源、transaction/default RO on/on，1 connect、0 写、rollback/close 完成。47 行元数据详前置清单；CATEGORY 列当时不存在。DATA_SCOPE 当前 deleted=0，明细总 8、有效 5，不沿用旧 deleted1 前像。

该快照没有读取 Flyway history、触发器、RLS、规则、DDL event trigger 或授权表指纹，也没有读取完整字段正文。不能把它们写成已通过。实施前获批窗口内的 fresh RO 才会检查这些事实、保存 history 技术元数据、生成哈希；若异常停止，不尝试 repair、baseline、伪造 history 或覆盖现值。当前 SQL 对保护表要求 eva 普通表，缺表、视图、分区或来源不符均停止交审，不能自动略过。

## 停写与启动边界

总控须先批准具体字典停写窗口、操作进程/路由与实施账户，阻断旧代码和 B 代码的字典写入口及并发迁移；停服务或阻断入口的方式不由本提案自动执行。不让旧客户端在迁移间隙写入，也不通过篡改用户/角色/授权制造窗口。

源码 `application-dev.yaml:33–37` 的 Flyway 默认 enabled=false，baseline-on-migrate=true，locations 为 classpath:/db/migration；实际启动参数、环境和外部配置仍可能覆盖。实际批准前不启动新 B 产物、不启用迁移配置，不假定“默认 false”能保证所有运行环境无自动 DDL。维护入口显式 baselineOnMigrate(false)、cleanDisabled(true)、outOfOrder(false)、target14、skipDefaultCallbacks(true)，不修改配置文件。

新 B 代码依赖 CATEGORY，必须等结构、全部九行映射、独立后验和常规部署验收准备就绪后再另行安排启动。当前旧服务的实际加载版本尚未由本提案证明；不借分类实施自动重启、切换或修复其他模块。

## 正向阶段与硬停止条件

1. 先获实际执行批准，再使用固定 cwd `C:/home/workspace/lab/EVA-API`。入口核批准 seal 的外部 SHA、所有文件/JAR、精确迁移目录清单和 B 的 inputs/support/14 保护文件；漂移即停止。不覆盖或重新计算 seal 来迎合当前文件。
2. 入口创建 `.gradle/dict-b-category-execution-<UTC>-<UUID>` 全新绝对 RUN，mkdir(exist_ok=False)；所有输出用独占创建模式，不复用旧目录、不删旧文件。SQL 中固定逻辑 receipt 路径由入口映射到该 RUN 的绝对路径，不依赖调用者 cwd、不用 psql 直接执行固定路径脚本。
3. 从既有 datasource 解析函数读取 dev/prod，核批准开发目标指纹、eva/currentSchema、无未解析变量、源/构建 datasource 一致；不输出 DSN/用户名/口令，不连接生产。RO 连接 default/transaction 均 on，REPEATABLE READ；只做技术元数据和 hash 取证。
4. 结构前 SQL 核 CATEGORY 不存在、47 行精确 id/code/type/frozen/deleted 与已批前像完全一致、有效 code 唯一、两字典表及 11 个保护表真实来源；历史最新成功版本必须 13，无失败或已有 14。存在活动用户数据触发器、规则、RLS、DDL event trigger 则停止审查。保存 history 的 installed_rank/version/type/script/checksum/success；不输出人员/业务正文。
5. RO 在停写窗口内为全部 47 主项的除 category 字段、所有明细及保护集合生成 count + 全行 MD5 摘要，导出 `preflight-seal.csv`，flush/fsync 后记 SHA256。MD5 是字段不变比较指纹，不作为抗攻击证明。RO rollback/close 后才进入 Flyway；不把此只读事务延长为写事务。
6. 官方 Flyway 先 validateWithResult 和 info：current=13、仅 pending V14、脚本名准确，所有状态仅 SUCCESS/已有合法 BASELINE/PENDING；缺失、失败、超前、忽略、校验和不符或其他 pending 均停止。已有 baseline 历史必须作为 history 前像交审，不在此次创建 baseline。第二个官方入口在 migrate 前再次执行同样检查，只 target14；成功必须 migrationsExecuted=1、current14、pending0，重新 validate 成功。源码 V14 checksum 由官方 Flyway记录，不 INSERT/UPDATE/repair history。
7. V14 由 Flyway 自身事务提交，随后分类在另一个 SERIALIZABLE 事务执行；两阶段不是一个原子事务，不声称外部 DML 跟 Flyway 自动同事务。DDL ADD COLUMN 需要 ACCESS EXCLUSIVE；设置 lock_timeout=3s、statement_timeout=15s，锁超时或任何异常停止，不自动延长或重试。
8. 分类事务先主表 SHARE ROW EXCLUSIVE 防写，再对全部明细/权限保护表取 SHARE 锁防并发写，随后恰好九个目标行 FOR UPDATE。完整 47 行 metadata、NULL category 前像、列型/nullable/已验证约束与成功 V14 history 再核；与结构前 CSV seal 比较，V14 期间任意非分类字段变化即回滚分类事务。所有锁只在批准维护窗口使用。
9. 仅 UPDATE 九行 CATEGORY，条件同时匹配精确 id/code/type/frozen/deleted 和 category IS NULL，RETURNING 必须恰好批准九 ID/对应八 SYSTEM 一 BUSINESS；没有 upsert、恢复、插入根、改 code/type/frozen/deleted 或明细写入。其余 38 主项仍 NULL 分类，原非有效状态不变。
10. 在 UPDATE 前导出 `rollback-receipt.csv`：九行 id/code/type/frozen/deleted、before_category=NULL、approved_category 与全非分类字段 MD5。入口 exclusive-create、flush/fsync、九行完整精确校验、SHA256；更新后提交前再次读文件核 SHA 和九行，独占落盘 `before-data-commit.json` 成功后才 conn.commit。任何文件写入/hash/映射检查失败均回滚，不仅依赖“不得覆盖”注释。
11. 事务内核全部 47 主项非 category 字段逐 ID 指纹相同、38 非目标主项分类仍 NULL、有效项全部已分类；明细及所有保护集合 count/hash 相同。保护集合为 sys_dict_item、sys_role、sys_roleuser_ref、sys_roleres_ref、sys_role_resource、sys_postuser_ref、sys_tenant_resource、sys_tenant_package_resource、sys_tenant_role、sys_module、sys_module_resources；不读取或导出其正文。缺表即停止，不擅改范围。COMMIT 返回成功才标 CONFIRMED；发送 COMMIT 后连接异常标 UNKNOWN，不能以 rollback 尝试证明未提交。
12. 连接 rollback/close、Java子进程退出和结果状态落盘；报告中的 pythonConnections/pythonAllClosed 仅统计 Python 连接，Flyway 内部连接不伪报精确数量。提交后仍须总控安排一次独立限定 RO 后验及实际启动/接口验收；不自动调用 HTTP、不清全 cache、不据维护脚本成功宣称运行时已验。

## 命令提案（当前均不得执行数据库动作）

仅本地 seal 检查的命令默认不导入 psycopg2、不调用 Java、不连接 DB：

```powershell
python docs/legion/design/sql/dict_b_category_proposal.py --manifest-sha B616B200491E3D406EB7B7253BBD4831EB8EA5F2E29D3F4D1177279882945256
```

实际正向命令只在设计签审和用户实际执行批准、停写窗口已落实后，由总控以实际窗口号替换示意值；当前提供命令不构成授权：

```powershell
python docs/legion/design/sql/dict_b_category_proposal.py --manifest-sha B616B200491E3D406EB7B7253BBD4831EB8EA5F2E29D3F4D1177279882945256 --action apply --execute-approved --window-id APPROVED_WINDOW_ID
```

凭据仅由既有解析器在内存读取，官方库通过子进程环境获得，argfile 中仅为已封存 classpath、Java 文件和 inspect/migrate；库 stdout/stderr 被维护入口屏蔽，父进程只接受受限 JSON，不保存任意异常消息/DSN。没有安装新工具、启动应用或运行其他迁移测试。

## 故障与精确回退

V14 前失败：没有结构或分类写入，关闭连接，保留证据并停止。Flyway 可能已提交或确认 V14 后、分类未提交：列/history 可能已在，旧应用保持或恢复原批准版本，B 不开放；九行数据事务失败回滚，禁止自行 drop 列、repair history 或自动重跑。这种“已存在列”只有和本 RUN 的成功 V14 记录绑定后才可提出恢复执行方案；不能按初始脚本跳过漂移。

分类提交后需回退：先由总控批准停 B 写并切回已批准旧应用的具体顺序，再以实际正向 RUN receipt 绝对路径及其获批 SHA 执行 rollback 模式。入口校验原文件 SHA/九行，并独占复制到新的回退 RUN，复制后再核 SHA；目标九行必须仍是预期八 SYSTEM/一 BUSINESS，且全部非 category 字段与执行前/后指纹相同。任何目标被业务修改就停止另行审查，不能覆盖。

回退只将同九行 CATEGORY 恢复 receipt 中原 NULL，提交前核全主项集合和其他所有行逐字段不变、保护集合指纹不变；不会删除后续新建业务字典或恢复 38 条历史数据。由于九个有效项恢复 NULL，B 管理 list 会按前置门禁拒绝，所以必须先停用 B。默认保留 V14 列、CHECK 和合法 history，不 drop column、不手改 history；若将来要撤结构，须另行评估依赖并制作前向 Flyway 迁移，不在本回退范围。

```powershell
# 下列 receipt 与SHA必须来自实际获批正向RUN；不得使用示意值执行。
python docs/legion/design/sql/dict_b_category_proposal.py --manifest-sha B616B200491E3D406EB7B7253BBD4831EB8EA5F2E29D3F4D1177279882945256 --action rollback --execute-approved --window-id APPROVED_ROLLBACK_WINDOW --receipt ABSOLUTE_APPROVED_RECEIPT_PATH --receipt-sha APPROVED_RECEIPT_SHA256
```

回退不删除新业务记录、不清全缓存、不回放 V1，不改 deleted/frozen/type/code/明细/授权。schema 或 commit 结果不确定时，不根据本地异常猜未执行，先停并另批最小只读取证。

## 当前状态

仅离线提案、已确认映射和静态文件/API核验。未进行新数据库连接、Flyway inspect/validate/migrate、SQL执行、Java编译运行、服务启动停止或新重测试；实际 history/trigger/授权摘要仍是实施前待核事实。设计师需绑定上述 seal 和全材料签审后，总控再提交实际执行批准；不再问九行归属。
