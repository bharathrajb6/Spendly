package com.example.transaction_service.repo;

import com.example.transaction_service.model.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecurringTransactionRepo extends JpaRepository<RecurringTransaction, String> {
}
