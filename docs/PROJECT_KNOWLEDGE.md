# EVA-API 项目知识库

> 基于 GitNexus 索引（3851 个节点 / 8734 条关系 / 146 个功能簇 / 300 条执行流）整理。
> 数据陈旧后请运行 `node .gitnexus/run.cjs analyze` 重新索引。

---

## 1. 项目快照

| 项 | 值 |
|---|---|
| GroupId / Version | `org.pkaq` / `4.0.3.12` |
| JDK | **Java 25**（`languageVersion = 25`，启用 `-parameters`） |
| 构建 | Gradle 多模块 + `libs.versions.toml` 版本目录 |
| 核心框架 | Spring Boot **4.1.1** + Spring Security |
| ORM | MyBatis-Plus **3.5.17**（兼容 Spring Boot 4 starter） |
| 数据库 | MySQL 9.7 / PostgreSQL（默认）；Mongo（日志） |
| 缓存 | Caffeine（本地）/ Redis（分布式），按 Condition 切换 |
| 鉴权 | JWT（Nimbus JOSE 10.9.1）+ 双 Token + 设备 ID |
| 对象映射 | MapStruct **1.6.3**（`defaultComponentModel = spring`） |
| 文件存储 | MinIO 9.0.3 / Ali OSS / FastDFS / 本地目录（Condition 切换） |
| 接口文档 | SpringDoc **3.1.0** + Knife4j 4.5.0（`!prod` Profile） |
| License | TrueLicense 1.33（独立 server + 拦截器） |
| 私服 | `http://dev.jm-sk.com:12081/` |

---

## 2. 模块拓扑

```
EVA-API (root)
├── web-booter                          # 唯一启动入口（Spring Boot 主应用）
│   └── org.pkaq.WebBooter
│
├── eva-core/                           # 框架级能力（无业务）
│   ├── eva-core-common                 # MVC 基类、Properties、i18n、JWT 工具、ThreadUser
│   ├── eva-core-log                    # 业务日志 / 错误日志 + 多 Supporter
│   ├── eva-core-data-mybatis           # StdEntity/StdService/StdCtrl + 数据权限 + 多租户
│   ├── eva-core-data-mongo             # Mongo 日志存储
│   ├── eva-core-cache                  # Caffeine / Redis / 序列号生成
│   ├── eva-core-upload                 # 多 Provider 文件上传 + 临时文件清理
│   ├── eva-core-websocket              # WebSocket + 心跳调度
│   └── eva-core-license                # License 校验拦截器（客户端）
│
├── eva-server/
│   └── eva-license-server              # 独立启动：证书生成 / 校验服务
│
└── eva-web/                            # Web 整合层
    ├── eva-web-core                    # 通用 Advice / Filter / Web Utils（CachedBody、Cookie、IP…）
    ├── eva-web-auth                    # 鉴权域：Security 配置、JWT 过滤器、OpenAPI AppKey、RBAC 缓存
    └── eva-web-sys/
        ├── eva-web-sys-domain          # BO / VO / Service 接口（仅契约）
        └── eva-web-sys-service         # 实体 / Mapper / Service 实现 / Ctrl
```

构建配置要点（build.gradle）：

- `subprojects` 统一 `java-library` + `maven-publish` + `signing`
- 全项目编译开启 `-Amapstruct.defaultComponentModel=spring` —— 所有 MapStruct 转换器即 Spring Bean
- 私服凭证已硬编码在 root `build.gradle`，迁移环境时需调整

---

## 3. 分层与命名约定（与 `.claude/CLAUDE.md` 一致）

| 层 | 后缀 | 父类 / 接口 | 备注 |
|---|---|---|---|
| 控制器 | `Ctrl` | `Ctrl` / `StdCtrl` / `StdTreeCtrl` / `StdMultiCtrl` / `StdActiveCtrl` | 统一返回 `Response` |
| 服务 | `Service` | `Service` / `StdService` / `StdTreeService` / `StdMultiService` / `StdActiveService` | 接口以 `I` 开头 |
| 实体 | `Entity` | `StdEntity` / `StdTreeEntity` / `StdMultiEntity` / `StdLineEntity` | 必须 `@Alias`，禁止 Swagger 注解 |
| 输入对象 | `Bo` | `Bo` / `StdBo` / `StdTreeBo` / `PageBo` / `DateRangeBo` / `IdCodeBo` | 可加 Swagger 注解 |
| 输出对象 | `Vo` | `Vo` / `StdVo` / `StdTreeVo` / `PageVo` / `TreeSelectVo` | 可加 Swagger 注解 |
| 转换 | `Convert` | `Convert<E,B,V>` | MapStruct，无须手写实例化 |
| Mapper | `Mapper` | `BaseMapper<T>` / `StdTreeMapper<T>` | XML 在 `resources/mapper` |
| 错误码 | `Codes` 枚举 | `BizAssert` / `BizCode` | code 即 i18n key |

> 编写或修改任何业务类前请优先复用上述基类，不要重复实现分页、树形、批量保存等通用能力。

---

## 4. eva-core 子模块详情

### 4.1 `eva-core-common` —— 框架基石
- **MVC 基类**：`Ctrl`、`Service`、`Entity`、`Convert`、`Response`、`PageVo`
- **配置体系（`org.pkaq.core.properties`）**：所有配置以 `eva.*` 为前缀，根类是 [`EvaConfig`](../eva-core/eva-core-common/src/main/java/org/pkaq/core/properties/EvaConfig.java)，组合 `Auth`/`Jwt`/`Cookie`/`Cache`/`BizLog`/`ErrorLog`/`File`/`Page`/`Tenant`/`License`/`DataPermission`/`ResourcePermission`
- **i18n**：`I18NConfig` + `I18NHelper` + `I18NInit`（`spring.messages.basename` 多文件聚合）
- **异常**：`BizException` + `BizAssert.newException(...)` + `Exceptions`
- **JWT**：`JwtUtil`（签发、解析、Claims、密钥）
- **线程上下文**：`ThreadUser` / `ThreadUserHelper`（请求维度的用户上下文，配合 TransmittableThreadLocal）
- **响应增强**：`CommonResponseAdvice`、`ExceptionAdvice`、`JsonFilterAdvice` + `@JsonFilter`/`@Ignore`/`@NoRepeatSubmit`
- **工具类**：`SecureUtils`、`Snowflake`、`SpringUtils`、`DateUtils`、`Json/JacksonObjectMapper`、`Reflect*`、`FileUtils`、`Bean/Coll/Str/Array/Object/IO/Image/Net Utils`
- **依赖来源**：`agent/transmittable-thread-local-2.12.6.jar`（JVM Agent，在启动脚本中通过 `-javaagent` 注入）

### 4.2 `eva-core-data-mybatis` —— MyBatis-Plus 增强
- **标准基类**：`StdEntity`、`StdTreeEntity`、`StdMultiEntity`、`StdLineEntity`、`TreeEntity`
- **标准 CRUD**：`StdService` / `StdTreeService` / `StdMultiService` / `StdActiveService` + 对应 `Std*Ctrl`
- **配置**：`MybatisPlusConfig`（拦截器装配）、`MybatisMetaObjectHandler`（自动填充审计字段）
- **数据权限**：`MybatisPlusPermissionConfig` + `MybatisPlusDataPermissionHandler` + `DataPermissionEnumm`
- **多租户**：`CustomTenantLineHandler`
- **枚举映射**：`UniversalEnumTypeHandler`（实现 `BaseEnum`）
- **错误日志**：`MybatisErrorLogSupporter` + `ErrorlogEntity` + `ErrorlogMapper`
- **工具**：`PageResult`、`TreeHelper`

### 4.3 `eva-core-cache` —— 缓存与序列号
- `CaffeineConfiguration` / `RedisConfiguration` 通过 `CaffeineCacheCondition` / `RedisCacheCondition` 二选一
- `SequenceGenerator` + `SequenceRepository` + `RuleFunction` —— 规则化序列号
- `RedisUtil` —— 业务侧统一 Redis 操作入口

### 4.4 `eva-core-log` —— 日志域
- **入口**：`@BizLog` 注解 + `BizLogAdvice` AOP 切面
- **事件机制**：`LogEvent` / `BizLogEvent` + `LogAsyncConfig`（异步落地）
- **Supporter（按 Condition 切换写入目标）**：Console / Jdbc / Mongo / Redis / Kafka / File / MyBatis
- **错误日志**：`ErrorLogSupporter` + `ErrorLogEntity`
- **配置入口**：`eva.bizlog`、`eva.error-log`

### 4.5 `eva-core-data-mongo` —— Mongo 日志载体
- `MongoLogSupporter` + `MongoBizLogEntity` + `MongoBizLogRepository`
- 默认承接日志归档与查询（与 log 模块协作）

### 4.6 `eva-core-upload` —— 文件上传
- 控制器：`FileUploadCtrl`
- **Provider（按 Condition 自动选择）**：MinIO / AliOSS / FastDFS / Nginx 目录 / 本地目录
- MinIO 实现：`MinioConfig` / `MinioInit` / `MinIOFileUtil`
- 调度：`DirTempFileCleanTask` —— 本地临时文件清理

### 4.7 `eva-core-websocket`
- `WebSocketConfig` + `WebSocketHandler` + `WebSocketSessionManager`
- `HeartbeatTask` + `ScheduledConfig` —— 心跳与定时

### 4.8 `eva-core-license`（客户端校验）
- `LicenseCheckInterceptor` 在 `WebConfigurer` 中注册
- `LicenseVerify` + `LicenseManagerHolder`
- 服务端 → 见 `eva-server/eva-license-server`

---

## 5. eva-web 子模块详情

### 5.1 `eva-web-core` —— 通用 Web 能力
- `WebLogAdvice`、`NoRepeatSubmitAdvice`、`HttpExceptionAdvice`、`ErrorLogEnricher`
- `CachedBodyFilter` + `CachedBodyHttpServletRequest`（多次读取 Body）
- `RequestFilter`
- Web 工具：`WebUtil`、`RequestUtil`、`ResponseUtil`、`HeaderUtil`、`CookieUtils`、`IpUtils`、`TokenUtils`、`CachedRequestUtil`

### 5.2 `eva-web-auth` —— 鉴权与权限
- **Spring Security 装配**：`WebSecurityConfig` / `AuthenticationManagerConfig` / `PasswordEncoderBean`
- **认证主流程**：
  - `JwtUsernamePasswordAuthenticationFilter`（接收登录请求）
  - `LoginAuthenticationProvider`（密码校验，使用 `JwtUserDetailsService`）
  - `UrlAuthenticationSuccessHandler` / `UrlAuthenticationFailureHandler`
  - `UrlLogoutSuccessHandler` / `UrlAccessDeniedHandler` / `UnauthorizedHandler`
- **JWT 透传**：`JwtAuthFilter` + `CacheTokenUtil` + `JwtUserDetail` + `JwtUserFactory` + `JwtGrantedAuthority`
- **动态权限**：`DynamicSecurityMetadataSource` + `DynamiclAccessDecisionManager`
- **领域内 RBAC**：`AuthUserService`、`AuthRolePermissionService`、`RoleResourceCacheService`、`SysRoleResource` / `AuthRoleEntity` / `AuthUserEntity`
- **OpenAPI AppKey 渠道**：`AppKeyAuthenticationFilter` + `SignatureValidator` + `AppCredentialEntity` + `AppKeyService`
- **在线用户**：`OnlineUserCtrl`、`TokenCtrl`
- **错误码**：`AuthCodes`

### 5.3 `eva-web-sys-domain` —— 系统域契约
- 仅包含 BO/VO + Service 接口：`IDictService` / `IModuleService` / `IRoleService` / `ITenantService` / `IUserService` / `IUserRoleRefSerivce`
- 业务域：dict / module / organization / post / role / tenant / user
- **后续若拆分微服务，此模块即为对外契约**

### 5.4 `eva-web-sys-service` —— 系统域实现
- **业务子域**（每个均含 ctrl/service/entity/mapper/convert，user 域还含 `AuthCtrl`）：
  - `dict` —— 数据字典（含 `@Dict` 注解 + `JacksonCodeSerializer` 自动翻译 + `DictCacheHelper` + `DictInit`）
  - `module` —— 菜单 / 资源（`ModuleEntity` + `ModuleResources`，含资源权限映射）
  - `organization` —— 组织机构（树形）
  - `post` —— 岗位（`PostUserEntity` 关联用户）
  - `role` —— 角色（`RoleResourceEntity` 关联资源、`RoleUserEntity` 关联用户）
  - `tenant` —— 租户（多租户模式开关下生效）
  - `user` —— 用户 + 登录入口 `AuthCtrl`
  - `notice` —— 通知
  - `log` —— 业务日志 / 错误日志查询 `BizLogCtrl` / `ErrorCtrl`
- **错误码**：`SysCodes`
- **Web 配置**：`SysApiConfiguration`

---

## 6. web-booter & eva-server

### 6.1 `web-booter`
- **入口**：[`org.pkaq.WebBooter`](../web-booter/src/main/java/org/pkaq/WebBooter.java) —— `@SpringBootApplication` + `@EnableCaching` + `@ComponentScan("org.pkaq.*")`
- **Swagger 分组**：`APIConfiguration`（访问中心 / Actuator / 全部接口）
- **测试套件**：`BaseTest` + `TestCASE` + dict/module/org/role/user/auth `CtrlTest`

### 6.2 `eva-server/eva-license-server`（独立部署）
- 入口：`LicenseBooter`
- 用途：证书生成（`LicenseGenerator`）与验签（`LicenseVerify`），供客户端 `eva-core-license` 调用

---

## 7. 配置约定（`eva.*`）

读取根类：`EvaConfig`（`@ConfigurationProperties(prefix = "eva")`）。常用项：

| 配置 | 类 | 说明 |
|---|---|---|
| `eva.mode` | String | 单例 / 多租户模式开关 |
| `eva.i18n` | boolean | 是否启用国际化 |
| `eva.norepeat-check` | boolean | 重复提交校验 |
| `eva.jwt.*` | `Jwt` | 签名密钥、双 token、过期时间 |
| `eva.cookie.*` | `Cookie` | Cookie 维度配置 |
| `eva.auth.*` | `Auth` | 白名单 / 登录路径等 |
| `eva.tenant.*` | `Tenant` | 多租户隔离字段 |
| `eva.cache.*` | `Cache` | 缓存类型选择 |
| `eva.bizlog.*` / `eva.error-log.*` | `BizLog`/`ErrorLog` | 日志开关、包过滤、Supporter |
| `eva.upload.*` | `File` | 文件存储选型 |
| `eva.page.*` | `Page` | 分页默认值 |
| `eva.data-permission.*` | `DataPermission` | 数据权限规则 |
| `eva.resource-permission.*` | `ResourcePermission` | 资源权限 |
| `eva.license.*` | `License` | 是否启用授权校验 |

> 新增配置：**新增 Properties 类 → 在 `EvaConfig` 组合 → 写 `getXxx()` 提供空对象兜底**（与现有约定一致）。

---

## 8. 核心执行流（来自 GitNexus 索引）

### 8.1 登录成功 → JWT 签发（`OnAuthenticationSuccess → Jwt`，7 步，intra_community）
```
UrlAuthenticationSuccessHandler.onAuthenticationSuccess
  → CacheTokenUtil.buildCacheValue
  → JwtUtil.getIssuedAt / getClaimsFromToken / generalKey
  → EvaConfig.getJwt → Jwt(properties)
```

### 8.2 拉取菜单（`AuthCtrl.fetchMenus → BizCode.getMsg`，8 步，cross_community）
```
AuthCtrl.fetchMenus
  → ModuleService.fetchUserModules
  → ModuleService.handleFetchResource
  → StdService.get
  → Convert.toVo → convert
  → BizException
  → BizCode.getMsg（i18n）
```

### 8.3 其他高频流
- `Edit → GetMsg` / `Edit → Encipher` / `Edit → StreamToWord` —— 通用编辑型 CRUD
- `Repwd → GetMsg` —— 重置密码
- `CreateTenantAdmin → GetMsg` —— 创建租户管理员
- `SaveUser → GetMsg` —— 用户保存
- `List → GetMsg` —— 列表型查询统一收口到 `BizCode.getMsg`

> 完整 300 条执行流可通过 `gitnexus://repo/EVA-API/processes` 浏览；针对单条用 `gitnexus://repo/EVA-API/process/{name}` 取 step trace。

---

## 9. 修改代码前的工作流（强制约束）

由 [`CLAUDE.md`](../CLAUDE.md) 定义：

1. **修改任何 function/class/method 前**：调用 `impact({target, direction: "upstream"})`，向我汇报 blast radius
2. **遇到 HIGH/CRITICAL 风险**：必须先告知，待确认后再动手
3. **重命名**：使用 `rename`（基于调用图），禁止全局替换
4. **提交前**：调用 `detect_changes()`；如需对比主干，`detect_changes({scope:"compare", base_ref:"cornerstone"})`
5. **探索时**：优先用 `query({query:"concept"})` 找执行流，再用 `context({name})` 看 360 度视图

> 索引过期：在项目根运行 `node .gitnexus/run.cjs analyze`；若无该脚本，运行 `npx gitnexus analyze`。

---

## 10. 快速导航索引

| 场景 | 文件 |
|---|---|
| 启动应用 | [WebBooter.java](../web-booter/src/main/java/org/pkaq/WebBooter.java) |
| 添加配置项 | [EvaConfig.java](../eva-core/eva-core-common/src/main/java/org/pkaq/core/properties/EvaConfig.java) |
| Web Security 装配 | [WebSecurityConfig.java](../eva-web/eva-web-auth/src/main/java/org/pkaq/core/auth/config/WebSecurityConfig.java) |
| JWT 工具 | [JwtUtil.java](../eva-core/eva-core-common/src/main/java/org/pkaq/core/jwt/JwtUtil.java) |
| 登录入口 | [AuthCtrl.java](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/user/ctrl/AuthCtrl.java) |
| 用户菜单 | [ModuleService.java](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/module/service/ModuleService.java) |
| 通用 CRUD 基类 | [StdService.java](../eva-core/eva-core-data-mybatis/src/main/java/org/pkaq/core/mybatis/mvc/service/StdService.java) / [StdCtrl.java](../eva-core/eva-core-data-mybatis/src/main/java/org/pkaq/core/mybatis/mvc/ctrl/StdCtrl.java) |
| 树形 CRUD 基类 | [StdTreeService.java](../eva-core/eva-core-data-mybatis/src/main/java/org/pkaq/core/mybatis/mvc/service/StdTreeService.java) / [StdTreeCtrl.java](../eva-core/eva-core-data-mybatis/src/main/java/org/pkaq/core/mybatis/mvc/ctrl/StdTreeCtrl.java) |
| 数据字典使用 | [`@Dict`](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/dict/annotation/Dict.java) + [DictCacheHelper.java](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/dict/cache/DictCacheHelper.java) + [JacksonCodeSerializer.java](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/config/JacksonCodeSerializer.java) |
| 业务日志注解 | [`@BizLog`](../eva-core/eva-core-log/src/main/java/org/pkaq/core/log/annotation/BizLog.java) + [BizLogAdvice.java](../eva-core/eva-core-log/src/main/java/org/pkaq/core/log/pointcut/BizLogAdvice.java) |
| 异常 / 错误码 | [BizException.java](../eva-core/eva-core-common/src/main/java/org/pkaq/core/exception/BizException.java) + [BizAssert.java](../eva-core/eva-core-common/src/main/java/org/pkaq/core/exception/BizAssert.java) |
| 文件上传 | [FileUploadCtrl.java](../eva-core/eva-core-upload/src/main/java/org/pkaq/core/upload/ctrl/FileUploadCtrl.java) + [FileProvider.java](../eva-core/eva-core-upload/src/main/java/org/pkaq/core/upload/provider/FileProvider.java) |
| WebSocket | [WebSocketHandler.java](../eva-core/eva-core-websocket/src/main/java/org/pkaq/websocket/handler/WebSocketHandler.java) |
| License 客户端 | [LicenseCheckInterceptor.java](../eva-core/eva-core-license/src/main/java/org/pkaq/core/license/LicenseCheckInterceptor.java) |
| License 服务端 | [LicenseBooter.java](../eva-server/eva-license-server/src/main/java/org/pkaq/core/LicenseBooter.java) |
| 版本目录 | [libs.versions.toml](../gradle/libs.versions.toml) |
| 多模块装配 | [settings.gradle](../settings.gradle) |

---

## 11. 已知待办（来自 README / TODO）

- 日志异步记录（部分已落地，见 `LogAsyncConfig`）
- form 请求登录参数处理
- 岗位与部门关系表，单用户可在多部门有不同岗位
- 角色 / 岗位权重值（登录返回时显示最大权重）
- 逻辑删除：`DELETED` 索引前置；超 6 个月归档（mongo / 不存）；原表物理删除 —— 策略模式 + SPI
