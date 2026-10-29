# java.io.tmpdir门禁读取API窄修交审

2026-10-28 backend_developer。总控授权仅菜单init对此单键的读取/安全诊断，Python8403、生产五文件、所有测试和其它门禁冻结，未运行Gradle/PG/真实Task或observer。

## 已证机制及证据边界

本机已安装Gradle9.5.1字节码：Test.getSystemProperties转发DefaultJavaForkOptions，后者只返回JvmOptions.mutableSystemProperties。JvmOptions.systemProperty会将java.io.tmpdir等IMMUTABLE_SYSTEM_PROPERTIES键存入另一map，getAllJvmArgs则合并mutable和immutable JVM参数。原init用systemProperty正确设置此键，再用systemProperties回读会缺失，造成路径门禁误拒，不是已证明的临时目录漂移。

b0c6643f未在门禁前保存四键值；实际缺值/类型不能从通用错误日志全部还原。TEMP/TMP当前API是普通Map.put，未获得Windows大小写漂移实证，因此未修改两环境键或unpack读取。原22类208通过是QA该run的范围结论，web-booter4类止于doFirst，不能整体绿或当本修已验证。

## 精确差异

只改.gradle/menu-isolated-20261028.gradle：从test.allJvmArgs精确提取java.io.tmpdir键，裸-Djava.io.tmpdir也计入候选；总数必须恰1、形式必须key=且提取值严格等RUN/pg/tmp的canonicalPath，空值、缺失、重复/冲突均拒，不trim/toString兜底，不打印整args。不变更此键的原systemProperty设置。

ot.epg.working-dir仍原mutable读取且严格等RUN/pg/unpack；TEMP/TMP仍原精确键和值判断。成功web-booter-pg-jvm.json的temporary改为已验证提取值，避免继续记null。门禁前web-booter-pg-directory-check.json仅包含四批准键的source/type/count/matches技术字段，不输出完整环境/JVM参数或任意实际路径值。

## 最终身份及自检

- 新init SHA256：7AFEA135B5DF8D8E95613CAAB707B543CE7DB0BC369ADC3379D4AE2536078BB8。
- Python仍8403CB7DB4E291F8EDEA2AFBFD8168599F3BFE7B44FABFC8CB966FB84E3C553F。
- 新menu测试仍A78269ABACA1B9B871F4969D40B74F800C4B11BE0D8DDECBE6739A5159472D28。
- 685项输入纯读取汇总SHA256：8BCF589E7D5B761A4A690756EFA2E20FA427BDF0032A1ED8E9DA4FB80B4A48AB。

用已安装groovy-4.0.29.jar的GroovyMain只在内存运行从实际init提取的这段表达式，test为合成Map，不初始化Gradle。2正例（唯一正确参数、带无关普通参数）通过；8负例（缺参、重复同值、重复冲突、错路径、单裸键、正确加裸键、空值、相近其它键）均拒绝；没有目录创建。该结果不证明真实Gradle Test动态getter或实际JVM启动参数已验收。

当前仅交designer独审精确单键差异，不授权新run或复用b0c6643f产物；实际编译/四PG类及whole-JVM有效目录仍待总控另授验证。无服务/IDE/UI/HTTP/DB/Redis/提交推送操作。
