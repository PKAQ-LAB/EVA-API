# 菜单XML标准资源补齐执行记录

2026-10-28 backend_developer，cwd=C:/home/workspace/lab/EVA-API，cornerstone。经总控明确授权，仅正常本地Gradle精确资源任务；没有从隔离RUN复制产物或手抄XML。

实际命令：

```text
C:/home/DevApp/gradle/bin/gradle.bat --offline --no-daemon --no-parallel --max-workers=1 --no-build-cache --no-configuration-cache --no-configure-on-demand --console=plain --project-cache-dir .gradle/authorized-menu-resource-dryrun-cache-20261028 :eva-web:eva-web-sys:eva-web-sys-service:processResources --rerun-tasks
```

执行前685项冻结输入汇总SHA与8403交接一致；源资源完整16项、目标15项仅缺AuthorizedMenu.xml。此前配置dry-run核实际SourceSet/ProcessResources包含该XML、无include/exclude限制、图仅该任务。正式执行不使用dry-run审计init，控制台实际仅一项Task、BUILD SUCCESSFUL in 9s、1 actionable task: 1 executed，exit0；没有compile/test/clean/PG/observer任务。正常Gradle配置更新根Problems报告，不删除或改写其内容。

执行后源16项未变化、目标16项全部同源SHA，无额外目标资源；已有四class字节不变。9016仍PID32020，启动时间14:59:27+08，未停止/重启服务。新XML标准输出mtime15:15:46.024844+08，晚于当前进程启动，不能据磁盘补齐声称当前factory已加载。

技术记录（仅文件路径/hash与进程允许字段）：

```json
{
  "task": ":eva-web:eva-web-sys:eva-web-sys-service:processResources",
  "exitCode": 0,
  "executedTasks": 1,
  "sourceFiles": 16,
  "destinationBeforeFiles": 15,
  "destinationAfterFiles": 16,
  "addedDestination": [
    "mapper/AuthorizedMenu.xml"
  ],
  "changedExistingDestination": [],
  "extraDestination": [],
  "sourceSHA256": {
    "i18n/sys_en_US.properties": "C9C5FBBD696B0C307A47EEDE3CA44596DDD19A2CF8F56D9A53208FCEFE4A0FFB",
    "i18n/sys_zh_CN.properties": "A7FFA8D0D9A482E4DE4DBAB7417497AA03EC7031BF606E13A5AEC168B0C0AC10",
    "mapper/AuthorizedMenu.xml": "0F14C0C7EB3B572E58957CB9B2AE40E0C0792BFFACAB2B856EBF6DF288329F26",
    "mapper/Dict.xml": "CB0B699CD14119DCB4606030D666D460A4B061560570EF49E90427F8290151D5",
    "mapper/DictItem.xml": "4FA692F7CEC31770EF442E1821F6A8BE24C355933685C98845C76DF482513B9C",
    "mapper/Module.xml": "4ED87CFCF2597F96159DFE836F92F01287480D30C29346A48ED294A506062853",
    "mapper/Organization.xml": "61AB9326944D30214EB4514326FE7C3DE57AB2E6BB9FD22EE7659B9D9FFCBC2E",
    "mapper/Post.xml": "FD1FCFDFA1B7CFFC5F8221C95881CCD04E52C7AD372B28E0A942F5DC98B501F8",
    "mapper/Role.xml": "8DA6CDE2CF1E0D5453B515DD260A249A9C5D506310CC34DC10124955593429C5",
    "mapper/RoleModule.xml": "173BFFB619467D12D8E38DC208249243C398951C184760C6A72D29439C54BAE4",
    "mapper/TenantAuthorization.xml": "8333E20C68BF186CF3B57ADF3F122D2887F0DAF97DBEF83532971E5D73AC7B70",
    "mapper/TenantPackageResource.xml": "442F91955C933B855B428E5C038F81C234C73683BDB829A4C7691D0FF593E428",
    "mapper/TenantResource.xml": "EFC6C042941B332B8516C57440F2F67F829ADB3B337A298D0ECEA36D8EBC3FB1",
    "mapper/User.xml": "5ED33960854D9203F6AE3D9D1F0F5D8D4F2F822409C49D6169B097C93A89B9E9",
    "mapper/UserRoleRef.xml": "014BD54455B20E07580AC7F447ACAC8226C10A72B9C26AA8D1B33DE54957CAAD",
    "META-INF/additional-spring-configuration-metadata.json": "79E41F3AE7D3ADFED429A68C5447FAB77F0DE884E21F5A9EE15DFBCB1019A1BA"
  },
  "existingClassSHA256": {
    "org/pkaq/sys/user/ctrl/AuthCtrl.class": "7A58F65CDB529469D61CA56B989070D0E6E083527E8C95040C29FA5FED5B8336",
    "org/pkaq/sys/module/service/AuthorizedMenuService.class": "F27E32BF89CCBC120472E0D53CA79EA11812DFF9F504FBB91FFCFC1626F637F7",
    "org/pkaq/sys/module/mapper/AuthorizedMenuMapper.class": "B58A3AB2B243161F61703E67680AEA4F97E9ED12FD91E96AE7C29CB6932953C3",
    "org/pkaq/sys/module/mapper/AuthorizedMenuRow.class": "DFACC99FED7A1CEDD08F5C26266235D878D2BC3F5E302060E16C59C9F10CED5F"
  },
  "listenerBefore": {
    "PID": 32020,
    "StartedLocal": "2026-10-28 14:59:27 +08:00"
  },
  "listenerAfter": {
    "PID": 32020,
    "StartedLocal": "2026-10-28 14:59:27 +08:00"
  },
  "xmlMtime": "2026-10-28T15:15:46.024844+08:00",
  "verifiedAt": "2026-10-28T15:16:09.545950+08:00",
  "frozenInputClosureSHA256": "C20A55D60069C01C5B55F5D8E07ECAC413CC33C26778D2DF2D3A74D309D745AC"
}
```

只证明标准资源输出已补齐；不是测试通过、真实菜单响应通过或S权限链完成。编码helper8403、init、生产源、测试、配置和旧RUN未改，ecc70444观察无效仍保留，不复用其产物。没有HTTP/DB/Redis/IDE/服务/提交推送操作。由总控请用户正常重启后再安排菜单与业务SELF验收。
