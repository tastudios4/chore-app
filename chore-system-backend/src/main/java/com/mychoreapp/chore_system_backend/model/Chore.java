package com.mychoreapp.chore_system_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Represents a Chore (task) that needs to be completed within a Tribe.
 * Chores have a name, description, point value, and can be recurring.
 */
@Entity // Marks this class as a JPA entity
@Table(name = "chores") // Explicitly maps this entity to a database table named 'chores'
@Getter // Lombok will automatically generate all getter methods
@Setter // Lombok will automatically generate all setter methods
@NoArgsConstructor // Lombok will automatically generate the default no-argument constructor
@ToString // Lombok will generate a toString() method
public class Chore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generated ID
    private Long id;

    @Column(nullable = false) 
    private String name; 

    @Column(columnDefinition = "TEXT") // Use TEXT for potentially longer descriptions
    private String description; // More details about the chore (nullable)

    @Column(nullable = false) 
    private int pointsValue; // How many points a user gets for completing this chore

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate; // When the chore should ideally be completed (nullable)

    @Column(nullable = false) // Recurring flag cannot be null, default to false
    @JsonProperty("isRecurring")
    private boolean isRecurring; // Whether this chore repeats

    private String recurrencePattern; // If recurring, how often (e.g., "DAILY", "WEEKLY", "MONTHLY") (nullable)

    @Column(nullable = false) // Active flag cannot be null, default to true
    private boolean isActive = true; // Whether the chore is currently active/assignable

    // --- Relationships ---

    @ManyToOne // Many Chores belong to One Tribe
    @JoinColumn(name = "tribe_id", nullable = false) // Foreign key to the tribes table, a chore must belong to a tribe
    private Tribe tribe; // The Tribe this chore belongs to

    @ManyToOne // Many Chores can be assigned to One User
    @JoinColumn(name = "assigned_user_id", nullable = true) // Foreign key to the users table, a chore can be unassigned
    private User assignedTo; // The User this chore is currently assigned to (nullable)

    /**
     * Constructor for creating a new Chore.
     * @param name The name of the chore.
     * @param description A description of the chore.
     * @param pointsValue The points awarded for completing the chore.
     * @param tribe The tribe this chore belongs to.
     */
    public Chore(final String name, 
                final String description, 
                final int pointsValue, 
                final Tribe tribe) {
        this.name = name;
        this.description = description;
        this.pointsValue = pointsValue;
        this.tribe = tribe;
        this.isActive = true;
        this.dueDate = null;
        this.recurrencePattern = null;
        this.assignedTo = null;
    }

    /**
     * Constructor for creating a new Chore with a due date.
     * @param name The name of the chore.
     * @param description A description of the chore.
     * @param pointsValue The points awarded for completing the chore.
     * @param dueDate The due date for the chore.
     * @param tribe The tribe this chore belongs to.
     */
    public Chore(final String name, 
                final String description, 
                final int pointsValue, 
                final LocalDate dueDate, 
                final Tribe tribe) {
        this(name, description, pointsValue, tribe);
        this.dueDate = dueDate;
    }

    /**
     * Constructor for creating a new recurring Chore.
     * @param name The name of the chore.
     * @param description A description of the chore.
     * @param pointsValue The points awarded for completing the chore.
     * @param isRecurring True if the chore recurs.
     * @param recurrencePattern How often the chore recurs (e.g., "DAILY").
     * @param tribe The tribe this chore belongs to.
     */
    public Chore(final String name, 
                final String description, 
                final int pointsValue, 
                final boolean isRecurring, 
                final String recurrencePattern, 
                final Tribe tribe) {
        this(name, description, pointsValue, tribe);
        this.isRecurring = isRecurring;
        this.recurrencePattern = recurrencePattern;
    }
}