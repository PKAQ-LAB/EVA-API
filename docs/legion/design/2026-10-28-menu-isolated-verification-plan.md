# 菜单边界：26类隔离验证准备计划（未执行）

2026-10-28 backend_developer。仅测试窄补及只读依赖/API/缓存检查，没有新增执行helper、运行Gradle、启动PG或应用、解压到磁盘、访问实际DB/HTTP/Redis/IDE/服务。

## 测试窄补及口径

仅修改新AuthorizedMenuPostgresTest.java。仍四个测试方法，补充schema101已有合法非空TenantContext/search_path下正常菜单调用恢复；对真实Executor.query执行ID为AuthorizedMenuMapper.selectMenuRows的StatementHandler.prepare阶段注入合成异常，先断言此时已位于core public，再在未执行失败SQL的情况下抛异常，核core及外层finally恢复原非空context/search_path，避免PG事务abort掩盖恢复行为。

SqlCapture用MyBatis现有Executor.query四参数与StatementHandler.prepare签名，沿执行栈保存恢复statementId，在prepare记录其ID及实际SQL。断言仅对该专用ID注入，不用配置存在性冒充执行ID。另补有效ThreadUser但无事务调用必须拒绝。测试仍未执行；直接构造service+TransactionTemplate不证明AuthCtrl实际Spring代理。部门范围仍deptId0、自定义orgIds空，只验证业务拒绝状态下专用元数据，不声称实际匹配部门业务集合通过。

测试新SHA256：4B449EA4F85D76F0CA11C7A429C03AF32FA1109CE0F39A1C4A919390FA79FFFE。五生产文件SHA与designer审查表完全相同，未改生产。

## 项目闭包与任务边界

从当前build.gradle中只提取project依赖声明，不执行Gradle。web-booter的main/test依赖闭包为11个产品项目：

- :eva-core:eva-core-contract
- :eva-core:eva-core-common
- :eva-core:eva-core-log
- :eva-core:eva-core-cache
- :eva-core:eva-core-data-mybatis
- :eva-core:eva-core-upload
- :eva-web:eva-web-core
- :eva-web:eva-web-auth
- :eva-web:eva-web-sys:eva-web-sys-domain
- :eva-web:eva-web-sys:eva-web-sys-service
- :web-booter

其中web-booter testImplementation直接依赖core-cache/core-log及flywaypg，embedded-postgres:2.2.2及Windows binary沿既有依赖，不新加依赖。license/websocket/license-server不属此闭包，不能为方便加入；所有19 Project（含隐式父项目）仍须在配置最早阶段统一RUN buildDirectory并锁定，防未选项目输出外逸。

三阶段候选：main只允许11项目compileJava/processResources/classes（声明任务集合33，未计算真实Gradle图）；prepare允许相同main集合及SYS/AUTH/DATA/web-booter各compileTestJava/processTestResources/testClasses；test精确四Test任务，禁止所有producer/jar/bootJar/clean/其它Test。prepare最终实际图不能由旧25推算，未来新helper需对允许集合/必要任务/未知节点严格核验。当前没有运行Gradle取得图，不声称实际图计数已证。

CLI保留offline、no-daemon、no-parallel、max-workers=1、no-build-cache、no-configuration-cache、独立project-cache及精确阶段；各Test须同步CLI --tests与filter精确集合。第三阶段只消费fresh sealed main与本模块prepared tests及已锁外部jar。不得修改原C或20类helper/RUN/seal，未来新增执行辅助另授。

## 固定26类selector

SYS13：

```text
org.pkaq.sys.role.service.AssignedRoleDetailTest
org.pkaq.sys.user.service.UserAccountProfileServiceTest
org.pkaq.sys.role.service.RoleCustomScopeStandaloneTest
org.pkaq.sys.role.service.RoleServiceTest
org.pkaq.sys.RoleFrozenMappingTest
org.pkaq.sys.AccountTenantFrozenMappingTest
org.pkaq.sys.TreeFrozenMappingTest
org.pkaq.sys.TreeFrozenEditTest
org.pkaq.sys.WriteProtectionServiceTest
org.pkaq.sys.role.service.UserMapperBootstrapTest
org.pkaq.sys.role.service.UserMapperConsumerTest
org.pkaq.sys.module.service.ModuleServiceTest
org.pkaq.sys.module.mapper.ModuleListMappingTest
```

AUTH6：

```text
org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAuditCoreRoutingTest
org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAuditMapperSqlTest
org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAuditReadAccessTest
org.pkaq.core.auth.authorization.service.ResourceAuthorizationServiceTest
org.pkaq.core.auth.authentication.filter.JwtAuthFilterTest
org.pkaq.core.auth.config.WebSecurityCapabilitiesTest
```

DATA3：

```text
org.pkaq.config.OpenApiAuditQueryPermissionTest
org.pkaq.config.MybatisPlusDataPermissionHandlerTest
org.pkaq.config.LogQueryPermissionInterceptorTest
```

web-booter4：

```text
org.pkaq.sys.module.AuthorizedMenuPostgresTest
org.pkaq.core.mybatis.tenant.TenantSchemaModePostgresTest
org.pkaq.core.mybatis.tenant.TenantSchemaAdvisorOrderPostgresTest
org.pkaq.sys.role.service.RoleSchemaIsolationPostgresTest
```

后三既有类只读检查无application配置读取、System.getenv、DriverManager、SpringBootTest。TenantSchemaMode两处EmbeddedPostgres.start，含合成fixture数据库内执行指定migration资源，不启动WebBooter；Advisor仅注册嵌套测试配置，RoleSchema仅合成表。仍须完整源锁及测试任务白名单，不启全仓或其它migration测试。逐类fresh XML必须存在且测试数正，26类准确集合、零fail/error/skip，并与executed suites一致，不能用历史203填新总数。

## main精确差异与封存

以原固定10模块C绿色snapshot494为只读参考，允许变化仅：SYS-service/classes/org/pkaq/sys/user/ctrl/AuthCtrl.class；允许增加仅该模块classes/org/pkaq/sys/module/service/AuthorizedMenuService.class、mapper/AuthorizedMenuMapper.class、mapper/AuthorizedMenuRow.class及resources/mapper/AuthorizedMenu.xml。原mapper/User.xml必须继续C SHA5ED33960854D9203F6AE3D9D1F0F5D8D4F2F822409C49D6169B097C93A89B9E9；其它原494键完整集合/hash不变。预期原十模块498文件仅为候选，编译若产生任何未列新inner/generated产物须停审，不能通配放行。

web-booter首次纳入：当前main仅WebBooter、config/APIConfiguration、config/Swagger3Configuration、app/doc/IndexCtrl四个Java顶级类，未发现匿名/嵌套声明；resource源文件27个。未来PLAN在不输出内容前提下逐路径记录这27源resource SHA，processResources输出须同路径同字节；四类精确路径允许首次生成但必须锁源与编译输入，不能给整个web-booter目录任意通行。若实际annotation processor生成额外metadata或class，停报审新精确键，不自动扩大白名单。main累计候选529文件=原498+4class+27resource，不作为已执行计数。

所有11模块fresh classes/resources/generated/header、测试输出、快照与外部jar跨阶段累计hash/presence锁沿既有门禁，另包含web-booter。测试compile所有源的依赖需求须真实model/图确认，缺包不能临时加入无来源模块或回落旧build。配置资源只作opaque输入/输出hash闭包，不初始化应用、不输出内容或读取秘密值。

## 已安装EmbeddedPG实际API与离线缓存

既有Gradle缓存根为C:/home/DevApp/cache/gradle/caches/modules-2/files-2.1。embedded-postgres/2.2.2实际jar SHA256 9b1722ccfbfb441b7b1440edd35c1a229b173d82ab25d6111dd8642b48ba66fa；sources SHA333939edcfb5941c7bc929abcce9c3f068736f501f47b84fc6df9c96df2dd1b6。用本机javap只列API，确认Builder具有setDataDirectory、setOverrideWorkingDirectory、setServerConfig、setPort、setPgBinaryResolver等方法，未运行库。

已存在io.zonky.test.postgres/embedded-postgres-binaries-windows-amd64/14.22.0/e6a6523c59a54855f9f373cfb28173e0e022fd0d/embedded-postgres-binaries-windows-amd64-14.22.0.jar，SHA256 badc584f9598557e91f4c95d811c277207cd9dac94c7cc5da4ed70dd4e64b307。其唯一postgres-windows-x86_64.txz成员SHA256 ea1da60233129d7fee6e5f5b6c0e8d6df9d96b8e1bcf7b1d669fcffb6ebf18e5，库按MD5生成目录PG-3942d5a1dcdbfebc27772e9e670d5d85。只在内存读取归档，未落盘解压：

| 成员 | SHA256 | PE Machine |
| --- | --- | --- |
| bin/initdb.exe | fb482dda195ecaad17227924d15aa489eaa6be413a5a92dce8699c9a5772582d | 0x8664 AMD64 |
| bin/pg_ctl.exe | bb7fff2d3c8b7b4b32fe6b339959267a749361fe0dd72743f8bce7c786d0a29f | 0x8664 AMD64 |
| bin/postgres.exe | 6e5f90d94d50cb44467b6d30d14d10d1dcceeccc7834941e450393b2be7d4aa3 | 0x8664 AMD64 |

DefaultPostgresBinaryResolver按ClassLoader查唯一classpath资源postgres-windows-x86_64.txz，重复报错、缺失返回失败；未来actual CP预检必须唯一且来源为已锁本地jar，不允许HTTP URL/新解析下载。Gradle --offline并非单独足够，需对库实际资源来源核准。缺jar/归档/架构或SHA漂移立即停止，不联网下载/安装。

## 四PG类全局目录、进程与待授权最小差异

实际2.2.2源码EmbeddedPostgres:486 getWorkingDirectory读取系统属性ot.epg.working-dir，否则java.io.tmpdir/embedded-pg；:831解压在该workingDirectory/PG-md5。Builder.parentDirectory也取此目录，cleanOldDataDirectories会扫描它。Builder.start:610未指定dataDirectory则Files.createTempDirectory(epg)，使用JVM java.io.tmpdir。Process命令:924从pgDir/bin解析initdb/pg_ctl/postgres；process working directory继承测试JVM，而非builder API指定。

因此未来web-booter整个Test JVM启动前设置java.io.tmpdir=RUN/pg/tmp、ot.epg.working-dir=RUN/pg/unpack、Test workingDir=RUN/test-work/web-booter，并仅在该Test子进程设置TEMP/TMP=RUN/pg/os-tmp；报告、products、project-cache同样在RUN。这可同时约束四类全部start（含旧TenantSchemaMode两处），不必为目录修改既有测试builder。候选实际exe将位于RUN/pg/unpack/PG-3942d5a1dcdbfebc27772e9e670d5d85/bin，数据目录RUN/pg/tmp/epg*；运行后仍需实际子进程/目录/端口证据，不把本静态路径推导说成已观察。

尚需显式收口loopback：Builder默认port0，经detectPort ServerSocket(0)选动态端口；JDBC默认localhost，但源码默认serverConfig并未显式写listen_addresses。要强制监听loopback，最小候选为四类全部五处start调用（新menu1、TenantSchemaMode2、Advisor1、RoleSchema1）改为EmbeddedPostgres.builder().setServerConfig(listen_addresses,127.0.0.1).start()（实际Java字符串需带引号）；无需固定端口、迁移或改生产。该差异目前未执行，既有三测试不在当前写锁内，须另授后才改；不能据默认localhost或java.io.tmpdir证明显式监听安全。

未来执行辅助只记录由本Test JVM派生、exe规范路径和PG数据-D均在本RUN且SHA匹配的ownedchild。正常每个fixture try-with-resources关闭，库close会pg_ctl stop并清理自己的data目录；异常/中断需在有完整本RUN路径+进程归属证据后仅清该ownedchild，不能按postgres名称全杀或碰既有9016/开发PG。库close吞stop异常并不证明进程已退出，必须独立确认。未启动子进程，本轮不存在待清理实例。

## 当前门禁结论

测试窄补待designer审；新执行脚本及Gradle无授权，尚未创建或执行。明确loopback builder候选与未来main产物严格白名单后，再由总控审新隔离辅助。五生产、旧C/helpers/RUN、配置和用户修改保持冻结，未提交推送。

## 后续获授五处loopback窄改（替代上文待授权状态）

总控随后明确授权仅四个测试类五处EmbeddedPostgres.start()替换为builder().setServerConfig("listen_addresses", "127.0.0.1").start()，已用apply_patch完成，未设置固定端口、目录或改变其它测试行为。未运行PG或Gradle，IPv4监听与JDBC localhost连接是否实际可用仍待执行验证。

| 文件 | 新SHA256 | 精确逆改所得原SHA256 | 调用数 |
| --- | --- | --- | --- |
| AuthorizedMenuPostgresTest.java | 8190BB1ACE982FDE3B622AF808FA654993DC99A1A676294995E599B4AE605715 | 4B449EA4F85D76F0CA11C7A429C03AF32FA1109CE0F39A1C4A919390FA79FFFE | 1 |
| TenantSchemaModePostgresTest.java | 8F3DB6BFFC3F9630B8C84DC39C2C5A34EA9312532FDB5A7F3168916D918CB963 | D84569F9DF923605FE363462E1A782DC7733DCB8BFB399D70D8E0D9A1CF64A00 | 2 |
| TenantSchemaAdvisorOrderPostgresTest.java | 695418BE9775CDF36B06E7D15A70CFDBEDC0529A899C99CC09E6469347017532 | 17ED996E4BBE1ACA50633E28CF1392EBA019F3E49B015FE99E45DB5BE6A378F6 | 1 |
| RoleSchemaIsolationPostgresTest.java | E8F612D91A2D8D347FFDA96487CEC886A4305FBAE9F3BAA1A5531BD008AE9DAC | 9D65D9CF9C7A9E00D7BF1E05CF09BA7B25DB0F02B03A8AFA1F6D6538D2061EC0 | 1 |

四项inverseEqualsOriginalSHA全部true，原CRLF/LF各自保留。三旧文件scoped diff --check通过，旧TenantSchemaMode原三条超120行未改，本次新增行均不超120。五生产文件重算仍与独立审查表一致；其它测试、旧helper/RUN及配置未改。新helper仍未授权创建或执行，当前交designer独审loopback及本隔离计划。
