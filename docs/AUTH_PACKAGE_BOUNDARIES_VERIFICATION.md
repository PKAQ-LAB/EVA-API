# 认证模块分包与存储边界验收

日期：2026-10-08。仓库：EVA-API。分支：cornerstone。总控：orchestrator；实现：backend_developer_security、backend_adapter_packages；独立验收：quality_reviewer_security。

## 实现范围

保留现有 Gradle 模块，将认证流程放入 `authentication`，权限决策放入 `authorization`；账号、角色、资源、会话、租户路由、应用凭据和审计写入通过 `spi` 接口对接。MyBatis、Redis 与 schema 路由实现分别位于 `adapter.mybatis`、`adapter.redis`、`adapter.tenant`。审计管理页面对应的 BO、VO、Controller 及在线用户管理位于 `audit`；数据库审计查询仍由 MyBatis 适配器负责。

账号、角色和应用凭据快照不继承持久化实体；密码及应用密钥不参与 JSON 序列化或默认 `toString` 输出。登录审计端口使用普通身份字段，不反向引用认证实现类。登录、请求与刷新统一按资源权限、数据权限及平台模式判断是否需要读取可信角色。

迁移中的 Mapper XML namespace 与嵌套 select 全限定名已同步，实体别名保留。未修改 HTTP 路径、响应、数据库结构和 Redis 会话 key。Java 类全限定名发生变化，仓库外直接引用旧类的应用需要同步迁移导入并重新编译。

仍保留 `eva-core-data-mybatis` 构建依赖，因为适配器没有物理拆成独立模块。此轮是源代码依赖隔离，不是完全移除数据库、Redis 的纯认证依赖包。

## 审查发现与处理

- 新角色读取路径会嵌套进入租户路由，既有 `finally` 清空线程上下文会丢失外层租户。现改为保存并恢复调用前上下文；查询异常、路由异常及 search_path 恢复异常也执行线程上下文恢复。schema SQL 不变。
- 审计 SPI 初稿反向引用 `JwtUserDetail`，已改为普通身份字段；接口常量迁入 `LoginAuditConsts`。
- 迁移文件的通配符导入替换为具体类，本轮认证模块源码统一为 LF。

## 验证边界

### 最终命令与结果

先执行认证模块 `clean` 后构建，去除旧包残留字节码。四模块测试曾通过 `--rerun-tasks` 强制生成本轮报告；最后收口后的命令分开执行，防止全局 `-x test` 排除显式测试任务：

```powershell
gradle.bat :eva-web:eva-web-auth:test :eva-core:eva-core-common:test :eva-core:eva-core-cache:test :eva-core:eva-core-data-mybatis:test --no-daemon
gradle.bat build -x test --no-daemon
```

最终测试 `BUILD SUCCESSFUL`，耗时 1 分 21 秒，24 个任务，其中 4 个执行、20 个已是最新。随后构建 `BUILD SUCCESSFUL`，耗时 57 秒，40 个任务，其中 2 个执行、38 个已是最新。全项目构建包含启动包的编译与打包，不运行启动包测试。

29 份测试 XML 合计 89 个测试，失败 0、错误 0、跳过 0：认证 56、公共配置 9、缓存 2、数据权限 22。认证 XML 全部更新于 2026-10-08 11:01:51；其余模块本轮强制重跑报告分别为 10:53:10、10:53:11、10:53:24，最终增量命令未重复执行它们。`web-booter` 测试 XML 最新时间仍为上一轮 2026-10-07 20:43:04，不属于本轮验收。

架构及 XML 装配测试 3/3 通过，扫描 37 个核心 class；租户路由嵌套及三种异常恢复测试 4/4 通过。首轮新增路由异常测试因 Mockito `when` 重设存根时提前执行原 answer，改变了测试上下文而失败；改为 `doAnswer` 后，最终重跑通过，此次失败属于新测试夹具问题，不是构建环境问题或业务行为被放宽。

MockMvc 测试 2/2 通过：公开 `GET /public/ping` 返回 200/ok；匿名访问 `GET /sys/user/list` 与认证关闭时访问 `/auth/getAlpha` 返回项目既有 200/业务 `success=false`；有效身份替身通过真实 JWT 过滤链访问 `/sys/user/list` 返回 200/ok。拒绝响应的 HTTP 200 不能单独解释为授权通过。

### 证据限制

独立架构测试读取实际编译字节码引用，禁止认证、授权和 SPI 使用 ORM、Mapper 或具体适配器；另验证 SPI 不反向依赖认证与授权实现，快照不携带持久化实体依赖。使用实际 MyBatis XMLMapperBuilder 在内存中解析 XML，校验 namespace、别名及 statement 装配，不连接数据库。

请求证据来自真实 Spring Security 过滤链的 MockMvc 测试。账号、JWT 校验、会话及租户依赖使用替身，因此不能将其称为真实数据库、Redis 和签名登录的完整端到端联调。

本轮不执行 `web-booter:test` 或全量 `test`；不启动应用服务，不打开浏览器，不做开发数据库迁移或业务写入。会话测试使用模拟 Redis，租户路由测试使用替身事务及 schema 路由器。

Jev 为 disabled，未调用云端。未提交、推送或创建 PR；原有工作区改动及 `GO_REFACTOR_PLAN.md` 保留。
