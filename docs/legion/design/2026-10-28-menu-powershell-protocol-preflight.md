# 冻结8403真实PowerShell协议预检

2026-10-28 backend_developer，cwd=C:/home/workspace/lab/EVA-API。经总控专项授权仅调用冻结helper实际powershell_json/validator，未改辅助或其它源。全部用预先固定的合成payload，不重试或自修；仅查询该PowerShell自身PID的CIM ProcessId/CreationDate，不读取其他进程命令、exe、身份或环境。

## 实际结果

```json
{
  "helperSHA256": "8403CB7DB4E291F8EDEA2AFBFD8168599F3BFE7B44FABFC8CB966FB84E3C553F",
  "pythonVersion": "3.15.0b2",
  "powerShellVersion": "5.1.26100.9711",
  "verifiedAt": "2026-10-28T16:25:30.285164+08:00",
  "positiveAccepted": 6,
  "negativeRejected": 3,
  "results": [
    {
      "case": "normalZeroPorts",
      "outcome": "accepted",
      "rows": 1,
      "ports": 0
    },
    {
      "case": "emptyList",
      "outcome": "accepted",
      "rows": 0,
      "ports": null
    },
    {
      "case": "normalOnePort",
      "outcome": "accepted",
      "rows": 1,
      "ports": 1
    },
    {
      "case": "normalManyPorts",
      "outcome": "accepted",
      "rows": 1,
      "ports": 2
    },
    {
      "case": "ambiguityOnly",
      "outcome": "accepted",
      "rows": 1,
      "ports": null
    },
    {
      "case": "normalAmbiguityMixed",
      "outcome": "accepted",
      "rows": 2,
      "ports": 0
    },
    {
      "case": "stderr",
      "outcome": "rejected",
      "safeCategory": "Observation stderr is not empty",
      "technicalSummary": "exit=0 stdoutBytes=4 stderrBytes=18"
    },
    {
      "case": "nonzero",
      "outcome": "rejected",
      "safeCategory": "Observation subprocess exit failure",
      "technicalSummary": "exit=7 stdoutBytes=4 stderrBytes=0"
    },
    {
      "case": "badJSON",
      "outcome": "rejected",
      "safeCategory": "Observation JSON decode failure",
      "technicalSummary": "exit=0 stdoutBytes=18 stderrBytes=0"
    }
  ],
  "selfCimOnly": true,
  "observerCalls": 0,
  "pgStarts": 0,
  "gradleStarts": 0,
  "helperUnchanged": true
}
```

normalZeroPorts使用真实自身CreationDate.ToUniversalTime().ToString('o')，经生产validator验证UTC及normal字段；name携中文/ASCII与实际PSVersion。one/many ports仅合成127.0.0.1端口对象，没有创建监听或查询实际连接。ambiguity是合成标记，不认领真实进程。stderr负例只写合成中文到真实stderr，错误仅保存类别/exit/字节长度，未打印原诊断正文或命令。badJSON/nonzero也经真实PS与生产binary管道路径拒绝。

## 范围与未知

九项真实协议调用均已结束，命令槽释放。未创建正式RUN、未复用ecc70444任何产物、未调用完整observe_processes、未PG/Gradle/业务HTTP/DB/Redis/IDE/服务/UI操作、未修改配置或提交推送。8403前后SHA一致。该结果仅证明本机所列Python/WinPS下的实际UTF8编码/错误传播/合成序列化路径，不证明真实进程树CIM/端口观察、八cluster采样、终态无残留、编译/测试或业务权限验收；ecc70444仍观察无效，不升格为通过。正式新RUN仍须总控另授。
