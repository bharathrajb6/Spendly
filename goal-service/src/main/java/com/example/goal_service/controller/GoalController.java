package com.example.goal_service.controller;

import com.example.goal_service.dto.request.GoalRequest;
import com.example.goal_service.dto.response.GoalResponse;
import com.example.goal_service.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
