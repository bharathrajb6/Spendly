package com.example.goal_service.repo;

import com.example.goal_service.model.Goal;
import com.example.goal_service.model.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepo extends JpaRepository<Goal, String> {

    /**
     * Find a goal by its id.
     *
     * @param goalId the id of the goal to find
     * @return an optional containing the goal if found, or an empty optional otherwise
     */
    Optional<Goal> findByGoalId(String goalId);

    /**
     * Finds a goal by its username and goal name.
     *
     * @param username the username of the user who owns the goal
     * @param goalName the name of the goal to find
     * @return an optional containing the goal if found, or an empty optional otherwise
     */
    Optional<Goal> findByUsernameAndGoalName(String username, String goalName);


    /**
     * Finds all goals associated with a given username.
     *
     * @param username the username of the user whose goals to find
     * @return a list of goals associated with the given username
     */
    @Query("SELECT g FROM Goal g WHERE g.username = ?1")
    List<Goal> findGoalByUsername(String username);

    /**
     * Finds all goals associated with a given username, using pagination.
     *
     * @param username the username of the user whose goals to find
     * @param pageable the pageable object containing the page number and size
     * @return a page of goals associated with the given username
     */
    @Query("SELECT g FROM Goal g WHERE g.username = ?1")
    Page<Goal> findGoalByUsername(String username, Pageable pageable);


    /**
     * Updates the saved amount of a goal with the given id.
     *
     * @param savedAmount the new saved amount of the goal
     * @param goalId      the id of the goal to update
     */
    @Modifying
    @Transactional
    @Query("UPDATE Goal g SET g.savedAmount = ?1 WHERE g.goalId = ?2")
    void updateGoalSavedAmount(double savedAmount, String goalId);


    /**
     * Updates the details of a goal with the given id.
     *
     * @param goalName     the new name of the goal
     * @param targetAmount the new target amount of the goal
     * @param deadline     the new deadline of the goal
     * @param status       the new status of the goal
     * @param goalId       the id of the goal to update
     */
    @Modifying
    @Transactional
    @Query("UPDATE Goal g SET g.goalName = ?1, g.targetAmount = ?2, g.deadline = ?3, g.status = ?4 WHERE g.goalId = ?5")
    void updateGoalDetails(String goalName, double targetAmount, Date deadline, GoalStatus status, String goalId);

    /**
     * Checks if any goal is present for the given user.
     *
     * @param username the username of the user to check
     * @return true if any goal is present, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Goal g WHERE g.username = ?1")
    boolean isAnyGoalPrentForThisUser(String username);


    /**
     * Updates the saved amount and progress percentage of a goal with the given id.
     *
     * @param savedAmount the new saved amount of the goal
     * @param percentage  the new progress percentage of the goal
     * @param goalId      the id of the goal to update
     */
    @Modifying
    @Transactional
    @Query("UPDATE Goal g SET g.savedAmount = ?1, g.progressPercent = ?2 WHERE g.goalId = ?3")
    void updateGoalSavedAmountAndPercentage(double savedAmount, double percentage, String goalId);


    /**
     * Finds all goals associated with a given username and status.
     *
     * @param username the username of the user whose goals to find
     * @param status   the status of the goals to find
     * @return a list of goals associated with the given username and status
     */
    @Query("SELECT g FROM Goal g WHERE g.username = ?1 and g.status = ?2")
    List<Goal> findGoalsBasedOnStatusForUser(String username, GoalStatus status);

    /**
     * Checks if a goal with the given id is present in the database.
     *
     * @param goalId the id of the goal to check
     * @return true if the goal is present, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Goal g WHERE g.goalId = ?1")
    boolean isGoalFound(String goalId);

    long countByUsername(String username);

    long countByUsernameAndStatus(String username, GoalStatus status);
}
