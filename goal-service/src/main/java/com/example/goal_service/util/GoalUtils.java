package com.example.goal_service.util;

import com.example.goal_service.dto.request.GoalRequest;
import com.example.goal_service.dto.response.GoalResponse;
import com.example.goal_service.exception.GoalException;
import com.example.goal_service.model.Goal;
import com.example.goal_service.model.GoalStatus;

public class GoalUtils {


    /**
     * Converts a Goal object to a GoalResponse object.
     *
     * @param goal the Goal object to convert
     * @return the GoalResponse object
     */
    public static GoalResponse toGoalResponse(Goal goal) {

        GoalResponse goalResponse = new GoalResponse();
        goalResponse.setGoalId(goal.getGoalId());
        goalResponse.setGoalName(goal.getGoalName());
        goalResponse.setTargetAmount(goal.getTargetAmount());
        goalResponse.setSavedAmount(goal.getSavedAmount());
        goalResponse.setDeadline(goal.getDeadline());
        goalResponse.setUsername(goal.getUsername());
        goalResponse.setStatus(goal.getStatus().toString());
        goalResponse.setProgressPercent(goal.getProgressPercent());

        return goalResponse;
    }

    /**
     * Creates a new Goal object from the given GoalRequest object.
     *
     * @param request the GoalRequest object to create the Goal object from
     * @return the Goal object created from the given GoalRequest object
     */
    public static Goal createGoal(GoalRequest request) {
        Goal goal = new Goal();
        goal.setGoalId(java.util.UUID.randomUUID().toString());
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setDeadline(request.getDeadline());

        GoalStatus goalStatus;
        try {
            goalStatus = GoalStatus.valueOf(request.getStatus());
        } catch (Exception exception) {
            throw new GoalException("Invalid Goal Status");
        }

        goal.setStatus(goalStatus);
        goal.setSavedAmount(0);
        return goal;
    }

    /**
     * Calculates the percentage of the goal's target amount that has been saved.
     *
     * @param savedAmount  the amount that has been saved
     * @param targetAmount the target amount of the goal
     * @return the percentage of the goal's target amount that has been saved
     */
    public static double calculatePercentage(double savedAmount, double targetAmount) {
        return (savedAmount / targetAmount) * 100;
    }
}
