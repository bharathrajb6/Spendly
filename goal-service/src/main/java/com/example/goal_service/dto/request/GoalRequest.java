package com.example.goal_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {
    private String goalName;
    private double targetAmount;
    private Date deadline;
    private String status;
}
