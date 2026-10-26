# 包 S：七对象元数据与 CUSTOM 未确认保存的只读诊断

日期2026-10-24，backend_developer；本轮只读，无数据/HTTP/Redis写、无源码/服务/测试构建/授权变更。实际UI事件依据 QA 最新S账本：四组织、三个角色已回读；CUSTOM提交前DOM仅选D2、scope0003，按钮loading结束表单未关，取消后UI查无记录，未取得HTTP状态/请求体，不猜4200或成功/失败次数。ALL及账号/作者样本未由本成员创建。

当前9016仍PID47664、10:08:31启动的同一WebBooter；沿用户已确认的开发eva/eva，只允许以下指定七ID、CUSTOM原/规范化code/name及三个角色关联技术ID。审计仅技术create/modify ID与时间，不读个人名称/联系方式、密码/Token或日志正文。数据库时间按实际列原字符串记录，不凭UTC字段名补时区。


## 1. 当前指定对象、关联及D2限定谓词只读结果

```json
{
  "observedAt": "2026-10-24T05:08:14.921082+00:00",
  "fingerprint": {
    "database": "eva",
    "schema": "eva",
    "transactionReadOnly": "on",
    "defaultTransactionReadOnly": "on"
  },
  "organizations": [
    {
      "id": "2113856261050376193",
      "code": "QA_P_20261023",
      "name": "QA_P_20261023",
      "pid": "0",
      "path": "/2113856261050376193",
      "frozen": 0,
      "deleted": 0,
      "tenant_id": null,
      "sort": 13.0,
      "create_id": "1000000000000000005",
      "modify_id": null,
      "utc_create": "2026-10-24T12:53:00.897298",
      "utc_modify": null
    },
    {
      "id": "2113856614139469826",
      "code": "QA_D1_20261023",
      "name": "QA_D1_20261023",
      "pid": "2113856261050376193",
      "path": "/2113856261050376193/2113856614139469826",
      "frozen": 0,
      "deleted": 0,
      "tenant_id": null,
      "sort": 1.0,
      "create_id": "1000000000000000005",
      "modify_id": null,
      "utc_create": "2026-10-24T12:54:25.077863",
      "utc_modify": null
    },
    {
      "id": "2113856917966462977",
      "code": "QA_D2_20261023",
      "name": "QA_D2_20261023",
      "pid": "2113856261050376193",
      "path": "/2113856261050376193/2113856917966462977",
      "frozen": 0,
      "deleted": 0,
      "tenant_id": null,
      "sort": 2.0,
      "create_id": "1000000000000000005",
      "modify_id": null,
      "utc_create": "2026-10-24T12:55:37.508775",
      "utc_modify": null
    },
    {
      "id": "2113857168370606081",
      "code": "QA_D1C_20261023",
      "name": "QA_D1C_20261023",
      "pid": "2113856614139469826",
      "path": "/2113856261050376193/2113856614139469826/2113857168370606081",
      "frozen": 0,
      "deleted": 0,
      "tenant_id": null,
      "sort": 1.0,
      "create_id": "1000000000000000005",
      "modify_id": null,
      "utc_create": "2026-10-24T12:56:37.219972",
      "utc_modify": null
    }
  ],
  "roles": [
    {
      "id": "2113857683657629697",
      "code": "ROLE_QA_R_SELF_20261023",
      "name": "QA_R_SELF_20261023",
      "frozen": 0,
      "deleted": 0,
      "tenant_id": null,
      "data_scope": "0007",
      "data_org_ids": null,
      "sort": 0.0,
      "create_id": "1000000000000000005",
      "modify_id": null,
      "utc_create": "2026-10-24T12:58:40.018473",
      "utc_modify": null
    },
    {
      "id": "2113858118523068418",
      "code": "ROLE_QA_R_DEPT_20261023",
      "name": "QA_R_DEPT_20261023",
      "frozen": 0,
      "deleted": 0,
      "tenant_id": null,
      "data_scope": "0001",
      "data_org_ids": null,
      "sort": 0.0,
      "create_id": "1000000000000000005",
      "modify_id": null,
      "utc_create": "2026-10-24T13:00:23.710949",
      "utc_modify": null
    },
    {
      "id": "2113858458634985474",
      "code": "ROLE_QA_R_TREE_20261023",
      "name": "QA_R_TREE_20261023",
      "frozen": 0,
      "deleted": 0,
      "tenant_id": null,
      "data_scope": "0002",
      "data_org_ids": null,
      "sort": 0.0,
      "create_id": "1000000000000000005",
      "modify_id": null,
      "utc_create": "2026-10-24T13:01:44.790171",
      "utc_modify": null
    }
  ],
  "customCandidateAllStateCount": 0,
  "customCandidateRows": [],
  "roleAssociationsCompleteForThreeIds": {
    "sys_roleres_ref": [],
    "sys_roleuser_ref": []
  },
  "d2PredicateBindings": {
    "id": "2113856917966462977",
    "standaloneRequiredTenantId": "0",
    "deleted": 0
  },
  "d2ActiveIdOnlyMatches": [
    "2113856917966462977"
  ],
  "d2ActiveTenantZeroMatches": [],
  "writes": 0,
  "rolledBack": true,
  "closed": true
}
```


## 2. 指定两表租户列的必要技术元数据

```json
{
  "fingerprint": {
    "database": "eva",
    "schema": "eva",
    "transactionReadOnly": "on",
    "defaultTransactionReadOnly": "on"
  },
  "targetTenantColumnMetadata": [
    {
      "table": "sys_organization",
      "column": "tenant_id",
      "nullable": "YES",
      "default": "0"
    },
    {
      "table": "sys_role",
      "column": "tenant_id",
      "nullable": "YES",
      "default": "0"
    }
  ],
  "enabledCustomTriggerRows": [],
  "writes": 0,
  "rolledBack": true,
  "closed": true
}
```


## 3. 本次七对象与CUSTOM候选结论

第1节UTC05:08:14.921082Z（北京时间13:08:14）的READ ONLY快照确认：四组织和三角色七个真实ID均存在、DELETED=0/FROZEN=0，TENANT_ID全为NULL。P为根，D1/D2的PID为P，D1C的PID为D1；PATH完整按 /P、/P/D1、/P/D2、/P/D1/D1C，与QA结构一致，SORT分别13/1/2/1。三个角色规范化CODE与QA一致，scope为0007/0001/0002，DATA_ORG_IDS实际是NULL，不伪装成已存空数组；各SORT=0。create/modify技术ID与实际时间详见JSON，不将创建人技术ID解读为其实际权限。

只按三个新role_id查询SYS_ROLERES_REF和SYS_ROLEUSER_REF完整技术集合，两表结果都为空，故这三个角色的初始资源/用户关联为空已补证。未读其它角色或任意既有用户权限。

CUSTOM原code QA_R_CUSTOM_20261023、规范化ROLE_QA_R_CUSTOM_20261023及候选name全状态匹配合计0，当前没有持久化CUSTOM角色行；不编造实际ID、不把UI表单值记成落库成功，也不重试提交。此时间点证据不回溯断言该次HTTP请求是否发出、哪个异常或状态。

## 4. 参数绑定、事务与实际编译路径

- RoleAoeBo.dataScope与dataOrgIds都是String，后者契约为英文逗号分隔ID。当前前端role/aoeform提交读取form dataOrgIds，数组join(',')；service.saveRole对数组再做join，已是String则保留，同时要求真实非空dataScope，不合成ALL。本轮只读这段数据绑定，未修改前端；源码join路径不等于取得该次实际payload。
- 指定D2字符串2113856917966462977是合法正Long值。后端split(',',-1)逐段trim、要求[0-9]+、Long.parseLong并>0，0003必须非空集合；空段、非数字、越界或空集合都会PARAM_ERROR，不能将label选中当成这些实际值已送达。未读取真实payload日志。
- RoleCtrl POST /sys/role/edit把@RequestBody RoleAoeBo交editUniqueCode。该方法@Transactional(rollbackFor=Exception, REQUIRED)+@TenantSchema，先非空code/name、加载编辑原值，再validateDataScope，之后才查唯一、Convert、insertOrUpdate。更新已有角色后才走受影响用户permVer；校验前不会授资源/用户。若此次确在该校验拒绝分支，无角色插入；本轮只证明源码顺序和当前CUSTOM行不存在，不伪造历史异常。
- 0004/5/6新授仍拒绝；编辑缺scope保留原scope/orgIds；0003既有相同scope但缺orgIds会保留原值。本案是新建目标，不可借保留逻辑省略组织集合。

只读JVM classpath首解析RoleService与MybatisMetaObjectHandler均为当前模块build/classes/java/main，字节与当前输出一致。RoleService SHA256=d8ca70bb04643b00bd6d77e46e0182184c4da0e34d5c326ad78ee0342a161202；MetaObjectHandler SHA256=1daad24a5a0fd2140dd600648e34bf4748530b5de04aa4db258561b4be949474。javap证实editUniqueCode的调用顺序为validateDataScope→isUnique→fromBo→insertOrUpdate；validateDataScope编译路径包含String.split/Long.parseLong、isStandaloneMode、standalone lconst_0、非standalone ThreadUserHelper.getTenantId、OrgMapper.selectList及visible Set.equals，不只是读未编译源码。此为实际classpath可解析磁盘字节证据，不声称dump了运行内存或那次请求参数。

## 5. 已证实的阻断条件：NULL与0不等价

当前validateDataScope对orgIds非空时，按提交ID集合查OrganizationMapper，并强制：standalone时TENANT_ID=0，否则TENANT_ID=可信ThreadUser tenant。随后要求visibleIds完整等于orgIds，不能静默过滤部分非法项。当前可信模式已由用户确认为standalone，所以该分支比较值是常量0，不取客户端tenant字段；本轮未读当前ThreadUser/任意授权。

对唯一D2的实际只读等价查询：

```sql
SELECT id FROM eva.sys_organization WHERE id = %s AND deleted = 0;
-- binding: 2113856917966462977 -> 1行
SELECT id FROM eva.sys_organization
WHERE id = %s AND tenant_id = %s AND deleted = 0;
-- bindings: 2113856917966462977, 0 -> 0行
```

D2的TENANT_ID为NULL，SQL中NULL=0不为true。故**如果实际payload为0003且仅该D2，当前强制tenant=0本身足以使visibleIds为空并拒绝**，即使数据权限是ALL也不能使该WHERE项为true；不必猜是否又被数据插件缩小。这个确定阻断条件与现场现象吻合，但缺少真实payload/响应时不能断言该次具体分支、HTTP状态或错误码已经被运行证实。

源分支抛CommonCodes.PARAM_ERROR（0x0400-00002，默认请求参数错误），不是以4200命名的此分支。真实请求可能更早因绑定/认证/其它条件失败；不得由静态code把它登记成已取得的HTTP错误。

数据插件仍独立存在：当前OrgMapper的selectList会按可信ThreadUser/data scopes决定可见性；空scope拒绝、受限作用创建/修改者/部门、ALL也只是不追加限制，不应绕过当前租户/来源判定。不得通过给测试主体ALL、全局Ignore或删除所有tenant/data guard来让CUSTOM保存。

## 6. 插入惯例证据与最小安全纠正边界

当前Organization/Role BO不提供tenantId，生成的Convert和对应新增Service未设置tenantId。StdEntity.tenantId实际编译注解为@TableField(fill=INSERT)；当前MetaObjectHandler文档明确standalone/schema均不自动填tenantId，insertFill字节码只填创建/时间/sort/frozen/deleted等，没有tenantId。已加载MyBatis-Plus 3.5.17的TableFieldInfo.getInsertSqlPropertyMaybeIf/ColumnMaybeIf字节码显示withInsertFill=true时直接保留该列/参数，不走普通非空条件。因此存在“NULL字段被作为填充列发送、数据库默认值未采用”的明确生成路径；本轮没有实际INSERT日志，不把静态路径冒充抓到了当次SQL。

实际两表tenant_id均nullable YES/default 0，没有启用自定义触发器记录。数据库默认0不会把已显式传入的NULL自动转成0；所以“有DEFAULT0就必定新行=0”不成立。更关键的是，不自动填tenantId是现有源码明文的schema隔离惯例，不能仅见NULL就擅自判这七行损坏并批量改0。

交technical_designer复核的最小候选是**使0003组织有效性校验与既有运行模式/可信来源契约一致**：standalone限已核唯一可信schema内的全部指定ID、未删和实际可见性，不因假定tenant列必为0误拒；正tenant/其它模式的可信tenant/schema隔离保持，不能把NULL解释为正tenant通配或全局OR NULL。全部ID须合法且完整可见，不能只保留命中的部分。具体代码由总控正式写锁和设计结论确定，本轮不修改。

若改采“所有standalone新行必须写tenant0”的另一政策，将涉及创建填充约定及存量这七行归属调整，需明确契约/具体ID前后值与独立批准，不能作为本轮只读诊断的默认修复。既不修七行，也不删除或改冻/排序/授予；R字典与缓存已通过，与本问题无关，不再修改。

## 7. 后续合法取证与交接

最小缺口为正常合法表单请求的安全摘要：dataScope是否确0003，dataOrgIds是否精确D2字符串，业务success/错误code、HTTP状态及表单错误处理；不要求Token/Cookie、个人资料、密码或旧日志正文。没有证据时不重复创建CUSTOM/ALL来测试，也不清理七个已创建对象。包S仍有七对象实际存在、CUSTOM全状态不存在、无关联授予，等待总控决定最小代码修复与写锁后再复验。

两次PG元数据/对象核验均READ ONLY、default on、rollback/close；没有DB/HTTP/Redis写或源码/服务/依赖/测试构建/发布/vault操作。仅本人status与此附件；全部命令连接已结束，交总控与technical_designer。当前结果不是当次HTTP异常或完整S0–S8权限验收通过。
