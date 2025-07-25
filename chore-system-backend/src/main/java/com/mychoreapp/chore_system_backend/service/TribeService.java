package com.mychoreapp.chore_system_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.mychoreapp.chore_system_backend.repository.ITribeRepository;
import com.mychoreapp.chore_system_backend.model.Tribe;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Tribe-related business logic.
 * It interacts with the ITribeRepository to perform database operations.
 */
@Service
public class TribeService {

    private final ITribeRepository tribeRepository;

    /**
     * Constructor for dependency injection.
     * Spring automatically injects an instance of ITribeRepository.
     * @param tribeRepository The repository to be injected.
     */
    @Autowired
    public TribeService(final ITribeRepository tribeRepository) {
        this.tribeRepository = tribeRepository;
    }

    /**
     * Creates a new tribe.
     * Performs validation to ensure the tribe name is unique.
     * The joinCode is automatically generated by the Tribe entity's constructor.
     * @param tribe The Tribe object to create.
     * @return The saved Tribe object with its generated ID and joinCode.
     * @throws IllegalArgumentException if the tribe name already exists.
     */
    public Tribe createTribe(final Tribe tribe) {
        // Basic validation: Check if tribe name already exists
        if (tribe.getName() != null && tribeRepository.findByName(tribe.getName()).isPresent()) {
            throw new IllegalArgumentException("Tribe name already exists: " + tribe.getName());
        }
        // No need to check joinCode uniqueness here, as it's generated.
        // The database's unique constraint will catch collisions, which should be rare for UUID-based codes.
        // In a production app, you might add retry logic for joinCode generation if a collision occurs.

        return tribeRepository.save(tribe); // Save the tribe to the database
    }

    /**
     * Retrieves a tribe by its ID.
     * @param id The ID of the tribe to retrieve.
     * @return An Optional containing the Tribe if found, or empty if not found.
     */
    public Optional<Tribe> getTribeById(final Long id) {
        return tribeRepository.findById(id);
    }

    /**
     * Retrieves a tribe by its name.
     * @param name The name of the tribe to retrieve.
     * @return An Optional containing the Tribe if found, or empty if not found.
     */
    public Optional<Tribe> getTribeByName(final String name) {
        return tribeRepository.findByName(name);
    }

    /**
     * Retrieves a tribe by its join code.
     * @param joinCode The join code of the tribe to retrieve.
     * @return An Optional containing the Tribe if found, or empty if not found.
     */
    public Optional<Tribe> getTribeByJoinCode(final String joinCode) {
        return tribeRepository.findByJoinCode(joinCode);
    }

    /**
     * Retrieves all tribes.
     * @return A list of all Tribes.
     */
    public List<Tribe> getAllTribes() {
        return tribeRepository.findAll();
    }

    /**
     * Updates an existing tribe.
     * @param tribe The Tribe object with updated information.
     * @return The updated Tribe object.
     * @throws IllegalArgumentException if the updated tribe name already exists for another tribe.
     */
    public Tribe updateTribe(final Tribe tribe) {
        // Ensure the ID is present for an update
        if (tribe.getId() == null) {
            throw new IllegalArgumentException("Tribe ID must be provided for update.");
        }

        // Check for duplicate name, excluding the current tribe itself
        Optional<Tribe> existingTribeWithName = tribeRepository.findByName(tribe.getName());
        if (existingTribeWithName.isPresent() && !existingTribeWithName.get().getId().equals(tribe.getId())) {
            throw new IllegalArgumentException("Tribe name already exists: " + tribe.getName());
        }

        return tribeRepository.save(tribe); // Save the updated tribe
    }

    /**
     * Deletes a tribe by its ID.
     * @param id The ID of the tribe to delete.
     */
    public void deleteTribe(final Long id) {
        tribeRepository.deleteById(id);
    }
}