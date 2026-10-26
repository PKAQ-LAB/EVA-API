# 账号 view 已授角色：原详情接口最小增量契约

日期：2026-10-24；technical_designer；只读设计，不实现或查新库。

## 1. 可直接授权实现的结论

沿用户明确允许的必要最小读契约补充，只扩 GET `/sys/account/get/{id}` 的 UserDetailVo：新增 `assignedRoles` 摘要列表；原 `roleIds`、`postId`、账号字段和全部写入 BO/授权接口保持。无新 route、resource seed、数据库或授权变更，无需重复笼统询问用户是否允许读契约。

view 仅按该目标账号完整 roleIds 展示已授关系，并用安全摘要补名字/状态；新增编辑仍沿原角色候选流程，不借此改候选、分页或授权写入。禁止从候选前200条筛选已授角色，禁止逐ID N+1，不调用认证快照或加载目标用户模块权限来凑字段。

## 2. 字段定稿（仅新增一个字段）

| 字段 | 类型与语义 |
| --- | --- |
| roleIds | 原有 List<Long>，保持原精确目标用户关系查询结果与返回契约，不因摘要缺项删改或替换；前端沿项目既有规则无损规范成字符串ID |
| assignedRoles | 新增 List<AssignedRoleSummaryVo>，成功响应必为数组，可空；集合只含真实已授ID中当前操作者能安全读取的未删角色摘要 |
| 摘要 id | Long，必有；JSON与现有ID序列化一致，前端规范为string，不用JS Number转换长ID |
| 摘要 name / code | String，原真实角色名和编码；数据库缺值时允许null，前端显示“—”，不猜名字 |
| 摘要 frozen | Integer或现有枚举JSON值，真实0/1/9999；数据库缺值为null，显示未知，不把null默认成正常 |

不新增 assignedRolesComplete。前端从原 roleIds 规范化后的唯一ID集合 A 与 assignedRoles.id 唯一集合 B 推导：字段存在且每个摘要来自 A、没有非法ID、A=B 时关系摘要完整；A≠B 或旧服务未返回 assignedRoles 时不完整。重复关系不重复显示，不因去重删数据库关联；若原返回含非法/null关系ID，保持原响应，不将其过滤后假称完整。重复/越集合摘要属于契约异常，不能当候选角色追加展示。

不增加 dataScope、资源、菜单、权限集合、创建人或角色用户列表；摘要只用于显示关系，不是目标用户实际有效权限快照。名字/编码为空只影响文本可用性，不表示关系未分配；frozen1/9999不影响是否已授。

## 3. 状态、空与失败

| 情况 | 服务端与前端行为 |
| --- | --- |
| 原关系空 | roleIds=[]、assignedRoles=[]，view显示“未分配角色” |
| 未删且可见角色，包括frozen1/9999 | 返回真实摘要；view可显示“已冻结/只读”状态，与可授权候选规则分离，不说明其权限有效 |
| 关系指向已删/孤立/数据不可见角色 | 原roleIds保持；该摘要不返回；view保留原ID位置、名称“—”及统一“部分角色信息不可读取”提示；不区分已删/不存在/不可见以免泄露 |
| 部分摘要可见 | 只显示真实已授ID及已返回摘要，缺项同上；不得用全候选或编号猜名称 |
| assignedRoles字段未返回 | 兼容旧部署/未加载契约，显示信息未完整加载，不当空关系、不回退全候选、不发逐ID补查 |
| 目标账号不可见/不存在、身份或schema源核验失败 | 沿原统一Response失败；不返回目标关系/名字，不用目标用户身份代操作者鉴权 |
| 实际Mapper/SQL/事务异常 | 沿原请求失败路径，不把异常吞为[]；前端保留原错误/重试语义，不提示“无角色” |

历史失效关系是正常缺项，不抛“角色不可用”阻断原账号编辑详情；禁止复用写入用 ensureRolesUsable 或 ensureUserExists：前者排冻结角色且要求全集，后者拒只读账号，会误伤只读查看。读取必须允许原可见只读账号详情。新摘要缺项不覆盖 roles/roleIds 的原编辑/授权提交值，不触发任何自动清理、保存或撤权。

## 4. 服务器查询顺序与最小文件范围

在原 `UserService.getUser` 的 `@Transactional(readOnly=true)+@TenantSchema` 事务内，先完成可信来源核验、再用原非Ignore `UserMapper.selectById(id)` 确认目标账号当前操作者可见，随后仅按该已核目标ID读取真实关系；角色摘要用 **RoleMapper BaseMapper 的非Ignore参数化selectList**，只投影 id/name/code/frozen、IN去重后的真实关系ID，不加分页、不加frozen!=1。MyBatis-Plus逻辑删除和当前操作者数据权限插件保持。

关系读取沿现 `RoleUserMapper.selectRoleIds(Long)` 精确 USER_ID 查询；该类已有 @Ignore，因此只能在账号可见与来源核验之后使用，不增加Ignore join，不用它读取角色元数据。空关系直接返回[]，绝不能让空IN落成无条件角色列表。原 roleIds 与摘要必须使用同一次取得的原关系快照，不为组装新增字段重复读关系造成集合漂移；有并发变更时不伪称跨请求强一致。

可在现 `UserRoleRefSerivce`（已有 userMapper/roleUserMapper/roleMapper）增加局部只读组装方法/内部结果，避免向 UserService 另塞全套Mapper。该方法不能调用写用ensureUserExists，须遵守先来源、目标可见、精确ref、非Ignore角色的次序。UserService原账号可见校验不可被替换为Ignore；若组装方法作为可独立调用服务则本身也验证目标可见性，不能靠“上游应检查”成为旁路。实施者可复用一次可见账号结果，避免无谓重查，但不得新建全局抽象。

建议写入范围：UserDetailVo、新的小摘要VO、UserService/getUser局部组装、UserRoleRefSerivce与确需的服务接口、定向测试；前端仅account data.d.ts/service规范化与aoeform view渲染及定向测试。原UserConvert/实体可不变；MapStruct如需ignore新字段，只做新增字段对应映射声明。无理由修改Role.xml/AuthRole.xml/UserWithModuleAndRole或全局插件。

## 5. 资源与可信schema边界

请求仍由当前操作者的 JWT/资源权限按原 method/path 鉴权，保持 `/sys/account/get/{id}` 原资源覆盖；不额外要求 role/list，也不因无需该资源而绕过 SYS_ROLE 数据权限。开关遵守原通用语义，但可信身份/源校验常驻，不按目标账号角色、客户端header/schema/tenantId或前端canAdmin判断资格。

同一原事务中，账号、档案、关系、角色必须来自预期同一schema；current_schema正确仍不能排除search_path缺表回退core。standalone使用服务端配置唯一schema、可信tenant0并拒冲突TenantContext；schema模式仅可信正tenant，经现控制面resolver确认有效映射、TenantContext与实际事务来源一致。平台tenant0不借此扩成跨租户账号查看，不接收targetTenant参数，不复用TENANT_INSPECT扩新读能力。

局部源核验至少覆盖实际查询的 SYS_ACCOUNT、SYS_ACCOUNT_PROFILE、SYS_ROLEUSER_REF、SYS_ROLE 的已解析关系namespace，均须等于预期schema；若角色数据插件会读取组织表，SYS_ORGANIZATION也必须同源。原详情还返回postId，SYS_POSTUSER_REF为该原查询的同源依赖，不能在声称整个详情同源时遗漏它。固定关系名可用同事务catalog参数化解析检查，不裸SQL读取角色元数据，不拼客户端schema，不SET到第三schema找记录。

任一必要表缺失、解析落core/public/第三schema、模式混合、可信tenant/context冲突或事务不一致时整请求失败闭合，不读取fallback角色或返回成功假摘要；这类基础隔离错误与“某个历史关联无可见角色”不同，不应被吞掉以维持详情。当前RoleService已有局部standalone源核验思路可参照，但私有方法不直接跨层复用、不以那一个组织表检查代替全部新依赖。没有要求新全局框架或改全局search_path。

schema_only基础表不能假定有TENANT_ID；不新增 eq tenant0、OR NULL、全局tenant fill，也不把行NULL当共享角色。正tenant同名ID仅在自己的受信物理表读取。角色摘要更少可见仅体现当前操作者dataPlugin，不能切换成被查看者身份绕过；data关闭仅按原开关不加数据条件，身份/资源原规则/源校验仍保持。

## 6. 新增安全与契约测试

定向后端至少覆盖：目标不可见时不读ref/role；仅按精确目标ref返回摘要、空ref不查全表；>200个已授角色不截断（必要时按数据库参数限制内部批量分块，不逐角色N+1、不静默cap）；重复ref/长ID无损；冻结1/只读9999仍返回可见摘要；已删/孤立/数据隐藏缺项保原roleIds且不抛写入可用性错误；只读账号可view；摘要不得混入未授角色。RoleMapper非Ignore、逻辑删除与数据插件的实际SQL必须有证据，不能只mock任意结果就称权限测试通过。

来源测试覆盖同一事务/连接、standalone可信tenant0、正tenant A/B同ID、第三schema同名表、缺表回退core、冲突TenantContext、platform0、无身份、查询异常、finally线程恢复；旧角色历史缺项不得导致详情失败，基础隔离/SQL异常必须明确失败。原roleIds/postId、数据权限开关、method/path资源规则与get编辑契约回归保持，无账号/角色/关联/permVer写调用。

前端覆盖：双击view只有canonical roleIds对应内容，不出现未授候选；字段缺失、全空、部分缺项、冻/只读/未知状态、长ID、>200已授；不新发候选/逐ID请求以获取view摘要，不受候选page200限制；新增编辑原候选与选中/保存不回归；加载失败不显示“无角色”，退出/切换当前账号忽略迟到响应。实现与QA由总控授权后执行，本设计没有运行这些测试。

## 7. 当前交付事实

本轮只读当前UserDetailVo/getUser、关系/角色Mapper与XML、前端account view/service/type及已有租户/事务代码，未重新查PEER或任何数据库、HTTP、日志、秘密。既有PEER关系空快照不能替代多角色/隐藏/跨租户测试。只写本设计与本人status，不改源、数据、授权、QA卡/vault/服务/构建/Git，不开会话。用户已允许必要读契约，总控可直接按本字段与边界派实现，无需再次泛化确认。
