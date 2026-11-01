# 字典B阶段实施检查清单

最新状态（2026-10-31）：A新1191CA15冻结版源码独审与QA独立41项已通过；总控已转达用户明确批准B两个响应布尔businessWritable/readOnly。B进入standalone源码实施，不再等待或重问字段/分类政策；C分源/cache后续，不抢跑。旧数据fresh映射与实际DDL/DML执行仍另批，本清单不是数据库执行授权。下文原“A通过后”前置现已满足。

冻结响应契约：list的data保持DictViewVo平数组，成功外层businessWritable为严格boolean；行及get的readOnly为严格boolean，与category共同表达有效只读；两位仅响应不入库/不进BO，操作时后端重验。category仅SYSTEM/BUSINESS，NULL过渡拒写。前端缺能力/分类保持旧平表可见全只读，不默认业务、不第三根。有效同源维护资格不得由ROLE快照/ALL/资源关闭替代；系统行全入口拒写，普通业务仍受9999保护。get可选category消歧、旧缺省仅唯一命中可回，重码拒绝不覆盖。

2026-10-31。总控已转达用户批准A/B/C分步契约，不重问分类政策；B仅在A独立审查通过后实施。本卡仅准备，无DB读取、分类写入或源码修改。

1. 固定两个前端虚根SYSTEM/BUSINESS，后端主项增加category及remark，list仍平数组；不新增持久根或pid/path树，不改已验500 Modal。
2. 拟新增SYS_DICT.CATEGORY VARCHAR(16)，仅SYSTEM/BUSINESS/过渡NULL，新增业务服务端定BUSINESS；更新查真实分类，拒伪SYSTEM/改类/未知值，NULL拒写。
3. 按明确配置standalone源及真实relation核定实施目标，不因同名表/现type/code猜源；schema阶段不提前并入B单库写。
4. fresh只读候选清单必须逐主项记录ID/code/既有deleted/frozen/type及真实历史PID/PATH证据，明细仅必要结构摘要；旧10/18七组历史系统路径可作为候选依据，不能当现值或自动DML。
5. QA主项没有可靠父关系证据，保持未分类候选，不凭QA命名或frozen0判业务；若其他批准清单已给归属则直接引用，不重复问。不存在的行不补建。
6. DATA_SCOPE恢复已有独立R包，旧deleted1快照已过时；任何分类更新都不夹带删除恢复、冻结改9999、重写PID/明细、授权或全缓存清理。
7. 历史根/伪明细容器单列，须按证据确认其不进入具体字典list/query/cache；不得把NULL隐入BUSINESS、造第三根或静默删掉有效未知行。
8. 分类版启用前，每条有效具体主项映射完整；未满足则保持原部署，不能把B部分SQL或缺列当成功。实际逐项CATEGORY前后像、目标与回退须另批后再写。
9. 全入口edit、旧GET/POST del、switch与服务内部调用按真实分类守卫；系统主/全部明细拒写，业务按有效管理员+操作资源，资源关闭不取消资格。A的9999精确保护继续保留。
10. 业务新建/改名code核系统保留code，旧冲突失败不覆盖；category相同/不同不取消现唯一性。type原义保留，不替代category。
11. 不将分类版响应附带ownerSchema或客户端targetTenant写参数；前端虚根ID不能进入Long接口。get可选category消歧按已批提案，旧缺省仅唯一命中可回。
12. A阶段remark回读与只读行保护结果作为基线，B只补分类/虚根/身份负例和旧客户端兼容必要验证；不因历史用例已绿而宣称新分类/真实数据迁移通过。

本卡不重复大方案批准问题，唯一可能业务缺项是具体未知历史行归属；由总控以fresh清单合并为一批决定。core系统/私有业务分源、v2归属缓存、Serializer与tenant版本仍是C独立门禁，不能B先开跨schema兜底。C3skip、平台0认证另案。

## B接线与缓存收口

管理list/get的分类及有效能力必须直接从可信单库主表/有效身份关系计算，不从dictdata推断。B分类映射不改变现code/明细键值，因此query/select/fetch/validate、DictCacheHelper、DictInit、JacksonCodeSerializer及现afterCommit定code刷新维持原单库契约，不为B临时将cache.get置null、返回原code或停用缓存刷新；这些会改变现业务显示与更新行为。C再独立升级归属缓存及旁路，不在B引第二套临时namespace或全局清键。

get可选category只收两值且与真实分类匹配；新增/更新由真实分类守卫。B单库ID唯一，旧GET/POST del/{id}不新增category参数；switch保留POST /switch及SingleArray.param集合，不造endpoint或客户端owner参数。前端不回传两个能力布尔，构造BO字段白名单；创建可省category，更新可传BUSINESS，不以回传值授权。

依赖核查记录：当前sys-service的build直接依赖不含auth，sys-domain仅api core-contract，web-core/common链也无auth；不能仅因booter同时装载就称已api依赖。若复用auth服务/SPI需要新依赖，应交精确最小方案核验，而非当既有依赖事实。资格SQL语义可在sys现层私有实现，不先造全局框架。该核查不改变已批资格/资源门禁政策。
