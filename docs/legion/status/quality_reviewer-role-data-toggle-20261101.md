# 数据权限开关独立QA

2026-11-01。本轮仅dataPermissionEnabled能力与角色保存分支；资源按钮联动不在实现或验收范围。dataPermission关闭指原SQL数据拦截器不注册，仍受业务/资源门禁，不解释为0007。无DB/Redis/服务/浏览器/业务写/认证/密码/Git发布操作。

## 冻结与源审

后端冻结.gradle/role-data-toggle-20261101/source-freeze.json SHA B4B27F66B3F2BAC8B142C21ABFD1F833B7998F09E228F59F0665D9E36D02D6C0，7输入/9真实Gradle产物/5日志XML逐SHA匹配。仅成功登录和fetchMenus外层输出真实配置独立boolean，ALWAYS保false；失败响应不带能力。RoleService由服务端开关决定，false新建强制0000/空关联，false编辑转换前避开客户端岗位列表解析、转换后逐项恢复原scope/org/rawPostCSV，不重验旧坏CSV/旧关联；true原strict及冻结/唯一性/事务/版本保持。原MybatisPlusConfig条件注册无diff，hash0BAC7FA8931CAB97EF34E8107296ABF91567F2F4AE097141AE79AE6C9F230E40匹配；此为源审，非重启后现场插件观测。

前端旧B1A5因异步校验时flag变化仍可能沿旧闭包提交而撤回。最终.git/role-data-capability-source-20261101.json SHA D0EEF9FD36821F9E143C38D9DFCD84AC7FA462A58C6DF4309233D73B6445E6C3，15输入前后匹配。外层严格成功boolean覆盖缓存/userinfo，resource独立；false新增编辑不挂scope/dept/post候选，Form及service两层省略五字段；unknown隐藏并禁存，view保持原只读，true原校验保持。latestCapability ref在异步validate后与捕获值比对，能力变化停止旧提交。E350普通左右动态宽/nochips及字典同步算法不扩改。未见必须修正问题。

## 实际独立测试

后端初次同命令exit0但34任务全UP-TO-DATE，不计独验；随后三个test目标各加--rerun，实际命令为Gradle :eva-web:eva-web-auth:test --rerun --tests org.pkaq.core.auth.authentication.entrypoint.AuthLoginCapabilityTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.role.service.RoleServiceTest :web-booter:test --rerun --tests org.pkaq.sys.dict.service.DictSyncHttpTest.menuBootstrapReportsIndependentCurrentFlagsOnlyOnSuccess --offline --no-daemon --no-parallel --max-workers=1 --console=plain。自然exit0/BUILD SUCCESSFUL42s，34tasks3executed31up-to-date。XML分别3/18/1，共22项、0失败/错误/跳过，7输入运行后无漂移。登录真实成功处理器和菜单真实MVC/Security/Advice链使用合成账户/菜单替身，角色测试包含mapper/converter替身与既有真实转换用例，无真实DB。

后端结果.gradle/qa-role-data-toggle-20261101/result.json SHA7FE98420850581E555388E1B1227314D4505F436B50364619E2E75FB0DC14706，独立XML副本同目录；日志.gradle/role-data-toggle-20261101/qa-rerun-targeted.log SHA57E53D3095027EB27D88F38A8CAF70F7211A6B1B4BA04FF914847B509D6524B2。

前端实际Vitest仅dataPermission.test.ts、role/postscope.test.tsx及role/service.test.ts三文件43通过/0skip/12.58s；随后app.test.tsx和login/resourcePermission.test.tsx按“数据能力|菜单bootstrap失败”过滤12通过/27skip/6.12s，两轮自然exit0，共55个不同用例。覆盖false新增/编辑省略五字段、无候选请求、unknown禁存、true原路径、能力切换草稿、true→false/unknown异步迟到0save及bootstrap/login覆盖旧缓存flag。结果eva-web/.git/qa-role-data-capability-result-20261101.json SHA2305CA38BB81A721467B9B17EBF4C99E83B8F5BC9556B392D00F2ED9C5E81509；核心日志SHA4F7872149136BC9AB1B24ABC54F322C51A01670907B60BD935408990B983F3FC，传播日志SHA60279A7A4268646CFEDBF30313C8BE3D6C99A788C3F3A6383DA2CA933A2D6A63。

未重137全批、旧字典47/后端2、0006PG或全仓；未复跑fulltsc，不将开发已知外部诊断称全类型通过。无活动重进程，测试槽已归还。结论限冻结源码及上述定向验证；实际部署开关效果需用户构建重启后才可观测，不新增现场gate阻塞本轮代码收尾，未获本轮提交推送授权。
