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

    Optional<Transaction> findByTransactionID(String transactionID);

    @Query("SELECT t FROM Transaction t WHERE t.username = ?1")
    Page<Transaction> findTransactionByUsername(String username, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.username = ?1")
    List<Transaction> findTransactionByUsername(String username);

    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.transactionType = ?1, t.category = ?2, t.amount = ?3, t.notes = ?4 WHERE t.username = ?5")
    void updateTransaction(String type, String category, double amount, String notes, String username);
}
