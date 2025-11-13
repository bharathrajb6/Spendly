package com.example.goal_service.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GoalSummaryResponse {
    long totalGoals;
    long achievedGoals;
}
