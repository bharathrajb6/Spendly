package com.example.goal_service.service.goal;

import com.example.goal_service.exception.GoalException;
import com.example.goal_service.kafka.EmailDto;
import com.example.goal_service.kafka.EventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoalNotificationService {

    @Autowired
    private EventProducer eventProducer;

    public void notifyGoalAchieved(String username, String goalName, double targetAmount) {
        log.info("[Notification] Goal achieved for user {}: {} (target: {})", username, goalName, targetAmount);

        EmailDto emailDto = new EmailDto();
        emailDto.setUserEmail(username);
        emailDto.setSubject("Your Goal - {} is completed.");
        emailDto.setBody("Your Goal has been completed. Congratulations!. \n Regards, \n Spendly Team.");

        try {
            String json = new ObjectMapper().writeValueAsString(emailDto);
            eventProducer.sendTopic("send-email", json);
        } catch (Exception exception) {
            throw new GoalException("Error while sending email");
        }


    }
}
