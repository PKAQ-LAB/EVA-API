package tech.yunyue.websocket.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author PKAQ
 */
@Slf4j
public abstract class WebSocketHandler extends AbstractWebSocketHandler {

    public abstract String socketPath();

    /**
     * socket连接成功后触发
     *
     * @param session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 获取当前用户
        String key = session.getId();
        log.info("【有新的客户端连接了】：{}", key);
        WebSocketSessionManager.add(key, session);
        log.info("【websocket消息】有新的连接，总数为:" + WebSocketSessionManager.SESSION_POOL.size());
    }

    /**
     * 获得客户端传来的消息
     *
     * @param session
     * @param message
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端发送普通文件信息时触发
        String payload = message.getPayload();
        log.info("【websocket消息】收到客户端消息:" + payload);
        Optional.ofNullable(handleTextMessage(payload)).ifPresent(response -> {
            try {
                session.sendMessage(response);
            } catch (IOException e) {
                log.info("【websocket消息】回复消息失败：" + e.getMessage());
            }
        });
    }

    protected abstract WebSocketMessage<?> handleTextMessage(String payload);


    /**
     * 客户端发送二进信息是触发
     *
     * @param session
     * @param message
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        ByteBuffer payload = message.getPayload();
        log.info("发送二进制消息:" + payload.toString());
        Optional.ofNullable(handleBinaryMessage(payload)).ifPresent(response -> {
            try {
                session.sendMessage(response);
            } catch (IOException e) {
                log.info("【websocket消息】回复消息失败：" + e.getMessage());
            }
        });
    }

    protected abstract WebSocketMessage<?> handleBinaryMessage(ByteBuffer payload);

    /**
     * 异常时触发
     *
     * @param session
     * @param exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // 获取当前用户
        String key = session.getId();

        log.error("用户错误,原因:" + exception.getMessage());
        WebSocketSessionManager.removeAndClose(key);
    }

    /**
     * socket连接关闭后触发
     *
     * @param session
     * @param status
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        // 获取当前用户
        String key = session.getId();
        WebSocketSessionManager.removeAndClose(key);
        log.info("【websocket消息】连接断开，总数为:" + WebSocketSessionManager.SESSION_POOL.size());
    }
}
