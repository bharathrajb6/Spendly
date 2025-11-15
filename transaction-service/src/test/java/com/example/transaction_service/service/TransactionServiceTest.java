package com.example.transaction_service.service;

import com.example.transaction_service.dto.request.TransactionRequest;
import com.example.transaction_service.dto.response.TransactionResponse;
import com.example.transaction_service.exception.TransactionException;
import com.example.transaction_service.kafka.EventProducer;
import com.example.transaction_service.kafka.TransactionEventType;
import com.example.transaction_service.model.ExpenseCategory;
import com.example.transaction_service.model.IncomeCategory;
import com.example.transaction_service.model.RecurringTransaction;
import com.example.transaction_service.model.Transaction;
import com.example.transaction_service.repo.RecurringTransactionRepo;
import com.example.transaction_service.repo.TransactionRepo;
import com.example.transaction_service.service.budget.BudgetService;
import com.example.transaction_service.service.insights.TransactionAnalyticsService;
import com.example.transaction_service.util.TransactionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepo transactionRepo;

    @Mock
    private RedisService redisService;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private TransactionUtil transactionUtil;

    @Mock
    private SavingsService savingsService;

    @Mock
    private RecurringTransactionRepo recurringTransactionRepo;

    @Mock
    private TransactionAnalyticsService transactionAnalyticsService;

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;
    private TransactionRequest testRequest;
    private final String testUsername = "testuser";
    private final String testTransactionId = "txn123";
    private final LocalDate testDate = LocalDate.now();

    @BeforeEach
    void setUp() {
        // Initialize test data
        testTransaction = new Transaction();
        testTransaction.setTransactionID(testTransactionId);
        testTransaction.setUsername(testUsername);
        testTransaction.setTransactionType("EXPENSE");
        testTransaction.setCategory(ExpenseCategory.RENT.name());
        testTransaction.setAmount(100.0);
        testTransaction.setTransactionDate(Timestamp.valueOf(LocalDateTime.now()));
        testTransaction.setRecurringTransaction(false);

        testRequest = new TransactionRequest();
        testRequest.setType("EXPENSE");
        testRequest.setCategory(ExpenseCategory.RENT.name());
        testRequest.setAmount(100.0);
        testRequest.setRecurring(false);
    }

    @Test
    @DisplayName("Should add a new transaction successfully")
    void addTransaction_ValidRequest_ReturnsTransactionResponse() throws JsonProcessingException {
        // Arrange
        when(transactionRepo.save(any(Transaction.class))).thenReturn(testTransaction);
        when(savingsService.updateSaving(any(Transaction.class))).thenReturn(100.0);
        when(transactionUtil.generateTransactionData(any(), anyDouble(), any(), any(TransactionEventType.class)))
                .thenReturn("test-json");

        // Act
        TransactionResponse response = transactionService.addTransaction(testUsername, testRequest);

        // Assert
        assertNotNull(response);
        verify(transactionRepo).save(any(Transaction.class));
        verify(redisService).setData(eq(testTransactionId), any(Transaction.class), anyLong());
        verify(eventProducer).sendTopic(eq("transaction"), eq("test-json"));
    }

    @Test
    @DisplayName("Should get transaction by ID from cache")
    void getTransaction_ExistsInCache_ReturnsCachedTransaction() {
        // Arrange
        when(redisService.getData(testTransactionId, Transaction.class)).thenReturn(testTransaction);

        // Act
        TransactionResponse response = transactionService.getTransaction(testTransactionId);

        // Assert
        assertNotNull(response);
        assertEquals(testTransactionId, response.getTransactionID());
        verify(redisService).getData(testTransactionId, Transaction.class);
        verify(transactionRepo, never()).findByTransactionID(anyString());
    }

    @Test
    @DisplayName("Should get all transactions for user with pagination")
    void getAllTransactionForUser_ValidRequest_ReturnsPagedTransactions() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(testTransaction);
        Pageable pageable = PageRequest.of(0, 10);
        when(transactionRepo.findTransactionByUsername(testUsername)).thenReturn(transactions);

        // Act
        Page<TransactionResponse> response = transactionService.getAllTransactionForUser(testUsername, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(redisService).setData(eq(testUsername + "-transactions"), anyList(), anyLong());
    }

    @Test
    @DisplayName("Should update an existing transaction successfully")
    void updateTransaction_ValidRequest_ReturnsUpdatedTransaction() throws JsonProcessingException, InterruptedException {
        // Arrange
        when(transactionRepo.findByTransactionID(testTransactionId)).thenReturn(Optional.of(testTransaction));
        when(transactionRepo.updateTransaction(anyString(), anyString(), anyDouble(), anyString(), anyString(), anyBoolean(), anyString()))
                .thenReturn(1);
        when(savingsService.updateSavingsDataAfterTransactionUpdate(anyString(), any(Transaction.class), any(Transaction.class)))
                .thenReturn(100.0);
        when(transactionUtil.generateTransactionData(any(), anyDouble(), any(), any(TransactionEventType.class)))
                .thenReturn("test-json");

        // Act
        TransactionResponse response = transactionService.updateTransaction(testUsername, testTransactionId, testRequest);

        // Assert
        assertNotNull(response);
        verify(redisService).deleteData(testTransactionId);
        verify(eventProducer).sendTopic(eq("transaction"), eq("test-json"));
    }

    @Test
    @DisplayName("Should delete a transaction successfully")
    void deleteTransaction_ValidId_DeletesTransaction() throws JsonProcessingException {
        // Arrange
        when(transactionRepo.findByTransactionID(testTransactionId)).thenReturn(Optional.of(testTransaction));
        when(savingsService.updateSavingsAfterTransactionDelete(anyString(), any(Transaction.class))).thenReturn(100.0);
        when(transactionUtil.generateTransactionData(any(), anyDouble(), any(), any(TransactionEventType.class)))
                .thenReturn("test-json");

        // Act & Assert
        assertDoesNotThrow(() -> transactionService.deleteTransaction(testTransactionId));
        verify(transactionRepo).delete(testTransaction);
        verify(redisService).deleteData(testTransactionId);
        verify(eventProducer).sendTopic(eq("transaction"), eq("test-json"));
    }

    @Test
    @DisplayName("Should filter transactions by date range")
    void getFilteredTransaction_ValidDateRange_ReturnsFilteredTransactions() {
        // Arrange
        String startDate = testDate.minusDays(1).toString();
        String endDate = testDate.plusDays(1).toString();
        Pageable pageable = PageRequest.of(0, 10);

        when(transactionRepo.findTransactionByUsername(testUsername, pageable))
                .thenReturn(new PageImpl<>(List.of(testTransaction)));

        // Act
        Page<TransactionResponse> response = transactionService.getFilteredTransaction(
                testUsername, startDate, endDate, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should get transactions by category")
    void getTransactionsListByCategory_ValidCategory_ReturnsFilteredTransactions() {
        // Arrange
        String category = ExpenseCategory.RENT.name();
        Pageable pageable = PageRequest.of(0, 10);
        when(transactionRepo.findTransactionByUsername(testUsername, pageable))
                .thenReturn(new PageImpl<>(List.of(testTransaction)));

        // Act
        Page<TransactionResponse> response = transactionService
                .getTransactionsListByCategory(testUsername, category, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should get total transaction amount by category from cache")
    void getTransactionAmountByCategory_ExistsInCache_ReturnsCachedAmount() {
        // Arrange
        String category = ExpenseCategory.RENT.name();
        when(redisService.getData(testUsername + category, Double.class)).thenReturn(100.0);

        // Act
        double amount = transactionService.getTransactionAmountByCategory(testUsername, category);

        // Assert
        assertEquals(100.0, amount);
        verify(redisService).getData(testUsername + category, Double.class);
        verify(transactionRepo, never()).findTransactionByUsername(anyString());
    }

    @Test
    @DisplayName("Should get transactions by month and year")
    void getTransactionByMonth_ValidMonthAndYear_ReturnsTransactions() {
        // Arrange
        int month = testDate.getMonthValue();
        int year = testDate.getYear();
        Pageable pageable = PageRequest.of(0, 10);
        when(transactionRepo.findTransactionByUsername(testUsername, pageable))
                .thenReturn(new PageImpl<>(List.of(testTransaction)));

        // Act
        Page<TransactionResponse> response = transactionService
                .getTransactionByMonth(testUsername, month, year, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should get total transaction amount for month")
    void getTotalTransactionAmountInTheMonth_ValidRequest_ReturnsAmounts() {
        // Arrange
        int month = testDate.getMonthValue();
        int year = testDate.getYear();
        when(transactionRepo.findTransactionByUsername(testUsername)).thenReturn(List.of(testTransaction));

        // Act
        Map<String, Double> result = transactionService
                .getTotalTransactionAmountInTheMonth(testUsername, month, year);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("INCOME"));
        assertTrue(result.containsKey("EXPENSE"));
        assertTrue(result.containsKey("REMAIN"));
    }

    @Test
    @DisplayName("Should add recurring transaction")
    void addRecurrency_ValidTransaction_AddsToRecurring() {
        // Arrange
        testTransaction.setRecurringTransaction(true);
        when(recurringTransactionRepo.save(any(RecurringTransaction.class))).thenReturn(new RecurringTransaction());

        // Act & Assert
        assertDoesNotThrow(() -> transactionService.addRecurrency(testTransaction));
        verify(recurringTransactionRepo).save(any(RecurringTransaction.class));
    }

    @Test
    @DisplayName("Should throw exception when adding non-recurring transaction")
    void addRecurrency_NonRecurringTransaction_ThrowsException() {
        // Arrange
        testTransaction.setRecurringTransaction(false);

        // Act & Assert
        assertThrows(TransactionException.class,
                () -> transactionService.addRecurrency(testTransaction));
    }

    @Test
    @DisplayName("Should get filtered transactions for user")
    void getFilteredTransactionsForUser_ValidDateRange_ReturnsTransactions() {
        // Arrange
        String startDate = testDate.minusDays(1).toString();
        String endDate = testDate.plusDays(1).toString();
        when(transactionRepo.findTransactionByUsername(testUsername)).thenReturn(List.of(testTransaction));

        // Act
        List<Transaction> result = transactionService
                .getFilteredTransactionsForUser(testUsername, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Should validate transaction request")
    void validateTransaction_InvalidRequest_ThrowsException() {
        // Arrange
        TransactionRequest invalidRequest = new TransactionRequest();
        invalidRequest.setType("INVALID");
        invalidRequest.setAmount(-100.0);

        // Act & Assert
        assertThrows(TransactionException.class,
                () -> transactionService.addTransaction(testUsername, invalidRequest));
    }
}
