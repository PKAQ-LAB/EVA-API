# 能力响应专用隔离验证工具交接

2026-10-28 backend_developer。依据总控对backend六文件独审后的工具实现授权；仅新增ignored helper/init和本记录，完成Python AST、默认只读PLAN与纯清单检查。未创建正式RUN、复制产物、执行Gradle/observer/PG/DB/HTTP/服务/IDE/UI、下载或提交推送，生产六文件及前端保持原冻结SHA。

## 工具身份与输入

- `.gradle/resource-capability-20261028.py` SHA256：D74ECC1F59E84C2D7F1AAD8BA2D84213014CF6E3A27988F2FC8C117EDEE46937。
- `.gradle/resource-capability-20261028.gradle` SHA256：C07632D979BF45D73AE702576E2FAF1E663CA6D8411ED715521AAF965F305BB6。
- 当前690输入closure SHA256：46D24A2EF9ADE779CDB519C7D49980522168462C5209240F13AE6826866E99A4。

新工具另锁两自身文件，且固定91D/9919及B51/FE36/7392/7AFE六个旧验证工具。导入前/每阶段前后逐项复核，不改旧入口。复用91D中的已批准五PG测试源码SHA及B51完整只读参考核验函数；在新进程内仅将其输入差异策略明确替换为690精确集合：9e原685加既有PG证据helper/四PG测试/7392差异及本轮新增DTO/三测试、handler/AuthCtrl差异。任意其它键/哈希/presence变化停止，不宽放源码目录。

9e旧main529/generated/header/snapshot/全部prepared38+33+15+45/external214及10份证据文件SHA仍完整重核，只作历史差异基线。没有将其classes放到新生产编译CP，旧529不冒称当前main；旧测试prepared也不作为本轮三个focused类输入。default PLAN真实只读核验通过，不调用任何pg_preflight、powershell_json、observe_processes或旧执行入口。

## 三阶段精确闭包

fresh RUN父目录为`.gradle/resource-capability-runs`，UTC+UUID独占创建，19个Project build/cache/report/tmp/generated/header/archive/test工作目录均在RUN早期绑定并锁定。仍固定11main模块，JDK编译器与Test launcher metadata均必须25且安装路径C:/home/DevApp/jdk/zulu25；所有命令offline/no-daemon/no-parallel/workers1/no build/config cache，不下载。

main阶段选择11个classes任务，仅允许这些模块compileJava/processResources/classes，不运行Test/jar。沿原已审main依赖顺序，从本RUN先编模块classes复制编译输入，源前后与copy完整清单/hash相等；实际CP只准本RUN已声明classes/编译copy及外部jar，内部jar/cache org.pkaq jar/live输出拒绝，annotation processor目录拒绝。

实际main键集合必须严格旧529+一个DTO，合计530；仅下列两个旧class允许且必须发生字节变化，其余527项（包含User C/菜单XML等）必须原SHA：

```text
新增 eva-core-contract/classes/org/pkaq/core/mvc/vo/AuthBootstrapResponse.class
改变 eva-web-auth/classes/org/pkaq/core/auth/authentication/entrypoint/UrlAuthenticationSuccessHandler.class
改变 eva-web-sys-service/classes/org/pkaq/sys/user/ctrl/AuthCtrl.class
```

main generated/header也必须与参考原清单全字节相等。任意其它class/resource变化、漏DTO或stale handler/controller停止，不自动扩大白名单。fresh main通过后，exclusive逐文件复制新snapshot并核源前/源后/目标SHA、记录copy-manifest，seal manifest/generated-header；新snapshot才是后续CP。

test-classes阶段实际任务图必须恰六项：COMMON/AUTH/SYS各compileTestJava和processTestResources；main compile/resources/classes、jar、testClasses及Test均排除。每个JavaCompile.setSource只含一个已审源码：

| 模块 | 精确源码 | 唯一允许class |
| --- | --- | --- |
| eva-core/eva-core-common | org/pkaq/core/properties/ResourcePermissionBindingTest.java | org/pkaq/core/properties/ResourcePermissionBindingTest.class |
| eva-web/eva-web-auth | org/pkaq/core/auth/authentication/entrypoint/AuthLoginCapabilityTest.java | org/pkaq/core/auth/authentication/entrypoint/AuthLoginCapabilityTest.class |
| eva-web/eva-web-sys/eva-web-sys-service | org/pkaq/sys/user/ctrl/AuthMenuCapabilityTest.java | org/pkaq/sys/user/ctrl/AuthMenuCapabilityTest.class |

三文件均在对应src/test/java，当前没有额外test helper/匿名内部class闭包；实际出现额外或缺class即停审。编译CP仅本RUN snapshot22 main roots+实际外部jar，AP仅外部jar；实际单源清单、compiler/JDK、CP/AP逐项记录和核验。

每模块原生SourceSet.test.resources及processTestResources.source.files独立记录根、目标路径和源SHA，拒重复资源目标/模糊根；不将SYS资源模型套给COMMON。afterTask记录实际TaskState.noSource/skip/failure，因为NO-SOURCE不会执行doLast。准备输出必须唯一class加该模块实际资源原字节，NO-SOURCE须与资源空集合严格对应，test generated/header预期全空。

默认PLAN只读文件系统候选为COMMON0、AUTH0、SYS一项fixtures/user-before-c.xml（BFC0F6B427D65F8E1F62D3D5364FECDD2F181DBC05C3AC40729BD0144A5F24EF），因此候选prepared1/1/2共4项；PLAN明确标注这不是fresh Gradle模型，实际执行仍须上述SourceSet/TaskState核验，不能先宣称NO-SOURCE通过。

test阶段恰三个原生Test任务，各一个精确class filter，全部producer排除；不运行web-booter/PG/数据库集成类，不使用observer。CP仅新snapshot22根、各自新prepared及当前外部jar；实际目录集合必须完整且无重复/未知/旧RUN/live/别的模块test目录。CommonResponseAdvice/ResponseUtil的mock servlet链在focused测试内部直接执行，不发HTTP。

fresh XML/suite必须各类正计数、零fail/error/skip且一致；额外检查六个精确方法名，不能漏class/method拼数：COMMON bindsEffectiveResourceEnable；AUTH serializesCurrentEnable/rejectsFailedCapability/preservesMetadataAndFalse；SYS keepsArrayAndCurrentEnable/rejectsFailedBootstrap。六项目前只是源码/候选，实际通过须待fresh XML。

## 封存、跨阶段及失败

完整690源/config/build/test/presence、工具、旧参考字节在每阶段前后重核。所有19Project实际path/source/variant模型必须与main阶段完全一致。新main、generated/header、新snapshot在后阶段前后封存，Test的prepared/generated/header也前后核；失败路径先记录实际test-output/test-auxiliary，检查已封main/snapshot/prepared漂移，再评XML或退出失败，不自动重试。

旧external214先逐项复核，仅作累计锁起点；每阶段实际classpath/AP文件须真实存在、在repo外、jar格式、非内部artifact，首次见记录其SHA。已见jar任何漂移立即拒绝，阶段后发现集与累计锁合并并复核；下一阶段独立phase-external文件和phase-controls seal绑定该累计集合，不重新基线化，不把旧CP报告当本轮实际CP。

phase controls、所有source/tool/external在compile/Test doFirst复核；Test还由原有门禁核新snapshot及三个prepared。Python保存每阶段seal本身SHA，前后比对。实际Problems报告路径必须在RUN；任何失败仅写gate与已有证据，不下载/放宽/重跑。BaseException不伪称完整后验已做完。

## 本轮验证与执行界限

Python AST通过。最终默认PLAN在execute/subprocess.run/Popen/mkdir断言禁止下退出0，返回PLAN_ONLY_NO_RUN_NO_GRADLE_NO_OBSERVER_NO_PG、input690/closure如上、NOT_RUN；正式RUN父目录成员前后相同。选择器数量11/6/3，Test --tests恰3。

10项纯内存清单检查通过：精确530与两变更可接受；漏DTO/额外class/User C漂移/stale handler拒绝；精确690差异可接受，未批准源变更/额外输入/缺输入拒绝；AST确认新工具无observer/PG协议调用。没有写产物或启动任何进程。生产六文件SHA仍由690精确输入检查覆盖。

尚未运行Groovy配置、实际main/三源编译、原生资源/NO-SOURCE、AP/CP/JDK配置或六测试；不能将以上静态/PLAN结果称为编译或测试通过。designer/QA独审新两SHA后，须总控另授实际执行。

只读入口（EVA-API根）：

```powershell
C:/home/DevApp/python3/python.exe -B .gradle/resource-capability-20261028.py
```

未来独审并另授后才可使用，当前未执行：

```powershell
C:/home/DevApp/python3/python.exe -B .gradle/resource-capability-20261028.py --execute
```

旧9e219、4b11、JDBCv1待跑与本轮能力六项结果保持独立，不拼数，不更新开发服务或宣称真实UI/S链已验收。
