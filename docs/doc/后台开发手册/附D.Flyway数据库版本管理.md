# 引言

　　`Flyway`是一款开源的数据库版本管理工具，它更倾向于规约优于配置的方式。Flyway可以独立于应用实现管理并跟踪数据库变更，支持数据库版本自动升级，并且有一套默认的规约，不需要复杂的配置，Migrations可以写成SQL脚本，也可以写在Java代码中，不仅支持Command Line和Java API，还支持Build构建工具和Spring Boot等，同时在分布式环境下能够安全可靠地升级数据库，同时也支持失败恢复等。

　　通常在项目上线后，由于需求的不断改进或功能的完善需要对个别字段进行调整或新增。如果采用传统方式直接修改开发和生产数据库但凡涉及人为操作总无法避免出错的可能。一种方法是避免直接修改数据库，所有数据库修改通过PDM去操作。当然毕竟PDM是花钱的，通过采用`Flyway`编写变更脚本的方式，可以使应用在启动时检查数据库变更并进行自动同步。

<!-- more -->

# 版本

- Flyway: 5.1.4
- Gradle: 4.10
- Spring Boot: 2.0.4

# 步骤

　　Flyway对数据库进行版本管理主要由Metadata表和6种命令完成，Metadata主要用于记录元数据，在Flyway首次启动时会创建默认名为`flyway_schema_history`的元数据表，该表用于记录版本变更日志、Checksum等信息。

　　Flyway脚本的命名规则如下：

![sql_migration_naming.png](https://upload-images.jianshu.io/upload_images/1899339-ffde311e00986800.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


- prefix: 可配置，前缀标识，默认值`V`表示Versioned，`R`表示Repeatable
- version: 标识版本号，由一个或多个数字构成，数字之间的分隔符可用点`.`或下划线`_`
- separator: 可配置，用于分隔版本标识与描述信息，默认为两个下划线`__`
- description: 描述信息，文字之间可以用下划线或空格分隔
- suffix: 可配置，后续标识，默认为`.sql`

　　要实现在`Spring boot`中使用`Flyway`进行版本管理需要做到如下步骤：

 	1. 添加依赖  
 	2. 来点配置
 	3. 编写脚本

　　这里示例以一个存在若干张表的数据库为例，为该库添加一张新的`test_alpha`表，然后追加一列`bravo`。   

1.添加依赖   

```
 compile "org.flywaydb:flyway-core:5.1.4"
```

2.来点配置 `application.yaml`   

```yaml
# flyway配置 对于非空数据库 需要开启此项避免报错
spring:
 flyway:
  baseline-on-migrate: true
```

3.准备脚本   

　　依据上文描述的命名规则，在`src\main\resources\db\migration`下添加你的数据库变更脚本，这里需要注意的是，由于我们是在非空数据库进行操作，所以这里的版本号是从`v2`开始。

`V2__Add_new_table.sql`

```sql
DROP TABLE IF EXISTS flyway_test_alpha;
CREATE TABLE `flyway_test_alpha`  (
	`id`          	int(11) AUTO_INCREMENT NOT NULL,
	`column_alpha`	varchar(30) NULL,
	PRIMARY
```
`V3__Add_column.sql`

```sql
ALTER TABLE `flyway_test_alpha`
	ADD COLUMN `column_bravo` varchar(25) NULL
```

现在启动你的应用，你会发现这两项操作会隐士完成了，实在是非常省心。

附录: Flyway配置详解

```properties
flyway.baseline-description= # 执行基线时标记已有Schema的描述
flyway.baseline-version=1 # 基线版本默认开始序号 默认为 1. 
flyway.baseline-on-migrate=false # 针对非空数据库是否默认调用基线版本 ， 这也是我们上面版本号从 2 开始的原因
flyway.check-location=false # 是否开启脚本检查 检查脚本是否存在 默认false
flyway.clean-on-validation-error=false # 验证错误时 是否自动清除数据库 高危操作！！！
flyway.enabled=true # 是否启用 flyway.
flyway.encoding=UTF-8 # 脚本编码.
flyway.ignore-failed-future-migration=true # 在读元数据表时，是否忽略失败的后续迁移.
flyway.init-sqls= # S获取连接后立即执行初始化的SQL语句
flyway.locations=classpath:db/migration # 脚本位置， 默认为classpath: db/migration.
flyway.out-of-order=false # 是否允许乱序（out of order）迁移
flyway.placeholder-prefix= # 设置每个占位符的前缀。 默认值： ${ 。 
flyway.placeholder-replacement=true # 是否要替换占位符。 默认值： true 。 
flyway.placeholder-suffix=} # 设置占位符的后缀。 默认值： } 。 
flyway.placeholders.*= # 设置占位符的值。
flyway.schemas= # Flyway管理的Schema列表，区分大小写。默认连接对应的默认Schema。
flyway.sql-migration-prefix=V # 迁移脚本的文件名前缀。 默认值： V 。 
flyway.sql-migration-separator=__ # 迁移脚本的分割符 默认双下划线
flyway.sql-migration-suffix=.sql # 迁移脚本的后缀 默认 .sql
flyway.table=schema_version # Flyway使用的Schema元数据表名称 默认schema_version
flyway.url= # 待迁移的数据库的JDBC URL。如果没有设置，就使用配置的主数据源。
flyway.user= # 待迁移数据库的登录用户。
flyway.password= # 待迁移数据库的登录用户密码。
flyway.validate-on-migrate=true # 在运行迁移时是否要自动验证。 默认值： true 。 
```



