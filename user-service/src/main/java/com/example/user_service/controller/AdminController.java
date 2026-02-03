package com.example.user_service.controller;

import com.example.user_service.model.User;
import com.example.user_service.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserRepo userRepo;

    /**
     * Get all users - admin only
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "X-Username") String adminUsername) {
        // Verify admin role
        if (!isAdmin(adminUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admin role required.");
        }

        List<User> users = userRepo.findAll();
        List<Map<String, Object>> userList = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("username", user.getUsername());
            userMap.put("firstName", user.getFirstName());
            userMap.put("lastName", user.getLastName());
            userMap.put("email", user.getEmail());
            userMap.put("contactNumber", user.getContactNumber());
            userMap.put("role", user.getRole());
            userMap.put("active", user.getActive());
            userMap.put("createdAt", user.getCreatedAt());
            return userMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(userList);
    }

    /**
     * Get admin dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats(@RequestHeader(value = "X-Username") String adminUsername) {
        if (!isAdmin(adminUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admin role required.");
        }

        List<User> users = userRepo.findAll();
        long totalUsers = users.size();
        long activeUsers = users.stream().filter(u -> Boolean.TRUE.equals(u.getActive())).count();
        long adminUsers = users.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getRole())).count();

        // Count users created in the last 7 days
        long newUsersWeek = users.stream()
                .filter(u -> u.getCreatedAt() != null)
                .filter(u -> u.getCreatedAt().toInstant().isAfter(
                        java.time.Instant.now().minus(java.time.Duration.ofDays(7))))
                .count();

        // Count users created in the last 30 days
        long newUsersMonth = users.stream()
                .filter(u -> u.getCreatedAt() != null)
                .filter(u -> u.getCreatedAt().toInstant().isAfter(
                        java.time.Instant.now().minus(java.time.Duration.ofDays(30))))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);
        stats.put("adminUsers", adminUsers);
        stats.put("regularUsers", totalUsers - adminUsers);
        stats.put("newUsersThisWeek", newUsersWeek);
        stats.put("newUsersThisMonth", newUsersMonth);

        return ResponseEntity.ok(stats);
    }

    /**
     * Toggle user active status
     */
    @PutMapping("/users/{username}/toggle-active")
    public ResponseEntity<?> toggleUserActive(
            @RequestHeader(value = "X-Username") String adminUsername,
            @PathVariable String username) {

        if (!isAdmin(adminUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admin role required.");
        }

        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Toggle active status
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        userRepo.save(user);

        log.info("Admin {} toggled active status for user {} to {}", adminUsername, username, user.getActive());

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("active", user.getActive());
        response.put("message", "User status updated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Promote user to admin
     */
    @PutMapping("/users/{username}/make-admin")
    public ResponseEntity<?> makeUserAdmin(
            @RequestHeader(value = "X-Username") String adminUsername,
            @PathVariable String username) {

        if (!isAdmin(adminUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admin role required.");
        }

        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        user.setRole("ADMIN");
        userRepo.save(user);

        log.info("Admin {} promoted user {} to ADMIN", adminUsername, username);

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("message", "User promoted to admin successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Demote admin to regular user
     */
    @PutMapping("/users/{username}/remove-admin")
    public ResponseEntity<?> removeUserAdmin(
            @RequestHeader(value = "X-Username") String adminUsername,
            @PathVariable String username) {

        if (!isAdmin(adminUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admin role required.");
        }

        // Prevent removing your own admin role
        if (adminUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot remove your own admin role");
        }

        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        user.setRole("USER");
        userRepo.save(user);

        log.info("Admin {} demoted user {} to USER", adminUsername, username);

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("message", "Admin role removed successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get current user's role
     */
    @GetMapping("/role")
    public ResponseEntity<?> getUserRole(@RequestHeader(value = "X-Username") String username) {
        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("isAdmin", "ADMIN".equalsIgnoreCase(user.getRole()));

        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to check if user is admin
     */
    private boolean isAdmin(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        User user = userRepo.findByUsername(username).orElse(null);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
