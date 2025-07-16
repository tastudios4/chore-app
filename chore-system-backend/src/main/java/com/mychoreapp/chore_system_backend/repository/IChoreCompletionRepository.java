package com.mychoreapp.chore_system_backend.repository;

import com.mychoreapp.chore_system_backend.model.ChoreCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ChoreCompletion entities.
 * Extends JpaRepository to provide basic CRUD operations and more.
 * Spring Data JPA automatically generates the implementation for this interface at runtime.
 */
@Repository // Marks this interface as a Spring Data JPA repository component
public interface IChoreCompletionRepository extends JpaRepository<ChoreCompletion, Long> {

    /**
     * Finds all chore completion records for a specific user.
     * @param completedByUserId The ID of the user who completed the chores.
     * @return A list of ChoreCompletion records by the given user ID.
     */
    List<ChoreCompletion> findByCompletedBy_Id(Long completedByUserId);

    /**
     * Finds all chore completion records for a specific chore.
     * @param choreId The ID of the chore that was completed.
     * @return A list of ChoreCompletion records for the given chore ID.
     */
    List<ChoreCompletion> findByChore_Id(Long choreId);

    /**
     * Finds all chore completion records for a specific tribe within a given time range.
     * This method assumes that ChoreCompletion has a direct link to Chore, and Chore has a link to Tribe.
     * Spring Data JPA can traverse these relationships.
     * @param tribeId The ID of the tribe.
     * @param startDate The start date/time for the search range.
     * @param endDate The end date/time for the search range.
     * @return A list of ChoreCompletion records for the given tribe within the date range.
     */
    List<ChoreCompletion> findByChore_Tribe_IdAndCompletionDateBetween(Long tribeId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds chore completion records for a specific user within a given time range.
     * @param completedByUserId The ID of the user who completed the chores.
     * @param startDate The start date/time for the search range.
     * @param endDate The end date/time for the search range.
     * @return A list of ChoreCompletion records by the given user within the date range.
     */
    List<ChoreCompletion> findByCompletedBy_IdAndCompletionDateBetween(Long completedByUserId, LocalDateTime startDate, LocalDateTime endDate);
}
