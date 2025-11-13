package com.example.transaction_service.dto.response;

import lombok.Data;

@Data
public class GoalSummaryResponse {
    private long totalGoals;
    private long achievedGoals;
}
