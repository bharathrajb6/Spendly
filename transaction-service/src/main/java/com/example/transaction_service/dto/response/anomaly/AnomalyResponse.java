package com.example.transaction_service.dto.response.anomaly;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AnomalyResponse {
    String anomalyId;
    String category;
    double currentAmount;
    double averageAmount;
    double percentageDeviation;
    LocalDateTime detectedAt;
    String message;
}
