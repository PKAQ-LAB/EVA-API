# 安全能力组合

本项目保留现有 Gradle 模块结构。认证、接口资源权限和数据权限分别控制，注册暂缓，不提供没有实现的注册开关。

## 配置位置

公共配置位于 `web-booter/src/main/resources/config/eva/eva.yaml`，环境覆盖位于同目录的 `eva-dev.yaml`、`eva-prod.yaml`。由 `application.yaml` 的 `spring.config.import` 加载，仍通过 `spring.profiles.active` 切换环境。

| 场景 | `eva.auth.authentication.enabled` | `eva.resource-permission.enable` | `eva.data-permission.enable` |
| --- | --- | --- | --- |
| 普通应用，仅登录认证 | true | false | false |
| 登录与接口资源权限 | true | true | false |
| 登录与数据权限 | true | false | true |
| 完整管理系统 | true | true | true |
| 显式公开路径应用 | false | false | false |

以上开关在启动时读取，需要重启生效；不是在线热切换开关。

## 认证与权限边界

- `eva.auth.authentication.enabled` 默认 true，既有应用不设置该项时保持启用。
- `eva.auth.jwt.enabled`、`eva.auth.open-api.enabled` 继续选择认证机制；认证总开关关闭时两项机制均不执行。
- 开启认证却关闭全部现有认证机制时，启动失败。
- 关闭认证却开启资源权限、数据权限或租户时，启动失败。
- 关闭认证时仅 `eva.auth.anonymous` 显式声明的公开路径可访问，其他路径拒绝；不能把关闭认证理解为开放整个管理后台。登录、刷新和退出入口不作为公开业务接口继续工作。
- `eva.auth.permit` 只跳过接口资源判定，不跳过身份认证；它与 `anonymous` 的用途不同。
- 关闭接口资源权限不影响 JWT 签名、过期、服务端会话、账号状态、租户状态、schema 世代和权限版本校验。
- standalone 普通应用关闭资源权限和数据权限后，登录、请求认证与刷新不再加载角色关联；平台模式仍需要角色来判断平台管理员跨租户查看能力。
- 数据权限可独立于接口资源权限开启；当前数据范围仍来源于角色上的 `data_scope/data_org_ids`。没有范围的已认证用户拒绝查询，不能因为没有角色而默认取得全部数据或本人数据。
- 数据权限 SQL 仍在 `eva-core-data-mybatis` 执行，并先于分页插件处理，使分页数据和总数采用相同范围。

## 使用示例

纯认证应用的环境覆盖：

```yaml
eva:
  auth:
    authentication:
      enabled: true
    jwt:
      enabled: true
    open-api:
      enabled: false
  resource-permission:
    enable: false
  data-permission:
    enable: false
  tenant:
    enable: false
```

仅开放显式公开路径：

```yaml
eva:
  auth:
    authentication:
      enabled: false
    anonymous:
      - /public/ping
  resource-permission:
    enable: false
  data-permission:
    enable: false
  tenant:
    enable: false
```

这仍是现有完整脚手架的配置组合，不代表启动应用已经移除 Redis、数据库、系统管理 Bean 或数据库表。构建依赖与业务模块裁剪需要按具体应用另行处理。

## 模型与后续事项

### 认证模块内部依赖边界

认证模块保留现有 Gradle 模块，不新增仅有少量类的物理子模块。内部通过以下职责边界隔离数据库与会话存储实现：

| 包 | 职责 |
| --- | --- |
| `authentication` | 登录、JWT 请求认证、刷新、响应处理与身份模型 |
| `authorization` | 接口资源判定、角色与数据范围上下文 |
| `spi` | 账号、权限、会话、租户路由、应用凭据与审计的接口及普通快照模型 |
| `adapter.mybatis` | 数据库投影、Mapper、查询与持久化实现 |
| `adapter.redis` | Redis 会话存储实现 |
| `adapter.tenant` | schema 路由及租户解析实现 |
| `config` | Spring Security 装配 |

核心认证与授权只使用 SPI，不导入 Mapper、数据库 Entity 或具体存储适配器；SPI 快照不继承 `StdEntity`，也不包含 MyBatis 分页或 SQL 映射类型。系统管理用户写模型与认证只读投影继续分别承担 CRUD 和凭证校验，避免认证反向依赖整个系统管理模块。

依赖方向是“核心逻辑 → SPI ← 存储适配器”，而不是“核心逻辑 → 具体持久化服务”。这是源代码边界，不是 Gradle 依赖裁剪：当前 `eva-web-auth` 同时包含 MyBatis 适配器，因此仍保留 `eva-core-data-mybatis` 构建依赖。若应用需要完全移除数据库依赖，后续必须将适配器物理拆出；仅关闭权限或调整包名不能达到此目的。

审计后台的列表查询、管理接口可以面向存储适配器；认证过程中的审计写入使用 SPI。数据权限 SQL 拦截仍属于 `eva-core-data-mybatis`，不移入认证核心。

包迁移改变 Java 全限定类名：旧 `security` / `domain` 对应 `authentication`，旧用户、角色及 RBAC 持久化包对应 `adapter.mybatis`，具体租户与 Redis 会话实现对应 `adapter.tenant` / `adapter.redis`。仓库内导入及 Mapper XML 随迁移更新；仓库外直接引用旧类名的应用需要同步导入并重新编译，不能视为二进制兼容发布。HTTP 路径、响应、数据库结构和 Redis key 不因分包而变化。

本次保留现有 API、响应及数据库结构，不机械合并认证只读投影与用户管理写模型。认证模型承担凭证与账号状态查询，用户管理模型承担 CRUD；后续统一账号持久化需要单独评估管理模块依赖，避免让纯认证反向依赖全部系统管理。

用户自助注册尚未实现。本次不新增注册端点、默认角色、默认组织或租户加入规则。

现有“本人”数据范围包含创建者与修改者，后台任务无用户上下文时不注入用户数据范围。这些既有业务语义保持不变；需要收紧时须单独确认并补业务回归。
