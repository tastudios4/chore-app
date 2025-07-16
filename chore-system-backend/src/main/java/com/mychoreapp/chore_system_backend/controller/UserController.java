package com.mychoreapp.chore_system_backend.controller;

import com.mychoreapp.chore_system_backend.model.User;
import com.mychoreapp.chore_system_backend.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing User-related API endpoints.
 * Handles HTTP requests and delegates business logic to the UserService.
 */
@RestController // Marks this class as a REST controller, meaning it handles web requests
@RequestMapping("/api/users") // Base path for all endpoints in this controller
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserService userService; // Declare the service dependency

    /**
     * Constructor for dependency injection.
     * Spring automatically injects an instance of UserService.
     * @param userService The service to be injected.
     */
    @Autowired // This annotation tells Spring to inject the UserService dependency
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user.
     * Endpoint: POST /api/users/register
     * @param user The User object received from the request body.
     * @return ResponseEntity with the created User and HTTP status 201 (Created),
     * or 400 (Bad Request) if username/email already exists.
     */
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody final User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED); // Return 201 Created on success
        } catch (IllegalArgumentException e) {
            // Catch validation errors from the service layer
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Return 400 Bad Request
        }
    }

    /**
     * Retrieves a user by their ID.
     * Endpoint: GET /api/users/{id}
     * @param id The ID of the user from the path variable.
     * @return ResponseEntity with the User and HTTP status 200 (OK),
     * or 404 (Not Found) if user does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable final Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK)) // If user found, return 200 OK
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)); // If not found, return 404 Not Found
    }

    /**
     * Retrieves a user by their username.
     * Endpoint: GET /api/users/by-username/{username}
     * @param username The username of the user from the path variable.
     * @return ResponseEntity with the User and HTTP status 200 (OK),
     * or 404 (Not Found) if user does not exist.
     */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable final String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Retrieves a user by their Google ID.
     * Endpoint: GET /api/users/by-google-id/{googleId}
     * @param googleId The Google ID of the user from the path variable.
     * @return ResponseEntity with the User and HTTP status 200 (OK),
     * or 404 (Not Found) if user does not exist.
     */
    @GetMapping("/by-google-id/{googleId}")
    public ResponseEntity<User> getUserByGoogleId(@PathVariable final String googleId) {
        Optional<User> user = userService.getUserByGoogleId(googleId);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Retrieves a user by their email.
     * Endpoint: GET /api/users/by-email/{email}
     * @param email The email of the user from the path variable.
     * @return ResponseEntity with the User and HTTP status 200 (OK),
     * or 404 (Not Found) if user does not exist.
     */
    @GetMapping("/by-email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable final String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Retrieves all users.
     * Endpoint: GET /api/users
     * @return ResponseEntity with a list of all Users and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Adds points to an existing user.
     * Endpoint: PUT /api/users/{id}/add-points/{pointsToAdd}
     * @param id The ID of the user to update.
     * @param pointsToAdd The number of points to add.
     * @return ResponseEntity with the updated User and HTTP status 200 (OK),
     * or 404 (Not Found) if user does not exist.
     */
    @PutMapping("/{id}/add-points/{pointsToAdd}")
    public ResponseEntity<User> addPointsToUser(@PathVariable final Long id, @PathVariable final int pointsToAdd) {
        Optional<User> updatedUser = userService.addPointsToUser(id, pointsToAdd);
        return updatedUser.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                         .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Deletes a user by their ID.
     * Endpoint: DELETE /api/users/{id}
     * @param id The ID of the user to delete.
     * @return ResponseEntity with HTTP status 204 (No Content) on successful deletion,
     * or 404 (Not Found) if user does not exist (though deleteById often doesn't throw on non-existence).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable final Long id) {
        // Check if user exists before attempting to delete (optional, but good practice for clearer responses)
        if (userService.getUserById(id).isPresent()) {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful deletion
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found if user doesn't exist
        }
    }

    /**
     * Endpoint for a user to join a tribe.
     * Endpoint: PUT /api/users/{userId}/join-tribe/{joinCode}
     * @param userId The ID of the user.
     * @param joinCode The join code of the tribe to join.
     * @return ResponseEntity with the updated User and HTTP status 200 (OK),
     * or 400 (Bad Request) if user is already in a tribe,
     * or 404 (Not Found) if user or tribe does not exist.
     */
    @PutMapping("/{userId}/join-tribe/{joinCode}")
    public ResponseEntity<User> joinTribe(@PathVariable final Long userId, @PathVariable final String joinCode) {
        try {
            Optional<User> updatedUser = userService.joinTribe(userId, joinCode);
            return updatedUser.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                              .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // User already in a tribe
        }
    }

    /**
     * Endpoint for a user to leave their current tribe.
     * Endpoint: PUT /api/users/{userId}/leave-tribe
     * @param userId The ID of the user.
     * @return ResponseEntity with the updated User and HTTP status 200 (OK),
     * or 400 (Bad Request) if user is not in a tribe,
     * or 404 (Not Found) if user does not exist.
     */
    @PutMapping("/{userId}/leave-tribe")
    public ResponseEntity<User> leaveTribe(@PathVariable final Long userId) {
        try {
            Optional<User> updatedUser = userService.leaveTribe(userId);
            return updatedUser.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                              .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // User not in a tribe
        }
    }
}