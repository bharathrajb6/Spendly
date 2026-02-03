package com.example.transaction_service.service.budget;

import com.example.transaction_service.exception.TransactionException;
import com.example.transaction_service.kafka.EmailDto;
import com.example.transaction_service.kafka.EventProducer;
import com.example.transaction_service.kafka.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BudgetNotificationService {

    @Autowired
    private EventProducer eventProducer;

    private final ObjectMapper objectMapper;

    public BudgetNotificationService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Notifies the user if their budget has been exceeded.
     * 
     * @param userId   The ID of the user.
     * @param category The category of the budget.
     * @param amount   The amount spent.
     * @param limit    The budget limit.
     */
    public void notifyOverspending(String userId, String category, double amount, double limit) {
        double exceeded = amount - limit;
        log.info("[Notification] User {} exceeded {} budget by {} (limit {}, spent {}).", userId, category, exceeded,
                limit, amount);

        // Send in-app notification via WebSocket
        sendInAppNotification(userId, "⚠️ Budget Exceeded",
                String.format("You've exceeded your %s budget by ₹%.2f", category, exceeded),
                "BUDGET_EXCEEDED");

        // Send email notification
        EmailDto emailDto = new EmailDto();
        emailDto.setUserEmail(userId);
        String subject = "⚠️ Overspending Alert – Please Review Your Recent Expenses";
        emailDto.setSubject(subject);

        String body = String.format(
                "Hi %s,\n\nWe noticed that your recent spending has exceeded the limit you set in your budget.\nHere are the details:\n\t•\tCategory: %s\n\t•\tMonthly Budget: ₹%.2f\n\t•\tAmount Spent: ₹%.2f\n\t•\tExceeded By: ₹%.2f\n\nWe recommend reviewing your transactions and adjusting your budget or spending to stay on track with your financial goals.\n\nIf these expenses were expected, you can update your budget anytime within the Spendly app.\n\nIf you need help or have any questions, feel free to reach out—we're here to support your financial journey.\n\nWarm regards,\nTeam Spendly\n",
                userId, category, limit, amount, exceeded);

        emailDto.setBody(body);

        try {
            String json = objectMapper.writeValueAsString(emailDto);
            eventProducer.sendTopic("send-email", json);
        } catch (Exception exception) {
            log.error("Error while sending email");
            throw new TransactionException("Error while sending email");
        }
    }

    /**
     * Sends an in-app notification to the user.
     * 
     * @param username The username of the user.
     * @param title    The title of the notification.
     * @param message  The message of the notification.
     * @param type     The type of the notification.
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
