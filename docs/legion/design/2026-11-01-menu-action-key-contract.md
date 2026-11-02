# 菜单动作键与 Umi Access 已批准合同

最终阶段记录（2026-11-02，总控转达）：用户已明确批准现库执行；提交证据96CCC及独立后验E61D已通过，后端已重新启动，无凭据HTTP两端点拒绝符合预期。用户手工反馈“能看到所有按钮”。Edge自动UI被扩展阻止后已停止尝试，不宣称自动UI全验收，也不推断用户已打开详情。下文执行前待批措辞仅为审查阶段历史，不再作为当前阻塞。本角色设计已保存、源审结束，无活动测试、SQL、浏览器或后台命令；不扩任务，关机由总控按用户授权调度。

2026-11-01 technical_designer。用户已批准独立字段的代码与初始化变更。现库只编制精确适配提案，不执行数据库写入、重启或发布。本角色只维护设计与独立源码审查，不修改成员产品源或总控任务卡。既有dataflag未提交候选及其他已验成果保护。

## 字段与授权来源

在SYS_MODULE_RESOURCES新增可空字符串ACTION_KEY，API为actionKey。保留原ID、CODE、MAIN_ID、描述、HTTP method/path、现索引和授权关系。CODE仍是同模块内唯一的资源行标识，不改成按钮key，不删除或迁移既有授权ID。actionKey允许同一MAIN_ID多资源行取相同值，不能施加逐行唯一约束。

标准key为list、read、create、update、delete：list控制查询列表，read只代表详情按钮/查看手势，create新增，update编辑，delete删除。不使用edit代替update，不从GET统一推read，不从CODE、中文描述或通配HTTP路径推导任何CRUD。自定义key无命名空间前缀，总控精确规则为trim后1至64字符、正则`^[a-z][A-Za-z0-9_]{0,63}$`（允许下划线，覆盖早期纯lowerCamel文字），空串/空白规范为NULL；不含点/冒号。

空actionKey表示仅供后端HTTP授权的资源行，不输出按钮动作。新旧资源具体key值必须明确配置，旧MENU/API广域行不自动赋全CRUD。初始化加列和明确种子配置不授权重放V1覆盖现库；不新增动作组、依赖表或groupALL框架。

模块保存明确兼容：更新资源行省略actionKey保持原配置，显式null或trim后空串清为NULL，新建省略为NULL。BO以最小presence setter区分省略与显式null；仅新字段支持明确清空，不让ALWAYS策略在旧客户端省略时擦除已有key，不新增框架。前端清空显式发送null。同HTTP/path但不同actionKey/CODE/ID资源行必须保留，不能按HTTP路径去重合并授权身份。总控已接受此保存合同与freeze单key；格式始终采用上文含下划线的一套正则。

## fetchMenus输出

仅认证bootstrap菜单的resources由对象列表改为有序去重string[]，其余菜单字段保留。例如resources=['list','read','create']；无动作输出[]。

来源严格是当前真实有效主体经原角色成员、资源授权、有效模块和租户资源上限查询得到的已授资源行。仅提取该行非空actionKey，按原资源顺序取并集并去重；同key的多行无需全部授予才输出。不反向从按钮key生成HTTP grant，不把菜单祖先、其他菜单的同名key或客户端传入资源视为授权。

后端HTTP/path鉴权、角色资源缓存、账号/租户资格、只读事务与来源校验原样保留。本次只改变前端按钮控制，不扩接口鉴权架构。create/update可以共用POST /edit；即使由不同CODE/ID资源行分别授按钮，也不能声称后端因此能按动作区分同一接口。

## 管理接口元数据

角色fetchResource及资源授权、模块list/get/edit与资源行管理继续返回真实id/code/resourceDesc/resourceType/resourceUrl/checked及新增actionKey等元数据，仍按resourceId保存授权。仅bootstrap使用string[]投影，不把ModuleResourcesVo、RoleGrantedModuleVo等管理明细统一改为string[]，不删除角色勾选所需ID和描述。

## Umi Access与菜单身份

使用Umi Access统一canAction(menuIdentifier,key)，按稳定菜单ID分组；页面以当前菜单快照中的精确route唯一解析菜单ID，不在源码写死环境相关ID，不用名称/组件或路径前缀猜归属。route缺失、重复或无法解析时拒绝；不跨菜单借用同名read/create，不继承子孙动作。

已核路由/sys/account和/sys/user使用同一个账户页面，建议唯一显式别名/sys/user→/sys/account，解析仍须得到唯一授权菜单；不能建立任意prefix别名。若两个路由在真实菜单中分别具有不同有效菜单ID，不合并权限，交总控核具体配置。岗位接口当前挂组织模块MAIN_ID，不因/sys/post API前缀新造菜单或将组织key授给独立页面。

resourcePermissionEnabled=false沿现资源开关语义，true精确核当前菜单动作，缺失/异常拒绝；dataPermissionEnabled独立，不参与按钮授权。退出、切用户租户、bootstrap失败清旧能力，旧请求不得恢复上个身份动作。

原canRequest(method,path)生产调用必须逐项迁移到明确菜单+动作，不保留依赖已删bootstrap HTTP对象的静默失效路径，不以canAdmin/任意字符串放行替代。按钮可见、点击处理器、异步详情打开和双击查看使用同一read判断，双击不得退成编辑。read与list独立，不以列表许可获得详情。

在线用户现有metadata Drawer由列表内存数据打开，无独立get接口，但它仍是详情行为：查看按钮与双击必须共同受read控制，不能因没有新HTTP请求而仅配list。新库初始化显式补online read资源行，可沿既有GET list路径但必须独立CODE/ID和actionKey=read，不新造endpoint、不声称服务器可区分这两本地动作；list与read分开授予。Token不进入该Drawer，不借read扩大字段。本次不向现库补行或补grant。

## 自定义动作与依赖边界

标准CRUD以外已向总控及成员建议统一实际sort、freeze、grantResource、grantUser、grantRole：freeze为同switch路径冻结/解冻的单一动作，不拆lock/unlock或引入动作组；grantRole用于账户，grantResource/grantUser用于角色真实授权面板。成员须以实际controller/按钮匹配种子，不将清单假装成现库已有数据；无实际功能的export/kick/resetPassword不凭名称新造。本地copyToken不创建HTTP权限行。

已核RoleConfigModal仅显示“建设中”，不为该占位页创造可执行grantConfig权限或新业务。是否保留原占位入口由总控/前端按当前范围处理，不以动作迁移实现新配置功能。

总控已定嵌套边界：账户的角色列表若打开角色详情/权限树，预览按角色菜单read判断，不借账户read或grantRole；没有角色read时仅隐藏预览/阻止双击，不阻止已获grantRole的角色checkbox分配。当前真实链为account/rolemodulesview→listRoleModules→GET /sys/role/fetchResource，grantedTree沿checkedResource仅展示已授资源。本轮允许新安装独立role/read资源行沿同一GET支持该只读预览，不新建read端点或后端guard，不授POST grantResource。共享GET不代表后端能区分前端预览与授权面板动作，原后端真实资格/metadata边界不变；仓库没有getGrantedResources接口，不以该不存在名称作为依赖。

模块或字典的本地明细沿当前主流程的view/read或create/update状态，不从子表名称猜另一菜单，也不因本地查看增加写入口。RoleConfig“建设中”原入口仍可打开且保原9999保护，不新禁用/隐藏或造grantConfig；其关闭确认不是授权提交权限。

辅助校验与主写接口保持各自真实后端授权。例如组织create现依赖edit/checkUnique，不能仅凭辅助接口路径自动赋create；逐行配置需说明该key对应按钮而非全部HTTP必然可用。允许不同已授资源行同key并集去重，不为补足所有辅助请求新增groupALL规则或HTTP权限旁路。

## 初始化与现库适配

现库继承的12个明确辅助例外由总控收口：目标ID `1000000000000060066`至`1000000000000060072`、`1000000000000060074`至`1000000000000060078`的HTTP位于原其他MAIN，但动作归发起账户/角色MAIN。仅当同一角色以及同一租户各自已经具备该精确辅助method/path覆盖，且各自具备发起MAIN主操作的原HTTP资格时，才可列入用户一次审批的继承清单。每条列两个实际来源resourceID与原引用证据，缺覆盖或主操作资格即排除。不是通用跨菜单继承，不混多个角色拼一角色资格，不新增HTTP集合；前端仍只检查发起菜单key，不借另一菜单key。本角色离线核到12目标无同MAIN覆盖，接受此明确双证据例外后不再将同MAIN缺失单独视为必然阻塞；最终逐主体引用证明待后端限定RO结果，未写库。

代码与唯一核心V1的资源表加列及明确端点种子变更在本次批准范围；租户单模板实际不含SYS_MODULE_RESOURCES，不能为此虚构私有资源表。新库V1须按真实controller精确配置动作资源行、CODE仍唯一，并使新库默认admin通过既有初始化授权链具备这些动作；旧wide/MENU行保持NULL，不自动推CRUD。现库未授权写或新增grant，须另列限定只读盘点和精确提案：目标schema/资源表实际结构、既有资源ID/MAIN_ID/CODE/HTTP/状态、候选actionKey映射及角色引用影响。保原索引和grants，不在现库新增/删除资源行或补授角色来让按钮出现。

若需要额外资源行以独立授予共用接口的create/update，必须单列具体ID、CODE、key与现授权关系处理，不能静默迁移或重复重种。回退需以精确前后像恢复新字段映射，避免覆盖后续业务；不自动DROP已使用列。

## 独立审查与验收

重点核真实授权行投影及tenant ceiling、字符串去重顺序/空值、资源管理metadata未删、CODE索引及ID关系不变；前端按当前菜单准确Umi Access、list/read分离、只读与双击一致、授权失败与身份晚回不放行。自定义key/route别名和字段长度/格式需成员互发一致后记入本合同。

采用成员定向测试与独立QA，不重复旧全仓测试、不代用户重启、不运行现库写或发布。原CODE直接复用、源内映射和组ALL讨论已舍弃，不作为实施准则。

当前后端冻结为`.gradle/action-key-20261101/source-freeze-v2.json`，SHA256 `D46BE3710B1299994F31C835E337484841BF78AE4564079EDD01F444AE70566D`；15输入实算匹配，较原94BB仅ModuleService与其单元/合成PG三项变化。跨层源审发现的新建无ID/CODE同HTTP异key会合并丢行问题已修：fallback加入规范actionKey维度，ID/CODE优先及空key旧HTTP去重保持，新增双insert/同key去重断言与开发真实PG证据已给，源码复核关闭。其余省略保留/明确清空、bootstrap与管理metadata分离、授权SQL/tenant ceiling、78精确种子不变。前端冻结`.git/action-key-source-20261101.json` SHA256 `71C83218A0C7BA15E2798CAD54EE0C8FAEA915620C90CBC5BEBC7953A1413774`及74输入亦实算匹配，核心卸载/身份隔离、动作隐藏及处理器复核、嵌套role/read、actionKey null编辑独审未见另一个mustfix。开发测试与构建报告不替代独立QA或真实部署；本角色未重测/连接现库。

现库具体包为`.gradle/action-key-20261101/review-package.json` SHA256 `89EA9584FFC5E41F18C101932DE141DA59EE5FA40DD75FD1CD888AB6CBA891CA`，正向`3DF0938D5D23B66A0D3CFFAF4073B9B2B0148B3AF4922B5ACFCD3F7301615F46`、回退`F1F43AFD4275891607AB86ED1D209DCF6AA07D85D6B1F1E52A30F88488B3AD12`已实算匹配。固定范围1可空列/36旧行key/78资源/仅角色1000000000000000004新增65引用（23→88）/0租户新引用，其他角色租户引用保留；65条同角色tenant0/同MAIN原HTTP覆盖已离线独立复算失败0。前65覆盖43个唯一菜单动作，后13新增动作pair为0且原wide HTTP保留，所以13辅助行不新加refs，不需实际跨MAIN继承。正向前像/后像与触发器等漂移守卫、默认ROLLBACK回退/不DROP列已静态核；待总控用户明确写批准与执行器绑定，不自行执行。独立提案`2026-11-01-action-key-existing-db-adaptation-proposal.md`仍为现库审阅依据。
