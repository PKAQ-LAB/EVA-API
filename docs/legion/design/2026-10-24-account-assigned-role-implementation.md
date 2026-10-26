# account 已授角色详情实现与独立QA交接

2026-10-24；backend_developer；cornerstone。依据总控正式唯一源码锁、用户明确允许的最小读契约及设计2026-10-24-account-assigned-role-detail-contract.md实施。不是实际页面/HTTP验收或S/T包完成报告。

## 1. 最终实现与不变边界

原GET /sys/account/get/{id}的UserDetailVo仅新增assignedRoles数组；元素仅id(Long)、name/code(String|null)、frozen(Integer 0/1/9999|null)。ID沿现JSON Long→String序列化。无assignedRolesComplete、权限/资源/菜单/审计角色字段，没有新route/seed/DB/授权。原roleIds保持单次真实关系快照，postId保持原查询/列表语义；写BO、saveUser/saveProfile、saveRoles/savePosts、permVer链未改。

UserService.getUser在原readOnly+TenantSchema事务中调用已有UserRoleRefSerivce.readUserRoles。后者MANDATORY、自己核可信身份/模式/来源，使用原非Ignore UserMapper.selectById确认目标账号当前操作者可见（允许只读账号），再精确读一次RoleUserMapper.selectRoleIds(userId)。原列表不清理、不删除、不过滤；仅取其中合法正ID去重用于摘要IN，空集合不查RoleMapper，不退全表或候选前200，不逐角色N+1。

摘要经root/designer窄审批准，使用同一个非Ignore RoleMapper BaseMapper.selectMaps，投影id/name/code/frozen并IN真实ID，保MP逻辑删除及当前operator数据插件，不加冻结排除、不查认证快照/缓存。不能用RoleEntity setter映射，因为它会给历史code补前缀/转大写且默认空串会丢NULL；raw Map映射保原mixedcase/code/NULL，缺键映射null，Long精确十进制转换。未知不可表示状态是数据错误，不默认正常或伪造摘要。

已删/孤立/数据隐藏只缺摘要，原roleIds仍保留，不阻断正常历史缺项的详情，不自动清理或撤权。冻结1/只读9999的未删且可见角色照实摘要。只有实际身份/来源/SQL等错误沿失败路径，不catch转空；前端按原关系ID与摘要集合推导缺项，不将缺摘要当未分配。

## 2. 同一事务、身份与实际来源

standalone/singleton要求tenant关闭、可信operator userId>0/tenant0，若TenantContext任一存在则必须完整匹配。schema模式仅已知saas/platform主模式+tenant schema启用+可信正tenant，拒platform0/混合模式/冲突或部分上下文。

schema正tenant复用CoreSchemaExecutor与既有TrustedTenantSchemaResolver核有效控制面映射；在该局部callback内先核actual current_schema与未限定SYS_TENANT实际namespace确为配置core（同事务连接、固定relation参数），拒临时/第三schema遮蔽和缺表。执行器finally恢复原search_path。不修改全局resolver/router，不据此声称其它旧路径的全局风险已修复。

返回数据前，同一原事务连接固定catalog VALUES参数化一次核六表：SYS_ACCOUNT、SYS_ACCOUNT_PROFILE、SYS_ROLEUSER_REF、SYS_ROLE、SYS_ORGANIZATION、SYS_POSTUSER_REF。current_schema和每个未限定关系实际namespace都须等于可信预期schema，表名集合/行数也核全。组织是数据插件潜在子查询依赖，岗位关系是原postId依赖；都未遗漏。缺表/core/public/第三schema fallback或核验异常整请求拒绝。数据开关关闭只沿原数据范围语义，不关闭身份/来源。无客户端tenant/schema输入，无正tenant行级OR NULL/eq tenant0或DDL。

保原UserPostRefSerivce调用会在已核来源后再进TenantSchema嵌套路由，root/designer另核准仅当前getUser用已有postUserMapper执行完全相同select(postId).eq(userId,id)，避免二次resolver/set_config。保List、不去重、不排序、不造空值；空列表、重复与正常输出原语义保持，原无效null实体的异常不改为空列表。岗位service的其它方法/写链及全局路由不变。

RoleUserMapper、PostUserMapper原有Ignore以及data插件内部关系表排除没有新增或扩大；它们只在可信来源及目标账号非Ignore可读校验之后，按该同一target ID读取内部关系。角色元数据用的RoleMapper.selectMaps没有Ignore。原method/path资源鉴权仍在JWT过滤器构造可信operator后执行，不拿目标用户身份替代、不要求新role/list权限，也不加admin/ALL/permit/宽grant。

## 3. 变更文件与影响范围

生产仅：sys-domain UserDetailVo、AssignedRoleSummaryVo(新)；sys-service UserService/getUser、UserRoleRefSerivce局部读组装与来源核验、UserConvert仅新增assignedRoles映射ignore。UserConvert本来已有dirty冻结映射，其完整HEAD差异不能全归本次。

测试仅：UserAccountProfileServiceTest适配详情读/原岗位Mapper夹具；新增AssignedRoleDetailJdbcFixture、AssignedRoleDetailTest。没有改RoleMapper/Role.xml/AuthRole.xml、全局插件、UserAoeBo、保存链、依赖/配置/前端或数据库。

GitNexus getUser upstream显示UserCtrl.get单一直接调用、LOW，但索引落后21 commits，不据此认定完整无风险；当前文字检索与原详情测试补调用边界，没有重建索引。

## 4. 隔离验证方法与实际SQL

真实UserService/UserRoleRefSerivce、生成UserConvertImpl、原User.xml/UserRoleRef.xml、RoleMapper.selectMaps、PostUserMapper、MybatisPlusConfig实际数据插件以及Spring只读事务；JDBC/行数据全为合成替身，不连接实际PG。每次Statement/PreparedStatement断言同一个DataSource事务ConnectionHolder、同一Connection及readOnly事务；任何非SELECT SQL直接失败并计写尝试，账号创建/档案持久化mock无交互。

JSqlParser解析实际Mapper/插件完成后的SQL WHERE，用真实JDBC绑定参数筛合成账号/ref/role/post行；没有mock visibleIds或手写SQL替代Mapper。原用户SELECT为LEFT JOIN档案实际XML，角色raw SQL/Map为真实BaseMapper。未知谓词测试直接失败，不能被替身默认放行。XML的ASSIGNED_SYNTHETIC_BOUND_SQL记录实际四列与参数，例如：

```sql
SELECT id, name, code, frozen FROM sys_role WHERE deleted = 0 AND (id IN (?, ?, ?, ?)) AND ((sys_role.CREATE_ID = 7 OR sys_role.MODIFY_ID = 7)); PARAMETERS={1=777, 2=12, 3=13, 4=14}
```

42项新增测试覆盖：refs0/1/>200(207唯一+重复)/非法与NULL原关系保持/19位ID字符串、raw NULL与未前缀mixedcase code、隐藏/删除/孤立、冻结/只读角色和只读账号、空ref不查全表、不可见账号不读关系、数据开关、身份/mode/context、六表逐表core/第三/缺失fallback、同ID tenant A/B、正tenant权威控制表来源遮蔽/缺失、SQL异常与恢复、零写。

实际外层TenantSchemaTransactionalAspect+TenantSchemaRouter与Spring事务顺序也有成功/拒绝用例：从core有效映射→tenant A→局部core复核→恢复tenant A→原上下文/路径恢复；确认所有目标查询同源。另一fixture提供tenant内同名SYS_TENANT可错误映到B、A/B同ID和不同岗位输出，若详情重新调用岗位service会触发真实二次切面；最终实测shadow lookup=0、只权威lookup、post在A且无二次set_config。此测试只证明当前详情路径防回退，不是全局resolver整改或真实双租户数据库验收。

## 5. 当前产物、编译与快照

所有命令cwd=C:/home/workspace/lab/EVA-API。offline/no-daemon/-x jar，不覆盖用户实例占用JAR。最终init为.gradle/assigned-role-current-20261024.gradle，复制当前编译输入到独立目录，测试classpath前置新的snapshot-d。每个Test依赖十个相关模块全部classes/resources完成，防并行模块重编时清单短暂变化。

此前试跑保留而不计通过：首轮JDBC夹具缺静态Statement/复用mock回滚次数，已修夹具；snapshot-c联测全hash门禁在其它模块重编时拦下清单变化（结束后当前与快照均494，无永久额外文件）。最终先显式完整编译十模块，再制新d并将所有Test依赖全部main产物，未放松清单/哈希校验。a/b/c历史快照不覆盖、不作为最终证据。

```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' -I .gradle/assigned-role-current-20261024.gradle :eva-core:eva-core-contract:classes :eva-core:eva-core-common:classes :eva-core:eva-core-cache:classes :eva-core:eva-core-log:classes :eva-core:eva-core-data-mybatis:classes :eva-web:eva-web-core:classes :eva-web:eva-web-auth:classes :eva-core:eva-core-upload:classes :eva-web:eva-web-sys:eva-web-sys-domain:classes :eva-web:eva-web-sys:eva-web-sys-service:classes --offline --no-daemon --console=plain -x jar
& ./.gradle/assigned-role-snapshot-20261024.ps1
```
最终编译日志.gradle/assigned-role-compile-d-20261024.log：BUILD SUCCESSFUL 2m51s。新snapshot-d全量467类+27资源=494文件；每个最终测试前核全部原输出/副本及清单，测试结束后又全量核相同，27源资源逐字节一致。manifest SHA256 D87ED7CCFD71317A2A1909451D0425F4721A4B7636BF6CC51A798A02CD349EED。


关键最终class SHA256：

| 目标 | SHA256 |
| --- | --- |
| UserService.class | 50DEC874622E461BF1F35AC5BD079403392F1008C57340FE08E828D3A4CB9690 |
| UserRoleRefSerivce.class | 88A7A9C09763D16C00F98EF47F54EC8EC8218877379F8E1F86934E6DFA2B5FE5 |
| UserDetailVo.class | BD00D9D0EB5BBA103BF8A2306D77848DCD445314CA02B4E947131FD69CA46203 |
| AssignedRoleSummaryVo.class | F36F36F07C3985210A659403BC5942348642C1C8029C00A80F77CCCA4C6B0117 |

## 6. 最终测试命令及XML

```powershell
& 'C:/home/DevApp/gradle/bin/gradle.bat' -I .gradle/assigned-role-current-20261024.gradle :eva-web:eva-web-sys:eva-web-sys-service:test --tests org.pkaq.sys.role.service.AssignedRoleDetailTest --tests org.pkaq.sys.user.service.UserAccountProfileServiceTest --tests org.pkaq.sys.role.service.RoleCustomScopeStandaloneTest --tests org.pkaq.sys.role.service.RoleServiceTest --tests org.pkaq.sys.RoleFrozenMappingTest --tests org.pkaq.sys.AccountTenantFrozenMappingTest --tests org.pkaq.sys.TreeFrozenMappingTest --tests org.pkaq.sys.TreeFrozenEditTest --tests org.pkaq.sys.WriteProtectionServiceTest :eva-web:eva-web-auth:test --tests 'org.pkaq.core.auth.adapter.mybatis.audit.service.OpenApiAudit*Test' --tests org.pkaq.core.auth.authorization.service.ResourceAuthorizationServiceTest --tests org.pkaq.core.auth.authentication.filter.JwtAuthFilterTest --tests org.pkaq.core.auth.config.WebSecurityCapabilitiesTest :eva-core:eva-core-data-mybatis:test --tests org.pkaq.config.OpenApiAuditQueryPermissionTest --tests org.pkaq.config.MybatisPlusDataPermissionHandlerTest --tests org.pkaq.config.LogQueryPermissionInterceptorTest --offline --no-daemon --console=plain -x jar
```
日志.gradle/assigned-role-regression-d-20261024.log：BUILD SUCCESSFUL 2m32s。最终18类177项、failures/errors/skipped均0，均为本轮该命令生成XML，不把旧119或中间试跑重复相加。SYS9类126、AUTH6类37、DATA3类14。

| 类 | 本轮数量 |
| --- | ---: |
| AccountTenantFrozenMappingTest | 4 |
| AssignedRoleDetailTest | 42 |
| RoleCustomScopeStandaloneTest | 48 |
| RoleServiceTest | 14 |
| RoleFrozenMappingTest | 2 |
| TreeFrozenEditTest | 2 |
| TreeFrozenMappingTest | 2 |
| UserAccountProfileServiceTest | 8 |
| WriteProtectionServiceTest | 4 |
| OpenApiAuditCoreRoutingTest | 6 |
| OpenApiAuditMapperSqlTest | 1 |
| OpenApiAuditReadAccessTest | 22 |
| JwtAuthFilterTest | 2 |
| ResourceAuthorizationServiceTest | 2 |
| WebSecurityCapabilitiesTest | 4 |
| LogQueryPermissionInterceptorTest | 2 |
| MybatisPlusDataPermissionHandlerTest | 8 |
| OpenApiAuditQueryPermissionTest | 4 |


三个模块原XML为build/test-results/test/TEST-*.xml；最终副本.gradle/assigned-role-final-xml-20261024/{eva-web-sys-service,eva-web-auth,eva-core-data-mybatis}/。汇总与前后hash校验结果.gradle/assigned-role-final-results-20261024.json。init/snapshot/log/XML均忽略的本机证据，不提交发布。原账号档案/部门/岗位、编辑/冻结/permVer与CUSTOM、OpenAPI、JWT/资源/Data日志门禁定向回归保持；不是全仓所有测试。

## 7. 交锁与加载门禁

本轮新UserRoleRefSerivce/UserService class文件时间为2026-10-24北京时间17:07:25/26；本成员未启停/热加载服务，未核验运行实例已经返回assignedRoles。此前已知PID49000 14:48是历史元数据，不代替本次加载证明。需独立QA读本局部diff、494文件清单/哈希、42项安全测试及原关系Mapper Ignore边界；再由总控管理相关模块完整产物加载，不能混单class进旧包或把磁盘编译当HTTP成功。前端已收到定稿字段但运行门禁未解除。

所有编译/测试/命令结束后交回源码锁，等待独立QA；本成员不创建S账号/角色/样本、不授予、不回填、不读实际PG/HTTP/Redis/秘密日志或进程环境，本轮实际S/R数据与备份保持。无前端/vault/QA卡/依赖/配置/提交推送/PR，后续页面与S/T验收由总控派发，不声明all done。
