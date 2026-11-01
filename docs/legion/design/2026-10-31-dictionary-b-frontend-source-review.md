# 字典B前端冻结源审

2026-10-31 technical_designer。最新11AD新冻结版源码终审通过，身份及成功标记阻断已关闭，可交QA独立复验；旧94A4仅保历史，后端3C2C源码签审不受影响。

实核前端 `.git/dictionary-b-source-20261031.json` SHA `94A4C0E66981A564D743B5D13E8C9DCBECAAF6D749CFFFE422BBAFA0FAB0DA76`，所列7production+6test共13项逐项当前SHA匹配。另实核既有login-log-source清单所列12项无漂移；lineaoeform实际SHA `18AE111D4C2FFE6AE0C09D080DF3238ACCC6AAA3E2CE8BADF4DF1EF1023E0D25`，仍width500，与已验收基线一致。未以相对git HEAD旧改动误判新漂移。

## 必修：详情响应ID未与请求记录匹配

index.handleEdit有requestId、身份version、资源及category核验，但未核res.data.id。若点击BUSINESS A，响应同分类B，且B本身也在可写list，setCurrentItem会载入B；canWrite(record A)及后续permissionRef对B均可成立，用户可以保存B。get携category消歧不足以替代ID匹配。

最小修：归一后要求返回非空id与请求record.id严格一致，再setCurrentItem；错/缺ID走已有失败catch保留草稿，不切对象不POST。补同category错ID及缺ID负例，不需新API。另list query与详情当前只在success===false拒绝，缺失/非布尔success也可能配有效cap启写；http工具只转发请求不验证envelope，已要求成功必须严格true才采纳新数据/能力。

## 已读符合部分及证据范围

只读策略对category/row readOnly、9999均保守，canCreate/canWrite还要求外层businessWritable===true、完整契约、当前list匹配、实际动作资源。完整data才生成两个固定虚根，虚根点击/键盘均不发请求；unknown保平表且写关闭，不归BUSINESS/第三根。分类+ID用于query选择与identity，列表queryKey含当前用户引用version及组件scope；旧身份迟到详情不复活，脏态切换确认/取消/失败回稿仍有相应测试。

AOE保存前后及子回调核会话/即时权限，SYSTEM frozen0仍只读，单9999子不可改删且普通主仍可保存。editDict顶层BO明确字段白名单，新增省category、更新携真分类，不传readOnly/businessWritable/schema/owner/虚根。get真实query传category，del旧GET不新增参数。现布局保持500明细Modal，lineList只改行级9999守卫，未碰登录12冻结。

六份测试文件包含category完整性/服务白名单/页面身份迟到/根不请求/资源叠加/脏态回稿/旧明细回调用例，当前未见错详情ID覆盖。前端报告37个不同case分轮通过，不是同次完整37；本角色未运行测试/类型检查/浏览器。最终修后新SHA及必要结果另审，不能沿旧94A4直接签过。当前仅写本人设计/status，无生产源或测试修改，无运行操作。

## 身份窄修候选复核

前端已撤回旧94A4。已实读index补丁：list/detail success!==true即拒，详情在setCurrentItem前拒缺ID或String归一后ID不等请求记录，分类精确核验保留。原catch保留旧对象及草稿/dirty，未改接口或Modal。新增错/缺ID、缺/字符串success负例及列表success不真负例已见源码。当前源码mustfix已收口，待QA释放后端唯一测试槽后前端必要复验及新冻结SHA，不提前终签。

## 身份修复版正式终签

实核前端 `.git/dictionary-b-source-identity-20261031.json` SHA256 `11AD24CE24CEC68139CAB35920755F026898A307A161D96D8A5839A2E535D354`，13项当前SHA逐一匹配；与旧94A4清单比较，仅index.tsx及categoryPage.test.tsx两项改变，旧问题以外源链不扩。只读 `.git/dictionary-b-identity-fix-20261031.log` 为categoryPage/interaction两文件21/21通过；不把旧37分轮与新21合计成单轮完整结果。本角色未运行测试。

已读7个新增负例：同分类但返回另一个可写列表ID、缺ID、详情缺/字符串success均保留原草稿和对象且零POST；列表缺/字符串/false success不得采纳可写能力。对应源码在setCurrentItem之前精确检查ID/分类与严格successTrue，旧mustfix关闭，未见新可复现源码阻断。未知/不完整flat只读、双虚根零请求、身份version及迟到返回、strict双bool叠资源、单只读子不锁普通主、顶层BO白名单等前述结论保持。

再次实算登录12文件清单全部匹配；lineaoeform仍SHA18AE111D4C2FFE6AE0C09D080DF3238ACCC6AAA3E2CE8BADF4DF1EF1023E0D25、width500，与验收基线一致。前端报告定向tsc字典无诊断但外部appicon两项/organization一项仍失败，不称tsc全绿；日志AntD旧rowKey索引弃用提示保留，不扩改。源码签过不替QA独测、实际页面/HTTP、部署或DDL分类迁移，C不提前实施。

已主动交QA及root新清单，不按旧94A4执行。本人仅设计/status写，无源/测试/浏览器/DB/服务动作。
