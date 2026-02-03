package com.example.notification_service.controller;

import com.example.notification_service.dto.NotificationDto;
import com.example.notification_service.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController {

    private final WebSocketSessionManager sessionManager;

    public NotificationController(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * Sends a test notification to the specified user.
     * 
     * @param notification the notification to send
     * @return a map containing the status of the notification
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> sendTestNotification(@RequestBody NotificationDto notification) {
        log.info("Sending test notification to user: {}", notification.getUsername());

        boolean hasSession = sessionManager.hasActiveSession(notification.getUsername());

        if (hasSession) {
            sessionManager.sendToUser(notification.getUsername(), notification);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification sent to " + notification.getUsername()));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No active WebSocket session for user: " + notification.getUsername(),
                    "activeSessions", sessionManager.getActiveSessionCount()));
        }
    }

    /**
     * Gets the status of the WebSocket server.
     * 
     * @return a map containing the status of the WebSocket server
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "activeSessions", sessionManager.getActiveSessionCount(),
                "status", "running"));
    }

    /**
     * Gets the status of the WebSocket connection for a specific user.
     * 
     * @param username the username of the user
     * @return a map containing the status of the WebSocket connection for the user
     */
    @GetMapping("/status/{username}")
    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable String username) {
        boolean connected = sessionManager.hasActiveSession(username);
        return ResponseEntity.ok(Map.of(
                "username", username,
                "connected", connected));
    }
}
