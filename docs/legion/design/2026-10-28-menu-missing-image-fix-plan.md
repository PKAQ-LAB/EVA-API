# 缺image分类窄修与四PG类定向补验PLAN

2026-10-28 backend_developer。仅Python已授窄修及只读准备，无真实observer、PG、Gradle、进程kill、业务HTTP/DB/Redis、服务、IDE或UI操作。

## 窄修身份与范围

最新menu-isolated-20261028.py SHA256 7392C5DB44E74DBE35D443B39E848D4F64190AA50E11D563C47B13747D5BE00F；685输入汇总SHA256 73561250CA1DFDAE699BC253EB162BC29FBF88C552F94534BD5160DCF4D02631。生产、tests、init保持冻结。

增加ProcessObservationFailure，仅携原批此前已验证partial_records和最小summary，run_phase保存partial而仍保observerError失败；终态异常同样保terminalPartialRecords，不把部分证据当完整终态。摘要只PID/start/PG类名、字段可读或存在布尔、父链已证布尔、采样起止UTC、分类，不输出外部command/path/身份。

只有current实例已证明在本树且PG image缺失才调用recheck_pg_identity一次。该查询只此正整数PID，CIM投影ProcessId/CreationDate/ExecutablePath并仅输出pid/UTC started/imageReadable三字段。powershell_json原binary/strictUTF8/错误传播不放宽；新增专用严格三字段validator，通过显式validator参数只用于此复核，不扩大普通normal/ambiguity协议。

复核空集合为exited，start改变为reused，记录旧实例退出/复用并移除该旧current行，不将新start写入history/latest归属，不记录新proof或端口。仍为同实例时，无论imageReadable真假都失败，因为没有完整exe/hash/-D证据；查询异常、缺身份、错误PID或格式均失败。部分记录不使阶段成功，terminal未知歧义、strict8、RUN exe/hash/-D及loopback和no kill原规则保持。

纯合成快照通过gone/reused/live不可读/live恢复可读/query坏载荷/缺身份/错误PID/query异常八分支，失败均保原批1条合成verified记录；未知父实例不复核、不继承；未创建目录或调用进程工具。Python AST通过，未做真实协议预检或运行复核。

## 参考RUN只读复核（不是复用运行成功结论）

参考20261028T105551Z-9e4396c6：业务26类219项通过，观察失败；不能整体绿。当前与其locked-inputs完整685键集合一致，仅.gradle/menu-isolated-20261028.py内容不同。实际products main529对原audit main清单全SHA一致，generated/header全等；snapshot529完整清单/hash与main及原sealed manifest一致。prepared完整SYS38/AUTH33/DATA15/web-booter45全等；214累计external逐项SHA复核通过。未复制任何文件、未创建新RUN。

## 定向补验PLAN（未实现执行模式，未授权运行）

目标只验证新observer及四PG类，不重复编译全部26类。未来必须单独授权实现定向模式/相应init，再独审后授执行；现有默认26类执行模式不因本PLAN改变。

1. 新UTC+UUID RUN独占创建。执行前再次核参考所有source/config/build/test/presence/main/generated/header/prepared/external锁，允许输入差异仅批准observer Python最终SHA；任何其它变化停止，不宽放测试/业务源。参考旧529和所有prepared均只读。
2. 将已核字节按显式清单复制到新RUN声明的products和新snapshot并重封manifest；web-booter prepared45项仅复制到新RUN对应test roots。生成/headers同样依据真实模型逐文件复制或封存；复制后核源/目标与旧锁字节一致，再建新锁，不能从live build或开发服务目录取产物。
3. 新init全部19Project build/cache/work/reports/PG unpack/data/tmp仍隔离，固定11main模型仅供sealed输出/CP，不允许producer。actual CP仅新RUN sealed main、web-booter自己的prepared及已核外部jar，禁止任何旧RUN路径、其它模块test目录或live输出混入。新PG directory/binary/unique archive/loopback/terminal/strict8原门禁不变。
4. task图必须仅: web-booter:test（实际全路径:web-booter:test），没有compile/processResources/testClasses/jar/clean等producer。精确CLI与filter同四类：org.pkaq.sys.module.AuthorizedMenuPostgresTest、org.pkaq.core.mybatis.tenant.TenantSchemaModePostgresTest、org.pkaq.core.mybatis.tenant.TenantSchemaAdvisorOrderPostgresTest、org.pkaq.sys.role.service.RoleSchemaIsolationPostgresTest。其它Test禁止。
5. Test实际XML/suite四类均存在、每类正计数、零fail/error/skip，观察独立判定；实际项数由fresh XML确认，不预填通过。预期8个PG实例仍需严格观察，漏采仍失败交接，不自动降门禁或重跑。自然退出短命参数无须全采，任何已观察非loopback/越界/未证存活残留或ambiguity仍失败。
6. 成果必须分列旧9e的26类219业务证据和新定向四类结果/观察证据。不能旧208加新11冒称新26类219重新通过；也不把定向观察成功替代真实菜单/业务SELF/S链现场验收。

当前未写新定向执行模式、未复制/创建RUN、未运行PG或Gradle。仅交designer独审本Python窄修及PLAN，后续仍由总控明确派发。
