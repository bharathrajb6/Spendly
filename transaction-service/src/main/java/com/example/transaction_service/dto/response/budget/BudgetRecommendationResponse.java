package com.example.transaction_service.dto.response.budget;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BudgetRecommendationResponse {
    String category;
    double currentLimit;
    double suggestedLimit;
    int month;
    int year;
    String recommendationNote;
}
