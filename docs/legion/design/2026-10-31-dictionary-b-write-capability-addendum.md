# 字典B写能力响应最小补充候选

最新批准记录（2026-10-31）：总控转达用户在明确“9999记录属性与用户有效维护资格不同”的说明后回答“可以”，已批准本文businessWritable外层及行/get readOnly两个严格布尔响应契约。B单standalone进入实施，A新1191CA15/QA41基线冻结；不再将本文两字段列为未批准。下文“候选/待确认”保留历史，现以本记录为准。两个位不入库、不作为BO可写字段，后端始终重验；CATEGORY及实际旧数据映射/数据库执行边界沿已批最小提案，具体数据操作另批。C、平台0认证与C3不扩展。

2026-10-31 technical_designer。前端仅准备，B未开写；本补充交总控确认冻结，不把已批category政策推成客户端可猜身份。

推荐 `/sys/dictionary/list` 在原Response外层新增原生布尔 `businessWritable`，保留data平数组；每个DictViewVo另增原生布尔 `readOnly`。不复用userinfo的ROLE_ADMIN/TENANT_INSPECT/capabilities，不增加全局身份声明，不从任一行readOnly=false倒推空集合的新建资格。

`businessWritable` 表示当前请求经服务端真实模式、可信身份、有效维护关系、本业务物理源及分类版本核验后，具备本源业务字典维护资格；不包含某个现有行frozen状态。空业务集合仍可为true。它不是跨tenant能力，也不是只看resource开关或rolesMap。当前实际操作资源由前端已有canRequestResource按edit/del/switch再叠加，后端执行时重新核同源资格和资源；此位不是绕过接口守卫的令牌。写能力查询错误使整个list失败，不能置true或返回上一用户缓存位。

`readOnly` 对具体主项定义为 SYSTEM分类、未分类、frozen9999、或当前主体无本源业务维护资格中的任一成立；其它可维护BUSINESS行才为false。SYSTEM总true，即使平台管理员。行frozen1保持原可维护但停用语义。BUSINESS主项包含9999子行时主readOnly不必整体true：普通主字段/普通子仍可维护，只读子按A精确守卫；删除主项受包含只读子禁止规则额外限制。前端明细写条件叠主项readOnly严格false与该行frozen非9999；后端始终重验。

三字段位置固定：外层 `businessWritable`，data[i].category，data[i].readOnly。示意：`{success:true,code:"0000",businessWritable:true,data:[{id:123,category:"BUSINESS",readOnly:false,...}]}`。布尔位在成功响应显式输出，不能因false被JSON省略；不输出密码、角色/授权详情、schema路径或新可伪造admin标识。可用字典专用响应子类承载外层位，不改变通用Response或其它接口。

前端新建仅businessWritable===true再叠edit资源；现有行修改仅readOnly===false、category===BUSINESS再叠对应资源。false、缺失、null、字符串/数字、加载或失败均拒写；切身份/源必须丢旧能力及迟到响应。旧服务没有category/readOnly时可保留原平表可见但全部只读并提示未提供分类维护契约，不能强归BUSINESS或生成第三根；只有完整分类数据才启用两虚根模式。这不替代后端分类版上线前未知存量映射完整门禁。

本字段只为呈现，执行守卫不得信任客户端回传businessWritable/readOnly；BO不新增两位，输入有位也不参与授权。get可返回readOnly供详情切换时刷新，但create仍取当前成功list的外层位。list/get须由同一策略得出有效值，不能仅将frozen转换为readOnly。

这是此前最小提案未逐字列出的响应增量，建议总控作为B实施契约补充确认后再派前后端写入；本角色不代批准、不修改源。A不依赖这两个位，继续独立实施验收。C3、平台0认证与已验500Modal不变。
