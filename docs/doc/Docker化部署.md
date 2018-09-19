# 使用 Docker 发布 Spring Boot 应用



# 引言

　　一个 `Spring Boot`  应用在编写完成后，可以方便的打包成一个 `jar` 文件直接运行。通常的做法是使用 `nohup java -jar app.jar &` 的方式使其在服务器后台运行。 这种方式对服务器环境不仅有着很强的依赖并且会产生一个随时间推移越来越大的 `nohup` 文件 ，虽然可以利用管道黑洞将其输出到未知领域，但是对于运行版本的管理仍然依赖于人肉备份的方式。

　　借助 `Docker`  不仅可以做到运行环境与服务器环境的隔离，同时可以方便的管理发布版本。通过版本 `tag`  不仅可以方便的回滚到相应版本，使用端口映射也可以方便的配合蓝绿发布。

# 版本

- Spring Boot： 2.0.5 Release
- Docker：
- Centos: 7.3

# 步骤

## 1.编写脚本

```dockerfile
# 基于哪个镜像
FROM daocloud.io/java:8
# 作者
MAINTAINER PKAQ #pkaq@msn.com
# 映射/tmp到主机
VOLUME /tmp
#将打包好的spring程序拷贝到容器中的指定位置
COPY ./build/lib/app.jar /opt/app.jar
#容器对外暴露9006
EXPOSE 9006
# 容器启动后执行的命令
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]

```



## 2.构建镜像

　	在 `Dockerfile` 所在目录，使用 `docker build -t MyApp:1.0 .`  这句话的意思是从当前目录的 `Dockerfile` 文件构建一个名为 `MyApp` ，版本为 `1.0`  的镜像。

## 3.运行容器

　　使用 `docker run -d -e "SPRING_PROFILES_ACTIVE=dev" -p 9006:9006 --name Myapp1.0 Myapp:1.0`

　　上面操作的意思是 

- `-d` : 以守护进程运行一个容器

- `-e` : 为容器传递一个环境变量

- `-p`：映射容器端口 9006 到宿主机的 9006

- `--name`：指定容器的别名


　　下面是针对容器操作的几个常用命令：   

- 启动容器：`docker start Myapp1.0 `

- 停止容器：`docker stop Myapp1.0 `
- 删除容器：`docker rm Myapp1.0 `


## 4.应用升级

　　要升级应用版本时，需要重复步骤 2-3 构建新的 `Dockerfile` 镜像。需要注意的是不要忘记修改构建时的版本号，此时可以通过 `-p` 参数在与上一个版本不同的端口上启动一个新的容器。配合 `LB` 工具（如 `nginx`）切换到新的容器上实现蓝绿部署。

　　如果发布出现问题，如果应用了蓝绿部署只需要切换回原来运行端口即可。若未使用蓝绿部署的方式，那么也只需要 `stop` 有问题的版本，重新 `start` 即可。

