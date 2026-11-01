# 字典归属、备注与开关待办收口

2026-10-31，technical_designer；仅现源码、任务卡及既有设计只读核查。500 Modal及28项/真实取消操作验收由总控转达，本轮不重审、不重跑。

结论：删除“字典归属/备注开关契约待确认”这一笼统阻塞。两内置顶级分类、系统全只读、租户业务自治、standalone管理员维护已是明确政策；主要剩余项是未实现和未核实的数据映射，不应重问这些业务原则。备注没有另一个布尔“备注开关”字段，须分成主项remark回读缺字段与frozen启停守卫两个具体任务。

## 已定政策及实际差距

| 范围 | 当前源码事实 | 收口与最小修复 |
| --- | --- | --- |
| 两类归属 | DictEntity只有type/code/name，继承StdEntity也无pid；DictAoeBo/DictViewVo无父分类，type默认code；listDict返回平铺主项且附lines，前端list也是平表 | 系统/业务两根与可展开Table已定，无需问是否分两类。复用既有设计中已核开发库PID；接入真实父归属、只允许业务类新建、list返回两根children/具体主项，lines仍是右侧明细。type不可兼作归属。新增量版本只对实际缺列环境补，禁止重跑V1或跨schema搬数据。 |
| 主项写权限 | edit/del仅ensureDictEditable按主frozen==9999拒；新建没有有效管理员资格校验；switchFrozen遇9999跳过，其余按0/1切换 | 系统只读不能仅靠9999或资源门禁推断；按服务端真实父类拒全部系统主项及根的edit/del/switch。业务写在资源许可之外加明确有效身份与本源资格：standalone有效管理员；schema仅可信本租户获授维护主体。GET旧删除与POST删除、批量switch同守卫；无需重新选择系统能否编辑。 |
| 明细写权限 | 只有主表edit的lines差量保存；diffSaveItems对9999明细仅保留frozen，仍可更新值/排序，且提交时遗漏该行会deleteById；delDict直接删整组明细。前端linelist只检查主表isReadOnly，不检查单行9999 | 系统主项所有明细拒增改删；业务主项内9999明细也须服务端拒修改/排序/移除，前端对应禁动作。未知外mainId的line.id不得默认为新行。此为只读语义实现缺口，不需用户重复批准。 |
| 物理来源 | DictService读写均@TenantSchema；路由search_path是tenant,core。无系统core+业务tenant的显式聚合、分类来源/逐表namespace校验；缓存key当前只有schema::code，无context降为裸code | schema模式已批不等于字典分源已实现。沿既有设计显式core系统只读、本tenant业务可写，缺表/错源拒绝，不靠search_path回退写core；query/fetch/缓存旁路接同一归属，身份来源与路由finally恢复。standalone留配置源，不移到core。本项不借平台0认证另案放行。 |
| 主项备注 | SYS_DICT.REMARK及StdEntity.remark真实存在，DictAoeBo有remark，MapStruct入参可写；DictViewVo无remark，因此list/get无法回读，前端主表Form有name=remark | 最小为DictViewVo新增String remark并验证entityToVo回读，前端不用造字段或开关；这是响应增加已有业务字段，不需DB列/迁移。源层修复方案交总控确认范围后实施，不向用户重问是否存在备注。 |
| 明细备注 | SYS_DICT_ITEM.REMARK、DictLineBo/Vo及MapStruct链均真实存在；当前三mode Modal均用无name的文本Form.Item呈remark，未提供编辑输入 | 已验收的备注展示保持。不存在必须新增的remarkEnabled/description开关，不应凭旧备注待定字样扩大可编辑范围；要让明细备注变可写才是额外产品变化，当前无需提出该选择。 |
| 启停 | frozen语义0正常/1停用/9999只读，selectDict/query/validate只排1；switch API存在，UI主表当前无启停输入 | 无需再确认0/1/9999含义。系统分类禁止切换；业务范围仅沿现启停契约保护。不能把9999自动转换为1导致系统字典不可用于业务选择，也不能以业务分类取消既有行9999保护。不为清待办新增前端开关。 |

后端接口入口为DictCtrl的/list、/get/{id}、/get/type/{code}、/edit、GET及POST /del/{id}、/switch；资源权限与通用数据范围不能替代上述常驻分类/资格守卫。当前未访问数据库，不能从源码宣称现开发系统字典已全为9999或两根已恢复。

## 确实剩余的决定与实施前核验

旧10/18设计只将未知既有QA主项 `2111791956297613314` 的系统/业务归属列为证据不足项，历史七组有系统根路径证据。总控当前再次明确的两类政策并没有自动给每条无证据历史数据分类；若后续任务/已批准清单已明确该QA归属，直接采用，不重问。若仍没有，执行前仅问“该未分类QA主项归业务类，还是本次排除？”即可；也可以先排除它交付已证部分，不能仅凭名称写库。无需重新询问系统/业务模式、所有系统是否只读或租户是否自治。

其余不是新的业务选择：现库根/父路径、各主项前像、真实源及代码冲突需执行前fresh只读核验；旧设计快照和旧默认ROLLBACK SQL已过时，尤其DATA_SCOPE已由独立R包恢复，禁止原样照搬“保持其deleted1”。根据fresh清单给最小具体数据变更/回退再批准执行，不把未知现状升级为新产品问题，不自动恢复删除记录、改9999或建索引/约束。

总控可将清单改为三个可执行项：“字典两类真实归属与系统拒写/租户业务守卫待实现（政策已定；仅未知历史条目另定）”“主项DictViewVo.remark回读缺字段待修”“schema字典显式分源与归属缓存待实现”。明细统一Modal及备注只读展示从待确认清单关闭；启停归入分类守卫实现，不保留独立笼统契约阻塞。

当前只提出准确契约增量：主项BO/VO增加真实pid（根标识的提交须服务端核验）、VO children用于既定两根树表、DictViewVo.remark回读已有列；数据库仅按实际环境复用PID或单独新增量，不改SYS_DICT_ITEM表名，不新增kind/remark开关，不自动跨schema迁移。若总控批准实施，应先按写入范围定稿字段和数据映射，禁止先改接口后让前端猜归属。本文不授权任何生产或数据操作。
