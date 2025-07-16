package com.mychoreapp.chore_system_backend.repository;

import com.mychoreapp.chore_system_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for User entities.
 * Extends JpaRepository to provide basic CRUD operations and more.
 * Spring Data JPA automatically generates the implementation for this interface at runtime.
 * 
 * the entity type this repository will manage is User and the type of the primary key is Long id
 */
@Repository // Marks this interface as a Spring Data JPA repository component
public interface IUserRepository extends JpaRepository<User, Long> { 

    /**
     * Finds a User by their username.
     * Spring Data JPA automatically generates the query based on the method name.
     * @param username The username to search for.
     * @return An Optional containing the User if found, or empty if not found.
     */
    Optional<User> findByUsername(final String username);

    /**
     * Finds a User by their Google ID.
     * Useful for checking if a Google SSO user already exists.
     * @param googleId The Google ID to search for.
     * @return An Optional containing the User if found, or empty if not found.
     */
    Optional<User> findByGoogleId(final String googleId);

    /**
     * Finds a User by their email address.
     * Useful for checking if a user (either basic or SSO) already exists with a given email.
     * @param email The email address to search for.
     * @return An Optional containing the User if found, or empty if not found.
     */
    Optional<User> findByEmail(final String email);
}