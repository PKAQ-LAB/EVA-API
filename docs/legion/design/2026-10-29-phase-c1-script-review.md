# phaseC1 脚本独立终审

2026-10-29 technical_designer。只读实算新旧工具SHA，实读新脚本全文件与交接；未运行入口、PLAN、自测、HTTP，未接密码、改源码或操作DB/Redis/UI/服务，仅写本设计。

静态审查通过，未发现必须修复项。新 `.gradle/org-c1-get-probe-20261029.py` 实算SHA256 `AF5FEFBA9B317CA98C8CEA1241B53CCB8FB69A96E28B891BC7913F4DA2A514A3`；依赖 `.gradle/org-self-stage1-20261029.py` 实算仍 `D005A1E898CD5B900FB75E3149F78D20F1E99BCC2E7529424C79F5E2F95335AE`。入口先核依赖字节，再import其纯函数，不调用旧main/transport、不覆盖旧路由或globals。导入不会启动旧执行入口。

出站只含LIMIT本轮login、menu、己方org list?id/get、既批准SELF role GET五步；request逐次核ORDER[index]及index<5，基线失败不进入第五步。菜单能力要求 `is True`，不接受false/unknown/1。己方两读取复用既审严格样本匹配，未借旧成功结果。固定loopback连接、peer回环确认后才发HTTP、仅本次bodytoken内存Bearer；无Cookie读取/认证、redirect、proxy、retry、refresh或logout。独立transport是旧白名单耦合后的窄实现，没有可配置端点框架；3秒连接、5秒读取/墙钟timer、90秒总期限及2MiB响应上限保留。

角色响应只投影既审状态/成功bool/SAFEcode或UNKNOWN、dataNonempty；无角色字段遍历/展开/打印。任意success=true含空值一律失败，失败非空data亦失败；只有HTTP200、原生false、精确00024及空data为RESOURCE_GATE_DENIED。未知/无记录/过期/404/4008/500等不当拒绝通过。输出独立phaseC1、edit/cross未覆盖、数据权限根因未证明，不能覆盖或拼接旧stage1证据。

CLI仅接受精确空/--self-test/--run；默认NOT_RUN不索密码或网络，getpass需TTY、拒GetPassWarning、无普通输入回退。错误参数与运行异常只输出固定枚举，不回显argv、凭据或异常文本。finally关闭本次client并清凭据引用，不登出用户其它会话，不承诺物理擦除字符串。正常session/cache/audit维护可能发生的边界保留。

已实读22业务fake、4目标负例、7独立transport fake、期限及依赖hash负例，断言五步顺序/首错停/基线门禁/角色空成功与各种失败/秘密注入输出。实现方报告自测及默认入口通过，本角色未重复执行，QA fake独核另记；真实HTTP仍未验证。无须为本次引入全MVC测试或POST edit候选。

可交总控结合QA fake结果决定是否交用户本人本机运行；本次审查本身不执行真实请求，也不要求用户立即重试登录。若再次得到登录失败，保留安全loginObservation停报，不增加登录次数或更换认证通道。
