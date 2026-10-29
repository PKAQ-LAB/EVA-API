# 四PG类独立定向工具实现交接

2026-10-28，backend_developer。依据总控本轮仅实现授权完成；当前未授执行。没有创建正式RUN、复制参考产物、运行Gradle/PG/observer、操作业务接口/DB/Redis/UI/服务或提交推送。

## 独立工具身份

- `.gradle/menu-pg-targeted-20261028.py` SHA256：B51CED73226FC67DE58EBAC93DC6738DEC60820B17C3E30393B06A397D9AE542。
- `.gradle/menu-pg-targeted-20261028.gradle` SHA256：FE36DA115C7CF2A42DED5A265DE1761B22771DB2D832DACD0CEA356B0CB6EED6。
- 原helper `.gradle/menu-isolated-20261028.py` 仍为7392C5DB44E74DBE35D443B39E848D4F64190AA50E11D563C47B13747D5BE00F，原init仍为7AFEA135B5DF8D8E95613CAAB707B543CE7DB0BC369ADC3379D4AE2536078BB8，未修改。
- 新Python导入前严格核原helper哈希，复用已审observer、strict8、终态ambiguity、no kill、PG离线预检及结果解析函数。仅在导入模块内存中将FILTERS缩为四类，不写原文件；旧26类入口保持原字节。新init哈希另在新Python常量中固定，新两文件另存locked-tools，不混入参考685输入清单。

## 参考证据与门禁

参考RUN固定为`.gradle/menu-isolated-runs/20261028T105551Z-9e4396c6`，其业务26类219项通过、观察失败。新工具不复用其观察成功结论，也不将新结果与旧计数拼合。

新工具固定10份参考证据文件SHA：原locked-inputs、prepared、snapshot manifest、main实际path-model/main-output/generated-header/sealed-manifest、prepare/test实际path-model及累计external锁。运行前重新核这些文件及真实字节：685输入键集合/presence、529 main、main generated/header、snapshot完整内容、SYS38/AUTH33/DATA15/web-booter45全部prepared、四测试模块generated/header为空及214 external逐项哈希。除原observer文件允许精确7392外，任何其它源/配置/build/test/helper/presence变化均拒绝。当前默认PLAN实际通过这些只读门禁。

未来获执行许可后，独占新UTC+UUID RUN位于`.gradle/menu-pg-targeted-runs`。只有新RUN创建前通过完整参考核验和离线PG预检才进入复制。main classes/resources/generated/header全部复制到新products，只有web-booter自己的prepared及test generated/header复制；另从已核snapshot复制529项到新snapshot。复制均独占目标文件、源前/源后/目标SHA一致，逐文件source/target/锁定哈希写copy-manifest。新snapshot、输入、工具、外部、模型及各输出根清单分别封存，并用sealed-control-files二次锁定。

新init沿用全部19Project早期build隔离和固定11main模型；实际完整模型须等于映射至新RUN后的参考模型。唯一task图为`:web-booter:test`，禁止compileJava/processResources/classes/compileTestJava/processTestResources/testClasses/jar等任何producer及其它Test。CLI和init四类精确一致：AuthorizedMenuPostgresTest、TenantSchemaModePostgresTest、TenantSchemaAdvisorOrderPostgresTest、RoleSchemaIsolationPostgresTest。

实际Test CP仅新snapshot的22个main roots、web-booter自己的新prepared roots及参考214锁内的实际外部jar子集；逐项核SHA，禁止旧RUN/live/内部jar/其它模块test目录。Python检查完整sealed roots必须出现且无重复、未知目录/外部jar。Test doFirst复核输入/presence、工具、全部输出根、新snapshot/prepared、累计external及PG binary/archive/whole-JVM目录门禁。新init保留原PG目录检查全部代码及loopback约束；observer直接复用已审7392语义，无新PS命令或放宽。

自然结束后，包括Gradle/observer失败路径，重新核工具、control seals、完整参考、复制输出与snapshot；有实际模型时再核全19Project/单Test图/CP/XML。Gradle启动异常也先尝试这些完整只读复核后报失败。四类每类必须有fresh XML正计数，零fail/error/skip并与suite一致；实际cases从XML计算，不预填11。strict8要求仍由原run_phase在本次唯一test阶段执行；缺观察仍失败，不自动重跑、不杀进程。

## 本轮验证证据

- Python AST通过；未运行Groovy/Gradle编译，init实际配置与任务图仍待独审及将来获授执行验证。
- 默认PLAN实际退出0，status为PLAN_ONLY_NO_WRITES_NO_GRADLE_NO_OBSERVER，freshFourClassResult为NOT_RUN，685/529/38+33+15+45/214核验通过；参考产物复制数0。
- 15项纯内存检查通过：仅observer替换可接受；源变更、额外输入、presence变更、错误observer SHA、缺输入拒绝；路径只映射参考RUN；全新sealed CP可接受；旧RUN目录、未知目录、未知jar、缺sealed root、重复CP拒绝；唯一Test四filter命令；默认main在execute/Popen/mkdir/write入口断言禁止下仍完成PLAN且目录成员前后相同。
- 再增加冻结新init漂移拒绝检查，并在最终修改后重跑AST和带副作用断言的默认PLAN，仍无RUN创建或任何进程启动。

## 精确入口

只读PLAN，可在EVA-API根执行：

```powershell
C:/home/DevApp/python3/python.exe -B .gradle/menu-pg-targeted-20261028.py
```

未来须在designer/QA独审工具及两SHA后，由总控另行授执行；当前没有执行此命令：

```powershell
C:/home/DevApp/python3/python.exe -B .gradle/menu-pg-targeted-20261028.py --execute
```

helper生成唯一Gradle调用，固定offline/no-daemon/no-parallel/max-workers=1/no build/config cache、新RUN project-cache、新init、cPhase=test、:web-booter:test --rerun及四条--tests，并-x全部生产者任务。新四类结果与新observer证据单列；不宣称26类已fresh重跑或真实菜单/SELF/S链现场已验收。
