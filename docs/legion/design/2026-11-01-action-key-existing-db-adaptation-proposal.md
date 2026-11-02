# ACTION_KEY 现库适配记录（已批准执行，独立后验通过）

## 2026-11-02 当前状态

用户已明确批准精确现库适配及由总控重启原 dev 服务。固定写窗口 `1bd6ad61b18343dc8ddd70e57812968b` 已仅执行一次，结果为 `ACTION_KEY_FIXED_MIGRATION_COMMITTED`，`dbCommitted=true`、单连接已关闭，全部 SQL 前像及提交前精确后像守卫通过，无自动重试、缓存或服务操作。执行结果 `.gradle/native-self-bridge-70703ddaf2364e4fa612f98c92d2da48/action-key-execution-result.json`，SHA256 `96CCC674E49608C1796E2EE67833744DE938438DBCD7D231807C7AA4CAC2D57D`。

实际完成：新增 nullable `ACTION_KEY VARCHAR(64)` 无默认值；映射 36 条旧资源 key；插入 78 条精确资源；仅为角色 `1000000000000000004`、tenant 0 新增 65 条引用，原 23 条保留、合计 88 条，覆盖 43 个不同菜单/key。13 条辅助资源不新增引用，其 key 由已引用的主动作行提供，HTTP 资格仍由保留的原授权覆盖。被排除角色的原 23 条引用及 tenant 1 原 23 条能力引用保持，新租户引用为 0。未重放 V1、未修改原 code/path/method、账号、角色配置或 permVer。

独立 QA 后验 `.gradle/native-self-bridge-778ac4d2557b4c07b65111bbd9d6a832/qa-action-key-postcheck-result.json`，SHA256 `E61D09487579DF6B03F911F63BEA6392D61E5219527D5524E5DF3F3EBCA1FB12`；查询清单 SHA256 `5C3DF1317A2E28B6324550F04BE40EF57153B698A1BD357837D3880E5054A272`。结果 `ACTION_KEY_INDEPENDENT_READONLY_POSTCHECK_MATCHED`，单连接、6 SELECT、起止双只读 on、REPEATABLE READ、0 写入、回滚关闭；列定义、36 旧 key、原 78 技术投影、78 新资源及引用全部匹配。当前物理资源 156 条，有效角色引用为原 46 加新 65 的精确集合；角色 4 为 88 条，排除角色及租户能力引用各仍为 23 条。

总控报告原 9016 dev 服务已重启，PID 46744 → 39228，Flyway 关闭；无凭据请求确认服务按原认证门禁拒绝匿名访问。Edge 连接器两次被另一扩展界面占用阻止后已停止。用户随后手工反馈“能看到所有按钮”，构成当前 EVA 角色页新 key 接线的实际页面证据；未明确反馈已打开详情窗口，不称详情交互、其他页面或完整运行矩阵通过。数据库后验、匿名门禁和这项用户页面反馈各自保留证据边界。

以下清单保留执行前技术前像和原审查依据，表中数量及引用计数是迁移前值，不能再当作当前未适配状态。最终固定包为 `.gradle/action-key-20261101/review-package.json`，SHA256 `89EA9584FFC5E41F18C101932DE141DA59EE5FA40DD75FD1CD888AB6CBA891CA`；产品最终冻结为 `D46BE3710B1299994F31C835E337484841BF78AE4564079EDD01F444AE70566D`。回滚候选仍需另行批准，不自动执行。

## 已完成的技术前像

2026-11-01 22:16:34.931084 至 .986329 +08，已沿既有开发库授权完成一次最小只读盘点。目标仍为批准 dev 指纹 d22bc534e71c，eva/eva、standalone，源码与构建配置一致；单连接、起止双只读 on、REPEATABLE READ，共四条 SELECT，0 写入、ROLLBACK/关闭完成。运行进程覆盖配置未额外核验，不冒充运行态配置已证。未读取个人账号、角色详情、应用令牌或密码，未操作 Redis/服务。

受限结果：`.gradle/native-self-bridge-5063fc429e32499482a6608b5d57b25a/action-key-inventory-result.json`，SHA256 `FC8C4FF6B95CF12BCAA93064490DDA12DA7DC18CBCAEC5580F59A7F413B0AF87`；同目录查询清单 SHA256 `8D1E991100B75A0CDB94F0BF96E3D13E8A4C03F540ABEF12BEF20FEF31F906C5`。结果仅保存资源技术投影和引用计数。

执行前资源表没有 ACTION_KEY，物理 78 行、有效 77 行。原细粒度 HTTP 行的角色/租户引用计数均为 0，旧 MENU/API 行每行角色引用 2、租户引用 1，因此最终获批方案除加列及映射外，另有上述固定新增资源和仅角色 4 的 65 条引用；未向另一角色或租户自动补授权。

## 已执行加列的审查依据

已执行 `ALTER TABLE eva.sys_module_resources ADD COLUMN action_key VARCHAR(64) NULL`，无默认值或新增唯一约束。执行时固定守卫已核列缺失和技术前像未漂移。schema 模式资源位于真实 core，不为租户私有模板新造资源表。保留全部原 ID、CODE、HTTP 方法路径、原唯一索引及引用。

## 已执行的 36 条旧资源单值映射

以下逐行映射基于已核真实端点及已定动作，不依据 GET 通用推断或旧 code 前缀；已在固定事务内执行并经独立后验确认。执行绑定完整前像，不使用仅按路径批量更新；表中引用 0/0 为执行前值，这组旧行未新增角色或租户引用。

| 资源ID | MAIN_ID | 原CODE | 方法与路径 | 已执行actionKey | 执行前角色/租户引用 |
|---|---|---|---|---|---|
| 1000000000000011002 | 1000000000000000101 | HTTP_USER_POST_DEL | POST /sys/account/del | delete | 0/0 |
| 1000000000000011004 | 1000000000000000101 | HTTP_USER_POST_GRANT | POST /sys/account/grant | grantRole | 0/0 |
| 1000000000000011006 | 1000000000000000101 | HTTP_USER_GET_LIST | GET /sys/account/list | list | 0/0 |
| 1000000000000011007 | 1000000000000000101 | HTTP_USER_GET_GET | GET /sys/account/get/{id} | read | 0/0 |
| 1000000000000011008 | 1000000000000000101 | HTTP_USER_POST_CHANGE | POST /sys/account/switch | freeze | 0/0 |
| 1000000000000011010 | 1000000000000000102 | HTTP_ORGANIZATION_GET_LIST | GET /sys/organization/list | list | 0/0 |
| 1000000000000011011 | 1000000000000000102 | HTTP_ORGANIZATION_GET_GET | GET /sys/organization/get/{id} | read | 0/0 |
| 1000000000000011013 | 1000000000000000102 | HTTP_ORGANIZATION_POST_DEL | POST /sys/organization/del | delete | 0/0 |
| 1000000000000011014 | 1000000000000000102 | HTTP_ORGANIZATION_POST_SORT | POST /sys/organization/sort | sort | 0/0 |
| 1000000000000011015 | 1000000000000000102 | HTTP_ORGANIZATION_POST_SWITCHFROZEN | POST /sys/organization/switch | freeze | 0/0 |
| 1000000000000011017 | 1000000000000000102 | HTTP_POST_GET_LIST | GET /sys/post/list | list | 0/0 |
| 1000000000000011018 | 1000000000000000102 | HTTP_POST_GET_GET | GET /sys/post/get/{id} | read | 0/0 |
| 1000000000000011020 | 1000000000000000102 | HTTP_POST_POST_DEL | POST /sys/post/del | delete | 0/0 |
| 1000000000000011021 | 1000000000000000102 | HTTP_POST_POST_SORT | POST /sys/post/sort | sort | 0/0 |
| 1000000000000011022 | 1000000000000000102 | HTTP_POST_POST_SWITCHFROZEN | POST /sys/post/switch | freeze | 0/0 |
| 1000000000000011023 | 1000000000000000103 | HTTP_ROLE_GET_GET | GET /sys/role/get/{id} | read | 0/0 |
| 1000000000000011025 | 1000000000000000103 | HTTP_ROLE_POST_CHANGE | POST /sys/role/switch | freeze | 0/0 |
| 1000000000000011027 | 1000000000000000103 | HTTP_ROLE_POST_GRANTRESOURCE | POST /sys/role/grantResource | grantResource | 0/0 |
| 1000000000000011028 | 1000000000000000103 | HTTP_ROLE_GET_LISTUSER | GET /sys/role/listUser | grantUser | 0/0 |
| 1000000000000011029 | 1000000000000000103 | HTTP_ROLE_POST_GRANTUSER | POST /sys/role/grantUser | grantUser | 0/0 |
| 1000000000000011030 | 1000000000000000103 | HTTP_ROLE_POST_DEL | POST /sys/role/del | delete | 0/0 |
| 1000000000000011032 | 1000000000000000103 | HTTP_ROLE_GET_LIST | GET /sys/role/list | list | 0/0 |
| 1000000000000011033 | 1000000000000000104 | HTTP_MODULE_POST_DEL | POST /sys/module/del | delete | 0/0 |
| 1000000000000011036 | 1000000000000000104 | HTTP_MODULE_GET_GET | GET /sys/module/get/{id} | read | 0/0 |
| 1000000000000011037 | 1000000000000000104 | HTTP_MODULE_GET_LIST | GET /sys/module/list | list | 0/0 |
| 1000000000000011038 | 1000000000000000104 | HTTP_MODULE_POST_SORT | POST /sys/module/sort | sort | 0/0 |
| 1000000000000011039 | 1000000000000000104 | HTTP_MODULE_POST_FROZEN | POST /sys/module/frozen | freeze | 0/0 |
| 1000000000000011041 | 1000000000000000105 | HTTP_DICT_GET_LISTDICT | GET /sys/dictionary/list | list | 0/0 |
| 1000000000000011043 | 1000000000000000105 | HTTP_DICT_GET_GETDICT | GET /sys/dictionary/get/{id} | read | 0/0 |
| 1000000000000011044 | 1000000000000000105 | HTTP_DICT_GET_GETDICTBYCODE | GET /sys/dictionary/get/type/{code} | read | 0/0 |
| 1000000000000011047 | 1000000000000000105 | HTTP_DICT_POST_DELETEDICT | POST /sys/dictionary/del/{id} | delete | 0/0 |
| 1000000000000011049 | 1000000000000000105 | HTTP_DICT_POST_SWITCHFROZEN | POST /sys/dictionary/switch | freeze | 0/0 |
| 1000000000000011050 | 1000000000000000202 | HTTP_BIZLOG_GET_QUERY | GET /monitor/log/biz/get/{id} | read | 0/0 |
| 1000000000000011051 | 1000000000000000202 | HTTP_BIZLOG_GET_LIST | GET /monitor/log/biz/list | list | 0/0 |
| 1000000000000011052 | 1000000000000000203 | HTTP_ERROR_GET_QUERY | GET /monitor/log/error/get/{id} | read | 0/0 |
| 1000000000000011053 | 1000000000000000203 | HTTP_ERROR_GET_LIST | GET /monitor/log/error/list | list | 0/0 |

## 保留NULL及冲突清单

| 资源ID | 原CODE | 方法与路径 | 暂不映射原因 | 角色/租户引用 |
|---|---|---|---|---|
| 1000000000000001100 | MENU_SYS | * /sys | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001402 | API_SYS_TENANT | * /sys/tenant/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001403 | API_SYS_NOTICE | * /sys/notice/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001101 | MENU_SYS_ACCOUNT | * /sys/account | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001409 | API_SYS_ACCOUNT | * /sys/account/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000011000 | HTTP_USER_POST_CHECKUNIQUE | POST /sys/account/checkUnique | 纯HTTP辅助，保持NULL | 0/0 |
| 1000000000000011001 | HTTP_USER_POST_REPWD | POST /sys/account/repwd | 当前UI未确认对应动作，保持NULL | 0/0 |
| 1000000000000011003 | HTTP_USER_POST_EDIT | POST /sys/account/edit | 共享新增/编辑端点，单值不能同时create/update；待拆独立资源方案 | 0/0 |
| 1000000000000011005 | HTTP_USER_POST_GRANTPOST | POST /sys/account/grantPost | 当前UI未确认对应动作，保持NULL | 0/0 |
| 1000000000000001102 | MENU_SYS_ORGANIZATION | * /sys/organization | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001401 | API_SYS_POST | * /sys/post/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001410 | API_SYS_ORGANIZATION | * /sys/organization/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000011009 | HTTP_ORGANIZATION_POST_CHECKUNIQUE | POST /sys/organization/checkUnique | 纯HTTP辅助，保持NULL | 0/0 |
| 1000000000000011012 | HTTP_ORGANIZATION_POST_EDIT | POST /sys/organization/edit | 共享新增/编辑端点，单值不能同时create/update；待拆独立资源方案 | 0/0 |
| 1000000000000011016 | HTTP_POST_POST_CHECKUNIQUE | POST /sys/post/checkUnique | 纯HTTP辅助，保持NULL | 0/0 |
| 1000000000000011019 | HTTP_POST_POST_EDIT | POST /sys/post/edit | 共享新增/编辑端点，单值不能同时create/update；待拆独立资源方案 | 0/0 |
| 1000000000000001103 | MENU_SYS_ROLE | * /sys/role | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001411 | API_SYS_ROLE | * /sys/role/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000011024 | HTTP_ROLE_POST_CHECKUNIQUE | POST /sys/role/checkUnique | 纯HTTP辅助，保持NULL | 0/0 |
| 1000000000000011026 | HTTP_ROLE_GET_FETCHRESOURCE | GET /sys/role/fetchResource | 共享详情预览/授权编辑读取，单值不能同时read/grantResource | 0/0 |
| 1000000000000011031 | HTTP_ROLE_POST_SAVE | POST /sys/role/edit | 共享新增/编辑端点，单值不能同时create/update；待拆独立资源方案 | 0/0 |
| 1000000000000001104 | MENU_SYS_MODULE | * /sys/module | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001412 | API_SYS_MODULE | * /sys/module/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000011034 | HTTP_MODULE_POST_EDIT | POST /sys/module/edit | 共享新增/编辑端点，单值不能同时create/update；待拆独立资源方案 | 0/0 |
| 1000000000000011035 | HTTP_MODULE_POST_CHECKUNIQUE | POST /sys/module/checkUnique | 纯HTTP辅助，保持NULL | 0/0 |
| 1000000000000001105 | MENU_SYS_DICTIONARY | * /sys/dictionary | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001413 | API_SYS_DICTIONARY | * /sys/dictionary/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000011040 | HTTP_DICT_GET_QUERY | GET /sys/dictionary/query/{code} | 纯HTTP辅助，保持NULL | 0/0 |
| 1000000000000011042 | HTTP_DICT_GET_VALIDATEITEM | GET /sys/dictionary/validate/{type}/{value} | 纯HTTP辅助，保持NULL | 0/0 |
| 1000000000000011045 | HTTP_DICT_POST_CHECKUNIQUE | POST /sys/dictionary/checkUnique | 纯HTTP辅助，保持NULL | 0/0 |
| 1000000000000011046 | HTTP_DICT_GET_DELDICT | GET /sys/dictionary/del/{id} | 旧GET写入口，保持NULL，不映read | 0/0 |
| 1000000000000011048 | HTTP_DICT_POST_EDITDICT | POST /sys/dictionary/edit | 共享新增/编辑端点，单值不能同时create/update；待拆独立资源方案 | 0/0 |
| 1000000000000001200 | MENU_LOG | * /log | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001408 | API_LOG_OPENAPI | * /monitor/log/openapi/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001201 | MENU_LOG_ONLINE | * /log/online | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001404 | API_LOG_ONLINE | * /monitor/log/online/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001202 | MENU_LOG_BIZ | * /log/biz | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001405 | API_LOG_BIZ | * /monitor/log/biz/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001203 | MENU_LOG_ERROR | * /log/error | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001406 | API_LOG_ERROR | * /monitor/log/error/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 1000000000000001407 | API_LOG_LOGIN | * /monitor/log/login/** | 旧MENU或广域API，保留NULL，不推CRUD | 2/1 |
| 2111793873144872961 | NULL | GET /sys/module/list | 已逻辑删除，保留不映射 | 0/0 |

## 已批准的精确新增资源与引用边界

共享 POST/edit 与 GET/fetchResource 不能向一个 actionKey 写多个动作。固定方案分别登记标准动作、共享回填、online 本地详情以及 13 个复合动作候选 GET，已按用户单独批准插入资源 ID 1000000000000060001 至 1000000000000060078。执行前已核该范围不存在冲突；现库引用不沿新装管理员 all-resource 逻辑补齐，只执行具体批准的 65 条引用。

补充只读引用前像 SHA256 `1FE289EA705E9B1391C5E416412152B7B1231ECDC4B9EAC0F0D160A005BC4770` 已提供角色及租户技术关系，没有读取账号姓名或认证信息。固定计划对每条新增引用列明同主体、同 MAIN 的原 HTTP 覆盖源，原广域行和引用全部保留；13 条辅助资源不复制引用，原实际 HTTP 集合不扩大。任何后续资源或引用变更仍需另行明确，不延伸本次批准。

格式统一 `^[a-z][A-Za-z0-9_]{0,63}$`，空白归一 NULL。read 仅前端详情；共享 GET 本身不能由后端区分详情/编辑回填目的。菜单只投影真实已授资源的非空 key、有序去重，管理和角色授权明细仍保留 HTTP metadata。本次现库适配已获批执行并经独立后验通过；页面实际显示和交互验收状态以上方当前状态为准。
