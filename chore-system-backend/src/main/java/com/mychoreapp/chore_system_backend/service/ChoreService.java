package com.mychoreapp.chore_system_backend.service;

import com.mychoreapp.chore_system_backend.model.Chore;
import com.mychoreapp.chore_system_backend.model.Tribe;
import com.mychoreapp.chore_system_backend.model.User;
import com.mychoreapp.chore_system_backend.repository.IChoreRepository;
import com.mychoreapp.chore_system_backend.repository.ITribeRepository;
import com.mychoreapp.chore_system_backend.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Chore-related business logic.
 * It interacts with IChoreRepository, ITribeRepository, and IUserRepository
 * to perform database operations and enforce business rules.
 */
@Service
public class ChoreService {

    private final IChoreRepository choreRepository;
    private final ITribeRepository tribeRepository;
    private final IUserRepository userRepository;

    /**
     * Constructor for dependency injection.
     * Spring automatically injects instances of the required repositories.
     * @param choreRepository The chore repository to be injected.
     * @param tribeRepository The tribe repository to be injected.
     * @param userRepository The user repository to be injected.
     */
    @Autowired
    public ChoreService(final IChoreRepository choreRepository, final ITribeRepository tribeRepository, final IUserRepository userRepository) {
        this.choreRepository = choreRepository;
        this.tribeRepository = tribeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new chore within a specific tribe.
     * @param chore The Chore object to create.
     * @param tribeId The ID of the tribe this chore belongs to.
     * @return The saved Chore object.
     * @throws IllegalArgumentException if the tribe does not exist, or if a chore with the same name already exists in the tribe.
     */
    public Chore createChore(final Chore chore, final Long tribeId) {
        // Validate tribe existence
        Optional<Tribe> tribeOptional = tribeRepository.findById(tribeId);
        if (tribeOptional.isEmpty()) {
            throw new IllegalArgumentException("Tribe with ID " + tribeId + " not found.");
        }
        Tribe tribe = tribeOptional.get();
        chore.setTribe(tribe); // Associate the chore with the found tribe

        // Validate unique chore name within the tribe
        if (chore.getName() != null && choreRepository.findByNameAndTribeId(chore.getName(), tribeId).isPresent()) {
            throw new IllegalArgumentException("A chore with the name '" + chore.getName() + "' already exists in this tribe.");
        }

        // Validate pointsValue
        if (chore.getPointsValue() <= 0) {
            throw new IllegalArgumentException("Chore points value must be positive.");
        }

        // Handle assignedTo if provided, using the new helper method for decomposition
        if (chore.getAssignedTo() != null && chore.getAssignedTo().getId() != null) {
            chore.setAssignedTo(validateAndGetAssignedUser(chore.getAssignedTo().getId(), tribeId));
        } else {
            chore.setAssignedTo(null); // Ensure assignedTo is null if not explicitly set or invalid
        }

        return choreRepository.save(chore);
    }

    private void validateTribe(final Long tribeId) {
        // Validate tribe existence
        Optional<Tribe> tribeOptional = tribeRepository.findById(tribeId);
        if (tribeOptional.isEmpty()) {
            throw new IllegalArgumentException("Tribe with ID " + tribeId + " not found.");
        }
        Tribe tribe = tribeOptional.get();
    }

    /**
     * Retrieves a chore by its ID.
     * @param id The ID of the chore to retrieve.
     * @return An Optional containing the Chore if found, or empty if not found.
     */
    public Optional<Chore> getChoreById(final Long id) {
        return choreRepository.findById(id);
    }

    /**
     * Retrieves all chores for a specific tribe.
     * @param tribeId The ID of the tribe.
     * @return A list of chores belonging to the specified tribe.
     */
    public List<Chore> getChoresByTribe(final Long tribeId) {
        return choreRepository.findByTribeId(tribeId);
    }

    /**
     * Retrieves all active chores for a specific tribe.
     * @param tribeId The ID of the tribe.
     * @return A list of active chores belonging to the specified tribe.
     */
    public List<Chore> getActiveChoresByTribe(final Long tribeId) {
        return choreRepository.findByTribeIdAndIsActive(tribeId, true);
    }

    /**
     * Retrieves all chores assigned to a specific user.
     * @param userId The ID of the user.
     * @return A list of chores assigned to the specified user.
     */
    public List<Chore> getChoresAssignedToUser(final Long userId) {
        return choreRepository.findByAssignedToId(userId);
    }

    /**
     * Retrieves all active chores assigned to a specific user.
     * @param userId The ID of the user.
     * @return A list of active chores assigned to the specified user.
     */
    public List<Chore> getActiveChoresAssignedToUser(final Long userId) {
        return choreRepository.findByAssignedToIdAndIsActive(userId, true);
    }

    /**
     * Retrieves all chores in the system (for administrative purposes).
     * @return A list of all Chore objects.
     */
    public List<Chore> getAllChores() {
        return choreRepository.findAll();
    }

    /**
     * Updates an existing chore.
     * @param id The ID of the chore to update.
     * @param updatedChore The Chore object with updated information.
     * @return The updated Chore object.
     * @throws IllegalArgumentException if the chore ID is missing, the tribe does not exist,
     * or if a chore with the same name already exists in the same tribe (for a different chore).
     */
    public Chore updateChore(final Long id, final Chore updatedChore) {
        Optional<Chore> existingChoreOptional = choreRepository.findById(id);
        if (existingChoreOptional.isEmpty()) {
            throw new IllegalArgumentException("Chore with ID " + id + " not found for update.");
        }
        Chore existingChore = existingChoreOptional.get();

        // Ensure the ID in the path matches the ID in the body
        if (!existingChore.getId().equals(updatedChore.getId())) {
            throw new IllegalArgumentException("ID in path does not match ID in request body.");
        }

        // Validate tribe existence if tribe is being updated (though typically tribe is fixed)
        // Or ensure the existing chore's tribe is maintained if not explicitly changed.
        if (updatedChore.getTribe() == null || updatedChore.getTribe().getId() == null) {
            throw new IllegalArgumentException("Tribe must be specified for a chore.");
        }
        Optional<Tribe> tribeOptional = tribeRepository.findById(updatedChore.getTribe().getId());
        if (tribeOptional.isEmpty()) {
            throw new IllegalArgumentException("Tribe with ID " + updatedChore.getTribe().getId() + " not found.");
        }
        existingChore.setTribe(tribeOptional.get()); // Update tribe reference

        // Validate unique chore name within the tribe, excluding the current chore itself
        Optional<Chore> choreWithSameName = choreRepository.findByNameAndTribeId(updatedChore.getName(), updatedChore.getTribe().getId());
        if (choreWithSameName.isPresent() && !choreWithSameName.get().getId().equals(id)) {
            throw new IllegalArgumentException("A chore with the name '" + updatedChore.getName() + "' already exists in this tribe.");
        }

        // Update basic fields
        existingChore.setName(updatedChore.getName());
        existingChore.setDescription(updatedChore.getDescription());
        existingChore.setPointsValue(updatedChore.getPointsValue());
        existingChore.setDueDate(updatedChore.getDueDate());
        existingChore.setRecurring(updatedChore.isRecurring());
        existingChore.setRecurrencePattern(updatedChore.getRecurrencePattern());
        existingChore.setActive(updatedChore.isActive());

        // Handle assignedTo update using the new helper method for decomposition
        if (updatedChore.getAssignedTo() != null && updatedChore.getAssignedTo().getId() != null) {
            existingChore.setAssignedTo(validateAndGetAssignedUser(updatedChore.getAssignedTo().getId(), existingChore.getTribe().getId()));
        } else {
            existingChore.setAssignedTo(null); // Unassign if assignedTo is null or invalid
        }

        return choreRepository.save(existingChore);
    }

    /**
     * Deletes a chore by its ID.
     * @param id The ID of the chore to delete.
     */
    public void deleteChore(final Long id) {
        choreRepository.deleteById(id);
    }

    /**
     * Assigns a chore to a specific user.
     * This method fetches the chore and user, validates, and persists the assignment.
     * @param choreId The ID of the chore to assign.
     * @param userId The ID of the user to assign the chore to.
     * @return The updated Chore object.
     * @throws IllegalArgumentException if chore or user not found, or user is not in the chore's tribe.
     */
    public Chore assignChore(final Long choreId, final Long userId) {
        Optional<Chore> choreOptional = choreRepository.findById(choreId);
        if (choreOptional.isEmpty()) {
            throw new IllegalArgumentException("Chore with ID " + choreId + " not found.");
        }
        Chore chore = choreOptional.get();

        // Use the helper method for user validation
        User user = validateAndGetAssignedUser(userId, chore.getTribe().getId());

        chore.setAssignedTo(user);
        return choreRepository.save(chore);
    }

    /**
     * Unassigns a chore from any user it's currently assigned to.
     * @param choreId The ID of the chore to unassign.
     * @return The updated Chore object.
     * @throws IllegalArgumentException if chore not found.
     */
    public Chore unassignChore(final Long choreId) {
        Optional<Chore> choreOptional = choreRepository.findById(choreId);
        if (choreOptional.isEmpty()) {
            throw new IllegalArgumentException("Chore with ID " + choreId + " not found.");
        }
        Chore chore = choreOptional.get();

        chore.setAssignedTo(null); // Set assignedTo to null to unassign
        return choreRepository.save(chore);
    }

    /**
     * Helper method to validate an assigned user and ensure they belong to the specified tribe.
     * @param userId The ID of the user to validate.
     * @param choreTribeId The ID of the tribe the chore belongs to.
     * @return The validated User object.
     * @throws IllegalArgumentException if the user is not found or does not belong to the chore's tribe.
     */
    private User validateAndGetAssignedUser(final Long userId, final Long choreTribeId) {
        Optional<User> assignedUserOptional = userRepository.findById(userId);
        if (assignedUserOptional.isEmpty()) {
            throw new IllegalArgumentException("Assigned user with ID " + userId + " not found.");
        }
        User assignedUser = assignedUserOptional.get();
        // Ensure the assigned user belongs to the same tribe as the chore
        if (assignedUser.getTribe() == null || !assignedUser.getTribe().getId().equals(choreTribeId)) {
            throw new IllegalArgumentException("Assigned user does not belong to the chore's tribe.");
        }
        return assignedUser;
    }
}