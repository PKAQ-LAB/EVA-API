package tech.yunyue.websocket.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import tech.yunyue.core.threaduser.ThreadUserHelper;
import tech.yunyue.websocket.constant.WebSocketConsts;

import java.util.Map;

/**
 * WebSocket握手拦截器的基类，用于在握手前后处理共用逻辑，并保存用户信息到WebSocketSession中。
 *
 * @author 茂茂AdamEve
 */
public abstract class BaseWebSocketInterceptor implements HandshakeInterceptor {

    /**
     * 在握手前处理共用逻辑，并保存用户信息到WebSocketSession中。
     *
     * @param request    握手请求对象
     * @param response   握手响应对象
     * @param wsHandler  WebSocket处理器
     * @param attributes 存储WebSocket会话属性的Map
     * @return 握手是否成功
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public final boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                         Map<String, Object> attributes) throws Exception {
        attributes.put(WebSocketConsts.USER_INFO_KEY, ThreadUserHelper.getCurrentUser());
        // 在这里可以添加一些共用的逻辑
        return handleBeforeHandshake(request, response, wsHandler, attributes);
    }

    /**
     * 在握手后处理共用逻辑。
     *
     * @param request    握手请求对象
     * @param response   握手响应对象
     * @param wsHandler  WebSocket处理器
     * @param ex         握手过程中可能抛出的异常
     */
    @Override
    public final void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                     Exception ex) {
        // 在这里添加一些共用的逻辑
        handleAfterHandshake(request, response, wsHandler, ex);
    }

    // 子类必须实现的方法
    protected abstract boolean handleBeforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                     WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception;

    // 子类必须实现的方法
    protected abstract void handleAfterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                 WebSocketHandler wsHandler, Exception ex);
}
