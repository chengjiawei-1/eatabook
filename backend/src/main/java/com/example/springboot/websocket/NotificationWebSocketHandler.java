package com.example.springboot.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.springboot.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    //从Session 获取 UserId
    private Long getUserId(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj == null) {
            return null;
        }
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        }
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        try {
            return Long.parseLong(userIdObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);

        if (userId != null) {
            userSessions.put(userId, session);
            log.info("用户 {} WebSocket连接建立，当前在线人数: {}", userId, userSessions.size());
        } else {
            log.warn("WebSocket连接建立失败：无法获取有效用户ID");
            session.close();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Long userId = getUserId(session);

        if (userId != null) {
            userSessions.remove(userId);
            log.info("用户 {} WebSocket连接关闭，当前在线人数: {}", userId, userSessions.size());
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void sendNotificationToUser(Long userId, Notification notification) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(Map.of(
                        "type", "NEW_NOTIFICATION",
                        "notification", notification
                ));
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("给用户 {} 发送WebSocket消息失败", userId, e);
            }
        }
    }
}