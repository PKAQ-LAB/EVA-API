# 认证响应资源权限能力后端源码交接

2026-10-28 backend_developer。按总控批准的resource-permission-capability-response契约实施，仅必要后端源码与focused tests；没有Gradle/Java编译、服务重启、DB/Redis/HTTP/UI、前端改动或提交推送。菜单业务查询、PG测试/证据helper及全部验证工具/配置均冻结，本次未改。

## 当前配置与兼容位置

完整Spring键为eva.resource-permission.enable，由EvaPropertiesConfiguration.evaConfig上的@ConfigurationProperties(prefix="eva")绑定至EvaConfig.resourcePermission.enable。唯一响应来源是该当前bean的getResourcePermission().isEnable()，不是strict、文件文本、账号角色、admin或cookie。本轮没有新增开关、enabled别名或配置文件修改。

application.yaml使用spring.config.import=classpath:config/eva/eva.yaml，默认profile dev；同目录eva-dev.yaml和eva-prod.yaml声明对应profile激活，已有ConfigData集成测试覆盖dev/prod装载。项目README说明命令行/系统环境属性可覆盖文件。当前源eva.yaml的enable=true、strict=false只是文件默认事实，不代表运行实例实际值；本轮没有读取进程环境或运行HTTP。已检common/auth/booter源码未见该bean的RefreshScope/Rebinder/EnvironmentChangeEvent机制，不承诺仅编辑文件就热更新；响应在每次成功请求中读取当前已绑定bean，页面刷新走既有fetchMenus重新取快照。

现Response没有meta/ext。新增core-contract的AuthBootstrapResponse<T>继承Response<T>，仅多一个resourcePermissionEnabled primitive boolean，@JsonInclude(ALWAYS)确保false不被默认值省略。显式fromSuccess工厂返回子类，逐项保留success/code/message/mtype/args/data原值，拒null及失败响应，不调用子类继承的基类success静态方法。原Response类与CommonResponseAdvice未改；Advice现有instanceof Response分支保留实际子类对象，原i18n/code处理继续。

登录成功handler包裹原Response.success(LoginSuccessVo,欢迎业务码,姓名参数)，token/user_info与cookie/audit流程原样。AuthCtrl仅增EvaConfig注入并包裹原fetchCurrentMenus成功Response；菜单data数组/元素/resources不变。失败继续原base Response无能力字段。getAlpha/token刷新、LoginUserInfoVo.capabilities、JWT及其他接口不扩展。sys没有反向依赖auth，未改任何build依赖。

统一字段已同步designer和frontend：两现有成功响应外层resourcePermissionEnabled:boolean，与data同级。前端login暂存之后须用现有fetchMenus初始化/硬刷新成功外层值更新；缺失、类型错或失败不得从旧false回填。后端源码尚未编译/服务上线，不能据此声明运行接口已有字段。

## 精确源码与SHA

| 路径 | SHA256 |
| --- | --- |
| eva-core/eva-core-contract/src/main/java/org/pkaq/core/mvc/vo/AuthBootstrapResponse.java（新增） | C436DF0E4CDE1234BC22B6DC4DFA286C99DD74F1ABB17C4C5D8341FE928F5A31 |
| eva-web/eva-web-auth/src/main/java/org/pkaq/core/auth/authentication/entrypoint/UrlAuthenticationSuccessHandler.java | E9FB95EA1EE37A4B9975FBAB292E4E83005ABE7A0137A2162BB5B558A5AAD5C9 |
| eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/user/ctrl/AuthCtrl.java | 2E04277F9F27F5FCDD64B0202BD63460C886B357020E13118515DBD9453C15C7 |
| eva-core/eva-core-common/src/test/java/org/pkaq/core/properties/ResourcePermissionBindingTest.java（新增） | CE4192E4D3E2EB59A88C756CB92EB7ABDF2B2F81B5302AF66436D7CFD0EE3555 |
| eva-web/eva-web-auth/src/test/java/org/pkaq/core/auth/authentication/entrypoint/AuthLoginCapabilityTest.java（新增） | B8E1E84D6D798B5E384395D86115C20F87C1ED8CDFF20BA437189A4446478961 |
| eva-web/eva-web-sys/eva-web-sys-service/src/test/java/org/pkaq/sys/user/ctrl/AuthMenuCapabilityTest.java（新增） | BCBF3315664A41B083C59B5565549D3C363AEA0A8FA74C1B90DBF84F7F217988 |

相对上轮JDBCv1的686输入，本次新增DTO及三测试四Java文件，并改变两个生产class对应源码。当前690输入closure SHA256=46D24A2EF9ADE779CDB519C7D49980522168462C5209240F13AE6826866E99A4。相对9e旧685，新增还包括上轮PG证据helper、四PG测试及7392观察脚本属于既有批准差异；没有missing。

## 测试源码与轻量核对

AuthLoginCapabilityTest三@Test：真实onAuthenticationSuccess→ResponseUtil/JsonUtil序列化，同一handler配置先true后false且strict相反，核布尔与原data.token/user_info结构；真实登录失败handler不带字段，工厂拒失败/null；复制原code/message/i18n参数/data引用，额外以NON_DEFAULT mapper验证false仍真实序列化，忽略字段不泄漏。JWT、身份解析、审计和token缓存均mock；不签真实token或连接存储。

AuthMenuCapabilityTest两@Test：实际AuthCtrl→CommonResponseAdvice→ResponseUtil/JsonUtil链，核Advice保同一子类，true/false仍外层、菜单数组及resource method/path不变；空数组+false可表达，初始化异常失败无字段。菜单服务/字典/用户服务均mock，无SpringBoot全应用/开发库。使用本地mock servlet请求响应，不发HTTP。

ResourcePermissionBindingTest一@Test：ApplicationContextRunner只装载EvaPropertiesConfiguration，精确键的高优先环境property覆盖相反低值，true/false均从实际绑定getter读取，并确认strict独立；关闭JWT、开启OpenAPI机制使配置校验有效，不需密钥或外部资源。它测试Spring环境覆盖绑定，不声称验证实际操作系统环境变量或运行实例配置。

本轮仅完成源码静态核对：三个生产落点、六@Test声明、调用/注入边界、所有新行<=120、无候选unused import、git diff --check无内容错误（仅LF/CRLF提示）。未运行六项测试、编译、lint或真实序列化，以上为测试覆盖设计而非通过结果。PG证据helper仍B449DF73CEE7CDC118CE0ACF9BDF70A79A4F43DA49A1DF51A445606F1F3BF3D4。

## 后续隔离验证PLAN（当前不执行、不改旧工具）

当前新增生产DTO并修改AuthCtrl/登录handler，原529 main/686源锁已不足以证明本功能；旧已审JDBCv1工具应按原锁拒绝，不能改白名单暗放行或直接复用旧main当新实现。新验证工具需另授并独审。

建议新fresh RUN按原19Project全隔离/固定11main闭包离线编译及资源处理，锁当前690完整source/build/config/test/presence与独立新工具。原main字节只作为精确差异基线：预期新增core-contract/AuthBootstrapResponse.class，允许改变AUTH/UrlAuthenticationSuccessHandler.class和SYS/AuthCtrl.class；其他原main、opaque资源、generated/header/外部依赖均逐项复核，不预先宣称实际530通过。

测试先只编译COMMON/AUTH/SYS本次focused闭包并新封prepared，实际图不含业务Boot/开发库/PG；唯一选择三新测试类共六@Test，序列化使用实际方法链并mock一切存储/认证副作用，不能跑web-booter BaseTest集成类。之后可按影响最小补既有EvaPropertiesConfigurationTest、LoginUserInfoVoCapabilitiesTest、ResourceAuthorizationServiceTest、WebSecurityCapabilitiesTest、ModuleServiceTest及菜单映射测试，具体集合和实际cases需独审新工具后另授，不能把源码声明数当新绿。

若后续仍需四PG/JDBCv1联合验证，应另核新main和测试输入/产物，再按已批准八证据/终态门禁执行；本功能不修改PG fixture或S真实授权，也不将旧219/4b11与新测试拼数。当前仅交designer/QA审源码与本方案，运行、服务更新及现场验收仍由总控另派。
