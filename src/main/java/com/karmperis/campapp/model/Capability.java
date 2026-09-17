package com.karmperis.campapp.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * JPA entity representing a capability/permission with a unique name and
 * optional description. Extends {@link AbstractUuidEntity} to inherit UUID,
 * auditing timestamps and soft-delete support.
 */

@Entity
@Table(name = "capabilities")
@Getter
@Setter
@NoArgsConstructor
public class Capability extends AbstractUuidEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "capabilities", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();

    /**
     * Return an unmodifiable set of roles that have this capability.
     *
     * @return an immutable copy of the roles set
     */
    public Set<Role> getAllRoles() {
        return Set.copyOf(roles);
    }

    /**
     * Adds a role to this capability and keeps the bidirectional relationship in sync.
     *
     * @param role role to add
     */
    public void addRole(Role role) {
        roles.add(role);
        role.getCapabilities().add(this);
    }

    /**
     * Removes a role from this capability and keeps the bidirectional relationship in sync.
     *
     * @param role role to remove
     */
    public void removeRole(Role role) {
        roles.remove(role);
        role.getCapabilities().remove(this);
    }
}