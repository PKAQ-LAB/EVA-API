# 组织页按钮资源权限最小契约

2026-10-28 technical_designer。只读后端匹配、前端菜单/初始状态/access/组织入口与表单/service、权威前端规则；无源修改/HMR、测试执行、DB/网络/UI操作。

## 权限依据与未公开能力边界

组织页面当前只有status/tree/9999等业务保护，未消费资源权限。已授权菜单resources携resourceType/resourceUrl，前端初始状态保留menus；最小修复为共享纯method/path判定函数、access新增可调用入口，接线仅组织index和aoeform，不迁移全站或改全局请求。

app及登录归一化有access缺失补admin；现access.ts只canAdmin，此值不能当可靠管理身份或写权限。后端ResourceAuthorizationService关闭resourcePermission或permit匹配时放行，否则按role资源；没有在已读该服务中发现admin名称的无条件绕过。管理员宽授权只能由实际服务器resources（如* /sys/organization/**）匹配体现，禁止前端canAdmin/用户名/authority字符串自行OR放行。

已读LoginUserInfoVo仅身份/authorities/capabilities，菜单为元数据，前端auth service只有login/logout，未找到对本页面公开的可信resourcePermission enable/permit runtime配置。不能从服务器yaml、浏览器构建env、session/cookie或canAdmin推断开关。当前可以实现“已成功加载的当前菜单正授权允许，未知/未加载/损坏拒绝”的保守UI；不能宣称它在服务器资源校验关闭而菜单无授权项时仍完全等价。如果必须前端同步体现该关闭/permit绕过，需要明确公开的服务端有效权限契约，属新增API/策略边界须报总控/用户，不能擅添API或自造默认。不是本轮普通按钮修复的必要扩展。

## method/path匹配

后端实际链为JwtAuthFilter.resolveRequestPath（servletPath优先，否则剥真实context边界）→ResourceAuthorizationService→RoleResourceCacheService.matchesResource/matchesPath。资源method归一为大写，*通配；路径先Spring AntPathMatcher。随后仅method=*且pattern非/时，后端还有移除单个尾斜杠后pattern+斜杠的子路径兼容规则。这个额外规则必须显式镜像，不能改成所有method任意前缀，也不能声称后端只有Ant。

前端匹配输入为实际API请求method及pathname，不把资源code/routeUrl当HTTP授权。固定本项目/api只在请求路径等于/api或以/api/边界开始时剥一次，/apix不剥、/api/api/...不反复剥；资源模式按后端无context路径解释，不给资源和请求双方循环猜前缀。query/hash不参与pathname，空路径/绝对外域URL/非法编码或无法确定语义的模式拒绝，不擅自大小写折叠路径、decode通配或归一..路径扩大授权。

共享pure matcher按路径段实现或采用已有确证兼容库，不加新依赖便宜但错误的includes/startsWith替代。至少精确路径、?、段内*、跨段**、{id}及项目已有模式必须有与Spring语义一致的边界；regex变量若实现需按服务端实际模式与测试向量审，未支持或非法模式默认拒且不声称完全兼容。仅后端明确的method=*分支可使用严格path-segment子路径规则；/sys/organization不匹配/sys/organizations，根/不自动放整站。

## 组织实际请求与最小条件

| 动作 | 实际请求与必要授权 |
|---|---|
| 列表/搜索刷新 | GET /sys/organization/list，React Query enabled及queryFn实际入口均核 |
| 查看/编辑详情加载 | GET /sys/organization/get/{实际id}，按真实行ID匹配，不能只拿/get固定前缀 |
| 新增/添加下级/保存 | POST /sys/organization/edit；字段唯一性校验另POST /sys/organization/checkUnique |
| 编辑打开 | 该行GET详情允许且POST edit允许，叠原业务保护 |
| 行删/批删 | POST /sys/organization/del，叠原9999及父子阻塞等 |
| 排序 | POST /sys/organization/sort，叠原同级相邻/过滤态/status/9999约束 |
| 冻结切换（若页面实际呈现） | POST /sys/organization/switch，不因允许edit代替独立switch授权 |

新增与更新共用同POST edit，现接口/资源契约不能凭按钮名称拆成create与update两种授权，也不能一刀禁止编辑。checkUnique无授权时不得由form validator偷发请求；是否禁用带校验的保存，应按当前校验必需闭环保持，不静默跳过唯一性检查。仅get/list的受限用户仍可只读查看已授权行，删除/编辑/排序各自独立，9999保护继续叠加而非资源允许就移除。

## UI与延迟执行保护

同一个权限快照用于渲染和操作条件，未知/加载/失败均拒。未授权行删除与批删入口隐藏或禁用，checkbox不因无delete就阻止纯浏览；若选择只服务批删可在无delete时移除选择能力。已有选中项/确认框在权限变化后不能残留可执行写入。实际handler再次核当前权限和目标集合，不只包按钮Access。

modal.confirm的onOk、异步validateFields/checkUnique返回后、editOrg/delete/sort调用前均用最新权限谓词（ref或当前快照getter），避免确认/表单闭包持旧允许值。权限撤回/initialState更换/未加载时，晚到callback不能发请求；请求已发出不宣称前端可撤销，仍由服务器认证/资源/数据范围作最终裁决。详情响应回来后不能因旧模式复活写入口，dirty内容不自动提交/清空。view仍纯只读字段与原布局，保存失败保内容。

最小允许源码范围候选：共享资源纯函数及focused tests、access.ts/其必要类型、组织index.tsx与aoeform.tsx及focused tests；只有发现菜单归一确有资源丢失才改其对应最小位置，不改app登录默认admin以免全站影响，不改APIS/service请求契约。实际写锁由总控在QA备注恢复结束后授，当前不得HMR。

## 有意义的验证

纯匹配测试包括method大小写/*、精确/get/{id}、段内*/**/?、相近前缀拒、method=*特定子路径兼容、root不宽放、/api只剥一次、缺/坏resources、同模块兄弟资源不隐式授权。组织交互测试分get/list-only、edit+get、delete-only及wide grant，断言无授权不会触发实际service；已打开确认后权限撤回、validate await后撤回、批删目标变化、9999/树阻塞均无写请求，编辑允许者仍正常保存，不以所有按钮禁用作为通过。

校验disabled-runtime/管理员策略只能在有真实可信契约时测试，不能用本地造admin覆盖未授权用例。既有fulltsc/其它测试失败与本修分开报告；静态审不替代真实按钮与服务端SELF/权限拒绝。新API/运行时开关公开或其它模块接入均另报范围，不夹入本次组织修复。
