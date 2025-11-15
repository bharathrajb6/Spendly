package com.example.transaction_service.service.budget;

import com.example.transaction_service.exception.TransactionException;
import com.example.transaction_service.kafka.EmailDto;
import com.example.transaction_service.kafka.EventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BudgetNotificationService {

    @Autowired
    private EventProducer eventProducer;

    public void notifyOverspending(String userId, String category, double amount, double limit) {
        double exceeded = amount - limit;
        log.info("[Notification] User {} exceeded {} budget by {} (limit {}, spent {}).", userId, category, exceeded, limit, amount);
        // Send notification to user

        EmailDto emailDto = new EmailDto();
        emailDto.setUserEmail(userId);
        String subject = "⚠\uFE0F Overspending Alert – Please Review Your Recent Expenses";
        emailDto.setSubject(subject);

        String body = String.format("Hi {},\n" + "\n" + "We noticed that your recent spending has exceeded the limit you set in your budget.\n" + "Here are the details:\n" + "\t•\tCategory: {}\n" + "\t•\tMonthly Budget: ₹{}\n" + "\t•\tAmount Spent: ₹{}\n" + "\t•\tExceeded By: ₹{}\n" + "\n" + "We recommend reviewing your transactions and adjusting your budget or spending to stay on track with your financial goals.\n" + "\n" + "If these expenses were expected, you can update your budget anytime within the Spendly app.\n" + "\n" + "If you need help or have any questions, feel free to reach out—we’re here to support your financial journey.\n" + "\n" + "Warm regards,\n" + "Team Spendly\n", userId, category, limit, amount, exceeded);

        emailDto.setBody(body);

        try {
            String json = new ObjectMapper().writeValueAsString(emailDto);
            eventProducer.sendTopic("send-email", json);
        } catch (Exception exception) {
            log.error("Error while sending email");
            throw new TransactionException("Error while sending email");
        }
    }
}
