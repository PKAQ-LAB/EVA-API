# 本地组织只读接口验证第一阶段契约

2026-10-29 technical_designer。根据总控转达用户确认，仅供 backend 准备 ignored 脚本与合成 HTTP/脱敏测试；本角色只读核源码并写设计，未请求真实 HTTP、接收密码、执行测试或修改业务源码。

## 固定目标及输入

目标唯一为 `http://localhost:9016/api`，端口及协议不可由参数、环境或响应覆盖。连接仅本机回环；禁代理、自动 redirect、自动重试、自动刷新token、URL拼接任意路径、禁用TLS验证或转向其它host。拒绝所有3xx，不读取/输出Location；本阶段无HTTPS变更需求。

账号仅已批准 QA_LIM_20261023、QA_PEER_20261023。两组织样本的完整十进制ID必须从总控批准账本绑定，样本与主体归属固定；未取得准确两ID/归属就保持准备态，不从全量响应挑新样本、不创建或修改样本。ID按字符串精确比较，禁止浮点转换、任意path/query输入。不得以本设计猜测ID。

两密码必须由用户在本人本地交互终端通过getpass无echo输入；无可用安全终端即停止，不退回普通input。账号可固定，密码不经命令行/env/文件/日志/浏览器store/token。明文按UTF-8求MD5、输出小写hex，与前端 MD5.hash(password).toString()一致；仅内存生成JSON `{account,password}`。无必要不传autoLogin；禁止schemaName。后端schema模式另需tenantCode，但现前端登录不提供；不可从配置猜tenant或自动枚举，遇此情形停止报告前置不满足。

## 请求白名单及预算

每账号顺序：POST `/auth/login` 一次；成功后GET `/auth/fetchMenus` 一次；GET `/sys/organization/list` 一次；GET `/sys/organization/get/<己方样本>` 一次；GET `/sys/organization/get/<对方样本>` 一次。总计最多10次，一次性顺序运行，不并发；所有其它method/path/body拒绝。无fetchDicts必要，不调用dict/logout/refresh/角色/用户档案接口；禁止POST edit/del/sort/switch/checkUnique。

每请求连接超时3秒、读取超时5秒，完整脚本墙钟预算90秒；响应上限2MiB，超限终止，无额外探测/重试。任一登录/bootstrap/己方基线失败即停止，不能再采跨主体负例。请求采用固定技术User-Agent `EVA-Local-Read-Probe/1`，不伪造浏览器/转发IP/设备headers。登录无Authorization，后续只用该账号本次响应 `data.access_token` 的 `Authorization: Bearer <token>`；不复用另一账号token或Cookie，不建立持久CookieJar、不读取Set-Cookie值作为凭据。固定User-Agent用于技术识别，不保证与浏览器会话相同。

login本身会签发session、记录登录审计并可能写token缓存；这是授权登录的实际副作用，不能称完全无服务端写入。源码按newSessionId签发本次会话，脚本不得主动关闭已有会话。logout属额外请求，本阶段不调用。finally丢弃密码、digest、token及原始响应引用，进程退出释放内存；不得承诺Python字符串可物理擦除或服务端session已撤销，服务端本次session按原TTL管理。

## 响应及判定

实读 LoginSuccessVo：token位于data.access_token，身份位于data.user_info。成功严格要求HTTP200、JSON对象、success恰为true；登录还要求非空字符串token。不要用status=ok/任意2xx/有token即成功。fetchMenus还须data数组、外层resourcePermissionEnabled为原生boolean。false不算资源拒绝测试已开启，未知能力不是PASS。

JwtAuthFilter的资源拒绝为 Response.failure(AuthCodes.RESOURCE_FORBIDDEN)，精确code `0x000-00024`；ResponseUtil默认HTTP200。因此资源拒绝必须HTTP200且success恰false且code精确匹配，不能把任意401/403/404、错误页、timeout、网络故障或服务端异常记为权限PASS。认证过期/权限版本变更也须停止而非作为拒绝证据。

己方GET必须success=true且data对象中id与批准己方ID精确相等。OrganizationService.get查询不到记录调用 CommonCodes.CAN_NOT_FIND_RECORD，code为 `0x000-02001`；跨主体预期只可在两方己方存在基线均成立后，以HTTP200、success=false和该精确code记为「跨主体详情不可见」，不能单凭此确定SELF插件根因或把它叫资源拒绝。应先缓存两方结果，等双基线齐备再作最终判定。跨主体返回成功且data有任何对象均不算拒绝，不只检查id不等。

list只在内存解析树，核结构及目标样本存在布尔/节点计数；不得打印其它ID、名称、父节点、组织PII或整data。未知结构、坏JSON、空响应、success类型错误、业务码不识别一律失败或未覆盖，不计通过。

资源未授负例仅允许从以上已批准list/get请求中，根据已确认的当前账号资源清单选取确实未授且无写副作用的请求；不得增加请求预算或选其它接口。若两账号均拥有这些GET资源，则本阶段没有资源未授负例，必须单独标 `NOT_COVERED_RESOURCE_DENIAL`。读取成功只证明读取，不证明edit/del被拒。两类负例与正向基线分项输出，不用一个笼统PASS掩盖未覆盖。

## 输出与合成验收

只允许输出固定步骤标签、HTTP状态、原生success、已知业务code或UNKNOWN、计数、样本匹配布尔、固定结论/错误枚举及总请求数；主体用LIM/PEER标签，样本用SELF/OTHER标签。禁止打印密码/digest/token、headers、Cookie、URL中实际样本ID、响应message/args/data、异常repr/traceback、组织业务字段。响应可含敏感内容的code也不能原样打印，须先做已知code白名单映射。结果持久化如有需要也只限同一脱敏投影，默认仅stdout；不保存原始HTTP。

fakeHTTP测试覆盖出站白名单/固定回环/禁proxy+redirect+retry、账号token隔离、10次预算/超时/超限、HTTP200业务失败、认证失效/404/坏JSON不算PASS、双己方基线、资源拒绝与范围不可见分项、缺负例不冒PASS。植入synthetic秘密至token/Set-Cookie/message/code/data/异常，断言stdout/stderr/结果无泄漏；测试使用纯合成transport，不调用真实localhost。脚本与测试定稿仍须独审，不能由本设计自动获得真实HTTP执行许可；密码阶段由用户本人运行。

## 总控最终裁定

认证保持上述已审 Bearer/header 源链，不实施 Cookie-only 或认证通道 fallback，不读取 Set-Cookie。总控已核前端/header 源与后端默认及项目非敏感 headerName 一致；若运行环境外部覆盖导致拒绝，脚本安全停报，不读敏感配置、不自动改通道。

跨主体 get 返回 success=true 且 data 为 null 或空对象时，固定判为 UNEXPECTED_EMPTY_SUCCESS 并停报，不作为可见性隔离通过。仅纳入可从实际源码追溯的业务码；额外候选码未经核实不得加入 PASS 白名单。以上用于 backend 定稿及后续独审，不新增真实 HTTP 或其它执行授权。
