# 包 S：S0 后端只读预检证据与未满足前置

核验时间：首轮 2026-10-24 11:18、用户确认后的续核 11:27–11:28（Asia/Shanghai）；执行者：backend_developer；最新结论：**用户已确认实例有效配置和获准开发库身份，实际只读事务核得 8 条精确资源全部唯一有效、13 个候选全状态匹配数均 0；创建/授权仍为 0，交总控与 QA 审核后续阶段。** 第 1–7 节保留首轮历史边界，身份门禁与“未执行 DB”状态已由第 8–11 节后续证据更新，不能继续当作当前阻塞。

## 1. 授权、当前账本与操作范围

总控本次仅派发 S0 只读核验，依据 `2026-10-23-permission-test-fixtures.md` 第 10 节批准记录及 `quality_reviewer-package-s-20261024.md` 最新现场更新。包 S 获批不等于其预检已满足；包 T、字典契约、新建 tenant/schema、管理员修改、物理删除、配置切换与服务启停均未获本次授权。

本轮只读项目规则、Git、指定设计/QA 证据、必要实体/Mapper、已知启动配置，以及 9016 监听进程的允许元数据；只写本附件及自身 backend status。未浏览/操作 UI、发 HTTP、执行测试构建、写源码、改授权或库、连接 Redis、改配置、启动/停止/重启服务、提交推送或写 vault。没有输出或持久化整条进程命令行、环境、数据库 URL/地址、账号口令、Token、密码哈希或个人数据。

当前分支 cornerstone，原有大量 dirty/untracked 保留，index 空。最新 QA 账本记录 2 账号/5 角色/4 部门/2 样本实际创建全为 0、授权变化 0。本轮未改变这些值。R01/O01/D01/M01/M02 及现有管理员未被用作修改对象。

## 2. 已核实的当前进程证据

| 项目 | 本轮只读结果 | 证据限度 |
| --- | --- | --- |
| 监听 | 本机 TCP 9016，PID 47664，java.exe，复读仍同 PID | 只证明当前监听进程 |
| 启动时间 | 2026-10-24 10:08:31.509 +08:00 | 不据此推断新功能均正确加载 |
| 入口与工作目录 | org.pkaq.WebBooter；user.dir 对应本工作区 EVA-API | 未输出完整命令行 |
| 实际 Java classpath | 含本工作区 web-booter/build/classes/java/main 和 web-booter/build/resources/main；不是 bootJar 启动 | JVM 当前路径证据，不是 Spring Environment 快照 |
| JVM 系统属性 | 只读 jcmd VM.system_properties 成功；允许列表内未发现 profiles/config location/import、eva mode/tenant/data/resource 或 server.port 的 JVM 属性覆盖值 | 不读取/输出其他属性；JVM 属性不包括全部 Spring 来源 |
| 启动参数 | 内部核对实际 @argfile，未命中上述允许的 -D/-- profile/mode/tenant/data/resource 覆盖；无 --spring 参数命中 | 不输出完整 argfile、命令行或 classpath |
| 已知 IDE 配置 | .vscode/launch.json 有 WebBooter 配置；没有 envFile 声明、没有允许字段的显式 env/args 覆盖命中 | 当前文件不是已运行进程环境的证明 |

未访问目标进程内存或导出其环境，未启用 actuator/管理端点、执行诊断 HTTP 或注入代码读取 Spring Bean。故不能排除目标进程环境变量、SPRING_APPLICATION_JSON、其他配置来源或启动后绑定差异；没有可靠 effective 值时明确留空，不从“未发现 -D 覆盖”推导“必为文件值”。

## 3. 实际 classpath 中的配置文件证据

6 个已知文件均在 `web-booter/build/resources/main/`，与 `web-booter/src/main/resources/` 对应源文件 SHA-256 一致，构建副本修改时间均为 2026-10-19 21:50:27 +08:00，早于当前进程启动。读取仅内部提取允许键；使用已知缩进 scalar 键识别，不模拟完整 YAML/Spring profile、placeholder、import 与属性源优先级解析。

| 文件 | 已读文件值或声明 | 是否证明当前 effective |
| --- | --- | --- |
| application.yaml | spring.profiles.default=dev；声明导入 classpath:config/eva/eva.yaml；server.port=9016 | 否；default 不等于 active profile；端口监听独立已证 |
| config/eva/eva.yaml | eva.mode=standalone；eva.tenant.enable=false；eva.tenant.mode=standalone；data-permission.enable=true；resource-permission.enable=true；standalone-schema=eva；core-schema=eva_core | 否；这是实际 classpath 文件值，不能冒充 Spring 绑定结果 |
| application-dev.yaml | PostgreSQL 连接声明，非 loopback；含 currentSchema；其端点与该进程当前已建立的 5432 TCP 连接匹配 | 不证明 active=dev、数据库非生产身份或 SQL schema 实况 |
| application-prod.yaml | 文件存在且与源一致；本轮不取用其连接 | 否；不切库或尝试生产配置 |
| config/eva/eva-dev.yaml、eva-prod.yaml | 对应文件存在且与源一致；上述允许的模式/tenant/data/resource 键未命中额外声明 | 否；不等于这些 profile 当前激活 |

因此当前 effective 元组仍为：`mode=未核实；tenant.enable/mode=未核实；dataPermission.enable=未核实；resourcePermission.enable=未核实`。文件候选元组为 `standalone / false,standalone / true / true`，两者必须分开交付。未采用源码默认值填补运行证据。

## 4. 数据库身份门禁与本次未执行项

当前 JVM 的 5432 已建立连接与 dev 文件所配 PostgreSQL 端点匹配，但它不是本机回环目标。文件名含 dev、库名形态与开发命名相符、当前进程确有相同端点连接，都不足以单独证明该远程实例属于本次获准非生产测试库。

按总控“仅确定当前本地开发 PostgreSQL、身份不明就停”的限制，本轮停在认证前：未建立 psycopg/JDBC/psql 连接，未取用凭据认证，未发 SQL，没有 current_database/current_schema、真实 catalog、资源行或候选计数结果；也没有失败连接重试、凭据猜测、借 QA Token 或改走另一数据库。系统已有只读查询所需驱动不构成连接授权证明，未安装依赖。

以下均是**未核实**，不能记成 0、空集合或“未发现”：真实数据库/当前 schema、目标表列的 catalog 存在性、8 条资源真实 ID/code/tuple/mainId/有效性、相关模块和父模块状态、宽 * 授权、候选全状态匹配数、数据库唯一性、实际初始授权前值。

## 5. 可供后续精确核验的源码字段与 tuple 清单

当前实体/Mapper 明确：ModuleResources → `sys_module_resources`，字段 `ID/CODE/RESOURCE_TYPE/RESOURCE_URL/MAIN_ID/DELETED`；ModuleEntity → `sys_module`，关联 `ID/CODE/PID/DELETED/FROZEN`。资源实体没有独立 FROZEN 字段，不臆造该条件；有效性还依赖父模块未删/未禁用。真实数据库命名、列及类型仍须在已确认目标 schema 的 catalog 中核验后才执行数据 SELECT，不能凭实体或旧种子认定运行表一定存在。

| 集合 | 设计预期 code | method | 精确 path | 真实资源 ID / mainId / 有效状态 |
| --- | --- | --- | --- | --- |
| O_READ | HTTP_ORGANIZATION_GET_LIST | GET | /sys/organization/list | 未核 |
| O_READ | HTTP_ORGANIZATION_GET_GET | GET | /sys/organization/get/{id} | 未核 |
| O_WRITE | HTTP_ORGANIZATION_POST_CHECKUNIQUE | POST | /sys/organization/checkUnique | 未核 |
| O_WRITE | HTTP_ORGANIZATION_POST_EDIT | POST | /sys/organization/edit | 未核 |
| L_READ | HTTP_BIZLOG_GET_LIST | GET | /monitor/log/biz/list | 未核 |
| L_READ | HTTP_BIZLOG_GET_QUERY | GET | /monitor/log/biz/get/{id} | 未核 |
| L_READ | HTTP_ERROR_GET_LIST | GET | /monitor/log/error/list | 未核 |
| L_READ | HTTP_ERROR_GET_QUERY | GET | /monitor/log/error/get/{id} | 未核 |

此表 code 为获批设计的预期值，不是数据库回读；mainId 不抄种子值，不以组织模块已见 ID 替代每行资源 ID。精确授权集合在取得全部真实 ID 并验证 tuple/有效模块前无法形成，当前不提交可执行 grantResource 差异，不追加 OpenAPI 或 method=*、/** 资源。

SysRoleResourceMapper 的实际授权读取使用 `SYS_ROLERES_REF → SYS_MODULE_RESOURCES → SYS_MODULE`，要求资源/模块/角色有效并识别 HTTP method（含 *）；本次只允许后续识别相关模块的宽资源，不因此授予、不复制整个模块或执行角色资源重建。

候选查重源码边界：账号实体对应 `SYS_ACCOUNT.ACCOUNT`，管理档案 code 在 `SYS_ACCOUNT_PROFILE.CODE`，不能猜 `SYS_ACCOUNT.CODE`；RoleEntity 对 code 补 `ROLE_` 并大写，候选输入 QA_R_SELF_20261023 等若作为 code 保存会成为 ROLE_QA_R_SELF_20261023 等。后续只读核验应包含原候选 name/code 与该规范化 code 的匹配数，不能只用 UI 的 QA_R_ 空态判唯一性；不查密码/哈希/Token、真实姓名或联系方式。

| 候选组 | 候选 | 本包创建账本 / 初始授权前值 | 全状态数据库匹配数 |
| --- | --- | --- | --- |
| 账号 2 | QA_LIMIT_20261023、QA_PEER_20261023 | 本包尚未创建；无实际 userId，不能读取或写入授权 | 未核 |
| 角色 5 | QA_R_SELF/DEPT/TREE/CUSTOM/ALL_20261023 | 初始未存在（本包未创建，无实际 roleId）；不能将计划 ∅ 当成数据库既有角色当前资源值 | 未核；仍须排除同名/规范化 code 冲突 |
| 部门 4 | QA_P/D1/D2/D1C_20261023 | 本包尚未创建；无实际组织 ID | 未核 |
| 后续作者样本 2 | QA_SAMPLE_LIMIT/PEER_20261023 | S0 不创建、不代替受测作者 | 未核 |

## 6. 获得实例确认后的最小只读核验顺序（本轮未执行）

1. 只使用已确认的 9016 对应开发连接及实际目标 schema，凭据仅内存使用、不输出或持久化；连接前设只读默认，开启 READ ONLY 事务并验证 transaction_read_only=on，任何不符立即退出并 rollback；无 DDL/迁移/写入/临时表/显式锁操作。
2. 读取 current_database/current_schema 形成允许的无秘密技术指纹；仅在该 schema 的 catalog 核实上文 6 个指定表（含确有需要的 account_profile）与必要列，不扫其他库/schema 或任意表数据。
3. 只按上述 8 个精确 tuple 与预期 code 查技术资源元数据，并通过真实 MAIN_ID 查关联模块/父模块的必要状态；重复、缺失或 code/method/path 不一致即停。仅识别相关模块内宽 * 或通配 path，不创建授权。
4. 对上述专用候选做参数化、全状态存在布尔/匹配数查询，不返回账号 ID、个人信息或密码字段；冲突即停，不覆盖、改名复用或清理旧 QA 对象。未创建角色继续标“初始未存在”，不伪造实际资源集合。
5. 返回脱敏资源技术 ID/精确集合、候选计数与只读事务证据，rollback/关闭连接；交总控和 QA 判断 S0 是否满足，后续创建/授权仍按已批准阶段与合法入口另派。

## 7. 唯一待确认项与交接

最小前置为**同一张当前实例身份卡**：请由当前服务操作者确认“本机 9016/PID 47664 这一实例的已生效 mode、tenant.enable/mode、dataPermission.enable、resourcePermission.enable，以及它当前已连接的 PostgreSQL 确为本次获准开发测试库、非生产”。无需提供口令、Token、整份配置或完整环境；若无法确认该数据库，提供已获准目标的合法本地连接配置来源，而不是聊天凭据。

这不是重问包 S 总授权，也不要求重启、开 actuator 或临时切换配置。确认之前，本轮资源真实 ID/唯一性/授权前值仍缺证，QA 可使用本附件的已证进程、文件来源及精确 tuple 计划，但不能据此开始创建/赋权或将 S0 判通过。只读核验动作已结束，交总控统一收口。


## 8. 用户确认后：实际只读事务指纹

```json
{
  "observedAt": "2026-10-24T03:27:01.444936+00:00",
  "pid": 47664,
  "userConfirmedEffective": {
    "mode": "standalone",
    "tenantEnabled": false,
    "dataPermission": true,
    "resourcePermission": true,
    "databaseIsAuthorizedDevelopment": true
  },
  "fingerprint": {
    "database": "eva",
    "schema": "eva",
    "transactionReadOnly": "on",
    "defaultTransactionReadOnly": "on"
  },
  "rolledBack": true,
  "closed": true
}
```


## 9. 实际 catalog、8 条目标资源及其模块/宽资源

```json
{
  "observedAt": "2026-10-24T03:27:41.271904+00:00",
  "fingerprint": {
    "database": "eva",
    "schema": "eva",
    "transactionReadOnly": "on",
    "defaultTransactionReadOnly": "on"
  },
  "catalog": {
    "sys_module_resources": {
      "deleted": "bigint",
      "id": "bigint",
      "main_id": "bigint",
      "code": "character varying",
      "resource_url": "character varying",
      "resource_type": "character varying"
    },
    "sys_module": {
      "deleted": "bigint",
      "frozen": "integer",
      "pid": "bigint",
      "id": "bigint",
      "code": "character varying"
    },
    "sys_role": {
      "id": "bigint",
      "code": "character varying",
      "name": "character varying"
    },
    "sys_organization": {
      "id": "bigint",
      "name": "character varying",
      "code": "character varying"
    },
    "sys_account": {
      "account": "character varying"
    },
    "sys_account_profile": {
      "account_id": "bigint",
      "code": "character varying"
    }
  },
  "resourceRows": [
    {
      "id": "1000000000000011009",
      "code": "HTTP_ORGANIZATION_POST_CHECKUNIQUE",
      "type": "POST",
      "url": "/sys/organization/checkUnique",
      "mainId": "1000000000000000102",
      "deleted": 0
    },
    {
      "id": "1000000000000011010",
      "code": "HTTP_ORGANIZATION_GET_LIST",
      "type": "GET",
      "url": "/sys/organization/list",
      "mainId": "1000000000000000102",
      "deleted": 0
    },
    {
      "id": "1000000000000011011",
      "code": "HTTP_ORGANIZATION_GET_GET",
      "type": "GET",
      "url": "/sys/organization/get/{id}",
      "mainId": "1000000000000000102",
      "deleted": 0
    },
    {
      "id": "1000000000000011012",
      "code": "HTTP_ORGANIZATION_POST_EDIT",
      "type": "POST",
      "url": "/sys/organization/edit",
      "mainId": "1000000000000000102",
      "deleted": 0
    },
    {
      "id": "1000000000000011050",
      "code": "HTTP_BIZLOG_GET_QUERY",
      "type": "GET",
      "url": "/monitor/log/biz/get/{id}",
      "mainId": "1000000000000000202",
      "deleted": 0
    },
    {
      "id": "1000000000000011051",
      "code": "HTTP_BIZLOG_GET_LIST",
      "type": "GET",
      "url": "/monitor/log/biz/list",
      "mainId": "1000000000000000202",
      "deleted": 0
    },
    {
      "id": "1000000000000011052",
      "code": "HTTP_ERROR_GET_QUERY",
      "type": "GET",
      "url": "/monitor/log/error/get/{id}",
      "mainId": "1000000000000000203",
      "deleted": 0
    },
    {
      "id": "1000000000000011053",
      "code": "HTTP_ERROR_GET_LIST",
      "type": "GET",
      "url": "/monitor/log/error/list",
      "mainId": "1000000000000000203",
      "deleted": 0
    }
  ],
  "relatedModules": [
    {
      "id": "1000000000000000100",
      "code": "sys",
      "parentId": "0",
      "deleted": 0,
      "frozen": 0
    },
    {
      "id": "1000000000000000102",
      "code": "sys.organization",
      "parentId": "1000000000000000100",
      "deleted": 0,
      "frozen": 0
    },
    {
      "id": "1000000000000000200",
      "code": "log",
      "parentId": "0",
      "deleted": 0,
      "frozen": 0
    },
    {
      "id": "1000000000000000202",
      "code": "log.biz",
      "parentId": "1000000000000000200",
      "deleted": 0,
      "frozen": 0
    },
    {
      "id": "1000000000000000203",
      "code": "log.error",
      "parentId": "1000000000000000200",
      "deleted": 0,
      "frozen": 0
    }
  ],
  "relatedWideResources": [
    {
      "id": "1000000000000001102",
      "code": "MENU_SYS_ORGANIZATION",
      "type": "*",
      "url": "/sys/organization",
      "mainId": "1000000000000000102",
      "deleted": 0
    },
    {
      "id": "1000000000000001401",
      "code": "API_SYS_POST",
      "type": "*",
      "url": "/sys/post/**",
      "mainId": "1000000000000000102",
      "deleted": 0
    },
    {
      "id": "1000000000000001410",
      "code": "API_SYS_ORGANIZATION",
      "type": "*",
      "url": "/sys/organization/**",
      "mainId": "1000000000000000102",
      "deleted": 0
    },
    {
      "id": "1000000000000001202",
      "code": "MENU_LOG_BIZ",
      "type": "*",
      "url": "/log/biz",
      "mainId": "1000000000000000202",
      "deleted": 0
    },
    {
      "id": "1000000000000001405",
      "code": "API_LOG_BIZ",
      "type": "*",
      "url": "/monitor/log/biz/**",
      "mainId": "1000000000000000202",
      "deleted": 0
    },
    {
      "id": "1000000000000001203",
      "code": "MENU_LOG_ERROR",
      "type": "*",
      "url": "/log/error",
      "mainId": "1000000000000000203",
      "deleted": 0
    },
    {
      "id": "1000000000000001406",
      "code": "API_LOG_ERROR",
      "type": "*",
      "url": "/monitor/log/error/**",
      "mainId": "1000000000000000203",
      "deleted": 0
    }
  ],
  "finalTransactionReadOnly": "on",
  "rolledBack": true,
  "closed": true
}
```


## 10. 专用候选全状态存在性参数化计数

```json
{
  "observedAt": "2026-10-24T03:28:10.690238+00:00",
  "fingerprint": {
    "database": "eva",
    "schema": "eva",
    "transactionReadOnly": "on",
    "defaultTransactionReadOnly": "on"
  },
  "counts": [
    {
      "kind": "account",
      "candidate": "QA_LIMIT_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "account",
      "candidate": "QA_PEER_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "role",
      "candidate": "QA_R_SELF_20261023",
      "normalizedCode": "ROLE_QA_R_SELF_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "role",
      "candidate": "QA_R_DEPT_20261023",
      "normalizedCode": "ROLE_QA_R_DEPT_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "role",
      "candidate": "QA_R_TREE_20261023",
      "normalizedCode": "ROLE_QA_R_TREE_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "role",
      "candidate": "QA_R_CUSTOM_20261023",
      "normalizedCode": "ROLE_QA_R_CUSTOM_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "role",
      "candidate": "QA_R_ALL_20261023",
      "normalizedCode": "ROLE_QA_R_ALL_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "organization",
      "candidate": "QA_P_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "organization",
      "candidate": "QA_D1_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "organization",
      "candidate": "QA_D2_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "organization",
      "candidate": "QA_D1C_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "organization",
      "candidate": "QA_SAMPLE_LIMIT_20261023",
      "allStateMatches": 0
    },
    {
      "kind": "organization",
      "candidate": "QA_SAMPLE_PEER_20261023",
      "allStateMatches": 0
    }
  ],
  "allCandidatesAbsent": true,
  "finalTransactionReadOnly": "on",
  "rolledBack": true,
  "closed": true,
  "actualCreated": 0,
  "actualGrants": 0
}
```


## 11. QA 可用精确清单、差异及最终交接

用户经总控明确确认当前实例身份卡“正确”：effective mode=standalone、tenant.enable=false、dataPermission.enable=true、resourcePermission.enable=true，当前非 loopback PostgreSQL 确为本次获准开发测试库。该有效配置结论来源为用户确认，区别于早先文件值推断。续核前后 9016 都仍为 PID47664、原 10:08:31 启动的 WebBooter；目标配置与源一致，实际连接指纹为 database=eva/schema=eva，未改用其他库。

实际数据库证据为 3 个已完成的 psycopg2 只读会话（11:27:01、11:27:41、11:28:10）。每次连接即设置 default_transaction_read_only=on，首次事务只读，验证 transaction_read_only/default 均 on；资源和候选会话末尾再次确认只读；每次显式 rollback 后 close。catalog 只查 eva 中 6 个指定表的必要列，未读取其他用户权限、个人信息或业务日志正文。最初 psycopg3 脚本 exit1 未给出完整结果，不作 SQL 成功证明；未安装/升级驱动，改用本机已安装 psycopg2 取得以上可核证据。无数据库写入、DDL/迁移、显式锁操作、临时表或资源/授权重建。

以下均为本轮运行数据库真实回读 ID（用字符串保留 bigint 精度），不是从种子推定：

| 集合 | 真实 resource ID | 实际 code | method / path | 实际 mainId |
| --- | --- | --- | --- | --- |
| O_READ | 1000000000000011010 | HTTP_ORGANIZATION_GET_LIST | GET /sys/organization/list | 1000000000000000102 |
| O_READ | 1000000000000011011 | HTTP_ORGANIZATION_GET_GET | GET /sys/organization/get/{id} | 1000000000000000102 |
| O_WRITE | 1000000000000011009 | HTTP_ORGANIZATION_POST_CHECKUNIQUE | POST /sys/organization/checkUnique | 1000000000000000102 |
| O_WRITE | 1000000000000011012 | HTTP_ORGANIZATION_POST_EDIT | POST /sys/organization/edit | 1000000000000000102 |
| L_READ | 1000000000000011051 | HTTP_BIZLOG_GET_LIST | GET /monitor/log/biz/list | 1000000000000000202 |
| L_READ | 1000000000000011050 | HTTP_BIZLOG_GET_QUERY | GET /monitor/log/biz/get/{id} | 1000000000000000202 |
| L_READ | 1000000000000011053 | HTTP_ERROR_GET_LIST | GET /monitor/log/error/list | 1000000000000000203 |
| L_READ | 1000000000000011052 | HTTP_ERROR_GET_QUERY | GET /monitor/log/error/get/{id} | 1000000000000000203 |

8 个预期 code 或精确 URL 的全状态限定查询合计只有这 8 行；各 tuple 精确匹配 1 行，均 DELETED=0。关联模块 sys.organization、log.biz、log.error 及其父模块 sys、log 均 DELETED=0/FROZEN=0，父关联完整。当前 `docs/sql/module_resource_seed_routes_20261013.json` 的这 8 条记录，逐项 ID/code/type/url/main_id 与运行回读相同，差异 0；仅限这 8 条，不推广为全库种子一致、全局授权一致或 HTTP 验收通过。

可给 QA 的精确候选集合：

- O_READ = {"1000000000000011010", "1000000000000011011"}。
- O_WRITE = {"1000000000000011009", "1000000000000011012"}。
- L_READ = {"1000000000000011050", "1000000000000011051", "1000000000000011052", "1000000000000011053"}。

关联模块内识别到 7 条宽资源，**全部排除，不得作为本包对应授权项或用整模块勾选复制**：

| 真实 ID | 实际 code | type / path | mainId |
| --- | --- | --- | --- |
| 1000000000000001102 | MENU_SYS_ORGANIZATION | * /sys/organization | 1000000000000000102 |
| 1000000000000001401 | API_SYS_POST | * /sys/post/** | 1000000000000000102 |
| 1000000000000001410 | API_SYS_ORGANIZATION | * /sys/organization/** | 1000000000000000102 |
| 1000000000000001202 | MENU_LOG_BIZ | * /log/biz | 1000000000000000202 |
| 1000000000000001405 | API_LOG_BIZ | * /monitor/log/biz/** | 1000000000000000202 |
| 1000000000000001203 | MENU_LOG_ERROR | * /log/error | 1000000000000000203 |
| 1000000000000001406 | API_LOG_ERROR | * /monitor/log/error/** | 1000000000000000203 |

这些是既有元数据，不是本轮新增授权。尤其 API_SYS_POST 虽指向岗位路径，MAIN_ID 实际关联组织模块，进一步说明不能按模块整体授予。未修其归属或改表名，未因 `_line/_ref` 规则迁移存量表。

候选全状态查重：2 个账号按 SYS_ACCOUNT.ACCOUNT 精确参数化匹配，5 个角色按候选 name、原 code 与规范化 ROLE_QA_R_* code 匹配，4 部门及 2 作者样本按 code/name 匹配，13 个候选 count 全为 0；未按 DELETED/FROZEN 隐藏旧记录。五角色前值现在可记为“初始未存在，无实际 roleId/资源关联”，不是查询到既有角色的空资源集合。账号/部门/样本仍无实际 ID，不能预造。未查询任意既有管理员或历史 QA 的授权。

S0 本轮只读资源/候选证据缺口已补齐，没有缺行、重复、禁用或候选冲突；7 条宽资源只列排除项。实际创建 0、授权 0、无需回收。尚未证明新身份登录/请求权限、缓存撤销、组织父节点与必填可用性、资源精确集合后的菜单可达性或 S1–S8 行为；总控与 QA 后续只按获批阶段、合法入口和用户本地密码接管推进，遇菜单缺失不补宽资源。上述是时间点快照，不持续加锁、不保证之后无人并发修改，创建/授权前仍需合法入口即时回读与唯一性校验。

本轮只写本附件及 backend status，所有连接/命令已结束；未 HTTP/UI/Redis/服务启停、源码/配置/依赖/构建测试、提交推送或 vault 写入。后续行动由总控派发，本次不执行创建、grant、状态切换或清理。


## 12. 新实例 CUSTOM/ALL 角色及账号短名候选只读补证（13:55）

本节为2026-10-24北京时间13:55实际后续快照，更新本附件早期“创建0”的历史状态；原首轮和早期计数仍仅对应其原时间点，不改写历史。总控依据QA正常页面已创建4组织+5角色=9对象，本成员只对最新CUSTOM/ALL两个指定roleId及两个精确账号名进行本次元数据核验；未重新查询或改动旧七对象，未代创账号、作者样本或授予资源/用户。

9016监听新PID39300、java，启动时间2026-10-24 13:43:51 +08:00，只输出允许的PID/时间/进程名。approved开发连接取当前build/resources/main/application-dev.yaml，读取时与源application-dev.yaml内容一致；新PID现存PostgreSQL连接端点与该配置目标内部比较一致，不输出地址/URL/账号/口令、命令行或环境。沿既有用户确认的开发库授权，本次driver实际指纹database=eva/schema=eva，事务及默认只读均on，末尾再核on/on后rollback/close。此处不声称独立读取了Spring effective Environment；运行加载与实际页面创建成功来自QA，非本成员HTTP验证。

以下全是本轮实际只读结果，角色和关联查询都用固定两个roleId参数；关联未按deleted等过滤，取得两个角色的完整资源/用户技术集合。账号采用WHERE account=%s精确参数化count，无deleted/frozen过滤，不取密码/哈希/个人资料。候选全状态计数都是0；不是账号已创建或已登录，也不是并发锁，后续正常提交仍应即时回读唯一性。



```json
{
  "observedAt": "2026-10-24T05:55:35.971678+00:00",
  "writes": 0,
  "listener": {
    "port": 9016,
    "pid": 39300,
    "process": "java",
    "startedAtAsiaShanghai": "2026-10-24T13:43:51+08:00"
  },
  "configuredSource": "web-booter/build/resources/main/application-dev.yaml; exact content matches source; approved target matches new PID existing PostgreSQL peer",
  "fingerprint": {
    "database": "eva",
    "schema": "eva",
    "transactionReadOnly": "on",
    "defaultTransactionReadOnly": "on"
  },
  "roles": [
    {
      "id": "2113870080799694849",
      "name": "QA_R_CUSTOM_20261023",
      "code": "ROLE_QA_R_CUSTOM_20261023",
      "tenant_id": null,
      "frozen": 0,
      "deleted": 0,
      "data_scope": "0003",
      "data_org_ids": "2113856917966462977"
    },
    {
      "id": "2113870419179364353",
      "name": "QA_R_ALL_20261023",
      "code": "ROLE_QA_R_ALL_20261023",
      "tenant_id": null,
      "frozen": 0,
      "deleted": 0,
      "data_scope": "0000",
      "data_org_ids": null
    }
  ],
  "completeAssociationsForExactlyTwoRoleIds": {
    "sys_roleres_ref": [],
    "sys_roleuser_ref": []
  },
  "exactAccountCandidateAllStateCounts": [
    {
      "candidate": "QA_LIM_20261023",
      "length": 15,
      "allStateMatches": 0
    },
    {
      "candidate": "QA_PEER_20261023",
      "length": 16,
      "allStateMatches": 0
    }
  ],
  "finalReadOnlySettings": [
    "on",
    "on"
  ],
  "rolledBack": true,
  "closed": true,
  "scope": "Only two specified role IDs, their complete two relation sets, and two exact account names; old seven objects not queried or changed"
}
```


CUSTOM实际scope0003、data_org_ids恰D2单一字符串2113856917966462977；ALL scope0000、data_org_ids实际NULL。两行tenant_id均NULL、frozen0/deleted0，符合当前普通插入约定，不回填0。SYS_ROLERES_REF与SYS_ROLEUSER_REF在这两个角色ID范围内完整集合均空；与先前只读的SELF/DEPT/TREE空关联证据组合，可补足五个新角色的初始关联账本，但不冒充本次重查全部五角色。

总控指定的LIMIT替代候选为QA_LIM_20261023，实际长度15；PEER精确候选QA_PEER_20261023，长度16，两者当前全状态匹配数均0。原QA_LIMIT_20261023长度17、现输入约束4–16的发现来源QA已记录，未改前后端contract。本次只替换同用途唯一测试账号候选，不扩账号数量或权限；本成员没有进入密码页面、填写联系方式或保存账号。

原始安全结果JSON位于忽略目录.gradle/package-s-newroles-accounts-readonly-20261024.json，SHA256 FCE2BE7CA8D39CCC5A3745C42D49940BC707F112B3893B2E5DE035104E7D4230。只写此S0附件及本人status；所有连接/命令已结束，DB/HTTP/Redis写0，未HTTP/Redis调用、编译测试/源码配置/服务启停、前端/vault/QA卡/提交推送。结果同步总控和指定QA会话；账号本地资料及密码接管仍由总控/用户推进。


## 13. 用户亲存PEER后的部门与完整空关联只读补证（15:32）

当前9016 PID49000/java、14:48:04启动，历史39300旧探针先安全停止未连库。UTC07:32:59.926753在获准eva/eva READ ONLY核PEER2113894345662021633：账号tenant0/frozen0/deleted0，档案共享ACCOUNT_ID同值，DEPT_ID实际D1=2113856614139469826而非NULL；完整SYS_ROLEUSER_REF/SYS_POSTUSER_REF按该USER_ID不加过滤均空，无意外admin或非空。D1及五S role状态有效、原scope/CUSTOM仅D2保持。档案没有独立ID/tenant/frozen/deleted，两关系无deleted，不能伪造过滤后“空”为完整空。

详细JSON/技术列、deptId写读链/VO无deptName、Long字符串与树字段、19class+4XML首来源同当前输出，见2026-10-24-package-s-peer-department-readonly.md。role候选GET /sys/role/list返回PageVo.records，无scope字段；账号已选role空不能推出可选列表空。未回填部门/改授权/创建第二账号或代填个人资料；事务on/on末尾复核后rollback/close。该诊断不是页面修复或S1–S8完成。
