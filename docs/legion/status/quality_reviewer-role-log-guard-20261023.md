# P0-ROLE-SCOPE-LOG-QUERY-GUARD 独立复验（2026-10-23）

结论：本次5生产文件既定P0代码范围独立通过，未发现新增需修缺陷。原日志脱敏41项保持通过。前端另报P2-ROLE-EMPTY-ORG-LEGACY仍待补修；全项目发布/真实权限/运行加载门禁不据此解除。所有命令结束，EVA-API读测窗口交回总控。

范围与规则：收到总控后端排队任务后，先完成并交回前端读测，再读后端规则、当前Git/status、backend最新交接和设计2026-10-18-dictionary-ownership第9节，审查RoleService、LogQueryAccess、两日志Repository、MybatisPlusDataPermissionHandler及4直接测试；cornerstone/index空，无源码写入。设计9.2明确编辑null保真实原scope/orgIds，原null/非法/未实现值保留且运行仍拒绝；这不是允许重新授这些值，也不是要求改名时自动修数据。前端主体三处去ALL与此配套，空字典仍阻断新增，不恢复字典/权限。

角色审查：新增null/blank/unknown/0004/5/6在角色写入及permVer前拒；显式更新blank/invalid/未实现值也拒。更新未传/null从同@Transactional+TenantSchema中的ensureRoleEditable真实原记录回填scope和orgIds，并忽略同期伪造orgIds；原无效值不转ALL。明确合法ALL保持原契约，没有新授予上限规则。0003相同scope未传org保原值，切换要求非空；完整split(-1)识别空分段，逐项正long/溢出/非法字符拒，不静默剔除。查询完整ID集合，显式可信tenant_id条件，返回可见全集与请求集合相等才保存；使用原schema及数据权限可见性。成功继续原受影响用户permVer递增；失败先于角色/权限版本写。

RoleServiceTest真实Spring TransactionInterceptor + TenantSchemaTransactionalAspect + TenantSchemaRouter，mock DataSource/Connection/PreparedStatement/ResultSet与resolver；断言组织wrapper含tenant_id且参数22、actualTransactionActive、TenantContext tenant_22，拒绝后connection.rollback/nevercommit且上下文恢复、无角色和permVer写。说明JVM事务/路由控制流，不能证明真实PG、数据库约束/隔离级别、用户现场事务或跨租户运行通过。

日志审查：list/get入口guard在ID解析/Mapper/count/page之前，要求ThreadUser userId>0/tenant>=0且明确ALL，多角色并集有ALL可读；null项安全，无身份/缺省/unknown/受限拒。独立调用当前已编译LogQueryAccess（JShell+当前common/contract/data classes与已安装slf4j binary）输出ANONYMOUS=4200，scope null/空/invalid/0001/2/3/4/5/6/7均4200，仅0000 ALLOW。未复制guard或读取真实上下文；JShell无SLF4J provider提示，仅合成验证环境，不改项目配置。

Repository测试对拒绝身份verifyNoInteractions(mapper)，可信ALL检查本tenant、standalone0、原platform admin target规则（rolesMap而非伪roles数组），两种includeArchived、detail/count/page一致。两Controller仍走原Repository入口，原动态资源鉴权未改/无Ignore新增；静态保留不等同于真实URL鉴权负向验收。AuthPermissionContextService仍按角色真实scope构建上下文，不补ALL；数据权限关闭时新日志守卫仍要求可信明确ALL而不会因通用开关放开。

实际DataPermissionInterceptor.beforeQuery + 真实BusinessLogMapper.xml生成selectLogById/countLogs/selectLogPage及hot/archive UNION，拒匿名/受限、ALL保tenant且不加CREATE_ID。错误日志selectOne/List/Page/generated mpCount/Count在通用权限关闭时仍拒未知和匿名；guard在一般exclude/Ignore配置判断之前，限相关表与明确读取statement，没有全局Ignore/exclude扩张。后台tryArchiveLock名称不匹配读取guard，archiveBefore是INSERT statement，匿名save/upsert未加查询guard。补跑既有BizLogArchiveJobTest验证无ThreadUser时锁竞争退出和正常两批归档到mockMapper，不把mock任务当真实归档SQL执行。

## 独立命令与证据

```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' :eva-core:eva-core-data-mybatis:test --rerun --tests org.pkaq.config.MybatisPlusDataPermissionHandlerTest --tests org.pkaq.config.LogQueryPermissionInterceptorTest --tests org.pkaq.core.mybatis.log.PostgresBusinessLogRepositoryTest --tests org.pkaq.core.mybatis.exception.PostgresErrorIncidentRepositoryTest --tests org.pkaq.core.mybatis.tenant.TargetTenantExecutorSecurityTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.role.service.RoleServiceTest --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest -x jar --offline --no-daemon --console=plain
```

exit0，BUILD SUCCESSFUL33s，17tasks/3executed14up-to-date，core-data/sys/core-log三个test均实际执行，compile复用未变产物。XML UTC2026-10-22T23:25:41.289Z至23:25:46.776Z（北京时间10-23 07:25），共74，全部failure/error/skipped0：Advice16+Sanitizer15+Registration1；LogQueryInterceptor2+DataPermission8+ErrorRepository6+BusinessRepository4+TargetTenant2=22；Role11+OperationType1+Module4+WriteProtection4=20。

为补后台归档关心点，追加同一core-data5类+BizLogArchiveJobTest，不扩大到其它模块：
```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' :eva-core:eva-core-data-mybatis:test --rerun --tests org.pkaq.config.MybatisPlusDataPermissionHandlerTest --tests org.pkaq.config.LogQueryPermissionInterceptorTest --tests org.pkaq.core.mybatis.log.PostgresBusinessLogRepositoryTest --tests org.pkaq.core.mybatis.exception.PostgresErrorIncidentRepositoryTest --tests org.pkaq.core.mybatis.tenant.TargetTenantExecutorSecurityTest --tests org.pkaq.core.mybatis.log.BizLogArchiveJobTest -x jar --offline --no-daemon --console=plain
```
exit0/BUILD SUCCESSFUL21s、7tasks1executed6up-to-date，core-data:test实际执行，24项全0。最新core-data XML时间23:27:26.837Z至23:27:28.770Z，覆盖前轮同模块XML；log/sys仍为23:25新XML，因此当前三目录合计76（原74+归档2），不是76个全部在单一命令同时执行。XML均各模块build/test-results/test/TEST-完整类名.xml，可按上列类名定位；真实命令输出与此卡独立保留结果，不冒用开发07:22报告。

git diff --check0/index空；既有LF/CRLF、JVM bootstrap sharing、Gradle弃用提示仍在。RoleService SHA256 C04071CEB1D600EF965DAA99C03A8862ED569C88113378B05159B2B153884BFE；LogQueryAccess 3B3CD7D5F8440A3ADC3F2601C766938507FAFCF2FCAA46D324A783DC35422536；DataPermissionHandler 4FCF79EC243A00E62073372BF2E2DC6DB530B405F59499F7839C1AD572CCE92F。未声称全量build，-x jar仅绕开用户运行JAR锁，模块当前classes测试不证明用户进程已加载新类。

未证实项：真实无宽授权受限账户的URL/日志total/detail拒绝、实际PG hot/archive SQL和跨tenant隔离、用户运行加载及真实Spring异步事务、日志日期上界、全应用序列化异常内容。前端P2补修与真实菜单现场/Edge扩展阻塞仍独立待办；不恢复真实日志详情、改密或权限写入。本角色只写此QA状态，无源码/测试/依赖/vault/DBHTTPRedis/服务/UI/Git提交推送或子代理操作。
