# CUSTOM standalone 局部修复独立安全QA

日期2026-10-24；quality_reviewer。代码与隔离回归通过，运行加载/一次真实保存另留证；不据119合成测试称包S完成或正租户已验。

## 审查范围与安全结论

已完整读designer ownership设计、backend implementation交付/status、实际PG只读七对象诊断、批准S边界、RoleService当前代码和HEAD diff（区分既有scope/冻结变更）、新CustomScopeJdbcFixture/RoleCustomScopeStandaloneTest、旧RoleService测试及相关调用/模式配置/事务路由。未发现本次standalone增量必须修复项。只改局部校验及末位JdbcTemplate依赖，不写DB归属0、不改MetaObjectHandler/Mapper全局插件、API或授权链。

兼容仅在server standalone/singleton、tenant关闭、可信userId>0/tenant0、合法配置schema、TenantContext不存在或完整同tenant0/schema时启用；未知/混合模式和部分/矛盾上下文拒绝，未清理冲突后放行。实际Spring事务与JdbcTemplate DataSource绑定门禁在catalog查询前；静态to_regclass参数sys_organization，current_schema和真实解析namespace必须均等服务端配置schema，第三schema、公有回退、缺表/NULL、临时同名表namespace不同及异常均不能进入组织Mapper/角色写。该SQL只读catalog，不裸读组织业务数据或SET search_path寻表。

原真实OrganizationMapper参数化ID IN与逻辑未删/data插件保留；NULL或0严格成AND内括号，不越过ID/deleted，可见ID集合必须与全部提交集合相等。standalone重复/非法/负/溢出/空段拒绝；positive platform/saas保原tenant相等条件，不给NULL跨租户含义。原scope拒省略/4/5/6、编辑遗漏scope/orgIds保留、只读冻结/permVer及失败rollback不退化，OpenAPI窄读取窗口与本分支无互相绕过。

同Connection证据来自真实Spring事务+SqlSessionTemplate/MyBatis SpringManagedTransaction链的隔离fixture：schema与组织每次prepareStatement都断言DataSource的ConnectionHolder为同一mock Connection；当前生产调用沿默认单DataSource配置，未新增路由/事务管理器。本轮未观察运行Bean/真实PG事务，因此不扩大为已验证任意多DataSource装配或当前HTTP运行连接。

48新用例的JDBC替身解析插件完成后的真实SQL AST和实际绑定参数，再在合成组织行上执行AND/OR/IN/NULL/删除/作者谓词；不支持AST即失败，不直接mock visibleIds或照条件重新拼预期SQL。独立额外99行能捕获OR绕ID，正tenant/删除/隐藏混入要求全拒；原MapStruct转换真实，角色写入/关联依赖为mock。catalog回读为模拟值，未声称建立第三schema/临时表/真实PG来复现。

## 独立快照与执行结果

先逐行审.gradle/custom-scope-current-20261024.gradle和snapshot脚本；不执行重建快照脚本、不覆盖证据。独立核十模块当前输出与副本所有492文件，465类/27资源、清单无缺失或额外副本；每个SHA256一致，27资源另与src/main/resources逐字节一致。manifest SHA A5853E84F152E9C952E60217C01BC2F44BC4B89CC4685A28149ED856467DDEFE。测试后再次核全部492当前输出/副本一致。init每Test.doFirst也输出492已核；它不改变源码或连接真实库。

精确执行均cwd本EVA-API、offline/no-daemon/plain/-x jar，强制对应test --rerun：

1. sys-service:test选择RoleCustomScopeStandaloneTest、RoleServiceTest、RoleFrozenMappingTest、AccountTenantFrozenMappingTest、TreeFrozenMappingTest、TreeFrozenEditTest、WriteProtectionServiceTest；BUILD SUCCESSFUL21s，12 tasks中仅test1执行/11 up-to-date。
2. auth:test选择OpenApiAudit*Test；data-mybatis:test选择OpenApiAuditQueryPermissionTest、MybatisPlusDataPermissionHandlerTest、LogQueryPermissionInterceptorTest，各test独立--rerun，--max-workers=2；BUILD SUCCESSFUL44s，13tasks4执行/9 up-to-date（含contract/common两个compileJava重新执行，但全部产物哈希仍与原快照一致，非新增源码或热替换）。

本次三个模块当前XML共13类119项，failures/errors/skipped均0；SYS76=新48+原28，AUTH29，DATA14，不能累加开发旧XML或其他119/116历史。SYS XML UTC05:39:41.350–05:39:47.745（北京时间13:39），AUTH/DATA UTC05:41:10.038–05:41:16.091（北京时间13:41）。13个本次XML独立保存到ignored新目录.gradle/quality-reviewer-custom-scope-20261024-a/{模块名}，未覆盖backend旧final XML副本。

实际本次XML SQL：SELECT id FROM sys_organization WHERE deleted=0 AND (id IN (?,?) AND (tenant_id IS NULL OR tenant_id=?)) AND 1=1，参数12/13/0；creator受限由原插件加条件，全集失败和成功/无写入/版本/事务断言通过。只证明该真实代码链在合成JDBC环境行为，不代实际生产/开发PG提交结果。Git精确diffcheck无内容错（已有LF/CRLF提示）、index空，未commit/push。

## 数据保护、加载与下一步

已读backend指定七ID真实READ ONLY快照：四组织物理pid/PATH正确，七行deleted0/frozen0/tenant_idNULL，三角色data_org_ids为NULL，SYS_ROLERES_REF/SYS_ROLEUSER_REF完整集合均空；不是QA本轮新开DB复验。CUSTOM全状态0为13:08时间点证据，真正再提交前仍须UI即时查重。不得为过校验将这七行填0、删重建/冻或补权限。

RoleService.class当前SHA 80DFFA86998E324D80068B20F9588D71D5C35B8A849DA0530E86441E5C01E30C，编译北京时间13:29:47；原PID47664启动10:08，不能据旧进程读取新磁盘类就称已加载。原最小一致加载方案为用户按当前完整IDE/WebBooter的classes/resources入口正常重启相关产物，不用测试init/snapshot作为运行classloader、不单类hotswap或混旧JAR。QA不启停服务。

独立复验中总控转交用户正在重启、约2分钟后继续的信号；13:41后首次监听核为未就绪，故当时未触UI保存，继续等待。下一仅在新实例启动/实际入口和产物加载边界可靠且页面正常后，以原CUSTOM候选/原D2即时查重、scope0003及唯一D2/popupfalse回读，再一次保存/列表详情核scope-orgIds；出新错先停不重提，不建ALL/账号/样本或授资源。实际后续结果追加，不把准备/启动信号当保存已通过。

正tenant V1缺TENANT_ID的独立兼容问题与包T仍未验；新统一view布局/四审计字段规则也未在本修复实现。此轮无真实PG/HTTP/Redis/UI写、秘密读取、源/依赖/服务/既有七对象或授权变更、Git发布；只写自己的QA证据/矩阵及允许的隔离测试输出。

## 用户正常重启后运行加载与原CUSTOM一次保存复验

前节无UI写为代码阶段历史。用户经总控明确重启并再次“可以了”后，等待原约2分钟窗口已过，9016新PID39300、启动UTC05:43:51.4180022Z（北京时间13:43:51），晚于当前RoleService.class UTC05:29:47。仅内部白名单解析jcmd VM.system_properties；首次直接比较JavaProperties转义路径出现false，按Java路径转义解码后校正，不把解析问题当服务故障。未输出完整属性/命令行/环境/认证。实际user.dir为本工作区，classpath含十模块当前classes及WebBooter当前classes、无test snapshot；按实际顺序找第一个RoleService提供者就是当前输出，hash与本次已验快照一致。该证据为启动/磁盘classpath首解析，非运行内存dump或所有Bean装配证明；随后真实页面保存补行为证据。

fresh现有管理员角色页正常；先完整code ROLE_QA_R_CUSTOM_20261023即时查询最终暂无数据（首次短wait早于异步回读超时，随后AX确认空，未重复查询）。原name/code、scope指定部门0003，仅展开P并勾新D2，Escape收起后DOM popupfalse/tag仅QA_D2_20261023，确定Enter仅一次；loading后表单关闭，列表/详情真实回读成功：ID2113870080799694849、规范化code、scope0003、组织关联仅2113856917966462977、未锁定/备注—，截图27。无网络响应监听，不伪造HTTP200/错误码或实际请求数；本次可关闭“CUSTOM不能保存”的现场阻塞，旧失败全部原因仍不补造请求证据。

随后按总控明确批准同S0创建ALL独立空关联角色一次，ID2113870419179364353、scope0000/组织关联—/未锁定；未授予任何现有或新用户、未操作资源授权。原七对象与NULL归属不修改；新两角色完整空资源/用户集合待总控限定只读补核。当前四组织五角色实际9对象，账号0/样本0/grant0；账号仅进入原PEER候选+D1/角色全未选的本地接管准备，密码/联系方式均未读取/填写/截图。S0未完、包T与新view规则仍未验；UI已markHandoff交本地用户，后续不能在用户填秘密时自动继续取证。
