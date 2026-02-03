package com.example.user_service.service;

import com.example.user_service.dto.request.AuthRequest;
import com.example.user_service.dto.request.UserRequest;
import com.example.user_service.dto.response.UserResponse;
import com.example.user_service.exceptions.UserException;
import com.example.user_service.model.User;
import com.example.user_service.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisService redisService;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequest testUserRequest;
    private AuthRequest testAuthRequest;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setContactNumber("1234567890");

        testUserRequest = new UserRequest();
        testUserRequest.setUsername("testuser");
        testUserRequest.setPassword("password123");
        testUserRequest.setEmail("test@example.com");
        testUserRequest.setFirstName("Test");
        testUserRequest.setLastName("User");
        testUserRequest.setContactNumber("1234567890");

        testAuthRequest = new AuthRequest("testuser", "password123");
    }

    @Test
    @DisplayName("Should return true when user exists in Redis and password matches")
    void validateUserCredentials_UserExistsInRedis_ReturnsTrue() {
        // Arrange
        when(redisService.getData(anyString(), eq(User.class))).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        boolean result = userService.validateUserCredentials(testAuthRequest);

        // Assert
        assertTrue(result);
        verify(redisService).getData(testAuthRequest.getUsername(), User.class);
        verify(userRepo, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Should fetch user from DB and return true when not in Redis but exists in database")
    void validateUserCredentials_UserNotInRedisButInDB_ReturnsTrue() {
        // Arrange
        when(redisService.getData(anyString(), eq(User.class))).thenReturn(null);
        when(userRepo.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        boolean result = userService.validateUserCredentials(testAuthRequest);

        // Assert
        assertTrue(result);
        verify(redisService).setData(eq(testAuthRequest.getUsername()), any(User.class), anyLong());
    }

    @Test
    @DisplayName("Should throw UserException when user not found in both Redis and database")
    void validateUserCredentials_UserNotFound_ThrowsUserException() {
        // Arrange
        when(redisService.getData(anyString(), eq(User.class))).thenReturn(null);
        when(userRepo.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserException.class, () -> userService.validateUserCredentials(testAuthRequest));
    }

    @Test
    @DisplayName("Should successfully register a new user and initialize default budgets")
    void registerUser_ValidUser_ReturnsTrue() {
        // Arrange
        when(userRepo.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        // Mock WebClient chain for initializeDefaultBudgets
        WebClient mockWebClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec mockRequestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec mockRequestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec mockResponseSpec = mock(WebClient.ResponseSpec.class);
        reactor.core.publisher.Mono<Void> mockMono = reactor.core.publisher.Mono.empty();

        when(webClientBuilder.build()).thenReturn(mockWebClient);
        when(mockWebClient.post()).thenReturn(mockRequestBodyUriSpec);
        when(mockRequestBodyUriSpec.uri(anyString())).thenReturn(mockRequestBodySpec);
        when(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.bodyToMono(Void.class)).thenReturn(mockMono);

        // Act
        boolean result = userService.registerUser(testUserRequest);

        // Assert
        assertTrue(result);
        verify(userRepo).save(any(User.class));
        verify(redisService).setData(eq(testUserRequest.getUsername()), any(User.class), anyLong());
    }

    @Test
    @DisplayName("Should throw UserException when registering with an existing username")
    void registerUser_UserAlreadyExists_ThrowsUserException() {
        // Arrange
        when(userRepo.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(UserException.class, () -> userService.registerUser(testUserRequest));
    }

    @Test
    @DisplayName("Should retrieve user details from Redis when available")
    void getUserDetails_UserInRedis_ReturnsUserResponse() {
        // Arrange
        when(redisService.getData(anyString(), eq(User.class))).thenReturn(testUser);

        // Act
        UserResponse response = userService.getUserDetails(testUser.getUsername());

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getUsername(), response.getUsername());
        verify(redisService).getData(testUser.getUsername(), User.class);
        verify(userRepo, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Should update user details and clear cache when valid update request is provided")
    void updateUserDetails_ValidUpdate_ReturnsUpdatedUser() {
        // Arrange
        when(userRepo.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userRepo.updateUserDetailsByUsername(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1);

        // Act
        UserResponse response = userService.updateUserDetails(testUserRequest);

        // Assert
        assertNotNull(response);
        verify(redisService).deleteData(testUserRequest.getUsername());
        verify(userRepo).updateUserDetailsByUsername(
                eq(testUserRequest.getUsername()),
                eq(testUserRequest.getFirstName()),
                eq(testUserRequest.getLastName()),
                eq(testUserRequest.getEmail()),
                eq(testUserRequest.getContactNumber()));
    }

    @Test
    @DisplayName("Should update password and return true for valid user")
    void updatePassword_ValidPassword_ReturnsTrue() {
        // Arrange
        when(userRepo.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepo.updateUserPasswordByUsername(anyString(), anyString())).thenReturn(1);

        // Act
        boolean result = userService.updatePassword("testuser", "newPassword");

        // Assert
        assertTrue(result);
        verify(userRepo).updateUserPasswordByUsername(eq("testuser"), eq("newEncodedPassword"));
    }

    @Test
    @DisplayName("Should delete user from both database and cache when user exists")
    void deleteUser_ValidUser_DeletesSuccessfully() {
        // Arrange
        when(userRepo.isUserExists(anyString())).thenReturn(true);
        doNothing().when(userRepo).deleteById(anyString());
        doNothing().when(redisService).deleteData(anyString());

        // Act & Assert
        assertDoesNotThrow(() -> userService.deleteUser("testuser"));
        verify(userRepo).deleteById("testuser");
        verify(redisService).deleteData("testuser");
    }

    @Test
    @DisplayName("Should throw UserException when attempting to delete non-existent user")
    void deleteUser_UserNotFound_ThrowsUserException() {
        // Arrange
        when(userRepo.isUserExists(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(UserException.class, () -> userService.deleteUser("nonexistent"));
    }
}
