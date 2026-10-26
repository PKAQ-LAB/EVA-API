# LOCAL-GRADLE-NO-WRAPPER-20261019 独立配置QA

结论：本轮最小workspace本地Gradle配置与唯一ignored Wrapper属性删除通过；没有本范围新增P0/P1/P2。此结论不表示稳定JDT/ECJ blankfinal诊断已经修复，也不是应用调试/全仓构建通过。当前用户调试进程未操作，无UI/启动/停止/导入/clean/reload。

实际配置：workspace settings为onBuildFailureProceed=false、java.import.gradle.wrapper.enabled=false、java.import.gradle.home=C:/home/DevApp/gradle。与workspace-settings.before.json对照仅新增后两键，原Debug策略保持。独立读取已装redhat.java1.57.0 schema：wrapper.enabled boolean/window、home string/machine-overridable，故workspace home可覆盖User Pleiades latest；jvmArguments machine。User原-Dorg.gradle.parallel=false仍在，User home仍为原Pleiades路径，没有擅自改User值。实际Gradle3.18.1 dist/index.js使用getConfigJavaImportGradleHome/Version/WrapperEnabled组装配置，非猜测字段。匹配EVA window日志Code/logs/20261018T224657/window1/exthost/output_logging_20261018T231113/2-Gradle for Java.log第28/30/31行显示Zulu25、Gradle User Home C:/home/DevApp/cache/gradle、Gradle9.5.1；此前14行为9.6.1，证明实际切换而非只看settings。

独立PATH命令Get-Command指向C:/home/DevApp/gradle/bin/gradle.bat，--version独立exit0：9.5.1、Zulu25.0.3。process与Machine GRADLE_USER_HOME均C:/home/DevApp/cache/gradle，User环境项未设；IDEA项目.idea/gradle.xml的myGradleHome同为C:/home/DevApp/gradle，未设另一serviceDirectoryPath，此轮未触碰IDEA或共享缓存。未运行命令行进程枚举/读取用户运行参数，不接触credentials。

备份与删除：C:/Users/A/.codex/backups/eva-local-gradle-20261019/gradle-wrapper.properties.before确为238B，SHA256=5f6039ba1fdd81614ecf3cbf75527431ebe027e9e92aa5504f50e15294b1d2f3，内容原Gradle9.6.1 distributionURL。独立断言hash/大小通过。当前gradlew、gradlew.bat、gradle-wrapper.jar、gradle-wrapper.properties均不存在，wrapper目录0文件；git ls-files四目标0条、check-ignore属性命中。此删除不在Git可恢复范围，不能承诺git restore。恢复方法：先确认无后续修改，再从上述备份复制属性到原gradle/wrapper/gradle-wrapper.properties，并仅撤销此次新增wrapper.enabled/home键；不覆盖完整现settings或动其他源码。原本就没有Wrapper脚本/JAR，只恢复属性不等于恢复完整可执行Wrapper。

gradle/libs.versions.toml仍在、4465B，未递归删除目录。含隐藏目录的定向README/Docker/YAML/scripts literal扫描无active gradlew/gradle-wrapper调用（rg正常无匹配exit1），历史安全报告与backend状态中的Wrapper文字保留合理，不改写历史。未来CI/新机器需另行安装兼容本地Gradle，仓库没有Wrapper自动下载/版本锁定保证。

验证证据：独立PowerShell只读断言workspace值、Debug值与备份一致、238B/hash及catalog存在全部exit0；实际--versionexit0。复核开发.gradle/local-gradle-951-verify-20261019.log末尾BUILD SUCCESSFUL17s/4tasks4executed，范围projects+cache compileJava；.gradle/local-gradle-951-apt-20261019.log为MODEL_SUCCESS projectCount18，本地9.5.1 ToolingAPI+serial参数证据，原APT脚本SHA256 fc827f1ab7fd81c676b77c7d83c29871bd0d1b39281817021ccfe0513a1ead81一致。只复核这些现有日志，不将其说成本角色重新编译/重放，也不触发IDE导入或下载。旧native/Gradle10/plugin弃用警告不当产品失败。

保护核对：HEAD6a0fee51b7e2582600a4621b4e8c9bf8495d2cc3/cornerstone/index空，旧Maven/业务/他人修改保留。当前hash记录build.gradle DA4393E6FDB1494EB6B9D6D9B1EE569A1CDAE404B1C7270143EF852CDF0B6A2E，CacheCodes.java 02D957C0FFD9D3CC7EFA75CC6F8FDA38C5B4AC9AEB5BFEC60D8243BD1F17D6CD，launch.json CF30CAFF82B9B87F168EE706A7DFFFD72B2135319A353C28D95FB65CA656868F。开发声明前后hash相同，但未发现其本轮独立beforehash清单可比；因此历史不变依开发报告、本角色仅记录当前值并确保自身全程只读，不冒称持有独立删除前源码哈希。diffcheck0仅既有换行提示。

少数只读定位先误用PowerShell glob路径/Gradle扩展dist/extension.js，分别改为实际glob与package.main=dist/index.js核实；失败未当通过，也没有为查日志扩扫敏感文件。只新增本QA卡，无源码/配置/DBRedis/依赖/vault/Git发布/branchstash/服务/子代理操作。全部命令结束，写锁/UI锁交回总控，不轮询。新任务无提交推送授权。
