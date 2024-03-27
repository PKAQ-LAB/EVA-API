package tech.yunyue.websocket.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import tech.yunyue.websocket.handler.BaseWebSocketHandler;
import tech.yunyue.websocket.interceptor.BaseWebSocketInterceptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * WebSocket配置类，用于配置WebSocket处理器和拦截器。
 *
 * @author PKAQ
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired(required = false)
    private List<BaseWebSocketHandler> socketHandlers;

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 注册WebSocket处理器和拦截器。
     *
     * @param registry WebSocket处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 创建一个默认的拦截器实例
        BaseWebSocketInterceptor defaultInterceptor = getDefaultInterceptor();
        if (CollUtil.isNotEmpty(socketHandlers)) {
            socketHandlers.forEach(handler -> {
                registry.addHandler(handler, handler.socketPath())
                        .addInterceptors(Optional.ofNullable(handler.getInterceptor()).orElse(defaultInterceptor))
                        .setAllowedOrigins("*");
            });
        }
    }

    /**
     * 获取默认的WebSocket拦截器实例。
     *
     * @return 默认的WebSocket拦截器实例
     */
    private static BaseWebSocketInterceptor getDefaultInterceptor() {
        return new BaseWebSocketInterceptor() {
            @Override
            protected boolean handleBeforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                return true;
            }

            @Override
            protected void handleAfterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
                // 空实现
            }
        };
    }
}
