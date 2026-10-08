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
