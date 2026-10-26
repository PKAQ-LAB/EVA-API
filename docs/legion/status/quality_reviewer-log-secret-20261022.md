# LOG-DESCRIPTION-SECRET-20261019 独立安全QA（2026-10-22）

结论：未通过，P1-LOG-SECRET-TEXT-FALLBACK。已读最终backend LOG交接、5生产/3test实际diff与当前Advice→EventHandler→Repository调用，强制26项通过仍有合成凭据遗漏路径。全部命令结束，后端修复锁交总控；浏览器继续暂停，不因单测通过解除安全门禁。

## P1 非JSON/解析失败回退会保留带标签的凭据

位置LogSanitizer.java63-73/89。sanitizeContent仅当trimmed以{/[或引号开头才readTree，解析失败也回到JSON_SECRET_PATTERN文本替换。该正则对token数组只吞到首个逗号，后续元素仍在；key中的JSON Unicode转义未解码也不会命中token/password等标签。嵌套TextNode递归同样进入此回退，因此不是仅任意无标签秘密猜测，而是明确定义的token字段/数组仍可泄露。

真实当前已编译LogSanitizer，经JShell+已安装Jackson执行，不复制函数或改源码，输出四个布尔结果均true（canary仍在）：
- prefix {"token":["QA_SYNTHETIC_A","QA_SYNTHETIC_B"]} → PREFIX_ARRAY_CANARY_REMAINS=true。
- prefix {"pass\u0077ord":"QA_SYNTHETIC_C"} → PREFIX_UNICODE_CANARY_REMAINS=true。
- {"token":["QA_SYNTHETIC_A","QA_SYNTHETIC_B"]（缺右花括号）→ MALFORMED_JSON_ARRAY_CANARY_REMAINS=true。
- 用真实ObjectMapper把第一字符串包为合法外层JSON {"remark": innerString}，再sanitize → VALID_OUTER_JSON_CANARY_REMAINS=true。

最后一例等价普通业务BO remark等字符串字段经JsonUtil序列化后的形态：Advice params/response调用sanitize，外层JSON成功解析后TextNode89递归退到不完整正则，未遮蔽的第二个canary仍会进入保存字段。当前final writer并未再次独立拒绝/清除该尾值；整对象description只投影ID并不能保护params/response中的这一路字符串。未读取真实密码/旧日志/数据库，未证明生产数据曾泄露，只证真实当前脱敏函数与实际调用链可保留合成secret。

最小修复建议：对无法完整解析的结构化凭据内容采取保守整体隐藏，或可靠解析并递归处理嵌入JSON片段后再返回；不能把数组/Unicode/失败JSON回落到当前不完整正则。普通安全文本/目标数字ID可保留，不需要引入通用安全框架或改权限/表。新增上述四类回归，至少一条穿真实Advice→最终mockRepository全字段断言，并补转义/深度解析失败路径；修后重跑精确26及新边界，不以已有纯合法JSON样例自证。

## 其它已审范围与现有证据

描述前整对象仅numeric id/userId/roleId/tenantId/mainId/param投影，不直接未知BO.toString；显式secret source路径遮蔽，description限长；多个param字段独立回填修正、空bizId避免finally null.toString，反射失败不带异常参数消息；rollback明确successfalse。5个改密/授权注解UPDATE与既有请求契约一致。本轮不声称任意无标签文本都能自动识别secret，也未在发现P1后扩大并修源码。

强制独立命令：C:/home/DevApp/gradle/bin/gradle.bat :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest -x jar --offline --no-daemon --console=plain。exit0/BUILD SUCCESSFUL28s、14tasks/2executed12up-to-date；两个修改模块test均真实executed。XML9Advice+7Sanitizer+1注册+1注解+4Module+4WriteProtection=26，failure/error/skipped全0。只是这些样例通过，新增独立探针明确失败安全目标。-x jar仅复用运行锁住的既有依赖包装，生产compile任务up-to-date，不声称完整打包或新运行进程已生效。

JShell初次classpath误选sources/javadoc jar导致NoClassDefFoundError，该次不当复现；改为已安装非sources/javadoc二进制Jackson jar后真实函数四探针均正常exit0并打印true。探针仅内存脚本，不写测试或生产文件、不安装依赖。diffcheck0，仅既有换行/JVM/Gradle弃用警告保留，index空。

业务日志LOG_BIZ数据权限缺CREATE_ID/MODIFY_ID及无Ignore、end=当天00:00且SQL<=等在backend报告是独立待验证源码风险，不属于此P1的运行复现，本角色不拿空列表推断写失败或越权。发现P1后先停扩展审查并交锁，待补修后继续必要分类，不擅改审计访问策略。图索引落后21commit/UNKNOWN不当低风险，当前调用链补证但不声称全图覆盖。

本角色只写此QA子卡，所有源码/DBRedis/HTTP/UI/服务/依赖/vault/Git/branchstash/用户真实密码和日志未动，无子代理。前端菜单在另一写者锁中，未检查其正在改的源码或页面；五系统测试对象保持。所有命令结束，交总控派最小补修，安全门禁不通过/运行版本未确认前不恢复真实日志详情或改密。

## P1-TEXT-FALLBACK补修独立复验（2026-10-22）

结论：原TEXT-FALLBACK四类探针已闭合，但日志安全整体仍未通过，新增P1-LOG-SECRET-LINEBREAK-KEY。所有命令结束，后端修锁交回总控；不得解开真实日志/改密/运行版本安全门禁。

补修实际：已读2production/2test增量、strict FAIL_ON_TRAILING_TOKENS、去旧JSON正则猜回退、结构化解析失败整体隐藏、32层及1100层解析失败、合法外层TextNode递归、Class[id=17]数字投影不重新拼JSON。四个原二进制探针逐个重跑，ORIGINAL_PROBE_1/2/3/4_REMAINS均false：prefix数组、Unicode转义key片段、malformed数组、合法外层字符串都不再带原canary。普通文本/数字目标保留，无Base64或任意编码推断；前缀安全JSON/引号/反斜杠被整体隐藏的可用性代价已接受记录。

新增P1位置：LogSanitizer.SECRET_FIELD_PATTERN/isSecretField。正则为(?i).*(password|...|token...).*, 使用matcher.matches()，Java的点号不匹配换行终止字符。合法JSON敏感key若为token后跟换行，Jackson解码后key包含实际LF，整个字段不被判为secret；其字符串值不带结构标记，sanitizeContent原样保留。

真实当前编译binary复现：JShell+已安装非sources/javadoc Jackson，new ObjectMapper().writeValueAsString(Map.of("token\n", "QA_SYNTHETIC_NEWLINE"))构造合法JSON，再LogSanitizer.sanitize(edge,16000).contains("QA_SYNTHETIC_NEWLINE")输出NEWLINE_KEY_CANARY_REMAINS=true。不是无标签任意秘密猜测，字段中实际含token，只因尾部LF绕过整串匹配。该JSON可在普通字符串/Map字段的编码JSON中出现，现Advice params/response与ErrorIncident字段均依赖同一入口，嵌套解码后同样有缺口。未读真实日志/密码/DB，不证明实库泄露。

最小修复：敏感字段匹配须在含换行的完整key中仍找出标签；可去掉多余.*后用find，或使用合适DOTALL匹配，复用现matcher不增加新架构/特殊源路径。新增LF/CR及Unicode行分隔符前后边界和嵌套编码JSON回归，至少一条实际Advice或ErrorIncident最终实体全字段无canary断言。原strict/失败闭合不可退回窄正则。修后重跑原四+此新probe及扩展定向测试。

独立强制37项：与前次相同core-log/sys --rerun精确6test classes及-x jar --offline --no-daemon，exit0/BUILD SUCCESSFUL53s、14tasks/2executed12up-to-date。两修改模块test确执行，当前XML Advice14/Sanitizer13/注册1/注解1/Module4/WriteProtection4=37，全failure/error/skipped0，时间13:38Z；仍不能据此宣告安全通过，因为独立新增probe明确未遮蔽。最终writer用真实Advice→events→实际handler→mockrepository，success/failure/rollback/ErrorIncident覆盖的已有用例已完整读；原业务异常保持/空ID/多字段/rollbackfalse通过。不宣称完整JAR打包或用户运行进程加载了这些class。

循环对象/序列化异常补充只读边界：sanitize只接受有限String，不存在JsonNode循环构造输入；Advice之前的共享JsonUtil.toJson使用Jackson并catch Exception返回null、同时log.error(e.getMessage(),e)，并非本次新增代码。循环/自定义getter失败的应用日志内容未做独立合成采集，不将其说成已覆盖安全，也不读取真实错误日志补证；如需扩大至全应用异常日志，由总控另派。当前发现P1后不扩改sharedJsonUtil或日志权限策略。

后台role/log policy guards仍未实施，不混入此补修，缺列与end当天00:00<=为此前源码风险/未做受限会话实测。前端菜单源码QA已交锁，浏览器被Edge扩展UI阻止后未恢复；没有local新记录/未确认写。源码/依赖/DBRedis/HTTP/vault/Git/服务均只读，原5sys对象不动，无子代理。只追加本人卡，命令结束交总控补修。

## P1-LINEBREAK-KEY 最终独立复验（2026-10-23）

结论：P1-LOG-SECRET-LINEBREAK-KEY通过，原P1-LOG-SECRET-TEXT-FALLBACK同时保持闭合。本轮既定日志脱敏代码安全范围独立通过；项目整体日志访问/发布门禁仍待role/log policy P0及运行版本确认，不能据此恢复真实日志详情/改密或宣称整体验收完成。前次remote compact Connection failed是会话中断，不记为测试失败或通过，本节为重新执行的独立证据。

已重读当前项目规则、权威军团规则、backend交接、本人前两轮失败记录、当前Git差异与实际调用链。保持cornerstone/index空；没有生产或测试源码修改。当前共享SECRET_FIELD_PATTERN仅移除外层点号通配并改find，仍为大小写不敏感password/passwd/pwd/token/authorization/cookie/secret/api[-_]?key子串判定，不缩成整字段等值；null和非敏感字段仍为false。JSON字段、Advice显式描述路径、带凭据数组文本失败兜底都调用isSecretField，无独立窄正则回退。strict FAIL_ON_TRAILING_TOKENS、解析异常不输出原文、结构化失败整体隐藏、TextNode递归、32层投影与长度限制、独立数字目标保留均在。

先于Gradle重跑，直接调用当前build/classes/java/main中的LogSanitizer及已安装Jackson二进制（排除sources/javadoc）JShell，未复制实现、未写探针源文件：ORIGINAL_PROBE_1/2/3/4_REMAINS均false，分别是前缀token数组、Unicode转义key片段、畸形数组、合法外层嵌套字符串；new ObjectMapper().writeValueAsString(Map.of("token\n","QA_SYNTHETIC_NEWLINE"))的NEWLINE_KEY_CANARY_REMAINS=false。扩展8种分隔符LF/CR/CRLF/NEL/LS/PS/VT/FF×10种现有标签写法×前后位置，共160组合法JSON，LINEBREAK_MATRIX_160_MISSES=0，exit0。全部值为合成canary。

独立强制命令（完整执行，无目标跳过）：
```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.pointcut.BizLogAdviceTest --tests org.pkaq.core.log.util.LogSanitizerTest --tests org.pkaq.core.log.util.RegistrationBodySanitizerReviewTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.BizLogOperationTypeTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest -x jar --offline --no-daemon --console=plain
```
结果exit0、BUILD SUCCESSFUL in 1m12s，14tasks/2executed/12up-to-date；core-log:test与sys-service:test均实际执行，非UP-TO-DATE。生产/测试compile复用未变产物。新XML UTC2026-10-22T23:09:50.315Z至23:09:53.267Z（北京时间10-23 07:09），不同于此前13:43旧报告：Advice16+Sanitizer15+Registration1+OperationType1+Module4+WriteProtection4=41，failures/errors/skipped全部0。XML路径为eva-core/eva-core-log/build/test-results/test/TEST-org.pkaq.core.log.pointcut.BizLogAdviceTest.xml、同目录TEST-org.pkaq.core.log.util.LogSanitizerTest.xml及RegistrationBodySanitizerReviewTest.xml；eva-web/eva-web-sys/eva-web-sys-service/build/test-results/test/TEST-org.pkaq.sys.BizLogOperationTypeTest.xml、TEST-org.pkaq.sys.module.service.ModuleServiceTest.xml、TEST-org.pkaq.sys.WriteProtectionServiceTest.xml。

已读真实测试fixture：Advice调用实际around，publisher回调真实BusinessLogEventHandler提交/回滚方法，Captor捕获最终mock BusinessLogRepository.save实体并对其全部字段JSON断言三个canary均不存在；成功/原业务异常原样抛出/finally/回滚false、嵌套编码JSON、目标31、tenant44/user55、空bizId、显式secret、多字段与CREATE/UPDATE均保留。错误链用真实ErrorIncidentEventHandler直到最终mock ErrorIncidentRepository.save，全实体canary断言和tenant44保留。此为真实类调用至模拟仓储证据，没有真实事务调度/异步线程/数据库写入证据，不把mock称作实库入库验证。

前后生产源码SHA256一致：LogSanitizer BB66A18F3099DDA4190FB72B7FECABC21968844A2E611F49A9165E45ADEB54C6；BizLogAdvice 0C52BC9590885B5E67966B59375443EB546F5B9BF3AF715B3455C50A9CA7EB93。git diff --check退出0，现有LF/CRLF/JVM及Gradle弃用提示仍存在。-x jar仅避开用户运行中锁住的依赖包装，不代表完整打包或运行进程加载新类。

边界：前缀安全JSON/引号/反斜杠可能被保守隐藏；不推断Base64或无标签任意秘密；共享JsonUtil序列化异常应用日志未独立合成采集，不承诺全应用异常安全。权限P0、受限账号跨租户/数据scope、日志日期上界及运行态确认继续由总控组织；前端写锁与Edge扩展UI阻止未解除，未操作浏览器。无凭据/旧日志读取、DB/HTTP/Redis写入、服务启停、依赖/vault/Git提交推送或子代理。只追加本人QA卡；所有命令结束，后端独立读测窗口交回总控。
