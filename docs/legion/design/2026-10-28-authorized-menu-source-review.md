# 专用菜单实现独立静态安全复核

2026-10-28，technical_designer。已读实施附件及六个实际文件、当前数据权限handler、TenantSchemaRouter/CoreSchemaExecutor与实体/建表依据；未运行辅助、编译、测试、DB、HTTP、IDE或服务，未改业务源。

## 结论

当前生产差异未发现可复现的安全阻断或扩大API/DB契约。专用service/mapper/XML/扁平投影及AuthCtrl最小调用替换符合批准的共享调用隔离范围；可进入新隔离验证方案准备。测试源码真实使用MyBatis/XML、安装数据权限插件及合成EmbeddedPG，但尚未执行，且异常恢复覆盖有下面的明确缺口，应补齐或明确保留未证后再作验收结论。

## 实际文件身份

| 文件 | SHA256 |
|---|---|
| AuthCtrl.java | 5A617F990312644D6F13B33C16CD4D981AB9D182F37238CAD7ED13E2E49D42CB |
| AuthorizedMenuService.java | 0407FF78B90C7771DCCA6352B3990B8C72F3FFC80A930F140C18450433DA9982 |
| AuthorizedMenuMapper.java | A08D7625E2D56774F302262BAB53ADDDAB7E2131E122DEEBE839FB30D47645C2 |
| AuthorizedMenuRow.java | 4402E87C405269AD66B9F5A4601372B530B0ACF40BDECD0199527D6809844BFE |
| AuthorizedMenu.xml | 0F14C0C7EB3B572E58957CB9B2AE40E0C0792BFFACAB2B856EBF6DF288329F26 |
| AuthorizedMenuPostgresTest.java | CF04C881B9562E4E3AF10D9BB09833E2CD463AD95278B96FC4462223146797A0 |

前五生产文件位于SYS-service对应包/mapper资源；测试位于web-booter/src/test/java/org/pkaq/sys/module。六项均独立重算并与交付一致。

## 授权、调用及投影

AuthCtrl只替换菜单服务依赖/调用，URL和Response结构、异常处理未变；不接受客户端uid。服务从ThreadUser取正uid并要求实际只读事务。旧ModuleService、ModuleMapper、RoleService、UserService的scoped git diff均无本次差异，不在共享listGrantedModules/handleFetchResource上加Ignore。

仅两个精确Mapper方法@Ignore取消业务dataScope，handler/全局配置/认证许可未改。第一查询在本地可信schema按user→有效role→resource引用；第二只取该ID集合对应的有效module/resource，schema模式再求tenant平台授权交集。空ID的XML分支AND1=0，服务空授权提前返回；无按模块全取兄弟资源。XML autoMapping=false，返回仅菜单与资源VO，无作者或业务实体字段。去重及循环拒绝在服务聚合，缺授权父节点作为根、不查询未授权祖先。

V1__INITDB与StdLineEntity确认resource有tenant_id但无frozen，SQL未造r.FROZEN；role/module按deleted0/null及frozen非1/null，9999可读不等于可写。standalone只取tenant0/null role/module/resource，符合本路径明确独立模式边界；接口授权判定仍不由菜单结果代替。

## 路由与最小性

standalone验证配置模式、关闭tenant、认证tenant0、事务datasource绑定、current_schema及五表解析namespace；schema先拒tenant<=0，用现router在只读事务选可信schema并核三租户授权表namespace，防缺表从core回落，再用现CoreSchemaExecutor切core查询。原core executor的finally恢复路径，服务外层finally再恢复search_path，嵌套finally恢复TenantContext。sys-service不反向依赖auth，不放宽resolver或造平台0身份。

namespace逐表查询增加固定往返，但对应当前tenant,core search_path可能从core补齐缺失授权表的具体风险；不是无依据通用校验层。本次未引缓存、全局权限框架或改路由基础设施，可保留。对coreExecutor/JdbcTemplate与SqlSessionTemplate实际同事务连接、Spring代理注入和运行schema绑定，仍须独立隔离执行证明，静态构造不能代替。

## 测试覆盖及精确缺口

四测试方法创建独立EmbeddedPG与合成表/数据，不读应用连接配置；web-booter已有embedded-postgres:2.2.2依赖，本次未改依赖。真实旧ModuleMapper+新Mapper/XML和MybatisPlusConfig插件并存；旧SELF作者谓词与专用投影、兄弟资源、删除冻结、重复/无授权、A/B同userId+平台交集及缺表回落均有实际调用断言。resolver是合成101/102实现，不宣称覆盖TrustedTenantSchemaResolver。

应补的具体用例：当前schema异常用例DROP tenant_102.sys_roleuser_ref后在requireTableSources失败，此时尚未进入coreExecutor；正常和异常的外层TenantContext均从null开始，缺少已有合法非空上下文的恢复断言。因此现四方法不能支撑“core查询异常与嵌套非空上下文均已覆盖”的交付口径。最小补法是在合成环境中对实际selectMenuRows准备阶段精确注入异常（避免用实际SQL错误使PG事务先行abort），断言失败后search_path和原非空TenantContext一致；正常嵌套同样断言不被clear。继续用真实Mapper/SQL链，不用mock整个服务返回值。

另需明确：0001/0003用deptId0/空orgIds，证明专用元数据不受业务拒绝影响，但不证明有实际部门匹配的业务数据集合正确；事务测试直接构造服务+TransactionTemplate，不证明Spring @Transactional自动代理。现用例的statement ID断言为配置绑定检查，SqlCapture实际仅记录SQL，未记录执行statement ID；不把它写成已捕获运行statement身份。这些可以分别由既有回归/新增窄断言验证，不改权限契约。

独立新RUN方案还须预检离线嵌入PG二进制与临时数据/进程目录、覆盖web-booter依赖闭包、所有Project输出隔离及新主产物白名单。EmbeddedPostgres.start源码用法不等于已经证明不会下载或标准临时目录满足新RUN边界；缺依赖停报，不直接运行标准build。旧helper/run/seal冻结，历史203不计作本次通过。最终还缺真实菜单HTTP/运行绑定及业务SELF现场验证。

## 测试窄补及26类准备计划复审

实核测试SHA4B449EA4F85D76F0CA11C7A429C03AF32FA1109CE0F39A1C4A919390FA79FFFE，仍四个@Test；五生产文件哈希均保持本表值。新增Executor.query四参数拦截保存当前MappedStatement ID并finally恢复，在StatementHandler.prepare记录该ID与实际SQL；拦截器为每fixture实例，无静态共享状态。当前同步mapper调用链适用，未来Test须关闭JUnit并行且单fork，不把它当通用跨线程追踪器。

测试对selectMenuRows实际prepare精确注入异常，先用同fixture JdbcTemplate断言已在core public，再抛非SQL异常，避免事务abort掩盖恢复；断言最深异常原因、原search_path与101/tenant_101非空上下文，并检查实际prepare ID。正常嵌套路径也核相同恢复；外层fixture最后恢复路由。useCache=false/flushCache=true避免第二次菜单查询由缓存跳过prepare。有效身份无事务新增独立拒绝断言。此前三项静态覆盖缺口因此已补源码，未执行，不称已通过。

联合计划26个selector均唯一且当前对应测试源文件存在，SYS13/AUTH6/DATA3/web-booter4。11main闭包、最终恰四Test无producer、四模块准备产物锁、原C494仅AuthCtrl允许变化及三class/新XML新增、web-booter四class/27resource精确首封存可进入辅助实现；498/529及33任务只作候选，不代替实际模型。额外编译产物应停审，不通配放行。

独立读安装embedded-postgres2.2.2 sources（SHA333939ED…）确认getWorkingDirectory用ot.epg.working-dir、Builder parentDirectory与解包默认同源，缺省数据目录用Files.createTempDirectory(epg)。精确核指定Windows binary14.22.0 jar SHA为BADC584F9598557E91F4C95D811C277207CD9DAC94C7CC5DA4ED70DD4E64B307。whole web-booter JVM同时绑定ot.epg.working-dir、java.io.tmpdir、TEMP/TMP与workingDir至新RUN可覆盖四类默认start；实际CP唯一binary资源、库及归档/架构锁须由辅助门禁实现。未独立复算归档内全部exe/PE证据，backend的内存归档结果不可写成本角色实测。

总控已授权四类五处builder显式listen_addresses=127.0.0.1，动态port0不改，待最终SHA和仅五行差异统一核。此前4B449为loopback前中间身份，不作最终执行锁。没有发现阻止新增隔离辅助实现的静态问题，但不授权执行。

辅助失败收尾必须覆盖Fixture构造期间已start但随后抛错的情况：这时try-with-resources还没拿到Fixture，不能靠close保证无进程残留。只能针对已记录的本Test JVM派生链、RUN内exe规范路径及SHA、RUN内-D数据目录共同确认的ownedchild收尾，不能按postgres进程名杀或依PID复用误伤服务；证据不全停报。短生命周期initdb/pg_ctl与持续postgres应分别记录，库吞stop错误不能当退出证明。进程目录、实际loopback动态监听与终态无残留仍需新RUN实际证据。

## loopback五处最终静态验收

实际重算四文件SHA并在内存以Latin1逐字节映射逆向撤销builder替换，四个reverseExact均true（原CRLF/LF均保留）：

| 测试 | 替换数 | 最终SHA256 |
|---|---|---|
| AuthorizedMenuPostgresTest | 1 | 8190BB1ACE982FDE3B622AF808FA654993DC99A1A676294995E599B4AE605715 |
| TenantSchemaModePostgresTest | 2 | 8F3DB6BFFC3F9630B8C84DC39C2C5A34EA9312532FDB5A7F3168916D918CB963 |
| TenantSchemaAdvisorOrderPostgresTest | 1 | 695418BE9775CDF36B06E7D15A70CFDBEDC0529A899C99CC09E6469347017532 |
| RoleSchemaIsolationPostgresTest | 1 | E8F612D91A2D8D347FFDA96487CEC886A4305FBAE9F3BAA1A5531BD008AE9DAC |

仅将EmbeddedPostgres.start改为builder().setServerConfig("listen_addresses", "127.0.0.1").start；无setPort或其它逻辑增量，原测试默认动态端口保持。旧三文件实读diff仅四处替换。新菜单测试逆改精确等已审4B449，旧三逆改精确等backend交付修改前哈希。本轮无静态阻断，可按总控后续授权准备新辅助；未执行编译、PG或JDBC，IPv4连接、实际监听与收尾均待实际验证。
