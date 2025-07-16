package com.mychoreapp.chore_system_backend.controller;

import com.mychoreapp.chore_system_backend.model.Tribe;
import com.mychoreapp.chore_system_backend.service.TribeService;

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

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Tribe-related API endpoints.
 * Handles HTTP requests and delegates business logic to the TribeService.
 */
@RestController // Marks this class as a REST controller
@RequestMapping("/api/tribes") // Base path for all endpoints in this controller
public class TribeController {

    private final TribeService tribeService; // Declare the service dependency

    /**
     * Constructor for dependency injection.
     * Spring automatically injects an instance of TribeService.
     * @param tribeService The service to be injected.
     */
    @Autowired // This annotation tells Spring to inject the TribeService dependency
    public TribeController(final TribeService tribeService) {
        this.tribeService = tribeService;
    }

    /**
     * Creates a new tribe.
     * Endpoint: POST /api/tribes
     * @param tribe The Tribe object received from the request body.
     * @return ResponseEntity with the created Tribe and HTTP status 201 (Created),
     * or 400 (Bad Request) if tribe name already exists.
     */
    @PostMapping
    public ResponseEntity<Tribe> createTribe(@RequestBody final Tribe tribe) {
        try {
            Tribe createdTribe = tribeService.createTribe(tribe);
            return new ResponseEntity<>(createdTribe, HttpStatus.CREATED); // Return 201 Created on success
        } catch (IllegalArgumentException e) {
            // Catch validation errors from the service layer (e.g., duplicate name)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Return 400 Bad Request
        }
    }

    /**
     * Retrieves a tribe by its ID.
     * Endpoint: GET /api/tribes/{id}
     * @param id The ID of the tribe from the path variable.
     * @return ResponseEntity with the Tribe and HTTP status 200 (OK),
     * or 404 (Not Found) if tribe does not exist.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tribe> getTribeById(@PathVariable final Long id) {
        Optional<Tribe> tribe = tribeService.getTribeById(id);
        return tribe.map(value -> new ResponseEntity<>(value, HttpStatus.OK)) // If tribe found, return 200 OK
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)); // If not found, return 404 Not Found
    }

    /**
     * Retrieves a tribe by its name.
     * Endpoint: GET /api/tribes/by-name/{name}
     * @param name The name of the tribe from the path variable.
     * @return ResponseEntity with the Tribe and HTTP status 200 (OK),
     * or 404 (Not Found) if tribe does not exist.
     */
    @GetMapping("/by-name/{name}")
    public ResponseEntity<Tribe> getTribeByName(@PathVariable final String name) {
        Optional<Tribe> tribe = tribeService.getTribeByName(name);
        return tribe.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Retrieves a tribe by its join code.
     * Endpoint: GET /api/tribes/by-join-code/{joinCode}
     * @param joinCode The join code of the tribe from the path variable.
     * @return ResponseEntity with the Tribe and HTTP status 200 (OK),
     * or 404 (Not Found) if tribe does not exist.
     */
    @GetMapping("/by-join-code/{joinCode}")
    public ResponseEntity<Tribe> getTribeByJoinCode(@PathVariable final String joinCode) {
        Optional<Tribe> tribe = tribeService.getTribeByJoinCode(joinCode);
        return tribe.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Retrieves all tribes.
     * Endpoint: GET /api/tribes
     * @return ResponseEntity with a list of all Tribes and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Tribe>> getAllTribes() {
        List<Tribe> tribes = tribeService.getAllTribes();
        return new ResponseEntity<>(tribes, HttpStatus.OK);
    }

    /**
     * Updates an existing tribe.
     * Endpoint: PUT /api/tribes/{id}
     * @param id The ID of the tribe to update from the path variable.
     * @param tribe The Tribe object with updated information received from the request body.
     * @return ResponseEntity with the updated Tribe and HTTP status 200 (OK),
     * or 400 (Bad Request) if tribe ID is missing or name already exists,
     * or 404 (Not Found) if the tribe to update does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tribe> updateTribe(@PathVariable final Long id, @RequestBody final Tribe tribe) {
        if (!tribe.getId().equals(id)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // ID in path must match ID in body
        }
        try {
            Optional<Tribe> existingTribe = tribeService.getTribeById(id);
            if (existingTribe.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Tribe to update not found
            }
            Tribe updatedTribe = tribeService.updateTribe(tribe);
            return new ResponseEntity<>(updatedTribe, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Duplicate name or other validation error
        }
    }

    /**
     * Deletes a tribe by its ID.
     * Endpoint: DELETE /api/tribes/{id}
     * @param id The ID of the tribe to delete.
     * @return ResponseEntity with HTTP status 204 (No Content) on successful deletion,
     * or 404 (Not Found) if tribe does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTribe(@PathVariable final Long id) {
        if (tribeService.getTribeById(id).isPresent()) {
            tribeService.deleteTribe(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful deletion
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found if tribe doesn't exist
        }
    }
}
