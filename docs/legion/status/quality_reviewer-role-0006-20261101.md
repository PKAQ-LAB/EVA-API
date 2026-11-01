# 0006 指定岗位读取资格：QA 验证清单

2026-11-01。当前仅方案复核与验收准备，**NOT_RUN / 未验收实现**。用户已批准代码与新库初始化；现库加列、字典适配及现场验证仍由总控另审、另派。仅本卡由 QA 写入，不修改产品源码、其他成员卡或 Git。

依据：`docs/legion/design/2026-11-01-role-0006-post-read-proposal.md` 的“最终最小实施清单”与“纯读取入口清单”，早期 API 字符串候选及待实施说明不覆盖后文最终契约。最终 API 为 `List<Long> dataPostIds`，前端十进制字符串 ID 数组，实体存规范 CSV。历史 vault 的行租户描述不代替本轮可信 schema 隔离契约。

## 冻结后必要定向验证

| 项目 | 判定条件 |
| --- | --- |
| 登录主体资格 | 当前可信登录用户的有效岗位与有效 0006 角色配置有交集，才贡献全局受管读取；不按作者岗位、不递归岗位子孙，不新增模块资源资格或编辑、删除权限。正例使用作者岗位不同的记录，反例使用同作者但登录用户无交集。 |
| 资格失败封闭 | 空、格式非法、无交集、删除或冻结岗位、删除或冻结角色、无实际 membership、跨 schema、资格读取错误，均无 0006 贡献；岗位 9999 按已定契约允许有效引用，不扩大岗位编辑权。可信 uid/schema 来自服务端，不信 JWT 岗位 claims、客户端岗位或 schema。 |
| 其他范围保持 | 0006 未命中不压掉其他角色贡献；0006+SELF、部门/CUSTOM、ALL 各按原 OR。0006 命中仍必须通过资源、租户、字典 category、日志等原有门禁；数据权限总开关关闭行为保持原契约。 |
| 读窗口三重约束 | 仅登记 controller 方法进入可信同步窗口，实际事务已激活且 readonly，当前 Mapper 命令为 SELECT，三者缺一不可。验证 AOP 顺序、无事务、实际写事务调用 readonly helper、嵌套调用、异常 finally 清理及后续请求无残留；异步不继承窗口。 |
| 同 statement 读写分离 | Organization 的 get 与 edit 共用 selectById：登记读取可有 0006 贡献，写前预读无该贡献；直接 UPDATE/DELETE/INSERT、批量写同样不得借 0006 扩权。仅 0006 不可写，叠加 SELF 只保留原可写范围。 |
| 入口精确覆盖 | 下表 16 个端点分别核 controller 标记、真实只读事务及 Mapper 路径；列表/分页 count/详情一致。GET del、edit/sort/switch/grant/grantPost/repwd、checkUnique 不开窗口；不能标整个 controller 或通用 StdService。未登记查询不冒已支持。 |
| 角色配置契约 | 新建/切入 0006 非空、正 Long、去重规则为重复全拒、无溢出、全量本源有效岗位；任一非法整批拒且角色/配置/permVer 零写。原 0006 省略岗位保持并重验，显式空数组拒；scope 省略保 scope/两集合；切离清岗位、0006 清组织，非所属 scope 非空集合拒。长 ID 全程不经 JS Number。 |
| 权限版本失效 | 角色岗位配置、用户岗位绑定、角色成员或冻结删除、岗位冻结删除及实际级联影响用户后，旧 token 按既有 permVer 策略失效；无关用户不误失效。先取受影响集合再删 refs，同事务失败全回滚。下一请求重建有效资格，不借旧缓存继续放行；不承诺执行中请求快照即时撤销。 |
| 前端定向交互 | 右侧岗位多选复用真实 /sys/post/list；0006 新选与原 0006 编辑默认展开、已选回显，0003 部门独立。只读无写控件/请求，无候选权限或错误不造值/默认 ALL；加载、切换记录、关闭后旧响应不污染，取消不保存、错误保稿、保存数组。只做必要功能验证，不补视觉全套。 |
| 初始化边界 | 核心单 V1 与租户单模板仅加可空 DATA_POST_IDS；字典保原五码并新增唯一 0006，0004/0005 不扩。不得新 Flyway 链、重放 V1 或夹带现库执行；现库适配、单 Owner DATA_SCOPE 缓存失效、部署兼容性另审后才运行。 |

## 首批 16 个读取入口

| 控制器 | 明确登记端点 |
| --- | --- |
| OrganizationCtrl | `/sys/organization/list`、`/sys/organization/get/{id}` |
| PostCtrl | `/sys/post/list`、`/sys/post/get/{id}` |
| RoleCtrl | `/sys/role/list`、`/sys/role/get/{id}` |
| UserCtrl | `/sys/account/list`、`/sys/account/get/{id}` |
| NoticeCtrl | `/notices`、`/sys/notice/list`、`/sys/notice/get/{id}` |
| DictCtrl | `/sys/dictionary/list`、`/sys/dictionary/get/{id}`、`/sys/dictionary/get/type/{code}` |
| ModuleCtrl | `/sys/module/list`、`/sys/module/get/{id}`（需补实际只读事务） |

明确负例：GET `/sys/dictionary/del/{id}` 无窗口；RoleCtrl fetchResource/listUser、AuthCtrl fetchMenus/fetchDicts、DictCtrl query/validate 沿各自专门资格，不借本次扩大。Mapper Ignore、模块资源排除与审计窗口原边界保持。

## 当前状态

已读最终设计与入口清单，尚未审成员未完成实现、未运行测试、浏览器、真实账户或数据库、未重启服务。待成员交冻结清单后，只围绕新增功能做独立源审与必要定向复验；live 前置由总控统一安排。此卡不是通过结论，不继承此前核心包绿项来证明 0006。

## 前端首轮冻结候选窄源审（未测试）

manifest `.git/role-post-scope-source-20261101.json` 声明 SHA057347441E9C1D36173D20EB360790D1B81D868ED2D13CE64BFD40A3D47EBB37，7文件逐一SHA匹配。已读完整新RolePosts、表单接线、service及新增测试。候选加载失败封闭、active清理忽略旧响应、readOnly无checkbox、有效0/9999及失效已选保留移除、长ID字符串数组、切离岗位数组清空等源码已核，不视为运行通过。测试槽backend持有，QA未启动测试或live。

需收口：原0003带dataOrgIds切0006时，仅隐藏部门区却仍无条件读取旧dataOrgIds并提交CSV，形成0006岗位+旧部门双集合；与最终清组织/非所属集合拒契约不一致。已直接交frontend/root最小保存边界及0003→0006回归，不改产品源。另saveRole接受Partial类型却对0006省略岗位字段直接拒，需确认完整表单helper边界或保持API省略语义；当前UI完整详情编辑会传数组，此条不冒已证明实际UI故障。等待成员回应/最终重冻，不签全部合同通过，不读取backend未冻结实现。

## 前端两项修正的增量源审（测试待槽）

新 fixed manifest SHA93F4C1AEC1599457B20FCD00A9C23FAF1CE70AAF7A859412D5A76A8B4C133749及7文件SHA逐项一致，旧0573仅历史。0006 formData及service均明确dataOrgIds空串，原Form值保留供切回且旧scope组织序列化不变；existing id省略dataPostIds不补字段，交后端判保持/非法切换，新建缺省或显式空数组拒。已读新增实Form0003→0006清组织及adapter省略/旧0003回归断言，两项源审闭合，无新增mustfix。QA尚未运行测试，不把开发分轮通过当独立通过；等待backend正式交还重槽后，以此最终候选单轮验证7Form+3adapter共10核心case。backend未freeze、现库未适配/live仍未验收。

## 前端独立定向运行通过；后端旧候选撤回

前端绑定93F4最终7文件候选，QA独立运行postscope+service仅新增核心选择，修正标题过滤后单轮10pass/6旧项skip，自然exit0。首轮仅9pass因加载失败用例标题不含“岗位/0006”被过滤，不冒首轮10绿；修正后完整10同轮通过。只有组织123夹具缺候选warning，无生产失败。两项源修及交互定向通过，不代真实DATA_SCOPE适配/服务/浏览器联调。

后端CB768候选40inputs/15argslogs/50产品独核均匹配；只读审三重窗口、实际MapperSELECT、主体/finally、资格SQL与配置/版本服务后，尚未启动QA后端编译或测试。root发现standalone资格SQL遗漏岗位成员引用pu.tenant_id过滤，CB768正式撤回；需本源0/NULL负例，私有schema无该列保持。QA立即暂停旧冻结验证、不签已安全，前端命令已结束，唯一重槽明确归还backend做窄修重封；无活动Java/PG/node重任务，无现库或live动作。待新freeze再独立编译/定向复验。

## 最终冻结的独立定向验收通过（不是现库/live验收）

后端新冻结237DB48F945863B9B377B275AEEC550C4245D821C97FF05F1A7855789D621462，40 inputs、20参数日志、50产物独hash一致，旧CB768撤回且未跑。standalone追加pu.tenant_id NULL/0、private不造该列，真实fixture包含pu99拒、NULL0允许及private无列positive，原mustfix闭合。QA独立目录`.gradle/qa-role-0006-64251dfe59`重编译全部编译清单源，自有classes/generated及classpath优先、移除旧后端roleclasses，javac自然exit0；这是局部cached javac，不是全Gradle，依赖注解警告保留。

六类必要定向单轮28/28、0skip/0fail、自然exit0，包含一个本地隔离PG14.22且正常shutdown。覆盖本源/跨schema同ID/缺表不回落/冻结删除坏CSV/pu租户负例、真实MP同selectById读vs写预读/直接UPDATE DELETE拒绝/SELF原写范围、list/count/page/detail、版本失败业务/引用/permVer同事务回滚，以及当前源主V1 Flyway首次1/validate/再跑0、主与私有模板TEXT及六码种子。初始化只在合成PG执行。

独立源审已核16 controller方法标记、GETdel等写入口无窗口，actual active readonly txn与真实MappedStatement SELECT同时要求、主体绑定/finally恢复/异步不继承、原role OR/Ignore/日志门禁/数据总开关保持；岗位交集不查作者/子孙，角色集合全量本源校验、字段省略保持/切换清、数组CSV映射及实际岗位级联用户permVer符合。前端最终93F4单轮10核心通过证据保持。本次无新增mustfix。

QA结果`result.json` SHA4925AA027D6EC01FD207DD49E3A3D7FEA5407154088ACF4FD02D9E5D2EF3DFCE，绑定后端/前端freeze及本次compile/run args/log；运行后40源输入仍全匹配。已向root/backend先交结果并归还唯一重槽，当前无活动node/javac/Java/PG重任务。

限制：未做真实JWT请求/API/浏览器联调或现库字段/字典/缓存适配，不声明运行部署已通过，不要求重启未适配服务；现库另批。Post.xml既有cascadeFrozen未排9999后代，本次只按真实变更范围推进版本，不擅改旧政策。未碰真实dev/Redis/服务/Git/产品源码，仅本卡与ignored验收产物，不重复旧全仓/视觉。

## 现库维护候选窄独审及离线fake通过，真实执行未批准

candidate-manifest A9CD50A4E41C428FEC1A60F49079D493FD02A3865DEE6D9AF6409F5DFA29F796的11文件SHA匹配。已完整读Pythonrunner/Java维护器/forward与rollback/REVIEW，确认两项精确SQL、前像与约束/触发器/RLS守卫、受限技术前像fsync后写、SERIALIZABLE单事务、后验与行数1、commit未知与cache pending状态不冒未写、不自动重试恢复。回退仅另批固定0006 deleted恢复1且默认ROLLBACK，不自动DROP列。受限技术前像不是全库备份。无mustfix。

独立`.gradle/qa-role0006-maintenance-d3463c34`局部javac exit0，自编class与候选class字节hash完全一致；Python与Java默认PLAN各exit0/0连接，独立Java单键fake exit0、fakeDelKeys1、真实Redis0/PG0/writes0，验证现CacheManager精确dictdata::v2::eva::SYSTEM::DATA_SCOPE，无scan/全清。未运行真实--execute-approved/--check-binding-approved/--evict-approved/PING，不重产品测试。

按root明确授权仅生成QA maintenance-seal.json SHA453F10428A7AB47846A714876A72603FA606C93D9D7249E04E2D886E12E19438，qaReviewedtrue但existingDbOrRedisExecutionApprovedfalse，绑定候选11文件及冻结bridge/parser/productfreeze共14；不创建批准窗口。真实ADD列、恢复固定0006和SYSTEM单键失效仍须root向用户单独确认。已交root/backend精确SHA及执行模式，当前无活动重进程，归还测试槽；无DB/缓存/应用/产品源/Git写，仅本卡和ignored审查封存产物。

## 获新明确批准的一次现库维护执行证据复核

root新批准窗口ba740637d272489eb90e2757db399d6b已执行一次exit0。QA仅读result/DB提交收据/cache结果与封存，实核result SHA398BE19D9DF4FEEE3809197B0C1FA494F548127F5C44398DEAD512BD813AE0C5，maintenance-seal453F10428A7AB47846A714876A72603FA606C93D9D7249E04E2D886E12E19438保持，14绑定文件仍全匹配。E58前像与窗口一致，单PG连接SERIALIZABLE、dbCommitted/columnAddedtrue、固定4008恢复影响1行，最终closedtrue。提交收据先记录DB_COMMITTED_CACHE_PENDING/closedfalse是提交时点，随后cache结果为SYSTEM_DATA_SCOPE_EVICTED、dictdata/v2::eva::SYSTEM::DATA_SCOPE及批准指纹一致，最终DB_AND_SYSTEM_CACHE_DONE，无自动恢复。

维护实际执行证据一致；backend另派唯一PG只读后验尚待安全结果，QA不重复连接PG/Redis、执行或跑测试。新后端重构建/runtime JWT/API/browser尚未验收，不能以维护完成替代联调通过，等待root统一运行安排。

## 唯一现库只读后验离线复核通过

backend在16:41:06.658489→.721203+08完成唯一后验，QA仅读固定脚本、6条实际SELECT清单及结果。result实核C9E1AC99DF79E67B5F79801C4F5A06E797AE8FEBE75CA10F23E56C1A2DD73711，queries实核335FDB97B28D8377D2D691CCFA1EA3B07BD457F786B0F9E2AC4238517759B556，E58前像及实际执行398BE19绑定一致。单连接/6SELECT/0写/起止eva-eva双RO on/repeatable/rollbackclose证据完整，INDEPENDENT_POST_ADAPTATION_RO_MATCHED。

新增列text nullable无default且全NULL，主项与原五条投影逐字段不变，限定查询6项恰批准后像；固定4008仅deleted1→0，其余投影不变，活动码0000/0001/0002/0003/0006/0007符合。这里的6项是限定查询投影，不扩大为全库历史明细普查。QA未重新连PG/Redis/执行或测试。

当前可确认产品代码定向QA、现库两项精确适配、SYSTEM单键维护及独立后验完成；runtimeProcessConfigurationVerified=false，新后端重构建重启、真实JWT/API/browser联调仍未覆盖。结果已交root/backend，等待root统一安排用户构建重启，不自启服务或重跑已绿项。

## 用户追加0003右侧部门树：新合并前端冻结验收

合并manifest71B9891BFF427A02F32FE83F2196032664B83633DE1B1F503FB2646E47D36B52及9文件逐SHA匹配，替代旧93F4前端签。独审新RoleDepartments及表单接线/新增断言，原TreeSelect treeCheckable默认联动与SHOW_CHILD行为保持，右侧默认开树、原0003展开回选，view同位纯文本无写控件；严格成功加载/错误重试保值/active清理忽略晚回，0003/0006互斥，部门CSV和岗位精确array及原scope语义保持，无mustfix。

QA独立postscope与aoeform两文件仅部门相关定向，单轮7pass/21旧项skip，自然exit0；包括新部门4、跨scope2及原0003仅改名称仍保组织1。仅旧夹具101/102无候选warning，非生产失败；无视觉/旧全仓/产品28重跑。已交root/frontend通过与测试槽释放，无活动node任务，未浏览器/DB/服务/产品源/Git操作。该追加改变前端冻结，原0006后端与现库适配结果仍保持；真实运行态尚未验，字典登录本地初始化/后台变更同步仍只分析合同，不包含实现授权或验收。

## RecordRow测试编译阻断窄修独验

新v3冻结BE51969D5484938BF2999BC69072D48EA3F1F2315FB4B41FB84DBDE0084B7CBA/40输入独核匹配，较v2仅PostScopePostgresTest一文件变，其余39一致。当前测试文件SHA5ED87B2E3CAB54B80185F88C5D843BED029B5B47D06FF2E9E807B4564B38080D，QA文本逆变换还原@Data/import并去两getter与Doc后精确恢复v2文件hash，字段/lambda/断言均未变。根因是项目只有main Lombok annotationProcessor、无testAnnotationProcessor，先前显式processorpath局部javac不能证明真实Gradle测试编译，本次移除fixture依赖而不扩全局配置。

QA实际运行Gradle :web-booter:compileTestJava --rerun --offline --no-daemon --no-parallel --max-workers=1 --console=plain，自然exit0/BUILD SUCCESSFUL20s，13tasks1executed12up-to-date，目标compileTestJava确实执行；日志在原QA目录gradle-fixture-compile.log。仅原unchecked/Gradle弃用提示，无mustfix。没有重28或PG，原28通过证据仅绑定v2，v3本轮只签等价源差异与实际Gradle编译，不冒新一轮测试全绿。现库398/C9E1证据及runtime未验边界不变；无DB/Redis/服务/全局processor/Git操作，无活动重进程，已归还测试槽。

## 重启后0003/0006页面选择区窄验

2026-11-01 19:52前后正式Edge localhost:8000，一次正常刷新后系统管理员会话恢复。仅新增角色空表单：0003指定部门右侧树默认展开，选中统合部主体并出现父半选，再取消/确认放弃；重新新增0006指定岗位，右侧显示总经理/有效，勾选行成功、确定按钮可用，再取消/确认放弃回原8条列表。中文数据权限六项均显示，未保存、未填写角色信息、未打开旧QA对象或授予权限。此轮通过仅覆盖右侧区域加载、选择及取消；既有角色保存回显、0006实际数据过滤、网络响应头和远程字典变更未验，沿用既有独立源码/定向测试证据，不能写成完整运行态验收。

## 用户新侧栏交互：470260最终冻结独源审及13项通过

旧5F8F候选因tagRender返回null新增类型错误撤回，最终改空fragment隐藏chips。准确冻结路径eva-web/.git/role-pane-ui-source-20261101.json，SHA470260748C5BDC7D73A2FB36F31B2E247AE311ED307DBFC10AC431E84F1A7BF3，4输入运行前逐SHA匹配、后0漂移。独源审固定同700 Modal、同Form及常驻pane，原dataScope/组织ID/岗位精确数组与dirty保护不扩权限；部门tagRender隐藏chips不改TreeSelect原父子联动。宽屏静态1060足迹+absolute右pane transform，<=1099内联下展；无OS判断或新持久化。

QA实际仅postscope.test.tsx全文件自然exit0，1file13pass/7.69s，含DOM同dialog/form/input/pane、焦点/输入/dirty、初始0003展开无chips及原部门/岗位功能。QA日志eva-web/.git/qa-role-pane-20261101.log SHA8810F8743EB439D133E5C1014A968F0E8D7B1E34EEA1A03CA7613D095140AEF4。未复跑全tsc/旧47/0006或后端2，开发scoped类型结果不冒QA全类型通过。root最新要求等待用户构建重启新CA68，故尚未开始本次新版UI真实浏览器验证；主表单坐标稳定、侧滑视觉/收回及窄屏不横溢仍待重启后窄验，不以DOM测试冒像素通过。无新角色草稿/保存或临时字典改删，无活动重进程。

## 用户重启后470260宽屏UI窄验通过

20:29前后官方Edge刷新前旧页有504提示；一次正常reload后管理员及8角色列表恢复。新增未保存草稿仅填写名称QA临时草稿不保存、备注侧栏交互验收不保存，不填编码、不点确定。0003展开、0006展开、0000收回四态通过正常可见DOM几何观测主dialog均x1059/y446.5/width700/height328，名称备注值全保留；DOM同实例由独立13项测试支持，运行页不读隐藏React状态。截图确认右pane在Modal右侧独立显示；open true的computed transform为0，收回后open false/visibility hidden/translateX -24，主form坐标无变化。未逐帧采动画，右滑方向/200ms由冻结CSS及最终两态支持。

0003选统合部主体后子勾选父半选正常，已选chips DOM计数0；0006列表总经理/有效可选，当前只有1候选，不能冒多候选现场实测（多选精确数组由定向测试覆盖）。取消后未保存确认正常，已明确放弃并回原列表，无role保存/授权。未调整用户浏览器窗口/屏幕设置，窄屏横溢尚未实际测量，仅冻结media CSS源审，不签实际窄屏验收。随后按root指令只读打开指定字典，不改标签或删除。

## 用户撤回侧滑：原左右布局恢复最终生效

470260侧滑候选及其固定位置结论已被用户明确撤回，不再代表最终行为。现冻结eva-web/.git/role-layout-restore-source-20261101.json SHA E3501D4F3F55E640275588D7663100EB6A207A3EBF73CB1682E2E9998F750572，3输入前后逐SHA匹配无漂移。本次独源审aoeform精确恢复71B9原hash59B3B8A0C6F99F7A5214BD19FA03B1C624C00E0AC100231F6FF436F7FEFF6622，恢复Modal动态1060/700与原flex左右布局；仅侧滑useStyles文件已删除且role目录无其引用。部门文件移除保留的tagRender空fragment一行后，内存重构SHA精确恢复71B9原B6B1132EEF3DC38746DDF8574335FAAFE74528264EC1256D518BA2A1711992BA，证明部门功能仅增加隐藏chips，不改原联动/回显或请求。

QA实际仅现postscope.test.tsx单轮自然exit0，1file13pass/9.68s，覆盖动态宽度切换、同dialog/form/input与值/焦点/dirty保护、初始0003无chips、原部门/岗位等；日志eva-web/.git/qa-role-layout-restore-20261101.log SHA0132FF90D8D844413352EC100CE3251F3EBDC6BDF6990E68179CB73F48166F56。无mustfix、所有进程已结束。按root最新范围未再浏览器/视觉/真实保存/后端/fixture/DB/服务/密码/全类型/旧字典47；现行为以恢复后的普通左右布局为准，不冒仍有固定位置右滑，独源审与定向测试可收尾，无新gate。
