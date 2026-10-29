# loginObservation安全投影窄修交接

2026-10-29 backend_developer。按总控批准的local-api-login-safe-diagnostic方案，仅修改ignored脚本及新增本交接；未实际HTTP、接收新密码、读取console/history/浏览器、操作DB/Redis/服务/UI或业务源码，未自动要求用户重试。

## 身份及历史证据

脚本`.gradle/org-self-stage1-20261029.py`新SHA256：D005A1E898CD5B900FB75E3149F78D20F1E99BCC2E7529424C79F5E2F95335AE。

旧69AE56AF3E8D60700F2BE58784C380E721ABA19793299E1F56309478AB27CB59版本的用户实际一次结果保持不变：success=false/code=LOGIN_FAILED/requests=1，LIM checks空，cross NOT_VERIFIED，resource/edit NOT_COVERED，rootCause NOT_PROVEN。此次不能用新投影补出旧响应内容或认定密码错误；原版本交接和失败证据未修改。

## 最小差异

仅新增login_observation和token_observation两个纯投影函数、每主体初始loginObservation、三处登录路径挂接，以及18个fake诊断场景。原envelope/login_token校验、MD5、Bearer/路由/10请求预算/noRetry、双己方基线、cross精确noRecord和未覆盖标记均未改；成功码仍严格0000，0003不加入SAFE_CODES也不接受。

每主体初始loginObservation为httpStatus:null/responseSuccess:null/businessCode:UNKNOWN/failureStage:NOT_REACHED。请求成功返回已解析响应后、envelope断言前，安全写入httpStatus（仅原生int且100..599）、responseSuccess（仅strict bool）、businessCode（仅原生str且既有SAFE_CODES精确成员，否则UNKNOWN），stage=LOGIN_RESPONSE。坏类型不引发投影异常，也不修改原HTTP_REJECTED/INVALID_ENVELOPE等判定。

只有原envelope成功后才附四布尔dataIsObject/tokenPresent/tokenIsString/tokenFormatValid；tokenPresent表示字典包含access_token键，null与缺键可区分，格式布尔与现有login_token条件等价。原能力/身份校验保持原处；即将调用login_token前stage改LOGIN_TOKEN。没有token值/长度/claims/ID/message/headers/Cookie或原body。stage表示本次登录已达的判断点，成功登录也会留下LOGIN_TOKEN及tokenFormatValid=true，不是新增认证结论。

网络、Content-Type、JSON失败尚未返回已解析载荷时仅保初始NOT_REACHED，原固定错误仍保留，不为补字段重发。两个账号各自观察，第一主体失败不伪造第二主体。

## 自测结果

AST通过。最终--self-test退出0：原17业务、4目标负例、7假连接、2预算/期限、3安全输入全部保持通过；新增loginDiagnosticCases=18，真实HTTP请求0。

18个新增场景：HTTP200 false+真实认证失败两码0x000-00001/0x000-00000；未知秘密code/0003；401/403/404；success坏类型/code数组/根body数组；token缺键/null/数值/空/超限/非法字符；坏JSON/timeout。全部仍首请求停止、无checks或第二主体，stage和原失败枚举准确。非envelope成功不多出token投影；token失败的存在/类型/格式布尔按实际fixture断言。秘密注入code/data/token/message/异常后，安全JSON无秘密且stdout/stderr空；没有记录秘密或token长度。

默认无参数仍退出0、NOT_RUN/maxRequests10/realHttpRequests0/passwordInput=false，没有索密码或网络。本轮新增fake全部首次通过，未触发真实请求或重试；原69AE实际失败不被改写为通过。

## 待独审与使用边界

自测命令：

```powershell
C:/home/DevApp/python3/python.exe -I -B C:/home/workspace/lab/EVA-API/.gradle/org-self-stage1-20261029.py --self-test
```

真实--run入口和预算没有改变，但此新版本须designer源码独审、QA仅fake独测后，由总控另决定是否交用户下一步；当前不让用户重输密码或执行真实登录。诊断只能区分安全状态/业务码和客户端校验阶段，仍不能单靠通用登录失败码确定tenant/锁定或密码根因。
