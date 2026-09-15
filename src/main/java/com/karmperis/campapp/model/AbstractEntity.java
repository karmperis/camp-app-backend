package com.karmperis.campapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base mapped superclass that provides a primary key, auditing timestamps
 * (createdAt, updatedAt) and soft-delete support (deletedAt).
 */

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(6)")
    private Instant updatedAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME(6)")
    @Setter(AccessLevel.PROTECTED)
    private Instant deletedAt;

    /**
     * Mark the entity as deleted by setting {@code deletedAt} to the current time.
     * This performs a soft-delete.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Returns whether the entity has been soft-deleted.
     *
     * @return {@code true} if {@code deletedAt} is non-null, otherwise {@code false}
     */
    @Transient
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}