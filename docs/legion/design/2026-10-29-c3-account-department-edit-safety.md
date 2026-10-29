# C3 用户部门单字段变更的现有UI安全边界

2026-10-29 technical_designer，仅只读前端account页面/表单/service/角色表及后端UserAoeBo、UserCtrl、UserService、角色关系服务，无真实PII/密码/响应读取，无UI/HTTP/DB/测试/代码修改。

## 结论

现有正式入口可以在用户只操作部门控件后保存，但提交语义是整张用户资料重提，不能实现严格仅{id,deptId} payload。因此「只动一个控件」不自动证明其余字段完全保持；当前11个无label checkbox/1checked及保存可用不足以批准该保存。若无法无秘密地核实完整角色集合及其余字段保持，保留未保存状态并报总控/用户，不为测试增加新API、旁路请求、DB写或扩账号管理授权。

## 实际初始化及提交

account/index.openDetail清旧详情并刷新orgs/roles，异步getAccount详情归一化后传给表单。两个选项查询按当前用户scope缓存，edit每次refetch。无单独hooks.ts，也没有本表单的岗位异步查询/岗位控件。

aoeform的effect每次operateType/currentItem变化都resetFields，再填account/name/tel/email/deptId/remark及roles；roles取currentItem.roles.map(id)，缺失则[]。因此需等最终详情到齐，避免晚到详情再次reset覆盖刚选的部门；选项刷新结束本身不应给表单另填roles。service.normalizeAccount优先把完整roleIds转为roles，RoleList的受控selectedRowKeys来自Form roles且preserveSelectedRowKeys=true，分页/可见DOM的一项checked不能代表完整原集合。

handleSave发送account/name/deptId/tel/email/remark/password/repassword/roles/avatar/id/locked。service额外发送code=account、frozen由locked映射、roleIds=roles.map(id)。没有只部门分支；也没有postId字段。电话/邮箱前端required，邮箱还校验格式，不能为空时不允许凭空补造。UserAoeBo的tel/email并无对应NotBlank，UserCtrl.edit也未加@Valid；这是前端现验证边界，不应为C3更改。

密码不从详情回填，edit不是必填，保存时仅非空才MD5。浏览器自动填充若使密码非空，同样会进入改密链；新密码/确认密码必须保持空，仅确认布尔空态而不读取/记录内容。后端handleEditPasswordAndAvatar对非空password重新BCrypt，空则置null，随后updateById；本次没有执行SQL验证，不将前端空值单独当已证密码列未改。头像payload为pname或原avatar或空串，后端旧头像不同会删文件、并可能storageWithThumbnail；保持原头像、无上传/移除仍可能重走存储，不宣称纯profile零副作用。

UserService.saveUser先校验account/唯一性/部门，读取旧账号并ensureUserEditable；编辑updateById后总incrementPermVer，再saveProfile写code/name/dept。roleIds非null即进入saveRoles，按完整传入集合与数据库做差集增删；[]可能清空角色，完全相同集合则不增删关系。postId仅非null时savePosts，现表单未提供该字段，故该入口不主动清岗位。9999或系统管理员code9999后端拒编辑，前端9999也禁保存；不为本测试绕过。

## 必要稳定条件及可验证信息

详情loading=false且无error，部门和角色isFetching=false且无error，正确PEER身份/记录ID，非9999，无上传/提交，原档案已稳定回填后才考虑选部门。保存按钮源代码虽叠上述状态，但不校验edit完整roleIds元数据是否已知；按钮可用不能替代核角色集合。

可核信息只含批准测试账号标识/技术ID、当前及目标部门合成标签/技术ID、完整已选角色ID集合与原SELF一致、原岗位完整空集合（据独立PG窗口）、loading/error/dirty/上传布尔及密码两控件为空布尔。不得读取或截图手机号/邮箱/密码具体值；电话邮箱是否由原详情保持及格式有效可由用户本人确认或已有授权的仅布尔相等检查证明，不能用新值补缺，不能以1checked替代角色ID集合相等。name/account/locked/avatar等保持也须沿原详情，不自动重填其它字段。保存后由另授独立核验确认仅预期profile部门改变，角色/岗位/密码等未漂移，permVer更新属于正常编辑行为。

## D1C 选择建议与限制

TreeSelect用完整orgs树、fieldNames id/title/children、labelInValue、无treeDefaultExpandAll，保留组件默认展开/虚拟列表。源代码并未因D1C不在当前DOM就删除该节点；只凭虚拟DOM未出现不能判不存在。可在正常部门弹层先展开正确已知P祖先，再展开D1以显露D1C，然后只选择准确D1C；展开不得等同选值。目标既知技术ID2113857168370606081仍须由现场正确标签/ID确认。

没有源码证据可保证运行orgs响应必含D1C，故展开后仍没有目标应停止反馈，不用近似节点替代，也不新增/修树。此为正常控件操作建议，本角色未操作UI或读取其真实树数据。

若要求严格只部门payload，当前UI无法做到；直接调用现user/edit的{id,deptId}也不成立，因为service.validateUsername/唯一性等需要account，且saveProfile/账号更新有其它字段语义。任何专门profile更新契约或新UI实现需另行用户决策；C3当前不授权为测试修改后端契约或旁路保存。
