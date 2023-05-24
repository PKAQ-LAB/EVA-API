package org.pkaq.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.pkaq.websocket.handler.WebSocketSessionManager;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * 消息发送工具类
 *
 * @author PKAQ
 */
@Service
@Slf4j
public class WebSocketService {
    /**
     * 发送消息
     */
    public void sendMsg(WebSocketSession session, String text) throws IOException {
        log.info("【websocket消息】 单点消息:" + text);
        session.sendMessage(new TextMessage(text));
    }

    /**
     * 广播消息
     */
    public void broadcastMsg(String text) throws IOException {
        log.info("【websocket消息】广播消息:" + text);
        for (WebSocketSession session : WebSocketSessionManager.list()) {
            session.sendMessage(new TextMessage(text));
        }
    }

    /**
     * 此为单点消息(多人)
     *
     */
    public void sendMsg(String[] userIds, String message) {
        for (String userId : userIds) {
            WebSocketSession session = WebSocketSessionManager.get(userId);
            if (session != null && session.isOpen()) {
                try {
                    log.info("【websocket消息】 单点消息:" + message);
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    log.error("【websocket消息发送错误】:" + e.getMessage());
                }
            }
        }
    }
}