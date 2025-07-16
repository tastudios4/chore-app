package com.mychoreapp.chore_system_backend.controller; 

import com.mychoreapp.chore_system_backend.model.ChoreCompletion;
import com.mychoreapp.chore_system_backend.service.ChoreCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing ChoreCompletion-related API endpoints.
 * Handles HTTP requests and delegates business logic to the ChoreCompletionService.
 */
@RestController
@RequestMapping("/api/chore-completions")
@CrossOrigin(origins = "http://localhost:3000")
public class ChoreCompletionController {

    private final ChoreCompletionService choreCompletionService;

    /**
     * Constructor for dependency injection.
     * Spring automatically injects an instance of ChoreCompletionService.
     * @param choreCompletionService The service to be injected.
     */
    @Autowired
    public ChoreCompletionController(final ChoreCompletionService choreCompletionService) {
        this.choreCompletionService = choreCompletionService;
    }

    /**
     * Records a chore completion for a specific chore by a specific user.
     * This endpoint will trigger the point award and recurring chore logic.
     * Endpoint: POST /api/chore-completions/{choreId}/complete-by/{userId}
     * @param choreId The ID of the chore that was completed.
     * @param userId The ID of the user who completed the chore.
     * @return ResponseEntity with the created ChoreCompletion record and HTTP status 201 (Created),
     * or 400 (Bad Request) if validation fails (e.g., chore/user not found, user not in chore's tribe).
     */
    @PostMapping("/{choreId}/complete-by/{userId}")
    public ResponseEntity<ChoreCompletion> recordChoreCompletion(
            @PathVariable final Long choreId,
            @PathVariable final Long userId) {
        try {
            final ChoreCompletion newCompletion = choreCompletionService.recordChoreCompletion(choreId, userId);
            return new ResponseEntity<>(newCompletion, HttpStatus.CREATED); // Return 201 Created on success
        } catch (IllegalArgumentException e) {
            // Catch validation errors from the service layer
            // Distinguish between 400 (bad request data) and 404 (resource not found)
            if (e.getMessage().contains("not found")) { // Simple check, more robust error handling could use custom exceptions
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Return 400 Bad Request
        }
    }

    /**
     * Retrieves a chore completion record by its ID.
     * Endpoint: GET /api/chore-completions/{id}
     * @param id The ID of the chore completion record from the path variable.
     * @return ResponseEntity with the ChoreCompletion and HTTP status 200 (OK),
     * or 404 (Not Found) if record does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChoreCompletion> getChoreCompletionById(@PathVariable final Long id) {
        final Optional<ChoreCompletion> completion = choreCompletionService.getChoreCompletionById(id);
        return completion.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Retrieves all chore completion records for a specific user.
     * Endpoint: GET /api/chore-completions/user/{userId}
     * @param userId The ID of the user.
     * @return ResponseEntity with a list of ChoreCompletion records completed by the specified user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChoreCompletion>> getChoreCompletionsByUser(@PathVariable final Long userId) {
        final List<ChoreCompletion> completions = choreCompletionService.getChoreCompletionsByUser(userId);
        return new ResponseEntity<>(completions, HttpStatus.OK);
    }

    /**
     * Retrieves all chore completion records for a specific chore.
     * Endpoint: GET /api/chore-completions/chore/{choreId}
     * @param choreId The ID of the chore.
     * @return ResponseEntity with a list of ChoreCompletion records for the specified chore.
     */
    @GetMapping("/chore/{choreId}")
    public ResponseEntity<List<ChoreCompletion>> getChoreCompletionsByChore(@PathVariable final Long choreId) {
        final List<ChoreCompletion> completions = choreCompletionService.getChoreCompletionsByChore(choreId);
        return new ResponseEntity<>(completions, HttpStatus.OK);
    }

    /**
     * Retrieves all chore completion records for a specific tribe within a given date range.
     * Endpoint: GET /api/chore-completions/tribe/{tribeId}/range
     * Query Parameters: startDate (yyyy-MM-ddTHH:mm:ss), endDate (yyyy-MM-ddTHH:mm:ss)
     * Example: /api/chore-completions/tribe/1/range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
     * @param tribeId The ID of the tribe.
     * @param startDate The start date/time for the search range.
     * @param endDate The end date/time for the search range.
     * @return ResponseEntity with a list of ChoreCompletion records.
     */
    @GetMapping("/tribe/{tribeId}/range")
    public ResponseEntity<List<ChoreCompletion>> getChoreCompletionsByTribeAndDateRange(
            @PathVariable final Long tribeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime endDate) {
        final List<ChoreCompletion> completions = choreCompletionService.getChoreCompletionsByTribeAndDateRange(tribeId, startDate, endDate);
        return new ResponseEntity<>(completions, HttpStatus.OK);
    }

    /**
     * Retrieves all chore completion records for a specific user within a given date range.
     * Endpoint: GET /api/chore-completions/user/{userId}/range
     * Query Parameters: startDate (yyyy-MM-ddTHH:mm:ss), endDate (yyyy-MM-ddTHH:mm:ss)
     * Example: /api/chore-completions/user/1/range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
     * @param userId The ID of the user.
     * @param startDate The start date/time for the search range.
     * @param endDate The end date/time for the search range.
     * @return ResponseEntity with a list of ChoreCompletion records.
     */
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<ChoreCompletion>> getChoreCompletionsByUserAndDateRange(
            @PathVariable final Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime endDate) {
        final List<ChoreCompletion> completions = choreCompletionService.getChoreCompletionsByUserAndDateRange(userId, startDate, endDate);
        return new ResponseEntity<>(completions, HttpStatus.OK);
    }

    /**
     * Retrieves all chore completion records in the system (for administrative purposes).
     * Endpoint: GET /api/chore-completions/all
     * @return ResponseEntity with a list of all ChoreCompletion objects and HTTP status 200 (OK).
     */
    @GetMapping("/all")
    public ResponseEntity<List<ChoreCompletion>> getAllChoreCompletions() {
        final List<ChoreCompletion> completions = choreCompletionService.getAllChoreCompletions();
        return new ResponseEntity<>(completions, HttpStatus.OK);
    }

    /**
     * Deletes a chore completion record by its ID.
     * Note: Deleting a completion record does NOT automatically deduct points from the user.
     * If point deduction is required, it must be handled separately.
     * Endpoint: DELETE /api/chore-completions/{id}
     * @param id The ID of the chore completion record to delete.
     * @return ResponseEntity with HTTP status 204 (No Content) on successful deletion,
     * or 404 (Not Found) if record does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChoreCompletion(@PathVariable final Long id) {
        if (choreCompletionService.getChoreCompletionById(id).isPresent()) {
            choreCompletionService.deleteChoreCompletion(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful deletion
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found if record doesn't exist
        }
    }
}