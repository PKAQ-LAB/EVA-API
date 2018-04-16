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

## 目录说明

+ eva   
    |-ui : 前台UI   
    |-web : 单体工程整合层,可将任意api-*工程接入统一发布   
    |-docs : 文档相关   
        |-config : 环境配置   
        |-generator : mybatis generator   
    |-mobile : 手机端工程  
    |-api-core : 核心包-框架核心   
    |-api-sys : 基础管理    
    |-api-jxc : 进销存业务管理   
    |-addon-swagger : 插件配置,swagger支持   
    
    