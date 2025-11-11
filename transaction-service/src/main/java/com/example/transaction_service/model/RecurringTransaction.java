package com.example.transaction_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "recurring_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransaction {
    @Id
    @Column(name = "recurring_id", nullable = false, unique = true)
    private String recurringTransactionID;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "frequency", nullable = false)
    private int frequency;

    @Column(name = "next_due_date", nullable = false)
    private Timestamp nextDueDate;
}
