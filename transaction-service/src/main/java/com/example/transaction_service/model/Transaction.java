package com.example.transaction_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private String transactionID;

    @Column(name = "username")
    private String username;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "transaction_date", nullable = false)
    private Timestamp transactionDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "is_recurring_transaction")
    private boolean isRecurringTransaction;

    @Transient
    private int frequency;
}
