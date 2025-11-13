package com.example.transaction_service.dto.response.budget;

import com.example.transaction_service.model.budget.BudgetStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BudgetResponse {
    String budgetId;
    String userId;
    String category;
    double limitAmount;
    Integer month;
    Integer year;
    BudgetStatus status;
    Double recommendedLimitAmount;
}

