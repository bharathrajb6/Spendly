package com.example.goal_service.dto.response;

import lombok.Data;

@Data
public class FinancialSummaryResponse {
    private double totalIncome;
    private double totalExpense;
    private double remainingBalance;
}

