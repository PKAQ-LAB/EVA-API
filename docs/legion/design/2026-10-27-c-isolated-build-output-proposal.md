# C验证专用输出隔离提案（只读设计，未实施）

2026-10-27，technical_designer。依据backend当前status、C实施附件及冻结的C/旧helper、现项目构建声明；未改helper/构建配置/IDE/源码或运行Gradle。

## 1. 建议与证据边界

建议另建**验证专用init与调度helper**，只把该次Gradle进程的所有Project输出绑定到全新run目录，在同一run的三个独立串行Gradle进程中复用该绑定。继续原十模块main→原获准test准备→精确Test的流程，输出/清单/复制源/classpath全面脱离各项目标准build目录。旧snapshot-d、旧C helper、所有失败run不改、不清理；不继续在共享build路径盲跑。

df529796已证common先被多个消费者完整读取81类，auth前sourceReadable=true却before/after/copy均0；说明现场共享源目录为空，不证明Project.copy漏复制或具体PID写者。暂关autobuild仍复现、设置已恢复，不能把隔离输出当“已查明IDE根因”。此方案绕开共享产物消费并保留失败闭合，不承诺未知外部写者绝不触及新run；真相仍以执行证据为准。

可覆盖十模块main/test的**路径空间**；这不等于授权执行十模块全部测试。当前C helper实际只准备/筛选SYS两个新Test，原其他回归是否并入仍由总控固定清单，不以隔离名义扩筛选。所有十模块main和test路径都应重定向，即使某模块当轮没有test任务执行，也不能留下共享输出后门。

## 2. 为什么不能只改一个buildDirectory

当前 `.gradle/C-20261027-verify.gradle` 明确硬编码：compile拷贝源为item.file(build/classes/java/main)、protectedMain含标准classes/resources、test类/资源允许路径仍为标准build；Python的main_outputs也读标准build，test准备与结束又调用旧OLD.test_outputs读SYS/AUTH/DATA共享目录。只移Gradle目录而保这些函数会继续取live旧输出、误删classpath条目或出现空清单。

因此最小完整改动是**新验证helper/init中的路径模型**，不是生产构建声明：统一取得本run的Project buildDirectory、SourceSet实际输出与任务目的地，所有compile-copy、清单、封存、testClassesDirs、Test classpath与报告路径依此模型。不得调用旧sealed_main/main_outputs/test_outputs来读取共享目录。旧code_inputs/sha等纯输入哈希逻辑可按固定版本只读复用；旧d仅读取已封存副本与manifest，不遍历当前标准build来作基线。

## 3. 具体路径与绑定时机

候选新根为本仓 `.gradle/C-isolated-runs/<UTC-UUID>/`，需总控批准新helper写入范围后才创建。下列路径为提案，不是已存在产物：

| 项目 | 计划路径 |
| --- | --- |
| 每Project buildDirectory | `<RUN>/products/<完整project path分段>/build`；root用_root，父项目同样绑定 |
| 项目缓存 | `<RUN>/project-cache`，用验证进程专属project-cache-dir，不覆盖工作区默认.gradle任务历史 |
| compile暂存依赖 | `<RUN>/compile-inputs/<消费者完整path>/<依赖完整path>/<输出标识>` |
| main封存 | `<RUN>/snapshot-C/<已批准模块标识>/{classes,resources}`，一旦封存不覆写 |
| 日志/图/路径模型/清单 | `<RUN>/audit/<phase>/…`，新文件独占创建 |
| test报告/临时文件 | 默认随重定向buildDirectory；每个Test的实际目录另核在RUN内 |

固定十模块：core-contract、core-common、core-cache、core-log、core-data-mybatis、web-core、web-auth、core-upload、sys-domain、sys-service，完整Gradle path沿现helper固定名单。禁止从磁盘动态猜模块或自动扩大依赖闭包。

绑定须早于任何项目脚本/插件配置：在新init的projectsLoaded时，对rootProject.allprojects逐一设置layout.buildDirectory为上述完整path对应值；不能等projectsEvaluated才改。根脚本会在subprojects闭包中给子项目应用Java插件，过晚覆盖可能已经留下旧路径。若属性已finalize、插件提前缓存了绝对标准build路径，立即失败报告，不以改生产脚本兜底。

三阶段命令须传同一个RUN/init/参数，保持现离线、no-daemon、no-parallel、no-configure-on-demand、max-workers=1及原task/filter/exclude。建议本次验证再显式no-build-cache、no-configuration-cache及独立project-cache-dir，避免复用共享任务历史/配置缓存；不关state tracking、不doNotTrackState、不clean、不删共享产物、不换依赖版本/Java工具链。是否接受这些验证进程选项由总控审新方案，不持久化gradle.properties。

不能仅给单一compile任务设置destinationDirectory，因为processResources、annotation processor、test与报告仍可写旧路径。反之全Project布局重定向不授权执行非十模块任务：实际图仍按原白名单拒其他项目、jar/bootJar、publish/sign、启动/IDE等任务。

## 4. 必须核实的实际输出与插件边界

当前项目脚本所见没有生产侧硬编码build/classes/resources；硬编码主要在验证helper。root应用Java库/发布/签名及依赖管理，MapStruct/Lombok是注解处理器，当前十模块并无自定义重写JavaCompile输出的已见配置。不能只凭文本无命中就称全部插件路径已随布局生效。

新init必须在配置完成后、任何准备任务执行前写出并检查实际模型：

- 十模块main/test SourceSet.output.classesDirs及resourcesDir；compileJava/compileTestJava.destinationDirectory；processResources/processTestResources.destinationDir。
- 编译generated annotation sources、声明的header输出、incremental/tmp与每个允许任务的outputs；MapStruct生成UserConvertImpl必须来自本run，不从live生成源/旧classes拼凑。
- Test.testClassesDirs、classpath、workingDir、binaryResults、JUnit XML/HTML report、task temporaryDir；若workingDir沿原项目目录，测试不得以此隐式读build或写项目路径，当前两新C测试未见标准build路径硬编码。没有理由为隔离改测试断言/SQL/fixture。
- root及隐式父Project的临时/报告输出也要核，特别Gradle problems报告，不能只检查十个叶模块而让根build报告继续写live目录。
- Java Library的project依赖编译/运行variant、SourceSet输出及任何Jar artifact引用；Spring Boot/Java/发布生成的jar/bootJar/sourcesJar/javadocJar/sign输出虽预计跟随layout，任务图仍不允许执行，不需读签名材料或触发布。
- sourceSets/IDE/Eclipse/JDT/annotation processing扩展若缓存了绝对旧路径，或任何included build、自定义生成任务/未知输出出现，先停报具体路径/任务，不改IDE、不重启语言服务、不扩buildSrc/插件。

不得把模型检查实现为配置时提前解析testRuntimeClasspath；现C首轮已经证明Gradle9.5.1拒这种时机。保持FileCollection/Provider惰性，实际classpath在允许任务执行前按原门禁核；若新模型需要未知任务才能解析，报告图差异，不加入producer来“修通”。

## 5. 依赖与classpath保持

生产依赖声明/api-implementation边界、sourceSets源目录、processorPath、工具链、编译参数、Mapper注册与Test框架不变。新init只把既有验证产物引用换为本run对应输出，不向Maven仓库发布/安装新jar，不把旧internal jar当新编译替代，不新增implementation依赖。既有C验证的compile暂存classpath前置策略如保留，必须相同模块/顺序/内容并改为本run源，不能扩大可见模块；此仍是已明示的验证专用classpath策略，不宣称纯原生发布构建已验。

每个compile执行前实际classpath分类检查：本仓项目产物只能来自本RUN的输出/已核compile暂存；外部依赖只沿现离线缓存和固定artifact版本/hash。任何本仓标准build、旧run/snapshot、旧internal模块jar、IDE输出路径混入，立即失败；不能靠前置新classes遮盖同时存在的旧module资源。annotationProcessorPath也按同样原则核身份，不把外部库缓存误当live模块。

Test只允许封存十模块main classes/resources、该run已准备的指定test类/资源与原外部依赖。原internal jar/项目main输出条目按固定模块身份替换为snapshot，避免重复User.xml/common.xml；不得粗删所有org.pkaq依赖、整包外部资源或用资源扫描排序来掩盖冲突。缺少尚未创建的隔离jar也不能从旧build/libs补齐；任务图/variant不符时停报，是否允许增加本run打包任务须另审，不能改变当前-x jar和Test-only约束。

新Python输出清单必须读实际新路径模型，不读OLD.test_outputs；test准备和结束只封锁本次批准的目标module测试产物。十模块所有SourceSet的test目的地重定向可核实，但未执行test准备的模块输出应标明未生成，不把它们伪称测试通过。

## 6. 三阶段与失败闭合门禁

1. 准备前锁当前源码/测试/构建配置/helper/init哈希和明确不存在候选，保存只读旧d manifest/hash；RUN全新、路径规范化后在批准根内、无链接外跳，与live build互不包含。禁止从live build作为本轮输入或拷贝源。
2. main阶段只执行原十模块classes及其原compile/processResources，图禁Test/jar/发布/其它项目。每个compile的已准备依赖来自隔离路径，复制前后源与副本全清单相等；不因隔离而移除before/after门禁。
3. main后生成新清单，与旧不可变d比较，预期仅User.xml资源为获准C源哈希；其它class/resource差异停报。全新输出若暴露旧d残留类或非确定性差异，也必须具体分类再审，不自动把差异当“干净构建合理”接受。封存snapshot-C后不覆盖。
4. test准备仅按原批准compileTestJava/processTestResources/testClasses目标，真实annotation processing与测试编译全部落RUN。前后main/source输入不变，封存本次测试输出；不读共享testClasses。
5. Test阶段图仍只允许原精确Test任务，无compile/process/copy/jar生产者；每次doFirst和结束核snapshot/main/test/source输入与external jar哈希。Test计数来自当轮实际suite/XML，零测试/异常skip不作通过；失败日志/图/清单完整保留，不自动重试新run或覆写证据。

路径模型和实际图检查可能需要执行后才能证明Gradle插件完全遵循布局；本提案不执行help/dry-run/任务图。待新helper/init静态审与总控授唯一槽后，首轮一旦发现越界路径/不在白名单任务立即停止，不把隔离设计当已生效事实。

## 7. 为何仍能验证真实auto-config与反例

移动的是类/资源输出位置，不改Java/XML/配置或classloader资源相对名称。新SYS test classpath保真实当前MP Boot4 auto、MybatisPlusConfig与同版本外部库；main snapshot中仍各一份mapper/User.xml/common.xml，旧反例仍在test fixtures，不覆盖正常mapper扫描目录。Test框架/JDBC精确白名单/BeanDefinition安全门禁不变，不能为适应目录取消它们。

NORMAL仍由真实resolver自然枚举新classpath，不sort/预parse；因目录根变化资源顺序可能不同，必须记录该实际顺序，不能要求它与live index170/189完全相同才能测。独立User-first/common-first/legacy反例继续显式强制已定义顺序，验证根因对顺序不再敏感；旧反例内容/当前16映射/SQL/handler与六消费者原规则检查仍有效。隔离首轮若旧反例不能复现，报告实际statement，不放宽断言。

User/common唯一资源数量、最终MappedStatement来源/BoundSql、13/16/9映射与真实插件/同事务mock测试由实际新测试结果证明。该方案能检验C的生产自动配置组件路径，但**仍不是live IDE classpath或服务加载验收**。禁止把验证snapshot回拷到live build、供服务热替换、改launch.json/settings或重启用户服务；之后部署/实际factory/UI由总控另定。

## 8. 可审执行包与限制

建议总控下一步只授权新独立helper/init准备和静态审：全Project早期layout绑定、十模块原图、基于SourceSet的独立路径清单、三阶段及原哈希门禁；不授权立即跑Gradle、修改生产构建文件、IDE设置、服务或数据。新辅助必须默认PLAN_ONLY且不写目录/不执行Gradle；显式execute才进入已批准RUN，不能无参数自动修旧路径。旧helper全部封存不改。

本设计可消除现已知“验证仍依赖共享输出”的路径，但无法证明外部写者身份，也无法在静态阶段保证全部插件/variant已重定位。若有硬编码输出确需改生产构建或改变任务/依赖语义，停在该具体项另审，不通过禁tracking/清缓存/跳模块来缩短过程。

本轮只读status、C附件与helper及相关构建路径/依赖声明，写本设计和本人status。未改源码/配置/helper/IDE/服务、未运行任何Gradle/编译/测试/DB/UI/HTTP或发布操作；没有新验证结果。

backend同项目只读交接与本方案一致：业务构建脚本未见显式输出硬编码，不等插件已证明迟绑定；旧C硬编码源/允许根须一起替换，OLD.test_outputs等不得续用。该交接不是新执行或目录重定位生效证明。

## 9. 新隔离辅助独立静态审查（未执行）

初读Python SHA8596837C64CE8C1EFC5D3EFC375171D0F256BD07766F807EC08B3492645FBB71、init B905456A6D564BFF8C9F37F520EEC7B08155DC684D9F5747E5661E3F040C32A7，与派工相同。期间backend修正自身main.resources门禁后，本轮最终实读init为 **A0BD3342E2A553CBC96B585103C43138BB6A8457754918402D96DFF71EABC63D**，Python仍8596837…；以下结论针对这组冻结字节。

### 9.1 已确认与提案一致的部分

projectsLoaded在项目脚本前给root/allprojects完整path绑定RUN并disallowChanges，核project-cache-dir/includedBuilds/路径碰撞；projectsEvaluated逐Project核布局、SourceSet/compile/generated/header/tmp/test工作及报告/archive/outgoing variant。sourceElements仅精确匹配原SourceSet声明源根，不能借该例外进入实际compile/Test classpath。未见调用clean/Delete/doNotTrackState、关tracking或修改生产依赖配置/IDE/服务的路径。

实际classpath的files解析在compile/Test doFirst，未见配置期直接解析configuration的compile/testRuntimeClasspath；配置期outgoing.artifact.file读取仍需首轮证明当前插件API可用，不当作已执行成功。旧C的live build硬编码已移除：main/test清单读取新model；只复用旧reference.code_inputs纯输入锁，不调用旧sealed_main/test_outputs。Python默认PLAN在execute分支前返回，不导入旧reference、不创建RUN或启动Gradle；本角色没有执行PLAN。

compile与Test剔除固定十模块旧jar，拒本仓file/非jar、org.pkaq缓存artifact及非RUN/非精确允许目录；未见把旧模块jar替成不同外部库的逻辑。仍保原固定模块copy前置顺序，不新增生产依赖。模型覆盖root/父/叶，任务图只允许原十模块准备任务或单SYS Test，root problems记录预期目录且Python核实际日志URI；预期不是实际报告已写入RUN的证明。

静态复核旧文件现哈希：C Python=C06A8B3A…、C init=A053111F…、reference=0A85F5E2…、旧current init=DF6C0C52…、旧phase guard=68417F27…、d manifest=D87ED7CC…，与既有冻结记录一致。本轮仅读，不覆写或重新封旧d。

### 9.2 已修的确定误报

初版init允许compileTestJava目录仅main.classes/copies，漏了目标自身SourceSet.main.resources。Gradle9.5.1安装JavaPlugin字节码确认test compileClasspath由mainOutput+testCompileClasspath组成，合法SYS资源目录会被误报Unsealed。backend仅加四行：compileTestJava且own main.resources非null时经inside RUN后精确加入。A0BD版实际已含该修正；不宽放整个RUN，此项关闭。

### 9.3 尚需修正：外部JAR跨阶段没有累计锁

**结论暂阻塞执行，需这一项窄修后重新冻结审阅。** Python external_inputs（155–167行）只把当前阶段doFirst记录的sha与该阶段结束时实际文件比较，270行只保存各阶段verified-external-jar-sha256.json；run_verification循环没有跨阶段的external锁或对已见JAR的前置复核。

具体情形：main结束后某同路径外部缓存JAR由A变成B，test-classes或Test doFirst用B计算新sha，阶段结束仍B；当前helper会分别接受main的A与后阶段的B。source/config/main输出封存都可不变，依然缺少“main编译与后阶段使用同一已锁依赖字节”的保证。阶段内复核不能替代阶段间锁，不把这项当未知运行API。

最小修复仅helper：维护累计 `canonical external path → 首次sha`；每阶段启动前复核此前见过的全部文件，阶段结束取得该阶段记录时重复path必须同sha，仅首次出现的测试专用依赖可加入累计集，结束再复核累计集。保存每阶段累计锁及新增项，不能每阶段重建baseline。无需改依赖/任务/测试/生产域，不必扩大为全缓存扫描；只锁本次实际classpath/processor已出现的文件。该修正仍由获锁backend实施，本角色不改辅助。

### 9.4 运行前明确未证项

修正后也只能给受控首轮条件结论：真实Gradle9 Provider/variant模型是否可取、资源空目录/插件是否全部跟随layout、实际Test与processor classpath是否没有旧路径、fresh编译是否与d仅差C资源，均未运行证明。若失败保日志停报，不加入未知任务或旧jar兜底。

root problems当前只有预期路径模型和结束日志URI校验；若初始化在projectsLoaded绑定前失败，不能预先声称报告已隔离，更不能把事后越界报错说成未发生过写入。需按首轮实际报告位置记录判断，不自动续跑；未看到实际新运行，不能认定必然越界或必然安全。

不需要为这些运行未知项重构插件或扩大测试框架。当前执行意见为：main.resources误报已关，累计external锁尚待修；完成后由QA/总控结合最终冻结字节决定唯一槽。没有运行新helper/PLAN/Gradle、编译、测试、DB、UI或服务，也未更改辅助/源/配置。

## 10. 累计external锁修后终审：可申请一次隔离run

本轮实读最终Python SHA **3C944ECBA933068FF58E0513C894D40711D43BB5D741E2252621306E0A44561E**，init仍 **A0BD3342E2A553CBC96B585103C43138BB6A8457754918402D96DFF71EABC63D**，与总控新派工一致。第9.3节缺口已关闭，旧8596837版的暂阻塞意见不再代表本版本。

verify_external_lock遍历先前累计路径并复核文件/sha；merge_external_lock先检查所有同路径旧sha，不允许重基线，再仅登记首次见的新路径并复核全锁。external_lock只在run循环外初始化一次；每阶段启动前验证，结束后合并并写当阶段累计JSON，最终完成前再验证。本轮仅读源码；backend报告的合成临时文件/函数验证是开发侧证据，本角色未执行或冒充独立测试。

结合已关闭的compileTestJava自身main.resources误报，本组合**未见剩余确定静态阻塞，可由总控在独占槽批准一次以验证门禁为目的的隔离run**。此为有条件执行建议，不是helper已实际运行、Gradle模型已兼容、C测试已通过或live服务已加载。保持原固定任务/筛选、三阶段、输出RUN、旧d比较及所有拒绝门禁，不增生产依赖/任务，不自动重试。

运行前核两辅助最终SHA、当前批准源/tests/构建输入、旧冻结辅助/d manifest仍同，使用全新RUN；不改IDE、服务、生产构建配置或标准build。运行未知仍按第9.4节留证：布局/variant/provider API实际值、classpath与资源唯一性、fresh字节与d差异、root problems实际位置。任何不符立即停并保失败log/图/清单，不删门禁、补旧jar/共享产物或把布局预期称为实际零越界。若需helper调整，另列最小差异重冻，不沿本次建议连续盲跑。

本轮仅本人设计与status追加，无新辅助/PLAN/Gradle执行、编译测试、DB、UI或服务动作。最终执行许可与独立QA交总控，静态审查不代替后续实际结果。

## 11. 首次配置失败后的单点Groovy语法复核

总控交接首次隔离run：main配置约22秒报init221 String.positive，无taskgraph/path-model/编译/Test；该轮实际Problems报告已证位于RUN/products/_root/build/reports/problems。它只补足这一次报告位置证据，不证明准备图或C测试通过，也不能外推所有更早失败的报告路径。

实际原220–221行在collectMany闭包内，classes表达式结束换行，下一裸行首为`+ [resources]`，Groovy可将其解析为单独unary positive表达式。backend获准仅把二元+前移；本角色实读最终init SHA **E1BA9AC37BBDDB2D718C9C35CF778903FECBC927CD55B181333A16F702434EF5**，现为classes+换行resources。内存中仅将这一个运算符移回原行，重算全文SHA精确恢复 **A0BD3342E2A553CBC96B585103C43138BB6A8457754918402D96DFF71EABC63D**，证明无其他名单、目录、过滤、任务或门禁差异。Python仍保持3C944ECB…最终版本。

相似行首+候选已只读逐处核：146位于entry.variants.add(...)尚未闭合调用括号，184位于inside(new File(...))参数括号，269位于allowedDirectories=(...)括号表达式；不具备本次裸语句换行边界，没有依据连带修改它们或扩大语法清理。本角色没有运行Groovy或Gradle来宣称这些候选已动态验证。

结论：此次单点修正与明确失败位置相符、表达式集合意图不变，未见阻止**再一次受控隔离配置验证**的确定静态问题。由总控指定新RUN/唯一槽及执行范围；若经原三阶段入口继续后续编译/Test，仍须原授权、固定图与全部哈希/路径门禁，不因本条静态意见自动扩大。再次失败即停报具体位置，不批量调整运算符或盲重试。

本轮仅只读辅助/哈希及本人设计/status追加，无辅助修改/执行、无Gradle/编译/测试/源/IDE/服务操作。
