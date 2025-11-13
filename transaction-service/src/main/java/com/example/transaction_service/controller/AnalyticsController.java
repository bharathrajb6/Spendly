package com.example.transaction_service.controller;

import com.example.transaction_service.dto.response.FinancialSummaryResponse;
import com.example.transaction_service.service.insights.TransactionAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final TransactionAnalyticsService transactionAnalyticsService;

    @Operation(summary = "Fetch aggregate financial summary for a user", description = "Returns total income, total expense, and remaining balance accumulated across all transactions.")
    @GetMapping("/users/{username}/summary")
    public FinancialSummaryResponse getUserFinancialSummary(@PathVariable("username") String username) {
        var summary = transactionAnalyticsService.buildUserTransactionSummary(username);
        return FinancialSummaryResponse.builder().totalIncome(summary.getTotalIncome()).totalExpense(summary.getTotalExpense()).remainingBalance(summary.getRemainingBalance()).build();
    }
}

