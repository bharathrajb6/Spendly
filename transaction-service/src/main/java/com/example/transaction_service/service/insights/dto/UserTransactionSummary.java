package com.example.transaction_service.service.insights.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserTransactionSummary {
    double totalIncome;
    double totalExpense;
    double remainingBalance;
}

