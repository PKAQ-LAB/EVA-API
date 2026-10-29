# phaseC1固定角色GET资源拒绝脚本交审

2026-10-29 backend_developer。仅依据总控新增脚本与纯fake授权实现；没有真实HTTP、密码接收、DB/Redis/服务/UI/权限操作、测试升级或提交推送。当前LIMIT窗口未操作；真实命令只作待审用户草案。

## 新旧身份与范围

- 新脚本`.gradle/org-c1-get-probe-20261029.py` SHA256：AF5FEFBA9B317CA98C8CEA1241B53CCB8FB69A96E28B891BC7913F4DA2A514A3。
- 原stage1 `.gradle/org-self-stage1-20261029.py`仍D005A1E898CD5B900FB75E3149F78D20F1E99BCC2E7529424C79F5E2F95335AE，未改。
- 新脚本每次入口核D005文件SHA后导入，仅使用其纯JSON/断言/登录投影/token校验/envelope/己方读取校验。没有改旧target/ROUTES/globals或调用旧transport/main/self_test；D005原路由白名单仍冻结。
- 原transport与旧白名单耦合，故本脚本明确独立最小HTTPConnection实现，不建立通用配置框架。

## 唯一五请求

目标只http://localhost:9016/api，LIMIT固定QA_LIM_20261023/账号2114719096971497474/己组织2115435549295775746，角色只已批准SELF2113857683657629697。核心request同时检查白名单、顺序索引及<5上限：

1. POST /api/auth/login，getpass后UTF8 MD5小写hex的account/password JSON。
2. GET /api/auth/fetchMenus。
3. GET /api/sys/organization/list?id=2115435549295775746。
4. GET /api/sys/organization/get/2115435549295775746。
5. GET /api/sys/role/get/2113857683657629697。

前四步本轮基线重新成立才发第五步，不借旧stage1结果。login需原0000成功、可信主体ID和boolean能力；menu需0000、数组及resourcePermissionEnabled严格true，false/null/字符串/数值均止。己方list/get使用精确ID正向断言。任一步错停，不PEER/跨组织/dict/logout/refresh/写路由或额外探测。

固定UA EVA-Local-Read-Probe/1，仅本次body access_token内存Bearer，无Cookie/Set-Cookie读取、proxy、redirect/retry/fallback。连接3秒、socket read5秒、请求wall timer<=5秒、总90秒、最大2MiB，peer回环校验在发凭据前。参数只空/--self-test/--run，密码不argv/env/文件，getpass无echo且无回退。finally关闭自有连接、清除引用，不宣称Python字符串物理擦除或server session登出。

## 第五步严格判定

只有HTTP200、success恰false、code精确0x000-00024且data缺省/null/空容器，才success=true及code/resourceDenial=RESOURCE_GATE_DENIED。

任何success=true包括null/{}/[]都RESOURCE_GATE_NOT_REJECTED；失败但非空data为RESOURCE_RESPONSE_DATA_NONEMPTY。未知/无记录/expired/404/4008/500等均不是拒绝PASS；body类型错误同样止。角色数据不遍历、不展开、不打印，只投影httpStatus/严格responseSuccess/已知code或UNKNOWN/dataNonempty。没有实际角色ID、name、scope、作者字段或body出现在摘要。

安全摘要独立phaseC1，含本次loginObservation和己方两个checks；editDenial固定NOT_COVERED_EDIT_DENIAL、crossRead固定NOT_COVERED_PHASE_C1、dataPermissionRootCause固定NOT_PROVEN_BY_READS。不能合并为stage1跨主体成功或SELF根因证明。

正常认证可能维护session/token缓存、审计及角色资源cache miss加载，不宣称全无副作用；此脚本不含组织/角色业务写路由，不将Redis会话维护等同业务数据更改。

## 完成的验证

Python AST通过，新旧两SHA已实算。纯fake --self-test退出0：22业务场景、4目标负例、7新transport假连接场景，总90秒期限负例、D005依赖hash漂移拒绝均通过，realHttpRequests=0，secretRedaction/requestOrderAndMaxFive=true。

业务覆盖正常五步至00024；login失败；menu false/null/string/number；两己方读失败且不发第五；role success含对象/null/{}/[]；failure+非空对象/标量；noRecord/expired/404/4008/500/未知秘密code；timeout/redirect。逐场景断言出站顺序、实际请求数<=5、首错停、token清除；每个错误码/count结果均与预期一致。秘密植入token/rolebody/message/code/异常，摘要及stdout/stderr不泄漏。

新transport自身fake覆盖固定Host/port/UA/Bearer且无Cookie、peer非回环拒绝、3xx未读Location/body即拒、坏JSON、响应超限、timeout与秘密异常。最多一次调用无重试，全部假连接，无实际localhost请求。

默认入口已实际运行退出0：phaseC1/NOT_RUN/maxRequests5/realHttpRequests0/passwordInput=false。未输入密码或运行真实--run。本轮fake未发生需修复的失败；早前stage1证据没有改写。

## 交审命令

离线自测：

```powershell
C:/home/DevApp/python3/python.exe -I -B C:/home/workspace/lab/EVA-API/.gradle/org-c1-get-probe-20261029.py --self-test
```

以下仅为designer/QA审查中的用户本地命令草案，审过后由总控交付用户本人，当前不得由代理执行：

```powershell
C:/home/DevApp/python3/python.exe -I -B C:/home/workspace/lab/EVA-API/.gradle/org-c1-get-probe-20261029.py --run
```

用户后续只回安全JSON，绝不回密码/token/body。现只交审，不建议用户重试或主动接收凭据。
