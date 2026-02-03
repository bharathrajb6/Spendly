package com.example.transaction_service.controller;

import com.example.transaction_service.dto.request.BudgetUpdateRequest;
import com.example.transaction_service.dto.response.budget.BudgetRecommendationResponse;
import com.example.transaction_service.dto.response.budget.BudgetResponse;
import com.example.transaction_service.service.budget.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * Creates default budgets for a new user.
     * 
     * @param userId the ID of the user
     * @return a list of budget responses for the user
     */
    @Operation(summary = "Create default budgets for a new user", description = "Generates default budgets for the top expense categories. Intended for onboarding workflows.")
    @PostMapping("/default/{userId}")
    public List<BudgetResponse> createDefaultBudgets(@PathVariable("userId") String userId) {
        return budgetService.createDefaultBudgets(userId);
    }

    /**
     * Gets the monthly budgets for a specific user.
     * 
     * @param userId the ID of the user
     * @param month  the month for which to get the budgets
     * @return a list of budget responses for the user
     */
    @Operation(summary = "Get monthly budgets for a user", description = "Returns budget allocations for the specified month. Defaults to current month if not provided.")
    @GetMapping("/{userId}")
    public List<BudgetResponse> getBudgetsForMonth(@PathVariable("userId") String userId,
            @RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return budgetService.getBudgetsForUser(userId, month);
    }

    /**
     * Updates a specific budget.
     * 
     * @param budgetId the ID of the budget to update
     * @param request  the budget update request
     * @return the updated budget response
     */
    @Operation(summary = "Update a budget", description = "Updates the limit amount for a specific budget.")
    @PutMapping("/{budgetId}")
    public BudgetResponse updateBudget(@PathVariable("budgetId") String budgetId,
            @RequestBody BudgetUpdateRequest request) {
        return budgetService.updateBudget(budgetId, request.getLimitAmount());
    }

    /**
     * Gets the budget recommendations for a specific user.
     * 
     * @param userId the ID of the user
     * @return a list of budget recommendations for the user
     */
    @Operation(summary = "Get budget recommendations", description = "Provides category-wise budget adjustments based on the last three months of spending.")
    @GetMapping("/recommendations/{userId}")
    public List<BudgetRecommendationResponse> getBudgetRecommendations(@PathVariable("userId") String userId) {
        return budgetService.generateRecommendations(userId);
    }
}
