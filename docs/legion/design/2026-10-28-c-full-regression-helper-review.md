# 固定20类隔离回归辅助静态审查

2026-10-28，technical_designer。范围仅新ignored Python/init与其调用的冻结输入门禁；未执行辅助、PLAN、Gradle、测试、IDE或服务操作，未修改源文件或辅助文件。

## 1. 最终身份与结论

- `.gradle/C-full-regression-20261028.py`：833A4293D895E9CE58608CA4A4D4E5F8CC95F2E721DA7B479DC506D33A17D359。
- `.gradle/C-full-regression-20261028.gradle`：4E0F05675184875E56984B575E42D1D7DB3DA3BEC233D9A90935565DF36093A2。
- 原C Python仍3C944ECBA933068FF58E0513C894D40711D43BB5D741E2252621306E0A44561E，init仍E1BA9AC37BBDDB2D718C9C35CF778903FECBC927CD55B181333A16F702434EF5。两个新增文件均由git check-ignore确认ignored。

未发现阻止一次受控QA独立运行的静态问题。可交总控按唯一执行槽授权QA一次全新RUN；不是编译、路径模型或20类动态通过结论。失败须保留证据停报，不自行扩大过滤、补旧jar或追加运行。

## 2. 精确范围与任务图

实读新旧两文件完整差异，并核Python/init两份过滤集合均20且差异0。按每个当前源码文件的路径、package及class声明逐项核实：SYS11、AUTH6、DATA3；原18类清单保持，仅增加C的Bootstrap/Consumer两类。没有通配模式、Integration或全仓Test入口。

commands保持main十模块；准备阶段分别请求三个模块compileTestJava强制执行、processTestResources、testClasses，排除test；最终每个指定Test紧跟其自身--rerun及精确--tests列表，并排除六类producer。init还为每个Test设置相同精确include集合。图门禁要求最终任务集合恰好三个指定Test，无额外任务；main和准备阶段仍只允许原十模块内既定任务类型，测试编译/资源/classes仅放行三个目标模块。实际Gradle图仍须由新RUN证明。

## 3. 三模块SourceSet与测试classpath

所有Project在projectsLoaded先绑定RUN/products/完整project路径/build并disallowChanges，project-cache独占；SourceSet、compile/generated/header/tmp、Test类目录/报告/工作目录、archive及variant模型沿原严格校验。Test的testClassesDirs必须与其所在项目test SourceSet一致，不拿SYS类目录替AUTH/DATA。

准备产物按模块名分别封存；三个模块各自必须有正的classes产物，每个Test执行前重核全部三份准备清单。Python在执行前后亦核三模块全部内容。suite、实际CP审计及XML副本均按模块名隔离，避免覆盖。main snapshot仍固定十模块，Test使用snapshot20个类/资源目录及自己模块的test roots，加实际外部依赖，不引入其它模块测试类目录。

已核当前SYS存在src/test/resources，而AUTH/DATA均不存在。新逻辑从原Test CP精确移除自己testModel声明的classes/resources根，惰性加回其中实际存在者。省略范围仅这两个已声明的own roots；未以整个RUN作为目录白名单，未静默过滤其它缺失路径。缺失classes仍由准备阶段正产物和执行前内容锁拒绝；未知目录/缺失路径仍由checkClasspath拒绝。当前单classes根模型又禁止多根重复身份。因此该适配符合NO-SOURCE测试资源情形，不需创建假资源目录或放宽通用路径检查。

## 4. 输入、封存及结果计数

新inputs在原源码/配置/缺失候选门禁外额外锁原C辅助；新辅助自身也入锁。逐项复算C26成功run锁定清单中的非presence内容键，当前差异0，未据此冒称所有presence或历史run内容均已重新核验。旧d snapshot仍只读核manifest及逐文件内容，新main只准User.xml的批准差异。输出/source资源一致、隔离副本、generated/header、测试产物、外部jar跨阶段累计锁及失败后核验保持；未调用旧reference的产物或测试读取函数。

verify_results按三模块各自读取本RUN实际suite和XML，类集合必须精确相等且无重复、每类tests大于0、模块计数与实际suite相等，fail/error/skip均0且suite SUCCESS；总数仅加本次三个模块。不预填203，不累计历史177或26。全新RUN、拒覆盖写入及独立模块报告路径共同隔离旧XML。最终QA应独立核实际任务图、CP、prepared清单、suite/XML、累计外部锁和三阶段gate，而非只看COMPLETED文本。

未证明事项：Groovy与Gradle9实际求值、真实SourceSet/variant/processor/Test CP模型及任务调度、三模块编译和测试执行。隔离合成夹具通过也不代表真实数据库、运行中factory、HTTP/UI或权限矩阵验收完成。
