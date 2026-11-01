# 字典B精确映射执行版独审准备

同步后版本边界（2026-10-31）：总控交回锁并报告后端快进064ed4f9，Flyway升级13.12.0、Minio9.0.4，本地候选输入保留。原B66及基于13.11.1的工具/依赖证据仅历史，不能当新HEAD全绿或新维护工具已验。实际执行版必须以新版本可用本地依赖封存classpath/工具入口及V14校验链，缺依赖停止，不自行下载/构建/执行测试。mapping仍8SYSTEM+QA BUSINESS，DDL/DML尚未批准。前端已完成测试不重复。

2026-10-31 technical_designer。总控转达用户明确批准8条内置主项SYSTEM、QA主项BUSINESS，其余38条不动。该归属决定已关闭，不再提UNKNOWN选择；实际DB执行尚未批准。当前仅旧只读清单及源码预核，待backend可执行版定稿，不代表执行方案通过。

分类技术前像依据backend单次RO文件，非本角色重新查询：有效9条、非有效38条、category列不存在、DATA_SCOPE当前deleted0且8明细中5有效；本角色不重算或披露正文。不把旧10/18 deleted快照作为现值。

## 执行版审查要点

1. 正式迁移只能走实际Flyway及真实history，不直接SQL加列后伪造V14历史，不repair/baseline掩盖不匹配，不删历史或手动造checksum。批准目标应fresh核schema/history/checksum/pending集合；只有明确验证仅V14待执行才继续，缺history/旧迁移异常或其它pending即停止另报，不顺带重跑V1或补13个版本。
2. 源码application-dev.yaml当前flyway.enabled=false，prod亦false；dev同时保baseline-on-migrate=true、locations迁移目录。默认关闭不证明运行绑定必关；必须核批准命令及实际配置/环境覆盖，防新产物启动意外执行pending。未获部署/启动授权不启动新产物，不以源码包含V14当执行授权。
3. Flyway执行V14与外部9row DML不能默认视为同一事务。优先在明确获批的字典维护停用/禁止并发写与迁移窗口内先正式V14、再条件化单事务分类，全部后验后才启用新版；若要求真正原子必须提供受支持执行链及事务证据，不能把dev QA ID塞进通用V14。具体停用方式由总控决定并纳入一次执行批准，不自行杀服务或改共享配置。
4. DDL前像列不存在，已存在列/约束或V14异常历史即拒漂移；正式V14成功后schema/category全NULL为中间态，分类未成功必须继续封闭管理入口，不能上线报完成。
5. 分类DML在固定eva.sys_dict，仅精确八ID/code/type/frozen/deleted预像与QA同样限定；只改CATEGORY，影响行数恰9、8 SYSTEM/1 BUSINESS，其余38全部NULL原样。新主项/新增有效行/前像任一变化立即全批回滚，不用upsert或按code批量推断。
6. 写窗口fresh备份、前后完整不变检查需覆盖主47条除CATEGORY外所有列及全部明细、相关授权表（只需数量/稳定摘要，不输出正文），不是只type/frozen几个字段或仅9行计数。保护DATA_SCOPE恢复、deleted/frozen/PID/PATH/revision/remark等原值；只快照摘要可核不变，但精确回退还需真实目标字段前像和受控备份。
7. 锁和隔离要明确覆盖并发新行/明细/授权漂移，锁超时停止，不无限等；不要以REPEATABLE READ快照独自声称防写入竞态。批准窗口中不运行其它DDL/分类、UI保存或缓存重建操作。
8. 失败回退先阻止使用分类版的读写，再按批准后像条件仅恢复9个CATEGORY至原NULL；不得覆盖字段或误回收样本/角色。已正式成功V14的列/约束/history默认保留，不手动删列同时留下成功history。若要求物理退回无列须另有正式前向补偿迁移及应用依赖退回方案，不在此次顺手执行。真正恢复旧部署必须先核旧代码不依赖新字段、停用窗口未解除，不丢用户草稿。
9. 运行启用前要求新schema版本与9分类完整性均通过，后端/前端新冻结源及QA证据一致；真实页面验收另在已授窗口进行，不以数据写入代替能力/只读运行验收。分类不改codeKV，原缓存不全清，不因SYSTEM标记顺便改9999或授权限。

已向backend说明这些预审边界，等待其具体SQL/命令、备份路径与哈希、fresh停止条件和回退依赖顺序再独审。执行前由root一次询问实际DB写/必要窗口，不重复询问映射与业务规则。本角色仅design/status，无DB/测试/HTTP/服务/源操作，C3skip、C和平台0另案。

## 两份离线SQL首审

已完整读取backend `sql/2026-10-31-dictionary-b-category-forward-proposal.sql` 与rollback同名前缀文件。forward固定eva、47行ID/code/type/frozen/deleted且category全NULL、批准8SYSTEM+QA BUSINESS、表锁与9行锁、全主项非category逐行指纹、明细及11项保护集合count+digest，实际UPDATE RETURNING必须9行再COMMIT。rollback以实际receipt对9ID/已批分类/完整非分类指纹比较，只把9category复NULL，新增业务行及其它列保持，不drop列或手改history。当前未发现DML逻辑阻断，不等于整套可执行方案批准。

待补齐的具体执行门禁：psql的copy输出固定路径会覆盖已有文件，注释“不得覆盖”不构成运行检查；实际runner必须用新RUN绝对目录且存在即拒，保存并验证receipt恰9项及SHA，再允许COMMIT。当前SQL末尾直接COMMIT，外层若还没做到提交前停点/验证就不能声称已有该能力。出错或连接断开不能据receipt存在推断已提交，须有提交结果和独立后验。

SQL内检查V14 success/script/latest仅是额外数据前置，不替官方Flyway validate/checksum/info。实际合法工具入口、固定classpath/脚本校验、仅pending14、目标指纹及正式migrate target14仍须具体化；当前PATH无CLI不得编造命令可用或启动应用代维护。停写窗口/备份与回退操作顺序文件未齐，等待最终执行runner与文档后统一审查。用户mapping已定不重问，实际DB执行仍未批准，本角色无执行。
