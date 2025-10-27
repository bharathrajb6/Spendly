package com.example.goal_service.service;

import com.example.goal_service.dto.request.GoalRequest;
import com.example.goal_service.dto.response.GoalResponse;
import com.example.goal_service.exception.GoalException;
import com.example.goal_service.model.Goal;
import com.example.goal_service.repo.GoalRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.goal_service.util.GoalUtils.*;
import static com.example.goal_service.util.GoalValidator.validateGoal;

@Service
@Slf4j
public class GoalService {

    @Autowired
    private GoalRepo goalRepo;


    /**
     * Add a new goal
     *
     * @param username the username of the user adding the goal
     * @param request  the goal request object
     * @return the goal response object
     * @throws GoalException if there is an error while adding the goal
     */
    public GoalResponse addGoal(String username, GoalRequest request) {
        log.info("Adding a new goal for user: {}", username);

        // Validate the request
        validateGoal(request);

        // Create a new goal object
        Goal goal = createGoal(request);
        goal.setUsername(username);

        // Save the goal to the database
        try {
            log.info("Saving goal to database: {}", goal);
            goalRepo.save(goal);
        } catch (Exception exception) {
            log.error("Error while saving goal: ", exception);
            throw new GoalException("Error while saving goal", exception);
        }

        log.info("Goal saved successfully: {}", goal.getGoalId());
        return getGoalDetails(goal.getGoalId());

    }

    /**
     * Get the details of a goal
     *
     * @param goalId the id of the goal to get the details of
     * @return the goal response object
     * @throws GoalException if there is an error while getting the goal details
     */
    public GoalResponse getGoalDetails(String goalId) {
        log.info("Getting the details of a goal with id: {}", goalId);

        // Get the goal from the database
        Goal goal = goalRepo.findByGoalId(goalId)
                .orElseThrow(() -> new GoalException("Goal not found"));

        // Convert the goal object to a goal response object
        log.info("Converting goal object to a goal response object: {}", goal);
        return toGoalResponse(goal);
    }


    /**
     * Update a goal
     *
     * @param goalId  the id of the goal to update
     * @param request the goal request object
     * @return the goal response object
     * @throws GoalException if there is an error while updating the goal
     */
    public GoalResponse updateGoal(String goalId, GoalRequest request) {
        log.info("Updating goal with id: {}", goalId);

        // Check the goal id
        if (goalId == null || goalId.isEmpty()) {
            log.error("Goal id cannot be empty");
            throw new GoalException("Goal id cannot be empty");
        }

        // Get the goal from the database
        Goal goal = goalRepo.findByGoalId(goalId)
                .orElseThrow(() -> new GoalException("Goal not found"));

        try {
            log.info("Updating goal details: {}", request);
            // Update the goal details
            goalRepo.updateGoalDetails(request.getGoalName(), request.getTargetAmount(), request.getDeadline(), request.getStatus(), goalId);
        } catch (Exception exception) {
            log.error("Error while updating goal: ", exception);
            throw new GoalException("Error while updating goal");
        }

        // Return the updated goal details
        log.info("Returning updated goal details: {}", goalId);
        return getGoalDetails(goalId);

    }

    /**
     * Delete a goal
     *
     * @param goalId the id of the goal to delete
     * @throws GoalException if there is an error while deleting the goal
     */
    public void deleteGoal(String goalId) {
        log.info("Deleting goal with id: {}", goalId);

        // Check the goal id
        if (goalId == null || goalId.isEmpty()) {
            log.error("Goal id cannot be empty");
            throw new GoalException("Goal id cannot be empty");
        }

        // Get the goal from the database
        Goal goal = goalRepo.findByGoalId(goalId)
                .orElseThrow(() -> new GoalException("Goal not found"));

        try {
            // Delete the goal
            log.info("Deleting goal: {}", goal);
            goalRepo.delete(goal);
        } catch (Exception exception) {
            log.error("Error while deleting goal: {}", exception);
            throw new GoalException("Error while deleting goal", exception);
        }
    }
}
