# 独立actionKey授权QA准备

2026-11-01。当前PREPARED / NOT_RUN：用户批准无前缀独立actionKey、bootstrap resources字符串数组、Umi Access按菜单与key组合；read仅详情、list独立，后端HTTP门禁继续保留，管理metadata/对象ID/grant关系不改。本卡仅验收矩阵，未读取施工中产品源码、未运行测试，不表示候选已通过。资源按钮联动不得回退已验dataPermissionEnabled合同。

| 定向项 | 必要通过条件 |
| --- | --- |
| 字段与输入合同 | actionKey独立于URL/method/资源ID；使用已批准的无前缀key，不能靠拼接菜单名或HTTP路径生成授权。bootstrap resources仅按已定字符串数组处理，管理metadata对象不得被误识别为授权集合。 |
| 菜单隔离 | 菜单A与B使用相同key时只在对应菜单生效；一个菜单的read/create/edit/delete不泄漏给另一菜单，不能退化为全局key集合。菜单未获授权不因其key存在而放行。 |
| 未知与关闭 | 成功bootstrap的严格资源开关决定分支，缺失/失败/非法能力与资源形状按合同fail closed，不从旧缓存或metadata补授权。资源开关false保已批准绕过资源动作限制的既有行为，但不改变认证/业务/租户门禁，也不把dataPermissionEnabled改为false或true。 |
| 列表与详情 | list和read独立；仅list不开放详情，read不自动授list。全部sys/log详情按钮、行双击及其它等价详情入口使用同一菜单+read判据；只隐藏按钮不能放过双击。 |
| 写动作与后端HTTP | create/edit共享API仍分别按前端actionKey控制入口；不得将前端分离称为后端已隔离create/edit。后端原HTTP资源检查、权限版本及业务写保护继续有效，未因key开放通用跳过入口。 |
| 管理与授权保存 | 资源管理仍返回原metadata对象与稳定ID；角色授权树回显及保存继续使用准确资源ID，不将actionKey或bootstrap字符串数组当授权ID，未勾选metadata不授予访问。 |
| 身份与租户 | bootstrap只反映可信主体和当前租户实际授权；旧用户/旧租户/旧请求迟到不恢复新会话权限，未知失败不沿旧缓存放行。租户上限与有效grant交集保持，客户端key/ID不能扩授权。 |
| 原dataflag回归 | dataPermissionEnabled独立严格boolean、false角色新增ALL空关联/编辑原三配置保持、前端false隐藏并省略五字段、unknown禁存及异步校验能力变化停止旧提交不回退。仅必要相邻回归，不重旧大全。 |

后续顺序：backend先持重槽、frontend后、QA最后；只有各责任成员正式freeze与归槽后，QA才逐SHA独源审并运行最小定向测试。若资源形状/菜单归属尚未落定，以正式合同为准，不在准备阶段自行扩接口或数据模型。

本轮不写现库、不建测试账号/真实业务fixture、不重启服务、不收密码、不发认证探针、不提交推送。按钮/双击覆盖优先源码调用清单与必要组件用例，不自动新增全仓视觉或真实保存验收关卡。

## 后端最终78候选：源审已核，独测待前端归槽

冻结.gradle/action-key-20261101/source-freeze.json SHA94BB4C5202E22185850613854B3459A1AFB8755EDEECD75CB8A9A4F5796FEF72，15输入/20相关真实Gradle产物/7日志XML逐SHA独核全匹配；现库提案hash5BE78620236B40C7B402495FA45F5D8AF44C1435A87EC877B15B0E8760191C17匹配，前dataflag B4B27七输入0漂移。

源审独立nullable ACTION_KEY与格式校验、BO presence区分省略与显式NULL/空、entity ALWAYS更新NULL；菜单按实际已授资源每menu输出有序去重非空字符串key，管理及rolegrant metadata仍保ID/code/url/type。AuthorizedMenu SQL仅增投影，原resourceIds/有效状态/租户上限或standalone归属WHERE不变。新V1固定78行、72非空key/6辅助NULL、13compound GET归已定发起menu/action；旧wide与MENU仍NULL、新装admin沿既有all-resource种子。不由共享POSTedit/GET回填反推后端能区分UI目的，不复制种子或授权到现库。

暂未发现必须修正问题；开发单元7项与专用PG最终78的一项不冒QA独测。旧65候选已替代；旧两个PG因历史RUN绑定未进业务，不选、不放宽、不计回归通过。测试槽现归frontend，QA仅源审，无Gradle/DB/服务/浏览器操作。待前端最终freeze归槽后仅ModuleServiceTest、AuthMenuCapabilityTest、ActionKeyPostgresTest（三类8项/单隔离PG）及必要前端目标。现库尚未适配，不开展新列现场读取或部署。

## 现库一次只读盘点：QA仅离线核验

结果.gradle/native-self-bridge-5063fc429e32499482a6608b5d57b25a/action-key-inventory-result.json SHA FC8C4FF6B95CF12BCAA93064490DDA12DA7DC18CBCAEC5580F59A7F413B0AF87，查询清单SHA8D1E991100B75A0CDB94F0BF96E3D13E8A4C03F540ABEF12BEF20FEF31F906C5独核匹配，内部绑定一致。仅离线读取脚本/清单/结果，不执行脚本或再查库。脚本限定approved dev/source-build standalone、单连接、只读事务RR及参数，不触应用凭据；清单4SELECT仅起止session、资源列及资源技术投影/引用count。结果22:16:34.931084至.986329+08，起止eva/eva双RO on/RR，0写/rollback/closed；运行进程覆盖配置未证边界保持。

实证ACTION_KEY列缺失、78物理/77有效。更新后提案docs/legion/design/2026-11-01-action-key-existing-db-adaptation-proposal.md SHA BD099ECEE5CB35EA272BE7F1860CAFF9FC2EAADC79DAE7BCDCD162E5B996636C替代94BB中盘点前文档hash，非产品源冻结变更。QA离线逐行比对36映射候选的ID/main/code/path/method及引用全0无差异，另42保NULL，78行唯一完整覆盖；新V1的60001至60078完整新ID范围现库均不存在。旧广域资源引用为2角色/1租户，不推断具体身份或grant ID。

仅加列及36无引用key不会让当前旧广域授权主体自动获得前端动作；共享edit/fetchResource不能一行塞多个key，旧wide不推CRUD。若实际部署需要现有角色等价动作，须按明确资源及角色/租户引用清单另审适配，不重放V1或擅自补grant。当前无现库写/新授权，QA不代拟或执行迁移，候选代码独验与现库上线适配分开结论。

## 独立测试及最终跨层修正待验

后端94BB实际Gradle仅ModuleServiceTest、AuthMenuCapabilityTest及ActionKeyPostgresTest，每test任务--rerun/offline/no-daemon/no-parallel/maxworkers1，exit0/BUILD SUCCESSFUL54s，32tasks2executed30up-to-date；XML5+2+1共8/0fail/error/skip，15输入后0漂移。结果.gradle/qa-action-key-20261101/result.json SHA85321FEECC4645B42B9CC75D9BCB51D4BC592661FEB907D75AAD2F078607FD00。随后designer发现无ID/CODE新资源同HTTP不同actionKey被fallback去重合并，94BB不再签最终通过；开发最小修ModuleService及新unit/专用PG后待新freeze，旧8项仅历史。QA已明确结束自有后端进程交源锁，完成前端后归重槽backend，不并测、不重旧8全类。

前端最终.git/action-key-source-20261101.json SHA71C83218A0C7BA15E2798CAD54EE0C8FAEA915620C90CBC5BEBC7953A1413774，74输入前后逐SHA匹配0漂移。独源审Access按唯一菜单id/严格资源字符串数组显式key；metadata对象不能授予，false只放行已有对应菜单且不改业务门禁，unknown拒。useMenuAccess按user稳定callback+latest能力/加载错误/同主体与mounted setup true-cleanup false，旧身份或卸载后回调无能力。sys/log详情按钮与双击复用read守卫，update回填不借read；嵌套role/read不阻勾选。固定fixture仅非权限旧表单用按menu稳定显式替身，核心/组织/role预览仍走真实helper；不为替身修改生产memo。

QA实际Vitest核心6文件（resourcePermission/useMenuAccess/login permissions/account roleAccess/module resources/organization permissions）41通过/16.60s；相邻role/postscope、rolemodule及login/index按“能力|编码校验|实际授权展示|list-only|详情先显示”过滤15通过/25skip/14.03s，两轮自然exit0，共56不同通过。覆盖菜单隔离、metadata拒、未知/关闭、真实组织撤权与卸载、update不需read、角色预览撤权、resource ID保存、dataflag false/unknown/异步竞态及日志list/read。结果eva-web/.git/qa-action-key-result-20261101.json SHA84075777EC1FFD93E1C07E8A3BEC8021FB77901F8F22D071561468F31E22AAE0，核心logSHA72EC4700DC219C01C96A7FA26FCEB322EDDDF1625043CFAF889AEED7FA1E372D、相邻logSHA11FE24EDDF6454D32AFEC09E8121BDD3A4337EEF148FDDEA8A5A7A8D0F411086。未全247/旧137/47、未重build/fulltsc，开发build0仅采其证据；保原5+生成可移植性2共7类型诊断，不冒全类型绿。前端独验无mustfix，后端修后仍待独验delta。

## 精确迁移包仅静态复核

review-package.json SHA89EA9584FFC5E41F18C101932DE141DA59EE5FA40DD75FD1CD888AB6CBA891CA绑定forward3DF0938D5D23B66A0D3CFFAF4073B9B2B0148B3AF4922B5ACFCD3F7301615F46、rollbackF1F43AFD4275891607AB86ED1D209DCF6AA07D85D6B1F1E52A30F88488B3AD12及plan528E47030E71F5C9CE9BF9449C4CDBE04EDCF0BA102FB352A999E2FCE24A96DF，全部SHA独核一致。离线plan交叉原技术投影：65target同MAIN、method/path被保留旧源wide覆盖，13辅助无新ref、同MAIN/key primary在65且真实GET由原保留源覆盖，43不同menu/key、新tenantrefs0相符。候选限定1列/36旧key/78新资源/role4新增65refs(23→88)，tenant1原角色/23ceilings保留。SQL固定事务锁、漂移/源tenant0前置、DDL及精确后像检查，rollback默认ROLLBACK保列；未盘点source tenant字段仍由执行前guard确认，连接approveddev由执行器实核，不伪造已证。此为可审静态包，不代用户审批，不执行SQL/查库或授予权限。

## D46最终修正独验通过，代码可收尾

最终后端.gradle/action-key-20261101/source-freeze-v2.json SHA D46BE3710B1299994F31C835E337484841BF78AE4564079EDD01F444AE70566D；相对94BB精确ModuleService/其test/专用PG三变化，其余12输入不变，15输入前后0漂移，20产物及绑定日志XML独hash匹配。无ID/CODE新行fallback加入规范非空actionKey维度，同HTTP create/update不再合并；重复trim后同key仍去重，NULL/空key保旧fallback、ID/CODE优先保持。无CODE生成器或额外唯一约束：原CODE可空无默认、转换/审计不补CODE，真实Mapper双NULL CODE同main/HTTP插入已验证。

QA实际Gradle仅:eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.module.service.ModuleServiceTest.keepsDifferentActionKeysForCodeLessNewRowsAndDeduplicatesTheSameKey :web-booter:test --rerun --tests org.pkaq.sys.module.ActionKeyPostgresTest --offline --no-daemon --no-parallel --max-workers=1 --console=plain。自然exit0/BUILD SUCCESSFUL41s，32tasks2executed30up-to-date；XML各1/0fail/error/skip，单自有隔离PG，不选旧RUN门禁。结果.gradle/qa-action-key-delta-20261101/result.json SHA0CBE07BC6C35DCA2F0A3788E3E2DE151E2A6EC430E49CCA5D32975900F86F00D；日志SHA91088B61C58036F15D38FA2B01AA3AD902C1537D13245E89A9F297743C0B5C89。原94BB的8通过与本轮2变化目标分别绑定，不冒单轮10或所有旧PG通过。前端71C832独56通过及类型7诊断边界保持，未重前端测试/build。

无未解决mustfix，代码独验可收尾，源码停止修改、所有QA自有进程已自然结束、重槽归还root。现库适配/服务重启仍由root待具体批准处理，不能因代码通过称部署完成；QA无新DB/服务/浏览器/密码/发布/关机操作，不新增验收gate。

## 固定执行器仅静态及fake验收

.gradle/action-key-20261101/execute-approved.py SHA33C98A274211BC21CDF728D96CFA044DA9B726AD1BB81EAF141008F315F1EF1D独核匹配。只运行默认PLAN与--self-test，分别输出PLAN_ONLY connections0/writes0和FAKE_PASS_COMMIT_ROLLBACK_UNKNOWN_NO_DATABASE，exit0，无真实连接/输入。源码核D46全部输入/B4七输入、89EA及SQL/plan文件、FC8C/1FE前像与sealed parser/ACL绑定；固定approved dev/source-build standalone、32hex排它window文件不复用。去SQL外层BEGIN/COMMIT后由SERIALIZABLE连接单次提交，保已审SQL全部前后guard；SQL异常回滚关闭，commit异常UNKNOWN/null停止不重试；关闭或结果落盘失败保留已知提交状态，不输出异常原文/凭据，不自动缓存或服务操作。

无执行器mustfix，可交root待具体用户批准后单次调度；本任务消息/静态通过不等于数据库写授权，当前审批仍pending，QA从未使用--execute-approved，不代批准或真实执行。产品测试不再追加。

## 2026-11-02 已批准提交后的独立只读后验

root转达用户具体DB及服务两项批准，backend唯一窗口已知提交结果SHA96CCC674E49608C1796E2EE67833744DE938438DBCD7D231807C7AA4CAC2D57D且dbCommitted=true。QA实核此结果后，按root一次独立RO授权执行自有postcheck.py（SHA DEB66C624B570FE325AAC74464F348AE2351DCA5056C4F431A34F5E4FD649F17），无重试/额外连接。六SELECT为起止session、列定义、资源完整技术投影（含描述）、有效role引用、tenant引用；实际单连接双只读on/RR、timeout5000、writes0/rollbackclosed true，未读账号PII/认证或操作缓存/服务。

结果.gradle/native-self-bridge-778ac4d2557b4c07b65111bbd9d6a832/qa-action-key-postcheck-result.json SHA E61D09487579DF6B03F911F63BEA6392D61E5219527D5524E5DF3F3EBCA1FB12，查询清单绑定SHA5C3DF1317A2E28B6324550F04BE40EF57153B698A1BD357837D3880E5054A272。ACTION_KEY nullable varchar64/defaultNULL、36旧key及原78技术投影、78新资源完整后像及refcounts全匹配；物理156，原46+65有效role引用精确相等，role4为88，排除角色仍23、tenantceilings仍23且全投影相同；原HTTP集合保留/新65被原覆盖核符。status ACTION_KEY_INDEPENDENT_READONLY_POSTCHECK_MATCHED，实际exit0。该独立后验窗口已用完，QA不再查库。

本节替代前文审批pending/现库未适配历史状态，仅确认已知提交及独立数据库后验；服务重启仍由root按批准操作，不冒重启或现场按钮已验证。无产品源修改/测试/浏览器/密码/Git/关机操作。

## root服务重启与现场验收边界

2026-11-02 root报告已通过VSCode现有Run Restart完成服务重启，9016进程PID46744变为39228，并核defaultdev/source flyway=false，后端源码未改；本角色未操作服务，不将root报告冒作QA独立重启验证。root随后尝试现有Edge管理员页面reload，官方连接器明确因另一扩展UI占用而硬阻止，已停浏览器且未改native/私有token绕过。页面动作授权运行态尚未验收，不能称全部运行通过；数据库独立后验结论保持。root等待用户解除扩展或明确暂缓页面验收，用户未答前未关机。本角色仅更新自有卡，不新增数据库连接、测试、服务或浏览器动作；无凭据HTTP门禁探针由root独立记录，不冒本角色实测。
