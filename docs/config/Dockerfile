# 使用zulujdk
#FROM mcr.microsoft.com/java/jre:17-zulu-alpine
# 使用带有skywalking-agent的jdk
FROM apache/skywalking-java-agent:8.6.0-jdk11
# 作者
MAINTAINER pkaq@msn.com
ENV SKYWALKING_COLLECTOR_BACKEND_SERVICE=""
ENV APPLICATION_NAME=""
ENV JAVA_TOOL_OPTIONS="-javaagent:/skywalking/agent/skywalking-agent.jar=agent.service_name=$(APPLICATION_NAME),collector.backend_service=$(SKYWALKING_COLLECTOR_BACKEND_SERVICE)
# 映射/tmp到主机
VOLUME /tmp
#将打包好的spring程序拷贝到容器中的指定位置
COPY *.jar /opt/app.jar
#容器对外暴露9006
EXPOSE 9006
# 容器启动后执行的命令
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]