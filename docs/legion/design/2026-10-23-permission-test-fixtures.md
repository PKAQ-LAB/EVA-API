# 权限测试身份、授权与回收执行包（待一次确认）

日期：2026-10-23；责任角色：technical_designer；状态：仅准备，未创建身份、组织、租户或授权。

## 1. 当前证据与执行边界

当前运行实例的 mode、租户开关、资源权限和数据权限开关没有在本轮重新只读核实。源码默认 standalone、tenant.enable=false、resourcePermission=true、dataPermission=true 不能作为当前运行证明；历史仅 eva schema 的记录也不能作为当前证明。后续先由总控确认实例、服务版本、实际加载配置与合法管理入口，再执行获准包。

包 S 是当前 standalone 环境的专用测试数据；包 T 是已有独立租户测试环境的跨租户验证，二者分别批准。不得为运行 T 把当前实例临时切换配置、重启或迁移成租户环境。缺少合法管理入口或无法确定运行模式时停止依赖步骤，不猜路由、不直接写库、不绕过权限。

本文件只读核对源码、种子清单和已有 QA 记录。没有执行 DB/HTTP 访问、服务启停、构建测试、生产代码修改、Git 暂存提交推送或秘密读取。源码行为需要后续运行验证；本文件不是权限验收通过证明。

## 2. 身份与数据清单

所有名称均为候选，执行前只读查重并记录实际 ID；禁止把下列语义标签作为数据库 ID。候选冲突时停止该项，重新列出候选供总控确认，不覆盖同名数据。

| 标签 | 候选及数量 | 用途与边界 | 实际 ID |
| --- | --- | --- | --- |
| QA_LIMIT | QA_LIMIT_20261023，1 个新账号 | 初始 D1；受限主体；无 ROLE_ADMIN、TENANT_ADMIN 或其他既有角色 | 待只读确认/创建后回读 |
| QA_PEER | QA_PEER_20261023，1 个新账号 | 初始 D1；专用对照作者；阶段性移动到 D1C、D2，最后恢复 D1 | 待只读确认/创建后回读 |
| P / D1 / D2 / D1C | QA_P_20261023 等，4 个新组织 | P 下 D1、D2；D1 下 D1C；仅专用账号任职部门 | 待只读确认/创建后回读 |
| S_LIMIT / S_PEER | QA_SAMPLE_LIMIT_20261023、QA_SAMPLE_PEER_20261023，2 条新组织样本 | 分别由对应主体经合法入口创建，用于作者/修改者与部门范围；合法父节点及必填值须先核实 | 待只读确认/创建后回读 |
| R_SELF / R_DEPT / R_TREE / R_CUSTOM / R_ALL | QA_R_SELF_20261023 等，5 个新角色 | 范围分别 0007 / 0001 / 0002 / 0003 / 0000；CUSTOM 的 orgIds 仅 D2 的真实 ID | 待只读确认/创建后回读 |
| tenantA / tenantB | 优先使用明确获准的两个既有测试租户 | 仅包 T；不是 standalone 下的虚拟 tenantId | 待只读确认 |
| QA_TENANT_B | QA_TENANT_B_20261023，1 个新账号 | 仅包 T，在 B 内创建；不得借用租户管理员充当受限身份 | 待只读确认/创建后回读 |

包 S 上限：2 账号、5 角色、4 部门组织、2 样本组织；不创建岗位、套餐、字典、凭据或第三个账号。若组织创建依赖当前主体不可见的父节点，先记录实际拒绝并停止样本创建，不因此扩大资源或数据范围；总控重新确定可执行样本入口后继续。

登录密码由用户在应用本地接管输入。新账号保存要求非空密码，服务端进行哈希；不在聊天、文档、截图、命令行、Git 或 QA 结果中传递密码、JWT、Cookie、验证码、密钥或密码哈希。部门移动不附带密码，且不附带无关岗位或角色字段。既有管理账号仅作为获准的执行者，禁止改变其授权。

保护既有 QA 数据：R01、O01、D01、M01、M02 均不复用、不改授权、不作为清理对象；其他成员的源码和会话数据保持原状。

## 3. 管理入口与变更语义

| 操作 | 当前源码入口 | 关键依赖与副作用 |
| --- | --- | --- |
| 新建/调整专用账号 | POST /sys/account/edit | UserAoeBo；先核实必填、唯一性、部门可用性及租户配额；新建明确 roleIds=[]、postId=[]，随后专门授权；非空 roleIds 会调用角色集合替换 |
| 专用账号角色替换 | POST /sys/account/grant | UserGrantBo：userId、roleIds；完整集合替换，非追加；变化递增账号 permVer |
| 新建专用角色及数据范围 | POST /sys/role/edit | 使用当前真实 BO/UI 字段，范围 code 与 orgIds 回读核对；不得复制管理员角色或既有宽资源 |
| 专用角色资源替换 | POST /sys/role/grantResource | RoleResourceRefBo：roleId、resourceId（数组，字段名为单数）；完整集合替换；变化发布资源变更事件并更新受影响用户 permVer |
| 组织创建/样本写入 | POST /sys/organization/edit | 合法父子关系与实际 PATH 回读；样本必须由实际作者登录创建，不伪造 CREATE_ID/MODIFY_ID |
| 状态收尾 | POST /sys/account/switch、/sys/role/switch、/sys/organization/switch | 先核实当前控制器和状态语义；开关可能是翻转操作，未知请求结果不得盲目重试；先回读再决定 |
| 租户创建 | POST /sys/tenant/edit | 仅包 T 的独立创建补充确认，见第 7 节；没有已核静态 UI 时不能自行调用代替 |

账号、角色、组织已有可识别前端页面；岗位、通知、租户、套餐、平台 inspect、登录日志及 OpenAPI 日志没有已核静态 UI 入口。源码存在端点不代表当前身份有调用资格。直接端点验证必须使用总控明确批准的合法测试入口/客户端和当前主体会话，不由本准备任务代为发请求。

## 4. 资源核对表与授权集合

下表 code、method、path、mainId 来自当前 Controller 与 `docs/sql/module_resource_seed_routes_20261013.json` 对照；mainId 是种子模块关联值，不是运行资源行 ID。执行前须只读核实真实资源 ID、tuple、所属模块、有效状态及当前 tenant 上限，一项不符即停止对应授权。不得按种子 id 猜运行 ID，也不运行种子脚本。

| 集合 | code | method | path | 种子 mainId | 运行资源 ID |
| --- | --- | --- | --- | --- | --- |
| O_READ | HTTP_ORGANIZATION_GET_LIST | GET | /sys/organization/list | 1000000000000000102 | 待只读确认 |
| O_READ | HTTP_ORGANIZATION_GET_GET | GET | /sys/organization/get/{id} | 1000000000000000102 | 待只读确认 |
| O_WRITE | HTTP_ORGANIZATION_POST_CHECKUNIQUE | POST | /sys/organization/checkUnique | 1000000000000000102 | 待只读确认 |
| O_WRITE | HTTP_ORGANIZATION_POST_EDIT | POST | /sys/organization/edit | 1000000000000000102 | 待只读确认 |
| L_READ | HTTP_BIZLOG_GET_LIST | GET | /monitor/log/biz/list | 1000000000000000202 | 待只读确认 |
| L_READ | HTTP_BIZLOG_GET_QUERY | GET | /monitor/log/biz/get/{id} | 1000000000000000202 | 待只读确认 |
| L_READ | HTTP_ERROR_GET_LIST | GET | /monitor/log/error/list | 1000000000000000203 | 待只读确认 |
| L_READ | HTTP_ERROR_GET_QUERY | GET | /monitor/log/error/get/{id} | 1000000000000000203 | 待只读确认 |

O_READ、O_WRITE、L_READ 仅代表上述逐条资源 ID 的集合。O_WRITE 不含删除、排序、冻结和账号/角色管理资源；初始不授予任何 method=*、/**、前缀宽授权或管理员角色。执行者原有的管理权限不能复制给受测账号。

菜单由已授资源的模块关联构建，仍须核实当前菜单 SQL/父模块有效性与运行效果；仅授上述精确 HTTP 资源后菜单若不可达，不自动加 MENU_SYS 等宽资源。记录菜单缺失后，请总控确定合法入口。按钮隐藏只证明表现；必须补直接端点拒绝验证。

账号/角色管理端点由原管理员执行，不在受测角色集合中：HTTP_USER_POST_EDIT、HTTP_USER_POST_GRANT 的 mainId 为 1000000000000000101；HTTP_ROLE_POST_SAVE、HTTP_ROLE_POST_GRANTRESOURCE 的 mainId 为 1000000000000000103。其运行资源 ID 也待只读确认，不因列入本表产生新增授权。

## 5. 包 S 的逐阶段差异与验收

执行前保存脱敏快照：运行模式/版本、执行者身份标签、每个专用对象的实际 ID/code/状态、父关系/PATH、两账号部门及完整角色 ID 集合、五角色完整资源 ID 集合和 scope/orgIds、permVer。记录前值/目标值/回读值和操作时间；不导出个人信息或认证材料。没有前值不得改既有对象。

| 阶段 | 专用对象授权前 → 后 | 验证与停止条件 |
| --- | --- | --- |
| S0 准备 | 五角色资源均 ∅；两个新账号角色 ∅ | 只读预检通过后创建上述上限内对象；查重冲突、不可用部门或未核资源立即停止；失败已创建对象记账待收尾 |
| S1 作者样本 | R_SELF：∅ → O_READ∪O_WRITE；LIMIT、PEER：∅ → {R_SELF} | 用户本地切换登录，各创建自己唯一的样本；LIMIT 在 0007 只能读取/更新自己创建或修改的样本，PEER 样本不得读取或修改；管理员不得代改样本污染 MODIFY_ID |
| S2 写拒绝 | R_SELF：O_READ∪O_WRITE → O_READ | 用原会话及重新登录验证资源撤销；编辑按钮与直接 edit 均拒绝，样本内容保持原值；资源缓存/permVer 未刷新即停，不追加清缓存绕过 |
| S3 同部门 | R_DEPT：∅ → O_READ；LIMIT：{R_SELF} → {R_DEPT}；PEER 保持 {R_SELF} | 两者 D1 时 LIMIT 可见两条作者样本；PEER 移到 D1C 后 LIMIT 的 0001 不应再见 PEER 样本；按实际当前档案核验，不写死历史部门归属 |
| S4 子部门 | R_TREE：∅ → O_READ；LIMIT：{R_DEPT} → {R_TREE} | PEER 在 D1C 时 0002 应包含其样本；再移到 D2 后不应包含；核实 PATH 正确，否则停止，不能据错误树判权限 |
| S5 自定义 | R_CUSTOM：∅ → O_READ；LIMIT：{R_TREE} → {R_CUSTOM} | orgIds 精确 {D2真实ID}；只验证两个专用作者样本对应的部门范围；0003 不是自动追加本人范围，按实际谓词核验 |
| S6 受限并集 | LIMIT：{R_CUSTOM} → {R_DEPT,R_CUSTOM} | D1 本人样本与 D2 对照样本的并集；逐条比较列表/详情，不能只比总条数 |
| S7 ALL 并集 | R_ALL：∅ → O_READ；LIMIT：{R_DEPT,R_CUSTOM} → {R_DEPT,R_ALL} | 0000 与受限角色并存按 ALL；仅验证专用样本，确认结果后立即进入撤销或下个明确阶段，不能把 ALL 视为跨租户或审计资格 |
| S8 日志读边界 | R_SELF、R_DEPT、R_TREE、R_CUSTOM：O_READ → O_READ∪L_READ；R_ALL：O_READ → O_READ∪L_READ | LIMIT 依次单角色、受限并集、含 ALL；restricted 的详情/list/count 应拒绝且不泄露数据；ALL 仅查专用主体、时间窗口内合法产生的测试日志，完成即收尾 |

S8 每次完整角色集合均取自 S3–S7 中对应目标，PEER 始终只有 R_SELF。日志查询会产生审计记录，不能承诺固定总数；分页 total/count、列表、详情必须在同一过滤边界下核验。错误日志没有合法可控样本或入口时标记未测，不手工造库记录、不制造生产故障。组织 list 是树接口，不假称其有分页 count；日志分页才补 count 泄露验证。

数据范围源码：0007 包含 CREATE_ID 或 MODIFY_ID 为当前人；0001/0002/0003 依据作者或修改者当前有效账号档案部门，0002 依组织 PATH；受限角色并集为 OR，含 0000 为 ALL。因此 PEER 的部门移动是明确的测试变更，可能改变其历史样本可见性，必须限于新专用账号并最后恢复 D1。不能把管理员创建的样本当作 LIMIT 自有样本。

0004/0005/0006 当前不受支持，不创建这些角色，不宣称这些范围通过；另列为未支持项。权限关闭、登录切换、缓存失效必须记录实况；配置开关矩阵未获服务配置变更授权，不在包 S 中临时切换。

每阶段须回读完整授权集合、permVer 和当前部门，使用用户本地接管的实际身份验证菜单/按钮、直接 list/detail/write 拒绝和分页 count。保留旧会话验证撤销，再重新登录验证新权限；不得通过复制 token 切换主体。若请求结果不明、越权写成功、范围过宽或数据变化超出清单，停止后续正向扩大授权，先收回专用授权并报告。

## 6. 包 S 回收与失败收尾

记录本次实际创建 ID 清单，只针对该清单操作。清理顺序：停止受测登录与写入；LIMIT、PEER 的完整角色集合恢复为初始 ∅；五个专用角色资源集合恢复为 ∅；回读 permVer 与资源事件后的撤销效果；PEER 部门恢复 D1；管理员按实际状态禁用两个专用账号和五个专用角色；确认无有效关联后从叶到根处理专用样本与部门组织。

默认保留已禁用的专用对象与脱敏审计证据供复核，不自动物理删除；需要逻辑删除时先记录具体对象及 API 行为，确认它是否删除关联关系且能否恢复。禁用/软删除不等于完整可逆，不能承诺 ID、关联、审计、缓存和外部事件都能事务回滚。未知结果先回读，禁止重复翻转 switch。禁止全局 Redis flush、重置管理员权限、删除业务日志或借清理重建既有对象。

阶段失败按已产生副作用收尾：S0 失败回收已创建对象；S1 创建一条成功时记录并保留/禁用该样本；S2–S8 失败优先撤回专用角色/资源集合，再回读会话撤销状态。撤销本身失败则停止其他操作并向总控报告实际残留集合，不标记“已回收”。本地登录退出由用户接管；禁止强退其他用户、改既有密码或不可恢复删除。

## 7. 包 T：独立租户环境及可恢复性

包 T 不包含在包 S 的一次批准中。优先由总控提供获准 tenantA/B 的真实 ID、模式、有效状态、合法平台/租户入口、各自测试资源上限；A 使用其自己的 QA_LIMIT/QA_PEER 和同结构专用角色，B 仅另建 QA_TENANT_B 与测试所需的专用角色/部门/样本；不能把 standalone 创建物直接挂入 tenantA。

跨租户验收须覆盖 A→B 与 B→A 的 list/page/count/detail/write、相同权限不同租户主体、伪造/不匹配 tenant 标识与会话、失败后的路由恢复；以真实服务端身份与 schema 路由为准，不信任客户端 tenant header，不用 ALL 或租户管理员作为越租户豁免。tenantB 的管理员用于受控准备，不能代替受测受限账号。

如果没有两个现成测试租户，必须另外列出创建包再确认。候选 code 仅 qaa23、qab23，待全状态唯一性检查；源码 TenantCodeRules 要求 ^[a-z][a-z0-9_]{0,5}$，不自动归一化、不复用已删除编码。实际 ID 由服务生成，严禁编造。

租户 editTenant 新建会插入租户记录、provision schema、迁移初始化、创建只读根组织与管理员/档案，并同步租户授权；本地管理员 TENANT_ADMIN 默认 0000 且受租户资源上限约束。仅填空资源集合不代表没有 schema/账号副作用。需要用户本地提供获准的必填测试资料、管理员账号/密码、配额/有效期和精确资源/套餐/角色集合；不捏造主体证件或联系方式，不记录密码。

schema 初始化、迁移、事件和缓存不保证随业务事务完整回滚。租户编码可能永久占用；禁用或软删除租户不等于删除 schema。创建部分失败先记录已生成租户/schema/账号/映射，停止自动重试；只能按获准的逐项恢复方案处理。默认收回测试授权、禁用专用账号/租户并保留 schema 与脱敏记录，不执行 DROP SCHEMA、删除平台映射或重用编码；不可逆物理删除需要单独明确批准。

## 8. 独立决策与一次确认文案

字典两根恢复/业务根新增、既有主表归属、D01 remark 返回契约仍是独立未决项，详见 `2026-10-18-dictionary-ownership.md`；本包不隐含批准数据修复。OpenAPI 推荐 A 集中管理员审计（含开关关闭仍保管理员门禁）也仍待单项确认，详见 `2026-10-23-openapi-log-boundary.md`；不因给测试身份 ALL 而批准该审计政策，不在 L_READ 中加入 OpenAPI 资源。

建议总控在真实 ID 只读核对完成后向用户呈现以下一个可审阅执行包，并附脱敏实际 ID/资源集合表：

> 是否批准仅执行包 S：在已核实的 standalone 测试实例，创建本表最多 2 个专用账号、5 个专用角色、4 个部门和 2 条组织样本；由用户在本地输入密码并切换登录；仅按 S0–S8 的精确集合替换与 PEER 部门移动测试 0007/0001/0002/0003、受限并集和短暂 ALL；完成或失败后撤回专用授权并禁用保留，记录无法回收的实际残留。不修改现有管理身份、R01/O01/D01/M01/M02，不启停/切换配置、不直接 DB 写、不物理删除，不包含包 T、新建租户/schema、字典修复或 OpenAPI A/B 决策。

本轮只提交准备文件。确认不是已经执行的证据；后续执行者仍须遵守工具对身份、授权操作和用户本地接管的实际要求，不以此文案覆盖平台门禁。

## 9. 追溯入口

源码：UserCtrl/UserService/UserAoeBo/UserGrantBo、UserRoleRefSerivce、RoleCtrl/RoleService/RoleResourceRefBo、OrganizationCtrl、ModuleService、数据权限范围处理器、TenantCtrl/TenantService、TenantCodeRules、TenantSchemaProvisioner、TenantPrivateAccountService、TenantLocalAdminService；资源清单为上述 JSON。运行 QA 边界来自 `docs/legion/status/quality_reviewer-sys-log-matrix-20261023.md` 及前端 `docs/legion/status/quality_reviewer-runtime-20261019.md`。本轮没有更改这些证据文件。

## 10. 2026-10-24 批准记录

据总控转达的用户结构化明确批准，测试包 S 已获执行批准；本记录替代前文关于 S“待确认”的历史状态，无需重复询问同一批准。批准范围严格按第 2、5、6 节及第 8 节确认文案：最多 2 个专用账号、5 个专用角色、4 个部门、2 条组织样本，按 S0–S8 精确授权集合和 PEER 部门移动完成验证及撤权、禁用保留收尾。密码输入与身份切换仍由用户在应用本地接管，不进入聊天、文件或 Git。

批准不等于预检完成：实际运行模式、开关、合法入口、候选唯一性、对象与资源真实 ID、有效资源 tuple 及前值快照仍须按 S0 核实；不以源码默认或种子 ID 替代。预检不符时停止依赖操作，沿原失败收尾边界报告，不自行扩大包。

包 S 不含包 T、新建租户/schema、修改现有管理员或其授权、物理删除、字典根/归属/D01 remark 契约修复，也不授权切换配置、服务启停或直接 DB 写；R01/O01/D01/M01/M02 保护边界保持。总控另已转达用户独立批准 OpenAPI 集中审计 A 第 7 节完整政策，批准记录见对应设计文件；该政策不扩展 S 的账号、资源或测试范围。

本次仅追加批准记录；QA 由总控另行派发 S0 预检/安全准备。本设计角色没有创建数据、授权或执行运行测试，不据批准声明验证通过。
