package com.example.transaction_service.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FinancialHealthResponse {
    double score;
    double savingsRatio;
    double expenseStability;
    double goalCompletionRate;
    String feedback;
}
