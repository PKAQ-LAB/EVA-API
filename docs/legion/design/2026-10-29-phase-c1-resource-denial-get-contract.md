# phaseC1 资源拒绝 GET 最小契约

2026-10-29 technical_designer。按总控派发只做源码核对与安全设计，不实现脚本、不执行HTTP/DB/测试/UI/服务。此前POST edit空对象候选停止，不纳入本方案。

## 源码成立条件

RoleCtrl的类映射为 `/sys/role`，真实get方法显式 `@GetMapping("/get/{id}")`，调用 RoleService.getRole(id)，并标QUERY BizLog。后者 `@Transactional(readOnly=true)`、`@TenantSchema`，仅selectById及convert.toVo，查无记录抛CAN_NOT_FIND_RECORD。无需构造请求参数绕过或先证明MVC写入校验。

JwtAuthFilter在chain.doFilter进入MVC前调用ResourceAuthorizationService.hasPermission；拒绝直接Response.failure(AuthCodes.RESOURCE_FORBIDDEN)经ResponseUtil.write，实际HTTP200、success=false、code0x000-00024。资源校验关闭或permit命中可绕过查询，本阶段必须核fetchMenus外层resourcePermissionEnabled恰true；不能凭角色数据不可见冒称资源门禁通过。

总控已独核C1：两账号仅SELF，当前仅组织list/get资源11010/11011，账号部门/角色/样本无漂移。此为已有证据前置，本角色不重复DB核查。角色仅用既批准包S合成SELF角色ID `2113857683657629697`，不枚举或读取其它角色。

## 固定五请求

只使用LIMIT账号 `QA_LIM_20261023`，身份ID `2114719096971497474`，己方组织样本 `2115435549295775746`。目标仍唯一 `http://localhost:9016/api`：

1. POST `/auth/login`，仅account及本机getpass后UTF8 MD5小写hex password。
2. GET `/auth/fetchMenus`。
3. GET `/sys/organization/list?id=2115435549295775746`。
4. GET `/sys/organization/get/2115435549295775746`。
5. GET `/sys/role/get/2113857683657629697`。

最多5次、单账号单轮，不加入PEER登录、跨组织GET、dict/logout/refresh或其它调用。没有前四步完整成功就不发第五步；任一步失败停止，不重试、不自动改身份/参数/认证通道。只收紧已审传输预算为5，其余connect3秒/read及wall5秒/总90秒/2MiB、固定回环校验、无proxy/redirect/Cookie fallback保持原规则。

前四步复用已审0000成功envelope、原生能力boolean、主体身份精确匹配与组织己方list/get的ID基线。菜单能力false/unknown停止为前提不满足，不将其作为资源拒绝PASS。

## 第五步精确判定及输出

仅HTTP200、JSON对象、success恰false、code精确0x000-00024且data不存在/null/空容器才记 `RESOURCE_GATE_DENIED`，阶段可标通过。不要添加4200、404、4008、无记录0x000-02001、认证过期/权限版本变更等替代成功条件。

若success=true，无论data是否为空，均失败 `RESOURCE_GATE_NOT_REJECTED`；绝不遍历、展开、打印或持久化roledata。若失败响应仍带非空data，同样失败并仅记dataNonempty布尔。未拒结果作为授权边界异常停报，具体运行配置/permit/部署等原因仍未证明，不凭此自动变更授权。

只投影固定phaseC1/步骤标签、HTTP状态、严格bool/null、已知业务码否则UNKNOWN、计数/样本匹配与dataNonempty布尔、固定结论。主体用LIM、样本SELF、角色用ASSIGNED_SELF_ROLE标签，不输出实际ID、名称、账号、角色字段、message/args、headers/Cookie/token/claims或异常文本。沿用D005版已审loginObservation，不回填旧运行结果。

最终明确：本阶段仅证明已授权组织己方读取正常及该既存未授角色GET被资源门禁拒绝；edit/del拒绝仍NOT_COVERED，SELF插件根因仍NOT_PROVEN，不把phaseC1结果并成此前未完成stage1的跨主体读取成功。

## 最小实现及合成验收建议

复用现有已审本地transport、凭据输入、响应投影方式，使用明确独立phaseC1标识及固定五调用白名单，不建立可配置端点/脚本框架。不要求全MVC测试，不实施任何业务或数据库变更；脚本实现范围与交付SHA由总控另派。

合成用例最少覆盖五步正常至00024、任一基线失败时不发roleGET、capability false/unknown停止、role返回success=true含对象或空值一律失败、00024非空data失败、无记录/过期/404/400/500/timeout/redirect不PASS、预算5及首错停、sentinel角色体/未知code/token/异常均不进入JSON/stdout/stderr。只用fake transport，不发真实HTTP。

正常登录和认证读取仍可能触发session touch/token缓存/过期token更新，QUERY可能记录审计；本方案不承诺任何缓存或审计零写入，仅无组织/角色业务写路由。不能自动logout或关闭其它session。真实运行须完成脚本独审/QA fake核验并由总控组织用户本人运行，本设计不直接授权代理请求。

backend补充源码核对：RoleResourceCacheService缓存缺失时还可能读取当前角色资源并delete/set Redis缓存，不能将资源门禁探针描述为Redis零写入。该正常缓存加载不等于角色资源业务表重建或扩权；本阶段仍不调用grant/角色成员/permVer更新。此补充为backend源码分析证据，本角色没有访问运行缓存或数据库。
