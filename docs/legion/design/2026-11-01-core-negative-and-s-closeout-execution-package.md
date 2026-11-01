# 核心负向与收尾：fresh 前像后的统一批准包

2026-11-01，cornerstone。当前仅完成获准的一次 dev 只读前像及源码设计；权限、POST edit、禁用、逻辑删除、服务与缓存均未执行。请由总控把下述 A/B/C/D 一次提交用户批准，未选项保留为未覆盖。已完成项不重测，C3 跳过、平台 0 和真实多租户均排除。

## 已确认的本次前像

RUN：.gradle/core-closeout-ro-20261101T015928Z-fd0f3334。北京时间 09:59:30.184344→09:59:30.435294，eva/eva，单连接、repeatable read，开始/结束 transaction_read_only 与 default_transaction_read_only 均 on，22 条限定 SELECT，writes=0，rollback/close=true。四份 source/build 配置相等，dev/prod 目标区分及既有 dev 指纹吻合；未输出目标地址或凭据。result.json SHA256 D18132104F37909744CD1C2ED4811E7834EB56FFDADB97BF2BD5A26EC67751EF。

目录已核受限 ACL，仅当前用户、SYSTEM、Administrators 三主体 FullControl。private-preimage.json 含批准本机保存的原 profile name/code/dept，姓名不打印、不传播；未读取密码/hash、联系方式、头像值或日志正文。proposed-private-payloads.json 是拟议 body，不是执行授权，SHA256 F63DB2A672BABCF732EE39FF508CE4D3D194BED7290056804AC41D03F64A25A2。

| 固定对象 | 精确 ID | 本次实际状态 |
| --- | --- | --- |
| LIMIT / QA_LIM_20261023 | 2114719096971497474 | tenant0/deleted0/frozen0/revision0/permVer5；头像空；profile name非空、dept为D1；roles恰CUSTOM+ALL，posts=[] |
| PEER / QA_PEER_20261023 | 2113894345662021633 | tenant0/deleted0/frozen0/revision0/permVer6；头像空；profile name非空、dept为D1；roles恰SELF，posts=[] |
| SELF | 2113857683657629697 | scope0007，完整资源R，仅成员PEER |
| DEPT | 2113858118523068418 | scope0001，完整资源R，无成员 |
| TREE | 2113858458634985474 | scope0002，资源/成员均空 |
| CUSTOM | 2113870080799694849 | scope0003，data_org_ids原串仅D2；资源R，仅成员LIMIT |
| ALL | 2113870419179364353 | scope0000，资源R，仅成员LIMIT |
| R01 | 2111725377803354113 | 先按历史矩阵第34行定位，再实核CODE=ROLE_EVA_QA_20261018_R01；frozen0/deleted0，资源/成员均空；保留不写 |

五 S 角色均 frozen0/deleted0/revisionNULL/tenantNULL。S完整 refs 为上述8条资源关系和3条用户关系，无其它成员；R01也无资源引用。SYS_ROLE_RESOURCE 中 S 资源均为对应两条精确 GET，TREE/R01无派生行。

R={1000000000000011010 GET /sys/organization/list，1000000000000011011 GET /sys/organization/get/{id}}；E=1000000000000011012 POST /sys/organization/edit，四候选资源实际有效且所属模块有效。1000000000000011009 POST checkUnique 不新增，直接 edit 不调用该校验端点。

| 组织目标 | 精确 ID | 本次公开业务/作者前像 |
| --- | --- | --- |
| L | 2115435549295775746 | QA_SAMPLE_LIMIT_20261023 / QA_S_LIM_20261023；pid0/path为自身根；revisionNULL/frozen0/sort1/deleted0；create_id=modify_id=LIMIT |
| P 样本 | 2115579583561605122 | QA_SAMPLE_PEER_20261023 / QA_S_PEER_20261023；pid0/path为自身根；revisionNULL/frozen0/sort1/deleted0；create_id=PEER、modify_id=NULL |
| L/P 引用 | 同上 | 两者均无子组织、无账号profile引用；P符合SELF跨作者负向前提 |

四 S 部门保护集合为 P部门2113856261050376193、D1=2113856614139469826、D2=2113856917966462977、D1C=2113857168370606081。实际 P→D1/D2、D1→D1C，D1有两个账号profile引用。全部不移动、不删；O01=2111792835176267777、R01、P作者样本、PEER主行亦不选作删除目标。

## A 必需：有效 POST 负向、旧会话及原 S 禁用收尾

1. LIMIT保持现CUSTOM+ALL，用户本地 fresh 登录 T0，核固定主体及 fetchMenus 的原生 resourcePermissionEnabled=true，向P仅POST一次下列有效body。必须 HTTP200/原生success=false/code=0x000-00024；不能用GET拒绝、参数错误、过期或成功空结果代替。T0随后丢弃，不用于SELF阶段。
2. 管理员正常专用授权入口两笔保存：SELF完整资源 R→R∪{E}；LIMIT完整roles {CUSTOM,ALL}→{SELF}，PEER原SELF不动。SELF的E也临时影响PEER，明确纳入批准。不改scope、data_org_ids、部门、岗位或其它资源，不授checkUnique。
3. LIMIT fresh登录 T1，用同一个P body仅POST一次，期望 success=false/code=0x000-02001。源码OrganizationService.edit在原数据权限selectById找不到P后、任何写前失败；00024只说明资源未到位，400/只读保护/过期/成功空不是SELF数据拒绝。
4. 同一T1仅GET /api/sys/organization/get/2115435549295775746 一次，作为立即可用会话控制，不重跑六请求scope套件。保持原token仅本地内存并暂停。管理员正常将LIMIT完整roles {SELF}→[]，准确回读后，同一T1再GET同一路径一次，不新登录、不换token、不refresh。
5. saveRoles的真实差分会增permVer，JwtAuthFilter在资源判断前比较数据库版本，预期00023。结论严格为“旧会话撤权后不能沿用原权限”，不称JWT已销毁或必然过期。若只见00024，最多记原权限不可用，不把permVer机制标通过；普通00005/超时/冻结拒绝均不代证。
6. 旧T1完成后，才将PEER roles {SELF}→[]。清SELF完整3资源、DEPT/CUSTOM/ALL各2资源；TREE已空不保存。五角色无其它成员后禁用至frozen1（若选C先删除DEPT，仅冻其余4个）。两账号从本次确认0各禁用一次至1，版本正常推进、既有下线事件处理本账号会话；不盲toggle、不改密码。
7. 用户本地getpass对LIMIT、PEER各fresh登录一次，核无成功token并结合账号frozen1后值。当前LockedException被通用失败handler映射预计00000，不硬指定00003；00001不能当冻结证据。若选C，LIMIT的逻辑删除安排在此后，避免把删除后登录失败冒充禁用拒绝。

拟议的 P body（尚未发送）：

```json
{"id":"2115579583561605122","name":"QA_SAMPLE_PEER_20261023","code":"QA_S_PEER_20261023","pid":0,"revision":null,"frozen":0,"sort":1,"remark":"EVA_QA_NEG_20261101"}
```

只改自有QA备注，不传作者/修改者/tenant/path/isleaf。潜在意外放行会改变P备注以及正常revision/修改审计，甚至让SELF因modify_id变为LIMIT而获得可见性，因此必须将“有效POST可能误写这个固定QA样本”的风险列入用户批准。

**意外放行、未知响应、超时或持久值异常，一律立即停止后续动作，保留样本、审计及当前授权状态，报告root。禁止自动恢复业务、作者/审计或revision，禁止执行原草案中的前像UPDATE，禁止自行恢复宽角色。后续仅在用户另批最小业务回退后恢复；不得抹去误写痕迹。** 本次RO不是任何恢复写入授权。

本地小客户端待批准后才实现/独审：固定localhost:9016/api、LIM/PEER标签与上述路径，默认PLAN；getpass无回显，密码/MD5/token仅内存，不入文件/env/argv/聊天；固定请求预算，禁止代理/redirect/retry/Cookie fallback/refresh。T1暂停不心跳、有限等待、token失效则停止，不能登录新会话代T1。只输出阶段、HTTP、安全业务码、匹配布尔/请求数；不打印message/args/body/header/claims。旧只读脚本白名单不修改，无通用DSL/大框架。管理员操作仍现有正常入口，不新增真实管理员或其权限。

## B 可复用：PEER普通资料备注往返

本次RO已确认头像空、profile CODE=QA_PEER_20261023、NAME非空、DEPT_ID=D1，原值已本机受限封存。源码和现配置未覆盖字段策略，既有缓存MyBatis依赖的DbConfig构造字节码也确认为NOT_NULL默认；这是本地只读依赖检查，不是编译或运行测试。

无remark-only PATCH。saveUser每次均更新账号/推进permVer，并对profile.code/name/dept以ALWAYS同值重写。B须用户明确接受这三个同值字段写入及正常审计/revision/permVer推进；不能声称只触一个数据库列或精确回退技术版本。

固定payload已受限暂存：PEER id/account/revision0/frozen0/sort原值、原code/name/dept、remark=EVA_QA_PROFILE_20261101；password/avatar/tel/email/nickName/lastIp/lastLogin/roleIds/postId全部省略，不传空角色/岗位数组。姓名不打印。原头像空故不进入删文件/thumbnail分支；可选账号字段省略按NOT_NULL保留，profile三字段必须原样齐备。

安排在T0/T1之前：一次备注保存→正常详情读取→用详情实际fresh revision及原remark恢复一次→回读。StdVo包含revision，可取真实成功版本，不预猜+1；两次保存会正常推进PEER权限版本。仅改QA备注，部门/联系人/凭据/角色/岗位不改变。若现正常UI会附带未批准字段且没有已审固定payload通道，就暂停B，不硬save、不通过private浏览器HTTP绕过，也不新增PATCH。B未选或未满足通道约束，普通资料保存项仍未覆盖。

## C 可选：三个原S保留实体的逻辑删除例外

原包只禁用保留，需本次明确改为下列一次逻辑删除，不是自动授权：

- DEPT 2113858118523068418：在A撤完引用/资源后删，实际当前无成员、原资源R会先清空；随后其它4角色禁用保留。
- L 2115435549295775746：A两次POST及旧T1验证结束后再删；本次无子/无账号引用；P样本始终保留。
- LIMIT 2114719096971497474：完成账号禁用与fresh登录拒绝后再删；profile物理保留、roles/posts此前已空。PEER仅禁用保留。

回读须核主实体deleted>0且物理行仍存在、profile仍存在、批准refs精确解除。角色/账号源码会级联移除关系，无“普通成员/档案存在必拒删”业务守卫；不按旧矩阵制造引用拒删预期。正常关系refs物理解除符合既有业务语义，主实体/profile/资源定义不物理删。

组织源码先拒子节点02002，再拒账号引用4305。D1同时有D1C，因此对D1发删除只能先命中子节点守卫；本包不移动账号/删子树或新建叶部门来补单独的账号引用拒删。该分支如实未覆盖，不顺删P部门/D1。

## D 可选：M01/M02旧保留例外，当前引用已精确澄清

| 对象 | 本次真实前像 |
| --- | --- |
| M01=2111793871655895042 | CODE/NAME EVA_QA_20261018_M01；pid0/path为自身；deleted0/frozen1/revisionNULL/sort3/isleaf=false |
| M02=2111794099880558593 | CODE EVA_QA_20261018_M02、NAME EVA_QA_20261019_M02；唯一父M01；deleted0/frozen1/revisionNULL/sort1/isleaf=true |
| 唯一自有资源 | ID2111793873144872961，MAIN_ID=M01，GET /sys/module/list，deleted0；M02资源空 |
| 角色引用 | 精确SYS_ROLERES_REF为[]，包括R01、所有S角色、真实管理员/其它角色均无引用；R01自身资源/成员/派生行均空 |

**模块不需要新增grant，也没有任何待解除的QA或真实管理员资源refs。** 完整树只有M01/M02两节点，不能沿旧记录把M02当冻结0。本次所涉十表相关外键中仅见profile.account_id→account.id这一FK；用户触发器为[]，没有模块引用拒删的FK/trigger证据。

模块源码明确子节点拒绝02002；资源引用处理实际先清相关refs再逻辑删资源/模块，catch异常才映射MODULE_RESOURCE_USED，不是“有引用必拒”的业务守卫。本包不新授资源来造该场景。

用户若选D，正常目标解锁M01至0，明确允许级联使M02也从1→0；带子删M01一次应02002且业务状态不变；随后M02逻辑删→核M01叶子状态→M01及唯一自有资源逻辑删，核物理行保留。若正常UI没有相应入口则停报，不擅解锁其它节点或改走私有通道。任何预期拒绝意外成功按A的全局停止规则，保留痕迹、不自动恢复审计/版本或业务字段。

R01/O01不写，D1/P部门等不动；如果执行前出现任何新角色引用、额外子节点或状态漂移，立即停，不按本次快照自动解除后来的授权。

## 最终一次批准需要说清的范围

A：批准固定P的两次有效负向POST风险、SELF临时E/影响PEER、LIMIT完整角色替换、原T1保留撤权验证，以及最终两账号/五角色撤权禁用；失败仅停止报告不自动恢复。

B：是否接受PEER QA备注往返的实际固定payload及profile同值重写，姓名只在受限本机使用；不接受则不硬做。

C：是否把DEPT/L/LIMIT从原保留规则改为上述三个实体的逻辑删除；未选不能把禁用当删除通过。

D：是否批准旧M01/M02同时解锁、一次父带子拒删及子→父和唯一自有资源逻辑删除；当前引用为空，无真实管理员权限变更。

本次只有RO证据和拟议payload落地，未实现/运行新客户端、未做任何HTTP/授权/冻结/删除/回退，连接窗口已关闭。此快照是09:59时刻事实，不是后续永不漂移保证；执行前正常入口确认身份/无草稿及目标未漂移，未知就停，不扩大目标或反复DB窗口。
