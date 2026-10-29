# 4b65668d端口观察只读诊断

2026-10-28 backend_developer。按总控授权只读指定RUN审计、7392源码及RUN内测试XML中严格提取的PG监听/启停技术行。未运行新observer、PowerShell协议、PG、Gradle、服务、系统扫描、业务HTTP/DB/Redis或浏览器；未修改helper/init/fixture/业务源。

## 可证结论

RUN：`.gradle/menu-pg-targeted-runs/20261028T112535Z-4b65668d`。

本次唯一任务`:web-booter:test`，forbidden为空、四filter精确；fresh XML为四类11项，fail/error/skip均0，suite SUCCESS。该结果独立于旧9e的26类219项业务证据；本次strict8观察失败，不能整体绿。

43条保存的PG observation：initdb.exe 38条、16个PID/start实例、覆盖8个dataDir；pg_ctl.exe 5条、5个实例、覆盖5个dataDir。全部43条completeProof=true，ports均为空。没有任何postgres.exe观察记录。completeProof在源码只说明RUN exe/hash、data归属和派生父链成立，不包含端口成立；strict8另要求同时ports非空，因此正确拒绝。

observerError/terminalError均null、residual=[]、无kill；三个缺image recheck均exited，分别为pg_ctl一次、initdb两次，没有postgres。history总36个实例，终态current为0。成功记录只保存进程started，没有sampleStartedAt/sampleCompletedAt；不能把进程创建时间当端口采样时间。

从四类RUN内XML system-out/system-err严格提取到8条IPv4监听行，均为127.0.0.1。日志中的8个服务器PID均未出现在known.history或known.latest中：7080、8400、12668、20592、24812、32488、37144、37884。日志证明这些服务器曾声明监听；不提供本观察协议所需的同采样PID/start/exe/-D/端口联合证明，不能据此替换strict8。

| 类 | 日志服务器PID/端口 | 日志监听至shutdown请求（本地CST） |
| --- | --- | --- |
| AdvisorOrder | 37884 / 56442 | 19:26:53.150—19:26:55.581 |
| SchemaMode | 37144 / 52253 | 19:26:59.288—19:26:59.943 |
| SchemaMode | 8400 / 52267 | 19:27:03.764—19:27:04.966 |
| AuthorizedMenu | 7080 / 63092 | 19:27:08.820—19:27:10.087 |
| AuthorizedMenu | 20592 / 50122 | 19:27:13.667—19:27:14.229 |
| AuthorizedMenu | 32488 / 55185 | 19:27:17.725—19:27:19.854 |
| AuthorizedMenu | 24812 / 56767 | 19:27:23.238—19:27:25.185 |
| RoleIsolation | 12668 / 56796 | 19:27:28.787—19:27:30.017 |

这些是日志时间范围，不是已测采样间隔或漏采原因。未读取或输出XML其它业务字段、整段日志或完整命令行。

## 源码机制与证据边界

当前`.gradle/menu-isolated-20261028.py`哈希仍为7392C5DB44E74DBE35D443B39E848D4F64190AA50E11D563C47B13747D5BE00F。

observe_processes先以known root/history的数字PID构造精确`ProcessId=... OR ParentProcessId=...`条件，查询Win32_Process；非terminal不扫描所有PG进程。新后代发现依赖现有已知PID逐轮扩展，refresh_process_instances同时要求当前父实例可证；保存历史可防PID复用借边，但不是一次递归完整树快照。

只有行的image在RUN内且Name为postgres.exe时，才执行`Get-NetTCPConnection -State Listen -OwningProcess $_.ProcessId -ErrorAction SilentlyContinue`，投影LocalAddress/LocalPort。initdb和pg_ctl无论是否运行，都只得到预设空ports；本轮43条均属于这两类，空ports本身不能作为端口cmdlet失败证据。

该cmdlet的OwningProcess来自外层Win32_Process行；Select-Object表达式内的`$_`则指TCP连接行，映射address/port。Python对已返回endpoint要求address精确127.0.0.1、有效正端口；normal协议还验证port<=65535。未见把所有PG的端口错误关联到父PID的静态代码。

`-ErrorAction SilentlyContinue`会令该命令的可继续错误不进入常规错误输出，而当前载荷没有端口queryAttempted/queryStatus/errorCode等字段；空结果与被抑制错误无法通过现有JSON区分。这是静态可证的诊断缺口，不等于本轮已发生无权限、模块故障或查询失败。binary/strictUTF8/exit/stderr门禁只能检查实际产生的输出，不能恢复已被命令抑制的错误。

本轮无postgres记录、服务器PID也不在history/latest，首先说明监听实例的发现/保存证据缺失。短服务器生命周期与逐轮树发现存在潜在漏采窗口，但没有成功批次原始rows、批次采样时间、query计数或端口错误状态，不能证明具体是时间竞争、父进程短命、CIM过滤或其它原因，也不能断言Get-NetTCPConnection实际是否被调用。

## 最小协议预检PLAN（未实现、未执行）

1. 先授权纯合成PowerShell协议预检：以函数mock替代Get-CimInstance/Get-NetTCPConnection，禁止真实系统枚举；输入合成正常endpoint、成功空结果、可继续错误及异常四分支，复用binary/strictUTF8/JSON门禁，验证当前SilentlyContinue如何影响结果。仅输出合成字段、查询次数和安全状态，不打印异常原文、命令或外部路径。它只定位协议，不能证明真实cmdlet权限。
2. 如需验证真实cmdlet，再独立授权一个普通Python本进程临时socket绑定127.0.0.1:0，不启动PG或Gradle。记录该自有PID/start/实际本地端口，PowerShell只对该PID使用Listen/OwningProcess过滤并显式Stop，精确投影地址/端口；分离正常返回、成功空集、错误安全类型。查询前后确认同一自有实例，最后只关闭自有socket，不按名称杀进程、不读取其它进程command/image/端点。此预检只能证明该自有监听器的协议能力，不能补齐本轮PG观察。
3. 若预检通过，再交设计决定最小发现侧诊断改动范围：保存每批安全采样起止、候选/已证PG类计数、实际端口查询次数及安全成功/空/错误状态。不能为拿到postgres而继承旧PID身份、全系统宽扫、放宽loopback、降低strict8或修改测试fixture延时。本轮不实施该改动、不新增实际observer。
4. 任何未来修订均需独立冻结helper/init SHA、合成验证、独审和总控另授；不得自动重跑本次四类。旧219业务结果、新11业务结果、观察失败和协议预检必须分列。

本次交付只解释已有证据和预检边界，所有新执行、修复或再跑保持未授权状态。
