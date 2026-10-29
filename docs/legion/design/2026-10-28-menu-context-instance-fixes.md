# f3667f22两项独立失败的获授窄修

2026-10-28 backend_developer；仅测试生命周期两文件与Python实例跟踪模型，不修改生产、init、身份/租户边界或测试排序。未Gradle/PG/真实observer/新run/服务/IDE/UI/DB操作。

## 测试生命周期

实际XML顺序为TenantSchemaMode 08:55:44Z、AuthorizedMenu 08:55:58Z、RoleSchemaIsolation 08:56:28Z。Mode直接router后残留TenantContext；ThreadUserHelper只恢复ScopedValue；Menu首方法以tenant0进服务57被拒，close清理后其它三个方法通过，与污染机制一致。当刻ThreadLocal值未存，不能断言具体tenant=101。Role并非前序原因，但也有直接路由后未清理缺陷。

仅Mode和Role两个测试类各新增JUnit @AfterEach clearTenantContext，调用TenantContext.clear，正常、异常或断言失败后均执行。没有@BeforeEach，不在service/router/withUser内无条件clear，不改变原嵌套/冲突负例或fork/排序。此处是测试归属生命周期修复，不放宽生产guard。

## PID实例模型精确变化

旧known[pid]永久积累，后续同数字PID不同started即停止所有观察；正常旧子进程退出后，复用该号的无关进程也会被历史PID查询带入。f366仅存旧六节点，未存冲突新tuple，不能追认此次已证为OS正常复用或某个PID/时间值；已存started均UTC-o七位小数字符串，非明显类型比较错误。

新refresh_process_instances为纯函数式状态更新，不引进程框架。rootPid/rootIdentity锚、history不可变实例记录、current本快照PID→实例映射、latest仅诊断与transitions生命周期摘要分开。实例键编码pid|完整UTC起始时间（七位小数，支持等值UTC表示归一化，不浮点、不丢精度）；同数字PID新start永不覆盖旧history。

首次root锚定前，在真实观察调用中用已有Popen保留句柄的poll复核launcher仍存活；若已退出，不能把复用后的同号进程当root。已锚root的start变更或快照歧义严格拒，并存旧/新start的rootRejected摘要。

新实例只有当前快照唯一可证parent实例、parent开始不晚于child且parent可递归证明到本root才进入history，isTestJvm只取该新实例自身证据；父缺失/跨代/歧义不认领。已证明旧实例后续仍是同(pid,start)时，可沿已封存父实例边保留起源，即使旧父已自然退出；不会把它接到复用该PID的新父。新无关PID实例只留下最小观察/转换摘要，不继承旧角色/后代权利。旧history与父边不覆盖。

RUN内PG沿实例父边核本root及Test JVM来源；父实例不能证明即失败交接/no kill。原exe规范路径/SHA、-D目录、IPv4 loopback、终态不可读ambiguity、严格8 dataDir采样和无自动kill全部保留。自然退出进程不认领清理；所有审计只有PID/start/parent实例/角色布尔，不新增外部命令、路径或身份日志。

## 最终身份与纯合成验证

| 文件 | SHA256 |
| --- | --- |
| TenantSchemaModePostgresTest.java | 662505E45B6B54383EA616BF36BA757090926FBCD7657229AE786AE13D6A7DBD |
| RoleSchemaIsolationPostgresTest.java | 9136D6154B437554FD2DBE683B1F79B863BF351AC40985B0E58E2BC0D6677F6C |
| .gradle/menu-isolated-20261028.py | CD399140BBB07A6F123F2B29844C604127BEE04A7F2E05CE7BF5AEF29A60B441 |

init仍7AFEA135B5DF8D8E95613CAAB707B543CE7DB0BC369ADC3379D4AE2536078BB8，新menu测试仍A78269ABACA1B9B871F4969D40B74F800C4B11BE0D8DDECBE6739A5159472D28。685输入汇总SHA256 6FF3FAF86C15C376DB98ABDD9FA10CBE3ED737A1ED4009513698C8A1CC811B36。

Python AST通过；纯合成快照验证同代合法链、无关PID复用不认领、旧history不变、新实例不继承旧Test JVM角色、父跨代拒/root复用拒、已接受旧父边保留、RUN PG父证不足拒均通过。PG路径/hash在该最后负例仅为内存替身，没有真实文件或进程调用，未创建目录。两测试scoped diff --check通过，仅旧换行提示；原Test方法数5/1及新menu4不变。没有真实测试执行，不能称生命周期或实例观察已运行验收。

当前交designer独审，正式新run仍需总控另授。f3667f22为219项218pass/1fail且观察失败的历史记录，不整体绿、不复用产物、不据terminal[]推全程观察通过。
