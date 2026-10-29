# 组织资源权限前端源码独立终审

2026-10-28 technical_designer。只读复核 EVA-Web 冻结清单16文件、生产调用链、测试、现 Umi initialState 生成模型及后端实际资源匹配。已读当前 AGENTS/CLAUDE 和 AIBrain 权威前端规则，沿用 eva-frontend、antd、ponytail-review。仅写本设计；未执行测试、构建、HMR、HTTP、UI、DB或服务，未修改前端源码、工具或 Git 状态。

## 结论

源码安全性与最小性审查通过，未发现本轮必须修复的可复现阻塞，可交总控组织验收。范围为组织页资源权限与已批准能力字段接线；动态 HTTP、真实 UI 和跨作者会话交互仍由对应验收判定。本轮未授权发布或改业务源码。

清单 `.git/organization-resource-permission-source-20261028.json` 实算 SHA256 为 `86BEE556C42E2E1948CE272FA267591239683B64FC3469579299816A25257BBF`，16份文件逐项实算均匹配。patch 实算 `535CAFF403967598CC4EE15C11C7B0C6B1E6B11834A052B3C75C0C2C6436A7A2` 与交接一致。工作区存在既有 CubeLoading/PageLoading 等变更；app 的 Cube 接线及对应测试为既有范围，本审没有将其计作本轮权限新增或要求回退。现场安装 antd6.6.3、ProComponents3.1.14-7。

## 权威能力与初始化

`resourcePermissionFromResponse` 只接收成功响应外层的原生 boolean；显式 success=false 覆盖 status=ok，缺失/null/字符串/无成功标记均返回 undefined。app 与登录归一化均先展开身份字段，再显式覆盖能力字段，缓存身份和 data/user_info 内 false 无法回填。

登录先获取 token，随后实际 fetchMenus 响应覆盖 login 的能力；菜单或字典初始化抛错时不提交 currentUser，不以 login 的 false 兜底。app 的每次 fetchUserInfo 重取 fetchMenus，保留原 menus 数组，不改变数据契约。登录/菜单 service 仅增加响应类型，不改 URL、method、headers、参数或载荷。

实读现 `src/.umi/plugin-initialState/@@initialState.ts`：refresh 开始保留 initialState 并设置 loading=true，失败仍保留旧 initialState、置 error。组织页因此单独叠加 `!loading && !error`，其稳定 canRequest 回调经 permissionRef 读取最新状态，旧 false 在刷新中/错误后不能放行。未修改自动生成模型，也未全站改造初始状态流程。

`access.canAdmin` 原行为保留，新的 canRequest 只调用资源判定，不 OR admin/用户名。没有 currentUser 时拒绝；显式 false 仅跳过资源集合校验，组织业务状态、树结构与服务端认证/授权仍在原链路。

## 匹配与业务入口

共享纯函数按资源 resourceType/resourceUrl 判定，递归菜单 children，处理坏对象/坏数组及环；不使用 routeUrl、code、name 推导授权。真实请求路径仅剥一次 `/api` 边界，去 query/hash；拒绝相近前缀、绝对 URL、反斜杠、百分号编码及点路径段。路径大小写不折叠。

源码覆盖项目要求的段内 `*`/`?`、独立段 `**` 和命名 `{id}`；自定义正则变量拒绝。与后端 RoleResourceCacheService 实际代码对照，额外下级兼容只用于 method=*、非根且无未知模式的字面路径，并保持路径段边界及移除一个尾斜杠。没有使用宽泛 includes/前缀来授权兄弟资源。该实现是当前项目支持子集；不宣称对任意 Spring Ant 模式完全等价。

已逐项追踪组织页实际 service：GET list、按真实 id 的 GET get、POST edit/checkUnique/del/sort。页面无 switch 状态操作入口，未为未使用 service 额外扩权。所有生产 OrgAOEForm 调用已传 canRequest，已有独立表单测试相应补明确权限 fixture。

列表同时设 query enabled、queryFn 发出前/返回后守卫，数据展示也受 canList 控制；refresh 入口再次校验。查看与编辑读取按真实行 id 复核。新增/添加下级/编辑与保存同时要求 edit/checkUnique；get-only 保留查看，edit 另需该行 get。删除与排序独立判定，不因 edit 允许而隐式授予其它写入。

旧 modal.confirm 的 onOk 与 handler 均通过稳定回调读取最新权限；页面卸载 mounted=false 后旧确认框不能发写请求。行删和批删读取 live 树数据，批删确认目标固定且要求与当前选择集合相同，避免确认后换目标；父子删除阻塞仍执行。排序以最新树及搜索状态计算同级相邻目标，9999/status 保护保留。资源关闭也未绕过这些业务限制。

表单用 permissionRef/contextRef 同时读取最新许可、模式、详情状态及记录只读属性；唯一校验请求前/返回后均复核。validateFields await 后、editOrg 调用前再次校验最新权限及 leaveGuard 会话身份；late get 回来后若 get 已撤销，canSave 仍拒绝。原 leaveGuard 对切换/卸载作会话隔离，dirty内容及失败处理保留。已发出请求仍由服务端作最终裁决。

## 最小性与验证依据

生产范围为一份可跨 access/app/login 复用的纯 helper、access、两处初始化归一化、两个响应类型与组织页/表单守卫；没有新依赖、全局请求拦截器、后端配置猜测、APIS改写或跨模块权限迁移。新增 ref 分别解决旧确认/await 与最新业务树数据问题，未增加通用权限框架。组织行按钮由链接换为 Button 与本轮权限条件就近组合，既有查看/表单布局未重构。

已阅读新增匹配/登录/组织权限测试及既有测试改动，覆盖 true/false/unknown、菜单权威覆盖、缓存false拒绝、错误/加载旧false、旧confirm撤权与卸载、unique await撤权、迟到详情撤get、批删换目标、9999/树阻塞和正常可授权写入。已有 view/readonly/recordView fixture 仅补所需显式权限，未删断言来换取全绿。

已读取实现方旧日志：首轮8文件122项为119通过/3失败，后五文件73项通过。总控说明另三文件53项与后五文件去重共126通过；此为分批结果，不表述为一次126项全绿。本角色不执行测试，QA正在获授独立槽对冻结完整8文件一次重测；最终动态结论以QA该次结果及后续真实验收记录为准，不把源码通过扩展成UI/HTTP已通过。

## QA 收口更新

总控同步 QA 最新独立完整单轮结果：8文件126/126通过、exit0，session12626；执行前后16份源码 SHA 及清单86BEE556C42E2E1948CE272FA267591239683B64FC3469579299816A25257BBF均无差。日志为 `runtime-20261028-resumed/qa-resource-capability-vitest-20261028.log`，报告 SHA256 `CDF4726F70DA9287E4C66767C64BCB4C277A9F8A17D79807BD018DD028B2DD78`，summary及system-view卡已保存。

据此，前述「等待QA完整八文件重测」已关闭；当前为源码独审通过且QA独立单轮126项通过。此项更新依据总控转达的QA执行证据，本角色没有再次运行测试或源审。实际HTTP/UI待后台标准产物核对及用户重启后验，继续保持该验收边界。
