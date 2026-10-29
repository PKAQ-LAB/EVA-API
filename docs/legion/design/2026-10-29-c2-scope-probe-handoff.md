# C2/C3 六请求脚本交接

2026-10-29 backend_developer。依据任务卡 `docs/legion/tasks/2026-10-29-c2-scope-probe.md` 及 C2/C3 scope-read 契约完成实现。本次仅新增 ignored 脚本及本交接；没有执行真实 HTTP、读取密码、访问 DB/Redis、启停服务或修改生产源码。

脚本：`.gradle/org-scope-read-probe-20261029.py`。

SHA256：`EF9E9184238D86FA1DA4465E6FACFB9CC2F0F59477EE2A8918605E6B005AC9D9`。

冻结依赖 D005 SHA256：`D005A1E898CD5B900FB75E3149F78D20F1E99BCC2E7529424C79F5E2F95335AE`；运行自测和实际模式均先校验该哈希，仅复用解析和判定纯函数。AF5 原脚本 SHA256 仍为 `AF5FEFBA9B317CA98C8CEA1241B53CCB8FB69A96E28B891BC7913F4DA2A514A3`。独立最小 transport 来源于冻结 AF5 实现，没有修改旧模块 globals、路由或认证逻辑。

## 已完成离线验证

默认 PLAN：`success=false/code=NOT_RUN/maxRequests=6/realHttpRequests=0/passwordInput=false`。

纯 fake 自测：`success=true/code=SYNTHETIC_SELF_TEST_PASS/scenarios=33/targetNegativeCases=4/argumentNegativeCases=5/transportScenarios=7/requestOrderAndMaxSix=true/deadlineCase=true/dependencyHashNegative=true/secretRedaction=true/realHttpRequests=0`。

覆盖 visible/hidden、四个读取基线提前停止、隐藏列表与详情严格不对称条件、成功空详情、资源拒绝/过期/未知码/403/404/400/非空失败data、错误或额外ID及浮点ID、menu能力非true、固定顺序及第七请求拒绝、总超时、外部目标/ID、参数枚举、依赖哈希漂移及秘密 sentinel 不泄漏。独立 transport 以 fake HTTPConnection 核验回环、超时、重定向拒绝、坏 JSON、响应上限、异常固定码及无 Cookie。

## 精确命令

默认安全 PLAN：

```powershell
& 'C:/home/DevApp/python3/python.exe' -I -B 'C:/home/workspace/lab/EVA-API/.gradle/org-scope-read-probe-20261029.py'
```

离线自测：

```powershell
& 'C:/home/DevApp/python3/python.exe' -I -B 'C:/home/workspace/lab/EVA-API/.gradle/org-scope-read-probe-20261029.py' --self-test
```

以下命令本次未执行，供 designer 独审、QA 离线独测完成后总控交用户本人在 TTY 输入密码。当前 C2 使用 visible：

```powershell
& 'C:/home/DevApp/python3/python.exe' -I -B 'C:/home/workspace/lab/EVA-API/.gradle/org-scope-read-probe-20261029.py' --run --expect-peer visible
```

后续 C3 仅在相应授权及独立事实核验完成后由总控组织使用 hidden：

```powershell
& 'C:/home/DevApp/python3/python.exe' -I -B 'C:/home/workspace/lab/EVA-API/.gradle/org-scope-read-probe-20261029.py' --run --expect-peer hidden
```

固定顺序为 login/menu/listL/listP/getL/getP；任一步不符立即停止，六步全部匹配才输出 `EXPECTATION_MATCHED`。预期参数不改变或证明真实 scope。脚本始终保留 `scopeRootCause=NOT_PROVEN_BY_API/resourceDenial=NOT_COVERED/editDenial=NOT_COVERED`，不拼接历史结果。正常认证可能维护会话缓存，本交接不宣称服务器零副作用。

本次结果只证明 fake 下判定与安全边界；真实 API 结果仍未运行，交总控组织后续独审和验收。
