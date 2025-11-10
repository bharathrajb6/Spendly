package com.example.goal_service.service;

import com.example.goal_service.dto.request.GoalRequest;
import com.example.goal_service.dto.response.GoalResponse;
import com.example.goal_service.exception.GoalException;
import com.example.goal_service.model.Goal;
import com.example.goal_service.model.GoalStatus;
import com.example.goal_service.model.TransactionDto;
import com.example.goal_service.repo.GoalRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.example.goal_service.util.GoalUtils.*;
import static com.example.goal_service.util.GoalValidator.validateGoal;

@Service
@Slf4j
public class GoalService {

    @Autowired
    private GoalRepo goalRepo;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RestTemplate restTemplate;


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
        log.info("Validating the goal request: {}", request);
        validateGoal(request);

        // Create a new goal object
        log.info("Creating a new goal object from the request: {}", request);
        Goal goal = createGoal(request);
        goal.setUsername(username);

        double savedAmount = getSavingsAmountFromExistingGoal(username);
        log.info("Getting the saved amount from the existing goal for user: {} - Saved amount: {}", username, savedAmount);
        if (savedAmount != 0) {
            goal.setSavedAmount(savedAmount);
        }

        log.info("Calculating the progress percentage for the goal: {}", goal);
        goal.setProgressPercent(calculatePercentage(goal.getSavedAmount(), goal.getTargetAmount()));

        if (goal.getProgressPercent() >= 100) {
            goal.setStatus(GoalStatus.COMPLETED);
        }

        // Save the goal to the database
        log.info("Saving goal to database: {}", goal);
        try {
            goalRepo.save(goal);
            log.info("Goal saved successfully to the database: {}", goal.getGoalId());
            redisService.setData(goal.getGoalId(), goal, 3600L);
            log.info("Goal data saved to the cache: {}", goal.getGoalId());
        } catch (Exception exception) {
            log.error("Error while saving goal: ", exception);
            throw new GoalException("Error while saving goal", exception);
        }

        log.info("Returning the goal response: {}", goal.getGoalId());
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

        // Get the goal data from cache
        Goal goal = redisService.getData(goalId, Goal.class);

        if (goal == null) {
            // Get the goal from the database
            goal = goalRepo.findByGoalId(goalId).orElseThrow(() -> new GoalException("Goal not found"));
            redisService.setData(goalId, goal, 3600L);
        }

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
        boolean isGoalFound = goalRepo.isGoalFound(goalId);
        if (!isGoalFound) {
            throw new GoalException("Goal not found");
        }

        // Validate the goal data
        validateGoal(request);

        GoalStatus goalStatus;
        try {
            goalStatus = GoalStatus.valueOf(request.getStatus());
        } catch (Exception exception) {
            throw new GoalException("Invalid status");
        }


        try {
            log.info("Updating goal details: {}", request);
            // Update the goal details
            goalRepo.updateGoalDetails(request.getGoalName(), request.getTargetAmount(), request.getDeadline(), goalStatus, goalId);
            redisService.deleteData(goalId);
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
            log.error("Goal id cannot be empty.");
            throw new GoalException("Goal id cannot be empty");
        }


        if (!goalRepo.isGoalFound(goalId)) {
            throw new GoalException("Goal not found");
        }

        try {
            // Delete the goal
            log.info("Deleting goal ID : {}", goalId);
            goalRepo.deleteById(goalId);
            redisService.deleteData(goalId);
        } catch (Exception exception) {
            log.error("Error while deleting goal with id : {}", exception.getMessage());
            throw new GoalException("Error while deleting goal with id : " + exception.getMessage());
        }
    }


    /**
     * Listens to Kafka topic "transaction-added" and updates the saved amount of all goals for the given user.
     * If no goal is present for the user, the function does nothing.
     * For each goal, the saved amount is updated based on the transaction type and amount.
     * The updated saved amount and percentage for each goal is then saved to the database.
     *
     * @param transaction The transaction object received from Kafka
     */
    @KafkaListener(topics = "transaction", groupId = "goal-service", containerFactory = "transactionDataKafkaListenerContainerFactory")
    public void updateGoalAmountAfterTransaction(TransactionDto transaction) {
        log.info("Received transaction: {}", transaction);

        // Check if any goal is present for this user
        boolean isAnyGoalPresentForThisUser = goalRepo.isAnyGoalPrentForThisUser(transaction.getUsername());

        // If no goal is present, return
        if (!isAnyGoalPresentForThisUser) {
            log.info("No goal present for user: {}", transaction.getUsername());
            return;
        }

        // Get all the goals for this user
        List<Goal> goals = goalRepo.findGoalsBasedOnStatusForUser(transaction.getUsername(), GoalStatus.ACTIVE);

        // Update the saved amount for each goal
        for (Goal goal : goals) {
            // Update the saved amount and percentage for this goal
            goalRepo.updateGoalSavedAmountAndPercentage(transaction.getSavingsAmount(), calculatePercentage(transaction.getSavingsAmount(), goal.getTargetAmount()), goal.getGoalId());
            redisService.deleteData(goal.getGoalId());
            updateGoalStatus(goal.getGoalId());
            log.info("Updated saved amount and percentage for goal: {}", goal.getGoalId());
        }
    }

    /**
     * Updates the status of the goal with the given id to "Completed" if the saved amount is greater than or equal to the target amount.
     * If the goal is not found, a GoalException is thrown.
     *
     * @param goalId the id of the goal to update
     */
    public void updateGoalStatus(String goalId) {
        if (goalId == null || goalId.isEmpty()) {
            return;
        }

        Goal goal = goalRepo.findByGoalId(goalId).orElseThrow(() -> new GoalException("Goal not found"));
        if (goal.getSavedAmount() >= goal.getTargetAmount()) {
            goalRepo.updateGoalDetails(goal.getGoalName(), goal.getTargetAmount(), goal.getDeadline(), GoalStatus.COMPLETED, goalId);
        }
    }


    /**
     * Returns the saved amount of the first goal found for the given user.
     * If no goal is found, the method will attempt to call the user-service to fetch the savings amount.
     * If the user-service is unavailable, the method will throw a GoalException.
     * If the user-service returns a non-zero and non-null savings amount, the method will return that amount.
     * Otherwise, the method will return 0.0.
     *
     * @param username the username of the user
     * @return the saved amount of the first goal found for the given user
     * @throws GoalException if the user-service is unavailable or returns an invalid savings amount
     */
    private double getSavingsAmountFromExistingGoal(String username) {
        boolean isAnyGoalFoundForThisUser = goalRepo.isAnyGoalPrentForThisUser(username);
        if (!isAnyGoalFoundForThisUser) {
            log.warn("No goal found for this user");
            ResponseEntity<Double> validationResponse;
            String url = "http://localhost:8082/api/v1/savings/" + username;
            try {
                validationResponse = restTemplate.getForEntity(url, Double.class);
            } catch (RestClientResponseException exception) {
                throw new GoalException(exception.getMessage());
            } catch (ResourceAccessException exception) {
                log.error("Upstream user-service unavailable", exception);
                throw new GoalException(exception.getMessage());
            }
            if (validationResponse.getBody() != null && validationResponse.getBody() != 0.0) {
                return validationResponse.getBody();
            }
            return 0.0;
        } else {
            // Get all the goals for this user
            List<Goal> goals = goalRepo.findGoalsBasedOnStatusForUser(username, GoalStatus.ACTIVE);
            // Take first goal
            Goal existingGoal = goals.get(0);
            // Return the saved amount of the first goal
            return existingGoal.getSavedAmount();
        }
    }
}
