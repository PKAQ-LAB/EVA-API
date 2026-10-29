# 菜单26类验证辅助：实现交审，未获执行

backend_developer，2026-10-28，cornerstone，cwd=C:/home/workspace/lab/EVA-API。本轮依据总控撤销短命进程全参数硬要求后的最小方案实施；没有ETW/Job/ctypes、新依赖或系统设置变更。

## 最终文件与入口

| 文件 | SHA256 |
| --- | --- |
| .gradle/menu-isolated-20261028.py | 8BA40EC8C6A90F360AAAF66F2CB0C5ECE82486281942ED991AB6E9D828262F21 |
| .gradle/menu-isolated-20261028.gradle | 5550EC20B3E7DF989ADEF2DC898589FD9674F1C9F664486454729B0EF7B9BCCE |
| web-booter/src/test/java/org/pkaq/sys/module/AuthorizedMenuPostgresTest.java | A78269ABACA1B9B871F4969D40B74F800C4B11BE0D8DDECBE6739A5159472D28 |

测试仅在已启动PG成功返回后的初始化段加catch(Exception|Error)，调用该实例close；close失败附为suppressed，重抛原异常。成功构造仍原tryresources；库start自身返回前失败不宣称已覆盖自动清理。其它三个测试保持此前loopback新SHA，五生产文件不变。

精确默认PLAN命令（已运行，仅只读）：

```text
C:/home/DevApp/python3/python.exe C:/home/workspace/lab/EVA-API/.gradle/menu-isolated-20261028.py
```

正式候选命令（当前禁止执行，须独审后再授）：

```text
C:/home/DevApp/python3/python.exe C:/home/workspace/lab/EVA-API/.gradle/menu-isolated-20261028.py --execute
```

默认PLAN结果PLAN_ONLY_NO_WRITES_NO_GRADLE，11main/26类/3阶段，685项输入（含presence）排序JSON汇总SHA256为11D33ED74EB3CB505536B72EAA3935AA7CE9034F54A53A78FB5A85BC957CD3D1。原始逐项输入由纯inputs(reference_module())可重算，执行获授后才封存到新RUN/locked-inputs.json；当前未为PLAN创建RUN或写输入清单。候选RUN根为.gradle/menu-isolated-runs，UTC+UUID且独占创建，不覆盖旧目录。

## 沿用门禁及专用差异

新辅助从冻结固定20类复制，保留全部Project最早layout隔离、project-cache独占、源presence与完整清单、main/generated/header/test快照、实际compile/processor/Test CP、内部jar拒绝、外部jar跨阶段累计锁、精确图/selector及逐module XML/suite对照。只引用原staged helper的纯code_inputs函数，不调用旧输出/测试函数；补web-booter全部src输入与构建文件，旧四个C/20 helper也纳入锁，未改其字节。

MODULES固定11、Test模块固定SYS/AUTH/DATA/web-booter；SYS13/AUTH6/DATA3/web-booter4的26 FQCN与Groovy filters/CLI完全对应，均查源文件存在。末阶段精确四Test、禁止所有producer；prepare阶段仍实际task图核验，不预填计数。

主产物validate_main只允许原494中AuthCtrl.class变化、三新class与AuthorizedMenu.xml增加，以及web-booter四class和27resource首次纳入；User.xml必须C，旧其它键/hash不变。XML和27opaque资源输出须同锁定源字节。完整集合候选529；未知新增/缺失、旧稳定项变化立即失败，不放整个模块目录。新RUN封存后各阶段前后继续核字节与完整清单。

web-booter整个测试JVM预设java.io.tmpdir、ot.epg.working-dir、TEMP/TMP和workdir在RUN；doFirst重核属性未漂移、PG2.2.2实际jar版本/hash、actual Test CP唯一Windows binary jar及唯一归档内容SHA。Python默认PLAN只在内存校验归档中三exe SHA和PE AMD64，不解压执行。缺包/漂移停，不下载。whole-JVM目录约束覆盖四PG类全部start。

## 最小进程观察与失败交接

不要求自然退出的initdb/pg_ctl全参数捕获，也不对已退出PID认领或清理。执行获授后，用本轮Gradle launcher PID起步，仅查询已知节点及其直接子进程技术字段，累积启动时间与父链；Java命令行内部仅归约为Gradle Test Executor布尔，不输出任何完整命令行。只对RUN内PG候选提取-D目录与实际监听地址/端口，核规范exe路径及锁定SHA、PID启动身份和含Test JVM父链；观察到PID复用、owned-tree PG路径缺失或在RUN外均失败。

每阶段终态仅额外枚举PG进程中exe位于本RUN的候选，不输出外部开发PG信息或按名操作。正常fixture close优先。辅助刻意没有自动kill分支：任一存活RUN候选、观察失败或终态无法读取都保失败记录并交接，不能称无残留；即使完整proof也不以快照后PID进行未经重核的终止。中断写launcher/已知归属记录，不能保证任意异常自动回收，后续清理须独立重核完整证据。

成功Test还要求观察到四类共8个合成PG实例的数据目录各有实际127.0.0.1动态正端口及完整归属证据；若测试太快未捕到足够样本，属于观察门禁失败，不解释为业务断言失败或自动重跑。无短命进程参数缺失即失败的规则。实际监听/IPv4 JDBC localhost是否可用、进程终态均尚未执行验证。

## 已做静态自检

Python AST解析及默认PLAN通过；PLAN前后RUN目录清单相同，未创建RUN/未Gradle/未PG。26 selector Python/Groovy集合准确相等且每个源类存在。主manifest内存合成正例通过；新增未知键、稳定旧键变化、缺新键、User C变化四负例全部拒绝。离线binary实际归档/PE/hash预检通过；最初手录postgres.exe哈希多一字符导致默认PLAN拒绝，已按归档真实SHA纠正并复检，不涉及运行测试。

Groovy只作源码独审准备，未用Gradle解析或声称Groovy编译通过。新测试UTF-8/LF、无tab、行长<=120，四测试方法未增加；没有Java编译结果。生产五文件、旧C/20 helper、旧RUN/seal和配置保持冻结，未实际DB/HTTP/Redis/IDE/服务/提交推送。

本交接替代旧计划中‘新helper尚未创建’和‘短命全参数必需’的状态；原隔离计划及source-review中真实业务/租户测试、代理/现场验证未证边界继续适用。当前仅交designer与QA审辅助，不授权执行。

## 终审获授歧义窄修后的最终身份

designer指出终态先筛ExecutablePath可读会漏未知PG。按总控另授，仅改Python：终态PG候选保留不可读路径记录，在PowerShell读取CommandLine前即降为最小pid/ambiguous/imageReadable字段；Python不将其加入known或认领，保completeProof=false、非空residual，阶段失败交接且不kill。可读且明确RUN外PG仍排除，不因存在正常开发PG一律失败。八dataDir采样严格门禁保持。

最新Python SHA256 B58F948FB3715C6C987DFB8017DBA87293910619D22347C8DAE2F3F6F0480ADA，替代上表8BA40；init与menu测试SHA保持。685项输入汇总最新SHA256 3D4932363C03AB8B3974BA32C3A052CAF1682B5263B418CAF8E042C9A0F53A8F。内存替身自检未知不可读PG保歧义/不认领、可读RUN外排除均通过，无实际进程观察或目录创建；Python AST及默认PLAN再次通过、未创建RUN/Gradle/PG。当前交designer复审，仍未授权执行。

## 首次执行观察无效后的编码协议窄修

总控转达QA已对ecc70444的owned session99158单次官方Ctrl-C，exit1，未到Test图、XML0；该RUN观察因reader线程解码错误无效，保留历史且不复用。main BUILD SUCCESS或旧gate0不证明观察无错误。本成员未执行该RUN或中断命令。

获授仅修改Python powershell_json及纯payload校验函数。PowerShell协议头显式无BOM UTF8 Console.OutputEncoding及OutputEncoding、Stop与Progress SilentlyContinue；Python用binary pipes，先验证stdout/stderr均bytes，再在主线程strict UTF8解码。缺管道、解码错误、非0exit、任何非空stderr（含CLIXML）、空或损坏JSON都拒绝，不replace/ignore/回退编码。subprocess读异常/超时也仅保技术失败类别，避免打印EncodedCommand或原始文本。

JSON必须实际list；normal行严格八字段类型及UTC启动时间、监听端点类型，ambiguity仅pid/ambiguous=true/imageReadable=false，不能以缺输出兜空列表。不改变进程范围、八实例门禁、init、源码或测试。

最新Python SHA256 8403CB7DB4E291F8EDEA2AFBFD8168599F3BFE7B44FABFC8CB966FB84E3C553F，替代B58；685项输入汇总SHA256 C20A55D60069C01C5B55F5D8E07ECAC413CC33C26778D2DF2D3A74D309D745AC。init5550EC20及menuTestA78269保持。

纯内存替身17负例全部拒绝：stdout/stderr分别None、任一管道非法UTF8、非0exit、非空stderr普通文本/CLIXML、空stdout、invalidJSON、非list、坏row、bool PID、ambiguity多字段/错布尔、坏port、normal缺字段、坏时间。合法[]、normal、ambiguity、混合列表四正例通过。Python AST与默认PLAN通过，既有RUN清单未变；实际observer调用0、PG/Gradle0，未重跑、未声称真实编码协议或测试已验收。交designer独审，尚未授权真实观察或执行。
