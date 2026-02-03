package com.example.goal_service.service.goal;

import com.example.goal_service.exception.GoalException;
import com.example.goal_service.kafka.EmailDto;
import com.example.goal_service.kafka.EventProducer;
import com.example.goal_service.kafka.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoalNotificationService {

    @Autowired
    private EventProducer eventProducer;

    private final ObjectMapper objectMapper;

    public GoalNotificationService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Notifies the user that their goal has been achieved.
     * 
     * @param username     the username of the user who achieved the goal
     * @param goalName     the name of the goal that was achieved
     * @param targetAmount the target amount of the goal that was achieved
     */
    public void notifyGoalAchieved(String username, String goalName, double targetAmount) {
        log.info("[Notification] Goal achieved for user {}: {} (target: {})", username, goalName, targetAmount);

        // Send in-app notification via WebSocket
        sendInAppNotification(username, "🎯 Goal Achieved!",
                String.format("Congratulations! You've achieved your goal '%s' with target ₹%.2f", goalName,
                        targetAmount),
                "GOAL_ACHIEVED");

        // Send email notification
        EmailDto emailDto = new EmailDto();
        emailDto.setUserEmail(username);
        emailDto.setSubject("Your Goal - {} is completed.");
        emailDto.setBody("Your Goal has been completed. Congratulations!. \n Regards, \n Spendly Team.");

        try {
            String json = objectMapper.writeValueAsString(emailDto);
            eventProducer.sendTopic("send-email", json);
        } catch (Exception exception) {
            throw new GoalException("Error while sending email");
        }
    }

    /**
     * Sends an in-app notification to the user.
     * 
     * @param username the username of the user to send the notification to
     * @param title    the title of the notification
     * @param message  the message of the notification
     * @param type     the type of the notification
     */
    private void sendInAppNotification(String username, String title, String message, String type) {
        try {
            NotificationDto dto = new NotificationDto(username, title, message, type);
            String json = objectMapper.writeValueAsString(dto);
            eventProducer.sendTopic("create-notification", json);
            log.info("In-app notification sent for user: {}", username);
        } catch (Exception e) {
            log.error("Error sending in-app notification: {}", e.getMessage());
        }
    }
}
