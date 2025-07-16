package com.mychoreapp.chore_system_backend.repository;

import com.mychoreapp.chore_system_backend.model.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Chore entities.
 * Extends JpaRepository to provide basic CRUD operations and more.
 * Spring Data JPA automatically generates the implementation for this interface at runtime.
 */
@Repository
public interface IChoreRepository extends JpaRepository<Chore, Long> {

    /**
     * Finds all chores belonging to a specific tribe.
     * @param tribeId The ID of the tribe.
     * @return A list of chores associated with the given tribe ID.
     */
    List<Chore> findByTribeId(Long tribeId);

    /**
     * Finds all active chores belonging to a specific tribe.
     * @param tribeId The ID of the tribe.
     * @param isActive A boolean indicating if the chore should be active (true) or inactive (false).
     * @return A list of active chores associated with the given tribe ID.
     */
    List<Chore> findByTribeIdAndIsActive(Long tribeId, boolean isActive);

    /**
     * Finds all chores currently assigned to a specific user.
     * @param assignedToId The ID of the user to whom chores are assigned.
     * @return A list of chores assigned to the given user ID.
     */
    List<Chore> findByAssignedToId(Long assignedToId);

    /**
     * Finds all active chores currently assigned to a specific user.
     * @param assignedToId The ID of the user to whom chores are assigned.
     * @param isActive A boolean indicating if the chore should be active (true) or inactive (false).
     * @return A list of active chores assigned to the given user ID.
     */
    List<Chore> findByAssignedToIdAndIsActive(Long assignedToId, boolean isActive);

    /**
     * Finds a chore by its name and the ID of the tribe it belongs to.
     * This can be useful for ensuring unique chore names within a tribe.
     * @param name The name of the chore.
     * @param tribeId The ID of the tribe.
     * @return An Optional containing the Chore if found, or empty if not found.
     */
    Optional<Chore> findByNameAndTribeId(String name, Long tribeId);
}