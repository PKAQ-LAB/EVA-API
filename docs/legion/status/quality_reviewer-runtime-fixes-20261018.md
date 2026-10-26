# RESOURCE-CTRL-RECONCILE-ERROR-20261018 独立源码QA

状态：本次6生产+4新增测试范围通过，无本次新增P0/P1/P2。当前应用进程未重启，本卡不表示真实HTTP/浏览器/数据库运行修复生效；原runtime7问题不因此整体关闭。仅自己此状态子卡写入，其他文件只读。

范围核对：LogQueryBo begin/end仅加yyyy-MM-dd DateTimeFormat，与现DateRangeBo做法一致；字段/查询契约未改。Module.xml实际diff仅selectModuleMapList BaseResultMap→现有FlatResultMap，原ModuleDetailColumns含route/icon、DELETED/name/id/pid/code过滤与SORT顺序均原样；FlatResultMap无递归children query。ModuleService.list仍toDetailMap→可选handleFetchResource→一次TreeHelper.buildTree；资源与getModule详情转换未改，消除了先递归通用子节点再组完整详情子节点的重复来源，不手工过滤后代或修改授权SQL。

ModuleAoeBo与OrganizationAoeBo冻结转换仅具体方法改BaseEnum.of业务code，生成实现确为BaseEnum.of，不再values()[code]；实际FrozenEnumm顺序FROZEN1/UN_FROZEN0/READ_ONLY9999保持，BaseEnum null返回null且未知码抛既有异常。实体→视图、全局enum和其它领域Convert未修改。两Service普通edit在查询origin并通过ensureEditable后setFrozen(origin.getFrozen())，同父及迁父后续均沿同一对象；不能用普通编辑绕过显式switch冻结。switch仍仅接受0/1、跳过READ_ONLY、父冻结时不允许对子单独解冻，cascade SQL仍排除9999；现有编辑/删除只读保护未挪除。模块编辑原注释已经约定冻结由switch维护，最小改动符合此边界。

强制独立测试命令：gradle.bat :eva-core:eva-core-log:test --rerun --tests org.pkaq.core.log.bo.LogQueryBoTest :eva-web:eva-web-sys:eva-web-sys-service:test --rerun --tests org.pkaq.sys.module.mapper.ModuleListMappingTest --tests org.pkaq.sys.TreeFrozenMappingTest --tests org.pkaq.sys.TreeFrozenEditTest --tests org.pkaq.sys.module.service.ModuleServiceTest --tests org.pkaq.sys.WriteProtectionServiceTest --offline --no-daemon --console=plain --continue。exit0/BUILD SUCCESSFUL in15s、24tasks/2executed22up-to-date；两个test任务均真实执行，必要生产编译为up-to-date，不冒称重新编译或全仓测试。

实际XML2026-10-18T12:55:32至35Z计数：Date2/MyBatis1/frozenMapping2/frozenEdit2/ModuleService4/WriteProtection4，共15，failures/errors/skipped全0。Date为真实Spring DataBinder+DefaultFormattingConversionService，接受当前日期/非法输入报错；MyBatis真实XMLMapperBuilder检查flat结果、无nested query及route/icon列；真实生成MapStruct两对象覆盖0/1/9999/null；服务使用Mapper/Event mock验证普通编辑保持原状态、模块显式冻结仍调用cascade，旧模块详情资源/不存在/参数及只读保护继续通过。没有SpringBootTest/真实数据库写入。diffcheckexit0仅换行提示，JVMsharing/Gradle10兼容与versions插件ID弃用警告保留。

图覆盖限制：开发报告ModuleConvert CRITICAL28、OrganizationConvert HIGH13、OrganizationService CRITICAL9，旧图落后21commit；本次不因UNKNOWN或测试通过降为全图低风险，以实际方法/caller/XML/生成实现与定向回归补证。当前生成UserConvert/RoleConvert/TenantConvert/TenantPackageConvert仍有FrozenEnumm.values()[code]，已独立只读确认，是明确存量同类风险，未扩大本任务修复。ModuleQueryBo未使用的转换也不趁机改。

资源设计仅文档审查：78=23旧菜单广域+54真实HTTP+1自建，28标准CRUD/26特殊HTTP及旧广域引用处理等待需求选择；文档批准计划add0/update0/delete0，不重放旧apply、不按文案批删或自动授权。本角色未访问DB/Redis复查数字，不能把文档历史统计当独立当前数据库验证。DictVO remark契约待用户确认，未加VO；岗位归属与资源语义亦未自行决策。

保护与交锁：cornerstone/HEAD6a0fee51b7e2582600a4621b4e8c9bf8495d2cc3，index空。Maven准备build.gradle/PROJECT_KNOWLEDGE、他人WebBooter、techdesign、GO与忽略目录备份均未改；原5自建对象不清理。检查XML汇总第一次命令因PowerShell不支持大括号路径而解析失败未执行，改成实际目录glob后已正常完成，不以失败检查充通过。

所有命令结束，后端源码锁交回总控；未跑前端检查、无浏览器/服务启动或重启、真实HTTP、SQL/Redis、依赖/vault/Git发布/branchstash或子代理操作。后续运行验收须由总控在用户自行重启应用后另派，不在旧进程上冒称新代码生效。
