# 日志结束日补修独立复验（2026-10-23）

当前结论（21:28补修复验）：P2-ERRORLOG-OMITTED-END-EXPANSION代码门禁通过，原103项与新增真实Controller链4项共107项独立复跑全过。运行加载及真实日期查询尚未复验，全部sys/日志/权限矩阵仍未完成，不满足整体发布门禁。下文原103项失败发现保留为历史，最新证据见末节。全部命令结束，读测窗口交回总控，无浏览器/数据操作。

## 必须修复：错误日志未传end经Controller后被扩到次日

位置sys/log/error/ctrl/ErrorCtrl.java46-47与PostgresErrorIncidentRepository.java73/81/85-88。ErrorCtrl.list对HTTP绑定后getEnd()==null先setEnd(new Date())，然后把同一BO传Repository；新Repository以原BO.end是否非null区分用户明确日期和缺省，故Controller已填的当前时刻被当成明确日期，截到当天0点再+1天，最终request_time<次日0点。本次要求的“未传end维持当前时刻<=，不扩整天”在真实HTTP入口不成立，虽然直接new DateRangeBo→Repository测试能走正确默认分支。

具体输入：GET错误日志list省略end（也包括仅begin）。Controller传入end=当时now；若now为20点，Repository将上界改到翌日00点，20点后同日记录也会落入默认查询；旧行为为<=20点。此为新Repository语义与既有调用者组合回归，不要求证明实库存在未来时间记录才判断契约变化。明确用户end=日期仍应按新整日语义，不能撤回业务日期修复。

独立实际编译类探针：JShell使用当前sys/common/contract/log classes及已安装commons-lang3/slf4j binary，直接new ErrorCtrl(合成repository sink).list(new DateRangeBo(),null,null)，输出INPUT_END_NULL=true、CONTROLLER_OMITTED_END_REPLACED=true；sink也记录入参已非null。REPOSITORY_EXPLICIT_END_BRANCH=true仅是与当前生产判定相同的非null条件演示，不伪称该sink执行了真实Repository或数据库。结合已读真实Repository代码和已过explicit-end wrapper测试，路径明确。未复制Controller实现、未写生产/测试源、未接DB/HTTP；JShell无SLF4J provider提示为合成环境限制。

最小修复建议：Controller不要在进入Repository前丢失“用户未传end”的信息，沿已有Repository统一缺省处理或等价保留明确标志；外部DateRangeBo参数/响应不变。至少新增实际ErrorCtrl→实际Repository→mock Mapper wrapper的默认end与only-begin测试，断言上界仍接近now/<=；明确日期仍<次日，租户/ALL与默认分页保持。不要只新增直接Repository单测来绕开该调用者。

## 已通过与证据边界

业务Repository只以safeQuery.getEnd()!=null设置endExclusive；明确end通过原DateUtils.beginOfDay/addDay本地日历，SQL严格<次日0点，begin>=；缺end仍new Date/<=原秒格式，未扩默认范围。内部Mapper countLogs/selectLogPage均新增同名@Param boolean，Repository同一begin/end/flag传给两方法；XML count热/归档、page热/归档四支全部choose<或<=。直接Java调用者仅Repository与同步测试，归档/保存方法未改，get无日期变化。ErrorRepository显式日期wrapper同逻辑且保tenant；其直接缺省分支本身正确，但上节Controller使其HTTP默认不可达。

测试BusinessLogDateRangeTest真实Spring DataBinder/DefaultFormattingConversionService按LogQueryBo yyyy-MM-dd注解绑定，真实Repository→mockMapper采相同count/page前4参数→实际BusinessLogMapper.xml BoundSql，验证同日、1/31、12/31、闰日2/29，包含起点和末微秒、排除前日末/次日0点；hot/archive开关与每分支参数出现次数、strict<与begin>=、defaultfalse<=均核。DST使用America/New_York实际DateBinder与本地Calendar，春/秋日分别23/25小时，finally恢复TimeZone。该验证不是执行实PG行过滤；inRange为JVM边界比较，配真实SQL操作符，不当数据库执行证明。Error新增wrapper测试只直接Repository跨年，恰未覆盖Controller默认填充值。

时区/存储：DateUtils.format走DateFormatUtils，beginOfDay走commons truncate，addDay走commons addDays，均沿原JVM默认日历/时区，未硬设UTC/加86400秒。当前迁移V1定义LOG_BIZ.OPERATE_DATETIME和LOG_ERROR.REQUEST_TIME为VARCHAR(32)，V4归档同VARCHAR32；过滤参数是同固定yyyy-MM-dd HH:mm:ss字符串，非TIMESTAMPTZ参数，因此代码模型为本地墙钟字符串字典序，不能把数据库session timezone自动换算当保证。SQL参数名/方法String类型静态一致，测试生成BoundSql但未检查真实PG PreparedStatement或现库schema。真实部署JVM时区、当前PG列类型/精度/排序及实际多实例时区一致性均未读取验证，不能由客户端Asia/Shanghai或测试默认时区代替。

LoginLogQueryBo/OpenApiCallLogQueryBo为LocalDateTime timestamp契约，服务仍明确timestamp<=，本轮未改；不机械推进一天。ALL/可信身份/tenant/平台target guard在两Repository读取入口保持；原coredata22拒绝零Mapper/真实DataPermissionInterceptor+XML回归仍通过。写入/upsert/归档生产逻辑未改；本轮103不包含额外BizLogArchiveJob2，不能说这两项本轮重跑。

## 独立强制测试

```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' :eva-core:eva-core-data-mybatis:test --rerun --tests org.pkaq.core.mybatis.log.BusinessLogDateRangeTest --tests org.pkaq.config.MybatisPlusDataPermissionHandlerTest --tests org.pkaq.config.LogQueryPermissionInterceptorTest --tests org.pkaq.core.mybatis.log.PostgresBusinessLogRepositoryTest --tests org.pkaq.core.mybatis.exception.PostgresErrorIncidentRepositoryTest --tests org.pkaq.core.mybatis.tenant.TargetTenantExecutorSecurityTest :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.bo.LogQueryBoTest --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.RoleFrozenMappingTest --tests org.pkaq.sys.AccountTenantFrozenMappingTest --tests org.pkaq.sys.role.service.RoleServiceTest --tests org.pkaq.sys.TreeFrozenMappingTest --tests org.pkaq.sys.TreeFrozenEditTest --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest --tests org.pkaq.sys.user.service.UserAccountProfileServiceTest --tests org.pkaq.sys.tenant.service.TenantServiceCompatibilityTest -x jar --offline --no-daemon --console=plain
```

exit0/BUILD SUCCESSFUL23s，17tasks3executed14up-to-date；三个module test真实执行，生产/测试compile复用未变产物。最新XML UTC2026-10-23T12:53:27.166Z至12:53:32.746Z（北京时间20:53），20classes103项failure/error/skipped全0。corelog34=LogQueryBo2+Advice16+Sanitizer15+注册1；coredata26=Date3+Interceptor2+DataPermission8+Error7+Business4+TargetTenant2；sys43与前轮一致。原97保持，未用开发20:49旧XML。各模块build/test-results/test/TEST-完整类名.xml当前可定位。git diff --check0/index空，旧CRLF/JVM/Gradle警告保留。-x jar仅避当前用户服务JAR锁，不算完整包装/当前进程加载。

## 后续交接，未执行

先最小补修上述Controller默认边界并独立复验，之后总控协调用户按原启动方式一致重建/重启加载core-data Repository、Mapper接口与XML及依赖，不能旧JAR/新XML或单类热替换混用（签名新增boolean）。QA不启停。运行再以同一专用模块当天begin=end验证列表/total与翻页不漏当天记录，结束前一天排除、次日零点边界对照；热归档真实数据与受限账号/tenant仍需明确可读测试前提，不能制造日志。错误日志省略end/onlybegin验证<=now，明确日期验证整日；Login/OpenAPI timestamp不回归成日期整日。仅看当前专用对象安全摘要，不旧全文/凭据/改密。R01与M01/M02已恢复原值，本轮未动；其它矩阵未完成，不发all done或提交推送。

## 缺省结束日补修独立复验通过（2026-10-23 21:28）

正式交锁后先完整读取实际ErrorCtrl diff、新ErrorCtrlDateRangeTest、真实Repository与Mapper/XML关联、backend状态及临时.gradle/errorlog-controller-current-classes.gradle。生产增量仅删除ErrorCtrl.list中缺end时setEnd(new Date())三行并留中文说明，未改begin、分页、Response、参数名或HTTP映射。保留HTTP绑定的null，Repository统一以now和<=默认；明确日期仍本地日历nextday<。旧问题在此调用链已消除，本次未发现新增必须修复项。

init脚本逐行已审：allprojects/afterEvaluate仅匹配sys-service Test任务，dependsOn当前core-data classes；classpath=currentData.sourceSets.main.output+classpath，保证当前Repository/Mapper class及processResources输出排在既有JAR之前。无依赖、发布、服务或文件写操作，不修改build.gradle。测试执行记录core-data classes及processResources均UP-TO-DATE，sys与core-data compile也UP-TO-DATE，此次复用已有最新编译产物；三个test任务因--rerun真实执行。源XML与build/resources/main/mapper/BusinessLogMapper.xml的SHA256相同：09DF62DB797D5CFFD8EFE2980B2ABEC62268C8BA1935108088B352123C247190。此脚本只影响本轮测试JVM，绝不证明运行服务classpath一致。

新增4项源码确为真实Spring DataBinder/DefaultFormattingConversionService→ErrorCtrl→PostgresErrorIncidentRepository→mock ErrorIncidentMapper：省略两日期与onlybegin保持range.end=null、SQL request_time<=值夹在调用前后当前秒字符串；默认begin近7日、默认1/10与自定义3/7分页；显式12-31保begin>=当日、end<2027-01-01 00:00；tenant34参数在三种正向wrapper保留；匿名/0007链在Mapper前抛异常且零交互。Mapper为替身，未执行实际PG、HTTP鉴权或真实数据过滤；Response返回表达式静态未改，不把该4项宣传为完整响应或HTTP集成测试。

开发旧Controller红证据已读取.gradle/errorlog-omitted-end-red-20261023.log：4 tests completed,2 failed，失败名准确为omittedEndRetainsNowInclusiveAndOriginalDefaultBeginAndPagination与onlyBeginPreservesExplicitBeginAndNowInclusiveWithCustomPagination，BUILD FAILED34s；其它两项未失败。此为开发保存的红日志核对，不是QA回退源码重新造红；本轮QA不修改生产/测试源码。新绿为以下独立亲跑结果，非引用开发21:22旧XML。

独立命令使用上节完整原103项命令，唯一增加全局-I '.gradle/errorlog-controller-current-classes.gradle'和sys任务--tests org.pkaq.sys.log.error.ctrl.ErrorCtrlDateRangeTest；所有原20类过滤和-x jar --offline --no-daemon --console=plain均保留。exit0 BUILD SUCCESSFUL24s，17tasks3executed14up-to-date，三个module test全部实际执行。新XML UTC2026-10-23T13:28:31.477Z至13:28:38.484Z（北京时间21:28），21类107项，failure/error/skipped均0：corelog34、coredata26、sys47。原日志脱敏/注册/Advice34、数据权限/日志guard/tenant/热归档及日期26、原Frozen/Role/Module/只读/User/Tenant43全部保持，加新Controller4。日期3和Mapper BoundSql回归也在本轮；额外ArchiveJob2不在107内，不能说本轮复跑。git diff --check0、index空；既有CRLF/JVM/Gradle警告仍在，未声称lint/完整打包通过。

存储/时区边界复核：V1 LOG_ERROR.REQUEST_TIME与LOG_BIZ.OPERATE_DATETIME、V4 LOG_BIZ_ARCHIVE.OPERATE_DATETIME均VARCHAR(32)；ARCHIVED_AT TIMESTAMP是另一字段。DateUtils既有JVM本地Calendar推进/固定yyyy-MM-dd HH:mm:ss字符串查询；DST及JVM微秒边界与真实BoundSql操作符证明参数生成，不是PG timestamp精度、session时区、现库schema/collation或多实例时区一致性实证。登录/OpenAPI LocalDateTime契约未改。

最小可靠加载交接（仅方案，本轮未执行）：由总控/用户按原启动方式停止旧后端后，从同一checkout正常构建完整运行依赖；若使用web-booter可执行JAR，执行原Gradle环境的 :web-booter:bootJar（任务 :web-booter:bootJar，不带本测试init、不加-x jar，离线缓存可用时--offline），检查成功后使用本次新bootJar按原配置启动。需确保包内同一版sys-service ErrorCtrl及core-data两Repository、BusinessLogMapper新增boolean签名和mapper/BusinessLogMapper.xml一起加载；不得复用旧core-data JAR配新XML、只替Controller或靠单类热替换。若原为IDE启动，重建全部相关project classes/resources及运行依赖，结束旧进程后按原配置完整启动，同样不混旧JAR。不要为本补修自动clean、删用户缓存/数据、迁移DDL或改时区。用户最近重启发生在此次ErrorCtrl修改前，不足以证明新逻辑已加载；QA没有启停或打包。

运行复验待正式派窗：fresh浏览器只读查询同一M01/M02专用对象当天begin=end，确认当天已生成更新能查到并记录total/翻页，改结束前一天应排除；明确日期次日零点边界仅在已有安全记录可支持时验证，不制造日志。错误日志需有合法可观测的省略end/onlybegin查询入口，核仍<=now；明确end核整日，若浏览器入口总传end或无边界记录则保留“运行未证”，不以空列表推通过、不从QA绕走HTTP/DB。读取仅当前专用对象安全摘要，禁止旧正文/凭据。真实受限账号、多角色、tenant、归档实数据等前置仍待总控按矩阵准备。本轮R01/M01/M02恢复状态保持，浏览器未操作，无源/依赖/服务/DBHTTPRedis/Git发布/vault改动；只更新自己的日期卡和矩阵。
## 再次重启后业务日期现场验证（2026-10-23）

用户再次明确前后台重启、总控交唯一浏览器窗口后，当前实际日期10-23，同日begin=end查询稳定67条/首页20，M01/M02原20:31:54及20:31:41更新均可见；第二页21-40/67，20条日期均当日；前一日10-22为2条、专用ID零命中。原同日0行场景运行通过，非静态版本号或编译类当加载证据。未核全67/归档实数据/次日零点夹具。错误重置默认查询无可见错误但仅1条、无参数监听及未来边界数据，<=now/onlybegin运行未证；107代码门禁保留。新业务表格外框缺失已只读复现并交总控前端补bordered，浏览器暂停，原亮恢复、保留安全空表，详前端runtime卡与矩阵。整体验收未完。