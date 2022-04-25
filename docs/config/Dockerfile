# �����ĸ羵��
FROM mcr.microsoft.com/java/jre:17-zulu-alpine
# ����
MAINTAINER W.F#pkaq@msn.com
# ӳ��/tmp������
VOLUME /tmp
#������õ�spring���򿽱��������е�ָ��λ��
COPY *.jar /opt/app.jar
#�������Ⱪ¶9006
EXPOSE 9006
# ����������ִ�е�����
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]
