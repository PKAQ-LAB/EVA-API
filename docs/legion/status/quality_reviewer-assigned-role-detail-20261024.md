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