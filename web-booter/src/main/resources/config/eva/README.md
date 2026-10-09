# EVA 配置说明

## 文件职责

- `eva.yaml`：所有环境共享的 EVA 业务配置和安全默认值。
- `eva-dev.yaml`：开发环境覆盖配置。
- `eva-prod.yaml`：生产环境覆盖配置。
- `application*.yaml`：仅维护 Spring、数据源、Redis、日志和服务端口等基础设施配置。

## 环境切换

运行时必须通过 `SPRING_PROFILES_ACTIVE` 切换环境；未指定时默认使用 `dev`。

```powershell
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:EVA_JWT_SECRET = '<至少32字节的生产密钥>'
java -jar web-booter.jar
```

禁止通过重新构建应用切换环境，禁止在仓库中保存生产密钥。

## 常用外部变量

| 环境变量 | 对应配置 | 说明 |
| --- | --- | --- |
| `EVA_JWT_SECRET` | `eva.jwt.secret` | 生产环境必填，至少 32 字节 |
| `EVA_MODE` | `eva.mode` | standalone、platform 或 saas |
| `EVA_TENANT_ENABLE` | `eva.tenant.enable` | 是否开启 schema 租户模式 |
| `EVA_TENANT_MODE` | `eva.tenant.mode` | standalone 或 schema |
| `EVA_AUTH_REGISTRATION_ENABLED` | `eva.auth.registration.enabled` | 默认 false；仅 standalone、非租户且认证和 JWT 有效时开放自助注册 |
| `EVA_MINIO_URL` | `eva.upload.min-io.url` | MinIO 服务地址 |
| `EVA_MINIO_ACCESS_KEY` | `eva.upload.min-io.access` | MinIO 访问标识 |
| `EVA_MINIO_SECRET_KEY` | `eva.upload.min-io.secret` | MinIO 访问密钥 |
| `EVA_UPLOAD_TEMP_PATH` | `eva.upload.temp-path` | 本地上传临时目录 |
| `EVA_UPLOAD_STORAGE_PATH` | `eva.upload.storage-path` | 本地上传存储目录 |
| `EVA_LICENSE_KEYSTORE_PASSWORD` | `eva.license.keystore-pwd` | License 密钥库密码 |

命令行参数和操作系统环境变量可以覆盖文件配置；生产部署必须由部署系统注入敏感值。

## 租户 schema 命名

租户主键 ID 继续用于关系表、JWT 和权限关联；schema 改为配置前缀加不可变 code，默认如 `tenant_acme`。code 只允许小写字母开头、后接小写字母、数字或下划线，最多 6 字符；不会自动转小写。创建后不可修改，删除租户后也不可复用 code，避免接入遗留 schema 数据。

schema 名仅在服务端创建时生成，后续路由按 `SYS_TENANT.SCHEMA_NAME` 解析，客户端不能提交 schema 名。同名 schema 已存在而没有可信归属映射时，创建必须拒绝；不得复用或覆盖。

`V13__TENANT_CODE_SCHEMA.sql` 在单个事务内将默认 `tenant_<ID>` schema 重命名为 `tenant_<code>`，同步映射，不重建或删除业务表。执行前必须停止租户流量并盘点、备份；发现非法 code、重复历史 code、映射缺失、源不存在、目标冲突或非默认前缀时会阻断，不能自动清洗或跳过。非默认前缀需单独确认迁移方案，不能从名称后缀猜测前缀。

当前 dev/prod 的 Flyway 自动执行处于关闭状态。本次提供迁移代码与隔离 PostgreSQL 验证，不代表真实环境数据库已迁移；不得为执行迁移而擅自开启整个历史迁移链。

PostgreSQL 迁移支持由 `flyway-database-postgresql` 提供，与现有 Flyway 使用相同版本；这是数据库插件，不是启动迁移的开关。依赖依据：[Flyway PostgreSQL 官方说明](https://documentation.red-gate.com/flyway/reference/database-driver-reference/postgresql-database)。

### 本次验证范围

独立复核完成 224 项模块测试和 26 项定向启动模块测试，共 250 项，零失败、零错误、零跳过；`gradle.bat build :web-booter:compileTestJava -x test --no-daemon` 构建通过。启动模块仅运行指定的 9 个测试类，没有执行会访问开发数据库的全量测试。

隔离 PostgreSQL 中验证了真实 Flyway V1–V13 初始化、旧租户账号拆分后 schema 重命名、数据及主键关联保留、序列与权限保留，以及后续目标冲突时整个事务回滚。租户编码不可变由业务服务强制执行；数据库约束保证格式、非空和全历史唯一性，不代替对直接 SQL 更新的权限管控。真实环境迁移和全部模块端到端验收不在本次已验证范围内。
