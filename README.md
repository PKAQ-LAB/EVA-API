## 运行方式

1.启动后台 `web/../Booter`

```java
run main
```

2.启动前台

```
yarn install
yarn start
```

3.访问`http://localhost`

api(libs.web}"){
		exclude module: 'spring-boot-starter-tomcat'
	}
libs.undertow}",

## 目录说明

+ EVA   
    |-web : 前台UI   
    |-mobile : 手机端工程  
    |-eva-web : 单体工程整合层,可将任意eva-*工程接入统一发布   
    ------- 服务包 -------
    |-eva-core : 核心包-框架核心    
        |-src/main/resources   
            |-db/migration : flyway脚本   
            |-spy.properties : p6spy脚本   
            |-mapper : mybatis sql文件   
            |-i18n : 国际化文件  
            |-logback : 日志配置
            |-mybatis-config : myabtis配置   
    |-eva-sys : 基础管理 + 鉴权   
    |-eva-jxc : 进销存业务管理   
    ------- 可选增强包 -------
    |-addon-weixin 微信接入  
    |-addon-pay 支付接入
    |-addon-cache 缓存   
    ------- 独立项目包 -------           
    |-nerv-admin-server spring boot admin server端  
    |-nerv-generator 代码生成器
    |-nerv-license 证书生成/验证   
    |-docs : 文档相关   
        |-db : pdm   
        |-doc : 开发文档   
        |-config : 环境配置   
        |-electron : electron打包配置     
        |-generator : mybatis generator    
        
-----------------------------------------------------------------   

 - 连接池: Hikari
 - 配置加密: Jassypt
 - SQL监控: P6SPY
 - 数据库版本管理: Flyway
 - JSON: jackson
 - 日志: Logback
 - 工具包: Hutool
 - ORM: Mybatis, Mybatis-plus

JWT:
1.双token
2.加入设备id防止跨设备使用

请求:
1.请求头加入版本号/设备类型

逻辑删除
1.把 DELETED 放到索引最前面
2.超过6个月的数据归档（mongo或者不存），原表物理删除： 策略模式+SPI

日志：
可配置只记录那些包相关的日志