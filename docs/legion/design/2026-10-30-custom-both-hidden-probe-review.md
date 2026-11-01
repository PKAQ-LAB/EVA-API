# CUSTOM 双 hidden 探针独立源码审查

2026-10-30 technical_designer。结论：冻结包装脚本未发现静态阻断，可交 QA 无网络 PLAN/fake 独立复验。真实运行仍依赖 UI 调整后的独立授权状态核验及总控组织用户本机执行，不从预期或脚本审查推定当前 CUSTOM 已生效。

实核 `.gradle/org-custom-both-hidden-probe-20261030.py` SHA256 `EED7FDBDCBD55E3269680A333FC2BDE30DD1B65A6188214C6D317A9DBE53A8FC`，冻结 scope 依赖 `EF9E9184238D86FA1DA4465E6FACFB9CC2F0F59477EE2A8918605E6B005AC9D9`，pure 依赖 `D005A1E898CD5B900FB75E3149F78D20F1E99BCC2E7529424C79F5E2F95335AE`，均与加载前校验一致。后端证据文件实际 SHA `20125C691E928AFCB0926EE2FC4F55BE03E8CBDC116D2E47041BA0121D268D0C`；其 PLAN/59fake 由后端报告，本角色未执行，不代独立 QA 签审。

## 调用与判定

包装直接复用冻结 scope 的 LocalHttp/Budget 及其 pure 解析/认证函数，不复制传输、不修改旧模块 globals、不调用旧 read_check。因此旧 L 样本强制 positive 的行为没有进入新包装。依赖加载核 scope.BASE/ACCOUNT/ACTOR 与六条 literal ORDER，实际固定 LIMIT login→menu→L list→P list→L get→P get；无模式/样本/端点参数，最多六请求、首错停。默认 PLAN 在加载依赖和输入密码前返回，真实 --run 仅 TTY getpass，禁止回显 fallback、仅内存摘要和token，认证主体及 login/menu 外层 capability 均严格核验。

两 list 只认 HTTP200、success 严格 True、code0000、data 类型严格 list 且等于[]；两 get 只认 HTTP200、success 严格 False、完整0x000-02001及data不存在/null/空list或dict。任一读取非空data即 UNEXPECTED_VISIBLE 停止且不展开；get成功空值或list成功null/空object也拒绝。资源00024、过期、其它失败和非200不当 hidden PASS。该组合与 OrganizationService list→TreeHelper 空列表及 get→CAN_NOT_FIND_RECORD→ExceptionAdvice HTTP200路径相符。

CUSTOM0003 在现 handler 上按样本CREATE_ID或MODIFY_ID对应有效账号资料部门匹配指定集合，不按当前用户部门猜测。总控已有作者D1/仅指定D2/两existsfalse证据为预期依据，本角色未访问DB或改部门。结果 dataScopeRootCause 恒 NOT_PROVEN_BY_READS；API结果只能证明指定时点两样本隐藏，与另行核实的新角色资源/数据范围状态结合，不能单独断定根因或推广任意组织。

## 传输与证据边界

已重新实读冻结 scope LocalHttp/Budget：唯一localhost9016/api，发送前peer loopback，直接http.client不走代理/Cookie或跳转重试；connect3秒、单请求wall5秒、总90秒、2MiB上限、严格UTF8/重复key拒绝。budget核顺序及次数，finally关闭连接、cancel timer、清token引用和凭据。依赖导入旧main均受入口条件保护，不触旧脚本请求。

输出使用pure登录观察投影，未知code为UNKNOWN，固定阶段/HTTP/strictbool/空data标记，不输出对象内容、姓名/实际ID、token、headers/Cookie、message或异常原文。读取判定不遍历数据树。fake覆盖每条读取可见、混淆码/非200/timeout、两list错误空形状、两get成功空形状、正确空失败容器、能力不真、坏身份/登录、预算第七请求及sentinel不进入结果；传输继续依赖原已审冻结实现，不冒称新fake又独立覆盖全部transport分支。

本角色仅设计/status写入，未运行脚本、测试、真实HTTP或DB，未改包装、旧工具、生产源、部门、资源/grant、服务。正常登录/QUERY仍可能写session/cache/系统审计，不称所有存储零写；C3保持USER_SKIPPED，未恢复部门操作或扩大ALL。
