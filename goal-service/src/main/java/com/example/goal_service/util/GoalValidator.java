package com.example.goal_service.util;


import com.example.goal_service.dto.request.GoalRequest;

public class GoalValidator {


    public static void validateGoal(GoalRequest request) {

        if (request.getGoalName() == null || request.getGoalName().isEmpty()) {
            throw new IllegalArgumentException("Goal name cannot be empty");
        }

        if (request.getTargetAmount() <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than zero");
        }

        if (request.getDeadline() == null) {
            throw new IllegalArgumentException("Deadline cannot be empty");
        }

        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
    }
}
