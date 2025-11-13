package com.example.goal_service.controller;

import com.example.goal_service.dto.request.GoalRequest;
import com.example.goal_service.dto.response.GoalResponse;
import com.example.goal_service.dto.response.GoalSummaryResponse;
import com.example.goal_service.service.goal.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GoalController {

    @Autowired
    private GoalService goalService;

    /**
     * Adds a new goal for a given user.
     *
     * @param username the username of the user adding the goal
     * @param request  the goal request object
     * @return the goal response object
     */
    @RequestMapping(value = "/goal", method = RequestMethod.POST)
    public GoalResponse addGoal(@RequestHeader("X-Username") String username, @RequestBody GoalRequest request) {
        return goalService.addGoal(username, request);
    }

    /**
     * Retrieves the details of a goal with the given id.
     *
     * @param goalId the id of the goal to retrieve
     * @return the goal response object with the goal details
     */
    @RequestMapping(value = "/goal/{goalId}", method = RequestMethod.GET)
    public GoalResponse getGoalDetails(@PathVariable String goalId) {
        return goalService.getGoalDetails(goalId);
    }

    /**
     * Updates the details of a goal with the given id.
     *
     * @param goalId  the id of the goal to update
     * @param request the goal request object
     * @return the goal response object with the updated goal details
     */
    @RequestMapping(value = "/goal/{goalId}", method = RequestMethod.PUT)
    public GoalResponse updateGoalDetails(@PathVariable String goalId, @RequestBody GoalRequest request) {
        return goalService.updateGoal(goalId, request);
    }

    /**
     * Deletes a goal with the given id.
     *
     * @param goalId the id of the goal to delete
     */
    @RequestMapping(value = "/goal/{goalId}", method = RequestMethod.DELETE)
    public void deleteGoal(@PathVariable String goalId) {
        goalService.deleteGoal(goalId);
    }

    /**
     * Manually triggers goal progress recalculation for the given user.
     *
     * @param userId the ID of the user whose goals need to be recalculated
     * @return list of updated goal responses
     */
    @Operation(summary = "Refresh goal progress for a user", description = "Recalculates goal progress using the latest financial summary for the user.")
    @PutMapping("/goals/update-progress/{userId}")
    public List<GoalResponse> refreshGoalProgress(@PathVariable("userId") String userId) {
        return goalService.updateGoalsProgressForUser(userId);
    }

    @Operation(summary = "Get goal summary for a user", description = "Provides aggregated counts for total and achieved goals.")
    @GetMapping("/goals/{userId}/summary")
    public GoalSummaryResponse getGoalSummary(@PathVariable("userId") String userId) {
        return goalService.getGoalSummary(userId);
    }
}
