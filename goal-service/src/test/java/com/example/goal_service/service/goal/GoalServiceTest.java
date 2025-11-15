package com.example.goal_service.service.goal;

import com.example.goal_service.dto.request.GoalRequest;
import com.example.goal_service.dto.response.FinancialSummaryResponse;
import com.example.goal_service.dto.response.GoalResponse;
import com.example.goal_service.dto.response.GoalSummaryResponse;
import com.example.goal_service.exception.GoalException;
import com.example.goal_service.model.Goal;
import com.example.goal_service.model.GoalStatus;
import com.example.goal_service.model.TransactionDto;
import com.example.goal_service.repo.GoalRepo;
import com.example.goal_service.service.RedisService;
import com.example.goal_service.util.GoalUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepo goalRepo;

    @Mock
    private RedisService redisService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GoalNotificationService goalNotificationService;

    @InjectMocks
    private GoalService goalService;

    private Goal testGoal;
    private GoalRequest testGoalRequest;
    private TransactionDto testTransaction;
    private final String testUsername = "testuser";
    private final String testGoalId = "goal123";

    @BeforeEach
    void setUp() {
        // Initialize test data
        testGoal = new Goal();
        testGoal.setGoalId(testGoalId);
        testGoal.setGoalName("Test Goal");
        testGoal.setTargetAmount(1000.0);
        testGoal.setSavedAmount(500.0);
        testGoal.setDeadline(Date.from(LocalDate.now().plusMonths(6).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        testGoal.setStatus(GoalStatus.ACTIVE);
        testGoal.setUsername(testUsername);
        testGoal.setProgressPercent(50.0);

        testGoalRequest = new GoalRequest();
        testGoalRequest.setGoalName("Test Goal");
        testGoalRequest.setTargetAmount(1000.0);
        testGoalRequest.setDeadline(Date.from(LocalDate.now().plusMonths(6).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        testGoalRequest.setStatus(GoalStatus.ACTIVE.name());

        testTransaction = new TransactionDto();
        testTransaction.setUsername(testUsername);
        testTransaction.setTotalIncome(2000.0);
        testTransaction.setTotalExpense(1500.0);
    }

    @Test
    @DisplayName("Should add a new goal successfully")
    void addGoal_ValidRequest_ReturnsGoalResponse() {
        // Arrange
        when(goalRepo.save(any(Goal.class))).thenReturn(testGoal);
        when(redisService.getData(anyString(), eq(Goal.class))).thenReturn(null);
        when(goalRepo.isAnyGoalPrentForThisUser(anyString())).thenReturn(false);
        when(restTemplate.getForEntity(anyString(), eq(Double.class)))
                .thenReturn(ResponseEntity.ok(0.0));

        // Act
        GoalResponse response = goalService.addGoal(testUsername, testGoalRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testGoalId, response.getGoalId());
        verify(goalRepo).save(any(Goal.class));
        verify(redisService).setData(eq(testGoalId), any(Goal.class), anyLong());
    }

    @Test
    @DisplayName("Should get goal details from Redis cache")
    void getGoalDetails_GoalInCache_ReturnsCachedGoal() {
        // Arrange
        when(redisService.getData(testGoalId, Goal.class)).thenReturn(testGoal);

        // Act
        GoalResponse response = goalService.getGoalDetails(testGoalId);

        // Assert
        assertNotNull(response);
        assertEquals(testGoalId, response.getGoalId());
        verify(redisService).getData(testGoalId, Goal.class);
        verify(goalRepo, never()).findByGoalId(anyString());
    }

    @Test
    @DisplayName("Should update goal details successfully")
    void updateGoal_ValidRequest_ReturnsUpdatedGoal() {
        // Arrange
        when(goalRepo.isGoalFound(testGoalId)).thenReturn(true);
        when(redisService.getData(testGoalId, Goal.class)).thenReturn(testGoal);

        // Act
        GoalResponse response = goalService.updateGoal(testGoalId, testGoalRequest);

        // Assert
        assertNotNull(response);
        verify(goalRepo).updateGoalDetails(
                eq(testGoalRequest.getGoalName()),
                eq(testGoalRequest.getTargetAmount()),
                eq(testGoalRequest.getDeadline()),
                any(GoalStatus.class),
                eq(testGoalId)
        );
        verify(redisService).deleteData(testGoalId);
    }

    @Test
    @DisplayName("Should delete goal successfully")
    void deleteGoal_ValidGoalId_DeletesGoal() {
        // Arrange
        when(goalRepo.isGoalFound(testGoalId)).thenReturn(true);
        doNothing().when(goalRepo).deleteById(testGoalId);
        doNothing().when(redisService).deleteData(testGoalId);

        // Act & Assert
        assertDoesNotThrow(() -> goalService.deleteGoal(testGoalId));
        verify(goalRepo).deleteById(testGoalId);
        verify(redisService).deleteData(testGoalId);
    }

    @Test
    @DisplayName("Should update goal progress based on transaction")
    void updateGoalAmountAfterTransaction_ValidTransaction_UpdatesProgress() {
        // Arrange
        List<Goal> activeGoals = Arrays.asList(testGoal);
        when(goalRepo.isAnyGoalPrentForThisUser(testUsername)).thenReturn(true);
        when(goalRepo.findGoalsBasedOnStatusForUser(testUsername, GoalStatus.ACTIVE))
                .thenReturn(activeGoals);

        // Act
        goalService.updateGoalAmountAfterTransaction(testTransaction);

        // Assert
        verify(goalRepo).saveAll(anyList());
        verify(redisService, atLeastOnce()).deleteData(anyString());
    }

    @Test
    @DisplayName("Should recalculate goal progress for user")
    void updateGoalsProgressForUser_ValidUser_ReturnsUpdatedGoals() {
        // Arrange
        FinancialSummaryResponse summaryResponse = new FinancialSummaryResponse(2000, 1500, 500);
        when(restTemplate.getForObject(anyString(), eq(FinancialSummaryResponse.class)))
                .thenReturn(summaryResponse);
        when(goalRepo.findGoalsBasedOnStatusForUser(testUsername, GoalStatus.ACTIVE))
                .thenReturn(Arrays.asList(testGoal));

        // Act
        List<GoalResponse> responses = goalService.updateGoalsProgressForUser(testUsername);

        // Assert
        assertNotNull(responses);
        verify(goalRepo).saveAll(anyList());
    }

    @Test
    @DisplayName("Should get goal summary with correct counts")
    void getGoalSummary_ValidUser_ReturnsSummary() {
        // Arrange
        when(goalRepo.countByUsername(testUsername)).thenReturn(5L);
        when(goalRepo.countByUsernameAndStatus(testUsername, GoalStatus.ACHIEVED)).thenReturn(2L);
        when(goalRepo.countByUsernameAndStatus(testUsername, GoalStatus.COMPLETED)).thenReturn(1L);

        // Act
        GoalSummaryResponse summary = goalService.getGoalSummary(testUsername);

        // Assert
        assertNotNull(summary);
        assertEquals(5L, summary.getTotalGoals());
        assertEquals(3L, summary.getAchievedGoals()); // 2 ACHIEVED + 1 COMPLETED
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent goal")
    void updateGoal_NonExistentGoal_ThrowsException() {
        // Arrange
        when(goalRepo.isGoalFound(testGoalId)).thenReturn(false);

        // Act & Assert
        assertThrows(GoalException.class, () ->
                goalService.updateGoal(testGoalId, testGoalRequest)
        );
    }

    @Test
    @DisplayName("Should handle Redis cache miss by fetching from database")
    void getGoalDetails_GoalNotInCache_FetchesFromDatabase() {
        // Arrange
        when(redisService.getData(testGoalId, Goal.class)).thenReturn(null);
        when(goalRepo.findByGoalId(testGoalId)).thenReturn(Optional.of(testGoal));

        // Act
        GoalResponse response = goalService.getGoalDetails(testGoalId);

        // Assert
        assertNotNull(response);
        verify(goalRepo).findByGoalId(testGoalId);
        verify(redisService).setData(eq(testGoalId), any(Goal.class), anyLong());
    }

    @Test
    @DisplayName("Should update goal status to ACHIEVED when saved amount reaches target")
    void updateGoalStatus_SavedAmountReachesTarget_UpdatesStatus() {
        // Arrange
        testGoal.setSavedAmount(1000.0);
        when(goalRepo.findByGoalId(testGoalId)).thenReturn(Optional.of(testGoal));
        when(goalRepo.updateGoalSavedAmountAndPercentage(anyDouble(), anyDouble(), anyString())).thenReturn(1);

        // Act
        goalService.updateGoalStatus(testGoalId);

        // Assert
        verify(goalRepo).updateGoalDetails(
                anyString(), anyDouble(), any(), eq(GoalStatus.ACHIEVED), eq(testGoalId)
        );
    }

    @Test
    @DisplayName("Should handle transaction with no active goals gracefully")
    void updateGoalAmountAfterTransaction_NoActiveGoals_DoesNothing() {
        // Arrange
        when(goalRepo.isAnyGoalPrentForThisUser(testUsername)).thenReturn(false);

        // Act
        goalService.updateGoalAmountAfterTransaction(testTransaction);

        // Assert
        verify(goalRepo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should round progress percentage to two decimal places")
    void roundToTwoDecimals_NumberWithManyDecimals_RoundsCorrectly() {
        // Arrange
        double value = 50.123456;

        // Act
        double result = goalService.roundToTwoDecimals(value);

        // Assert
        assertEquals(50.12, result);
    }
}
