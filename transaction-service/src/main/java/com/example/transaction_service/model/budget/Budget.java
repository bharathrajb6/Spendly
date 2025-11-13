package com.example.transaction_service.model.budget;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @Column(name = "budget_id", nullable = false, updatable = false, length = 36)
    private String budgetId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "limit_amount", nullable = false)
    private double limitAmount;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "year", nullable = false)
    private int year;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private BudgetStatus status;

    @Column(name = "recommended_limit_amount")
    private Double recommendedLimitAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

