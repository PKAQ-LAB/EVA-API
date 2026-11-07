# Nexus 私服发布

发布配置在根目录 `publish.gradle`，依赖下载配置在 `build.gradle`。继续使用项目本机 Gradle，不需要 Wrapper 或 Nexus 专用发布插件。

## 已配置的仓库

| 用途 | 地址 | 类型 |
| --- | --- | --- |
| 正式版发布 | `http://dev.jm-sk.com:12081/repository/maven-releases/` | Maven hosted |
| SNAPSHOT 发布 | `http://dev.jm-sk.com:12081/repository/maven-snapshots/` | Maven hosted |
| 依赖下载 | `http://dev.jm-sk.com:12081/repository/maven-public/` | Maven group |

以上名称及类型已通过该实例的仓库列表接口核对。发布目的地根据项目实际版本是否以 `-SNAPSHOT` 结尾自动选择；不要把带 `#admin` 的控制台地址当作发布地址。版本由项目手工维护；后续新版本遵守 `docs/DEVELOPMENT_RULES.md` 中的 `1.y.z.n` 规则，不因切换私服自动调整版本。

## 凭据只配置在本机

控制台登录不会给 Gradle 提供凭据。使用有目标仓库上传权限的账号，在 **Gradle 用户目录** 的 `gradle.properties` 中添加下列两项；不要添加到本仓库的 `gradle.properties`：

```properties
nexusUsername=你的发布账号
nexusPassword=你的发布密码或可用用户令牌
```

Gradle 用户目录优先使用 `GRADLE_USER_HOME`，未设置时为用户主目录下的 `.gradle`。也可以通过 `ORG_GRADLE_PROJECT_nexusUsername` 和 `ORG_GRADLE_PROJECT_nexusPassword` 环境变量提供对应值。不要把密码放在命令参数、聊天或 Git 中。

依赖下载默认匿名；提供上述属性时会同时用于 Maven group 的认证。`mavenLocal()` 和 `mavenCentral()` 的原有解析顺序保留。

发布脚本也兼容本机 `publish.gradle` 中的 `ext.nexusUsername` 和 `ext.nexusPassword`。选择这种本机保存方式时，含凭据的 `publish.gradle` 改动不得提交；文档和日志中不得记录真实凭据。此方式仅用于制品上传，依赖下载仍按上述用户级属性或匿名方式认证。

## 预检和发布

在项目根目录使用本机 Gradle：

```powershell
# 仅查看发布计划，不编译、不上传。
gradle publish --dry-run --no-parallel --max-workers=1

# 确认版本、目标仓库与凭据后才执行实际上传。
gradle publish --no-parallel --max-workers=1
```

本项目只有引入 `publish.gradle` 的库模块定义 `mavenJava` publication。`web-booter` 和 License Server 不会因此变成待发布的脚手架依赖。

正式版本不要覆盖已发布的同版本制品；按仓库的部署策略处理重复版本，优先使用新的正式版本号，日常反复发布使用 `-SNAPSHOT` 版本。

## 可选配置

- `nexusBaseUrl`：替换 Nexus 根地址，同时影响发布和依赖下载，默认上述地址。
- `nexusPublishUrl`：仅覆盖制品上传的完整仓库 URL，通常不必设置。
- `evaSigningEnabled=true`：显式启用原 Gradle GPG 签名流程，签名所需 `signing.keyId`、`signing.password`、`signing.secretKeyRingFile` 均放在用户级 Gradle 配置中。默认不开启，不再依赖原开发机的密钥文件。

HTTP 仅对使用 HTTP 的仓库显式允许；以后换 HTTPS 根地址时不需要继续允许不安全协议。
