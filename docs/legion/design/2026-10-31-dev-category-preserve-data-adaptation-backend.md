# 开发库保留数据：字典分类精确适配提案

2026-10-31。当前仅获准一次开发库只读取证、离线提案及必要专用执行载体SOURCE准备；本文件、两份SQL和载体均须绑定增量源审。没有实际下载、备份、DDL/DML、Flyway历史操作、数据库恢复、服务停启、HTTP、Redis或Git发布授权。不得将先前单初始化通过理解为现库已迁移。原EED995版本保留，修订仅收口QA完整约束mustfix与执行前置，不重跑已绿测试或视觉补验。

## 最新事实与边界

只读结果为 `.gradle/dev-category-adaptation-ro-20261031T134806Z-fe20cfc2/result.json`，SHA256 `73AF8FEAB6517FF2562C882929949AE35AF485D3D1CD5125C098E5EEBBB3864C`。窗口为北京时间21:48:07.526505至21:48:12.550380，单次连接、27条SELECT、default/transaction只读均on、REPEATABLE READ，rollback/close完成、写0。工具源604E550F经QA安全审；独立报告核验9A5767F0通过，未再连库。原单初始化清单F604BC56及14个保护文件不变。

开发库目标eva/eva、源与构建datasource相同且与prod区分，必要表的qualified与unqualified来源均eva普通表。服务器版本180003，即PostgreSQL 18.3。CATEGORY列及分类约束不存在；默认表名flyway_schema_history的全schema技术目录查询为空，本次没有该表的历史行、版本或checksum可保全，绝不是已成功运行V13。该证据不排除尚未核定的运行实例使用自定义历史表或配置覆盖。保持现状，不创建/插入/repair/baseline/history，不执行新的单V1、已删除增量或退役V14工具，也不新增生产Flyway版本。

47条主项的id/code/type/frozen/deleted与此前只读前像逐项一致：9条有效、38条非有效，有效code无重码；完整47行技术前像在结果JSON和两份SQL的VALUES中。8条内置主项映射SYSTEM，原开发QA主项映射BUSINESS；其余38条保持原deleted/frozen及NULL分类。现库DATA_SCOPE明细总8、有效5的既有状态不改；新空库默认只预置五码不是本次删除现库数据的授权。

| 精确ID | code与type | frozen/deleted | 新category |
| --- | --- | --- | --- |
| 1000000000000004001 | DATA_SCOPE | 0/0 | SYSTEM |
| 1000000000000004010 | FROZEN_STATUS | 0/0 | SYSTEM |
| 1000000000000004020 | TENANT_TYPE | 0/0 | SYSTEM |
| 1000000000000004030 | CARD_TYPE | 0/0 | SYSTEM |
| 1000000000000004040 | POST_LEVEL | 0/0 | SYSTEM |
| 1000000000000004050 | RESOURCE_TYPE | 0/0 | SYSTEM |
| 1000000000000004060 | LOGIN_TYPE | 0/0 | SYSTEM |
| 1000000000000004070 | BIZ_OPERATE_TYPE | 0/0 | SYSTEM |
| 2111791956297613314 | EVA_QA_20261018_D01 | 0/0 | BUSINESS |

明细36条/有效33，account与profile各3条、role9条、role-user关系5条、role-resource关系54条、module13条、module_resources78条。明细及三类授权/资源集合另有仅技术ID/frozen/deleted的聚合指纹；没有读取明细值、账号个人字段值/口令、日志正文或业务正文。该指纹不代表读取或逐字节验证所有业务字段。既有账号分表及B权限检查所需列已存在；此次已发现的B结构缺口仅CATEGORY varchar(16) nullable和已验证ck_sys_dict_category，不借机升级其他表。

必要8表无RLS/rules/partition、无非internal用户trigger，数据库event trigger为空；当前连接角色对这些表的owner/member布尔均true。profile仅有结构和计数证据，未读取其个人字段。实施事务将重新核主表继承、相关源/结构与触发器边界，任何新增或不符都停止，不自动处理。

## Flyway和应用依赖

HEAD为064ed4f9，依赖Flyway13.12.0。源码和现构建dev配置均enabled=false、baseline-on-migrate=false；本维护进程三个相关环境覆盖键均不存在。这不能证明当前服务实际加载的profile、外部配置、环境或JVM参数。实际写库批准前，需由总控核定现服务绑定与停写方式，确认维护窗口内没有其他启动器或迁移器启用Flyway；本任务没有扫描其完整命令行/环境、调用接口或重启服务。

现库非空且无history，新V1校验/初始化与其不兼容，不运行validate/info/migrate来“补齐”，不伪造兼容结论。正确顺序是核实际配置及停写窗口→取得已验备份和技术前像→精确维护事务→独立只读后验→再单独安排已验B应用产物的常规部署。新B依赖CATEGORY和有效主项完整分类；不能先启动B再期待它修库。分类只影响管理读写资格，不改变code KV缓存、Serializer、角色/菜单授权或C3skip，不清Redis。

## 备份工具与输出

当前PATH、DevApp及常见PostgreSQL安装目录未找到pg_dump/pg_restore/psql；已封临时PG14运行时bin只有运行工具，不能用来备份PG18服务。现没有已核定可直接使用的备份客户端路径；可把获批后从官方入口获取便携18.x工具与备份/适配合并确认，不为缺工具另开审批循环。工具核验未通过前不进入备份或写库，也不能声称已备份或已验证恢复。

需先提供经来源和文件hash核定的PostgreSQL18客户端包，记录pg_dump/pg_restore绝对路径、--version和hash。优先使用18系列并与服务器major核对；pg_dump不能处理比自己major更新的服务器。此限制见[PostgreSQL18 pg_dump官方文档](https://www.postgresql.org/docs/18/app-pgdump.html)。获实际操作批准后，可从[官方Windows入口](https://www.postgresql.org/download/windows/)转[EDB便携二进制入口](https://www.enterprisedb.com/download-postgresql-binaries)核当前18.x具体版本及链接，下载至本任务隔离目录；不猜版本URL、不安装服务、不改PATH。获取工具与备份/停写下适配合并在同一次实际操作确认，不单开工具审批循环。现在不下载或运行新客户端。

拟备份整个目标eva数据库的custom archive，保留原schema、数据、序列、表内账号/角色/授权和日志；不导出其他数据库，也不把数据库集群角色密码作为本次备份对象。现数据库约19,715,775字节不是dump体积预测。输出拟在获批受限备份根 `C:/home/backup/EVA-API-dev/<UTC>-<UUID>/eva-before-category.dump`；目录归属/ACL及可用空间须先核定。目录独占创建、解析后仍在该根内，文件必须xb独占创建，不能只依赖pg_dump的--file覆盖行为。备份不得纳入Git、AIBrain或公开报告。

下面是受控入口应调用的精确参数形式，不是当前可运行命令；凭据和host从已封datasource解析进入仅子进程环境，禁止出现在参数、终端或日志，不继承会改目标的其他PG连接环境。实际入口须再次执行dev/prod区分、批准指纹d22bc534e71c、eva及source/build相同检查。

```python
# 仅在实际执行获批且PG18客户端/受限目录已核定后由维护入口调用。
dump_args = [pg_dump18, '--format=custom', '--no-password',
             '--lock-wait-timeout=3s', '--dbname=eva']
with backup_path.open('xb') as backup:
    completed = subprocess.run(dump_args, env=approved_dev_child_environment,
        stdin=subprocess.DEVNULL, stdout=backup, stderr=subprocess.PIPE, timeout=120)
    backup.flush()
    os.fsync(backup.fileno())
    if completed.returncode != 0 or completed.stderr:
        raise RuntimeError('备份失败或有待审警告；保留不完整文件，禁止继续写库')
```

入口不得记录包含连接信息的原始stderr，只保受限原始材料或脱敏退出状态；异常/超时的半成品不删除、不覆盖，也不能标可恢复。成功后记录byte数和SHA256，独占写入备份receipt并fsync。随后用同版本pg_restore离线检查TOC，完整解码至DEVNULL而不连接数据库、不把SQL/数据打印或落明文：`[pg_restore18,'--list',backup]`和`[pg_restore18,'--file=-',backup]`，stdout须受控处理。custom格式和pg_restore的无dbname输出模式见[官方恢复工具文档](https://www.postgresql.org/docs/18/app-pgrestore.html)。这证明archive可读取，不冒作真实恢复演练成功。

若需真实恢复演练，应另指定获批的隔离PostgreSQL18目标及新空数据库，例如eva_restore_category_<nonce>，明确不得是eva，也不得连接生产；准备`[pg_restore18,'--exit-on-error','--single-transaction','--no-owner','--no-privileges','--dbname=<获批隔离空库>',backup]`。不得使用--clean/--create或直接覆盖现eva。该演练不自动得到建库/连接/恢复授权，也不恢复集群身份权限；是否将其列为本次实际写入的必需前置，由总控在实际执行方案中一并明确。原库全量恢复只能在独立灾难恢复审批后讨论，本提案默认不用dump覆盖现库。

## 精确事务与维护入口约定

正向片段为 `sql/2026-10-31-dev-category-adaptation-forward-proposal.sql`；回退片段为 `sql/2026-10-31-dev-category-adaptation-rollback-proposal.sql`。两者不位于生产migration目录，不调用Flyway，不包含BEGIN/COMMIT，也不是可直接交psql运行的脚本。专用维护载体SOURCE现已准备，事务片段和载体一起待QA增量源审；没有运行任何可写phase。

实际入口须先绑定批准窗口、SQL及RO前像SHA、PG18客户端/备份receiptSHA、固定dev目标，建立单独的SERIALIZABLE、readonly=false、autocommit=false维护连接。只在已批准执行后设置对应本地会话键eva.dev_category_approval（apply或rollback固定值）、eva.dev_category_backup_sha256；rollback另绑定已提交forward receipt的SHA和从已校验receipt读取的完整约束JSONB。SQL中的键检查仅是外层入口交接门禁，不证明服务器能够自行验证备份文件；实际入口必须先验证该文件、其目标、摘要、完成状态与窗口，不能靠手工SET字符串代替。

事务先以lock_timeout3s/statement_timeout15s取得sys_dict ACCESS EXCLUSIVE及明细/账号/资料/角色/授权/资源的SHARE锁；维护窗口必须暂停这些写入和并发DDL。锁失败、读写资格/目标/版本/历史存在性/结构漂移即回滚并关闭，不重试或延长。正向写前同时精确比较fresh已有6项有序完整约束JSONB（名称、类型、validated、真实definition），不得让同名改表达式或新增未知check进入更新。只有当前47条技术前像和计数/技术摘要仍与fresh证据精确相同，才新增nullable CATEGORY varchar(16)及SYSTEM/BUSINESS/NULL check，并直接更新恰好九行category；DDL和DML在同一事务，不经历已提交的半迁移结构。没有upsert、删除、恢复历史明细、根项、其他字段更新或授权传播。

更新后核九行精确分类、其余38行NULL、约束validated、原计数/技术摘要不变；入口读取九行技术后像和真实7项完整约束后像，确认原6项未变，在同一事务中独占保存before-commit receipt、flush/fsync并重算SHA，再存commit-intent后commit。COMMIT成功应答后才能生成COMMIT_CONFIRMED凭证；若发送COMMIT后连接错误则标UNKNOWN，不主动rollback作结论、不自动再次apply，关闭连接并保留材料，须另行获准只读核实。提交已成功但确认receipt落盘失败也必须保留CONFIRMED状态，不假称已回滚。整个受控入口、凭据隐藏、目标及receipt校验仍须独审后才进入总控的实际执行确认，不能仅凭两份SQL直接放行。

## 回退及影响

提交前任何错误由同一事务回滚，新增列/check和九分类一并撤销。提交后如需回退，先停止新B管理入口并由总控安排兼容旧应用；无此应用顺序不能把分类置NULL。受控rollback必须另获批准，读取已校验COMMIT_CONFIRMED→before-commit→backup凭证链，再次匹配同47技术前像、摘要、列型、九分类及完整7项约束后像；SQL和入口均在持锁状态精确比较完整约束目录，不靠名称/validated或手填SHA自证、不猜PG规范化表达式。只有全部相同才把这九个category精确恢复原NULL。其余字段/38非有效项/明细/账号角色授权/日志/history均不更新；出现后来新增字典、改码、删冻、分类或约束变更则停止，不覆盖后来写入。

默认回退保留已新增nullable列和check，兼容旧应用，不物理删列。DROP COLUMN/CONSTRAINT不在本提案内；若用户另要求清除，仅可在证明确为本次新增、所有依赖已退回且无后续分类数据时另审，不使用CASCADE。原schema/数据/命名不迁移，_line/_ref规则不触发本次存量重命名。

## 当前交审结论

所需业务映射已确认，fresh元数据支持仅补分类列/check和九行分类。总控已将独审交QA，不再等待旧designer；唯一约束mustfix与专用载体现一起交QA增量源审。只读取证已结束，不增加连接。实际执行前一次补齐可信PG18便携工具、真实配置/全相关停写条件及用户明确批准，不新增其它业务功能或重复已绿验收。

## 专用载体与最小命令（仅供批准后使用）

`sql/dev_category_adaptation.py`为本任务专用入口，复用已审datasource解析/目标识别及独占fsync凭证流程，不调用Spring/Flyway，不操作服务，不自动下载。默认`--plan`无连接、无网络、无输出目录写入；只有显式phase、execute-approved、窗口、操作方已核运行绑定与停写确认、外部固定seal SHA全部具足才继续。操作方两个确认flag是总控核事实后的显式交接，不是脚本已自动探测服务状态的证明。

新冻结seal覆盖SQL/载体/RO事实/必要源及14保护文件。`--backup`要求外部固定SHA的client attestation：官方入口、具体目录、18major、已验证出版方证据、包内文件hash；Authenticode方式由脚本复核Valid和已核签名thumbprint，或OfficialChecksum方式复核官方记录的archive hash；随后实际运行两工具--version核18系列。不接受仅手填publisherVerified作为实际来源核验，相关官方hash/证书来源须由总控在已批准获取工具时实证记录，attestation SHA从该核验交接。

备份在全新私有ACL目录中独占写archive，检查空间、退出/警告、TOC和全解码后才生成BACKUP_VALIDATED凭证。失败保留半成品和失败状态。apply要求同窗同target同seal的成功备份凭证及仍匹配的archive字节/hash，持锁执行冻结SQL，保存九行/完整约束后像及提交意图；不接收裸SHA替代可读取凭证。rollback需额外rollback-approval-id和已提交forward凭证链，默认保结构，只改九NULL。

```text
python docs/legion/design/sql/dev_category_adaptation.py --plan

python docs/legion/design/sql/dev_category_adaptation.py --backup --execute-approved --window-id <已批窗口>
  --runtime-verified --writers-stopped-confirmed --seal-sha <冻结sealSHA>
  --client-attestation <已核PG18工具凭证.json> --client-attestation-sha <工具凭证SHA>

python docs/legion/design/sql/dev_category_adaptation.py --apply --execute-approved --window-id <同一已批窗口>
  --runtime-verified --writers-stopped-confirmed --seal-sha <同一sealSHA>
  --receipt <backup-receipt.json> --receipt-sha <实际备份凭证SHA>

python docs/legion/design/sql/dev_category_adaptation.py --rollback --execute-approved --window-id <已批回退窗口>
  --runtime-verified --writers-stopped-confirmed --seal-sha <同一sealSHA>
  --receipt <forward的committed.json> --receipt-sha <实际已提交凭证SHA>
  --rollback-approval-id <另批回退编号>
```

上述换行仅展示参数，不能直接复制为多条执行；载体默认不执行。正常apply成功后，在同一实际操作批准范围内复用已审604E只读工具一次新RUN作必要后验（九分类/47前像/结构/历史存在性/摘要），再交QA核证，不重跑66项、临时PG或界面主题/像素测试。当前仅AST、默认plan、缺批准拒绝、纯fake提交前回滚/UNKNOWN不主动rollback或重试/成功凭证检查通过，均不构成真实PG/备份/写库或恢复证据。
