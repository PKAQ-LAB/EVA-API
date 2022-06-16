# 用percona ppm的docker容器进行数据库监控备份  

Percona Monitoring and Management是percona一款开源的用于管理和监控MySQL 和MongoDB性能的开源平台，通过PMM客户端收集到的DB监控数据用第三方软件Grafana画图展示出来，包括两个部分：PMM client：部署在每个监控数据库主机。搜集主机，数据库和查询分析数据等。PMM Server：汇集数据并展示。提供表，dashboards和graph的web界面。


# 2 docker部署pmm与mysql监控

## 2.1 下载PMM Server Docker镜像  

```
#版本可自选
docker create -v /opt/prometheus/data -v /opt/consul-data -v /var/lib/mysql -v /var/lib/grafana --name pmm-data percona/pmm-server:1.14.1 /bin/true
```

#2.2启动

```
docker run -d -p 80:80  --volumes-from pmm-data --name pmm-server --restart always percona/pmm-server:1.14.1
端口默认是 80 ，如果80端口被占用，可改为其它端口号   比如 81
```



#2.3查看docker运行状态

```
[root@open-falcon mysql]# docker ps
CONTAINER ID        IMAGE                       COMMAND                  CREATED             STATUS              PORTS                         NAMES
59455e7fa828        percona/pmm-server:1.14.1   "/opt/entrypoint.sh"     18 hours ago        Up 7 minutes        443/tcp, 0.0.0.0:81->80/tcp   pmm-server
```

#2.4浏览器访问

直接输IP地址

#2.5安装pmm-client客户端

```
wget https://www.percona.com/downloads/pmm-client/pmm-client-1.14.1/binary/tarball/pmm-client-1.14.1.tar.gz
tar -zxvf pmm-client-1.14.1.tar.gz
cd pmm-client-1.14.1 && ./installs
##此时你会发现可以使用pmm-admin指令
```

#2.6连接pmm Server

```
pmm-admin config --server ip地址
#注意，如果以上步骤docker run映射的端口不是80，比如为81，此时应该pmm-admin config --server ip地址:81
```

#2.7添加mysql监控

```
#添加的用户必须要有select以上的权限
pmm-admin add mysql --user root --password xxxx --host localhost(此处host可自定义)
```

#2.8查看列表状态

pmm-admin list 

#2.9查看网络状态

pmm-admin list 

#2.10查看日志

 ls /var/log/ | grep pmm 

#2.11

原理图

![1560819695369](C:\Users\xilai03\AppData\Local\Temp\1560819695369.png)



#3常用命令

管理PMM客户端：<https://www.percona.com/doc/percona-monitoring-and-management/pmm-admin.html#pmm-admin-add-mysql-metrics>