package com.example.goal_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "status", nullable = false)
    private String status;
}
