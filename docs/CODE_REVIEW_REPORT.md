# EVA-API 代码审查报告

> 审查范围：树形模块（模块管理、组织/部门管理）、主子表模块（用户、角色、租户）、字典管理多级支持。
> 严重程度：🔴 P0 阻断/数据错乱 · 🟠 P1 功能缺失/逻辑错乱 · 🟡 P2 设计缺陷/规范偏差

---

## 一、树形模块审查

### 1.1 通用基类问题（`eva-core-data-mybatis`）

| ID | 级别 | 位置 | 问题 |
|---|---|---|---|
| T-01 | 🟠 | [StdTreeEntity.java:17-48](../eva-core/eva-core-data-mybatis/src/main/java/org/pkaq/core/mybatis/mvc/entity/StdTreeEntity.java) | 仅有 `pid/path/isleaf`，**完全没有 `pathId/pathName/parentName`**，与需求"维护 pathid + pathname"不符。同包另一个 [TreeEntity.java:46-50](../eva-core/eva-core-data-mybatis/src/main/java/org/pkaq/core/mybatis/mvc/entity/TreeEntity.java) 有完整字段但**未被任何业务实体继承**，两套基类并存。 |
| T-02 | 🔴 | [StdTreeService.java:87-88](../eva-core/eva-core-data-mybatis/src/main/java/org/pkaq/core/mybatis/mvc/service/StdTreeService.java) | `entity.getPath() + "/" + entity.getId()` —— 拼接的是**自身 path**，根本没用上 `parent.getPath()`，path 计算完全错。 |
| T-03 | 🔴 | StdTreeService.java:101 | `getParentById(orgId)` 实际是按 id 查"父节点"，但传入的是节点自身 id；Mapper 方法名误导，xml 行为未定义。 |
| T-04 | 🟠 | StdTreeService.java:139 | 调用 `mapper.updateChildParentName(name, id)`，**与 ModuleMapper 的 6 参签名完全不匹配**，调用链断裂；编译期靠泛型擦除蒙混，运行期 MyBatis 解析将报错。 |
| T-05 | 🟡 | StdTreeService.java:118 | `setSort(selectCount(...))` —— sort 字段是 `double`（StdEntity:43），把行数当排序值，多人并发新增会重复。 |
| T-06 | 🔴 | StdTreeMapper.java:32 | 接口里 `updateChildParentName(String name, Long id)` 只接受两个参数，但同名方法在 ModuleMapper.java:100 是 6 参；同一抽象在不同子接口语义错位，**只要走基类调用就会失败**。 |

### 1.2 模块管理（`sys/module`）

| ID | 级别 | 位置 | 问题 |
|---|---|---|---|
| M-01 | 🔴 | [ModuleService.java:101-103](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/module/service/ModuleService.java) | **新增模块完全不设置 `path`**。`isNew` 分支直接 `insert(module)` 就结束，没有像编辑分支那样调用 `handleParentChange` 设置 path。新模块入库后 `path=null`，后续 `likeRight(path, ...)` 级联操作全部失效。 |
| M-02 | 🔴 | ModuleService.java:240-258 `handleFrozenStatus` | `likeRight(path, originModule.getPath())` —— 根节点 `path` 是 `null`（见 L304-307 `module.setPath(null)`）；MyBatis-Plus `likeRight(null)` 行为不确定，可能**全表更新 frozen**。 |
| M-03 | 🔴 | ModuleService.java:338-348 `switchFrozen` | 多处错误同时存在：<br>① `selectOne(eq(ModuleEntity::getPid, id))` —— 把 id 当父节点查，逻辑完全错。<br>② `parent.getPid() != null` —— 若 selectOne 返回 null 直接 **NPE**。<br>③ for 循环里每次都 `mapper.switchFrozen(ids)` 传整批，N 个 id 触发 N 次全量切换。<br>④ XML SQL（Module.xml:24）`path like concat('%', #{id}, '/%')` 用 `%` 前缀，**id=12 会误命中 path=120/15**，要锚定为以 id 起始或以 `/id` 分隔。 |
| M-04 | 🔴 | ModuleService.java:295 | `String.format("%s/%s", newParent.getPath(), moduleId)` —— 新父节点 path 若为 null（根节点的直接子节点），结果是字符串 `"null/123"` 写入库。 |
| M-05 | 🟠 | ModuleService.java:283-290 | 仅当 `!isRoot` 才把新父节点 isleaf=false，但**新增子节点时（M-01 分支）**没维护父 isleaf；新父若原本是叶子，依然显示 isleaf=true。 |
| M-06 | 🟠 | ModuleService.java:267-308 | `handleParentChange` 仅在父节点变化时更新 path；**重命名节点（name 变化）时不更新任何 pathName**（题目明确要求维护 pathname），且 StdTreeEntity 根本没这个字段。 |
| M-07 | 🔴 | [Module.xml:30-44](../eva-web/eva-web-sys/eva-web-sys-service/src/main/resources/mapper/Module.xml) `updateSort` | **WHERE 条件没有 pid 限定**！`sort BETWEEN ... OR id = #{id}` 会把全表 sort 在区间内的所有节点同时增减——**跨父节点污染**。题目明确要求"只允许同级之间拖拽排序"，必须加 `AND pid = (SELECT pid FROM ...) `。 |
| M-08 | 🟠 | ModuleService.java:55-84 `deleteModule` | 题目要求"删除时检查引用表（暂无此设计）"。当前**未做任何业务引用校验**，并直接 `roleResourceMapper.deleteByModuleIds(ids)` 静默连带删除授权关系，违反"被引用不允许删除"。建议引入"是否被外部业务引用"的扩展点（接口 + SPI）。 |
| M-09 | 🟠 | ModuleService.java:317-331 `handleResources` | 每次 edit 都用 `batchId` 标记，再 `delete ne batchId`。问题：<br>① 编辑时**未传 resources 则 batchId 不写入**，最后的 `ne batch_id` 会把已有资源全删，反而和注释中"存在权限引用，不可使用先删除再写入方案"的初衷相反。<br>② `batchId` 是 `long` 但 ne 时仍要序列化比较，并发 edit 期间存在中间态被读取的窗口。 |
| M-10 | 🟡 | ModuleService.java:194-209 `checkUnique` | `if (pid != null && pid != 0)` 与下一段 `if (pid == null && pid == 0)` 两个条件**重叠且都做 `ne id`**；现写法导致根节点新增校验缺少 `ne id`，编辑时校验逻辑分支重复。 |
| M-11 | 🟡 | [ModuleCtrl.java:53](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/module/ctrl/ModuleCtrl.java) | `@BizLog description="插叙了模块信息"` —— 错别字（应为"查询"），且 `operateType=EDIT` 与"查询"语义不符。 |
| M-12 | 🟡 | ModuleService.java:295 | `;;` 多余分号。 |

### 1.3 组织/部门管理（`sys/organization`）

| ID | 级别 | 位置 | 问题 |
|---|---|---|---|
| O-01 | 🔴 | [OrganizationService.java:69-123](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/organization/service/OrganizationService.java) | **editOrg 整段被注释，方法体空**——前端调用编辑后什么都不会发生。 |
| O-02 | 🔴 | OrganizationService.java:126-131 | `refreshChild` 整体注释，无任何效果。 |
| O-03 | 🔴 | OrganizationService.java:169-174 | `sortOrg` 整体注释。题目要求"同级拖拽排序"完全未实现。 |
| O-04 | 🔴 | OrganizationService.java:181-184 | `switchStatus`（题目所述"冻结"）整体注释，**冻结/级联冻结根本未实现**。 |
| O-05 | 🟠 | OrganizationService.java:47-62 `deleteOrg` | 仅检查子节点。**部门-用户关系未校验**——UserEntity 有 deptId，删除部门时不阻止；与题目"被引用不允许删除"严重不符。 |
| O-06 | 🟠 | [OrganizationEntity.java:36](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/organization/entity/OrganizationEntity.java) | `pid` 是 `String`，而 StdEntity.id 是 `Long`，类型不一致；OrganizationMapper 多处方法签名（listChildren/getParentById/countPrantLeaf 等）也是 String，与 module 流派不统一。 |
| O-07 | 🟠 | OrganizationEntity 缺少 `pathId/pathName/isleaf`（继承自 StdEntity 而非 StdTreeEntity，且 StdTreeEntity 自己也没这两个字段），节点重命名/移动时无法维护需求所要求的 pathname 链。 |
| O-08 | 🟠 | [Organization.xml:73-76](../eva-web/eva-web-sys/eva-web-sys-service/src/main/resources/mapper/Organization.xml) `updateChildPathInfo` | `REPLACE(PATH, oldPath, newPath)` 且匹配 `path like concat('%', oldPath, '%')`——若 oldPath 出现在路径任意位置都会被替换；当 oldPath 是 `/1` 之类短前缀时，会**污染所有包含 "1" 的兄弟节点路径**。需改成锚定前缀 + 长度替换。 |

---

## 二、主子表模块审查

### 2.1 用户管理（`sys/user`）

| ID | 级别 | 位置 | 问题 |
|---|---|---|---|
| U-01 | 🔴 | [UserService.java:232-237](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/user/service/UserService.java) | **保存岗位时条件写错为 roleIds**：<br>`if (CollUtils.isNotEmpty(user.getRoleIds())) { ... userPostRefSerivce.savePosts(postBo); }`<br>导致：①只传角色不传岗位 → 仍然走 savePosts；②只传岗位不传角色 → 永远不保存岗位。 |
| U-02 | 🟠 | UserService.java:234 | `postBo.setPostIds(user.getPostId())` —— **方法名是单数 `getPostId`**，应核对 `UserAoeBo` 字段是 `postId`（单值）还是 `postIds`（列表），否则只能绑定一个岗位。 |
| U-03 | 🟠 | UserService.java:91-97 `delete` | 删除用户时只清理 RoleUserEntity，**未清理 PostUserEntity**（岗位关系残留）。 |
| U-04 | 🟠 | UserService.java:265-274 `checkUnique` | account 和 code OR 校验在 nested 内，编辑时 `ne(id)` 在 nested 外——意图正确；但对**冻结/逻辑删除**的用户未排除，可能误判已删除用户冲突。 |
| U-05 | 🟡 | UserService.java:107 | `orderByDesc(UserEntity::getModifyBy)` —— 按"修改人姓名"排序，应为 `getUtcModify`。 |
| U-06 | 🟡 | UserService.java:53-55 | `ILLEGAL_USERNAMES` 含 "admin"、"root"，但**系统超管账号本身就需要 admin 角色**；该黑名单可能阻止首次初始化。 |
| U-07 | 🟡 | UserService.java:188-195 | 用户数量限制只检查"单例模式"分支；多租户模式下 `availableCounts` 不校验，与 `TenantEntity.authUserCount` 字段冲突，套餐限制形同虚设。 |

### 2.2 角色管理（`sys/role`）

| ID | 级别 | 位置 | 问题 |
|---|---|---|---|
| R-01 | 🔴 | [RoleEntity.java:31-37](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/role/entity/RoleEntity.java) | `getCode()` 是 **getter 中带副作用** 改写字段：<br>`this.code = CommonConstant.AUTH_PREFIX + this.code;`<br>① MapStruct/Lombok 生成的序列化、JSON 输出每次都触发；<br>② MyBatis 反射读取 fieldName 不一定走 getter，导致库里和返回值可能不一致；<br>③ 与 [RoleService.java:88-90](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/role/service/RoleService.java) `isUnique` 中"添加前缀"再走持久化时**双重添加**风险。建议改为 `@PrePersist` 等价钩子或上层规范化一次。 |
| R-02 | 🟠 | RoleService.java:69-74 | `QueryWrapper queryWrapper = new QueryWrapper<>();` —— 原始 QueryWrapper 未带泛型，且**复用同一个 wrapper 给两个不同表**（RoleUserEntity、RoleResourceEntity），列名靠 `"role_id"` 字符串，逻辑能通但容易踩混表字段。 |
| R-03 | 🟠 | RoleService.java:120-125 | `module.setResources(v)` 前未判空，若 `moduleMap.get(k)` 返回 null（资源映射了一个已被删除/过滤的模块）→ **NPE**。 |
| R-04 | 🟠 | RoleService.java:141-162 `grantResource` | 先 delete 再 insert，未做 batchId 模式；并发授权时存在"中间无权限"窗口。 |
| R-05 | 🟡 | RoleService.java:199-226 `grantUser` | 同 R-04，先 delete 再 insert 的窗口期；另：`affectedUsers` 在 delete 之前收集，**未包含被解除授权又重新授权的用户**（已包含，但删除之后被取消授权的用户因为已采集进 affectedUsers，会被自增 permVer——这是对的；问题反而是"原本无权限新加的用户"由于已在 union 中加入也会自增，OK）。可以接受，但建议增加注释。 |
| R-06 | 🟡 | RoleService.java:200-209 | `grantUser` **没有 @Transactional**！跨多张表写入，事务边界缺失。 |

### 2.3 租户管理（`sys/tenant`）

| ID | 级别 | 位置 | 问题 |
|---|---|---|---|
| T-T01 | 🔴 | [TenantService.java:76-86](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/tenant/service/TenantService.java) `delete` | **判空条件写反**：<br>`if (CollUtils.isNotEmpty(ids)) { CommonCodes.PARAM_ERROR.newException(); }`<br>正常传 ids 反而抛异常 → **租户删除接口完全不可用**。 |
| T-T02 | 🔴 | TenantService.java:52-58 `switchFrozen` | `updateWrapper.in("id", ids)` —— `ids` 是 `SingleArray<Long>` 对象，**未取 `.getParam()`**，MyBatis-Plus 不会展开为 IN 列表，结果是 SQL `WHERE id IN (SingleArray@xxx)` 编译失败。 |
| T-T03 | 🔴 | TenantService.java:104-114 | `adminId = IdWorker.getId()` 后 `entity.setAdminId(adminId)` 入库；但创建用户时 `new UserEntity()` 并未 `user.setId(adminId)`——**user 自动生成的 id ≠ tenant.adminId**，关联失效。 |
| T-T04 | 🟠 | TenantService.java:126-134 `reGrantUser` | 减少授权时直接锁定"超出数量"的用户，**没有规则**（按创建时间？按权重？任意？）；增加时解锁"超出锁定的"也无优先级，业务行为不确定。 |
| T-T05 | 🟠 | TenantService.java:60-68 | 切换冻结后用 `frozen == 1` 判断"是否冻结后再批量同步用户"，但 `setSql("frozen = abs(frozen - 1)")` 是按原值翻转，**传入的 `frozen` 参数与实际写入结果不一定一致**（如原本已经是冻结，翻转后变非冻结），导致 frozenUser/unfronzenUser 错位。 |
| T-T06 | 🟠 | [TenantEntity.java:74](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/tenant/entity/TenantEntity.java) | `expirationDate` 是 `java.util.Date`，[TenantAoeBo.java:68](../eva-web/eva-web-sys/eva-web-sys-domain/src/main/java/org/pkaq/sys/tenant/bo/TenantAoeBo.java) 是 `LocalDateTime`，MapStruct 自动转换会丢时区。 |
| T-T07 | 🟠 | 全局 | **"租户套餐"完全缺失**：当前 TenantEntity 只有 `authUserCount + expirationDate`。题目询问的"套餐包：用户数 + 到期时间 + 授权模块" → 建议新增 `SYS_TENANT_PACKAGE` 表：`id/name/maxUser/durationDays/moduleIds`，并将 `tenantId` 关联到 package；`TenantRoleEntity` 当前已建表但 Service 完全未使用，可改造为 `SYS_TENANT_PACKAGE_RESOURCE` 中间表。 |
| T-T08 | 🟡 | TenantService.java:117-118 | `entity.setCode(null); entity.setAdminId(null);` 防止修改，但 MapStruct 转换后字段已被 BO 值覆盖，置 null 后再 `updateById` 依赖 MP 默认"null 不更新"策略——**与全局策略相关**，可能因配置变化导致 admin 被清空。 |
| T-T09 | 🟡 | [TenantRoleEntity.java:24,27](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/tenant/entity/TenantRoleEntity.java) | `@NotBlank` 用于 `Long` 字段——**JSR-303 中 @NotBlank 只能用在 CharSequence**，对 Long 校验不生效；应为 `@NotNull`。 |

---

## 三、字典管理审查（`sys/dict`）

### 3.1 多级支持

| ID | 级别 | 位置 | 问题 |
|---|---|---|---|
| D-01 | 🟠 | [DictEntity.java:34,39](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/dict/entity/DictEntity.java) | 字段已经有 `pid` 和 `path`，**但 Service 完全没有读写**——edit 不维护 pid/path；list/get 不组装树。需求所谓"只支持一级"其实是"代码里没用"。 |
| D-02 | 🔴 | [DictService.java:117-131](../eva-web/eva-web-sys/eva-web-sys-service/src/main/java/org/pkaq/sys/dict/service/DictService.java) `delDict` | **顺序错乱**：先 `delete child/main`，再 `selectById(id)`——已被删，永远返回 null，所以 `dictCacheHelper.remove(code)` **永远不执行**，缓存不会清理。 |
| D-03 | 🔴 | DictService.java:142-189 `edit` | 子表"先删后插"：<br>① 题目业务侧"已被引用的字典项"会因 id 变化导致脏数据；<br>② 该 Service 没用 batchId 模式（与 module 不一致）；<br>③ `conditionEntity` 来自 `new LambdaQueryWrapper<>().eq(...).getEntity()`——`getEntity()` 仅取查询包装器里的 entity 模板，**不是查询数据库**，永远是 null 或刚 set 的对象，校验逻辑完全失效。 |
| D-04 | 🟠 | DictService.java:145-146 | 校验 code 唯一性的实现错（D-03 已述）。即便修好，也未排除自身 id，编辑时会误报已存在。 |
| D-05 | 🟡 | DictService.java:198-201 `checkUnique` | `new QueryWrapper<>(entity)` 把 BO 转换后的 entity 全部字段作为查询条件，若 BO 传了 name 也会一起过滤，单元逻辑不正确。 |
| D-06 | 🟡 | DictService.java:65-75 `selectDict` | 用 `DictViewEntity` (`V_DICT` 视图) 又用 `DictItemEntity`，**双表 + 视图三处真相源**，维护成本高。 |

### 3.2 单表 vs 双表设计建议

**建议方案：合并为单表多级字典**

```
sys_dict（单表）
├─ id            字典节点 id
├─ pid           父节点（NULL 为根分类）
├─ path          /1/12/123
├─ path_name     /系统配置/状态码/启用
├─ code          节点 code（分类的 code 或字典项的 dCode）
├─ name / value  显示名 / 字典值（叶子节点）
├─ is_leaf       是否字典项（true=叶子）
├─ sort/frozen/...
```

理由：
- 当前 `DictEntity + DictItemEntity` 本质就是"分类 + 项"的两级，硬拆双表只覆盖 1 级，**多级分类**（如"行政区划/省/市/县"）无法表达
- 单表 + pid 自然支持 N 级，复用现有 `StdTreeEntity` 基类（前提是先补齐 pathId/pathName 字段）
- 缓存结构需要从 `Map<code, LinkedHashMap<dCode, dValue>>` 升级为按 path 索引的 `Map<pathCode, List<Item>>`，或允许按"任意一级 code"取下属所有叶子
- 视图 `V_DICT` 可保留为兼容旧的双表 API（过渡期），后续删除
- `@Dict` 注解的解码端（JacksonCodeSerializer）也需要相应支持多级 path 查找

---

## 四、跨模块共性问题

| ID | 级别 | 范围 | 问题 |
|---|---|---|---|
| C-01 | 🟠 | StdTreeEntity vs TreeEntity | 两个树形基类并存且字段集不同，业务实体（ModuleEntity）只继承前者 → **pathId/pathName 永远没地方存**。建议统一只保留一个，补齐字段。 |
| C-02 | 🟠 | 所有"先删后插"的中间表写入（dict items / role users / role resources / post users） | 都存在**事务窗口期** + 触发的级联更新（如 user permVer）次数偏多。建议统一采用 module 已用的 `batchId` 模式或 diff 模式（计算新增、删除、保留三集合）。 |
| C-03 | 🟠 | 所有"被引用不允许删除"场景 | 仅 module 做了模糊处理（连带删除）；organization / dict / role 等**无引用方校验或硬连带删除**。建议引入 `ReferenceChecker` SPI：每个被引用方注册检查器，删除前聚合调用。 |
| C-04 | 🟡 | 所有 `BizLog` 注解 | 多处 `operateType=EDIT` 但描述是"查询/删除"，BizLog 类型与实际行为不一致，会污染审计日志。 |
| C-05 | 🟡 | sys_module XML SQL 中的 path LIKE 匹配 | 大量使用 `concat('%', id, '/%')` 这种**未锚定前缀**的模式，存在 id=12 命中 path=120 的风险。所有 path 查询应统一为 `path LIKE concat(prefix, '/%') OR path = prefix`。 |

---

## 五、优先修复建议（按收益排序）

1. **P0 立刻修**（影响功能可用性）
   - 🔴 T-T01 `tenant.delete` 判空反向
   - 🔴 T-T02 `tenant.switchFrozen` SingleArray 未展开
   - 🔴 T-T03 租户管理员 id 不一致
   - 🔴 U-01 `user.saveUser` 岗位条件用错字段
   - 🔴 D-02 `dict.delDict` 删除后才查 code
   - 🔴 D-03 `dict.edit` 用 `getEntity()` 错误做唯一性校验
   - 🔴 M-01 `module.editModule` 新增不设 path
   - 🔴 M-03 `module.switchFrozen` 多重错误
   - 🔴 M-07 `module.updateSort` 跨父节点污染
   - 🔴 O-01~O-04 organization 四个核心方法全注释
   - 🔴 T-02/T-03/T-04 StdTreeService 基类 path/Mapper 调用错乱

2. **P1 计划修**（设计缺陷）
   - 🟠 引入 pathId/pathName 字段并补齐维护逻辑
   - 🟠 引入"引用校验 SPI"，模块/部门/字典统一接入
   - 🟠 RoleEntity.getCode 副作用 → 改为入口规范化
   - 🟠 grantUser/grantResource 加 @Transactional + 改 diff 模式
   - 🟠 字典合并为单表多级（含 `@Dict` 解码端升级）

3. **P2 顺手修**
   - 🟡 `@NotBlank` 用在 Long 字段
   - 🟡 BizLog operateType 与描述匹配
   - 🟡 path LIKE 锚定前缀
   - 🟡 expirationDate Date↔LocalDateTime 类型统一

---

## 六、租户套餐设计建议（题目询问）

建议新增独立表，与 tenant 解耦：

```sql
CREATE TABLE SYS_TENANT_PACKAGE (
    id              BIGINT PRIMARY KEY,
    name            VARCHAR(40)  COMMENT '套餐名称',
    code            VARCHAR(40)  COMMENT '套餐编码',
    max_user        INT          COMMENT '最大用户数',
    duration_days   INT          COMMENT '默认有效天数',
    price           DECIMAL(10,2),
    remark          VARCHAR(255),
    -- StdEntity 基础字段...
);

CREATE TABLE SYS_TENANT_PACKAGE_RESOURCE (
    package_id  BIGINT,
    module_id   BIGINT,
    PRIMARY KEY (package_id, module_id)
);

ALTER TABLE sys_tenant ADD COLUMN package_id BIGINT COMMENT '套餐ID';
ALTER TABLE sys_tenant ADD COLUMN package_start LocalDateTime COMMENT '套餐生效时间';
-- 保留 auth_user_count + expiration_date 作为"实际生效值"（套餐快照），允许单租户特殊调整
```

实施要点：
- 创建 / 续费租户时从 package 复制配置到 tenant 的快照字段（避免修改套餐影响已售出的租户）
- 模块授权改为：登录时取 `tenant.package_id → SYS_TENANT_PACKAGE_RESOURCE → module`，与 `RoleResource` 求交集
- `TenantRoleEntity` 表当前未使用，可考虑改造或删除（替换为 `TenantPackageModule`）
