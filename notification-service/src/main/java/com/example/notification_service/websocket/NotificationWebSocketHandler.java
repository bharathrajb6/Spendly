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

    /**
     * Handles the establishment of a WebSocket connection.
     * 
     * @param session the WebSocket session
     * @throws Exception if an error occurs
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        // Client should send username as first message to register
    }

    /**
     * Handles incoming text messages from WebSocket clients.
     * 
     * @param session the WebSocket session
     * @param message the incoming text message
     * @throws Exception if an error occurs
     */
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

    /**
     * Handles the closing of a WebSocket connection.
     * 
     * @param session the WebSocket session
     * @param status  the status of the connection close
     * @throws Exception if an error occurs
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        // Note: Session will be cleaned up when next notification attempt is made to
        // this user
    }

    /**
     * Handles transport errors that occur during WebSocket communication.
     * 
     * @param session   the WebSocket session
     * @param exception the exception that occurred
     * @throws Exception if an error occurs
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
    }

    /**
     * Extracts the username from a registration message.
     * 
     * @param payload the registration message
     * @return the extracted username
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
