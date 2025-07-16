package com.mychoreapp.chore_system_backend.service;

import com.mychoreapp.chore_system_backend.model.Chore;
import com.mychoreapp.chore_system_backend.model.ChoreCompletion;
import com.mychoreapp.chore_system_backend.model.User;
import com.mychoreapp.chore_system_backend.repository.IChoreCompletionRepository;
import com.mychoreapp.chore_system_backend.repository.IChoreRepository;
import com.mychoreapp.chore_system_backend.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing ChoreCompletion-related business logic.
 * Handles recording chore completions, awarding points, and retrieving completion records.
 * Now includes logic for handling recurring chores.
 * a new instance is created for the next cycle, and the original completed instance is set to inactive.
 */
@Service
public class ChoreCompletionService {

    private final IChoreCompletionRepository choreCompletionRepository;
    private final IChoreRepository choreRepository;
    private final IUserRepository userRepository;

    /**
     * Constructor for dependency injection.
     * Spring automatically injects instances of the required repositories.
     * @param choreCompletionRepository The chore completion repository to be injected.
     * @param choreRepository The chore repository to be injected.
     * @param userRepository The user repository to be injected.
     */
    @Autowired
    public ChoreCompletionService(
            final IChoreCompletionRepository choreCompletionRepository,
            final IChoreRepository choreRepository,
            final IUserRepository userRepository) {
        this.choreCompletionRepository = choreCompletionRepository;
        this.choreRepository = choreRepository;
        this.userRepository = userRepository;
    }

    /**
     * Records a chore completion and awards points to the completing user.
     * This operation is transactional to ensure atomicity (either both completion is saved and points are updated, or neither).
     * For recurring chores, a new instance of the chore is created for the next cycle,
     * and the original completed chore instance is marked as inactive.
     * @param choreId The ID of the chore that was completed.
     * @param completedByUserId The ID of the user who completed the chore.
     * @return The created ChoreCompletion record.
     * @throws IllegalArgumentException if the chore or user is not found, or if the user is not in the chore's tribe.
     */
    @Transactional
    public ChoreCompletion recordChoreCompletion(final Long choreId, final Long completedByUserId) {
        // Validate Chore existence
        final Chore chore = validateChore(choreId);

        // Validate User existence
        final User user = validateUser(completedByUserId);

        // Validate that the user belongs to the same tribe as the chore
        if (user.getTribe() == null || !user.getTribe().getId().equals(chore.getTribe().getId())) {
            throw new IllegalArgumentException("User must belong to the same tribe as the chore to complete it.");
        }

        // Create ChoreCompletion record
        final ChoreCompletion completion = new ChoreCompletion(chore, user, chore.getPointsValue());
        final ChoreCompletion savedCompletion = choreCompletionRepository.save(completion);

        // Award points to the user
        updateUserPoints(user, chore.getPointsValue());

        // Handle Recurring Chores: Create a new instance for the next cycle
        handleRecurringChore(chore);

        return savedCompletion;
    }

    /**
     * Retrieves a chore completion record by its ID.
     * @param id The ID of the chore completion record to retrieve.
     * @return An Optional containing the ChoreCompletion if found, or empty if not found.
     */
    public Optional<ChoreCompletion> getChoreCompletionById(final Long id) {
        return choreCompletionRepository.findById(id);
    }

    /**
     * Retrieves all chore completion records for a specific user.
     * @param userId The ID of the user.
     * @return A list of ChoreCompletion records completed by the specified user.
     */
    public List<ChoreCompletion> getChoreCompletionsByUser(final Long userId) {
        return choreCompletionRepository.findByCompletedBy_Id(userId);
    }

    /**
     * Retrieves all chore completion records for a specific chore.
     * @param choreId The ID of the chore.
     * @return A list of ChoreCompletion records for the specified chore.
     */
    public List<ChoreCompletion> getChoreCompletionsByChore(final Long choreId) {
        return choreCompletionRepository.findByChore_Id(choreId);
    }

    /**
     * Retrieves all chore completion records for a specific tribe within a given date range.
     * @param tribeId The ID of the tribe.
     * @param startDate The start date/time for the search range.
     * @param endDate The end date/time for the search range.
     * @return A list of ChoreCompletion records for the given tribe within the date range.
     */
    public List<ChoreCompletion> getChoreCompletionsByTribeAndDateRange(final Long tribeId, final LocalDateTime startDate, final LocalDateTime endDate) {
        return choreCompletionRepository.findByChore_Tribe_IdAndCompletionDateBetween(tribeId, startDate, endDate);
    }

    /**
     * Retrieves all chore completion records for a specific user within a given date range.
     * @param userId The ID of the user.
     * @param startDate The start date/time for the search range.
     * @param endDate The end date/time for the search range.
     * @return A list of ChoreCompletion records by the given user within the date range.
     */
    public List<ChoreCompletion> getChoreCompletionsByUserAndDateRange(final Long userId, final LocalDateTime startDate, final LocalDateTime endDate) {
        return choreCompletionRepository.findByCompletedBy_IdAndCompletionDateBetween(userId, startDate, endDate);
    }

    /**
     * Retrieves all chore completion records in the system (for administrative purposes).
     * @return A list of all ChoreCompletion objects.
     */
    public List<ChoreCompletion> getAllChoreCompletions() {
        return choreCompletionRepository.findAll();
    }

    /**
     * Deletes a chore completion record by its ID.
     * Note: Deleting a completion record does NOT automatically deduct points from the user.
     * If point deduction is required, it must be handled separately.
     * @param id The ID of the chore completion record to delete.
     */
    public void deleteChoreCompletion(final Long id) {
        choreCompletionRepository.deleteById(id);
    }

    /**
     * Validates the existence of a chore.
     * @param choreId The ID of the chore to validate.
     * @return The Chore object if found, otherwise throws an exception.
     */
    private Chore validateChore(final Long choreId) {
        Optional<Chore> choreOptional = choreRepository.findById(choreId);
        if (choreOptional.isEmpty()) {
            throw new IllegalArgumentException("Chore with ID " + choreId + " not found.");
        }
        return choreOptional.get();
    }

    /**
     * Validates the existence of a user.
     * @param userId The ID of the user to validate.
     * @return The User object if found, otherwise throws an exception.
     */
    private User validateUser(final Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }
        return userOptional.get();
    }

    /**
     * Updates the points of a user.
     * @param user The User object to update.
     * @param points The number of points to add to the user.
     */
    private void updateUserPoints(final User user, final int points) {
        user.addPoints(points);
        userRepository.save(user);
    }

    /**
     * Handles the creation of a new instance of a recurring chore.
     * @param chore The Chore object to handle.
     * @return The new Chore object if created, otherwise null.
     */
    private void handleRecurringChore(final Chore chore) {
        if (chore.isRecurring()) {
            // Create a new Chore object based on the original chore's properties
            final Chore nextChoreInstance = new Chore(
                chore.getName(),
                chore.getDescription(),
                chore.getPointsValue(),
                chore.getTribe()
            );

            // Calculate the next due date
            final LocalDate nextDueDate = calculateNextDueDate(chore.getDueDate(), chore.getRecurrencePattern());
            nextChoreInstance.setDueDate(nextDueDate);

            // Copy recurrence properties to the new instance
            nextChoreInstance.setRecurring(true);
            nextChoreInstance.setRecurrencePattern(chore.getRecurrencePattern());

            // Optionally, unassign the new instance or assign it based on a rotation logic
            nextChoreInstance.setAssignedTo(null); // Unassign by default for next cycle

            // Ensure the new instance is active
            nextChoreInstance.setActive(true);

            // Save the new chore instance
            choreRepository.save(nextChoreInstance);

            // Set the original chore instance to inactive, so it disappears from active lists
            chore.setActive(false);
            choreRepository.save(chore);
        }
    }

    /**
     * Helper method to calculate the next due date for a recurring chore.
     * If the current due date is null, it starts from today.
     * @param currentDueDate The current due date of the chore.
     * @param recurrencePattern The recurrence pattern (e.g., "DAILY", "WEEKLY", "MONTHLY").
     * @return The calculated next due date.
     */
    private LocalDate calculateNextDueDate(final LocalDate currentDueDate, final String recurrencePattern) {
        // If no current due date is set, start from today
        final LocalDate newDueDate = currentDueDate == null ? LocalDate.now() : currentDueDate;

        if (recurrencePattern == null) {
            // If recurrence pattern is null, it's not truly recurring in a defined way.
            // For safety, just return the current date or throw an error.
            // For this implementation, we'll assume it's handled by isRecurring check.
            return newDueDate;
        }

        switch (recurrencePattern.toUpperCase()) {
            case "DAILY":
                return newDueDate.plusDays(1);
            case "WEEKLY":
                return newDueDate.plusWeeks(1);
            case "MONTHLY":
                return newDueDate.plusMonths(1);
            case "YEARLY":
                return newDueDate.plusYears(1);
            // Add more cases as needed (e.g., "BI-WEEKLY", "FORTNIGHTLY")
            default:
                // If an unrecognized pattern, log a warning and default to daily or throw an exception
                System.err.println("Unrecognized recurrence pattern: " + recurrencePattern + ". Defaulting to daily.");
                return currentDueDate.plusDays(1);
        }
    }
}