# assignedRoles独立安全QA（2026-10-24）

当前阶段：只读审查与快照预检完成，独立177项回归仍在总控队列，未运行编译/测试。前端阶段1已交槽，字典/系统查看优先；未浏览器/服务/DBHTTPRedis/源码依赖写/发布。不将backend自测177或旧CUSTOM119算作本角色新回归。

## 源码与测试审查

已读designer最终契约、backend实现附件/最新status、局部Git diff、新UserDetailVo/AssignedRoleSummaryVo、UserService.getUser、UserRoleRefSerivce完整源、RoleMapper/UserPostRef原路径，以及CoreSchemaExecutor/TrustedTenantSchemaResolver/Router/外层Aspect。只原详情新增assignedRoles{id,name,code,frozen}，不新增complete/route/seed/BO/DB。原ref仅精确target一次快照，原roleIds保非法/NULL/重复历史项；合法正ID去重只用于摘要IN，空不查询全表，单次selectMaps四列无200cap/N+1。rawMap保历史code大小写/前缀与NULL，不用实体setter污染；frozen1/9999保留，未知值失败而非默认正常。当前未发现新必须修复项。

新读辅助MANDATORY并自核可信operator、mode/context、同事务DataSource绑定；非Ignore账号可见后才调用原Ignore关系Mapper，RoleMapper BaseMapper.selectMaps本身无Ignore，逻辑删除及operator数据权限插件保留。无ensureUserExists/ensureRolesUsable写守卫误排只读账号或冻结角色，读失败不catch转[]。单纯历史删/孤立/不可见摘要缺项与源异常区分，原写roles/posts/permVer链不改。

可信schema检查固定六表SYS_ACCOUNT/PROFILE/ROLEUSER_REF/ROLE/ORGANIZATION/POSTUSER_REF的实际namespace+current_schema与预期一致，参数化catalog不读角色裸SQL。正tenant局部core executor先复核SYS_TENANT真实来源再权威resolver，finally恢复原path；初始router若受shadow影响会在目标查询前被后续权威映射/context校验拒绝。getUser岗位用原postUserMapper同target查询，避免核源之后再进岗位service的二次TenantSchema；其它service/全局resolver未扩修，不声称旧全局风险已全部消除。无ORNULL/行tenant0共享/客户端tenant/schema/目标角色代operator。

已逐段读AssignedRoleDetailTest及JdbcFixture：新增参数化共42项；真实Service/生成Convert/XML/BaseMapper/MP插件/Spring只读事务与实际外层Aspect，合成JDBC/行数据而非真实PG。prepared/plainStatement检查事务ConnectionHolder同连接及只读事务标志，SQL仅SELECT，实际BoundSql和参数经JSqlParser谓词执行，未知语法直接失败，无mock visibleIds；覆盖精确目标、207角色/重复/19位、空ref零role查询、NULL/raw/冻结只读/删隐藏、六表逐表fallback/第三schema、无身份/坏模式/context、A/B同ID、core控制面shadow及post二次路由、SQL异常/恢复/零写。关系Mapper原Ignore没有扩成role元数据Ignore。该审查不是42项已独立执行或真实租户验证。

## init/snapshot只读预检

已完整读.gradle/assigned-role-current-20261024.gradle与snapshot脚本：十模块main编译输入前置本地副本，Test classpath前置snapshot-d，每Test依赖全部十模块classes后逐文件同时核当前输出与快照SHA+完整清单。历史c竞态门禁失败保留；本角色未执行会重制snapshot的脚本（已有即throw，不覆盖证据），未偷偷重编译。

独立只读遍历当前十模块输出与snapshot-d，494文件（467class/27resources）全SHA匹配manifest，两个目录清单数量一致；27个当前源资源逐字节与输出/副本相同。manifest SHA256 D87ED7CCFD71317A2A1909451D0425F4721A4B7636BF6CC51A798A02CD349EED，与交付相符。此为测试前只读预检，不代后续测试前后再次全量核验或Java新编译。没有运行Gradle/Test/tsc/build或访问实例。

## 后续门禁

等待root授后端验证槽后按附件精确命令强制三个test任务执行，18类177与SYS126/AUTH37/DATA14逐类fresh XML/执行任务/skip0核对并独立保存证据，不累计旧轮；运行前后重核494清单与SHA及27源资源，不一致停报而非悄编译替代。源/fixture进一步变动需重新核当前产物。

后续完整产物加载由总控统一协调：相关十模块classes/resources或完整构建包须同一批，不能热换单class；由用户正常重启，QA再限定核新实例启动时间及classpath首来源/新class资源hash，随后合法界面最小详情验证assignedRoles存在/仅已授/缺项明确/无写。当前服务是否加载新契约未证，前端Mock不能代替；本阶段不要求用户在本会话再次重启。S账户PEER1+原九对象保护，未新建LIMIT/样本或grant。
## 独立177首轮被产物清单门禁拦下（2026-10-24 21:07后）

用户明确继续中断后，总控正式授后端验证槽。预检仅输出进程ID/类别，未输出命令行或环境；存在旧GradleDaemon但没有GradleWorkerMain/launcher/vitest，未启动重复任务、未杀服务。测试前再次全量核494文件/27源资源与snapshot-d相同，manifest仍D87ED7CCFD71317A2A1909451D0425F4721A4B7636BF6CC51A798A02CD349EED。

执行实现附件精确18类筛选，三个Test任务分别加--rerun，统一--offline --no-daemon --console=plain --max-workers=2 -x jar，init仍原.gradle/assigned-role-current-20261024.gradle，stdout/stderr存.gradle/qa-assigned-role-regression-20261024-a.log。29s BUILD FAILED/exit1，13actionable9executed4up-to-date；data-mybatis:test在doFirst/init第56行Current output inventory mismatch失败，未进入测试用例执行，SYS/AUTH test未执行，因此独立177未完成、不生成通过计数，不能拿旧XML顶替fresh结果。

日志实际顺序为contract/common/domain/cache/log/upload/data等compileJava执行后，data:test门禁先失败，随后web-core compileJava完成；这与所述所有main产物在所有Test前结束的预期不符，是调度/快照门禁失败而非已证明业务用例失败。结束后独立遍历当前全部输出：expected494/current494、added[]/missing[]/changed[]，全部SHA仍与d一致；支持临时清单变化可能性，但不能仅凭结束一致武断证明全部原因。

全部后端命令已退出，未重建/覆盖snapshot、未放松哈希门禁或自行改init/源码，未偷偷重复编译或重跑。已交总控派backend最小安全调度诊断，等待后续独立重验；原42项安全审查保留但不能当执行通过。按总控顺序转冻结前端日志聚焦QA，与后端无并行回归。服务PID已经变化，旧49000仅历史，不作当前加载证据；无实例重启或实际DBHTTPRedis/数据操作。
## 后端三阶段辅助草案独立只读审计（2026-10-24）

仅读Python/phase guard/原init及backend末节，连默认PLAN均未执行，无Gradle或ast执行。冻结SHA吻合：Python3BDB1170757F2B79B33C16CCC18C24F5C1E12993B3DF7E4F2CE16CB18446DA89；guard68417F27830D9209F8B5DAF530F8E45C38B380D9F53139C905640326DD165125；原initDF6C0C52D56EFD41D819C67663458830F4C4F23A6E0350450902DBF0F449DACF。

静态核三subprocess同步返回再进入下阶段；main十模块classes，test-classes三模块强制compileTestJava及资源，第三仅3Test并排除所有main/test生产者。固定路径/argparse仅execute、 cwd锁仓库；新UTC+UUID目录exist_okfalse、不覆盖旧manifest/snapshot，原init/classpath与hash门禁保留；每阶段前后封存494/源资源比对、所有源和helper变化拒绝、测试输出准备后锁定，失败exit不重制snapshot。guard真实图要求精确三个Test且无其它节点，源输出producer也显式拒绝；根suite实际计数126/37/14和失败skip0与新报告路径核对，不借旧XML凑数。目录/命令无外来schema/凭据，编译日志只合成测试，不访问实例。运行Groovy排除图/原门禁是否可成功仍未证，不能静态报177通过。

执行前发现P2输入封闭缺口：code_inputs未锁实际父项目eva-core/build.gradle、eva-web/build.gradle、eva-web/eva-web-sys/build.gradle，只root及10叶build.gradle。这三个文件参与配置（当前内容为publish disabled），未来/并发配置变化可能逃过baseline；已报root派backend仅辅助脚本补父构建输入并重新冻结SHA。未擅改脚本。补齐后可建议受控执行，仍须root单独授槽并核新SHA；本角色先按root顺序sys完整复验，后端helper继续未执行。
## 当前实例与Edge只读加载前置核查（2026-10-24晚）

总控单独授权仅元数据/页面结构读取。实际9016监听Java PID43412，启动21:01:38.6088719+08:00，Zulu25；8000为Node45044，启动20:59:44.5997800，未据此认定其服务dist或已更新所有前端source。旧PID49000/39300仅历史，不再当现实例。

jcmd仅VM.system_properties在命令内取java.class.path和sun.java.command允许项，不输出完整属性/命令/环境/凭据。classpath199、main org.pkaq.WebBooter、含当前十相关模块及web-booter目录，无测试snapshot。按实际classpath顺序（含先行JAR只读ZIP目录查目标），UserService/UserRoleRef首提供者index171为workspace classes；UserDetailVo/AssignedRoleSummaryVo index188；两个User/UserRoleRef XML index170为当前资源目录。四class SHA分别50DEC874...、88A7A9C0...、BD00D9D0...、F36F36F0...与d封存完整值一致；XML SHA BFC0F6B427D65F8E1F62D3D5364FECDD2F181DBC05C3AC40729BD0144A5F24EF及014BD54455B20E07580AC7F447ACAC8226C10A72B9C26AA8D1B33DE54957CAAD。service两class21:01:34早于进程，VO21:08后写但字节一致；不是内存dump/实际HTTP契约验证，不凭磁盘时间要求或宣称重启完成。

已读新版computer-use skill与guidance/confirmations，沿浏览器专用CUA Edge3读取当前新tab328077397 #/sys/role，管理员页面结构可用，visibleDialogs0/editPanelfalse/password输入false，viewport1912x949。只读取可见角色列表和DOM结构，无账号私人字段/token/日志正文/输入值/截图，无刷新/切路由/CRUD，不改变用户未保存资料。副屏位置沿用户说明，不以浏览器API冒物理屏幕坐标核验。UI随后停止，等待后端回归与统一运行交接。

## 辅助闭包修正审完并正式受控执行

最终Python0A85F5E26C226D6B231E3D3C4DFD1AE82401190BE6C24737A3A87B165207B7C1，仅code_inputs补父级/实际额外build配置、presence键与buildSrc/6原不存在配置出现即拒绝，旧guard68417F...与initDF6C...哈希不变。P2输入闭包缺口已补，无新必须修复项；原运行门禁仍不放松。总控明确授唯一后端槽后以已确认本地C:/home/DevApp/python3/python.exe运行 .gradle/assigned-role-staged-verify-20261024.py --execute，stdout/stderr .gradle/qa-assigned-role-staged-20261024-b.log；三阶段当前执行中，不先计177通过。无并行前端/UI/实际DBHTTPRedis/服务/源码依赖改动，不重制snapshot。
## 三同步阶段独立177项通过、完整门禁闭合（2026-10-24 21:43–21:45）

总控在最终输入闭包补齐后明确授权执行，实际唯一命令C:/home/DevApp/python3/python.exe .gradle/assigned-role-staged-verify-20261024.py --execute，cwd本仓库，helper最终SHA0A85F5E26C226D6B231E3D3C4DFD1AE82401190BE6C24737A3A87B165207B7C1、guard68417F27830D9209F8B5DAF530F8E45C38B380D9F53139C905640326DD165125、原initDF6C0C52D56EFD41D819C67663458830F4C4F23A6E0350450902DBF0F449DACF。没有默认PLAN试跑或盲重复失败命令、未重制snapshot。stdout/stderr .gradle/qa-assigned-role-staged-20261024-b.log，fresh审计目录.gradle/assigned-role-staged-runs/20261024T134347Z-171d729a。

实际三个同步进程都exit0：main BUILD SUCCESSFUL31s/14tasks2executed12up-to-date；test-classes21s/16tasks3executed13up-to-date，三模块compileTestJava强制执行后测试输出锁定；test36s/3tasks全executed。test-task-graph.json实际只有SYS/AUTH/DATA三个Test，无其它节点或forbidden producer。每个Test的原门禁记录ALL_OUTPUT_HASHES_VERIFIED494，新增即时清单494/added[]/missing[]。三个phase gate before/after494且manifest不变；source/build/helper输入和准备后测试输出闭包都通过，无删除原门禁/吞差异/新快照。

本轮执行套件SYS126/AUTH37/DATA14=18类177，failures/errors/skipped全部0，明确是本角色独立执行而非开发旧177。新42真实Service/生成Convert/实际Mapper/XML/非Ignore RoleMap/数据插件/事务及外层路由mockJDBC安全用例通过；原CUSTOM48、用户档案8、冻结/写保护、OpenAPI/JWT/资源/日志数据门禁回归保持。隔离合成JDBC，不冒真实PG租户A/B或HTTP权限实证，不累计旧119/开发177/前端各轮。

已逐类核build/test-results/test本轮18个XML LastWriteTimeUtc晚于新审计目录，Junit timestamp UTC13:44:55.508–13:45:20.067（北京时间21:44:55–21:45:20）；计数SYS9类126、AUTH6类37、DATA3类14，AssignedRoleDetail42/RoleCustomScope48均fresh。独立副本在审计目录quality-reviewer-fresh-xml/{module}，每类统计quality-reviewer-fresh-results.json，拒覆盖旧证据；未读XML中实际私密日志，全部合成夹具。

结束后另用QA独立PowerShell遍历当前十模块输出、封存d副本完整清单与SHA：当前494/副本494完全吻合，27源资源逐字节同输出，manifest仍D87ED7CCFD71317A2A1909451D0425F4721A4B7636BF6CC51A798A02CD349EED。index空。前29s门禁失败及原因未全证的历史仍保留，不用这次成功抹去；当前只说明分阶段调度这一轮完整通过，不宣称原单图dependsOn已足以消除所有竞态。

所有进程/命令结束，后端验证槽交总控。前端sys242/242、日志23/23、TanStack20/20与生产build0各独立保留，fulltsc59诊断不漂白。当前实例43412/21:01、关键类路径与封存字节前置已证，但仍没有实际get响应或内存加载证明；后续统一运行界面验证新assignedRoles及同布局，必要重启由用户/总控协调，不由QA启停或热换class。S账户PEER1+九对象保护、账号LIMIT/作者样本未建、S0/跨tenant/日志现场边界未完；无实际DBHTTPRedis/秘密/授权/业务源/依赖/Git发布/vault写。
## 2026-10-28 C隔离26项交付独立只读证据复核（非QA重跑）

按总控只读授权审.gradle/C-isolated-runs/20261027T155115Z-7f8f0654，不运行辅助/PLAN/Gradle/浏览器/IDE。identity HEAD1f708e202af0ba817b55c0f6be9a9085eea054ea；main/test-classes/test gate各exit0。实际图30/15/1，Test唯一SYS:test、forbidden0，命令仅UserMapperBootstrapTest/UserMapperConsumerTest两类及producer排除；非全仓/原18类。

独立解析RUN内fresh XML（非读取实现者摘要计数）：Bootstrap14/Consumer12，26testcase，均fail0/error0/skip0，XML UTC15:55:24.640与15:56:05.093；executed-suite26/SUCCESS一致。实际case含legacyUserBeforeCommon、NORMAL/USER_FIRST/COMMON_FIRST、Frozen0/1/9999/NULL/非法、缺tenant列、optionalprofile/NULLdeleted、权限隐藏、角色raw/post同源、JDBC未知写拒与配置增强正负边界；Consumer含真实C映射有/无profile普通save+role/post、保留编码/READ_ONLY拒写和原repwd规则。只证明实际安装MP/Spring最小auto-config+合成DS隔离链通过，不是整个真实Web应用/数据库/HTTP。

独立Get-FileHash逐项复算snapshot494均match，manifestSHA44D18D9417FB88E868530A9134881CABFF8481618D9B92A1850A0079984876BF。相对旧d494唯一不同key为eva-web-sys-service/resources/mapper/User.xml；三个phase main-output各494与新manifest0差异，实际products主classes/resources现值核0hash差，prepared-test-output38项现值0差；源/构建/辅助锁中64位SHA键当前0差；累计外部jar178逐项重算0差。path-model19projects/10modules，main目录无RUN外路径，Test CP22目录全在RUN（snapshot20+SYS test2），无拿live/旧jar重复User资源冒C；完整路径模型审计仍以其actual文件为证。

当前生产diff仍User.xml的BaseResultMap13增1删，16显式属性/原SQL和嵌套引用保持。最新fixture窄修getType仅新bootstrap+无参返回TYPE_FORWARD_ONLY，旧manual不变；配置增强仅明确mybatisPlusConfig且已解析ClassUtils原类身份/direct父类/注解metadata/BeanFactoryAware，增强工厂仅两方法，不以CGLIB后缀或Spring整包放行。26通过不能抹掉此前配置/API/白名单/getType失败；此前所有run保留。证据复核通过，无新增真实安全mustfix；不是本人独立执行通过。

仍缺：新fixture旧order=null手工路径的AssignedRoleDetailTest42本轮回归；UserAccountProfileServiceTest原普通创建/补档案/乐观锁/失败保护；SYS其余原门禁及AUTH6/DATA3。建议下一获授的独立运行固定20类=原明确SYS9/AUTH6/DATA3+新C2，一次fresh RUN沿隔离main→三模块testClasses→严格3个Test tasks（SYS/AUTH/DATA，逐task精确filter，无producer），只接受当前锁定源/外部及新C snapshot，不盲套旧177或预填203为通过，实际XML定当轮数。新2可在该独立集合再验一次；不必先重复单独26又再跑相同2。

list/page/嵌套仅注册SQL/映射引用尚未执行完整分页/嵌套；原六调用mock写不等真实权限写。后续用户正常加载新运行产物后，仍需真实factory selectById来源/BoundSql、PEER与LIMIT/D1正常只读展示/空profile及9999原保护的限定验收，不通过真实改密/DB回填/管理员写来证明。IDE临时设置恢复证据、浏览器扩展清阻后九格现场均待；C/S11/字典明细冲突不all done，无新commitpush。

## 2026-10-28 固定20类隔离独立执行：203/203通过，live验收未完成

总控正式授唯一重验证槽后，先读designer终审附件并核新Py833A4293D895E9CE58608CA4A4D4E5F8CC95F2E721DA7B479DC506D33A17D359/init4E0F05675184875E56984B575E42D1D7DB3DA3BEC233D9A90935565DF36093A2精确一致；对7f8f0654冻结清单逐项SHA及presence重核0差，HEAD1f708e202af0ba817b55c0f6be9a9085eea054ea、cornerstone保护旧docs/.vscode/GO计划，未暂存/提交。python命令解析C:/home/DevApp/python3/python.exe，与批准入口一致；只执行一次新helper --execute，无PLAN重跑/失败后自动改动。

本人独立RUN=.gradle/C-full-regression-runs/20261027T162106Z-4bef1b8a（北京10-28 00:21起），session58237最终exit0。三独立串行阶段main2m12s、test-classes1m39s、test2m22s均BUILD SUCCESSFUL/gateexit0无gateError；实际图30/25/3，forbidden0，末图仅SYS:test、AUTH:test、DATA:test，无producer。非全仓build、不启动应用或真实DBRedis、不回拷live，原失败run不覆盖。完整shell及等待的子命令已退出，按当前RUN命令行未发现派生进程；没有杀进程/操作服务或IDE。

独立读取本RUN实际20份JUnit XML，classname集合与批准SYS11/AUTH6/DATA3精确20/20、无重复/零项，testcase数与每suite tests一致；零fail/error/skip。模块实际SYS152、AUTH37、DATA14，总203/203，当轮三executed-suite均SUCCESS且与XML对应，不以旧177+26推算通过。XML时间UTC16:25:47.276至16:27:36.615（本地10-28 00:25至00:27），新RUN内正常执行，未用历史XML。关键AssignedRoleDetailTest42/RoleCustomScopeStandalone48/UserAccountProfile8/Bootstrap14/Consumer12均实际执行，其余原资源/认证/日志/数据权限安全用例在本轮固定范围。

执行后再独立复算source/config/test/helper锁及presence0差；snapshot494逐项SHA0差，manifest44D18D9417FB88E868530A9134881CABFF8481618D9B92A1850A0079984876BF，对旧d唯一UserXML key差且批准C5ED；源/隔离main产物门禁均过。三模块prepared清单SYS38/AUTH33/DATA15实际SHA0差，累计外部178jar逐项重算0差。actual CP目录SYS22/AUTH21/DATA21，逐项属于新snapshot或本模块声明test roots，无其它模块测试目录/标准build/旧run混入；AUTH/DATA无testresources仅省精确NO-SOURCE根。一次手工CP核因把model.classes单根数组嵌套成own数组而误标3个合法own classes，已按模型展开重核invalid0，非实际CP越界/未改门禁。新model与3phase图/gates、suite/XML/CP/prepared/external证据全在该RUN。

结论：本次固定20类受控隔离独立回归通过，包含新C机制与六消费者合成控制流及原manual fixture/安全回归，不是实现者26转述。本结论不等真实PG/Redis/运行中factory/HTTP/UI/管理员业务写通过。分页/嵌套仍主要注册与结果引用核，未新增完整业务分页/嵌套case；实际live新statement来源/BoundSql及PEER、LIMIT正常详情D1展示、原9999保护的限定现场仍待总控安排。不会通过改密码/重填部门/DB回填/真实授权扩大自证。

浏览器扩展阻挡仍未人侧清除，未尝试浏览器/IDE，九宫格实际亮暗/尺寸/节奏及其它未完矩阵仍待；S11与字典明细A/B冲突不变。IDE临时自动构建设置恢复以专属后台证据核，不冒本轮操控。所有命令结束主动交回唯一验证槽，源/helper/配置均未写，只有ownQA卡新增提交后dirty；无新commitpush，不all done。

## 2026-10-28 继续现场复验：Edge扩展阻挡仍在

用户继续后，官方CUA恢复文档并fresh Edge3 inventory，实际仅业务tab328082602，http://localhost:8000/#/sys/role。claim该用户tab后只读DOM白名单成功：dialogs0、modal/drawer forms0、visible password false，未读取任何输入值或账号联系信息。随后唯一一次正常reload调用返回Edge另一个扩展UI已打开、需人完成或关闭的阻挡；不能证明reload已执行或runtime已更新，立即停止，无低层绕过或重复重试。

本轮PEER/LIMIT详情与正常编辑打开、D1回填、assigned-only角色、左右加载稳定布局，以及Cube九格动画/40px/尺寸/亮暗现场均未取得新证据，继续待验。既有独立203自动回归与36前端自动验证结论保留，未重跑、未拿自动结果替代实屏。未修改源/配置/业务数据/角色授权/密码/服务/cache/Git；S对象11、S1-S8未开始不变。需人先在Edge完成或关闭阻挡扩展界面，再交唯一UI锁；本轮释放锁并向总控回报。
## 2026-10-28 用户清阻后恢复：刷新被Fitkun图片批量下载界面接管

用户经总控明确可以了后fresh官方连接：旧Edge3不可用；inventory实际Edge5/6（同业务tab328082721），依fresh选Edge6并claim，实际http://localhost:8000/#/sys/role。最小DOM确认dialogs0、modal/drawer forms0、visible password false，未读取input值/contact。唯一一次正常reload后官方domSnapshot返回四项：×、拖拽到此处、图片将完成下载、由Fitkun图片批量下载提供。此轮未返回先前blocking错误字符串，而是实际读到Fitkun扩展界面，不能说已进入正常业务页或完成加载验收。

按仍拦截一次即停，未点击扩展关闭/改变扩展设置、未换连接绕过、未反复reload、未继续账号菜单/详情/编辑；未截取任何账号或凭据信息。PEER/LIMIT D1回填与assigned-only、左右loading稳定布局、Cube九格/40px/尺寸/明暗均无新实屏结论。自动203/36未重跑，源/helper/config/服务/DB/Redis/授权/密码/Git不变，S对象11、S1-S8未开始。交回唯一UI锁；需用户在Edge关闭Fitkun当前拖拽界面后再派发。
## 2026-10-28 Fitkun状态澄清与PEER真实部门复验

总控允许正常关闭已识别Fitkun浮层后，fresh Edge6/tab328082721官方snapshot同时读到正常角色业务页与Fitkun文本。进一步只读DOM定位其自身#fitkun-drop-panel及唯一#fitkun-drop-panel-close-btn，实际panel y=-33554430、close y=-33554428，位于屏外；本轮无需也未强点关闭。此前仅看到Fitkun文本即停不足以证明其可见阻挡，本段更正该判断，不推断浏览器实际错误已被脚本处理。

正常用户管理link导航成功，URL#/sys/account；仅查查询控件元数据及表头，跳过手机/邮箱/密码值。合法精确账号QA_PEER_20261023查询完成spin0唯一行，技术ID2113894345662021633、dept—、未锁定。正常查看该唯一行：初始1040×608Modal，左右aria-busy true；ready左右false、表Spinfalse，资料645.328×458、角色322.656×458同top375.5，view输入/textarea/select/combobox0。所属部门纯文本仍—；角色区仅表头与暂无已分配角色（0候选选择控件）。只证明本次空角色assigned-only消费和独立busy转变，不冒称实际拿过响应JSON、完整左右pending-ready像素零位移、C已生效或后端已重启。

证据runtime-20261028/peer-department-runtime.json、peer-department.png、peer-assigned-role.png，安全裁图已视觉核，均无contact/密码。按部门仍缺即停链，未重开PEER编辑、未重选部门、未验证LIMIT或继续S权限；Cube九格/40px/尺寸/亮暗实屏也尚未完成。已正常关闭只读详情，当前#/sys/account dialogs0、visiblepasswordfalse，无未保存输入，交回唯一UI锁。无业务写/授权/密码/服务/source/helper/config/Git操作，无203/36重复自动。S11、S1-S8未开始不变。
## 2026-10-28 用户确认后台已重启后：正常刷新被官方扩展阻挡拒绝

总控转述用户明确后台已经重启，授仅PEER/LIMIT部门回填只读UI，不检查动画偏好。fresh官方Edge6用户tab328082721，实际http://localhost:8000/#/sys/account；最小DOM成功：visibleDialogs0、visible modal/drawer forms0、visible password false，未读取contact/input值。唯一正常reload立即由官方工具明确返回Microsoft Edge is blocking automation because another extension UI is open on this page. Complete or dismiss that extension UI in Microsoft Edge, then ask me to continue. 本次为真正官方拒绝，区别于之前DOM中屏外Fitkun文本；没有可安全取得的关闭target，不猜扩展身份/关闭目标、不再刷新或换连接绕过。

本轮未取得重启后PEER/LIMIT详情、部门D1或role/busy新证据，不能沿用此前dept—当重启后结论，也不能宣称修复通过。未调用matchMedia、未推断OS；未作动画验收。未打开编辑/补部门/提交/改密/授权/读秘密/业务写/服务IDE断点/source/helper/config/Git操作，无203/36重复。阶段安全停止并交回UI锁；需人关闭或完成当前扩展界面后再恢复只读详情。
## 2026-10-28 暂停后明确继续：新连接正常刷新仍被官方拒绝

已读前端docs/legion/tasks/2026-10-28-orchestrator-paused-checkpoint.md（后端同路径不存在，已在前端找到）、本人卡及军团权威规则；无本人旧运行命令/持锁，恢复唯一UI锁。已重新读取computer-use 26.1023.50603 SKILL及更新官方CUA文档，无native操作。fresh Edge4对应此前同一扩展连接6795b222，用户新tab328083472实际http://localhost:8000/#/sys/role。claim后最小DOM成功：visibleDialogs0、visibleForms0、visiblePasswordfalse，未读任何输入值/联系方式。

唯一正常reload被官方直接拒绝：Microsoft Edge is blocking automation because another extension UI is open on this page. Complete or dismiss that extension UI in Microsoft Edge, then ask me to continue. 已按一次即停，无第二次刷新/切连接/原生绕过/猜关目标；本轮不是因snapshot含屏外Fitkun文字而判阻挡。没有可辨识官方关闭target的新证据。重启后PEER/LIMIT部门D1、角色/布局及CSS后普通九格动画本轮仍无新实屏证，不用旧dept—当本轮失败或旧203/36当真实通过。未调matchMedia、不推断OS、不改偏好；未业务/授权/密码/source/helper/config/IDE/服务/Git操作，S链未推进。交回唯一UI锁，等待总控协调解除官方扩展阻挡。
## 2026-10-28 副屏Edge正常导航完成重启后部门验收：PEER/LIMIT均D1

用户继续并明确Edge副屏后fresh官方库存仍Edge3/4，沿同扩展6795b222的Edge4绑定，fresh用户tab328083472实际#/sys/role；无visible dialog/form/password。没有reload。当前业务role表正常但侧栏仅TanStack，用户管理link不存在；未猜权限原因。一次正常goto此前已核实http://localhost:8000/#/sys/account成功进入实际账号页，无官方拒绝。未凭屏外扩展文本判断遮挡、未操作扩展配置或换连接绕过。

精确账号查询QA_PEER_20261023，完成唯一ID2113894345662021633后正常查看；pending左右及表busytrue、12Skeleton。ready均false、controls0，实际部门纯文本QA_D1_20261023、角色区暂无已分配角色。正常关闭后精确查询QA_LIM_20261023唯一ID2114719096971497474，正常查看同样pending三busytrue/12Skeleton，ready全false/controls0、实际部门QA_D1_20261023、暂无已分配角色。未读取contact/密码/完整JSON或重选部门/保存。本轮是真实重启后新UI证，两个详情部门回填均通过；不据UI推断mapper具体加载来源或替代S受限身份授权链。ready左右同y139/h458，资料x442/w645.328、角色x1111.328/w322.656；未采集本轮pending外框几何，不称本轮像素零位移。

证据runtime-20261028-resumed/peer.json、limit.json及两张department安全裁图，均已视觉核显示D1、无联系方式。LIMIT pending顺便捕捉真实9cube animationName cubeScaleDelay/duration1.3s，其中第7块transform matrix(0.989529,0,0,0.989529,0,0)，其余尚none，证明当前普通动画已运行；Modal进场缩放阶段指示器bounding4×4不可误称实际设计尺寸4px。该样式输出在本会话工具证据，未独立保存该pendingJSON或动画截图，不当成完整周期视觉验收。未调用matchMedia或推断OS、未改偏好。40px首屏/14pxsmall/32pxlarge/暗色loading仍未取得本轮证据。

已正常关闭LIMIT详情，停#/sys/account visibleDialogs0/visiblePasswordfalse，无草稿，唯一UI锁交回。未业务写/授权/改密/部门补填/源/helper/config/服务/IDE/Git操作，无203/36重跑。S对象11、S1-S8仍未开展，交总控另排。侧栏系统菜单缺失仅记录实际现象，不擅自定新缺陷或扩展范围。