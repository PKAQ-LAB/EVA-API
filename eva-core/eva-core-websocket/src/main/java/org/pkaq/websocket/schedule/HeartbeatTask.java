package org.pkaq.websocket.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.websocket.handler.WebSocketSessionManager;
import org.pkaq.websocket.service.WebSocketService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * websocket心跳检测
 *
 * @author PKAQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HeartbeatTask {
    private final WebSocketService webSocketService;

    @Scheduled(cron = "0/2 * * * * *")
    public void run() {
        try {
            if (WebSocketSessionManager.list().size() > 0) {
                webSocketService.broadcastMsg("HeartBeat:" + System.currentTimeMillis() + "");
            }
        } catch (IOException e) {
            log.error("websocket 心跳检测异常");
        }
    }
}
