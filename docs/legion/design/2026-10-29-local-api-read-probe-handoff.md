# 本地SELF只读API脚本交审

2026-10-29 backend_developer。用户已允许本地专用API脚本，总控仅授源码准备与纯合成自测；当前未访问真实HTTP、接收密码、读取浏览器凭据、操作DB/Redis/服务/UI或修改生产/前端/旧验证工具，未提交推送。真实--run仅在独审通过、总控交用户后由用户本人本地运行。

## 定稿身份

- 脚本：`.gradle/org-self-stage1-20261029.py`。
- SHA256：69AE56AF3E8D60700F2BE58784C380E721ABA19793299E1F56309478AB27CB59。
- Git只读核对：cornerstone，HEAD 1f708e202af0ba817b55c0f6be9a9085eea054ea，相对origin/cornerstone ahead4；无Git写入。
- 权威契约：2026-10-29-local-api-read-probe-contract.md及总控最终Bearer裁定。cookie初稿未执行，已移除；定稿不读Set-Cookie、不建CookieJar、不cookie fallback。

## 已核源码协议

前端login/index.tsx使用MD5.hash(password).toString()；脚本本地getpass后按UTF-8求小写MD5 hex，登录body仅account/password。JwtUsernamePasswordAuthenticationFilter读取这两个键，LoginAuthenticationProvider将presentedPassword交PasswordEncoder.matches；不额外hash、不猜tenantCode或schemaName。

前端requestErrorConfig.ts实际Authorization: Bearer拼接，与Jwt.java默认header/tokenHead及当前源eva.yaml的Authorization/Bearer一致。脚本只从本次成功login data.access_token取得该主体token，并校验data.user_info.id为账本账号ID；之后只发Bearer。运行外部覆盖若改变认证方式导致失败，安全停报，不换通道、不读环境。

OrganizationQueryBo显式Long id，Organization.xml selectOrgMapList用q.id精确过滤y.ID。每主体list只查己方ID，避免全表返回；get使用控制器get/{id}。get缺记录由OrganizationService.get抛CAN_NOT_FIND_RECORD，确切code0x000-02001；JwtAuthFilter资源拒绝为0x000-00024，不能混同。旧4200候选已移除，泛401/403/404/未知业务码不算范围不可见。

## 固定请求清单与预算

目标常量唯一http://localhost:9016/api，不接受argv/env/响应覆盖host/port/protocol/path。HTTPConnection直连，无代理、redirect、retry、refresh；连接后先确认peer为回环，再发凭据或token。任何3xx在读取Location/body前拒绝。

固定账本：QA_LIM_20261023账号2114719096971497474/组织2115435549295775746；QA_PEER_20261023账号2113894345662021633/组织2115579583561605122。无参数扩展或从列表挑新样本。

每主体严格顺序五请求：

1. POST /api/auth/login，body仅该固定account与本地MD5。
2. GET /api/auth/fetchMenus。
3. GET /api/sys/organization/list?id=己方组织ID。
4. GET /api/sys/organization/get/己方组织ID。
5. GET /api/sys/organization/get/对方组织ID。

总最多10请求，串行，无重试或额外探测；固定UA EVA-Local-Read-Probe/1。连接timeout3秒、socket读取timeout5秒，单请求还设不大于5秒墙钟中断timer，整轮90秒预算在每次请求前检查；响应最多2MiB。timer仅关闭当前自建HTTP连接，不碰服务进程。没有dict/logout/getAlpha/edit/del/sort/switch/checkUnique/角色/profile/写数据库调用。

## 成功与未覆盖

login严格HTTP200/success原生true/code0000、token合法非空、身份ID精确且能力为boolean；fetchMenus还需data数组及能力boolean。任何失败/缺字段不复用旧快照，token每主体独立内存；不持久化header/body/token。

己方list树必须仅返回己方一节点ID（允许正常children空结构），己方get对象ID精确，二者都需真实success。跨主体get任何非空data立即CROSS_DATA_EXPOSED停止，不继续第二主体；success=true的null/{}返回UNEXPECTED_EMPTY_SUCCESS而非PASS。仅HTTP200、success=false、code0x000-02001、data空可记暂存不可见；等两主体己方基线全部成功后才将双方跨详情结论确认。第二主体基线失败时第一方暂存结论仍非通过。

最终code=READ_PHASE_PASSED仅表示此只读阶段，另显式输出crossDetailVisibility；resourceDenial固定NOT_COVERED_RESOURCE_DENIAL、editDenial固定NOT_COVERED_EDIT_DENIAL、dataPermissionRootCause固定NOT_PROVEN_BY_READS。不能凭无记录证明SELF插件根因，不能凭读取证明edit/del拒绝；已有授权GET不另找资源负例。

stdout只含固定结论/标签、HTTP状态、白名单code、boolean/计数；主体LIM/PEER、样本SELF/OTHER，不含实际ID、account/name、message/args/data、token/digest/header/Cookie/异常文本或traceback。未知code不原样输出。输入仅无echo getpass；非TTY/GetPassWarning失败，无普通input回退；argv只认精确空/--self-test/--run，误传密码参数不回显。finally清除内存引用；不承诺物理擦除Python字符串或服务端session已登出。正常login仍会签发session/审计/可能token缓存，这是用户授权登录副作用，不称无服务端写入。

## 合成校验及安全失败记录

最终Python AST通过。--self-test退出0，安全摘要：scenarios17、targetNegativeCases4、transportScenarios7、budgetAndDeadlineCases2、safeInputCases3、realHttpRequests0、leakCheck/crossExposureStopsImmediately/bothSelfBaselinesRequired均true。

业务场景含正常/精确无记录、HTTP200登录失败、错身份、缺/null能力、success类型错、第二己方基线失败、cross404/未知code/非空data、过期/泛错误/资源拒绝/成功空值、超时与带秘密的异常。假连接层核固定localhost9016/peer回环/UA/Bearer无Cookie、无redirect/Location读取或重试、坏JSON/超限/非本机/timeout/秘密异常。预算及deadline拒绝有独立负例。synthetic秘密放入token、fake Set-Cookie、message/private data/code/异常，确认安全结果无泄漏；传输异常stdout/stderr均空，错误argv与getpass安全失败输出不含秘密。未连接真实localhost。

扩展测试首次失败实际安全输出保留如下（退出1）：`{"success": false, "code": "INTERNAL_ERROR"}`。只读定位为self_test断言：fake的second-baseline模式在第一方cross前错误返回成功空值，导致先触发UNEXPECTED_EMPTY_SUCCESS；修正fake为第一方cross正常无记录后，才到第二方己方失败，验证整轮仍NOT_VERIFIED。修复未放宽真实read_result门禁；随后扩展全部通过。此前12场景版本也曾通过，但以本定稿结果为准。

默认无参数实际运行退出0：NOT_RUN、maxRequests10、realHttpRequests0、passwordInput=false，无密码提示/网络。所有真实--run均未执行。

## 待审命令

纯合成自测：

```powershell
C:/home/DevApp/python3/python.exe -I -B C:/home/workspace/lab/EVA-API/.gradle/org-self-stage1-20261029.py --self-test
```

以下只作审查中的用户本地命令草案，须designer独审通过后由总控交用户本人，当前不执行：

```powershell
C:/home/DevApp/python3/python.exe -I -B C:/home/workspace/lab/EVA-API/.gradle/org-self-stage1-20261029.py --run
```

用户只需回传脚本安全JSON摘要，不提供密码/token或完整响应。代码定稿仍需designer审，脚本本身不给代理真实HTTP许可。
