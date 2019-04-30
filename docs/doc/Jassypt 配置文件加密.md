
# 引言

　　[Jasypt](http://jasypt.org/) 是一个Java库，允许开发人员以很简单的方式添加基本加密功能，而无需深入研究加密原理。利用它可以实现高安全性的，基于标准的加密技术，无论是单向和双向加密。加密密码，文本，数字，二进制文件。并且可集成到Spring应用程序中，与Spring Security集成，加密的应用程序（即数据源）的配置等信息。

# 版本

- jasypt-spring-boot-starter ： 2.1.0
- Spring Boot : 2.0.4
- JDK 1.9

# 步骤
　　借助`jasypt-spring-boot-starter`给`Spring boot`应用配置加密非常简单。   

1. 引入依赖
  ```groovy
      compile "com.github.ulisesbocchio:jasypt-spring-boot-starter:${jasypt}"
  ```
2. 配置密码
　　在`application.yml`中配置加密需要使用的密钥。
  ```yaml
  jasypt:
  	encryptor:
    	password: eva
  ```
3. 加密内容
　　编写一个单元测试加密你需要加密的内容。
```java
@Autowired
StringEncryptor stringEncryptor;
@Test
public void encrypt() {
    System.out.println("PWD: " + stringEncryptor.encrypt("yourpassword"));
}
```
4. 修改配置文件,使用`ENC`包裹加密得到的密文即可。
```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://localhost:3306/jxc?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
    username: ENC(dghf0DZZJA+vQ95xgHz5iA==)
    password: ENC(TNbKfi0lHjpfPcdepEnR5oKHikoDfVr+/amPonxytqK5u+B8Pid7k4hmlGUOhv+6QxEuB3gjJPPn7L8ishuFYym/Gr59qRO6Uf5/XiyT+3FUTujtyybxUjTyo4CO1wKe+zubp27QVYM=)
```
# 法二
下载jasypt压缩包 解压后执行
encrypt input=root123 password=::Lazy@8 algorithm=PBEWITHMD5ANDDES


> 参考: https://github.com/ulisesbocchio/jasypt-spring-boot