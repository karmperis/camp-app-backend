package com.karmperis.campapp.repository;

import com.karmperis.campapp.model.Capability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Capability entities.
 */

public interface CapabilityRepository extends JpaRepository<Capability, Long> {

    /**
     * Finds a non soft-deleted capability by its UUID.
     *
     * @param uuid The UUID of the capability to find
     * @return An optional containing the capability if found and not soft-deleted
     */
    Optional<Capability> findByUuidAndDeletedAtIsNull(UUID uuid);

    /**
     * Finds a non soft-deleted capability by its name.
     *
     * @param name The name of the capability to find
     * @return An optional containing the capability if found and not soft-deleted
     */
    Optional<Capability> findByNameAndDeletedAtIsNull(String name);

    /**
     * Finds all non soft-deleted capabilities, ordered by name in ascending order.
     *
     * @return A list of all non soft-deleted capabilities, ordered by name in ascending order
     */
    List<Capability> findAllByAndDeletedAtIsNullOrderByNameAsc();

    /**
     * Finds all soft-deleted capabilities, ordered by name in ascending order.
     *
     * @return A list of all soft-deleted capabilities, ordered by name in ascending order
     */
    List<Capability> findAllByAndDeletedAtIsNotNullOrderByNameAsc();

    /**
     * Checks whether a capability name is already used, including soft-deleted capabilities.
     *
     * @param name The name of the capability to check
     * @return true if the name is already used, false otherwise
     */
    boolean existsByName(String name);
}