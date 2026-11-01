# 本地凭证目录ACL窄修

首次获批scope apply在private-output-acl处停止，发生于connect之前，NOT_ATTEMPTED；旧目录20261031T223147Z-ba489c14保留，无DB连接、事务或数据变化。纯解析无语法错误；本地只读/in-memory诊断定位Get-Acl加载Microsoft.PowerShell.Security失败，其runtime模块Security.types.ps1xml触发AuthorizationManager校验失败。仅清除子进程PSModulePath仍失败；使用Windows系统自带PowerShell绝对路径及其内置Modules路径可正常加载。未修改执行策略、系统模块、PATH或全局环境。

旧C302/10ABB/E486和原scope A95源码副本/EC88 seal保留。只在新scope入口替换目录步骤：新独占目录，复用旧冻结ACL脚本，新增拒绝预存deny规则；调用SystemRoot/System32/WindowsPowerShell/v1.0/powershell.exe，子进程PSModulePath仅WindowsBuiltin Modules。本人/SYSTEM/Administrators三条FullControl及容器/对象继承不放宽，落地后再读ACL，要求protected、恰三允许SID、无deny/非预期身份。技术stdout/stderr/exitcode独占留证，不打印环境/口令。仅改本任务新目录权限，不动旧目录或父workspace权限。

已获批本地ACL自检创建20261031T223446Z-172990ca，returncode0，实际ACL protected=true/allowRules3/denyRules0/exactApprovedSids=true/fullControlInheritance=true；stderr仅模块首次准备progress，证据在acl-check.json。该自检没有数据库连接/apply。新scope seal-v2绑定新源码及本说明，业务SQL/目标/用户scope批准不变。QA增量明确通过后仅一次新RUN apply；真实前置漂移仍停，不自动重试，不重跑PG/绿套件/视觉。
