package com.example.transaction_service.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FinancialSummaryResponse {
    double totalIncome;
    double totalExpense;
    double remainingBalance;
}

