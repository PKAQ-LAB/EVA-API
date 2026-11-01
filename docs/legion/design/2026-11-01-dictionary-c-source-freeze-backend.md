# C 字典隔离源码冻结与验证交接

2026-11-01，cornerstone。总控已授权原 C 范围的 8 个现有生产文件及必要就近测试；本次没有新增 HTTP/API/数据库字段，没有修改 StdEntity、全局租户路由或认证框架。当前精确冻结版本已通过独立 QA 源审和一次定向隔离运行，无剩余 mustfix；真实运行加载和租户联调未执行，不能据此宣告 EVA 全部完成。下文待验表述为原交接时状态，由末尾独验记录更新。

## 最终行为

SYSTEM 仅从可信 core 读取，BUSINESS 仅从当前认证租户的私有 schema 读取；standalone 的两类均来自配置的唯一 schema。来源依据服务端 ThreadUser、core 租户映射、实际有效账号、物理表 namespace、必要列和已验证的 CATEGORY 约束核定。缺结构拒绝，不使用 core 兜底，不执行 DDL、旧租户升级或租户扫描。平台 tenant 0 在 schema 模式下仍被原路由边界拒绝，未加入 C3 认证能力。

两个现有 Mapper 使用明确列及服务端验证后的 schema 限定 SQL，不包含私有表不存在的 tenant_id，也不添加 Ignore。显式 schema 同时使 MyBatis 一级缓存的 SQL 查询键区分来源。管理 list/get 保留原数据权限插件；相同 ID 的主明细始终在各自来源内关联。get 的 category 可选，省略且双命中时拒绝歧义。写入只作用于当前 BUSINESS，SYSTEM 只读，系统保留 code 在个人数据权限之外校验；标签 query/fetch 遇到受影响的跨源同 code 冲突拒绝，其他独立 code 仍可查询。

真实维护资格来自 standalone ROLE_ADMIN 或私有 TENANT_ADMIN 的有效账号、角色及成员关系。启用资源权限时，租户本地资源引用与 core 实际资源和租户授权上限取交集；关闭该开关不免除真实维护资格。主权限锁、完整 9999 保护快照、批处理写前全量校验、审计字段、逻辑删除及版本比较保留。非空客户端 bo.revision 必须在任何主子写入前匹配锁读版本，null 沿原兼容边界处理；SQL 仍比较期望 revision 并校验受影响行数。

共享标签缓存仅接受受控完整数据库读取，管理 SELF 查询不能裁剪共享值。键为 v2::<schema>::<SYSTEM|BUSINESS>::<code>，不读取或迁移旧键；有界清理仅允许指定 owner 的非空精确前缀，并再次过滤返回键。fetch 每次先取得权威清单与冲突结果，再逐字典填缺键，部分暖缓存不能充当完整集合。写后回调只按提前捕获的不可变 BUSINESS owner 和新旧 code 失效，不回填旧快照，不改动 ThreadUser/TenantContext；回滚不触发失效。实际外层读写事务中完全绕过缓存读写，保证 read-your-write 且不发布未提交值。

Serializer 通过 Spring 代理的 IDictService.queryDict 读取，正常已认证普通账号保留既有业务标签显示能力，不因此新增管理 list/get 或写资格。路由 finally 后按可信主体重新解析来源；无可信主体保留原码，不记录字段原值或异常全文。现有 bootstrap 继续委托同一服务，无新增入口。启动无请求主体时跳过。init(Map)/reload(Map) 不接受传入 Map 作为共享值，使用数据库权威清单和值重载；外层写事务仍不发布缓存。已有全局路由会以可信主体覆盖临时上下文，方法仅读该主体来源并 finally 恢复原上下文，不新增进入前上下文不一致必须拒绝的契约。

## 范围与证据

最终清单：.gradle/dict-c-20261031T235530Z/source-freeze-v2.json，SHA256 C21751F68472842485CB2950441B6996416E7F5D05D2B50BA18018415C258FF3。清单含 8 个生产文件、1 个新增合成 PG 测试、4 个内部签名适配后的旧夹具、27 个最终编译产物哈希及保护项核验。source-freeze.json 是保留的前一格式候选，不作为最终冻结清单。

Java 25 使用既有缓存依赖、独立 .gradle 输出进行 javac，最终源码及全部就近字典测试编译成功，日志 freeze-final-compile.log；不是 Gradle 全量构建。存在缓存依赖的 APIguardian 注解缺失警告及 Lombok/Mockito 的 JDK 警告，未冒称无警告。修改代码已标准化 UTF-8/LF、单行不超过 120 字符，限定 git diff --check 通过；Git autocrlf 提示并非内容检查失败。

新增 DictSchemaIsolationPostgresTest 的同一个 JUnit @Test 由 public main 调用，在一个本地隔离 PG 14.22 中完成六组断言：core/两租户同主明细 ID、同事务 MyBatis 缓存、可选 category 消歧；旧键忽略、精确前缀、部分预热、Map 注入与同 code 冲突；普通标签/管理 SELF、真实管理员、core 授权上限、SYSTEM/9999 整批保护；私有无 tenant_id 的真实增改删、审计/revision、过期 bo、冷/暖缓存外层读写后回滚、提交后 owner/新旧 code 失效及外层上下文保持；缺表/列/约束拒绝回落与可信主体来源/finally 恢复；standalone 及 schema 模式平台 0 边界。未将六组标记冒称六个 JUnit 用例或 JUnit XML 结果。

最终行为运行 test-6/result.json SHA256 3DD1E8F915E928226B5094A1DBD91F822F8FBE41F8D7212066A6C2BE3AF3E972；test-6/run.log SHA256 F5E6EA1E2D578E3DEF24D0B5C4AB7C13C86952F38F2B269F01C62EB5C57D0232。北京时间 08:07:47.262615 至 08:08:22.254888，exit 0，六组标记及 C_SCHEMA_ISOLATION_PASS 全部出现。随后仅整理格式并最终编译，精确最终冻结哈希的独立 runtime 核验交给 QA，当前不冒称已执行。4 个旧夹具仅编译，未重跑 B66 或旧 18 套件。

test-1 至 test-4 的失败事实保留：夹具跨连接 SET、缺 Mapper Bean、真实约束文本中的数组 cast 规范化遗漏、夹具未注册项目枚举处理器；第三项生产核验解析已修复，其余夹具补齐真实运行配置。test-5 为首轮行为通过，test-6 为新增冷回滚及外层上下文精确断言后通过。没有覆盖或删除失败目录。

冻结 helper 的 pg_preflight() 只用于本地已有 PG archive/exe 哈希验证，没有下载。所有自有合成 PG 均自然关闭；最后一轮日志确认 shutdown，限定查询本轮 launcher/server/pg_ctl 的 42904/56052/22804 无存活进程，未作全进程扫描或终止操作。

F604 的 14 个保护文件、主 V1、私有唯一模板、TenantSchemaProvisioner，以及已批准开发库逻辑删除配置和测试哈希全部保持一致。没有真实 dev 数据库连接、迁移、HTTP、共享缓存清理、服务重启、HMR/build 目录写入、提交或 push。

## QA 交接

8 个生产文件及上述测试源码冻结，后端不再重跑已绿项。QA 可先只读源审，再在独立输出、独立 PG tmp/unpack 中对精确冻结版本进行一次必要的 C 定向验证；编译参数参考 javac-with-adapted-tests.args，执行参数参考 test-6/java.args，禁止复用写入后端冻结 classes。具体 mustfix 集中交回后再开窄修，不等待旧 designer。真实租户 rollout、旧租户升级及 C3 不在本次授权中。

## 08:15 QA 精确冻结版本独验通过

QA 已完成源审，原客户端 revision 和写事务缓存污染两项 mustfix 均由源码与真实 stale/warm/cold 事务断言关闭，无剩余 mustfix。独验绑定同一 source-freeze-v2.json SHA C21751F68472842485CB2950441B6996416E7F5D05D2B50BA18018415C258FF3，13 个源码及 19 个保护项始末不漂移；后端未再修改源码或运行测试。

QA RUN 为 .gradle/qa-dict-c-20261101T001442Z-d642216d，独立输出重新 javac 编译最终 22 个源文件 exit 0，再仅运行一次相同 @Test 的 main 入口及独立临时 PG 14.22。北京时间 08:14:42.325606 至 08:15:19.711186，exit 0，六阶段及 C_SCHEMA_ISOLATION_PASS 通过。后端只读核对 result.json 和三项哈希与 QA 消息一致，没有再次连接数据库。

result.json SHA256 CBA4D39016E60C95A317BE0FCBB190778C59F02DBF1CAC286DE1C82B87E41243；compile.log SHA256 AEF4D3B25D4980682CE1D8CEFCF7F8AAFE28CAC2B41D14F50C42BB47502697E1；run.log SHA256 B20386226A5C1D20D98E9B0FF52F5257003122AA83DDC13ADAC44C91A716DB1D。

该结果仍限定为 cached-classpath javac 和一个 @Test 经 main 执行，不是完整 Gradle/JUnit XML。4 个旧夹具仅编译；缓存为内存实现与 mock Redis，bootstrap 是 AuthCtrl 委托源码核验和服务合成链，未验证真实 HTTP/dev/Redis。用户正常构建或重启以加载该候选尚未执行。真实 schema 联调若后续另获授权，仅核明确租户的实际字典结构、可信主体/成员关系及 core 授权上限；缺结构拒绝，不自动升级、迁移、修改账本或清 Redis。B 不重开，C3、平台 0 认证和账户角色最终清理仍另案。源码和测试槽已交回总控。
