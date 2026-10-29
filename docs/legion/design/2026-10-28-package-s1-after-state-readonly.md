# S1三笔UI提交后：独立开发PG只读复核

backend_developer独立查询，未执行UI写入；不以QA重开UI集合代替本次DB证据。cwd=C:/home/workspace/lab/EVA-API，cornerstone，HEAD 1f708e202af0ba817b55c0f6be9a9085eea054ea。

## 只读及数据时刻

沿12:43 before-state相同开发PostgreSQL连接解析方式；再次内部确认dev源/build配置字节一致、开发端点与production不同、无未解析变量、配置database/schema为eva/eva。未输出凭据、整URL或环境信息。

连接启动default_transaction_read_only=on，再set_session(readonly=True,isolation_level=REPEATABLE READ,autocommit=False)。实际current_database/current_schema=eva/eva、transaction_isolation=repeatable read，开始及末尾transaction_read_only/default_transaction_read_only均on/on，只读验证true。所有数据SELECT显式列名、eva限定表、参数化指定两accountId或五roleId，不加状态/候选过滤；未查其他账号或其他租户数据。

数据库观测开始2026-10-28 12:54:29.477435+08:00，结束12:54:29.530332+08:00。rollback=true，connectionClosed=true，writes=0；连接/数据窗口已释放。未占UI锁。

## 两账号完整后值及差异

| accountId | 完整role_id集合 | 完整post_id集合 | dept_id | perm_ver前→后 |
| --- | --- | --- | --- | --- |
| 2113894345662021633 | [2113857683657629697] | [] | 2113856614139469826 | 4→5 |
| 2114719096971497474 | [2113857683657629697] | [] | 2113856614139469826 | 0→1 |

完整SYS_ROLEUSER_REF在这两个USER_ID下恰2行，每账号仅SELF一次，无多余角色或重复行；SYS_POSTUSER_REF按两USER_ID完整0行。SYS_ACCOUNT仅投影ID/perm_ver，SYS_ACCOUNT_PROFILE仅投影ACCOUNT_ID/DEPT_ID；两部门仍D1不变。相对本人12:43附件，两账号各新增一条SELF关联，各perm_ver实际加1；不据版本号反推其他业务请求。

## 五角色完整后值

| roleId | data_scope | data_org_ids原值 | frozen | deleted | 完整resource_id集合 |
| --- | --- | --- | --- | --- | --- |
| 2113857683657629697 | 0007 | NULL | 0 | 0 | [1000000000000011009,1000000000000011010,1000000000000011011,1000000000000011012] |
| 2113858118523068418 | 0001 | NULL | 0 | 0 | [] |
| 2113858458634985474 | 0002 | NULL | 0 | 0 | [] |
| 2113870080799694849 | 0003 | 2113856917966462977 | 0 | 0 | [] |
| 2113870419179364353 | 0000 | NULL | 0 | 0 | [] |

完整SYS_ROLERES_REF按五ROLE_ID共4行，全部属于SELF，恰上述四ID且无重复；其余四角色集合仍为空。五角色data_scope/data_org_ids/frozen/deleted与12:43完全相等，CUSTOM原始组织字符串仍仅D2。SELF资源完整差异为[]→该四ID；没有额外资源或未知集合。

## 逐项核对结果与边界

exactTwoAccounts=true；eachUserExactlySelfRole=true；bothPostsEmpty=true；selfExactlyFourResourcesOthersEmpty=true；fiveRolesUnchanged=true；bothDepartmentsUnchanged=true。未发现上述获授范围内偏差，未修复或撤销数据。

前值参见2026-10-28-package-s-before-state-readonly.md（SHA256 05BFADF7DF3AB2AE0B196EBA1EA0C502E8BB33426DBA6FAF6A2698E0D1EE95CB）。本次仅查询相同技术字段及完整关联；未读取密码、token、联系方式、姓名、邮件、头像或业务样本；未查资源缓存、未HTTP/Redis/IDE/服务操作、未改源/helper/配置、未测试或重203、未暂存/提交/推送。仅新增本人此附件。

该结果仅证明12:54时刻S1提交后技术持久化集合符合指定预期，不代表已完成受测身份登录、作者归属样本、拒读拒改或S1-S8全链验收，不授权后续变更或撤未知集合。
