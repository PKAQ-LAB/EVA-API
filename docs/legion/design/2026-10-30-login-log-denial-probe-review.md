# 登录日志资源拒绝最小探针独立源码审查

2026-10-30 technical_designer。结论：冻结脚本未发现静态阻断，可交 QA 独立 PLAN/fake 检查；本审查不执行真实请求，不证明业务管理员门禁或实例已装载新产物。

实际重算 `.gradle/login-log-denial-probe-20261030.py` SHA256 为 `769FE2F8D7C705BD9DB760863C3D38FC5C68F0C8FDB94B9A1D2D5E77BDD93C05`。依赖 `.gradle/org-self-stage1-20261029.py` 实际 SHA 为 `D005A1E898CD5B900FB75E3149F78D20F1E99BCC2E7529424C79F5E2F95335AE`，与加载前校验相同；导入只取解析/envelope/token方法，旧main受入口条件保护，不执行旧请求，也未修改其globals。后端 evidence JSON 实际SHA `C86D1FE3EF76B148B77D02AF26429DEB74DD3C45B120DBC86B338394F093AA35`，只读载明其 PLAN与fake56通过、无真实run；本角色未复跑，不代 QA 签审。

## 源码及真实拒绝路径

默认无参在加载依赖/输入密码/建立HTTP前返回PLAN；仅允许无参、--self-test、--run三个精确参数形态，无自定义URL/账号/ID。真实模式必须TTY，getpass警告升级异常禁止回显fallback，密码UTF8 MD5后只放内存；不读env/配置凭据、不写响应或token文件，不自动logout/refresh。

固定 `localhost:9016/api`、固定LIMIT主体、精确四次顺序login→fetchMenus→实际LoginLogCtrl list（固定pageNo1/pageSize1/account）→总控已批准自身get ID。Budget同时核目标、顺序、次数和总90秒；首错停止，不枚举、分页重试或额外请求。get样本归属以总控已有批准为前置，本角色不查询日志重新确认。login/menu均要求HTTP200/0000/严格success真，外层resourcePermissionEnabled恰原生True；login主体ID只收str/int精确匹配并核token格式，menu data必须list。

`JwtAuthFilter` 在MVC前资源校验失败返回 `Response.failure(AuthCodes.RESOURCE_FORBIDDEN)`，`ResponseUtil` 默认HTTP200/application-json，真实完整码 `0x000-00024`。脚本两日志API仅接受该HTTP状态、严格success False、精确完整码及data缺失/null/空list或dict。任何success True即使空data，或任意非空data，立即UNEXPECTED_ALLOWED并停止后续读取；其他业务拒绝、过期、权限版本改变、无记录、4200、HTTP403/404/500等不计通过。最终明确resourceGateOnly=true、administratorGuard=NOT_PROVEN，未将资源先行拒绝当作新业务管理员guard实测。

LocalHttp直接使用http.client，不使用代理环境、Cookie jar、跳转或重试。连接后且发送凭据前验证实际peer为loopback；connect至多3秒、单请求wall timer至多5秒、读取上限2MiB+1以拒超限，严格UTF8/重复JSON key拒绝，非application/json拒绝。timer和connection在finally取消/关闭。90秒预算从获取密码后的run_checks开始，非密码输入超时承诺。

输出仅固定步骤/结论、状态、白名单业务码和emptyData布尔，不包含响应body、菜单、账号ID、样本ID、token、Cookie/headers、message/异常原文；未知码映射UNKNOWN。错误仅白名单原因或INTERNAL_ERROR；credentials/token引用在finally清理，不承诺Python字符串可物理擦除。正常认证可能更新session/cache/审计，故这是固定业务读取/拒绝探针，不是所有系统存储零写入承诺。

## fake静态覆盖与交接

已通读fake实现：不真实联网的HTTPConnection替身覆盖run_checks分支，包含两日志请求分别成功/非空数据/相似错误码/严格布尔、login或menu能力不真/身份不符/形状错误、各步transport错误、最大次数/顺序/目标变更/预算。另用FakeConnection/Timer/Response覆盖真实LocalHttp方法的超时、跳转、远端peer、坏JSON/类型、体积、到期及正常路径，检查关闭、计时器清理、零stdout/stderr与sentinel不进入结果。实际fake是否通过由QA独立执行确认。

本角色仅写本设计/本人status、读取脚本及现源码，未改辅助/生产源，未运行PLAN/fake或真实HTTP、DB、服务、授权操作；C3仍USER_SKIPPED。真实run只能在总控确认独审及QA结果后，由用户本机输入密码执行。
