# 包 S：PEER部门与编辑候选只读诊断

2026-10-24，backend_developer；执行cwd始终C:/home/workspace/lab/EVA-API。本轮只读，不修复或保存数据、授权、源码、构建测试。

## 1. 结论与实际只读数据

PEER指定accountID2113894345662021633，SYS_ACCOUNT_PROFILE共享ACCOUNT_ID同值，DEPT_ID实际为预期D1 **2113856614139469826**，不是NULL。账号tenant0/frozen0/deleted0；完整用户角色、岗位关联按USER_ID全状态查询均空，无意外管理员或非空关联。D1存在且tenant NULL/frozen0/deleted0；五个S角色均frozen0/deleted0，原scope0007/0001/0002/0003/0000保持，CUSTOM仍仅D2。

只证明持久化技术现状，不证明某次HTTP响应、Form store或候选树内容。不得把列表部门—、编辑无combobox等同数据库未保存，现无回填理由；库中角色正常也不等于某请求可见。未查任意管理员/其他用户权限、密码/哈希、手机号/邮箱/姓名/IP/Token或用户请求日志。

首次历史PID39300前置因进程退出而停止，未建立DB连接。重新只读确认9016为PID49000/java，2026-10-24北京时间14:48:04启动，本成员未启停。approved开发连接取当前build/resources/main/application-dev.yaml，与源内容一致，内部核新PID已有PG端点匹配，不输出地址/URL/凭据/命令行/环境。沿用户已确认开发库，实际driver eva/eva，transaction/default只读on/on，末尾复核on/on后rollback/close。实际UTC07:32:59.926753=北京时间15:32:59。

以下只参数化指定账号ID、其完整关联、预期D1和五S角色；没有SELECT *账号。technicalColumnPresence仅为指定相关表必要技术列存在性：档案无独立id/tenant/frozen/deleted，角色关系无id/tenant/frozen/deleted，岗位关系有id/tenant但无frozen/deleted。不得伪造不存在状态列为0。本次两关系未加任何状态过滤，完整集合确为空，不是只查询有效子集。

```json
{
  "observedAt": "2026-10-24T07:32:59.926753+00:00",
  "pid": 49000,
  "accountId": "2113894345662021633",
  "knownLabel": "QA_PEER_20261023",
  "writes": 0,
  "fingerprint": {
    "database": "eva",
    "schema": "eva",
    "transactionReadOnly": "on",
    "defaultTransactionReadOnly": "on"
  },
  "technicalColumnPresence": {
    "sys_account": {
      "id": true,
      "tenant_id": true,
      "frozen": true,
      "deleted": true
    },
    "sys_account_profile": {
      "id": false,
      "account_id": true,
      "dept_id": true,
      "tenant_id": false,
      "frozen": false,
      "deleted": false
    },
    "sys_roleuser_ref": {
      "id": false,
      "user_id": true,
      "role_id": true,
      "tenant_id": false,
      "frozen": false,
      "deleted": false
    },
    "sys_postuser_ref": {
      "id": true,
      "user_id": true,
      "post_id": true,
      "tenant_id": true,
      "frozen": false,
      "deleted": false
    },
    "sys_role": {
      "id": true,
      "code": true,
      "data_scope": true,
      "data_org_ids": true,
      "tenant_id": true,
      "frozen": true,
      "deleted": true
    },
    "sys_post": {
      "id": true,
      "code": true,
      "tenant_id": true,
      "frozen": true,
      "deleted": true
    },
    "sys_organization": {
      "id": true,
      "code": true,
      "pid": true,
      "path": true,
      "tenant_id": true,
      "frozen": true,
      "deleted": true
    }
  },
  "account": [
    {
      "id": "2113894345662021633",
      "tenant_id": "0",
      "frozen": 0,
      "deleted": 0
    }
  ],
  "profile": [
    {
      "account_id": "2113894345662021633",
      "dept_id": "2113856614139469826"
    }
  ],
  "completeUserRolesAllStates": [],
  "completeUserPostsAllStates": [],
  "unexpectedAssociationsPresent": false,
  "referencedRoleStates": [],
  "referencedPostStates": [],
  "expectedD1": [
    {
      "id": "2113856614139469826",
      "code": "QA_D1_20261023",
      "pid": "2113856261050376193",
      "path": "/2113856261050376193/2113856614139469826",
      "tenant_id": null,
      "frozen": 0,
      "deleted": 0
    }
  ],
  "fiveSRoleStates": [
    {
      "id": "2113857683657629697",
      "code": "ROLE_QA_R_SELF_20261023",
      "data_scope": "0007",
      "data_org_ids": null,
      "tenant_id": null,
      "frozen": 0,
      "deleted": 0
    },
    {
      "id": "2113858118523068418",
      "code": "ROLE_QA_R_DEPT_20261023",
      "data_scope": "0001",
      "data_org_ids": null,
      "tenant_id": null,
      "frozen": 0,
      "deleted": 0
    },
    {
      "id": "2113858458634985474",
      "code": "ROLE_QA_R_TREE_20261023",
      "data_scope": "0002",
      "data_org_ids": null,
      "tenant_id": null,
      "frozen": 0,
      "deleted": 0
    },
    {
      "id": "2113870080799694849",
      "code": "ROLE_QA_R_CUSTOM_20261023",
      "data_scope": "0003",
      "data_org_ids": "2113856917966462977",
      "tenant_id": null,
      "frozen": 0,
      "deleted": 0
    },
    {
      "id": "2113870419179364353",
      "code": "ROLE_QA_R_ALL_20261023",
      "data_scope": "0000",
      "data_org_ids": null,
      "tenant_id": null,
      "frozen": 0,
      "deleted": 0
    }
  ],
  "finalReadOnlySettings": [
    "on",
    "on"
  ],
  "rolledBack": true,
  "closed": true
}
```

## 2. 部门写读链与字段语义（源码及当前字节码）

- UserAoeBo.java:58 唯一部门参数Long deptId；roleIds为List<Long>，postId为List<Long>，不是orgId/departmentId。
- UserCtrl POST /sys/account/edit @RequestBody→UserService.saveUser（272行）。先ensureDepartmentUsable(user.deptId)，null/0允许，非空经原OrganizationMapper读与冻结校验；没有CUSTOM特加tenant0条件。
- UserConvert.boToEntity→UserEntity.deptId，字段exist=false因为它不存账号主表；不能由该注解断言部门丢失。新增createAccount获ID后saveProfile；编辑账号更新须恰1行，后续档案/关联在原事务。
- UserService.java:551 saveProfile设置ACCOUNT_ID=user.id及DEPT_ID=user.deptId，以共享主键insert或update，影响行须1否则抛错回滚。AccountProfileEntity以ACCOUNT_ID输入主键，DEPT_ID更新策略ALWAYS；后续编辑若真的提交null可清空部门，不能只修显示而忽略Form提交保护。本轮没有编辑或读取此前真实payload。
- mapper/User.xml:20 LEFT JOIN profile P ON P.ACCOUNT_ID=U.ID并投影P.DEPT_ID，BaseResultMap第8行DEPT_ID→deptId；自定义selectById第41行与selectManagedPage第50行均用该投影。
- getUser（252行）entityToDetailVo后再查roleIds/postId；UserDetailVo.java:51、UserListVo.java:54都是deptId，两VO均无deptName。ID持久化与名称显示是不同字段，列表只读deptName可显示—，不是部门丢失证明。
- 生成UserConvertImpl含bo.getDeptId→entity.setDeptId及entity.getDeptId→Detail/ListVo.setDeptId；当前javap均确认。RoleUserRef.xml按USER_ID返回ROLE_ID，UserPostRefSerivce按USER_ID取POST_ID，没有关系逻辑删列；当前实际完整两集合均空。
- JacksonConfiguration给Jackson2和Boot4 Jackson3都注册Long转字符串；Java Long字段的响应源契约为String ID，前端不应Number化19位ID。本轮不声称读到了实际响应体。

## 3. 合法角色候选与组织树（source，无HTTP或权限扫描）

RoleCtrl GET /sys/role/list→RoleService.listPage(RoleQueryBo)，后者只有name/code，继承pageNo/pageSize，**没有scope参数**或“编辑该账号则只返已选角色”的分支。PageBo默认1/30，pageSize声明@Max(100)，当前RoleCtrl参数没有@Valid，不能仅据注解断言pageSize200实际必然400。本轮不测接口或改分页契约。

原RoleMapper分页/逻辑删/数据插件保留，按可选name/code模糊条件及修改时间排序，经Convert→PageResult.map为PageVo.records/total/current/size，Response.data保留该分页对象；CommonResponseAdvice不改为list或数组。没有scope可跳过权限。未找到字面roleslist端点，不把组件名当另一路由。

前端成员独立回报的当前源码事实：APIS.ROLE_LIST=/api/sys/role/list，queryRoles用normalizePageParams({current:1,pageSize:200})→pageNo1/pageSize200，无scope/roleslist/roleId/accountId；期望Response.data为PageVo.records，normalizePageData兼容既有list/数组后为data.list。普通编辑不另发角色请求，页面挂载rolesQuery key为sys/roles、staleTime5分钟，AOE使用props.roles。此是其源码核查，不是backend实际HTTP/React缓存实证；现场空的请求/缓存根因尚未确定，不能以已选角色空解释可选必空。

组织读取GET /sys/organization/list→OrganizationService.list→selectOrgMapList→Convert→TreeHelper。查询BO支持id/name/code/pid，XML未删及这些参数过滤，JOIN父节点要求父未删；无tenant0硬筛或默认D1过滤，原数据插件仍可缩小范围。TreeHelper可将缺父节点作为root；OrganizationListVo有id/key/title/name/pid/children，没有value别名，空children返回null。可供前端核fieldNames、候选缺失、控件条件渲染，不能因DB有D1就放宽权限或断言请求已返D1。

已经主动同步前端D1持久化、完整空关联、VO字段、JSON字符串ID及树字段。前端自行核实际Form/request边界，不由backend回填、扩shape、读真实缓存/store、日志或权限。

## 4. 实际首class/resource与当前产物

UTC2026-10-24T07:36:50.453636+00:00仅内部提取PID49000允许的java.class.path/user.dir，workspace匹配，classpath199项。指定19个class和4个Mapper XML首命中均与当前build输出逐字节一致，四XML也与source一致；未输出全classpath或其它属性。完整目标/相对首来源/SHA与布尔核验在下方安全JSON，不按JAR名猜版本。


目标包括UserService/Ctrl/Convert/ConvertImpl/Entity、AccountProfileEntity、UserAoeBo/DetailVo/ListVo、RoleService/Ctrl/QueryBo、UserPostRefSerivce、OrganizationService、PageBo、DataPermissionHandler、JacksonConfiguration、CommonResponseAdvice、PageResult，以及User/UserRoleRef/Role/Organization四XML。限定javap部门引用均true：getDeptId/setDeptId、boToEntity、saveProfile、AccountProfileEntity.setAccountId/setDeptId、AccountProfileMapper.insert/updateById、两个VO的setDeptId。

未发现该链首来源与当前输出差异；这是classpath磁盘/字节码证据，未读进程内存class，不代替实际HTTP内容或Spring全部effective配置证明。

## 5. 安全证据与交付

- DB JSON：.gradle/package-s-peer-profile-readonly-20261024.json，SHA256 DBDB4DDD9F56B9DF10FBFDCE8AE4338D09F271CDE755A2A64E58513E065B6D2F。
- 首来源/字节码JSON：.gradle/package-s-peer-runtime-readonly-20261024.json，SHA256 67D07C250AEC073F6C919CAA791376813F2084746FFFC9FAA247568E0AC46C63。

所有连接/命令已结束，SQL/HTTP/Redis写0，无HTTP/Redis调用、源码/配置/依赖修改、测试构建、服务启停、授权、第二账号创建或提交推送。只写本S附件、S0补录和本人status。部门D1及初始空角色/岗位已证，不等于页面显示/编辑修复或S1–S8已验收，不登记all done。
