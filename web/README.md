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
-addon-i18n : 国际化
-addon-swagger: 文档


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