package com.example.transaction_service.repo;

import com.example.transaction_service.model.budget.Budget;
import com.example.transaction_service.model.budget.BudgetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepo extends JpaRepository<Budget, String> {

    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(String userId, String category, int month, int year);

    List<Budget> findByUserIdAndMonthAndYear(String userId, int month, int year);

    List<Budget> findByUserIdAndStatus(String userId, BudgetStatus status);

    List<Budget> findByUserId(String userId);

    Optional<Budget> findTopByUserIdAndCategoryOrderByYearDescMonthDesc(String userId, String category);

    List<Budget> findByUserIdAndStatusAndMonthAndYear(String userId, BudgetStatus status, int month, int year);

    @Query("SELECT DISTINCT b.userId FROM Budget b")
    List<String> findDistinctUserIds();
}

