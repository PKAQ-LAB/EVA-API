1.bootstrap.yml失效,导致nacos报错Param 'serviceName' is illegal, serviceName is blank

根据报错可知,就是注册服务时候没有服务名,默认使用的是spring.application.name,我的确设置了;于是查阅网上相关问题及查看代码; spring cloud 2020以后的版本,默认不优先读取bootstrap.yml了; 相关链接:https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#config-first-bootstrap

解决方法: 添加bootstra启动器依赖:
```
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-bootstrap</artifactId>
    </dependency>
```

2.#  Failed to start bean ‘documentationPluginsBootstrapper’; nested exception is java.lang.NullPointerException
#  Springboot2.6以后将SpringMVC 默认路径匹配策略从AntPathMatcher 更改为PathPatternParser，导致出错 需要配置
```yaml
spring:
    mvc:
        pathmatch:
            matching-strategy: ant_path_matcher
```
