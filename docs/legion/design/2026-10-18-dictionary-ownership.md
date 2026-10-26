# 两级可展开字典表格：开发库实证与最小恢复契约

- 设计任务：DICTIONARY-OWNERSHIP-DESIGN-20261018；本轮主卡：RUNTIME-PERMISSIONS-DICTIONARY-TANSTACK-20261019。
- 角色：technical_designer；更新：2026-10-19（Asia/Shanghai）；唯一总控：01a0bc51-e17e-7990-ad76-13c63f385f33。
- 本轮只写本设计与本人状态卡；源码、SQL文件、migration、DB/Redis均未写入。获准读取开发PG结构与最少字典关联元数据，已完成，不读取业务个人信息。
- **用户展示目标已确定：左侧是可展开/收起的Ant Design树形Table，不是纯Tree。只有系统字典/业务字典两个顶级分类，下面是具体字典主项；右侧显示SYS_DICT_ITEM键值明细，不能成为左侧第三层。** 不再询问这个UI选择。
- 保持SideLayout左右40/60、collapsible=false。系统根/子项/明细只读，业务tenant自治、standalone管理员维护；普通故障修复和最终提交push授权不等于任意DB迁移或未知归属已批准。本成员不提前发布。

## 1. 可以交付实施的明确方案

开发库已存在`pid BIGINT NOT NULL DEFAULT 0`，**不要重复新增PID或KIND**。复用当前SYS_DICT/SYS_DICT_ITEM；恢复已存在但逻辑删除的系统根`1000000000000004000 / SYSTEM_DICT`，新增当前未占用的业务根`1000000000000004080 / BUSINESS_DICT`。当前七组有效字典有一致的历史根路径证据，挂回系统根；现有QA字典没有父关系证据，只有这一条归属需要确认。所有已删除字典及旧叶子保持删除。

推荐一次性确认语句（供总控呈现，不代表已获批准）：**“确认按本文清单复用现有PID，恢复4000系统根、新建4080业务根，七组有效旧字典挂系统根，现QA字典2111791956297613314挂业务根，保持DATA_SCOPE等删除状态；同步将/list改为两根children表格契约后实施。”** 若不认可QA归属，只需指明这一条归属或明确排除；不能默认按测试名称把它分业务。

这是一份有实际目标ID、归属证据、API形状及停止条件的实施方案。当前尚未执行文中任何DDL/DML，也未改后端/前端源码；主卡仍要求总控确认具体DB/API变更后派写者。

## 2. 当前开发PG只读实证

使用现有开发连接解析与目标校验逻辑，仅调用`connect(True)`；目标指纹`d22bc534e71c`，数据库eva、schema eva，与生产目标不同。连接在SERIALIZABLE只读事务内，逐次核验`transaction_read_only=on`，结束均rollback/close。凭据未输出、未存文档/脚本；没有执行备份文件、维护run、缓存或写函数。

| 实测项 | 2026-10-19只读结果 | 实施含义 |
| --- | --- | --- |
| 相关schema | 仅eva；未发现eva_core/tenant_*命名空间，相关字典表仅eva.sys_dict与eva.sys_dict_item | 本开发目标按现有单schema恢复，不凭其他部署上下文创建core或tenant schema |
| SYS_DICT列 | 已有pid、path、isleaf、value，以及当前type/code/name等 | 当前实体/现V1无父列与实际库不一致；复用PID，不运行新增列方案 |
| PID | bigint、NOT NULL、DEFAULT 0；已有idx_sys_dict_pid | 不增加冗余父列或第二个相同索引 |
| 主表记录 | 47总行：8有效、39逻辑删除 | 恢复分类不做清库、不物理删除旧数据 |
| 系统根 | 4000完整ID为1000000000000004000；code/type SYSTEM_DICT，name系统字典，pid0，path=/4000完整ID，isleaf=false，deleted=1783056863，frozen0 | 是现存已删除根，恢复是明确数据变更，不能当作“已有效存在”或无授权复活 |
| 业务根候选 | ID1000000000000004080与code BUSINESS_DICT在所有状态记录中均不存在 | 可以在确认后创建，不占用现有记录；执行前重查 |
| 旧ID1/3 | SYS_DICT均未占用，但也没有对应根/父关系 | 不用更早备份的1/3覆盖当前可靠的4000结构；新根选4080 |
| 当前有效主项 | 七组V1码表+一条QA字典，全部pid0 | 不能把pid0全部当新增根显示，先按确认清单恢复父关系 |
| 已删除DATA_SCOPE | 主项4001完整ID逻辑删除标记1；对应8条SYS_DICT_ITEM也已删除 | 本次不复活；历史关系可留证，不混入七组有效系统清单 |
| 旧单表叶子 | 37条，均已逻辑删除；保留pid与路径指向4000→各主项→旧叶子 | 是根归属佐证，不是第三层展示内容，不恢复叶值到SYS_DICT |
| SYS_DICT_ITEM | 36总行、28有效；有效孤儿0；两个根候选均无main_id明细引用 | 迁移不修改明细行，右侧沿当前双表读取 |
| 唯一性 | 当前无活动重复code；实际主表只有旧uk_sys_dict_sibling_code(type,pid,code) WHERE deleted=0，**没有当前V1声明的活动CODE唯一索引** | 分类落地时补活动code唯一约束兜底，不误报库里已有；旧索引先保留 |
| FK/版本表 | 未发现指向这两字典表的数据库FK；eva schema未发现flyway或eva_tenant_schema_version表 | 不代表没有应用逻辑引用；禁止为收口重跑全部V1或擅自baseline Flyway |
| V_DICT视图 | 实际仍从SYS_DICT的isleaf/value投影旧单表叶值；当前Java/XML链路未发现引用它，现服务直接读SYS_DICT_ITEM | 不靠旧view分类或取明细；本次不顺手改无消费者的旧view，另记录结构差异 |

元数据漂移校验摘要（不含凭据/个人信息）：SYS_DICT受检字段MD5=`97fe3525dbadbec4faea9bd841c58469`；SYS_DICT_ITEM受检字段MD5=`2d331236f832869fb761fdd7a444b033`。只用于本次快照变化检测，执行前不一致必须重新盘点/审阅，不能覆盖正在浏览器验收的数据。

源码基线后端cornerstone/6a0fee51、前端trunk/c220a9c0；两仓库已有其他成员未提交变更，均保留。源码默认standalone不能代替运行进程有效配置；本次PG目标仅eva schema是直接数据库证据，未据此修改服务或配置。

## 3. 逐项归属清单：不按名称或只读状态猜分

下表主项的当前pid虽已归零，但它们的已删除旧叶子具有一致的`pid=主项ID`与精确`path=/1000000000000004000/主项ID/叶子ID`；4000本身有完整系统根记录。因此是结构关联证据，不是凭名称/语义/9999判断。列中的历史叶子都保持删除。

| 当前主项完整ID | code | 当前有效键值明细数 | 精确系统根路径匹配的旧叶子/全部旧叶子 | 恢复归属 |
| --- | --- | --- | --- | --- |
| 1000000000000004010 | FROZEN_STATUS | 3 | 3/3 | 系统根4000 |
| 1000000000000004020 | TENANT_TYPE | 2 | 2/2 | 系统根4000 |
| 1000000000000004030 | CARD_TYPE | 4 | 4/4 | 系统根4000 |
| 1000000000000004040 | POST_LEVEL | 4 | 4/4 | 系统根4000 |
| 1000000000000004050 | RESOURCE_TYPE | 6 | 8/8 | 系统根4000；旧BUTTON/OPERATE仍删除，不恢复 |
| 1000000000000004060 | LOGIN_TYPE | 1 | 1/1 | 系统根4000 |
| 1000000000000004070 | BIZ_OPERATE_TYPE | 7 | 7/7 | 系统根4000 |

| 未决/不执行恢复项 | 实际证据 | 本次处理 |
| --- | --- | --- |
| 2111791956297613314 / EVA_QA_20261018_D01 | 有效、pid0、path=NULL、1条有效明细；无可靠旧父根关系 | **唯一有效归属未知项**。推荐经一次确认挂业务4080以继续专用业务CRUD验证，不依据名称直接执行 |
| 1000000000000004001 / DATA_SCOPE | 主项deleted1，8条明细均删除；8/8旧叶子路径指向4000 | 保持删除，不自动恢复；若运行中因其缺失出现错误，先通知QA/root，另按真实故障证据派修 |
| 37条已删除旧单表叶子 | 叶子模型残留，全deleted非0；已有当前SYS_DICT_ITEM真实明细 | 保持删除、不塞入左表第三层、不复制或生成重复键值 |

可靠归属可确定七条有效系统主项。未知不是七/八组全部重问，而仅QA那一条；历史更早备份中data_permission挂业务根1不能覆盖当前这批不同编码/ID及4000结构证据，也不能借本次恢复改变已经删除的DATA_SCOPE。

## 4. 历史与当前契约对照

- EVA-API `6412e670:.../V1__INITDB.sql.bak`确有biz/业务代码根1与sys/系统代码根3及PARENT_ID；旧data_permission挂1。证明历史两根确实存在，但不是当前库ID清单。
- `ed2f32ca`实体含pid/path/lines/children；Mapper曾按pid查根/子，同时夹杂旧PARENT_ID映射，不能原样复制。`6412e670`中间单表多级版含value/isleaf，`abb62428`（2026-07-03）删除树字段改平表主项+明细。
- eva-web `1eaa2424` mock明确业务字典/系统字典两个根；`83178e23`列表使用**Table的children树形行**，根不可编辑删除；`393a9c75`（2026-07-03）移除两根与parentId所属分类改平表。
- 当前dictionary/list.tsx仍是AntD Table，index.tsx已有SideLayout width40%、collapsible=false，当前后端BO/VO/Entity不映射PID。最小修复是恢复父关系与children数据到现Table，不换成纯Tree。
- 本地antd 6.6.3；已通过现装CLI读取Table接口，核对expandable.childrenColumnName、expandedRowKeys、onExpandedRowsChange及默认展开选项。CLI提示升级未执行，遵守本任务无依赖安装/升级边界。
- GitNexus此前后端索引落后21次提交、前端存储版本43/42不兼容；本轮不重建索引，读当前文件和git历史；实施写者仍须补impact，不把文档分析当图检查通过。

## 5. 精确的两级Table与BO/VO契约

左侧保留现表头“字典描述、字典编码、操作”和紧凑AntD Table；`dataSource`是两个根对象，各自`children`只装具体字典主项。**不使用Tree组件、不用expandedRowRender放键值明细，也不把lines映成children。** 根顺序系统→业务；主项按sort/id；右侧键值按sort/orders、id。默认展开两根，可分别收起；SideLayout不提供整体左右collapse且保持40/60。

| 对象/API | 可实施约定 |
| --- | --- |
| DictEntity | 映射已有Long pid；不新增kind；现type仍为类型编码，不作分类。当前开发库path/isleaf保持结构一致但不恢复通用多级树功能 |
| DictAoeBo | 增加Long pid；新增缺省服务端业务根4080；编辑缺省保留真实原父项，禁止从业务移系统或改到根0/其他主项；兼容当前旧BO，不为无现用消费者添加parentId别名 |
| DictViewVo | 增加pid、children、editable；两根children只含具体字典，根lines空；具体字典无children，lines保留当前键值形状。父关系派生来源，不持久化kind |
| ID序列化 | 沿现Long按字符串JSON约定；前端id/pid均字符串，根使用完整ID不截断；rowKey含来源/根/id防跨schema重复 |
| GET /sys/dictionary/list | 保持Response.data为数组，数组长度恒2，根SYSTEM_DICT/4000与BUSINESS_DICT/4080；children是分组后的当前有效主项。两根即使为空也返回。当前扁平数组契约有意变更，前后端同批适配 |
| 当前开发目标预期树 | 系统根children恰好上节七条、业务根children为获批准的QA条目；DATA_SCOPE与37旧叶子不返回 |
| GET /get/{id} | 第二级具体字典按当前接口取主项+lines；根不进入编辑。跨schema同ID时需明确来源或拒绝歧义，不能默认一个来源 |
| queryDict(code)、/query/{code}、/get/type/{code}、validate | 保持具体字典code含义及原键值Map形状；只查两根下的当前有效具体字典，排除根与已删除叶子 |
| /auth/fetchDicts | 仍code→Map，只聚合具体字典；不把两根当选项字典，不恢复以可编辑dict_type作为安全分类来源 |
| 唯一性 | 保存及checkUnique按活动具体字典code核对并保留根code；当前8有效code无重复，补活动CODE唯一索引。不允许系统/业务同code静默覆盖，跨tenant业务可同code |

根行无新增/编辑/删除/排序/冻结/保存入口；点击根只操作展开，具体字典行单击更新右侧主项信息及键值列表。系统主项和明细右侧纯查看，业务维护守卫满足才显示写操作。新增编辑仍沿项目aoeform侧滑抽屉规范，不能把行选择本身当写授权。

展开使用以两根key为初值的受控expandedRowKeys，异步首载时初始化一次；后续刷新保留用户收起状态，不因重新拿到数组总展开。保留既有操作列align:center，内部操作阻止行点击冒泡。首载选首个可见具体字典，刷新保留有效选择；无可选项右侧空态。搜索过滤具体字典时保留两根，按来源/租户/主项标识丢弃迟到详情响应。

## 6. 数据变更草案：只供确认后实施，当前未执行

目标仅开发eva，不建core/tenant、不改权限授权、不写SYS_DICT_ITEM或日志，不恢复任何已删除业务项。现有PID列复用，给未来新schema的新增量版本才按列存在性补PID。不要重跑V1、删旧索引、DROP旧列或批量清数据。

执行前协调QA与写入锁，获得本表具体归属确认后，重新按指纹连接、核验当前快照并做原行/索引精确备份；本轮尚无迁移备份，不能声称可随时无损回滚。运行服务不重启，未获锁不热改正在验收的页面。下列草案有意默认阻断，批准后仅由后端写者实现/演练：

```sql
-- 草案，当前没有执行；连接仍须先验证开发目标指纹，不因同名eva库就认定正确。
BEGIN;
SET LOCAL lock_timeout = '3s';
SET LOCAL statement_timeout = '15s';
LOCK TABLE eva.sys_dict, eva.sys_dict_item IN SHARE ROW EXCLUSIVE MODE;
DO $$
DECLARE
    approved boolean := false; -- 取得本文具体DB/API/QA归属确认后由写者显式置真。
    dict_digest text;
    item_digest text;
BEGIN
    IF NOT approved THEN RAISE EXCEPTION 'dictionary plan requires explicit approval'; END IF;
    IF current_database() <> 'eva' THEN RAISE EXCEPTION 'unexpected database'; END IF;
    SELECT md5(COALESCE(string_agg(
        ROW(id,revision,deleted,frozen,sort,tenant_id,type,code,name,value,pid,path,isleaf)::text,
        '|' ORDER BY id),'')) INTO dict_digest FROM eva.sys_dict;
    SELECT md5(COALESCE(string_agg(
        ROW(id,revision,deleted,frozen,sort,main_id,d_code,d_value)::text,
        '|' ORDER BY id),'')) INTO item_digest FROM eva.sys_dict_item;
    IF dict_digest <> '97fe3525dbadbec4faea9bd841c58469'
        OR item_digest <> '2d331236f832869fb761fdd7a444b033' THEN
        RAISE EXCEPTION 'dictionary snapshot changed; re-inventory required';
    END IF;
    -- 确认后的业务根方案：恢复既有系统根，仅新增一条业务根。
    UPDATE eva.sys_dict SET deleted=0,frozen=9999,revision=COALESCE(revision,0)+1
      WHERE id=1000000000000004000 AND code='SYSTEM_DICT' AND pid=0;
    INSERT INTO eva.sys_dict
      (id,revision,deleted,frozen,sort,tenant_id,type,code,name,pid,path,isleaf)
      VALUES (1000000000000004080,0,0,9999,2,0,'BUSINESS_DICT','BUSINESS_DICT',
        '业务字典',0,'/1000000000000004080',false);
    UPDATE eva.sys_dict
      SET pid=1000000000000004000,path='/1000000000000004000/'||id::text,
          frozen=9999,revision=COALESCE(revision,0)+1
      WHERE deleted=0 AND id IN
        (1000000000000004010,1000000000000004020,1000000000000004030,
         1000000000000004040,1000000000000004050,1000000000000004060,1000000000000004070);
    UPDATE eva.sys_dict
      SET pid=1000000000000004080,path='/1000000000000004080/'||id::text,
          revision=COALESCE(revision,0)+1
      WHERE id=2111791956297613314 AND code='EVA_QA_20261018_D01' AND deleted=0;
    IF (SELECT count(*) FROM eva.sys_dict WHERE deleted=0 AND pid=0)<>2
       OR (SELECT count(*) FROM eva.sys_dict WHERE deleted=0 AND pid=1000000000000004000)<>7
       OR (SELECT count(*) FROM eva.sys_dict WHERE deleted=0 AND pid=1000000000000004080)<>1
       OR EXISTS (SELECT 1 FROM eva.sys_dict WHERE deleted=0
                  AND id NOT IN (1000000000000004000,1000000000000004080)
                  AND pid NOT IN (1000000000000004000,1000000000000004080)) THEN
        RAISE EXCEPTION 'unexpected root membership';
    END IF;
END $$;
CREATE UNIQUE INDEX uk_sys_dict_code ON eva.sys_dict(code) WHERE deleted=0;
-- 实施者还需完成列/父关系约束、逐行原值对比、明细摘要不变与恢复演练。
ROLLBACK; -- 草案默认回滚，不能将本段当已授权提交脚本。
```

上面在批准业务QA挂靠时才成立；若QA明确排除/另归类，必须改清单、预期计数与受检摘要后再审，不跳过快照校验。实际执行还要验证索引名称未占用、确认新索引定义，不能盲目用IF NOT EXISTS掩盖错误索引。本次只读检测表明活动code可建唯一索引。

开发库pid=0用作根哨兵，不能直接加普通`FOREIGN KEY(pid) REFERENCES sys_dict(id)`而要求不存在的0行，也不为此增加第三个根。最小约束是活动根只允许批准的4000/4080且pid0，活动主项只能pid指向这两根；服务层再验证根存在且不可删除/改身份。所有已删除残留可保留旧关系。活动具体字典不能有SYS_DICT子节点，根不能有SYS_DICT_ITEM，校验在迁移/服务入口完成，不新建通用树框架。

预期数据差异（批准清单不变时）：SYS_DICT修改9条（系统根+七系统主项+QA）、新增1条业务根；总行47→48、有效8→10、删除39→38。SYS_DICT_ITEM仍36总/28有效，摘要完全相同；DATA_SCOPE保持deleted1、37旧叶子全保持删除；type/code/name等具体字典字段、业务明细值和其他表不变。新DDL仅活动code唯一索引及经审查的父关系约束，实际开发库不新增PID列。

恢复方案：写者先备份9条原行及新索引/约束清单，保存确认后清单与摘要；回滚仅删除本次新业务根/新索引约束、恢复9条原行（包括原deleted/PID/path/frozen/revision），并逐行核明细不变。若原行或QA出现后续业务修改，拒绝覆盖式恢复，重新评估。未获备份与恢复演练证据不得发布迁移。

## 7. 服务守卫与未来schema模式（不扩大当前开发库写入）

系统判定用真实父根关系和批准根ID/code，不能用客户端pid/rootCode、名称、type、frozen猜分类。新增只允许业务根，编辑先查真实父项，删除/冻结/主项排序/明细增删改排序/导入（若后续新增）全复用同一守卫。两根以及系统主项/明细对所有用户拒写；系统frozen9999是兼容旧客户端，不替代守卫。GET和POST旧删除都保护，批量switch先全检全批失败，不静默跳过。业务下只读明细也拒绝改值/排序/移除，外main_id或外租户ID不能被当新增。

standalone业务维护要求服务端有效ROLE_ADMIN与操作资源；TENANT_ADMIN与ThreadUserHelper.isAdmin的ROLE_ADMIN不同。schema模式业务限定可信当前tenant，租户维护角色仍由本租户明确授权、受平台资源上限约束；系统字典core唯一读取且产品侧只读，不能在tenant复制并独立改系统值。平台targetTenant只沿现inspect能力只读查看，不扩业务写接口、JWT身份或全租户授权。

当前eva仅单schema，本次不迁往core。未来新/既有tenant版本按同一两根父关系契约初始化，各schema根锚点、系统core源与tenant业务源显式聚合，禁止靠tenant,core search_path同名表回退读写。正租户缺表/根/版本拒绝，不落core；平台tenant0读取系统不得套正租户解析；finally恢复路由上下文，CoreSchemaExecutor不绑定TenantContext须显式传缓存归属。新的schema新增量版本允许补PID但不重跑V1/覆写租户自维护数据。

缓存沿dictdata，key包含可信ownerSchema+根ID/身份+具体dictCode；旧code-only接口按明确来源解析，无跨分类静默覆盖。getAll/removeAll限定归属，不缺上下文降为裸code；按主项清单补齐缓存缺项。事务提交后只刷新本owner的新旧code，回滚不刷新；串行/并发快照不能覆盖新值。/auth/fetchDicts与JacksonCodeSerializer直接缓存旁路一并接入来源规则，序列化在业务路由恢复后仍需可信身份，不依赖线程残留。

共享字典不以个人CREATE_ID范围结果填租户共享缓存；仅对受限字典查询方法明确共享范围并强制schema/父根/身份，不全局扩大data_scope。前端queryKey含身份/租户/查看目标，选择与退出登录清旧展示、忽略迟到响应。当前QA唯一浏览器、前端唯一写者，本成员不热改或自行接管服务。

## 8. 文件影响、验收与一次性决策边界

| 位置 | 最小预计变更 |
| --- | --- |
| 数据升级 | 当前开发目标复用PID及上述精确清单；新增量版本/未来tenant版本确保PID、两根及活动code约束；不改旧V1、不baseline全部迁移 |
| 后端模型/API | DictEntity/Dict.xml、DictAoeBo/DictViewVo/DictConvert/IDictService、DictService/DictCtrl；list children、真实父项拒写、根排除与唯一性 |
| 缓存/旁路 | DictCacheHelper/DictInit、AuthCtrl.fetchDicts、JacksonCodeSerializer，必要的受限共享读取方法 |
| 前端 | 原dictionary index/list/data/service/readonly/aoeform及明细组件与mock；保持Table和SideLayout布局，不换成纯Tree；上下文共享文件先与唯一写者协调 |
| 现有回归 | DictService/Convert/CacheHelper/Controller/schema测试；字典children展开、选择/搜索/只读/异步与mock契约测试；根不进query/fetch |

验证必须覆盖：只返回两根与7+1具体主项（批准清单）；无第三层/键值混树；默认两根展开且可收起、异步刷新不抢回展开状态；40/60及无左右collapse；根无CRUD；系统主项/明细全只读；业务本tenant/standalone管理员边界；两个删除方法、混合批量全回滚、明细外ID；code-only读取和部分缓存补齐；DATA_SCOPE不被意外恢复；迁移前后明细及其他表不变、原行备份/恢复演练；类型检查/定向测试与独立运行QA分别留证。

UI、两根和权限基本边界均已确定。**剩余一次性确认仅针对本开发目标的具体DB/API恢复清单，尤其QA唯一未知归属、恢复逻辑删除系统根及/list children变更**；无需重问要不要表格/两根/展开。普通修复与最终push授权已知，但根/数据归属变更不能靠泛修复授权推断批准；最终提交由总控在全部任务/独立QA完成后正式唤起，精确暂存保护他人dirty/GO/原型stash。

本轮已完成当前源码/历史/开发PG只读核验、本地antd接口核对和文档检查；未执行草案SQL、写DB/Redis、改生产/测试源码、安装升级、启动/重启服务、浏览器、编译测试、commit/push或JEV调用。

## 9. 2026-10-22 安全补充：日志范围与角色空值（只读设计）

本节只读核对当前源码，不再查库或读取日志/个人数据；第2节开发PG数字与DATA_SCOPE删除状态为2026-10-19快照，本轮未刷新。字典4000/4080及/list children具体恢复仍未获确认，本节不启动迁移或改变其待确认状态。

### 9.1 根因及必须一起覆盖的入口

| 当前源码 | 证据与影响 |
| --- | --- |
| RoleService.editUniqueCode:180附近 | dataScope空白时补DEFAULT_DATA_SCOPE=0000；DataPermissionEnumm明确0000=ALL。缺值从“无明确范围”变全部权限，是安全问题，不能当普通缺省展示 |
| 前端role/aoeform.tsx:49、service.ts:52/107附近 | 初始化、saveRole和normalizeRole均有0000兜底；后端即使不再补值，前端仍可能把未知值包装成显式ALL提交。必须同批去掉伪造默认，不只修后端一处 |
| MybatisPlusDataPermissionHandler | ALL提前返回1=1；0001/2/3/7路径生成CREATE_ID/MODIFY_ID条件；0004/5/6枚举虽存在但switch未实现，最终拒绝。日志表没有CREATE_ID/MODIFY_ID，受限查询会生成无效列SQL；管理员ALL路径可能掩盖此缺陷 |
| BusinessLogMapper.xml | 列表、count和详情均读LOG_BIZ/LOG_BIZ_ARCHIVE；当前含TENANT_ID、USER_ID，语义是操作用户。热表/归档两支及count/get必须一致保护，不能只修list |
| PostgresBusinessLogRepository.currentTenantId | standalone固定0；普通租户强制本tenant；平台管理员targetTenant=null当前表示不加tenant条件。不能把tenantId=null当普通调用者默认，也不借本次修复增加新跨租户入口 |
| PostgresErrorIncidentRepository | get/list用MyBatis查询LOG_ERROR并显式tenant过滤；ErrorIncident按tenant+fingerprint聚合，无稳定USER_ID/部门/业务对象归属，LOGIN_USER文本不能当不可变所有者 |
| ResourceAuthorization/RoleResourceCacheService | 当前多角色/多资源是任意匹配即允许，*方法及旧宽路径可覆盖精确资源；加精确资源不产生“默认拒绝”优先级，不得据此宣称逐操作权限已收敛 |

### 9.2 可在原接口下直接修复的安全守卫

以下是具体修复建议，生产写者由总控派发，本成员不改代码；不需为每次普通防错修复重复询问用户，也不能借此扩大授权。

1. **停止自动授予ALL**：新增角色缺dataScope或仅空白时返回参数校验失败，不写角色/资源/用户关系；不自动改为0000或0007。字段存在但非有效枚举值、组织参数格式非法也拒绝。可保留API路径、字段名及Response形状，错误用现有参数错误契约。
2. **兼容旧编辑缺字段**：更新角色时，在可信schema/同一事务中读原记录；dataScope未传/null按“未修改”保留原dataScope及dataOrgIds，不能经过默认转换改变范围。显式空字符串/空白拒绝，不支持用空值清除范围。原值本来null/非法时仍保留其原拒绝效果，不能因只改角色名就提升ALL；修复此脏值必须明确提交合法范围。
3. **前端不得合成权限值**：移除表单初始化、normalizeRole、saveRole的0000兜底；缺值显示待选择/不可保存，新建必选；原记录有范围则回填原值，旧别名dataPermissionType仅在有真实值且与新字段无冲突时兼容。不能用缺失DATA_SCOPE字典强塞ALL；字典加载失败明确失败并保留输入，用户没看见的默认不能当授权意图。
4. **保留明确已有ALL**：有权限的角色管理者明确提交原本合法的0000仍按既有契约处理；本补丁不批量改已有ALL、不改变多角色范围并集规则、不凭空新增“最多等于操作者范围”算法。更严格的授予上限属于后续明确权限政策，不隐式推行。
5. **未实现岗位范围保持拒绝**：0004/5/6不再作为“支持且可新授”的选项；新建/切换到这些未实现范围明确拒绝，运行已有此值继续不放行。编辑其他字段且未传scope可原值保留；恢复/实现岗位范围另有验证与决策，不转换ALL或本人范围蒙混过关。
6. **日志查询先保护再取数**：在BusinessLogRepository的list/get与ErrorIncidentRepository的list/get查询入口复核可信身份与现有读取权限上下文；未登录/范围缺失/未知/当前不支持的受限日志策略直接明确拒绝，不跑会引用CREATE_ID的SQL，不用“查询成功但空列表”掩盖拒绝。保留已经明确ALL且现有日志资源许可的查询行为及其已有tenant约束；不为修SQL自动授角色ALL。
7. **数据拦截器失败闭合兜底**：针对日志查询的明确表名/statement识别“不适用CREATE_ID模型”的情况，未建立等价显式范围前返回拒绝条件或在查询入口拒绝；不在全局excludeTables、整个Mapper类或全局@Ignore上放行。修复范围仅查询路径，不阻断匿名错误记录、业务日志保存或后台归档调度。COUNT、热表/归档UNION、详情必须相同拒绝/过滤，不能总数泄露。

0003指定部门输入必须至少有合法且本租户授权可选的组织ID；拒绝非法/跨tenant/部分非法后静默丢弃的输入。更新时未修改范围而缺orgIds应保留旧值，不能把不相关编辑变成空范围或ALL。真正切换范围后的用户权限版本与缓存按现有机制失效，失败事务不改变权限快照。

### 9.3 日志可见性的最小推荐与需确认语义

推荐第一阶段：**业务日志与错误日志先只向“已有日志读取资源许可+可信有效ALL范围”的用户提供现有tenant边界内的数据；所有受限范围明确拒绝，解决SQL错误同时保持失败闭合。** 这可作为当前缺乏策略时的安全guard，不扩大任何先前可读集合。长期是否让受限用户看本人/部门日志，必须明确选择后再实现；不为消错把日志整体Ignore。

| 日志/范围 | 可复用字段与推荐语义 | 确认边界 |
| --- | --- | --- |
| LOG_BIZ本人范围0007 | 若批准，按TENANT_ID+USER_ID=可信操作者ID看“自己发起的操作”，不按可改operator名称，也不把B_ID关联任意业务表 | 日志操作人范围不等于“我能看其业务对象的数据”；选哪一种须确认，不能静默当同义词 |
| LOG_BIZ部门0001/2/3 | 若批准按“操作人当前所属部门”筛选，可以在可信tenant账户/资料/组织表中求操作者集合；资料缺失/跨tenant/被删除身份不能回退全部 | 调岗后历史日志会跟随当前部门，这是产品语义变化；若要按发生时部门，当前无事件部门快照，需要新增字段和以后记录策略，不能补造旧历史归属 |
| LOG_BIZ无USER_ID旧行/匿名行 | 无法可靠映射个人或部门，受限用户一律不命中；ALL在既定tenant边界可查 | 不按operator文本匹配账号、不将无值视为当前用户；旧日志修复不自动回填 |
| LOG_ERROR聚合事件 | 推荐仍仅日志read资源+ALL的tenant审计视角；受限用户拒绝，不强行映射本人 | 聚合一行可能含多个用户的发生事实，LOGIN_USER不是安全分隔。要让普通用户看本人错误需事件粒度所有者/单独脱敏视图，属于新契约，当前不实现 |
| 平台跨tenant日志 | 保留当前已批准平台管理员路径，不新增target自由透传；需要进一步收窄到显式目标/能力则列单独权限变更 | 普通tenant参数不能选择别的tenant；是否允许平台无target看所有tenant应由政策确认，不借修SQL扩大或撤销既有明确平台权限 |

若批准本人/部门策略，最小实现把已验证的操作者ID/受限集合传入现有Mapper，在热表和归档每个分支、COUNT、get同加`tenant AND scope AND 用户筛选条件`；用户传的operator/时间/mCode仅能缩小结果。只有这些显式条件与回归完整后，才可窄到具体读取statement豁免通用CREATE_ID过滤；绝不能先Ignore、后补scope。日志记录/查询若经core，操作者集合仍从对应可信tenant求，不在core用户同ID上误关联。

### 9.4 给总控的最小执行/决策清单

| 顺序 | 建议动作 | 是否需新增政策确认 |
| --- | --- | --- |
| P0 | 后端停止新建缺scope→ALL；编辑未传保留原范围；前端三处同步去ALL兜底；验证合法枚举与0003组织输入 | 原API可直接安全修复；不是授权改已有角色范围。若坚持新增角色必须有默认值，才需选默认政策，推荐无默认必选 |
| P0 | 日志受限/无身份查询入口明确拒绝；拦截器仅对相关日志查询防无效列，禁止全局Ignore；ALL保持现tenant/资源约束 | 失败闭合guard可直接修复；受限日志的实际可见范围仍待下一项确认 |
| P1 | 选择业务日志受限策略：维持仅ALL审计，或本人=操作者USER_ID、部门=操作者当前部门 | 只需确认这组日志归属语义；不新增框架。推荐先仅ALL，后按明确需求开放受限视图 |
| P1 | 错误聚合日志是否维持仅ALL；若要个人视图另定义事件所有权与脱敏 | 推荐维持，仅在用户要求个人视图时另审新契约/DDL |
| P1 | 是否实施岗位0004/5/6；在此之前新授拒绝、已有继续拒绝 | 岗位范围实现/默认改写需明确业务规则，不借故障修复自动扩权 |
| P1 | 列出受限测试角色实际命中的旧*宽授权，提出精确撤销差异并确认 | 真实角色/tenant/套餐授权变更需具体确认；不改matcher的“任一允许”语义，不因新精确资源存在就声称负向权限测试通过 |

最低验证矩阵：新增null/空白/非法scope零写；编辑遗漏scope和orgIds保留原0000/0001/0003/null且不扩大；前端读/存未知值不合成ALL；0003跨租户/空组织拒绝；0004/5/6新授失败、旧值继续拒绝；有效ALL与受限多角色并集语义保持；日志ALL/受限/匿名、热表/归档/count/get一致、无CREATE_ID SQL错/无总数泄露、输入targetTenant不能越权；缺USER_ID旧日志不归本人；错误聚合不伪按LOGIN_USER分割；保存/归档后台任务不被查询guard误伤；角色更新permVer/缓存与事务回滚正常。权限负向运行验收须有真实无宽授权的受限测试身份，不用管理员成功冒充隔离通过。

本节仅新增待修与决策清单，未实现或测试上述guard；没有连接DB、运行SQL、启动服务、改生产文件、授权数据、编译测试、提交或push。
