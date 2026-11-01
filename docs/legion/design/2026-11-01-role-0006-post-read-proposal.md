# 0006 指定岗位读取资格最小契约（只读提案）

最新实施阶段（2026-11-01）：前后端源码及独审修订已完成；总控依据用户最新明确批准完成现库角色列新增、既有0006恢复和SYSTEM DATA_SCOPE精确单键失效，执行结果398BE19D9DF4FEEE3809197B0C1FA494F548127F5C44398DEAD512BD813AE0C5为DB_AND_SYSTEM_CACHE_DONE。此处原“待实施/未批准”仅表示设计阶段历史，不要求重问已批准事项；后端/QA正在收限定后验，用户新构建重启和运行联调仍未完成，不能宣称线上功能已验收。本角色仅本地证据核对，未重复连接或测试。

最新决策收口：用户已确认沿当前全局scope作用于已有模块权限允许、受数据权限管理的读取；不做菜单资源新绑定、不授edit/delete，其他角色OR保持。此问题已关闭，不再作为待选。以下最终字段以本节补充为准：API使用 `dataPostIds: List<Long>`（前端传十进制字符串ID数组，按项目Long序列化输出），持久化仍 `DATA_POST_IDS TEXT` 规范CSV；不沿用下文早期API字符串候选。API/DDL实施尚待总控一次确认，不执行。

2026-11-01 technical_designer。用户语义已由总控确认：登录用户当前有效岗位与该0006角色配置的岗位集合有交集，才获得该角色的读取贡献；不是按记录创建/修改人的岗位筛选。仅设计，未获API/DB实现批准，无源/迁移/DB/账户/测试操作，不扩0004/0005。

## 现源码与作用边界

当前cornerstone工作区仅保护未跟踪文件，无本角色改源。GitNexus EVA-API索引落后53提交，仅列仓发现，不重建；以现源码为证，vault旧行租户描述也不作为现schema事实。

RoleService.validateDataScope拒0004/5/6，仅0003使用dataOrgIds；RoleEntity/BO/VO、AuthRoleEntity/RoleSnapshot、AuthRole.xml及AuthPermissionContextService链只传组织ID。MybatisPlusDataPermissionHandler把当前全部角色scope取OR，ALL提前1=1，没有按“哪个角色授了该接口”分别过滤；部门/本人条件用于通用SELECT及UPDATE/DELETE处理。资源门禁在此前独立执行，数据范围不授新接口。data开关关闭时插件不注册，不能把0006误称开关关闭也常驻。

多数写服务会selectById取原记录；所以只在SqlCommandType.SELECT时返回0006的1=1仍会让写前读取扩大，进而允许修改。GET也含旧删除路由，不能靠HTTP GET判断只读。独立审计、字典共享元数据等现精确scope规则继续原政策，不借0006绕过。

## API与DB字段

推荐沿现dataOrgIds的字符串接口风格，新增独立 `dataPostIds: String`，内容为正Long十进制ID逗号串，落 `SYS_ROLE.DATA_POST_IDS TEXT NULL`；绝不复用DATA_ORG_IDS。RoleAoeBo、RoleDetailVo、RoleListVo、RoleEntity及MapStruct同名读写；AuthRoleEntity、AuthRole.xml、RoleSnapshot和其适配器接同字段。前端表单用字符串ID数组、多选列表，service提交规范逗号串，不能经JS Number丢长整型精度。接口URL/method、资源授权和角色冻结守卫保持。

新建/明确切到0006必须非空集合，未知/非正/溢出/重复/空段ID全批拒，规范排序后保存；编辑原0006且未传dataPostIds表示保持原值，但仍重新验证其有效性，显式空串拒。dataScope未传保原scope和两集合，不能因默认空值扩大权限；切离0006清空dataPostIds，0006清空dataOrgIds，0003部门字段按原规则；非所属scope提交非空对应集合拒而非悄然混用。旧角色NULL值不回填岗位、不改为0006，不额外绑定用户。

岗位保存集合必须全部为当前主体有权选择的本租户有效SYS_POST，逻辑删除拒、frozen1拒、9999允许引用但不授编辑岗位；不自动包含上级/子岗位，选中哪个ID就匹配哪个。standalone明确配置schema且post/ref归属0或NULL沿既有单库契约；schema模式可信正tenant映射/实际post与postuser_ref/role同私有schema，不能search_path回落或按客户端tenant/schema查。选择集与实际本源集合精确相等才提交，拒一个就角色/岗位配置/permVer全部零写。无权读取岗位列表时前端显示不可配置，不给ALL或伪造候选。

主初始化单V1与租户单模板的SYS_ROLE各加该列，不加V2等Flyway链，不新增关系表。现开发库适配必须另外提交精确ADD列和现有字段不变/回退计划，不能重放单V1、repair/baseline或借此前字典适配执行授权写这列。

## 当前用户岗位与角色并集

认证重建上下文时，以可信uid及当前物理租户，从SYS_POSTUSER_REF JOIN SYS_POST取得当前有效岗位（deleted0、frozen0或9999），不用JWT自带岗位、客户端postIds、角色名或管理页面选中值；私有表无tenant列时以schema归属验证隔离，不造列。查询限定当前主体、固定字段，不用受个人作者scope裁剪的管理岗位查询生成资格，避免自递归。角色自身还须有效且有实际membership；AuthRole.xml当前无deleted过滤，新0006读取贡献必须独立保证已删角色不能经旧快照参与。

ThreadUser.DataScope增加独立postIds及该角色资格结果/必要roleId，不污染orgIds，旧两参构造兼容；AuthPermissionContextService解析和适配器扩展，形成请求内可信快照。0006空/非法集合、无有效岗位交集或资格查询错误都不产生读取贡献，错误不回退ALL。

每个角色贡献仍OR：读取中0006命中贡献1=1，不命中贡献0；其它SELF/部门/CUSTOM/ALL按现逻辑贡献。没有命中不全局拒掉其它角色；ALL仍按现规则。既有资源授权仍需通过。不能将0006额外变成全局岗位必需条件，也不能默默改成逐资源角色绑定。

## 仅读取，不授写

推荐0006只在明确登记的读取service链生效，至少同时满足实际只读事务及真实Mapper SELECT，且没有上层写操作意图。写服务内部selectById、UPDATE/DELETE/INSERT、旧GET删除路由均不得获得0006贡献。实际实现可用一处窄同步读取意图上下文/注解配拦截器确认MappedStatement.commandType，不按方法名select前缀或URL猜；退出finally恢复，不复用审计特权窗口。无明确只读边界的查询先不获得0006贡献，需按真实调用清单补读入口，而不是全局放行。

写入仍仅由其它已存在的范围与操作权限决定：仅0006角色即使岗位命中，也不因此变成全数据编辑者；有其它SELF等角色则保原可写范围。有独立批量写或直接更新入口必须同样验证不能借0006扩大，不宣称单selectById检查涵盖所有写。该读/写贡献拆分是现通用dataScope的新行为，需要用户确认后才能实现；不将0006简单塞原ALL分支。

## 失效与UI

RoleService.editUniqueCode现更新角色后对已授用户incrementPermVer，新岗位集合变化沿同一事务刷新；UserPostRefSerivce.savePosts在关系差异时已有incrementPermVer，保持。PostService.del/switchFrozen当前未见相应权限版本刷新；需捕获受影响岗位（包含真实级联范围）关联用户，在同事务增版本，失败全回滚。即使版本传播尚未完成，下一请求的有效岗位查询也必须排除已删/冻结岗位，不能只相信旧membership/缓存。JWT过滤器现每次校验DB permVer并构建用户上下文，无需新增JWT岗位claims；旧token版本失配沿原策略要求重登。允许正在执行请求持其请求内快照，不承诺中途撤销事务。

岗位资格不新增跨请求共享缓存；如现角色/资源缓存涉及此字段，按原permVer/角色变更链精确失效，不全清Redis、不强退无关会话。旧岗位配置保留但岗位变无效时角色读取贡献立即失效；再次保存配置须重新选择有效岗位，不自动替换成父级或其它岗位。

前端仅role右侧新增岗位多选区，选择0006及编辑原0006默认展开/回选，0003部门选择保持独立。复用已存在GET /sys/post/list的真实岗位树返回id/title/pid等字段，必要在apis加已有路由常量而非新接口；无权/错误不回显伪候选。前端只负责交互，后端重新核全部集合。不做主题/尺寸/像素补验或扩大新功能。

## DATA_SCOPE与最小验收

新初始化SYS_DICT_ITEM仅增加0006“指定岗位”一项，既有5个已实现值保留，0004/0005不种入、不删枚举、不扩其行为。实际新ID、mainId及sort沿现单V1批准种子规则核唯一，不猜现库可用ID。开发库DATA_SCOPE值的新增是独立精确数据适配，须核主项/原5项/目标ID前像并另批；此前category列+9主项映射的保数据适配不能夹带此项，也不重跑V1。事务提交后仅该真实源DATA_SCOPE缓存按原流程刷新，数据未批准前UI不能伪造服务字典返回值。

必要针对性用例仅：角色0006集合保存/保持/切换/外租户及非法全拒、真实当前岗位交集正负/删除冻结/9999、0006+SELF与ALL并集、列表详情count一致、只读命中但所有写路径不扩大、岗位与角色改动下一JWT失效、右侧0006编辑展开回选及取消/错误保稿。优先复用现测试与租户隔离fixture，不重跑已绿全仓或视觉任务。

## 总控需一次确认的确切增量

语义“登录用户有效岗位交集获得读取贡献”已定不重问。建议确认：新增DATA_POST_IDS/角色API字符串字段与单初始化结构；只在显式只读链贡献并与其它role原范围OR，写不借0006扩权；沿现dataPermission总开关（关闭仍原全局禁用数据范围）及现全局scope对各已授权资源生效，不新增逐资源绑定；现库字段/字典0006精确适配分开审后执行。

如用户希望0006岗位资格即使关闭data开关也常驻，或仅作用该角色授予的特定资源，这是独立于原全局范围的新契约，必须说明再确认，不能本轮暗改。当前提案不实施、不操作数据库、不建测试账号，C3及无关历史任务不并入。

## 最终最小实施清单（替代前文未决候选）

1. SYS_ROLE仅新增DATA_POST_IDS TEXT NULL；核心唯一V1及租户唯一模板各加同列，不新增Flyway版本链/关系表。现库ADD列及DATA_SCOPE字典0006新增分别给精确前像与回退，另批执行，不能夹入已批category九行适配。
2. RoleAoeBo/RoleDetailVo/RoleListVo新增List<Long> dataPostIds，RoleEntity/AuthRoleEntity/RoleSnapshot为String CSV，RoleConvert显式List↔CSV；AuthRole.xml补该列，快照适配器及ThreadUser.DataScope独立postIds，不混orgIds。前端service数组与后端API一致，不双支持两种入参类型。原dataScope未传保持原scope和两集合；原0006岗位字段省略保持，明确空数组拒；切离清空岗位字段，显式非0006非空岗位集合拒。集合全量正ID/唯一/本源有效校验后才允许角色和permVer写。
3. 读取用途采用一个窄同步窗口，入口仅实际纯读取controller方法标记（明确list/get/tree/count等，由backend当前sys调用清单逐项绑定，绝不根据GET或方法名推断），内部还要求真实只读事务及MappedStatement命令SELECT。窗口绑定可信主体，finally恢复；UPDATE/DELETE及业务写controller从未进入窗口，写service内部的selectById不因SQL是SELECT获贡献。不能只给get service本身加注解，因为写流程可能复用同一get。若现纯读service没有只读事务，须只在确认读入口补该事务/边界，不无差别开放。旧GET del/edit不标记；异步不传窗口；当前未覆盖的读入口0006不贡献，覆盖清单完整后才称该模块支持。
4. handler对0006仅在上述窗口且有效岗位交集非空时追加true分支，窗口外/写命令/无效交集只不给该角色贡献；其它role ALL/SELF/部门/CUSTOM OR按原逻辑。账号、角色membership及岗位每请求从当前可信schema查有效状态，严格过滤role.deleted，不能用旧快照遗漏deleted来授新0006。所有原资源/租户/审计额外门禁保留；现dataPermission开关策略不变，不引入关闭后仍常驻的新门禁。
5. 角色岗位配置变化沿现RoleService受影响用户permVer；用户岗位绑定变化复用现savePosts增版本。PostService.del/switchFrozen新增受实际影响岗位（含级联）的用户permVer同事务推进，先采用户集合再删refs；无需新增JWT岗位claims或跨请求缓存。AuthPermissionContextService每请求一次查有效岗位集合，所以冻结/删除后下一请求不能靠旧token岗位快照继续通过；版本失配沿原00023要求重登，错误不回退ALL。
6. 前端role右侧岗位树/列表复用GET /sys/post/list及原资源门禁，0006选中/详情编辑默认打开，保存数组，不调用新端点；部门0003独立。0004/0005仍拒，字典新初始化为原5码加0006，枚举不删不扩其余语义。现库字典新增必须另批且只定源DATA_SCOPE单键提交后刷新，不运行初始化覆盖现库。

具体拟动：sys-domain角色BO/VO，sys-service RoleEntity/RoleConvert/RoleService及PostService、现岗位引用服务；auth RoleEntity/XML/RoleSnapshot/适配器/权限上下文的本源有效岗位查询；common ThreadUser.DataScope，data handler及窄读窗口/入口标记；核心/租户单V1、role前端data/service/aoeform与现岗位选择器接线。没有新业务接口或全站框架；backend须交纯读入口与写预读负例清单作为源审附件。

总控可一次确认：**“批准角色接口新增dataPostIds数组、SYS_ROLE新增DATA_POST_IDS并接入单初始化；0006按已确认登录用户有效岗位交集，仅在明确纯读入口/只读事务/SELECT贡献全局读取范围，与其它角色OR，不增加写权限；接入角色及岗位变化permVer失效，复用现岗位列表右侧多选。现库ADD列与DATA_SCOPE新增0006另列精确适配后批准执行，不重放V1、不扩0004/0005。”** 用户读取语义/全局并集已定，不能再次问同一选择。本节仍是待实施契约，不自行写源/DB或测试。

## 纯读取入口清单与写前读取反例（后端源码补证）

后端成员已逐项核对服务事务与调用，本角色复核控制器映射。首批明确登记以下16个端点，仅标记对应controller方法，禁止标记整个controller类。表中均为GET；GET本身不是读取资格判据。

| 控制器 | 端点 | 服务读取方法 | 事务处理 |
| --- | --- | --- | --- |
| OrganizationCtrl | /sys/organization/list、/sys/organization/get/{id} | OrganizationService.list/get | 已有只读事务 |
| PostCtrl | /sys/post/list、/sys/post/get/{id} | PostService.list/get | 已有只读事务 |
| RoleCtrl | /sys/role/list、/sys/role/get/{id} | RoleService.listPage/getRole | 已有只读事务 |
| UserCtrl | /sys/account/list、/sys/account/get/{id} | UserService.listPage/getUser | 已有只读事务 |
| NoticeCtrl | /notices、/sys/notice/list、/sys/notice/get/{id} | NoticeService.listAvailable/list/get | 已有只读事务 |
| DictCtrl | /sys/dictionary/list、/sys/dictionary/get/{id}、/sys/dictionary/get/type/{code} | DictService.listWithCapability/getDict | 已有只读事务，保留category与SYSTEM原资格 |
| ModuleCtrl | /sys/module/list、/sys/module/get/{id} | ModuleService.list/getModule | 当前无事务，实施时补实际只读事务 |

最终读取判据固定为三者同时成立：该登记方法进入的可信同步窗口、实际事务已激活且实际readonly、当前MappedStatement为SELECT。只读service注解不自行打开窗口，REQUIRED写事务调用只读helper不得改变实际用途。禁止在StdService通用get/list上全局开放0006；未列继承读入口需完成调用核对后逐项登记。窗口finally清理、代理顺序、嵌套事务和MP分页count一致仍须实施后针对性验证，本清单不表示已实测通过。

明确负例为DictCtrl.java:139的GET /sys/dictionary/del/{id}，其调用delDict属于写操作，不登记。所有edit/del/sort/switch/frozen/grant/grantPost/repwd及其前置SELECT均不登记；checkUnique默认保持原范围，不因其执行SELECT获得0006贡献。OrganizationService.edit:128与get:153共用selectById，必须作为同一statement在读写两路径范围不同的回归样本。RoleService.ensureRoleEditable、UserService及岗位/角色引用save、PostService、ModuleService、NoticeService和DictService写事务内预读亦无0006贡献，其他role原范围继续OR。

RoleCtrl.fetchResource/listUser、AuthCtrl.fetchMenus/fetchDicts、DictCtrl.query/validate沿现专门资格，不通过0006无差别扩大授权面板或bootstrap能力。Mapper的既有Ignore、sys_module_resources排除、日志专用窗口和租户门禁保留。全局受管读取决定不变；此16端点是当前七域读取接线清单，不宣称它覆盖未核模块或原先不受数据插件管理的查询。实施者若发现其它现有受管纯读取入口，必须补列并接线后才能宣称全范围完成，不得暗中改变用户已定全局语义。
