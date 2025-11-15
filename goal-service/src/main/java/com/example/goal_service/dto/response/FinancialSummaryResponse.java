package com.example.goal_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialSummaryResponse {
    private double totalIncome;
    private double totalExpense;
    private double remainingBalance;
}

