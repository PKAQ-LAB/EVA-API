# 字典C：当前源码差异与最小实施边界

2026-11-01，仅源码只读核对及设计记录。依据已批准的2026-10-31-dictionary-minimal-contract-proposal，保留B全部资格、分类+ID、9999整批拒写和响应能力契约。B真实CRUD及本轮样本清理已闭环，不再验。当前不改业务源、初始化SQL、真实库、服务、HTTP、Redis或Git；C尚未实施。

## 实际结构与旧提案修正

当前生产只有V1__INITDB.sql，私有只有V1__TENANT_SCHEMA_BASE.sql。私有V1已经有sys_dict、sys_dict_item、CATEGORY varchar16 nullable、ck_sys_dict_category、有效code和main_id/d_code唯一索引，无核心字典种子。账本为EVA_TENANT_SCHEMA_VERSION版本1，在结构完成后写入；不是生产Flyway链。旧C提案中“现V1/V2再新增后续租户版本”已不符合当前单初始化基线，不能照搬。

TenantSchemaProvisioner已对无账本非空schema拒绝，对已有账本要求仅版本1且最终account/profile存在；它没有证明字典表/分类列完备。C入口须额外核实际两表、分类列/约束/来源，不以账本1代替完整性检查。已存在但缺字典结构的租户应拒启并单列其升级提案，不重跑模板、猜分类或遍历全部schema。新租户沿现V1一次创建空业务字典、随后既有租户管理员同步；无需为C修改模板/版本或破坏F604前像证据。没有读取真实私有schema，不能称存量已满足。

另有必须处理的映射差异：私有模板两字典表不含tenant_id，而DictEntity/DictItemEntity继承StdEntity的持久化tenantId字段。现BaseMapper默认投影和INSERT填充列可能带TENANT_ID；不能直接把standalone Mapper CRUD切到私有表。建议在现DictMapper/DictItemMapper中增加仅schema分支的明确列投影/写SQL，排除不存在的tenant_id，保审计/逻辑删除/数据权限与源锁；standalone保原BaseMapper路径，不改StdEntity全局映射、不补私有列、不作JDBC全权限写旁路。此为内部Mapper适配，不新增HTTP/DB契约。若改选“私有表补tenant_id列”，则属于明确新DB契约，须先交总控收口，当前不实施。

## 当前调用链缺口

DictService.selectDict/queryDict/fetchDicts/get/list/write均依赖当前Mapper+TenantSchema；其默认search_path为tenant,core。它们没有显式core SYSTEM与private BUSINESS两源合并，不能靠该search_path代替分源或缺表回落校验。get带category目前仅校实体字段，不能解决跨源同Long ID。selectDict用Map.put聚合，会覆盖重码，fetchDicts把任意非空缓存当完整集合，均须改为明示冲突拒绝和完整来源清单。

DictBusinessAccess目前只允许standalone/tenant关闭/主体tenant0，按真实SYS_ACCOUNT与ROLE_ADMIN关系及操作资源重验；schema租户尚不能通过。DictItemSnapshot已有正租户可信resolver/core元数据核、事务及保护明细锁，但锁主仍走BaseMapper，需同步私有投影适配。不得削弱其9999遗漏、改值、跨主ID、整批拒绝与个人可维护主校验。

DictCacheHelper在无TenantContext时读写裸code，有context时schema::code；getAll先全dictdata扫描，removeAll无context直接cache.clear。JacksonCodeSerializer直接cache.get(codeKey)，序列化常发生于service路由恢复以后；DictInit启动执行init，standalone可无context全清/全载。以上均尚未实现C，不能把已验standalone B作为C隔离证据。

## 目标路由、身份与事务

| 路径 | 合法来源 | 限制 |
| --- | --- | --- |
| standalone | 配置standaloneSchema同表两分类 | 有效已认证主体/tenant0，B维护关系与资源规则保持 |
| schema正租户系统 | 配置core的SYSTEM | 仅明确SYSTEM读取，始终只读，不纳入core BUSINESS |
| schema正租户业务 | 可信SYS_TENANT映射的当前私有schema BUSINESS | 只当前租户，不纳入私有SYSTEM，不迁移遗留行 |
| schema平台0/无可信主体 | 无新增可读写源 | 不修改认证resolver或扩大平台0能力；Serializer保原码 |

在DictBusinessAccess中集中产生本次已核来源描述（schema/category/tenant），不要从客户端schema或已恢复的TenantContext单独推owner。正租户须ThreadUser有效且tenant>0，core限定控制表解析resolver，私有SYS_ACCOUNT验证实际有效账号；私有表没有tenant_id，不能照抄standalone TENANT_ID=0条件。租户维护角色现由TenantLocalAdminService创建TENANT_ADMIN（frozen9999也有效），不是ROLE_ADMIN；按其真实私有role/user关系核资格，资源还须核私有授权引用与core模块资源及本租户有效授权上限，不凭rolesMap/ALL或资源开关授维护资格，不新增授权数据。

公共service方法保事务代理；core SYSTEM和私有BUSINESS分别受控读取，逐表确认真实namespace、必要列和分类版本，缺表不得从tenant,core补齐。优先对字典专用SQL明确合法schema限定，core控制读取复用CoreSchemaExecutor并finally恢复search_path；不改变全局TenantSchemaRouter以免波及认证/其它模块。恢复原TenantContext和外层事务路径，afterCommit使用提前捕获owner，不重新从已恢复context猜源。错误/回滚不刷新缓存。

保留当前URL、Long ID及code→键值返回结构；get的已批准可选category用于精确core/private选源。省略category只有恰一源命中兼容，双命中报歧义；写默认BUSINESS只定位当前私有源，显式SYSTEM拒绝。业务建改码同时查core有效SYSTEM保留码及本业务源有效重码；query单code和聚合遇合法两源同码均失败，不以put覆盖，独立其它code查询可继续。未分类有效项按契约报就绪错误，不把NULL当BUSINESS；不复制/移动core BUSINESS或私有SYSTEM历史行。

## v2缓存、序列化和bootstrap

缓存名仍dictdata；内部API改传显式已核owner，key为v2::<schema>::<SYSTEM|BUSINESS>::<code>。standalone同样带实际schema/category。schema白名单及固定分类防owner伪造；code作完整键末段，不从客户端拆owner。只读v2，不读取/搬迁裸code或schema::code，不全清Redis。core SYSTEM仅同core共享，private BUSINESS仅同tenant共享。

getAll/removeAll必须传当前获准owner，利用现RedisUtil.scanPureAll(cacheName, entryPrefix)限定v2 owner前缀，再严格核前缀；无owner拒绝，不调用cache.clear或空前缀扫描。fetchDicts先取得合法两源的完整有效code清单并检重，逐owner/code读缓存、缺项冷载，不能把部分已暖缓存当完整聚合。写提交后仅本BUSINESS owner的新旧code失效/刷新，其他tenant/core/旧键不动；失败不刷新。

现init(Map)/reload(Map)及cache helper的add/update/getObject等内部入口也不能保留裸code旁路；保留现service签名时，必须先按可信来源解析code，并以受限权威读取取得共享值，不能把调用方传来的任意Map当已核owner的缓存内容。私有Mapper适配还须保持原审计填充、revision/更新行数语义和个人维护主校验，不能仅解决缺tenant_id投影而丢乐观锁或写权限。

共享字典缓存只能由经身份、源、分类核验的字典专用完整读取填充，不能塞入SELF裁剪结果。管理可维护主项仍沿原数据权限和主锁；针对共享字典语义的受限读取不得扩展成全局Ignore/ALL。

Serializer改经统一只读queryDict解析，从仍可信ThreadUser重新取得来源，不直接读cache或依赖残留TenantContext。无法定源保原code，不跨租户兜底；日志不打印字段值。AuthCtrl.fetchDicts现已委托DictService，无需改URL或bootstrap外层能力字段；对该委托路径补来源一致性验证即可。DictInit无可信owner时跳过预热/清除，采用v2自然冷载；不启动扫描租户。若standalone启动需保预热，只能显式受控owner，不能恢复裸code/全清行为。

## 最小源文件与验证范围

必要8个现有生产文件：DictService、DictBusinessAccess、DictItemSnapshot、DictMapper、DictItemMapper、DictCacheHelper、DictInit、JacksonCodeSerializer。现IDictService公共方法与AuthCtrl/DictCtrl的HTTP契约、DictAoeBo/DictViewVo分类+ID能力保持，不预设改动；内部Cache API及Mapper方法可必要适配。CoreSchemaExecutor、resolver/router、认证主体/全局拦截器、单V1与模板暂不改。实施前对实际拟改符号做GitNexus影响核对，以当前源码为准（索引旧行号不作事实）。不新增通用supporter、迁移框架或跨项目规则。

必要合成验证只覆盖C核心：core/t1/t2相同ID及不同值分源；同code冲突拒绝、同category/private跨tenant不串；缺私有表/分类列而core存在时拒回落；普通主体不能维护、TENANT_ADMIN与资源上限、SYSTEM/9999整批保护；v2不读旧键、prefix限定扫描/删除、owner afterCommit且rollback不刷新；serializer在原路由恢复后仍同源、无主体原码、bootstrap同源；私有无tenant_id的真实Mapper读写。使用已有隔离PG机制和缓存mock，不重跑B真实UI或主题、不实际写现库/清缓存；测试通过也不代表所有存量tenant就绪。

当前结论：C总体政策已批准，无需重问。基于现模板，可优先不新增API/DB契约、不修改初始化文件而实施上述8文件内部适配；私有Mapper无tenant_id和真实TENANT_ADMIN资格为必须先解决的当前源码差异。实施源码写入范围及具体内部SQL影响先交总控/QA，当前只冻结本报告，不生成业务patch。
