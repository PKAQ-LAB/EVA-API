# loginObservation 窄修独立审查

2026-10-29 technical_designer。只读实算脚本SHA及读取投影、挂接、合成用例、原transport/CLI与判定，不执行脚本/PLAN/selftest/HTTP，不接收密码，不操作DB/UI/服务，仅写本人设计记录。

结论：静态审查通过，未发现必须修复项。`.gradle/org-self-stage1-20261029.py` 当前SHA256为 `D005A1E898CD5B900FB75E3149F78D20F1E99BCC2E7529424C79F5E2F95335AE`，与交接一致。可交QA按已授边界作fake独核；后续是否交用户真实运行仍由总控决定，此审查不自动要求用户重试。

login_observation只从已解析载荷投影原生int状态100—599、原生boolean成功标志、既有SAFE_CODES精确成员或UNKNOWN，以及固定LOGIN_RESPONSE阶段。坏根类型、success/code类型不会引出自由文本或新的投影异常。请求未返回已解析对象时保NOT_REACHED，不新增请求。

只有原envelope成功后才增加dataIsObject/tokenPresent/tokenIsString/tokenFormatValid四布尔。tokenPresent明确表示键存在，可区别缺键/null；格式判定与原login_token相同且无token值、长度、前后缀或claims输出。能力及身份条件继续先验，login_token前更新LOGIN_TOKEN。成功登录也保留该阶段，它表示已达的校验点，不能单独解释为错误；仍以顶层code/success及布尔共同解读。

实读原envelope仍要求HTTP200+原生success=true+code0000；login_token非空/长度/字符限制未放宽，0003未加入白名单。route/MD5/Bearer/预算10/无Cookie及重试、双己方基线、跨主体非空立即失败、成功空值失败与精确无记录条件均保持。新增观察每主体独立，没有第二主体伪造记录；原未知错误继续固定枚举，不输出异常文本。

新增18个fake诊断用例覆盖两真实登录失败码、未知code/0003、401/403/404、坏success/code/body类型、六种缺坏token、坏JSON/timeout；逐一断言首请求停止、无checks/第二主体、原失败码和阶段、非成功envelope无token投影、秘密sentinel未进入JSON/stdout/stderr。实现方报告全部通过，本角色仅静态阅读，未重复运行；QA结果独立记录。

旧69AE实际LOGIN_FAILED/requests1仍无法补出响应状态或token形状，本次不得回填旧证据或推断坏密码/tenant/锁定。resource/edit拒绝仍NOT_COVERED，SELF根因仍NOT_PROVEN。新诊断仅改善下一次获准运行的可观察性，不新增成功证明。
