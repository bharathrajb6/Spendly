package com.example.transaction_service.model.anomaly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "anomalies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anomaly {

    @Id
    @Column(name = "anomaly_id", nullable = false, updatable = false, length = 36)
    private String anomalyId;

    @Column(name = "username", nullable = false)
    private String userId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "current_amount", nullable = false)
    private double currentAmount;

    @Column(name = "average_amount", nullable = false)
    private double averageAmount;

    @Column(name = "percentage_deviation", nullable = false)
    private double percentageDeviation;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;
}

