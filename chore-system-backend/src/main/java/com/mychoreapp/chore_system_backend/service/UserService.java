package com.mychoreapp.chore_system_backend.service;

import com.mychoreapp.chore_system_backend.model.Tribe;
import com.mychoreapp.chore_system_backend.model.User;
import com.mychoreapp.chore_system_backend.repository.IUserRepository;
import com.mychoreapp.chore_system_backend.repository.ITribeRepository;

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
    private final ITribeRepository tribeRepository;

    /**
     * Constructor for dependency injection.
     * Spring automatically injects an instance of IUserRepository.
     * @param userRepository The repository to be injected.
     * @param tribeRepository The repository to be injected.
     */
    @Autowired // This annotation tells Spring to inject the IUserRepository dependency
    public UserService(final IUserRepository userRepository, 
                       final ITribeRepository tribeRepository) {
        this.userRepository = userRepository;
        this.tribeRepository = tribeRepository;
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

        return userRepository.save(user);
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
            user.addPoints(pointsToAdd); 
            return userRepository.save(user); 
        });
    }

    /**
     * Deletes a user by their ID.
     * @param id The ID of the user to delete.
     */
    public void deleteUser(final Long id) {
        userRepository.deleteById(id); 
    }

    /**
     * Assigns a user to a tribe using the tribe's join code.
     * @param userId The ID of the user to assign.
     * @param joinCode The join code of the tribe to join.
     * @return An Optional containing the updated User object if successful, or empty if user or tribe not found.
     * @throws IllegalArgumentException if the user is already in a tribe or if the join code is invalid.
     */
    public Optional<User> joinTribe(final Long userId, final String joinCode) {
        final Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        final User user = optionalUser.get();

        // check if user already belongs to a tribe
        // NON MVP: user can belong to multiple tribes
        if (user.getTribe() != null) {
            throw new IllegalArgumentException("User already belongs to a tribe");
        }

        // check if tribe exists
        final Optional<Tribe> optionalTribe = tribeRepository.findByJoinCode(joinCode);
        if (optionalTribe.isEmpty()) {
            return Optional.empty();
        }
        final Tribe tribe = optionalTribe.get();
        
        // set the tribe for the user
        user.setTribe(tribe);
        return Optional.of(userRepository.save(user));
    }

    /**
     * Removes a user from their current tribe.
     * @param userId The ID of the user to remove from a tribe.
     * @return An Optional containing the updated User object if successful, or empty if user not found.
     * @throws IllegalArgumentException if the user is not currently in a tribe.
     */
    public Optional<User> leaveTribe(final Long userId) {
        final Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        final User user = optionalUser.get();
        
        // Check if user is actually in a tribe before trying to remove them
        if (user.getTribe() == null) {
            throw new IllegalArgumentException("User is not currently a member of any tribe.");
        }

        // Set the user's tribe to null to remove them from the tribe
        user.setTribe(null);
        return Optional.of(userRepository.save(user));
    }
}