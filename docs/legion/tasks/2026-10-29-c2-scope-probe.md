# 当前可执行任务：C2/C3 只读探针

总控恢复派发。此前交接未完成，后端成员空闲且未生成脚本；不得继续等待重复任务。用户已批准本地只读 API 测试，当前仅准备与自测，不发送真实 HTTP，不接收密码。

## 精确实现

新增 ignored `.gradle/org-scope-read-probe-20261029.py`，用 apply_patch 编辑。先读设计 `docs/legion/design/2026-10-29-c2-c3-scope-read-probe-contract.md`。原 D005 和 AF5 脚本保持冻结，可 hash 锁复用纯函数，不修改其全局路由或 monkeypatch 认证。

- 固定目标 http://localhost:9016/api、仅账号 QA_LIM_20261023，密码用户未来本地 getpass 无回显，MD5 与前端一致；Bearer token 只取本次 login body、仅内存，不读取浏览器或 cookies，不打印原响应、凭据、令牌、异常原文。
- 固定样本 L=2115435549295775746、P=2115579583561605122。最多六请求，顺序为 POST auth/login、GET auth/fetchMenus、GET sys/organization/list?id=L、GET sys/organization/list?id=P、GET sys/organization/get/L、GET sys/organization/get/P。
- 参数仅 `--expect-peer visible|hidden` 显式选择。默认 PLAN 无网络无密码；实际必须 `--run`。当前 C2 应 visible。不是根据客户端选项证明真实角色；角色和部门另有独立 PG 核验。
- L 的 list/get 永远要求 HTTP200、success=true、code0000、精确单一目标 ID。visible 的 P 同要求；hidden 的 P list 仅接受成功严格空数组，P get 仅接受 HTTP200、success=false、code0x000-02001 且空失败 data。成功空、泛403/404、资源拒绝、过期或未知码不得通过。
- menu 外层 resourcePermissionEnabled 必须严格 true；登录身份核 LIMIT 技术 ID=2114719096971497474。输出只白名单 L/P 标签、状态、已知业务码、数量和布尔，不输出实际记录或个人信息。
- 禁代理、重定向、重试；连接3秒、请求5秒、总90秒、响应2MiB 等沿已审传输约束。无任何 edit/del、账号角色或数据库写请求。正常认证会话缓存可能刷新，不宣称零副作用。
- 六步全部满足才报结果符合预期；资源拒绝、编辑拒绝及数据权限根因仍各自标未覆盖/未证明，不拼接旧阶段结果。

## 测试与交付

实现最小独立 transport，旧工具冻结。纯 fake 自测覆盖两种预期、基线失败提前停止、隐藏条件严格、外部目标/ID/顺序/请求上限/超时、秘密注入输出不泄露；禁止真实 HTTP，自测必须证明 realHttpRequests=0。给脚本 SHA、精确用户命令、默认 PLAN 和自测安全结果及简短交接，主动回总控及 designer。不要以缺任务再次停下，不创建下级代理，不轮询。

## 已核当前状态（不改动）

C2 用户已行动确认并 UI/PG 独核：DEPT 2113858118523068418 scope0001、资源仅 1000000000000011010 和 1000000000000011011；LIMIT 仅 DEPT，PEER 2113894345662021633 仍 SELF；两账号部门均 D1=2113856614139469826、岗位空，permVer PEER6/LIMIT3。两样本作者分别为各账号、pid0，不得移动或修改。C3 尚未批准执行，不能移动 PEER 部门或授 TREE/ALL 等。

禁止真实数据库/Redis/HTTP、浏览器、服务启停、依赖下载、生产代码或其他验证工具修改、提交推送。脚本和自测就绪后需 designer 独审、QA 离线独测，才由总控交用户本地执行。
