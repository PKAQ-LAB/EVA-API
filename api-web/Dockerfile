FROM daocloud.io/java:8
MAINTAINER W.F # pkaq@msn.com
VOLUME /tmp
COPY *.jar /opt/app.jar
EXPOSE 9006
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]
