# OpenAPI 集中审计 A：独立安全复验（2026-10-24）

结论：本轮最小实现的独立代码/隔离测试门禁通过，未发现新的必须修复缺陷。116项/25类亲跑全过；另当前编译类纯内存线程探针确认窗口不传播到显式重绑同一用户的子线程、嵌套异常恢复、退出拒4200。真实HTTP/数据库/已加载服务、standalone当前运行模式、platform实际core路由与旧记录物理存储位置均未验，不以隔离绿宣称全部完成。包S零创建/零授权与登录阻塞保持。全部命令结束，读测锁交回总控。

## 批准规则及生产链

完整读设计2026-10-23-openapi-log-boundary.md第7/8节批准政策、backend09:08交接、当前Gitdirty及本轮6生产/4测试；并读权威eva-api-table-naming新规则。现SQL按已确认SYS_ACCOUNT/SYS_ROLE/SYS_ROLEUSER_REF物理名读取，关系表_REF；本次无表设计/建表/迁移或存量重命名授权。原大量dirty与cornerstone保持，未把先前日志/日期/Frozen等差异混报为本轮新增。

资格：OpenApiAuditReadAccess对当前ThreadUser要求userId>0、可信tenantId=0、standalone且非schema或platform且schema；缺身份/普通正tenant/不支持mode在资格查询、资源与Mapper前拒绝。可信源静态核JwtAuthFilter经permissionContextService从服务端身份组装后runWithUser，AppKeyAuthenticationFilter仅SecurityContext ROLE_API_USER，不由其字符串角色伪造ThreadUser人类资格。API/inspect/TENANT_ADMIN/ALL/空roleMap都不是正向依据；空roleMap在resource关时有效管理员可经窄查通过，但resource开仍原RBAC（含原permit语义），并不新授ALL或更改全局角色装载。

窄资格SPI在auth，Jdbc适配器参数化userId与ROLE_ADMIN；SQL account/role均TENANT_ID=0、DELETED=0、FROZEN IN(0,9999)，membership.USER_ID=account.ID及role.ID=membership.ROLE_ID。原DDL关系表仅ROLE_ID/USER_ID复合键及索引，没有deleted/frozen列，不能臆加过滤；实体账号和角色逻辑删除/冻结均显式核验。旧角色快照缺R.DELETED查询未复用/未全局改。已删/冻结资格本轮为真实SQL谓词检查+替身返回false组合，不冒称PG实际删除数据验证。

模式/路由：standalone要求不冲突TenantContext，沿配置唯一数据源schema，内层绑定可信0/schema后finally恢复，没用请求schema。platform仅可信0，经真实@Transactional(readOnly)代理和CoreSchemaExecutor显式set_config本事务search_path到core，资格与日志动作同窗口/同事务，finally原search_path、TenantContext和读取窗口均恢复/清理；失败闭合。没有TenantAuthRouter.execute(0)捷径。现库JDBC默认schema/search_path/旧NULL日志物理位置未读，不把配置字符串绑定或mockJDBC当PG实证。

读取：OpenApiCallLogService仅get/list新增同一readAccess入口，资格和resource都完成才开OpenApiAuditQueryScope，参数/VO/Response/时间戳<=与筛选原样。Scope普通ThreadLocal（非Inheritable）绑定同一ThreadUser对象引用，精确完整Mapper命名空间+selectById/selectList/selectPage/selectCount/两个_mpCount白名单；其它Mapper/同Mapper custom方法/API_CALL_LOG旁路拒绝。DataPermissionHandler在ignore/exclude/通用scope前先核该表窗口，合法窗口只免该表CREATE_ID/scope，其他表仍原deny/范围；无全局Ignore/新exclude。真实MybatisPlusConfig先data再pagination，自动count在同同步读取动作内完成。data关闭插件不注册，但Service管理员常驻guard不随之消失。窗口是已核资格后同步内部调用工具，唯一生产调用者ReadAccess；不是把其public方法或当前ThreadUser当新的远程授权入口。

save未改，匿名/鉴权失败审计插入不经人类readguard，insert非查询拦截分支，失败仍隔离。既有存储schema路由未由本轮修改；不能据本轮read测试保证实际新旧日志都已在core/standalone同表，须运行环境后续确认，不回填TENANT_ID或迁移旧日志。

## 测试加载/快照独立核对

先完整逐行审.gradle/openapi-current-classes-20261024.gradle：编译auth两compile任务前置6依赖当前class目录并显式dependsOn其compileJava；auth/coredata/sys的Test依赖同compile并前置7模块快照classes/resources，各doFirst校5关键原输出及快照hash，输出3次验证日志。没有源码/build.gradle/依赖/服务/数据库写操作。5类hash本身不足覆盖所有相关依赖，所以QA没有盲用该清单。

运行前独立逐文件核7模块当前build/classes/java/main全部class与快照的相对路径/数量/hash；resources逐文件与当前src/main/resources核。运行后再次核所有class及resources，且resources再与实际build/resources/main核，0缺文件/0hash不一致/无多余文件。7模块class数：contract28、common81、cache8、log16、data42、web-core20、auth93，共288；resources contract0/common7/cache0/log0/data2/web-core0/auth3，共12。编译/资源任务在本次亲跑均UP-TO-DATE，Gradle依据当前输入复用；sys自身classes/testclasses按标准target classpath，未被旧依赖sys JAR代替。快照是隔离测试用拷贝，不是运行发布产物；本次源码/产物全核匹配才可使用，未来若源码或任一依赖/资源变化须重新编译、构造并全量核对，原init仅5类检查不能自动证明未来全部一致。本角色未修改init或快照。

## 独立回归与新XML

亲跑backend交接的准确最终联合命令：

```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' -I .gradle/openapi-current-classes-20261024.gradle :eva-web:eva-web-auth:test --rerun --tests '*OpenApiAudit*Test' --tests '*ResourceAuthorizationServiceTest' --tests '*AuthPermissionContextServiceTest' --tests '*RoleResourceCacheServiceTest' --tests '*SysRoleResourceMapperTest' --tests '*MybatisPermissionSnapshotQueryTest' --tests '*OnlineUserServiceTest' --tests '*LoginLogServiceTest' --tests '*TenantRoutingBoundaryReviewTest' --tests '*AuthStorageBoundaryTest' --tests '*JwtTenantClaimsTest' --tests '*TokenCtrlCapabilitiesTest' --tests '*LoginUserInfoVoCapabilitiesTest' --tests '*CacheTokenUtilTest' :eva-core:eva-core-data-mybatis:test --rerun --tests '*OpenApiAuditQueryPermissionTest' --tests '*MybatisPlusDataPermissionHandlerTest' --tests '*LogQueryPermissionInterceptorTest' --tests '*PostgresBusinessLogRepositoryTest' --tests '*PostgresErrorIncidentRepositoryTest' --tests '*BusinessLogDateRangeTest' --tests '*TargetTenantExecutorSecurityTest' :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests '*RoleServiceTest' --tests '*ErrorCtrlDateRangeTest' -x jar --offline --no-daemon --console=plain
```

exit0 BUILD SUCCESSFUL22s，19tasks3executed16up-to-date；auth/coredata/sys三个test任务真实执行，三次OPENAPI_CURRENT_SNAPSHOT_SHA256_VERIFIED和前置输出列表已见本次工具结果。新XML UTC2026-10-24T01:11:14.173Z至01:11:22.731Z（北京时间09:11:14至09:11:22），25类116、failure/error/skipped全0：auth16类68、coredata7类30、sys2类18。实际XML逐类核，不引用开发09:06旧XML；每模块build/test-results/test/TEST-完整类名.xml可定位。

新增33=Access22+CoreRouting6+MapperSql1+QueryPermission4。保原resource/authcontext/cache/rolemapper/snapshot、login9/online4、Jwt3/Token2/permVer/存储架构/tenant路由、biz/error guard/date、RoleService14和实际ErrorCtrl日期4；本轮不是此前107+86+116累加，当前selection重写结果目录，未执行原脱敏全套或ArchiveJob2就不说同次全部重跑。

真实组件边界：Spring AOP事务代理+DataSourceTransactionManager/CoreSchemaExecutor真实执行；连接/JdbcTemplate是mock，SQL资格谓词由真实适配器送mock检查，没有PG search_path/commit执行。真实BaseMapper注入SQL、DataPermissionInterceptor/PaginationInnerInterceptor生成selectList_mpCount，Executor以合成count返回，无网络/DB。Access四组合data/resource及真实局部Bean装配、普通/正tenant/无身份/资源拒绝/资格异常/上下文冲突、匿名save/insert异常均隔离。其他mapper/table/count/同用户ID不同对象、嵌套异常窗口恢复测试按源码确认为实际拦截器，不仅mock guard。

另独立JShell纯内存探针使用当前hash已核contract/common/data编译类与本机slf4j binary，无源码/测试文件修改：父线程开实际Scope，子线程显式runWithUser同一个对象，实际requireAuthorized仍抛4200；父嵌套Scope异常后仍能读取，退出后拒4200。输出SAME_USER_ASYNC_DENIED=true、NESTED_EXCEPTION_PARENT_RESTORED=true、AFTER_SCOPE_DENY=4200。这不是线程/事务HTTP集成测试或Mapper执行；SLF4J无provider提示仅探针日志环境限制。无秘密/真实日志/外部存储或服务。

开发此前初始跨模块输出依赖校验失败与路由fixture when-restub触发旧answer的红日志仅作为其过程证据保留，不宣称本轮重现或生产错误。本次没有新编译/用例失败。git diff --check0/index空，既有CRLF/Gradle/JVM警告保留；-x jar只是规避用户运行锁，本轮未完整包装或验证当前进程类加载。

## 一致加载与后续实际验收

由总控/用户按原方式停止旧后端后，在同一checkout正常 :web-booter:bootJar（不带测试init、不-x jar，不自动clean/DDL/改配置），确认新包包含auth新ReadAccess/AdministratorQuery/SPI/Service及coredata Scope/Handler、必要当前common/contract/resources和既有Mapper新签名/XML，一致产物按原配置完整启动；IDE则重建相关classes/resources/依赖后完整启动。不能只替某个class、混旧auth或旧coredata JAR、把本轮快照当部署包。QA没有启停/打包或读取当前服务配置。

运行需合法可控入口/测试客户端与可信身份：standalone当前模式/唯一schema及有效admin、普通身份否定，data/resource开关各实际已获准配置，list/detail/total同资格同源、角色删除/冻结与资源撤销失败闭合；platform仅适用已获准环境验证可信tenant0 core与正tenant否定/路由恢复。当前没有已核OpenAPI日志静态UI，不能自行猜端点/复制token绕HTTP；包T未批准，不新建租户或切现服务模式。旧NULL归属不回填/物理位置不推断。包S仍零对象/零授权、登录接管前置待满足，浏览器未重试；全部sys/logCRUD、双角色/tenant/字典与真实日志边界继续矩阵，不能all done/提交push。

本角色本轮仅写自己本QA卡和sys-log矩阵，无源/依赖/配置/vault/服务/DBHTTPRedis/UI/授权/发布操作。所有命令退出，源码读测锁交回总控。