package com.example.transaction_service.repo;

import com.example.transaction_service.model.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface SavingsRepo extends JpaRepository<Savings, String> {

    Optional<Savings> findByUsername(String username);


    @Modifying
    @Transactional
    @Query("UPDATE Savings s SET s.savedAmount = ?1 WHERE s.username = ?2")
    void updateSavingAmount(double savingAmount, String username);
}
