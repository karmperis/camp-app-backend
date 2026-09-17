package com.karmperis.campapp.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a role with a unique name and associated capabilities.
 * Extends {@link AbstractUuidEntity} to inherit UUID, auditing timestamps and
 * soft-delete support.
 */

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role extends AbstractUuidEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "roles_capabilities",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "capability_id")
    )
    private Set<Capability> capabilities = new HashSet<>();

    /**
     * Return an unmodifiable set of this role's capabilities.
     *
     * @return an immutable copy of the capabilities set
     */
    public Set<Capability> getAllCapabilities() {
        return Set.copyOf(capabilities);
    }

    /**
     * Checks whether this role already contains the given capability.
     *
     * @param capability capability to check
     * @return true if the capability is already assigned to this role
     */
    public boolean hasCapability(Capability capability) {
        return capabilities.contains(capability);
    }

    /**
     * Adds a capability to this role and keeps the bidirectional relationship in sync.
     *
     * @param capability capability to add
     */
    public void addCapability(Capability capability) {
        capabilities.add(capability);
        capability.getRoles().add(this);
    }

    /**
     * Removes a capability from this role and keeps the bidirectional relationship in sync.
     *
     * @param capability capability to remove
     */
    public void removeCapability(Capability capability) {
        capabilities.remove(capability);
        capability.getRoles().remove(this);
    }
}