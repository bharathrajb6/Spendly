package com.example.goal_service.repo;

import com.example.goal_service.model.Goal;
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

    Optional<Goal> findByGoalId(String goalId);

    Optional<Goal> findByUsernameAndGoalName(String username, String goalName);


    @Query("SELECT g FROM Goal g WHERE g.username = ?1")
    List<Goal> findGoalByUsername(String username);

    @Query("SELECT g FROM Goal g WHERE g.username = ?1")
    Page<Goal> findGoalByUsername(String username, Pageable pageable);


    @Modifying
    @Transactional
    @Query("UPDATE Goal g SET g.savedAmount = ?1 WHERE g.goalId = ?2")
    void updateGoalSavedAmount(double savedAmount, String goalId);


    @Modifying
    @Transactional
    @Query("UPDATE Goal g SET g.goalName = ?1, g.targetAmount = ?2, g.deadline = ?3, g.status = ?4 WHERE g.goalId = ?5")
    void updateGoalDetails(String goalName, double targetAmount, Date deadline, String status, String goalId);
}
