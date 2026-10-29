# stage1 首请求失败安全补诊断建议

2026-10-29 technical_designer。总控转达用户运行69AE版结果 LOGIN_FAILED、requests1、LIM无checks、crossNOT_VERIFIED。此次仅只读源码/现测试，不读取实际响应、密码、日志敏感体，不发HTTP、不执行测试或改脚本。保持停止，不要求用户重试以免累积失败锁定。

## 源码复核结论

未发现当前源码下此前测试mock掩盖code/mtype或token字段序列化差异。Response.code默认0000，success(data,BizCode,args)只setMtype；AuthBootstrapResponse复制原code；handler直走ResponseUtil.write→JsonUtil.toJson，不走MVC Advice。JsonUtil静态使用真实JacksonObjectMapper，未配置改写code/命名策略；Long转字符串不会影响脚本按字符串核身份ID。LoginSuccessVo以@JsonProperty指定access_token、refresh_token、user_info。

AuthLoginCapabilityTest虽mock JWT签发、resolver、audit/cache，但真实调用handler→ResponseUtil→JsonUtil，并检查写出JSON的0000、access_token/user_info与忽略mtype/args。因此不能用欢迎枚举0003推翻当前链；也不能把该源码测试当已运行实例字节和实际登录结果的证明。Mock没有覆盖真实密码验证、token签发或部署产物，仍有这些运行证据边界。

69AE中的LOGIN_FAILED只有两类判定入口：登录envelope在允许状态/有效类型下未满足HTTP200+success=true+code0000；或成功响应中的access_token不满足非空字符串、上限及字符白名单。身份/能力缺失、坏JSON等有其它固定错误码。因此现摘要不能确定坏密码、tenant、锁定、服务版本或token格式中的哪一种。

登录failure handler对BadCredentialsException映射0x000-00001，其它AuthenticationException默认0x000-00000。即使补出这两码，也不能单靠通用失败码细分tenant/锁定根因；禁止按message猜测或输出message。密码构造仍与UI一致：UTF8明文MD5小写hex，provider对presentedPassword直接交passwordEncoder.matches，不新增额外hash或字段fallback。

## 最小补充字段

保留原成功条件、失败退出、请求白名单及10次预算，新增一个只针对本次登录的固定投影 `loginObservation`，在任何后续断言之前保存：

- `httpStatus`：实际整数状态，限定100—599；不可用为null。
- `responseSuccess`：仅原生boolean，否则null。
- `businessCode`：仅既有SAFE_CODES中精确值，否则固定UNKNOWN；绝不回显任意字符串或message。
- `failureStage`：固定枚举 LOGIN_RESPONSE 或 LOGIN_TOKEN；未到此处为NOT_REACHED。也可用固定布尔envelopeAccepted与tokenAccepted表达，不输出自由文本。
- 仅当envelope已成功才追加 `dataIsObject`、`accessTokenPresent`、`accessTokenIsString`、`accessTokenFormatValid` 四布尔，不输出token值、前后缀、长度、claims、header或Cookie。

LOGIN_RESPONSE观察应在envelope可能raise前构造，LOGIN_TOKEN观察应在login_token断言前构造；构造本身必须容忍坏类型。网络、JSON或Content-Type失败尚未取得投影时，只保留原固定错误与NOT_REACHED，不为补诊断重发请求。主体仅LIM/PEER标签，样本仍SELF/OTHER。代码0003如果实际出现仍UNKNOWN并失败，未获证不得放宽成功白名单。

## 必要合成用例

保持全部既有边界用例，并增：HTTP200/false+0x000-00001、HTTP200/false+0x000-00000、允许状态401/403/404、HTTP200/true+未知code分别停在LOGIN_RESPONSE，且不进行第二请求；正确envelope但token缺失、非字符串、非法字符或超限分别停LOGIN_TOKEN。正确0000及token保持原成功行为。

额外以秘密sentinel注入未知code、message、data、token及异常，断言摘要/stdout/stderr不含秘密，code严格UNKNOWN。坏envelope类型观察不抛出新的异常，原失败分类不被覆盖；两个账号使用独立loginObservation，失败第一账号不伪造第二账号结果。已有10次预算、无fallback/redirect/retry、跨主体空成功失败等规则不变。

该建议仅供总控决定是否另授脚本窄补及fake测试；当前69AE真实结果仍为未完成，资源/edit拒绝未覆盖。任何后续用户真实运行需总控结合新审查结果另行组织，不能自动要求重输密码或重试。
