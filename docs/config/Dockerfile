# 基于哪哥镜像
FROM mcr.microsoft.com/java/jre:11u2-zulu-alpine
# 作者
MAINTAINER W.F#pkaq@msn.com
# 映射/tmp到主机
VOLUME /tmp
#将打包好的spring程序拷贝到容器中的指定位置
COPY *.jar /opt/app.jar
#容器对外暴露9006
EXPOSE 9006
# 容器启动后执行的命令
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]
