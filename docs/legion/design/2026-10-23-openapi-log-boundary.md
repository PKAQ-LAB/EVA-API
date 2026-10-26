# OpenAPI调用日志：读取边界与归属待决说明

- 日期：2026-10-23（Asia/Shanghai）；technical_designer；唯一总控01a0bc51-e17e-7990-ad76-13c63f385f33。
- 范围：只读当前差异、后台状态、QA矩阵、源码和git历史；只新增本文并追加本人状态卡。无生产/API/DB/授权写入，无HTTP、浏览器、服务、测试构建、依赖、提交/push、子代理或云端JEV。
- 证据等级：**存在可定位的隔离/归属缺口，但未做HTTP/数据库复现，不认定已有越权、泄露或实际日志写入失败。** 用户尚待重启后台加载已修代码，不能把磁盘源码等同运行行为。

## 1. 结论与可执行顺序

OpenApiCallLogService的get/list没有显式tenant谓词、可信路由或日志读取守卫；API_CALL_LOG也不受已补biz/error专用P0守卫保护。通用数据范围过滤不是租户隔离，关闭该开关时连拦截器都不注册。因此biz/error的代码验收结果不能外推到本路径。

历史与当前实现集中查询应用凭据、记录外部调用，较接近集中审计实现；但没有明确“仅平台全局”或“租户可看本租户”的产品约定。凭据实体继承tenantId，实际传入认证核心的AppCredentialSnapshot却无tenantId，日志写入未设置tenantId；不能只在读侧补tenant_id=currentTenant就声称解决归属，也不能把NULL一律解释为平台0或当前租户。

推荐总控先派**最小失败闭合保护**：读入口验证可信身份及明确审计资格，缺身份/缺范围拒绝；不在save套相同守卫。不改变已有正向可读策略、不假称ALL具有跨租户权限。随后就第5节的全局/租户归属与权限开关政策一次确认，再派精确读写归属实现。若无法证明某正向请求的归属边界，安全临时措施应明确拒绝该路径并报告功能受限，不能以全局Ignore掩盖。

## 2. 当前证据与历史

下表auth路径前缀为`eva-web/eva-web-auth/src/main/java/org/pkaq/core/auth/`；data路径前缀为`eva-core/eva-core-data-mybatis/src/main/java/org/pkaq/`。

| 文件/行 | 已核对事实 | 边界含义 |
| --- | --- | --- |
| auth `audit/openapi/ctrl/OpenApiCallLogCtrl.java:22/29/35` | 无条件RestController，GET /monitor/log/openapi/get/{id}、/list；没有前台新增/编辑/删除 | 当前classpath扫描可形成读路由，前端无菜单不证明HTTP不可达；实际Bean/请求仍待现场验证 |
| auth `adapter/mybatis/audit/service/OpenApiCallLogService.java:46/47/56/59` | get直接selectById，list直接selectPage；过滤appKey/appName/path/success/时间，未加tenant或操作者条件；无TenantSchema/事务注解 | ID与筛选项都是查询条件，不是归属授权；分页自动count同样依赖原wrapper，无单独安全count边界 |
| 同文件`:86/94/103/104` | save设置应用、路径、状态/耗时/IP/errorMsg和utcCreate后insert；未设置tenantId或可信操作者，异常只warn | 无法从此路径证明行归属；save成功/失败不能只看主API是否成功 |
| auth `adapter/mybatis/audit/entity/OpenApiCallLogEntity.java:16/18` | 表API_CALL_LOG继承StdEntity；Mapper为普通BaseMapper，无Ignore | 与biz/error不同，本表确有标准审计列，不应套“不存在CREATE_ID列”的根因 |
| `web-booter/src/main/resources/db/migration/V1__INITDB.sql:449–476` | API_CALL_LOG有nullable TENANT_ID、CREATE_ID/MODIFY_ID和UTC字段；无本段tenant非空约束/归属索引 | 只是源码DDL，不是本轮实库结构；nullable不是“全球可见”的授权规则 |
| data `config/MybatisPlusConfig.java:39–47` | 仅dataPermission启用时装DataPermissionInterceptor，之后分页、乐观锁，无TenantLine | 拦截器内部的disabled前置guard在生产不装拦截器时不会运行 |
| data `config/MybatisPlusDataPermissionHandler.java:54–67/89–95` | 专用guard只列log_biz/log_biz_archive/log_error及其Mapper；API_CALL_LOG走普通规则，ALL返回1=1，无当前ThreadUser返回null | ALL不自动限制tenant；无ThreadUser也不是通用拦截器的拒绝判定。实际外层认证仍可能拒请求，不能据此直接宣称匿名可查 |
| data `config/MybatisMetaObjectHandler.java:19/29–39` | 明确不填tenantId；按ThreadUser填创建人，没有用户时helper给-1/空名称 | StdEntity有字段不等于写入tenant有值；外部应用也不是人类操作者，CREATE_ID不能充作租户或App所有者 |
| auth `openapi/filter/AppKeyAuthenticationFilter.java:105/157–174/181` | 匹配OpenAPI路径才执行；认证为AppCredentialSnapshot+ROLE_API_USER，未建立ThreadUser/tenant事务；成功链返回后或异常时调用IOpenApiAudit.save，credential可null | 审计写入不依赖JWT身份；业务方法结束已恢复的路由不能假设仍有效。失败在凭据前的调用尤其无可信tenant |
| auth `spi/model/AppCredentialSnapshot.java`；`adapter/mybatis/openapi/service/AppKeyService.java:27–40` | 快照无tenant/owner字段，findCredential只拷应用、安全配置；凭据Mapper按app_key查询 | 应用与tenant关系在该认证契约中未传递；不能拿请求header/客户端targetTenant自行补归属 |
| auth `authentication/filter/JwtAuthFilter.java:183–200` | 管理JWT请求构建ThreadUser、资源判定后以runWithUser包住chain | 权限上下文在chain有效，但认证时的TenantAuthRoutingService只路由凭据/角色查询并finally恢复，不是全请求schema路由 |
| auth `authorization/service/AuthPermissionContextService.java:56/68/98–101` | resource/data开关与platform决定是否加载角色；data关闭时dataScopes明确List.of() | 身份已认证不等于有ALL审计资格；直接复用requireAll时data关闭会失败闭合，而非自动允许 |
| `db/migration/V7__API_RESOURCE_PERMISSION.sql:24/25` | 有API_LOG_OPENAPI、方法*、/monitor/log/openapi/**资源种子，隶属既有日志模块 | 有种子不证明现场授权；旧宽*也可能命中，精确路由不存在菜单不等于拒绝 |
| `db/tenant-migration/V1__TENANT_SCHEMA_BASE.sql`及V2文本检索 | 未发现API_CALL_LOG或api_app_credential初始化 | 与“每tenant私有调用日志表”不吻合，但不能据此单独宣布全局语义；实际schema/旧手工表未查 |

历史核对：`50868232`的原`openapi/log/service/OpenApiCallLogService.java`已是无tenant条件的get/list与save；`83603115`主要调整IP解析；`7e060637`将服务移到adapter并改SPI快照，本次读侧/写侧缺口并非当前biz/error P0补丁引入。没有找到宣布OpenAPI调用日志平台全局或租户私有的明确历史契约，继承StdEntity与集中放置只能作为结构线索，不能代替产品决定。

已读后台状态`P0-ROLE-SCOPE-LOG-QUERY-GUARD（2026-10-23交锁）`及最新sys/log补充说明。biz/error已按原设计补LogQueryAccess，尚待现场加载；其专用条件不含API_CALL_LOG。GitNexus本轮query未得到此调用完整process，索引仍落后21次提交，已用当前文件及git历史补核，不声称当前图完备。

## 3. 实际可达性与开关矩阵（源码预期，未HTTP验证）

默认配置OpenAPI路径为`/apr/**`，管理日志路径`/monitor/log/openapi/**`不在anonymous/permit中。正常管理读取预计走JWT认证及资源判定，不是提供AppKey就默认能读。OpenApiCallLogCtrl无额外模式/角色注解；WebSecurityConfig的anyRequest.authenticated只保证认证，具体资源许可在JWT过滤器判定。若用户改了认证匹配范围，须按实际配置重新验，不能假定默认路径。

| 场景 | 当前源码预期 | 缺口/未知 |
| --- | --- | --- |
| standalone，data开，JWT+资源有权 | 普通dataScopes按CREATE/MODIFY过滤；ALL不追加owner限制；get/list/count在默认连接schema读同表 | 无显式tenant0条件；外部调用创建人可能-1，个人scope不表达应用归属；实库行/真实返回未知 |
| schema租户，data开，JWT有ALL+资源 | 权限角色读取可在tenant事务中完成；进入本日志Service未建立租户路由，ALL=1=1 | 读取到哪个schema取决连接默认/外层事务，不能承诺本tenant表；若共享表有多tenant行，查询缺tenant谓词有越界风险，未证明现场存在这些行 |
| data关、resource开 | 生产不装DataPermissionInterceptor；权限上下文有角色但scopes空；通过现资源校验后直接Mapper | 无数据/tenant谓词兜底；interceptor单元测试“disabled仍guard”不足以覆盖生产Bean未装链 |
| data/resource均关，JWT仍开 | 身份认证仍执行；standalone/非platform常无角色/scopes，ResourceAuthorizationService开关放行 | 任何已认证人是否允许全审计没有明确契约；当前Service没有独立审计资格检查。不能把“关闭数据权限”解释成批准全日志公开 |
| authentication关 | WebSecurityConfig默认非anonymous请求denyAll | 不能绕过配置把此场景记为已泄露；实际端点拒绝待QA |
| AppKey成功/失败调用写入 | 仅匹配OpenAPI路径且认证/调用条件成立才审计，save无读取guard、没有可信tenant赋值或专用事务路由 | 元数据自动填充不补tenant；失败凭据null无法推导tenant。未验证表存在/事务/持久化成功 |
| 自定义配置扩大OpenAPI认证匹配范围 | AppKey主体可能参与新的路径认证，但无ThreadUser/人工角色scope | 这是条件性配置风险，不证明默认管理端能被AppKey访问；应与JWT、filter顺序共同测试 |

QA矩阵`quality_reviewer-sys-log-matrix-20261023.md:25`明确本模块缺入口/安全合成调用、直口负向及租户范围未测；仅管理员身份，缺合法受限账户/另一可信tenant。本文不改该矩阵，不将源码分析或其他日志97项代码测试改写为此模块运行通过。

## 4. 可直接补的最小失败闭合保护与其限度

1. 在OpenApiCallLogService.get/list、Mapper前统一复核可信管理ThreadUser（userId>0、tenantId>=0）、明确审计范围；可复用现LogQueryAccess.requireAll，不从请求字段或ROLE_API_USER伪造ALL。缺身份/空或不支持scope直接现PERMISSION_DENY，detail/count/page均不执行。保留URI、BO/VO和Response形状。
2. **此守卫只缩小无资格请求，不能解决ALL跨tenant。** 普通租户ALL只表示租户内数据范围，不是平台全球审计权；归属未定前不能宣布补requireAll即隔离完成。对无法证明归属的tenant正向读取保持不开放/明确暂时拒绝，比拿ALL放共享全表安全；是否将原有租户读取收口到平台专用属于第5节决定，本文不自行实施。
3. guard放在Service入口，不能只扩MybatisPlusDataPermissionHandler日志表集合：data关时生产不装该Bean。若另做Mapper防御必须配真实配置装配测试，不全局Ignore/excludeTables，也不在DataPermission关闭时自动构造ALL。
4. 读保护只作用get/list，不套到IOpenApiAudit.save；成功/失败/未知App调用仍应按原失败不影响业务的契约审计。不能为了“可信用户必填”让匿名失败审计或后台任务抛异常破坏调用。
5. 参数界限不变：id非法/分页越界按现校验处理，appKey/appName/path仅缩小查询，不承载授权。复用同一可信归属谓词到page/count/detail，不能列表安全、按ID越界或total泄露。不要引入客户端可随意指定的tenantId字段作为临时修复。
6. 不修改凭据、角色/tenant授权、schema或旧日志归属；不把历史NULL tenant/CREATE_ID=-1归给当前用户或tenant0。旧日志缺归属的展示与保留策略先审阅，安全修复不能静默搬迁/覆盖旧数据。

## 5. 最小决策选项与推荐

| 决策 | A：集中管理面审计（推荐最小方案） | B：租户可看本应用调用 |
| --- | --- | --- |
| 记录归属 | OpenAPI外部应用调用集中审计，不把CREATE_ID当人类所有者；standalone唯一schema，schema模式显式core存储 | 仍推荐core共享审计表+可信tenant_id行归属，比给全部tenant复制表简单；必须从已验证应用凭据传可信tenant/owner |
| 读取正向资格 | standalone有效ROLE_ADMIN+日志read资源；platform可信tenant0管理员/批准审计能力+read资源；普通tenant不开放全局调用日志 | 本tenant明确审计读授权+可信tenant条件；平台跨tenant必须独立能力和显式目标规则，不能租户ALL变global |
| 失败调用 | 未识别或未通过可信凭据验证的调用归平台未知来源审计区，仅平台审计可看；不能相信自称的appKey/tenant header | 同样保留未知区；仅凭未验证签名的AppKey不建立租户归属。明确要把哪一认证阶段的失败归租户需另审规则 |
| 必须新增契约 | 明确原本未声明的“平台审计only”及配置关闭时资格；显式存储路由、历史NULL如何看不凭空改值 | AppCredentialSnapshot可信tenant字段、来源校验与迁移、成功/失败归属、读tenant谓词与历史未知策略，新增契约与数据变更更多 |
| 当前数据影响 | 不改历史tenant值，不把NULL批量改0；批准后可在平台审计视角展示旧集中记录，需先确认实际存储位置 | 现有行不得按appName/operator文字回填；能由批准可信应用映射证明的行另行清单回填，其余仅平台未知区；不得塞给当前tenant |

A是推荐，不是已确认的历史语义，也不等于当前已安全。若用户已有租户应用自治需求，选择B；不能因成本更低在代码里先决定A。

第二项独立决定：**dataPermission关闭时审计仍怎样授权？** 推荐敏感调用审计保持独立资格检查：数据权限关闭只停止通用行scope规则，不解除管理身份/日志read资源及存储归属。若批准A，则按真实服务端ROLE_ADMIN/平台能力窄校验，不合成scopeALL；若仅先复用requireAll，则data关闭时scopes为空而拒绝，是有意临时失败闭合，并须明确提示该配置暂不支持。不能暗改AuthPermissionContext全局角色/scopes装载来绕过开关；选B还须明确租户审计角色与范围。resourcePermission关闭也不能自动允许全部认证人看审计；能否关闭独立审计资格须用户明确，不由泛开关推断。

总控一次性最小问题可收口为：**“OpenAPI调用日志按A集中管理员审计，还是B租户查看本应用？推荐A，并在数据/资源权限开关关闭时仍保留独立管理员审计资格；旧归属不明记录不回填、不向租户展示。”** 未答前不扩API/迁移/授权；可以先实施第4节缺身份/缺范围的否定guard，但ALL的tenant正向必须继续列未闭合。

## 6. 需后续写者与QA覆盖的验证（本轮均未执行）

- 真实Bean装配的data开/关×resource开/关，不能只直接new拦截器测试；验证不装拦截器时Service仍拒无审计资格、count/page/get Mapper零调用。
- standalone普通/管理员、platform tenant0、tenant A/B的合法受限身份；ALL、受限、多角色、缺scope/无ThreadUser、AppKey-only与JWT分开。默认/自定义认证路径、外层资源拒绝、宽*命中均留证，不用菜单不存在替代HTTP拒绝。
- 选A后验证显式core/standalone路由、普通tenant不可读；选B后用批准的合成A/B日志检验tenant+应用权限谓词作用detail/page/count，外tenant已知ID无返回/无total泄露，AppKey筛选只能缩小。
- 调用写入覆盖成功、业务异常、无凭据、签名失败、时间戳非法；可信/未知归属、写入schema与读侧一致，不因chain结束ThreadUser/路由恢复丢归属；存储失败仍不改主调用原响应。只用无秘密合成字段，不读旧IP/errorMsg个人内容。
- 生产初始化/tenant版本不一致、表缺失、NULL tenant旧行、被删应用、AppKey重复/变更等先盘点再测，不借验收生成凭据/扩大真实授权；不得临时用SQL伪造成功调用日志。
- 已修biz/error与角色守卫回归，登录/在线日志单独契约不受此文泛化；构建/测试及运行加载分别留证，当前用户服务不由本成员重启。

实施影响预计仅auth内OpenApiCallLogService/必要凭据SPI/查询条件及对应测试、经确认后的core路由与独立审计资格校验；A/B未定前不列具体迁移执行。新身份能力/角色授权、schema变更、历史行归属回填都须具体批准，现有业务读取guard不能作为其授权依据。

## 7. 最小实施收口：复用既有管理员，不新增角色/ALL

用户总括要求“完成这些”尚未明确选择A/B，本节只补具体可执行建议，不将其当精确归属契约批准。

**确认前可做的原接口否定保护**：get/list在Mapper前拒绝缺可信ThreadUser、userId<=0、tenantId<0；不能以SecurityContext存在或ROLE_API_USER作为人类审计身份。可复用现requireAll拒空/受限scope，且明确这是临时失败闭合，不是最终集中审计资格；data关时空scopes拒绝，不自动补ALL。参数错误与拒绝应保持现Response错误语义；save不加此读guard。租户ALL正向仍未闭合，不能为收口把未确认的tenant读取判为已安全。

**若一次批准推荐A，最终资格使用现有权威来源**：

| 对象 | 实施规则与已有来源 |
| --- | --- |
| 管理员角色 | CommonConstant.ADMIN_ROLE_NAME=ROLE_ADMIN；通过服务端SYS_ROLEUSER_REF→SYS_ROLE.CODE的真实有效关系核验，不按账号admin、前端canAdmin、客户端/JWT自报角色或dataScope=0000判管理员 |
| standalone | 运行模式确为standalone、可信账户身份有效；在唯一受信schema验证该账户的有效ROLE_ADMIN关系，才可进入集中审计读取；不要求给其新授ALL |
| platform | 运行模式platform、可信tenantId=0、在core验证有效ROLE_ADMIN；普通正tenant即使也有ROLE_ADMIN或ALL也不能变平台审计员。租户TENANT_ADMIN不是平台ROLE_ADMIN |
| 平台能力 | 现有PlatformCapabilities只有TENANT_INSPECT，没有OpenAPI审计能力；不得借租户查看能力扩成看全调用日志，也不为本次新造capability/审计角色 |
| 角色装载 | AuthPermissionContextService.findRoles:68在standalone两开关都关时直接空；ThreadUserHelper.isAdmin只看当前roleMap，不能因为空就放行或全局强制加载scope。需要时仅在本审计get/list入口窄查既有角色关系，查不到/异常则拒绝 |
| 有效性缺口 | MybatisPermissionSnapshotQuery.findRoles复用AuthRole.xml:4–19；该自定义SQL只有R.FROZEN!=1与用户关联，**没有显式R.DELETED=0**。不能直接将“查到ROLE_ADMIN”声明为有效管理员；审计资格查询须明确逻辑未删、未禁用、有效关联；如复用原查询须先补有效性并做影响检查，或用同一适配器窄查询，不能因复用而接受已删角色 |
| 路由 | standalone查唯一schema；platform tenant0查显式core。TenantAuthRoutingService按正tenant路由且TrustedTenantSchemaResolver拒<=0，不能直接execute(0)当core捷径；复用CoreSchemaExecutor在实际只读事务中访问、finally恢复上下文，不用客户端schema |

**开关只影响原通用能力，集中审计管理员门禁常驻**：推荐将本次单项政策精确定为“data开/关均不改变管理员资格，也不把空scopes变ALL；resource开时仍执行原JWT日志read资源判定，resource关时只跳过该资源匹配，仍必须经上述有效管理员检查；authentication关闭仍遵守原非匿名拒绝”。这样复用既有ROLE_ADMIN与资源开关语义，不要求新角色、新授权行或新全局权限框架。此方案细化并替代第5节笼统的“开关关闭仍保独立资格”：独立资格就是有效管理员，不是偷偷保持全局resource开关或发明新的ALL scope。若用户要求resource关闭后仍核每条资源，那是另一更强政策，本次不暗加。

集中模式正向读还需按批准A将日志访问固定到standalone/core，detail/page/count同源；不靠tenant0谓词静默丢弃旧NULL归属，也不把NULL回填0。外部调用审计save维持系统内部入口及原失败隔离，集中存储路由需和读同源；旧记录位置/迁移另用精确盘点计划，不因A批准就批量搬旧日志。

**最少且唯一须用户决定的完整一句话**：

> 是否采用A：OpenAPI调用日志集中审计，仅standalone有效ROLE_ADMIN或platform可信tenant0有效ROLE_ADMIN可读；该管理员门禁不受data/resource开关关闭影响，resource开启仍按现日志资源授权、关闭只跳过资源匹配；普通tenant暂不提供本应用视图，旧归属不明记录不回填？

批准这一句即可实施已有角色资格与集中读取的最小闭环，不用分开再问角色名、默认ALL或UI选择；不批准A而要租户自治则走B，不能由开发自行挑。影响明确：普通tenant不能查全局调用日志；data关闭的管理员可在真实角色窄查后通过，普通已认证用户仍拒绝；不会修改任何已有用户/角色/资源授权记录。确认前仅可先补否定guard，不把A当已确认。

最低追加验证：两开关四组合下无身份/普通/有效管理员/已删除或冻结ROLE_ADMIN；standalone空roleMap需真实窄查，失败不放行；platform tenant0走core且正tenant同名角色拒；TENANT_INSPECT和ROLE_API_USER不等审计权；resource开启仍可被现资源规则拒、关闭也不能普通用户直读；不造ALL、不改全局role加载，get/list/count同资格，save与匿名失败审计保持。以上未实施、未测试，也未查DB或改授权。

## 8. 2026-10-24 批准记录

据总控转达的用户结构化明确批准，OpenAPI 采用集中审计 A，批准包含第 7 节完整政策；本记录替代前文“A/B 未选择、确认前仅否定 guard”的历史决策状态，无需重复提出同一批准问题。实现仍须满足既定资格、路由、有效性和验证要求，不能以本记录代替实现或测试证据。

最终读取资格仅为 standalone 的有效 ROLE_ADMIN，或 platform 模式可信 tenantId=0 且在 core 核实有效 ROLE_ADMIN 的主体。管理员门禁常驻，不因 data/resource 开关关闭而省略；resource 开启仍执行原日志资源授权，关闭只跳过资源匹配。空 roleMap、ALL、TENANT_ADMIN、TENANT_INSPECT、ROLE_API_USER 及普通正 tenant 的同名管理员均不替代集中审计资格；缺可信身份或资格查询失败时拒绝。

集中读取的 detail/page/count 须同资格、同受信 standalone/core 存储来源；普通 tenant 不提供本应用读取视图，旧归属不明记录不回填，不因本次批准批量迁移旧日志。审计 save 仍保持系统内部入口与原失败隔离，不能套用人类读取门禁破坏匿名或鉴权失败审计。无需新建审计角色、授予 ALL、修改既有管理员授权或全局强制加载角色。

总控已另派 backend 实施本政策；本设计角色只追加批准记录，不修改实现、QA 卡或 vault，不创建数据、不执行运行测试。用户另批准测试包 S，但 S 不含包 T、新租户/schema、现管理员修改、物理删除及字典契约；两项批准不得互相扩大范围。
