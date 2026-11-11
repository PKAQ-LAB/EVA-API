# 租户日志文件

宿主通过 `logging.config: classpath:eva-logback-spring.xml` 启用共享配置，文件根目录为 `logging.file.path`，未配置时使用进程工作目录下的 `logs`。衍生宿主升级到包含此能力的新制品后，需显式使用该配置；仅升级依赖不会覆盖宿主自定义日志配置。

现有 dev／aliyun 配置保留根目录 `d:/applogs`，prod 保留 `/opt/data/logs`，均可通过 `EVA_LOG_PATH` 覆盖；原固定文件名 webapp.log 改为上述根目录下的按日文件。

开启 `eva.tenant.enable` 时，可信正数租户 ID 输出到 `tenant-ID`；平台租户 0、未知身份和启动日志进入 `platform`。关闭时所有身份统一进入 `standalone`。ID 从服务端用户／租户上下文或审计事件取得，不使用浏览器提供的任意目录名。

目录示例：`logs/tenant-123/operation/2026-11-11.log`。日志类型为 `application`、`login`、`operation`、`error`；普通 ERROR 含堆栈输出到 error，其他普通日志输出到 application。各类型独立文件，不把错误再重复写入 application。控制台输出保留。

使用上海时区每日滚动，活动文件直接带日期；跨日后首次写入创建新文件，无事件日期不生成空文件。默认每个分流保留 30 天，通过 `logging.logback.rollingpolicy.max-history` 调整；不使用按大小分片，因此单日文件可能较大。SiftingAppender 最多跟踪 256 个活跃输出，空闲 30 分钟关闭，可在日志 XML 中按部署规模调整。

日志线程池复制日志 MDC，并在结束或异常后恢复，避免线程复用串租户；不复制认证身份或数据库路由。业务、错误、登录事件显式固定事件租户，核心 schema 写入不能改变日志归属。其他自建异步线程池需自行传递日志上下文；无可信上下文的日志进入平台目录。

多实例部署每实例使用独立本地目录或独立卷路径，禁止多个 JVM 共享写同一个文件。此能力只负责文件输出，不能替代 Loki 查询授权。

原有数据库写入、错误聚合和业务日志归档仍保留。此阶段不发布制品、不升级 Template 或会务项目、不接入 Alloy／Loki、不修改数据库或自动重启服务。现有部署需重建并加载新配置后才生效。
