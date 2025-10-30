package com.example.transaction_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "savings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Savings {

    @Id
    @Column(name = "saving_id", nullable = false, updatable = true)
    private String savingsId;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "saved_amount")
    private double savedAmount;
}
