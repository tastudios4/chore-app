package com.mychoreapp.chore_system_backend.controller; 

import com.mychoreapp.chore_system_backend.model.Chore;
import com.mychoreapp.chore_system_backend.service.ChoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Chore-related API endpoints.
 * Handles HTTP requests and delegates business logic to the ChoreService.
 */
@RestController
@RequestMapping("/api/chores")
public class ChoreController {

    private final ChoreService choreService; 

    /**
     * Constructor for dependency injection.
     * Spring automatically injects an instance of ChoreService.
     * @param choreService The service to be injected.
     */
    @Autowired
    public ChoreController(final ChoreService choreService) {
        this.choreService = choreService;
    }

    /**
     * Creates a new chore within a specific tribe.
     * Endpoint: POST /api/chores/tribe/{tribeId}
     * @param tribeId The ID of the tribe this chore belongs to, from the path variable.
     * @param chore The Chore object received from the request body.
     * @return ResponseEntity with the created Chore and HTTP status 201 (Created),
     * or 400 (Bad Request) if validation fails (e.g., tribe not found, duplicate name).
     */
    @PostMapping("/tribe/{tribeId}")
    public ResponseEntity<Chore> createChore(@PathVariable final Long tribeId, @RequestBody final Chore chore) {
        try {
            // The chore object might contain a dummy tribe/user, the service will set the correct references
            Chore createdChore = choreService.createChore(chore, tribeId);
            return new ResponseEntity<>(createdChore, HttpStatus.CREATED); // Return 201 Created on success
        } catch (IllegalArgumentException e) {
            // Catch validation errors from the service layer
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Return 400 Bad Request
        }
    }

    /**
     * Retrieves a chore by its ID.
     * Endpoint: GET /api/chores/{id}
     * @param id The ID of the chore from the path variable.
     * @return ResponseEntity with the Chore and HTTP status 200 (OK),
     * or 404 (Not Found) if chore does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Chore> getChoreById(@PathVariable final Long id) {
        Optional<Chore> chore = choreService.getChoreById(id);
        return chore.map(value -> new ResponseEntity<>(value, HttpStatus.OK)) // If chore found, return 200 OK
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)); // If not found, return 404 Not Found
    }

    /**
     * Retrieves all chores for a specific tribe.
     * Endpoint: GET /api/chores/tribe/{tribeId}
     * @param tribeId The ID of the tribe.
     * @return ResponseEntity with a list of chores belonging to the specified tribe and HTTP status 200 (OK).
     */
    @GetMapping("/tribe/{tribeId}")
    public ResponseEntity<List<Chore>> getChoresByTribe(@PathVariable final Long tribeId) {
        List<Chore> chores = choreService.getChoresByTribe(tribeId);
        return new ResponseEntity<>(chores, HttpStatus.OK);
    }

    /**
     * Retrieves all active chores for a specific tribe.
     * Endpoint: GET /api/chores/tribe/{tribeId}/active
     * @param tribeId The ID of the tribe.
     * @return ResponseEntity with a list of active chores belonging to the specified tribe and HTTP status 200 (OK).
     */
    @GetMapping("/tribe/{tribeId}/active")
    public ResponseEntity<List<Chore>> getActiveChoresByTribe(@PathVariable final Long tribeId) {
        List<Chore> chores = choreService.getActiveChoresByTribe(tribeId);
        return new ResponseEntity<>(chores, HttpStatus.OK);
    }

    /**
     * Retrieves all chores assigned to a specific user.
     * Endpoint: GET /api/chores/assigned-to/{userId}
     * @param userId The ID of the user.
     * @return ResponseEntity with a list of chores assigned to the specified user and HTTP status 200 (OK).
     */
    @GetMapping("/assigned-to/{userId}")
    public ResponseEntity<List<Chore>> getChoresAssignedToUser(@PathVariable final Long userId) {
        List<Chore> chores = choreService.getChoresAssignedToUser(userId);
        return new ResponseEntity<>(chores, HttpStatus.OK);
    }

    /**
     * Retrieves all active chores assigned to a specific user.
     * Endpoint: GET /api/chores/assigned-to/{userId}/active
     * @param userId The ID of the user.
     * @return ResponseEntity with a list of active chores assigned to the specified user and HTTP status 200 (OK).
     */
    @GetMapping("/assigned-to/{userId}/active")
    public ResponseEntity<List<Chore>> getActiveChoresAssignedToUser(@PathVariable final Long userId) {
        List<Chore> chores = choreService.getActiveChoresAssignedToUser(userId);
        return new ResponseEntity<>(chores, HttpStatus.OK);
    }

    /**
     * Retrieves all chores in the system (for administrative purposes).
     * Endpoint: GET /api/chores/all
     * @return ResponseEntity with a list of all Chore objects and HTTP status 200 (OK).
     */
    @GetMapping("/all")
    public ResponseEntity<List<Chore>> getAllChores() {
        List<Chore> chores = choreService.getAllChores();
        return new ResponseEntity<>(chores, HttpStatus.OK);
    }

    /**
     * Updates an existing chore.
     * Endpoint: PUT /api/chores/{id}
     * @param id The ID of the chore to update from the path variable.
     * @param chore The Chore object with updated information received from the request body.
     * @return ResponseEntity with the updated Chore and HTTP status 200 (OK),
     * or 400 (Bad Request) if validation fails (e.g., ID mismatch, duplicate name, invalid tribe/user).
     * or 404 (Not Found) if the chore to update does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Chore> updateChore(@PathVariable final Long id, @RequestBody final Chore chore) {
        try {
            Chore updatedChore = choreService.updateChore(id, chore);
            return new ResponseEntity<>(updatedChore, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Distinguish between 400 (bad request data) and 404 (resource not found)
            if (e.getMessage().contains("not found")) { // Simple check, more robust error handling could use custom exceptions
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes a chore by its ID.
     * Endpoint: DELETE /api/chores/{id}
     * @param id The ID of the chore to delete.
     * @return ResponseEntity with HTTP status 204 (No Content) on successful deletion,
     * or 404 (Not Found) if chore does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChore(@PathVariable final Long id) {
        // Check if chore exists before attempting to delete for a clearer 404 response
        if (choreService.getChoreById(id).isPresent()) {
            choreService.deleteChore(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful deletion
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found if chore doesn't exist
        }
    }

    /**
     * Assigns a chore to a specific user.
     * Endpoint: PUT /api/chores/{choreId}/assign/{userId}
     * @param choreId The ID of the chore to assign.
     * @param userId The ID of the user to assign the chore to.
     * @return ResponseEntity with the updated Chore and HTTP status 200 (OK),
     * or 400 (Bad Request) if validation fails (e.g., user not in chore's tribe).
     * or 404 (Not Found) if chore or user does not exist.
     */
    @PutMapping("/{choreId}/assign/{userId}")
    public ResponseEntity<Chore> assignChore(@PathVariable final Long choreId, @PathVariable final Long userId) {
        try {
            Chore assignedChore = choreService.assignChore(choreId, userId);
            return new ResponseEntity<>(assignedChore, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Unassigns a chore from any user it's currently assigned to.
     * Endpoint: PUT /api/chores/{choreId}/unassign
     * @param choreId The ID of the chore to unassign.
     * @return ResponseEntity with the updated Chore and HTTP status 200 (OK),
     * or 404 (Not Found) if chore does not exist.
     */
    @PutMapping("/{choreId}/unassign")
    public ResponseEntity<Chore> unassignChore(@PathVariable final Long choreId) {
        try {
            Chore unassignedChore = choreService.unassignChore(choreId);
            return new ResponseEntity<>(unassignedChore, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}