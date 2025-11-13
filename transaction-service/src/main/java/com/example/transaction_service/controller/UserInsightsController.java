package com.example.transaction_service.controller;

import com.example.transaction_service.dto.response.FinancialHealthResponse;
import com.example.transaction_service.service.insights.FinancialHealthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserInsightsController {

    private final FinancialHealthService financialHealthService;

    @Operation(summary = "Get the financial health score for a user", description = "Calculates a composite score between 0 and 100 using savings, spending stability, and goal completion metrics.")
    @GetMapping("/{userId}/health-score")
    public FinancialHealthResponse getHealthScore(@PathVariable("userId") String userId) {
        return financialHealthService.calculateHealthScore(userId);
    }
}
