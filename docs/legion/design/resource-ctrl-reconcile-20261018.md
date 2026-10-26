# 2026-10-18 资源与 Controller 重审

状态：只读对照与待确认计划，不授权执行。开发 PostgreSQL 指纹 d22bc534e71c / eva schema / transaction_read_only=on；没有 DB/Redis 写入。

当前共78条：旧菜单/广域23、实际HTTP种子54、QA自建1。独立重扫当前Controller类级与方法级Mapping，没有具体method/path找不到端点的条目；这不等于资源语义、归属或授权展示符合用户需求。

标准CRUD按GET list/get/{id}与POST edit/del（含字典del/{id}）列28条；其余26条是真实校验、排序、锁定、授权及字典辅助/兼容GET删除等接口，不凭功能词臆造或改方法。岗位7条当前归sys.organization；没有独立岗位菜单，不能未经确认新建模块。

## 推荐及执行边界

优先按实际Ctrl及具体中文操作对照CRUD展示，特殊接口保留其真实HTTP定义，不伪装成CRUD，也不自动扩权。是否把26个非标准CRUD接口从本批资源清单移除，需用户明确；旧23个广域候选均有关联，不能因展示不符直接删除。

本轮已确定安全执行计划为add=0/update=0/delete=0。若批准退役全部旧广域，精确候选就是下表23行，涉及角色引用46、租户引用23、套餐0、旧扁平权限46，先做逐Ctrl资源与现授权范围映射、租户上限核验、备份/事务/缓存方案，再单独确认；不能把54资源全授给所有角色。若只讨论26个其它真实接口，当前相关角色/租户/套餐引用均0，仍需明确范围后才产生精确删除计划。

不得重放旧--apply：测试新增数据使旧脚本固定计数/目标不适用于本轮，且它不是本次语义更正方案。QA自建资源与5条测试对象均保护，不做SQL清理。

## 完整现状与引用

|ID|分类|所属模块code|资源code|HTTP|实际路径|描述|角色引用|租户引用|套餐引用|
|---|---|---|---|---|---|---|---:|---:|---:|
|1000000000000001100|旧菜单/广域授权|sys|MENU_SYS|*|/sys|菜单：sys|2|1|0|
|1000000000000001101|旧菜单/广域授权|sys.account|MENU_SYS_ACCOUNT|*|/sys/account|菜单：sys.account|2|1|0|
|1000000000000001102|旧菜单/广域授权|sys.organization|MENU_SYS_ORGANIZATION|*|/sys/organization|菜单：sys.organization|2|1|0|
|1000000000000001103|旧菜单/广域授权|sys.role|MENU_SYS_ROLE|*|/sys/role|菜单：sys.role|2|1|0|
|1000000000000001104|旧菜单/广域授权|sys.module|MENU_SYS_MODULE|*|/sys/module|菜单：sys.module|2|1|0|
|1000000000000001105|旧菜单/广域授权|sys.dictionary|MENU_SYS_DICTIONARY|*|/sys/dictionary|菜单：sys.dictionary|2|1|0|
|1000000000000001200|旧菜单/广域授权|log|MENU_LOG|*|/log|菜单：log|2|1|0|
|1000000000000001201|旧菜单/广域授权|log.online|MENU_LOG_ONLINE|*|/log/online|菜单：log.online|2|1|0|
|1000000000000001202|旧菜单/广域授权|log.biz|MENU_LOG_BIZ|*|/log/biz|菜单：log.biz|2|1|0|
|1000000000000001203|旧菜单/广域授权|log.error|MENU_LOG_ERROR|*|/log/error|菜单：log.error|2|1|0|
|1000000000000001401|旧菜单/广域授权|sys.organization|API_SYS_POST|*|/sys/post/**|岗位管理接口|2|1|0|
|1000000000000001402|旧菜单/广域授权|sys|API_SYS_TENANT|*|/sys/tenant/**|租户管理接口|2|1|0|
|1000000000000001403|旧菜单/广域授权|sys|API_SYS_NOTICE|*|/sys/notice/**|通知管理接口|2|1|0|
|1000000000000001404|旧菜单/广域授权|log.online|API_LOG_ONLINE|*|/monitor/log/online/**|在线用户接口|2|1|0|
|1000000000000001405|旧菜单/广域授权|log.biz|API_LOG_BIZ|*|/monitor/log/biz/**|业务日志接口|2|1|0|
|1000000000000001406|旧菜单/广域授权|log.error|API_LOG_ERROR|*|/monitor/log/error/**|错误日志接口|2|1|0|
|1000000000000001407|旧菜单/广域授权|log|API_LOG_LOGIN|*|/monitor/log/login/**|登录日志接口|2|1|0|
|1000000000000001408|旧菜单/广域授权|log|API_LOG_OPENAPI|*|/monitor/log/openapi/**|OpenAPI调用日志接口|2|1|0|
|1000000000000001409|旧菜单/广域授权|sys.account|API_SYS_ACCOUNT|*|/sys/account/**|用户管理接口|2|1|0|
|1000000000000001410|旧菜单/广域授权|sys.organization|API_SYS_ORGANIZATION|*|/sys/organization/**|组织管理接口|2|1|0|
|1000000000000001411|旧菜单/广域授权|sys.role|API_SYS_ROLE|*|/sys/role/**|角色管理接口|2|1|0|
|1000000000000001412|旧菜单/广域授权|sys.module|API_SYS_MODULE|*|/sys/module/**|模块管理接口|2|1|0|
|1000000000000001413|旧菜单/广域授权|sys.dictionary|API_SYS_DICTIONARY|*|/sys/dictionary/**|字典管理接口|2|1|0|
|1000000000000011000|其它真实接口|sys.account|HTTP_USER_POST_CHECKUNIQUE|POST|/sys/account/checkUnique|账号管理：校验账号唯一性|0|0|0|
|1000000000000011001|其它真实接口|sys.account|HTTP_USER_POST_REPWD|POST|/sys/account/repwd|账号管理：重新设置密码|0|0|0|
|1000000000000011002|标准CRUD|sys.account|HTTP_USER_POST_DEL|POST|/sys/account/del|账号管理：根据ID删除/批量删除记录|0|0|0|
|1000000000000011003|标准CRUD|sys.account|HTTP_USER_POST_EDIT|POST|/sys/account/edit|账号管理：新增/编辑记录|0|0|0|
|1000000000000011004|其它真实接口|sys.account|HTTP_USER_POST_GRANT|POST|/sys/account/grant|账号管理：授权角色|0|0|0|
|1000000000000011005|其它真实接口|sys.account|HTTP_USER_POST_GRANTPOST|POST|/sys/account/grantPost|账号管理：授权岗位|0|0|0|
|1000000000000011006|标准CRUD|sys.account|HTTP_USER_GET_LIST|GET|/sys/account/list|账号管理：列表查询|0|0|0|
|1000000000000011007|标准CRUD|sys.account|HTTP_USER_GET_GET|GET|/sys/account/get/{id}|账号管理：根据ID获取记录信息|0|0|0|
|1000000000000011008|其它真实接口|sys.account|HTTP_USER_POST_CHANGE|POST|/sys/account/switch|账号管理：锁定/解锁|0|0|0|
|1000000000000011009|其它真实接口|sys.organization|HTTP_ORGANIZATION_POST_CHECKUNIQUE|POST|/sys/organization/checkUnique|组织管理：校验 code / name 在同一 pid 下是否唯一|0|0|0|
|1000000000000011010|标准CRUD|sys.organization|HTTP_ORGANIZATION_GET_LIST|GET|/sys/organization/list|组织管理：获取组织树|0|0|0|
|1000000000000011011|标准CRUD|sys.organization|HTTP_ORGANIZATION_GET_GET|GET|/sys/organization/get/{id}|组织管理：根据 ID 获取组织详情|0|0|0|
|1000000000000011012|标准CRUD|sys.organization|HTTP_ORGANIZATION_POST_EDIT|POST|/sys/organization/edit|组织管理：新增 / 编辑组织|0|0|0|
|1000000000000011013|标准CRUD|sys.organization|HTTP_ORGANIZATION_POST_DEL|POST|/sys/organization/del|组织管理：根据 ID 批量删除|0|0|0|
|1000000000000011014|其它真实接口|sys.organization|HTTP_ORGANIZATION_POST_SORT|POST|/sys/organization/sort|组织管理：同级拖拽排序|0|0|0|
|1000000000000011015|其它真实接口|sys.organization|HTTP_ORGANIZATION_POST_SWITCHFROZEN|POST|/sys/organization/switch|组织管理：切换冻结状态（级联子节点）|0|0|0|
|1000000000000011016|其它真实接口|sys.organization|HTTP_POST_POST_CHECKUNIQUE|POST|/sys/post/checkUnique|岗位管理：校验 code / title 在同一 pid 下是否唯一|0|0|0|
|1000000000000011017|标准CRUD|sys.organization|HTTP_POST_GET_LIST|GET|/sys/post/list|岗位管理：获取岗位树|0|0|0|
|1000000000000011018|标准CRUD|sys.organization|HTTP_POST_GET_GET|GET|/sys/post/get/{id}|岗位管理：根据 ID 获取岗位详情|0|0|0|
|1000000000000011019|标准CRUD|sys.organization|HTTP_POST_POST_EDIT|POST|/sys/post/edit|岗位管理：新增/编辑岗位|0|0|0|
|1000000000000011020|标准CRUD|sys.organization|HTTP_POST_POST_DEL|POST|/sys/post/del|岗位管理：根据 ID 批量删除|0|0|0|
|1000000000000011021|其它真实接口|sys.organization|HTTP_POST_POST_SORT|POST|/sys/post/sort|岗位管理：同级拖拽排序|0|0|0|
|1000000000000011022|其它真实接口|sys.organization|HTTP_POST_POST_SWITCHFROZEN|POST|/sys/post/switch|岗位管理：切换冻结状态（级联子节点）|0|0|0|
|1000000000000011023|标准CRUD|sys.role|HTTP_ROLE_GET_GET|GET|/sys/role/get/{id}|角色管理：根据ID获取角色详情|0|0|0|
|1000000000000011024|其它真实接口|sys.role|HTTP_ROLE_POST_CHECKUNIQUE|POST|/sys/role/checkUnique|角色管理：校验角色编码唯一性|0|0|0|
|1000000000000011025|其它真实接口|sys.role|HTTP_ROLE_POST_CHANGE|POST|/sys/role/switch|角色管理：锁定/解锁角色|0|0|0|
|1000000000000011026|其它真实接口|sys.role|HTTP_ROLE_GET_FETCHRESOURCE|GET|/sys/role/fetchResource|角色管理：获得角色绑定的菜单资源列表|0|0|0|
|1000000000000011027|其它真实接口|sys.role|HTTP_ROLE_POST_GRANTRESOURCE|POST|/sys/role/grantResource|角色管理：保存角色模块关系|0|0|0|
|1000000000000011028|其它真实接口|sys.role|HTTP_ROLE_GET_LISTUSER|GET|/sys/role/listUser|角色管理：获得角色绑定的用户列表|0|0|0|
|1000000000000011029|其它真实接口|sys.role|HTTP_ROLE_POST_GRANTUSER|POST|/sys/role/grantUser|角色管理：保存角色用户关系|0|0|0|
|1000000000000011030|标准CRUD|sys.role|HTTP_ROLE_POST_DEL|POST|/sys/role/del|角色管理：根据ID删除/批量删除角色|0|0|0|
|1000000000000011031|标准CRUD|sys.role|HTTP_ROLE_POST_SAVE|POST|/sys/role/edit|角色管理：新增记录|0|0|0|
|1000000000000011032|标准CRUD|sys.role|HTTP_ROLE_GET_LIST|GET|/sys/role/list|角色管理：分页查询|0|0|0|
|1000000000000011033|标准CRUD|sys.module|HTTP_MODULE_POST_DEL|POST|/sys/module/del|模块管理：根据ID删除/批量删除记录|0|0|0|
|1000000000000011034|标准CRUD|sys.module|HTTP_MODULE_POST_EDIT|POST|/sys/module/edit|模块管理：新增/编辑记录|0|0|0|
|1000000000000011035|其它真实接口|sys.module|HTTP_MODULE_POST_CHECKUNIQUE|POST|/sys/module/checkUnique|模块管理：校验同级模块编码唯一性|0|0|0|
|1000000000000011036|标准CRUD|sys.module|HTTP_MODULE_GET_GET|GET|/sys/module/get/{id}|模块管理：根据ID获得模块|0|0|0|
|1000000000000011037|标准CRUD|sys.module|HTTP_MODULE_GET_LIST|GET|/sys/module/list|模块管理：获取模块树 |0|0|0|
|1000000000000011038|其它真实接口|sys.module|HTTP_MODULE_POST_SORT|POST|/sys/module/sort|模块管理：同级拖拽排序|0|0|0|
|1000000000000011039|其它真实接口|sys.module|HTTP_MODULE_POST_FROZEN|POST|/sys/module/frozen|模块管理：设置冻结状态（级联子节点）|0|0|0|
|1000000000000011040|其它真实接口|sys.dictionary|HTTP_DICT_GET_QUERY|GET|/sys/dictionary/query/{code}|字典管理：根据类型从缓存中获取字典项|0|0|0|
|1000000000000011041|标准CRUD|sys.dictionary|HTTP_DICT_GET_LISTDICT|GET|/sys/dictionary/list|字典管理：获取字典列表|0|0|0|
|1000000000000011042|其它真实接口|sys.dictionary|HTTP_DICT_GET_VALIDATEITEM|GET|/sys/dictionary/validate/{type}/{value}|字典管理：校验字典值是否属于指定字典|0|0|0|
|1000000000000011043|标准CRUD|sys.dictionary|HTTP_DICT_GET_GETDICT|GET|/sys/dictionary/get/{id}|字典管理：根据 ID 获取字典|0|0|0|
|1000000000000011044|其它真实接口|sys.dictionary|HTTP_DICT_GET_GETDICTBYCODE|GET|/sys/dictionary/get/type/{code}|字典管理：根据编码获取字典|0|0|0|
|1000000000000011045|其它真实接口|sys.dictionary|HTTP_DICT_POST_CHECKUNIQUE|POST|/sys/dictionary/checkUnique|字典管理：校验 code|0|0|0|
|1000000000000011046|其它真实接口|sys.dictionary|HTTP_DICT_GET_DELDICT|GET|/sys/dictionary/del/{id}|字典管理：根据ID删除|0|0|0|
|1000000000000011047|标准CRUD|sys.dictionary|HTTP_DICT_POST_DELETEDICT|POST|/sys/dictionary/del/{id}|字典管理：根据ID删除|0|0|0|
|1000000000000011048|标准CRUD|sys.dictionary|HTTP_DICT_POST_EDITDICT|POST|/sys/dictionary/edit|字典管理：新增/编辑字典|0|0|0|
|1000000000000011049|其它真实接口|sys.dictionary|HTTP_DICT_POST_SWITCHFROZEN|POST|/sys/dictionary/switch|字典管理：锁定/解锁|0|0|0|
|1000000000000011050|标准CRUD|log.biz|HTTP_BIZLOG_GET_QUERY|GET|/monitor/log/biz/get/{id}|业务日志：query|0|0|0|
|1000000000000011051|标准CRUD|log.biz|HTTP_BIZLOG_GET_LIST|GET|/monitor/log/biz/list|业务日志：list|0|0|0|
|1000000000000011052|标准CRUD|log.error|HTTP_ERROR_GET_QUERY|GET|/monitor/log/error/get/{id}|错误日志：根据id获取错误日志明细|0|0|0|
|1000000000000011053|标准CRUD|log.error|HTTP_ERROR_GET_LIST|GET|/monitor/log/error/list|错误日志：获取错误日志列表|0|0|0|
|2111793873144872961|QA自建，保护|EVA_QA_20261018_M01|—|GET|/sys/module/list|EVA_QA_20261018_查询|0|0|0|

## 需补充确认

用户指出的具体不正确资源行/期望操作标签；是否仅标准CRUD；特殊操作的授权管理位置；岗位资源模块归属是否需要独立菜单。没有选择前不能把原54条端点吻合自证为需求正确，也不能把23旧广域批删导致管理员失去现授权。
