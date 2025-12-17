package com.example.notification_service.kafka;

import com.example.notification_service.dto.NotificationDto;
import com.example.notification_service.websocket.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Listens to the "create-notification" Kafka topic and pushes notifications
     * to connected WebSocket clients.
     *
     * @param message the JSON string received from Kafka
     */
    @KafkaListener(topics = "create-notification", groupId = "notification-service")
    public void consumeNotification(String message) {
        log.info("Received notification from Kafka: {}", message);

        try {
            // The message might be double-encoded (JSON string wrapped in quotes)
            // First, try to unwrap if it's a quoted JSON string
            String jsonContent = message;
            if (message.startsWith("\"") && message.endsWith("\"")) {
                // It's a quoted string, need to parse it first to get the actual JSON
                jsonContent = objectMapper.readValue(message, String.class);
                log.info("Unwrapped JSON content: {}", jsonContent);
            }

            // Now parse the actual NotificationDto
            NotificationDto notification = objectMapper.readValue(jsonContent, NotificationDto.class);
            log.info("Parsed notification for user: {} - Title: {}", notification.getUsername(),
                    notification.getTitle());

            // Push notification to user via WebSocket
            if (sessionManager.hasActiveSession(notification.getUsername())) {
                sessionManager.sendToUser(notification.getUsername(), notification);
                log.info("Notification pushed to user {} via WebSocket", notification.getUsername());
            } else {
                log.warn("User {} is not connected. Notification will not be delivered.", notification.getUsername());
            }
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
        }
    }
}
