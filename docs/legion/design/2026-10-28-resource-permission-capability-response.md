# 认证响应资源权限有效能力boolean契约

2026-10-28 technical_designer，只读设计。用户已批准有效能力字段，并确认沿eva现有配置；不重复询问。未源修改/测试/Gradle/UI/DB/网络/服务操作。

## 配置唯一来源

精确Spring属性为`eva.resource-permission.enable`，对应EvaPropertiesConfiguration中@ConfigurationProperties(prefix="eva")绑定的EvaConfig.resourcePermission.enable；服务读取`evaConfig.getResourcePermission().isEnable()`。ResourcePermission另有strict，不属于本字段，不能把enable换成strict，也不新增enabled别名或第二套frontend配置。Java原始缺省为false，但响应必须取Spring已合并当前环境/外部覆盖后的实际bean值，不读取yaml文本或向前端输出环境/配置对象。只公开该非敏感boolean。

## 统一响应位置与兼容

两现有成功端点使用同一外层字段：`resourcePermissionEnabled`，与success/code/message/data同级。POST /api/auth/login保持data里的access_token/refresh_token/user_info原结构；GET /api/auth/fetchMenus保持data为原菜单数组，元素和resources不改。示意是`{success:true,code:"0000",resourcePermissionEnabled:false,data:原值}`，boolean必须真JSON boolean，false不能被序列化省略。

登录成功handler已注入EvaConfig；菜单AuthCtrl可按最小注入同bean读取。只在成功认证响应赋值，失败/401/初始化失败不提供可用授权快照，前端拒绝。当前不存在已公开resourcePermission运行时载体可直接复用；LoginUserInfoVo的capabilities是Set<String>，不能把环境boolean伪成字符串能力。现Response无扩展metadata槽，因此需要一个仅认证响应的轻量包装DTO：可在auth与sys共同依赖的core-contract层扩展Response<T>只加该boolean，两处显式构造；不反向sys依赖auth，不全站包装或改全局ResponseAdvice。该共享DTO服务两个真实调用点，不是新bootstrap端点或菜单data改对象。

实现可用其它更小的等价加法载体，但最终JSON位置须保持统一，避免登录放user_info、bootstrap放外层导致前端猜字段。不要通过Response.success静态工厂误返回基类而丢扩展字段；保原成功code/message与欢迎消息/i18n，序列化需实际验证。无需修改token刷新/getAlpha的data或JWT载荷：页面刷新指既有fetchMenus bootstrap，非新增认证API；若后续要让token轮换也发该字段，另列需求，当前不扩大。

## 前端信任与刷新

服务返回类型在login/fetchMenus两处均声明外层可选boolean（兼容旧服务器）。成功响应且typeof字段为boolean才采纳。登录时先取得服务器字段，随后已有fetchMenus bootstrap成功值作为更新的权威快照；刷新页面getInitialState也从fetchMenus同外层取得。归一化函数显式传能力值，避免当前只传msg.data导致字段被丢。

只接受严格false表示服务器资源权限关闭，可免当前组织动作的资源列表检查；严格true表示开启，必须按当前已授权菜单resources+method/path逐项判断。缺失/null/字符串"false"/错误响应/初始化未完成均未知，默认拒绝；不得用`!value`把缺失当关闭。新bootstrap缺字段或失败，不能从fallbackUser、cookie、session或上次initialState保留旧false；应置未知并禁止写。旧已缓存菜单/admin字段不提供能力信任。

该boolean是资源校验全局有效开关，不是账号超级管理员或数据范围开关。false也不能消除认证、9999、tree/父子约束、只读模式或后端SELF保护；true时禁止canAdmin/名字/authorities字符串OR放行，宽授权由真实资源体现。晚到confirm/onOk/validateFields回调在请求前仍读取最新能力与菜单快照。

## permit例外限制

ResourceAuthorizationService在enable=true时还会按eva.auth.permit匹配放行。已读当前源配置permit为/auth/**、/notices等，没有组织路径；本轮不公开整个permit列表，不添加前端通配或默认admin例外。组织路径在当前源配置场景仍按菜单grant判断。

若某环境通过外部覆盖另将/sys/organization加入permit，而enable仍true，仅本boolean无法表达该例外；前端应继续保守拒绝无grant，不猜测或尝试写请求探测。不能声称这个boolean对任意permit环境完整等价。如必须支持这类额外策略，应另报最小“该操作有效许可”契约，用户批准后再扩，本轮不擅增字段/接口/资源。能力来源是实际环境boolean，并非声称已读取运行实例的permit配置。

## 必要验证与最小实施范围

后端聚焦两路径序列化：enable true与false都必须输出同名外层boolean，菜单data仍数组（含空数组）、resources/登录token及userinfo shape不漂，false不被omit，失败不冒成功；bean覆盖绑定测试确认精确eva.resource-permission.enable生效，不读/输出秘密。Shared DTO经ResponseUtil/Advice真实序列化链验证，禁止仅反射字段存在测试。

前端聚焦登录→fetchMenus覆盖、硬刷新bootstrap、空菜单+false、旧服务器缺字段、null/字符串假false拒绝、旧缓存false不能回填、权限切换时confirm和表单await后撤权。组织资源true/false各验证：false免资源检查仍受原业务约束，true精确method/path，未知拒写；不以管理员cache值覆盖。后端permit依旧不改，接口认证/数据范围回归保持。

最小候选写范围：一个共享认证响应DTO、登录成功handler、菜单AuthCtrl及focused tests；前端两服务返回类型、两既有归一/初始化能力接线、access/pure matcher和组织页原已授范围及focused tests。不改全站request拦截器、不新增bootstrap请求、不改fetchMenus data类型、不全站迁移或HMR越过QA写锁。实际写锁由总控分配；本记录仅设计。
