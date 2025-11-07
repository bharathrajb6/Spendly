package com.example.goal_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "goal")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goal {

    @Id
    @Column(name = "goal_id", nullable = false, updatable = false)
    private String goalId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "goal_name", nullable = false)
    private String goalName;

    @Column(name = "target_amount", nullable = false)
    private double targetAmount;

    @Column(name = "saved_amount", nullable = false)
    private double savedAmount;

    @Column(name = "deadline", nullable = false)
    private Date deadline;

    @Column(name = "progress_percent", nullable = false)
    private double progressPercent;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GoalStatus status;
}
