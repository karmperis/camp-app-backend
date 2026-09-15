package com.karmperis.campapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

/**
 * Base mapped superclass that provides a UUID identifier in addition to
 * the auditing and soft-delete fields inherited from {@link AbstractEntity}.
 * The UUID is generated if missing before persisting.
 */

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractUuidEntity extends AbstractEntity {

    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    private UUID uuid = UUID.randomUUID();

    /**
     * Ensure a UUID is assigned before the entity is persisted.
     * If {@code uuid} is null it will be set to a newly generated random UUID.
     */
    @PrePersist
    protected void onCreateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    /**
     * Equality is based on the {@code uuid} field. Two entities are considered
     * equal when their UUIDs are equal.
     *
     * @param o the object to compare
     * @return {@code true} if the given object is an {@code AbstractUuidEntity}
     * with the same UUID, otherwise {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractUuidEntity that)) return false;
        return Objects.equals(this.getUuid(), that.getUuid());
    }

    /**
     * Compute hash code using the entity UUID.
     *
     * @return hash code derived from {@code uuid}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }
}