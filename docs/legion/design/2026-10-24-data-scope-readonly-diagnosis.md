# 包 S：组织编码及 DATA_SCOPE 限定只读诊断

日期：2026-10-24；执行者 backend_developer。本轮不获生产源码写锁，不构建测试，不发 HTTP、不读秘密日志，不执行 DB/Redis 写或恢复。当前实例已获用户确认：9016 为 standalone、tenant 关闭、data/resource 开启，当前 PG 为获准开发测试库；本轮复核仍 PID47664、10:08:31 启动的 WebBooter。后续实际只读指纹与 DATA_SCOPE 专项快照记录如下，恢复方案仍交技术设计与总控确认。


## 1. 实际开发库 DATA_SCOPE 专项只读快照

```json
{
  "observedAt": "2026-10-24T03:40:31.475438+00:00",
  "fingerprint": {
    "database": "eva",
    "schema": "eva",
    "transactionReadOnly": "on",
    "defaultTransactionReadOnly": "on"
  },
  "catalog": {
    "sys_dict": {
      "code": "character varying",
      "deleted": "bigint",
      "frozen": "integer",
      "id": "bigint",
      "isleaf": "boolean",
      "name": "character varying",
      "path": "character varying",
      "pid": "bigint",
      "sort": "double precision",
      "tenant_id": "bigint",
      "type": "character varying"
    },
    "sys_dict_item": {
      "d_code": "character varying",
      "d_value": "character varying",
      "deleted": "bigint",
      "frozen": "integer",
      "id": "bigint",
      "main_id": "bigint",
      "sort": "double precision",
      "tenant_id": "bigint"
    }
  },
  "mainRows": [
    {
      "id": "1000000000000004001",
      "type": "DATA_SCOPE",
      "code": "DATA_SCOPE",
      "name": "数据权限",
      "deleted": 1,
      "frozen": 0,
      "sort": 1.0,
      "tenant_id": "0",
      "pid": "0",
      "path": "/1000000000000004001",
      "isleaf": true
    }
  ],
  "associatedItemCount": 8,
  "associatedItems": [
    {
      "id": "1000000000000004002",
      "main_id": "1000000000000004001",
      "d_code": "0000",
      "d_value": "全部权限",
      "deleted": 1,
      "frozen": 0,
      "sort": 1.0,
      "tenant_id": "0"
    },
    {
      "id": "1000000000000004003",
      "main_id": "1000000000000004001",
      "d_code": "0001",
      "d_value": "仅本部门",
      "deleted": 1,
      "frozen": 0,
      "sort": 2.0,
      "tenant_id": "0"
    },
    {
      "id": "1000000000000004004",
      "main_id": "1000000000000004001",
      "d_code": "0002",
      "d_value": "本人所属部门及下属部门",
      "deleted": 1,
      "frozen": 0,
      "sort": 3.0,
      "tenant_id": "0"
    },
    {
      "id": "1000000000000004005",
      "main_id": "1000000000000004001",
      "d_code": "0003",
      "d_value": "指定部门",
      "deleted": 1,
      "frozen": 0,
      "sort": 4.0,
      "tenant_id": "0"
    },
    {
      "id": "1000000000000004006",
      "main_id": "1000000000000004001",
      "d_code": "0004",
      "d_value": "仅本岗位",
      "deleted": 1,
      "frozen": 0,
      "sort": 5.0,
      "tenant_id": "0"
    },
    {
      "id": "1000000000000004007",
      "main_id": "1000000000000004001",
      "d_code": "0005",
      "d_value": "本人所属岗位及下属岗位",
      "deleted": 1,
      "frozen": 0,
      "sort": 6.0,
      "tenant_id": "0"
    },
    {
      "id": "1000000000000004008",
      "main_id": "1000000000000004001",
      "d_code": "0006",
      "d_value": "指定岗位",
      "deleted": 1,
      "frozen": 0,
      "sort": 7.0,
      "tenant_id": "0"
    },
    {
      "id": "1000000000000004009",
      "main_id": "1000000000000004001",
      "d_code": "0007",
      "d_value": "本人创建或修改",
      "deleted": 1,
      "frozen": 0,
      "sort": 8.0,
      "tenant_id": "0"
    }
  ],
  "absentMainLegacyFields": [],
  "absentItemLegacyFields": [
    "pid",
    "path",
    "isleaf"
  ],
  "finalTransactionReadOnly": "on",
  "rolledBack": true,
  "closed": true,
  "writes": 0
}
```


## 2. 组织编码：当前源码规则与现场现象的界限

GitNexus query/context 定位 OrganizationCtrl.checkUnique 及 OrganizationService，但索引落后 HEAD 21 提交；只用于定位，以本节读取的当前源码为准，未重建索引或运行测试。

| 源码位置 | 实际规则 | 能说明与不能说明 |
| --- | --- | --- |
| sys-domain/.../organization/bo/OrganizationAoeBo.java:19–30 | name/code 都仅 @NotBlank；pid 可空 | 没有字母数字 Pattern，也没有禁止下划线的后端注解 |
| core-contract/.../mvc/bo/StdBo.java | code 无格式约束；remark @Size(max=400) | 父类也未提供该字母数字规则；IdCodeBo 亦仅 code 非空，且不是组织 checkUnique 的参数类 |
| OrganizationCtrl.java:35–44 | checkUnique 无 @Valid；code/name 均空时 MISS_CODE_OR_NAME，否则调 service，重复返回 ORG_CODE_EXIST | 本接口不是格式校验，name-only/code-only 可请求；不能把其业务错误说明成后端编码正则 |
| OrganizationService.java:57–86 | pid null→0，查询同 pid 下 code 或 name 重复，编辑时排除自身 id | 未 trim/替换下划线、未执行 code 正则；逻辑删除及数据权限由既有 Mapper/插件机制处理 |
| OrganizationCtrl.java:67–74 / OrganizationService.java:108–146 | edit 的 @Valid 触发上述非空等 Bean 校验，随后 MapStruct 原样映射 code 并做树保存 | 当前手写及已有生成 Convert 都没有字母数字格式处理；未因此推断当前服务已加载这些精确类或当前 HTTP 一定成功 |
| SysCodes.java / CommonCodes.java | ORG_CODE_EXIST=4310，默认“组织(部门)编码已经存在”；MISS_CODE_OR_NAME 默认“未接收到岗位管理编码或名称”；父记录缺失为 CAN_NOT_FIND_RECORD，循环父关系等为 PARAM_ERROR | 这些默认文案与“编码格式错误，仅允许字母数字”不同；MISS 文案复用了岗位名称是现有源码现象，不是本次下划线拦截规则 |
| ExceptionAdvice.java | @Valid 参数异常走 HTTP400/PARAM_TYPE_ERROR+field defaultMessage；BizException 走原 BizCode Response | 此处没有追加“仅允许字母数字”；所读 common/sys i18n 中解码后也未命中该格式提示或上述 code 的覆盖项 |

因此，**不能把 QA_P_20261023 被拦截认定为已证实的后端既有 alphanumeric policy**。当前源码链不含该规则；也不能仅凭静态链断言现场一定是前端、指出具体前端控件根因或宣称请求已经/尚未发出。真实 Form 链由 frontend_developer 另查；本轮不触 HTTP、不读日志/Token、不为了通过而擅改测试候选编码或放宽服务端规则。

组织 parent/path 的实际约束是另一条链，不能混为编码格式错误：

- pid 空或 0 是源码根节点；新建根 PATH 为 /新ID，不读取父节点，不要求客户端提交 parentPath。
- 新建非根通过 Mapper.selectById(pid) 读取父节点，父不存在或 path 为 null 则 CAN_NOT_FIND_RECORD；父 path 不自动推测、补写或用 UI 字符串替代。
- 编辑既有节点先检查只读状态；换父禁止自身、后代节点，非根父须存在，重建当前与后代 path，维护新旧父叶子标记。仍须合法身份能读取父节点，数据权限隐藏父时不能擅扩权限绕过。
- 同级排序从实际父节点的 MAX(SORT) 派生；新增/改父会更新父 isleaf。前端 BO 不接收 path/isleaf、不能据前台传值建立可信树关系。
- 本轮未查组织业务行或约束 catalog，未核当前 UI 可用父节点、实际根可创建性或真实插入结果。此处只是当前 Controller/Service/Mapper 的静态约束，不替代现场验收。

## 3. DATA_SCOPE 当前事实与待确认的最小恢复候选

第 1 节为北京时间 11:40:31 的当前数据库快照，旧设计的删除线索本轮已重新核实：eva.sys_dict 只有一个 DATA_SCOPE 主项，ID=1000000000000004001，DELETED=1/FROZEN=0/TYPE=CODE=DATA_SCOPE，NAME=数据权限、SORT=1、TENANT_ID=0、PID=0、PATH=/1000000000000004001、ISLEAF=true。关联 eva.sys_dict_item 恰 8 条，全 DELETED=1/FROZEN=0/TENANT_ID=0，MAIN_ID 都为该主项。

| 实际明细 ID | D_CODE（提交值） | D_VALUE（显示文本） | SORT | 当前 DELETED | 当前支持情况 | 仅供确认的 DELETED 后值 |
| --- | --- | --- | ---: | ---: | --- | ---: |
| 1000000000000004002 | 0000 | 全部权限 | 1 | 1 | 支持 ALL，须管理者明确选择，不可作为缺值默认 | 0 |
| 1000000000000004003 | 0001 | 仅本部门 | 2 | 1 | 支持 | 0 |
| 1000000000000004004 | 0002 | 本人所属部门及下属部门 | 3 | 1 | 支持 | 0 |
| 1000000000000004005 | 0003 | 指定部门 | 4 | 1 | 支持，另须合法非空 orgIds | 0 |
| 1000000000000004006 | 0004 | 仅本岗位 | 5 | 1 | 枚举存在，但新授拒绝、数据谓词未实现 | 保持 1 |
| 1000000000000004007 | 0005 | 本人所属岗位及下属岗位 | 6 | 1 | 同上 | 保持 1 |
| 1000000000000004008 | 0006 | 指定岗位 | 7 | 1 | 同上 | 保持 1 |
| 1000000000000004009 | 0007 | 本人创建或修改 | 8 | 1 | 支持 | 0 |

技术上的最小候选是主项 1000000000000004001 的 DELETED 1→0，加上上表 0000/1/2/3/7 对应 5 条明细的 DELETED 1→0（合计 6 行）；其余 3 条保持删除。**这只是供 technical_designer/总控提交用户确认的具体候选，不是本成员决定恢复或已获执行批准。** 名称、提交值、显示值、排序、tenant、frozen、PID/PATH/ISLEAF、MAIN_ID 全保持快照前值，不趁机改为 9999、挂到旧系统根、改归属、创建根或重命名表。是否另改为系统只读/归属必须单独确认，不能由“内置字典恢复”概括推得。

RoleService.validateDataScope（约463行）当前拒绝 null/非法枚举与 0004/5/6 的新授；编辑缺 scope 保留原值，不合成 0000。MybatisPlusDataPermissionHandler 对 0000 返回 ALL，对 0001/2/3/7 有谓词，岗位 0004/5/6 没有实现，最终拒绝。恢复 8 个标签不能等同实现 8 种权限；恢复数据不授角色或改既有授权。

实际 catalog 中 sys_dict 仍有 PID/PATH/ISLEAF，sys_dict_item 没有这些旧父字段；明细使用 MAIN_ID/D_CODE/D_VALUE，不能猜成 CODE/VALUE/NAME 或按树叶表改写。未查询旧 SYS_DICT 子叶或其他根/字典，不据本轮快照声称所有历史树归属已核。

## 4. 空字典缓存：namespace、定向驱逐与现有调用边界

- DictCtrl 的 GET /sys/dictionary/query/{code} 调 DictService.queryDict；queryDict 在缓存 get(type) 返回非 null 时直接返回，空 Map 也命中。没有“空值自动回源”的保护。selectDictByType 仅查询可见未删除且非 FROZEN 的主项，listItems 只查该 MAIN_ID 的可见明细，过滤 FROZEN 后返回 D_CODE→D_VALUE；主项删除时结果是空 LinkedHashMap，随后 cachePut(type, items)。所以只恢复 DB 不能保证当前缓存立即正确。
- CommonConstant.CACHE_DICTDATA=“dictdata”。DictCacheHelper.get/remove/cachePut 都使用 scopedKey(code)：TenantContext.schemaName 为 null 时逻辑 key=DATA_SCOPE；非 null 时 key=<schema>::DATA_SCOPE。默认 RedisConfiguration 采用 RedisCacheConfiguration.defaultCacheConfig、String key 序列化且未自定义 prefix，因此默认物理 key 预期为 dictdata::DATA_SCOPE，或有 schema 上下文时 dictdata::<schema>::DATA_SCOPE。此为当前源码默认路径，不是对运行 Redis 实际 key/Bean 的读取证明。
- 已确认 standalone/tenant 关闭，TenantSchemaRouter.routeCurrentTransaction 在非 schema 模式直接返回，不新增 schema 绑定；正常无外层 TenantContext 的字典请求预期使用未带 schema 的 DATA_SCOPE。若确有外层上下文，须核实合法执行上下文后选定唯一 namespace，不能见库名 eva 就盲删 dictdata::eva::DATA_SCOPE，更不能两种前缀全扫全删。
- **已有定向 Java 能力**：DictCacheHelper.remove("DATA_SCOPE") 只 evict scopedKey；DictService 的 reloadTypesAfterCommit 对受影响 type 捕获当前 schema、提交后只 remove(type)+cachePut(type, snapshot)，reloadTypes 私有方法也逐 type remove+回填。正常获准字典写操作会调用这些定向路径，能够保留事务与 namespace 语义。
- **不是现有定向 HTTP 能力**：当前 DictCtrl 没有公开的单 code evict/reload 接口。不能把已有 Java helper 冒充能直接调用的受支持 HTTP；外部 SQL 恢复也不会触发应用的 afterCommit 回调。本轮不为触缓存而制造无意义 edit/switch 或修改字典其他字段。若用户批准 SQL 精确恢复，设计还须明确受控调用现有 helper 或仅该唯一物理 key 的合法驱逐方式、正确 namespace、DB 提交后时序及失败补偿；没有可用合法调用入口就保持这一执行前置，不自行加端点、开管理接口或启动脚本写 Redis。
- DictService.init/reload() 会 removeAll，reload(Map) 也先 removeAll；DictCacheHelper.removeAll 可能清整个 cache/当前 schema，**均非本次定向替代方案**。remove(code,key) 只是从读出 map 移除条目，没有回写 cache，也不适合作整字典空缓存驱逐。本轮没有调用任何 Redis/缓存写操作。

本轮不改变前端 showall=false、必填范围或默认值；设计方已核的空下拉表现不能通过合成 0000 绕过。恢复获批后仍须独立验证仅支持 5 值、前端失败/空态、合法范围选择、定向缓存回源，以及资源与数据权限门禁；静态链和只读快照不等于这些运行验证通过。

## 5. 操作收尾与交接

唯一 DB 会话验证 transaction_read_only/default_transaction_read_only 为 on，结束前再次为 on，已 rollback/close；查询仅 eva 的两指定字典表必要 catalog、DATA_SCOPE 主项与其 8 明细，没有其他字典、旧 root、个人数据或业务日志。全部命令结束，只写本附件与 backend status，无源码/测试构建/HTTP/UI/Redis/服务/配置/依赖/Git 发布/vault 写入。

恢复 6 行候选、保留 3 未实现项及缓存执行方式交 technical_designer/总控审阅并请用户确认，未执行。组织代码前端真实 Form 根因由 frontend_developer 另核，本成员不越界改动。S0 实际创建/授权仍为 0，旧 QA 与现管理员不变。
