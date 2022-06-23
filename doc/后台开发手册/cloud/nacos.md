使用如下dockercompse-file进行构建
```dockerfile
version: "2"
services:
  nacos:
    image: nacos/nacos-server:latest
    container_name: nacos-standalone-mysql
    env_file:
      - env/nacos-standlone-mysql.env
    volumes:
      - ./standalone-logs/:/home/nacos/logs
      - ./init.d/custom.properties:/home/nacos/init.d/custom.properties
    ports:
      - "8848:8848"
      - "9848:9848"
      - "9555:9555"
    depends_on:
      - nacos_mysql
    restart: always
  nacos_mysql:
    container_name: nacos_mysql
    hostname: nacos_mysql
    image: nacos/nacos-mysql:8.0.16
    env_file:
      - env/mysql.env
    volumes:
      - ./mysql:/var/lib/mysql
    ports:
      - "9306:3306"
```

已知得问题：
1.启动过程中提示config_info表中缺少encrypted_data_key字段[version.2.1.x]
分别在config_info 和 history_config_info 这两个表里添加这两个字段就可以了， 应该是官方的docker脚本里没更新进去 我用的standalone-mysql8

2.创建时init.d/custom/properties会映射为文件夹[version.2.1.x]
提前建立好该文件

以下问题的统一解决方式为 配置中心的公共配置 dataid均加上.yaml 同时bootstrap配置时添加.yaml后缀
3.evaconfig放在shared-configs公有配置中无法读取[version.2.1.x]
放置在项目相关文件中

4.数据库、redis相关配置放置到shared-configs公有配置中无法读取[version.2.1.x]
放置在项目相关文件中

5.shared-configs无法正确读取[version.2.1.x]
新版本的dataId子配置无需加yaml后缀