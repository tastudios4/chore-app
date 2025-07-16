package com.mychoreapp.chore_system_backend.repository;

import com.mychoreapp.chore_system_backend.model.Tribe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
    
/**
 * Repository interface for Tribe entities.
 * Extends JpaRepository to provide basic CRUD operations and more.
 * Spring Data JPA automatically generates the implementation for this interface at runtime.
 */
@Repository
public interface ITribeRepository extends JpaRepository<Tribe, Long> {

    /**
     * Finds a Tribe by its name.
     * @param name The name to search for.
     * @return An Optional containing the Tribe if found, or empty if not found.
     */
    Optional<Tribe> findByName(final String name);

    /**
     * Finds a Tribe by its join code.
     * @param joinCode The join code to search for.
     * @return An Optional containing the Tribe if found, or empty if not found.
     */
    Optional<Tribe> findByJoinCode(final String joinCode);

    /**
     * Finds all Tribes.
     * @return A list of all Tribes.
     */
    List<Tribe> findAll();

}