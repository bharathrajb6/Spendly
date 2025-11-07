package com.example.goal_service.util;


import com.example.goal_service.dto.request.GoalRequest;

public class GoalValidator {


    /**
     * Validates a goal request.
     *
     * @param request the goal request object to validate
     * @throws IllegalArgumentException if the goal request is invalid
     */
    public static void validateGoal(GoalRequest request) {

        // Check if goal name is null or empty
        if (request.getGoalName() == null || request.getGoalName().isEmpty()) {
            throw new IllegalArgumentException("Goal name cannot be empty");
        }

        // Check if target amount is null or negative
        if (request.getTargetAmount() <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than zero");
        }

        // Check if deadline is null
        if (request.getDeadline() == null) {
            throw new IllegalArgumentException("Deadline cannot be empty");
        }

        if (request.getDeadline().before(java.util.Calendar.getInstance().getTime())) {
            throw new IllegalArgumentException("Deadline must be in the future");
        }

        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
    }
}
