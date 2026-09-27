# 系统管理后端 CRUD 与业务联动验收矩阵

> 基线日期：2026-09-27

## 验收结论

系统管理后端已具备字典、模块、组织、岗位、角色、用户、租户、租户套餐、通知及平台跨租户只读查询的接口闭环。接口面由 `SystemManagementEndpointContractTest` 统一约束，业务行为继续由各领域单元测试和 PostgreSQL 集成测试验证。

## 能力矩阵

| 领域 | 查询 | 新增/修改 | 删除 | 状态/排序 | 业务联动证据 |
| --- | --- | --- | --- | --- | --- |
| 字典 | list/get/query | edit | del | switch | `DictCtrlTest`、`DictServiceTest`、`DictConvertTest`、租户缓存测试 |
| 模块 | list/get | edit | del | frozen/sort | `ModuleCtrlTest`、`ModuleTreeIntegrationTest`、API 资源迁移测试 |
| 组织 | list/get | edit | del | switch/sort | `OrgCtrlTest`、`OrganizationTreeIntegrationTest`、只读保护测试 |
| 岗位 | list/get | edit | del | switch/sort | 接口契约、删除引用保护和只读保护测试；岗位用户差异更新的直接单测列入后续覆盖补强 |
| 角色 | list/get | edit | del | switch | 用户授权、资源授权、数据范围和 schema 隔离测试 |
| 用户 | list/get | edit | del | switch | 角色授权、岗位授权、密码重置、会话和权限版本测试 |
| 租户 | list/get | edit | del | switch | schema 创建、管理员、根组织、授权、升级迁移及私有账号测试 |
| 租户套餐 | list/get | edit | del | switch | 套餐资源授权、活动授权引用保护测试 |
| 通知 | list/get | edit | del | switch | `NoticeIntegrationTest` 完整 CRUD 与状态联动测试 |
| 平台租户查询 | options | — | — | — | 组织/角色只读查询、目标 schema 安全和迁移测试 |

## 强制回归

```powershell
gradle :eva-web:eva-web-sys:eva-web-sys-service:test --tests org.pkaq.sys.SystemManagementEndpointContractTest
gradle test
```

接口契约测试只证明路由能力没有缺失；发布验收仍必须在真实 PostgreSQL 上验证新增、修改、删除、冻结、排序、授权、引用保护、租户隔离和异常回滚。
