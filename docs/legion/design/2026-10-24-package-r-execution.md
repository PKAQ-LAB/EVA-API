# 包 R 执行记录

日期：2026-10-24；唯一 R 数据执行者 backend_developer。**数据与缓存执行已完成：SQL 实际提交六行、唯一缓存键比较前值后失效；应用回源及页面五项验收待 QA。** 第 1–4 节保留首次写前停止的历史证据；用户后续已明确确认 effective Redis DB0、默认 namespace、无运行覆盖，原来源缺口解除。最新实际结果见第 6–8 节；不把数据/缓存成功当成 QA 五选项已经通过。

## 1. 已有批准与复核

已完整读取 `2026-10-24-data-scope-dictionary-repair.md` 第 8 节、第 9 节批准记录及本人实际只读诊断。用户批准的差异仍仅为 eva.sys_dict 主项 1000000000000004001、eva.sys_dict_item 明细 1000000000000004002、1000000000000004003、1000000000000004004、1000000000000004005、1000000000000004009 六行 DELETED 1→0；FROZEN=0 及其余列、0004/5/6 对应三行保持。未扩大到根、归属、其它字典或角色授权。

9016 仍由原 PID47664/2026-10-24 10:08:31.509+08:00 的 WebBooter 监听。当前 cornerstone、index 空，既有 dirty 文件保持；没有源码写锁，未构建测试、修改服务/配置/前后端源码/依赖、发 HTTP、创建包 S 对象或授予权限。

已知实际 classpath 中 application-dev.yaml 与源副本相同，Redis 配置声明 DB=0、port6379；认证与地址仅内部从该既有配置取用，没有输出/持久化/命令行传递。运行 JVM 的只读 VM.class_hierarchy 确认 RedisCacheManager 类已加载，当前源码默认 cacheName=dictdata、无外层 schema 上下文时逻辑键 DATA_SCOPE、默认物理候选 `dictdata::DATA_SCOPE`。**类已加载不等于已证明它是当前实际 CacheManager Bean；配置 DB=0 和源码默认也不等于实际应用连接所选 DB/namespace。**

## 2. 限定只读 Redis 预检及停止条件

采用本地已有 Python 运行时的受控维护客户端读取同配置 Redis 服务的连接技术元数据，不安装依赖、不新增 HTTP 接口。仅内部 AUTH/SELECT 配置 DB 后读取 CLIENT LIST TYPE NORMAL，将返回元数据立即按该 PID 已建立的同 Redis 端点连接的本地源地址/端口筛选；不输出或保存其它客户端、地址、认证、用户名。没有 KEYS、SCAN、任何其它 key 读取或缓存写。

首次严格地址/端口对应未通过，已立即向总控报告；随后仅排除 IPv4/IPv6/IPv4-mapped 规范表示差异，没有降低为只看端口即信任、尝试其它 DB 或其它前缀。第二次结果 UTC 2026-10-24T04:05:45.789804Z（北京时间 12:05:45）：

| 允许的技术结果 | 结果 |
| --- | --- |
| PID 当前同配置 Redis 端点的已建立连接 | 2 |
| Redis 客户端记录中源端口单独相符 | 2 |
| 地址规范化后，源地址及端口同时严格相符 | 0 |
| 文件声明的 Redis DB | 0 |
| 可严格归属到该应用的实际 Redis DB 集合 | 未证明，空匹配集合不能当成 DB 无数据 |
| 唯一源码候选 key | dictdata::DATA_SCOPE |
| 对候选 key 的 TYPE/GET/DUMP/PTTL | 均未执行 |
| Redis key 失效或其它变更 | 0 |
| R 数据库连接/新前像查询/更新/提交 | 均未执行 |

端口相符而地址不同可能涉及代理/NAT或其它地址关系，但本轮没有可靠证据认定原因。不能拿端口相符、源码 DB0、PG schema=eva 或 CacheManager 类已加载补足实际来源证明，因此执行包 R 明确的**数据库写之前停止**门禁。没有凭猜测读取候选键、切 DB 试探、尝试 `eva::DATA_SCOPE`、删除多个 namespace 或继续 SQL。

本轮读取范围只有同服务连接技术元数据，未查询 Redis 任意业务键、其它字典映射、个人资料或日志。连接正常关闭；没有比较失效、EVAL、DEL、FLUSH/init/removeAll/改 TTL 或 Redis 恢复动作。

## 3. 受保护本地记录与备份状态

本地目录 `.gradle/package-r-20261024/` 已先经绝对路径核对位于当前工作区 `.gradle` 下并确认 Git ignored；首次创建，不覆盖旧备份。目录 ACL 取消继承，仅当前执行用户与 SYSTEM 两条 FullControl 规则，后续文件继承该保护。未把任何凭据写入文件。

当前只存在 `preflight-stop-metadata.json`，内容为上表允许的计数、候选键、停止原因 `actual_app_database_not_proven_after_address_normalization`、dbWrites=0/redisMutation=0/candidateKeyRead=false 及时间；它是停止证据，**不是 SQL 行前像或 Redis key 备份，不可用于回退任何数据**。文件 SHA-256 由本轮收尾校验记录到 backend status；未复制到 Git/vault，不导出任何旧字典值、连接凭据或地址。

没有完成实际单键前值/TTL/DUMP 验证与备份，所以没有宣称具备可执行的比较失效或回退快照；也未先恢复六行后临时寻找缓存维护入口。此前只读诊断仍是历史行证据，本轮未用其假装执行前新鲜前像。

## 4. 失败收尾及继续所需的最小证据

由于所有写均停于前置之前，本轮无需数据库回退、缓存恢复或权限回收；六行 executedIDs 为空、SQL affected=0、缓存 mutation=0。不存在本包提交后跨 DB/cache 的半完成结果，不宣称五选项已经恢复。

继续 R 需要取得**当前应用实际 Redis DB 与 namespace 的可追溯归属证据**，例如合法实例管理信息证明这些代理/地址不同的现有连接确属该 PID 且实际选 DB0、当前 CacheManager 使用此单键；或者由已获准且能核实实际 Bean/context 的受控维护入口执行同一来源的单键操作。不能仅重申文件 DB0 或端口相符，也不要求聊天提供凭据/整份环境。

取得证据后仍须先只读核唯一键及可比较失效能力，私有目录保存单键值/DUMP/TTL及六行全关键前像，重新核当前九行与触发器/规则边界；任一前值变化停止，不复用旧行快照盲写。只有这些前置成立后，才能按既有批准在单事务条件更新 1+5 行、完整字段前后比对、三排除行不变、提交后单键比较失效。缓存并发变化或失效失败须暂停 S 并报告，SQL 回退仅在六行其它字段仍保持本包结果时条件执行，不能覆盖并发修改或盲回灌旧空 map。

本轮所有动作结束，已交总控处理该精确运行证据缺口；不重新请求包 R 总授权，不自行解除原安全前置。

## 5. 用户确认后的续核与写前失败收尾计划

总控转达用户对唯一实例缓存卡明确回复“确认”：当前应用实际使用既有配置 Redis DB0，没有自定义缓存前缀、代理路由或运行时覆盖。因此来源认定使用操作者的 effective 确认结合现有源码路径，保留第 2 节地址不匹配观察，不再用同一缺口阻塞或猜测 NAT。续核仍原 9016/PID47664/10:08:31 启动实例，配置与源相同，私有备份目录 ACL/ignored 状态不变。

UTC04:17:43.287341Z（北京时间12:17:43），仅 DB0 的 dictdata::DATA_SCOPE 实读为 string，PTTL=-1，值4字节，解码结构为空 Map；没有其它 key/DB/前缀读取。单键 DUMP/值及元数据已在私有 ignored 目录独占创建，未覆盖旧文件：

- redis-before-value.bin，SHA256=c89a148be40e6752261e3038609a4b68de22fa3bfdaf32f884edffb8480b9bbe。
- redis-before-dump.bin，SHA256=c45d1df7d48973669f8c15c63ce09d649a202872834eb96d87c79342c93f4b1c。
- redis-before-metadata.json，SHA256=261381384ae942c6f2540c3850926c201247cc591b6e15ffda180afc83b5f931，记录 DB/key/TYPE/PTTL/结构及指纹，不含认证/地址。

已运行同一精确单键 Lua 的 probe 分支：TYPE、GET、DUMP、PTTL 比较前值成功，返回1，不执行DEL；ACL DRYRUN 核实同客户端可以删除这个单键。正式分支只在值/DUMP/无过期状态与备份相同才 DEL 该 KEY；不存在返回0、不造删除结果；前值/类型/TTL变化返回负值并停止，不盲删。没有全缓存 init/FLUSH、额外前缀或新端点。

UTC04:18:43.221134Z（北京时间12:18:43），对同开发eva/eva用只读事务重新采集主1+关联8行的完整 to_jsonb 前像；九行所有此前已知关键字段与批准快照一致，完整字段含版本/审计列仅保存在受保护本地文件。两目标表无启用自定义触发器、无rewrite rules、无RLS；没有潜在已知审计列自动变更被默许。已rollback/close，写0。pg-before-full.json SHA256=f6cf2bef3cc78d800c8d275439d6bc13fa1780b1a57ad64d85b5199781aa3961。

写入与失败处理计划已在提交前明确：事务内重核这些完整前像及唯一性、元数据副作用；每条UPDATE同时匹配ID、DELETED=1和完整旧行JSONB，仅SET DELETED=0，main必须1行、items必须5行、实际ID集合精确六行。写后比对完整九行只允许所选六行deleted变化，三排除行和所有其它字段不变；任一不符或备份落盘失败则提交前rollback，不触缓存。提交成功后再读回全部九行确认，再对备份前值原子比较失效唯一键。

DB提交与缓存不原子：提交结果不明时先停止并核实际行，不当作成功去删缓存；缓存前值并发改变或失效失败时不盲删、不回灌旧空Map，立即报告六行实际提交状态并暂停S。SQL回退仅可对本包六行、且其完整当前其它字段仍等于本包后像时将deleted精确0→1；不覆盖任何并发改动。若缓存已有并发新值，不能仅回退DB却宣称全局一致，须将实际残留与精确恢复条件交总控处理。未到失败分支不执行回退/RESTORE，旧缓存DUMP仅作受控证据，不自动覆盖并发键。

以下继续记录实际SQL、缓存和回源结果；此节写前检查通过仍不等于已完成恢复或页面验证。

## 6. 六行实际事务执行与完整字段验证

写前再次对缓存原前值运行不含删除动作的 probe，返回1。数据库仍使用同配置、同进程已建立连接对应的开发 eva/eva；先只读复核指纹，随后仅该维护连接开启 SERIALIZABLE READ WRITE 事务，未修改应用配置或其他会话默认值。事务内再次确认无启用自定义触发器/rules/RLS，并将主项 code=DATA_SCOPE 或指定ID、关联 MAIN_ID 的全部九行与新完整备份逐字段比较，相同才继续。

实际绑定参数执行的两条更新形状如下；完整旧行 JSON 参数只保存在私有备份，不写入本文件：

```sql
UPDATE eva.sys_dict AS d
SET deleted = 0
WHERE d.id = %s AND d.deleted = 1 AND to_jsonb(d) = %s::jsonb
RETURNING d.id;

UPDATE eva.sys_dict_item AS d
SET deleted = 0
FROM jsonb_array_elements(%s::jsonb) AS b(value)
WHERE d.id = (b.value->>'id')::bigint
  AND d.deleted = 1 AND to_jsonb(d) = b.value
RETURNING d.id;
```

第一条参数仅主项完整前像，第二条数组恰是五个获批明细的完整前像。实际 RETURNING/affected 为 main=1、items=5、total=6，实际ID集合与批准集合完全相同：

| 表 | 实际 executed ID | code | DELETED 前→后 | FROZEN 前→后 |
| --- | --- | --- | --- | --- |
| eva.sys_dict | 1000000000000004001 | DATA_SCOPE | 1→0 | 0→0 |
| eva.sys_dict_item | 1000000000000004002 | 0000 | 1→0 | 0→0 |
| eva.sys_dict_item | 1000000000000004003 | 0001 | 1→0 | 0→0 |
| eva.sys_dict_item | 1000000000000004004 | 0002 | 1→0 | 0→0 |
| eva.sys_dict_item | 1000000000000004005 | 0003 | 1→0 | 0→0 |
| eva.sys_dict_item | 1000000000000004009 | 0007 | 1→0 | 0→0 |

提交前再读完整九行，与“原完整前像仅替换这六个 deleted 值”的后像完全相同；没有版本、审计/创建/修改字段、name/type/code、D_CODE/D_VALUE、sort、tenant、main_id、pid/path/isleaf 或其他列变化。三排除项 1000000000000004006、1000000000000004007、1000000000000004008 保持 DELETED=1/FROZEN=0，完整行不变。先独占保存预期完整后像成功，才提交。

SQL提交成功后，另起只读事务再次读完整九行，仍与该后像完全相同；按当前主项/明细已删除及冻结过滤逻辑的限定 SQL 返回 code 恰为 `0000,0001,0002,0003,0007`，SORT/ID 顺序保持。复核事务只读 on，rollback/close。完成证据时间 UTC2026-10-24T04:21:46.414961Z（北京时间12:21:46）。这是数据库逻辑查询证据，不是经过应用数据权限/CacheManager的真实 queryDict 响应。

## 7. 提交后唯一缓存键的原子比较失效

UTC2026-10-24T04:22:22.229622Z（北京时间12:22:22），只在用户确认的同 Redis DB0 对 `dictdata::DATA_SCOPE` 执行一个 KEY 的 Lua 正式分支。脚本先比较 TYPE=string、GET原值、DUMP字节与备份一致、PTTL仍无过期；任一不符返回拒绝，不删除。实际比较成功，DEL返回 **1**，表示仅这个键由本包删除；没有其它 KEY、前缀或 namespace 被处理。

随后仅复读该键：TYPE=none、PTTL=-2；不是以“原本不存在”凑删除结果，也未手工回填五项、RESTORE旧空map、改TTL、重启、全局清缓存或调用任何写接口。Redis连接已正常关闭。正式结果文件 `redis-eviction-result.json` 保留返回码和复读状态，`applicationQueryDictAndUiPendingQA=true`。

本轮没有发 HTTP、借用或传递管理 Token、打开 UI 或创建测试角色。请 QA 在既有合法管理员会话正常单项 GET 触发 queryDict 回源，再关闭重开新增角色表单；验恰好五个 code、0004/5/6不出现、无默认0000、空/失败状态和原守卫保留。SQL五项和键已失效不能替代这一步。也不能用普通受限主体先生成共享空缓存来证明管理员回源。

## 8. 私有备份清单、恢复界限与交接

目录 `.gradle/package-r-20261024/` 在当前 workspace 内，Git ignored，ACL仍受保护、仅当前用户及SYSTEM；逐文件权限核查均两条规则。完整行审计字段或缓存原值只在此目录，不进入 Git/vault/聊天或公开附件。`backup-manifest.json` 逐项列8个历史/前像/结果文件的字节数与SHA256，其自身 SHA256为 **38798B01328AFF83F6935162A517B8FAF5B0ED9A224349E6800068A6A4E38BC1**。

| 本地文件 | 用途 | SHA256 |
| --- | --- | --- |
| pg-before-full.json | 主1+明细8的完整旧行及指纹 | F6CF2BEF3CC78D800C8D275439D6BC13FA1780B1A57AD64D85B5199781AA3961 |
| pg-expected-after-full.json | 仅六行deleted变化的九行完整后像 | CE87396F90F76ED9023EDCFD6708DC7389048FA50ED6789C58BC98BD5552D003 |
| pg-commit-result.json | 1+5 affected、实际ID、全字段/只读复核结果 | 0902298771174032EA7AD5BEC399E429CBA405FEB03E37DD4E6F63488D6D6173 |
| redis-before-value.bin | 精确原缓存值，不在此展开 | C89A148BE40E6752261E3038609A4B68DE22FA3BFDAF32F884EDFFB8480B9BBE |
| redis-before-dump.bin | 精确单键DUMP，不在此展开 | C45D1DF7D48973669F8C15C63CE09D649A202872834EB96D87C79342C93F4B1C |
| redis-before-metadata.json | DB/key/TYPE/PTTL/结构及值指纹 | 261381384AE942C6F2540C3850926C201247CC591B6E15FFDA180AFC83B5F931 |
| redis-eviction-result.json | 原子返回1及TYPE none/PTTL-2 | E57A02DB9E73F4599E46E8E7CB1F18A176006E8D91B47EB9793EF564E464F716 |
| preflight-stop-metadata.json | 首次停止的历史技术证据 | A85FC699286A23DC00F7C5AAF3EA43D803D6BCAEAF1C463548269626753700A2 |

本轮没有触发失败回退。若后续明确要求回退，只能重新核同一实例、备份哈希及本包六行当前完整后像，按ID+DELETED=0+完整本包后像条件将这六行DELETED恢复1；affected必须1+5，其它字段及三排除行保持，任何并发变化停止而不覆盖。不得将“完整前像备份”解释为能用全行覆盖/upsert恢复。回退后仍须处理同一缓存来源的真实当前单键，不能将旧空Map DUMP不加判断RESTORE回去；缓存应按获准当前DB状态定向失效后由合法应用重新生成。包S后续对象/授权不属于R回退对象，须由总控重新核进展。

六行与单键本次执行已完成，所有DB/Redis连接及命令结束，数据执行窗口交回总控供独立QA；本成员不创建包S对象或授权。未改源码、前端、依赖、配置、角色dataScope/资源/用户关联、根/旧叶/其它字典/schema/表结构，没有服务启停、构建测试、提交push或vault写入。未声明全系统权限或页面矩阵通过。
