# 单初始化源码整合独审门禁

2026-10-31 technical_designer。用户已授权未发布阶段整合并清理被吸收的增量源文件，不再询问该源码删除许可。原字典V14独立维护方案和seal/runner封为历史，停止准备执行；实际数据库/已有history处理仍未授权。

## 职责与对象矩阵

平台唯一production Flyway location为db/migration，仅保留V1__INITDB.sql。tenant-migration是TenantSchemaProvisioner通过ClassPathResource直接执行的模板，不是第二条Flyway链；其两个旧脚本合为一份最终租户模板，改实际引用及初始化版本账本，不把core表与private表混建。最终路径/名称由backend定稿后逐项核实。

精确澄清：这里的职责分离仅指两条既有初始化来源不互相夹带对象/种子，不要求主脚本新增schema_only模式或删去主init既有账户/admin种子。backend现源码核无schema_only或分模式SQL参数，不能发明新开关；本角色曾在交接中沿用该表述的验证建议已撤回。主路径按旧链最终账户/种子等价保留，tenant模板按原private对象边界验证。

| 旧来源 | 单init必须保留的最终状态 | 可去掉的迁移过程 |
| --- | --- | --- |
| V1 | 当前有效核心表、索引、初始管理员及既有授权、主/明细分表种子 | 被后续替代的旧表/列定义，不恢复历史伪明细主行或删除根 |
| V2 | 角色template/system字段、租户角色active/时间/原因、package_grant表及唯一/active索引；明确合法seed关系 | 只针对既有历史数据的反复关闭再恢复/backfill，不能借简化丢最终授权对象 |
| V3 + V13 | tenant schema字段、安全provision函数最终实现、code及schema全局唯一/规范CHECK/NOT NULL等最终约束 | legacy schema改名、已登记旧库扫描/原值迁移，不对空库猜tenant数据 |
| V4 | 三日志tenant默认0/非空、业务日志user/success、archive结构与全部复合索引 | 给旧NULL数据补0的UPDATE |
| V5 + V6 | 核心及租户模板的notice最终列/索引 | 扫描已登记租户补表；新模板直接具备，不擅升级现库 |
| V7 | 完整实际API资源ID/url/method及原有效授权、tenant资源交集种子 | 迁移期幂等重复插入不能成为删减授权理由，也不新授测试身份 |
| V8 | error聚合字段、指纹非空/tenant唯一、最近发生索引、默认计数/status | 历史异常文本聚合回填；EX_DESC最终已删不得再建 |
| V9 | login设备/浏览器/会话/风险/活跃退出字段与两个索引 | ALTER逐列过程 |
| V10 | 平台inspect资源与原受控管理员授权 | 不借其存在放行平台0认证/菜单 |
| V11 | SYS_BLACK_LIST最终不存在 | 创建后再DROP，不改变其它黑名单业务边界 |
| V12 | SYS_ACCOUNT及SYS_ACCOUNT_PROFILE直接建、资料外键/索引、原管理员ID和有效账号种子/关系保留 | SYS_USER rename、跨登记schema扫描、复制再删管理列；最终SYS_USER不存在 |
| V14 | SYS_DICT.CATEGORY VARCHAR16 nullable与两值CHECK；8个内置主项直接SYSTEM | 无QA seed或38历史恢复，QA归BUSINESS是特定现库映射决定，不进入通用初始化 |
| tenant旧V1+V2 | private最终账户/资料/角色授权/业务结构与模板版本账本一致 | 旧SYS_USER→ACCOUNT过渡及provisioner第二阶段文件执行 |

字典仍主SYS_DICT+明细SYS_DICT_ITEM，不改存量表名；SYSTEM/BUSINESS虚根仅前端。不要为合并新增pid/path树或把所有type=code推成系统种子。登录日志叶菜单及1407归属按已批准的有效菜单方案落种子，ID/url/method及授权不扩大；不把开发库QA/包S角色/账号/样本带进初始化。

最终等价矩阵需补到确切表列/索引约束名、种子ID/关系及新V1行锚点。不是把以上概要当已验完整对象清单；backend交实际合并文件与矩阵后才签。

## 必要最小验证范围（本角色不执行）

1. 复用现有隔离合成PG fixture与真实已缓存Flyway13.12 core/pg，classpath唯一版本；精确扫描production location只有一个V1。空库migrate恰1、官方validate成功、history仅合法版本1；第二次migrate0且对象/seed不变，不repair/baseline制造通过。
2. 用现有迁移测试改为最终结构断言：账户/profile及外键索引、无SYS_USER/BLACK_LIST/EX_DESC，tenant code/schema约束与安全函数，日志归档/聚合/会话、notice、角色模板/package及唯一索引。选代表合法插入/非法值拒绝证明关键约束，不新建全库测试框架。
3. 断言8内置字典SYSTEM、有效主/明细独立、无QA/历史旧叶主项/持久分类根，主seed及授权关系正确；初始化不顺手增加9999或ALL范围承诺。既有明确种子状态逐项保持。
4. fresh租户通过真实provisioner模板链建A/B schema，断言最终ACCOUNT+PROFILE/授权表与版本账本、同ID隔离、模板重复调用不破坏已有数据；如新初始化只对空schema成立，重复已初始化路径应按受控版本账本短路，不重新覆盖业务值。
5. 旧11/12→13升级专用用例不再适用，改成新单init相应职责；账户split/tenant模板等直接文件引用测试同步改为最终状态，而非留下不存在路径或继续期待13条history。rg全仓定位迁移名/路径/版本数断言与资源加载点，旧辅助seal不得默默更新伪称原证据。
6. 旧history/非空库不得由新测试或启动代码自动修复。只做隔离历史不匹配拒绝/只读计划断言，不连接dev演练repair/clean/drop。源码单V1授权不是已有库可重建许可。

不用大型infra或旧228全回归证明本事：先目标init/tenant/account相关必要合成PG与编译检查，动态任务清单及唯一槽由root/backend/QA协调，证据绑定新HEAD及实际源SHA。旧Flyway13.11及旧链测试只历史，不称13.12或新init已过。没有完整新冻结输入、测试结果及逐对象矩阵不签通过。

## 源码与部署界线

保持Flyway运行默认配置边界，不为验证启用真实dev自动DDL或启动应用。已有库checksum/history变化应向用户单独交具体处理选择；禁止自动repair/baseline/clean/drop/删history。C字典分源/缓存及平台0认证不随模板整合实施，C3skip；本角色仅design/status及只读源码审查。

## 首轮实际候选审查

已读backend实现矩阵、主1320行/租户225行候选关键结构与新SingleInitializationPostgresTest三个用例、provisioner差异及生产/测试引用。当前production目录确实仅主V1，tenant目录仅模板V1；基线旧16脚本副本存在。静态索引名inventory按SYS_USER→ACCOUNT映射比较，仅旧账号DEPT_ID索引不再存在，最终profile的DEPT_ID/CODE索引具备；该静态比对不替完整catalog执行证明。

发现必须补的旧库保护：provisioner无账本分支仅检测sys_user/account/profile/版本表。已登记schema若只存在旧sys_role/sys_dict或marker但无这四表，会执行IF NOT EXISTS模板并写version1，把未验证旧结构当成新init。建议只在实际目标schema用户关系集合为空时执行模板，非空立即拒；不扫描其它schema、不加mode。补只有marker/旧role无账本场景，断言不建账户/版本、不改变原对象；可复用init-tenant实例，不增加PG实例。已交backend/root，未签通过。

DATA_SCOPE新seed8值/5值仍由root已提交用户的独立决策收口；当前8值及测试断言仅候选，不能当全局已批准政策，也不从dev删除状态自动推广。其余八个主项SYSTEM和QA不入seed保持已批边界。

最小验证拟固定五类：SingleInitializationPostgresTest、TenantSchemaModePostgresTest、TenantAuthorizationPostgresTest、TenantAuthorizationContractTest、TenantSchemaProvisionerIdentityTest，预期6个合成PG实例（init3/mode2/auth1）。范围足够，复用原证据协议/只读observer与薄wrapper/init，不改旧helper或旧证据、不额外228；实际任务图、selector、classpath唯一13.12、RUN输出隔离及实例/监听/收尾均以执行证据为准，不因计划数6就通过。DATA_SCOPE决定、窄修、薄入口核验及root唯一槽前置满足后由backend执行，本角色不运行。
