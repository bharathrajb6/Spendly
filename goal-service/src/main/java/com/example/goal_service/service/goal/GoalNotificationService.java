package com.example.goal_service.service.goal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoalNotificationService {

    public void notifyGoalAchieved(String username, String goalName, double targetAmount) {
        log.info("[Notification] Goal achieved for user {}: {} (target: {})", username, goalName, targetAmount);
        // TODO: Integrate with centralized EmailService when available
    }
}
