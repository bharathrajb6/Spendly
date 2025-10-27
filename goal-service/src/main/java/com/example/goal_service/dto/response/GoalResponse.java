package com.example.goal_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalResponse {
    private String goalId;
    private String goalName;
    private String username;
    private double targetAmount;
    private double savedAmount;
    private Date deadline;
    private String status;
}
