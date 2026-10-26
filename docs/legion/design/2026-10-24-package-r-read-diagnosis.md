# 包 R 页面仍空：限定只读后端诊断

日期2026-10-24；backend_developer。只读核验，未再次修改SQL/Redis、未失效/回填任何键、不发HTTP、不读取任意用户授权或日志，不重启/构建/改源码。实际页面证据来自 QA 的 `quality_reviewer-data-scope-r-20261024.md`，仅证明新增角色下拉仍空、必填与无默认ALL正常，未取得HTTP状态，不能猜测请求结果。

当前进程仍9016/PID47664/10:08:31启动WebBooter；用户已确认standalone、tenant关闭、data/resource开启以及effective Redis DB0/default namespace，无需重复确认。R的六行提交与单键失效是已完成历史，当前诊断不获得再次写入授权。

## 1. 页面动作之后唯一缓存键只读结果

UTC2026-10-24T04:30:10.904406Z（北京时间12:30:10），仅 DB0 的 dictdata::DATA_SCOPE：TYPE=string、PTTL=-1，解码为 code_map，code键恰 `0000,0001,0002,0003,0007`。值SHA256=898fa95f715d0afb8bb507df5dac027a0fab6ce198a59e26da6ef43a7e7653ad，DUMP SHA256=db81244a2229b33582b2fc09d405fdf0e68aeaf473800b3b41c787d892761601。没有读其它键/前缀或输出原字节/凭据。

与 R 执行后该键 TYPE none/PTTL-2 相比，当前已被重新填入五code；本成员没有写或触发HTTP。不能据时间先后认定就是QA这一次请求回填，不能由缓存五code推断该次HTTP200、Response封装正确或下拉渲染通过。此结果不支持“当前这个键仍空”的判断，不应再删键或回退/扩改R数据。


## 2. 当前 DATA_SCOPE 与限定等价SQL实际结果

```json
{
  "observedAt": "2026-10-24T04:32:06.181504+00:00",
  "fingerprint": {
    "database": "eva",
    "schema": "eva",
    "transactionReadOnly": "on",
    "defaultTransactionReadOnly": "on"
  },
  "mainRows": [
    {
      "id": "1000000000000004001",
      "type": "DATA_SCOPE",
      "code": "DATA_SCOPE",
      "name": "数据权限",
      "deleted": 0,
      "frozen": 0,
      "tenant_id": "0",
      "pid": "0",
      "path": "/1000000000000004001",
      "isleaf": true,
      "sort": 1.0
    }
  ],
  "items": [
    {
      "id": "1000000000000004002",
      "main_id": "1000000000000004001",
      "d_code": "0000",
      "d_value": "全部权限",
      "deleted": 0,
      "frozen": 0,
      "tenant_id": "0",
      "sort": 1.0
    },
    {
      "id": "1000000000000004003",
      "main_id": "1000000000000004001",
      "d_code": "0001",
      "d_value": "仅本部门",
      "deleted": 0,
      "frozen": 0,
      "tenant_id": "0",
      "sort": 2.0
    },
    {
      "id": "1000000000000004004",
      "main_id": "1000000000000004001",
      "d_code": "0002",
      "d_value": "本人所属部门及下属部门",
      "deleted": 0,
      "frozen": 0,
      "tenant_id": "0",
      "sort": 3.0
    },
    {
      "id": "1000000000000004005",
      "main_id": "1000000000000004001",
      "d_code": "0003",
      "d_value": "指定部门",
      "deleted": 0,
      "frozen": 0,
      "tenant_id": "0",
      "sort": 4.0
    },
    {
      "id": "1000000000000004006",
      "main_id": "1000000000000004001",
      "d_code": "0004",
      "d_value": "仅本岗位",
      "deleted": 1,
      "frozen": 0,
      "tenant_id": "0",
      "sort": 5.0
    },
    {
      "id": "1000000000000004007",
      "main_id": "1000000000000004001",
      "d_code": "0005",
      "d_value": "本人所属岗位及下属岗位",
      "deleted": 1,
      "frozen": 0,
      "tenant_id": "0",
      "sort": 6.0
    },
    {
      "id": "1000000000000004008",
      "main_id": "1000000000000004001",
      "d_code": "0006",
      "d_value": "指定岗位",
      "deleted": 1,
      "frozen": 0,
      "tenant_id": "0",
      "sort": 7.0
    },
    {
      "id": "1000000000000004009",
      "main_id": "1000000000000004001",
      "d_code": "0007",
      "d_value": "本人创建或修改",
      "deleted": 0,
      "frozen": 0,
      "tenant_id": "0",
      "sort": 8.0
    }
  ],
  "currentCodeBasedLoaderBindings": {
    "id": "1000000000000004001",
    "code": "DATA_SCOPE",
    "deleted": 0,
    "frozenExcluded": 1
  },
  "codeBasedMainIds": [
    "1000000000000004001"
  ],
  "eligibleItemCodes": [
    "0000",
    "0001",
    "0002",
    "0003",
    "0007"
  ],
  "counterfactualTypeFilteredCounts": {
    "code": 0,
    "DATA_SCOPE": 1
  },
  "rawSqlExcludesUnknownRuntimeUserPermissionPredicate": true,
  "writes": 0,
  "rolledBack": true,
  "closed": true
}
```


## 3. 当前 JVM classpath、编译产物与资源

只读 JVM.system_properties 内部提取 classpath（199 项，不输出整份属性/参数），按顺序解析目标类或资源的第一个可用来源；不重建/启动服务，不读取进程对象/Token，不 dump 全堆。当前 PID 仍47664。

| 目标 | 实际 classpath 首个来源 | 与当前输出/源一致性 |
| --- | --- | --- |
| DictService / DictCtrl / DictEntity / DictItemEntity / DictCacheHelper | eva-web/eva-web-sys/eva-web-sys-service/build/classes/java/main | 字节与该模块当前输出相同 |
| MybatisPlusDataPermissionHandler | eva-core/eva-core-data-mybatis/build/classes/java/main | 字节与当前输出相同 |
| RedisConfiguration | eva-core/eva-core-cache/build/libs/eva-core-cache-1.1.1.jar | jar 中字节与当前 core-cache 输出相同，不因文件名旧就认定内容旧 |
| Dict.xml / DictItem.xml | sys-service/build/resources/main | 与当前 source resources 分别逐字节相同，只有 resultMap，没有覆盖 selectOne/selectList 的额外 loader SQL |
| Response | eva-core/eva-core-contract/build/classes/java/main | 当前输出相同 |
| CommonResponseAdvice | eva-core/eva-core-common/build/classes/java/main | 当前输出相同 |

关键字节指纹：DictService=60b015309ee1fe8a68b948265396523a6eefeb1f18062c37a4e62f62b8f19e99；DictCacheHelper=30c3471349910dee9ab9a2324855c9ca3724be15797a98abbef1a6a49cd98e76；DictCtrl=8c4db58441b7f1e9a49686a6fc847326d2b90245281ca945093e723950fc7630。对应 class 文件修改时间为2026-10-19 21:50:22+08:00，早于当前进程10-24 10:08启动。此为实际 JVM classpath 首解析位置及磁盘文件证据，不冒充已读取进程内存字节、排除了所有热替换机制或拿到实际HTTP返回。

对该来源执行只读 javap：queryDict 先 get(type)，非 null 直接返回；miss 才调用 selectDictByType 并 cachePut。selectDictByType 的 invokedynamic bootstrap #12 明确绑定 **DictEntity.getCode**，#1 绑定 StdEntity.getFrozen。当前字节码和源码一致，不能把变量名 type 误读为按 TYPE 列筛选，也没有发现硬编码 TYPE='code'。

第一次 javap -v 输出读取遇终端编码解码问题，随后明确以容错解码只取 ASCII 方法绑定，成功核到上述 bootstrap；这是只读工具输出问题，未构建或改运行产物。

## 4. queryDict → loader → SQL 与权限边界

当前可追踪调用链：DictCtrl GET /sys/dictionary/query/{code} → DictService.queryDict(code) → DictCacheHelper.get(code) → Spring Cache(dictdata)；命中非null（包括空Map）就返回。未命中才 selectDictByType，以 **CODE=请求code、FROZEN<>1** 加 StdEntity @TableLogic 的 DELETED=0 查询主项；再按 MAIN_ID 查询 DELETED=0 明细、SORT/ID 排序，Java层排除 FrozenEnumm.FROZEN，收集 D_CODE→D_VALUE。

当前源码没有单独的 DictLoader Bean，loader 指这里的 selectDictByType/listItems 路径；queryDict/接口没有 @Cacheable 另加第二份方法缓存。DictMapper/DictItemMapper 都是 BaseMapper，当前 XML 仅做字段映射，D_CODE/D_VALUE 是真实明细字段，不是 VALUE/NAME 伪列。主项PID/父根不参与该 code-only 查询。

第2节实际绑定参数与结果：code='DATA_SCOPE'、deleted=0、excluded frozen=1，限定在已知主ID内命中1行；main_id=该ID、deleted=0、excluded frozen=1得到五项。当前 TYPE 与 CODE 同为 DATA_SCOPE；仅作反事实验证的 TYPE='code' 在同一主ID下计数0、TYPE='DATA_SCOPE'计数1。**当前 loader 使用 CODE，不使用这个反事实 TYPE='code' 条件；0结果不能作为改 TYPE 的理由。** R 所恢复的行落在当前源码/字节码 loader 基本筛选内，不支持“六行恢复了但不在该loader内”的判断。

实际身份相关 SQL 仍有单独边界：data/resource 已由用户确认开启；当前配置文件的数据权限排除表不含 sys_dict/sys_dict_item，两Mapper无 @Ignore。数据插件可能根据可信 ThreadUser scopes追加条件：ALL→1=1，受限→创建/修改者或部门条件，空 scopes→1=0；schema模式关闭时 TenantSchemaRouter 不新增tenant路由，本路径不自行追加tenantId客户端条件。JwtAuthFilter 在构造 runWithUser 执行业务链前做 ResourceAuthorizationService.hasPermission。此处没有读取当前管理员/其他用户真实授权或发请求，所以不能认定当次资源许可、data scope、归一化路径或HTTP状态已通过/被拒。

第2节等价SQL明确未伪造这一真实请求未知的权限谓词，因此只是基本loader数据可命中证明。当前缓存已有五项时，按本逻辑同namespace命中可跳过DB loader，但HTTP仍可能在更前面的认证/资源/路由或客户端处理处失败；具体一段需要那次合法页面请求的安全状态/结构证据，不能从空UI或缓存单独下结论。不通过全局Ignore、授ALL、修改TYPE或新建其它字典补救未知请求问题。

## 5. 缓存“两个层次”的实际含义

DictCacheHelper.scopedKey(code) 产生逻辑key，Spring RedisCacheManager再叠加cacheName前缀；这是 **同一个Redis条目的命名组合**，不是已发现另一个独立陈旧缓存。当前standalone且用户确认默认namespace来源，对应DB0 dictdata::DATA_SCOPE；没有因PG schema=eva探其它prefix，未读其它key。

值有两次JSON编码：cachePut先JsonUtil.toJson(Map)得到String，Redis配置再以Jackson序列化该String；get先由Spring反序列化回String，再JsonUtil.parse成Map。只读复核UTC04:33:59.133121Z，原字节第一次JSON解码类型str，第二次dict，仍恰五code、PTTL=-1，值SHA256与第1节相同。这是正常序列化路径的两步，不是前端应接收两层JSON字符串的API契约，也不是能据此再清一层缓存的授权。

目前未发现 DictCacheHelper 中独立持久本地Map或 queryDict 方法级缓存；DictInit启动会init全字典但本轮没有运行它。fetchDicts/getAll是同一cache的另一访问方式，未在本轮调用（避免其它key读取）。React/前端请求缓存或具体组件内部状态由frontend在另仓任务核验，本附件不猜测其存在/命中。

## 6. 给前端的响应契约与最小后续项

成功路径的当前源契约：DictCtrl.query → Ctrl.success(data) → Response.success(data,BizCode)，data是 **Map<String,String>**。CommonResponseAdvice 对已有Response保留data，只处理code/message等；没有再包一层data/result、没有转数组或字典详情lines。预期结构是 `{success:true,code:"0000",message:...,data:{"0000":...,"0001":...,"0002":...,"0003":...,"0007":...}}`；这只是源码成功响应形状，不能标成当前HTTP实测。

已按总控指定项目协作范围把安全Redis五code、实际loader的getCode字节码绑定、PG结果和Map响应形状同步frontend会话01a132b1-0e33-7a73-9344-4ccaee63479c；无认证/地址/个人数据。该成员继续核实际Form/request响应包装、错误吞没、option映射和生命周期，不据本附件盲改契约或硬编码选项。

**当前未证实需要再改后端数据、TYPE、loader或权限契约。** 最小下一步是在总控授权的正常页面链上获取该次请求的安全结果：是否发到预期API、HTTP/业务success、data的结构及前端消费路径。尚无这段证据，不能给后端做猜测修复；若真实结果指出R外字段/契约错误，先提交精确差异再确认，不扩大R恢复。未请求额外身份授权、服务启停或秘密日志。

## 7. 收尾

本轮唯一PG诊断事务为READ ONLY，default/transaction均on，结束复核on后rollback/close；仅DATA_SCOPE主项及8明细、当前条件和反事实TYPE绑定，不查询其它字典/用户权限。Redis只读同DB0精确单键TYPE/GET/DUMP/PTTL，未失效或回填。没有HTTP/UI/测试构建/源码/服务/配置/依赖/角色授权/提交push/vault动作；只写本附件与自身status，原R私有备份/manifest未修改。所有连接/命令结束，S继续等待页面问题定位，未新增对象或授予权限。
