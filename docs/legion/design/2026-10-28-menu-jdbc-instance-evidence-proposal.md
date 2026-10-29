# 用合成JDBC实例证据替代八实例端口采样的可审方案

2026-10-28 technical_designer。总控要求评估内部验收口径替代；当前仅方案，不改测试/helper，不运行PG、SQL、observer、网络预检或UI。

## 结论及明确替代范围

可以采用。已审合成EmbeddedPG由测试实例创建，使用该实例已在用的DataSource真实连接读取服务器事实，比事后采样短命父进程并猜监听owner更直接。正式批准后，将“八个dataDir均被PowerShell捕获且带owner端口”替换为“八个独立合成实例均取得已绑定新RUN的JDBC技术证据”，明确版本化记录此验收变更；不回填4b65668d旧日志，不取消本机loopback、RUN目录/二进制锁或终态残留歧义/no kill。

一个连接的inet_server_addr只能证明该连接接入地址，不能单独证明服务无其它监听；因此必须同时严格断言listen_addresses正好127.0.0.1，并保留四测试五处builder显式同值及锁定二进制/配置。data_directory也不能单独证明可执行文件来自批准归档；离线jar/txz/exeSHA及实际classpath唯一性仍不可省略。

## 最小源与调用范围

可新增一个web-booter测试专用静态helper，不进生产依赖、不自动配置或扫描；只接受四fixture已创建的DataSource、固定测试类/生命周期标签。不创建数据库或连接来源、不读取应用配置/环境/凭据、不收客户端tenant等业务值。四类五处start后，立即在原资源try范围内或新Fixture已有构造catch内调用一次；四个菜单方法各一个实例、Mode两个、Advisor一个、Role一个，共八生命周期。保原assertions、清理及事务/租户上下文测试，不能替代真实业务断言。

fixture初始化失败时，证据调用也受原close保护；start自身未返回的失败仍不能自动声称已收尾。helper连接用try-with-resources，不泄漏到业务事务；在该测试自己的PG实例上读技术SELECT，关闭后继续原测试。即使随后业务失败已有证据文件，最终XML失败仍使整轮失败。

## JDBC技术事实与断言

在一条仅技术字段的SELECT中使用pg_catalog限定的inet_server_addr、inet_server_port、current_setting('listen_addresses')、current_setting('data_directory')、current_setting('server_version_num')及pg_postmaster_start_time。无需数据库用户、库名、连接URL、密码、作者/业务行或全setting表。

断言服务器地址严格127.0.0.1、端口正整数1..65535、listen_addresses严格127.0.0.1；不接受*、localhost、逗号其它地址或空值。端口来自实例默认port0启动，不固定或对开发网络探测。data_directory必须存在并经toRealPath/canonical验证为新RUN/pg/tmp下严格子目录，禁止相邻前缀、旧RUN、live目录或路径逃逸；版本为有效服务器版本标识并与已批准二进制版本策略一致，版本字符串不替代exe hash。postmaster启动时间与dataDir组合用于实例身份，PID复用/端口复用都不能单独当唯一实例。

这些事实证明测试连接实际抵达该合成实例及其有效监听配置，不宣称外部防火墙、其它服务或真实开发库安全验收。后续四PG测试可能改变search_path，采证在初始化阶段且使用pg_catalog内建函数，避免项目schema同名函数干扰。

## 新RUN绑定与防旧证据

定向helper独占创建fresh RUN后生成本次随机nonce和证据协议版本，封入新control seal；init仅给web-booter Test JVM传固定RUN根、专用evidence目录与nonce，doFirst按seal回读。测试helper拒缺值/非法目录、不得默认写系统tmp或旧RUN。路径须在新RUN、专用目录无已有记录；每次写CREATE_NEW且先写临时文件再原子发布，消费者不读取半条JSON，禁止覆盖。

每条记录只含协议版本、本RUN nonce、测试类/生命周期标签、dataDir canonical、服务器地址/port/listen配置、postmaster启动时间和版本技术值。不输出JDBC URL/其它环境。记录由真实SELECT和断言成功后产生，不能先写成功状态再查。可用dataDir/start的摘要作文件名，保原字段用于核验。fresh目录+nonce+control工具/源锁+测试执行时间界限与fresh XML共同防读旧记录，不能只按文件mtime或stdout关键词认成功。

helper结束时必须恰八条唯一实例、恰八个预期生命周期标签（菜单四方法逐一标记、Mode两处及其它各一），拒重复dataDir/重复标签/额外文件/缺失/旧nonce/版本/schema错。若现代码最终只定义五个调用点，菜单调用点由四个固定方法标签区分，不能只验总数八允许重复调用抵缺。无需固定每次动态端口互不复用；唯一身份不能依赖port总数。

## 交叉校验与残留门禁

保留当前已观察PG的规范RUN exe路径/SHA、-D路径和父实例证明。对任何可用已验证-D记录按canonical与本次JDBC dataDir交叉一致；观察覆盖只是补强，不再要求八实例均碰巧有-D采样或端口记录，否则又引回已明确替代的采样要求。已观察越界/hash错/非loopback等明确违规仍失败，不以JDBC证据抵销。进程name为initdb/pg_ctl的无端口不再触发监听不足。

保留终态存活RUN PG或未知不可读PG歧义失败/no kill，查询/协议错误失败，库close不等于已退出；本次只替换采样覆盖判定，不暗消除实际观察错误。若要连观察器采样阶段都取消，属于另一次明确门禁变更，本提案不包含。已退出短命实例按已批准身份复核分类，不认领新PID。

定向执行仍单web-booter Test、四精确类、无producer，复用产物需重新核参考全锁。因为新增测试helper和五处调用改变测试源码，不能沿原prepared45字节直接跑新源码：必须另审仅测试编译闭包，允许新测试class及必要变化class，重封prepared后再单Test执行；生产main未变可按已审字节复用，不能假称所有685源仍仅observer变化。新定向工具/测试源码/编译输入差异须显式锁，不宽放任意测试或业务源。

## 必要验证与结果口径

实现后先静态审与纯序列化/路径校验负例：错nonce/旧RUN、缺标签/重复实例/多文件、空或非loopback、多监听值、越界dataDir、字段类型错/不完整记录、测试失败但文件齐全不能成功。真实server事实最终仍须四PG类新轮执行证明；不要求再造合成socket预检或新观测框架。

报告分开旧9e26类219业务通过、当前4b四类11通过但旧观察口径失败、以及新批准口径下四类/JDBC八实例/终态的新结果。不能将旧轮悄改成整体通过，也不把三个结果拼成fresh26或真实菜单/S权限链验收完成。
