package com.example.transaction_service.repo;

import com.example.transaction_service.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, String> {

    /**
     * Finds a transaction by its ID.
     *
     * @param transactionID The ID of the transaction to find.
     * @return An optional containing the transaction if found, or an empty optional if not found.
     */
    Optional<Transaction> findByTransactionID(String transactionID);

    /**
     * Retrieves all transactions for a given username.
     *
     * @param username The username for which all transactions are being retrieved.
     * @param pageable The pageable object containing the page and size details.
     * @return A page of transaction objects containing all transactions for the given user.
     */
    @Query("SELECT t FROM Transaction t WHERE t.username = ?1")
    Page<Transaction> findTransactionByUsername(String username, Pageable pageable);

    /**
     * Retrieves all transactions for a given username.
     *
     * @param username The username for which all transactions are being retrieved.
     * @return A list of transaction objects containing all transactions for the given user.
     */
    @Query("SELECT t FROM Transaction t WHERE t.username = ?1")
    List<Transaction> findTransactionByUsername(String username);

    /**
     * Updates an existing transaction for the given username.
     *
     * @param type     The new type of the transaction.
     * @param category The new category of the transaction.
     * @param amount   The new amount of the transaction.
     * @param notes    The new notes of the transaction.
     * @param username The username of which the transaction is being updated.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.transactionType = ?1, t.category = ?2, t.amount = ?3, t.notes = ?4 WHERE t.username = ?5")
    void updateTransaction(String type, String category, double amount, String notes, String username);
}
