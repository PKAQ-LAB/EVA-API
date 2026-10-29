# C2/C3 六请求可见性探针最小契约

2026-10-29 technical_designer。根据总控派发，仅设计固定只读API契约，不实现/运行脚本、HTTP、DB、UI或服务。原D005/AF5及历史交接冻结；当前C2独立PG核验由backend另行完成，不由客户端代证。

## 主体、预期与范围

仅LIMIT账号QA_LIM_20261023，身份ID2114719096971497474，两个既批准组织样本L=2115435549295775746、P=2115579583561605122。两样本白名单不可由argv/env/响应改变，不使用管理员或PEER登录，不新增样本/角色/资源授权，不调用任何业务写路由。

用户真实运行必须显式 `--expect-peer visible` 或 `--expect-peer hidden`，只允许这两个枚举，缺值/其它值/额外参数立即安全拒绝且不索密码、不发请求。默认无参仍为NOT_RUN。可保留既有--self-test入口，仅fake。该枚举是用户声明的测试预期，不选择或修改服务端角色/数据范围，也不能凭枚举给测试贴已核真实scope标签。

当前C2计划使用visible：总控说明DEPT角色具有组织两GET，LIMIT关联改为DEPT，PEER仍SELF，两账号部门D1及两样本未变，backend独立PG核中。须等该窗口确认再组织真实运行。后续C3只有PEER作者档案D1→D1C的批准动作及独立PG证据完成，才由总控组织用户用hidden；脚本不实施、不自动检测或推断这些状态变更。

## 固定六请求

目标仍仅http://localhost:9016/api，固定顺序：POST auth/login；GET auth/fetchMenus；GET sys/organization/list?id=L；GET sys/organization/list?id=P；GET sys/organization/get/L；GET sys/organization/get/P。总计最多6次，严格索引顺序、任一不满足即停止，无重试/redirect/proxy/Cookie fallback/refresh/logout或附加探测。

沿既审D005/AF5传输及输入：用户本人TTY getpass无echo、UTF8 MD5小写hex、body仅account/password；身份精确匹配、token仅本次login body内存Bearer、固定UA、peer回环核验后发HTTP；connect3秒/read与wall5秒/总90秒/2MiB。参数或错误不回显自由文本，正常登录/auth/cache/audit可能维护会话，不称服务器零副作用。

login严格已审HTTP200/true/code0000及能力原生boolean；菜单严格HTTP200/true/code0000/data数组且外层resourcePermissionEnabled恰true。缺失/false/字符串/数值、认证失效、权限版本变更或未知错误均停报，不换认证通道或自动重新登录。

## 判定矩阵

两种预期下L的精确list及get都必须成功可见。visible下P的list/get也必须成功可见。可见list须HTTP200/原生success=true/code0000/data数组，按既审树ID解析结果必须恰好单个目标ID，无多余节点/错ID；get同成功envelope，data须对象且id精确等于该目标。19位ID按字符串精确比较，不转浮点。坏结构、成功空值或错误ID不当可见PASS。

hidden下P的list仅接受HTTP200/原生success=true/code0000/data严格空数组；P的get仅接受HTTP200/原生success=false/code精确0x000-02001、data缺省/null/空容器。不能互相替代：list业务失败或get成功null/{}均失败；get的资源拒绝00024、404/403/400、过期或其它失败不当数据不可见PASS。

任何预期不可见步骤返回非空data立即失败，且不展开打印内容。角色/作者/部门含义不从响应字段或空结果推断；本轮精确list筛选使无关祖先/其它样本返回也失败。步骤可以暂记观察，只有本轮六请求全部匹配、L的两个正向基线和P的两个预期结果均成立才标阶段成功；中途失败不能把先前空list提升成scope已证明。

## 输出与复用

独立协议/phase标签（如scope-read-v1）、subject=LIM、expectPeer枚举、步骤list/get及sample=L/P、HTTP状态、原生success/null、原SAFEcode或UNKNOWN、count/idMatches布尔/visible布尔、固定结果枚举及requestCount<=6。沿D005安全loginObservation，不输出实际账号/ID、组织字段、作者/部门、token/claims、message/args、headers/Cookie、响应body或异常文本。

最终只表述API结果符合/不符合显式预期，`scopeRootCause=NOT_PROVEN_BY_API`；resource/edit拒绝未覆盖，不拼接为旧stage1/C1结果。客户端只验证响应，真实角色/授权/作者部门来自带时间和阶段的独立PG核验窗口，脚本不读取数据库或运行配置以自证。

最小复用建议：既审hash锁导入JSON解析、envelope、loginObservation/token格式及正向样本树校验；隐藏list用一个紧邻分支核严格[]，隐藏get复用既审精确noRecord分支。固定六项ORDER及一个expectPeer枚举足够，不扩成通用步骤DSL、可配置端点或自动scope切换框架。旧工具不改globals/白名单，不增加密码来源。新工具是否复用既审传输由backend按最小差异实现，交最终SHA独审。

合成用例至少覆盖visible两样本均可见、hidden仅P两项不可见、L任一失败不能整体PASS、P仅list隐藏/get暴露及反向不匹配、精确ID/额外节点拒绝、get成功空/资源拒绝/过期/未知业务码/状态不替代、菜单能力off/unknown停止、六请求顺序预算/首错停、参数枚举拒绝及秘密sentinel不进入输出。仅fake，无需额外MVC框架或业务写操作。

该契约本身不授实现/真实运行，等待总控在C2事实核验和设计确认后另派。后续不得因预期不符自行更改作者档案、角色或权限，也不自动要求用户重试。

## C2 独立事实窗口更新

backend报告C2只读PG独核已通过：2026-10-29北京时间20:01:56.458227至20:01:56.925615，同REPEATABLE READ、事务/默认readonly均on，rollback/close后数据窗释放、writes0。DEPT完整两GET资源及scope0001、SELF相同两GET及scope0007；LIMIT仅DEPT、PEER仅SELF，两账号作者profile仍同D1、posts空、两组织样本关键字段无漂移。permVer实测LIMIT3/PEER6，不从登录次数推断。

当前C2事实前置据此关闭待核，API可见结果仍未运行。backend进一步源核0001按样本CREATE_ID/MODIFY_ID对应作者profile部门作过滤，并非组织样本pid；预期两样本可见仍需本轮实际六请求验证。本角色仅记录上述backend独核证据，没有重复DB或等价SQL。请求顺序继续保持本契约的login/menu/listL/listP/getL/getP，不因其它候选顺序改变冻结契约；等待总控另授实现。
