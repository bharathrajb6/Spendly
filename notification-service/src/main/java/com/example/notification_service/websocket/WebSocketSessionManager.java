package com.example.notification_service.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionManager {

    // Map of username to WebSocket session
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public WebSocketSessionManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Registers a WebSocket session for a user, mapping their username to the
     * active session.
     *
     * @param username the username of the user
     * @param session  the WebSocket session to register
     */
    public void registerSession(String username, WebSocketSession session) {
        sessions.put(username, session);
        log.info("Registered WebSocket session for user: {}", username);
    }

    /**
     * Removes the WebSocket session associated with a user.
     *
     * @param username the username of the user whose session should be removed
     */
    public void removeSession(String username) {
        sessions.remove(username);
        log.info("Removed WebSocket session for user: {}", username);
    }

    /**
     * Sends a notification object to a specific user via their active WebSocket
     * session.
     *
     * @param username     the username of the recipient
     * @param notification the notification object to be serialized and sent
     */
    public void sendToUser(String username, Object notification) {
        WebSocketSession session = sessions.get(username);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(json));
                log.info("Sent notification to user: {}", username);
            } catch (IOException e) {
                log.error("Error sending notification to user {}: {}", username, e.getMessage());
            }
        } else {
            log.warn("No active WebSocket session found for user: {}", username);
        }
    }

    /**
     * Broadcasts a notification object to all currently connected users.
     *
     * @param notification the notification object to be broadcasted
     */
    public void broadcast(Object notification) {
        sessions.forEach((username, session) -> {
            if (session.isOpen()) {
                sendToUser(username, notification);
            }
        });
    }

    /**
     * Checks if a user currently has an active and open WebSocket session.
     *
     * @param username the username to check
     * @return true if the session exists and is open, false otherwise
     */
    public boolean hasActiveSession(String username) {
        WebSocketSession session = sessions.get(username);
        return session != null && session.isOpen();
    }

    /**
     * Retrieves the total count of currently active and open WebSocket sessions.
     *
     * @return the number of active sessions
     */
    public int getActiveSessionCount() {
        return (int) sessions.values().stream().filter(WebSocketSession::isOpen).count();
    }
}
