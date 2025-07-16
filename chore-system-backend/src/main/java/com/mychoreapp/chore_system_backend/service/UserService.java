package com.mychoreapp.chore_system_backend.service;

import com.mychoreapp.chore_system_backend.repository.IUserRepository;
import com.mychoreapp.chore_system_backend.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


/**
 * Service class for managing User-related business logic.
 * It interacts with the IUserRepository to perform database operations.
 */
@Service // Marks this class as a Spring service component
public class UserService {

    private final IUserRepository userRepository; 

    /**
     * Constructor for dependency injection.
     * Spring automatically injects an instance of IUserRepository.
     * @param userRepository The repository to be injected.
     */
    @Autowired // This annotation tells Spring to inject the IUserRepository dependency
    public UserService(final IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user.
     * Performs validation to ensure username and email are unique.
     * For simplicity, password is not hashed here, but it should be in a real application.
     * @param user The User object to register.
     * @return The saved User object with its generated ID.
     * @throws IllegalArgumentException if username or email already exists.
     */
    public User registerUser(final User user) {
        // Check if username already exists
        if (user.getUsername() != null && userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        // Check if email already exists (for Google SSO users or future email-based logins)
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // For a real application, passwords should be hashed here before saving
        // user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user); // Save the user to the database
    }

    /**
     * Retrieves a user by their ID.
     * @param id The ID of the user to retrieve.
     * @return An Optional containing the User if found, or empty if not found.
     */
    public Optional<User> getUserById(final Long id) {
        return userRepository.findById(id);
    }

    /**
     * Retrieves a user by their username.
     * @param username The username of the user to retrieve.
     * @return An Optional containing the User if found, or empty if not found.
     */
    public Optional<User> getUserByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Retrieves a user by their Google ID.
     * @param googleId The Google ID of the user to retrieve.
     * @return An Optional containing the User if found, or empty if not found.
     */
    public Optional<User> getUserByGoogleId(final String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    /**
     * Retrieves a user by their email.
     * @param email The email of the user to retrieve.
     * @return An Optional containing the User if found, or empty if not found.
     */
    public Optional<User> getUserByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Retrieves all users.
     * @return A list of all User objects.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Updates an existing user's points.
     * @param id The ID of the user to update.
     * @param pointsToAdd The number of points to add.
     * @return The updated User object, or empty if user not found.
     */
    public Optional<User> addPointsToUser(final Long id, final int pointsToAdd) {
        return userRepository.findById(id).map(user -> {
            user.addPoints(pointsToAdd); // Use the addPoints method from the User entity
            return userRepository.save(user); // Save the updated user
        });
    }

    /**
     * Deletes a user by their ID.
     * @param id The ID of the user to delete.
     */
    public void deleteUser(final Long id) {
        userRepository.deleteById(id); // JpaRepository provides deleteById
    }
}