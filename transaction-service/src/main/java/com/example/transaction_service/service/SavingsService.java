package com.example.transaction_service.service;

import com.example.transaction_service.exception.TransactionException;
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

    /**
     * Updates the user's total savings based on a new transaction.
     * If no savings record exists for the user, a new one is created.
     *
     * @param transaction the transaction to process
     * @return the updated total savings amount
     */
    public double updateSaving(Transaction transaction) {
        Optional<Savings> savings = savingsRepo.findByUsername(transaction.getUsername());

        if (savings.isEmpty()) {
            Savings newSavings = new Savings();
            newSavings.setUsername(transaction.getUsername());
            newSavings.setSavingsId(UUID.randomUUID().toString());
            if (Objects.equals(transaction.getTransactionType(), "INCOME")) {
                newSavings.setSavedAmount(transaction.getAmount());
            } else {
                newSavings.setSavedAmount(0 - transaction.getAmount());
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

    /**
     * Retrieves the total savings amount for a specific user.
     *
     * @param username the username of the user
     * @return the total savings amount, or 0.0 if no record exists
     */
    public Double getSavingsData(String username) {

        Optional<Savings> savings = savingsRepo.findByUsername(username);
        if (savings.isEmpty()) {
            return 0.0;
        }
        return savings.get().getSavedAmount();

    }

    /**
     * Updates the savings data after a transaction update.
     *
     * @param username       The username of the user for which the savings is being
     *                       updated.
     * @param oldTransaction The old transaction for which the savings is being
     *                       updated.
     * @param newTransaction The new transaction for which the savings is being
     *                       updated.
     * @return The updated savings amount.
     */
    public double updateSavingsDataAfterTransactionUpdate(String username, Transaction oldTransaction,
            Transaction newTransaction) {
        Savings savings = savingsRepo.findByUsername(username)
                .orElseThrow(() -> new TransactionException("Savings not found for this user."));

        double currentSavings = savings.getSavedAmount();
        double oldAmount = oldTransaction.getAmount();
        double newAmount = newTransaction.getAmount();

        String oldType = oldTransaction.getTransactionType();
        String newType = newTransaction.getTransactionType();

        double updatedSavings = currentSavings;

        // Case 1: Same transaction type
        if (oldType.equalsIgnoreCase(newType)) {
            if (oldType.equalsIgnoreCase("INCOME")) {
                // Adjust based on income change
                updatedSavings = currentSavings - oldAmount + newAmount;
            } else if (oldType.equalsIgnoreCase("EXPENSE")) {
                // Adjust based on expense change
                updatedSavings = currentSavings + oldAmount - newAmount;
            }
        }
        // Case 2: Type changed
        else {
            if (oldType.equalsIgnoreCase("INCOME") && newType.equalsIgnoreCase("EXPENSE")) {
                // Reverse old income and apply new expense
                updatedSavings = currentSavings - oldAmount - newAmount;
            } else if (oldType.equalsIgnoreCase("EXPENSE") && newType.equalsIgnoreCase("INCOME")) {
                // Reverse old expense and apply new income
                updatedSavings = currentSavings + oldAmount + newAmount;
            }
        }

        try {
            savingsRepo.updateSavingAmount(updatedSavings, username);
        } catch (Exception exception) {
            throw new TransactionException("Unable to update the saving amount for the user");
        }

        return updatedSavings;
    }

    /**
     * Updates the user's total savings after a transaction is deleted.
     * Reverses the impact of the deleted transaction on the savings balance.
     *
     * @param username    the username of the user
     * @param transaction the transaction that was deleted
     * @return the updated total savings amount
     * @throws TransactionException if the savings record is not found for the user
     */
    public double updateSavingsAfterTransactionDelete(String username, Transaction transaction) {
        Savings savings = savingsRepo.findByUsername(username)
                .orElseThrow(() -> new TransactionException("Savings not found for this user."));

        double currentSavings = savings.getSavedAmount();
        double updatedSavings = 0;

        if (transaction.getTransactionType().equals("INCOME")) {
            updatedSavings = currentSavings - transaction.getAmount();
        } else {
            updatedSavings = currentSavings + transaction.getAmount();
        }

        try {
            savingsRepo.updateSavingAmount(updatedSavings, username);
        } catch (Exception exception) {
            throw new TransactionException("Unable to update the savings for the user.");
        }

        return updatedSavings;
    }
}
