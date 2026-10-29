# 账号部门回填：live Mapper 注册差异与局部修复建议

2026-10-26，technical_designer；只读评估，未授权实现或数据操作。

**当前状态：用户已于2026-10-27明确选择C，批准范围和实施验收门槛见第9节；已选择不等于已修复。第1–8节保留前期评估与选择前状态，不再重复询问同一选择。**

## 1. 结论

推荐 **B：唯一命名的内部专用详情语句 selectDetailById，仅替换当前已授权详情读取链的一个调用点**。保持原 HTTP、UserDetailVo、账号/档案表及数据不变；继续账号 LEFT JOIN 可选档案，继续非 Ignore 数据权限及原六表可信来源检查。不要全局覆盖 BaseMapper.selectById，不修改前端把 NULL 猜成部门，不回填数据库。

本建议不是实现授权：当前生产源码继续冻结，需总控确认选择、划定源码/测试锁后实施。本建议无外部契约或归属政策变化；若实际修法必须改变公共 API、权限、注册/档案业务或全局 Mapper 注册行为，则按具体差异另列选项，不以本建议代替用户决定。

## 2. 当前证据与根因层次

总控交接的同一 debug PID25764 live 元数据表明：PEER ID2113894345662021633，UserService.getUser处内部user.id与vo.id相同，而user.deptId/vo.deptId均NULL；实际 SqlSessionFactory 的 `UserMapper.selectById` MappedStatement.resource 为 `org/pkaq/sys/user/mapper/UserMapper.java (best guess)`，BoundSql为 MyBatis-Plus生成的仅SYS_ACCOUNT SELECT，未JOIN PROFILE、未投影DEPT_ID。临时断点/watches已清0、线程恢复，仅元数据读取未执行SQL。本角色没有重新连接debug/UI/DB，以上运行事实以总控记录为证。

本轮读磁盘当前 `User.xml`：namespace正确，BaseResultMap显式DEPT_ID→deptId，ManagedProjection为U.*加P.CODE/P.NAME/P.DEPT_ID，LEFT JOIN SYS_ACCOUNT_PROFILE；同名selectById引用该投影。UserMapper接口确实override继承方法；UserEntity的deptId/code/name为exist=false联合投影字段，MP基础单表SELECT不会带它们。UserConvert正常映射deptId，不能靠转换器或前端String化恢复上游没有读到的数据。

因此已经成立的是 **live实际statement与管理详情预期不一致**。磁盘XML存在、class/resource唯一不能证明该XML已在该factory注册，不能确定仅凭上述证据就是某一个扫描配置错误、注册先后或MP覆盖机制。A的具体机制仍由backend只读定位；不在缺证时直接改全局配置/注册顺序，也不把“best guess”字面等同于SQL异常。

此前DB已证PEER档案DEPT_ID为D1并非NULL，旧只读附件 `2026-10-24-package-s-peer-department-readonly.md` 是该时间点证据，不由本轮再查；这里不依靠新SQL推断数据库已变化。运行部门NULL可由已核live投影缺失解释，但最终新语句生效及页面回填仍须实际验收。

## 3. 原业务契约与当前调用影响

已读取 `docs/ACCOUNT_PROFILE_AND_REGISTRATION.md`、TODO第9节、当前UserMapper/User.xml/UserEntity/UserConvert及已授角色实现。原账号拆表契约明确：SYS_ACCOUNT为身份与认证主表；SYS_ACCOUNT_PROFILE是可选工号/姓名/部门资料，以ACCOUNT_ID关联；管理端组合视图保持现接口，注册账号可无profile。LEFT JOIN必须保留，不能改INNER JOIN、要求所有账号都有部门、补造档案或归属。

现详情链为 `GET /sys/account/get/{id}` → UserService.getUser → UserRoleRefSerivce.readUserRoles：先requireDetailSource，再非Ignore UserMapper.selectById取得当前操作者可见账号，随后一次roleIds、非Ignore RoleMapper.selectMaps摘要；UserService转换并在原事务直接读postUserMapper。保留这一安全次序，不改为Ignore的getUserWithModuleAndRoleById，也不恢复嵌套岗位service路由。

UserMapper.selectById其他已读调用包含：UserService.repwd、saveUser编辑前原账号、ensureSystemAdminCodeAvailable；UserRoleRefSerivce写授权的ensureUserExists；UserPostRefSerivce写岗位的ensureUserExists，以及继承/其他间接BaseMapper消费者。部分调用涉及账号code/只读保护与密码，不能因本次仅需部门回填就更换全部消费者或宣布它们未受影响。B只隔离本次详情调用，**不声称修复所有历史BaseMapper/XML差异**；其他调用机制风险留给A的独立影响审查。

## 4. A/B对比与选择条件

| 方案 | 可解决范围 | 风险与当前选择 |
| --- | --- | --- |
| A 修XML加载/注册 | 若证明生产factory未按预期解析User.xml或发生覆盖，可从注册源头恢复原自定义语句；也可能涉及其他Mapper | 先需要同一factory启动顺序、加载标记/statement全集、配置生效来源与冲突证据。修改mapper-locations、Factory、自定义注入/覆盖策略可能同时改变全库SQL/插件/ResultMap和写链，当前冻结下不直接实施。不能热删/替换live statement后报正式修复 |
| B 内部专用selectDetailById | 只保证本次授权详情联合投影，在普通生产注册顺序下避免与BaseMapper内置selectById碰名 | 推荐。一个新内部Mapper方法、唯一显式语句/结果映射、readUserRoles一个读取调用点和必要测试；无新HTTP/VO/DB、不接新tenant参数，不扩大Ignore/权限 |

B若仅在同一个尚未证明加载的XML里换id，仍可能没有有效statement；它可能从静默单表降级变成BindingException，并不能据磁盘新增判成功。推荐在UserMapper为唯一新方法声明 **显式注解SQL与自有结果映射**，不依赖User.xml先被手工解析、不复用未证注册的common/BaseResultMap；或者采用其他有正常bootstrap注册证据的唯一显式语句方式。两者只能选一个定义，禁止XML和注解同id双定义。此为局部实现建议，不要求新增全局配置或加载器。

新语句使用参数化id，保留原ManagedProjection的业务字段与LEFT JOIN、账号逻辑未删条件；不额外过滤profile不存在（profile本来无独立deleted/frozen/tenant列）。结果映射显式保证账号ID及CODE/NAME/DEPT_ID，继承账号字段、冻结枚举/时间等遵守现类型处理器，无需更换VO/Convert。U.ID作为主体身份，不误映射P.ACCOUNT_ID，code/name仅档案来源，profile缺失允许NULL。不要复制任何密码/hash到摘要、日志或测试输出；原内部实体到安全VO的脱敏边界保持。

实现建议范围：UserMapper新增一个内部方法+唯一注解映射、UserRoleRefSerivce.readUserRoles将本处selectById换成selectDetailById、定向测试；User.xml原selectById和其余调用保持以控制变更范围。UserService.getUser/前端/DTO无须改；若实现者发现不能维持这些边界，先向总控报具体原因。

## 5. 安全不变量

必须先原可信身份/模式、同原只读事务连接和六表实际namespace核源，再执行新详情语句。新Mapper方法没有@Ignore，类也不能加Ignore；statement名字变化须核实际数据插件配置是否命中豁免，不能仅因方法没注解就假定插件必生效。当前账号数据条件作用U，profile内部表已有既定JOIN处理保持，不全局修改数据插件排除表。

目标账号不存在、逻辑删除或当前操作者范围不可见时仍失败，不查其ref/role/post；纯注册无profile的可见账号仍可详情，deptId/name/code可NULL。角色摘要仍按真实roleIds与当前操作者可见未删角色交集、冻结/只读可展示、原roleIds完整、raw Map保NULL/code原值；不能为修部门切到认证快照/模块加载或ALL。

standalone唯一schema、正tenant可信core映射及account/profile/ref/role/org/postref同源检查全部保持；不硬编码eva、不加tenant0/ORNULL行级兼容、不收客户端schema、不转core读部门、不重查跨schema目标。保持岗位原事务直接Mapper读，避免已核源后嵌套路由；本次不借机改全局resolver或表命名。已读最新_line/_ref权威规则，不授权改SYS_ACCOUNT_PROFILE或任何存量表名。

## 6. 验证门槛：注册生效与SQL安全均需证据

此前177结果是既有定向回归，不是本次live statement已生效的证据。当前AssignedRoleDetailJdbcFixture明确先用XMLMapperBuilder解析common/User/UserRoleRef，再addMapper/建factory；原Postgres Mapper专项也手工解析XML。它们可证明特定构造下SQL/插件语义，不覆盖当前生产启动注册路径，不能单靠复跑它们就关闭本故障。

实施后的必要验证分层如下，具体执行由总控另派，不由本设计角色运行：

| 层次 | 必要证据 |
| --- | --- |
| 正常注册路径 | 使用真实生产MyBatis/Spring配置与版本的受控启动测试，禁止为了让测试通过先手工parse User.xml；确认新完整statement id唯一、resource为预期显式定义，原selectById未被覆盖。可用隔离替身DataSource，禁止为取statement元数据意外连接开发/生产库 |
| BoundSql与映射 | 新statement BoundSql确含SYS_ACCOUNT LEFT JOIN SYS_ACCOUNT_PROFILE、ACCOUNT_ID关联、DEPT_ID投影、参数化ID与逻辑删除条件；结果映射DEPT_ID→deptId。并记录实际数据插件最终SQL/绑定，而非只检查注解字符串。原selectById等已冻结语句元数据前后不变 |
| 行与权限 | 可见有profile返回真实部门/code/name；可见无profile账号仍返回且deptIdNULL；不存在/已删/范围隐藏拒绝，后续ref/role/post不执行；长ID无损，profile不会改变账号id |
| 插件/租户 | 0007/部门/ALL及数据开关维持原语义；新statement非Ignore、账号谓词确实加到U；六表同源、缺表core回退/第三schema/正tenant同ID/冲突context失败，原岗位无二次路由与raw角色摘要回归保持 |
| 更新安全 | 新读不得触发profile创建/更新、账号/角色/岗位关系或permVer写；原编辑/改密/授权调用未切换方法，不把fixture先parse造成原语句变化冒充未回归 |
| 受控运行加载 | 总控管理新产物加载/允许的调试方式，不由成员重启；新实际SqlSessionFactory中检查新statement resource/BoundSql/结果映射，确认详情链调用新方法；清临时断点/watches、恢复线程并记录结束，不输出用户全对象、密码或响应正文 |
| 真实QA | 在原获准PEER详情中回读deptId=D1并由页面合法组织选项显示D1；只读账号/纯注册账号缺资料显示规则、role/post/原编辑行为回归。真实请求/UI与后端元数据分别留证；不通过前端硬编码/候选替换伪造回填 |

若B新statement在正常启动路径仍未注册，必须停止并把机制证据交总控，不通过动态addMappedStatement/热覆盖、修改live Configuration或临时Ignore“修通”。A如后续证明确属必须全局改，另列具体配置、受影响statement清单与外契约差异，再申请对应范围，不沿用B的局部授权扩大。

## 7. 本轮状态

已核当前cornerstone、HEAD 1f708e20、相对origin ahead4；工作区仅现有两QA状态脏及未跟踪.vscode/GO_REFACTOR_PLAN，均未改。已读取最新仓库规则、AIBrain项目profile/表命名/军团规则、TODO拆表任务及当前设计/实现；相关注册差异按现源码与总控live证据判定，不把历史记忆或旧mock结果当当前事实。

本轮只写本设计及本人status。未改生产/测试源或依赖、无构建/测试、DB/SQL/HTTP/秘密/日志正文、debug/UI/服务、Git暂存提交推送、其他角色卡或vault写入。生产源码仍冻结，A具体初始化机制待backend只读报告；本设计交付推荐与验收要求，未实施且无恢复成功结论。

## 8. 新机制证据后的 A/B/C 最终比较（只读补充）

### 8.1 机制证据与仍待证明处

总控新转backend已安装源码链证据：classpath资源User index170、common index189；User.BaseResultMap跨namespace继承common.resultMap，父map晚到导致结果映射/依赖语句pending；XML bindMapperForNamespace先addMapper，MP注入时同id XML语句尚未完成注册，生成selectById先占位；common补齐后MybatisConfiguration同id分支error+return保留先到生成语句。它与live仅单表select及磁盘联合XML差异吻合，**但具体该进程初始化时序尚未重放**。本角色仅读代码/转交证据，不声称复现启动竞争。

该机制把原A笼统“全局加载修复”收窄为可测试的局部依赖问题，因此初步B不再是唯一推荐。不能据机制猜测跳过结果映射逐字段/handler保持或真实反序注册测试。

### 8.2 已实际读取的父映射与C最小形式

真实父map位于 `eva-core/eva-core-data-mybatis/src/main/resources/mapper/common.xml`，namespace=common、type=StdEntity。它没有显式javaType/jdbcType/typeHandler、嵌套查询、构造器映射或discriminator，仅以下13项。User.BaseResultMap增加code/name/deptId，id覆盖父同名映射，合并后16个唯一属性。

| 属性 | 原列 | C保留要求 |
| --- | --- | --- |
| id | id（User本地为ID） | 保账号U.ID、Long及主键标记，不取profile主键替代 |
| revision | revision | 保整数版本，不归零或遗漏 |
| deleted | deleted | 保原逻辑删除字段/实体类型，不改SQL过滤 |
| frozen | frozen | 保FrozenEnumm类型及现Composite/UniversalEnumTypeHandler解析，0/1/9999与NULL行为不变 |
| sort | sort | 保Double映射 |
| tenantId | tenant_id | 保原属性映射；物理结果无该列时不造值，不新增列或填0 |
| createId / createBy | create_id / create_by | 保Long/文本审计映射 |
| utcCreate | utc_create | 保LocalDateTime及当前handler |
| modifyId / modifyBy | modify_id / modify_by | 保Long/文本审计映射 |
| utcModify | utc_modify | 保LocalDateTime及当前handler |
| remark | remark | 保备注映射 |
| code / name / deptId | CODE / NAME / DEPT_ID | 保可选profile真实投影和NULL，不设默认 |

C推荐形式：仅让User.xml的BaseResultMap不再 `extends="common.resultMap"`，在同一个type=UserEntity map中显式保留上述完整16个唯一属性，id只定义一次。不要只删extends然后假定继承字段自动补齐；也不增全局autoMapping开关。现AccountEntity其余账号、密码、头像、昵称、电话、邮箱、lastIp/lastLogin/permVer等原自动映射继续按现配置，需字段等价验证；不顺手把密码/实体序列化到响应或输出。

当前FrozenEnumm处理是MybatisPlusConfig设CompositeEnumTypeHandler默认UniversalEnumTypeHandler、类型扫描既有配置；原common.frozen没有专用handler，C应沿相同UserEntity继承属性与registry，不新换EnumOrdinal/名称handler。当前UniversalEnumTypeHandler对NULL返回NULL，对0/1/9999按code枚举，未知code抛原异常，均保持。若实际构建结果handler实例/类型不同，必须停并说明差异，不能仅凭XML字段名字相同就称等价。

User.xml本轮所见仅这一处common父依赖；本地UserWithModuleAndRole仍继承已先定义的BaseResultMap，保原roles/modules嵌套引用，不能为了避免依赖而删掉它们。ManagedProjection、条件、现SQL、Mapper接口及全局factory/扫描/MP注入器均不改；无需新selectDetailById或重复SQL。

C维护成本是User本地复制13项公共映射，未来公共map新增/调整时需要同步审查；可用定向映射等价测试约束，不能为消除这点重复又引入一个晚加载父map。此有限重复小于新增一整套详情SQL/映射及分裂selectById语义，但并非零成本。

### 8.3 三方案与用户实际选择

| 方案 | 当前评价 | 行为范围/选择含义 |
| --- | --- | --- |
| A 全局扫描/加载排序/注册覆盖 | 不推荐用于当前故障先行修 | 可能改变所有XML、BaseMapper冲突和插件statement；机制已有更窄候选，不需要先扩大全局 |
| B 只新增详情专用唯一显式语句 | 保留作窄止血备选 | 只让获准详情链读profile，其他selectById消费者仍为当前生成语句；重复SQL/结果映射，已知公共语义不一致仍留存 |
| C User本地完整自包含结果映射 | **当前技术推荐，待选择和复现验证** | 消除该User父map晚到pending，恢复原User.xml同名selectById联合查询，所有其调用者恢复原预期profile值；无全局注册修、无新HTTP/VO/DB |

C恢复的不仅是部门展示：saveUser的ensureUserEditable、ensureSystemAdminCodeAvailable、UserRoleRefSerivce/UserPostRefSerivce的写入ensureUserExists会重新看到profile.code=9999，因而按原代码拒绝受保护账号编辑/授权；当前仅因单表缺code而未触发的行为可能改变。repwd读取联合实体仍走原密码逻辑，不能推断它会因C自动禁止改密，也不能新加未有守卫。三类profile字段与版本/状态等结果映射都需等价回归。

这不是新增业务政策的设计提案，而是恢复原明确只读保护，但其实际影响超出“仅详情修部门”。按总控当前授权边界，需先让用户知情选择C恢复原六类调用语义，或B只限详情而保留其他问题；**尚未选择，不能把最初B技术建议或原修bug总授权当成C源码锁**。不把选择扩大为DB回填、账号授权变更或新只读政策。

可供总控呈现的一次具体选择：

> 建议C：只把User.xml的结果映射改为完整自包含，消除公共父map晚到造成的注册抢占；保原联合SQL、LEFT JOIN、数据权限和接口数据结构。这样部门/姓名/工号恢复，也让既有编辑和角色/岗位授权守卫重新按profile.code=9999执行原保护。另一选择B仅修详情读取，其他调用保留当前行为。两者都不改DB或已有授权；确认选择后再实施和验收。

### 8.4 C专属最低验证

先在隔离、无需实际DB的当前安装MyBatis/MP构造中 **User先common** 重放原map：记录pending、自动注入与最终MappedStatement来源/BoundSql，复现原失败；再以C自包含map按同顺序构造，确认最终selectById来自User.xml、LEFT JOIN/DEPT_ID正确，无重复同id冲突/未完成map。再测common先User，以及正常生产Spring配置的扫描路径；不能为了验证C把common强制前排以掩盖缺陷。

结果映射对比需覆盖16个显式属性、原自动账号字段、FrozenEnumm0/1/9999/NULL、revision/Long审计/时间/备注的保持；无profile注册账号仍LEFT JOIN可读、profile字段NULL，不能改INNER或补profile。外部VO及密码脱敏保持不变。

六类调用分别回归：详情部门回填；repwd仍沿原密码校验（测试不得真实改密或输出密码）；saveUser普通账号与code9999/READ_ONLY保护；系统admincode保留校验；角色授权写用ensureUserExists；岗位授权写用ensureUserExists。受保护账号应在写和permVer/关联变化前拒绝；普通无profile账号仍遵守原用例规则，不以profile缺失当不存在。使用受控fixture验证写保护，不能用实际管理员账号尝试写。

新statement仍非Ignore、U数据谓词/六表同源/摘要raw角色/岗位不二次路由，原第5、6节相关安全与live加载/QA证据要求继续适用。C可能让同一BaseResultMap的list/page及原UserWithModuleAndRole恢复完整字段，须核同namespace statement清单和映射差异，不把密码/profile归属扩散到新响应。加载后必须在实际factory重新核selectById resource/BoundSql，再合法只读验证PEER/D1展示；旧177或仅按正序解析XML通过均不足。

本补充仅设计与本人状态更新，不改User.xml/源码/测试、不重放factory、不执行build/DB/HTTP/debug/UI/服务。A/B/C均未实施；具体初始化次序和C行为由backend与QA后续获授权验证，不提前宣布根因修复完成。

## 9. 2026-10-27 用户选择C与独立实施门槛复核

### 9.1 已选范围与当前事实

据总控转达，用户已明确选C局部根因修复；此前“待选择”的表述为历史状态，无需重问。backend获单一生产文件 User.xml 的结果映射自包含及必要隔离tests候选范围，目前等待QA安全窗口才写；本设计角色无源码锁。获选不是已写、已加载、已注册正确或页面已回填的证明。

本次生产范围仅User.BaseResultMap消除跨namespace common父依赖并完整保留字段语义。原SQL投影/条件/LEFT JOIN、HTTP/VO/DB、Mapper接口、全局Factory/注册策略/排序/插件、其他Mapper一律不动；不实施A或B，不迁移表、不回填部门/权限、不改认证模型。新业务规则或无法保持映射的冲突应停报总控，待用户决定，不由成员扩大。

### 9.2 本轮只读核对与拟测覆盖要求

本轮静态解析原XML核得common13属性、User合并16唯一属性，当前磁盘仍extends common.resultMap；不把这个解析当初始化重放或测试。已再次只读确认六类消费入口和code9999/READ_ONLY守卫。总控给出的拟测范围是“必要隔离tests候选”，本轮所读backend状态尚未见2026-10-27具体新测试清单/实现；因此以下是独立审查的**必要验收要求**，不是对尚未落盘方案/测试通过的背书。backend交具体类/用例后按这些项逐一核覆盖，不只核总数。

| 必测组 | 通过所需证据 |
| --- | --- |
| 局部根因 | 同安装MyBatis/MP、User-before-common：旧map重现pending/生成抢注，新map不pending且最终User.selectById为原XML联合SQL；common-before-User与普通生产bootstrap也正确。不能只用原正序fixture跑绿 |
| common13与User16 | id/revision/deleted/frozen/sort/tenantId/createId/createBy/utcCreate/modifyId/modifyBy/utcModify/remark全保，加code/name/deptId，id仅一次；原type/handler与映射等价，非删extends后靠自动补缺 |
| 自动账号列 | account/password/avatar/nickName/tel/email/lastIp/lastLogin/permVer九项与原有效联合map一致，含NULL/长ID/时间；密码只用合成fixture，不输出值、不新增响应字段 |
| handler | FrozenEnumm0/1/9999/NULL、原未知值异常及现Universal/Composite registry行为保持；Long审计/版本/日期/备注不被换handler或默认化 |
| 六消费与写保护 | 详情部门/code/name；repwd原密码逻辑不新加禁改密规则；saveUser普通与受保护账号；系统admincode可用性；role授权ensureUserExists；post授权ensureUserExists。code9999或READ_ONLY按各自既有守卫在写/关联/permVer之前拒绝，不泛化成所有读取拒绝 |
| 可选profile | 可见无profile注册账号仍LEFT JOIN返回，profile三字段NULL；有profile映射真实值，已删/隐藏账号拒绝，不伪造profile状态列或补数据 |
| 权限与来源 | 非Ignore/dataPlugin实际U谓词、原开关语义、6表同原事务namespace、正tenant/core回退/第三schema拒、raw角色摘要及岗位无二次路由保持。C不能以修映射增加Ignore或硬编码schema |
| 作用范围 | 同namespace映射/statement前后审查，业务SQL与其他Mapper资源哈希不变；测试无实际DB、真实账号写、HTTP/服务操作。若隔离fixture不足以证明普通bootstrap，单独列未证项，不用旧177替代 |
| 运行与QA | 由总控在授权安全窗口管理产物加载后，实际factory resource/BoundSql/结果映射确认User.xml而非generated，再受控只读回验PEER部门与页面显示；编译/隔离测试通过不代替该项 |

六消费者恢复原code9999保护是用户已选C的明确影响，不用再次申请同一选择；但不得借它给repwd新增规则、扩大只读政策、改角色或数据库归属。副作用零变断言限于隔离测试，不能拿实际管理员试写来证明保护。

### 9.3 结束边界

本轮只更新本附件与本人status，未运行测试/初始化重放、无源/构建/DB/UI/debug/HTTP/服务/授权/其他角色卡/vault/Git发布操作。没有发现需要扩大到A/B、其他Mapper、迁移或认证改造的已证冲突；具体实现中出现新冲突必须停报，不以选择C作扩权。当前结论为已选待实施验收，未称修复完成。

## 10. backend具体拟测方案静态审查（2026-10-27）

已读backend状态“C方案授权范围与隔离验证准备”：新增UserMapperBootstrapFixture/Test、UserMapperConsumerTest，扩AssignedRoleDetailJdbcFixture，fixtures/user-before-c.xml封存旧反例。审时新三Java及反例目录尚不存在，以下为计划落实前的必要补充，**不是声称这些未生成文件已有bug或测试覆盖已实现**。本轮不运行Gradle、fixture或Spring context。

### 10.1 必须落实的上下文隔离

只读安装的Boot4 starter 3.5.17源码可见：MybatisPlusAutoConfiguration以单DataSource为条件，还收集容器内Interceptor/TypeHandler/LanguageDriver、DatabaseIdProvider、configuration/factory/properties customizer、MetaObjectHandler/ISqlInjector等bean。MybatisPlusConfig的VendorDatabaseIdProvider会取连接metadata；“mock DataSource”本身不足保证整个应用上下文不会装进其它启动bean。

必须用白名单最小context，仅导入真实MP Boot4 auto、当前MybatisPlusConfig、合成EvaConfig、唯一mock DataSource和必要事务组件；不得启动WebBooter、广ComponentScan、整@SpringBootTest、不受限EnableAutoConfiguration或真实ConfigData。禁止拉入DictInit/CommandLineRunner、Redis/Flyway/Hikari/业务调度。断言DataSource恰1且就是fixture对象，核factory/插件/handler/provider及customizer清单，不能有额外bean改变扫描次序或配置。这样验证的是生产自动配置的受控注册路径，不是整个生产应用已启动。

允许mock Connection/metadata读取PostgreSQL产品名，不能声称零getConnection；每次getConnection都只能是内存fixture对象，无DriverManager/真实URL。metadata、事务方法和允许SELECT分别白名单，未知execute/update/多语句/存储过程/非许可SELECT失败计数，不用真实DataSource包装器或CALLS_REAL_METHODS。SQL只读组与service写mock组分开，后者仅验证保护控制流，不落JDBC持久化；不输出合成密码。

### 10.2 普通扫描与反例真实性

普通扫描直接使用实际resolver与项目公开MyBatis属性，保留项目glob `classpath*:/mapper/*.xml` 的原解析顺序；不能误用安装默认 `classpath*:/mapper/**/*.xml` 却称实际项目绑定。不得sort、先手工parse common、预addMapper或customizer先装common；记录原resource数量/来源/索引和最终statement。无需为这些公开属性读取整份含真实连接配置的application-dev。

User-first/common-first为另两个独立factory顺序用例，可明确给测试专用resource数组，不能混称普通classpath扫描。每场景独立context/factory，结束关闭和恢复ThreadUser/Tenant；MP静态registry/枚举handler副作用与测试并发应隔离，不依赖前一例装好的common或handler。

旧 `fixtures/user-before-c.xml` 须与获准修改前User.xml全字节/hash一致，保留 **extends+本地4项id/code/name/deptId**；它不是C的16显式项。原13父项合并16是旧有效继承语义，不能因“16项”把反例做成去extends的新文件。旧context不得同时装生产User.xml同namespace；正常扫描必须断言没有拾取fixtures或test资源mapper/User.xml遮蔽生产。旧例核generated resource/无JOIN，新例核XML resource/LEFT JOIN+DEPT_ID，不只断言context启动成功。

### 10.3 Consumer与原详情fixture

ConsumerTest必须使用实际新factory映射的实体再执行写mock测试，不手工setCode9999就称映射修复。保第9节13/16/9字段和handler/NULL/日期/长ID、六消费者、原保护写前无profile/role/post/permVer写；repwd仅原密码逻辑不新加只读限制。可见无profile LEFTJOIN、隐藏/删除账号等不可遗漏。

AssignedRoleDetailJdbcFixture扩联合投影可补六表同事务/raw角色/岗位无二次路由安全回归，但原common-first手工解析只属于SQL语义测试，不替新增bootstrap；须实际从联合读取取得deptId/code/name，不以构造UserEntity代替。当前计划未列具体用例名与断言，落实前不能称覆盖完整。

结论：方案方向合C局部边界，以上细节须补清后复核，不是Ship或Gradle槽放行；最终仍需独立QA、live加载/statement与页面证据。本轮仅读安装源码/项目/状态，写本人设计与status，无源/测试/构建/DB/服务/UI动作。

## 11. 已落盘C草稿独立静态复核：可进入受控隔离首轮

已实际读取UserMapperBootstrapFixture.java、UserMapperBootstrapTest.java、UserMapperConsumerTest.java、扩展AssignedRoleDetailJdbcFixture.java、fixtures/user-before-c.xml及生产User.xml。此节替代第10节“文件未生成、仅计划”的审查状态；本角色没有编译/启动context/运行测试。

**结论：未发现必须在隔离首轮之前修正的静态阻塞，可由总控分配原窄范围隔离执行槽。** 这只表示草稿可进入实际编译/执行检验，不代表可发布、测试已过、全覆盖完成或live问题已修；不扩大生产/SQL/接口/DB边界。

已核落实情况：

- 生产User.xml只去BaseResultMap跨namespace extends并保16个唯一显式属性；静态替换该map区域后与旧反例区域外文本完全一致，全部SQL/其他map不变。反例Git内容哈希与HEAD原User.xml均为d49f46b18f03777e50ebe86f8c4abaf6316948e6，确实是extends+原4本地项，非C16文件。
- BootstrapFixture为AnnotationConfigApplicationContext白名单注册MybatisPlusConfig/MybatisPlusAutoConfiguration、合成EvaConfig和唯一Mockito DataSource；AutoConfigurationPackages只给org.pkaq元数据，不ComponentScan/WebBooter/整应用自动配置。显式StandardEnvironment移除系统环境/系统属性源，只加公开MyBatis配置，未导入ConfigData/真实配置。
- refresh后有DataSource单一且同对象、插件/provider数量、customizer/handler/injector/taskScheduler清单断言。虽然这些断言在refresh之后，其前置注册列表也没有外部组件或未知自动配置路径；未发现真实数据源、Redis/Flyway/Hikari/任务启动入口。
- 新bootstrap模式DataSource只许可无参getConnection返回内存Connection；metadata只许可PostgreSQL产品名，Connection未许可调用失败；写/批处理/未知表及未支持谓词失败，bootstrap路径拒多语句/注释，所有JDBC均mock，无URL/DriverManager或真实网络能力。PreparedStatement的get/close等无副作用默认值仍属mock，不是实际JDBC操作；不能把该fixture安全结论外推到真实数据库。
- NORMAL不注册factory customizer、不sort/预parse资源，真实auto按项目glob解析；USER_FIRST/COMMON_FIRST/LEGACY_USER_FIRST另明确数组且保其他资源数量，旧User替换后不再同时装prod User。NORMAL也仍为受控MyBatis注册测试，不是完整WebBooter应用已启动。
- BootstrapTest有旧反序generated/无JOIN/profileNULL对照；新NORMAL/正反序XML/LEFTJOIN/DEPT_ID、16属性、13父映射JavaType/handler等价、handler枚举/NULL/未知值与9自动账号字段；无profile、已删/无scope账号、真实Service详情/raw角色/post/第三schema拒在SQL前。反例不在正常resources列表检查也已写入。
- ConsumerTest先从该真实factory读取合成映射实体，再交写mock调用真实saveUser/role-post服务及repwd；profileCode9999设置在合成源数据，非直接set到待测实体。保留已有code/READ_ONLY写前拒、系统code校验，repwd错误原密码拒、正确原密码对ordinary/code/frozen均沿原流程，无新只读规则。未知UserMapper写调用用AssertionError，不能被assertThrows(RuntimeException)吞作保护成功。
- 原Assigned fixture增加bootstrap可选路径而保旧手工语义路径，实际有JOIN才附profile列，避免给generated SQL假造deptId；事务连接holder/readOnly断言、raw角色/内部关系/来源拒保持。其旧手工正序路径仍只作语义回归，不能取代新bootstrap证据。

进入首轮后需据实际结果再判断，而不是预先承诺一定能运行：Spring/MP API与bean扫描完整性、元数据/ResultSet方法白名单是否满足真实调用、全局registry状态及handler实际类型、旧反例是否确实重现抢注，均须由新测试结果证明；失败应保留具体日志和最小fixture修正，不放宽数据插件、改扫描顺序或移除旧反例。正常扫描资源列表是另一次resolver观测，最终需联合实际factory statement证据判断，不仅看列表。

现Consumer草稿重点是受保护路径及repwd原行为；普通账号完整编辑/无profile管理与既有正tenant/6表矩阵仍需原适用回归配合，不能把这两新Test类单独概括为所有业务通过。此点不要求本轮扩大生产代码，也不阻止隔离首轮查编译/注册；最终关闭仍按第9节覆盖表与实际计数逐项报告。保留独立QA、总控授权加载、live statement和PEER/D1页面复验门槛。

本次仅两本人文档更新，无源/构建/测试/DB/HTTP/UI/debug/服务/Git发布操作。静态结论交总控决定槽位，不由本成员启动或授权实际执行。

## 12. QA门禁补充后的审查更正：暂不能进入执行

总控转交QA实际草稿预审mustfix后，本角色复核此前已读代码事实，**撤回第11节“可进入隔离首轮”的结论**，该句不再作为当前门禁意见。全部DataSource/JDBC为mock、未见外部IO路径只证明物理隔离，不代表未知JDBC接口已失败闭合；上轮将前者作为足够执行前提判断不充分。

具体缺口：新bootstrap路径PreparedStatement与Statement仍有RETURNS_DEFAULTS，PreparedStatement允许任意startsWith("set")且参数形态匹配的方法；ResultSetMetaData、ResultSet亦默认返回false/null/0。若新框架或handler使用未实现getter，可能把测试能力缺失静默当数据库NULL；不被识别的statement/setter也不会按既定要求明确失败。因此不能用“mock不会连网”来豁免该门禁。

最小修正仅限现新bootstrap分支：列出实际需要的setter/getter/metadata/execute/结果迭代/close等精确方法及重载参数形态白名单，对其他调用抛AssertionError；不扩大至旧order==null手工fixture语义。写、批处理、未知setter/getter/metadata/statement方法必须有负向测试，明确失败且不被业务RuntimeException断言误收为正常拒绝。不能为得到绿结果在未知路径回默认NULL，不能通过删负例或放松SELECT来源/谓词来处理失败。

QA另列Consumer普通save、role/post、无profile正向及同namespace映射/VO等价缺项，须backend给精确“新用例/原适用用例→门槛”组合清单：受保护账号拒绝不代普通流程通过；factory实体映射不代UserService→UserConvert→VO结果等价；一个BaseResultMap正确不代同namespace其他映射不受影响。不要求扩全库或新增生产逻辑，只把C已明确的影响面验清。

当前状态：等待backend最小补齐、QA与总控重新核执行门禁；生产XML16与SQL未变的静态检查仍有效，但不能替代测试安全门禁。按总控新指令本角色不再运行命令或改源码，本次仅依既读事实追加本人设计和status，不运行任何编译/测试/DB/UI/服务。

## 13. QA收口冻结后映射与业务正向窄复核（2026-10-27）

按总控新指令只读最新实际BootstrapTest/ConsumerTest与生产User.xml、原saveProfile控制流；本轮重点为映射和新增正向语义，QA同时独立核隔离框架，**不以本节替代QA安全门禁或授执行槽**。未运行编译/context/tests。

未发现新的确定业务/映射阻断：生产仍仅16项自包含，保同namespace UserWithModuleAndRole本地继承及roles/modules引用。最新测试对common13的column/JavaType/handler逐项比较，9个自动账号字段、冻结0/1/9999/NULL及未知值错误、profile缺失LEFTJOIN均有明确断言；新增缺tenant列用例要求getTenantId保持NULL不造0。selectById/list/page的原XML资源及BaseResultMap引用、嵌套roles/modules引用也已列检查，不为这些检查新增数据查询或认证加载入口。

Consumer新增有/无profile两种普通账号正向：实体先由C正常bootstrap真实映射取得，原code分别fixture-worker/NULL；真实UserConvert用于saveUser，账号更新与profile存在则update/不存在则insert均为mock断言。原saveProfile代码明确首次管理编辑可补档案，这个正向不改变注册“查看不造profile”契约：查询仍不写，只有既有显式管理保存流程才补建。没有给无profile账号新增默认部门/角色。

角色/岗位正向以普通可编辑账号、incoming空集合和已有一条关系，调用真实原saveRoles/savePosts，核原关系删除及permVer次数；这验证的是C恢复profile字段后普通账号仍能沿既有授权控制流清空专用关联，**不是非空角色/岗位可用性或新增授权矩阵已通过**。后者仍依原适用回归，不要求本轮扩测试框架或生产范围。保护负向仍用factory读出的code9999或READ_ONLY，原写前拒保持；repwd普通/保留code/只读状态均保原密码规则，不引入新禁改密。

VO用例现在经真实getUser/转换链返回deptId、原完整roleIds、raw摘要与postId，再序列化检查无password字段/合成hash泄露；该断言是隔离JSON安全补证，不等价完整HTTP响应或部署Jackson配置已验。无profile/未知数据返回语义、同namespace结果映射保持在静态范围未见冲突。

实际编译/API/bean/handler实例/JDBC白名单是否满足真实调用、旧反序抢注是否在当前依赖下重现，仍需QA静态门禁通过后由总控授受控首轮检验；不因某个断言可能需要适配而推测性扩生产配置，也不提前报绿。本节不重新声称第12节安全block已由本角色独立清除：框架修正的最终结论以QA最新实际读审为准。

仅本人设计/status追加，无生产或测试源修改，无编译/测试/DB/UI/debug/HTTP/服务/Git发布操作。
