package com.example.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String username;
    private String title;
    private String message;
    private String type;
    private LocalDateTime timestamp;

    public NotificationDto(String username, String title, String message, String type) {
        this.username = username;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }
}
