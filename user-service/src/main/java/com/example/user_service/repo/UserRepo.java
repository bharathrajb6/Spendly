package com.example.user_service.repo;

import com.example.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, String> {

    /**
     * Finds a user by the given username.
     *
     * @param username The username to search for.
     * @return An Optional containing the user if found, or an empty Optional if not found.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by the given email.
     *
     * @param email The email address to search for.
     * @return An Optional containing the user if found, or an empty Optional if not found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Updates the user details (firstName, lastName, email, contactNumber) for the given username.
     *
     * @param username      The username to update.
     * @param firstName     The new first name.
     * @param lastName      The new last name.
     * @param email         The new email address.
     * @param contactNumber The new contact number.
     * @return The number of rows affected by the update (should be 1 if the username exists).
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.firstName = :firstName, u.lastName = :lastName, u.email = :email, u.contactNumber = :contactNumber WHERE u.username = :username")
    int updateUserDetailsByUsername(String username, String firstName, String lastName, String email, String contactNumber);

    /**
     * Updates the password for the given username.
     *
     * @param username The username to update.
     * @param password The new password to set.
     * @return The number of rows affected by the update (should be 1 if the username exists).
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :password WHERE u.username = :username")
    int updateUserPasswordByUsername(String username, String password);
}
