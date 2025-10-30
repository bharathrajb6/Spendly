package com.example.transaction_service.kafka;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private String username;
    private String transactionType;
    private double oldAmount;
    private double newAmount;
    private double savingsAmount;
}
