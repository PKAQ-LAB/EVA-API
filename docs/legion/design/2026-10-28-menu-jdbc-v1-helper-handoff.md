# JDBCv1定向验证工具交接

2026-10-28 backend_developer，依据总控正式批准的八实例JDBC门禁和工具实现授权。当前仅实现、只读PLAN、纯内存消费者检查及专用scratch硬链接自检；无正式RUN、Gradle/PG/observer、业务SQL/服务/浏览器、下载或提交推送。

## 新工具冻结身份

- `.gradle/menu-jdbc-v1-20261028.py` SHA256：91D12BF6897F2E369A16100027D3EA1128B72B1BCAF0C96CDFFB4388330E5AC4。
- `.gradle/menu-jdbc-v1-20261028.gradle` SHA256：99197292C26898AC20A49A9345838226125C3FF4D7E95EE02AF9DD947A88085D。
- 当前686输入closure SHA256：0022B64933C5C0D1DB64C0EA3EA5587B77F9FA71FA30228784F26A4123FF2427。
- B51/FE36/7392/7AFE旧四工具均硬编码SHA冻结并复核，全部保持原字节；旧RUN不修改。新两工具另入locked-tools，不混充原685源锁。

新Python导入B51前核SHA，仅在新进程内将其verify_input_delta策略替换为显式批准的686精确清单，复用其原10份参考证据及全main/prepared/external核验；不调用旧入口或修改旧文件。新策略只准新增测试helper、四测试精确SHA及7392 observer差异，其余source/config/build/test/presence键或哈希变动拒绝。实际默认PLAN已完整核9e参考main529/generated/header/snapshot、全部prepared38/33/15/45及214外部jar。

## 两阶段任务与封存

fresh RUN独占`.gradle/menu-jdbc-v1-runs/<UTC>-<8hex>`，执行前先完整核源/参考/旧工具/新init/离线PG归档。当前没有创建此父目录或任何正式RUN。

首先只从已锁9e复制main及generated/header到新products、新snapshot；准备测试resources两项，classes初始为空。逐文件exclusive目标复制，源前/源后/目标SHA一致并记录copy-manifest。全部19Project的build/reports/tmp/generated/header/archive/test工作路径仍早期隔离；固定11main完整模型必须精确等于参考映射。

compile阶段唯一任务`:web-booter:compileTestJava --rerun`。精确setSource五个已审Java文件，禁main compile/classes/resources/jar、其它测试producer和所有Test。实际source集合必须五个、CP仅新snapshot22 main roots及214锁中外部子集，annotation processor无目录、jar逐项锁；JDK metadata必须25且安装路径精确C:/home/DevApp/jdk/zulu25。init doFirst及Python后验分别核实际source、CP/processor、JDK和完整模型/单任务图，禁止借live或旧RUN输出。

测试编译实际输出键集合必须精确十个class，超键/缺键停止交审，不扩容：

```text
org/pkaq/test/PostgresInstanceEvidence.class
org/pkaq/sys/module/AuthorizedMenuPostgresTest.class
org/pkaq/sys/module/AuthorizedMenuPostgresTest$Fixture.class
org/pkaq/sys/module/AuthorizedMenuPostgresTest$SqlCapture.class
org/pkaq/core/mybatis/tenant/TenantSchemaModePostgresTest.class
org/pkaq/core/mybatis/tenant/TenantSchemaAdvisorOrderPostgresTest.class
org/pkaq/core/mybatis/tenant/TenantSchemaAdvisorOrderPostgresTest$ProbeResult.class
org/pkaq/core/mybatis/tenant/TenantSchemaAdvisorOrderPostgresTest$ProbeService.class
org/pkaq/core/mybatis/tenant/TenantSchemaAdvisorOrderPostgresTest$ProxyTestConfiguration.class
org/pkaq/sys/role/service/RoleSchemaIsolationPostgresTest.class
```

之后将旧prepared中其余34 class原字节复制到新classes，连同原两resources和新十class共46项；未受影响36项必须逐项原SHA，无法把旧45冒称新源码编译产物。编译generated/header必须保持预期空；main/snapshot不变。另记prepared-copy-manifest、新prepared及所有输出根SHA。

test阶段唯一`:web-booter:test --rerun`，精确原四PG类filter，无producer。实际CP只新snapshot main+新prepared46+214外部锁子集，所有新输出根/controls/inputs/presence/doFirst复核；fresh XML每类正计数、零fail/error/skip且suite一致，实际cases由XML读取，不预填11。

compile阶段和Test阶段均保留旧7392 observe_processes的完整PS协议、PID/start父链、RUN exe/hash/-D、明确违规与loopback endpoint检查、missing-image一次复核、失败partial保留、终态未知歧义/存活fail及no kill。新run_phase显式无旧strict8端口coverage分支，coverageProtocol=eva-pg-jdbc-v1；compile出现任何PG观察也失败。不会以八实例JDBC证据消除实际observer/terminal错误。

## JDBCv1消费者与RUN绑定

测试前创建空专用pg/jdbc-evidence，随机64hex nonce，version=eva-pg-jdbc-v1，固定8 label→完整testClass map写pg/jdbc-context.json。context只含version/nonce/runRoot/evidenceDirectory/expectedLabels五键，其SHA连同compile审计/source清单、新prepared/全部control封入sealed-control-files；Python内存还保持该seal本身SHA。init doFirst核全部封存、context/四pgJdbc JVM属性和证据目录仍为空；测试helper再核seal/context/标签及RUN路径。

消费者严格JSON重复键/尾随内容拒绝，恰8固定label.json且无额外/pending/目录/符号链接；每记录恰10键、nonce/version/class/label匹配，addr/listen均精确127.0.0.1，port必须JSON整数1..65535（bool不接受），serverVersionNum整数140022。dataDir必须绝对规范Windows路径且严格RUN/pg/tmp下，以Windows normcase/normpath语义去重；恰8个独立dataDir和8个UTC postmasterStart，启动时间位于本次开始至消费者结束界内。允许动态端口复用，不要求8端口唯一。

对本轮可得的每条completeProof观察-D，以同Windows路径语义交叉到8 JDBC dataDir集合；缺某实例采样不再失败，但已观察违规/查询协议错/终态未知残留仍失败。消费者在fresh四类XML成功后才运行，随后还要求Gradle返回0且observer guard为空；即便文件齐全，测试或观察失败不能整体通过。

每阶段前后重核工具、686源、参考全部产物/外部、controls及新main/snapshot，Test后额外核全部新prepared/输出根。编译失败不自动重跑，不扩大十class白名单；BaseException中断保owner/no kill交接，仍不宣称中断时全部postcheck完成。

## 本轮自检证据

1. Python AST通过；最终默认PLAN在execute/Popen/mkdir均替换为断言禁止的情况下退出0，报告PLAN_ONLY_NO_WRITES_NO_GRADLE_NO_OBSERVER及NOT_RUN。实际只读核686/529/214通过；两阶段生成命令分别只有一个compile任务和一个Test任务，后者四条filter。没有正式RUN或复制参考产物。
2. 消费者28项纯内存正反例通过：八实例允许复用端口；拒旧nonce/错version/class/label、非loopback/多监听、bool/0/越界/string端口、错/string版本、旧RUN/tmp本身/相邻前缀/父路径逃逸、时间越界/非UTC、缺/额外/pending文件、额外/缺字段、重复start、Windows仅大小写不同重复data、重复JSON键及尾随JSON。未写证据文件或启动进程。
3. 已批准专用scratch内使用JDK25 Java源码启动器运行最小java.nio.file.Files真实自检，仅文件API，无PG/Gradle/网络：完整关闭后createLink发布、已有目标FileAlreadyExists且保旧内容、CREATE_NEW拒覆盖、删除pending后已发布内容保持均通过。自检finally删除全部测试数据，自检源码另经apply_patch删除。
4. 自动审批以blocked by policy先后拦截递归清理和非递归空目录Remove-Item，未继续绕过。目前只留下空目录`.gradle/menu-jdbc-link-selfcheck-20261028`，只读确认0条目；没有数据/源码残留，未创建正式RUN。

未运行Groovy/Gradle配置、真实五源编译、prepared组合、PG/JDBC采证或observer；这些仍须designer/QA独审新两SHA后总控另授执行。硬链接自检证明本机scratch文件系统支持该API，不能代替未来RUN实际发布或所有运行门禁。

## 精确命令与结果口径

EVA-API根目录只读PLAN：

```powershell
C:/home/DevApp/python3/python.exe -B .gradle/menu-jdbc-v1-20261028.py
```

未来独审通过并另获执行许可后才可使用，当前未执行：

```powershell
C:/home/DevApp/python3/python.exe -B .gradle/menu-jdbc-v1-20261028.py --execute
```

旧9e26类219业务通过但观察失败、4b四类11业务通过但旧coverage失败、新JDBCv1四类/八实例/终态结果三者分列；本轮无新执行结果，不追认旧RUN，不拼成fresh26或真实菜单/SELF/S链现场验收。
