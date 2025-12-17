package com.example.notification_service.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;

    public NotificationWebSocketHandler(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        // Client should send username as first message to register
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message: {}", payload);

        // First message from client should be the username for registration
        if (!payload.isEmpty()) {
            // Parse as registration message
            // Expected format: {"type": "register", "username": "john"}
            try {
                if (payload.contains("register")) {
                    String username = extractUsername(payload);
                    if (username != null) {
                        sessionManager.registerSession(username, session);
                        session.sendMessage(new TextMessage(
                                "{\"type\":\"connected\",\"message\":\"Successfully connected to notification service\"}"));
                    }
                }
            } catch (Exception e) {
                log.error("Error processing message: {}", e.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        // Note: Session will be cleaned up when next notification attempt is made to
        // this user
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
    }

    /**
     * Extract username from registration message
     * Expected format: {"type": "register", "username": "john"}
     */
    private String extractUsername(String payload) {
        try {
            // Simple extraction - in production, use proper JSON parsing
            int usernameStart = payload.indexOf("\"username\"") + 12;
            int usernameEnd = payload.indexOf("\"", usernameStart);
            if (usernameStart > 11 && usernameEnd > usernameStart) {
                return payload.substring(usernameStart, usernameEnd);
            }
        } catch (Exception e) {
            log.error("Error extracting username from payload: {}", e.getMessage());
        }
        return null;
    }
}
