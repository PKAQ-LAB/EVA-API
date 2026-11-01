# 字典登录本地映射与条件同步最小契约（方案A已批准，分角色实施）

发布收口：CA68 后端 HTTP／隔离 PostgreSQL 两项已获独立 QA 实际 Gradle 验证；A2A710 前端独立定向验证已完成，真实接口已取得 200／200／304 基线，临时业务字典的修改回读、删除及只读物理后验均完成。浏览器全局 React 映射接收远端更新未直接观察，边界见本轮收尾记录。下文“独立 QA 仍待”等文字为当时的阶段记录，不作为当前阻塞。

最新优化阶段：后端批量候选`.gradle/dict-sync-20261101/source-freeze-v2.json` SHA256 `CA68BBCC4C6FA19496E4F4A13ABBEB8CD25183E33BE5A46A4E3D2390CA3EC06E`替代原06BE。9输入实算匹配，相对原签仅DictService、DictBusinessAccess及PG测试三变，原0006 v3的40输入仍0漂移。本角色逐源核验一次可信requireSource、请求内schema结构分类去重、每Owner完整main含冻结冲突及JOIN批量items、NULLmain旧语义、顺序空map及同次hash均保持，无public bypass或其它缓存管理变更，未见新阻断。开发者隔离schema PG报告计数50（含路由与测试SHOW），增加10字典仍50；本角色核到实际Connection语句计数及相等断言，未重跑、不称生产时延SLA或standalone精确数。独立QA仍待两个target复验，不运行真实probe。

2026-11-01 technical_designer。总控转达用户在说明复用全局initialState.dict后回复“可以”，正式批准方案A并分别派发backend/frontend唯一源写者。本角色只维护设计和独立读审，不修改产品源码、不测试或连接DB/Redis。0003右侧部门树交互已完成候选独审，0003数据范围语义不变；0006源码及现库维护完成但用户新后端重启/live联调未验，不被本任务覆盖。

已批准方案A：会话内存快照+ETag，复用全局initialState.dict，不使用localStorage；页面visible且online时60秒校验，focus/online恢复事件10秒合并，本标签页字典提交成功立即校验。普通网络断连/timeout/5xx保留同身份最后有效快照并标stale/retry；认证失效、退出/切用户或租户、可信来源冲突或响应结构无效清可用快照及ETag，保留表单原code。此批准不包含DB结构/版本表/缓存清空/服务启停或Git发布。

原选项B（仅事件无定时）保留为未采用的比较历史，不再作为待用户选择项；不重复确认已定60秒/10秒、0003或0006语义。API冲突机器code、304客户端分支类型及ETag可信身份输入由前后端成员协调并抄设计核对后在本文件记录，不能自行扩大合同。

## 当前源码证据

前端`src/app.tsx`的getInitialState在服务端登录态初始化后调用fetchDict，将全量字典放入initialState.dict；`src/pages/user/login/index.tsx`登录后也并行请求菜单与字典，然后写入同一状态。`src/services/user.ts`使用既有GET /api/auth/fetchDicts。未发现字典写入localStorage/sessionStorage；当前内存初始快照不是带版本的同步方案。

`src/components/DictSelector/index.tsx`在无显式data时仍每个code请求/api/sys/dictionary/query/{code}，未复用initialState.dict；log/biz和module/linelist等已有本地快照直接映射。因此“登录已加载”与“所有组件直接映射”目前未形成完整闭环。显式data属于调用者数据，仍优先，不可因统一字典缓存吞掉其现有语义。

`src/utils/authState.ts`的clearAuthState清凭据、用户与菜单存储；无字典snapshot/ETag/同步中generation的统一清理。当前并无已验证的完整租户切换同步契约，不能声称现切换已经安全覆盖。

后端AuthCtrl.fetchDicts返回现Response.success(dictService.fetchDicts())，没有ETag/304逻辑。DictService.fetchDicts在只读租户事务中先inventory核完整SYSTEM/BUSINESS源、分类与code冲突，再按Owner读取原共享缓存；缓存未命中读完整明细。字典维护提交后只失效对应BUSINESS Owner的code键，没有字典变更递增用户permVer逻辑。不能绕开inventory直接按客户端ETag返回304，也不能把缓存中恰好存在的一部分code当完整快照。

## 推荐最小实现边界

快照内部优化限定：package-private snapshot方法自行执行一次现sources核验，捕获同请求可信主体/连接/Owner；每Owner一次inSource完成未分类拒绝、完整未删主清单（冻结项仍先参与同码冲突）及一次有序JOIN批量明细读取，全部完成后才生成map/ETag。不提供public免校验参数、不跨请求缓存已验证来源、不改其它query/管理/serializer缓存链。主项NULL frozen沿原getInt映射0可输出，批量筛选不得误排；明细继续原frozen<>1语义。保空字典、Owner/main/item顺序及RR快照。SQL数与字典数量无关的目标须用隔离PG实际计数证明；40条仅当前方案静态估算，不是产品时延SLA。

1. 复用现initialState.dict作为唯一可渲染数据快照，补同一状态的同步status与validator；组件只从它读取code→value→label。仅在初始化/统一同步入口请求全量字典，不再由每个DictSelector各发query请求，不另建React Query缓存、localStorage字典库、版本表或全站push。
2. 首版“本地”明确为当前已认证会话的浏览器页面内存；页面刷新重新认证并拉取全量200。不开启持久离线缓存、不把字典/ETag放cookie、localStorage或sessionStorage，不在浏览器账号之间保留字典，也不将token写入字典缓存键。
3. 新的成功登录、用户或租户身份切换开始、退出、登录失效立即清快照与ETag并递增请求generation，取消或忽略旧请求。所有同步响应只有在其捕获身份和generation仍匹配当前有效会话时才能提交；同一uid重新登录也视为新generation。服务端只用可信认证主体决定来源，不接受客户端user/schema/Owner参数改变范围。
4. 同一会话并发刷新合并为一个在途请求，登录页与应用启动复用同一初始化结果，组件挂载不触发额外同步；token正常续期且主体/租户不变不等于登录切换，不强制丢快照或要求重新登录。

## 既有端点的条件请求

保留GET /auth/fetchDicts及成功200的Response.data映射结构，增加响应ETag和请求If-None-Match支持。无本地有效快照的初始化不发送validator，返回200全量；同身份有快照才条件请求。服务端每次先完成认证、可信来源解析、完整分类/冲突核验，再基于实际要返回的完整结果生成稳定validator；相同返回304且无业务body，变化返回200和新完整映射/ETag。不得把304包装为success=true空字典。

后端只读核到必要一致性修正：当前cachedValues的miss→DB→cachePut可与提交后evict发生竞态，旧读取可能在evict之后回填；dictdata TTL配置0s不过期。因此不得直接对现Redis内容哈希并宣称能发现所有后台更新。最小建议仅同步快照路径采用单次实际只读REPEATABLE_READ事务，复用现selectDict中的inventory+completeItems完整数据库投影，一次构造有序快照和摘要，不在该路径读写Redis；不为此改管理query/serializer其他缓存链，不新增缓存、版本表、锁或计数器。事务须经真实代理进入，不能靠同类自调用丢失隔离声明；快照和摘要共用一次构造，controller不分别两次读取。条件304仍需完整来源核验与DB读取/hash，只减少下发体积。

validator是不可反解的内容摘要并绑定响应格式版本与可信来源身份，禁止暴露schema或用户ID明文；不得只取max时间、只看主表revision或当前租户一处计数而遗漏SYSTEM、明细、删除/冻结变化。字典值及有意义的展示顺序变化都必须使validator变化；哈希的规范化须稳定，不能依HashMap偶发遍历顺序产生假更新。304只代表当前读快照一致，不承诺响应之后发生的并发提交已经可见。

客户端必须在现request拦截器/错误处理之前正确识别304无body，并取得ETag头；现全局errorThrower会把无success的普通解析结果当失败，不能直接把304送进现JSON业务成功解析。仅为此既有端点做窄适配，不改变其它API错误语义。304到达但本地快照已丢失时，最多重试一次无条件全量请求；身份已变则直接丢弃，不拿旧validator恢复旧数据。

推荐该端点使用private/no-store响应策略，避免浏览器/代理形成另一个不可控持久字典缓存；ETag由应用内存显式持有并发条件请求。正式实现按当前实际请求库和部署头行为验证200/304两条路径，不能只凭mock声称304兼容。该策略是本次拟确认的协议增量，不改现鉴权或资源门禁。

后端成员已读当前HTTP栈及本机Spring 7.0.10 HttpEntityMethodProcessor：WebSecurity现有no-cache/no-store头保留，不另造浏览器持久缓存。建议控制器返回正常200 ResponseEntity、保原Response.success映射body并设置弱ETag，让Spring在统一Advice之前执行条件GET并输出无体304；禁止直接手工status304(null)，因为项目CommonResponseAdvice会处理Ctrl的null并可能重新包成功体。CORS当前允许请求头但exposedHeaders无ETag，仅补暴露ETag，不能为了版本同步扩大允许域、凭据或资源权限。以上源码结论来自后端成员只读核验，不表示已完成真实HTTP验证。

200/304仅合并Vary: Authorization,Cookie且保留现Origin等值，不覆盖已有Vary。validator绑定可信user/tenant、现schema代次及Owner身份与格式版本后再对完整有序内容摘要，禁止用请求参数推导源。/auth/**现资源permit不等于匿名，JWT账号/租户schemaGeneration/permVer/session核验仍先于条件响应；字典更新本身不递增权限版本。

## 更新触发与失败行为（拟一次确认）

成功登录/身份初始化全量拉取。页面visible且网络online期间每60秒条件校验，隐藏或离线暂停；focus/online恢复事件按10秒最短间隔合并。当前标签页后台字典保存/删除/冻结提交成功后立即统一校验，不受上述10秒限制。并发触发复用同一身份在途请求；若写提交发生在旧同步请求已经开始后，须在该旧请求结束后补一次校验，不能仅复用可能读到提交前内容的旧结果而丢掉提交后触发。其它标签页或管理员变更在下一次成功校验时发现，不承诺离线、后台暂停或请求失败期间60秒内完成更新；不新增跨标签页广播或实时push。

每次有效200必须校验success及完整code→字符串映射形状后原子替换，移除旧快照中已删code/值，不做只增不删合并。空的合法完整映射和失败分别表示，未知code/值显示现缺失态，不伪造“全部”或新字典项。原显式showall交互不升级为业务授权。

同身份普通网络断连、timeout及5xx等临时失败保留最后有效快照与ETag，标stale/retry，不伪装成功或成功空集；从未取得有效快照时明确不可用。离线不跨身份复用，也不从持久存储恢复；网络恢复后沿同身份条件校验。鉴权失效走原退出，退出或切用户/租户立即清可用快照、ETag及旧请求generation。可信来源/同码冲突或响应结构无效同样清可用快照与ETag，阻止旧标签继续作为当前有效候选。表单已填写的真实code均保留，不伪造label、不借失败重置用户输入；普通临时错误不强制重登或修改permVer。

错误类别必须由可信响应明确区分。现来源冲突可能被统一包装为5xx，不能仅按HTTP 5xx一律判临时网络错误：实施时须为此端点提供可识别的来源冲突机器结果，沿项目既有错误格式，不新增多套错误体系，不泄露schema/明细内容；现统一异常处理、可用业务错误码及前端识别需一并窄审。结构无效由客户端验证识别，不能因某次body不合预期就误分类为可保留的网络失败。真实来源冲突先于ETag命中返回；清空后下次有效身份重试不发送旧validator。

后台普通字典更新只改变字典内容与原Owner缓存失效，不递增用户token权限版本、不强制重登；后端业务保存仍重新校验真实可用字典值，前端快照不是业务授权或校验的替代。SYSTEM仍只读，BUSINESS维护资格/资源校验原样，跨源同code或同源重复沿现流程拒绝且不能304绕过。

## 验证与已批准范围

仅做新增同步的针对性验证：登录一次全量与选择器零重复请求、未变304保持同快照/变200原子替换与删除、SYSTEM/BUSINESS冲突不304、身份切换/退出与晚回清理、同身份临时失败保旧快照及重试、前台恢复事件合并、后台写提交后触发且无需重登。HTTP真实请求库304适配、服务端条件响应与现Owner隔离须各验证；不重复0006视觉或全仓检查，不把源码审当运行联调。

已批准增量为：本地仅当前会话内存快照+ETag、现fetchDicts加条件304、组件统一映射、visibleonline每60秒/focusonline恢复10秒合并/本标签页提交成功立即同步；同身份普通网络断连/timeout/5xx保有效旧快照标stale/retry，认证失效/退出切用户租户/可信来源冲突或结构无效清可用快照及ETag并保表单code；字典更新无需重登。后端HTTP及错误分类路径须窄验证，200/304必须经实际项目HTTP链验证；由已派成员实施，本角色不写产品源，不重问0003/0006语义。

该确认范围还包含仅同步端点使用上述数据库完整投影的一次只读一致性事务，以解决现Redis旧值回填造成的永久漏更新；不借机重写全部字典缓存。现管理list/get/category/revision/frozen/维护资格、checkUnique和validate均继续走原业务接口，不被标签map替代。后端全仓没有已确认的实际VO字段使用@Dict，RoleListVo/DetailVo.dataScope仍原String码；不删除全局serializer或批量修改VO/标签返回合同。

## 精确HTTP接线合同（成员协调值）

| 分支 | 已协调协议 |
| --- | --- |
| 全量200 | GET /auth/fetchDicts，前端/api前缀不变；原Response success/code/message/data，data为完整有序code→key→label映射 |
| validator | If-None-Match；响应弱ETag为W/"dict-v1-<64位小写SHA256 hex>" |
| 未变304 | 正常ResponseEntity 200带ETag交Spring条件GET处理，最终304无body；客户端不得要求success或解析空JSON |
| 可信来源/分类/重复code或结构无效 | HTTP409，原失败Response，success=false、code=0x0000-10002、固定message“字典快照来源或结构不可用”，无ETag和真实来源细节 |
| 普通数据库故障等 | 保原全局500等临时失败路径，不笼统转换成来源冲突 |
| 鉴权 | 保现业务认证失败码/拒绝，不为字典重写全局auth |
| 头 | 200/304 Cache-Control private,no-store；Vary合并Authorization,Cookie并保Origin；CORS仅增加暴露ETag |

后端DictSnapshot摘要UTF8逐段长度分隔：格式v1、可信tenantId/userId、实际Owner schema/category有序列表、完整map及键值/标签顺序。TenantLoginResolver现schemaGeneration确由schema的SHA256派生，实际schema纳摘要已覆盖重指向，不新造版本存储。前端窄判别类型已对齐为`{kind:'updated',data,etag}`或`{kind:'unchanged',etag}`；304要求当前身份已有快照且validator相同，首次无快照不送条件。已核本机Umi生成请求层仅data.success显式false才调用全局errorThrower，空体304可进入窄分支，未修改全局HTTP错误处理。

前端初候选`.git/dict-sync-source-20261101.json` SHA256 `730D3AADB2420485C1DFB04128D2D27E80930FC97B9DD79913C6021230B5F59C`及23输入实算匹配；部门/岗位合并候选除已授权测试夹具外8个输入保持。同步单内存、owner/epoch晚回校验、60/10清理、写入碰在途后followup以及消费者当前label/原码回退方向契合。尚有已报责任成员的运行时认证问题：poll/focus/localwrite中fetchDict使用skipErrorHandler，最终401/认证失效被包装为IntegrityError后sync仅清字典，未保证调用既有退出。即使全局能刷新，刷新后重放仍失效时也必须在当前身份守卫下退出；旧身份晚回不可退出新会话，source409/普通403不可误退出。待前端最小修订与定向验证后再关闭该问题，不据初候选判最终通过。

最新修订已关闭上述运行时认证问题：`.git/dict-sync-source-auth-fixed-20261101.json` SHA256 `A2A710618430041F1097611A8772FA6D1C374573E2349EEB19A718BA5D0ABFAB`及25输入已由本角色实算匹配，旧730D仅历史。DictionarySync在当前owner/epoch/token匹配且isAuthExpiredError时调用既有redirectToLogin再传播，旧身份迟到错误不退出新会话，source409/普通403不误退出。Selector复用finalOptions供options及labelRender，findLast沿原同值最后项优先，合成“全部”和真实0000均有覆盖。

新增dictionaryRequestChain测试确实导入项目生成request、现errorConfig并使用本地axios adapter，覆盖原生200/ETag、空304不触发errorThrower、业务false仍触发旧链；生成request本地SHA256 `0937A9F2F0D346394DB3B156FF624F958BA6B96A0DD58119BCD41B7C4686DE97`实算匹配，未改生成源或生产HTTP处理。Vitest alias仅解析现有Umi依赖，无安装包或新框架。源码独审未发现新增阻断，本角色未重跑测试；独立QA/部署重启/live联调仍由总控收口，不把候选签名或开发者分轮测试报告等同最终运行验收。

施工源审所报缺表分类问题已修：requireTables namespace查询改单行scalar子查询，实际缺表得NULL后typed拒，不捕获全部数据库故障。冻结候选`.gradle/dict-sync-20261101/source-freeze.json` SHA256 `06BEA8A7C5CE27F2DEABC77C90FEB02634AF04AF2C936273455B43947C393EDE`及9个源/测试输入由本角色实算匹配；原0006 v3的40输入也逐项匹配未变。独立源码复核确认同步单路径REQUIRES_NEW实际readonly REPEATABLE_READ、完整DB投影/摘要一次生成、冲突typed409与普通DB错误分离、原200及框架空304、仅CORS expose ETag，未发现新契约阻断。backend报告真实Gradle隔离PG新方法与真实HTTP链定向测试通过，前期fixture失败保留；本角色没有重跑，最终独立QA及前端整合尚需收口，不把源码审等同运行联调完成。
