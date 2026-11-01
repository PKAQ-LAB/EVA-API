# 字典会话内存快照同步 QA

2026-11-01。当前仅按最终用户批准方案 A 整理验收条件，**NOT_RUN / 未审冻结实现**。设计依据：`docs/legion/design/2026-11-01-dictionary-local-snapshot-sync-contract.md`；文内历史“待确认”状态以总控最新用户批准为准。代码/API协议批准不包含 DB 变更，无 DDL。

| 必要定向项 | 通过条件 |
| --- | --- |
| 唯一数据源 | initialState.dict 为唯一可渲染完整 map；登录/初始化合并为一次全量读取，DictSelector 挂载不再逐 code 请求。显式 data 优先、表单原 code 与 showall 语义保持；无 localStorage/sessionStorage/cookie 字典快照，无第二缓存框架。 |
| 完整响应协议 | 现 fetchDicts 的 200 保原 Response.data，304 无业务 body；真实项目 request/errorThrower/Advice 链能取得 ETag 并识别 304，适配仅限此端点。无快照不带 validator；有快照的 304 保原数据，异常丢快照后最多一次无条件补取。 |
| 服务端认证与摘要 | JWT/会话/权限版本/可信来源及完整冲突校验先于条件响应。摘要绑定可信身份、来源、格式版本及完整有序内容；展示值、顺序、删除和冻结变化改变摘要，稳定数据不因无序容器产生假更新。不得因 ETag 命中绕来源冲突。 |
| 一致性与响应头 | 快照只用经真实代理进入的一次 active readonly REPEATABLE_READ 数据库完整投影，不哈希旧 Redis、不在此路径回填 Redis；body 与摘要同一构造。保持 private/no-store，Vary 合并 Authorization/Cookie 与既有 Origin，不覆盖；CORS 仅暴露 ETag，不扩大允许域/凭据。 |
| 身份与并发 | 登录新 generation（含同 uid 重登）、用户/租户切换、退出与认证失效清有效 snapshot/ETag，旧响应即使成功也不提交。正常同主体 token 续期不误清；同身份在途请求合并且异常退出可再次同步。 |
| 错误状态 | 同身份断网/超时/普通 5xx 保最后有效快照与 ETag 并标 stale/retry；无历史快照明确不可用。认证/可信来源或 code 冲突/结构无效清有效快照及 ETag，但保表单原 code。来源冲突有可信机器分类，不能被普通 5xx stale 策略吞掉；合法空 map 与失败严格区分。 |
| 原子替换与写后同步 | 有效 200 完整结构校验后原子替换，旧 code/值删除真实消失，不只追加合并。本标签页字典提交成功立即校验；若写撞在途旧读取，结束后补一次重验，不只复用提交前结果。失败写不触发成功同步，不改变权限版本或强制重登。 |
| 调度与清理 | 仅 visible 且 online 期间 60 秒校验；隐藏/离线暂停，focus/online 恢复事件按 10 秒间隔合并。本地提交不受 10 秒限制。卸载/身份更换清 timer、listener、在途 generation，无重复订阅或后台轮询。假时钟覆盖这些状态，不承诺离线实时更新。 |
| 原功能边界 | 管理 list/get/category/revision/frozen/维护资格及 checkUnique/validate 不改成标签 map；0003/0006 原表单行为保持，SYSTEM/BUSINESS 隔离与原资源门禁保持。无实际 VO 使用证据不删 @Dict serializer、不批量改字段合同。 |

成员冻结后再做独立源审和必要定向测试，HTTP 200/304 必须验证实际项目链，不能只用 mock 冒整链通过。当前未读取未冻实现、未测试/DB/Redis/浏览器/服务/账户/重启；不重产品28、部门7、旧全仓或视觉。0006 原代码及维护证据单列保持，runtime 仍未验。

## 后端首个冻结：只读源审，测试等待槽

backend freeze06BEA8A7C5CE27F2DEABC77C90FEB02634AF04AF2C936273455B43947C393EDE的9输入/13真实Gradle产物/5日志XML独核SHA全一致。已读snapshot/typed异常、controller、CORS及真实HTTP测试：独立REQUIRES_NEW readonly RR完整inventory与completeItems、不触Redis；同一不可变结果length-prefix身份/Owner/有序内容摘要；正常ResponseEntity在Advice前走条件304、typed409完整0x0000-10002、no-store及Vary/CORS窄增量已核。管理链仍复用原资格，不声称整个应用运行通过。

当前无已证mustfix但QA测试未运行，重槽归frontend，等待归还后仅实际Gradle新增HTTP全类与PG新增单方法；不沿开发分轮测试冒QA通过。原source主体/上下文不匹配仍走PERMISSION_DENY，前端冻结后需交叉核清有效snapshot分类，不只看typed409。无产品源码/DB/Redis/浏览器/服务操作。

## 后端独立实际Gradle新增两项通过

绑定06BE冻结，QA实际Gradle只选DictSyncHttpTest全类和DictSchemaIsolationPostgresTest.conditionalSnapshotReadsOneReadonlyDatabaseVersionWithoutSharedCache单方法，--rerun仅目标test/offline/no-daemon/no-parallel/maxworkers1，自然exit0/BUILD SUCCESSFUL41s，29任务1执行28up-to-date。XML各1测试、0失败/错误/skip，方法名精确对应新增两项，未重旧PG/0006。运行后9输入SHA无漂移；XML副本/result在.gradle/qa-dict-sync-backend-20261101，result SHA358ABE013137AA8124F926CF347715C8A08F97B8E372798137DC5E69B855425D，Gradle日志SHA B93895C6AC63CED646DA37BC83B386A6149EF703D282001FC35FC496F24FAC2C。

独审无mustfix。HTTP使用真实Security/JWT Filter/Advice/Controller的MockMvc，认证数据与DictService替身，覆盖200/weak304真空体/headers/CORS/匿名冻结与typed409对比普通DB500，不称真实服务运行通过。PG仅一个本机合成实例，实际readonly RR与并发提交快照边界、无共享Redis快照链、顺序冻结/身份租户/冲突缺表缺列断言通过。未连真实dev/Redis/服务或浏览器；无重进程，槽可由root调配。等待前端正式freeze后交叉审实际request与状态机，不沿开发分轮冒整体绿，runtime继续未验。

## 前端730D候选首轮窄源审：未通过，需修正

已读唯一map/generation同步器、窄fetchDict、定时组件、selector、bootstrap/login及写后接线。尚未启动QA前端测试，不沿开发44分轮签通过。已向frontend/root交两项具体问题：其一labelRender仅查原options而未查showall额外合成“全部”选项，value0000在原字典不含0000时显示原码，违反保持原showall交互；其二最终认证失败在skipErrorHandler窄请求后只作为IntegrityError清字典，没有当前owner/generation下显式沿原退出，重放仍失效时全局退出被跳过，需要保留来源/结构与认证差别及旧响应不误退新会话。

此外现服务测试mock request，虽只读当前实际生成plugin-request判定仅success===false才触发业务thrower（304应不触发），仍需本地实际项目request链的200/304必要验证，不将mock解析当整链实证。无产品源修改/DB/服务/浏览器或测试动作；等责任成员修复与新封存，后端独验结果不受影响。

## 最终前端冻结独验及重启后页面窄验

最终冻结.git/dict-sync-source-auth-fixed-20261101.json SHA A2A710618430041F1097611A8772FA6D1C374573E2349EEB19A718BA5D0ABFAB，25文件独核匹配且QA运行后无漂移。两项mustfix已修：showall与labelRender共用最终选项；最终认证失败仅当前owner/generation沿原redirectToLogin，旧响应不误退新会话。新增本地Axios adapter运行真实生成Umi request及项目errorConfig，200/304与原success=false业务错误链有实际断言，无外部网络。

QA三轮自然exit0，共47项不同用例通过：核心5文件31项；启动及标签相关定向10项（57项过滤skip）；登录能力及实际退出6项（3项过滤skip）。证据eva-web/.git/qa-dict-sync-result-20261101.json SHA35A0783CB4C79D139D13B01391BA41AF9C50530501ABF5EC18D514978BAA2F4C。不称单轮全仓通过；未复跑全量类型检查，既有诊断不冒通过。测试槽已归还。

19:52前后，按root独占Edge锁授权，仅正式localhost:8000角色页UI窄验。初始旧页显示暂无法验证登录；一次正常reload后恢复系统管理员会话并加载8条角色，无再次认证失败或登录操作。新增角色数据权限六项中文标签正常，0003指定部门右侧默认展开树，选择统合部主体后显示选中且父节点半选；取消并确认放弃。重新新增选择0006指定岗位，右侧岗位列表加载总经理/有效，勾选后行选中且确定按钮恢复可用；取消并确认放弃，回原列表。未填写角色名称编码、未点确定、未保存或授权、未复用旧QA对象，无DB/Redis/服务操作。只证明bootstrap标签和两个选择区可见交互；未观察Network，运行态200/304、ETag/Cache-Control、挂载零逐code请求及远程变更同步仍未实测。

## 正常DevTools Network补证受控能力限制

随后仅按root追加授权检查官方Edge可观测性。当前角色列表、系统管理员、无表单draft；通过tab正常Ctrl+Shift+I一次后，AX树无变化、页面截图仍只有角色页，官方Edge标签清单未暴露DevTools或Network可控页面。当前browser扩展面无法观察所需Network界面，未绕至native既有hardstop，未使用私有HTTP、网络dump、存储或React状态。因缺少请求列表可见性，不再重复0003/0006操作或等待周期来假造网络证明；fetchDicts live状态、公开ETag/Cache-Control及query请求计数仍NOT_OBSERVED，而非0请求或live304通过。未新建/修改字典、未重跑脚本测试，UI锁归还root。

## 用户指定临时BUSINESS字典：初值已创建，等待基线

root转达用户specific授权后，正式Edge系统管理员无draft，正常进入字典管理并精确搜索EVA_QA_DICT_SYNC_20261101_01，未见同code/name项。新建code/name均为该值、备注QA同步验收，仅一明细QA_SYNC_01/同步前/排序1，点击保存一次后等待完成；再从刷新后的业务字典列表重新选中，通过正常get加载回读上述全部字段。主记录可见DOM行键BUSINESS:2116862974824505346，确认category BUSINESS及精确master ID2116862974824505346；明细DOM行键仅0-QA_SYNC_01，页面未暴露其数据库ID，不将行键冒作ID。当前无未保存修改，保留页面等待root基线，尚未修改标签或删除，不触碰原字典/角色/DB/Redis。

只读console探针源码与launcher独核SHA分别D68CBC43CE88DA23A208B3DCD627CB4CD6D24B80347A3E5D0ED20F95D5E95966、7712A3DD9E22B01E3C5390FA9B7A23376608CB23123EC9B7C2E94F27572CE103。源审固定localhost9016 loopback最多5请求、唯一POST登录、4只读fetchDicts、秘密仅内存不输出/落盘、白名单summary、阶段signal精确continue、限时无retry/proxy/redirect，无旧维护器或业务写。QA实际3fake通过exit0且无真实HTTP/input；PLAN_ONLY通过；WinPS ParseFile errors0。候选可交root调度真实可见TTY登录，不由QA代启动；本机真实HTTP结果尚未产生，非React全局map观测。

## 探针启动诊断增量窄审

脚本更新SHA CC3D814949C8319EE99378A83489A93D6F54B0280F356EEBD0A2EBE2B50C16A8、新诊断launcher SHA F2920F40E366058B31EDA8F876C62847AA2CF2307637F028594168B04477118C独核匹配，原run launcher7712未变。增量仅白名单startup_info、无HTTP/input的diagnose早退、实际run在TTY校验前落startup并明确失败summary、summary先落盘再容忍输出失败；TTY条件及原固定5请求/凭据内存/阶段门禁未放宽。QA原3fake、PLAN实际exit0，诊断launcher ParseFile errors0；QA工具宿主diagnose exit0，入口/import true、stdinTTY true、stdout/stderrTTY false、UTF8/中文可编码、requests0/inputfalse。此结果仅证明QA工具宿主，不替代root同可见console通道；旧启动无summary不能推导实际登录失败、API同步失败或0请求。未启动真实run/可见窗口/凭据输入，fixture仍等待基线，不提前改删。

## 探针失败阶段与GET预算增量窄审

root更正旧summary实际requests2/status[200]，计数在send前而status在send返回后，现证据仅支持登录已解析成功、index1 fetch尚未完整返回，非已确认启动失败或0请求。新探针SHA A35FED4C57E4B7191884C678E3DFCE50C2981FB3F1AB059066A75FCFDFC5129F独核匹配，原launcher7712/诊断F292不变。源审固定失败类别/phase/route/耗时/已观测status，不输出异常原文或payload/header；GET socket和硬预算仅按root批准改max30s，login仍3s/5s，loopback/5请求/no retry/凭据内存/TTY/阶段信号限制保留。QA实际4fake及PLAN exit0，无真实HTTP/input；新增fake正确保留index1 BODY TIMEOUT、已收200/30000ms及requests2，无秘密输出。候选可交root单次真实调度，QA未代启动、未改产品；不以30秒内功能响应称性能通过，未独立复核后端静态查询次数推导。fixture继续同步前，不提前改删。

## 后端批量快照CA68冻结：独源审通过，独测等待前端槽

source-freeze-v2.json SHA CA68BBCC4C6FA19496E4F4A13ABBEB8CD25183E33BE5A46A4E3D2390CA3EC06E，9输入/14真实Gradle产物/3日志XML逐SHA独核匹配；与06BE精确只有DictService、DictBusinessAccess及PG测试3文件变化。独源审无已证mustfix：snapshot原真实REQUIRES_NEW readonly RR保留，可信主体来源核验一次，每唯一schema结构/分类一次，每owner有序主清单与批JOIN明细；冻结主code仍参与全局冲突，main NULL保原getInt未冻语义（JOIN COALESCE），item NULL仍原<>1排除，空字典、顺序与不可变摘要输入保持，其余query/cache/管理无新bypass。新增PG真实Connection statement计数，基线<=60且增加10字典后相同，旧RR/并发等断言保持。开发50/50计数与两项通过尚不冒QA独测；前端仍持重槽，QA等其归还后仅同HTTP类+PG单方法，未并发启动Gradle。产品未重建重启、fixture同步前无新写、浏览器锁归frontend。

## CA68后端独立两目标实际通过

前端归还槽后，QA实际Gradle仅DictSyncHttpTest全类及DictSchemaIsolationPostgresTest.conditionalSnapshotReadsOneReadonlyDatabaseVersionWithoutSharedCache单方法，--rerun/offline/no-daemon/no-parallel/maxworkers1，自然exit0，BUILD SUCCESSFUL38s、29tasks1executed28up-to-date。两份XML各1项/0fail/error/skip，方法名精确匹配；隔离PG真实输出SNAPSHOT_JDBC_STATEMENTS=50; EXTRA_DICTIONARIES=10，增加10项后计数相同断言通过，原HTTP认证/条件响应、RR并发/缓存等断言保持。运行后9输入SHA无漂移，无mustfix。证据.gradle/qa-dict-sync-batch-20261101/result.json SHA524C2D72123C36189A1FBBE9AF75872E684C2EB4E2F370ABB5F5A031E31E4C61，QA日志SHA F0802F0AF3FBC6B1069B1CC26AA33959E8E47DE71D1E0274992ABED670103747；XML已独立复制。未连真实dev/Redis/服务、未重旧0006或前端47，非生产耗时SLA。重槽已归还frontend补role tagRender类型等价修正后的必要检查，旧role5F8F冻结不采纳；QA未新建浏览器草稿或写临时字典，等待新role freeze。

## 用户重启后fixture只读回查

20:32前后完成470260角色UI未保存草稿并放弃后，正常进入字典管理、从业务列表打开既有EVA_QA_DICT_SYNC_20261101_01，等待管理详情正常加载后回读code/name均原值、remark QA同步验收、唯一明细QA_SYNC_01/同步前/排序1；可见主行DOM键仍BUSINESS:2116862974824505346，同一主记录无漂移。无编辑/保存/删除字典，页面无未提交修改并markHandoff交root等待A35 probe基线；尚未修改标签，未代运行探针。管理UI读取不能冒条件GET及React内存map更新已观测。

## 真实基线后UI阻断及探针Vary窄修

root通知A35真实BASELINE_READY后派QA仅改指定标签。fresh UI仍管理员及同fixture同步前，但新增/保存disabled、明细只查看无编辑删除，未进入任何写控件；一次正常reload被官方Edge明确阻止，提示该页有另一扩展UI打开。按hardstop停止，不重试/换provider/native/privatefetch，未改标签或保存，立即回root不得发modified.signal；当前未签编辑或清理完成。

探针重复Vary解析窄修新SHA5683AF49D72F75F90EC30CA50B1F4B9D5FF3953C73D7D47D618447EFBF174766独核，只有collapse_headers按小写名合并Vary及新增fake；QA内存逆补丁精确恢复A35FED旧SHA，证明其它5请求/URL/秘密/超时/信号策略未变。QA实际5fake及PLAN exit0，无真实HTTP/input。旧A35在途版不受新文件影响，varyIdentity false不能推出server缺头，旧最终successfalse证据不得改写；本轮未重请求，不以fake冒live Vary已证。

## 临时字典修改回读及正常UI清理完成

用户明确“Edge在主要屏幕、已刷新、继续”解除hardstop后，按root恢复唯一官方Edge1锁；fresh inventory可信localhost、管理员、无draft。正常选择同fixture并等待加载完成，维护资格恢复，主可见DOM键BUSINESS:2116862974824505346/原code-name/remark及QA_SYNC_01同步前/sort1均核对。20:41前后仅明细keyValue改同步后、提交本地行，再主保存一次；等保存及列表刷新结束，正常重新选择同主项，等待get完成后再次回读code-name不变、remark QA同步验收、唯一line QA_SYNC_01/同步后/1，非只看乐观显示。

随后仅该主行点击删除并正常确认一次，约20:44页面明确删除成功；列表刷新完成后按精确code EVA_QA_DICT_SYNC_20261101_01查询，仅系统/业务虚分组、目标有效行DOM count0、详情与明细为空、dialog count0，无未保存修改。20:45:11北京时间回报root交还UI锁，可由其安排既授权一次仅该fixture物理只读后验；QA不执行DB/Redis查询或写。数据库明细ID依旧UI未暴露，不以展示行键冒ID；主记录ID2116862974824505346精确已记。此次未触碰其它字典/角色、未重建fixture/重启/修改产品源码，未console/authprobe/login/密码请求/token/privatefetch，未重跑47/2/13。全局React字典map的后台即时更新仍未现场观测，证据仅管理保存回读/逻辑删除UI闭环、独立自动化与root持有真实200→304基线，不升级为overall live同步全绿。

## 固定fixture物理后验离线核验完成

仅离线读取backend已完成的固定后验脚本/查询清单/结果，不执行或连接DB。结果.gradle/native-self-bridge-d746ced9b00847cab95aa63614a86dfc/dict-fixture-postcheck-result.json SHA D87FAE23B6911BE24E66B5EC8610A97AE1B4ABA3E3C35F60EC98E32937D9A90F，清单SHA0568BAAF52E63633D43A40D287802283896939F78D079B4B570727B1D033EB3C独核匹配且结果内绑定清单hash。清单仅起止session、固定master、仅其items及精确code有效count共5SELECT；脚本真实事务双只读/RR、固定eva来源及approved dev fingerprint/source-build匹配、0写/rollback/close限制与结果相符。

20:46:17.039451至.686069+08结果：主2116862974824505346 BUSINESS/code正确，唯一明细真实ID2116862983326359554/main同主/QA_SYNC_01/同步后/sort1；主从均物理保留且deleted1793537002，activeFixtureCodeCount0、activeItemsUnderMaster0，INDEPENDENT_FIXTURE_READONLY_POSTCHECK_MATCHED。单连接/5SELECT/writes0/rollbackclose true，runtimeProcessConfigurationVerified false边界保留。由此管理修改回读与逻辑清理已闭环，不等同React全局map现场观察；不新增登录/探针/测试/数据操作或提交push。
