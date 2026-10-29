# 本地 stage1 脚本独立终审

2026-10-29 technical_designer。实读 ignored 脚本全文件、合成测试与交接，追踪登录 handler/Response/AuthBootstrapResponse/ResponseUtil、现登录测试、组织 get 与 list XML。只读审查，未运行脚本、PLAN、自测、HTTP、DB、UI或服务；仅写本设计。

## 结论

脚本 SHA256 实算并复核均为 `69AE56AF3E8D60700F2BE58784C380E721ABA19793299E1F56309478AB27CB59`。未发现需阻止交用户本人本地执行的静态问题，可由总控按已确认范围交付。此结论不表示真实接口已通过。

路径：`.gradle/org-self-stage1-20261029.py`。默认参数仅输出 NOT_RUN，无getpass/transport；真实运行仅精确--run，误传参数不回显。凭据仅用户本机TTY/getpass，GetPassWarning拒绝，不回退普通输入；UTF8/MD5小写hex，body仅account/password。本次login body token合法性及user_info.id均核；两个独立client分别持token，finally清除。无Set-Cookie读取、CookieJar、Cookie认证、refresh/logout或浏览器凭据来源。

## 实读边界

- 常量固定localhost9016/api、两已批准账号及两组织样本。ROUTES只允许POST login、GET fetchMenus、两ID限定list?id及get；无可配URL/其它写路径。HTTPConnection直连不使用代理环境，建立连接后核peer回环才发HTTP；拒绝3xx且不读Location，不重试，不关TLS认证。
- 每主体login/menu/list己/get己/get他，最多10。连接3秒、请求读取5秒并有timer/关闭、总运行90秒及2MiB响应限制；错误固定枚举输出。计数在exchange前消耗，不因失败补请求。CLI输入阶段不发网络。
- Json拒重复key，envelope核HTTP与原生boolean/code类型。登录/菜单成功码0000，菜单数组与能力boolean必需；旧false无缓存回填。list XML确有 `q.id` 对 y.ID 精确绑定，避免全量组织读取。
- 己方list/get必须精确己样本ID；任意跨主体非空data立即失败停报。成功空值/空对象为UNEXPECTED_EMPTY_SUCCESS；只有HTTP200、success=false、精确0x000-02001及空data可暂存不可见。第二账号己方基线不成立则全轮失败，第一账号也不标成功；双基线全部成功才确认跨详情不可见。
- 资源拒绝0x000-00024、过期、泛401/403/404、未知业务失败不冒范围PASS；resourceDenial/editDenial明确NOT_COVERED，SELF根因NOT_PROVEN。输出固定标签/白名单业务code/计数/boolean，不输出实际ID、组织字段、message、headers、body、凭据或异常文本。最后设置的READ_PHASE_PASSED只表示此只读阶段。
- 合成测试从源码可见业务17场景、假连接7场景、目标4负例、预算/期限2及输入3；覆盖双己方基线失败、空成功、跨暴露立即停、token隔离、HTTP故障及秘密注入。结果为实现方实际自测报告，本角色未重复执行。测试不直接等同真实服务验证。

## 登录成功码判断纠正

审查中曾仅据CommonCodes.LOGIN_SUCCESS_WELCOME的0003判定脚本0000有误，已立即撤回并告总控/backend不要据此修改。完整链为 Response.success(data,BizCode,args) 仅setMtype而不setCode，默认code仍0000；AuthBootstrapResponse复制原code，登录handler直接ResponseUtil.write，不经控制器响应Advice。现AuthLoginCapabilityTest也明确断言登录JSON code0000。因此脚本当前0000与实际直写链一致，无须新增0003通行条件。该判断更正未导致脚本变更，最终SHA不变。

## 交付限制

只交用户本人本机运行及安全JSON摘要，不向代理提供密码/token/完整响应。真实登录会产生本次session、审计及可能token缓存，退出只释放内存，不称服务器session已注销。外部运行覆盖若导致认证拒绝安全停报，不改认证通道。未授权额外负例、写操作、创建样本/账号、扩权、服务操作或重跑其它测试。
