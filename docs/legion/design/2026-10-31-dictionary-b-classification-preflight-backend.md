# 字典 B：开发库分类前置只读清单（待审核，不执行）

2026-10-31。总控正式授权一次限定开发库元数据调查。本文件只交分类候选及精确技术前像，不授权 DDL、DML、样本清理、恢复或启用分类版。B 源码冻结清单与本数据提案分离。

## 取证范围与连接收口

实际窗口 2026-10-31 15:31:03.483415+08:00 → 2026-10-31 15:31:03.591310+08:00；开发与生产目标已区分，源与已构建 datasource 配置一致，批准开发目标匹配。实际 database/schema 为 eva/eva，SYS_DICT 与 SYS_DICT_ITEM 的物理关系均为 eva；REPEATABLE READ，开始/结束 transaction_read_only 与 default_transaction_read_only 均 on。仅一次连接，0 写入，rollback/close 均已完成；后续文档由此离线快照生成，没有重复查询。

仅读取主项 id/code/type/frozen/deleted、category 列是否存在、按 main_id 的明细数量及 frozen9999 数量。未读取 keyValue、remark、联系人、业务正文、账号、日志或其他 schema 数据；无 HTTP、Redis、服务操作。

原始脱敏结果：`.gradle/dict-b-metadata-ro-20261031T073102Z-634822d9/result.json`，SHA256 `F7FFB2A55613441197D5E1F671B903E12B6B8758201B1144537A28C6AE614AF0`。单次执行脚本：`.gradle/dict-b-metadata-readonly-20261031.py`，禁止无新授权重复运行。

## 当前事实与候选

CATEGORY 列尚不存在，V14 未执行，当前 47 条主表记录中 deleted=0 有效 9 条、其余 38 条 deleted 非零。有效主项代码无重复；八条内置项的 ID 与 code 同时匹配当前初始化源码，列为 SYSTEM 候选，不以 type=code、frozen=0 或名称推断分类。QA 项缺少分类证据，保持 UNKNOWN。当前业务候选数量为 0，不代表已确认不存在业务字典。

源码依据：`web-booter/src/main/resources/db/migration/V1__INITDB.sql:670–699` 明确以单级模式种入八个字典主项，文件 SHA256 `225137E1C1F9C4D5148B4DF79FFA5067ABDBF975FAF729AE6BBEC1FDEB4E09B4`；这是初始化来源证据，不运行该脚本，不把其中 ON CONFLICT 的其他字段覆盖行为带入分类迁移。DATA_SCOPE 另有 `RoleAoeBo.java:21`、`RoleDetailVo.java:21`、`RoleListVo.java:22` 的字典类型引用。

| 精确主项 ID | code | 当前 type | frozen/deleted | 明细 总数/有效/有效9999 | 候选 | 源码证据 |
| --- | --- | --- | --- | --- | --- | --- |
| 1000000000000004001 | DATA_SCOPE | DATA_SCOPE | 0/0 | 8/5/0 | SYSTEM（待审核） | V1:676–678 |
| 1000000000000004010 | FROZEN_STATUS | FROZEN_STATUS | 0/0 | 3/3/0 | SYSTEM（待审核） | V1:679–681 |
| 1000000000000004020 | TENANT_TYPE | TENANT_TYPE | 0/0 | 2/2/0 | SYSTEM（待审核） | V1:682–684 |
| 1000000000000004030 | CARD_TYPE | CARD_TYPE | 0/0 | 4/4/0 | SYSTEM（待审核） | V1:685–687 |
| 1000000000000004040 | POST_LEVEL | POST_LEVEL | 0/0 | 4/4/0 | SYSTEM（待审核） | V1:688–690 |
| 1000000000000004050 | RESOURCE_TYPE | RESOURCE_TYPE | 0/0 | 6/6/0 | SYSTEM（待审核） | V1:691–693 |
| 1000000000000004060 | LOGIN_TYPE | LOGIN_TYPE | 0/0 | 1/1/0 | SYSTEM（待审核） | V1:694–696 |
| 1000000000000004070 | BIZ_OPERATE_TYPE | BIZ_OPERATE_TYPE | 0/0 | 7/7/0 | SYSTEM（待审核） | V1:697–699 |
| 2111791956297613314 | EVA_QA_20261018_D01 | EVA_QA_20261018_D01 | 0/0 | 1/1/0 | UNKNOWN | 无分类证据 |

DATA_SCOPE 当前 deleted=0；8 条明细中有效 5 条、非有效 3 条，本次不读明细键值，不修改已有恢复结果、type 或明细状态。八个内置主项目前 frozen 均为 0，分类为 SYSTEM 后由 B 的真实分类守卫拒写；不附带改为 9999。

## UNKNOWN 决策项

ID 2111791956297613314、code EVA_QA_20261018_D01：可审核指定 BUSINESS 或 SYSTEM；也可选择不将该样本纳入分类版有效集合，但其排除方式必须另行明确批准，当前不得删除、隐藏或恢复其他行。只要它仍是 deleted=0 且未分类，B 管理 list 就会按现已审门禁拒绝整个请求；不能静默过滤 UNKNOWN、默认 BUSINESS 或造第三根。未决定时保留旧部署，不启用分类版。

## 编码占用与历史项

有效 9 项之间没有重复 code，八个内置 code 已由上述有效 ID 占用，任何业务新建/改名不得覆盖。全 47 条里 ALL 重复于 1000000000000004002/1000000000000004051，DELETE 重复于 1000000000000004055/1000000000000004072，均 deleted=1783056863；这不是当前有效 code 冲突，不恢复或统一改名。

SYSTEM 与 BUSINESS 未被任何真实主项 code 占用，且两个前端虚根不需要数据库根行。旧 SYSTEM_DICT 的 ID1000000000000004000 当前 deleted=1783056863，不补建、不恢复、不映射为具体系统字典；其余非有效历史行均不进入本轮分类映射。当前读取未包含 PID/PATH，故不把所有非有效行一律断言为历史子节点。

## 47 条元数据精确前像

CATEGORY 的前像是“列不存在”，不是已经存在且值为 NULL。未来仅在另批并执行 V14 后才有列值前像；本表的“保持非有效、不分类”不会生成任何 UPDATE。ID 按十进制字符串保存，避免 JavaScript 数值精度损失。

| id | code | type | frozen | deleted | 分类提案处理 |
| --- | --- | --- | --- | --- | --- |
| 1000000000000004000 | SYSTEM_DICT | SYSTEM_DICT | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004001 | DATA_SCOPE | DATA_SCOPE | 0 | 0 | SYSTEM 候选 |
| 1000000000000004002 | ALL | DATA_SCOPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004003 | DEPT_ONLY_LIMIT | DATA_SCOPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004004 | DEPT_AND_CHILDREN_LIMIT | DATA_SCOPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004005 | DEPT_LIMIT | DATA_SCOPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004006 | POST_ONLY_LIMIT | DATA_SCOPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004007 | POST_AND_CHILDREN_LIMIT | DATA_SCOPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004008 | POST_LIMIT | DATA_SCOPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004009 | CREATOR_LIMIT | DATA_SCOPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004010 | FROZEN_STATUS | FROZEN_STATUS | 0 | 0 | SYSTEM 候选 |
| 1000000000000004011 | UN_FROZEN | FROZEN_STATUS | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004012 | FROZEN | FROZEN_STATUS | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004013 | READ_ONLY | FROZEN_STATUS | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004020 | TENANT_TYPE | TENANT_TYPE | 0 | 0 | SYSTEM 候选 |
| 1000000000000004021 | ENTERPRISE | TENANT_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004022 | PERSONAL | TENANT_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004030 | CARD_TYPE | CARD_TYPE | 0 | 0 | SYSTEM 候选 |
| 1000000000000004031 | USCC | CARD_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004032 | ID_CARD | CARD_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004033 | PASSPORT | CARD_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004034 | OTHER | CARD_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004040 | POST_LEVEL | POST_LEVEL | 0 | 0 | SYSTEM 候选 |
| 1000000000000004041 | LEVEL_1 | POST_LEVEL | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004042 | LEVEL_2 | POST_LEVEL | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004043 | LEVEL_3 | POST_LEVEL | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004044 | LEVEL_4 | POST_LEVEL | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004050 | RESOURCE_TYPE | RESOURCE_TYPE | 0 | 0 | SYSTEM 候选 |
| 1000000000000004051 | ALL | RESOURCE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004052 | GET | RESOURCE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004053 | POST | RESOURCE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004054 | PUT | RESOURCE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004055 | DELETE | RESOURCE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004056 | PATCH | RESOURCE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004057 | BUTTON | RESOURCE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004058 | OPERATE | RESOURCE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004060 | LOGIN_TYPE | LOGIN_TYPE | 0 | 0 | SYSTEM 候选 |
| 1000000000000004061 | PASSWORD | LOGIN_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004070 | BIZ_OPERATE_TYPE | BIZ_OPERATE_TYPE | 0 | 0 | SYSTEM 候选 |
| 1000000000000004071 | CREATE | BIZ_OPERATE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004072 | DELETE | BIZ_OPERATE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004073 | UPDATE | BIZ_OPERATE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004074 | QUERY | BIZ_OPERATE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004075 | EDIT | BIZ_OPERATE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004076 | IMPORT | BIZ_OPERATE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 1000000000000004077 | EXPORT | BIZ_OPERATE_TYPE | 0 | 1783056863 | 保持非有效，不分类 |
| 2111791956297613314 | EVA_QA_20261018_D01 | EVA_QA_20261018_D01 | 0 | 0 | UNKNOWN 待决定 |

## 后续启用与回退门禁

用户先审核八条 SYSTEM 候选和 UNKNOWN 的明确归属或独立排除处理，审核前不生成可自动执行的分类 UPDATE。候选映射完整后，再单独提交精确目标、fresh 前像、条件化 DDL/DML 与回退方案审核；本次时间点快照不持续锁定数据，不能直接充当未来写入事务的现值。

分类启用前必须完成 QA 独立源码验证、受控 V14 实施与所有有效主项的分类映射。写入事务须核定批准开发源及实际两表来源，核对精确 ID/code/type/frozen/deleted 与约束版本，检查有效 code 唯一；只变批准主项 CATEGORY，不变 deleted/frozen/type/code/PID/PATH/明细/授权。未知未决、数据漂移、列提前出现或结构版本不符即停止，不猜测补齐。

分类映射回退只能针对获批精确 ID、匹配预期分类后值恢复该字段的获批前值；不能重放 V1、恢复删除行、覆盖全行或清全缓存。B 开始服务后不能单独撤掉必需 CATEGORY 列；应用/字段回退顺序须在实际执行方案中另批。本次未执行任何迁移或回退，原 code KV 缓存不因分类提案清理。
