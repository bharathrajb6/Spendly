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

    /**
     * Retrieves the savings data for the given user.
     * 
     * @param username The username for which to retrieve the savings data.
     * @return An optional containing the savings data for the given user, or an
     *         empty optional if no such savings data is found.
     */
    Optional<Savings> findByUsername(String username);

    /**
     * Updates the savings amount for the given user.
     * 
     * @param savingAmount The new savings amount.
     * @param username     The username for which to update the savings amount.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Savings s SET s.savedAmount = ?1 WHERE s.username = ?2")
    void updateSavingAmount(double savingAmount, String username);
}
