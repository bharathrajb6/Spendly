package com.example.transaction_service.service;

import com.example.transaction_service.model.Savings;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.repo.SavingsRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SavingsService {

    private final SavingsRepo savingsRepo;


    public double updateSaving(Transaction transaction) {
        Optional<Savings> savings = savingsRepo.findByUsername(transaction.getUsername());

        if (savings.isEmpty()) {
            Savings newSavings = new Savings();
            newSavings.setUsername(transaction.getUsername());
            newSavings.setSavingsId(UUID.randomUUID().toString());
            if (Objects.equals(transaction.getTransactionType(), "INCOME")) {
                newSavings.setSavedAmount(transaction.getAmount());
            } else {
                newSavings.setSavedAmount(0);
            }
            try {
                savingsRepo.save(newSavings);
            } catch (Exception exception) {
                throw new RuntimeException(exception.getMessage());
            }
            return newSavings.getSavedAmount();
        } else {
            Savings existingSaving = savings.get();
            if (Objects.equals(transaction.getTransactionType(), "INCOME")) {
                existingSaving.setSavedAmount(existingSaving.getSavedAmount() + transaction.getAmount());
            } else {
                existingSaving.setSavedAmount(existingSaving.getSavedAmount() - transaction.getAmount());
            }

            try {
                savingsRepo.updateSavingAmount(existingSaving.getSavedAmount(), existingSaving.getUsername());
            } catch (Exception exception) {
                throw new RuntimeException(exception.getMessage());
            }
            return existingSaving.getSavedAmount();
        }
    }

    public Double getSavingsData(String username) {

        Optional<Savings> savings = savingsRepo.findByUsername(username);
        if (savings.isEmpty()) {
            return 0.0;
        }
        return savings.get().getSavedAmount();

    }
}
