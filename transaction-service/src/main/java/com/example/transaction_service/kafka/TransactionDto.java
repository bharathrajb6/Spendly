package com.example.transaction_service.kafka;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private String username;
    private double savingsAmount;
    private double totalIncome;
    private double totalExpense;
    private double remainingBalance;
    private String transactionId;
    private String transactionType;
    private String category;
    private double transactionAmount;
    private long transactionTimestamp;
    private int transactionMonth;
    private int transactionYear;
    private TransactionEventType eventType;
}
