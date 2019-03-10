FROM mcr.microsoft.com/java/jre:11u2-zulu-alpine
MAINTAINER W.F # pkaq@msn.com
VOLUME /tmp
COPY *.jar /opt/app.jar
EXPOSE 9009
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]
