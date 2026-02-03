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

    /**
     * Retrieves a budget for the given user, category, month, and year.
     * 
     * @param userId   The ID of the user for whom to retrieve the budget.
     * @param category The category of the budget to retrieve.
     * @param month    The month of the budget to retrieve.
     * @param year     The year of the budget to retrieve.
     * @return An optional containing the budget for the given user, category,
     *         month, and year, or an empty optional if no such budget is found.
     */
    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(String userId, String category, int month, int year);

    /**
     * Retrieves a list of budgets for the given user, month, and year.
     * 
     * @param userId The ID of the user for whom to retrieve the budgets.
     * @param month  The month of the budgets to retrieve.
     * @param year   The year of the budgets to retrieve.
     * @return A list of budgets for the given user, month, and year.
     */
    List<Budget> findByUserIdAndMonthAndYear(String userId, int month, int year);

    /**
     * Retrieves a list of budgets for the given user and status.
     * 
     * @param userId The ID of the user for whom to retrieve the budgets.
     * @param status The status of the budgets to retrieve.
     * @return A list of budgets for the given user and status.
     */
    List<Budget> findByUserIdAndStatus(String userId, BudgetStatus status);

    /**
     * Retrieves a list of budgets for the given user.
     * 
     * @param userId The ID of the user for whom to retrieve the budgets.
     * @return A list of budgets for the given user.
     */
    List<Budget> findByUserId(String userId);

    /**
     * Retrieves the most recent budget for the given user and category.
     * 
     * @param userId   The ID of the user for whom to retrieve the budget.
     * @param category The category of the budget to retrieve.
     * @return An optional containing the most recent budget for the given user and
     *         category, or an empty optional if no such budget is found.
     */
    Optional<Budget> findTopByUserIdAndCategoryOrderByYearDescMonthDesc(String userId, String category);

    /**
     * Retrieves a list of budgets for the given user, status, month, and year.
     * 
     * @param userId The ID of the user for whom to retrieve the budgets.
     * @param status The status of the budgets to retrieve.
     * @param month  The month of the budgets to retrieve.
     * @param year   The year of the budgets to retrieve.
     * @return A list of budgets for the given user, status, month, and year.
     */
    List<Budget> findByUserIdAndStatusAndMonthAndYear(String userId, BudgetStatus status, int month, int year);

    /**
     * Retrieves a list of distinct user IDs from the Budget table.
     * 
     * @return A list of distinct user IDs.
     */
    @Query("SELECT DISTINCT b.userId FROM Budget b")
    List<String> findDistinctUserIds();
}
