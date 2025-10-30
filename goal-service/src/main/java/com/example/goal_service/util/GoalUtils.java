package com.example.goal_service.util;

import com.example.goal_service.dto.request.GoalRequest;
import com.example.goal_service.dto.response.GoalResponse;
import com.example.goal_service.model.Goal;

public class GoalUtils {


    public static GoalResponse toGoalResponse(Goal goal) {

        GoalResponse goalResponse = new GoalResponse();
        goalResponse.setGoalId(goal.getGoalId());
        goalResponse.setGoalName(goal.getGoalName());
        goalResponse.setTargetAmount(goal.getTargetAmount());
        goalResponse.setSavedAmount(goal.getSavedAmount());
        goalResponse.setDeadline(goal.getDeadline());
        goalResponse.setUsername(goal.getUsername());
        goalResponse.setStatus(goal.getStatus());

        return goalResponse;
    }

    public static Goal createGoal(GoalRequest request) {
        Goal goal = new Goal();
        goal.setGoalId(java.util.UUID.randomUUID().toString());
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setDeadline(request.getDeadline());
        goal.setStatus(request.getStatus());
        goal.setSavedAmount(0);

        return goal;
    }

    public static double calculatePercentage(double savedAmount, double targetAmount) {
        return (savedAmount / targetAmount) * 100;
    }
}
