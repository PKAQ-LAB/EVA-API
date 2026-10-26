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
