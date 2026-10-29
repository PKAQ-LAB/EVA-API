# C2/C3 六请求脚本独立终审

2026-10-29 technical_designer。只读实算新旧SHA，实读新脚本完整生产路径、fake及CLI；未执行任何入口/PLAN/测试/HTTP，未接密码或操作DB/UI/服务，仅写本设计。

静态审查通过，无必须修复项。`.gradle/org-scope-read-probe-20261029.py` 实算SHA256 `EF9E9184238D86FA1DA4465E6FACFB9CC2F0F59477EE2A8918605E6B005AC9D9`。D005与AF5工具实算仍各自原SHA。新入口按hash锁D005后只复用纯函数，不改旧白名单/globals，不运行旧入口。独立transport与既审固定传输边界一致。

READS确切生成listL/listP/getL/getP，ORDER前置login/menu共六次。Budget同时校验顺序索引、<6及90秒期限，每请求消耗一次，无自动重试；路径仅固定两组织样本，无角色/PEER登录/其它写路由。菜单能力严格is True，本轮身份及两组精确读取全部匹配后才整体EXPECTATION_MATCHED。

visible下四读均复用既审精确目标ID正向校验；hidden下L仍正向，P list只接受200/true/0000及严格[]，P get只接受200/false/02001及空data。成功空get、资源拒绝、过期、未知业务码及异常状态不替代负例。无额外节点/错ID/浮点ID放宽；中途失败顶层success保持false，不将已观察步骤推成scope已证明。

CLI真实运行只能精确 `--run --expect-peer visible` 或 `--run --expect-peer hidden`；无参只NOT_RUN、未进入依赖导入或密码/网络，错参数固定ARGUMENTS_REJECTED不回显。getpass需TTY并拒回退，MD5/Bearer/bodytoken内存/peer回环后发HTTP/noProxy/noRedirect/noCookie/noRetry、3秒连接/5秒读墙钟/2MiB保持。finally关本client并清引用，正常会话/cache/audit副作用边界不变。

输出固定phase/预期枚举/LIM/L或P/状态/SAFEcode或UNKNOWN/计数及布尔；不输出实际账号/ID/组织字段、token/长度/claims、body/message/headers/Cookie或异常文本。scopeRootCause=NOT_PROVEN_BY_API，resource/edit未覆盖。loginObservation只有已解析登录响应后出现；未取得响应时仍以原固定错误停报，不额外请求获取诊断。不存在把客户端预期枚举当真实scope的分支。

已读33业务fake、7transport、4target、5参数负例，以及Budget顺序/上限、期限及依赖hash负例；实现方报告通过，本角色不重复执行，QA独核结果另记。未见引入通用步骤DSL/端点配置或权限变更。可交总控结合QA fake结果和独立PG阶段证据组织用户运行C2 visible；C3 hidden须后续批准的档案变更及独立事实窗口成立，脚本不自动切阶段或改服务端数据。
