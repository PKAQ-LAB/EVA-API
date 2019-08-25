package io.nerv.server.undertow;

import io.undertow.Undertow;
import io.undertow.server.ConnectorStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @Author: guoyankui
 * @DATE: 2018/5/20 5:47 PM
 *
 * 优雅关闭 Spring Boot undertow
 */
@Component
@Slf4j
public class GracefulShutdownUndertow implements ApplicationListener<ContextClosedEvent> {

    @Autowired
    private GracefulShutdownUndertowWrapper gracefulShutdownUndertowWrapper;

    @Autowired
    private ServletWebServerApplicationContext context;

    @Override
    public void onApplicationEvent(ContextClosedEvent contextClosedEvent){
        gracefulShutdownUndertowWrapper.getGracefulShutdownHandler().shutdown();
        try {
            UndertowServletWebServer webServer = (UndertowServletWebServer)context.getWebServer();
            Field field = webServer.getClass().getDeclaredField("undertow");
            field.setAccessible(true);
            Undertow undertow = (Undertow) field.get(webServer);
            List<Undertow.ListenerInfo> listenerInfo = undertow.getListenerInfo();
            Undertow.ListenerInfo listener = listenerInfo.get(0);
            ConnectorStatistics connectorStatistics = listener.getConnectorStatistics();
            while (connectorStatistics.getActiveConnections() > 0){}
        }catch (Exception e){
            log.error(" ---------------------------- 服务停止异常 ----------------------------");
            e.printStackTrace();
        }
    }
}