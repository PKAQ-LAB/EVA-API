# technical_designer 状态卡

- 任务：DICTIONARY-OWNERSHIP-DESIGN-20261018；本轮主卡RUNTIME-PERMISSIONS-DICTIONARY-TANSTACK-20261019。
- 日期：2026-10-19（Asia/Shanghai）；唯一总控01a0bc51-e17e-7990-ad76-13c63f385f33。
- 状态：可展开Table展示契约与开发PG只读核验完成，精确迁移草案已准备但未执行；待总控取得一次具体DB/API/QA归属确认后派写者，写锁释放。
- 本成员只写：docs/legion/design/2026-10-18-dictionary-ownership.md、本卡。
- 后端cornerstone/6a0fee51、前端trunk/c220a9c0；其他成员dirty/GO等均保留，未fetch/切分支/commit/push。

## 本轮关键纠正

已重写[设计](../design/2026-10-18-dictionary-ownership.md)：左侧是当前AntD **Table的可展开children行**，不是纯Tree。保留表头/紧凑样式/操作列center、左右40/60及SideLayout.collapsible=false；默认展开系统字典与业务字典两根，可分别收起；仅根→具体字典两层，SYS_DICT_ITEM键值只在右侧。根无CRUD，系统子主项/明细只读，业务tenant自治/standalone有效ROLE_ADMIN。展示不再重复询问。

现装antd6.6.3 CLI已只读核对Table childrenColumnName、expandedRowKeys/onExpandedRowsChange等；不升级CLI或依赖。使用受控展开状态保证异步首载展开一次、刷新不抢回用户已收起状态。

## 开发PG实际结果

获准后调用既有开发连接解析/指纹校验的connect(True)，未执行其run或写操作。开发目标指纹d22bc534e71c / eva schema；SERIALIZABLE、transaction_read_only=on，连接结束均rollback/close。凭据未输出或落盘，未读取业务个人信息。

- 仅eva命名空间，无eva_core/tenant_*；本目标不创建或迁往core/tenant。
- **实际SYS_DICT已有pid/path/isleaf/value**，pid BIGINT NOT NULL DEFAULT0并有索引；当前实体和当前V1无父列不能代表实库无列，开发库不新增PID或KIND。
- 47条SYS_DICT，8有效/39删除；系统根1000000000000004000 / SYSTEM_DICT真实存在但deleted=1783056863；业务根候选1000000000000004080 / BUSINESS_DICT所有状态未占用，根明细引用0。旧根1/3虽未占用但无此现库关联，不强行套用。
- 七组有效主项4010/4020/4030/4040/4050/4060/4070（完整前缀100000000000000）均pid0；其已删除旧叶子的精确pid/path分别3/3、2/2、4/4、4/4、8/8、1/1、7/7一致指向现存4000系统根，可据此确认旧系统归属，不按名称/状态猜。
- 唯一有效归属未知项2111791956297613314 / EVA_QA_20261018_D01：pid0/pathNULL、1条有效明细，无旧父根证据。推荐确认后挂业务4080，不先执行。
- DATA_SCOPE主项1000000000000004001已deleted1、8明细均删除；37条旧单表叶子均删除；本方案保持删除，不顺便修复/复活QA删除记录。
- SYS_DICT_ITEM36总/28有效，有效孤儿0；本次恢复不写明细，RESOURCE_TYPE旧BUTTON/OPERATE也不复活。
- 实库主表只有旧活动(type,pid,code)唯一索引，**无当前V1声明的活动CODE唯一索引**；当前无有效code重复，批准实施可精确新增活动CODE唯一约束，旧索引先保留。
- 没有指向字典表的数据库FK；eva schema无flyway/tenant版本表，不代表可以重跑V1或擅自baseline。旧V_DICT还是单表叶值view，当前Java/XML未发现消费者，不借本次扩大修改。

快照检测：SYS_DICT受检元数据MD5 97fe3525dbadbec4faea9bd841c58469，SYS_DICT_ITEM受检字段MD5 2d331236f832869fb761fdd7a444b033。文档SQL草案默认approval=false且ROLLBACK结尾，未执行；快照变化即停止重审，不能覆盖活跃QA数据。

## 推荐一次性确认与准确改动

确认复用已有PID、恢复4000系统根、新增4080业务根；七组有效旧字典挂系统4000，唯一未知QA条目2111791956297613314批准挂业务4080；保持DATA_SCOPE和旧叶子删除；/list改两根children契约后同批实施。UI不再重问，只确认具体DB/API清单和唯一未知挂靠。

确认后预期SYS_DICT改9条（系统根+七系统主项+QA）增1条业务根，总行47→48、有效8→10、删除39→38；SYS_DICT_ITEM仍36/28且摘要不变。两根与系统主项frozen设9999兼容旧端但服务层仍按真实父根拒写；保留type/code及键值数据。PID0根哨兵不能直接加需要id0行的普通父FK，不创建第三根。

源码实施需最小改DictEntity/BO/VO/Convert/Mapper PID、list children、根排除code读取、所有写入口守卫及缓存旁路；前端复用Table，不换Tree。当前只读/设计不等于已实现或运行验收。普通修复与最终提交push授权已知，但本成员不自行发布，不把泛授权当任意迁移许可；全部当前任务完成、独立QA后由root正式唤起发布。

## 验证与限制

已完成源码/历史、开发PG只读及本地Table接口核验；未执行草案SQL、写DB/Redis、改生产/测试代码、运行编译测试/服务/浏览器、安装升级、JEV、子代理、commit/push。没有迁移备份或恢复演练，不声称迁移已安全落库。GitNexus之前后端落后21提交、前端43/42版本限制未刷新，写者仍需impact。

仅两文档经UTF-8/LF/无行尾空白检查；git diff --check通过，其他成员已有文件的LF→CRLF提示不属于本成员变更。状态卡先落盘，再主动总控回报精确契约/数据库证据/待确认项。

## 2026-10-22 安全补充完成（当前状态）

按总控LOG审查发现，仅对当前RoleService、前端role表单/service、DataPermission处理器、业务/错误日志Repository/Mapper及资源matcher做只读核验；未再查库/日志个人数据。原开发PG数字仍是10-19快照，原字典根/PID/listchildren具体恢复继续等待用户确认，无擅批。

已在原design第9节追加可执行清单：后端role缺scope默认0000=ALL，与前端初始化/normalize/save三处同样兜底必须一起修；推荐新增必选无默认、更新未传/null保留原scope+orgIds、显式空白/非法拒绝，不将0007替成新默认、不改已有ALL。0003须验证本tenant合法组织，0004/5/6未实现继续拒绝、不新授。

日志通用CREATE_ID/MODIFY_ID过滤不适用于LOG_BIZ/ARCHIVE/ERROR；推荐先对受限/缺身份日志读取明确拒绝并避免进错误SQL，已有可信ALL+资源许可继续按原tenant边界。COUNT/热归档两支/详情一致，禁止全局Ignore/excludeTables，不能误伤匿名日志保存或后台归档。此为可直接原契约安全guard；受限可见范围另确认。

待确认最小策略：业务本人是否按TENANT_ID+USER_ID操作者、部门是否按操作者当前部门（调岗会改变历史可见性）；错误聚合无稳定个人所有者，推荐仍仅ALL，不按LOGIN_USER文本分隔；岗位范围实现或默认政策、撤旧宽*授权需单独具体决策。角色资源现任一匹配放行，精确资源不盖掉宽*，不得用管理员正向成功冒充受限隔离验证。

本轮本成员仅改原design与本卡；未生产/前端/SQL/DBRedis/授权数据写入、服务/浏览器/编译测试、依赖、子代理、commit/push。上述建议未实现，运行故障尚须root派生产写者和独立QA验证；字典迁移仍未实施。

## 2026-10-23 OpenAPI调用日志边界只读分析完成（当前状态）

新增局部设计[OpenAPI日志边界](../design/2026-10-23-openapi-log-boundary.md)，只追加本卡，不改原QA矩阵/生产/API/DB/授权/vault。已读军团规则、backend最新P0/日志矩阵说明、quality_reviewer-sys-log-matrix-20261023与当前差异；用户待重启加载，未自行UI/HTTPDB/服务/测试构建/依赖/子代理/提交push/JEV。

证据：OpenApiCallLogService get46/47直接selectById，list56/59直接selectPage（自动count），save86/94–104无tenant赋值，均无TenantSchema/事务/LogQueryAccess；API_CALL_LOG继承StdEntity，V1:449有nullable TENANT_ID和CREATE/MODIFY列，与biz/error无该列的根因不同。MybatisPlusConfig:39只在data开启装拦截器，无TenantLine；专用P0 guard只含biz/error表不含API_CALL_LOG。data关时AuthPermissionContext:98 scopes空、直接Mapper路径无本地guard；不能据“disabled拦截器单测”外推生产根本未注册Bean。

AppKeyAuthenticationFilter:157 ROLE_API_USER只建SecurityContext不建ThreadUser/tenant路由，chain返回及异常save；AppCredentialSnapshot无tenant、AppKeyService快照不拷tenant；MetaObjectHandler明确不填tenantId。凭据实体的继承tenant字段不等于外部请求有可信归属，失败credential还可null。当前路径/API资源种子存在但无静态UI，默认OpenAPI仅/apr/**，管理读预计JWT+资源许可；无菜单不等于不可达，未HTTP/DB验证不能报已越权。

历史50868232原服务已有同类get/list/save，83603115主要IP调整、7e060637迁adapter/SPI；没有找到明确平台global或tenantprivate业务声明。集中凭据/日志初始化更像全局审计仅为推断，不是政策事实；tenant V1/V2未见本表，实际schema不查。

可直接最小否定guard：Service get/list前复用可信身份+明确审计范围，缺身份/scopes拒且不调用Mapper；不能只扩拦截器、不能全局Ignore、不能把guard套save破坏外部失败审计。**requireAll不能解决租户ALL跨tenant，正向隔离仍未闭合**；data关scopes空则临时失败闭合，不合成ALL或全局改角色加载。

需一次确认：A集中管理员审计（推荐最小，standalone管理员、platform可信tenant0审计资格，普通tenant不开放）或B租户本应用审计（需凭据可信tenant SPI、core共享表行归属、未知失败独立平台区与旧行审阅）。另外推荐data/resource开关关时仍保独立审计资格；未答不实现正向归属/新增权限、迁移或回填NULL。未知失败不能凭header appKey猜tenant，旧CREATE_ID=-1/NULL不改给当前用户。测试清单覆盖开关真实装配、detail/page/count与A/B、AppKey调用写入、路由恢复、旧未知记录；本轮未执行。

文件UTF-8/LF/无尾空白检查；本结论为源码风险与决策建议，不是实际泄露、日志写失败或权限运行通过证明。原字典恢复具体确认仍未获答复，保持待命无写锁。

### 2026-10-23 A方案既有资格与最小单项决策补充

已在局部OpenAPI设计第7节收口：确认前只补get/list缺可信身份/非法身份的否定guard，可临时requireAll失败闭合但不能假称ALL解决tenant隔离；save保持原系统审计写。最终推荐A复用既有ROLE_ADMIN，不新角色/能力/ALL：standalone唯一schema有效角色关系；platform必须可信tenant0并core有效ROLE_ADMIN；正tenant的同名角色、TENANT_ADMIN、TENANT_INSPECT、ROLE_API_USER均不能替代平台审计资格。

新核证据：standalone两开关均关时AuthPermissionContext.findRoles返回空；不能用空roleMap放行，需仅审计入口窄查服务端关系。现IPermissionSnapshotQuery背后AuthRole.xml自定义SQL未显式过滤DELETED，只有FROZEN!=1与关联，所以不能不加有效性就复用其ROLE_ADMIN结果；需窄查明确未删/未冻结，或补原查询后impact。platform tenant0不能直接TenantAuthRouter.execute(0)当core，正租户resolver拒0，须受信core只读事务边界。

给root的一句确认精确包含开关：是否A集中审计，仅standalone或platform tenant0有效ROLE_ADMIN可读；data/resource关也保该管理员门禁，resource开保原日志资源授权、关只跳过资源匹配；普通tenant无本应用视图，旧不明归属不回填。批准这一个完整决策后即可实现，未答前不将“完成这些”当A/B批准。无新授权行/全局role加载/scopeALL，既有权限开关语义保持；本补充未实施测试/查库/改生产或授权，仅两本人文档。

### 2026-10-23 权限测试身份与授权准备包（未执行）

已交付 `docs/legion/design/2026-10-23-permission-test-fixtures.md`：S 包上限 2 个候选账号、5 专用角色、4 部门、2 主体作者组织样本；实际 ID 与候选唯一性均待只读确认。当前运行 mode/开关未重新核实，不以源码 standalone 默认或历史 eva schema 代替运行事实。密码/登录只由用户本地接管，不进聊天/Git。

S0–S8 明确完整集合替换前后值：初始 0007、精确组织读写资源，随后撤写；0001/0002/0003、受限并集、短暂含 ALL 并集及日志详情/list/count 门禁。资源逐项 code-method-path-mainId 来自 Controller/种子，运行资源 ID 不猜；不授初始通配符。菜单/按钮/直接端点和会话撤销分别验，0004/5/6 保持未支持。PEER 专用账号受控 D1→D1C→D2→D1，说明当前档案部门及修改者会影响历史样本可见性；保护 R01/O01/D01/M01/M02。

核实 RoleResourceRefBo 字段为 roleId+resourceId 数组、用户 roleIds 与角色资源均为完整替换；permVer/资源事件后回读验证，不全局清缓存。租户编码正则最多六位，候选 qaa23/qab23 仍待查重；新租户连带 schema、根组织、管理员及 TENANT_ADMIN/ALL，不是可自动回滚的数据。T 包与新建租户补充批准独立于 S，缺合法入口/模式/两租户时不执行、不切配置。收尾默认撤权、禁用保留；软删不承诺完整可逆，物理删 schema 不在包内。

文件末尾备好一次确认文案与排除项：字典根/归属/D01 remark、OpenAPI A 审计政策仍各待独立决定。只写本设计与本人状态，未创建数据/账号/授权，未 DB/HTTP、服务操作、构建测试、生产代码或 Git 写入；只做源码/证据只读核对与文档 UTF-8/LF/尾空白检查，不能报告权限运行通过。

### 2026-10-24 用户批准 S 与 OpenAPI A（记录追加，未执行）

据总控转达用户结构化明确批准，已在原两设计文件末尾追加批准记录，替代其中 S 待确认、A/B 未选择的历史状态，不重复提问同一批准。S 仅按原 S0–S8 上限和授权差异执行、回收；不含 T、新租户/schema、现有管理员及其授权修改、物理删除、字典根/归属/D01 remark 契约。实际模式/开关、合法入口、唯一性、对象/资源真实 ID 和前值快照仍须 S0 核实，密码与登录本地接管，批准不等于预检已完成。

OpenAPI 批准第 7 节完整集中审计 A：standalone 有效 ROLE_ADMIN 或 platform 可信 tenant0/core 有效 ROLE_ADMIN；data/resource 关闭仍保管理员门禁，resource 开保原资源判定、关仅跳过匹配；普通 tenant 不可读，旧不明归属不回填，不新角色/ALL/既有授权行或全局角色装载，读 detail/page/count 同资格同源、save 保原失败隔离。总控已派 backend 实施、QA 预检；本角色只追加原两设计及本人状态，未改实现/QA卡/vault，未创建数据或运行测试。

### 2026-10-24 DATA_SCOPE 最小修复提案（待单项批准）

S0角色新增下拉空，S尚无创建/授权。新增本人设计 `2026-10-24-data-scope-dictionary-repair.md`，仅读取backend同轮READ ONLY证据 `2026-10-24-data-scope-readonly-diagnosis.md`，未另连DB。实际eva/eva主4001、item4002..4009全deleted1/frozen0/tenant0；主pid0/path自身，明细无pid/path/isleaf。按总控最新明确最小包R：完整ID主1000000000000004001和明细4002/4003/4004/4005/4009仅deleted1→0，frozen0及其余字段保持；4006/7/8未实现岗位值仍删。不改9999、不回挂4000、不恢复根/其它字典/旧叶子；系统全局只读及两根契约仍独立，已读新表命名规则但不迁SYS_DICT_ITEM。

已核DictSelector本页showall=false、无默认ALL；queryDict命中空map即返回，单改DB不足；主项查询不依赖PID，只需原main与五明细。scope缺省/非法/岗位新授拒、0003合法组织校验不变，不改已有角色。设计列九行前后、六行条件回退、同源单键失效与失败停S，不造evict接口、不清全缓存。源码候选dictdata::DATA_SCOPE非Redis实测，等待backend补缓存/合法执行路径后收口。只写本人设计和状态，无DB/HTTP/Redis/生产源码/QA卡/vault/服务/构建/Git写入；包S既有批准不替代本共用字典修复批准。

缓存补证已读backend附件第4节：现有helper单键remove与提交后定向刷新是内部Java能力，无HTTP入口，外部恢复不触回调；没有实际Redis键/Bean读证。包R定稿选择本地合法维护客户端接管，先只读确认同实例Redis/db/namespace，唯一候选dictdata::DATA_SCOPE备份/比较前值后失效，管理员GET+重开表单验证五项。客户端不可用、同源/namespace不明或不能比较前值均在DB写之前停包，不先写后寻路；不造接口/无意义edit/switch、不多键试删、不重启。六行实际差异与一次确认文案已具备，缓存运行核实保留为明确执行前置，非已完成证明。文档UTF-8/LF/无尾空白检查通过，准备完成交总控；R仍未获批准、未执行。

### 2026-10-24 包 R 已批准（仅批准记录，未执行证明）

总控转达用户结构化明确批准 R 完整文案，已在原 R 设计第9节追加，替代前述待批准状态，不重复提问。backend_developer 获唯一R数据执行权；QA UI暂停，S当前创建/授权仍0，frontend组织问题另行定位。六行仅DELETED1→0，所有FROZEN0/原PID与其余字段保持；先核同实例唯一缓存键、合法入口、备份及比较前值能力，安全前置不满足则DB写前停，禁止先写后寻路。

非R字典根/旧叶子/其他字典/两根归属与D01 remark/系统全局只读及9999政策仍未批准；不扩到角色授权、租户副本/schema、迁移/物理删除、全局清缓存、新接口或服务启停。批准不是已恢复/缓存失效/下拉验收证据；本角色只追加原设计与自身status，无数据/缓存/HTTP/源码/QA卡/vault/授权或新会话，结果以执行者与QA后续证据为准。

### 2026-10-24 CUSTOM 0003 standalone 归属校验最小审查

交付 `2026-10-24-custom-scope-standalone-ownership.md`。复用backend只读附件当前D2 ID2113856917966462977：tenantNULL、按ID+deleted命中而tenant0谓词不命中；四组织/三角色均NULL且CUSTOM候选0。QA无payload/HTTP实证，不能宣称具体错误码已复现或修复成功。源核MetaObjectHandler明确两模式不填tenant，Org/Role普通insert未set，User部门selectById不加tenant0；tenant初始化SQL/V1依schema且相关表无TENANT_ID，StdEntity自动填充注释过时，当前插件无TenantLine。

推荐仅RoleService standalone分支的既有内部bug修：可信mode/tenant关闭、人类tenant0、无冲突TenantContext，同原事务实际current_schema及sys_organization解析namespace等于配置唯一schema后，原参数化Mapper按完整ID/deleted/data插件再加分组NULL或0。严格Set全等，伪ID/隐藏ID/第三schema/异常拒绝，不仅删guard、不全局Ignore、不填0/回填七行、不改新建约定/DB/API。可由总控依既有修bug授权派backend实现，本角色无源码锁。

正tenant不进NULL兼容，不加跨tenant OR NULL，保原失败闭合；当前positive eq与tenant V1无列另列未解潜在缺口，不能宣称schema租户CUSTOM已通。后续schema_only正tenant纠正应按可信映射/同事务实际表namespace/完整可见集合另派核证，不新DDL；若要统一列/归属/迁移/跨schema共享才需精确新批准。设计含SQL分组、身份/mode/schema冲突、数据插件、各scope/更新原值、事务无写/permVer、路由与真实QA回读必要回归。仅本设计+status，无新DB/日志/秘密/代码/构建/UI/服务/SQL/HTTP/Redis/Git写入。

### 2026-10-24 账号view已授角色增量详情契约（仅设计）

交付 `2026-10-24-account-assigned-role-detail-contract.md`，用户已允许必要读契约，可总控直接派实现：原GET account/get仅加assignedRoles数组{id:Long,name/code:String可null,frozen:0/1/9999/null}；原roleIds/postId/写BO不改，不新route/seed/DB。无需新complete布尔，前端用canonical原roleIds与摘要ID集合推导完整性；字段缺失是未就绪，只有两数组确空才未分配。失效/孤立/不可见摘要缺项保原ID/名称—和统一不完整提示，不冒称无角色，不删ref/角色、不阻断原编辑详情；基础隔离或SQL错误正常失败不伪装空。冻结/只读已授摘要照实返回，不等候选/有效权限。

先可信源与原非Ignore账号可见性→一次真实ref查询→非Ignore RoleMapper精确IN投影未删且operator数据可见元数据，不分页200/全候选过滤/N+1，不复用认证Ignore快照/写可用性守卫；UserRoleRefSerivce可局部组装。view仅消费摘要，新增编辑保候选，缺项不覆盖原写值。事务内account/profile/ref/role及实际插件/原post查询依赖的组织/postref均核解析namespace，schema模式可信正tenant映射，缺表fallbackcore/第三schema/冲突身份context整请求拒；历史单个无可见role只是缺项不拒。无tenant0/ORNULL行补丁、无Ignore/ALL/admin/CoreSQL取元数据，不扩平台inspect。设计列>200、隐藏/已删/孤立、冻/只读、长ID、完整性、原编辑/候选、资源/dataPlugin及schema/事务必要测试；本轮未新DB/HTTP/源/测试/UI/服务/Git，只设计+status。

### 2026-10-26 部门回填 live statement 差异只读评估

交付 `2026-10-26-account-detail-live-statement.md`。读最新规则/任务、账号档案拆分契约及当前Mapper/实现：cornerstone HEAD1f708e20/ahead4，现有QA两卡dirty/.vscode/GO均不触碰。总控live PID25764证据：同PEER内部user与vo部门均NULL，实际UserMapper.selectById resource为Mapper.java best guess，BoundSql只有SYS_ACCOUNT无profile join；磁盘XML正确不等于live注册正确。现fixture手工先parseXML，可证明SQL语义但177不证明生产注册顺序和新statement生效。A具体扫描/覆盖机制尚待backend证据，不自行归因全局配置。

推荐B仅详情链新增唯一内部selectDetailById（优先唯一注解SQL及自有结果映射，避免新XML仍不加载/同id冲突），readUserRoles本处调用切换；原BaseMapper/selectById消费者/全局factory不动，不新HTTP/VO/DB。保持LEFTJOIN无profile注册账号、DEPT_ID显式映射、非Ignore账号可见、六表同原事务源检查、raw角色摘要及岗位无二次路由，不能String补NULL/回填DB/Ignore/硬编码schema。A如改全局XML加载/覆盖将影响改密/编辑/只读code及授权/岗位校验等，需先精确影响与锁，不沿B授权扩大。

设计列正常生产bootstrap注册测试（不手工先parseXML）、statement来源/唯一性/实际BoundSql与插件最终谓词、真实资料与无profile/隐藏/已删、schema回退与原编辑写链不变、受控加载后live新statement及QA回填必要验收；本角色无实现授权、不源码/测试构建/新DB/HTTP/UI/debug/服务/日志秘密/提交push，只写设计+status。结果已交总控，源码继续冻结，不把建议当已修复。

### 2026-10-26 新机制证据后A/B/C补评估（待具体选择）

原live-statement设计顶部已标第8节当前建议替代初步B。backend安装源码链与资源顺序支持User跨namespace common父map晚到→pending→bind先addMapper/MP注入selectById→后XML同id保留生成的机制候选，尚未重放该进程初始化。本角色实读core-data-mybatis/mapper/common.xml：13普通公共字段，无显式handler/javaType/嵌套/构造；User加code/name/deptId，合并16唯一属性。推荐C仅User BaseResultMap去跨namespace extends并完整自包含16项，保原FrozenEnumm/UniversalEnumTypeHandler及账号自动字段/SQL/LEFT JOIN，无新方法/重复SQL/全局排序。有限13项复制维护成本已列，不只删extends赌自动映射。

C比B更贴局部根因，但会恢复所有selectById消费者读取profile.code9999：saveUser/系统admincode/role-post写校验原保护重新生效，不只是详情；repwd仍按原密码逻辑不新加禁改密。总控需向用户明确选择C恢复原六类调用语义，或B仅详情且留其余不一致，未选不改，不把初步B或总修bug授权当C源码锁。A全局扫描/Factory/覆盖影响过大不推荐先行。C测试要求User-before-common失败重放+修后同反序/正序/普通bootstrap语句元数据；16映射/handler与无profile、六调用写前保护/零关联permVer写、安全插件/6表同源及live/QA证据。只两本人文档写入，未源/构建/初始化重放/DB/HTTP/debug/UI/服务/Git，仍未实施。

### 2026-10-27 用户已选C：仅设计状态与实施门槛复核

已据总控转达在原live-statement设计顶部及第9节记录用户明确选C，不再询问；backend仅User.xml结果映射+必要隔离tests候选锁，待QA安全窗口写，选择不是已修复。原SQL/HTTP/VO/DB/全局Factory/其它Mapper均排除，不转A/B、迁表或认证改造。静态XML解析再次核common13与合并16唯一属性，当前extends仍在；此非初始化/测试证据。

独立最低覆盖表已补：User-before-common旧失败/新通过+正序/普通bootstrap、13公共/16显式/9自动账号列和真实handler、六消费者原code9999/READ_ONLY写前保护及关联permVer零变、repwd不新加规则、注册无profile LEFTJOIN、非Ignore/dataPlugin/6表同源/raw角色/岗位无二次路由、实际factory及QA。已读backend状态未见本轮具体拟测清单，明确这里只给覆盖要求不替其测试方案/结果背书；后续按类用例对照。若新冲突需扩大范围停报root待用户，不因选C擅扩。仅本人设计/status，无源/build/tests/DB/UI/debug/HTTP/服务/授权/Git发布操作。

### 2026-10-27 backend具体bootstrap拟测静态审查

按ponytail-review只读审backend精确准备计划/现fixture，新增三测试Java/旧反例目录审时尚不存在，不称已审实现。设计第10节补运行前必清项：最小白名单context+唯一mockDS，无WebBooter/广扫描/整应用自动配置/真实ConfigData或startup/Redis/Flyway/Hikari；安装auto会收customizer/handler/injector等，须inventory防额外覆盖。VendorDatabaseIdProvider允许mock PostgreSQL metadata，不承诺零getConnection但每次只能fixture对象，拒外URL/未知SQL。

普通resolver保项目实际glob及原顺序，不sort/common预parse；另独立factory才User-first/common-first。旧fixtures XML须改前全字节/hash一致、extends+本地4项，非C16显式map，不进正常扫描/遮蔽生产，不同namespace重复加载；旧查generated无JOIN、新查XML有JOIN/dept，不只context绿。Consumer用实际映射实体测13/16/9/handler/code9999写前零关联permVer、repwd无新规则、无profile/隐藏/删除；原手工fixture仅语义/6表raw/post补证，不替bootstrap。方向合C，细节待补不称Ship或Gradle放行；仅设计/status，无源/测试/构建/DB/服务/UI动作。

### 2026-10-27 C已落盘草稿实际静态复核

已读三新Java、扩Assigned fixture、旧XML反例与生产User.xml，设计第11节替代“仅计划”状态。结论无必须在隔离首轮前修的静态阻塞，可总控派窄隔离槽，但非测试已过/可发布/live修复。生产map16唯一无extends，map外文本与旧反例相同，反例Git内容hash与HEAD原XML同d49f46b18f03777e50ebe86f8c4abaf6316948e6。最小白名单AnnotationConfig context/真实MP auto+config、系统env/properties源移除、唯一mockDS/metadata/拒未知连接/SQL、无整应用/ConfigData/外bean路径已实际落实；NORMAL不customizer/sort/预parse，另独立正反序及旧替换反例。

实际BootstrapTest有generated-vsXML/13父+16子handler/9自动字段/NULL/枚举/无profile/已删隐藏/详情raw-post同事务与源拒；Consumer先实际factory读合成实体再写mock，code9999不是手设实体，repwd原逻辑不新增禁改密。未知Mapper写AssertionError不能被RuntimeException保护断言吞掉。普通完整编辑/无profile管理与既有positive/6表矩阵需原适用回归配合，不称两新Test覆盖全业务。真实API兼容/实际旧反序/bean/ResultSet调用仍待首轮结果，失败不得弱化扫描/guard或删反例。仅两本人文档，未编译/运行/DB/UI/服务/源码/Git写。

### 2026-10-27 QA门禁更正：撤回隔离执行建议

总控转QA mustfix后，依据此前已读实际代码确认Prepared/Statement/ResultSetMeta/ResultSet仍RETURNS_DEFAULTS、setter startsWith(set)宽放行，不满足未知JDBC API失败闭合。全mock无真实IO不等测试能力严格，上轮据此建议可进入首轮不充分，已在设计第12节明确撤回第11节该结论并向root报告。最小限新bootstrap路径精确方法/重载白名单+unknown AssertionError，补写/batch/unknown setter/getter等负例，旧order==null语义不变；不扩生产或全库。

Consumer普通save/role-post/noProfile正向及同namespace/VO等价须精确新旧组合覆盖表，不能拿保护负向或entity映射替代。当前等backend修与QA/root重核，未获执行放行；XML16/SQL未变不能替安全门禁。按新指令不再跑命令/改源，仅已读事实补本人设计/status，无测试/DB/UI/服务。

### 2026-10-27 冻结收口后映射/业务正向复核

按新窄派工只读最新实际Bootstrap/Consumer、User.xml及原saveProfile，设计第13节已补；QA独立核框架，本角色不代其清安全block或授槽。映射仍16，测试列common13 handler/9自动列/NULL枚举/缺tenant列不造0、同namespace list/page与nested引用；新增真实C映射实体+真实UserConvert普通有无profile保存正向，profile缺失仅原显式管理保存补档案，查询仍LEFTJOIN不造数据。role/post正向验证已有一条关系被空集合按原逻辑清除和permVer次数，不宣称非空授权矩阵通过；原适用回归继续。

code9999/READ_ONLY保护仍真实映射输入，repwd不新加限制；真实详情VO有dept/原refs/raw摘要/post及序列化不泄露密码断言，非完整HTTP/Jackson部署验收。未见新确定业务/映射阻断，不扩测试框架；真实编译/bean/API/handler/JDBC/反序重现留QA门禁后受控首轮，未报绿。仅设计/status，无源/编译测试/DB/UI/服务动作。

### 2026-10-27 C验证共享输出阻塞后的隔离路径提案（未实施）

交付 `2026-10-27-c-isolated-build-output-proposal.md`。读backend df529796现场：此前common81完整，auth前sourceReadable却before/after/copy0，只证明共享源瞬态空，写者未明，设置已恢复；不建议盲跑/改Files.copy。现C init37/82/107/108、Python main_outputs与OLD.test_outputs硬编码live build，故只改layout不够。建议新专用helper/init封旧，projectsLoaded项目脚本前给root/全部父叶Project设新RUN/products完整projectpath/build，十模块main/test/generated/tmp/reports及root problems全部核RUN，实际任务仍原10模块/筛选；独立project-cache-dir，离线单worker三阶段不clean/关tracking/改依赖。

所有复制源/清单/testClasses/classpath从SourceSet实际新路径取，不能用旧live inventory函数；旧d仅副本baseline，预期仅User.xml变，其他fresh差异停报不自动收。编译及Test拒live/旧run/旧模块jar，外部离线库锁hash；原classpath策略保固定名单不扩生产依赖，nativevariant若带jar/图变化先停，不放宽Test-only。实际布局要核JavaCompile dest/processor-generated/header、processResources、全部SourceSet/test outputs/报告/插件任务，配置期不提前解析classpath；无业务侧硬编码不等所有插件已迟绑定。

两新测试未见build路径硬编码，正常resolver保持自然新classpath顺序、User/common各一、旧反例fixtures不改，强制正反序仍证明顺序无关；隔离构建不等live加载，禁止产物回拷/IDE/服务操作。当前仅可审新helper准备方案，不授执行；无helper/config/源码/IDE修改、无Gradle/编译/测试/DB/UI/服务/发布。backend只读交接方向一致，已主动报root，不等待轮询。

### 2026-10-27 新隔离辅助实际静态审查（尚有一项阻塞）

已读新Python8596837…、init初B905…及窄修后A0BD3342E2A553CBC96B585103C43138BB6A8457754918402D96DFF71EABC63D，正式提案第9节。早期allProject布局/SourceSet模型/CP任务期核/旧live硬编码移除/SourceSet源码variant例外限输入/单Test任务图方向符合，无clean/关tracking/生产配置写。旧C06A/A053/reference0A85/currentDF6C/guard68417/d-manifestD87E本轮只读hash与原冻一致。

确定误报已由backend四行修：compileTestJava自身main.resources经inside RUN精确加入，原允许目录漏此会拒正常mainOutput，安装JavaPlugin字节码已证。仍阻塞项是Python external_inputs仅阶段内doFirst与结束hash匹配，每阶段结果单独写，无跨阶段累计外部JAR锁；main后缓存同path字节变化可被下阶段新sha接受。最小累计canonical path→首次sha，每阶段前/后核已见，首次test-only新依赖允许加入但重复path不可换，不需扫全缓存/改依赖。已报root，本角色未修helper。

运行API/provider/插件迟绑定、目录缺失/实际CP/root problems位置等留首轮条件验证，不报通过；特别初始化早于projectsLoaded失败时报告未必在RUN，事后检查不等零越界写。意见暂为external累计锁修冻前不执行；只本人设计/status，无helper/源/配置修改，无PLAN/Gradle/编译测试/DB/UI/服务。

### 2026-10-27 隔离辅助累计锁修后终审

已实读Python最终3C944ECBA933068FF58E0513C894D40711D43BB5D741E2252621306E0A44561E与init A0BD3342…，设计第10节关闭9.3阻塞。累计external map仅循环外建一次、每阶段前复核、阶段后同path禁止换sha/仅新test依赖登记+全锁再核并落JSON、最后再核；未重基线。只读确认，不冒充backend合成函数测试为独立通过。连同own-main.resources窄修，未见余下确定静态阻塞，可总控授独占槽一次门禁验证run；不是测试已绿/实际模型已证/live修复，不自动重跑。

运行前仍核最终辅助/批准源输入/旧辅助d哈希，freshRUN与原固定图/筛选/拒绝门禁；实际Provider/variant/API、输出CP/资源/d差异、root problems位置留执行取证，失败停报不降门禁/补旧jar/改IDE服务。只设计/status，无helper/PLAN/Gradle/编译测试/DB/UI/服务操作，主动交root不轮询。

### 2026-10-27 隔离首次配置String.positive窄修复核

总控交首轮main约22s配置失败init221、无graph/model/compile/Test，Problems实际已落RUN根products/_root/build/reports/problems，此非测试通过。已实读init新E1BA9AC37BBDDB2D718C9C35CF778903FECBC927CD55B181333A16F702434EF5，仅collectMany的二元+前移；内存逆向还原得全文SHA精确A0BD3342…，名单/过滤/门禁不变，Python仍3C944ECB…。类似行首+146/184/269均在未闭合括号内，不据此批量改。

设计第11节记可由root授再一次受控配置验证，无确定静态阻断，但不是已动态通过/自动扩后续任务；原唯一槽/新RUN/固定图/哈希边界保留，失败停报。仅只读辅助与本人设计/status，无Groovy/Gradle/helper执行或源/IDE/服务操作。

### 2026-10-27 c7099319配置增强Bean门禁只读设计

独立读main/test-classes gate EXIT0及两实际XML：Bootstrap13/Consumer12共25全fail/0error/0skip，唯一message为Fixture139拒mybatisPlusConfig$$SpringCGLIB$$0，未到Mapper/consumer断言。交付 `2026-10-27-c-configuration-enhancement-guard.md`，按root最新精确授权仅Fixture+必要BootstrapTest候选，其它生产/测试/helper冻结。

最终最小边界按root最新统一：仅显式mybatisPlusConfig→MybatisPlusConfig.class非名单raw类分支；原Annotated metadata/无factory/已解析AbstractBD class、原@Configuration(proxy=true)、directsuper与ClassUtils userClass同Class身份、公开BeanFactoryAware关系全满足才返回原名。内部EnhancedConfiguration marker不要求、不引入或反射加载；安装ClassUtils只按$$返父，不能单用或泛normalize。139定义与143 factoryowner同helper；增强owner仅原interceptor/provider两方法，未知name/metadata/super/公开增强关系/factory/method拒，原其余名单不扩。

已列窄负例和同源owner检查；不用绝对CodeSource路径替来源验证，不改BeanDefinition/proxy政策或生产接口。root已给backend窄写锁，设计完成即交，不等ACK；本角色未改测试/源/helper、未重测/Gradle/DB/UI/服务，实际修差异待只读审。

root后续权威统一已落实到原设计全文，替代交叉消息中曾要求内部marker的口径。当前固定显式配置注册+输入冻结上下文内未见新可触发缺口，不把局部门禁当通用不受信代码沙箱，也不擅扩测试/生产。两文件窄修待backend交差异，无执行动作。

### 2026-10-27 Bean增强两测试窄修实际终审

实读Fixture ADDFD47CA48037AD211A92888291307BEED614C636FC51F060152710C635D564、BootstrapTest9DE3EFE620F4C72766392677558FFD447E118992F7E7809B10BB4F7AB2F92EC7，相对c709锁定输入只此两内容变。实际只exact mybatisPlusConfig增强分支，Class身份/parent/userClass/原metadata/nonfactory/proxytrue/公开BeanFactoryAware全检查；139定义与owner共helper，增强owner仅原两方法，无内部marker/类加载/getBean/包前缀扩大。设计第6节已记可交root/QA授一次新RUN，非编译/动态门禁或C测试已过。

新增小context仅EvaConfig+MybatisPlusConfig，后者仅内存插件/provider及标准事务AOP，无DS/auto/扫描/ConfigData/Redis/任务/网络入口，provider未被查询DB。纯metadata负例错名/metadata/parent/缺公开关系/factory/owner越权方法均拒，负向类不实例化。预计多一@Test不当实际26计数，后续新失败停报不自动扩名单。无源/测试/helper修改，无重测/Gradle/DB/UI/服务，仅设计/status。

### 2026-10-27 d3f8469b前向单结果能力只读核查

独立XML26项2pass24fail/0error/0skip，strict getType与一处包装，未报业务绿。新设计 `2026-10-27-c-jdbc-resultset-capabilities.md`：安装MyBatis3.5.19简单结果skipRows先getType即使offset0，FORWARD_ONLY配现cursor-1/next递增；现004EC…/745F…中间窄修bootstrap无参常量+next前确定值断言已读。后续handleResultSets203必调getNextResultSet261→metadata.supportsMultipleResultSets，源码catch Exception不吞AssertionError，故有确定第二能力而非盲猜。

root已明确合并仅新bootstrap无参supportsMultipleResultSets=false及断言，语义为synthetic单结果不冒称PGdriver；不需多结果/滚动/写能力。git HEAD独立对照原manual getMetaData默认null，C无条件strict对象偏移会把旧NPE兜底变成AssertionError，应恢复order==null原default，仅bootstrap strict。已报root，等最终两文件diff一次审；无测试/生产/helper修改，无重测/Gradle/DB/UI/服务。

最终复核完成：Fixture3EBECA1D…、BootstrapTest22B14EFB…与backend交付一致；内存反向撤销本轮精确增量后，两文件均匹配d3f锁定基线，原未知/写/batch/重载setter负例及其余逻辑保留。基线非presence现存内容仅此两文件变，生产/Consumer/BootstrapFixture/helper冻结。设计第4节记录可交总控依原边界授权一次全新隔离RUN，无静态阻断；未声称26通过，不自动扩名单或续跑。本角色仅写本人设计/status，无构建测试或源文件修改。

### 2026-10-28 固定20类fresh回归辅助待静态交审

总控通知QA已独立复核C26证据；新任务仅审backend新增ignored回归helper/init，不执行PLAN/Gradle或改辅助/源/IDE/服务。目标SYS原9+C2、AUTH原6、DATA原3：固定main10模块→三模块testClasses→严格三个Test，逐模块exact filters，实际fresh XML定计数，不能将旧177与26累加冒充本轮结果。

已读现C辅助相关调用链和QA交接，新辅助尚未交定稿，已向backend请求最终路径/SHA及差异；依赖未满足保持待命，不预判通过。重点核三份prepared-test-output/各自testClassesDirs与resources、每task自身test CP和snapshot主CP、分别保存suite/CP/XML避免覆盖、三任务精确集合与全producer排除，以及旧C/run/seal冻结、全RUN输出/源码与产物/累计external锁原门禁。仅本人status写入，无运行或环境操作。

新辅助833A4293…/4E0F0567…定稿只读审完成，见design/2026-10-28-c-full-regression-helper-review.md。Python/init精确20类一致，当前源码package/class逐项核SYS11/AUTH6/DATA3；三模块各自classes/resources/准备锁/suite/CP/XML分开，图强制恰好三Test无producer。AUTH/DATA确无test/resources，惰性仅省略对应own声明且不存在的根，其它未知/缺失CP仍拒；classes须有正产物且执行前核全部三模块。C26锁非presence内容当前0差，旧C辅助SHA未变。未见静态阻断，可交总控授权QA唯一槽一次全新RUN；不预填计数、不声称动态通过。本角色未PLAN/Gradle/测试或改辅助源/IDE服务。

### 2026-10-28 限定恢复：已授权菜单元数据语义只读设计

仅按总控限定恢复源层分析，交design/2026-10-28-authorized-menu-metadata-boundary.md。PG两个账号未过滤1/SELF后0为backend已执行聚合证据，本角色未复算，不冒称本次HTTP根因。发现listGrantedModules共用于角色授权树，fetchUserModules还供用户资源读取；菜单handleFetchResource按模块全取资源，故不能直接在共享方法加Ignore后称授权集合保持。建议用户先批准“已授权菜单/资源元数据不按维护者SELF裁剪、业务数据范围不变”，再新增专用当前认证用户菜单语句/资源投影，精确方法级豁免，仅返回获授资源，冻结删除及tenant路由须实保，禁止全表/全局/ALL/扩permit。

现菜单直达ModuleService，非UserService既有@Transactional+TenantSchema/schema先授权后core元数据链；不能从历史TenantLine记忆自证隔离。设计列真实插件+合成JDBC正负集合、一般业务SELF仍生效、资源兄弟泄露、身份/有效性及跨租户回归；runtime最终SQL/成功HTTP仍缺。历史20类203通过仅据总控报告保存，不当新方案验证。当前只写设计/status，无源配置/helper/测试/DB/HTTP/IDE/服务操作，不恢复其它暂停事项，待用户决策及总控派发。

总控转达用户已明确批准上述菜单元数据权限边界，backend获授源码实现与轻量静态检查，尚无Gradle/DB/配置/服务授权。本角色恢复限定独立安全静态复核，等待实现定稿后检查共享调用隔离、同模块兄弟资源、有效性、可信tenant/core路由与默认拒绝；已向backend明确交付文件/statement/SHA与路由依据要求。当前所见只有既有C两处diff，新菜单差异尚未交，不预判实现通过、不轮询。若涉及扩大API/DB契约先报总控，不自行实现；其余暂停边界保持，无执行锁或临时设置。

实施前澄清已补设计第6节并报backend/root：资源真实无frozen，纠正前文泛称，资源只deleted、module/role按现有效接口授权冻结删除与tenant交集，不添加假字段/迁移。schema>0可信router事务→租户有效资源ID→core授权交集，旧无role有效性关系查询不可直接替代；schema0当前resolver/login契约均拒<=0，必须默认拒绝，不以0转core授权、不造schema特例；standalone0正常独立。若要schema平台0可用先报契约扩展。只设计/status，无源/测试/DB或运行操作。

六文件定稿独立审完成，实际SHA均同backend交付，见design/2026-10-28-authorized-menu-source-review.md。生产实现未见可复现安全阻断或新API/DB契约，旧共享四Java无diff，精确授权/投影/namespace防回落/双finally符合批准边界，可准备新隔离方案。测试静态缺口已报root/backend：现异常在core前，未测core查询异常与原非空context恢复，建议另授仅测试窄补；当前capture只SQL，ID只是配置绑定，不夸称执行ID。部门空范围与手工TransactionTemplate限制保留。嵌入PG离线/temp路径尚待新方案预检，未运行。本人仅设计/status，无源/辅助/Gradle/DB/HTTP/IDE/服务操作。

测试4B449窄补与26类准备计划已独审补入source-review：真实执行ID栈/prepare记录、core阶段非SQL异常注入及原非空上下文恢复、有效身份无事务拒均已具源码；生产5hash不变。最新loopback四文件8190BB1A/8F3DB6BF/695418BE/E8F612D9，实际替换1/2/1/1，内存逐字节逆改四项均精确匹配修改前SHA，旧三diff无其它增量，默认port0保留。无静态阻断可准备新辅助，但实际四Test图/CP/离线PG资源唯一性/wholeJVM目录/ownedchild（含Fixture构造失败）及监听收尾仍需辅助实现与独立审后另授执行；未跑任何测试或PG。

菜单辅助终审见design/2026-10-28-menu-helper-final-review.md。最小生命周期方案替代曾讨论的Job方向，不做ETW/Job/新依赖；新Fixture初始化失败close/保原异常已补。初版py8BA终态漏未知不可读exe候选为唯一阻塞；窄修后实核最终py B58F948FB3715C6C987DFB8017DBA87293910619D22347C8DAE2F3F6F0480ADA，init5550EC20…及menuTestA78269AB…不变。歧义在读CommandLine前最小输出、未认领completeProof=false并使terminal失败/no kill；明确外部可读PG不泛失败。阻塞已消除，可交root决定一次受控独立执行。root严格保留8实例观察，漏采失败停报非业务回归、不自动重跑/事后放宽；26类实际未执行，本角色未PLAN/Gradle/PG/进程观察或改源/helper。
