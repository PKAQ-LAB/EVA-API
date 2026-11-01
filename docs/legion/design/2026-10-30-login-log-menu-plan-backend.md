# 历史登录日志运行准备与最小菜单补齐提案

2026-10-30 backend_developer。本轮仅应用编译和精确元数据只读核验；以下 SQL 是待总控呈用户审核的方案，未执行、未写入迁移文件、未更改任何 grant 或服务。

## 应用编译结果与运行边界

用户本地 `C:/home/DevApp/gradle/bin/gradle.bat` 执行 `:web-booter:compileJava`，`--offline --no-daemon --no-parallel --max-workers=1 --no-build-cache --no-configuration-cache`，禁工具链自动下载。一次真实编译 `BUILD SUCCESSFUL in 59s`，11 个 compileJava 任务全部执行，覆盖 web-booter 与其 10 个依赖模块；没有复用此前局部 javac 作为本次应用编译结果。

发现 live JDT、Gradle daemon 及 WebBooter 后，没有对默认 build 目录编译或停进程。18 行临时 init 将所有项目 buildDirectory 指向 `.gradle/login-log-integration-20261030/products`，project-cache 同在该独立目录。任务图仅允许 compileJava/processResources/classes/jar，并在任务执行前拒绝任何越界输出；实际任务图仅 11 个 compileJava。未修改 IDE 设置，未使用 wrapper、clean、bootRun、测试或依赖下载。编译产物共 480 个 class，详细命令、init、dry/compile 日志及哈希见该目录 result.json。

已独审冻结的 7 个生产文件与 4 个定向测试 SHA 全部保持一致；原 QA fresh 30/30 证据仍绑定原 manifest。此次是实际应用主代码的 Gradle 集成编译，不是 bootJar、完整测试、Spring Bean 启动装配、真实权限或页面运行通过。当前运行后端未重启，仍不能认为它已加载新源码。后续需要正常构建/部署并重启 web-booter；本轮不自行操作。

## 当次只读开发库事实

只读窗口一为 2026-10-30 12:43:19.466698 至 12:43:19.497802 +08，窗口二为 12:47:13.858975 至 12:47:13.871185 +08。每次均验证 dev 与 prod 目标不同，固定 eva/eva，transaction_read_only 和 default_transaction_read_only 均 on，REPEATABLE READ，最终 rollback/close，写入 0。第一次还核 source/build 的 dev datasource 一致，以及四张目标表实际属于 eva。未查询账号、个人信息、登录或业务日志内容，也未扫描私有租户 schema。

父菜单 `1000000000000000200` 为“日志管理”，code=log，route=/log，path=/1000000000000000200，tenant0、有效未冻结。现有子菜单仅 online/biz/error，ID 分别 201/202/203（均以前缀 1000000000000000 组成完整 ID）；不存在候选完整 ID `1000000000000000204`、route=/log/login 或 code=log.login 的叶节点。

既有资源 `1000000000000001407`：API_LOG_LOGIN，MAIN_ID=`1000000000000000200`，URL=/monitor/log/login/**，RESOURCE_TYPE=*，tenant0、deleted0。父菜单资源 `1000000000000001200` 为 MENU_LOG，URL=/log。

两个资源的直接 role grant 均恰为：有效 tenant0 ROLE_ADMIN，role ID=`1000000000000000004`；以及旧 core 正租户管理员角色，role ID=`766008469304664561`、tenant ID=`1000000000000000001`。两角色 deleted0/frozen9999。没有读取其账号 membership，因此这里只证明角色级菜单授权，不能据此证明任意账号当前有效或该旧 core 角色就是私有租户资格。证据为 menu-readonly.json 与 parent-role-readonly.json。

## 推荐只改一条菜单和一条挂接

新增模块 ID=`1000000000000000204`，标题“登录日志”，code=log.login，PID=`1000000000000000200`，PATH=/1000000000000000200/1000000000000000204，ROUTE_URL=/log/login，COMPONENT_URL=./log/login，ISLEAF=true，SORT=4，tenant0、deleted0、frozen0。仅把既有 API_LOG_LOGIN 的 MAIN_ID 从父菜单 200 改为新叶 204。

不新增 MENU_LOG_LOGIN 资源，不改既有 URL/method，不增加任何通配授权，不修改 SYS_ROLERES_REF、SYS_TENANT_RESOURCE、套餐或私有角色副本。关联的资源 ID=1407 保持，因此已有直接资源授权自然投影到新叶，不需要 grant。按 AuthorizedMenu.xml 的精确 resource ID 筛选，只有父资源的普通角色不会因父子关系而继承新叶；ALL 数据范围也不生成资源授权。当前 dev standalone 模型会排除正 tenant 的旧 core 角色，已有有效 tenant0 管理员在具备 1200 与 1407 的现关系下可得到父与叶。

这不是所有运行环境的可见性承诺：未读取真实 userinfo、运行 profile 或私有租户 role refs。schema 模式还要求其真实私有角色和 SYS_TENANT_RESOURCE 已授 1407，不能为了露菜单补 grant；现 AuthorizedMenuService 的 schema 分支明确拒绝 tenantId<=0，因此本提案不解决平台 tenant0 的菜单入口，该独立缺口不能以静态路由或后端跨租户读资格替代。普通用户即便已有其它宽资源或手输路由，历史读取仍受本次有效管理员门禁。

technical_designer 已只读复核：菜单 SQL 不按 MENU_ 前缀或资源类型过滤，故复用 1407 可投影新叶；其同时确认前端 registeredMenuRoutes 已含 /log/login，读取谓词同时要求菜单路径与 API GET 权限。本角色没有跨仓读取或修改前端，也未将该静态复核当作页面实证。

## 待批准执行 SQL（仅当前已核 dev eva）

批准执行时必须重新核目标、角色级关系和新 ID/route/code 未占用；如数据漂移则停止，不 upsert 覆盖现有节点。以下事务只填结构字段和数据库时间，不虚构操作者名称或 ID；如执行流程要求审计操作者，必须由批准执行渠道提供真实值。锁超时后停止，不等待无界锁。

```sql
BEGIN;
SET LOCAL lock_timeout = '2s';
SET LOCAL statement_timeout = '5s';
LOCK TABLE eva.sys_module, eva.sys_module_resources IN SHARE ROW EXCLUSIVE MODE;
DO $$
DECLARE affected integer;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM eva.sys_module
        WHERE id = 1000000000000000200 AND code = 'log' AND route_url = '/log'
          AND tenant_id = 0 AND deleted = 0 AND frozen = 0 AND isleaf = FALSE
          AND pid = 0 AND path = '/1000000000000000200'
    ) THEN RAISE EXCEPTION 'log parent changed'; END IF;
    IF EXISTS (
        SELECT 1 FROM eva.sys_module
        WHERE id = 1000000000000000204 OR route_url = '/log/login'
           OR code = 'log.login'
    ) THEN RAISE EXCEPTION 'login menu identity occupied'; END IF;
    IF NOT EXISTS (
        SELECT 1 FROM eva.sys_module_resources
        WHERE id = 1000000000000001407 AND main_id = 1000000000000000200
          AND code = 'API_LOG_LOGIN' AND resource_url = '/monitor/log/login/**'
          AND resource_type = '*' AND tenant_id = 0 AND deleted = 0
    ) THEN RAISE EXCEPTION 'login API resource changed'; END IF;

    INSERT INTO eva.sys_module (
        id, revision, deleted, frozen, sort, tenant_id, utc_create,
        code, name, pid, path, isleaf, route_url, component_url
    ) VALUES (
        1000000000000000204, 0, 0, 0, 4, 0, CURRENT_TIMESTAMP,
        'log.login', '登录日志', 1000000000000000200,
        '/1000000000000000200/1000000000000000204', TRUE, '/log/login', './log/login'
    );
    UPDATE eva.sys_module_resources SET main_id = 1000000000000000204
    WHERE id = 1000000000000001407 AND main_id = 1000000000000000200
      AND code = 'API_LOG_LOGIN' AND resource_url = '/monitor/log/login/**'
      AND resource_type = '*' AND tenant_id = 0 AND deleted = 0;
    GET DIAGNOSTICS affected = ROW_COUNT;
    IF affected <> 1 THEN RAISE EXCEPTION 'unexpected resource row count'; END IF;
END $$;
-- 同事务独立核对：新增模块 1 行、挂接资源 1 行；原资源 URL/method/ID、全部 grants 不变。
-- 核对通过才 COMMIT，否则 ROLLBACK；当前提案未执行。
```

上述 SQL 没有 COMMIT，批准后的实际执行必须另做执行前 grant 快照及事务内差异核验；不能把本次历史只读快照作为未来写时的锁定依据。

## 回退 SQL 与边界

未提交时直接 ROLLBACK。若已提交且需回退，先核节点未被其它操作改造、无新增子菜单或其它资源挂接，再将 1407 挂回原父并软删除本次叶，不物理删数据、不删资源或 grant。若任何检查不符则停止人工复核。

```sql
BEGIN;
SET LOCAL lock_timeout = '2s';
SET LOCAL statement_timeout = '5s';
LOCK TABLE eva.sys_module, eva.sys_module_resources IN SHARE ROW EXCLUSIVE MODE;
DO $$
DECLARE affected integer;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM eva.sys_module
        WHERE id = 1000000000000000204 AND revision = 0 AND deleted = 0 AND frozen = 0 AND sort = 4
          AND tenant_id = 0 AND code = 'log.login' AND name = '登录日志'
          AND pid = 1000000000000000200
          AND path = '/1000000000000000200/1000000000000000204'
          AND route_url = '/log/login' AND component_url = './log/login' AND isleaf = TRUE
    ) OR EXISTS (
        SELECT 1 FROM eva.sys_module WHERE pid = 1000000000000000204
    ) OR EXISTS (
        SELECT 1 FROM eva.sys_module_resources
        WHERE main_id = 1000000000000000204 AND id <> 1000000000000001407
    ) THEN RAISE EXCEPTION 'login menu no longer matches this change'; END IF;
    IF NOT EXISTS (
        SELECT 1 FROM eva.sys_module
        WHERE id = 1000000000000000200 AND code = 'log' AND route_url = '/log'
          AND tenant_id = 0 AND deleted = 0 AND frozen = 0 AND isleaf = FALSE
          AND pid = 0 AND path = '/1000000000000000200'
    ) THEN RAISE EXCEPTION 'original log parent changed'; END IF;
    UPDATE eva.sys_module_resources SET main_id = 1000000000000000200
    WHERE id = 1000000000000001407 AND main_id = 1000000000000000204
      AND code = 'API_LOG_LOGIN' AND resource_url = '/monitor/log/login/**'
      AND resource_type = '*' AND tenant_id = 0 AND deleted = 0;
    GET DIAGNOSTICS affected = ROW_COUNT;
    IF affected <> 1 THEN RAISE EXCEPTION 'login resource no longer matches this change'; END IF;
    UPDATE eva.sys_module SET deleted = 1 WHERE id = 1000000000000000204 AND deleted = 0;
    GET DIAGNOSTICS affected = ROW_COUNT;
    IF affected <> 1 THEN RAISE EXCEPTION 'unexpected module row count'; END IF;
END $$;
-- 核对挂接恢复、叶隐藏及 grants 不变，再决定 COMMIT 或 ROLLBACK。
```

回退保留软删除记录，因此之后重试不能直接重复 INSERT 覆盖该 ID，须另审恢复方案。执行后通过现有登录/userinfo 刷新流程回读菜单；不直接清空 Redis、不改 permVer、不强退会话。若缓存机制或前端入口与预期不符，先报告，不补权限或硬编码菜单。
