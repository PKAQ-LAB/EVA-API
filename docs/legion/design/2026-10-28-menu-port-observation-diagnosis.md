# 4b65668d端口观察缺口只读审查

2026-10-28 technical_designer。只读helper及本RUN安全聚合/监听技术日志；未运行observer、PG、Gradle、网络查询或UI。

实读pg-process-observation.json分组：43条为initdb.exe38条（16个pid/start、8个dataDir）、pg_ctl.exe5条（5个实例、5个dataDir），无postgres.exe记录；全部ports空。observerError/terminalError为空、residual空，最终gate因严格八实例带监听证据不足而失败。initdb/pg_ctl不监听是正常生命周期属性；它们的dataDir completeProof只证明路径/父链，不证明postmaster监听。

源码仅在RUN内且Name=postgres.exe时调用Get-NetTCPConnection，因此本轮43条不能证明该TCP分支曾实际执行、失败或全部OS端口不可见。尚无每批原始PG类型计数，不能断言是父链漏采、CIM不可见或特定权限原因。不能用提高轮询频率或再次编译来代替定位。

仅从fresh TEST XML提取匹配固定技术模式的安全摘要，独立核有8次database ready及8条127.0.0.1 IPv4监听，端口为50122、52253、52267、55185、56442、56767、56796、63092。未读取/输出其它日志正文或业务数据。该证据反驳“端口空即可判PG没监听”，但没有同时绑定(pid,start)/exeSHA/-D/父链，不能填充完整八实例门禁或把旧219与新11拼成fresh26。

## 采集错误语义

Get-NetTCPConnection使用-ErrorAction SilentlyContinue，可能将权限/查询错误吞为[]。这是静态错误分类缺口，但不是本轮已证明的失败原因。应改为仅在明确postgres候选上查询并区分成功有端口、成功无端口、预期无匹配、工具/权限/协议错误；未知错误必须fail closed并保存安全类别，不输出命令行或外部端点，不把任何查询失败默认[]。对已退出实例按现有精确身份复核分类，不按PG进程名豁免存活者。

ownerPID只允许与本轮已证明的真实postgres实例配对，须保同启动身份前后核验；不能用initdb/pg_ctl的PID或仅dataDir计数当监听归属。若OS显示监听owner为其它PID，先保存不匹配技术分类再审，不把整个家族或按端口扫描的任意进程都算owned。原RUN/hash/-D/loopback/终态/strict8不变。

## 可另授的最小协议预检

先用一个独立、明确由预检脚本创建的Python loopback TCP listener（bind127.0.0.1、port0，持有socket和当前PID/start）验证真实Windows cmdlet端口读取。只查询该PID与已知本地端口，不枚举开发网络或其它进程；使用现有binary UTF8严格协议，只输出预检自身PID/start、127.0.0.1、动态端口及匹配布尔/错误类别。socket用try/finally关闭，无PG/Gradle/服务操作，无对外连接。可在同预检关闭socket后验证明确无匹配如何返回，不能靠忽略所有stderr达成“空数组通过”。该预检须总控单独授权，当前仅方案。

预检通过只证明cmdlet及协议在本机可用，不证明PG owner模型或八集群采样。后续最小观察改动应先保存每批PG类名计数/采样起止/已证与缺证数量，明确实际postgres是否进入采集分支；无完整实例记录则保留失败。若仍需给PG观察稳定窗口，应单独设计明确有界的合成fixture验证窗口，不暗调采样频率或回填日志作为完整proof，不新增ETW/Job框架。当前不授权源/helper修改或重跑。
