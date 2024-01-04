package tech.yunyue.websocket.config;

import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import tech.yunyue.websocket.handler.WebSocketHandler;

import java.util.List;
import java.util.Optional;

/**
 * @author PKAQ
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired(required = false)
    private List<WebSocketHandler> socketHandlers;
    @Autowired(required = false)
    @Qualifier("webSocketAuthInterceptor")
    private HandshakeInterceptor interceptor;

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        if (CollUtil.isNotEmpty(socketHandlers)) {
            socketHandlers.forEach(handler -> {
                registry.addHandler(handler, handler.socketPath())
                        .addInterceptors(Optional.ofNullable(handler.getInterceptor()).orElse(interceptor))
                        // 允许跨域，方便本地调试，生产建议去掉
                        .setAllowedOrigins("*");
                //.setAllowedOriginPatterns("*")
                //.withSockJS();
            });
        }
    }
}
