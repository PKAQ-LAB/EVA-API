# 0006 现开发库精确适配审批清单

最新阶段（2026-11-01）：总控转达用户最新明确批准后已实际执行一次，结果为DB_AND_SYSTEM_CACHE_DONE；角色列新增、既有0006恢复一行及真实SYSTEM单键失效均完成。结果文件`.gradle/native-self-bridge-d03d415fbd994aee9d8da6411c9e561e/role0006-adaptation-result.json`，SHA256 `398BE19D9DF4FEEE3809197B0C1FA494F548127F5C44398DEAD512BD813AE0C5`，本角色仅本地读证据并实算匹配。下文“尚未批准/待QA”等为执行前审查历史，不代表最新状态，不要求再次批准同一维护。后端正在完成一次独立限定只读后验、QA审收据与后验；仍待用户新构建/重启及运行联调，本记录不将维护成功等同实际运行功能验收。

2026-11-01 technical_designer。此清单基于backend获批的一次限定只读核验，替代早期方案中“新增0006明细”的候选。代码/新库初始化已批，现库写入及缓存失效尚未批准；本角色只核本地证据文件，没有连接数据库或Redis。

## 已核证据与限制

核验结果：`.gradle/native-self-bridge-971bfcdf557b4760aaf0dfefc1861a21/role0006-preflight-result.json`，SHA256 `E58E09A72B9BDA287CA6A3C3618AD20D5E3876FB20825DA17A1F8F50C3886EED`（本角色已实算匹配）。查询清单同目录`role0006-preflight-queries.json`，结果绑定SHA256 `CEDADC261D4C281B60C4F28110ABD198DFC1548696163F2AAEDA16A9772E1C8B`。

核验时间为北京时间2026-11-01 16:13:51.496072至16:13:51.572444；1连接、7条SELECT、事务及默认只读均on、repeatable read、0写、最终rollback并关闭。目标数据库eva、实际schema eva，批准开发目标指纹、源码/构建配置standalone且tenant.enable=false匹配。未证明运行进程CLI/env配置一致（runtimeProcessConfigurationVerified=false），未读角色业务行、用户、其他字典、Redis或私有schema；不将此范围外状态当已核。

`eva.sys_role`存在，`data_post_ids`缺失。该表当前元数据含id主键及id/deleted/sort非空约束；本次适配不改变这些约束。不存在私有schema核验或批改授权，本次执行目标仅已核eva源，未来私有库加列须另有明确目标。

## 两项精确数据库变更

| 对象 | 已核前像 | 拟批准后像 |
| --- | --- | --- |
| eva.sys_role.data_post_ids | 列不存在 | 新增TEXT、允许NULL、无默认值；历史角色值自然NULL，其他列/行不改 |
| eva.sys_dict_item，ID 1000000000000004008 | 唯一物理0006明细，deleted=1 | 仅deleted改为0，精确影响1行，不INSERT、不换ID、不改其他字段 |

恢复行的完整已核业务断言为：id=1000000000000004008、main_id=1000000000000004001、d_code='0006'、d_value='指定岗位'、sort=7、frozen=0、tenant_id=0、deleted=1。该ID已被此行占用，是恢复既有行，不能把占用当作无关冲突而换号。revision、remark、作者、时间未在本次限定结果中输出，不能声称已读取；更新语句只赋值deleted，写前备份并验证这些未修改列保持前后相等，不伪造审计身份或套用新库种子的作者/时间。

主项断言为：id=1000000000000004001、code='DATA_SCOPE'、category='SYSTEM'、deleted=0、frozen=0、sort=1、tenant_id=0，且主项唯一活动。保留以下五条明细全部原值，禁止补0004/0005、改sort、主项分类/状态或权限。

| d_code | id | d_value | sort | deleted/frozen/tenant_id |
| --- | --- | --- | --- | --- |
| 0000 | 1000000000000004002 | 全部权限 | 1 | 0/0/0 |
| 0001 | 1000000000000004003 | 仅本部门 | 2 | 0/0/0 |
| 0002 | 1000000000000004004 | 本人所属部门及下属部门 | 3 | 0/0/0 |
| 0003 | 1000000000000004005 | 指定部门 | 4 | 0/0/0 |
| 0007 | 1000000000000004009 | 本人创建或修改 | 8 | 0/0/0 |

两项放同一数据库事务，使用明确限定schema的对象名；执行者在锁定并限制相关并发写后重核列仍缺失、主项与六条物理明细完全匹配前像、无额外0006或候选ID冲突，再加列并精确更新一行。断言失败、影响数非1或锁失败均回滚零写，不使用IF NOT EXISTS、宽泛upsert或降级断言掩盖漂移。禁止重跑V1、repair/baseline/clean、改角色scope/org/post配置、成员授权、permVer或其他字典。此审批只允许0006这一行deleted 1→0；“status不变”指除此明确批准变化之外的全部状态不变。

## 备份、顺序及结果记录

获批执行者写前保存目标结构、主项与六条物理明细的完整前像及校验值，记录事务锁和断言；明确备份恢复仅针对本次变更，不能用整库回放覆盖后续业务数据。现有核验未读角色业务行，不为此审批要求预先扫描全部角色或用户；新增可空无默认列不会写旧角色配置。加列部署须早于启用读取该列的新认证/角色运行态，不能由代码已批推定旧库可直接加载。

提交后核验新列定义、恢复行精确后像、原五明细/主项全部未变及仅一个有效0006。记录“数据库回滚”“数据库已提交，缓存未完成”“数据库及单键失效完成”三种实际结果，不将缓存失败报告为数据库已回滚。

## 精确SYSTEM单键失效

源码确认本次Owner为`(eva,SYSTEM)`，CacheManager逻辑cacheName=`dictdata`，逻辑键=`v2::eva::SYSTEM::DATA_SCOPE`。数据库提交成功后仅通过已审受控维护通道调用既有`DictCacheHelper.remove(owner,"DATA_SCOPE")`或绑定同一应用CacheManager的等价evict，随后沿原可信读取回填。禁止removeAll、全清Redis、刷新全部字典或重启替代此动作。

DictService.reloadTypesAfterCommit捕获BUSINESS Owner，不适用于本次SYSTEM恢复；外部SQL提交不会触发Java afterCommit。当前没有公开精确刷新端点，也没有已审可直接执行的独立SYSTEM维护脚本。本轮未访问Redis，不能假定实时物理键存在；默认前缀形式`dictdata::v2::eva::SYSTEM::DATA_SCOPE`仅是源码配置推导，不能未经实例绑定核验就发原始DEL。执行包须把单键维护通道、应用实例来源和提交后触发顺序具体化供审，未具备前不得承诺缓存已刷新。

## 回退

提交前任何失败依数据库事务回滚。提交后业务回退仅允许在阻止新增0006依赖并核验精确后像后，把同一ID 1000000000000004008的deleted 0→1，其他字段不变；不是删除行。回退提交后同SYSTEM Owner单键失效。若该行已变更、已有新的0006业务依赖或无法证明后像一致，停止并回报，不擅改角色配置解除依赖。

新增列保留为可空兼容列是默认回退方式；依赖它的新运行态仍使用或已有非NULL岗位配置时禁止自动DROP。只有另批且证明应用已兼容退回、该列全NULL且无依赖后才可考虑删列，不把DROP夹入本次执行授权。

## 总控一次审批用语与待补执行件

拟向用户一次明确确认：“批准在已核开发数据库eva/schema eva中，为SYS_ROLE新增可空无默认值TEXT列DATA_POST_IDS，并仅将既有SYS_DICT_ITEM ID1000000000000004008、MAIN_ID1000000000000004001的0006‘指定岗位’明细DELETED从1恢复为0；其余字段、原五项、主项分类及授权保持。执行前备份、锁定重核前像，两项同事务；提交后仅失效真实SYSTEM来源的DATA_SCOPE单键。禁止新增重号行、重放初始化、repair/baseline或自动DROP列。”

这段是明确变更范围，当前尚未获该写授权。交实际执行前还需由总控指定唯一执行者，将精确SQL/断言/备份路径及校验值、受控单键维护工具和实例绑定、维护窗口及失败结果处理落成可审执行包；不要求用户重新选择已定岗位语义或全局读取范围。只读窗口已用完，准备执行件不延长其授权；任何新增数据库/缓存连接都按总控另派范围执行。

## 维护包候选已具体化（待独立QA冻结，不是执行授权）

候选目录`.gradle/role0006-adaptation-20261101/`；candidate-manifest.json SHA256 `A9CD50A4E41C428FEC1A60F49079D493FD02A3865DEE6D9AF6409F5DFA29F796`，本角色实算该签及11个输入均匹配。产品源沿237DB冻结，候选不修改产品40输入。forward-operations.sql仅加上述列及按完整已核业务条件将该行deleted改0；rollback-review.sql只反向恢复该行deleted1且默认ROLLBACK，不DROP列。此候选替代前文“尚无工具”的准备状态；维护包独立QA尚未完成，不能沿用产品QA作为维护包通过。

执行者唯一计划入口为role0006_adaptation.py，缺省PLAN，`--execute-approved`须新批准窗口及维护QA seal。先由Java子进程确认已核Redis开发配置绑定的只读PING，失败不建立PG连接；随后PG单串行化读写事务锁角色表及指定字典行，重核E58前像/约束并拒触发器规则RLS漂移，受限前像落盘后执行两SQL并核验。前像文件只含获批的限定元数据/字典业务字段，未将作者、时间或全部角色数据扩大读取；SQL只更新deleted、拒额外数据库行为，禁止声称完整行所有列已读取备份。

数据库提交确认后写不可变DB_COMMITTED_CACHE_PENDING收据，维护器校验收据/窗口/前像再复用项目CacheManager与DictCacheHelper精确单键失效。Java维护器是该执行器内部子进程，直接Java参数本身不验证maintenance-seal；不得把内部`--check-binding-approved`或`--evict-approved`单独当作已审真实执行入口，批准和QA绑定以Python入口及最终seal范围为准。seal最终绑定维护源码/SQL/类与参数文件，执行前由QA核其实际依赖与记录，不靠候选自报qaReviewed。

未提交异常回滚关闭；commit确认未知标COMMIT_OUTCOME_UNKNOWN，已提交但缓存失败保留DB_COMMITTED_CACHE_PENDING。均停止不自动重试、不恢复已提交业务；不能将故障误报为零写。当前仅后台本地默认PLAN/fake证据，真实PG、Redis连接与失效都未执行；待QA完成候选冻结再由总控一次请求上述具体写授权。
