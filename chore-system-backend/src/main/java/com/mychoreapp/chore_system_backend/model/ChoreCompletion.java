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

import java.time.LocalDateTime; // To record the exact date and time of completion

/**
 * Represents a record of a Chore being completed by a User.
 * This entity tracks who completed which chore and when, and how many points were awarded.
 */
@Entity
@Table(name = "chore_completions")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ChoreCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Many ChoreCompletions refer to One Chore
    @JoinColumn(name = "chore_id", nullable = false) // Foreign key to the chores table, a completion must be for a chore
    private Chore chore; // The Chore that was completed

    @ManyToOne // Many ChoreCompletions are completed by One User
    @JoinColumn(name = "completed_by_user_id", nullable = false) // Foreign key to the users table, a completion must have a user
    private User completedBy; // The User who completed the chore

    @Column(nullable = false) // Completion date/time cannot be null
    private LocalDateTime completionDate; // The exact date and time the chore was completed

    @Column(nullable = false) // Points awarded cannot be null
    private int pointsAwarded; // The points awarded for this specific completion (derived from Chore's pointsValue)

    /**
     * Constructor for creating a new ChoreCompletion record.
     * @param chore The Chore that was completed.
     * @param completedBy The User who completed the chore.
     * @param pointsAwarded The points awarded for this completion.
     */
    public ChoreCompletion(final Chore chore, final User completedBy, final int pointsAwarded) {
        this.chore = chore;
        this.completedBy = completedBy;
        this.pointsAwarded = pointsAwarded;
        this.completionDate = LocalDateTime.now(); // Automatically set to current time upon creation
    }

    /**
     * Constructor for creating a new ChoreCompletion record with a specific completion date.
     * @param chore The Chore that was completed.
     * @param completedBy The User who completed the chore.
     * @param pointsAwarded The points awarded for this completion.
     * @param completionDate The specific date and time of completion.
     */
    public ChoreCompletion(final Chore chore, final User completedBy, final int pointsAwarded, final LocalDateTime completionDate) {
        this.chore = chore;
        this.completedBy = completedBy;
        this.pointsAwarded = pointsAwarded;
        this.completionDate = completionDate;
    }
}
