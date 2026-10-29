# C隔离fixture：已批准配置类增强形态的精确识别

2026-10-27，technical_designer；仅只读分析与设计记录，未实现或重测。最终边界按总控最新统一：内部EnhancedConfiguration marker不要求、不引入或反射加载；使用公开BeanFactoryAware关系及以下精确来源条件。

## 1. 当前失败证据与分类

已独立读取run `.gradle/C-isolated-runs/20261027T151732Z-c7099319` 的main/test-classes gate与两个实际测试XML。main、test-classes exitCode均0；BootstrapTest为13 tests/13 failures，ConsumerTest为12/12，errors/skipped均0。25条唯一failure消息相同：assertBeanDefinitions第139行拒绝 `mybatisPlusConfig:org.pkaq.config.MybatisPlusConfig$$SpringCGLIB$$0`。还没有执行到Mapper根因、字段或消费者断言，不能报告C业务回归通过，也不能把25统一失败认定生产C无效。

当前fixture只显式register MybatisPlusConfig/MybatisPlusAutoConfiguration；普通BeanFactoryPostProcessor断言在context.refresh内运行。安装Spring7.0.9的ConfigurationClassPostProcessor先enhanceConfigurationClasses，并将已注册full Configuration的BeanDefinition替换为增强Class；MybatisPlusConfig本就@Configuration且proxyBeanMethods默认true。此处是门禁不兼容合法增强形态，不是允许加入新的配置业务类。

ClassUtils.getUserClass(Class)在当前安装源码中仅凭类名含$$就返回直接父类，**不能单独当配置来源真实性验证**。也不能删所有$$后缀、按org.pkaq/org.springframework包前缀或“任何CGLIB”放行。

## 2. 最小允许变化与文件边界

依据总控最新精确授权，仅改 `UserMapperBootstrapFixture.java` 的类准入检查和必要 `UserMapperBootstrapTest.java` 门禁回归；生产User.xml/其余生产、Consumer/旧fixture测试、隔离helper/init、配置/IDE/服务及实际数据全部冻结。用户已批准C，不意味着任意Spring增强形态自动获准。

保持原classes与methods白名单。仅当原始beanClass不在原精确名单时，为显式映射 **beanName=mybatisPlusConfig → expectedClass=MybatisPlusConfig.class** 提供下列窄分支；当前没有证据需要扩大到其他@Configuration。返回原类名只用于同一个白名单判定，不替换BeanDefinition本身，不手工去增强、不改proxyBeanMethods/Bean来源。

## 3. 增强分支必须全部满足

| 条件 | 精确要求 |
| --- | --- |
| 注册身份 | beanName恰为mybatisPlusConfig，与fixture显式注册对应；不接受另一个名字借相同父类 |
| 原定义来源 | BeanDefinition为AnnotatedBeanDefinition，metadata.getClassName恰为MybatisPlusConfig原全名；factoryBeanName/factoryMethodName均空，不能把其他@Bean产物当直接配置注册 |
| 已解析Class | 从AbstractBeanDefinition.hasBeanClass/getBeanClass取得已解析实际Class，不实例化bean、不用getBean/getType触发FactoryBean；没有已解析Class则拒绝，不按字符串猜 |
| 原类资格 | expectedClass就是本测试使用的MybatisPlusConfig.class身份，且原@Configuration(proxyBeanMethods=true)语义已明确；不只比较潜在同名不同loader类字符串 |
| 继承关系 | actual.getSuperclass()==expectedClass，ClassUtils.getUserClass(actual)==expectedClass；二者都按Class身份，不递归剥多层未知代理 |
| 公开增强关系 | BeanFactoryAware.class.isAssignableFrom(actual)；这一项只补充公开增强形态，不能单独作为准入，必须同时满足全部Class/注册来源条件 |
| 范围外情况 | 错名/元数据/父类/userClass/公开增强关系/工厂来源任一不符立即AssertionError，不宽回退到包名或endsWith代理判断 |

不需要检查内部EnhancedConfiguration marker，也不编译引用包私有外层、按接口全名反射加载或实例化Spring内部类型。最终采用公开BeanFactoryAware关系，主要来源边界是显式唯一注册名、原Annotated元数据、已解析Class身份/directsuper/getUserClass及无factory配置定义；不是把所有BeanFactoryAware或所有$$类放行。在当前固定两配置注册、无外部自定义注册器/不受信配置输入、源码/classpath锁定的测试上下文中，未见该最终组合相对任务安全目标有可触发新缺口；不将此局部门禁包装成允许任意不受信代码执行的通用沙箱。

无需给生成Class硬编码CodeSource绝对输出路径；隔离run本来锁classpath产物/源哈希，生成类的保护域/资源形式也可能与原class不同。记录实际类/原metadata可作诊断，但不得把“resourceDescription存在”或动态路径当放行替代条件。若未来同名类loader不一致，Class身份检查应拒绝并报具体来源，不自行扩名单。

## 4. 定义与factory owner必须同源复用

当前139行检查定义类，143行又检查 `beans.getBeanDefinition(owner).getBeanClassName()`。只修139会在 `mybatisPlusInterceptor` / `getDatabaseIdProvider` 的owner再次遇到相同增强原名差异。应由一个局部“已批准定义的有效原类名”检查复用于这两处；不能第二处临时剥后缀绕过前一处来源条件。

当owner走上述增强MybatisPlusConfig分支时，其@Bean方法仅允许该原配置既有 **mybatisPlusInterceptor、getDatabaseIdProvider**，原methods总集不扩；sqlSessionFactory/sqlSessionTemplate等仍必须来自原允许的其它配置定义，不能因为方法名在总集里就借增强owner执行不属于它的方法。原非增强owner按既有精确类/方法门禁维持，不为本次修复重构全容器注册规则。

不调用配置bean或factory来判断类型；在单例初始化之前只读BeanDefinition/已解析Class/metadata。未知BeanDefinition仍须拒绝，不能把此修变成推迟到getBeansOfType后再检查。

## 5. 必要窄回归与执行边界

现正常context真实增强路径应能越过该特定误拒，但后续任何未知bean/方法/JDBC调用仍按原门禁失败闭合。新增窄负例：错bean名、metadata指向其它类、非factory条件不符、直接父类/userClass不符、缺BeanFactoryAware或仅有该关系却不满足其它条件、未知$$类、增强owner调用非原两个方法；定义与owner检查应使用相同helper并分别证明拒绝，不能只测最终factory构造成功。不得把“没有内部marker”作为失败理由重新加入已移除的要求。

可对局部纯判定/helper构造隔离BeanDefinition做负例，不启动额外应用上下文或外部服务；真实合法Spring增强正例继续沿原bootstrap。测试范围限上述两文件，新增测试数以实际新suite为准，不修改helper里筛选或把25旧失败丢弃。当门禁修后再出现下一处失败须保实际信息停报，不能自动扩Spring包/Bean白名单。

编译/门禁正负例和原Mapper/消费者断言尚未由本分析运行；待backend交最小差异、QA/总控独立读审后再授一次执行槽。当前结论是“有边界的测试夹具修复建议”，不是C生产或25项已通过。只读数据/源码/实际XML与安装Spring源码，写本设计/status；无测试文件修改、辅助/Gradle执行、生产/DB/UI/debug/服务操作。

## 6. 两文件实际窄修独立终审

本轮独立实读并核SHA：BootstrapFixture为 **ADDFD47CA48037AD211A92888291307BEED614C636FC51F060152710C635D564**，BootstrapTest为 **9DE3EFE620F4C72766392677558FFD447E118992F7E7809B10BB4F7AB2F92EC7**。对c7099319 locked-inputs全部现有文件哈希只读比较，内容变化恰这两文件；未见生产/Consumer/隔离helper内容变化。本角色未调用helper/PLAN、编译或启动context。

实际resolvedBeanClassName仅在raw非空、不在原名单且beanName恰mybatisPlusConfig时进入增强分支；hasBeanClass/getBeanClass、ClassUtils userClass与directsuper按Class身份、Annotated原metadata、无factory、原Configuration.proxyBeanMethods和公开BeanFactoryAware逐项断言。没有内部marker/反射加载/getBean/按包名前缀宽放。raw/null原逻辑保留，不借增强修复改变全部白名单策略。

定义145和factory owner152复用同一函数；增强owner归一后与原raw不同的情况下，仅容许mybatisPlusInterceptor/getDatabaseIdProvider两方法，再受原methods总集检查。其它未知类、方法、错来源仍AssertionError，没有通过删139/143或将所有@Configuration增强还原的放宽。

新增唯一针对性@Test用AnnotationConfigApplicationContext只register内存EvaConfig与MybatisPlusConfig，未注册MybatisPlusAutoConfiguration/DataSource、未Mapper扫描、未ConfigData/组件扫描。已读MybatisPlusConfig只创建内存Interceptor与VendorDatabaseIdProvider，并启用标准事务基础设施；provider仅被构造/设置映射，不调用getDatabaseId(DataSource)，没有连接入口。其Spring配置增强可能注册AOP/事务基础设施，这是原注解的内存行为，不是启动调度、Redis/Flyway、网络或实际DB。未见该小context触外部服务的具体路径。

随后纯DefaultListableBeanFactory元数据正例保留真实增强Class及原定义，合法owner两方法通过；负例包括错bean名、原metadata为EvaConfig、错误直接父类、缺BeanFactoryAware、factory来源非空，以及增强owner试用原总集存在但不属该owner的sqlSessionFactory和未知方法。元数据负向类只作Class/BeanDefinition，不实例化；同一函数覆盖定义/owner，不只测试最终factory可构造。

**结论：本两文件窄修未见新的确定静态安全阻塞，可由总控/QA结合冻结范围批准一次新RUN隔离验证。** 这不是Bean/Java API已动态通过，不声称25或预计26项已绿。静态只看到新增一个@Test，实际发现/执行数与失败阶段由新suite/XML证明；新失败须停报原位置，不自动加入Spring类/方法或跳过负例。

保持生产/User.xml/SQL/HTTP/VO/DB、其它测试与helper冻结；源输入变化仍须被新RUN锁定，不复用c709旧测试产物或把其25失败覆盖掉。无测试/生产/辅助修改，无重测/Gradle/DB/UI/服务动作，只有本人设计与status追加。
