技术栈
- spring config
- shiro
- jwt
- mybatis plus
- druid
- fastjson

目录说明
-web：web接口
-ui：界面
-addon-swagger: 文档


国际化支持
```
ctrl层通过 locale("key")) 获取相应国际化信息
另外，通过此方式获取message内容可以统一管理message文本
```

实体类注解说明
```
@Data   -- lomobok 注解可以静态织入getter setter等
@Alias("organization") -- 设置别名，可以在mybatis xml中直接使用别名
@TableName("sys_organization") -- 当表名与实体名不一致时需显示指定表名

@TableId -- 主键ID
@TableField(exist = false) -- 虚拟列
@TableField(condition = SqlCondition.LIKE) -- 列的默认查询模式
```

mybatis generator    
```
java -jar mybatis-generator-core-1.0.3.jar -configfile generatorConfig.xml -overwrite
```
---
- 用户管理
    - 登陆
    - 退出
    - jwt(v2)
- 组织管理
- 模块管理
- 角色管理(v2)
- 国际化（v2）

druid 
  - 访问限制
  - 链接用户名密码加密
- https

答疑：
- 如果IDEA 提示mapper注入无法找到bean，请在mapper上添加@Repository注解

TODO
- flowable
- jwt
- truelicense
- 业务日志注解
- 特殊接口IP拦截注解

- 订单号 订单池