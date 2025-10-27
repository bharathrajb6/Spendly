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

    @RequestMapping(value = "/goal", method = RequestMethod.POST)
    public GoalResponse addGoal(@RequestHeader("X-Username") String username, @RequestBody GoalRequest request) {
        return goalService.addGoal(username, request);
    }

    @RequestMapping(value = "/goal/{goalId}", method = RequestMethod.GET)
    public GoalResponse getGoalDetails(@PathVariable String goalId) {
        return goalService.getGoalDetails(goalId);
    }

    @RequestMapping(value = "/goal/{goalId}", method = RequestMethod.PUT)
    public GoalResponse updateGoalDetails(@PathVariable String goalId, @RequestBody GoalRequest request) {
        return goalService.updateGoal(goalId, request);
    }

    @RequestMapping(value = "/goal/{goalId}", method = RequestMethod.DELETE)
    public void deleteGoal(@PathVariable String goalId) {
        goalService.deleteGoal(goalId);
    }
}
