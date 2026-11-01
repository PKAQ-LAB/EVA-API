# 同一remote dev：仅本次前像的备用入口（待实际范围批准）

目标仍为原已审remote dev eva/eva，非prod，dev/prod区分和固定目标指纹d22bc534e71c不变。用户确认相关写入已停止，但尚未批准本条“仅结构及九行前像、非全库备份”的替代范围。本文件及入口仅SOURCE准备，不授权连接、DDL/DML、下载、dump或服务操作。

新入口`sql/dev_category_scoped_preimage.py`显式标记SCOPED_PREIMAGE_ONLY_NOT_FULL_BACKUP，默认plan无连接。旧C302入口、10ABB seal、E486清单和全部旧SQL/证据完全不改，也不伪造BACKUP_VALIDATED。运行时从已核SHA的旧入口仅复用目标解析、源seal/14保护、连接/锁、独占fsync、九行和约束读取函数，不调用其backup/mutate或Flyway。

持锁后重新核47技术前像、缺少CATEGORY和完整六项约束，独占保存本次九条原NULL分类、字段不存在事实、完整约束目录及47技术字段，fsync并重读SHA。此材料没有账号口令/PII、明细业务值、日志正文或全库数据，不能用于全库恢复。经外部新seal绑定的旧forward SQL仅作三个精确文本替换：专用scope批准值、scoped_preimage_sha256会话键以及对应错误描述；原backup会话键不设置，不绕过旧runner门禁。其余所有事务/目标/版本/47前像/摘要/触发器与六约束守卫均保留。

DDL和九分类仍在同一SERIALIZABLE事务。真实七约束及九后像、前像路径/SHA写入prepared并fsync，再记录UNKNOWN提交意图后commit。提交错误不主动rollback推断结果、不重试；提交成功后凭证落盘错误保留CONFIRMED。提交前失败可由事务回滚新增字段/check和九分类。提交后的另批rollback仅从SCOPED_COMMIT_CONFIRMED链核对完整七约束/九后像并把九分类恢复NULL，保留新增nullable字段/check，不drop，不声称恢复完整原schema或全库。已确认的38历史项、账号/角色/授权/样本/日志/history都不主动修改。

最小执行形式（现在不得执行）：

```text
python docs/legion/design/sql/dev_category_scoped_preimage.py
python docs/legion/design/sql/dev_category_scoped_preimage.py --apply --execute-approved --scoped-preimage-approved --writers-stopped-confirmed --runtime-verified --window-id <本条实际已批窗口> --seal-sha <新scope seal SHA>
```

两个操作方确认flag仍是总控核事实后的交接，不能当自动运行态证明。必须同时取得用户对此备份范围变化的明确批准及QA增量SOURCE通过，才允许一次实际apply；随后在同一实际授权范围内用既有只读流程一次后验。rollback还需另批编号及该scope的实际已提交receipt/SHA。当前不下载工具、不dump、不建或repair历史、不重跑已绿套件/主题。C尚未完成，不能据此宣称EVA整体完成。
