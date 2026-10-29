# 能力响应隔离辅助终审

2026-10-28，technical_designer。按总控派发独立只读审查新辅助、继承的参考核验函数及六份已审 Java 输入；本轮仅写本人设计/status，不运行辅助入口、PLAN、Gradle、测试、observer、PG、DB、HTTP、服务或 UI，不修改源码、配置、辅助或 Git 状态。

## 结论与身份

静态审查未发现阻止一次受控执行的问题，可交总控决定另授执行。该结论不代表 Groovy 配置、编译、资源任务或六项测试已经通过。

本轮实算 SHA256：

- `.gradle/resource-capability-20261028.py`：D74ECC1F59E84C2D7F1AAD8BA2D84213014CF6E3A27988F2FC8C117EDEE46937。
- `.gradle/resource-capability-20261028.gradle`：C07632D979BF45D73AE702576E2FAF1E663CA6D8411ED715521AAF965F305BB6。

六份生产/测试输入 SHA 均与辅助 SOURCE_SHAS 及前次源码独审一致。690 条输入及 closure `46D24A2EF9ADE779CDB519C7D49980522168462C5209240F13AE6826866E99A4` 是实现方默认 PLAN 的报告值，本轮未调用入口重新计算完整 closure；实读确认入口使用原 685 精确映射叠加显式批准差异后，要求键数 690 且整表相等，未改成目录通配或只验新增文件。

## 编译、资源与测试闭包

11 main 模块与继承 helper.MODULES 一致，包含 web-booter 的主产物但不执行其 Test。19 项目 buildDirectory 在 projectsLoaded 阶段设至独占 RUN 并 disallowChanges，模型逐项校验 SourceSet、生成源、header、临时目录、测试报告/工作目录、archive/outgoing 输出；源码 outgoing 仅按实际 SourceSet 声明根分类。included builds 拒绝。

主编译按固定顺序依赖本 RUN 先编模块，复制记录源前/源后/目标清单且三者相等、非空；实际编译 CP 只接受声明的本 RUN 主 classes/复制目录与外部 jar，拒绝项目内 jar、内部缓存 jar、未知目录及缺失路径；AP 不接受目录。编译器元数据须 JDK25 且安装根为指定 zulu25。后验主产物精确 530 项：旧 529 加 DTO，仅 handler/AuthCtrl 两个旧 class 必须变化，其余 527 项及 generated/header 保持原 SHA。后续 snapshot 仅从本 RUN 主产物逐文件独占复制，复制前后及目标 SHA 相等。

准备阶段图精确 COMMON/AUTH/SYS 各 compileTestJava、processTestResources，共六任务，排除 main producers、jar、Test/testClasses。每个 compileTestJava.setSource 只指定该模块一份已审测试 Java，执行前再核实际 compile.source.files；CP 精确新 snapshot 的 22 个主根及外部 jar，AP 仅外部 jar。输出反查每模块唯一指定 class 加原生资源集合，额外隐式编译 class、生成源/header 均使门禁失败。

资源模型取各模块实际 SourceSet.test.resources 和 processTestResources.source.files，核唯一源根、重复目标、源路径/SHA及 RUN 内目标；三阶段模型要求一致。afterTask 记录实际 noSource/skipped/failure，准备后要求 noSource 与该模块资源空集合对应。当前文件系统静态候选 COMMON0、AUTH0、SYS1，SYS 的 fixtures/user-before-c.xml 实算 BFC0F6B427D65F8E1F62D3D5364FECDD2F181DBC05C3AC40729BD0144A5F24EF；候选 1/1/2 共四项不能替代未来真实 SourceSet、资源任务状态和产物核验。

执行阶段图精确三个原生 Test，无 producer；每任务一份精确 class filter，CP 为新 snapshot、该模块新 prepared 与外部 jar，仅省略该模块确实不存在的资源根。执行前核 snapshot/三个 prepared 及输入，执行后重核其字节。JDK launcher 安装身份固定，单 fork、JUnit 并行关闭。

继承 verify_results 拒绝多余/重复/缺失 suite、零用例、失败/错误/跳过及 suite/XML 计数不一致；新 exact_methods 进一步要求三个类的方法名集合及长度精确为 1+3+2 六项。已对照测试源码中的六个 @Test 方法，未见额外测试类或匿名类编译闭包。

## 封存及执行边界

原 external214 只作累计锁起点，每阶段前核旧锁、实际 CP/AP 文件记录 SHA，阶段后合并首次见 jar 并复核全部累计锁，已见项漂移不得重新基线化。工具、参考证据、输入/presence 和 phase-controls 在各阶段及任务执行前复核；Python 另封 phase-controls 本身 SHA。新 main/snapshot/prepared 的跨阶段前后字节门禁保留，错误路径写 gate，不重试、不下载、不扩白名单。

错误处理是遇到首个门禁错误即停止：若工具/输入/外部依赖或模型已先失败，不能声称随后所有产物后验都已完成；辅助没有把这种退出标为 PASSED。正常 Gradle 非零退出仍先走已封主产物/测试产物检查，再报告失败。BaseException 或进程中断同样不能视为完整后验。

静态调用链中，新入口只借用旧 API 的参考核验/清单函数，没有调用旧执行入口、pg_preflight 或 observer。实现方所报 AST、默认 PLAN 和十项内存检查未在本轮重复；真实隔离 RUN、Groovy/编译 CP/AP、native NO-SOURCE、六项 fresh XML 均待另授后执行。旧 219、旧 PG11 与本轮结果不得拼数；当前结论不涉及前端实现、真实 HTTP 或 UI 验收。
