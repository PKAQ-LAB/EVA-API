# schema 平台 tenant0 菜单入口边界设计

2026-10-30 technical_designer。仅只读源码与设计分析，无 DB/HTTP/测试/生产修改。本项独立于 dev standalone 登录日志菜单两项元数据提案，不阻已通过前后端局部成果。

## 结论

`AuthorizedMenuService` 的 schema `tenantId <= 0` 拒绝是明确的既有安全边界，而非本次登录日志新增的偶然错误。它与平台统管目标存在尚未实现的入口能力差距，但不能据目标倒推旧校验无效或直接删除。当前只能证明新的历史日志服务在获得可信平台0主体后支持已批准的集中读取，不能证明该主体已能通过现有认证与菜单全链。

10/28 `2026-10-28-authorized-menu-metadata-boundary.md` 第6节已明确：schema0认证契约未成立，默认拒绝，须先明确平台认证身份与core授权契约；`AuthorizedMenuPostgresTest` 的 schema 路由用例末尾显式断言0拒绝。该测试不能改成“任意0通过”。现standalone0成功和正租户私有授权交集不受本方案改变。

## 有界调用核查

`AuthCtrl.fetchMenus` 无客户端 uid，调用专用当前身份菜单服务，并保持 AuthBootstrapResponse 的 resourcePermissionEnabled 结构；异常转原失败响应。菜单 service 在身份及真实只读事务核验后，schema分支拒<=0，正租户经可信 resolver/router 读私有有效角色资源ID，再到core取平台授权交集。它不把0当standalone。

认证端还存在更早边界：schema用户名密码登录过滤器必须用 tenantCode 经 `TenantLoginResolver.resolveCode/resolveId`，resolveId拒<=0；`LoginAuthenticationProvider` 的租户分支使用正租户认证router。成功处理器签发JWT前再次resolveId；`JwtAuthFilter` 每次schema access验证也resolveId并校验schemaGeneration，再由租户router读取账号状态，AuthPermissionContextService 的角色/资料亦经该router。角色资源缓存缺失时也调用同router重载。故即便直接给菜单加0分支，登录、请求上下文和resource校验仍不能自动成立；不应通过伪造ThreadUser、缓存角色或测试夹具声称端到端支持。

新 `LoginLogReadAccess` 在真实platform/schema、可信tenant0下到core核有效账号/ROLE_ADMIN/membership，并保留实际GET资源校验；OpenAPI有类似独立准入。二者属于具体审计能力，不是平台认证入口，也没有授权任意core业务菜单。TENANT_INSPECT和rolesMap管理员快照均不替代有效core关系。

## 推荐最小规则：先限于已批登录审计入口

平台菜单须是显式的独立分支，成立条件同时满足：真实platform模式、schema模式、可信已认证tenant0及正uid、来路上下文不冲突、真实只读事务、配置core与实际解析的全部参与表来源一致，以及core有效tenant0账号—有效tenant0 ROLE_ADMIN—membership关系。失败或查询错误拒绝；saas0、负tenant、普通0、正租户同名管理员仍走原拒绝/租户分支。不得让通用正租户resolver把0解析成某租户或core别名。

资格后，从同一受信core按该主体的有效tenant0角色关系取得其实际获授resource ID；再仅取tenant0有效module/resource。平台控制面不借用任何正租户私有schema、不接受targetTenantId/schema参数、不读取其它账号的角色、不把SYS_TENANT_RESOURCE缺失解释为全租户许可。平台0没有已成立的租户套餐语义，是否无需SYS_TENANT_RESOURCE(0)必须作为本分支的显式政策，不以空结果或缺表自动兜底。

首版仅允许精确的 `/log/login` 登录日志叶与其实际既有API_LOG_LOGIN资源，以及本主体另有实际MENU_LOG授权时的 `/log`父元数据；同时核module code/route与资源 code/url/method身份，不能用log.*前缀或TENANT_ID=0就选出所有core菜单。没有父授权时按既有树规则让已授叶作根，不能补父授权；没有API授权不返回该叶，即使resource开关关闭也不凭空生成菜单。模块有效性及资源真实deleted字段沿现规则，不能造resource.frozen。

core仍可能存放租户业务模块定义，单靠其物理位置或tenant0字段无法判定为平台管理菜单。若用户要全套M1平台控制台，应另列确切平台模块与资源集合及其业务边界后确认，不能由本次审计页顺带开放sys/全部core/log子树。首版不默认纳入在线用户、业务/错误日志、租户私有业务或其它审计能力；既有OpenAPI读取批准不等于本轮授权其导航实施。

菜单返回沿现ModuleMenuVo/resources及bootstrap结构，不增加客户端admin标记或伪造capability。现前端显式route与资源谓词可消费它；后端日志资格依旧独立，菜单成功不提供跨租户业务查询许可。sys-service不得为复用auth具体服务而引入反向依赖；实现时可在现所属层按同一固定资格SQL语义执行，是否提取中立SPI需依真实依赖决定，不为此先造全局权限框架。

## 认证契约与批准边界

已批准内容覆盖历史登录日志的租户管理员本租户读取、有效平台管理员跨租户读取、core集中来源与独立审计事务，以及按真实授权菜单呈现该页面；无需重问这些政策。现dev standalone新增leaf/调整1407 MAIN_ID的待批SQL只改该环境元数据，不会打通schema平台认证，也不能复制到所有私有schema或增加grant。

仍需用户明确确认的是schema平台0独立认证与授权来源契约：由什么服务端可区分的入口认证core账号，如何签发及验证独立平台身份/代次、如何刷新/注销/校验permVer及重载core账号角色资源；本设计推荐独立平台认证路径，而不是空tenantCode、客户端tenantId=0或通用resolver对0放行。具体URL/请求字段或令牌声明若变，属于API/认证契约扩展，必须先定范围。现菜单API形状可保持，但将原schema0拒绝改为受限成功仍属行为契约变化，需要批准该core资格、精确菜单范围与平台不套租户套餐的规则。当前总控只授设计，不授这些生产改动。

若已有外部可信平台认证渠道，需先给出当前源码/契约证据验证它如何进入同一ThreadUser与session链；不能仅因测试能注入tenant0就认定成立。任何账号/角色/resource授予、迁移或部署重启都不由本方案自动授权。可以分期：先保留当前拒绝并交付standalone成果，再独立确认平台入口；不能把平台HTTP全链验收标为完成。

## 最小后续验证范围

批准后仅对新平台分支补正反例：有效core管理员且有指定resource成功；普通0、正tenant伪管理员、saas0、失效账号/角色/membership、错core/缺表/临时表回落、外来context、无真实事务、无资源或仅父授权、同模块未授兄弟资源均拒绝。用同ID租户私有干扰数据证明平台分支不访问私有schema；正常、core查询异常均恢复search_path及原非空TenantContext。保持standalone0与正租户A/B原交集测试，schema普通0拒绝断言仍有效。

若实施认证契约，需另外验证平台登录→JWT请求重建→bootstrap菜单→实际日志GET→refresh/logout/permVer失效，resource开启时真实core资源重载可达且资格不借缓存角色。当前局部30/30及应用compile仅覆盖各自冻结范围，不计作这些尚未实现的验证。本角色未执行任何候选测试。
