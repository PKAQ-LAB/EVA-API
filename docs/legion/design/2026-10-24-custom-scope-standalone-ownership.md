# CUSTOM 0003：standalone NULL/0 组织归属校验最小纠正审查

日期：2026-10-24；technical_designer；仅设计，未实现、未查新库或运行测试。

## 1. 结论与证据边界

推荐本次仅纠正 RoleService.validateDataScope 的 **standalone 分支**：在服务端确认运行模式、可信主体及当前事务实际组织表来源都是配置的唯一 standalone schema 后，仅校验本 schema 内原数据权限可见且逻辑未删的组织，允许其 TENANT_ID 为 NULL 或 0；仍要求完整提交 ID 集合全部匹配。不能只删除 tenant 条件，也不能把 OR NULL 加到所有模式。保留普通插入约定，不新增写0，不回填七条现有对象，不改全局 MetaObjectHandler、插件或路由框架。

这属于原 standalone 单 schema/普通插入 NULL 与新增局部校验强制 tenant0 之间的不一致修正，不改变 API、授权范围或数据归属，无需为此重复申请七行归属迁移批准。总控可按用户既有修 bug 授权另派 backend 最小实现；本角色不据审查取得源码锁。安全前置或定向回归无法满足时保持失败闭合，不以恢复 QA 进度为理由降低验证。

证据为 backend `2026-10-24-package-s-custom-readonly.md` 第1节：2026-10-24 UTC05:08:14、当前已确认开发 eva/eva、事务/default只读on、rollback/close、writes0。四组织及三个角色 tenant_id 均 NULL、deleted0/frozen0，三个角色无用户/资源关联；CUSTOM 候选全状态0。D2 实际 ID **2113856917966462977** 按 ID+deleted 命中，加 tenant_id=0 后零行；当前代码据 visibleIds!=orgIds 拒绝，足以构成确定的谓词冲突候选。

QA 实际仅选 D2、提交一次后抽屉未关、取消后无 CUSTOM 记录，但未取得请求体、HTTP状态或异常正文。本文件不声称已复现具体 HTTP 错误码或全部失败根因，也不声称修改已加载/保存已成功。

## 2. 当前真实 insert/read 约定

| 位置 | 当前行为 | 设计含义 |
| --- | --- | --- |
| MybatisMetaObjectHandler.insertFill | 类注释明确 standalone/schema 均不自动注入 tenantId；仅填创建者、时间、sort/frozen/deleted | NULL 是现行普通插入可能产生的值，不是本轮 SQL 人造脏值 |
| OrganizationService.edit | BO→Entity、生成ID/PATH、mapper.insert；未 setTenantId | 四个新组织 NULL 与实现一致 |
| RoleService.editUniqueCode | validateDataScope 后 convert、insertOrUpdate；未 setTenantId | 三个已建角色 NULL 与实现一致；先改校验，不能先插角色再补验 |
| UserService.ensureDepartmentUsable | organizationMapper.selectById；检查记录和锁定态，没有额外 tenant0 过滤 | 同一组织可以被原普通服务读到，与 CUSTOM 特加条件不一致；不是批准放宽账号部门验证 |
| OrganizationMapper.selectOrgMapList | 当前 schema 的 sys_organization、逻辑未删、查询条件、数据权限插件；无 tenant0 条件 | UI树可见不自动代表任意提交ID可用，仍需服务器完整集合核验 |
| TenantPrivateAccountService | 目标租户事务内创建根组织/管理员 SQL 不写 TENANT_ID | tenant schema 内物理归属不由每行 tenantId 自动填充证明 |
| tenant-migration/V1__TENANT_SCHEMA_BASE.sql | 所读组织、角色基础表未定义 TENANT_ID | 当前正租户 CUSTOM 的显式该列谓词另有潜在不兼容，不能用 standalone 兼容补丁暗改租户架构 |
| MybatisPlusConfig | 注册数据权限、分页、乐观锁，没有 TenantLineInnerInterceptor | 不可声称另一个全局行租户插件会替代本校验守住 schema 边界 |
| StdEntity.tenantId 注释 | 仍声称自动填充，与当前 handler 相反 | 注释不是运行保证；本轮不按旧注释改全局写入 |

正租户隔离现有主线为 `@Transactional + @TenantSchema` → TenantSchemaRouter → TrustedTenantSchemaResolver：仅服务端可信正 tenantId，经 SYS_TENANT 有效控制面映射解析受限前缀 schema，事务 search_path 路由并 finally 恢复。TenantProperties.isSchemaMode 是 enable && mode=schema；EvaConfig.isStandaloneMode 还接受 singleton 别名。两套开关必须共同判断，不仅看其中一项。

## 3. 最小实施边界（供 backend）

不新增通用归属框架；局部修改 RoleService 0003/非空组织集合校验和必要的局部服务端 schema 检查，复用当前事务连接查询实际来源。目标谓词为：`ID IN 全部已解析正ID AND DELETED=0 AND (TENANT_ID IS NULL OR TENANT_ID=0)`，其中 OR 必须分组，并保留原 Mapper/数据插件；不能让 OR 绕开 ID/deleted 条件。继续用 Set 完整相等判断，不接受查到任意一个就通过。

在进入上述兼容分支之前强制满足：

1. 服务端配置确为 standalone/singleton，且 tenant.enable=false、非 schema 模式；混合/未知 mode 失败闭合，不把平台tenant0当standalone。
2. 可信 ThreadUser 非空、userId>0、tenantId=0；客户端 BO/header/query 不得决定 mode/schema/tenant 或覆盖 TenantContext。
3. TenantContext 若已存在，tenantId 必须0、schemaName必须等于服务端配置 standaloneSchema；正租户或第三 schema 上下文拒绝，不清掉冲突上下文后继续。
4. 在原实际事务的同一 DataSource/Connection 上核实配置 standaloneSchema 非空合法、current_schema() 与未限定 sys_organization 的实际关系解析 namespace 均为该配置值；表缺失、解析到 public/core/第三schema、事务/连接异常或核验失败均拒绝。仅比较 TenantContext 字符串或库名不足，不能靠 search_path 同名回退。
5. 使用原参数化 Mapper 查询，不拼接客户端标识符，不 SET search_path 到其他 schema 寻找伪ID；记录只能来自本次已核 schema 中的该表，且原逻辑删除/数据可见性约束有效。

该 schema 检查是现有单源约束的局部失败闭合验证，不要求新启动策略、DB列、迁移、全局拦截器或配置切换；实现前需确认所用 JDBC 检查与 Mapper 共享同一原事务，不能另起连接读一个schema再让Mapper写另一个。若采用表OID/pg_namespace检查，静态关系名为 sys_organization，期望schema取服务端配置；不依赖客户端schema名。不能把 PostgreSQL catalog核验写成可绕过数据插件的组织数据裸SQL读取。

查询可见集合仍受 dataPermission 当前开关语义约束：开启时保原插件；关闭时仅按原开关关闭该通用数据范围，不绕过身份/schema/NULL或0/完整集合/删除条件。已有0000等角色scope及permVer更新路径保持，不让校验因数据权限关闭而省略。

## 4. 为什么不选择新建写0

只把组织新建写0不修复本次已存在的 NULL D2；仍需要 NULL 历史兼容。改全局填充还影响角色、账号、日志、租户 schema 表及所有衍生模块，既不是阻塞的最小修复，也可能写不存在的列。组织局部新建写0也会新增持久化归属约定及审计/迁移影响，现无必要；本包不实施。

不用把 D2 或七个已建对象临时回填0来让校验通过，不删除重建、不借重试换ID。当前 NULL 行仍 NULL；新增 CUSTOM 成功后其 tenant_id 沿原普通插入约定，不宣称恢复行级多租户隔离。用户以后若要求统一写0或回填历史，应另列对象范围、表/映射/SQL/tenant模式影响和回退再确认。

## 5. 正 tenant 与 schema_only 的明确边界

本次兼容只在第3节全部满足的 standalone 分支生效；platform/saas/schema 模式不进入该分支，尤其不对正tenant添加 `(tenantId=当前 OR tenantId IS NULL)` 作为跨schema访问资格。NULL从来不是“任何租户”的通配，0也不是平台任意可读标志。

本次不改变正tenant分支原有严格校验，维持其现失败闭合，不以修standalone为名删除所有tenantguard。tenant V1无 TENANT_ID 与现 positive eq 谓词的潜在问题另列未解决风险；正租户 CUSTOM 不在包 S 运行范围，也没有通过运行验收。本文件不建议长期依赖会报错的列谓词作为最终租户安全设计。

后续若需修正正tenant CUSTOM，应在单独明示范围内把归属验证改成“可信正tenant→控制面有效schema映射→同事务组织表实际namespace必须等于该tenant schema→原数据权限可见完整ID集合”，不靠 NULL/0/正数行标签判断拥有者，也不允许 search_path 缺表回退core。schema_only契约内修正无需改DB/API，但影响正tenant执行路径，须先补对应schema基础表、当前映射与路由回归证据，由总控另派；本次不自动实施。若希望租户表统一增加/填写TENANT_ID、共享表跨租户查询或NULL全局共享，则是新DDL/归属政策，必须用户精确批准，不能解释为内部bug修。

## 6. 必要回归与运行复验

| 场景 | 预期及证据要求 |
| --- | --- |
| standalone可信主体、真实唯一schema、可见D2 tenantId=NULL | 0003+仅D2可保存；回读dataOrgIds恰该ID，普通插入不改旧D2归属 |
| 同条件组织tenantId=0 | 同样可通过；NULL与0混合集合必须全部可见 |
| 同schema组织tenantId>0、deleted、未知ID、被数据权限隐藏 | 整体拒绝，角色insert/update/授权/permVer零变；不能部分成功 |
| 伪ID混入合法D2、0/负/非数字/溢出/空串、0003空组织 | 维持原输入解析与完整集合拒绝，不能因NULL兼容放过 |
| 无身份、正tenant身份冒standalone、冲突TenantContext、mode混合 | 在组织读取/角色写入前拒绝 |
| current_schema不符、同名表解析到第三schema/core、公有回退或缺表 | 拒绝；不能只mock TenantContext就称真实schema隔离通过 |
| platform/saas/schema、同名ID在A/B、NULL行在B | 不进入standalone兼容；当前失败闭合保持；若正tenant查询报无列，记录独立未解决，不吞错默认允许 |
| 受限数据范围、开关矩阵、更新遗漏scope/orgIds | 保原可见性/拒绝或原值保留，不改为ALL、不扩大组织范围 |
| 0000/1/2/7、0004/5/6、新增空scope | 原合法范围/未实现新授拒和缺省拒不回退；编辑失败事务不写版本或关联 |
| 同事务SQL/异常与线程复用 | schema核验与Mapper同连接；finally/失败后的上下文无残留，不改变其他模块路由 |

定向测试需验证实际 SQL 分组与数据插件仍生效，不仅 mock 返回visibleIds相等；schema源检查需事务/连接绑定证据。真实双租户/第三schema无需本设计角色建库，现阶段以隔离测试fixture验证且明确测试性质，未获目标环境授权不扩包T。测试/构建由获锁 backend 执行，本角色不执行。

运行加载由总控管理，不擅自重启。QA待加载门禁解除后，在原CUSTOM候选/原D2上完成一次受控提交、抽屉关闭和角色详情回读，补实际HTTP结果；重复提交前先核没有落库，避免把未知失败当允许重建。D2 tenant NULL及四组织/原三角色、关联前值保持；CUSTOM新建属原包S授权，不修改其它对象。不因静态修复通过而宣称菜单/负向权限/跨tenant矩阵通过。

## 7. 交付及排除

当前建议可在既有“修bug”授权内实施局部standalone兼容与源检查；不涉及DB/API/归属政策变更，不需为“把七行改0”申请新许可，因为本建议不做该动作。需另确认的只有未来提出的新归属/DDL/迁移或跨schema共享政策，不把它混进本次解阻。

本轮只读取现有backend附件与当前源文件，写本设计及本人status；无新数据库/日志/秘密访问、DB/HTTP/Redis/UI/服务、代码实现、构建测试、Git提交推送或新会话。原R执行权、backend只读/源码锁和QA冻结边界保持，实施与运行结果另报。
