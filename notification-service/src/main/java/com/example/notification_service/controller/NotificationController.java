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
     * Test endpoint to manually send a notification
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
     * Check WebSocket status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "activeSessions", sessionManager.getActiveSessionCount(),
                "status", "running"));
    }

    /**
     * Check if a specific user is connected
     */
    @GetMapping("/status/{username}")
    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable String username) {
        boolean connected = sessionManager.hasActiveSession(username);
        return ResponseEntity.ok(Map.of(
                "username", username,
                "connected", connected));
    }
}
