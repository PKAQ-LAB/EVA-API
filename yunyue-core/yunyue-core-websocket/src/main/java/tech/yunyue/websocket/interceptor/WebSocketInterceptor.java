package tech.yunyue.websocket.interceptor;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * @author 茂茂AdamEve
 * WebSocket拦截器，用于在WebSocket握手阶段执行鉴权逻辑。
 * 程序中的WebSocket拦截器必须继承该类。即一定要鉴权。
 */
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {
    private static final String JWT_FILTER_NAME = "jwtAuthFilter";

    /**
     * 在WebSocket握手前执行鉴权逻辑。
     *
     * @param request    握手请求
     * @param response   握手响应
     * @param wsHandler  WebSocket处理器
     * @param attributes 握手阶段的属性集合
     * @return 如果鉴权成功，返回true；否则返回false
     * @throws Exception 发生异常时抛出
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        Filter filter = SpringUtil.getBean(JWT_FILTER_NAME);
        try {
            // 调用jwtAuthFilter的doFilter方法，执行鉴权逻辑 不复制代码是因为该模块没有sa-token的依赖
            filter.doFilter(((ServletServerHttpRequest) request).getServletRequest(), ((ServletServerHttpResponse) response).getServletResponse(), (request1, response1) -> {
                // do nothing
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 握手成功后的处理，此处不执行具体逻辑。
     *
     * @param request   握手请求
     * @param response  握手响应
     * @param wsHandler WebSocket处理器
     * @param exception 握手过程中的异常（如果有）
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, @Nullable Exception exception) {
        // 握手成功后的处理，此处不执行具体逻辑
    }

}
