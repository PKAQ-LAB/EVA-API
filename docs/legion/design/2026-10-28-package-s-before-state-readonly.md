# 包S：限定开发库只读before-state

执行者backend_developer；cwd=C:/home/workspace/lab/EVA-API，cornerstone，HEAD 1f708e202af0ba817b55c0f6be9a9085eea054ea。仅依据总控转达的用户原包S开发PostgreSQL授权查询；所有S真实写仍冻结，本附件不授权后续写入。

## 目标及只读证据

沿既有本地开发连接解析方式，内部读取web-booter/src/main/resources/application-dev.yaml的datasource，确认其与当前build/resources/main/application-dev.yaml字节相等；开发端点与production配置端点不同，解析为PostgreSQL、database=eva、currentSchema=eva且没有未解析变量。没有输出凭据、完整URL、环境、账号隐私或其他项目配置。未使用旧诊断脚本的顶层代码，未运行其较宽元数据/数据查询。

连接启动参数default_transaction_read_only=on，随后连接set_session(readonly=True,isolation_level=REPEATABLE READ,autocommit=False)。实际查询current_database/current_schema为eva/eva；开始和结束时transaction_read_only、default_transaction_read_only均on，只读验证布尔true。全部数据查询在同一repeatable read事务内，以精确ID数组绑定参数、显式列名及eva限定表名执行，不加状态过滤伪造完整集合。没有SELECT *、修改、清缓存或迁移。

数据库实际观测开始2026-10-28 12:43:22.642833+08:00，结束12:43:22.689239+08:00；事务rollback=true、connectionClosed=true、writes=0。连接及数据窗口均已释放，没有后台命令或测试运行。

## 两账号完整技术前值

| accountId | perm_ver | profile.dept_id | 完整role_id集合 | 完整post_id集合 |
| --- | --- | --- | --- | --- |
| 2113894345662021633 | 4 | 2113856614139469826 | [] | [] |
| 2114719096971497474 | 0 | 2113856614139469826 | [] | [] |

SYS_ROLEUSER_REF仅按这两个USER_ID查询USER_ID/ROLE_ID，结果总行数0；SYS_POSTUSER_REF仅按同集合查询ID/USER_ID/POST_ID，结果总行数0。未查询其他账号的关联、角色成员或个人资料。SYS_ACCOUNT仅投影指定ID/perm_ver；SYS_ACCOUNT_PROFILE仅投影指定ACCOUNT_ID/DEPT_ID。部门技术ID对应此前包S的D1标签，本次未查询组织实体或业务样本。

## 五角色scope及完整资源前值

| roleId | data_scope | data_org_ids原值 | frozen | deleted | 完整resource_id集合 |
| --- | --- | --- | --- | --- | --- |
| 2113857683657629697 | 0007 | NULL | 0 | 0 | [] |
| 2113858118523068418 | 0001 | NULL | 0 | 0 | [] |
| 2113858458634985474 | 0002 | NULL | 0 | 0 | [] |
| 2113870080799694849 | 0003 | 2113856917966462977 | 0 | 0 | [] |
| 2113870419179364353 | 0000 | NULL | 0 | 0 | [] |

SYS_ROLE限定这五个ID投影ID/data_scope/data_org_ids/frozen/deleted，实际5行；SYS_ROLERES_REF仅按这五ROLE_ID投影ROLE_ID/RESOURCE_ID，无资源候选范围或删除过滤，实际0行，故上述为空的是完整持久化集合，不是UI候选checked[]推断。自定义组织关联存储在DATA_ORG_IDS，CUSTOM原始字符串恰单个2113856917966462977（此前D2标签），其他四个原值NULL，不转换为空串或空数组。

## 四资源候选实际技术前值

| resourceId | resource_type（HTTP method） | resource_url | deleted |
| --- | --- | --- | --- |
| 1000000000000011009 | POST | /sys/organization/checkUnique | 0 |
| 1000000000000011010 | GET | /sys/organization/list | 0 |
| 1000000000000011011 | GET | /sys/organization/get/{id} | 0 |
| 1000000000000011012 | POST | /sys/organization/edit | 0 |

仅按四个精确ID查询SYS_MODULE_RESOURCES的ID/resource_type/resource_url/deleted，实际4行；本次未查询其他资源、权限缓存或业务数据，也未核运行请求匹配。技术字段映射先由当前RoleEntity、RoleUserEntity、PostUserEntity、RoleResourceEntity、ModuleResources及RoleModule.xml核对；沿当前真实物理表名，不执行存量表命名调整。

## 边界及后续

这是一份指定时刻的只读前值，不是后续写前永不漂移保证，也不证明S1-S8权限链、真实拒读拒改或样本创建完成。后续真实差异、平台行动门禁与恢复方案由总控另派；不得据本附件自行授予资源、替换关联、改scope或permVer。未改源/helper/配置、操作浏览器/IDE/服务、发HTTP、读Redis或重跑203，未暂存/提交/推送。
