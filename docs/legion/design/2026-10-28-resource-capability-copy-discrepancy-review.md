# e03d398d 编译副本差异独立复核

当前状态（后续收口更新）：本轮后端隔离验证已经 QA 独核通过。以下前期「CP/AP待补」「整体待验收」为当时状态，已由文末收口更新取代；动态 HTTP、前端及 UI 不在此次通过范围内。

2026-10-28，technical_designer，按总控派发只读复核。RUN 为 `.gradle/resource-capability-runs/20261028T145038Z-e03d398d`。本轮没有运行辅助、PLAN、构建、测试或服务，没有修改、补回副本或改写 RUN 证据；仅写本设计记录。

## 结论

现有证据不足以认定编译时确实缺 class，也不足以认定 QA 的 92 项记录已经得到解释。应保留「三阶段 runner PASSED、六项测试零失败/错误/跳过」事实，同时将独立整体验收保持未完成，等待 QA 核对枚举算法及原始失败观察。不得猜测系统写者、称文件已恢复或直接用当前状态覆盖原失败记录。

## 当前独立观察

QA `audit/qa-independent-postcheck.json` 记录 sys-domain/auth 编译副本 expected93/current92，唯一 missing 为 `org/pkaq/core/auth/authentication/provider/JwtUsernamePasswordAuthenticationFilter.class`，并明确后续 CP/AP 检查未完成。

本轮实际 GetChildItem/GetItem/GetFileHash 读取该副本得到 93 项，该文件存在，7788 bytes，SHA256 `D0BB2D77F4432BB6AE2AB0F6F49FB1D530FB6AB6D234CDFCF62D838DBB849A6D` 与复制审计一致；其文件 CreationTimeUtc/LastWriteTimeUtc 均显示 14:52:56。文件时间只作属性记录，不能据此证明完整历史或写者。

继而只读逐一枚举全部 55 份 main `*-copy.json` 的目标目录，比较完整相对路径集合及每文件 SHA：本轮 missing/extra/changed 均为零。该结论只描述当前读取状态，不追溯消除 QA 的先前观察。

## 生命周期与编译影响

init 第246—265行按 `compile-inputs/<target projectKey>/<dependency projectKey>/classes` 创建副本，每个 target/dependency 独占路径；doFirst 内复制后记录 before/after/copy 三表并要求相等。该对象审计中的三表各93项且含上述 class。

查询本 RUN 三阶段所有实际 CP/AP 报告，该目录仅出现在 main 的 `eva-web-sys-domain-compileJava-classpath.json`，种类为 directory。后续 sys-service、web-booter 从各依赖的主输出复制至自己的独立路径；准备阶段和 Test 使用新 snapshot-C，不再使用 sys-domain 的 auth 副本。init 没有声明后续任务清理或重写这个已完成副本的操作，也没有证据显示实际发生过此类操作。

因此，该目录确为 sys-domain 编译的真实输入，不能视为无关缓存。若能证实 class 在编译消费期间缺失/变化，则是编译输入完整性风险；现有 doFirst 记录只能证明编译前检查时完整，无法证明整个编译消费区间不变。当前未发现实际编译故障，不能将风险写成已发生的错误或推断六项测试结果失效。

现 helper 的 main_outputs/snapshot 门禁封存产品 classes/resources 与 generated/header，没有对 compile-inputs 全树做编译后/阶段后持续封存；实际 CP 对目录只记录路径和 kind，sha256 为 null。这是现有验证覆盖缺口。此前静态终审确认了复制当时三表一致，但没有要求副本的任务后和阶段后再核；本次观察明确了应补的门禁范围。

## 最小后续方案

1. 当前 RUN 保持只读，不恢复、不清理、不重基线。先由 QA 对照原枚举代码、失败时原始清单/错误与当前文件集合，区分算法/读取错误和真实状态差异；无法区分就保留「差异原因未明」。当前93项不能单独解释先前92项。
2. 经授权后窄补未来 helper/init：保存复制审计原 expected；每个 compileJava 的任务后再次核其所有实际副本键集合与 SHA，编译失败也收集检查结果；Python 主阶段结束时再核全部复制审计及目标，不仅核产品输出。
3. 若验收要求证据目录一直保持完整，则将上述复制清单并入跨阶段冻结控制，在后续阶段前后及最终交付前比较，不允许重新生成 expected。首次差异保存 missing/extra/changed 与阶段/任务标识并失败停止，不自动补文件或重试。
4. 未来新门禁不能倒填到 e03d398d 历史区间。本 RUN 仍需补完 QA 尚未完成的实际 CP/AP 后验后，结合差异解释单独决策是否验收；无需为此抹去已有六项通过事实，也不得直接提升为整体绿。

本方案只涉及验证辅助和验收记录，不要求改生产代码、扩展测试范围、引入进程观察/新依赖或重跑当前 RUN；任何实际修改或新执行须总控另行派发。

## 追加：普通 Python 路径漏读独立复现

收到 backend 的 Windows 长路径诊断后，本角色仅运行独立只读 Python 片段，未导入或执行任何辅助。对同一争议文件，普通 pathlib 路径长度260，read_bytes 报 FileNotFoundError/errno2，is_file 返回 false；仅加 Windows 扩展路径前缀后，读取成功，7788 bytes，SHA D0BB2D77F4432BB6AE2AB0F6F49FB1D530FB6AB6D234CDFCF62D838DBB849A6D 与原审计相同。此次异常的 winerror 为 null，不套用 backend 其它 stat 调用所报 winerror3。

这证明本机存在无需恢复文件即可复现的普通 Python 路径漏读，可以解释为何不同读取方式观察到92/93；原 QA 枚举实现尚未由本角色审读，不能直接宣称已证明其具体失败路径。此前「当前差异原因未明」现收窄为「已复现读取方式差异，原 QA 路径尚待对应确认」，不应继续将当前观测描述为已经发生文件丢失或改写。

最小门禁应同时处理读取可靠性：先按逻辑规范路径校验确在 RUN 内，再以扩展路径做 Windows 文件 IO；清单键仍用规范相对路径。枚举后每项 stat/read 失败必须显式报错，禁止用 is_file=false 静默丢弃不可读项。既有复制前后检查与上述任务后/阶段后封存建议保留；尚未完成的 QA CP/AP 独核仍须补完，六项通过与整体待验收结论不变。

## 收口更新：QA 独核已补齐并通过

总控同步 QA 已完成剩余独核；本角色只读查看新增 `audit/qa-corrected-longpath-postcheck.json` 与 `audit/qa-longpath-boundary-copy-confirmation.json`，状态分别为 QA_CORRECTED_CURRENT_POSTCHECK_PASSED、ALL_55_COPIES_BOUNDARY_CHECKED_AND_PASSED。原失败 JSON 保留未覆盖。

QA 在 resolve 后严格限定本 RUN，再以扩展路径核全部55副本、2197项源/目标 SHA、17份 CP/AP、三份实际测试源、JDK、主产物及辅助产物；结合原已核530主产物、690输入、资源 NO-SOURCE、214外部 jar 和六个方法完成独核。四个普通长路径漏项已复现，未恢复文件或推定写者。

因此，本轮后端能力字段隔离验证已收口通过，不再保留「CP/AP 待补」或本轮「整体待验收」待办。未来 Windows IO 与副本持续封存属于辅助门禁改进建议，当前不改工具、不重跑。动态 HTTP、最终前端实现及 UI/跨作者交互仍须各自验证；等待前端最终源码交审，无业务源码写入授权。
