# 字典最小契约实施提案（待确认增量）

最新批准状态（2026-10-31，总控转达用户明确同意）：A/B/C分阶段政策与本文具体契约已批准，B两个响应布尔增量也已按write-capability-addendum批准，不再重复询问。A新1191CA15冻结版已独审及QA独立41项通过，作为B源码基线。B当前仅standalone实施：CATEGORY、两展示虚根、平数组及能力响应、真实同源资格与系统全入口拒写；C的core/私有分源与v2缓存后续独立推进。本文末尾旧“待确认”措辞保留提案历史，现以上述批准记录为准。

实际旧数据分类映射清单、DDL/DML执行目标与迁移仍另批，不从type/code或QA命名推分类，不自动执行任何生产/开发数据库写。两个布尔仅响应计算，不新增数据库列、不接受BO回传，后端操作时重验。B实现以同日B-checklist及write-capability-addendum最新批准记录共同约束，C3skip、平台0认证另案。

2026-10-31 technical_designer。既定政策不重问；本文仅提实施契约，未修改源、数据库、缓存、授权或服务，未运行测试。已验收的500 Modal不动；C3、平台0独立认证另案。

## 一、推荐固定展示虚根，不恢复持久树

仅有SYSTEM/系统字典、BUSINESS/业务字典两个固定分类，不需要任意父子树。推荐前端Table本地生成两条不可选作具体字典、不可增删改的虚根，children为后端真实分类后的主项；明细仍在右侧。虚根ID使用仅前端的 `category:SYSTEM`、`category:BUSINESS`，不提交Long ID接口，也不作为字典code进入query/cache。后端不新增根记录、不要求pid/path/isleaf，不恢复旧已删除根、不创建4080业务根；存量数据库若有历史pid/path保持原值但不用于新权限判定。该方案替代10/18持久根候选，不是已批准执行。

主表SYS_DICT仅新增 `CATEGORY VARCHAR(16)`，值只允许 `SYSTEM`、`BUSINESS`；迁移过渡可NULL，NULL即未分类，不是业务默认值。具体定义CHECK(category IS NULL OR category IN ('SYSTEM','BUSINESS'))；不新增kind、parentId、备注开关或树表，不改SYS_DICT_ITEM名/结构。新插入业务行由服务端强制BUSINESS；旧请求省略category可兼容新建，若提供SYSTEM或未知值拒绝。更新按真实数据库category核验，客户端省略则保持，提供与原值不符则拒绝；不得让客户端把系统转业务。系统种子仅随明确增量版本维护，无产品写入口。

`DictEntity`、`DictAoeBo`、`DictViewVo`增加String category，合法值服务端统一验证。`/list`仍返回原 `Response<List<DictViewVo>>`，每条主项仅增加category和remark，原id/code/name/type/frozen/sort/lines保留；前端按category组成两根，不要求后端children响应或将Long id改类型。虚根无明细，新增只在业务根触发。`type`仍保留旧含义，不从type=code推系统/业务。

## 二、可先完成的无DDL修复

1. `DictViewVo`增加 `String remark`（主备注已有SYS_DICT.REMARK和StdEntity/DictAoeBo.remark），MapStruct同名自动映射；补entityToVo及list/get回读验证。无需改表、入参或URL；前端原name=remark即可得到已存值。响应新增可选字段，旧客户端可忽略；不新增审计字段。空备注按现JSON策略，不造默认文案。
2. 修复既有行9999保护：`diffSaveItems`先对完整提交做预检，旧9999明细仅允许原字段完全不变并保留；改keyName/keyValue/orders/frozen/remark或遗漏该行均拒整个事务。main只读、删除主项（包含9999明细）、批量switch都先完整校验再写，拒绝不能静默跳过。显式line.id不存在于当前main的旧集合则拒绝，真正新增不带id。业务行9999不可由客户端降级；新建业务主/明细只收0或1，9999仅可信种子维护。前端只对该行禁编辑/删除，不能误禁整张业务字典；保持现查看Modal与备注文本只读。

这两步不依赖CATEGORY迁移，可独立交付和必要定向验证；属于执行已定只读政策及字段回读，不需重新询问产品原则，但仍由总控派源码写者。不会使尚未分类系统行自动只读，后续分类守卫仍必须实施。

## 三、常驻权限与全入口

SYSTEM主项及其全部明细对所有产品用户拒绝新增、编辑、删除、排序、启停；已有系统行frozen0也不能写。分类守卫优先，9999只作为兼容标志，不把9999改1使其退出可用字典。两虚根根本不进入后端写接口。BUSINESS写必须同时有真实有效维护主体、本源归属和操作资源；standalone核有效ROLE_ADMIN账号关系，schema核可信本租户有效维护角色/资源关系，不凭rolesMap字符串、ALL或客户端tenant字段。资源开关关闭也不能撤销分类/身份守卫。

所有写入口覆盖POST /edit、GET旧删除和POST /del/{id}、POST /switch及服务内部调用；当前没有独立line写API，全部由edit.lines归口。业务code改名同时校验唯一性及系统保留code。每批先完成身份、分类、源、版本与所有行预检，遇不存在/未分类/外源/只读混入整批拒绝，不能先处理一部分再跳过。业务主项删除时有只读明细也拒绝。冻结1保持原“不可用于选值”语义，不自动等同不可维护。

## 四、物理源、标识与重码契约

standalone始终使用配置standaloneSchema同一张主/明细表，category区分两类；不改数据源。schema正租户：系统只从配置core的SYSTEM行读取，业务只从可信resolver选定当前私有schema的BUSINESS行读取，显式两次受控查询合并；写只当前业务源。核每张实际relation namespace，缺表/缺分类版本/错上下文拒绝，禁止tenant,core search_path回落；finally恢复原上下文。core的业务行或私有SYSTEM遗留行不自动纳入、不猜归属、不复制或移动。平台0读取不为此修改认证resolver，另案处理。

为避免core和tenant碰巧同Long id，`get/{id}`增加可选query `category`；明确SYSTEM只去core，BUSINESS只去本tenant。旧省略category只在两个合法源恰有一个匹配ID时兼容返回；两个均匹配拒绝歧义，不能任选。前端新请求传真实分类。写默认BUSINESS且仅查本业务源，即使另一core恰同ID也无权写系统；若明确传SYSTEM拒绝。主ID始终Long，不增加客户端schema/owner参数或targetTenant。

`query/{code}`、validate、fetchDicts继续旧code键契约，推荐系统code为保留命名，禁止新增/改名业务code与core SYSTEM重名。既有重名不自动覆盖或给租户优先：该code解析与整份聚合遇冲突明确失败，待具体数据清单处理；其它独立code查询可继续。系统和业务都不会被最后put覆盖，不能用category新增列掩盖原值差异。此前全源有效code唯一索引保留；新私有业务不得因另一源相同code绕唯一性。此“冲突拒绝”是本次需确认的兼容行为变化。

## 五、缓存与种子

缓存名称仍dictdata，改用显式owner参数生成key `v2::<实际schema>::<SYSTEM或BUSINESS>::<code>`；standalone也带真实schema和分类。系统缓存可在同core内共享，业务只本tenant，未知owner不得回退裸code。新路径不读取旧裸code或schema::code键，不全局清Redis；旧键留待独立精确清单清理。getAll/removeAll必须限定本次获准owner，不再无context扫描或删除全缓存。提交后只刷新当前owner的新旧code，回滚不刷新，保留外层context；分类迁移完成后以新namespace自然冷加载。

`queryDict/selectDict/fetchDicts/validate`与 `AuthCtrl.fetchDicts`使用同一个明确来源解析；`JacksonCodeSerializer`当前直接cache.get(codeKey)，须转为有可信请求主体的统一只读解析，不依赖已恢复的TenantContext。无法确定来源保留原code不跨租户猜值；不得把个人SELF裁剪结果填共享缓存。只有精确字典读取方法在已核身份/源/分类前提下取得共享字典语义，不全局Ignore/ALL。DictInit在schema启动不扫描租户、无可信owner不加载/清除共享缓存。

core系统分类及内置值由受审核心增量种子维护；新tenant增量只建/升级其业务表CATEGORY及版本记录，不复制核心系统字典，新业务分类为空集合也合法。`TenantSchemaProvisioner`现按V1/V2账本执行，应增加后续独立版本入口及对应tenant-migration脚本，不重跑V1或批量遍历所有私有schema。每个已有tenant只有在自身获准升级/映射后启用，不自动改变用户值。

## 六、旧数据与分步验收

先fresh只读列清当前批准目标的主项ID/code/deleted/frozen/type、主明细摘要、既有PID/表列/版本及重码情况，以证据逐项映射SYSTEM/BUSINESS，另列历史根/旧伪明细容器。禁止所有type=code推分类，也不将QA样本默认系统或业务。历史deleted保持；只加category的映射不能夹带恢复删除/改9999/重写PID或明细。未知行保持NULL及原值，写拒绝；不能藏入业务根、生成第三根或静默丢行。

分类版管理列表仅在批准范围内全部有效具体主项已有明确映射、历史容器已明确排除且冲突已处理后启用。存在未知则报告未分类前置不满足，保留原产物，不能部署后偷偷少显示字典。无需强制一次升级所有tenant；不满足条件的环境/tenant保持旧部署，不声称已完成新政策。存量迁移须备份每条原值与增量列/版本，按实际修改条件回退；未知QA条目可以本批明确排除，但若仍有效且需启用同库列表，必须先由清单确认其分类或另定下线，不能假装满足门禁。

推荐三步：A无DDL remark回读与9999保护；B单一批准standalone目标category列/逐项映射/虚根UI/服务端分类与管理员守卫；C独立schema分源、版本种子、缓存及序列化旁路。每步必要验证分别覆盖字段回读/只读精确变更、两分类及恶意改类/批量事务、双tenant同ID同code/缺表回落/缓存隔离/路由恢复。无真实数据或schema环境证据时只报告已完成步，不以standalone测试代C通过。

## 七、拟动文件与批准语句

SYS-domain：DictAoeBo/DictViewVo/IDictService；SYS-service：DictEntity、DictService、DictConvert、DictMapper/DictItemMapper（必要专用读SQL）、DictCtrl、DictCacheHelper/DictInit、JacksonCodeSerializer；schema阶段另涉及TenantSchemaProvisioner及新的core/standalone和tenant增量脚本。修改AuthCtrl.fetchDicts只限来源调用，返回code→键值结构不变。前端dictionary的data/service/list/index/readonly/aoeform/linelist承接category、虚根与单行禁写；不改500Modal容器或全局主题。对应各层现有测试按步补必要行为覆盖，不建通用框架。

可一次确认的范围：**“批准按展示虚根SYSTEM/BUSINESS及SYS_DICT.CATEGORY分类实施，系统全只读、业务按既定身份本源维护；主项remark回读与9999保护先独立修复；列表保留平数组新增category/remark、get可选category消歧，同code冲突拒绝，schema系统core/业务私有及v2归属缓存按本文分步实施。存量只按fresh逐项归属和精确前像清单另批写入，不猜QA归属、不恢复删除、不迁移跨schema、不自动升级所有tenant或新增授权。”** 该批准不替代未知行的具体归属决定、每个实际数据写目标确认、运行启停或平台0认证契约批准。
