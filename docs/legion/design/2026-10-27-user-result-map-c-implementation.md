# User结果映射C：源文件准备与待验证清单

2026-10-27，backend_developer。当前仅准备文件并静态检查，没有运行Java编译、Gradle、Spring容器或测试，不代表启动故障已修复。

## 生产边界与已做静态检查

唯一生产差异为 `eva-web/eva-web-sys/eva-web-sys-service/src/main/resources/mapper/User.xml` 的 `BaseResultMap`：取消跨资源父映射，合并原13个公共属性与3个档案属性，id只保留一次，共16个唯一属性。SQL、本地子映射、Mapper/Service/转换/配置等生产文件不改。

使用Python标准库XML结构检查和原始字节比较，确认16项与当前common13项及code/name/deptId等价；替换整个BaseResultMap后，文件其余字节与HEAD完全相同。旧反例 `src/test/resources/fixtures/user-before-c.xml` 与HEAD原User.xml全字节相同，保留extends及本地4项，不在正常mapper目录内。生产XML SHA256为 `5ED33960854D9203F6AE3D9D1F0F5D8D4F2F822409C49D6169B097C93A89B9E9`；旧反例为 `BFC0F6B427D65F8E1F62D3D5364FECDD2F181DBC05C3AC40729BD0144A5F24EF`。原AssignedRoleDetailTest.java与HEAD全字节相同。

## 新文件与隔离设计

以下均位于sys-service的 `src/test/java/org/pkaq/sys/role/service/`：

- `UserMapperBootstrapFixture.java`：最小AnnotationConfigApplicationContext，仅实际MybatisPlusAutoConfiguration、MybatisPlusConfig、合成EvaConfig和唯一Mockito DataSource。移除系统环境/属性PropertySource，不导入ConfigData、WebBooter、组件扫描或整应用自动配置。注册与WebBooter相同org.pkaq根包元数据，由实际MP注册器只扫描@Mapper接口，避免手工addMapper掩盖注册时机。核DS、插件、provider及customizer/handler/injector数量。普通场景保留实际glob及resolver原顺序，无factorycustomizer；User先/common先/旧User先分别独立容器和factory。仅测试关闭SQL日志，其他相关公开MyBatis属性沿项目配置；这不是整个Web应用启动验证。
- `UserMapperBootstrapTest.java`：拟核旧反例generated无JOIN及档案缺失；C三顺序均XML JOIN；13父项/16显式项/9自动账号项及真实handler；Frozen0/1/9999/NULL/非法值；长ID、版本、审计、日期和备注；无profile与NULL deleted；删除/权限隐藏拒绝；详情原角色ID、raw摘要和岗位同事务、六表来源拒绝。每例关闭容器并核线程/事务清理，Isolated禁止与其它测试并行。
- `UserMapperConsumerTest.java`：先从C真实factory映射合成实体，再用严格UserMapper写mock调用实际Service逻辑；测saveUser、保留编码前置校验、角色与岗位守卫在写前拒绝code9999/READ_ONLY；关联/档案/permVer无写。repwd只测原密码校验、原哈希更新及事件，保留受保护账号原可改密行为，不加新只读政策。改密仅mock写入，不接JDBC。
- 扩展 `AssignedRoleDetailJdbcFixture.java`：旧两个构造入口仍走原手工common-first语义路径；新增显式order入口走真实bootstrap。新入口只接mock连接，metadata仅PostgreSQL产品名，未知连接/元数据调用拒绝；写SQL、批处理、多语句、未知合成表/谓词拒绝。只有实际JOIN才供档案列，防止给generated SELECT伪造部门。扩展合成行与类型getter；保原六表核源、事务连接校验、raw角色及岗位读取路径。旧42项尚未重跑，不能称已通过。

## 待总控授权的验证方式

先由designer/QA只读核真实文件隔离及重大问题，再申请唯一编译测试槽。本轮不创建或运行新辅助脚本，不覆盖旧snapshot-d及旧helper。

拟按三个独立串行阶段执行，均离线、禁并行且实际等待退出：显式十模块main classes/resources；sys-service compileTestJava/processTestResources；仅Test任务运行。复用旧辅助只能作为只读设计依据，需另建C-20261027专属init/清单/日志/产物目录，由总控批准后落实，禁止直接运行旧snapshot-d哈希门禁来声称C通过。

新增精确筛选类为 `org.pkaq.sys.role.service.UserMapperBootstrapTest`、`org.pkaq.sys.role.service.UserMapperConsumerTest`，并保留 `org.pkaq.sys.role.service.AssignedRoleDetailTest` 及原sys/auth/data全部既定回归筛选。计划Test入口形态为 `gradlew.bat :eva-web:eva-web-sys:eva-web-sys-service:test --tests org.pkaq.sys.role.service.UserMapperBootstrapTest --tests org.pkaq.sys.role.service.UserMapperConsumerTest --tests org.pkaq.sys.role.service.AssignedRoleDetailTest --offline --no-daemon --no-parallel --max-workers=2`；此为待补C专属classpath/任务图门禁后才可执行的形态，尚非已执行命令，不得直接照此触发隐式生产任务或jar依赖。

新产物必须记录HEAD/生产XML及测试源/构建配置哈希、实际classpath完整来源与模块输出清单、编译时间和依赖来源，显式替换旧User.xml资源并核其与源同字节；测试前后核新清单一致。其它生产字节预期与原基线一致，但出现差异须停报而非自动接受。测试计数来自当轮实际suite/XML，旧177不计入C结果。

之后仍需QA独立重验、用户正常重启、实际factory statement来源/BoundSql和合法UI只读验收；本轮无IDE/UI/服务、实际DB/Redis/HTTP、授权或账号数据修改，无提交推送。

## QA预审mustfix静态修订（尚未执行）

QA指出新bootstrap分支PreparedStatement/Statement/ResultSetMetaData/ResultSet默认RETURNS_DEFAULTS及任意set方法放行不满足失败闭合。现仅新bootstrap路径使用精确必要setter和getter/生命周期方法，其余由unknownJdbc直接AssertionError；旧手工入口保原默认应答和参数绑定语义。PreparedStatement只接受两参数标量setLong/setInt/setString/setObject/setNull；禁止流/LOB/未知setter和setObject三参数重载；未知ResultSet getter重载、列、metadata方法拒绝。新增负向用例逐项断言未知DS凭据连接、CallableStatement、metadata URL、statement方法、setter、ResultSet方法与更新、全部prepared/plain写和batch入口失败。只有此合成负向用例预期写拒绝计数大于零，正向只读用例仍要求零；尚未运行证明。

容器增加BeanFactoryPostProcessor，在单例初始化前检查BeanDefinition：仅固定MyBatis/MapperFactory、属性绑定、事务/AOP及必要注解处理基础设施类允许，工厂方法也限定名称和已许可配置拥有者。不以org.springframework前缀整包放行，也不允许Redis/Flyway/Hikari/调度或应用启动Bean。白名单若在实际安装版本下缺必要基础设施，必须按真实失败停报、核来源后最小调整，不能未知即默认返回。

本批明确新增覆盖：真实C映射普通账号分别有/无profile，再进入实际saveUser与角色/岗位服务；写入端仍是mock，断言档案更新/补建、原关联删除及三次permVer，禁止真实写。新增缺tenant_id结果列时属性保持NULL、selectManagedList/selectManagedPage/BaseResultMap与UserWithModuleAndRole及roles/modules嵌套statement引用检查，以及真实详情VO序列化无password字段/合成哈希。list/page/嵌套查询目前只检查statement注册/SQL与引用，未执行完整分页或嵌套查询，也不宣称HTTP覆盖。

本批精确补证类还包括 `org.pkaq.sys.user.service.UserAccountProfileServiceTest` 的managementCreationUsesSharedAccountCreatorAndProfile、editingRegisteredAccountCreatesMissingProfile、editingProfileCanClearOptionalDepartment、accountUpdateConflictDoesNotWriteProfile、profileWriteFailureRejectsWholeOperation等原正负向用例；这些mock既有实体用例只补原服务语义，不替新增实际C映射后正向。原RoleServiceTest属于角色本体/权限门禁，不冒称已有独立UserRole/UserPost普通写链覆盖；这两条由新增Consumer正向承担。完整当轮最终类数/用例数必须等实际执行后再报。

## 受控验证实际阻塞与设置恢复

全部run位于 `.gradle/C-20261027-runs/`，旧d及旧辅助未改。HEAD为1f708e202af0ba817b55c0f6be9a9085eea054ea。本轮没有测试编译或Test执行成功证据，未封C快照，不能声称旧反例已重现或新映射已通过。

| run | 实际结果 |
| --- | --- |
| 20261027T140944Z-4623a3be | 配置阶段失败：Gradle9.5.1拒绝projectsEvaluated中提前解析testRuntimeClasspath，无编译/Test。后以两行FileCollection惰性filter窄修并独立静态审。 |
| 20261027T141111Z-b4ab8577 | 调度消息交叉启动后立即中断，exit1；Task标记0，无阶段图/快照，不作成功。所属进程退出，无服务操作。 |
| 20261027T141246Z-0156c79a | main运行1m21s，upload编译14项common util缺包/符号；末态upload common拷贝目录缺，其他消费者拷贝有81类。此时未记录现场CP，不能由末态推定原因。 |
| 20261027T142000Z-5b7a1f21 | 首个contract任务输出快照读取失败，core/codes路径NoSuchFileException；结束后目录存在且全494文件与旧d相同，未进入copy诊断。 |
| 20261027T143903Z-df529796 | 临时workspace autobuild=false后的单次验证，main1m17s，auth编译前copy门禁失败；详下。 |

df529796的缓存、日志、data、web-core各次common拷贝均sourceBefore/sourceAfter/copy=81，SHA相等。到auth的 `main-eva-web-auth-compileJava-eva-core-common-copy.json` 则sourceReadable=true、sourceBeforeCount=0、sourceAfterCount=0、copyCount=0、didWork=false；门禁立即失败，auth编译器/SYS编译与资源/Test未执行。由此可判该现场源目录为空，不是已经证明Project.copy漏复制；未证明具体写入PID，也不能据单项workspace设置认定所有IDE构建已静止。没有改Files.copy、生产依赖、tracking、cache或接受新baseline。

只读发现本仓IDE项目metadata把Java及资源输出指向同一标准build目录，存在Java/Gradle语言服务派生daemon；进程存在及20秒静止不是写者因果证据。所有新run前后均未操作后台Stop/Restart/HotSwap按钮；IDE原有Applying code changes及Hot code replace失败提示未处置。

操作前用户已新增 `java.debug.settings.suspendAllThreads:true`，故最新原settings SHA为059FF8E0D20427CF6AED4E40C0A525942031D5E96E9D9E5B4A427BB8C290DDAE，而不是历史FB6E69。仅apply_patch增加java.autobuild.enabled:false，单键差异已核；文件false已证，IDE运行时生效/所有构建idle未证，按总控明确授权仅运行一次。失败后立即apply_patch仅移该键，恢复文件SHA精确等于059FF8，用户新设置保留；没有用旧backup覆盖。后台9016仍PID18544监听。

本轮结束当前十模块main494与旧d added/missing/changed均空，User.xml输出仍为旧BFC0，源C5ED未由该轮处理到，源码唯一生产差异仍为map。源码/tests/辅助冻结，释放验证槽。后续若需受控排除共享输出写入或隔离构建，须总控另定范围，不继续盲目重跑；独立QA、实际加载及UI验收尚未完成。

## 独立输出验证辅助已准备，未执行Gradle

按已批准的 `2026-10-27-c-isolated-build-output-proposal.md`，仅新建ignored `.gradle/C-isolated-20261027.py` 与 `.gradle/C-isolated-20261027.gradle`。前者SHA256为8596837C64CE8C1EFC5D3EFC375171D0F256BD07766F807EC08B3492645FBB71，后者为B905456A6D564BFF8C9F37F520EEC7B08155DC684D9F5747E5661E3F040C32A7。旧C06A/A053辅助、全部旧辅助和d manifest哈希复核均未变。生产XML/tests、IDE、配置、服务无本轮改动。

新init在projectsLoaded中、根及子项目配置前，对所有Project含root/隐式父固定RUN/products/完整唯一project path/build，并禁止随后改buildDirectory；独立project-cache-dir固定同RUN。只执行原十模块main、SYS测试准备、精确两个新测试类三个串行阶段。命令保offline/no-daemon/no-parallel/no-configure-on-demand/max-workers1/-xjar，并按方案关闭该次build/configuration cache，不关闭state tracking。没有clean、复制回live、变更依赖或SourceSet源目录。

路径模型实际取SourceSet和任务提供者：main/test classes/resources、JavaCompile dest/generated/header/tmp、ProcessResources dest/tmp、所有Project Test类/工作目录/二进制与XML/HTML报告、archive与outgoing variant。所有构建输出必须在RUN，graph任务输出逐一核；普通源码根仅允许该项目src或RUN内生成源，标准build/旧run不能作生成源。Gradle sourceElements若暴露精确声明的源码根，单独标记declared-source-input；它是只读输入，不伪称RUN产物，也不准作为compiled classpath目录。未知源码根/输出/variant直接失败。root problems报告先记录预期RUN根，实际日志中的报告位置也会核是否归RUN；未执行前不宣称插件已遵循布局。

编译暂存保持原模块/顺序与Project.copy实现，复制源改从真实隔离SourceSet取得，前后与副本全量hash一致才编译；实际compile/processor classpath在任务自身执行时解析并锁外部jar。只替换固定模块identity的internal jar，未知org.pkaq缓存artifact拒绝而不粗删；工作区标准build、旧run、未许可目录均拒绝。Test只含封存十模块main、实际SYS test类/fixtures和外部jar；不排序mapper资源，保原AUTO/NORMAL和强制正反序测试不变。

Python仅只读复用冻结旧helper的code_inputs输入闭包，AST检查确认不调用旧main_outputs/test_outputs/sealed_main。实际产物均从各阶段path-model读取；新main与旧d比较只允许User.xml为C5ED；封存classes/resources/manifest及main generated/header，再锁SYS测试产物。Test-only图仍精确一个SYS Test任务，不含生产者。执行前后源/配置/测试/辅助、main/test/snapshot、外部jar均核；失败也保主/测试/快照门禁及实际退出码，结果只认当轮suite与两个XML类、fail/error/skip计数，不据COMPLETED字符串或旧177认绿。

静态验证已完成Python ast.parse、输入函数限定检查、默认PLAN真实运行及其候选run目录不存在检查、flags/两类筛选/三阶段检查、ignored路径与旧冻结哈希复核。Groovy语义、所有插件实际路径、variant/model gate及两新测试仍未经执行，可能揭示需停报的API/路径问题，不能称隔离已生效。候选正式入口仅 `C:/home/DevApp/python3/python.exe .gradle/C-isolated-20261027.py --execute`，须独立静态审及总控另授唯一槽后执行；默认无参数只输出PLAN，无目录或Gradle副作用。当前不占前端QA验证槽，不轮询等待。

### 独立审窄修及隔离首轮实际结果

独立审后仅补compileTestJava自身main.resources精确RUN目录白名单，init最终A0BD3342E2A553CBC96B585103C43138BB6A8457754918402D96DFF71EABC63D；跨阶段外部jar累计锁补入Python，最终3C944ECBA933068FF58E0513C894D40711D43BB5D741E2252621306E0A44561E。累计锁在下一阶段前复核旧path/sha、阶段结束新增依赖只登记首次sha且重复path不准重建baseline、最终再复核；轻量合成文件负测验证首次/重复同sha、新test依赖通过、阶段间变化/重基线拒绝，临时文件已清理，未改缓存真实jar。两项以逆向差异恢复原文件hash证明范围。

经designer终审和总控单次授权，实际隔离run为 `.gradle/C-isolated-runs/20261027T151302Z-69bf3297`。main配置22秒失败exit1：init220-221的mains collectMany多行表达式在行首使用加号，Groovy报java.lang.String.positive()无此方法。未产生taskgraph/path-model，未进入compile/resources/test准备/Test；不能把它算作生产代码失败或C用例通过。未自动修改或重跑，原command/log/gate及累计外部记录保留。Gradle实际Problems报告位于RUN/products/_root/build/reports/problems，只有该已发生路径的隔离得到运行证明，其它路径模型仍未执行。

结束只读检查：IDE设置仍为恢复后的059FF8，后台9016仍PID18544监听，无本轮client残留。live main仍494且与旧d相比无增缺，唯一变化是SYS User.xml资源已为源C5ED（mtime22:43:15）；本run未执行任务且未回拷live，不能归因本run，也不能据此称live SqlSessionFactory已重新加载。隔离辅助不读取该live资源作新验证baseline。其它493字节仍与旧d相同，旧辅助/旧d冻结。源码/tests/helper冻结，已交回验证槽，后续仅可按新的窄授权修辅助再审，不盲重跑。

### 隔离第二轮：main与测试编译通过，25项被夹具白名单阻止

独立复核仅移动mains表达式二元加号后，init SHA为E1BA9AC37BBDDB2D718C9C35CF778903FECBC927CD55B181333A16F702434EF5，Python仍3C944ECB。新单次run为 `.gradle/C-isolated-runs/20261027T151732Z-c7099319`：main exit0、test-classes exit0，test exit1。三阶段实际图分别30、15、1任务，Test阶段唯一任务为SYS:test；未执行其它测试。模型记录19个Project、10个固定main模块，所有核验输出归本RUN。

新main494=467classes+27resources，与旧d比较仅User.xml为批准C5ED，snapshot manifest SHA为44D18D9417FB88E868530A9134881CABFF8481618D9B92A1850A0079984876BF。SYS真实编译测试产物36项已封存；外部依赖累计锁分别143、169、178个jar。结果判失败前，源/配置/辅助、主产物/generated/header、测试产物、快照与跨阶段jar哈希门禁均已完成，未以旧d产物冒充新编译。

当轮实际XML：UserMapperBootstrapTest13项、UserMapperConsumerTest12项，共25项，全部failure，error0、skip0；suite计数一致。25条失败消息完全相同：测试最小容器的BeanDefinition白名单仅接受MybatisPlusConfig原类名，Spring实际已将其增强为MybatisPlusConfig$$SpringCGLIB$$0，在UserMapperBootstrapFixture.assertBeanDefinitions139拒绝。所有用例尚未进入Mapper/consumer验证，不能称C业务通过或已证生产失败。此为fixture对白名单合法Spring增强形态处理不足，不能通过泛接CGLIB/放开包前缀规避；任何窄修仍待总控/独立审另授，本轮未改测试/辅助或自动重跑。

完整日志、路径模型、实际CP、每阶段累计jar清单、XML与gate均留该RUN。settings仍059FF8恢复态，后台9016/PID18544仍监听，无本轮client残留；无IDE/服务/缓存/业务源修改、无回拷live或发布。已主动报告并释放唯一槽；原回归、独立QA和live加载/UI仍未完成。

### d3f8469b：增强形态与JDBC负例通过，游标元数据漏项阻止其余24项

按后续精确授权仅修改BootstrapFixture/Test并经独立审：raw白名单保持，只有mybatisPlusConfig可依据已解析类、原类/直接父类身份、原注解元数据、非factory来源、Configuration代理及公开BeanFactoryAware关系还原；factory owner同校验且只接受其两方法。不引内部marker、Class.forName或getBean。新增1项内存正负定义测试。锁定Fixture ADDFD47C、BootstrapTest9DE3EFE6，Consumer/生产/辅助未变。

新单次隔离run `.gradle/C-isolated-runs/20261027T153549Z-d3f8469b`：main0、test-classes0、Test1，图仍30/15/1。真实26项中2pass、24failure、error0、skip0，suite/XML一致。两个实际通过项为onlyRegisteredConfigurationEnhancementAndItsTwoFactoryMethodsAreAllowed与syntheticJdbcRejectsUnknownCallsWritesBatchesAndOverloadedSetters。

其余24项底层同一strict JDBC漏项：ResultSet.getType被unknownJdbc拒绝。23项直接AssertionError，1项删除/隐藏用例因期待RuntimeException而包装为断言类型不符，其cause仍getType。MyBatis3.5.19实际源码DefaultResultSetHandler391-404显示skipRows无条件先读取getType，再判断forward-only和offset。因此为fixture只读游标元数据遗漏，尚不能判映射/消费者业务失败；没有弱化异常期望或自动放行默认。候选仅新bootstrap无参getType返回TYPE_FORWARD_ONLY并保旧手工入口原应答，当前未实施，需另授。

所有原输入/隔离main/test/snapshot/generated/header/累计外部hash门禁到结果判失败前均已核；未借旧产物或测试数。结束settings059FF8、9016/PID18544保持，无本轮client残留。失败证据完整保留，源码/tests/helper冻结并交槽，无自动修/重跑；C业务、原回归、独立QA及live/UI未完成。

### 7f8f0654：两新类26项实际全通过

MyBatis实际结果处理路径独立审后，仅补新bootstrap无参ResultSet.getType→TYPE_FORWARD_ONLY、DatabaseMetaData.supportsMultipleResultSets→false，并将manual Connection.getMetaData恢复HEAD默认null；原unknown/write/batch拒绝保留，确定值断言与manual原null已加入同一负例。锁定共用fixture3EBECA1D/Test22B14EFB，BootstrapFixture ADDFD47C/Consumer B9FD2D2A/生产/helper均未改。

新单次隔离run `.gradle/C-isolated-runs/20261027T155115Z-7f8f0654` 三阶段实际exit0：main2m、test-classes1m25s、Test1m31s。模型19Project/10固定module；实际任务图30→15→1，Test唯一SYS:test。main494=467classes+27resources，仅User C5ED差异；snapshot manifest SHA44D18D9417FB88E868530A9134881CABFF8481618D9B92A1850A0079984876BF。准备测试产物38项；累计外部jar143→169→178，输入/隔离main及generated/header/test/snapshot/外部跨阶段hash前后门禁全部通过。

当轮XML与suite一致：UserMapperBootstrapTest14项、UserMapperConsumerTest12项，共26项，failure0/error0/skip0，实际SUCCESS。覆盖旧User-first反例generated无JOIN且档案缺失、C正常及两个强制顺序XML JOIN与13/16/9字段/handler、无profile/NULL/删除/隐藏/缺tenant列、同事务六表来源及raw角色岗位、原六消费者的保护/普通正向/改密、合法增强与未知JDBC/写批处理拒绝。测试只核list/page/嵌套statement注册与引用，没有完整分页/嵌套执行；NORMAL resolver未持久化逐个资源枚举列表，只有唯一性与无fixtures覆盖断言及实际CP清单，不能声称已交该细粒度顺序日志。

本结果只属于两新类26项，不把旧177累加为本次结果，也不替原AssignedRoleDetail42/用户档案及其它安全回归、独立QA或live验收。main/test/source/ClassPath/模型/图/累计hash、两XML副本、xml-summary.json及实际套件均保该RUN；可交独立复核。settings仍恢复059FF8，9016/PID18544持续监听，无本轮client残留；无IDE/DB/服务/缓存/依赖/回拷live/提交推送。源码/tests/helper已冻结并交回唯一槽，后续原回归须先定精确清单再授。

### 固定20类fresh隔离回归辅助准备（尚未执行）

总控后续固定范围为SYS原9+C2、AUTH6、DATA3，另建ignored `.gradle/C-full-regression-20261028.py` 与 `.gradle/C-full-regression-20261028.gradle`，不修改两类C入口、旧run或任何生产/测试源。新Py SHA833A4293D895E9CE58608CA4A4D4E5F8CC95F2E721DA7B479DC506D33A17D359；init SHA4E0F05675184875E56984B575E42D1D7DB3DA3BEC233D9A90935565DF36093A2。冻结C入口仍3C944ECB/E1BA9AC3，7f8f0654两类26项证据不覆写。

每个类路径均已按 `<模块>/src/test/java/<全类名以斜杠替换点>.java` 核文件、package及class声明。下表数字仅2026-10-24旧XML历史计数，C2数字来自独立7f8f0654，不是本20类新执行结果；新验证器不预填合计用例数。

| 模块 | 精确类名 | 先前计数 |
| --- | --- | ---: |
| eva-web/eva-web-sys/eva-web-sys-service | org.pkaq.sys.role.service.AssignedRoleDetailTest | 42 |
| 同上 | org.pkaq.sys.user.service.UserAccountProfileServiceTest | 8 |
| 同上 | org.pkaq.sys.role.service.RoleCustomScopeStandaloneTest | 48 |
| 同上 | org.pkaq.sys.role.service.RoleServiceTest | 14 |
| 同上 | org.pkaq.sys.RoleFrozenMappingTest | 2 |
| 同上 | org.pkaq.sys.AccountTenantFrozenMappingTest | 4 |
| 同上 | org.pkaq.sys.TreeFrozenMappingTest | 2 |
| 同上 | org.pkaq.sys.TreeFrozenEditTest | 2 |
| 同上 | org.pkaq.sys.WriteProtectionServiceTest | 4 |
| 同上 | org.pkaq.sys.role.service.UserMapperBootstrapTest | 14 |
| 同上 | org.pkaq.sys.role.service.UserMapperConsumerTest | 12 |
| eva-web/eva-web-auth | org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAuditCoreRoutingTest | 6 |
| 同上 | org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAuditMapperSqlTest | 1 |
| 同上 | org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAuditReadAccessTest | 22 |
| 同上 | org.pkaq.core.auth.authorization.service.ResourceAuthorizationServiceTest | 2 |
| 同上 | org.pkaq.core.auth.authentication.filter.JwtAuthFilterTest | 2 |
| 同上 | org.pkaq.core.auth.config.WebSecurityCapabilitiesTest | 4 |
| eva-core/eva-core-data-mybatis | org.pkaq.config.OpenApiAuditQueryPermissionTest | 4 |
| 同上 | org.pkaq.config.MybatisPlusDataPermissionHandlerTest | 8 |
| 同上 | org.pkaq.config.LogQueryPermissionInterceptorTest | 2 |

AssignedRole42直接复核共用fixture的manual恢复与原六表/事务/摘要/岗位语义；UserProfile8补原账号/档案写链与失败回滚；SYS其它类覆盖共享枚举/冻结、角色/岗位写保护、permVer和权限边界。AUTH/DATA不是声称直接使用User.xml，而是恢复此前已批准安全闭包。没有单列旧UserRole/UserPost测试类，不冒称原角色本体测试替代C Consumer正向。只读核当前18个旧类源码哈希均与7f8f0654输入锁一致；SYS九类class已在该绿run准备，AUTH/DATA测试未准备，这不是它们已通过。

新入口仍fresh run三阶段：十模块main重新实际编译并核旧d仅User C5ED差异；三个目标模块真实testClasses；最后准确三个Test任务、零producer，每任务固定完整类名筛选。复用的是已审隔离代码和门禁，7f8f0654只作冻结证据，不把旧绿run目录直接加进新CP、不回读live build，不为省编译引入未审seed逻辑。每模块独立封存测试清单、实际CP/套件/XML；每个Test启动前核全部三个模块prepared清单。单worker/offline/全Project输出与project-cache独立、源与产物/生成头文件/快照、外部跨阶段累计锁全部保留。

AUTH/DATA源码无test/resources，新CP仅精确替换对应模块已声明test roots，惰性包含准备后实际存在的roots；合法NO-SOURCE资源缺失不冒称产物。测试类产物仍必须为正，各类XML必须正计数/0skip，未知缺失路径及其它目录不放行。此多模块适配已明确交designer/QA审查，未因空目录回读标准build或旧jar。新Py仅inputs/test_outputs/commands/verify_results/run_verification五函数AST有定向变化，其余门禁函数AST与冻结C版一致。

轻量静态核已过：Python AST、20类源包名与init/Py/PLAN筛选一致、3Test命令及离线flags、默认PLAN不创建RUN/不执行Gradle、无预填用例总数。对结果校验器用临时合成XML验证20类各一例可通过、零用例/skip/漏类拒绝，临时目录已清理；这不是实际测试执行。候选正式命令仅 `C:/home/DevApp/python3/python.exe .gradle/C-full-regression-20261028.py --execute`，须独立审后由QA获唯一槽执行，本成员未运行Gradle、不占槽，不操作IDE/服务或提交推送。
