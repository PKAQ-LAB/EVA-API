# JDBC八实例证据源码实现交接

2026-10-28 backend_developer。依据总控正式批准的designer提案实现测试源码；仅apply_patch和轻量只读静态检查，未执行SQL/PG/Gradle/observer/Java编译，未改任何已审helper/init、业务生产源、配置、数据库、服务或浏览器，未提交推送。

## 写入范围与冻结SHA

| 测试源码 | SHA256 |
| --- | --- |
| org/pkaq/test/PostgresInstanceEvidence.java（新增） | B449DF73CEE7CDC118CE0ACF9BDF70A79A4F43DA49A1DF51A445606F1F3BF3D4 |
| org/pkaq/sys/module/AuthorizedMenuPostgresTest.java | FF820726E8771229A8245196C6174FAE052073CAE9CA418979C84D8832A6C9D5 |
| org/pkaq/core/mybatis/tenant/TenantSchemaModePostgresTest.java | 17C37FD4442FB35D5945AD5CAA077A7172F1448DBD7D35E71ADB57B9C16A1756 |
| org/pkaq/core/mybatis/tenant/TenantSchemaAdvisorOrderPostgresTest.java | F04AA5721AEEABE2C0E8085BF68E161A18051F80924382757A7B858448BE5D1A |
| org/pkaq/sys/role/service/RoleSchemaIsolationPostgresTest.java | 3E536DE69E3D2937F70120792B500122C1F6D949C134E1796E7C8ABB0C4DC025 |

表中路径均相对web-booter/src/test/java。原菜单四方法分别将menu-scope/menu-state/menu-tenant/menu-identity传给Fixture；Fixture唯一start返回后的原try内，DataSource取得后立即capture，仍受catch(Exception|Error)关闭PG并保留原异常/suppressed的保护。Mode两处及Advisor、Role各一处均在原try-with内DataSource取得后、首次DDL前capture。其余业务断言、事务/上下文及关闭代码不变。

## helper契约

仅接已有DataSource和固定label，不创建PG、不读应用配置/环境/连接URL/凭据，不执行业务SQL。自有Connection/Statement/ResultSet全部try-with关闭，Connection只读，不改变业务事务或search_path。唯一SELECT如下，均pg_catalog限定：

```sql
SELECT pg_catalog.host(pg_catalog.inet_server_addr()), pg_catalog.inet_server_port(),
       pg_catalog.current_setting('listen_addresses'), pg_catalog.current_setting('data_directory'),
       pg_catalog.current_setting('server_version_num'), pg_catalog.pg_postmaster_start_time()
```

必须精确addr=127.0.0.1且listen_addresses=127.0.0.1、port整数1..65535、versionNum=140022（对应批准14.22），postmasterStart非空。data_directory存在并toRealPath，严格在已绑定RUN/pg/tmp真实路径下；System java.io.tmpdir还必须与该固定路径相等，不能仅靠可变tmpdir同名值。失败不发布记录，并由原fixture关闭范围处理。

JVM绑定属性：pgJdbc.version=eva-pg-jdbc-v1、pgJdbc.nonce=64位小写hex随机nonce、pgJdbc.run=本freshRUN canonical、pgJdbc.evidence=RUN/pg/jdbc-evidence canonical。缺任何属性直接失败，旧验证入口不再可用。

RUN必须UTC+8hex名称，已有pg/tmp和pg/jdbc-evidence目录。RUN/pg/jdbc-context.json必须在RUN内非符号链接，且其SHA在RUN/sealed-control-files.json的pg/jdbc-context.json项中。上下文只能有version/nonce/runRoot/evidenceDirectory/expectedLabels五键，各值与JVM和固定八标签映射一致；新验证init仍须在doFirst从control seal核该上下文及精确传入属性。本源码不能单独证明RUN确由fresh runner创建，fresh创建及seal锚点仍由待审工具负责。

八标签→testClass：menu四标签→AuthorizedMenuPostgresTest；mode-provision/mode-route→TenantSchemaModePostgresTest；advisor-order→TenantSchemaAdvisorOrderPostgresTest；role-isolation→RoleSchemaIsolationPostgresTest，均使用源码中的完整包名。

每条JSON严格十键：version、nonce、testClass、label、dataDirectory、serverAddress、serverPort、listenAddresses、serverVersionNum、postmasterStart。端口与版本为JSON整数，其余为字符串；postmasterStart是UTC Instant规范字符串。字段来自真实SELECT并通过断言、连接关闭之后才序列化。使用已有Jackson，不增加依赖，拒重复键与尾随JSON。

发布采用同evidence目录随机.pending文件CREATE_NEW完整写入并关闭，再Files.createLink(label.json,pending)原子创建新名称，最后删.pending；与ATOMIC_MOVE不同，硬链接创建不会替换已存在目标。不支持硬链接直接失败，无覆盖式fallback。失败清理临时文件并将清理异常suppressed至原异常；已发布后清理失败仍使测试失败。最终消费者必须拒.pending/额外文件，不能只忽略它们。

每次采证前验证既有条目的固定schema/nonce/version/class/标签文件名、loopback/版本/端口类型、规范RUN data路径及Instant；拒未知文件、重复标签/数据目录/启动时间。最终消费者仍必须在全部fresh XML成功基础上核恰八标签/八canonical dataDir/八start，不能只靠helper局部检查、计数器、mtime或日志。先前实例close可能删除dataDir，消费者不得要求所有历史目录在最终仍存在；应比对采证时canonical字符串及RUN路径边界，并以Windows路径语义去重。

## 轻量检查及限制

静态确认八个固定且唯一标签、五个capture源码点、首次DDL前位置、原资源关闭边界、SELECT仅六个技术字段、CREATE_NEW发布无REPLACE_EXISTING。新增helper UTF-8/LF、无Tab、新增行不超过120字符；现有Mode长行及Advisor/Role原换行风格未作无关格式迁移。git diff --check无内容错误，只有原LF/CRLF转换提示。

未做Java编译、Java序列化/路径负例运行、JDBC查询或真实证据发布，不将Python文本检查称为Java测试通过。后续必须独立审核硬链接发布与context/seal契约、缺/旧nonce/重复实例/额外文件/类型错/路径越界/失败XML等负例，再授工具或实际执行。当前所有旧RUN保持原结论。

## 新验证方案PLAN（未改工具、未执行）

当前源输入对9e只读差异已实际确认：685→686，新增唯一PostgresInstanceEvidence.java，既有变化为四个PG测试源码及已批准observer7392，无missing及其它变化。不得再声称onlyobserver。

1. 另建新版本工具与init、独立SHA审计；完整复核9e main529/generated/header/snapshot和external214。生产源码/配置/build锁完全不变；新686源锁显式枚举四test SHA及新增helper，禁止任意测试白名单。原7392端口coverage行为不能直接运行本新口径，须另授新版本精确替换coverage为JDBC证据，保明确违规/协议错/终态ambiguity/no kill及已证-D交叉。
2. freshRUN复用已锁main字节到新products/snapshot并封存；单独仅web-booter测试编译闭包，编译source精确五文件，不跑main producer、其它Test或其它模块编译。JavaCompile必须用新的隔离destination/generated/header/tmp、JDK及已核external/annotationProcessor路径，CP仅新RUN sealed main及已核外部依赖；不引用旧RUN/live测试输出。该阶段需另审实际task图与精确源集，不以普通compileTestJava隐含编译全项目。
3. 精确编译输出预计十个class：新增org/pkaq/test/PostgresInstanceEvidence.class；允许重编的旧九class为AuthorizedMenuPostgresTest.class及$Fixture/$SqlCapture、TenantSchemaModePostgresTest.class、TenantSchemaAdvisorOrderPostgresTest.class及$ProbeResult/$ProbeService/$ProxyTestConfiguration、RoleSchemaIsolationPostgresTest.class。内部类可能仅行号调试信息变化，不能提前承诺哈希；禁止增加未知class或宽放包目录。无helper匿名内部类/record生成闭包。
4. 单独新compile输出检验后，组合旧prepared中未受影响的36项原字节与新十class，得到新prepared46项（原45减旧九加新十）；不覆盖旧RUN，不将旧45当新源码产物。两项测试resources只从锁定参考复制，所有其它未受影响字节必须一致。实际输出键集合/全部SHA/generated/header/编译CP需记录，再独立重封。
5. 新Test阶段仍唯一web-booter:test、四精确filter、零producer；新CP只新snapshot main+新prepared46+外部锁子集。fresh nonce/context先封存、evidence目录独占空、init doFirst复核，再运行四类。最终四类fresh实际case/XML+八JDBC标签/实例证据+全部源产物/控制锁/终态门禁联合判定；任何测试失败即使八文件齐全也失败。
6. 结果分列：旧9e26类219业务通过但观察失败；4b四类11业务通过但旧coverage失败；未来新版本四类/JDBC八实例/终态结果待执行。当前无新RUN或新结果。
